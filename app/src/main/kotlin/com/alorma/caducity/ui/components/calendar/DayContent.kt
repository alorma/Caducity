package com.alorma.caducity.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

@Composable
fun DayContent(
  today: LocalDate,
  date: LocalDate,
  status: ItemStatus?,
  shapePosition: ShapePosition,
  isOutDay: Boolean,
  onClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  todayColor: Color = CaducityTheme.colorScheme.surfaceContainerHighest,
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

  Box(modifier = modifier.heightIn(max = 56.dp)) {
    val shape = shapePosition.toHorizontalShape()

    if (today == date) {
      val borderColor = if (status == null) {
        todayColor
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
        val internalShape = shapePosition.toHorizontalShape(
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

class DayContentPreviewContentProvider :
  CollectionPreviewParameterProvider<DayContentPreviewContent>(
    listOf(
      // Today - no status
      DayContentPreviewContent(
        date = LocalDate(2026, 2, 15),
        today = LocalDate(2026, 2, 15),
        status = null,
        shapePosition = ShapePosition.None,
        isOutDay = false,
      ),
      // Today - with Expired status (single item)
      DayContentPreviewContent(
        date = LocalDate(2026, 2, 15),
        today = LocalDate(2026, 2, 15),
        status = ItemStatus.Expired,
        shapePosition = ShapePosition.Single,
        isOutDay = false,
      ),
      // Today - with ExpiringSoon status (start of range)
      DayContentPreviewContent(
        date = LocalDate(2026, 2, 15),
        today = LocalDate(2026, 2, 15),
        status = ItemStatus.ExpiringSoon,
        shapePosition = ShapePosition.Start,
        isOutDay = false,
      ),
      // Regular day - Fresh status (middle of range)
      DayContentPreviewContent(
        date = LocalDate(2026, 2, 20),
        today = LocalDate(2026, 2, 15),
        status = ItemStatus.Fresh,
        shapePosition = ShapePosition.Middle,
        isOutDay = false,
      ),
      // Regular day - Frozen status (end of range)
      DayContentPreviewContent(
        date = LocalDate(2026, 2, 28),
        today = LocalDate(2026, 2, 15),
        status = ItemStatus.Frozen,
        shapePosition = ShapePosition.End,
        isOutDay = false,
      ),
      // Out of month day - with status (dimmed)
      DayContentPreviewContent(
        date = LocalDate(2026, 1, 31),
        today = LocalDate(2026, 2, 15),
        status = ItemStatus.Expired,
        shapePosition = ShapePosition.Single,
        isOutDay = true,
      ),
      // Out of month day - no status (dimmed)
      DayContentPreviewContent(
        date = LocalDate(2026, 3, 1),
        today = LocalDate(2026, 2, 15),
        status = null,
        shapePosition = ShapePosition.None,
        isOutDay = true,
      ),
    ),
  )

data class DayContentPreviewContent(
  val date: LocalDate,
  val today: LocalDate,
  val status: ItemStatus?,
  val shapePosition: ShapePosition,
  val isOutDay: Boolean,
)

@PreviewDynamicLightDark
@Composable
fun DayContentPreview(
  @PreviewParameter(provider = DayContentPreviewContentProvider::class) content: DayContentPreviewContent,
) {
  PreviewTheme {
    Surface {
      DayContent(
        modifier = Modifier.size(56.dp),
        date = content.date,
        today = content.today,
        status = content.status,
        shapePosition = content.shapePosition,
        isOutDay = content.isOutDay,
        onClick = {},
      )
    }
  }
}
