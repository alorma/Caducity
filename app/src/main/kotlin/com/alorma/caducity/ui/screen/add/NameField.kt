package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AddCircle
import com.alorma.caducity.base.ui.icons.symbols.Cancel
import com.alorma.caducity.base.ui.icons.symbols.History
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme

/** Section title in overline style. */
@Composable
fun AddOverline(
  text: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text.uppercase(),
    style = CaducityTheme.typography.labelSmall,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
    modifier = modifier,
  )
}

@Composable
fun NameField(
  name: String,
  onNameChange: (String) -> Unit,
  onClear: () -> Unit,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  hint: String? = null,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val focused by interactionSource.collectIsFocusedAsState()
  val textStyle =
    CaducityTheme.typography.titleSmall.copy(
      fontSize = 19.sp,
      fontWeight = FontWeight.SemiBold,
      color = CaducityTheme.colorScheme.onSurface,
    )
  Surface(
    shape = CaducityShapes.field,
    color = CaducityTheme.colorScheme.surfaceContainerHigh,
    border = if (focused) BorderStroke(2.dp, CaducityTheme.colorScheme.primary) else null,
    modifier = modifier.fillMaxWidth().height(60.dp),
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, end = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
        if (name.isEmpty()) {
          Text(
            text = hint ?: stringResource(R.string.add_name_hint),
            style = textStyle.copy(color = CaducityTheme.colorScheme.outline),
          )
        }
        // The text can change from outside (suggestion, clear): then the cursor goes to the end.
        var fieldValue by remember { mutableStateOf(TextFieldValue(name, TextRange(name.length))) }
        if (fieldValue.text != name) fieldValue = TextFieldValue(name, TextRange(name.length))
        BasicTextField(
          value = fieldValue,
          onValueChange = {
            fieldValue = it
            onNameChange(it.text)
          },
          singleLine = true,
          textStyle = textStyle,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
          cursorBrush = SolidColor(CaducityTheme.colorScheme.primary),
          interactionSource = interactionSource,
          modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        )
      }
      if (name.isNotEmpty()) {
        IconButton(onClick = onClear, modifier = Modifier.size(48.dp)) {
          Icon(
            imageVector = Symbols.Outlined.Cancel,
            contentDescription = stringResource(R.string.add_name_clear),
            tint = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

/** One history suggestion: the typed part in bold and its category on the right. */
@Composable
fun SuggestionRow(
  suggestion: String,
  typed: String,
  categoryName: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    shape = CaducityShapes.field,
    color = CaducityTheme.colorScheme.surfaceContainer,
    modifier = modifier.fillMaxWidth().height(52.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        imageVector = Symbols.Outlined.History,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = boldMatch(suggestion, typed),
        style = CaducityTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      if (categoryName != null) {
        Text(
          text = categoryName,
          style = CaducityTheme.typography.labelMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
      }
    }
  }
}

private fun boldMatch(
  text: String,
  typed: String,
) = buildAnnotatedString {
  val query = typed.trim()
  val start = if (query.isEmpty()) -1 else text.indexOf(query, ignoreCase = true)
  if (start < 0) {
    append(text)
  } else {
    append(text.substring(0, start))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(start, start + query.length)) }
    append(text.substring(start + query.length))
  }
}

@Composable
fun NewProductNotice(modifier: Modifier = Modifier) {
  Surface(
    shape = CaducityShapes.field,
    color = CaducityTheme.colorScheme.primaryContainer,
    contentColor = CaducityTheme.colorScheme.onPrimaryContainer,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(Symbols.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
      Text(
        text =
          buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(R.string.add_new_product_bold)) }
            append(" ")
            append(stringResource(R.string.add_new_product_rest))
          },
        style = CaducityTheme.typography.bodyMedium,
      )
    }
  }
}
