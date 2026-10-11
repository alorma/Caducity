package com.alorma.caducity.config.preferences

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** How many days before the expiration date a product counts as «Aviat» (1, 3 or 7). */
class SoonThresholdPreferences(
  private val settings: Settings,
) {
  private val state = MutableStateFlow(read())

  val thresholdDays: Flow<Int> get() = state

  fun current(): Int = state.value

  fun setThresholdDays(days: Int) {
    require(days in ALLOWED_DAYS) { "Unsupported threshold: $days" }
    settings.putInt(KEY, days)
    state.value = days
  }

  private fun read(): Int = settings.getInt(KEY, DEFAULT_DAYS).takeIf { it in ALLOWED_DAYS } ?: DEFAULT_DAYS

  companion object {
    const val KEY = "soon_threshold_days"
    const val DEFAULT_DAYS = 3
    val ALLOWED_DAYS = listOf(1, 3, 7)
  }
}
