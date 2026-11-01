package com.example.clipforge.video

import android.graphics.Bitmap
import android.graphics.PointF
import android.media.FaceDetector
import android.util.Log

data class SpeakerCropWindow(
  val centerXRatio: Float, // 0.0 to 1.0 (0.5 is dead center)
  val centerYRatio: Float,
  val confidence: Float,
  val faceCount: Int
)

object FaceAndSpeakerTracker {
  private const val TAG = "FaceAndSpeakerTracker"

  fun analyzeFrame(bitmap: Bitmap): SpeakerCropWindow {
    try {
      // FaceDetector requires Bitmap.Config.RGB_565 and even width
      val evenWidth = if (bitmap.width % 2 != 0) bitmap.width - 1 else bitmap.width
      val evenHeight = if (bitmap.height % 2 != 0) bitmap.height - 1 else bitmap.height

      // Scale down to max 640px wide for speed and responsiveness
      val scale = if (evenWidth > 640) 640f / evenWidth else 1.0f
      val targetW = (evenWidth * scale).toInt().let { if (it % 2 != 0) it - 1 else it }
      val targetH = (evenHeight * scale).toInt().let { if (it % 2 != 0) it - 1 else it }

      val rgb565Bitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, false)
        .copy(Bitmap.Config.RGB_565, false)

      val maxFaces = 3
      val faces = arrayOfNulls<FaceDetector.Face>(maxFaces)
      val detector = FaceDetector(targetW, targetH, maxFaces)
      val count = detector.findFaces(rgb565Bitmap, faces)

      if (count > 0 && faces[0] != null) {
        val midPoint = PointF()
        faces[0]!!.getMidPoint(midPoint)
        val conf = faces[0]!!.confidence()

        val normalizedX = (midPoint.x / targetW).coerceIn(0.2f, 0.8f)
        val normalizedY = (midPoint.y / targetH).coerceIn(0.25f, 0.75f)

        return SpeakerCropWindow(
          centerXRatio = normalizedX,
          centerYRatio = normalizedY,
          confidence = conf,
          faceCount = count
        )
      }
    } catch (e: Exception) {
      Log.w(TAG, "Face detection failed, defaulting to center", e)
    }

    // Default to optical center if no face detected
    return SpeakerCropWindow(
      centerXRatio = 0.5f,
      centerYRatio = 0.45f,
      confidence = 0.5f,
      faceCount = 0
    )
  }

  fun smoothTrackedCenters(
    keyframes: List<Pair<Long, SpeakerCropWindow>>,
    smoothingFactor: Float = 0.25f
  ): List<Pair<Long, SpeakerCropWindow>> {
    if (keyframes.isEmpty()) return emptyList()

    val result = mutableListOf<Pair<Long, SpeakerCropWindow>>()
    var runningX = keyframes.first().second.centerXRatio
    var runningY = keyframes.first().second.centerYRatio

    for (kf in keyframes) {
      val targetX = kf.second.centerXRatio
      val targetY = kf.second.centerYRatio

      // Exponential moving average for smooth pan
      runningX = runningX + (targetX - runningX) * smoothingFactor
      runningY = runningY + (targetY - runningY) * smoothingFactor

      result.add(
        Pair(
          kf.first,
          kf.second.copy(
            centerXRatio = runningX,
            centerYRatio = runningY
          )
        )
      )
    }
    return result
  }
}
