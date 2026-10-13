package com.alorma.caducity.ui.screen.transform

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.UnfoldMore
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.screen.add.ScreenGutter
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** Width of every card in the two horizontal rows: narrow enough for the next one to show half. */
internal val RowCardWidth = 148.dp
internal val RowCardSpacing = 10.dp
internal val RowCardShape = RoundedCornerShape(20.dp)
private val IngredientCardHeight = 118.dp

/**
 * «Ingredients · N»: a horizontal row of narrow cards, one per product. The «+ Afegeix» button sits in the header,
 * outside the scroll, so it is always within reach.
 */
@Composable
fun IngredientsSection(
  ingredients: List<IngredientUi>,
  categories: List<Category>,
  onCountChange: (key: String, count: Int) -> Unit,
  onRemove: (key: String) -> Unit,
  onAdd: () -> Unit,
  modifier: Modifier = Modifier,
  /** Free entry: ingredients are optional and, with none, the section shows a single dashed «Afegeix» row. */
  optional: Boolean = false,
  /** Cards below this index are folded away: the cascade that closes the screen after saving. */
  closedRows: Int = 0,
) {
  val empty = ingredients.isEmpty() && optional
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    RowSectionHeader(
      title =
        if (empty) {
          stringResource(R.string.transform_section_ingredients_optional)
        } else {
          stringResource(R.string.transform_section_ingredients_count, ingredients.size)
        },
      onAdd = if (empty) null else onAdd,
    )
    if (empty) {
      AddIngredientRow(onClick = onAdd)
      return@Column
    }
    val visible = ingredients.drop(closedRows)
    val listState = rememberLazyListState()
    // TODO(transforma-open-1): the stepper only folds when the user taps outside or scrolls; maybe also after a few seconds.
    var editingKey by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(listState) {
      snapshotFlow { listState.isScrollInProgress }.filter { it }.collect { editingKey = null }
    }
    LazyRow(
      state = listState,
      contentPadding = PaddingValues(horizontal = ScreenGutter),
      horizontalArrangement = Arrangement.spacedBy(RowCardSpacing),
      modifier =
        Modifier
          .pointerInput(Unit) { detectTapGestures { editingKey = null } }
          .semantics { collectionInfo = CollectionInfo(rowCount = 1, columnCount = visible.size) },
    ) {
      items(visible, key = { it.key }) { ingredient ->
        IngredientCard(
          ingredient = ingredient,
          category = categories.firstOrNull { it.id == ingredient.categoryId },
          editing = editingKey == ingredient.key,
          onEditingChange = { editingKey = if (it) ingredient.key else null },
          onCountChange = { onCountChange(ingredient.key, it) },
          onRemove = { onRemove(ingredient.key) },
          modifier = Modifier.animateItem(),
        )
      }
    }
  }
}

/** Section label (12 sp, uppercase, spaced) with the optional tonal «+ Afegeix» button on the right. */
@Composable
internal fun RowSectionHeader(
  title: String,
  onAdd: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = ScreenGutter).padding(start = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title.uppercase(),
      style = CaducityTheme.typography.labelLarge.copy(fontSize = 12.sp, letterSpacing = 1.2.sp),
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f),
    )
    if (onAdd != null) {
      Surface(
        onClick = onAdd,
        shape = CaducityShapes.full,
        color = CaducityTheme.colorScheme.primaryContainer,
        contentColor = CaducityTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.minimumInteractiveComponentSize().height(32.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Text(stringResource(R.string.transform_add), style = CaducityTheme.typography.labelLarge)
        }
      }
    }
  }
}

/** Free entry without ingredients: one dashed row instead of the big card. */
@Composable
private fun AddIngredientRow(onClick: () -> Unit) {
  val dash = CaducityTheme.colorScheme.outline
  Surface(
    onClick = onClick,
    shape = RowCardShape,
    color = Color.Transparent,
    contentColor = CaducityTheme.colorScheme.onSurfaceVariant,
    modifier =
      Modifier
        .padding(horizontal = ScreenGutter)
        .fillMaxWidth()
        .height(64.dp)
        .drawBehind {
          drawRoundRect(
            color = dash,
            cornerRadius = CornerRadius(20.dp.toPx()),
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))),
          )
        },
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(stringResource(R.string.transform_add_ingredient), style = CaducityTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun IngredientCard(
  ingredient: IngredientUi,
  category: Category?,
  editing: Boolean,
  onEditingChange: (Boolean) -> Unit,
  onCountChange: (Int) -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RowCardShape,
    color = CaducityTheme.colorScheme.surfaceContainer,
    modifier = modifier.size(width = RowCardWidth, height = IngredientCardHeight),
  ) {
    Box {
      Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (category != null) {
          CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 28.dp)
        } else {
          Spacer(Modifier.size(28.dp))
        }
        Spacer(Modifier.size(6.dp))
        Text(
          text = ingredient.name,
          style = CaducityTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = ingredientMeta(ingredient),
          style = CaducityTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        // Only products with more than one unit have an amount to change.
        if (ingredient.available > 1) {
          if (editing) {
            UnitsStepper(count = ingredient.count, available = ingredient.available, onChange = onCountChange)
          } else {
            UnitsChip(
              count = ingredient.count,
              available = ingredient.available,
              onClick = { onEditingChange(true) },
            )
          }
        }
      }
      IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd)) {
        Icon(
          Symbols.Outlined.Close,
          contentDescription = stringResource(R.string.transform_ingredient_remove, ingredient.name),
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

/** `1 de 25 ⇅`: tapping it turns it into the stepper. */
@Composable
private fun UnitsChip(
  count: Int,
  available: Int,
  onClick: () -> Unit,
) {
  val description = stringResource(R.string.transform_ingredient_units_edit, count, available)
  Surface(
    onClick = onClick,
    shape = CaducityShapes.full,
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    modifier = Modifier.height(32.dp).semantics { contentDescription = description },
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.transform_ingredient_units, count, available),
        style = CaducityTheme.typography.labelLarge,
        maxLines = 1,
      )
      Icon(Symbols.Outlined.UnfoldMore, contentDescription = null, modifier = Modifier.size(16.dp))
    }
  }
}

/** `− 2 de 25 +`: announces the new value politely. At least one unit, at most the ones left. */
@Composable
private fun UnitsStepper(
  count: Int,
  available: Int,
  onChange: (Int) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(
          32.dp,
        ).clip(CaducityShapes.full)
        .background(CaducityTheme.colorScheme.surface),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    StepperButton(
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
      modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
    )
    StepperButton(
      enabled = count < available,
      onClick = { onChange(count + 1) },
      label = stringResource(R.string.add_quantity_increase),
    ) {
      Icon(Symbols.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
    }
  }
}

@Composable
private fun StepperButton(
  enabled: Boolean,
  onClick: () -> Unit,
  label: String,
  content: @Composable () -> Unit,
) {
  Surface(
    onClick = onClick,
    enabled = enabled,
    shape = CircleShape,
    color = Color.Transparent,
    contentColor = if (enabled) CaducityTheme.colorScheme.onSurface else CaducityTheme.colorScheme.outline,
    modifier = Modifier.size(32.dp).semantics { contentDescription = label },
  ) {
    Box(contentAlignment = Alignment.Center) { content() }
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
