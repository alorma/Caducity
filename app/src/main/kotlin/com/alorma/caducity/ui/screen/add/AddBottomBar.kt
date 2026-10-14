package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.theme.CaducityTheme

/** The single «Desa» button, full width, below the form. Disabled while something is missing. */
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
    Button(
      onClick = onSave,
      enabled = state.canSave,
      modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
      Text(stringResource(R.string.add_save), maxLines = 1)
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
