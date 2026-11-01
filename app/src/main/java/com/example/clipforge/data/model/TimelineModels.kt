package com.example.clipforge.data.model

data class WordTiming(
  val word: String,
  val startMs: Long,
  val endMs: Long,
  val confidence: Float = 0.98f
)

enum class CaptionStyle {
  VIRAL_BOLD,      // Big bold yellow text with black stroke
  PODCAST_CLEAN,   // White text with subtle dark background pill
  MINIMAL,         // Simple clean bottom subtitle
  CINEMATIC,       // Elegant serif font with letter-spacing
  GAMING_NEON,     // High-energy neon green/cyan with pop animations
  KARAOKE_POP      // Current spoken word scales up in golden yellow
}

data class CaptionItem(
  val id: String,
  val text: String,
  val startMs: Long,
  val endMs: Long,
  val words: List<WordTiming> = emptyList(),
  val style: CaptionStyle = CaptionStyle.VIRAL_BOLD,
  val positionY: Float = 0.78f // Normalized Y position (0.0 top, 1.0 bottom)
)

enum class AspectRatioMode(val label: String, val ratioWidth: Int, val ratioHeight: Int) {
  VERTICAL_9_16("9:16 (TikTok, Reels, Shorts)", 9, 16),
  HORIZONTAL_16_9("16:9 (YouTube, Landscape)", 16, 9),
  SQUARE_1_1("1:1 (Square, Feed)", 1, 1),
  PORTRAIT_4_5("4:5 (Instagram Feed)", 4, 5)
}

data class VideoSegment(
  val id: String,
  val sourceUri: String,
  val startTrimMs: Long,
  val endTrimMs: Long,
  val speed: Float = 1.0f,
  val volume: Float = 1.0f,
  val zoomScale: Float = 1.0f,
  val panCenterX: Float = 0.5f, // Normalized 0..1
  val panCenterY: Float = 0.5f  // Normalized 0..1
)

data class AudioTrackItem(
  val id: String,
  val title: String,
  val uri: String,
  val startMs: Long,
  val durationMs: Long,
  val volume: Float = 0.7f,
  val isDucked: Boolean = true // auto-duck volume under speech
)

data class VisualEffect(
  val id: String,
  val type: String, // "VIGNETTE", "WARM_GRADE", "COOL_GRADE", "HIGH_CONTRAST", "SHARPEN"
  val intensity: Float = 0.5f,
  val startMs: Long = 0,
  val endMs: Long = Long.MAX_VALUE
)

data class Timeline(
  val width: Int = 1080,
  val height: Int = 1920,
  val fps: Int = 30,
  val durationMs: Long = 0,
  val targetAspect: AspectRatioMode = AspectRatioMode.VERTICAL_9_16,
  val segments: List<VideoSegment> = emptyList(),
  val captions: List<CaptionItem> = emptyList(),
  val audioTracks: List<AudioTrackItem> = emptyList(),
  val effects: List<VisualEffect> = emptyList()
)

data class ViralScoreBreakdown(
  val hook: Int,             // 0-100
  val curiosity: Int,        // 0-100
  val emotionalImpact: Int,  // 0-100
  val infoValue: Int,        // 0-100
  val storyCompleteness: Int,// 0-100
  val shareability: Int,     // 0-100
  val visualEngagement: Int, // 0-100
  val audioEngagement: Int,  // 0-100
  val pacing: Int,           // 0-100
  val overallScore: Int      // 0-100
)

enum class AIProviderType {
  GEMINI,
  OPENAI,
  CLAUDE,
  DEEPSEEK,
  OPENROUTER,
  OLLAMA_LOCAL,
  LM_STUDIO_LOCAL,
  CUSTOM_SERVER
}
