package com.example.clipforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.data.model.ClipEntity
import com.example.clipforge.ui.components.ViralScoreBadge
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipResultsScreen(
  viewModel: ClipForgeViewModel,
  onNavigateBack: () -> Unit,
  onOpenEditor: () -> Unit,
  onExportClip: (ClipEntity) -> Unit
) {
  val clips by viewModel.clips.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("AI Generated Clips", fontWeight = FontWeight.Bold, color = Color.White)
            Text(
              "${clips.size} viral segments identified",
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
      )
    },
    containerColor = StudioBackground
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(clips) { clip ->
        GeneratedClipCard(
          clip = clip,
          onEdit = {
            viewModel.openClipInEditor(clip)
            onOpenEditor()
          },
          onExport = {
            viewModel.openClipInEditor(clip)
            onExportClip(clip)
          }
        )
      }
    }
  }
}

@Composable
private fun GeneratedClipCard(
  clip: ClipEntity,
  onEdit: () -> Unit,
  onExport: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = StudioSurface),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top Row: Viral Score & Platform
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        ViralScoreBadge(clip = clip)

        Box(
          modifier = Modifier
            .background(Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = clip.suggestedPlatform,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Title & Duration
      Text(
        text = clip.title,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )

      val durationSec = (clip.endMs - clip.startMs) / 1000
      Text(
        text = "${durationSec}s • Timestamp: ${formatTime(clip.startMs)} - ${formatTime(clip.endMs)}",
        color = StudioSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Hook Quote Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(StudioSurfaceElevated)
          .padding(10.dp)
      ) {
        Column {
          Text("🔥 Hook Statement:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Text(
            text = "\"${clip.hookText}\"",
            color = Color(0xFFFACC15),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Hashtags
      Text(
        text = clip.hashtags,
        color = StudioPrimary,
        fontSize = 11.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onEdit,
          colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Edit in Studio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onExport,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Quick Export", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
      }
    }
  }
}

private fun formatTime(ms: Long): String {
  val totalSec = ms / 1000
  val min = totalSec / 60
  val sec = totalSec % 60
  return String.format("%02d:%02d", min, sec)
}
