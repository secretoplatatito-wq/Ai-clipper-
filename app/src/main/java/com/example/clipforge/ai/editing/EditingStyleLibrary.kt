package com.example.clipforge.ai.editing

import com.example.clipforge.ai.model.NormalizedPoint
import com.example.clipforge.ai.model.PlanOperation
import com.example.clipforge.ai.model.StructuredEditingPlan
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionStyle
import java.util.UUID

enum class EditingStylePreset(
  val displayName: String,
  val description: String,
  val captionStyle: CaptionStyle,
  val defaultSpeed: Float,
  val defaultZoomPunch: Float,
  val hasVignette: Boolean
) {
  VIRAL_PODCAST(
    displayName = "Viral Podcast",
    description = "Speaker tracking, dynamic punch-ins on punchlines, and high-retention captions",
    captionStyle = CaptionStyle.VIRAL_BOLD,
    defaultSpeed = 1.0f,
    defaultZoomPunch = 1.18f,
    hasVignette = false
  ),
  MRBEAST_HIGH_ENERGY(
    displayName = "High-Energy Short",
    description = "Paced 1.15x speed, bold yellow captions, dynamic zooms, and sound emphasis",
    captionStyle = CaptionStyle.KARAOKE_POP,
    defaultSpeed = 1.15f,
    defaultZoomPunch = 1.25f,
    hasVignette = false
  ),
  CINEMATIC_STORY(
    displayName = "Cinematic Storytelling",
    description = "Atmospheric color grading, vignette, smooth pacing, and elegant serif subtitles",
    captionStyle = CaptionStyle.CINEMATIC,
    defaultSpeed = 1.0f,
    defaultZoomPunch = 1.08f,
    hasVignette = true
  ),
  EDUCATIONAL_EXPLAINER(
    displayName = "Educational Explainer",
    description = "Clear clean typography, concept emphasis, and structured pacing",
    captionStyle = CaptionStyle.PODCAST_CLEAN,
    defaultSpeed = 1.0f,
    defaultZoomPunch = 1.12f,
    hasVignette = false
  ),
  GAMING_HIGHLIGHTS(
    displayName = "Gaming Highlights",
    description = "High-octane neon captions, fast cuts, and maximum visual punch",
    captionStyle = CaptionStyle.GAMING_NEON,
    defaultSpeed = 1.10f,
    defaultZoomPunch = 1.20f,
    hasVignette = false
  )
}

object EditingPlanner {

  fun buildEditingPlan(
    projectId: String,
    style: EditingStylePreset,
    startMs: Long,
    endMs: Long,
    hookText: String,
    faceCenterX: Float = 0.5f,
    faceCenterY: Float = 0.45f
  ): StructuredEditingPlan {
    val startSec = startMs / 1000f
    val endSec = endMs / 1000f
    val operations = mutableListOf<PlanOperation>()

    // 1. Trim Operation
    operations.add(
      PlanOperation.Trim(
        startSec = startSec,
        endSec = endSec,
        reason = "Extract viral story segment"
      )
    )

    // 2. 9:16 Vertical Reframing with face tracking
    operations.add(
      PlanOperation.Reframe(
        startSec = startSec,
        endSec = endSec,
        targetAspect = AspectRatioMode.VERTICAL_9_16,
        speakerTrackingEnabled = true,
        smoothTracking = true
      )
    )

    // 3. Dynamic Punch-In Zoom at the Hook
    val hookEndSec = (startSec + 3.5f).coerceAtMost(endSec)
    operations.add(
      PlanOperation.Zoom(
        startSec = startSec,
        endSec = hookEndSec,
        scaleFrom = 1.0f,
        scaleTo = style.defaultZoomPunch,
        center = NormalizedPoint(faceCenterX, faceCenterY),
        easing = "ease_in_out",
        reason = "Emphasize opening hook statement"
      )
    )

    // 4. Caption styling
    operations.add(
      PlanOperation.Caption(
        startSec = startSec,
        endSec = endSec,
        text = hookText,
        style = style.captionStyle,
        positionY = 0.78f
      )
    )

    // 5. Visual Effect if cinematic
    if (style.hasVignette) {
      operations.add(
        PlanOperation.VisualEffect(
          startSec = startSec,
          endSec = endSec,
          effectType = "VIGNETTE",
          intensity = 0.55f
        )
      )
    }

    return StructuredEditingPlan(
      planId = UUID.randomUUID().toString(),
      projectId = projectId,
      version = 1,
      editingStyle = style.displayName,
      targetPlatform = "tiktok",
      targetAspectRatio = AspectRatioMode.VERTICAL_9_16,
      operations = operations,
      rationale = "${style.displayName} strategy: Punch-in at hook [${startSec}s-${hookEndSec}s], 9:16 vertical crop, and ${style.captionStyle.name} captions."
    )
  }
}
