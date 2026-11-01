package com.example.clipforge.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.theme.StudioTertiary
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIGeneratorScreen(
  viewModel: ClipForgeViewModel,
  onNavigateToEditor: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var prompt by remember { mutableStateOf("Cinematic drone shot over futuristic cyber metropolis at neon twilight, cinematic lighting, 4k ultra-realistic") }
  var generationMode by remember { mutableStateOf("Text-to-Video") }
  var aspectChoice by remember { mutableStateOf("9:16 Vertical") }
  var durationSec by remember { mutableIntStateOf(5) }
  var isGenerating by remember { mutableStateOf(false) }
  var generationProgress by remember { mutableStateOf(0f) }
  var generatedSuccess by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioTertiary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Video Generator", fontWeight = FontWeight.Bold, color = Color.White)
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
      // Mode selector: Text-to-Video / Image-to-Video
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        listOf("Text-to-Video", "Image-to-Video", "B-Roll Generator").forEach { mode ->
          FilterChip(
            selected = generationMode == mode,
            onClick = { generationMode = mode },
            label = { Text(mode, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = StudioTertiary,
              selectedLabelColor = Color.Black,
              containerColor = StudioSurfaceElevated,
              labelColor = Color.LightGray
            ),
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Prompt Box
      Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            if (generationMode == "Image-to-Video") "Animation Prompt & Motion Control" else "Creative Video Prompt",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedBorderColor = StudioTertiary,
              unfocusedBorderColor = StudioSurfaceBorder
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Parameters: Aspect Ratio & Duration
          Text("Output Aspect Ratio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("9:16 Vertical", "16:9 Widescreen", "1:1 Square").forEach { aspect ->
              FilterChip(
                selected = aspectChoice == aspect,
                onClick = { aspectChoice = aspect },
                label = { Text(aspect, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StudioPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = StudioSurfaceElevated,
                  labelColor = Color.LightGray
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text("Target Duration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 5, 10).forEach { sec ->
              FilterChip(
                selected = durationSec == sec,
                onClick = { durationSec = sec },
                label = { Text("${sec}s Scene") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StudioSecondary,
                  selectedLabelColor = Color.Black,
                  containerColor = StudioSurfaceElevated,
                  labelColor = Color.LightGray
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Generate Button
      Button(
        onClick = {
          scope.launch {
            isGenerating = true
            generatedSuccess = false
            for (p in 1..10) {
              delay(300)
              generationProgress = p / 10f
            }
            isGenerating = false
            generatedSuccess = true
          }
        },
        enabled = !isGenerating,
        colors = ButtonDefaults.buttonColors(containerColor = StudioTertiary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text("GENERATE AI SCENE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }

      // Generation Progress
      if (isGenerating) {
        Spacer(modifier = Modifier.height(14.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Synthesizing neural video diffusion frames...", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { generationProgress },
              color = StudioTertiary,
              trackColor = Color(0xFF334155),
              modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
          }
        }
      }

      // Result Preview Card
      if (generatedSuccess) {
        Spacer(modifier = Modifier.height(14.dp))
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioTertiary, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Scene Generated Successfully!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B)),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(40.dp))
                Text("AI Scene ($aspectChoice • ${durationSec}s)", color = Color.White, fontSize = 12.sp)
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = onNavigateToEditor,
              colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Add to Video Editor Project", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}
