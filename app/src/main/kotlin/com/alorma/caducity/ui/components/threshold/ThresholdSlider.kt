package com.alorma.caducity.ui.components.threshold

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ThresholdStops
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlin.math.roundToInt

private val TrackHeight = 16.dp
private val TrackOverhang = 8.dp
private val ThumbWidth = 4.dp
private val ThumbHeight = 44.dp
private val ThumbGap = 6.dp
private val DotSize = 5.dp
private val LabelWidth = 24.dp
private val BracketHeight = 9.dp
private val InnerCorner = 4.dp

private val StopNumbers = listOf(1, 2, 3, 1, 2, 1, 2, 3)

// Stop ranges that share a unit: days, weeks and months.
private val UnitGroups =
  listOf(
    0..2 to R.string.threshold_unit_days,
    3..4 to R.string.threshold_unit_weeks,
    5..7 to R.string.threshold_unit_months,
  )

/** «1 dia», «3 dies», «1 setmana», «2 mesos»: the margin written the way the stops are labelled. */
@Composable
fun thresholdLabel(days: Int): String =
  when {
    days >= 30 && days % 30 == 0 ->
      pluralStringResource(R.plurals.threshold_months, days / 30, days / 30)
    days >= 7 && days % 7 == 0 ->
      pluralStringResource(R.plurals.threshold_weeks, days / 7, days / 7)
    else -> pluralStringResource(R.plurals.threshold_days, days, days)
  }

/**
 * Discrete slider over [ThresholdStops]. A value that is not a stop is shown on the nearest stop and
 * kept until the user moves the handle.
 */
@Composable
fun ThresholdSlider(
  days: Int,
  onDaysChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptics = LocalHapticFeedback.current
  val currentIndex = ThresholdStops.nearestIndex(days)
  val label = thresholdLabel(days)

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = label,
      style = CaducityTheme.typography.headlineMedium.copy(fontSize = 28.sp, lineHeight = 32.sp),
      fontWeight = FontWeight.ExtraBold,
      color = CaducityTheme.colorScheme.onSurface,
    )

    val sliderState =
      rememberSliderState(
        value = currentIndex.toFloat(),
        steps = ThresholdStops.lastIndex - 1,
        trackRange = 0f..ThresholdStops.lastIndex.toFloat(),
      )
    LaunchedEffect(currentIndex) { sliderState.value = currentIndex.toFloat() }
    Slider(
      state = sliderState,
      onValueChange = { raw: Float ->
        sliderState.value = raw
        val index = raw.roundToInt().coerceIn(0, ThresholdStops.lastIndex)
        if (index != currentIndex) {
          haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
          onDaysChange(ThresholdStops.daysAt(index))
        }
      },
      thumb = { Thumb() },
      track = { state -> Track(state) },
      modifier =
        Modifier
          .padding(horizontal = (LabelWidth - ThumbWidth) / 2)
          .height(ThumbHeight)
          .semantics { stateDescription = label },
    )

    StopLabels(selectedIndex = currentIndex)
  }
}

@Composable
private fun Thumb() {
  Box(
    modifier =
      Modifier
        .size(width = ThumbWidth, height = ThumbHeight)
        .clip(RoundedCornerShape(ThumbWidth / 2))
        .background(CaducityTheme.colorScheme.primary),
  )
}

@Composable
private fun Track(state: SliderState) {
  val active = CaducityTheme.colorScheme.primary
  val inactive = CaducityTheme.colorScheme.primaryContainer
  val stops = ThresholdStops.lastIndex
  Canvas(Modifier.fillMaxWidth().height(TrackHeight)) {
    val thumbHalf = ThumbWidth.toPx() / 2
    val inner = size.width - ThumbWidth.toPx()
    val overhang = TrackOverhang.toPx()
    val thumbCenter = thumbHalf + inner * (state.value / stops).coerceIn(0f, 1f)
    val gap = ThumbGap.toPx() + thumbHalf
    val full = size.height / 2
    val small = InnerCorner.toPx()

    val trackStart = -overhang
    val trackEnd = size.width + overhang
    val activeEnd = thumbCenter - gap
    val inactiveStart = thumbCenter + gap
    if (activeEnd > trackStart) {
      drawSegment(active, trackStart, activeEnd, leftRadius = full, rightRadius = small)
    }
    if (inactiveStart < trackEnd) {
      drawSegment(inactive, inactiveStart, trackEnd, leftRadius = small, rightRadius = full)
    }

    for (i in 0..stops) {
      val x = thumbHalf + inner * (i.toFloat() / stops)
      val onActive = x < thumbCenter - gap
      val onInactive = x > thumbCenter + gap
      if (onActive || onInactive) {
        drawCircle(
          color = if (onActive) inactive else active,
          radius = DotSize.toPx() / 2,
          center = Offset(x, size.height / 2),
        )
      }
    }
  }
}

private fun DrawScope.drawSegment(
  color: Color,
  left: Float,
  right: Float,
  leftRadius: Float,
  rightRadius: Float,
) {
  val path =
    Path().apply {
      addRoundRect(
        RoundRect(
          rect = Rect(left, 0f, right, size.height),
          topLeft = CornerRadius(leftRadius),
          bottomLeft = CornerRadius(leftRadius),
          topRight = CornerRadius(rightRadius),
          bottomRight = CornerRadius(rightRadius),
        ),
      )
    }
  drawPath(path, color)
}

/** Numbers under each stop, a bracket under each unit group and the unit name below it. */
@Composable
private fun StopLabels(selectedIndex: Int) {
  val half = LabelWidth / 2
  val bracketColor = CaducityTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
  BoxWithConstraints(modifier = Modifier.fillMaxWidth().clearAndSetSemantics { }) {
    fun centerOf(index: Int): Dp = half + (maxWidth - LabelWidth) * (index.toFloat() / ThresholdStops.lastIndex)

    Column {
      Box(Modifier.fillMaxWidth().height(24.dp)) {
        StopNumbers.forEachIndexed { index, number ->
          val selected = index == selectedIndex
          Text(
            text = number.toString(),
            style = CaducityTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color =
              if (selected) CaducityTheme.colorScheme.onSurface else CaducityTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = centerOf(index) - half).width(LabelWidth),
          )
        }
      }
      Canvas(Modifier.fillMaxWidth().height(BracketHeight)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val reach = 9.dp.toPx()
        val radius = 4.dp.toPx()
        UnitGroups.forEach { (range, _) ->
          val left = centerOf(range.first).toPx() - reach
          val right = centerOf(range.last).toPx() + reach
          val path =
            Path().apply {
              moveTo(left, 0f)
              lineTo(left, size.height - radius)
              quadraticTo(left, size.height, left + radius, size.height)
              lineTo(right - radius, size.height)
              quadraticTo(right, size.height, right, size.height - radius)
              lineTo(right, 0f)
            }
          drawPath(path, bracketColor, style = stroke)
        }
      }
      Box(Modifier.fillMaxWidth().height(22.dp).padding(top = 4.dp)) {
        UnitGroups.forEach { (range, nameRes) ->
          val left = centerOf(range.first) - 9.dp
          val width = centerOf(range.last) - centerOf(range.first) + 18.dp
          Text(
            text = stringResource(nameRes),
            style = CaducityTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.offset(x = left).width(width),
          )
        }
      }
    }
  }
}
