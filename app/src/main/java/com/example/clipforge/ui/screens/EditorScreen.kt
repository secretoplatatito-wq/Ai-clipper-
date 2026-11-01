package com.example.clipforge.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionStyle
import com.example.clipforge.data.model.Timeline
import com.example.clipforge.data.model.VisualEffect
import com.example.clipforge.ui.components.CaptionOverlay
import com.example.clipforge.ui.components.MultiTrackTimeline
import com.example.clipforge.ui.components.VideoPlayerView
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel
import java.util.UUID

enum class EditorTab {
  TRIM,
  ASPECT,
  SPEED,
  CAPTIONS,
  EFFECTS,
  AI_COPILOT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
  viewModel: ClipForgeViewModel,
  onNavigateBack: () -> Unit,
  onExport: () -> Unit
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val activeClip by viewModel.activeClip.collectAsState()
  val timeline by viewModel.activeTimeline.collectAsState()
  val copilotLogs by viewModel.copilotLogs.collectAsState()

  var isPlaying by remember { mutableStateOf(false) }
  var currentPlayheadMs by remember { mutableLongStateOf(0L) }
  var activeTab by remember { mutableStateOf(EditorTab.TRIM) }
  var showCopilotSheet by remember { mutableStateOf(false) }
  var copilotInputText by remember { mutableStateOf("") }

  val firstSeg = timeline.segments.firstOrNull()
  val startTrimMs = firstSeg?.startTrimMs ?: 0L
  val endTrimMs = firstSeg?.endTrimMs ?: (timeline.durationMs.takeIf { it > 0 } ?: 30000L)
  val clipDurationMs = (endTrimMs - startTrimMs).coerceAtLeast(1000L)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              activeClip?.title ?: activeProject?.name ?: "Video Studio Editor",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color.White,
              maxLines = 1
            )
            Text(
              "${timeline.targetAspect.label.split(" ").first()} • ${firstSeg?.speed ?: 1.0f}x Speed",
              fontSize = 11.sp,
              color = Color.Gray
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(onClick = { viewModel.undo() }) {
            Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.LightGray)
          }
          IconButton(onClick = { viewModel.redo() }) {
            Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.LightGray)
          }
          Button(
            onClick = onExport,
            colors = ButtonDefaults.buttonColors(containerColor = StudioSecondary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
      )
    },
    containerColor = StudioBackground
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Center Video Preview Canvas with live aspect ratio container
      val aspectModifier = when (timeline.targetAspect) {
        AspectRatioMode.VERTICAL_9_16 -> Modifier.aspectRatio(9f / 16f)
        AspectRatioMode.HORIZONTAL_16_9 -> Modifier.aspectRatio(16f / 9f)
        AspectRatioMode.SQUARE_1_1 -> Modifier.aspectRatio(1f / 1f)
        AspectRatioMode.PORTRAIT_4_5 -> Modifier.aspectRatio(4f / 5f)
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(Color.Black),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight(0.96f)
            .then(aspectModifier)
            .clip(RoundedCornerShape(8.dp))
            .border(1.5.dp, StudioPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
          val videoUriStr = firstSeg?.sourceUri?.ifBlank { activeProject?.videoUri }
          val videoUri = if (!videoUriStr.isNullOrBlank()) Uri.parse(videoUriStr) else null

          VideoPlayerView(
            videoUri = videoUri,
            isPlaying = isPlaying,
            startTrimMs = startTrimMs,
            endTrimMs = endTrimMs,
            speed = firstSeg?.speed ?: 1.0f,
            zoomScale = firstSeg?.zoomScale ?: 1.0f,
            panCenterX = firstSeg?.panCenterX ?: 0.5f,
            panCenterY = firstSeg?.panCenterY ?: 0.5f,
            aspectRatio = timeline.targetAspect,
            onProgressUpdate = { cur, _ ->
              currentPlayheadMs = cur
            },
            modifier = Modifier.fillMaxSize()
          )

          // Live Subtitles / Karaoke overlay
          CaptionOverlay(
            captions = timeline.captions,
            currentPlayheadMs = currentPlayheadMs,
            modifier = Modifier.fillMaxSize()
          )

          // Vignette effect overlay if active
          if (timeline.effects.any { it.type == "VIGNETTE" }) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
            )
          }
        }
      }

      // 2. Playback Control Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(StudioSurface)
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = formatTimecode(currentPlayheadMs - startTrimMs),
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              currentPlayheadMs = (currentPlayheadMs - 5000L).coerceAtLeast(startTrimMs)
            }
          ) {
            Icon(Icons.Default.FastForward, contentDescription = "Rewind", tint = Color.LightGray, modifier = Modifier.size(18.dp))
          }

          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(StudioPrimary)
              .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          IconButton(
            onClick = {
              currentPlayheadMs = (currentPlayheadMs + 5000L).coerceAtMost(endTrimMs)
            }
          ) {
            Icon(Icons.Default.FastForward, contentDescription = "Forward", tint = Color.LightGray, modifier = Modifier.size(18.dp))
          }
        }

        Text(
          text = formatTimecode(clipDurationMs),
          color = Color.Gray,
          fontSize = 12.sp
        )
      }

      // 3. Multi-Track Timeline
      MultiTrackTimeline(
        timeline = timeline,
        currentPlayheadMs = currentPlayheadMs,
        totalDurationMs = timeline.durationMs.takeIf { it > 0 } ?: 60000L,
        startTrimMs = startTrimMs,
        endTrimMs = endTrimMs,
        onSeek = { seekMs ->
          currentPlayheadMs = seekMs
        },
        onTrimChanged = { newStart, newEnd ->
          val updatedSegs = timeline.segments.map { it.copy(startTrimMs = newStart, endTrimMs = newEnd) }
          viewModel.updateTimeline(timeline.copy(segments = updatedSegs))
        },
        onSplitAtPlayhead = {
          val splitPoint = currentPlayheadMs
          if (splitPoint in (startTrimMs + 1000L)..(endTrimMs - 1000L)) {
            val seg1 = firstSeg?.copy(id = UUID.randomUUID().toString(), endTrimMs = splitPoint)
            val seg2 = firstSeg?.copy(id = UUID.randomUUID().toString(), startTrimMs = splitPoint)
            if (seg1 != null && seg2 != null) {
              viewModel.updateTimeline(timeline.copy(segments = listOf(seg1, seg2)))
            }
          }
        }
      )

      // 4. Editing Operations Toolbar (Trim, Aspect, Speed, Captions, Effects, AI Copilot)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(StudioSurfaceElevated)
      ) {
        // Tab Row
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(EditorTab.entries) { tab ->
            val isSelected = activeTab == tab
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) StudioPrimary else StudioSurface)
                .clickable {
                  activeTab = tab
                  if (tab == EditorTab.AI_COPILOT) showCopilotSheet = true
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = when (tab) {
                    EditorTab.TRIM -> Icons.Default.ContentCut
                    EditorTab.ASPECT -> Icons.Default.AspectRatio
                    EditorTab.SPEED -> Icons.Default.Speed
                    EditorTab.CAPTIONS -> Icons.Default.ClosedCaption
                    EditorTab.EFFECTS -> Icons.Default.Filter
                    EditorTab.AI_COPILOT -> Icons.Default.AutoAwesome
                  },
                  contentDescription = null,
                  tint = if (isSelected) Color.White else Color.LightGray,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (tab) {
                    EditorTab.TRIM -> "Trim"
                    EditorTab.ASPECT -> "Reframe"
                    EditorTab.SPEED -> "Speed"
                    EditorTab.CAPTIONS -> "Captions"
                    EditorTab.EFFECTS -> "Effects"
                    EditorTab.AI_COPILOT -> "AI Copilot"
                  },
                  color = if (isSelected) Color.White else Color.LightGray,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // Tab Content Panel
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          when (activeTab) {
            EditorTab.TRIM -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Trim Boundaries", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Text("Start: ${formatTimecode(startTrimMs)} | End: ${formatTimecode(endTrimMs)}", color = Color.Gray, fontSize = 11.sp)
                }
                Button(
                  onClick = {
                    val updatedSegs = timeline.segments.map { it.copy(startTrimMs = currentPlayheadMs) }
                    viewModel.updateTimeline(timeline.copy(segments = updatedSegs))
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Set Start to Playhead", fontSize = 11.sp)
                }
              }
            }
            EditorTab.ASPECT -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                AspectRatioMode.entries.forEach { mode ->
                  FilterChip(
                    selected = timeline.targetAspect == mode,
                    onClick = {
                      viewModel.updateTimeline(timeline.copy(targetAspect = mode))
                    },
                    label = { Text(mode.label.split(" ").first(), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = StudioSecondary,
                      selectedLabelColor = Color.Black,
                      containerColor = StudioSurface,
                      labelColor = Color.LightGray
                    )
                  )
                }
              }
            }
            EditorTab.SPEED -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                  FilterChip(
                    selected = (firstSeg?.speed ?: 1.0f) == spd,
                    onClick = {
                      val updatedSegs = timeline.segments.map { it.copy(speed = spd) }
                      viewModel.updateTimeline(timeline.copy(segments = updatedSegs))
                    },
                    label = { Text("${spd}x", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = StudioPrimary,
                      selectedLabelColor = Color.White,
                      containerColor = StudioSurface,
                      labelColor = Color.LightGray
                    )
                  )
                }
              }
            }
            EditorTab.CAPTIONS -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CaptionStyle.entries.forEach { style ->
                  FilterChip(
                    selected = timeline.captions.firstOrNull()?.style == style,
                    onClick = {
                      val styled = timeline.captions.map { it.copy(style = style) }
                      viewModel.updateTimeline(timeline.copy(captions = styled))
                    },
                    label = { Text(style.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = Color(0xFFFACC15),
                      selectedLabelColor = Color.Black,
                      containerColor = StudioSurface,
                      labelColor = Color.LightGray
                    )
                  )
                }
              }
            }
            EditorTab.EFFECTS -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val hasVignette = timeline.effects.any { it.type == "VIGNETTE" }
                FilterChip(
                  selected = hasVignette,
                  onClick = {
                    val eff = if (hasVignette) {
                      timeline.effects.filterNot { it.type == "VIGNETTE" }
                    } else {
                      timeline.effects + VisualEffect(UUID.randomUUID().toString(), "VIGNETTE", 0.6f)
                    }
                    viewModel.updateTimeline(timeline.copy(effects = eff))
                  },
                  label = { Text("Cinematic Vignette", fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = StudioSurface,
                    labelColor = Color.LightGray
                  )
                )
              }
            }
            EditorTab.AI_COPILOT -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("AI Copilot active", color = StudioSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Button(
                  onClick = { showCopilotSheet = true },
                  colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Open Command Chat", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  // AI Copilot Conversational Assistant Bottom Sheet
  if (showCopilotSheet) {
    ModalBottomSheet(
      onDismissRequest = { showCopilotSheet = false },
      containerColor = Color(0xFF14151D),
      sheetState = rememberModalBottomSheetState()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioSecondary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("AI Video Editing Copilot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text("Tell the AI to make edits, reframe, adjust captions, or zoom.", color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(10.dp))

        // Preset command suggestion chips
        Text("Instant Actions:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = false,
            onClick = { viewModel.executeCopilot("Make vertical 9:16") },
            label = { Text("Reframe 9:16", fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(containerColor = StudioSurfaceElevated, labelColor = Color.White)
          )
          FilterChip(
            selected = false,
            onClick = { viewModel.executeCopilot("Change captions to bold yellow") },
            label = { Text("Viral Captions", fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(containerColor = StudioSurfaceElevated, labelColor = Color.White)
          )
          FilterChip(
            selected = false,
            onClick = { viewModel.executeCopilot("Zoom in on speaker") },
            label = { Text("1.25x Zoom", fontSize = 10.sp) },
            colors = FilterChipDefaults.filterChipColors(containerColor = StudioSurfaceElevated, labelColor = Color.White)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Copilot Log Output
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurface),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().height(110.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            copilotLogs.takeLast(3).forEach { log ->
              Text("• $log", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Natural Language Command Input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = copilotInputText,
            onValueChange = { copilotInputText = it },
            placeholder = { Text("e.g. 'Make this video vertical and punch in'", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = StudioPrimary,
              unfocusedBorderColor = StudioSurfaceBorder
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
            onClick = {
              if (copilotInputText.isNotBlank()) {
                viewModel.executeCopilot(copilotInputText)
                copilotInputText = ""
              }
            },
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(StudioPrimary)
          ) {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

private fun formatTimecode(ms: Long): String {
  val totalSec = (ms / 1000).coerceAtLeast(0)
  val min = totalSec / 60
  val sec = totalSec % 60
  return String.format("%02d:%02d", min, sec)
}
