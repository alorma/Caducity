package com.alorma.caducity.ui.screen.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Warning
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingBackAndPrimary
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingStepLayout
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun DisclaimerStep(
  stepNumber: Int?,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  onBack: (() -> Unit)?,
  onAccept: () -> Unit,
  modifier: Modifier = Modifier,
  accepting: Boolean = false,
  stepCount: Int = 4,
) {
  val uriHandler = LocalUriHandler.current
  val foodSafetyUrl = stringResource(R.string.url_food_safety)
  val soon = CaducityTheme.colors.soon
  OnboardingStepLayout(
    modifier = modifier,
    stepNumber = stepNumber,
    stepCount = stepCount,
    title = stringResource(R.string.ob_disclaimer_title),
    actions = {
      OnboardingBackAndPrimary(
        primaryText = stringResource(R.string.ob_accept_and_start),
        onPrimary = onAccept,
        onBack = onBack,
        primaryEnabled = checked && !accepting,
        primaryLeadingIcon = Symbols.Outlined.Check,
      )
    },
  ) {
    Reveal(delayMillis = 240, modifier = Modifier.fillMaxWidth()) {
      Surface(shape = RoundedCornerShape(24.dp), color = soon.container, contentColor = soon.onContainer) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Symbols.Filled.Warning, contentDescription = null, modifier = Modifier.size(28.dp))
            Text(
              text = stringResource(R.string.ob_disclaimer_heading),
              style = CaducityTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 12.dp),
            )
          }
          Text(
            text = AnnotatedString.fromHtml(stringResource(R.string.ob_disclaimer_p1)),
            style = CaducityTheme.typography.bodyMedium,
          )
          Text(text = stringResource(R.string.ob_disclaimer_p2), style = CaducityTheme.typography.bodyMedium)
          Text(text = stringResource(R.string.ob_disclaimer_p3), style = CaducityTheme.typography.bodyMedium)
          Text(
            text = stringResource(R.string.ob_disclaimer_link),
            style = CaducityTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
            modifier =
              Modifier
                .clickable(role = Role.Button) { uriHandler.openUri(foodSafetyUrl) }
                .padding(vertical = 8.dp),
          )
        }
      }
    }
    Spacer(Modifier.size(16.dp))
    Reveal(delayMillis = 480, modifier = Modifier.fillMaxWidth()) {
      Column {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = CaducityTheme.colorScheme.surfaceContainer,
          modifier =
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).toggleable(
              value = checked,
              role = Role.Checkbox,
              onValueChange = onCheckedChange,
            ),
        ) {
          Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Checkbox(checked = checked, onCheckedChange = null)
            Text(
              text = stringResource(R.string.ob_disclaimer_check),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurface,
              modifier = Modifier.padding(start = 16.dp, top = 2.dp),
            )
          }
        }
        if (!checked) {
          Text(
            text = stringResource(R.string.ob_disclaimer_required),
            style = CaducityTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 12.dp),
          )
        }
      }
    }
  }
}

@PreviewLightDark
@Composable
private fun DisclaimerStepPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.background) {
      DisclaimerStep(stepNumber = 4, checked = false, onCheckedChange = {}, onBack = {}, onAccept = {})
    }
  }
}
