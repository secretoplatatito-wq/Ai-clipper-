package com.example.clipforge.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey val id: String,
  val name: String,
  val videoUri: String,
  val durationMs: Long,
  val width: Int,
  val height: Int,
  val fps: Int,
  val fileSize: Long,
  val thumbnailPath: String? = null,
  val status: String = "READY", // "PROCESSING", "READY", "EXPORTED", "ERROR"
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val transcriptJson: String? = null,
  val timelineJson: String? = null
)

@Entity(tableName = "clips")
data class ClipEntity(
  @PrimaryKey val id: String,
  val projectId: String,
  val title: String,
  val description: String,
  val startMs: Long,
  val endMs: Long,
  val hookText: String,
  val overallScore: Int,
  val hookScore: Int,
  val curiosityScore: Int,
  val infoScore: Int,
  val reasonForSelection: String,
  val suggestedPlatform: String, // "TikTok", "YouTube Shorts", "Instagram Reels"
  val hashtags: String,
  val faceCenterRatioX: Float = 0.5f,
  val faceCenterRatioY: Float = 0.45f,
  val zoomScale: Float = 1.15f,
  val renderedFilePath: String? = null,
  val isExported: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_providers")
data class AIProviderEntity(
  @PrimaryKey val id: String,
  val name: String,
  val type: String, // "GEMINI", "OPENAI", "CLAUDE", "DEEPSEEK", "OPENROUTER", "OLLAMA_LOCAL", "LM_STUDIO_LOCAL", "CUSTOM_SERVER"
  val baseUrl: String,
  val apiKey: String,
  val selectedModel: String,
  val isEnabled: Boolean = true,
  val isLocal: Boolean = false,
  val availableModels: String = "" // comma separated
)
