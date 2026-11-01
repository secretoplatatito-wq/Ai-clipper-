package com.example.clipforge.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.ai.personalization.EditingPace
import com.example.clipforge.ai.personalization.PersonalizationEngine
import com.example.clipforge.ai.personalization.ZoomIntensity
import com.example.clipforge.ai.usage.TokenUsageTracker
import com.example.clipforge.data.local.AppSettingsManager
import com.example.clipforge.diagnostics.DiagnosticLogger
import com.example.clipforge.diagnostics.LogCategory
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.theme.StudioTertiary
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel
import com.example.clipforge.video.ReliabilitySubsystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemDiagnosticsScreen(
  viewModel: ClipForgeViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabTitles = listOf("AI & Cost", "Hardware", "Profile", "Audit Logs")

  val tokenStats by TokenUsageTracker.usageStats.collectAsState()
  val hwReport by viewModel.hardwareReport.collectAsState()
  val personalProfile by viewModel.personalProfile.collectAsState()
  val auditLogs by DiagnosticLogger.logs.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("System & Diagnostics Studio", fontWeight = FontWeight.Bold, color = Color.White)
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
    ) {
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = StudioSurfaceElevated,
        contentColor = StudioSecondary,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            color = StudioSecondary
          )
        }
      ) {
        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = selectedTabIndex == index,
            onClick = { selectedTabIndex = index },
            text = {
              Text(
                title,
                fontSize = 13.sp,
                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTabIndex == index) StudioSecondary else Color.LightGray
              )
            }
          )
        }
      }

      when (selectedTabIndex) {
        0 -> {
          // Tab 0: AI & Cost Management (Section 10)
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("AI Token & Budget Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$%.4f USD".format(tokenStats.estimatedCostUsd), color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  }
                  Spacer(modifier = Modifier.height(12.dp))

                  // Progress to daily limit
                  val totalTokens = tokenStats.totalInputTokens + tokenStats.totalOutputTokens
                  val progress = (totalTokens.toFloat() / tokenStats.dailyLimitTokens.toFloat()).coerceIn(0f, 1f)
                  Text("Daily Token Quota (${totalTokens} / ${tokenStats.dailyLimitTokens})", color = Color.LightGray, fontSize = 11.sp)
                  Spacer(modifier = Modifier.height(4.dp))
                  LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(CircleShape),
                    color = if (progress > 0.8f) Color(0xFFEF4444) else StudioSecondary,
                    trackColor = Color(0xFF2A2D3D)
                  )

                  Spacer(modifier = Modifier.height(14.dp))
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                      Text("Input Tokens", color = Color.Gray, fontSize = 11.sp)
                      Text("%,d".format(tokenStats.totalInputTokens), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                      Text("Output Tokens", color = Color.Gray, fontSize = 11.sp)
                      Text("%,d".format(tokenStats.totalOutputTokens), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                      Text("Requests", color = Color.Gray, fontSize = 11.sp)
                      Text("${tokenStats.totalRequests}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                  }
                }
              }
            }

            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                OutlinedButton(
                  onClick = {
                    TokenUsageTracker.resetUsage(context)
                    Toast.makeText(context, "AI token usage stats reset", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reset Usage Counters", fontSize = 11.sp)
                }
              }
            }
          }
        }

        1 -> {
          // Tab 1: Hardware & Storage Subsystem (Sections 24, 25, 26)
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Device Resource Management", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  }
                  Spacer(modifier = Modifier.height(10.dp))

                  Text("Active Mode: ${hwReport.recommendedPerformanceMode.name}", color = StudioSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text("Total Memory: ${hwReport.totalRamMb} MB (Available: ${hwReport.availableRamMb} MB)", color = Color.LightGray, fontSize = 12.sp)
                  Text("Thermal Condition: ${if (hwReport.thermalThrottling) "Throttled" else "Nominal / Normal"}", color = Color.LightGray, fontSize = 12.sp)
                  val encoders = buildList {
                    if (hwReport.hasHardwareH264Encoder) add("H.264 (AVC)")
                    if (hwReport.hasHardwareH265Encoder) add("H.265 (HEVC)")
                  }
                  Text("Hardware Encoders: ${if (encoders.isNotEmpty()) encoders.joinToString(", ") else "Software Baseline (AVC/H.264)"}", color = Color.LightGray, fontSize = 12.sp)
                }
              }
            }


            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Storage & Bounded Frame Cache", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  }
                  Spacer(modifier = Modifier.height(10.dp))

                  val availMb = ReliabilitySubsystem.checkAvailableStorageMb(context)
                  val cacheSize = ReliabilitySubsystem.BoundedFrameCache.cacheSize()
                  Text("Available Disk Storage: $availMb MB", color = Color.LightGray, fontSize = 12.sp)
                  Text("In-Memory Frame Cache: $cacheSize frames cached", color = Color.LightGray, fontSize = 12.sp)

                  Spacer(modifier = Modifier.height(14.dp))
                  Button(
                    onClick = {
                      ReliabilitySubsystem.BoundedFrameCache.evictAll()
                      Toast.makeText(context, "Frame cache and temporary buffers cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Evict Cached Frames & Temp Storage", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }

        2 -> {
          // Tab 2: AI Personalization Engine (Section 34)
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Text("Continuous Learning & Feedback", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    "The autonomous editor tunes its hook detection and zoom pacing based on your approved and rejected clips.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                  )

                  Spacer(modifier = Modifier.height(12.dp))
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                      Text("Accepted Clips", color = Color.Gray, fontSize = 11.sp)
                      Text("${personalProfile.totalAcceptedClips}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                      Text("Rejected Clips", color = Color.Gray, fontSize = 11.sp)
                      Text("${personalProfile.totalRejectedClips}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                      Text("Brand Color", color = Color.Gray, fontSize = 11.sp)
                      Text(personalProfile.brandHighlightColorHex, color = StudioSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))
                  Text("Editing Pace", color = Color.LightGray, fontSize = 12.sp)
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditingPace.entries.forEach { p ->
                      FilterChip(
                        selected = personalProfile.pace == p,
                        onClick = {
                          val updated = personalProfile.copy(pace = p)
                          PersonalizationEngine.saveProfile(context, updated)
                          viewModel.refreshPersonalProfile()
                        },
                        label = { Text(p.name.replace("_", " "), fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = StudioPrimary,
                          selectedLabelColor = Color.White
                        )
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))
                  Text("Zoom Intensity", color = Color.LightGray, fontSize = 12.sp)
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZoomIntensity.entries.forEach { z ->
                      FilterChip(
                        selected = personalProfile.zoomIntensity == z,
                        onClick = {
                          val updated = personalProfile.copy(zoomIntensity = z)
                          PersonalizationEngine.saveProfile(context, updated)
                          viewModel.refreshPersonalProfile()
                        },
                        label = { Text(z.name, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = StudioSecondary,
                          selectedLabelColor = Color.Black
                        )
                      )
                    }
                  }
                }
              }
            }

            item {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                  onClick = {
                    PersonalizationEngine.resetProfile(context)
                    viewModel.refreshPersonalProfile()
                    Toast.makeText(context, "Personalization profile reset to defaults", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reset Profile", fontSize = 11.sp)
                }
              }
            }
          }
        }

        3 -> {
          // Tab 3: Centralized Diagnostic Logs (Section 32)
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Audit Logs (${auditLogs.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = {
                    val report = DiagnosticLogger.exportSanitizedReport()
                    clipboardManager.setText(AnnotatedString(report))
                    Toast.makeText(context, "Sanitized audit report copied to clipboard", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Copy Report", fontSize = 11.sp)
                }

                IconButton(onClick = { DiagnosticLogger.clearLogs() }) {
                  Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray)
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F111A), RoundedCornerShape(10.dp))
                .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(10.dp))
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(auditLogs) { entry ->
                Row(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "${entry.formattedTime()} [${entry.category.name}] ",
                    color = when (entry.category) {
                      LogCategory.AI_ORCHESTRATION -> StudioSecondary
                      LogCategory.MEDIA_ENGINE -> Color(0xFF60A5FA)
                      LogCategory.HARDWARE -> Color(0xFFFBBF24)
                      LogCategory.STORAGE -> Color(0xFF34D399)
                      else -> Color.Gray
                    },
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${entry.tag}: ${entry.message}",
                    color = if (entry.isError) Color(0xFFF87171) else Color(0xFFE2E8F0),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
