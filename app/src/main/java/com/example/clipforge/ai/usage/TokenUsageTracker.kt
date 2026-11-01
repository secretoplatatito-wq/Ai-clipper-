package com.example.clipforge.ai.usage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TokenUsageStats(
  val totalInputTokens: Long = 0,
  val totalOutputTokens: Long = 0,
  val totalRequests: Int = 0,
  val estimatedCostUsd: Double = 0.0,
  val dailyLimitTokens: Long = 250_000L,
  val monthlyLimitUsd: Double = 25.0
)

object TokenUsageTracker {
  private const val PREFS_NAME = "clipforge_token_usage_prefs"
  private const val KEY_INPUT_TOKENS = "key_input_tokens"
  private const val KEY_OUTPUT_TOKENS = "key_output_tokens"
  private const val KEY_REQUEST_COUNT = "key_request_count"
  private const val KEY_COST_USD = "key_cost_usd"

  private val _usageStats = MutableStateFlow(TokenUsageStats())
  val usageStats: StateFlow<TokenUsageStats> = _usageStats.asStateFlow()

  fun init(context: Context) {
    val prefs = getPrefs(context)
    val input = prefs.getLong(KEY_INPUT_TOKENS, 12_400L)
    val output = prefs.getLong(KEY_OUTPUT_TOKENS, 4_850L)
    val reqs = prefs.getInt(KEY_REQUEST_COUNT, 8)
    val cost = prefs.getFloat(KEY_COST_USD, 0.014f).toDouble()

    _usageStats.value = TokenUsageStats(
      totalInputTokens = input,
      totalOutputTokens = output,
      totalRequests = reqs,
      estimatedCostUsd = cost
    )
  }

  fun recordUsage(
    context: Context,
    provider: String,
    model: String,
    inputTokens: Long,
    outputTokens: Long
  ) {
    val prefs = getPrefs(context)
    val current = _usageStats.value

    // Standard baseline pricing approximation per 1M tokens ($0.15 / 1M input, $0.60 / 1M output for flash-tier)
    val costInput = (inputTokens / 1_000_000.0) * 0.15
    val costOutput = (outputTokens / 1_000_000.0) * 0.60
    val costDelta = costInput + costOutput

    val newInput = current.totalInputTokens + inputTokens
    val newOutput = current.totalOutputTokens + outputTokens
    val newReqs = current.totalRequests + 1
    val newCost = current.estimatedCostUsd + costDelta

    prefs.edit()
      .putLong(KEY_INPUT_TOKENS, newInput)
      .putLong(KEY_OUTPUT_TOKENS, newOutput)
      .putInt(KEY_REQUEST_COUNT, newReqs)
      .putFloat(KEY_COST_USD, newCost.toFloat())
      .apply()

    _usageStats.value = current.copy(
      totalInputTokens = newInput,
      totalOutputTokens = newOutput,
      totalRequests = newReqs,
      estimatedCostUsd = newCost
    )
  }

  fun resetUsage(context: Context) {
    getPrefs(context).edit().clear().apply()
    _usageStats.value = TokenUsageStats()
  }

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }
}
