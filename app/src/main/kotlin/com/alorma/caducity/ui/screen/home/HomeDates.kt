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

/** «23 oct.»: day and short month, for the «Més endavant» interval. */
internal fun LocalDate.shortDateText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("d MMM", locale).format(toJavaLocalDate())

/** «dv. 23 oct». */
internal fun LocalDate.weekdayShortDateText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("EEE d MMM", locale).format(toJavaLocalDate())
