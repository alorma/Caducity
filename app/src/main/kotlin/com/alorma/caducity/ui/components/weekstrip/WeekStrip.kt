package com.alorma.caducity.ui.components.weekstrip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.domain.usecase.WeekStripData
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate

/**
 * The next 7 days starting today, one dot per unit that expires that day (max 3). Swipe for the
 * following 7 days. Tapping the selected day again deselects it.
 */
@Composable
fun WeekStrip(
  data: WeekStripData,
  selected: LocalDate?,
  onDayClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(pageCount = { ObserveWeekStripUseCase.WINDOWS })
  HorizontalPager(
    state = pagerState,
    modifier =
      modifier
        .fillMaxWidth()
        .selectableGroup(),
    verticalAlignment = Alignment.Top,
  ) { page ->
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
      data.window(page).forEach { day ->
        WeekDayCell(
          day = day,
          isToday = day.date == data.today,
          isSelected = day.date == selected,
          onClick = { onDayClick(day.date) },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun WeekDayCell(
  day: WeekStripDay,
  isToday: Boolean,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val container =
    when {
      isSelected -> CaducityTheme.colorScheme.primary
      isToday -> CaducityTheme.colorScheme.surfaceContainer
      else -> Color.Transparent
    }
  val content = if (isSelected) CaducityTheme.colorScheme.onPrimary else CaducityTheme.colorScheme.onSurface
  val description = dayDescription(day)
  Surface(
    selected = isSelected,
    onClick = onClick,
    modifier =
      modifier.semantics {
        role = Role.Tab
        contentDescription = description
      },
    shape = RoundedCornerShape(18.dp),
    color = container,
    contentColor = content,
  ) {
    Column(
      modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = day.date.initial(),
        style = CaducityTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        color = if (isSelected) content else CaducityTheme.colorScheme.onSurfaceVariant,
      )
      Box(
        modifier =
          Modifier
            .size(34.dp)
            .then(
              if (isToday && !isSelected) {
                Modifier.border(BorderStroke(2.dp, CaducityTheme.colorScheme.onSurface), CircleShape)
              } else {
                Modifier
              },
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = day.date.day.toString(),
          style = CaducityTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
          textAlign = TextAlign.Center,
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.height(6.dp)) {
        day.dots.forEach { status ->
          Box(Modifier.size(6.dp).clip(CircleShape).background(status.dotColor()))
        }
      }
    }
  }
}

@Composable
private fun ExpiryStatus.dotColor(): Color = colors().strong

@Composable
private fun dayDescription(day: WeekStripDay): String {
  val name = day.date.longName()
  return if (day.unitCount == 0) {
    stringResource(R.string.week_strip_day_none, name)
  } else {
    pluralStringResource(R.plurals.week_strip_day_count, day.unitCount, name, day.unitCount)
  }
}

/** Localized initial of the weekday: «dl», «dt»… */
private fun LocalDate.initial(locale: Locale = Locale.getDefault()): String =
  dayOfWeek
    .let { java.time.DayOfWeek.of(it.ordinal + 1) }
    .getDisplayName(TextStyle.SHORT, locale)
    .removeSuffix(".")
    .take(2)
    .lowercase(locale)

/** «Dimarts 13». */
private fun LocalDate.longName(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("EEEE d", locale).format(toJavaLocalDate()).replaceFirstChar { it.titlecase(locale) }

// region Previews

private val previewToday = LocalDate(2026, 10, 11)
private val previewData =
  WeekStripData(
    today = previewToday,
    statusesByDate =
      mapOf(
        previewToday to listOf(ExpiryStatus.SOON, ExpiryStatus.SOON),
        previewToday.plus(1, DateTimeUnit.DAY) to List(5) { ExpiryStatus.SOON },
        previewToday.plus(4, DateTimeUnit.DAY) to listOf(ExpiryStatus.FRESH),
      ),
  )

@PreviewLightDark
@Composable
private fun WeekStripPreview() {
  PreviewTheme {
    Box(Modifier.background(CaducityTheme.colorScheme.background).padding(20.dp)) {
      WeekStrip(data = previewData, selected = previewToday.plus(1, DateTimeUnit.DAY), onDayClick = {})
    }
  }
}

// endregion
