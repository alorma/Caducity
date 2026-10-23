package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.theme.CaducityTheme

/** Under the options: what is still missing before «Desa» is enabled. Nothing is shown when the form is complete. */
@Composable
fun AddMissingNote(
  missing: List<AddMissing>,
  modifier: Modifier = Modifier,
) {
  if (missing.isEmpty()) return
  Text(
    text = missingMessage(missing),
    style = CaducityTheme.typography.labelMedium,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
    modifier = modifier.padding(horizontal = 4.dp),
  )
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
