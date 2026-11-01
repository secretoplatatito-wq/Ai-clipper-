package com.example.clipforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.data.model.CaptionItem
import com.example.clipforge.data.model.Timeline
import com.example.clipforge.ui.theme.PlayheadColor
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.theme.TrackAudioColor
import com.example.clipforge.ui.theme.TrackCaptionColor
import com.example.clipforge.ui.theme.TrackVideoColor

@Composable
fun MultiTrackTimeline(
  timeline: Timeline,
  currentPlayheadMs: Long,
  totalDurationMs: Long,
  startTrimMs: Long,
  endTrimMs: Long,
  onSeek: (Long) -> Unit,
  onTrimChanged: (startMs: Long, endMs: Long) -> Unit,
  onSplitAtPlayhead: () -> Unit,
  modifier: Modifier = Modifier
) {
  var pixelsPerSecond by remember { mutableFloatStateOf(40f) } // Timeline zoom scale
  val scrollState = rememberScrollState()

  val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(10f)
  val timelineWidthDp = (totalSeconds * pixelsPerSecond).dp.coerceAtLeast(340.dp)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(StudioSurface)
      .padding(vertical = 6.dp)
  ) {
    // Zoom & Split Tools Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { pixelsPerSecond = (pixelsPerSecond - 10f).coerceAtLeast(20f) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out timeline", tint = Color.LightGray)
        }
        Text(
          text = "${(pixelsPerSecond / 10).toInt()}x",
          color = Color.Gray,
          fontSize = 11.sp,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(
          onClick = { pixelsPerSecond = (pixelsPerSecond + 15f).coerceAtMost(120f) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in timeline", tint = Color.LightGray)
        }
      }

      // Quick Split Button
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(StudioSurfaceElevated)
          .clickable { onSplitAtPlayhead() }
          .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          Icons.Default.ContentCut,
          contentDescription = "Split clip",
          tint = Color(0xFFF43F5E),
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Split Here", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Scrollable Tracks Canvas
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .horizontalScroll(scrollState)
        .pointerInput(timelineWidthDp, totalDurationMs) {
          detectTapGestures { offset ->
            val fraction = (offset.x / (totalSeconds * pixelsPerSecond * density)).coerceIn(0f, 1f)
            val seekTargetMs = (fraction * totalDurationMs).toLong()
            onSeek(seekTargetMs)
          }
        }
    ) {
      Column(
        modifier = Modifier
          .width(timelineWidthDp)
          .fillMaxHeight()
          .padding(start = 16.dp, end = 24.dp)
      ) {
        // 1. Time Ruler Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
        ) {
          val stepSec = if (pixelsPerSecond > 60f) 2 else 5
          val markerCount = (totalSeconds / stepSec).toInt()
          for (i in 0..markerCount) {
            val sec = i * stepSec
            Text(
              text = String.format("%02d:%02d", sec / 60, sec % 60),
              color = Color.DarkGray,
              fontSize = 9.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.width((stepSec * pixelsPerSecond).dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Main Video Track with Trim Handles
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(TrackVideoColor.copy(alpha = 0.35f))
            .border(1.dp, TrackVideoColor, RoundedCornerShape(6.dp))
        ) {
          // Highlight active trimmed range
          val startFrac = (startTrimMs.toFloat() / totalDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
          val endFrac = (endTrimMs.toFloat() / totalDurationMs.coerceAtLeast(1L)).coerceIn(startFrac, 1f)

          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(endFrac)
              .padding(start = (startFrac * timelineWidthDp.value).dp)
              .background(TrackVideoColor.copy(alpha = 0.65f))
          ) {
            Text(
              text = "🎥 Main Video Track",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Caption Track
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(TrackCaptionColor.copy(alpha = 0.2f))
            .border(1.dp, TrackCaptionColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        ) {
          // Render individual caption blocks
          timeline.captions.forEach { cap ->
            val cStart = (cap.startMs.toFloat() / totalDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
            val cEnd = (cap.endMs.toFloat() / totalDurationMs.coerceAtLeast(1L)).coerceIn(cStart, 1f)
            val cWidth = ((cEnd - cStart) * timelineWidthDp.value).dp

            Box(
              modifier = Modifier
                .fillMaxHeight()
                .width(cWidth)
                .offset { IntOffset((cStart * timelineWidthDp.value * 2.5f).toInt(), 0) }
                .background(TrackCaptionColor, RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Text(
                text = cap.text,
                color = Color.Black,
                fontSize = 9.sp,
                maxLines = 1,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Audio Track (Simulated Waveform)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(TrackAudioColor.copy(alpha = 0.2f))
            .border(1.dp, TrackAudioColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
          contentAlignment = Alignment.CenterStart
        ) {
          Text(
            text = "🎵 Dialogue / Speech Audio Track",
            color = TrackAudioColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp)
          )
        }
      }

      // Playhead Indicator
      val playheadFraction = (currentPlayheadMs.toFloat() / totalDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
      val playheadOffsetDp = (playheadFraction * timelineWidthDp.value).dp + 16.dp

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(2.dp)
          .offset(x = playheadOffsetDp)
          .background(PlayheadColor)
      ) {
        // Playhead Top Cap
        Box(
          modifier = Modifier
            .size(10.dp)
            .offset(x = (-4).dp, y = 0.dp)
            .clip(CircleShape)
            .background(PlayheadColor)
        )
      }
    }
  }
}
