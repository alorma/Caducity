package com.alorma.caducity.ui.screen.onboarding.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.components.motion.rememberReducedMotion
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.OverlineTextStyle

/**
 * Common frame of the numbered steps: «Pas N de [stepCount]» + segmented progress, title and text on top, the step
 * content in the middle (scrolls) and the actions pinned to the bottom. [stepNumber] is null for the legal notice
 * shown alone.
 */
@Composable
fun OnboardingStepLayout(
  stepNumber: Int?,
  title: String,
  modifier: Modifier = Modifier,
  stepCount: Int = 4,
  body: String? = null,
  actions: @Composable ColumnScope.() -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
  ) {
    Column(
      modifier =
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.height(24.dp))
      if (stepNumber != null) {
        Text(
          text = stringResource(R.string.ob_step, stepNumber, stepCount).uppercase(),
          style = OverlineTextStyle,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        OnboardingProgressBar(stepNumber = stepNumber, stepCount = stepCount)
        Spacer(Modifier.height(28.dp))
      }
      Reveal(delayMillis = 120, durationMillis = 520) {
        Text(
          text = title,
          style = CaducityTheme.typography.headlineMedium,
          color = CaducityTheme.colorScheme.onSurface,
        )
      }
      if (body != null) {
        Spacer(Modifier.height(12.dp))
        Reveal(delayMillis = 200, durationMillis = 520) {
          Text(
            text = body,
            style = CaducityTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Spacer(Modifier.height(24.dp))
      content()
      Spacer(Modifier.height(16.dp))
    }
    Column(
      modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      content = actions,
    )
  }
}

/** [stepCount] segments of 6 dp: done ones `primary`, pending `outline-variant`; the current one fills from the left. */
@Composable
fun OnboardingProgressBar(
  stepNumber: Int,
  modifier: Modifier = Modifier,
  stepCount: Int = 4,
) {
  val reduced = rememberReducedMotion()
  val current = remember(stepNumber) { Animatable(if (reduced) 1f else 0f) }
  LaunchedEffect(stepNumber, reduced) {
    if (reduced) current.snapTo(1f) else current.animateTo(1f, tween(600, delayMillis = 100))
  }
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .semantics {
          progressBarRangeInfo =
            ProgressBarRangeInfo(
              stepNumber.toFloat(),
              0f..stepCount.toFloat(),
              stepCount - 1,
            )
        },
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    repeat(stepCount) { index ->
      val fill =
        when {
          index + 1 < stepNumber -> 1f
          index + 1 == stepNumber -> current.value
          else -> 0f
        }
      Box(
        modifier =
          Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CaducityTheme.colorScheme.outlineVariant),
      ) {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .graphicsLayer {
                scaleX = fill
                transformOrigin = TransformOrigin(0f, 0.5f)
              }.background(CaducityTheme.colorScheme.primary),
        )
      }
    }
  }
}

/** 56 dp pill button used as the main action of every step. */
@Composable
fun OnboardingPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: ImageVector? = null,
  trailingIcon: ImageVector? = null,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(56.dp).widthIn(min = 160.dp),
    shape = CircleShape,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = CaducityTheme.colorScheme.primary,
        contentColor = CaducityTheme.colorScheme.onPrimary,
        disabledContainerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        disabledContentColor = CaducityTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
      ),
  ) {
    if (leadingIcon != null) {
      Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(22.dp))
      Spacer(Modifier.size(10.dp))
    }
    Text(text, style = CaducityTheme.typography.titleSmall.copy(textAlign = TextAlign.Center))
    if (trailingIcon != null) {
      Spacer(Modifier.size(10.dp))
      Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(22.dp))
    }
  }
}

/** «Enrere» on the left and the main action on the right. */
@Composable
fun OnboardingBackAndPrimary(
  primaryText: String,
  onPrimary: () -> Unit,
  modifier: Modifier = Modifier,
  onBack: (() -> Unit)? = null,
  primaryEnabled: Boolean = true,
  primaryLeadingIcon: ImageVector? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    if (onBack != null) OnboardingBackButton(onBack) else Spacer(Modifier.size(1.dp))
    OnboardingPrimaryButton(
      text = primaryText,
      onClick = onPrimary,
      enabled = primaryEnabled,
      leadingIcon = primaryLeadingIcon,
    )
  }
}

@Composable
fun OnboardingBackButton(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TextButton(onClick = onBack, modifier = modifier.height(48.dp)) {
    Text(
      text = stringResource(R.string.ob_back),
      style = CaducityTheme.typography.titleSmall,
      color = CaducityTheme.colorScheme.primary,
    )
  }
}
