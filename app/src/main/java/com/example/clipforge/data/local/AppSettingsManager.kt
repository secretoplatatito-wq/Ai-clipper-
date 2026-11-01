package com.example.clipforge.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
  val defaultAspect: AspectRatioMode = AspectRatioMode.VERTICAL_9_16,
  val defaultCaptionStyle: CaptionStyle = CaptionStyle.VIRAL_BOLD,
  val defaultExportResolution: String = "1080p (1080x1920)",
  val exportBitrateMbps: Int = 12,
  val autoFaceTracking: Boolean = true,
  val lowMemoryMode: Boolean = false,
  val aiTemperature: Float = 0.7f
)

object AppSettingsManager {
  private const val PREFS_NAME = "clipforge_app_settings"
  private const val KEY_ASPECT = "key_aspect"
  private const val KEY_CAPTION = "key_caption"
  private const val KEY_RESOLUTION = "key_resolution"
  private const val KEY_BITRATE = "key_bitrate"
  private const val KEY_FACE_TRACKING = "key_face_tracking"
  private const val KEY_LOW_MEM = "key_low_mem"
  private const val KEY_TEMP = "key_temp"

  private val _settings = MutableStateFlow(AppSettings())
  val settings: StateFlow<AppSettings> = _settings.asStateFlow()

  fun init(context: Context) {
    val prefs = getPrefs(context)
    val aspectStr = prefs.getString(KEY_ASPECT, AspectRatioMode.VERTICAL_9_16.name)
    val captionStr = prefs.getString(KEY_CAPTION, CaptionStyle.VIRAL_BOLD.name)
    val resolution = prefs.getString(KEY_RESOLUTION, "1080p (1080x1920)") ?: "1080p (1080x1920)"
    val bitrate = prefs.getInt(KEY_BITRATE, 12)
    val faceTracking = prefs.getBoolean(KEY_FACE_TRACKING, true)
    val lowMem = prefs.getBoolean(KEY_LOW_MEM, false)
    val temp = prefs.getFloat(KEY_TEMP, 0.7f)

    val aspect = try {
      AspectRatioMode.valueOf(aspectStr ?: AspectRatioMode.VERTICAL_9_16.name)
    } catch (_: Exception) {
      AspectRatioMode.VERTICAL_9_16
    }

    val caption = try {
      CaptionStyle.valueOf(captionStr ?: CaptionStyle.VIRAL_BOLD.name)
    } catch (_: Exception) {
      CaptionStyle.VIRAL_BOLD
    }

    _settings.value = AppSettings(
      defaultAspect = aspect,
      defaultCaptionStyle = caption,
      defaultExportResolution = resolution,
      exportBitrateMbps = bitrate,
      autoFaceTracking = faceTracking,
      lowMemoryMode = lowMem,
      aiTemperature = temp
    )
  }

  fun updateSettings(context: Context, newSettings: AppSettings) {
    val prefs = getPrefs(context)
    prefs.edit()
      .putString(KEY_ASPECT, newSettings.defaultAspect.name)
      .putString(KEY_CAPTION, newSettings.defaultCaptionStyle.name)
      .putString(KEY_RESOLUTION, newSettings.defaultExportResolution)
      .putInt(KEY_BITRATE, newSettings.exportBitrateMbps)
      .putBoolean(KEY_FACE_TRACKING, newSettings.autoFaceTracking)
      .putBoolean(KEY_LOW_MEM, newSettings.lowMemoryMode)
      .putFloat(KEY_TEMP, newSettings.aiTemperature)
      .apply()

    _settings.value = newSettings
  }

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }
}
