package com.alorma.caducity.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.ThemeMode

@Composable
fun ThemeSelectionDialog(
  currentTheme: ThemeMode,
  onThemeSelected: (ThemeMode) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = "Choose theme") },
    text = {
      Column {
        ThemeOption(
          text = "Light",
          selected = currentTheme == ThemeMode.LIGHT,
          onClick = { onThemeSelected(ThemeMode.LIGHT) },
        )
        ThemeOption(
          text = "Dark",
          selected = currentTheme == ThemeMode.DARK,
          onClick = { onThemeSelected(ThemeMode.DARK) },
        )
        ThemeOption(
          text = "System",
          selected = currentTheme == ThemeMode.SYSTEM,
          onClick = { onThemeSelected(ThemeMode.SYSTEM) },
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(text = "Cancel")
      }
    },
  )
}

@Composable
private fun ThemeOption(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      selected = selected,
      onClick = onClick,
    )
    Text(
      text = text,
      modifier = Modifier.padding(start = 8.dp),
    )
  }
}
