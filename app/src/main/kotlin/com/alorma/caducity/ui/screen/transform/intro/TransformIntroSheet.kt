package com.alorma.caducity.ui.screen.transform.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingPrimaryButton
import com.alorma.caducity.ui.screen.onboarding.steps.TransformDiagram
import com.alorma.caducity.ui.screen.onboarding.steps.TransformHint
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.OverlineTextStyle
import com.alorma.caducity.ui.theme.preview.PreviewTheme

/**
 * «Novetat Transforma»: the content of the bottom sheet that tells whoever updates the app about Transforma, once.
 * It is a shorter version of the onboarding step (diagram and where to find it, without «not a recipe book» so the
 * sheet does not fill the screen) with two actions of the same size: try it, or not now.
 */
@Composable
fun TransformIntroSheet(
  onTry: () -> Unit,
  onNotNow: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // The content scrolls if it does not fit (big font, small screen); the two buttons stay fixed at the bottom.
  Column(
    modifier =
      modifier
        .navigationBarsPadding()
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(
      modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Surface(shape = CircleShape, color = CaducityTheme.colorScheme.primaryContainer) {
        Text(
          text = stringResource(R.string.transform_intro_badge),
          style = OverlineTextStyle,
          color = CaducityTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
      }
      Text(
        text = stringResource(R.string.transform_title),
        style = CaducityTheme.typography.headlineMedium,
        color = CaducityTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() },
      )
      Text(
        text = stringResource(R.string.transform_intro_body),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
      TransformDiagram()
      TransformHint(hint = stringResource(R.string.transform_intro_hint))
    }
    OnboardingPrimaryButton(
      text = stringResource(R.string.transform_intro_try),
      onClick = onTry,
      modifier = Modifier.fillMaxWidth(),
    )
    TextButton(onClick = onNotNow, modifier = Modifier.fillMaxWidth().height(60.dp)) {
      Text(
        text = stringResource(R.string.ob_not_now),
        style = CaducityTheme.typography.titleSmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@PreviewLightDark
@Composable
private fun TransformIntroSheetPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.surfaceContainerLow) {
      TransformIntroSheet(onTry = {}, onNotNow = {})
    }
  }
}
