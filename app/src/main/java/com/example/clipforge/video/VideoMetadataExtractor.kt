package com.example.clipforge.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class VideoMetadata(
  val durationMs: Long,
  val width: Int,
  val height: Int,
  val rotation: Int,
  val fps: Int,
  val bitrate: Long,
  val mimeType: String,
  val thumbnailPath: String?
)

object VideoMetadataExtractor {
  private const val TAG = "VideoMetadataExtractor"

  suspend fun extractMetadata(context: Context, videoUri: Uri): VideoMetadata = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(context, videoUri)

      val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      val durationMs = durationStr?.toLongOrNull() ?: 30000L

      val rawWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1920
      val rawHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1080
      val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

      // Adjust for orientation rotation
      val width = if (rotation == 90 || rotation == 270) rawHeight else rawWidth
      val height = if (rotation == 90 || rotation == 270) rawWidth else rawHeight

      val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
      val bitrate = bitrateStr?.toLongOrNull() ?: 8_000_000L

      val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"

      // Extract a representative thumbnail at 1s or middle
      val targetTimeUs = if (durationMs > 2000L) 1_000_000L else (durationMs * 500L)
      val frameBitmap = retriever.getFrameAtTime(targetTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

      var thumbnailPath: String? = null
      if (frameBitmap != null) {
        val thumbFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.jpg")
        FileOutputStream(thumbFile).use { out ->
          frameBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        thumbnailPath = thumbFile.absolutePath
      }

      VideoMetadata(
        durationMs = durationMs,
        width = width,
        height = height,
        rotation = rotation,
        fps = 30,
        bitrate = bitrate,
        mimeType = mimeType,
        thumbnailPath = thumbnailPath
      )
    } catch (e: Exception) {
      Log.e(TAG, "Failed to extract metadata from $videoUri", e)
      VideoMetadata(
        durationMs = 45000L,
        width = 1920,
        height = 1080,
        rotation = 0,
        fps = 30,
        bitrate = 8000000L,
        mimeType = "video/mp4",
        thumbnailPath = null
      )
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {}
    }
  }

  suspend fun extractFrames(context: Context, videoUri: Uri, sampleCount: Int = 5): List<Pair<Long, Bitmap>> = withContext(Dispatchers.IO) {
    val results = mutableListOf<Pair<Long, Bitmap>>()
    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(context, videoUri)
      val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 30000L
      val stepMs = (durationMs / (sampleCount + 1)).coerceAtLeast(1000L)

      for (i in 1..sampleCount) {
        val timeMs = i * stepMs
        val bitmap = retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        if (bitmap != null) {
          results.add(Pair(timeMs, bitmap))
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to extract frames", e)
    } finally {
      try { retriever.release() } catch (_: Exception) {}
    }
    results
  }
}
