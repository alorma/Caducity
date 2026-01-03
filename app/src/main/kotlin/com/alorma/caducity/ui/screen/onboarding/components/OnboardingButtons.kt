package com.alorma.caducity.ui.screen.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R

@Composable
fun OnboardingButtons(
  canSkip: Boolean,
  isLastPage: Boolean,
  isFirstPage: Boolean,
  onNext: () -> Unit,
  onPrevious: () -> Unit,
  onSkip: () -> Unit,
  onComplete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    // Left side: Previous or Skip button
    if (isFirstPage) {
      if (canSkip) {
        TextButton(onClick = onSkip) {
          Text(stringResource(R.string.onboarding_button_skip))
        }
      } else {
        Spacer(modifier = Modifier.width(0.dp))
      }
    } else {
      TextButton(onClick = onPrevious) {
        Text(stringResource(R.string.onboarding_button_previous))
      }
    }

    // Right side: Next or Complete button
    if (isLastPage) {
      Button(onClick = onComplete) {
        Text(stringResource(R.string.onboarding_button_understand))
      }
    } else {
      Button(onClick = onNext) {
        Text(stringResource(R.string.onboarding_button_next))
      }
    }
  }
}
