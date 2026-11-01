package com.example.clipforge.video

import android.app.ActivityManager
import android.content.Context
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

enum class ProcessingPerformanceMode(val label: String, val description: String) {
  LOW_MEMORY("Low-Memory Safe Mode", "Optimized for devices with limited RAM. Uses 540p proxy previews and sequential processing."),
  BALANCED("Balanced Mode", "Standard mode with 720p hardware acceleration and adaptive caching."),
  HIGH_PERFORMANCE("High Performance Mode", "Unlocks 1080p/4K multi-threaded rendering and instant frame cache.")
}

data class DeviceHardwareReport(
  val totalRamMb: Long,
  val availableRamMb: Long,
  val isLowMemoryDevice: Boolean,
  val hasHardwareH264Encoder: Boolean,
  val hasHardwareH265Encoder: Boolean,
  val recommendedPerformanceMode: ProcessingPerformanceMode,
  val recommendedPreviewWidth: Int,
  val recommendedPreviewHeight: Int,
  val maxConcurrentExportJobs: Int,
  val batteryPercentage: Int,
  val isPowerSaveMode: Boolean,
  val thermalThrottling: Boolean
)

object DeviceCapabilityManager {
  private const val TAG = "DeviceCapabilityManager"

  fun inspectDevice(context: Context): DeviceHardwareReport {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memInfo)

    val rawTotalMem = if (memInfo.totalMem > 0L) memInfo.totalMem else Runtime.getRuntime().maxMemory()
    val rawAvailMem = if (memInfo.availMem > 0L) memInfo.availMem else Runtime.getRuntime().freeMemory()
    val totalRamMb = (rawTotalMem / (1024 * 1024)).coerceAtLeast(1024L)
    val availRamMb = (rawAvailMem / (1024 * 1024)).coerceAtLeast(512L)
    val isLowRam = memInfo.lowMemory || totalRamMb < 3500

    // Codec hardware checking
    val (hasH264, hasH265) = checkHardwareCodecs()

    // Battery & Power check
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val batteryPct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isPowerSave = powerManager?.isPowerSaveMode ?: false

    // Thermal status on Android 10+
    val thermalThrottled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
      powerManager.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
    } else {
      false
    }

    val mode = when {
      isLowRam || isPowerSave || thermalThrottled || totalRamMb < 3000 -> ProcessingPerformanceMode.LOW_MEMORY
      totalRamMb > 6000 && !isLowRam -> ProcessingPerformanceMode.HIGH_PERFORMANCE
      else -> ProcessingPerformanceMode.BALANCED
    }

    val (prevW, prevH) = when (mode) {
      ProcessingPerformanceMode.LOW_MEMORY -> 540 to 960
      ProcessingPerformanceMode.BALANCED -> 720 to 1280
      ProcessingPerformanceMode.HIGH_PERFORMANCE -> 1080 to 1920
    }

    val maxJobs = when (mode) {
      ProcessingPerformanceMode.LOW_MEMORY -> 1
      ProcessingPerformanceMode.BALANCED -> 2
      ProcessingPerformanceMode.HIGH_PERFORMANCE -> 3
    }

    Log.i(TAG, "Device inspected: RAM=${totalRamMb}MB (avail: ${availRamMb}MB), H264=$hasH264, H265=$hasH265, Mode=${mode.label}")

    return DeviceHardwareReport(
      totalRamMb = totalRamMb,
      availableRamMb = availRamMb,
      isLowMemoryDevice = isLowRam,
      hasHardwareH264Encoder = hasH264,
      hasHardwareH265Encoder = hasH265,
      recommendedPerformanceMode = mode,
      recommendedPreviewWidth = prevW,
      recommendedPreviewHeight = prevH,
      maxConcurrentExportJobs = maxJobs,
      batteryPercentage = batteryPct,
      isPowerSaveMode = isPowerSave,
      thermalThrottling = thermalThrottled
    )
  }

  private fun checkHardwareCodecs(): Pair<Boolean, Boolean> {
    var hasH264 = false
    var hasH265 = false
    try {
      val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
      for (codecInfo in codecList.codecInfos) {
        if (!codecInfo.isEncoder) continue
        val types = codecInfo.supportedTypes
        for (type in types) {
          if (type.equals(MediaFormat.MIMETYPE_VIDEO_AVC, ignoreCase = true)) {
            if (codecInfo.isHardwareAcceleratedCompat()) hasH264 = true
          }
          if (type.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true)) {
            if (codecInfo.isHardwareAcceleratedCompat()) hasH265 = true
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to enumerate codecs: ${e.message}")
    }
    return Pair(hasH264, hasH265)
  }

  private fun android.media.MediaCodecInfo.isHardwareAcceleratedCompat(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      this.isHardwareAccelerated
    } else {
      // Prior to API 29, software codecs traditionally started with "OMX.google."
      !this.name.startsWith("OMX.google.", ignoreCase = true)
    }
  }
}
