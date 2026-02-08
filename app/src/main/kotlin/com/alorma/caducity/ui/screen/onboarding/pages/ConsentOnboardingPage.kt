package com.alorma.caducity.ui.screen.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.feature.consent.ConsentPreferences
import com.alorma.caducity.feature.consent.ConsentStatus
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun ConsentOnboardingPage(
  onConsentChanged: (ConsentPreferences) -> Unit,
  modifier: Modifier = Modifier,
) {
  var analyticsEnabled by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.onboarding_consent_title),
      style = CaducityTheme.typography.displaySmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.onboarding_consent_description),
      style = CaducityTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(32.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(
          modifier = Modifier.weight(1f),
        ) {
          Text(
            text = stringResource(R.string.onboarding_consent_analytics_title),
            style = CaducityTheme.typography.titleMedium,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = stringResource(R.string.onboarding_consent_analytics_description),
            style = CaducityTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = analyticsEnabled,
          onCheckedChange = { enabled ->
            analyticsEnabled = enabled
            val preferences = if (enabled) {
              ConsentPreferences.ANALYTICS_ONLY
            } else {
              ConsentPreferences.DEFAULT
            }
            onConsentChanged(preferences)
          },
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.onboarding_consent_privacy_note),
      style = CaducityTheme.typography.bodySmall,
      textAlign = TextAlign.Center,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
