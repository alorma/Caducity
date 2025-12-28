package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun ProductsCalendar(
  products: ImmutableList<ProductUiModel>,
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
        val kotlinDate: LocalDate = LocalDate(date.year, date.month, date.day)
        val status = productsByDate[kotlinDate]

        DayContent(
          day = date.day.toString(),
          status = status,
        )
      },
    )
  }
}

@Composable
private fun DayContent(
  day: String,
  status: InstanceStatus?,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .aspectRatio(1f)
      .padding(4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = day,
        style = CaducityTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
      )

      if (status != null) {
        Box(
          modifier = Modifier
            .padding(top = 2.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(getStatusColor(status)),
        )
      }
    }
  }
}

@Composable
private fun getStatusColor(status: InstanceStatus): Color {
  return when (status) {
    InstanceStatus.Expired -> CaducityTheme.colorScheme.error
    InstanceStatus.ExpiringSoon -> CaducityTheme.colorScheme.tertiary
    InstanceStatus.Fresh -> CaducityTheme.colorScheme.primary
  }
}

@Preview
@Composable
private fun ProductsCalendarPreview() {
  AppPreview {
    ProductsCalendar(
      products = listOf(productWithInstancesPreview).toImmutableList(),
    )
  }
}
