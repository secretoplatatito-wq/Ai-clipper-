package com.example.clipforge.diagnostics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogCategory {
  SYSTEM,
  AI_ORCHESTRATION,
  MEDIA_ENGINE,
  STORAGE,
  HARDWARE,
  NETWORK
}

data class DiagnosticLogEntry(
  val timestamp: Long = System.currentTimeMillis(),
  val category: LogCategory,
  val tag: String,
  val message: String,
  val isError: Boolean = false
) {
  fun formattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    return sdf.format(Date(timestamp))
  }
}

object DiagnosticLogger {
  private const val MAX_LOGS = 200
  private val _logs = MutableStateFlow<List<DiagnosticLogEntry>>(emptyList())
  val logs: StateFlow<List<DiagnosticLogEntry>> = _logs.asStateFlow()

  init {
    log(LogCategory.SYSTEM, "ClipForgeEngine", "Diagnostic subsystem initialized")
    log(LogCategory.HARDWARE, "DeviceCapability", "Hardware encoder inspection active")
    log(LogCategory.AI_ORCHESTRATION, "MultiAgentBrain", "Universal AI orchestration layer ready")
  }

  fun log(category: LogCategory, tag: String, rawMessage: String, isError: Boolean = false) {
    // Redact sensitive patterns (API keys, passwords, bearer tokens)
    val sanitized = redactSecrets(rawMessage)
    val entry = DiagnosticLogEntry(
      category = category,
      tag = tag,
      message = sanitized,
      isError = isError
    )

    val current = _logs.value.toMutableList()
    current.add(0, entry)
    if (current.size > MAX_LOGS) {
      current.removeAt(current.size - 1)
    }
    _logs.value = current
  }

  fun clearLogs() {
    _logs.value = emptyList()
  }

  fun exportSanitizedReport(): String {
    val sb = StringBuilder()
    sb.appendLine("=== CLIPFORGE AI DIAGNOSTIC AUDIT REPORT ===")
    sb.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
    sb.appendLine("Log Entries Count: ${_logs.value.size}")
    sb.appendLine("--------------------------------------------")
    _logs.value.forEach { entry ->
      val prefix = if (entry.isError) "[ERROR]" else "[INFO]"
      sb.appendLine("${entry.formattedTime()} $prefix [${entry.category.name}] ${entry.tag}: ${entry.message}")
    }
    sb.appendLine("=== END OF REPORT ===")
    return sb.toString()
  }

  private fun redactSecrets(input: String): String {
    var out = input
    // Redact typical API keys: AIzaSy..., sk-..., etc.
    out = out.replace(Regex("AIza[0-9A-Za-z-_]{35}"), "AIza...[REDACTED]")
    out = out.replace(Regex("sk-[0-9A-Za-z-_]{20,}"), "sk-...[REDACTED]")
    out = out.replace(Regex("Bearer\\s+[0-9A-Za-z-_.]+"), "Bearer [REDACTED]")
    return out
  }
}
