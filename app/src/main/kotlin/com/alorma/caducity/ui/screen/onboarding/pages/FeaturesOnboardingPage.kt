package com.alorma.caducity.ui.screen.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.CalendarExpand
import com.alorma.caducity.base.ui.icons.Dashboard
import com.alorma.caducity.base.ui.icons.Notifications
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun FeaturesOnboardingPage(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.onboarding_features_title),
      style = CaducityTheme.typography.displaySmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(48.dp))

    FeatureItem(
      icon = AppIcons.CalendarExpand,
      text = stringResource(R.string.onboarding_features_track),
    )

    Spacer(modifier = Modifier.height(24.dp))

    FeatureItem(
      icon = AppIcons.Notifications,
      text = stringResource(R.string.onboarding_features_notify),
    )

    Spacer(modifier = Modifier.height(24.dp))

    FeatureItem(
      icon = AppIcons.Filled.Dashboard,
      text = stringResource(R.string.onboarding_features_dashboard),
    )
  }
}

@Composable
private fun FeatureItem(
  icon: ImageVector,
  text: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier.size(48.dp),
      tint = CaducityTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.width(16.dp))

    Text(
      text = text,
      style = CaducityTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f),
    )
  }
}
