package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteEntity
import com.example.data.local.PlaybackHistoryEntity
import com.example.data.local.ServerEntity
import com.example.data.model.AuthenticateByNameRequest
import com.example.data.model.BaseItemDto
import com.example.data.model.JellyfinLibraryView
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.PublicSystemInfo
import com.example.data.model.PublicUserDto
import com.example.data.model.QuickConnectAuthRequest
import com.example.data.model.QuickConnectInitiateResponse
import com.example.data.remote.DemoMediaCatalog
import com.example.data.remote.JellyfinClientFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.example.data.local.CachedLibraryViewEntity
import com.example.data.local.CachedMediaItemEntity
import com.example.data.local.toCachedEntity
import com.example.data.local.toLibraryView
import com.example.data.local.toMediaItem
import kotlinx.coroutines.flow.map
import java.util.UUID

class JellyfinRepository(private val database: AppDatabase) {

    private val serverDao = database.serverDao()
    private val favoriteDao = database.favoriteDao()
    private val playbackHistoryDao = database.playbackHistoryDao()
    private val mediaCacheDao = database.mediaCacheDao()
    private val libraryViewCacheDao = database.libraryViewCacheDao()

    val allServers: Flow<List<ServerEntity>> = serverDao.getAllServers()
    val activeServer: Flow<ServerEntity?> = serverDao.getActiveServer()

    suspend fun ensureDefaultServer() = withContext(Dispatchers.IO) {
        val active = serverDao.getActiveServer().firstOrNull()
        val targetServer = if (active == null) {
            val demo = ServerEntity(
                id = DemoMediaCatalog.demoServerId,
                name = "Jellyfin Demo Cloud",
                url = "https://demo.jellyfin.org/stable",
                userId = "demo_user",
                userName = "Demo Guest",
                accessToken = "demo_token_xyz",
                isDemo = true,
                isActive = true,
                serverVersion = "10.10.6"
            )
            serverDao.insertServer(demo)
            demo
        } else {
            active
        }

        // Pre-populate Room cache with catalog items and library folders if cache is empty
        if (targetServer.isDemo && mediaCacheDao.getCacheCount(targetServer.id) == 0) {
            val demoItems = DemoMediaCatalog.getAllItems()
            mediaCacheDao.insertMediaItems(demoItems.map { it.toCachedEntity(targetServer.id) })

            val demoViews = listOf(
                JellyfinLibraryView(id = "all", name = "All Libraries", collectionType = null, type = null, itemCount = demoItems.size),
                JellyfinLibraryView(id = "movies", name = "Movies", collectionType = "movies", type = MediaType.MOVIE, itemCount = demoItems.count { it.type == MediaType.MOVIE }),
                JellyfinLibraryView(id = "shows", name = "TV Shows", collectionType = "tvshows", type = MediaType.SERIES, itemCount = demoItems.count { it.type == MediaType.SERIES }),
                JellyfinLibraryView(id = "music", name = "Music", collectionType = "music", type = MediaType.AUDIO, itemCount = demoItems.count { it.type == MediaType.AUDIO })
            )
            libraryViewCacheDao.insertViews(demoViews.map { it.toCachedEntity(targetServer.id) })
        }
    }

    /**
     * Checks if a server is online and returns its PublicSystemInfo (ServerName, Version, OS).
     */
    suspend fun pingServer(rawUrl: String): Result<PublicSystemInfo> = withContext(Dispatchers.IO) {
        val sanitizedUrl = JellyfinClientFactory.sanitizeUrl(rawUrl)
        if (sanitizedUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid server URL"))
        }

        try {
            val api = JellyfinClientFactory.createApi(sanitizedUrl)
            val response = api.getPublicSystemInfo()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Endpoint not found. Ensure this is a Jellyfin server address (remove /web if present)."
                    502, 503 -> "Server unavailable (HTTP ${response.code()}). Check if Jellyfin is running."
                    else -> "Server returned HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkException(e, sanitizedUrl))
        }
    }

    /**
     * Fetches public users if the Jellyfin server supports public profile login.
     */
    suspend fun fetchPublicUsers(rawUrl: String): Result<List<PublicUserDto>> = withContext(Dispatchers.IO) {
        val sanitizedUrl = JellyfinClientFactory.sanitizeUrl(rawUrl)
        try {
            val api = JellyfinClientFactory.createApi(sanitizedUrl)
            val response = api.getPublicUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(emptyList())
            }
        } catch (_: Exception) {
            Result.success(emptyList())
        }
    }

    /**
     * Authenticates by Username and Password with the Jellyfin server using Retrofit and OkHttp.
     * Injects both standard Authorization and X-Emby-Authorization headers with full client telemetry.
     */
    suspend fun authenticateByName(
        name: String,
        rawUrl: String,
        username: String,
        password: String
    ): Result<ServerEntity> = withContext(Dispatchers.IO) {
        val sanitizedUrl = JellyfinClientFactory.sanitizeUrl(rawUrl)
        if (sanitizedUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Server URL cannot be empty"))
        }
        val cleanUsername = username.trim()
        if (cleanUsername.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Username cannot be empty"))
        }

        try {
            val api = JellyfinClientFactory.createApi(sanitizedUrl)

            // Attempt to discover server name and version from public endpoint
            var serverVersion = "Connected"
            var resolvedName = name.trim().ifBlank { "Jellyfin Server" }
            try {
                val ping = api.getPublicSystemInfo()
                if (ping.isSuccessful && ping.body() != null) {
                    val info = ping.body()!!
                    serverVersion = "v${info.version ?: "10.x"}"
                    if (name.isBlank() && !info.serverName.isNullOrBlank()) {
                        resolvedName = info.serverName
                    }
                }
            } catch (_: Exception) {
                // Non-fatal, proceed with authentication
            }

            val request = AuthenticateByNameRequest(
                username = cleanUsername,
                pw = password,
                password = password
            )

            val authResponse = api.authenticateByName(request)

            if (authResponse.isSuccessful && authResponse.body() != null) {
                val authResult = authResponse.body()!!
                val token = authResult.accessToken
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Jellyfin server did not return a valid session token."))
                }

                val resolvedUserId = authResult.user?.id ?: authResult.sessionInfo?.userId
                val resolvedUserName = authResult.user?.name ?: authResult.sessionInfo?.userName ?: cleanUsername
                val serverId = authResult.serverId ?: UUID.randomUUID().toString()

                val server = ServerEntity(
                    id = serverId,
                    name = resolvedName,
                    url = sanitizedUrl,
                    userId = resolvedUserId,
                    userName = resolvedUserName,
                    accessToken = token,
                    isDemo = false,
                    isActive = true,
                    serverVersion = serverVersion,
                    lastConnected = System.currentTimeMillis()
                )
                serverDao.deactivateAllServers()
                serverDao.insertServer(server)
                Result.success(server)
            } else {
                val errorMsg = when (authResponse.code()) {
                    401 -> "Invalid username or password. Please verify your Jellyfin credentials."
                    403 -> "Access forbidden. User account may be disabled or restricted on this server."
                    404 -> "Authentication endpoint not found at $sanitizedUrl. Do not include '/web' in the server address."
                    500 -> "Server internal error during authentication. Check Jellyfin server logs."
                    else -> "Authentication failed (HTTP ${authResponse.code()})."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkException(e, sanitizedUrl))
        }
    }

    /**
     * Initiates a Quick Connect request with Jellyfin.
     * Returns a 6-character Code and secret to show to the user.
     */
    suspend fun initiateQuickConnect(rawUrl: String): Result<QuickConnectInitiateResponse> = withContext(Dispatchers.IO) {
        val sanitizedUrl = JellyfinClientFactory.sanitizeUrl(rawUrl)
        try {
            val api = JellyfinClientFactory.createApi(sanitizedUrl)
            val response = api.initiateQuickConnect()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Quick Connect is disabled on this Jellyfin instance by the administrator."
                    404 -> "Quick Connect endpoint not found. Verify server URL: $sanitizedUrl"
                    else -> "Quick Connect not available (HTTP ${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkException(e, sanitizedUrl))
        }
    }

    /**
     * Polls server to check whether Quick Connect code has been approved.
     */
    suspend fun checkQuickConnect(rawUrl: String, secret: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val sanitizedUrl = JellyfinClientFactory.sanitizeUrl(rawUrl)
        try {
            val api = JellyfinClientFactory.createApi(sanitizedUrl)
            val response = api.checkQuickConnect(secret)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()?.authenticated == true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completes Quick Connect authentication once approved.
     */
    suspend fun authenticateWithQuickConnect(
        name: String,
        rawUrl: String,
        secret: String
    ): Result<ServerEntity> = withContext(Dispatchers.IO) {
        val sanitizedUrl = JellyfinClientFactory.sanitizeUrl(rawUrl)
        try {
            val api = JellyfinClientFactory.createApi(sanitizedUrl)
            val authResponse = api.authenticateWithQuickConnect(
                request = QuickConnectAuthRequest(secret)
            )

            if (authResponse.isSuccessful && authResponse.body() != null) {
                val authResult = authResponse.body()!!
                val token = authResult.accessToken
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Quick Connect approval returned empty token."))
                }

                val resolvedUserId = authResult.user?.id ?: authResult.sessionInfo?.userId
                val resolvedUserName = authResult.user?.name ?: authResult.sessionInfo?.userName ?: "QuickConnect User"
                val serverId = authResult.serverId ?: UUID.randomUUID().toString()

                val server = ServerEntity(
                    id = serverId,
                    name = name.trim().ifBlank { "Jellyfin Server" },
                    url = sanitizedUrl,
                    userId = resolvedUserId,
                    userName = resolvedUserName,
                    accessToken = token,
                    isDemo = false,
                    isActive = true,
                    serverVersion = "Connected via QuickConnect",
                    lastConnected = System.currentTimeMillis()
                )
                serverDao.deactivateAllServers()
                serverDao.insertServer(server)
                Result.success(server)
            } else {
                Result.failure(Exception("Quick Connect authentication failed (HTTP ${authResponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkException(e, sanitizedUrl))
        }
    }

    /**
     * Validates if active token is still authorized on server via System/Info.
     */
    suspend fun validateSession(server: ServerEntity): Boolean = withContext(Dispatchers.IO) {
        if (server.isDemo) return@withContext true
        if (server.accessToken.isNullOrBlank()) return@withContext false
        try {
            val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
            val response = api.getSystemInfo(server.accessToken)
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Logs out the current user session from the Jellyfin server.
     */
    suspend fun logoutServer(server: ServerEntity) = withContext(Dispatchers.IO) {
        if (!server.isDemo && !server.accessToken.isNullOrBlank()) {
            try {
                val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
                api.logout(server.accessToken)
            } catch (_: Exception) {}
        }
        serverDao.deleteServer(server.id)
        ensureDefaultServer()
    }

    suspend fun activateServer(serverId: String) = withContext(Dispatchers.IO) {
        serverDao.deactivateAllServers()
        serverDao.setActiveServer(serverId)
    }

    suspend fun deleteServer(serverId: String) = withContext(Dispatchers.IO) {
        serverDao.deleteServer(serverId)
        ensureDefaultServer()
    }

    suspend fun getCachedUserLibraries(serverId: String): List<JellyfinLibraryView> = withContext(Dispatchers.IO) {
        libraryViewCacheDao.getCachedViews(serverId).map { it.toLibraryView() }
    }

    suspend fun getCachedLibraryItems(
        serverId: String,
        parentId: String? = null,
        type: MediaType? = null
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val cachedEntities = when {
            type != null -> mediaCacheDao.getCachedMediaByType(serverId, type.name)
            parentId != null && parentId != "all" && parentId != "movies" && parentId != "shows" && parentId != "music" -> {
                val byParent = mediaCacheDao.getCachedMediaByParent(serverId, parentId)
                if (byParent.isNotEmpty()) byParent else mediaCacheDao.getAllCachedMedia(serverId)
            }
            else -> mediaCacheDao.getAllCachedMedia(serverId)
        }
        cachedEntities.map { it.toMediaItem() }
    }

    suspend fun getCacheCount(serverId: String): Int = withContext(Dispatchers.IO) {
        mediaCacheDao.getCacheCount(serverId)
    }

    suspend fun clearCache(serverId: String) = withContext(Dispatchers.IO) {
        mediaCacheDao.clearCacheForServer(serverId)
        libraryViewCacheDao.clearViewsForServer(serverId)
    }

    /**
     * Fetches user library folders/views from Jellyfin (e.g., Movies, TV Shows, Anime, Collections).
     * Handles both /Users/{userId}/Views and /UserViews endpoints gracefully.
     * Caches views in Room database for instant offline access.
     */
    suspend fun fetchUserLibraries(server: ServerEntity): Result<List<JellyfinLibraryView>> = withContext(Dispatchers.IO) {
        if (server.isDemo) {
            val demoViews = listOf(
                JellyfinLibraryView(id = "all", name = "All Libraries", collectionType = null, type = null, itemCount = 12),
                JellyfinLibraryView(id = "movies", name = "Movies", collectionType = "movies", type = MediaType.MOVIE, itemCount = 5),
                JellyfinLibraryView(id = "shows", name = "TV Shows", collectionType = "tvshows", type = MediaType.SERIES, itemCount = 4),
                JellyfinLibraryView(id = "music", name = "Music", collectionType = "music", type = MediaType.AUDIO, itemCount = 3)
            )
            // Persist demo views to Room cache as well
            libraryViewCacheDao.insertViews(demoViews.map { it.toCachedEntity(server.id) })
            return@withContext Result.success(demoViews)
        }

        try {
            val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
            val response = if (!server.userId.isNullOrBlank()) {
                val res = api.getUserViews(userId = server.userId, token = server.accessToken)
                if (res.isSuccessful && res.body()?.items != null) res else api.getUserViewsWithoutId()
            } else {
                api.getUserViewsWithoutId()
            }

            if (response.isSuccessful && response.body()?.items != null) {
                val views = response.body()!!.items!!.map { dto ->
                    val type = when (dto.collectionType?.lowercase()) {
                        "movies" -> MediaType.MOVIE
                        "tvshows" -> MediaType.SERIES
                        "music" -> MediaType.AUDIO
                        else -> null
                    }
                    JellyfinLibraryView(
                        id = dto.id,
                        name = dto.name ?: "Library",
                        collectionType = dto.collectionType,
                        type = type,
                        itemCount = dto.childCount ?: dto.seasonCount
                    )
                }
                // Cache views to Room database
                libraryViewCacheDao.insertViews(views.map { it.toCachedEntity(server.id) })
                Result.success(views)
            } else {
                // Fallback to Room cached views on network error
                val cached = libraryViewCacheDao.getCachedViews(server.id)
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toLibraryView() })
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Session expired or unauthorized. Please re-authenticate."
                        else -> "Failed to load user libraries (HTTP ${response.code()})"
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            // Fallback to Room cached views if offline / network disconnected
            val cached = libraryViewCacheDao.getCachedViews(server.id)
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toLibraryView() })
            } else {
                Result.failure(mapNetworkException(e, server.url))
            }
        }
    }

    /**
     * Fetches items for grid-based browsing with filtering, searching, sorting, and pagination
     * directly via Jellyfin's Retrofit API interface.
     * Caches all metadata, poster URLs, and stream parameters in Room for instant offline loading.
     */
    suspend fun fetchLibraryGrid(
        server: ServerEntity,
        parentId: String? = null,
        type: MediaType? = null,
        sortBy: String? = "SortName",
        sortOrder: String = "Ascending",
        searchTerm: String? = null,
        genre: String? = null,
        limit: Int = 100
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        if (server.isDemo) {
            var items = DemoMediaCatalog.getAllItems()
            // Persist demo items to Room database cache
            mediaCacheDao.insertMediaItems(items.map { it.toCachedEntity(server.id, parentId) })

            if (type != null) {
                items = items.filter { it.type == type }
            }
            if (!searchTerm.isNullOrBlank()) {
                val query = searchTerm.trim().lowercase()
                items = items.filter {
                    it.title.lowercase().contains(query) ||
                    it.overview.lowercase().contains(query) ||
                    it.genres.any { g -> g.lowercase().contains(query) }
                }
            }
            if (!genre.isNullOrBlank()) {
                items = items.filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }
            }
            // Apply sorting
            items = when (sortBy) {
                "CommunityRating" -> if (sortOrder == "Descending") items.sortedByDescending { it.communityRating ?: 0f } else items.sortedBy { it.communityRating ?: 0f }
                "PremiereDate" -> if (sortOrder == "Descending") items.sortedByDescending { it.year ?: 0 } else items.sortedBy { it.year ?: 0 }
                else -> if (sortOrder == "Descending") items.sortedByDescending { it.title.lowercase() } else items.sortedBy { it.title.lowercase() }
            }
            return@withContext Result.success(items)
        }

        // Real Jellyfin server via Retrofit
        try {
            val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
            val includeType = when (type) {
                MediaType.MOVIE -> "Movie"
                MediaType.SERIES -> "Series"
                MediaType.EPISODE -> "Episode"
                MediaType.AUDIO -> "Audio"
                MediaType.SEASON -> "Season"
                null -> if (parentId != null) null else "Movie,Series"
            }

            val targetParentId = if (parentId == "all" || parentId == "movies" || parentId == "shows" || parentId == "music") null else parentId

            val response = if (!server.userId.isNullOrBlank()) {
                api.getUserItems(
                    userId = server.userId,
                    token = server.accessToken,
                    includeItemTypes = includeType,
                    parentId = targetParentId,
                    recursive = true,
                    sortBy = sortBy,
                    sortOrder = sortOrder,
                    searchTerm = searchTerm?.ifBlank { null },
                    genres = genre?.ifBlank { null },
                    limit = limit
                )
            } else {
                api.getItems(
                    includeItemTypes = includeType,
                    parentId = targetParentId,
                    recursive = true,
                    sortBy = sortBy,
                    sortOrder = sortOrder,
                    searchTerm = searchTerm?.ifBlank { null },
                    genres = genre?.ifBlank { null },
                    limit = limit
                )
            }

            if (response.isSuccessful && response.body()?.items != null) {
                val mediaItems = response.body()!!.items!!.map { dto ->
                    mapDtoToMediaItem(dto, server)
                }
                // Save fetched metadata and posters into Room database cache
                mediaCacheDao.insertMediaItems(mediaItems.map { it.toCachedEntity(server.id, parentId) })
                Result.success(mediaItems)
            } else {
                // Fallback to Room cached items on HTTP error
                val cachedFallback = getLocalCachedFiltered(server.id, parentId, type, sortBy, sortOrder, searchTerm, genre)
                if (cachedFallback.isNotEmpty()) {
                    Result.success(cachedFallback)
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Session expired. Please reconnect to your Jellyfin server."
                        404 -> "Library folder not found on server."
                        else -> "Failed to fetch library items (HTTP ${response.code()})"
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            // Fallback to Room cached items when offline / network disconnected
            val cachedFallback = getLocalCachedFiltered(server.id, parentId, type, sortBy, sortOrder, searchTerm, genre)
            if (cachedFallback.isNotEmpty()) {
                Result.success(cachedFallback)
            } else {
                Result.failure(mapNetworkException(e, server.url))
            }
        }
    }

    private suspend fun getLocalCachedFiltered(
        serverId: String,
        parentId: String?,
        type: MediaType?,
        sortBy: String?,
        sortOrder: String,
        searchTerm: String?,
        genre: String?
    ): List<MediaItem> {
        val cachedEntities = when {
            type != null -> mediaCacheDao.getCachedMediaByType(serverId, type.name)
            parentId != null && parentId != "all" && parentId != "movies" && parentId != "shows" && parentId != "music" ->
                mediaCacheDao.getCachedMediaByParent(serverId, parentId)
            else -> mediaCacheDao.getAllCachedMedia(serverId)
        }
        var items = cachedEntities.map { it.toMediaItem() }

        if (!searchTerm.isNullOrBlank()) {
            val query = searchTerm.trim().lowercase()
            items = items.filter {
                it.title.lowercase().contains(query) ||
                it.overview.lowercase().contains(query) ||
                it.genres.any { g -> g.lowercase().contains(query) }
            }
        }
        if (!genre.isNullOrBlank()) {
            items = items.filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }
        }
        return when (sortBy) {
            "CommunityRating" -> if (sortOrder == "Descending") items.sortedByDescending { it.communityRating ?: 0f } else items.sortedBy { it.communityRating ?: 0f }
            "PremiereDate" -> if (sortOrder == "Descending") items.sortedByDescending { it.year ?: 0 } else items.sortedBy { it.year ?: 0 }
            else -> if (sortOrder == "Descending") items.sortedByDescending { it.title.lowercase() } else items.sortedBy { it.title.lowercase() }
        }
    }

    suspend fun fetchMedia(
        server: ServerEntity,
        type: MediaType? = null,
        searchTerm: String? = null
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        if (server.isDemo) {
            var items = DemoMediaCatalog.getAllItems()
            if (type != null) {
                items = items.filter { it.type == type }
            }
            if (!searchTerm.isNullOrBlank()) {
                val query = searchTerm.trim().lowercase()
                items = items.filter {
                    it.title.lowercase().contains(query) ||
                    it.overview.lowercase().contains(query) ||
                    it.genres.any { g -> g.lowercase().contains(query) }
                }
            }
            return@withContext items
        }

        // Real Jellyfin server
        try {
            val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
            val includeType = when (type) {
                MediaType.MOVIE -> "Movie"
                MediaType.SERIES -> "Series"
                MediaType.EPISODE -> "Episode"
                MediaType.AUDIO -> "Audio"
                null -> "Movie,Series,Audio"
                else -> null
            }

            val response = if (!server.userId.isNullOrBlank()) {
                api.getUserItems(
                    userId = server.userId,
                    token = server.accessToken,
                    includeItemTypes = includeType,
                    searchTerm = searchTerm?.ifBlank { null }
                )
            } else {
                api.getItems(
                    includeItemTypes = includeType,
                    searchTerm = searchTerm?.ifBlank { null }
                )
            }

            if (response.isSuccessful && response.body()?.items != null) {
                val media = response.body()!!.items!!.map { dto ->
                    mapDtoToMediaItem(dto, server)
                }
                // Save to Room cache
                mediaCacheDao.insertMediaItems(media.map { it.toCachedEntity(server.id) })
                return@withContext media
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Check Room database cache before falling back
        val cached = if (type != null) {
            mediaCacheDao.getCachedMediaByType(server.id, type.name)
        } else {
            mediaCacheDao.getAllCachedMedia(server.id)
        }
        if (cached.isNotEmpty()) {
            return@withContext cached.map { it.toMediaItem() }
        }

        // Fallback to sample items if server query fails and cache is empty
        DemoMediaCatalog.getAllItems().filter { type == null || it.type == type }
    }

    suspend fun fetchContinueWatching(server: ServerEntity): List<MediaItem> = withContext(Dispatchers.IO) {
        if (server.isDemo) {
            return@withContext DemoMediaCatalog.getContinueWatching()
        }

        try {
            val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
            val response = if (!server.userId.isNullOrBlank()) {
                api.getResumeItems(
                    userId = server.userId,
                    token = server.accessToken
                )
            } else {
                api.getResumeItemsGeneral()
            }
            if (response.isSuccessful && response.body()?.items != null) {
                val items = response.body()!!.items!!.map { mapDtoToMediaItem(it, server) }
                mediaCacheDao.insertMediaItems(items.map { it.toCachedEntity(server.id) })
                return@withContext items
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Check Room database cache for resume items before falling back
        val cachedCw = mediaCacheDao.getCachedContinueWatching(server.id)
        if (cachedCw.isNotEmpty()) {
            return@withContext cachedCw.map { it.toMediaItem() }
        }

        DemoMediaCatalog.getContinueWatching()
    }

    suspend fun getEpisodesForSeries(
        server: ServerEntity,
        seriesId: String,
        seriesName: String
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        if (server.isDemo) {
            val demoEpisodes = DemoMediaCatalog.getEpisodesForShow(seriesId, seriesName)
            mediaCacheDao.insertMediaItems(demoEpisodes.map { it.toCachedEntity(server.id, seriesId) })
            return@withContext demoEpisodes
        }

        try {
            val api = JellyfinClientFactory.createApi(server.url, server.accessToken)

            // 1. Try official Jellyfin Shows/{seriesId}/Episodes endpoint
            if (seriesId.isNotBlank() && seriesId != "all") {
                val showEpResponse = api.getShowEpisodes(
                    seriesId = seriesId,
                    token = server.accessToken,
                    userId = server.userId
                )
                if (showEpResponse.isSuccessful && !showEpResponse.body()?.items.isNullOrEmpty()) {
                    val eps = showEpResponse.body()!!.items!!.map { dto ->
                        mapDtoToMediaItem(dto, server)
                    }
                    mediaCacheDao.insertMediaItems(eps.map { it.toCachedEntity(server.id, seriesId) })
                    return@withContext eps
                }

                // 2. Try querying Items with parentId = seriesId and includeItemTypes = Episode
                val parentEpResponse = if (!server.userId.isNullOrBlank()) {
                    api.getUserItems(
                        userId = server.userId,
                        token = server.accessToken,
                        parentId = seriesId,
                        includeItemTypes = "Episode",
                        recursive = true,
                        limit = 150
                    )
                } else {
                    api.getItems(
                        parentId = seriesId,
                        includeItemTypes = "Episode",
                        recursive = true,
                        limit = 150
                    )
                }

                if (parentEpResponse.isSuccessful && !parentEpResponse.body()?.items.isNullOrEmpty()) {
                    val eps = parentEpResponse.body()!!.items!!.map { dto ->
                        mapDtoToMediaItem(dto, server)
                    }
                    mediaCacheDao.insertMediaItems(eps.map { it.toCachedEntity(server.id, seriesId) })
                    return@withContext eps
                }
            }

            // 3. Fallback: Search episodes by seriesName
            if (seriesName.isNotBlank()) {
                val searchEpResponse = if (!server.userId.isNullOrBlank()) {
                    api.getUserItems(
                        userId = server.userId,
                        token = server.accessToken,
                        searchTerm = seriesName,
                        includeItemTypes = "Episode",
                        recursive = true,
                        limit = 100
                    )
                } else {
                    api.getItems(
                        searchTerm = seriesName,
                        includeItemTypes = "Episode",
                        recursive = true,
                        limit = 100
                    )
                }

                if (searchEpResponse.isSuccessful && !searchEpResponse.body()?.items.isNullOrEmpty()) {
                    val eps = searchEpResponse.body()!!.items!!.map { dto ->
                        mapDtoToMediaItem(dto, server)
                    }
                    mediaCacheDao.insertMediaItems(eps.map { it.toCachedEntity(server.id, seriesId) })
                    return@withContext eps
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Check Room database cache for episodes before falling back
        val cachedEps = mediaCacheDao.getCachedEpisodesForSeries(server.id, seriesName, seriesId)
        if (cachedEps.isNotEmpty()) {
            return@withContext cachedEps.map { it.toMediaItem() }
        }

        // Fallback to demo/sample episodes
        val fallback = DemoMediaCatalog.getEpisodesForShow(seriesId, seriesName)
        mediaCacheDao.insertMediaItems(fallback.map { it.toCachedEntity(server.id, seriesId) })
        fallback
    }

    suspend fun getEpisodesForSeries(server: ServerEntity, seriesName: String): List<MediaItem> {
        return getEpisodesForSeries(server, "", seriesName)
    }

    fun getFavorites(serverId: String): Flow<List<FavoriteEntity>> = favoriteDao.getFavoritesForServer(serverId)

    fun isFavorite(itemId: String, serverId: String): Flow<Boolean> = favoriteDao.isFavorite(itemId, serverId)

    suspend fun toggleFavorite(mediaItem: MediaItem, server: ServerEntity) = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavorite(mediaItem.id, server.id).firstOrNull() ?: false
        if (isFav) {
            favoriteDao.deleteFavorite(mediaItem.id, server.id)
            mediaCacheDao.updateFavorite(server.id, mediaItem.id, false)
            if (!server.isDemo) {
                try {
                    val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
                    api.unmarkFavorite(mediaItem.id, server.accessToken)
                } catch (_: Exception) {}
            }
        } else {
            val fav = FavoriteEntity(
                itemId = mediaItem.id,
                serverId = server.id,
                title = mediaItem.title,
                type = mediaItem.type.name,
                posterUrl = mediaItem.posterUrl
            )
            favoriteDao.insertFavorite(fav)
            mediaCacheDao.updateFavorite(server.id, mediaItem.id, true)
            if (!server.isDemo) {
                try {
                    val api = JellyfinClientFactory.createApi(server.url, server.accessToken)
                    api.markFavorite(mediaItem.id, server.accessToken)
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun savePlaybackProgress(mediaItem: MediaItem, server: ServerEntity, positionMs: Long, durationMs: Long) = withContext(Dispatchers.IO) {
        val entry = PlaybackHistoryEntity(
            itemId = mediaItem.id,
            serverId = server.id,
            title = mediaItem.title,
            type = mediaItem.type.name,
            seriesName = mediaItem.seriesName,
            seasonNumber = mediaItem.seasonNumber,
            episodeNumber = mediaItem.episodeNumber,
            posterUrl = mediaItem.posterUrl,
            backdropUrl = mediaItem.backdropUrl,
            streamUrl = mediaItem.streamUrl,
            positionMs = positionMs,
            durationMs = durationMs
        )
        playbackHistoryDao.recordProgress(entry)
        mediaCacheDao.updatePlaybackPosition(server.id, mediaItem.id, positionMs)
    }

    private fun mapNetworkException(e: Exception, url: String): Exception {
        return when (e) {
            is UnknownHostException -> Exception("Cannot resolve host. Check the server address: $url")
            is ConnectException -> Exception("Connection refused. Verify that Jellyfin is running and port is correct: $url")
            is SocketTimeoutException -> Exception("Connection timed out. Check your network, firewall, or port forwarding.")
            else -> Exception("Connection failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun mapDtoToMediaItem(dto: BaseItemDto, server: ServerEntity): MediaItem {
        val type = when (dto.type) {
            "Movie" -> MediaType.MOVIE
            "Series" -> MediaType.SERIES
            "Season" -> MediaType.SEASON
            "Episode" -> MediaType.EPISODE
            "Audio" -> MediaType.AUDIO
            else -> MediaType.MOVIE
        }

        val primaryTag = dto.imageTags?.get("Primary")
        val backdropTag = dto.backdropImageTags?.firstOrNull()
        val poster = if (primaryTag != null) JellyfinClientFactory.buildImageUrl(server.url, dto.id, "Primary", primaryTag) else null
        val backdrop = if (backdropTag != null) JellyfinClientFactory.buildImageUrl(server.url, dto.id, "Backdrop", backdropTag) else null
        val stream = JellyfinClientFactory.buildStreamUrl(server.url, dto.id, server.accessToken, isAudio = type == MediaType.AUDIO)

        val durationMs = (dto.runTimeTicks ?: 0L) / 10000L
        val playbackPositionMs = ((dto.userData?.playbackPositionTicks ?: 0L) / 10000L)

        return MediaItem(
            id = dto.id,
            title = dto.name ?: "Untitled",
            type = type,
            overview = dto.overview ?: "",
            year = dto.productionYear,
            communityRating = dto.communityRating,
            durationMs = durationMs,
            genres = dto.genres ?: emptyList(),
            posterUrl = poster,
            backdropUrl = backdrop,
            streamUrl = stream,
            seriesName = dto.seriesName,
            seasonName = dto.seasonName,
            seasonNumber = dto.parentIndexNumber,
            episodeNumber = dto.indexNumber,
            playbackPositionMs = playbackPositionMs,
            isFavorite = dto.userData?.isFavorite ?: false
        )
    }
}
