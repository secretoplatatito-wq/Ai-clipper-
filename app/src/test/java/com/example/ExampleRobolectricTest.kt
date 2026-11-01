package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.clipforge.ai.brain.MultiAgentAIBrain
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionStyle
import com.example.clipforge.data.model.Timeline
import com.example.clipforge.data.model.VideoSegment
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ClipForge AI", appName)
  }

  @Test
  fun `multi-agent transcript engine generates word level timestamps`() = runTest {
    val segments = MultiAgentAIBrain.generateTranscript(videoDurationMs = 45000L)
    assertTrue("Transcript segments should not be empty", segments.isNotEmpty())
    val first = segments.first()
    assertTrue("First segment must have start >= 0", first.startMs >= 0)
    assertTrue("First segment words must have timing", first.words.isNotEmpty())
    assertTrue("First word start should match segment start", first.words.first().startMs >= first.startMs)
  }

  @Test
  fun `viral moment detection generates scored candidate clips`() = runTest {
    val transcript = MultiAgentAIBrain.generateTranscript(videoDurationMs = 60000L)
    val clips = MultiAgentAIBrain.detectViralClips(
      projectId = "test_proj_001",
      videoDurationMs = 60000L,
      transcript = transcript,
      targetClipCount = 3,
      preferredDurationSec = 30,
      clipStyle = "Viral moments"
    )

    assertEquals(3, clips.size)
    for (clip in clips) {
      assertTrue("Overall score must be between 70 and 100", clip.overallScore in 70..100)
      assertTrue("Hook score must be high", clip.hookScore >= 70)
      assertTrue("Clip end must be greater than start", clip.endMs > clip.startMs)
      assertNotNull("Clip title must exist", clip.title)
    }
  }

  @Test
  fun `ai copilot command updates timeline correctly`() {
    val initialTimeline = Timeline(
      durationMs = 30000L,
      targetAspect = AspectRatioMode.HORIZONTAL_16_9,
      segments = listOf(
        VideoSegment(
          id = UUID.randomUUID().toString(),
          sourceUri = "test.mp4",
          startTrimMs = 0L,
          endTrimMs = 30000L,
          speed = 1.0f
        )
      )
    )

    var logReceived = ""
    val updatedVertical = MultiAgentAIBrain.executeCopilotCommand("Make this video vertical 9:16", initialTimeline) {
      logReceived = it
    }
    assertEquals(AspectRatioMode.VERTICAL_9_16, updatedVertical.targetAspect)
    assertTrue(logReceived.contains("9:16"))

    val updatedSpeed = MultiAgentAIBrain.executeCopilotCommand("Speed up to 1.25x", updatedVertical) {}
    assertEquals(1.25f, updatedSpeed.segments.first().speed, 0.01f)
  }

  @Test
  fun `editing planner generates structured operations matching selected preset`() {
    val plan = com.example.clipforge.ai.editing.EditingPlanner.buildEditingPlan(
      projectId = "proj_test_101",
      style = com.example.clipforge.ai.editing.EditingStylePreset.VIRAL_PODCAST,
      startMs = 5000L,
      endMs = 35000L,
      hookText = "The biggest mistake everyone makes",
      faceCenterX = 0.52f,
      faceCenterY = 0.42f
    )

    assertEquals("proj_test_101", plan.projectId)
    assertTrue("Plan operations must not be empty", plan.operations.isNotEmpty())
    
    // Check for trim, reframe, zoom, caption, and audio operations
    val hasTrim = plan.operations.any { it is com.example.clipforge.ai.model.PlanOperation.Trim }
    val hasReframe = plan.operations.any { it is com.example.clipforge.ai.model.PlanOperation.Reframe }
    val hasCaption = plan.operations.any { it is com.example.clipforge.ai.model.PlanOperation.Caption }
    assertTrue("Plan must contain trim operation", hasTrim)
    assertTrue("Plan must contain vertical reframe operation", hasReframe)
    assertTrue("Plan must contain hook caption operation", hasCaption)
  }

  @Test
  fun `quality controller provides thorough quality scores and checks`() {
    val clip = com.example.clipforge.data.model.ClipEntity(
      id = "clip_qc_001",
      projectId = "proj_001",
      title = "Why 99% Fail in Business",
      description = "Core secret explained",
      startMs = 0L,
      endMs = 45000L,
      hookText = "The secret why 99% of creators fail",
      overallScore = 95,
      hookScore = 96,
      curiosityScore = 94,
      infoScore = 92,
      reasonForSelection = "Proven curiosity trigger",
      suggestedPlatform = "TikTok",
      hashtags = "#business #success",
      faceCenterRatioX = 0.5f,
      faceCenterRatioY = 0.4f
    )

    val timeline = Timeline(
      durationMs = 45000L,
      targetAspect = AspectRatioMode.VERTICAL_9_16,
      segments = listOf(
        VideoSegment(
          id = "seg_1",
          sourceUri = "test.mp4",
          startTrimMs = 0L,
          endTrimMs = 45000L
        )
      ),
      captions = listOf(
        com.example.clipforge.data.model.CaptionItem(
          id = "cap_1",
          text = "The secret why 99% of creators fail",
          startMs = 0L,
          endMs = 3000L
        )
      )
    )

    val report = com.example.clipforge.ai.quality.QualityController.auditClip(clip, timeline)
    assertTrue("Overall quality score should be high", report.overallQuality >= 80)
    assertTrue("Must have passed opening hook check", report.checksPassed.any { it.contains("hook", ignoreCase = true) })
    assertTrue("Must have validated captions", report.checksPassed.any { it.contains("subtitle", ignoreCase = true) || it.contains("caption", ignoreCase = true) })
    assertTrue("Speaker face must be centered", report.checksPassed.any { it.contains("framed", ignoreCase = true) })
  }

  @Test
  fun `video editing tools perform inspection and timeline manipulation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val project = com.example.clipforge.data.model.ProjectEntity(
      id = "proj_tool_test",
      name = "Test Video",
      videoUri = "content://media/external/video/media/1",
      durationMs = 60000L,
      width = 1920,
      height = 1080,
      fps = 30,
      fileSize = 10485760L
    )

    val initialTimeline = Timeline(
      durationMs = 60000L,
      targetAspect = AspectRatioMode.HORIZONTAL_16_9,
      segments = listOf(
        VideoSegment(
          id = "seg_01",
          sourceUri = "content://media/external/video/media/1",
          startTrimMs = 0L,
          endTrimMs = 60000L
        )
      )
    )

    val tools = com.example.clipforge.ai.tools.VideoEditingTools(context, project, initialTimeline)
    val projRes = tools.get_project()
    assertTrue(projRes.success)

    // Test reframe
    val reframeRes = tools.reframe_video(AspectRatioMode.VERTICAL_9_16)
    assertTrue(reframeRes.success)
    assertEquals(AspectRatioMode.VERTICAL_9_16, tools.getCurrentTimeline().targetAspect)

    // Test trim
    val trimRes = tools.trim_clip(10000L, 40000L)
    assertTrue(trimRes.success)
    val updatedSeg = tools.getCurrentTimeline().segments.first()
    assertEquals(10000L, updatedSeg.startTrimMs)
    assertEquals(40000L, updatedSeg.endTrimMs)

    // Test add caption
    val captionRes = tools.add_caption("Mindblowing point!", 10000L, 13000L, CaptionStyle.VIRAL_BOLD)
    assertTrue(captionRes.success)
    assertEquals(1, tools.getCurrentTimeline().captions.size)
  }

  @Test
  fun `autonomous editing agent runs full workflow successfully`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val project = com.example.clipforge.data.model.ProjectEntity(
      id = "proj_auto_test",
      name = "Autonomous Test Video",
      videoUri = "content://media/external/video/media/2",
      durationMs = 60000L,
      width = 1920,
      height = 1080,
      fps = 30,
      fileSize = 10485760L
    )

    val initialTimeline = Timeline(
      durationMs = 60000L,
      targetAspect = AspectRatioMode.HORIZONTAL_16_9,
      segments = listOf(
        VideoSegment(
          id = "seg_auto_01",
          sourceUri = "content://media/external/video/media/2",
          startTrimMs = 0L,
          endTrimMs = 60000L
        )
      )
    )

    val clip = com.example.clipforge.data.model.ClipEntity(
      id = "clip_auto_001",
      projectId = project.id,
      title = "The Billionaire Mindset Secret",
      description = "Clip explaining the rule",
      startMs = 5000L,
      endMs = 35000L,
      hookText = "The secret to scaling faster",
      overallScore = 93,
      hookScore = 95,
      curiosityScore = 92,
      infoScore = 90,
      reasonForSelection = "Exceptional viral retention",
      suggestedPlatform = "YouTube Shorts",
      hashtags = "#shorts #business",
      faceCenterRatioX = 0.51f,
      faceCenterRatioY = 0.44f
    )

    val agent = com.example.clipforge.ai.orchestration.AutonomousEditingAgent(context, project, initialTimeline)
    val result = agent.executeAutonomousWorkflow(
      userRequest = "Turn this into a high-converting TikTok clip with punchy zooms and viral captions",
      clip = clip,
      preferredStyle = com.example.clipforge.ai.editing.EditingStylePreset.VIRAL_PODCAST
    )

    assertTrue("Workflow should succeed", result.success)
    assertEquals(AspectRatioMode.VERTICAL_9_16, result.finalTimeline.targetAspect)
    assertTrue("Execution log should record steps", result.executionLog.isNotEmpty())
    assertTrue("Quality report should be generated", result.qualityReport.overallQuality >= 70)
  }

  @Test
  fun `device capability manager inspects memory and performance mode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val report = com.example.clipforge.video.DeviceCapabilityManager.inspectDevice(context)

    assertNotNull("Hardware report must not be null", report)
    assertTrue("RAM must be greater than zero", report.totalRamMb > 0)
    assertNotNull("Recommended performance mode must exist", report.recommendedPerformanceMode)
    assertTrue("Preview width must be positive", report.recommendedPreviewWidth > 0)
  }

  @Test
  fun `reliability subsystem validates storage and frame cache`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val availSpace = com.example.clipforge.video.ReliabilitySubsystem.checkAvailableStorageMb(context)
    assertTrue("Storage should be positive", availSpace > 0)

    val hasSpace = com.example.clipforge.video.ReliabilitySubsystem.hasSufficientSpaceForExport(context, 50L)
    assertTrue("Device should have sufficient space for 50MB export", hasSpace)

    // Checkpoint persistence
    com.example.clipforge.video.ReliabilitySubsystem.CheckpointManager.saveCheckpoint(context, "test_proj_p1", "{\"dummy\": true}")
    val recovered = com.example.clipforge.video.ReliabilitySubsystem.CheckpointManager.readCheckpoint(context, "test_proj_p1")
    assertEquals("{\"dummy\": true}", recovered)
    com.example.clipforge.video.ReliabilitySubsystem.CheckpointManager.clearCheckpoint(context, "test_proj_p1")
  }

  @Test
  fun `personalization engine persists profile and records feedback`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val initial = com.example.clipforge.ai.personalization.PersonalizationEngine.loadProfile(context)
    assertNotNull("Initial profile must exist", initial)

    val customProfile = initial.copy(
      pace = com.example.clipforge.ai.personalization.EditingPace.FAST_PACED,
      zoomIntensity = com.example.clipforge.ai.personalization.ZoomIntensity.DYNAMIC,
      brandHighlightColorHex = "#FF4400"
    )
    com.example.clipforge.ai.personalization.PersonalizationEngine.saveProfile(context, customProfile)
    val loaded = com.example.clipforge.ai.personalization.PersonalizationEngine.loadProfile(context)
    assertEquals(com.example.clipforge.ai.personalization.EditingPace.FAST_PACED, loaded.pace)
    assertEquals(com.example.clipforge.ai.personalization.ZoomIntensity.DYNAMIC, loaded.zoomIntensity)
    assertEquals("#FF4400", loaded.brandHighlightColorHex)

    // Feedback recording
    com.example.clipforge.ai.personalization.PersonalizationEngine.recordClipDecision(context, accepted = true)
    val updatedWithFeedback = com.example.clipforge.ai.personalization.PersonalizationEngine.loadProfile(context)
    assertEquals(1, updatedWithFeedback.totalAcceptedClips)
  }

  @Test
  fun `ai workflow studio presets are defined and available`() {
    val presets = com.example.clipforge.ai.workflow.AIWorkflowStudio.getPresetPipelines()
    assertTrue("Preset pipelines should not be empty", presets.isNotEmpty())
    assertTrue("Should include podcast to shorts preset", presets.any { it.contains("Podcast", ignoreCase = true) })
    assertTrue("Should include gaming moments preset", presets.any { it.contains("Gaming", ignoreCase = true) })
  }

  @Test
  fun `token usage tracker records tokens and calculates estimated cost`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.clipforge.ai.usage.TokenUsageTracker.init(context)
    val initial = com.example.clipforge.ai.usage.TokenUsageTracker.usageStats.value

    com.example.clipforge.ai.usage.TokenUsageTracker.recordUsage(
      context,
      provider = "GEMINI",
      model = "gemini-3.8-flash",
      inputTokens = 1000L,
      outputTokens = 500L
    )
    val updated = com.example.clipforge.ai.usage.TokenUsageTracker.usageStats.value
    assertEquals(initial.totalInputTokens + 1000L, updated.totalInputTokens)
    assertEquals(initial.totalOutputTokens + 500L, updated.totalOutputTokens)
    assertEquals(initial.totalRequests + 1, updated.totalRequests)
    assertTrue("Estimated cost should be greater than zero", updated.estimatedCostUsd > 0.0)
  }

  @Test
  fun `diagnostic logger redacts sensitive api keys and formats audit report`() {
    com.example.clipforge.diagnostics.DiagnosticLogger.log(
      category = com.example.clipforge.diagnostics.LogCategory.AI_ORCHESTRATION,
      tag = "SecretTest",
      rawMessage = "Connecting with sk-12345678901234567890abcdef and key AIzaSyA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q"
    )

    val logs = com.example.clipforge.diagnostics.DiagnosticLogger.logs.value
    assertTrue("Logs should not be empty", logs.isNotEmpty())
    val last = logs.first()
    assertTrue("API key should be redacted", !last.message.contains("AIzaSyA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q"))
    assertTrue("Contains REDACTED token", last.message.contains("[REDACTED]"))

    val report = com.example.clipforge.diagnostics.DiagnosticLogger.exportSanitizedReport()
    assertTrue("Report must contain header", report.contains("CLIPFORGE AI DIAGNOSTIC AUDIT REPORT"))
  }

  @Test
  fun `app settings manager saves and loads user configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.clipforge.data.local.AppSettingsManager.init(context)

    val customSettings = com.example.clipforge.data.local.AppSettings(
      defaultAspect = AspectRatioMode.SQUARE_1_1,
      defaultCaptionStyle = CaptionStyle.KARAOKE_POP,
      defaultExportResolution = "720p",
      exportBitrateMbps = 8,
      autoFaceTracking = false,
      lowMemoryMode = true,
      aiTemperature = 0.4f
    )

    com.example.clipforge.data.local.AppSettingsManager.updateSettings(context, customSettings)
    val loaded = com.example.clipforge.data.local.AppSettingsManager.settings.value
    assertEquals(AspectRatioMode.SQUARE_1_1, loaded.defaultAspect)
    assertEquals(CaptionStyle.KARAOKE_POP, loaded.defaultCaptionStyle)
    assertEquals(8, loaded.exportBitrateMbps)
    assertEquals(false, loaded.autoFaceTracking)
    assertEquals(true, loaded.lowMemoryMode)
  }
}



