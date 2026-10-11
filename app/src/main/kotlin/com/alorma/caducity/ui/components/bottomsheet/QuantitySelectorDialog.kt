package com.alorma.caducity.ui.components.bottomsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.toComposeShape
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.motion.rememberReducedMotion
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Result returned from the quantity dialog. */
sealed interface QuantitySelectorResult {
  data object Dismissed : QuantitySelectorResult

  data class Selected(
    val quantity: Int,
  ) : QuantitySelectorResult
}

/** What the quantity dialog shows about the legacy pack being consumed. */
data class QuantityDialogData(
  val name: String,
  val shape: CategoryShape,
  val icon: ImageVector,
  /** Relative expiry, already localized and lowercase (e.g. «caduca demà»). */
  val expires: String,
  /** Units left in the pack; the selector goes from 1 to this value. */
  val max: Int,
)

/** Selector bounds: `−` is disabled at 1, `+` and the «Tots» chip's selected state depend on [max]. */
internal data class QuantityState(
  val quantity: Int,
  val max: Int,
) {
  val canDecrease: Boolean get() = quantity > 1
  val canIncrease: Boolean get() = quantity < max
  val allSelected: Boolean get() = quantity == max
  val showPips: Boolean get() = max <= MaxPips

  fun decrease(): QuantityState = if (canDecrease) copy(quantity = quantity - 1) else this

  fun increase(): QuantityState = if (canIncrease) copy(quantity = quantity + 1) else this

  fun all(): QuantityState = copy(quantity = max)

  companion object {
    const val MaxPips = 8
  }
}

/** Asks how many units of a legacy pack to consume. */
suspend fun AppDialogState.showQuantitySelectorDialog(data: QuantityDialogData): QuantitySelectorResult {
  var confirmed = 1
  val result =
    showCustomDialog { dismiss ->
      QuantityDialog(
        data = data,
        onConfirm = {
          confirmed = it
          dismiss(DialogResult.Positive)
        },
        onCancel = { dismiss(DialogResult.Negative) },
        onDismiss = { dismiss(DialogResult.Dismissed) },
      )
    }
  return if (result == DialogResult.Positive) {
    QuantitySelectorResult.Selected(confirmed)
  } else {
    QuantitySelectorResult.Dismissed
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuantityDialog(
  data: QuantityDialogData,
  onConfirm: (Int) -> Unit,
  onCancel: () -> Unit,
  onDismiss: () -> Unit,
) {
  var quantity by remember { mutableIntStateOf(1) }
  BasicAlertDialog(onDismissRequest = onDismiss) {
    QuantityDialogContent(
      data = data,
      state = QuantityState(quantity, data.max),
      onStateChange = { quantity = it.quantity },
      onConfirm = { onConfirm(quantity) },
      onCancel = onCancel,
    )
  }
}

@Composable
internal fun QuantityDialogContent(
  data: QuantityDialogData,
  state: QuantityState,
  onStateChange: (QuantityState) -> Unit,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scheme = CaducityTheme.colorScheme
  val container = if (scheme.surface.luminance() < 0.5f) scheme.surfaceContainer else scheme.surface
  Surface(
    modifier = modifier.widthIn(max = 326.dp).fillMaxWidth(),
    shape = RoundedCornerShape(32.dp),
    color = container,
    shadowElevation = 6.dp,
  ) {
    Column(
      modifier = Modifier.padding(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      QuantityHeader(data)
      Text(
        text = stringResource(R.string.quantity_question),
        style = CaducityTheme.typography.bodyLarge,
        color = scheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 0.dp),
      )
      QuantityStepper(state = state, onStateChange = onStateChange)
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (state.showPips) {
          Pips(quantity = state.quantity, max = state.max)
        } else {
          Spacer(Modifier.width(1.dp))
        }
        AllChip(
          max = state.max,
          selected = state.allSelected,
          onClick = { onStateChange(state.all()) },
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(onClick = onCancel) { Text(stringResource(R.string.item_actions_cancel)) }
        Button(
          onClick = onConfirm,
          modifier = Modifier.height(48.dp).animateContentSize(),
          shape = RoundedCornerShape(24.dp),
        ) {
          Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(8.dp))
          Text(
            text =
              if (state.allSelected) {
                stringResource(R.string.quantity_confirm_all, state.quantity)
              } else {
                stringResource(R.string.quantity_confirm, state.quantity)
              },
          )
        }
      }
    }
  }
}

@Composable
private fun QuantityHeader(data: QuantityDialogData) {
  Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
    CategoryShapeIcon(shape = data.shape, icon = data.icon, size = 48.dp)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.quantity_overline, data.max, data.expires).uppercase(),
        style = CaducityTheme.typography.labelMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = data.name,
        style = CaducityTheme.typography.headlineSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.semantics { heading() },
      )
    }
  }
}

@Composable
private fun QuantityStepper(
  state: QuantityState,
  onStateChange: (QuantityState) -> Unit,
) {
  val reducedMotion = rememberReducedMotion()
  val scheme = CaducityTheme.colorScheme
  val ofText = stringResource(R.string.quantity_of, state.max)
  val centerDescription = "${state.quantity} $ofText"
  Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth().height(64.dp)) {
    StepButton(
      plus = false,
      description = stringResource(R.string.quantity_less),
      position = ShapePosition.Start,
      enabled = state.canDecrease,
      onStep = { onStateChange(state.decrease()) },
    )
    Box(
      modifier =
        Modifier
          .weight(1f)
          .height(64.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(if (scheme.surface.luminance() < 0.5f) scheme.surfaceContainerHigh else scheme.surfaceContainer)
          .clearAndSetSemantics {
            contentDescription = centerDescription
            liveRegion = LiveRegionMode.Polite
          },
      contentAlignment = Alignment.Center,
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        AnimatedContent(
          targetState = state.quantity,
          transitionSpec = {
            if (reducedMotion) {
              ContentTransform(EnterTransition.None, ExitTransition.None)
            } else if (targetState > initialState) {
              (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            } else {
              (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
            }
          },
          label = "quantity",
        ) { value ->
          Text(
            text = value.toString(),
            style = CaducityTheme.typography.headlineLarge,
            color = scheme.onSurface,
          )
        }
        Text(
          text = ofText,
          style = CaducityTheme.typography.titleSmall,
          color = scheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 5.dp),
        )
      }
    }
    StepButton(
      plus = true,
      description = stringResource(R.string.quantity_more),
      position = ShapePosition.End,
      enabled = state.canIncrease,
      onStep = { onStateChange(state.increase()) },
    )
  }
}

/** 64 dp connected button; holding it repeats the step. */
@Composable
private fun StepButton(
  plus: Boolean,
  description: String,
  position: ShapePosition,
  enabled: Boolean,
  onStep: () -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  val shape: Shape = position.toHorizontalShape(RoundedCornerShape(24.dp), RoundedCornerShape(6.dp))
  val currentStep by rememberUpdatedState(onStep)
  val currentEnabled by rememberUpdatedState(enabled)
  val scope = rememberCoroutineScope()
  Box(
    modifier =
      Modifier
        .size(64.dp)
        .clip(shape)
        .background(if (enabled) scheme.primaryContainer else scheme.surfaceContainerHigh)
        .semantics {
          role = Role.Button
          contentDescription = description
          if (!enabled) disabled()
          onClick {
            if (enabled) currentStep()
            enabled
          }
        }.pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              if (!currentEnabled) return@detectTapGestures
              currentStep()
              val repeat =
                scope.launch {
                  delay(RepeatDelayMs)
                  while (currentEnabled) {
                    currentStep()
                    delay(RepeatIntervalMs)
                  }
                }
              try {
                tryAwaitRelease()
              } finally {
                repeat.cancel()
              }
            },
          )
        },
    contentAlignment = Alignment.Center,
  ) {
    val color = if (enabled) scheme.onPrimaryContainer else scheme.outline.copy(alpha = 0.5f)
    Canvas(Modifier.size(28.dp).clearAndSetSemantics {}) {
      val stroke = 3.dp.toPx()
      val inset = 5.dp.toPx()
      drawLine(
        color,
        Offset(inset, size.height / 2),
        Offset(size.width - inset, size.height / 2),
        stroke,
        StrokeCap.Round,
      )
      if (plus) {
        drawLine(
          color,
          Offset(size.width / 2, inset),
          Offset(size.width / 2, size.height - inset),
          stroke,
          StrokeCap.Round,
        )
      }
    }
  }
}

/** One flower per unit; filled while consumed. Decorative: the stepper already announces «2 de 4». */
@Composable
private fun Pips(
  quantity: Int,
  max: Int,
) {
  val scheme = CaducityTheme.colorScheme
  val shape = CategoryShape.CLOVER8.toComposeShape()
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clearAndSetSemantics {}) {
    repeat(max) { index ->
      val filled = index < quantity
      val scale by animateFloatAsState(if (filled) 1f else 0.8f, label = "pip")
      Box(
        modifier =
          Modifier
            .size(22.dp)
            .scale(scale)
            .clip(shape)
            .then(
              if (filled) {
                Modifier.background(scheme.primary)
              } else {
                Modifier.border(BorderStroke(1.5.dp, scheme.outline), shape)
              },
            ),
      )
    }
  }
}

@Composable
private fun AllChip(
  max: Int,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val scheme = CaducityTheme.colorScheme
  Surface(
    onClick = { if (!selected) onClick() },
    shape = RoundedCornerShape(12.dp),
    color = if (selected) scheme.primaryContainer else scheme.surface.copy(alpha = 0f),
    contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
    border = if (selected) null else BorderStroke(1.dp, scheme.outline),
    modifier =
      Modifier.height(36.dp).semantics {
        role = Role.Checkbox
        this.selected = selected
      },
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (selected) {
        Icon(Symbols.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
      }
      Text(stringResource(R.string.quantity_all, max), style = CaducityTheme.typography.labelLarge)
    }
  }
}

private const val RepeatDelayMs = 400L
private const val RepeatIntervalMs = 100L
