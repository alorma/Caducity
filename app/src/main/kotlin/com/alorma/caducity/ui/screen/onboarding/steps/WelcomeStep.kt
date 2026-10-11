package com.alorma.caducity.ui.screen.onboarding.steps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ArrowForward
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.toComposeShape
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.components.motion.rememberReducedMotion
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingPrimaryButton
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun WelcomeStep(
  onStart: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    DecorativeShapes()
    Column(
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp),
    ) {
      AppIcon()
      Spacer(Modifier.height(24.dp))
      Reveal(delayMillis = 250, durationMillis = 600, offsetY = 24.dp) {
        Text(
          text = stringResource(R.string.ob_welcome_title),
          style = CaducityTheme.typography.headlineLarge.copy(fontSize = 48.sp, lineHeight = 50.sp),
          color = CaducityTheme.colorScheme.onSurface,
        )
      }
      Spacer(Modifier.height(16.dp))
      Reveal(delayMillis = 400, durationMillis = 600, offsetY = 24.dp) {
        Text(
          text = stringResource(R.string.ob_welcome_body),
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(Modifier.height(28.dp))
      Reveal(delayMillis = 550, durationMillis = 600, offsetY = 24.dp, modifier = Modifier.fillMaxWidth()) {
        OnboardingPrimaryButton(
          text = stringResource(R.string.ob_start),
          onClick = onStart,
          modifier = Modifier.fillMaxWidth(),
          trailingIcon = Symbols.Outlined.ArrowForward,
        )
      }
    }
  }
}

@Composable
private fun AppIcon() {
  val reduced = rememberReducedMotion()
  val scale = remember { Animatable(if (reduced) 1f else 0.4f) }
  LaunchedEffect(reduced) {
    if (reduced) {
      scale.snapTo(1f)
    } else {
      scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
  }
  Box(
    modifier =
      Modifier
        .size(96.dp)
        .graphicsLayer {
          scaleX = scale.value
          scaleY = scale.value
          alpha = ((scale.value - 0.4f) / 0.3f).coerceIn(0f, 1f)
        }.clip(CircleShape)
        .background(colorResource(R.color.ic_launcher_background)),
    contentAlignment = Alignment.Center,
  ) {
    // The adaptive-icon foreground is drawn on a 108 dp canvas with a safe zone: it is enlarged to fill the circle.
    Image(
      painter = painterResource(R.drawable.ic_launcher_foreground),
      contentDescription = null,
      modifier = Modifier.requiredSize(138.dp),
    )
  }
}

@Composable
private fun DecorativeShapes() {
  val reduced = rememberReducedMotion()
  val transition = rememberInfiniteTransition(label = "welcome-shapes")
  val flowerAngle by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(tween(28_000, easing = LinearEasing)),
    label = "flower",
  )
  val sunAngle by transition.animateFloat(
    initialValue = 0f,
    targetValue = -360f,
    animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing)),
    label = "sun",
  )
  val breathe by transition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(tween(4_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "clover",
  )
  Box(modifier = Modifier.fillMaxSize()) {
    Shape(
      shape = CategoryShape.CLOVER8,
      color = CaducityTheme.colors.fresh.container,
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .offset(x = 50.dp, y = (-4).dp)
          .size(200.dp)
          .rotate(if (reduced) 0f else flowerAngle),
    )
    Shape(
      shape = CategoryShape.SUNNY,
      color = CaducityTheme.colors.soon.container,
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .offset(x = (-64).dp, y = 190.dp)
          .size(160.dp)
          .rotate(if (reduced) 0f else sunAngle),
    )
    Shape(
      shape = CategoryShape.CLOVER,
      color = CaducityTheme.colors.expired.container,
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .offset(x = (-30).dp, y = 280.dp)
          .size(52.dp)
          .scale(if (reduced) 1f else breathe),
    )
  }
}

@Composable
private fun Shape(
  shape: CategoryShape,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.clip(shape.toComposeShape()).background(color))
}

@PreviewLightDark
@Composable
private fun WelcomeStepPreview() {
  PreviewTheme {
    Box(Modifier.background(CaducityTheme.colorScheme.background)) { WelcomeStep(onStart = {}) }
  }
}
