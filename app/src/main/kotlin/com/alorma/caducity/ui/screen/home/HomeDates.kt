package com.alorma.caducity.ui.screen.home

import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** «diumenge, 11 d'octubre» (the header overline shows it in capitals). */
internal fun LocalDate.headerText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("EEEE, d MMMM", locale).format(toJavaLocalDate())

/** «dilluns 12». */
internal fun LocalDate.weekdayDayText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("EEEE d", locale).format(toJavaLocalDate())

internal fun String.capitalizeFirst(locale: Locale = Locale.getDefault()): String =
  replaceFirstChar { it.titlecase(locale) }
