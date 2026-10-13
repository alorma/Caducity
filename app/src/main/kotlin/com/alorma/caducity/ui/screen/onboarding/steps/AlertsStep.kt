package com.alorma.caducity.ui.screen.onboarding.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CheckCircle
import com.alorma.caducity.base.ui.icons.symbols.NotificationsActive
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.button.ConnectedButtonGroup
import com.alorma.caducity.ui.components.button.ConnectedButtonOption
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingBackAndPrimary
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingBackButton
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingPrimaryButton
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingStepLayout
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalTime

private val THRESHOLD_OPTIONS = listOf(1, 3, 7)

@Composable
fun AlertsStep(
  stepNumber: Int,
  thresholdDays: Int,
  time: LocalTime,
  permissionGranted: Boolean,
  onThresholdChange: (Int) -> Unit,
  onTimeClick: () -> Unit,
  onEnable: () -> Unit,
  onNotNow: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  stepCount: Int = 4,
) {
  OnboardingStepLayout(
    modifier = modifier,
    stepNumber = stepNumber,
    stepCount = stepCount,
    title = stringResource(R.string.ob_alerts_title),
    body = stringResource(R.string.ob_alerts_body),
    actions = {
      if (permissionGranted) {
        OnboardingBackAndPrimary(
          primaryText = stringResource(R.string.ob_continue),
          onPrimary = onEnable,
          onBack = onBack,
        )
      } else {
        OnboardingPrimaryButton(
          text = stringResource(R.string.ob_alerts_enable),
          onClick = onEnable,
          modifier = Modifier.fillMaxWidth(),
          leadingIcon = Symbols.Outlined.NotificationsActive,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OnboardingBackButton(onBack)
          TextButton(onClick = onNotNow) {
            Text(
              text = stringResource(R.string.ob_not_now),
              style = CaducityTheme.typography.titleSmall,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
  ) {
    Reveal(delayMillis = 350, offsetY = (-140).dp, bouncy = true, modifier = Modifier.fillMaxWidth()) {
      SampleNotification(time = time)
    }
    Spacer(Modifier.size(24.dp))
    Reveal(delayMillis = 480, modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
        ConnectedRow(index = 0, count = 2, contentPadding = PaddingValues16) {
          Column {
            Text(
              text = stringResource(R.string.ob_alerts_notify_me),
              style = CaducityTheme.typography.bodyLarge,
              color = CaducityTheme.colorScheme.onSurface,
            )
            Text(
              text = stringResource(R.string.ob_alerts_before),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(12.dp))
            ConnectedButtonGroup(
              options =
                THRESHOLD_OPTIONS.map { days ->
                  ConnectedButtonOption(label = thresholdLabel(days))
                },
              selectedIndex = THRESHOLD_OPTIONS.indexOf(thresholdDays),
              onClick = { onThresholdChange(THRESHOLD_OPTIONS[it]) },
              height = 48.dp,
            )
            Spacer(Modifier.size(12.dp))
            Text(
              text = stringResource(R.string.ob_alerts_default_help),
              style = CaducityTheme.typography.bodySmall,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        ConnectedRow(index = 1, count = 2, contentPadding = PaddingValues16) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.ob_alerts_time),
              style = CaducityTheme.typography.bodyLarge,
              color = CaducityTheme.colorScheme.onSurface,
            )
            Text(
              text = stringResource(R.string.ob_alerts_daily),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
            )
          }
          Surface(
            onClick = onTimeClick,
            shape = RoundedCornerShape(50),
            color = CaducityTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.semantics { role = Role.Button },
          ) {
            Text(
              text = formatTime(time),
              style = CaducityTheme.typography.titleLarge,
              color = CaducityTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
          }
        }
      }
    }
    if (permissionGranted) {
      Spacer(Modifier.size(16.dp))
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(
          imageVector = Symbols.Filled.CheckCircle,
          contentDescription = null,
          tint = CaducityTheme.colors.fresh.strong,
          modifier = Modifier.size(24.dp),
        )
        Text(
          text = stringResource(R.string.ob_alerts_enabled),
          style = CaducityTheme.typography.titleSmall,
          color = CaducityTheme.colorScheme.onSurface,
          modifier = Modifier.padding(start = 8.dp),
        )
      }
    }
  }
}

private val PaddingValues16 = PaddingValues(16.dp)

@Composable
private fun thresholdLabel(days: Int): String =
  when (days) {
    1 -> stringResource(R.string.ob_alerts_days_1)
    3 -> stringResource(R.string.ob_alerts_days_3)
    else -> stringResource(R.string.ob_alerts_days_7)
  }

private fun formatTime(time: LocalTime): String = "%d:%02d".format(time.hour, time.minute)

@Composable
private fun SampleNotification(time: LocalTime) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = CaducityTheme.colorScheme.surfaceContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colorResource(R.color.ic_icon_rebost_background)),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(R.drawable.ic_icon_rebost_symbol),
          contentDescription = null,
          modifier = Modifier.requiredSize(58.dp),
        )
      }
      Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text(
            text = stringResource(R.string.ob_alerts_sample_app),
            style = CaducityTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatTime(time),
            style = CaducityTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = stringResource(R.string.ob_alerts_sample_title),
          style = CaducityTheme.typography.bodyLarge,
          color = CaducityTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.ob_alerts_sample_body),
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
      }
    }
  }
}

@PreviewLightDark
@Composable
private fun AlertsStepPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.background) {
      AlertsStep(
        stepNumber = 2,
        thresholdDays = 3,
        time = LocalTime(9, 0),
        permissionGranted = false,
        onThresholdChange = {},
        onTimeClick = {},
        onEnable = {},
        onNotNow = {},
        onBack = {},
      )
    }
  }
}
