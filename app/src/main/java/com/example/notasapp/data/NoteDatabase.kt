package com.example.notasapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notasapp.media.MediaDao
import com.example.notasapp.media.MediaEntity

@Database(
    entities = [
        Note::class,
        Task::class,
        MediaEntity::class   // ← Agregado
    ],
    version = 4,            // ← Subir versión
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun mediaDao(): MediaDao   // ← Agregado

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .fallbackToDestructiveMigration()  // Borra y recrea si hay cambios
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}