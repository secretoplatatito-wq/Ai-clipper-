package com.example.clipforge.ai.model

import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionStyle

data class NormalizedPoint(
  val x: Float, // 0.0 to 1.0 (0.5 is horizontal center)
  val y: Float  // 0.0 to 1.0 (0.5 is vertical center)
)

sealed class PlanOperation {
  abstract val type: String
  abstract val startSec: Float
  abstract val endSec: Float

  data class Trim(
    override val startSec: Float,
    override val endSec: Float,
    val reason: String = "Select viral segment"
  ) : PlanOperation() {
    override val type = "trim"
  }

  data class Zoom(
    override val startSec: Float,
    override val endSec: Float,
    val scaleFrom: Float = 1.0f,
    val scaleTo: Float = 1.2f,
    val center: NormalizedPoint = NormalizedPoint(0.5f, 0.45f),
    val easing: String = "ease_in_out",
    val reason: String = "Emphasize key revelation"
  ) : PlanOperation() {
    override val type = "zoom"
  }

  data class Reframe(
    override val startSec: Float,
    override val endSec: Float,
    val targetAspect: AspectRatioMode = AspectRatioMode.VERTICAL_9_16,
    val speakerTrackingEnabled: Boolean = true,
    val smoothTracking: Boolean = true
  ) : PlanOperation() {
    override val type = "reframe"
  }

  data class Caption(
    override val startSec: Float,
    override val endSec: Float,
    val text: String,
    val style: CaptionStyle = CaptionStyle.VIRAL_BOLD,
    val highlightWords: List<String> = emptyList(),
    val positionY: Float = 0.78f
  ) : PlanOperation() {
    override val type = "caption"
  }

  data class Transition(
    override val startSec: Float,
    val durationSec: Float = 0.4f,
    val style: String = "cross_dissolve" // cut, cross_dissolve, zoom, fade_to_black
  ) : PlanOperation() {
    override val type = "transition"
    override val endSec = startSec + durationSec
  }

  data class Audio(
    override val startSec: Float,
    override val endSec: Float,
    val operation: String = "duck_music", // duck_music, enhance_dialogue, sound_effect
    val volume: Float = 0.5f,
    val soundEffectType: String? = null
  ) : PlanOperation() {
    override val type = "audio"
  }

  data class VisualEffect(
    override val startSec: Float,
    override val endSec: Float,
    val effectType: String = "VIGNETTE",
    val intensity: Float = 0.6f
  ) : PlanOperation() {
    override val type = "effect"
  }
}

data class StructuredEditingPlan(
  val planId: String,
  val projectId: String,
  val version: Int = 1,
  val editingStyle: String, // "dynamic_podcast", "high_energy_short", "cinematic", "educational"
  val targetPlatform: String = "tiktok",
  val targetAspectRatio: AspectRatioMode = AspectRatioMode.VERTICAL_9_16,
  val operations: List<PlanOperation>,
  val rationale: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

data class QualityScoreReport(
  val storytellingScore: Int,    // 0-100
  val captionAccuracyScore: Int, // 0-100
  val visualFramingScore: Int,   // 0-100
  val audioQualityScore: Int,    // 0-100
  val editingConsistency: Int,   // 0-100
  val technicalQuality: Int,     // 0-100
  val overallQuality: Int,       // 0-100
  val checksPassed: List<String> = emptyList(),
  val warnings: List<String> = emptyList(),
  val suggestedCorrections: List<String> = emptyList()
)

data class ToolExecutionResult(
  val toolName: String,
  val success: Boolean,
  val message: String,
  val data: Any? = null
)
