package com.example.clipforge.video

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class ExportSettings(
  val resolution: String = "1080p (FHD)",
  val width: Int = 1080,
  val height: Int = 1920,
  val fps: Int = 30,
  val bitrateMbps: Int = 12,
  val includeSubtitles: Boolean = true,
  val audioQualityKbps: Int = 192,
  val targetPlatform: String = "TikTok / Reels"
)

object VideoExportManager {
  private const val TAG = "VideoExportManager"

  suspend fun saveVideoToGallery(
    context: Context,
    srcFile: File,
    title: String
  ): Uri? = withContext(Dispatchers.IO) {
    val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val fileName = "ClipForge_${cleanTitle}_${System.currentTimeMillis()}.mp4"

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
          put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
          put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
          put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/ClipForge")
          put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = context.contentResolver.insert(collection, values)

        if (itemUri != null) {
          context.contentResolver.openOutputStream(itemUri)?.use { out ->
            FileInputStream(srcFile).use { input ->
              input.copyTo(out)
            }
          }
          values.clear()
          values.put(MediaStore.Video.Media.IS_PENDING, 0)
          context.contentResolver.update(itemUri, values, null, null)
          Log.d(TAG, "Saved clip to gallery MediaStore: $itemUri")
          return@withContext itemUri
        }
      }

      // Fallback for older or direct files
      val moviesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ClipForge")
      if (!moviesDir.exists()) moviesDir.mkdirs()
      val destFile = File(moviesDir, fileName)
      FileInputStream(srcFile).use { input ->
        FileOutputStream(destFile).use { out ->
          input.copyTo(out)
        }
      }
      Uri.fromFile(destFile)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save to gallery", e)
      null
    }
  }

  fun createShareIntent(context: Context, videoFile: File, title: String): Intent {
    val uri = try {
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", videoFile)
    } catch (_: Exception) {
      Uri.fromFile(videoFile)
    }

    return Intent(Intent.ACTION_SEND).apply {
      type = "video/mp4"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, "Created with ClipForge AI Studio: $title")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}
