package com.alorma.caducity.feature.fakedata.models

/**
 * Error types that can occur during AI data generation
 */
sealed interface GeminiError {
  /**
   * Network connectivity issues
   */
  data class NetworkError(val cause: Throwable) : GeminiError

  /**
   * AI service quota/rate limit exceeded
   */
  data class QuotaExceeded(val message: String) : GeminiError

  /**
   * AI returned invalid or unexpected response format
   */
  data class InvalidResponse(val message: String) : GeminiError

  /**
   * Failed to parse AI response into expected structure
   */
  data class ParseError(val cause: Throwable) : GeminiError

  /**
   * Firebase not configured (missing google-services.json)
   */
  data object FirebaseNotConfigured : GeminiError

  /**
   * Vertex AI API not enabled in Firebase Console
   */
  data class ServiceDisabled(val projectUrl: String?) : GeminiError

  /**
   * Unknown/unexpected error
   */
  data class UnknownError(val cause: Throwable) : GeminiError
}

/**
 * Extension to convert generic throwables to GeminiError
 */
fun Throwable.toGeminiError(): GeminiError {
  val className = this::class.simpleName ?: ""
  val errorMessage = message ?: ""

  return when {
    // Check for specific exception types first
    className == "ServiceDisabledException" -> {
      // Extract project URL from error message if present
      val urlPattern = "https://console\\.firebase\\.google\\.com/project/[^\\s]+".toRegex()
      val projectUrl = urlPattern.find(errorMessage)?.value
      GeminiError.ServiceDisabled(projectUrl)
    }
    errorMessage.contains("quota", ignoreCase = true) ->
      GeminiError.QuotaExceeded(errorMessage)
    errorMessage.contains("network", ignoreCase = true) ||
    errorMessage.contains("connect", ignoreCase = true) ->
      GeminiError.NetworkError(this)
    errorMessage.contains("google-services.json", ignoreCase = true) ->
      GeminiError.FirebaseNotConfigured
    errorMessage.contains("parse", ignoreCase = true) ||
    errorMessage.contains("json", ignoreCase = true) ->
      GeminiError.ParseError(this)
    else -> GeminiError.UnknownError(this)
  }
}
