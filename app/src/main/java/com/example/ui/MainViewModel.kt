package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ServerEntity
import com.example.data.model.JellyfinLibraryView
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.PublicSystemInfo
import com.example.data.model.PublicUserDto
import com.example.data.remote.JellyfinClientFactory
import com.example.data.repository.JellyfinRepository
import com.example.player.PlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Home : AppScreen()
    object Library : AppScreen()
    data class MediaDetail(val item: MediaItem) : AppScreen()
    data class VideoPlayer(val item: MediaItem) : AppScreen()
    object ServerManager : AppScreen()
    object Settings : AppScreen()
}

enum class SortOption(val label: String, val apiSortBy: String, val apiSortOrder: String) {
    TITLE_ASC("Title (A-Z)", "SortName", "Ascending"),
    TITLE_DESC("Title (Z-A)", "SortName", "Descending"),
    RATING_DESC("Highest Rated", "CommunityRating", "Descending"),
    YEAR_DESC("Newest Releases", "PremiereDate", "Descending"),
    YEAR_ASC("Oldest Releases", "PremiereDate", "Ascending"),
    RECENTLY_ADDED("Recently Added", "DateCreated", "Descending")
}

enum class LibraryLayoutMode {
    GRID_STANDARD, // 2-columns with badges & rating
    GRID_COMPACT,  // 3-columns dense poster grid
    LIST           // Detailed list row
}

data class QuickConnectState(
    val code: String? = null,
    val secret: String? = null,
    val isWaiting: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = JellyfinRepository(database)
    val playerManager = PlayerManager(application)

    val activeServer: StateFlow<ServerEntity?> = repository.activeServer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allServers: StateFlow<List<ServerEntity>> = repository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val navigationBackStack = mutableListOf<AppScreen>()

    private val _featuredItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val featuredItems: StateFlow<List<MediaItem>> = _featuredItems.asStateFlow()

    private val _continueWatching = MutableStateFlow<List<MediaItem>>(emptyList())
    val continueWatching: StateFlow<List<MediaItem>> = _continueWatching.asStateFlow()

    private val _latestMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val latestMovies: StateFlow<List<MediaItem>> = _latestMovies.asStateFlow()

    private val _tvSeries = MutableStateFlow<List<MediaItem>>(emptyList())
    val tvSeries: StateFlow<List<MediaItem>> = _tvSeries.asStateFlow()

    private val _musicTracks = MutableStateFlow<List<MediaItem>>(emptyList())
    val musicTracks: StateFlow<List<MediaItem>> = _musicTracks.asStateFlow()

    private val _libraryItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val libraryItems: StateFlow<List<MediaItem>> = _libraryItems.asStateFlow()

    private val _userLibraries = MutableStateFlow<List<JellyfinLibraryView>>(emptyList())
    val userLibraries: StateFlow<List<JellyfinLibraryView>> = _userLibraries.asStateFlow()

    private val _selectedLibraryView = MutableStateFlow<JellyfinLibraryView?>(null)
    val selectedLibraryView: StateFlow<JellyfinLibraryView?> = _selectedLibraryView.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TITLE_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _layoutMode = MutableStateFlow(LibraryLayoutMode.GRID_STANDARD)
    val layoutMode: StateFlow<LibraryLayoutMode> = _layoutMode.asStateFlow()

    private val _isLibraryLoading = MutableStateFlow(false)
    val isLibraryLoading: StateFlow<Boolean> = _isLibraryLoading.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _cachedCount = MutableStateFlow(0)
    val cachedCount: StateFlow<Int> = _cachedCount.asStateFlow()

    private val _libraryError = MutableStateFlow<String?>(null)
    val libraryError: StateFlow<String?> = _libraryError.asStateFlow()

    private val _episodes = MutableStateFlow<List<MediaItem>>(emptyList())
    val episodes: StateFlow<List<MediaItem>> = _episodes.asStateFlow()

    private val _isEpisodesLoading = MutableStateFlow(false)
    val isEpisodesLoading: StateFlow<Boolean> = _isEpisodesLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMediaType = MutableStateFlow<MediaType?>(null)
    val selectedMediaType: StateFlow<MediaType?> = _selectedMediaType.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pingResult = MutableStateFlow<String?>(null)
    val pingResult: StateFlow<String?> = _pingResult.asStateFlow()

    private val _discoveredServerInfo = MutableStateFlow<PublicSystemInfo?>(null)
    val discoveredServerInfo: StateFlow<PublicSystemInfo?> = _discoveredServerInfo.asStateFlow()

    private val _publicUsers = MutableStateFlow<List<PublicUserDto>>(emptyList())
    val publicUsers: StateFlow<List<PublicUserDto>> = _publicUsers.asStateFlow()

    private val _quickConnectState = MutableStateFlow(QuickConnectState())
    val quickConnectState: StateFlow<QuickConnectState> = _quickConnectState.asStateFlow()

    private var quickConnectPollingJob: Job? = null

    val playerUiState = playerManager.uiState

    init {
        JellyfinClientFactory.init(application)

        viewModelScope.launch {
            repository.ensureDefaultServer()
        }

        viewModelScope.launch {
            activeServer.collect { server ->
                if (server != null) {
                    loadFromCacheInstantly(server)
                    loadUserLibraries(server)
                    refreshMedia(server)
                }
            }
        }

        playerManager.onProgressUpdateListener = { item, currentPos, dur ->
            val server = activeServer.value
            if (server != null && dur > 0) {
                viewModelScope.launch {
                    repository.savePlaybackProgress(item, server, currentPos, dur)
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        navigationBackStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun handleBack(): Boolean {
        if (_currentScreen.value is AppScreen.VideoPlayer) {
            if (navigationBackStack.isNotEmpty()) {
                _currentScreen.value = navigationBackStack.removeAt(navigationBackStack.lastIndex)
            } else {
                _currentScreen.value = AppScreen.Home
            }
            return true
        }

        if (playerUiState.value.isAudioPlayerExpanded) {
            playerManager.setAudioPlayerExpanded(false)
            return true
        }

        if (navigationBackStack.isNotEmpty()) {
            _currentScreen.value = navigationBackStack.removeAt(navigationBackStack.lastIndex)
            return true
        }

        return false
    }

    private fun loadFromCacheInstantly(server: ServerEntity) {
        viewModelScope.launch {
            val cachedViews = repository.getCachedUserLibraries(server.id)
            if (cachedViews.isNotEmpty()) {
                _userLibraries.value = cachedViews
            }

            val cachedMedia = repository.getCachedLibraryItems(server.id)
            if (cachedMedia.isNotEmpty()) {
                _latestMovies.value = cachedMedia.filter { it.type == MediaType.MOVIE }
                _tvSeries.value = cachedMedia.filter { it.type == MediaType.SERIES }
                _musicTracks.value = cachedMedia.filter { it.type == MediaType.AUDIO }
                _featuredItems.value = cachedMedia.take(5)
                if (_libraryItems.value.isEmpty()) {
                    _libraryItems.value = cachedMedia
                }
                _cachedCount.value = cachedMedia.size
                _isLibraryLoading.value = false
            }
        }
    }

    fun loadUserLibraries(server: ServerEntity? = activeServer.value) {
        if (server == null) return
        viewModelScope.launch {
            // Instant load from Room cache before network request
            val cached = repository.getCachedUserLibraries(server.id)
            if (cached.isNotEmpty()) {
                _userLibraries.value = cached
            }

            val result = repository.fetchUserLibraries(server)
            result.onSuccess { views ->
                _userLibraries.value = views
            }
            updateCacheCount(server.id)
        }
    }

    fun updateCacheCount(serverId: String = activeServer.value?.id ?: "") {
        if (serverId.isBlank()) return
        viewModelScope.launch {
            val count = repository.getCacheCount(serverId)
            _cachedCount.value = count
        }
    }

    fun clearLocalCache() {
        val server = activeServer.value ?: return
        viewModelScope.launch {
            repository.clearCache(server.id)
            _cachedCount.value = 0
            refreshMedia(server)
        }
    }

    fun selectLibraryView(view: JellyfinLibraryView?) {
        _selectedLibraryView.value = view
        if (view?.type != null) {
            _selectedMediaType.value = view.type
        } else if (view != null && view.id != "all") {
            _selectedMediaType.value = null
        }
        val server = activeServer.value ?: return
        applyLibraryFilters(server)
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        val server = activeServer.value ?: return
        applyLibraryFilters(server)
    }

    fun setLayoutMode(mode: LibraryLayoutMode) {
        _layoutMode.value = mode
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedMediaType.value = null
        _selectedGenre.value = null
        _selectedLibraryView.value = null
        _sortOption.value = SortOption.TITLE_ASC
        val server = activeServer.value ?: return
        applyLibraryFilters(server)
    }

    fun refreshLibrary() {
        val server = activeServer.value ?: return
        loadUserLibraries(server)
        applyLibraryFilters(server)
    }

    fun refreshMedia(server: ServerEntity? = activeServer.value) {
        if (server == null) return
        viewModelScope.launch {
            // Preload cached media instantly so UI is never blank
            val cachedMedia = repository.getCachedLibraryItems(server.id)
            if (cachedMedia.isNotEmpty()) {
                _latestMovies.value = cachedMedia.filter { it.type == MediaType.MOVIE }
                _tvSeries.value = cachedMedia.filter { it.type == MediaType.SERIES }
                _musicTracks.value = cachedMedia.filter { it.type == MediaType.AUDIO }
                _featuredItems.value = cachedMedia.take(5)
                if (_libraryItems.value.isEmpty()) {
                    _libraryItems.value = cachedMedia
                }
                _cachedCount.value = cachedMedia.size
            }
            // Trigger instant display of filtered items from Room cache
            applyLibraryFilters(server)

            _isLoading.value = cachedMedia.isEmpty()
            try {
                val cw = repository.fetchContinueWatching(server)
                _continueWatching.value = cw

                val movies = repository.fetchMedia(server, MediaType.MOVIE)
                _latestMovies.value = movies

                val series = repository.fetchMedia(server, MediaType.SERIES)
                _tvSeries.value = series

                val music = repository.fetchMedia(server, MediaType.AUDIO)
                _musicTracks.value = music

                _featuredItems.value = (movies.take(3) + series.take(2)).shuffled()

                applyLibraryFilters(server)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                updateCacheCount(server.id)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val server = activeServer.value ?: return
        applyLibraryFilters(server)
    }

    fun setSelectedMediaType(type: MediaType?) {
        _selectedMediaType.value = type
        val server = activeServer.value ?: return
        applyLibraryFilters(server)
    }

    fun setSelectedGenre(genre: String?) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
        val server = activeServer.value ?: return
        applyLibraryFilters(server)
    }

    fun applyLibraryFilters(server: ServerEntity? = activeServer.value) {
        viewModelScope.launch {
            val targetServer = server ?: activeServer.value ?: return@launch
            val parentId = _selectedLibraryView.value?.id
            val mediaType = _selectedMediaType.value
            val sort = _sortOption.value
            val query = _searchQuery.value
            val genre = _selectedGenre.value

            // Instant load from Room database: populate UI immediately
            val cachedItems = repository.getCachedLibraryItems(targetServer.id, parentId, mediaType)
            if (cachedItems.isNotEmpty()) {
                var localFiltered = cachedItems
                if (query.isNotBlank()) {
                    val q = query.trim().lowercase()
                    localFiltered = localFiltered.filter {
                        it.title.lowercase().contains(q) ||
                        it.overview.lowercase().contains(q) ||
                        it.genres.any { g -> g.lowercase().contains(q) }
                    }
                }
                if (genre != null) {
                    localFiltered = localFiltered.filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }
                }
                localFiltered = when (sort.apiSortBy) {
                    "CommunityRating" -> if (sort.apiSortOrder == "Descending") localFiltered.sortedByDescending { it.communityRating ?: 0f } else localFiltered.sortedBy { it.communityRating ?: 0f }
                    "PremiereDate" -> if (sort.apiSortOrder == "Descending") localFiltered.sortedByDescending { it.year ?: 0 } else localFiltered.sortedBy { it.year ?: 0 }
                    else -> if (sort.apiSortOrder == "Descending") localFiltered.sortedByDescending { it.title.lowercase() } else localFiltered.sortedBy { it.title.lowercase() }
                }
                _libraryItems.value = localFiltered
            } else {
                _isLibraryLoading.value = true
            }

            _libraryError.value = null
            try {
                val result = repository.fetchLibraryGrid(
                    server = targetServer,
                    parentId = parentId,
                    type = mediaType,
                    sortBy = sort.apiSortBy,
                    sortOrder = sort.apiSortOrder,
                    searchTerm = query,
                    genre = genre
                )

                result.onSuccess { items ->
                    _libraryItems.value = items
                    _libraryError.value = null
                    _isOfflineMode.value = false
                }.onFailure { error ->
                    if (_libraryItems.value.isNotEmpty()) {
                        _isOfflineMode.value = true
                        _libraryError.value = null
                    } else {
                        _libraryError.value = error.message ?: "Failed to fetch library"
                    }
                }
            } catch (e: Exception) {
                if (_libraryItems.value.isNotEmpty()) {
                    _isOfflineMode.value = true
                } else {
                    _libraryError.value = e.localizedMessage ?: "Unknown error"
                }
            } finally {
                _isLibraryLoading.value = false
                updateCacheCount(targetServer.id)
            }
        }
    }

    fun loadEpisodes(seriesId: String = "", seriesName: String) {
        val server = activeServer.value ?: return
        viewModelScope.launch {
            _isEpisodesLoading.value = true
            try {
                val eps = repository.getEpisodesForSeries(server, seriesId, seriesName)
                _episodes.value = eps
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEpisodesLoading.value = false
            }
        }
    }

    fun loadEpisodes(seriesName: String) {
        loadEpisodes("", seriesName)
    }

    fun playMedia(item: MediaItem, playlist: List<MediaItem> = emptyList()) {
        if (item.type == MediaType.AUDIO) {
            playerManager.play(item, item.playbackPositionMs, playlist)
        } else {
            playerManager.play(item, item.playbackPositionMs, playlist)
            navigateTo(AppScreen.VideoPlayer(item))
        }
    }

    fun toggleFavorite(item: MediaItem) {
        val server = activeServer.value ?: return
        val newFav = !item.isFavorite
        _libraryItems.value = _libraryItems.value.map {
            if (it.id == item.id) it.copy(isFavorite = newFav) else it
        }
        viewModelScope.launch {
            repository.toggleFavorite(item, server)
        }
    }

    fun testServerPing(url: String, onDiscovered: ((PublicSystemInfo) -> Unit)? = null) {
        viewModelScope.launch {
            _pingResult.value = "Testing connection..."
            val result = repository.pingServer(url)
            result.onSuccess { info ->
                _discoveredServerInfo.value = info
                _pingResult.value = "Connected to ${info.serverName ?: "Jellyfin"} (${info.version ?: "10.x"})"
                onDiscovered?.invoke(info)
                fetchPublicUsers(url)
            }.onFailure { err ->
                _discoveredServerInfo.value = null
                _pingResult.value = "Failed: ${err.message ?: "Server unreachable"}"
            }
        }
    }

    fun fetchPublicUsers(url: String) {
        viewModelScope.launch {
            val result = repository.fetchPublicUsers(url)
            _publicUsers.value = result.getOrDefault(emptyList())
        }
    }

    fun authenticateWithCredentials(
        name: String,
        url: String,
        user: String,
        pass: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.authenticateByName(name, url, user, pass)
            _isLoading.value = false
            result.onSuccess {
                onDone(true, null)
            }.onFailure { err ->
                onDone(false, err.message)
            }
        }
    }

    fun startQuickConnect(url: String, serverName: String, onDone: (Boolean, String?) -> Unit) {
        quickConnectPollingJob?.cancel()
        viewModelScope.launch {
            _quickConnectState.value = QuickConnectState(isWaiting = true)
            val initResult = repository.initiateQuickConnect(url)
            initResult.onSuccess { initResp ->
                val code = initResp.code ?: ""
                val secret = initResp.secret ?: ""
                _quickConnectState.value = QuickConnectState(
                    code = code,
                    secret = secret,
                    isWaiting = true
                )

                // Poll every 3 seconds for user authorization on web client / TV
                quickConnectPollingJob = viewModelScope.launch {
                    var attempts = 0
                    while (attempts < 60 && isActive) {
                        delay(3000L)
                        attempts++
                        val checkRes = repository.checkQuickConnect(url, secret)
                        if (checkRes.getOrDefault(false)) {
                            // User authorized QuickConnect! Now authenticate
                            val authResult = repository.authenticateWithQuickConnect(serverName, url, secret)
                            authResult.onSuccess {
                                _quickConnectState.value = _quickConnectState.value.copy(
                                    isWaiting = false,
                                    isSuccess = true
                                )
                                onDone(true, null)
                            }.onFailure { err ->
                                _quickConnectState.value = _quickConnectState.value.copy(
                                    isWaiting = false,
                                    errorMessage = err.message
                                )
                                onDone(false, err.message)
                            }
                            break
                        }
                    }
                }
            }.onFailure { err ->
                _quickConnectState.value = QuickConnectState(
                    errorMessage = err.message ?: "Failed to initiate Quick Connect",
                    isWaiting = false
                )
                onDone(false, err.message)
            }
        }
    }

    fun cancelQuickConnect() {
        quickConnectPollingJob?.cancel()
        quickConnectPollingJob = null
        _quickConnectState.value = QuickConnectState()
    }

    fun logoutServer(server: ServerEntity) {
        viewModelScope.launch {
            repository.logoutServer(server)
        }
    }

    fun selectServer(serverId: String) {
        viewModelScope.launch {
            repository.activateServer(serverId)
        }
    }

    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            repository.deleteServer(serverId)
        }
    }

    fun checkServerSession(server: ServerEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = repository.validateSession(server)
            onResult(isValid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        quickConnectPollingJob?.cancel()
        playerManager.release()
    }
}
