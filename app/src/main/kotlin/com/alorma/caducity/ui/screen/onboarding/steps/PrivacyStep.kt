package com.alorma.caducity.ui.screen.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CloudUpload
import com.alorma.caducity.base.ui.icons.symbols.Smartphone
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.components.switch.CaducitySwitch
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingBackAndPrimary
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingStepLayout
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun PrivacyStep(
  stepNumber: Int?,
  analyticsEnabled: Boolean,
  onAnalyticsChange: (Boolean) -> Unit,
  onBack: () -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
  stepCount: Int = 4,
) {
  OnboardingStepLayout(
    modifier = modifier,
    stepNumber = stepNumber,
    stepCount = stepCount,
    title = stringResource(R.string.ob_privacy_title),
    body = stringResource(R.string.ob_privacy_body),
    actions = {
      OnboardingBackAndPrimary(
        primaryText = stringResource(R.string.ob_continue),
        onPrimary = onContinue,
        onBack = onBack,
      )
    },
  ) {
    Reveal(delayMillis = 240, modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
        InfoRow(0, Symbols.Outlined.Smartphone, stringResource(R.string.ob_privacy_local))
        InfoRow(1, Symbols.Outlined.CloudUpload, stringResource(R.string.ob_privacy_backup))
      }
    }
    Spacer(Modifier.size(24.dp))
    Reveal(delayMillis = 360, modifier = Modifier.fillMaxWidth()) {
      Column {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = CaducityTheme.colorScheme.surfaceContainer,
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(24.dp))
              .toggleable(value = analyticsEnabled, role = Role.Switch, onValueChange = onAnalyticsChange),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text(
                text = stringResource(R.string.ob_privacy_analytics),
                style = CaducityTheme.typography.bodyLarge,
                color = CaducityTheme.colorScheme.onSurface,
              )
              Text(
                text = stringResource(R.string.ob_privacy_analytics_sub),
                style = CaducityTheme.typography.bodySmall,
                color = CaducityTheme.colorScheme.onSurfaceVariant,
              )
            }
            CaducitySwitch(checked = analyticsEnabled, onCheckedChange = null)
          }
        }
        Text(
          text = stringResource(R.string.ob_privacy_default_off),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 8.dp, top = 16.dp, end = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun InfoRow(
  index: Int,
  icon: ImageVector,
  text: String,
) {
  ConnectedRow(index = index, count = 2, contentPadding = PaddingValues(16.dp)) {
    Icon(icon, contentDescription = null, tint = CaducityTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
    Text(
      text = text,
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 16.dp),
    )
  }
}

@PreviewLightDark
@Composable
private fun PrivacyStepPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.background) {
      PrivacyStep(stepNumber = 3, analyticsEnabled = false, onAnalyticsChange = {}, onBack = {}, onContinue = {})
    }
  }
}
