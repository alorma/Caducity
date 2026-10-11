package com.alorma.caducity.ui.components.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

/** Name field with live preview, shape grid and icon grid. Shared by «Nova categoria ràpida» and the editor. */
@Composable
fun CategoryForm(
  name: String,
  onNameChange: (String) -> Unit,
  shape: CategoryShape,
  onShapeChange: (CategoryShape) -> Unit,
  icon: CategoryIcon,
  onIconChange: (CategoryIcon) -> Unit,
  modifier: Modifier = Modifier,
  error: String? = null,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      CategoryShapeIcon(shape = shape, icon = icon.imageVector, size = 64.dp, selected = true)
      OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.category_form_name)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        shape = CaducityShapes.field,
        modifier = Modifier.weight(1f),
      )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_form_shape))
      ShapePicker(selected = shape, onSelect = onShapeChange)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_form_icon))
      IconPicker(selected = icon, onSelect = onIconChange)
    }
  }
}

@Composable
internal fun FormOverline(text: String) {
  Text(
    text = text.uppercase(),
    style = CaducityTheme.typography.labelSmall,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
  )
}

/** The 6 category shapes in a grid of 52 dp tiles. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShapePicker(
  selected: CategoryShape,
  onSelect: (CategoryShape) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CategoryShape.entries.forEachIndexed { index, shape ->
      val isSelected = shape == selected
      val description = stringResource(R.string.category_form_shape_option, index + 1, CategoryShape.entries.size)
      val scheme = CaducityTheme.colorScheme
      Surface(
        selected = isSelected,
        onClick = { onSelect(shape) },
        shape = CaducityShapes.field,
        color = if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh,
        border = if (isSelected) BorderStroke(2.dp, scheme.primary) else null,
        modifier =
          Modifier
            .size(52.dp)
            .semantics { contentDescription = description },
      ) {
        Box(contentAlignment = Alignment.Center) {
          Box(
            modifier =
              Modifier
                .size(30.dp)
                .clip(shape.toComposeShape())
                .background(if (isSelected) scheme.primary else scheme.primary.copy(alpha = 0.4f)),
          )
        }
      }
    }
  }
}

/** The 12 category icons in a grid of 48 dp circles. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPicker(
  selected: CategoryIcon,
  onSelect: (CategoryIcon) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CategoryIcons.picker.forEach { icon ->
      val isSelected = icon == selected
      val scheme = CaducityTheme.colorScheme
      Surface(
        selected = isSelected,
        onClick = { onSelect(icon) },
        shape = CircleShape,
        color = if (isSelected) scheme.primary else scheme.surfaceContainerHigh,
        contentColor = if (isSelected) scheme.onPrimary else scheme.onSurface,
        modifier = Modifier.size(48.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon.imageVector, contentDescription = null, modifier = Modifier.size(24.dp).padding(0.dp))
        }
      }
    }
  }
}
