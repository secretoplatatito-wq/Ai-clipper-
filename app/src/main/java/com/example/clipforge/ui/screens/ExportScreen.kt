package com.example.clipforge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.video.ExportSettings
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
  viewModel: ClipForgeViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val activeClip by viewModel.activeClip.collectAsState()
  val isExporting by viewModel.isExporting.collectAsState()
  val exportProgress by viewModel.exportProgress.collectAsState()
  val lastExportedUri by viewModel.lastExportedUri.collectAsState()

  var selectedResolution by remember { mutableStateOf("1080p (FHD)") }
  var selectedFps by remember { mutableIntStateOf(30) }
  var burnSubtitles by remember { mutableStateOf(true) }
  var isExportCompleted by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Render & Export Video", fontWeight = FontWeight.Bold, color = Color.White)
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
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      if (!isExportCompleted) {
        // Export Configuration Card
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              activeClip?.title ?: "Clip Export",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text("Select rendering quality and hardware acceleration options", color = Color.Gray, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Resolution Presets
            Text("Resolution", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("720p (HD)", "1080p (FHD)", "4K (Ultra)").forEach { res ->
                FilterChip(
                  selected = selectedResolution == res,
                  onClick = { selectedResolution = res },
                  label = { Text(res, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = StudioSurfaceElevated,
                    labelColor = Color.LightGray
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Frame Rate
            Text("Frame Rate", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(24, 30, 60).forEach { fps ->
                FilterChip(
                  selected = selectedFps == fps,
                  onClick = { selectedFps = fps },
                  label = { Text("${fps} FPS", fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioSecondary,
                    selectedLabelColor = Color.Black,
                    containerColor = StudioSurfaceElevated,
                    labelColor = Color.LightGray
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle burn-in toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Burn-in Animated Subtitles", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Permanently embed karaoke subtitles into the output MP4", color = Color.Gray, fontSize = 11.sp)
              }
              Switch(
                checked = burnSubtitles,
                onCheckedChange = { burnSubtitles = it },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = StudioSecondary,
                  checkedTrackColor = StudioPrimary
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Render Action Button
        Button(
          onClick = {
            viewModel.exportActiveClip(
              ExportSettings(
                resolution = selectedResolution,
                fps = selectedFps,
                includeSubtitles = burnSubtitles
              )
            ) { uri ->
              isExportCompleted = (uri != null)
            }
          },
          enabled = !isExporting,
          colors = ButtonDefaults.buttonColors(containerColor = StudioSecondary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
          Spacer(modifier = Modifier.width(8.dp))
          Text("RENDER & EXPORT MP4", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        // Live Render Progress
        if (isExporting) {
          Spacer(modifier = Modifier.height(16.dp))
          Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = StudioSecondary,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Rendering video frames & muxing audio...", color = Color.White, fontSize = 13.sp)
              }
              Spacer(modifier = Modifier.height(10.dp))
              LinearProgressIndicator(
                progress = { exportProgress },
                color = StudioPrimary,
                trackColor = Color(0xFF334155),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
              )
            }
          }
        }
      } else {
        // Export Complete Screen
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Export Succeeded!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
              "Your vertical short-form video has been rendered and saved directly to the device Movies gallery.",
              color = Color.LightGray,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                  type = "video/mp4"
                  putExtra(Intent.EXTRA_STREAM, lastExportedUri)
                  putExtra(Intent.EXTRA_TEXT, "Created with ClipForge AI: ${activeClip?.title}")
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Video"))
              },
              colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Share, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Share to Social Media", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
              onClick = onNavigateBack,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Back to Studio Editor")
            }
          }
        }
      }
    }
  }
}
