package com.example.clipforge.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.clipforge.data.model.ProjectEntity
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
  viewModel: ClipForgeViewModel,
  onOpenProject: (ProjectEntity) -> Unit,
  onNewProject: () -> Unit
) {
  val projects by viewModel.projects.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Project Library", fontWeight = FontWeight.Bold, color = Color.White)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNewProject,
        containerColor = StudioPrimary,
        contentColor = Color.White,
        modifier = Modifier.padding(bottom = 70.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Project")
      }
    },
    containerColor = StudioBackground
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (projects.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text("No projects saved yet", color = Color.White, fontWeight = FontWeight.Bold)
              Text("Import a video or generate short clips to see projects here.", color = Color.Gray, fontSize = 12.sp)
            }
          }
        }
      } else {
        items(projects) { project ->
          Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenProject(project) }
              .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(StudioSurfaceElevated),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(28.dp))
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(project.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  "${project.durationMs / 1000}s • ${project.width}x${project.height} • ${project.fps} FPS",
                  color = Color.LightGray,
                  fontSize = 12.sp
                )
                Text(
                  "Status: ${project.status}",
                  color = StudioSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Icon(Icons.Default.PlayArrow, contentDescription = "Open", tint = StudioSecondary)
            }
          }
        }
      }
    }
  }
}
