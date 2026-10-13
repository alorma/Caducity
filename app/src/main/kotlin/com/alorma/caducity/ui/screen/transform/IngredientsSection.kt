package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.SoupKitchen
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.components.shape.connectedShape
import com.alorma.caducity.ui.screen.add.StepButton
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** «Ingredients»: a connected list, one row per product, closed by the tonal «Afegeix ingredient» row. */
@Composable
fun IngredientsSection(
  ingredients: List<IngredientUi>,
  categories: List<Category>,
  onCountChange: (key: String, count: Int) -> Unit,
  onRemove: (key: String) -> Unit,
  onAdd: () -> Unit,
  modifier: Modifier = Modifier,
  /** Free entry: ingredients are optional and, with none, the section shows an empty state. */
  optional: Boolean = false,
) {
  val count = ingredients.size + 1
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
    Text(
      text =
        stringResource(
          if (optional) R.string.transform_section_ingredients_optional else R.string.transform_section_ingredients,
        ),
      style = CaducityTheme.typography.labelLarge,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
    )
    if (ingredients.isEmpty() && optional) {
      EmptyIngredients(onAdd = onAdd)
      return@Column
    }
    ingredients.forEachIndexed { index, ingredient ->
      IngredientRow(
        ingredient = ingredient,
        category = categories.firstOrNull { it.id == ingredient.categoryId },
        index = index,
        count = count,
        onCountChange = { onCountChange(ingredient.key, it) },
        onRemove = { onRemove(ingredient.key) },
      )
    }
    AddIngredientRow(index = ingredients.size, count = count, onClick = onAdd)
  }
}

/** «Què has cuinat?»: nothing added yet, and that is fine. */
@Composable
private fun EmptyIngredients(onAdd: () -> Unit) {
  Surface(
    shape = CaducityTheme.shapes.extraLarge,
    color = CaducityTheme.colorScheme.surfaceContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CategoryShapeIcon(shape = CategoryShape.COOKIE, icon = Symbols.Outlined.SoupKitchen, size = 56.dp)
      Text(
        text = stringResource(R.string.transform_empty_title),
        style = CaducityTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp),
      )
      Text(
        text = stringResource(R.string.transform_empty_body),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      Surface(
        onClick = onAdd,
        shape = CaducityShapes.full,
        color = CaducityTheme.colorScheme.secondaryContainer,
        contentColor = CaducityTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.padding(top = 8.dp),
      ) {
        Row(
          modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
          Text(stringResource(R.string.transform_add_ingredient), style = CaducityTheme.typography.labelLarge)
        }
      }
    }
  }
}

@Composable
private fun IngredientRow(
  ingredient: IngredientUi,
  category: Category?,
  index: Int,
  count: Int,
  onCountChange: (Int) -> Unit,
  onRemove: () -> Unit,
) {
  ConnectedRow(
    index = index,
    count = count,
    contentPadding = PaddingValues(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
  ) {
    if (category != null) {
      CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 44.dp)
    }
    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = ingredient.name,
        style = CaducityTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = ingredientMeta(ingredient),
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (ingredient.available > 1) {
        Spacer(Modifier.size(6.dp))
        UnitsStepper(count = ingredient.count, available = ingredient.available, onChange = onCountChange)
      }
    }
    IconButton(onClick = onRemove) {
      Icon(
        Symbols.Outlined.Close,
        contentDescription = stringResource(R.string.transform_ingredient_remove, ingredient.name),
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

/** `− 1 de 2 +`: announces the new value politely. */
@Composable
private fun UnitsStepper(
  count: Int,
  available: Int,
  onChange: (Int) -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    StepButton(
      enabled = count > 1,
      onClick = { onChange(count - 1) },
      label = stringResource(R.string.add_quantity_decrease),
    ) {
      Text("−", style = CaducityTheme.typography.titleMedium)
    }
    Text(
      text = stringResource(R.string.transform_ingredient_units, count, available),
      style = CaducityTheme.typography.labelLarge,
      textAlign = TextAlign.Center,
      maxLines = 1,
      modifier = Modifier.width(72.dp).semantics { liveRegion = LiveRegionMode.Polite },
    )
    StepButton(
      enabled = count < available,
      onClick = { onChange(count + 1) },
      label = stringResource(R.string.add_quantity_increase),
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
private fun AddIngredientRow(
  index: Int,
  count: Int,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = connectedShape(index, count),
    color = CaducityTheme.colorScheme.secondaryContainer,
    contentColor = CaducityTheme.colorScheme.onSecondaryContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(stringResource(R.string.transform_add_ingredient), style = CaducityTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun ingredientMeta(ingredient: IngredientUi): String {
  val first = ingredient.spent.firstOrNull() ?: return ""
  return if (first.frozen) {
    stringResource(R.string.item_actions_frozen_meta)
  } else {
    stringResource(R.string.item_actions_expires, formatDate(first.expiration, LocalLocale.current.platformLocale))
  }
}

/** «dc. 21 oct», localized. */
internal fun formatDate(
  date: LocalDate,
  locale: Locale,
): String = DateTimeFormatter.ofPattern("EEE d MMM", locale).format(date.toJavaLocalDate())
