package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PublicSystemInfo(
    @Json(name = "ServerName") val serverName: String? = null,
    @Json(name = "Version") val version: String? = null,
    @Json(name = "Id") val id: String? = null,
    @Json(name = "OperatingSystem") val operatingSystem: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthenticateByNameRequest(
    @Json(name = "Username") val username: String,
    @Json(name = "Pw") val pw: String? = "",
    @Json(name = "Password") val password: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthenticationResult(
    @Json(name = "AccessToken") val accessToken: String? = null,
    @Json(name = "User") val user: JellyfinUserDto? = null,
    @Json(name = "SessionInfo") val sessionInfo: SessionInfoDto? = null,
    @Json(name = "ServerId") val serverId: String? = null
)

@JsonClass(generateAdapter = true)
data class SessionInfoDto(
    @Json(name = "Id") val id: String? = null,
    @Json(name = "UserId") val userId: String? = null,
    @Json(name = "UserName") val userName: String? = null
)

@JsonClass(generateAdapter = true)
data class JellyfinUserDto(
    @Json(name = "Id") val id: String? = null,
    @Json(name = "Name") val name: String? = null,
    @Json(name = "ServerId") val serverId: String? = null
)

@JsonClass(generateAdapter = true)
data class ItemsQueryResult(
    @Json(name = "Items") val items: List<BaseItemDto>? = null,
    @Json(name = "TotalRecordCount") val totalRecordCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class BaseItemDto(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String? = null,
    @Json(name = "Type") val type: String? = null,
    @Json(name = "CollectionType") val collectionType: String? = null,
    @Json(name = "ChildCount") val childCount: Int? = null,
    @Json(name = "SeasonCount") val seasonCount: Int? = null,
    @Json(name = "SeriesCount") val seriesCount: Int? = null,
    @Json(name = "Overview") val overview: String? = null,
    @Json(name = "ProductionYear") val productionYear: Int? = null,
    @Json(name = "CommunityRating") val communityRating: Float? = null,
    @Json(name = "RunTimeTicks") val runTimeTicks: Long? = null,
    @Json(name = "SeriesName") val seriesName: String? = null,
    @Json(name = "SeasonName") val seasonName: String? = null,
    @Json(name = "IndexNumber") val indexNumber: Int? = null,
    @Json(name = "ParentIndexNumber") val parentIndexNumber: Int? = null,
    @Json(name = "Genres") val genres: List<String>? = null,
    @Json(name = "ImageTags") val imageTags: Map<String, String>? = null,
    @Json(name = "BackdropImageTags") val backdropImageTags: List<String>? = null,
    @Json(name = "UserData") val userData: UserItemDataDto? = null
)

@JsonClass(generateAdapter = true)
data class UserItemDataDto(
    @Json(name = "PlaybackPositionTicks") val playbackPositionTicks: Long? = null,
    @Json(name = "PlayCount") val playCount: Int? = null,
    @Json(name = "IsFavorite") val isFavorite: Boolean? = null,
    @Json(name = "Played") val played: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class QuickConnectInitiateResponse(
    @Json(name = "Code") val code: String? = null,
    @Json(name = "Secret") val secret: String? = null,
    @Json(name = "Authenticated") val authenticated: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class QuickConnectConnectResponse(
    @Json(name = "Authenticated") val authenticated: Boolean? = null,
    @Json(name = "Secret") val secret: String? = null
)

@JsonClass(generateAdapter = true)
data class QuickConnectAuthRequest(
    @Json(name = "Secret") val secret: String
)

@JsonClass(generateAdapter = true)
data class PublicUserDto(
    @Json(name = "Id") val id: String? = null,
    @Json(name = "Name") val name: String? = null,
    @Json(name = "HasPassword") val hasPassword: Boolean? = null,
    @Json(name = "PrimaryImageTag") val primaryImageTag: String? = null
)

data class JellyfinLibraryView(
    val id: String,
    val name: String,
    val collectionType: String? = null,
    val type: MediaType? = null,
    val itemCount: Int? = null,
    val iconName: String? = null
)

