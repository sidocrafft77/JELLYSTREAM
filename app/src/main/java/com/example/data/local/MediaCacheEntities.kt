package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CastMember
import com.example.data.model.JellyfinLibraryView
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "cached_media_items",
    primaryKeys = ["itemId", "serverId"]
)
data class CachedMediaItemEntity(
    val itemId: String,
    val serverId: String,
    val title: String,
    val type: String,
    val overview: String = "",
    val year: Int? = null,
    val communityRating: Float? = null,
    val durationMs: Long = 0L,
    val genres: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val streamUrl: String = "",
    val seriesName: String? = null,
    val seasonName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val playbackPositionMs: Long = 0L,
    val isFavorite: Boolean = false,
    val resolutionTag: String = "1080p",
    val hasHdr: Boolean = false,
    val audioChannels: String = "5.1",
    val artistName: String? = null,
    val albumName: String? = null,
    val parentId: String? = null,
    val castData: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedMediaItemEntity.toMediaItem(): MediaItem {
    val mediaType = try {
        MediaType.valueOf(type)
    } catch (_: Exception) {
        MediaType.MOVIE
    }

    val parsedCast: List<CastMember> = if (!castData.isNullOrBlank()) {
        castData.split(";;").mapNotNull { entry ->
            val parts = entry.split("||")
            if (parts.size >= 2) {
                CastMember(
                    name = parts[0],
                    role = parts[1],
                    avatarUrl = if (parts.size > 2 && parts[2].isNotBlank()) parts[2] else null
                )
            } else null
        }
    } else emptyList()

    return MediaItem(
        id = itemId,
        title = title,
        type = mediaType,
        overview = overview,
        year = year,
        communityRating = communityRating,
        durationMs = durationMs,
        genres = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() },
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        streamUrl = streamUrl,
        seriesName = seriesName,
        seasonName = seasonName,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        playbackPositionMs = playbackPositionMs,
        isFavorite = isFavorite,
        resolutionTag = resolutionTag,
        hasHdr = hasHdr,
        audioChannels = audioChannels,
        artistName = artistName,
        albumName = albumName,
        cast = parsedCast
    )
}

fun MediaItem.toCachedEntity(serverId: String, parentId: String? = null): CachedMediaItemEntity {
    val serializedCast = if (cast.isNotEmpty()) {
        cast.joinToString(";;") { member ->
            "${member.name}||${member.role}||${member.avatarUrl ?: ""}"
        }
    } else null

    return CachedMediaItemEntity(
        itemId = id,
        serverId = serverId,
        title = title,
        type = type.name,
        overview = overview,
        year = year,
        communityRating = communityRating,
        durationMs = durationMs,
        genres = genres.joinToString(","),
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        streamUrl = streamUrl,
        seriesName = seriesName,
        seasonName = seasonName,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        playbackPositionMs = playbackPositionMs,
        isFavorite = isFavorite,
        resolutionTag = resolutionTag,
        hasHdr = hasHdr,
        audioChannels = audioChannels,
        artistName = artistName,
        albumName = albumName,
        parentId = parentId,
        castData = serializedCast
    )
}

@Dao
interface MediaCacheDao {
    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId ORDER BY title ASC")
    fun getAllCachedMediaFlow(serverId: String): Flow<List<CachedMediaItemEntity>>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId ORDER BY title ASC")
    suspend fun getAllCachedMedia(serverId: String): List<CachedMediaItemEntity>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND type = :type ORDER BY title ASC")
    suspend fun getCachedMediaByType(serverId: String, type: String): List<CachedMediaItemEntity>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND type = :type ORDER BY title ASC")
    fun getCachedMediaByTypeFlow(serverId: String, type: String): Flow<List<CachedMediaItemEntity>>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND (parentId = :parentId OR (:parentId IS NULL AND parentId IS NULL)) ORDER BY title ASC")
    suspend fun getCachedMediaByParent(serverId: String, parentId: String?): List<CachedMediaItemEntity>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND type = 'EPISODE' AND (seriesName = :seriesName OR :seriesName = '') ORDER BY episodeNumber ASC")
    suspend fun getCachedEpisodes(serverId: String, seriesName: String): List<CachedMediaItemEntity>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND type = 'EPISODE' AND (seriesName = :seriesName OR parentId = :seriesId OR :seriesName = '') ORDER BY episodeNumber ASC")
    suspend fun getCachedEpisodesForSeries(serverId: String, seriesName: String, seriesId: String): List<CachedMediaItemEntity>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND playbackPositionMs > 0 ORDER BY cachedAt DESC LIMIT 20")
    suspend fun getCachedContinueWatching(serverId: String): List<CachedMediaItemEntity>

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND itemId = :itemId LIMIT 1")
    suspend fun getCachedItemById(serverId: String, itemId: String): CachedMediaItemEntity?

    @Query("SELECT * FROM cached_media_items WHERE serverId = :serverId AND (title LIKE '%' || :query || '%' OR overview LIKE '%' || :query || '%') ORDER BY title ASC")
    suspend fun searchCachedMedia(serverId: String, query: String): List<CachedMediaItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<CachedMediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: CachedMediaItemEntity)

    @Query("UPDATE cached_media_items SET playbackPositionMs = :positionMs WHERE serverId = :serverId AND itemId = :itemId")
    suspend fun updatePlaybackPosition(serverId: String, itemId: String, positionMs: Long)

    @Query("UPDATE cached_media_items SET isFavorite = :isFavorite WHERE serverId = :serverId AND itemId = :itemId")
    suspend fun updateFavorite(serverId: String, itemId: String, isFavorite: Boolean)

    @Query("DELETE FROM cached_media_items WHERE serverId = :serverId")
    suspend fun clearCacheForServer(serverId: String)

    @Query("SELECT COUNT(*) FROM cached_media_items WHERE serverId = :serverId")
    suspend fun getCacheCount(serverId: String): Int
}

@Entity(
    tableName = "cached_library_views",
    primaryKeys = ["viewId", "serverId"]
)
data class CachedLibraryViewEntity(
    val viewId: String,
    val serverId: String,
    val name: String,
    val collectionType: String? = null,
    val type: String? = null,
    val itemCount: Int? = null,
    val iconName: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedLibraryViewEntity.toLibraryView(): JellyfinLibraryView {
    val mediaType = type?.let {
        try {
            MediaType.valueOf(it)
        } catch (_: Exception) {
            null
        }
    }
    return JellyfinLibraryView(
        id = viewId,
        name = name,
        collectionType = collectionType,
        type = mediaType,
        itemCount = itemCount,
        iconName = iconName
    )
}

fun JellyfinLibraryView.toCachedEntity(serverId: String): CachedLibraryViewEntity {
    return CachedLibraryViewEntity(
        viewId = id,
        serverId = serverId,
        name = name,
        collectionType = collectionType,
        type = type?.name,
        itemCount = itemCount,
        iconName = iconName
    )
}

@Dao
interface LibraryViewCacheDao {
    @Query("SELECT * FROM cached_library_views WHERE serverId = :serverId ORDER BY name ASC")
    fun getCachedViewsFlow(serverId: String): Flow<List<CachedLibraryViewEntity>>

    @Query("SELECT * FROM cached_library_views WHERE serverId = :serverId ORDER BY name ASC")
    suspend fun getCachedViews(serverId: String): List<CachedLibraryViewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViews(views: List<CachedLibraryViewEntity>)

    @Query("DELETE FROM cached_library_views WHERE serverId = :serverId")
    suspend fun clearViewsForServer(serverId: String)
}
