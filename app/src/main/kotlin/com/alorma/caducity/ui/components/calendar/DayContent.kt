package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toCalendarShape
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusDays
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
    val color = ExpirationDefaults.getVibrantColors(status).container

    if (isOutDay) {
      color.copy(alpha = CaducityTheme.dims.dim2)
    } else {
      color
    }
  } else {
    Color.Transparent
  }

  val textColor = if (status != null) {
    ExpirationDefaults.getVibrantColors(status).onContainer
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
    val shape = shapePosition.toCalendarShape()

    if (today == date) {
      val borderColor = if (status == null) {
        CaducityTheme.colorScheme.surfaceContainerHighest
      } else {
        backgroundColor
      }
      Box(
        modifier = Modifier
          .aspectRatio(1f)
          .padding(2.dp)
          .clip(shape)
          .border(
            width = 2.dp,
            color = borderColor,
            shape = shape,
          )
          .clickable { onClick(date) }
          .padding(4.dp),
        contentAlignment = Alignment.Center,
      ) {
        val internalShape = shapePosition.toCalendarShape(
          externalBaseShape = CaducityTheme.shapes.medium,
          internalBaseShape = RoundedCornerShape(2.dp),
        )
        Box(
          modifier = Modifier
            .aspectRatio(1f)
            .background(
              color = backgroundColor,
              shape = internalShape,
            ),
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
          .clip(shape)
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

@PreviewDynamicLightDark
@Composable
private fun DayTodayWithStatusPreview() {
  PreviewTheme {
    DayTodayWithStatusPreviewContent()
  }
}

@Composable
fun DayTodayWithStatusScreenshot() {
  PreviewTheme {
    DayTodayWithStatusPreviewContent()
  }
}

@Composable
fun DayTodayWithStatusPreviewContent() {
  Surface {
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

@PreviewDynamicLightDark
@Composable
private fun DayTodayWithoutStatusPreview() {
  PreviewTheme {
    DayTodayWithoutStatusPreviewContent()
  }
}

@Composable
fun DayTodayWithoutStatusScreenshot() {
  PreviewTheme {
    DayTodayWithoutStatusPreviewContent()
  }
}

@Composable
fun DayTodayWithoutStatusPreviewContent() {
  Surface {
    DayContent(
      modifier = Modifier.size(56.dp),
      date = LocalDate.now(),
      today = LocalDate.now(),
      status = null,
      shapePosition = ShapePosition.Start,
      isOutDay = false,
      onClick = {},
    )
  }
}

@PreviewDynamicLightDark
@Composable
private fun DayWithStatusPreview() {
  PreviewTheme {
    DayWithStatusPreviewContent()
  }
}

@Composable
fun DayWithStatusScreenshot() {
  PreviewTheme {
    DayWithStatusPreviewContent()
  }
}

@Composable
fun DayWithStatusPreviewContent() {
  Surface {
    DayContent(
      modifier = Modifier.size(56.dp),
      date = LocalDate.now().plusDays(3),
      today = LocalDate.now(),
      status = InstanceStatus.Expired,
      shapePosition = ShapePosition.Start,
      isOutDay = false,
      onClick = {},
    )
  }
}

@PreviewDynamicLightDark
@Composable
private fun DayWithoutStatusPreview() {
  PreviewTheme {
    DayWithoutStatusPreviewContent()
  }
}

@Composable
fun DayWithoutStatusScreenshot() {
  PreviewTheme {
    DayWithoutStatusPreviewContent()
  }
}

@Composable
fun DayWithoutStatusPreviewContent() {
  Surface {
    DayContent(
      modifier = Modifier.size(56.dp),
      date = LocalDate.now().plusDays(3),
      today = LocalDate.now(),
      status = null,
      shapePosition = ShapePosition.Start,
      isOutDay = false,
      onClick = {},
    )
  }
}