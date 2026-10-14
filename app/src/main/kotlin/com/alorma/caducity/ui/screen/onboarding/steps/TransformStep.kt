package com.alorma.caducity.ui.screen.onboarding.steps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Eco
import com.alorma.caducity.base.ui.icons.symbols.MenuBook
import com.alorma.caducity.base.ui.icons.symbols.SetMeal
import com.alorma.caducity.base.ui.icons.symbols.SoupKitchen
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.components.motion.rememberReducedMotion
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingBackAndPrimary
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingStepLayout
import com.alorma.caducity.ui.screen.transform.RowLinksNode
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.coroutines.delay

/** Informative step about Transforma: free, stores nothing. */
@Composable
fun TransformStep(
  stepNumber: Int?,
  stepCount: Int,
  onBack: () -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  OnboardingStepLayout(
    modifier = modifier,
    stepNumber = stepNumber,
    stepCount = stepCount,
    title = stringResource(R.string.ob_transform_title),
    body = stringResource(R.string.ob_transform_body),
    actions = {
      OnboardingBackAndPrimary(
        primaryText = stringResource(R.string.ob_continue),
        onPrimary = onContinue,
        onBack = onBack,
      )
    },
  ) {
    Reveal(delayMillis = 240, modifier = Modifier.fillMaxWidth()) { TransformDiagram() }
    Spacer(Modifier.height(20.dp))
    Reveal(delayMillis = 2000, modifier = Modifier.fillMaxWidth()) { NotARecipeBookCard() }
    Spacer(Modifier.height(20.dp))
    Reveal(delayMillis = 2200, modifier = Modifier.fillMaxWidth()) {
      TransformHint(hint = stringResource(R.string.ob_transform_hint))
    }
  }
}

/**
 * Two sample ingredients → node → the result: the same shape as the Transforma screen, and it builds itself in that
 * order: the ingredients appear, the lines draw into the node, and the result drops out of it. [startDelayMillis] is
 * the wait before the first stage. With the system animations off it shows complete.
 */
@Composable
internal fun TransformDiagram(
  modifier: Modifier = Modifier,
  startDelayMillis: Int = DiagramStartDelay,
) {
  val line = CaducityTheme.colorScheme.outline
  val reduced = rememberReducedMotion()
  val linesProgress = remember { Animatable(if (reduced) 1f else 0f) }
  LaunchedEffect(reduced) {
    if (reduced) {
      linesProgress.snapTo(1f)
    } else {
      delay((startDelayMillis + LinesDelay).toLong())
      linesProgress.animateTo(1f, tween(LinesDuration, easing = FastOutSlowInEasing))
    }
  }
  Surface(
    shape = RoundedCornerShape(28.dp),
    color = CaducityTheme.colorScheme.surfaceContainer,
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Reveal(delayMillis = startDelayMillis, modifier = Modifier.weight(1f)) {
          IngredientPill(Symbols.Outlined.SetMeal, stringResource(R.string.ob_transform_sample_chicken))
        }
        Reveal(delayMillis = startDelayMillis + IngredientStagger, modifier = Modifier.weight(1f)) {
          IngredientPill(Symbols.Outlined.Eco, stringResource(R.string.ob_transform_sample_vegetables))
        }
      }
      Canvas(Modifier.fillMaxWidth().height(36.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val end = Offset(size.width / 2f, size.height + 4.dp.toPx())
        val measure = PathMeasure()
        listOf(size.width * 0.27f, size.width * 0.73f).forEach { x ->
          val path =
            Path().apply {
              moveTo(x, 0f)
              cubicTo(x, size.height * 0.7f, end.x, size.height * 0.4f, end.x, end.y)
            }
          measure.setPath(path, forceClosed = false)
          val drawn = Path()
          measure.getSegment(0f, measure.length * linesProgress.value, drawn, startWithMoveTo = true)
          drawPath(drawn, line, style = stroke)
        }
      }
      Reveal(delayMillis = startDelayMillis + NodeDelay, offsetY = 0.dp, bouncy = true) {
        RowLinksNode(links = null)
      }
      Reveal(delayMillis = startDelayMillis + ResultDelay, offsetY = (-16).dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
          val resultLine = CaducityTheme.colorScheme.primary
          // From the node down to the result's icon, curved like the ingredient lines.
          Canvas(Modifier.fillMaxWidth().height(28.dp)) {
            val iconX = size.width * (1f - ResultCardWidth) / 2f + ResultIconCenter.toPx()
            val path =
              Path().apply {
                moveTo(size.width / 2f, 0f)
                cubicTo(size.width / 2f, size.height * 0.6f, iconX, size.height * 0.4f, iconX, size.height)
              }
            drawPath(path, resultLine, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
          }
          SampleResultCard()
        }
      }
    }
  }
}

private const val DiagramStartDelay = 400
private const val IngredientStagger = 150
private const val LinesDelay = 600
private const val LinesDuration = 600
private const val NodeDelay = 1000
private const val ResultDelay = 1400

/** Width of the sample result card, as a fraction of the diagram. */
private const val ResultCardWidth = 0.8f

/** Center of the result's icon from the card's leading edge: padding + half the icon. */
private val ResultIconCenter = 12.dp + 22.dp

@Composable
private fun SampleResultCard() {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = CaducityTheme.colorScheme.primaryContainer,
    border = BorderStroke(2.dp, CaducityTheme.colorScheme.primary),
    modifier = Modifier.fillMaxWidth(ResultCardWidth),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      CategoryShapeIcon(shape = CategoryShape.COOKIE7, icon = Symbols.Outlined.SoupKitchen, size = 44.dp)
      Column {
        Text(
          text = stringResource(R.string.ob_transform_sample_result),
          style = CaducityTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onPrimaryContainer,
        )
        Text(
          text = stringResource(R.string.ob_transform_sample_result_meta),
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onPrimaryContainer,
        )
      }
    }
  }
}

@Composable
private fun IngredientPill(
  icon: ImageVector,
  name: String,
  modifier: Modifier = Modifier,
) {
  Surface(shape = CircleShape, color = CaducityTheme.colorScheme.background, modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CategoryShapeIcon(shape = CategoryShape.COOKIE7, icon = icon, size = 36.dp)
      Text(
        text = name,
        style = CaducityTheme.typography.titleSmall,
        color = CaducityTheme.colorScheme.onSurface,
        maxLines = 2,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

/** «No és un receptari»: the product rule, said out loud. The book is crossed out in red. */
@Composable
internal fun NotARecipeBookCard(modifier: Modifier = Modifier) {
  val red = CaducityTheme.colorScheme.error
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    border = BorderStroke(2.dp, CaducityTheme.colorScheme.outline),
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Surface(shape = CircleShape, color = CaducityTheme.colorScheme.background, modifier = Modifier.size(48.dp)) {
        Box(
          modifier =
            Modifier.drawWithContent {
              drawContent()
              val inset = size.width * 0.22f
              drawLine(
                color = red,
                start = Offset(inset, inset),
                end = Offset(size.width - inset, size.height - inset),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
              )
            },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Symbols.Outlined.MenuBook,
            contentDescription = null,
            tint = CaducityTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
          )
        }
      }
      Column {
        Text(
          text = stringResource(R.string.ob_transform_not_recipe_title),
          style = CaducityTheme.typography.titleMedium,
          color = CaducityTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.ob_transform_not_recipe_body),
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

/** Where Transforma lives: the end of the home list and every product. */
@Composable
internal fun TransformHint(
  hint: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = hint,
    style = CaducityTheme.typography.bodyMedium,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
  )
}

@PreviewLightDark
@Composable
internal fun TransformStepPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.background) {
      TransformStep(stepNumber = 3, stepCount = 5, onBack = {}, onContinue = {})
    }
  }
}
