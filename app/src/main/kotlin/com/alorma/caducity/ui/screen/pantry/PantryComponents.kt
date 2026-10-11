package com.alorma.caducity.ui.screen.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Search
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryCategory
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.categoryMeta
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import kotlinx.datetime.LocalDate

@Composable
internal fun PantrySearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
) {
  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).focusRequester(focusRequester),
    singleLine = true,
    shape = CaducityShapes.full,
    placeholder = { Text(stringResource(R.string.pantry_search)) },
    leadingIcon = { Icon(Symbols.Outlined.Search, contentDescription = null) },
    trailingIcon =
      if (query.isNotEmpty()) {
        {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.pantry_search_clear))
          }
        }
      } else {
        null
      },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(),
    colors =
      TextFieldDefaults.colors(
        focusedContainerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        unfocusedContainerColor = CaducityTheme.colorScheme.surfaceContainerHigh,
        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
      ),
  )
}

@Composable
internal fun CategoryCard(
  entry: PantryCategory,
  today: LocalDate,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val category = entry.category
  Surface(
    onClick = onClick,
    modifier = modifier.heightIn(min = 178.dp),
    shape = CaducityShapes.card,
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        CategoryShapeIcon(category.shape, CategoryIcons.vectorOf(category.icon), size = 56.dp)
        if (entry.expiredCount > 0) {
          val colors = ExpiryStatus.EXPIRED.colors()
          Text(
            text = pluralStringResource(R.plurals.pantry_card_expired, entry.expiredCount, entry.expiredCount),
            style = CaducityTheme.typography.labelMedium,
            color = colors.onContainer,
            modifier =
              Modifier
                .clip(CaducityShapes.chip)
                .background(colors.container)
                .padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }
      Spacer(Modifier.weight(1f))
      Column {
        Text(
          category.name,
          style = CaducityTheme.typography.titleMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = categoryMeta(entry.unitCount, entry.nearest, today),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      StatusBar(entry)
    }
  }
}

/** Decorative: the textual information is already on the card. */
@Composable
private fun StatusBar(entry: PantryCategory) {
  if (entry.statusCounts.isEmpty()) {
    Box(
      Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(CaducityShapes.inner)
        .background(CaducityTheme.colorScheme.surfaceContainerHighest),
    )
    return
  }
  Row(
    modifier = Modifier.fillMaxWidth().height(8.dp).clearAndSetSemantics { },
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    entry.statusCounts.forEach { (status, count) ->
      Box(
        Modifier
          .weight(count.toFloat())
          .height(8.dp)
          .clip(
            androidx.compose.foundation.shape
              .RoundedCornerShape(4.dp),
          ).background(status.colors().strong),
      )
    }
  }
}

@Composable
internal fun NewCategoryCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dash = CaducityTheme.colorScheme.outlineVariant
  Box(
    modifier =
      modifier
        .heightIn(min = 178.dp)
        .clip(CaducityShapes.card)
        .clickable(role = Role.Button, onClick = onClick)
        .drawBehind {
          val stroke = 2.dp.toPx()
          drawRoundRect(
            color = dash,
            topLeft =
              androidx.compose.ui.geometry
                .Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(28.dp.toPx()),
            style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))),
          )
        },
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Icon(
        Symbols.Outlined.Add,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp),
      )
      Text(
        stringResource(R.string.pantry_new_category),
        style = CaducityTheme.typography.labelLarge,
        color = CaducityTheme.colorScheme.primary,
      )
    }
  }
}
