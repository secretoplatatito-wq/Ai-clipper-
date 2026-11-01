package com.example.clipforge.ai.personalization

import android.content.Context
import android.content.SharedPreferences
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionStyle
import org.json.JSONObject

enum class EditingPace(val label: String, val averageSegmentSec: Float) {
  FAST_PACED("High-Energy Fast Pacing", 2.5f),
  MODERATE("Engaging Balanced Pacing", 4.0f),
  CINEMATIC("Thoughtful Storytelling", 7.0f)
}

enum class ZoomIntensity(val label: String, val scale: Float) {
  SUBTLE("Subtle Focus (1.08x)", 1.08f),
  STANDARD("Standard Punch-in (1.15x)", 1.15f),
  DYNAMIC("Dynamic Punch-in (1.25x)", 1.25f)
}

data class PersonalAIProfile(
  val pace: EditingPace = EditingPace.FAST_PACED,
  val zoomIntensity: ZoomIntensity = ZoomIntensity.STANDARD,
  val captionStyle: CaptionStyle = CaptionStyle.VIRAL_BOLD,
  val brandHighlightColorHex: String = "#FFB800", // Gold amber default
  val defaultAspectRatio: AspectRatioMode = AspectRatioMode.VERTICAL_9_16,
  val preferredClipDurationSec: Int = 30,
  val totalAcceptedClips: Int = 0,
  val totalRejectedClips: Int = 0,
  val autoBrollEnabled: Boolean = true,
  val soundEffectsEnabled: Boolean = true
)

object PersonalizationEngine {
  private const val PREFS_NAME = "clipforge_personal_profile"
  private const val KEY_PROFILE_JSON = "key_profile_json"

  fun loadProfile(context: Context): PersonalAIProfile {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_PROFILE_JSON, null) ?: return PersonalAIProfile()
    return try {
      val json = JSONObject(raw)
      PersonalAIProfile(
        pace = EditingPace.valueOf(json.optString("pace", EditingPace.FAST_PACED.name)),
        zoomIntensity = ZoomIntensity.valueOf(json.optString("zoomIntensity", ZoomIntensity.STANDARD.name)),
        captionStyle = CaptionStyle.valueOf(json.optString("captionStyle", CaptionStyle.VIRAL_BOLD.name)),
        brandHighlightColorHex = json.optString("brandHighlightColorHex", "#FFB800"),
        defaultAspectRatio = AspectRatioMode.valueOf(json.optString("defaultAspectRatio", AspectRatioMode.VERTICAL_9_16.name)),
        preferredClipDurationSec = json.optInt("preferredClipDurationSec", 30),
        totalAcceptedClips = json.optInt("totalAcceptedClips", 0),
        totalRejectedClips = json.optInt("totalRejectedClips", 0),
        autoBrollEnabled = json.optBoolean("autoBrollEnabled", true),
        soundEffectsEnabled = json.optBoolean("soundEffectsEnabled", true)
      )
    } catch (e: Exception) {
      PersonalAIProfile()
    }
  }

  fun saveProfile(context: Context, profile: PersonalAIProfile) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = JSONObject().apply {
      put("pace", profile.pace.name)
      put("zoomIntensity", profile.zoomIntensity.name)
      put("captionStyle", profile.captionStyle.name)
      put("brandHighlightColorHex", profile.brandHighlightColorHex)
      put("defaultAspectRatio", profile.defaultAspectRatio.name)
      put("preferredClipDurationSec", profile.preferredClipDurationSec)
      put("totalAcceptedClips", profile.totalAcceptedClips)
      put("totalRejectedClips", profile.totalRejectedClips)
      put("autoBrollEnabled", profile.autoBrollEnabled)
      put("soundEffectsEnabled", profile.soundEffectsEnabled)
    }
    prefs.edit().putString(KEY_PROFILE_JSON, json.toString()).apply()
  }

  fun recordClipDecision(context: Context, accepted: Boolean) {
    val current = loadProfile(context)
    val updated = if (accepted) {
      current.copy(totalAcceptedClips = current.totalAcceptedClips + 1)
    } else {
      current.copy(totalRejectedClips = current.totalRejectedClips + 1)
    }
    saveProfile(context, updated)
  }

  fun resetToDefaults(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
  }
}
