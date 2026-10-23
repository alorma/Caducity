package com.alorma.caducity.ui.screen.add

import androidx.annotation.StringRes
import com.alorma.caducity.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Pure parsing and formatting of the typed expiration date (`dd/mm/aa` or `dd/mm/aaaa`). */
object ExpiryDateParser {
  private const val MAX_DIGITS = 8
  private const val SHORT_YEAR_BASE = 2000
  private val separators = charArrayOf('/', '.', '-', ' ')

  /** Keeps only digits (max 8) so the field can re-insert the slashes itself. */
  fun digitsOf(input: String): String = input.filter { it.isDigit() }.take(MAX_DIGITS)

  /**
   * Digits for a typed or pasted [text]: a complete separated date (`1/2/26`) is padded to `010226`,
   * anything else just keeps its digits.
   */
  fun normalize(text: String): String {
    val date = parse(text)
    val hasSeparators = text.any { it in separators }
    if (date == null || !hasSeparators) return digitsOf(text)
    val parts = text.trim().split(*separators).filter { it.isNotEmpty() }
    return parts[0].padStart(2, '0') + parts[1].padStart(2, '0') + parts[2]
  }

  /** `01022026` → `01/02/2026`, `010` → `01/0`. Slashes appear as soon as the next group starts. */
  fun format(digits: String): String =
    buildString {
      digits.forEachIndexed { index, char ->
        if (index == 2 || index == 4) append('/')
        append(char)
      }
    }

  /** Returns the date typed in [input], or null when it is incomplete or does not exist. */
  fun parse(input: String): LocalDate? {
    val text = input.trim()
    val parts = text.split(*separators).filter { it.isNotEmpty() }
    val (day, month, year) =
      when {
        parts.size == 3 && parts.all { it.isDigitsOnly() } -> parts
        parts.size == 1 && parts[0].isPackedDate() ->
          listOf(parts[0].take(2), parts[0].substring(2, 4), parts[0].substring(4))
        else -> return null
      }

    val yearNumber =
      when (year.length) {
        2 -> SHORT_YEAR_BASE + year.toInt()
        4 -> year.toInt()
        else -> return null
      }
    if (day.length > 2 || month.length > 2) return null

    return try {
      LocalDate(yearNumber, month.toInt(), day.toInt())
    } catch (_: IllegalArgumentException) {
      null
    }
  }
}

/** Quick date chips, always counted from today. */
enum class QuickDate(
  @StringRes val labelRes: Int,
) {
  Tomorrow(R.string.add_quick_tomorrow),
  ThreeDays(R.string.add_quick_3_days),
  OneWeek(R.string.add_quick_1_week),
  TwoWeeks(R.string.add_quick_2_weeks),
  OneMonth(R.string.add_quick_1_month),
  ThreeMonths(R.string.add_quick_3_months),
  ;

  fun from(today: LocalDate): LocalDate =
    when (this) {
      Tomorrow -> today.plus(DatePeriod(days = 1))
      ThreeDays -> today.plus(DatePeriod(days = 3))
      OneWeek -> today.plus(DatePeriod(days = 7))
      TwoWeeks -> today.plus(DatePeriod(days = 14))
      OneMonth -> today.plus(DatePeriod(months = 1))
      ThreeMonths -> today.plus(DatePeriod(months = 3))
    }
}

private fun String.isDigitsOnly(): Boolean = all { it.isDigit() }

private fun String.isPackedDate(): Boolean = isDigitsOnly() && (length == 6 || length == 8)
