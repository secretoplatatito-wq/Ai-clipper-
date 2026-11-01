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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.ui.components.VideoPlayerView
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIClipperScreen(
  viewModel: ClipForgeViewModel,
  onNavigateBack: () -> Unit,
  onClipsGenerated: () -> Unit
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val isAnalyzing by viewModel.isAnalyzing.collectAsState()
  val analysisStep by viewModel.analysisStep.collectAsState()
  val analysisProgress by viewModel.analysisProgress.collectAsState()

  var selectedClipCount by remember { mutableIntStateOf(3) }
  var selectedDurationSec by remember { mutableIntStateOf(30) }
  var selectedStyle by remember { mutableStateOf("Viral moments") }
  var autoCaptionsEnabled by remember { mutableStateOf(true) }
  var speakerTrackingEnabled by remember { mutableStateOf(true) }
  var dynamicZoomsEnabled by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("AI Video Clipper", fontWeight = FontWeight.Bold, color = Color.White)
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Video Preview Card
      Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            if (activeProject != null) {
              VideoPlayerView(
                videoUri = Uri.parse(activeProject!!.videoUri),
                isPlaying = false,
                aspectRatio = AspectRatioMode.HORIZONTAL_16_9,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sample Podcast Video", color = Color.Gray, fontSize = 12.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                activeProject?.name ?: "Sample Masterclass Video",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                "${(activeProject?.durationMs ?: 60000L) / 1000}s duration • ${activeProject?.width ?: 1920}x${activeProject?.height ?: 1080}",
                color = Color.Gray,
                fontSize = 11.sp
              )
            }
            Box(
              modifier = Modifier
                .background(StudioPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("Ready to Clip", color = StudioPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Clip Parameters Card
      Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Number of Clips to Generate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 5, 10).forEach { count ->
              FilterChip(
                selected = selectedClipCount == count,
                onClick = { selectedClipCount = count },
                label = { Text("$count Clips") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StudioPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = StudioSurfaceElevated,
                  labelColor = Color.LightGray
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text("Preferred Clip Duration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 45, 60).forEach { dur ->
              FilterChip(
                selected = selectedDurationSec == dur,
                onClick = { selectedDurationSec = dur },
                label = { Text("${dur}s") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StudioPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = StudioSurfaceElevated,
                  labelColor = Color.LightGray
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text("Clip Selection Style", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          val styles = listOf("Viral moments", "Educational", "Motivational", "Podcast highlights", "Business insights")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            styles.take(3).forEach { st ->
              FilterChip(
                selected = selectedStyle == st,
                onClick = { selectedStyle = st },
                label = { Text(st, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StudioSecondary,
                  selectedLabelColor = Color.Black,
                  containerColor = StudioSurfaceElevated,
                  labelColor = Color.LightGray
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text("Smart Automation Features", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(6.dp))

          OptionToggleRow(
            title = "Animated Viral Captions",
            subtitle = "Karaoke highlighting on key words",
            checked = autoCaptionsEnabled,
            onCheckedChange = { autoCaptionsEnabled = it }
          )
          OptionToggleRow(
            title = "Smart 9:16 Speaker Tracking",
            subtitle = "Follows active face with smooth reframing",
            checked = speakerTrackingEnabled,
            onCheckedChange = { speakerTrackingEnabled = it }
          )
          OptionToggleRow(
            title = "Dynamic Punch-in Zooms",
            subtitle = "Zooms in during strong hooks and conflicts",
            checked = dynamicZoomsEnabled,
            onCheckedChange = { dynamicZoomsEnabled = it }
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Generate Button
      Button(
        onClick = {
          val projId = activeProject?.id ?: "sample_project"
          viewModel.generateClips(
            projectId = projId,
            clipCount = selectedClipCount,
            durationSec = selectedDurationSec,
            style = selectedStyle,
            onFinished = onClipsGenerated
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "GENERATE AI CLIPS",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.sp
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // Live Multi-stage Processing Dialog
  if (isAnalyzing) {
    AlertDialog(
      onDismissRequest = {},
      confirmButton = {},
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(
            color = StudioSecondary,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.5.dp
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text("AI Video Intelligence Engine", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
          Text(analysisStep, color = Color.LightGray, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(14.dp))
          LinearProgressIndicator(
            progress = { analysisProgress },
            color = StudioPrimary,
            trackColor = Color(0xFF334155),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "${(analysisProgress * 100).toInt()}% completed",
            color = StudioSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
          )
        }
      },
      containerColor = Color(0xFF1E202B)
    )
  }
}

@Composable
private fun OptionToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      Text(subtitle, color = Color.Gray, fontSize = 11.sp)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = StudioSecondary,
        checkedTrackColor = StudioPrimary,
        uncheckedTrackColor = Color(0xFF334155)
      )
    )
  }
}
