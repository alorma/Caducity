package com.alorma.caducity.ui.components.actions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.launch

/** How long «Consumit igualment» has to be held (spec §4). */
const val HOLD_TO_CONFIRM_MILLIS = 1_500

/**
 * A row that confirms only when held for [holdMillis]: it fills from left to right while pressed and empties
 * if let go before. With TalkBack, [onRequestConfirm] is offered as a custom action (the caller asks with a dialog).
 */
@Composable
fun HoldToConfirmRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  accessibilityLabel: String,
  onConfirmed: () -> Unit,
  onRequestConfirm: () -> Unit,
  shape: Shape,
  modifier: Modifier = Modifier,
  holdMillis: Int = HOLD_TO_CONFIRM_MILLIS,
  fillColor: Color = CaducityTheme.colors.expired.container,
  contentColor: Color = CaducityTheme.colors.expired.onContainer,
) {
  val progress = remember { Animatable(0f) }
  val scope = rememberCoroutineScope()

  Surface(
    modifier =
      modifier
        .semantics {
          customActions =
            listOf(
              CustomAccessibilityAction(accessibilityLabel) {
                onRequestConfirm()
                true
              },
            )
        }.pointerInput(holdMillis) {
          detectTapGestures(
            onPress = {
              val hold =
                scope.launch {
                  progress.animateTo(1f, tween(holdMillis, easing = LinearEasing))
                  onConfirmed()
                }
              tryAwaitRelease()
              hold.cancel()
              scope.launch { progress.animateTo(0f, tween(200)) }
            },
          )
        },
    shape = shape,
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .drawBehind { drawRect(fillColor, size = Size(size.width * progress.value, size.height)) }
          .heightIn(min = 56.dp)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(icon, contentDescription = null, tint = CaducityTheme.colorScheme.onSurfaceVariant)
      Spacer(Modifier.width(16.dp))
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = CaducityTheme.typography.bodyLarge, color = CaducityTheme.colorScheme.onSurface)
        Text(subtitle, style = CaducityTheme.typography.bodySmall, color = contentColor)
      }
    }
  }
}
