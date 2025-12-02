package com.example.notasapp.media

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM media ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaEntity): Long

    @Delete
    suspend fun delete(media: MediaEntity)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteById(id: Long)
}