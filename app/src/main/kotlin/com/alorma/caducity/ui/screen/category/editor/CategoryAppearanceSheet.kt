package com.alorma.caducity.ui.screen.category.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.FormOverline
import com.alorma.caducity.ui.components.category.IconPicker
import com.alorma.caducity.ui.components.category.ShapePicker
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.theme.CaducityTheme

/** «Aspecte» sheet: shape and icon with a small preview. Changes apply at once, so there is no cancel. */
@Composable
internal fun CategoryAppearanceSheet(
  name: String,
  shape: CategoryShape,
  icon: CategoryIcon,
  onShapeChange: (CategoryShape) -> Unit,
  onIconChange: (CategoryIcon) -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      CategoryShapeIcon(shape = shape, icon = icon.imageVector, size = 72.dp, selected = true)
      Column {
        Text(
          text = stringResource(R.string.category_editor_appearance),
          style = CaducityTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        val trimmed = name.trim()
        Text(
          text =
            if (trimmed.isEmpty()) {
              stringResource(R.string.category_editor_appearance_pantry)
            } else {
              stringResource(R.string.category_editor_appearance_named, trimmed)
            },
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_form_shape))
      ShapePicker(selected = shape, onSelect = onShapeChange)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      FormOverline(stringResource(R.string.category_form_icon))
      IconPicker(selected = icon, onSelect = onIconChange)
    }
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(R.string.category_editor_done))
    }
  }
}
