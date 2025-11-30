package com.example.notasapp.media

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val type: String = "image", // por si agregas video después
    val createdAt: Long = System.currentTimeMillis()
)
