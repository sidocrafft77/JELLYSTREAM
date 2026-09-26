package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val userId: String? = null,
    val userName: String? = null,
    val accessToken: String? = null,
    val isDemo: Boolean = false,
    val isActive: Boolean = false,
    val serverVersion: String? = null,
    val lastConnected: Long = System.currentTimeMillis()
)

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY lastConnected DESC")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE isActive = 1 LIMIT 1")
    fun getActiveServer(): Flow<ServerEntity?>

    @Query("SELECT * FROM servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)

    @Update
    suspend fun updateServer(server: ServerEntity)

    @Query("UPDATE servers SET isActive = 0")
    suspend fun deactivateAllServers()

    @Query("UPDATE servers SET isActive = 1, lastConnected = :timestamp WHERE id = :id")
    suspend fun setActiveServer(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServer(id: String)
}
