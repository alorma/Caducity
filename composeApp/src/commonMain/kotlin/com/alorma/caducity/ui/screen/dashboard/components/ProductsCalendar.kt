package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.ExpirationColors
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun ProductsCalendar(
  products: ImmutableList<ProductUiModel>,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
  val currentMonth = YearMonth(today.year, today.month)

  // Calculate start and end months
  val currentMonthNum = today.month.ordinal + 1 // Month ordinal is 0-based
  val startMonthNum = currentMonthNum - 1
  val startMonth = if (startMonthNum >= 1) {
    YearMonth(today.year, startMonthNum)
  } else {
    YearMonth(today.year - 1, 12)
  }

  val endMonthNum = currentMonthNum + 3
  val endMonth = if (endMonthNum <= 12) {
    YearMonth(today.year, endMonthNum)
  } else {
    YearMonth(today.year + 1, endMonthNum - 12)
  }
  val daysOfWeek = daysOfWeek()

  val calendarState = rememberCalendarState(
    startMonth = startMonth,
    endMonth = endMonth,
    firstVisibleMonth = currentMonth,
  )

  // Group products by expiration date with their most critical status
  val productsByDate = remember(products) {
    buildMap {
      products.forEach { product ->
        if (product is ProductUiModel.WithInstances) {
          product.instances.forEach { instance ->
            val date = instance.expirationDate
            val currentStatus = get(date)

            // Keep the most critical status (Expired > ExpiringSoon > Fresh)
            val newStatus = when {
              currentStatus == InstanceStatus.Expired -> InstanceStatus.Expired
              instance.status == InstanceStatus.Expired -> InstanceStatus.Expired
              currentStatus == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
              instance.status == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
              else -> InstanceStatus.Fresh
            }

            put(date, newStatus)
          }
        }
      }
    }
  }

  Column(modifier = modifier) {
    HorizontalCalendar(
      modifier = Modifier.fillMaxWidth(),
      state = calendarState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      monthHeader = { calendarMonth ->
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        ) {
          Text(
            text = "${calendarMonth.yearMonth.month.name} ${calendarMonth.yearMonth.year}",
            style = CaducityTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
          )

          // Day of week headers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            daysOfWeek.forEach { dayOfWeek ->
              Text(
                text = dayOfWeek.name.take(3),
                style = CaducityTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      },
      dayContent = { calendarDay ->
        val date = calendarDay.date
        val kotlinDate = LocalDate(date.year, date.month, date.day)
        val status = productsByDate[kotlinDate]

        // Check for consecutive days
        val prevDay = kotlinDate.plus(-1, DateTimeUnit.DAY)
        val nextDay = kotlinDate.plus(1, DateTimeUnit.DAY)
        val hasPrevDay = productsByDate.containsKey(prevDay)
        val hasNextDay = productsByDate.containsKey(nextDay)

        DayContent(
          date = date,
          status = status,
          hasPreviousDay = hasPrevDay,
          hasNextDay = hasNextDay,
          onClick = { onDateClick(it) },
        )
      },
    )
  }
}

@Composable
private fun DayContent(
  date: LocalDate,
  status: InstanceStatus?,
  hasPreviousDay: Boolean,
  hasNextDay: Boolean,
  onClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {

  val backgroundColor = if (status != null) {
    ExpirationColors.getSectionColors(status).container
  } else {
    Color.Transparent
  }

  val textColor = if (status != null) {
    ExpirationColors.getSectionColors(status).onContainer
  } else {
    CaducityTheme.colorScheme.onSurface
  }

  // Determine shape based on consecutive days (horizontal layout)
  val shape = when {
    status == null -> RoundedCornerShape(0.dp) // No shape if no status
    !hasPreviousDay && !hasNextDay -> CaducityTheme.shapes.small // Standalone day
    !hasPreviousDay && hasNextDay -> RoundedCornerShape(
      topStart = 8.dp,      // Top-left rounded
      topEnd = 0.dp,        // Top-right square
      bottomStart = 8.dp,   // Bottom-left rounded
      bottomEnd = 0.dp      // Bottom-right square
    ) // Start of sequence (left side rounded)
    hasPreviousDay && hasNextDay -> RoundedCornerShape(0.dp) // Middle of sequence
    hasPreviousDay && !hasNextDay -> RoundedCornerShape(
      topStart = 0.dp,      // Top-left square
      topEnd = 8.dp,        // Top-right rounded
      bottomStart = 0.dp,   // Bottom-left square
      bottomEnd = 8.dp      // Bottom-right rounded
    ) // End of sequence (right side rounded)
    else -> CaducityTheme.shapes.small
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(shape)
      .background(backgroundColor)
      .clickable { onClick(date) }
      .padding(4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = date.day.toString(),
      style = CaducityTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = textColor,
      fontWeight = if (status != null) FontWeight.Bold else FontWeight.Normal,
    )
  }
}

@Preview
@Composable
private fun ProductsCalendarPreview() {
  AppPreview {
    ProductsCalendar(
      products = listOf(productWithInstancesPreview).toImmutableList(),
      onDateClick = {},
    )
  }
}
