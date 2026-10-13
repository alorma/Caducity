package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.button.connectedButtonShape
import com.alorma.caducity.ui.theme.CaducityTheme

/**
 * The single «Desa» button, full width, below the form. It is only dimmed while something is missing: tapping
 * it still reaches [onSave], which answers with the `Reject` haptic.
 */
@Composable
fun AddBottomBar(
  state: AddState,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    if (state.missing.isNotEmpty()) {
      Text(
        text = missingMessage(state.missing),
        style = CaducityTheme.typography.labelMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
    }
    val scheme = CaducityTheme.colorScheme
    BarButton(
      modifier = Modifier.fillMaxWidth(),
      enabled = state.canSave,
      container = scheme.primary,
      content = scheme.onPrimary,
      onClick = onSave,
    ) {
      Text(stringResource(R.string.add_save), style = CaducityTheme.typography.labelLarge, maxLines = 1)
    }
  }
}

@Composable
private fun BarButton(
  enabled: Boolean,
  container: Color,
  content: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  label: @Composable () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = connectedButtonShape(0, 1),
    color = if (enabled) container else CaducityTheme.colorScheme.surfaceContainerHigh,
    contentColor = if (enabled) content else CaducityTheme.colorScheme.outline,
    modifier = modifier.height(64.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      label()
    }
  }
}

@Composable
private fun missingMessage(missing: List<AddMissing>): String {
  val parts =
    missing.map {
      stringResource(
        when (it) {
          AddMissing.Name -> R.string.add_missing_name
          AddMissing.Category -> R.string.add_missing_category
          AddMissing.Date -> R.string.add_missing_date
        },
      )
    }
  val list =
    if (parts.size == 1) {
      parts.single()
    } else {
      stringResource(R.string.add_missing_and, parts.dropLast(1).joinToString(", "), parts.last())
    }
  return stringResource(if (parts.size == 1) R.string.add_missing_one else R.string.add_missing_many, list)
}
