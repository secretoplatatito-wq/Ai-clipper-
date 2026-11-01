package com.example.clipforge.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.ui.screens.AIClipperScreen
import com.example.clipforge.ui.screens.AIGeneratorScreen
import com.example.clipforge.ui.screens.AIProvidersScreen
import com.example.clipforge.ui.screens.ClipResultsScreen
import com.example.clipforge.ui.screens.DashboardScreen
import com.example.clipforge.ui.screens.EditorScreen
import com.example.clipforge.ui.screens.ExportScreen
import com.example.clipforge.ui.screens.ProjectsScreen
import com.example.clipforge.ui.screens.SystemDiagnosticsScreen
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel

enum class MainTab(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
  CLIPPER("AI Clipper", Icons.Filled.ContentCut, Icons.Outlined.ContentCut),
  EDITOR("Editor", Icons.Filled.Movie, Icons.Outlined.Movie),
  GENERATOR("Generator", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
  PROJECTS("Projects", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)
}

enum class SubScreen {
  NONE,
  CLIP_RESULTS,
  PROVIDERS_SETTINGS,
  EXPORT_PROGRESS,
  SYSTEM_DIAGNOSTICS
}

@Composable
fun AppNavigation(viewModel: ClipForgeViewModel) {
  var currentTab by remember { mutableStateOf(MainTab.HOME) }
  var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

  // Predictive Back Handler
  BackHandler(enabled = currentSubScreen != SubScreen.NONE || currentTab != MainTab.HOME) {
    if (currentSubScreen != SubScreen.NONE) {
      currentSubScreen = SubScreen.NONE
    } else {
      currentTab = MainTab.HOME
    }
  }

  Scaffold(
    bottomBar = {
      if (currentSubScreen == SubScreen.NONE && currentTab != MainTab.EDITOR) {
        NavigationBar(
          containerColor = StudioSurfaceElevated,
          contentColor = Color.White,
          modifier = Modifier.navigationBarsPadding()
        ) {
          MainTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
              selected = isSelected,
              onClick = { currentTab = tab },
              icon = {
                Icon(
                  imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label
                )
              },
              label = {
                Text(
                  tab.label,
                  fontSize = 10.sp,
                  color = if (isSelected) StudioSecondary else Color.Gray
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = StudioSecondary,
                selectedTextColor = StudioSecondary,
                indicatorColor = StudioPrimary.copy(alpha = 0.25f),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray
              )
            )
          }
        }
      }
    },
    containerColor = StudioBackground
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(if (currentTab == MainTab.EDITOR || currentSubScreen != SubScreen.NONE) 0.dp else innerPadding.calculateBottomPadding())
    ) {
      when (currentSubScreen) {
        SubScreen.CLIP_RESULTS -> {
          ClipResultsScreen(
            viewModel = viewModel,
            onNavigateBack = { currentSubScreen = SubScreen.NONE },
            onOpenEditor = {
              currentSubScreen = SubScreen.NONE
              currentTab = MainTab.EDITOR
            },
            onExportClip = {
              currentSubScreen = SubScreen.EXPORT_PROGRESS
            }
          )
        }
        SubScreen.PROVIDERS_SETTINGS -> {
          AIProvidersScreen(
            viewModel = viewModel,
            onNavigateBack = { currentSubScreen = SubScreen.NONE }
          )
        }
        SubScreen.EXPORT_PROGRESS -> {
          ExportScreen(
            viewModel = viewModel,
            onNavigateBack = { currentSubScreen = SubScreen.NONE }
          )
        }
        SubScreen.SYSTEM_DIAGNOSTICS -> {
          SystemDiagnosticsScreen(
            viewModel = viewModel,
            onNavigateBack = { currentSubScreen = SubScreen.NONE }
          )
        }
        SubScreen.NONE -> {
          when (currentTab) {
            MainTab.HOME -> {
              DashboardScreen(
                viewModel = viewModel,
                onNavigateToClipper = { currentTab = MainTab.CLIPPER },
                onNavigateToEditor = { currentTab = MainTab.EDITOR },
                onNavigateToGenerator = { currentTab = MainTab.GENERATOR },
                onNavigateToProviders = { currentSubScreen = SubScreen.PROVIDERS_SETTINGS },
                onNavigateToDiagnostics = { currentSubScreen = SubScreen.SYSTEM_DIAGNOSTICS }
              )
            }

            MainTab.CLIPPER -> {
              AIClipperScreen(
                viewModel = viewModel,
                onNavigateBack = { currentTab = MainTab.HOME },
                onClipsGenerated = { currentSubScreen = SubScreen.CLIP_RESULTS }
              )
            }
            MainTab.EDITOR -> {
              EditorScreen(
                viewModel = viewModel,
                onNavigateBack = { currentTab = MainTab.HOME },
                onExport = { currentSubScreen = SubScreen.EXPORT_PROGRESS }
              )
            }
            MainTab.GENERATOR -> {
              AIGeneratorScreen(
                viewModel = viewModel,
                onNavigateToEditor = { currentTab = MainTab.EDITOR }
              )
            }
            MainTab.PROJECTS -> {
              ProjectsScreen(
                viewModel = viewModel,
                onOpenProject = { proj ->
                  viewModel.selectProject(proj)
                  currentTab = MainTab.CLIPPER
                },
                onNewProject = { currentTab = MainTab.CLIPPER }
              )
            }
          }
        }
      }
    }
  }
}
