package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toCalendarShape
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.AppPreview
import com.kizitonwose.calendar.core.now
import kotlinx.datetime.LocalDate


@Composable
fun DayContent(
  today: LocalDate,
  date: LocalDate,
  status: InstanceStatus?,
  shapePosition: ShapePosition,
  isOutDay: Boolean,
  onClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {

  val backgroundColor = if (status != null) {
    val color = ExpirationDefaults.getColors(status).container

    if (isOutDay) {
      color.copy(alpha = CaducityTheme.dims.dim2)
    } else {
      color
    }
  } else {
    Color.Transparent
  }

  val textColor = if (status != null) {
    ExpirationDefaults.getColors(status).onContainer
  } else {
    CaducityTheme.colorScheme.onSurface
  }.let { color ->
    if (isOutDay) {
      color.copy(alpha = CaducityTheme.dims.dim2)
    } else {
      color
    }
  }

  Box(modifier = modifier) {
    if (today == date) {
      Box(
        modifier = Modifier
          .aspectRatio(1f)
          .padding(2.dp)
          .clip(shapePosition.toCalendarShape())
          .border(
            width = 2.dp,
            color = backgroundColor,
            shape = shapePosition.toCalendarShape(),
          )
          .clickable { onClick(date) }
          .padding(2.dp),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier = Modifier
            .aspectRatio(1f)
            .clip(shapePosition.toCalendarShape())
            .background(backgroundColor),
          contentAlignment = Alignment.Center,
        ) {
          DayText(
            dayText = date.day.toString(),
            textColor = textColor,
          )
        }
      }
    } else {
      Box(
        modifier = Modifier
          .aspectRatio(1f)
          .padding(2.dp)
          .clip(shapePosition.toCalendarShape())
          .background(backgroundColor)
          .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
      ) {
        DayText(
          dayText = date.day.toString(),
          textColor = textColor,
        )
      }
    }
  }
}

@Composable
private fun DayText(
  dayText: String,
  textColor: Color,
) {
  Text(
    text = dayText,
    style = CaducityTheme.typography.bodyMedium,
    textAlign = TextAlign.Center,
    color = textColor,
  )
}

@Preview
@Composable
private fun DayContentPreview() {
  AppPreview {
    DayContent(
      modifier = Modifier.size(56.dp),
      date = LocalDate.now(),
      today = LocalDate.now(),
      status = InstanceStatus.Expired,
      shapePosition = ShapePosition.Start,
      isOutDay = false,
      onClick = {},
    )
  }
}