package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val itemId: String,
    val serverId: String,
    val title: String,
    val type: String,
    val posterUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE serverId = :serverId ORDER BY timestamp DESC")
    fun getFavoritesForServer(serverId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId AND serverId = :serverId)")
    fun isFavorite(itemId: String, serverId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE itemId = :itemId AND serverId = :serverId")
    suspend fun deleteFavorite(itemId: String, serverId: String)
}

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val itemId: String,
    val serverId: String,
    val title: String,
    val type: String,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val streamUrl: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history WHERE serverId = :serverId ORDER BY lastWatchedTimestamp DESC LIMIT 20")
    fun getHistoryForServer(serverId: String): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE itemId = :itemId AND serverId = :serverId LIMIT 1")
    suspend fun getHistoryItem(itemId: String, serverId: String): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordProgress(item: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE itemId = :itemId AND serverId = :serverId")
    suspend fun deleteHistory(itemId: String, serverId: String)
}
