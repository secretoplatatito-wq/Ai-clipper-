package com.example.clipforge.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.util.LruCache
import com.example.clipforge.data.model.Timeline
import org.json.JSONObject
import java.io.File

data class MediaIntegrityResult(
  val isValid: Boolean,
  val hasVideoTrack: Boolean,
  val hasAudioTrack: Boolean,
  val durationUs: Long,
  val errorMessage: String? = null
)

object ReliabilitySubsystem {
  private const val TAG = "ReliabilitySubsystem"

  // 1. Strict Media Integrity Validator
  fun validateMediaFile(context: Context, uri: Uri): MediaIntegrityResult {
    val extractor = MediaExtractor()
    var hasVideo = false
    var hasAudio = false
    var durationUs = 0L

    return try {
      extractor.setDataSource(context, uri, null)
      val trackCount = extractor.trackCount
      for (i in 0 until trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
        if (mime.startsWith("video/")) {
          hasVideo = true
          if (format.containsKey(MediaFormat.KEY_DURATION)) {
            val d = format.getLong(MediaFormat.KEY_DURATION)
            if (d > durationUs) durationUs = d
          }
        } else if (mime.startsWith("audio/")) {
          hasAudio = true
          if (format.containsKey(MediaFormat.KEY_DURATION)) {
            val d = format.getLong(MediaFormat.KEY_DURATION)
            if (d > durationUs) durationUs = d
          }
        }
      }

      if (!hasVideo) {
        MediaIntegrityResult(false, hasVideoTrack = false, hasAudioTrack = hasAudio, durationUs = 0, errorMessage = "No playable video track found in media container.")
      } else if (durationUs <= 0L) {
        MediaIntegrityResult(false, hasVideoTrack = true, hasAudioTrack = hasAudio, durationUs = 0, errorMessage = "Corrupted video duration (0ms).")
      } else {
        MediaIntegrityResult(true, hasVideoTrack = true, hasAudioTrack = hasAudio, durationUs = durationUs)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Media integrity check failed for $uri: ${e.message}")
      MediaIntegrityResult(false, false, false, 0, e.message ?: "Failed to parse video header.")
    } finally {
      try {
        extractor.release()
      } catch (ignored: Exception) {}
    }
  }

  // 2. Storage Capacity Monitor
  fun checkAvailableStorageMb(context: Context): Long {
    return try {
      val path = context.getExternalFilesDir(null) ?: context.filesDir
      val stat = StatFs(path.path)
      val calculated = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
      if (calculated > 0L) calculated else 1024L
    } catch (e: Exception) {
      1024L
    }
  }

  fun hasSufficientSpaceForExport(context: Context, estimatedSizeMb: Long = 100L): Boolean {
    val availMb = checkAvailableStorageMb(context)
    return availMb >= estimatedSizeMb + 50L // 50MB safety buffer
  }

  // 3. Bounded Frame Memory Cache (Prevents OutOfMemory on 4K/long video scrub)
  object BoundedFrameCache {
    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = maxMemoryKb / 8 // allocate 1/8th of available heap for frame cache

    private val cache = object : LruCache<String, Bitmap>(cacheSizeKb) {
      override fun sizeOf(key: String, bitmap: Bitmap): Int {
        return bitmap.byteCount / 1024
      }
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
      if (cache.get(key) == null) {
        cache.put(key, bitmap)
      }
    }

    fun cacheSize(): Int = cache.size()

    fun clear() {
      cache.evictAll()
    }

    fun evictAll() {
      cache.evictAll()
    }
  }


  // 4. Crash Recovery & Timeline Checkpoint Persistence
  object CheckpointManager {
    fun saveCheckpoint(context: Context, projectId: String, timelineJson: String) {
      try {
        val file = File(context.cacheDir, "checkpoint_${projectId}.json")
        file.writeText(timelineJson)
      } catch (e: Exception) {
        Log.w(TAG, "Failed to save recovery checkpoint: ${e.message}")
      }
    }

    fun readCheckpoint(context: Context, projectId: String): String? {
      return try {
        val file = File(context.cacheDir, "checkpoint_${projectId}.json")
        if (file.exists()) file.readText() else null
      } catch (e: Exception) {
        null
      }
    }

    fun clearCheckpoint(context: Context, projectId: String) {
      try {
        val file = File(context.cacheDir, "checkpoint_${projectId}.json")
        if (file.exists()) file.delete()
      } catch (ignored: Exception) {}
    }
  }
}
