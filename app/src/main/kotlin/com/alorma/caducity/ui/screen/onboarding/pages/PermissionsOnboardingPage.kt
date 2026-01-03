package com.alorma.caducity.ui.screen.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Notifications
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import org.koin.compose.koinInject

@Composable
fun PermissionsOnboardingPage(
  modifier: Modifier = Modifier,
  notificationHelper: ExpirationNotificationHelper = koinInject(),
) {
  notificationHelper.registerContract()

  val hasPermission by notificationHelper.areNotificationsEnabled()

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = AppIcons.Notifications,
      contentDescription = null,
      modifier = Modifier.size(80.dp),
      tint = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text(
      text = stringResource(R.string.onboarding_permissions_title),
      style = MaterialTheme.typography.displaySmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = stringResource(R.string.onboarding_permissions_message),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(32.dp))

    if (!hasPermission) {
      Button(
        onClick = { notificationHelper.launch() },
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(stringResource(R.string.onboarding_button_allow_notifications))
      }
    } else {
      Text(
        text = stringResource(R.string.onboarding_permissions_granted),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
