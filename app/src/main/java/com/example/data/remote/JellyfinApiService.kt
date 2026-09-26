package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.data.model.AuthenticateByNameRequest
import com.example.data.model.AuthenticationResult
import com.example.data.model.BaseItemDto
import com.example.data.model.ItemsQueryResult
import com.example.data.model.PublicSystemInfo
import com.example.data.model.PublicUserDto
import com.example.data.model.QuickConnectAuthRequest
import com.example.data.model.QuickConnectConnectResponse
import com.example.data.model.QuickConnectInitiateResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response as OkHttpResponse
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID
import java.util.concurrent.TimeUnit

interface JellyfinApi {

    @GET("System/Info/Public")
    suspend fun getPublicSystemInfo(): Response<PublicSystemInfo>

    @GET("System/Info")
    suspend fun getSystemInfo(
        @Header("X-Emby-Token") token: String? = null
    ): Response<PublicSystemInfo>

    @POST("Users/AuthenticateByName")
    suspend fun authenticateByName(
        @Body request: AuthenticateByNameRequest,
        @Header("X-Emby-Authorization") authHeader: String? = null
    ): Response<AuthenticationResult>

    @POST("Users/AuthenticateWithQuickConnect")
    suspend fun authenticateWithQuickConnect(
        @Body request: QuickConnectAuthRequest,
        @Header("X-Emby-Authorization") authHeader: String? = null
    ): Response<AuthenticationResult>

    @POST("QuickConnect/Initiate")
    suspend fun initiateQuickConnect(
        @Header("X-Emby-Authorization") authHeader: String? = null
    ): Response<QuickConnectInitiateResponse>

    @GET("QuickConnect/Connect")
    suspend fun checkQuickConnect(
        @Query("secret") secret: String
    ): Response<QuickConnectConnectResponse>

    @GET("Users/Public")
    suspend fun getPublicUsers(): Response<List<PublicUserDto>>

    @GET("Users/Me")
    suspend fun getCurrentUser(
        @Header("X-Emby-Token") token: String? = null
    ): Response<PublicUserDto>

    @POST("Sessions/Logout")
    suspend fun logout(
        @Header("X-Emby-Token") token: String? = null
    ): Response<ResponseBody>

    @GET("UserViews")
    suspend fun getUserViewsWithoutId(): Response<ItemsQueryResult>

    @GET("Users/{userId}/Views")
    suspend fun getUserViews(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") token: String? = null
    ): Response<ItemsQueryResult>

    @GET("Items")
    suspend fun getItems(
        @Query("userId") userId: String? = null,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("ParentId") parentId: String? = null,
        @Query("Recursive") recursive: Boolean = true,
        @Query("SortBy") sortBy: String? = "SortName",
        @Query("SortOrder") sortOrder: String = "Ascending",
        @Query("SearchTerm") searchTerm: String? = null,
        @Query("Genres") genres: String? = null,
        @Query("StartIndex") startIndex: Int? = null,
        @Query("Limit") limit: Int? = 100,
        @Query("Fields") fields: String? = "Overview,Genres,CommunityRating,RunTimeTicks,ProductionYear,UserData,SeriesName,SeasonName"
    ): Response<ItemsQueryResult>

    @GET("Users/{userId}/Items")
    suspend fun getUserItems(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") token: String? = null,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("ParentId") parentId: String? = null,
        @Query("Recursive") recursive: Boolean = true,
        @Query("SortBy") sortBy: String? = "SortName",
        @Query("SortOrder") sortOrder: String = "Ascending",
        @Query("SearchTerm") searchTerm: String? = null,
        @Query("Genres") genres: String? = null,
        @Query("StartIndex") startIndex: Int? = null,
        @Query("Limit") limit: Int? = 100,
        @Query("Fields") fields: String? = "Overview,Genres,CommunityRating,RunTimeTicks,ProductionYear,UserData,SeriesName,SeasonName"
    ): Response<ItemsQueryResult>

    @GET("UserItems/Resume")
    suspend fun getResumeItemsGeneral(
        @Query("Limit") limit: Int = 12
    ): Response<ItemsQueryResult>

    @GET("Users/{userId}/Items/Resume")
    suspend fun getResumeItems(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") token: String? = null,
        @Query("Limit") limit: Int = 12
    ): Response<ItemsQueryResult>

    @GET("Users/{userId}/Items/Latest")
    suspend fun getLatestItems(
        @Path("userId") userId: String,
        @Header("X-Emby-Token") token: String? = null,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("Limit") limit: Int = 16
    ): Response<List<BaseItemDto>>

    @GET("Shows/{seriesId}/Episodes")
    suspend fun getShowEpisodes(
        @Path("seriesId") seriesId: String,
        @Header("X-Emby-Token") token: String? = null,
        @Query("userId") userId: String? = null,
        @Query("seasonId") seasonId: String? = null,
        @Query("season") seasonNumber: Int? = null,
        @Query("fields") fields: String = "Overview,Genres,CommunityRating,RunTimeTicks,ProductionYear,UserData,SeriesName,SeasonName"
    ): Response<ItemsQueryResult>

    @POST("UserFavoriteItems/{itemId}")
    suspend fun markFavorite(
        @Path("itemId") itemId: String,
        @Header("X-Emby-Token") token: String? = null
    ): Response<ResponseBody>

    @DELETE("UserFavoriteItems/{itemId}")
    suspend fun unmarkFavorite(
        @Path("itemId") itemId: String,
        @Header("X-Emby-Token") token: String? = null
    ): Response<ResponseBody>
}

/**
 * Robust OkHttp Interceptor that injects Jellyfin / Emby headers:
 * - Authorization: MediaBrowser Client="...", Device="...", DeviceId="...", Version="...", Token="..."
 * - X-Emby-Authorization: MediaBrowser Client="...", Device="...", DeviceId="...", Version="...", Token="..."
 * - X-Emby-Token
 * - X-MediaBrowser-Token
 * - Accept: application/json
 *
 * This dual-header approach ensures full compatibility with:
 * 1. Jellyfin 10.7, 10.8, 10.9, 10.10, and newer releases.
 * 2. Reverse proxies (Nginx, Caddy, Cloudflare, Traefik) that strip non-standard headers.
 * 3. Direct LAN IP connections (HTTP cleartext).
 */
class JellyfinAuthInterceptor(
    private val clientName: String = "JellyStream",
    private val appVersion: String = "1.0.0",
    private val deviceName: String = Build.MODEL ?: "Android Device",
    private val deviceId: String = JellyfinClientFactory.getOrCreateDeviceId(),
    private val tokenProvider: () -> String? = { null }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val authHeaderValue = buildString {
            append("MediaBrowser Client=\"$clientName\", ")
            append("Device=\"$deviceName\", ")
            append("DeviceId=\"$deviceId\", ")
            append("Version=\"$appVersion\"")
            if (!token.isNullOrBlank()) {
                append(", Token=\"$token\"")
            }
        }

        val requestBuilder = originalRequest.newBuilder()
            // Provide both standard Authorization and legacy X-Emby-Authorization
            .header("Authorization", authHeaderValue)
            .header("X-Emby-Authorization", authHeaderValue)
            .header("Accept", "application/json")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("X-Emby-Token", token)
            requestBuilder.header("X-MediaBrowser-Token", token)
        }

        return chain.proceed(requestBuilder.build())
    }
}

object JellyfinClientFactory {

    private var cachedDeviceId: String? = null
    private var prefs: SharedPreferences? = null

    /**
     * Initializes the client factory with an application context to persist
     * the client device ID. This prevents session revocation on app restarts.
     */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("jellyfin_auth_prefs", Context.MODE_PRIVATE)
            cachedDeviceId = prefs?.getString("device_id", null)
            if (cachedDeviceId.isNullOrBlank()) {
                val newId = "jellystream-${UUID.randomUUID().toString().replace("-", "").take(16)}"
                prefs?.edit()?.putString("device_id", newId)?.apply()
                cachedDeviceId = newId
            }
        }
    }

    fun getOrCreateDeviceId(): String {
        cachedDeviceId?.let { if (it.isNotBlank()) return it }
        val saved = prefs?.getString("device_id", null)
        if (!saved.isNullOrBlank()) {
            cachedDeviceId = saved
            return saved
        }
        val id = "jellystream-${UUID.randomUUID().toString().replace("-", "").take(16)}"
        cachedDeviceId = id
        prefs?.edit()?.putString("device_id", id)?.apply()
        return id
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    /**
     * Sanitizes and normalizes the server URL input.
     * Handles:
     * - Pasted browser URLs (e.g., http://192.168.1.100:8096/web/index.html#!/home)
     * - Missing scheme (defaults to http://)
     * - Trailing slashes, query parameters, and anchor hashes
     */
    fun sanitizeUrl(rawUrl: String): String {
        var trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""

        // Strip anchor hashes (e.g., #!/home)
        if (trimmed.contains("#")) {
            trimmed = trimmed.substringBefore("#").trim()
        }

        // Strip query parameters
        if (trimmed.contains("?")) {
            trimmed = trimmed.substringBefore("?").trim()
        }

        // Add scheme if omitted
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed = "http://$trimmed"
        }

        // Remove trailing slashes
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.dropLast(1)
        }

        // Strip common web client suffixes copied from browsers
        val webSuffixes = listOf(
            "/web/index.html",
            "/web/index.html/",
            "/web/",
            "/web"
        )
        for (suffix in webSuffixes) {
            if (trimmed.endsWith(suffix, ignoreCase = true)) {
                trimmed = trimmed.dropLast(suffix.length)
                break
            }
        }

        while (trimmed.endsWith("/")) {
            trimmed = trimmed.dropLast(1)
        }

        val parsed: HttpUrl? = trimmed.toHttpUrlOrNull()
        return parsed?.toString()?.removeSuffix("/") ?: trimmed
    }

    fun buildAuthHeader(
        clientName: String = "JellyStream",
        version: String = "1.0.0",
        deviceName: String = Build.MODEL ?: "Android",
        deviceId: String = getOrCreateDeviceId(),
        token: String? = null
    ): String {
        return buildString {
            append("MediaBrowser Client=\"$clientName\", ")
            append("Device=\"$deviceName\", ")
            append("DeviceId=\"$deviceId\", ")
            append("Version=\"$version\"")
            if (!token.isNullOrBlank()) {
                append(", Token=\"$token\"")
            }
        }
    }

    fun createApi(baseUrl: String, token: String? = null): JellyfinApi {
        val sanitized = sanitizeUrl(baseUrl)
        val normalizedBaseUrl = if (sanitized.endsWith("/")) sanitized else "$sanitized/"

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(JellyfinAuthInterceptor(tokenProvider = { token }))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JellyfinApi::class.java)
    }

    fun buildImageUrl(baseUrl: String, itemId: String, imageType: String = "Primary", tag: String? = null): String {
        val base = sanitizeUrl(baseUrl)
        val tagParam = if (!tag.isNullOrBlank()) "?tag=$tag" else ""
        return "$base/Items/$itemId/Images/$imageType$tagParam"
    }

    fun buildStreamUrl(
        baseUrl: String,
        itemId: String,
        token: String?,
        isAudio: Boolean = false,
        quality: com.example.player.QualityPreset = com.example.player.QualityPreset.AUTO
    ): String {
        val base = sanitizeUrl(baseUrl)
        val endpoint = if (isAudio) "Audio" else "Videos"
        val tokenParam = if (!token.isNullOrBlank()) "&api_key=$token" else ""

        if (isAudio || quality == com.example.player.QualityPreset.AUTO || quality == com.example.player.QualityPreset.UHD_4K) {
            return "$base/$endpoint/$itemId/stream?static=true$tokenParam"
        }

        val maxBitrate = quality.maxBitrateBps
        val maxWidth = quality.maxWidth
        val maxHeight = quality.maxHeight
        return "$base/$endpoint/$itemId/stream?static=false&maxStreamingBitrate=$maxBitrate&videoBitRate=$maxBitrate&maxWidth=$maxWidth&maxHeight=$maxHeight&videoCodec=h264,hevc,vp9&audioCodec=aac,mp3$tokenParam"
    }
}
