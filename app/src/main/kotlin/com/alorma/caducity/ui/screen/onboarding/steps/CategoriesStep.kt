package com.alorma.caducity.ui.screen.onboarding.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.CheckCircle
import com.alorma.caducity.base.ui.icons.symbols.RadioButtonUnchecked
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.SuggestedCategory
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.motion.Reveal
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingBackAndPrimary
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingStepLayout
import com.alorma.caducity.ui.screen.onboarding.nameRes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme

@Composable
fun CategoriesStep(
  stepNumber: Int,
  selected: Set<SuggestedCategory>,
  onToggle: (SuggestedCategory) -> Unit,
  onBack: () -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  OnboardingStepLayout(
    modifier = modifier,
    stepNumber = stepNumber,
    title = stringResource(R.string.ob_categories_title),
    body = stringResource(R.string.ob_categories_body),
    actions = {
      OnboardingBackAndPrimary(
        primaryText =
          if (selected.isEmpty()) {
            stringResource(R.string.ob_categories_continue_none)
          } else {
            stringResource(R.string.ob_categories_continue, selected.size)
          },
        onPrimary = onContinue,
        onBack = onBack,
      )
    },
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      SuggestedCategory.entries.chunked(2).forEachIndexed { row, categories ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          categories.forEachIndexed { column, category ->
            Reveal(delayMillis = 300 + 70 * (row * 2 + column), modifier = Modifier.weight(1f)) {
              CategoryCard(
                category = category,
                checked = category in selected,
                onCheckedChange = { onToggle(category) },
              )
            }
          }
          if (categories.size == 1) Spacer(Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun CategoryCard(
  category: SuggestedCategory,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scheme = CaducityTheme.colorScheme
  val container by animateColorAsState(
    if (checked) scheme.primaryContainer else scheme.surfaceContainer,
    label = "card",
  )
  val border by animateColorAsState(
    if (checked) scheme.primary else scheme.surfaceContainer.copy(alpha = 0f),
    label = "border",
  )
  val iconScale by animateFloatAsState(if (checked) 1f else 0.92f, label = "icon")
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .height(104.dp)
        .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
    shape = RoundedCornerShape(24.dp),
    color = container,
    border = BorderStroke(2.dp, border),
  ) {
    Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
      CategoryShapeIcon(
        shape = category.shape,
        icon = category.icon.imageVector,
        size = 44.dp,
        selected = checked,
        modifier = Modifier.align(Alignment.TopStart).scale(iconScale),
      )
      Icon(
        imageVector = if (checked) Symbols.Filled.CheckCircle else Symbols.Outlined.RadioButtonUnchecked,
        contentDescription = null,
        tint = if (checked) scheme.primary else scheme.outline,
        modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
      )
      Text(
        text = stringResource(category.nameRes),
        style = CaducityTheme.typography.bodyLarge,
        color = scheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.align(Alignment.BottomStart),
      )
    }
  }
}

@PreviewLightDark
@Composable
private fun CategoriesStepPreview() {
  PreviewTheme {
    Surface(color = CaducityTheme.colorScheme.background) {
      CategoriesStep(
        stepNumber = 1,
        selected = SuggestedCategory.entries.filter { it.selectedByDefault }.toSet(),
        onToggle = {},
        onBack = {},
        onContinue = {},
      )
    }
  }
}
