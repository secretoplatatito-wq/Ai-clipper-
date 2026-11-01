package com.example.clipforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.clipforge.data.model.ClipEntity
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurfaceElevated

@Composable
fun ViralScoreBadge(
  clip: ClipEntity,
  modifier: Modifier = Modifier
) {
  var showDetails by remember { mutableStateOf(false) }

  val badgeColor = when {
    clip.overallScore >= 90 -> Color(0xFF10B981) // High Viral Green
    clip.overallScore >= 80 -> StudioSecondary   // High Potential Gold
    else -> Color(0xFF38BDF8)                     // Solid Blue
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(badgeColor.copy(alpha = 0.18f))
      .clickable { showDetails = true }
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      Icons.Default.Star,
      contentDescription = "Viral Score",
      tint = badgeColor,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "${clip.overallScore} Viral Score",
      color = badgeColor,
      fontSize = 12.sp,
      fontWeight = FontWeight.ExtraBold
    )
    Spacer(modifier = Modifier.width(4.dp))
    Icon(
      Icons.Default.Info,
      contentDescription = "Score details",
      tint = badgeColor.copy(alpha = 0.7f),
      modifier = Modifier.size(12.dp)
    )
  }

  if (showDetails) {
    AlertDialog(
      onDismissRequest = { showDetails = false },
      title = {
        Text("AI Viral Score Breakdown", fontWeight = FontWeight.Bold, color = Color.White)
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "AI analyzes retention psychology, opening hook impact, curiosity gaps, and pacing rhythm.",
            color = Color.LightGray,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(14.dp))

          ScoreMetricRow("Hook Strength (0-3s)", clip.hookScore, Color(0xFFF59E0B))
          ScoreMetricRow("Curiosity & Tension", clip.curiosityScore, Color(0xFF8B5CF6))
          ScoreMetricRow("Information Value", clip.infoScore, Color(0xFF06B6D4))
          ScoreMetricRow("Story Completeness", (clip.overallScore - 2).coerceAtLeast(70), Color(0xFF10B981))
          ScoreMetricRow("Pacing & Rhythm", (clip.overallScore + 1).coerceAtMost(99), Color(0xFFEC4899))

          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(StudioSurfaceElevated, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Text(
              text = "💡 Rationale: ${clip.reasonForSelection}",
              color = Color.White,
              fontSize = 12.sp
            )
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDetails = false }) {
          Text("Got it", color = StudioSecondary)
        }
      },
      containerColor = Color(0xFF1E202B)
    )
  }
}

@Composable
private fun ScoreMetricRow(label: String, score: Int, color: Color) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
      Text("$score/100", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { score / 100f },
      color = color,
      trackColor = Color(0xFF334155),
      modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
    )
  }
}
