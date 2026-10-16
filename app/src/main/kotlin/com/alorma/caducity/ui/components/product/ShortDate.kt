package com.alorma.caducity.ui.components.product

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** «4 oct», localized: the day a unit went into the freezer. */
@Composable
fun shortDateText(date: LocalDate): String = formatShortDate(date, LocalLocale.current.platformLocale)

fun formatShortDate(
  date: LocalDate,
  locale: Locale,
): String = DateTimeFormatter.ofPattern("d MMM", locale).format(date.toJavaLocalDate())
