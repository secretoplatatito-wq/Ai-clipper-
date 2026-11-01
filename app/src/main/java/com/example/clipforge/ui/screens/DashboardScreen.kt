package com.example.clipforge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.data.model.ProjectEntity
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.theme.StudioTertiary
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: ClipForgeViewModel,
  onNavigateToClipper: () -> Unit,
  onNavigateToEditor: () -> Unit,
  onNavigateToGenerator: () -> Unit,
  onNavigateToProviders: () -> Unit,
  onNavigateToDiagnostics: () -> Unit = {}
) {
  val projects by viewModel.projects.collectAsState()
  val providers by viewModel.aiProviders.collectAsState()
  val activeProvider = providers.firstOrNull { it.isEnabled }

  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.createProjectFromUri("My Video Project", uri) {
        onNavigateToClipper()
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StudioPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.ContentCut,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("ClipForge AI", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
              Text("AI Video Production Studio", fontSize = 11.sp, color = Color.Gray)
            }
          }
        },
        actions = {
          IconButton(onClick = onNavigateToDiagnostics) {
            Icon(
              Icons.Default.Build,
              contentDescription = "System Diagnostics",
              tint = StudioSecondary
            )
          }
          IconButton(onClick = onNavigateToProviders) {
            Icon(Icons.Default.Settings, contentDescription = "AI Providers", tint = Color.LightGray)
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
      contentPadding = PaddingValues(bottom = 80.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // AI Status Banner
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(12.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(if (activeProvider != null) Color(0xFF10B981) else StudioSecondary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "AI Brain: ${activeProvider?.name ?: "Multi-Agent System"}",
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = activeProvider?.selectedModel ?: "Gemini 3.8 Flash + Face Tracker",
                  color = Color.LightGray,
                  fontSize = 11.sp
                )
              }
            }
            Text(
              text = "BYOK / Settings",
              color = StudioPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.clickable { onNavigateToProviders() }
            )
          }
        }
      }

      // Hero Upload & Create Action
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = StudioSecondary,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Turn Long Videos Into Viral Shorts",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Auto-clip interesting moments, crop 9:16 to active speaker, and add animated viral captions.",
              color = Color.LightGray,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
              lineHeight = 16.sp
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { videoPickerLauncher.launch("video/*") },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select Video", fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  viewModel.createSampleProject {
                    onNavigateToClipper()
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Try Demo Video", color = Color.White, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // Quick Actions Grid
      item {
        Text("Studio Tools", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionCard(
            title = "AI Clipper",
            subtitle = "OpusClip style",
            icon = Icons.Default.ContentCut,
            iconTint = StudioSecondary,
            modifier = Modifier.weight(1f),
            onClick = onNavigateToClipper
          )
          QuickActionCard(
            title = "Video Editor",
            subtitle = "Multi-track CapCut",
            icon = Icons.Default.Movie,
            iconTint = StudioPrimary,
            modifier = Modifier.weight(1f),
            onClick = onNavigateToEditor
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionCard(
            title = "AI Generator",
            subtitle = "Text to video",
            icon = Icons.Default.AutoAwesome,
            iconTint = StudioTertiary,
            modifier = Modifier.weight(1f),
            onClick = onNavigateToGenerator
          )
          QuickActionCard(
            title = "Auto Captions",
            subtitle = "Karaoke styles",
            icon = Icons.Default.ClosedCaption,
            iconTint = Color(0xFFFACC15),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToEditor
          )
        }
      }

      // Recent Projects Header & List
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Recent Projects", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          Text("${projects.size} projects", color = Color.Gray, fontSize = 12.sp)
        }
      }

      if (projects.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("No projects yet", color = Color.White, fontWeight = FontWeight.Bold)
              Text("Upload a video or try the demo project to start clipping!", color = Color.Gray, fontSize = 12.sp)
            }
          }
        }
      } else {
        items(projects) { project ->
          ProjectItemCard(
            project = project,
            onClick = {
              viewModel.selectProject(project)
              onNavigateToClipper()
            }
          )
        }
      }
    }
  }
}

@Composable
private fun QuickActionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
      .clickable { onClick() }
      .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(12.dp))
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.Gray, fontSize = 10.sp)
      }
    }
  }
}

@Composable
private fun ProjectItemCard(
  project: ProjectEntity,
  onClick: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = StudioSurface),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(12.dp))
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(StudioSurfaceElevated),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Movie, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(24.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(project.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(
          text = "${project.durationMs / 1000}s • ${project.width}x${project.height} • ${project.fps} FPS",
          color = Color.Gray,
          fontSize = 11.sp
        )
      }
      Icon(Icons.Default.PlayArrow, contentDescription = "Open", tint = StudioSecondary)
    }
  }
}
