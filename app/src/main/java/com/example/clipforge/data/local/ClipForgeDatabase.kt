package com.example.clipforge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.clipforge.data.model.AIProviderEntity
import com.example.clipforge.data.model.ClipEntity
import com.example.clipforge.data.model.ProjectEntity

@Database(
  entities = [ProjectEntity::class, ClipEntity::class, AIProviderEntity::class],
  version = 1,
  exportSchema = false
)
abstract class ClipForgeDatabase : RoomDatabase() {
  abstract fun projectDao(): ProjectDao
  abstract fun clipDao(): ClipDao
  abstract fun aiProviderDao(): AIProviderDao

  companion object {
    @Volatile
    private var INSTANCE: ClipForgeDatabase? = null

    fun getDatabase(context: Context): ClipForgeDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ClipForgeDatabase::class.java,
          "clipforge_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
