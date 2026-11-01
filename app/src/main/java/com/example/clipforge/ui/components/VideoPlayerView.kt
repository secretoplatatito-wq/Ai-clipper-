package com.example.clipforge.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.clipforge.data.model.AspectRatioMode
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
  videoUri: Uri?,
  isPlaying: Boolean,
  startTrimMs: Long = 0L,
  endTrimMs: Long = Long.MAX_VALUE,
  speed: Float = 1.0f,
  zoomScale: Float = 1.0f,
  panCenterX: Float = 0.5f,
  panCenterY: Float = 0.5f,
  aspectRatio: AspectRatioMode = AspectRatioMode.VERTICAL_9_16,
  onProgressUpdate: (currentMs: Long, totalMs: Long) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
  var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
  var isPrepared by remember { mutableStateOf(false) }

  // Sync play/pause
  LaunchedEffect(isPlaying, isPrepared) {
    val vv = videoViewRef ?: return@LaunchedEffect
    if (isPrepared) {
      if (isPlaying) {
        vv.start()
      } else {
        vv.pause()
      }
    }
  }

  // Playhead position tracker & loop within trim bounds
  LaunchedEffect(isPlaying, isPrepared, startTrimMs, endTrimMs) {
    while (true) {
      val vv = videoViewRef
      if (vv != null && isPrepared) {
        val cur = vv.currentPosition.toLong()
        val dur = vv.duration.toLong().coerceAtLeast(1L)
        onProgressUpdate(cur, dur)

        // Loop if past endTrimMs
        if (cur >= endTrimMs && endTrimMs > startTrimMs) {
          vv.seekTo(startTrimMs.toInt())
          if (isPlaying) vv.start()
        }
      }
      delay(50)
    }
  }

  // Aspect ratio and zoom transformation
  val offsetX = (0.5f - panCenterX) * 200f
  val offsetY = (0.5f - panCenterY) * 200f

  Box(
    modifier = modifier
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    if (videoUri != null) {
      AndroidView(
        factory = { ctx ->
          VideoView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnPreparedListener { mp ->
              mediaPlayerRef = mp
              isPrepared = true
              mp.isLooping = (endTrimMs == Long.MAX_VALUE)
              try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                  mp.playbackParams = mp.playbackParams.setSpeed(speed.coerceIn(0.25f, 2.0f))
                }
              } catch (_: Exception) {}
              if (startTrimMs > 0) {
                seekTo(startTrimMs.toInt())
              }
              if (isPlaying) start()
            }
            setOnErrorListener { _, what, extra ->
              Log.w("VideoPlayerView", "VideoView playback error: $what, $extra")
              true
            }
            setVideoURI(videoUri)
            videoViewRef = this
          }
        },
        update = { vv ->
          try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
              mediaPlayerRef?.playbackParams = mediaPlayerRef?.playbackParams?.setSpeed(speed.coerceIn(0.25f, 2.0f))
                ?: android.media.PlaybackParams().setSpeed(speed)
            }
          } catch (_: Exception) {}
        },
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = zoomScale
            scaleY = zoomScale
            translationX = offsetX
            translationY = offsetY
          }
      )
    }

    DisposableEffect(videoUri) {
      onDispose {
        videoViewRef?.stopPlayback()
        videoViewRef = null
        mediaPlayerRef = null
        isPrepared = false
      }
    }
  }
}
