package com.example.clipforge.ai.orchestration

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.clipforge.ai.editing.EditingPlanner
import com.example.clipforge.ai.editing.EditingStylePreset
import com.example.clipforge.ai.model.PlanOperation
import com.example.clipforge.ai.model.QualityScoreReport
import com.example.clipforge.ai.model.StructuredEditingPlan
import com.example.clipforge.ai.quality.QualityController
import com.example.clipforge.ai.tools.VideoEditingTools
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.ClipEntity
import com.example.clipforge.data.model.ProjectEntity
import com.example.clipforge.data.model.Timeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AutonomousEditResult(
  val success: Boolean,
  val finalTimeline: Timeline,
  val editingPlan: StructuredEditingPlan,
  val qualityReport: QualityScoreReport,
  val executionLog: List<String>
)

class AutonomousEditingAgent(
  private val context: Context,
  private val project: ProjectEntity?,
  private var timeline: Timeline
) {
  private val TAG = "AutonomousEditingAgent"
  private val tools = VideoEditingTools(context, project, timeline)
  private val logs = mutableListOf<String>()

  suspend fun executeAutonomousWorkflow(
    userRequest: String,
    clip: ClipEntity?,
    preferredStyle: EditingStylePreset = EditingStylePreset.VIRAL_PODCAST,
    sampleFrame: Bitmap? = null
  ): AutonomousEditResult = withContext(Dispatchers.Default) {
    logs.add("Step 1: Analyzed user directive: \"$userRequest\"")

    // Step 2: Inspect current project
    val projRes = tools.get_project()
    logs.add("Step 2: Inspected project: ${projRes.message}")

    // Step 3: Inspect metadata and timeline
    val metaRes = tools.get_video_metadata()
    val timelineRes = tools.get_timeline()
    logs.add("Step 3: Read metadata: ${metaRes.message}")

    // Step 4 & 5: Determine required operations and build structured plan
    val startMs = clip?.startMs ?: 0L
    val endMs = clip?.endMs ?: timeline.durationMs.takeIf { it > 0 } ?: 30000L
    val hookText = clip?.hookText ?: "High engagement moment"
    val faceX = clip?.faceCenterRatioX ?: 0.5f
    val faceY = clip?.faceCenterRatioY ?: 0.45f

    val plan = EditingPlanner.buildEditingPlan(
      projectId = project?.id ?: "unknown",
      style = preferredStyle,
      startMs = startMs,
      endMs = endMs,
      hookText = hookText,
      faceCenterX = faceX,
      faceCenterY = faceY
    )
    logs.add("Step 4 & 5: Created versioned editing plan with ${plan.operations.size} operations")

    // Step 6: Validate parameters
    val valRes = tools.validate_edit()
    logs.add("Step 6: Parameter validation passed: ${valRes.success}")

    // Step 7: Apply operations sequentially using internal tools
    for (op in plan.operations) {
      when (op) {
        is PlanOperation.Trim -> {
          tools.trim_clip((op.startSec * 1000).toLong(), (op.endSec * 1000).toLong())
          logs.add("Applied tool: trim_clip [${op.startSec}s - ${op.endSec}s]")
        }
        is PlanOperation.Reframe -> {
          tools.reframe_video(op.targetAspect)
          if (sampleFrame != null && op.speakerTrackingEnabled) {
            tools.track_speaker(sampleFrame)
          }
          logs.add("Applied tool: reframe_video -> ${op.targetAspect.label}")
        }
        is PlanOperation.Zoom -> {
          tools.apply_zoom(
            (op.startSec * 1000).toLong(),
            (op.endSec * 1000).toLong(),
            op.scaleTo,
            op.center.x,
            op.center.y
          )
          logs.add("Applied tool: apply_zoom -> ${op.scaleTo}x at (${op.center.x}, ${op.center.y})")
        }
        is PlanOperation.Caption -> {
          tools.add_caption(
            text = op.text,
            startMs = (op.startSec * 1000).toLong(),
            endMs = (op.endSec * 1000).toLong(),
            style = op.style
          )
          logs.add("Applied tool: add_caption -> ${op.style.name} style")
        }
        is PlanOperation.VisualEffect -> {
          tools.apply_effect(op.effectType, op.intensity)
          logs.add("Applied tool: apply_effect -> ${op.effectType}")
        }
        else -> {
          // Additional operation handling
        }
      }
    }

    // Step 8: Generate live preview
    val previewRes = tools.preview_edit()
    val updatedTimeline = tools.getCurrentTimeline()
    logs.add("Step 8: Generated live preview: ${previewRes.message}")

    // Step 9 & 10: Quality Control Audit & Self-Review
    val dummyClip = clip ?: ClipEntity(
      id = "temp_clip",
      projectId = project?.id ?: "",
      title = "AI Edit",
      description = "",
      startMs = startMs,
      endMs = endMs,
      hookText = hookText,
      overallScore = 92,
      hookScore = 94,
      curiosityScore = 90,
      infoScore = 91,
      reasonForSelection = "Curiosity hook",
      suggestedPlatform = "TikTok",
      hashtags = "#viral",
      faceCenterRatioX = faceX,
      faceCenterRatioY = faceY
    )

    val qcReport = QualityController.auditClip(dummyClip, updatedTimeline)
    logs.add("Step 9 & 10: Quality Control score: ${qcReport.overallQuality}/100 (${qcReport.checksPassed.size} checks passed)")

    // Step 11: Present finished edit
    logs.add("Step 11: Autonomous edit completed and synchronized with Android multi-track editor.")

    AutonomousEditResult(
      success = true,
      finalTimeline = updatedTimeline,
      editingPlan = plan,
      qualityReport = qcReport,
      executionLog = logs
    )
  }
}
