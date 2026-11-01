package com.example.clipforge.ai.provider

import android.util.Log
import com.example.clipforge.data.model.AIProviderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TestConnectionResult(
  val success: Boolean,
  val message: String,
  val latencyMs: Long = 0,
  val discoveredModels: List<String> = emptyList()
)

object AIProviderManager {
  private const val TAG = "AIProviderManager"
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(45, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun testConnection(provider: AIProviderEntity): TestConnectionResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      when (provider.type) {
        "GEMINI" -> {
          val url = "https://generativelanguage.googleapis.com/v1beta/models?key=${provider.apiKey}"
          val request = Request.Builder().url(url).get().build()
          client.newCall(request).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
              val body = response.body?.string() ?: ""
              val models = parseGeminiModels(body)
              TestConnectionResult(true, "Connected to Gemini (${models.size} models available)", latency, models)
            } else {
              TestConnectionResult(false, "Gemini HTTP ${response.code}: ${response.message}", latency)
            }
          }
        }
        "OPENAI" -> {
          val url = "${provider.baseUrl.trimEnd('/')}/v1/models"
          val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${provider.apiKey}")
            .get()
            .build()
          client.newCall(request).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
              val body = response.body?.string() ?: ""
              val models = parseOpenAIModels(body)
              TestConnectionResult(true, "Connected to OpenAI (${models.size} models found)", latency, models)
            } else {
              TestConnectionResult(false, "OpenAI HTTP ${response.code}: ${response.message}", latency)
            }
          }
        }
        "CLAUDE" -> {
          // Anthropic test ping
          val url = "https://api.anthropic.com/v1/messages"
          val bodyJson = JSONObject().apply {
            put("model", provider.selectedModel.ifBlank { "claude-3-5-haiku-20241022" })
            put("max_tokens", 10)
            put("messages", JSONArray().put(JSONObject().apply {
              put("role", "user")
              put("content", "ping")
            }))
          }
          val request = Request.Builder()
            .url(url)
            .header("x-api-key", provider.apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()
          client.newCall(request).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful || response.code == 200) {
              TestConnectionResult(true, "Connected to Anthropic Claude", latency, listOf("claude-3-5-sonnet", "claude-3-5-haiku"))
            } else {
              TestConnectionResult(false, "Claude HTTP ${response.code}: ${response.message}", latency)
            }
          }
        }
        "OLLAMA_LOCAL" -> {
          val url = "${provider.baseUrl.trimEnd('/')}/api/tags"
          val request = Request.Builder().url(url).get().build()
          client.newCall(request).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
              val body = response.body?.string() ?: ""
              val models = parseOllamaModels(body)
              TestConnectionResult(true, "Connected to Local Ollama (${models.size} models ready)", latency, models)
            } else {
              TestConnectionResult(false, "Ollama HTTP ${response.code}: Check server address & network", latency)
            }
          }
        }
        "LM_STUDIO_LOCAL", "CUSTOM_SERVER", "DEEPSEEK", "OPENROUTER" -> {
          val url = "${provider.baseUrl.trimEnd('/')}/v1/models"
          val reqBuilder = Request.Builder().url(url).get()
          if (provider.apiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer ${provider.apiKey}")
          }
          client.newCall(reqBuilder.build()).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
              val body = response.body?.string() ?: ""
              val models = parseOpenAIModels(body)
              TestConnectionResult(true, "Connected successfully (${models.size} models)", latency, models)
            } else {
              TestConnectionResult(false, "Server returned HTTP ${response.code}", latency)
            }
          }
        }
        else -> TestConnectionResult(false, "Unknown provider type: ${provider.type}")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Connection test failed", e)
      val msg = if (provider.isLocal) {
        "Cannot reach local server at ${provider.baseUrl}. Ensure device is on the same Wi-Fi and host allows connections (e.g. OLLAMA_HOST=0.0.0.0)."
      } else {
        "Network error: ${e.localizedMessage ?: "Connection refused"}"
      }
      TestConnectionResult(false, msg)
    }
  }

  suspend fun generateCompletion(
    provider: AIProviderEntity?,
    systemPrompt: String,
    userPrompt: String
  ): String = withContext(Dispatchers.IO) {
    if (provider == null || provider.apiKey.isBlank() && !provider.isLocal) {
      // Local intelligent fallback engine if no external API key is configured
      return@withContext ""
    }

    try {
      when (provider.type) {
        "GEMINI" -> {
          val model = provider.selectedModel.ifBlank { "gemini-3.8-flash" }
          val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${provider.apiKey}"
          val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
              put("parts", JSONArray().put(JSONObject().apply {
                put("text", "$systemPrompt\n\n$userPrompt")
              }))
            }))
          }
          val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()
          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              val json = JSONObject(response.body?.string() ?: "")
              val candidates = json.optJSONArray("candidates")
              if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                  return@withContext parts.getJSONObject(0).optString("text", "")
                }
              }
            }
          }
        }
        "OPENAI", "DEEPSEEK", "OPENROUTER", "LM_STUDIO_LOCAL", "CUSTOM_SERVER" -> {
          val url = "${provider.baseUrl.trimEnd('/')}/v1/chat/completions"
          val bodyJson = JSONObject().apply {
            put("model", provider.selectedModel)
            put("messages", JSONArray().apply {
              put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
              })
              put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
              })
            })
            put("temperature", 0.7)
          }
          val reqBuilder = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
          if (provider.apiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer ${provider.apiKey}")
          }
          client.newCall(reqBuilder.build()).execute().use { response ->
            if (response.isSuccessful) {
              val json = JSONObject(response.body?.string() ?: "")
              val choices = json.optJSONArray("choices")
              if (choices != null && choices.length() > 0) {
                return@withContext choices.getJSONObject(0).optJSONObject("message")?.optString("content", "") ?: ""
              }
            }
          }
        }
        "OLLAMA_LOCAL" -> {
          val url = "${provider.baseUrl.trimEnd('/')}/api/generate"
          val bodyJson = JSONObject().apply {
            put("model", provider.selectedModel)
            put("prompt", "$systemPrompt\n\n$userPrompt")
            put("stream", false)
          }
          val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()
          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              val json = JSONObject(response.body?.string() ?: "")
              return@withContext json.optString("response", "")
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "generateCompletion failed for ${provider.type}", e)
    }
    ""
  }

  private fun parseGeminiModels(jsonStr: String): List<String> {
    val list = mutableListOf<String>()
    try {
      val obj = JSONObject(jsonStr)
      val models = obj.optJSONArray("models") ?: return list
      for (i in 0 until models.length()) {
        val name = models.getJSONObject(i).optString("name", "").replace("models/", "")
        if (name.contains("gemini")) list.add(name)
      }
    } catch (_: Exception) {}
    if (list.isEmpty()) {
      list.addAll(listOf("gemini-3.8-flash", "gemini-4-argon", "gemini-3.5-flash-lite"))
    }
    return list
  }

  private fun parseOpenAIModels(jsonStr: String): List<String> {
    val list = mutableListOf<String>()
    try {
      val obj = JSONObject(jsonStr)
      val data = obj.optJSONArray("data") ?: return list
      for (i in 0 until data.length()) {
        val id = data.getJSONObject(i).optString("id", "")
        if (id.isNotBlank()) list.add(id)
      }
    } catch (_: Exception) {}
    return list
  }

  private fun parseOllamaModels(jsonStr: String): List<String> {
    val list = mutableListOf<String>()
    try {
      val obj = JSONObject(jsonStr)
      val models = obj.optJSONArray("models") ?: return list
      for (i in 0 until models.length()) {
        val name = models.getJSONObject(i).optString("name", "")
        if (name.isNotBlank()) list.add(name)
      }
    } catch (_: Exception) {}
    return list
  }
}
