package com.example.clipforge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.data.model.CaptionItem
import com.example.clipforge.data.model.CaptionStyle

@Composable
fun CaptionOverlay(
  captions: List<CaptionItem>,
  currentPlayheadMs: Long,
  modifier: Modifier = Modifier
) {
  // Find caption segment that covers current playback position
  val activeCaption = captions.firstOrNull {
    currentPlayheadMs in it.startMs..it.endMs
  } ?: return

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(bottom = 72.dp),
      contentAlignment = Alignment.Center
    ) {
      when (activeCaption.style) {
        CaptionStyle.VIRAL_BOLD -> {
          ViralBoldCaptionView(activeCaption, currentPlayheadMs)
        }
        CaptionStyle.KARAOKE_POP -> {
          KaraokePopCaptionView(activeCaption, currentPlayheadMs)
        }
        CaptionStyle.PODCAST_CLEAN -> {
          PodcastCleanCaptionView(activeCaption)
        }
        CaptionStyle.CINEMATIC -> {
          CinematicCaptionView(activeCaption)
        }
        CaptionStyle.GAMING_NEON -> {
          GamingNeonCaptionView(activeCaption, currentPlayheadMs)
        }
        CaptionStyle.MINIMAL -> {
          MinimalCaptionView(activeCaption)
        }
      }
    }
  }
}

@Composable
private fun ViralBoldCaptionView(caption: CaptionItem, currentMs: Long) {
  Box(
    modifier = Modifier
      .background(Color(0xCC000000), RoundedCornerShape(8.dp))
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (caption.words.isNotEmpty()) {
        caption.words.forEach { word ->
          val isCurrent = currentMs in word.startMs..word.endMs
          val wordColor = if (isCurrent) Color(0xFFFACC15) else Color.White
          val scale by animateFloatAsState(if (isCurrent) 1.18f else 1.0f, label = "scale")

          Text(
            text = "${word.word} ",
            color = wordColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(scale)
          )
        }
      } else {
        Text(
          text = caption.text,
          color = Color(0xFFFACC15),
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Composable
private fun KaraokePopCaptionView(caption: CaptionItem, currentMs: Long) {
  Row(
    modifier = Modifier
      .background(Color(0xAA0F172A), RoundedCornerShape(12.dp))
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (caption.words.isNotEmpty()) {
      caption.words.forEach { word ->
        val isCurrent = currentMs in word.startMs..word.endMs
        val isPassed = currentMs > word.endMs
        val wordColor = when {
          isCurrent -> Color(0xFF38BDF8) // Bright Sky Blue
          isPassed -> Color(0xFFFACC15)  // Golden Yellow
          else -> Color.White.copy(alpha = 0.7f)
        }
        val scale by animateFloatAsState(if (isCurrent) 1.25f else 1.0f, label = "scale")

        Text(
          text = "${word.word} ",
          color = wordColor,
          fontSize = 23.sp,
          fontWeight = FontWeight.ExtraBold,
          modifier = Modifier.scale(scale)
        )
      }
    } else {
      Text(
        text = caption.text,
        color = Color(0xFFFACC15),
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold
      )
    }
  }
}

@Composable
private fun PodcastCleanCaptionView(caption: CaptionItem) {
  Box(
    modifier = Modifier
      .background(Color(0xEE1E293B), RoundedCornerShape(8.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    Text(
      text = caption.text,
      color = Color(0xFFF8FAFC),
      fontSize = 18.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun CinematicCaptionView(caption: CaptionItem) {
  Text(
    text = caption.text.uppercase(),
    color = Color(0xFFFEF08A),
    fontSize = 16.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = FontFamily.Serif,
    letterSpacing = 2.sp,
    textAlign = TextAlign.Center,
    modifier = Modifier.shadow(4.dp)
  )
}

@Composable
private fun GamingNeonCaptionView(caption: CaptionItem, currentMs: Long) {
  Box(
    modifier = Modifier
      .background(Color(0xDD000000), RoundedCornerShape(6.dp))
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = caption.text,
      color = Color(0xFF4ADE80), // Neon Green
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun MinimalCaptionView(caption: CaptionItem) {
  Text(
    text = caption.text,
    color = Color.White,
    fontSize = 17.sp,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.Center
  )
}
