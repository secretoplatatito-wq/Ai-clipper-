package com.example.clipforge.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.clipforge.data.model.AIProviderEntity
import com.example.clipforge.data.model.ClipEntity
import com.example.clipforge.data.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE id = :id")
  suspend fun getProjectById(id: String): ProjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity)

  @Update
  suspend fun updateProject(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProjectById(id: String)
}

@Dao
interface ClipDao {
  @Query("SELECT * FROM clips WHERE projectId = :projectId ORDER BY overallScore DESC")
  fun getClipsForProject(projectId: String): Flow<List<ClipEntity>>

  @Query("SELECT * FROM clips WHERE id = :clipId")
  suspend fun getClipById(clipId: String): ClipEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClips(clips: List<ClipEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClip(clip: ClipEntity)

  @Update
  suspend fun updateClip(clip: ClipEntity)

  @Query("DELETE FROM clips WHERE projectId = :projectId")
  suspend fun deleteClipsForProject(projectId: String)

  @Query("SELECT * FROM clips WHERE isExported = 1 ORDER BY createdAt DESC")
  fun getExportedClips(): Flow<List<ClipEntity>>
}

@Dao
interface AIProviderDao {
  @Query("SELECT * FROM ai_providers")
  fun getAllProviders(): Flow<List<AIProviderEntity>>

  @Query("SELECT * FROM ai_providers WHERE id = :id")
  suspend fun getProviderById(id: String): AIProviderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProvider(provider: AIProviderEntity)

  @Update
  suspend fun updateProvider(provider: AIProviderEntity)

  @Delete
  suspend fun deleteProvider(provider: AIProviderEntity)
}
