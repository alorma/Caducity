package com.alorma.caducity.ui.screen.category.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

/**
 * «Descripció» sheet. What is typed applies at once, so dragging the sheet away or tapping outside keeps it;
 * «Esborra» only shows when the category already had a description when the sheet opened.
 */
@Composable
internal fun CategoryDescriptionSheet(
  description: String,
  onDescriptionChange: (String) -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hadDescription = remember { description.isNotBlank() }
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .imePadding()
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column {
      Text(
        text = stringResource(R.string.category_editor_description_row),
        style = CaducityTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
      )
      Text(
        text = stringResource(R.string.category_editor_description_sheet_help),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
    OutlinedTextField(
      value = description,
      onValueChange = onDescriptionChange,
      minLines = 2,
      maxLines = 4,
      keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
      shape = CaducityShapes.field,
      supportingText = {
        Text(
          text =
            stringResource(
              R.string.category_editor_description_counter,
              description.length,
              CategoryEditorState.DESCRIPTION_MAX_LENGTH,
            ),
          modifier = Modifier.fillMaxWidth(),
        )
      },
      modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
    )
    Text(
      text = stringResource(R.string.category_editor_description_placeholder),
      style = CaducityTheme.typography.bodySmall,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      if (hadDescription) {
        OutlinedButton(
          onClick = {
            onDescriptionChange("")
            onDone()
          },
          modifier = Modifier.weight(1f),
        ) {
          Text(stringResource(R.string.category_editor_description_clear))
        }
      }
      Button(onClick = onDone, modifier = Modifier.weight(1f)) {
        Text(stringResource(R.string.category_editor_done))
      }
    }
  }
}
