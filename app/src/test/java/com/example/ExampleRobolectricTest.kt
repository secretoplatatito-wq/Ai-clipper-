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
}
