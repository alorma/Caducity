package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.alorma.compose.settings.ui.SettingsSwitch

@Composable
fun StyledSettingsSwitchCard(
  title: String,
  state: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  position: CardPosition = CardPosition.Single,
  subtitle: String? = null,
  icon: (@Composable () -> Unit)? = null
) {
  SettingsSwitch(
    modifier = modifier.fillMaxWidth(),
    shape = position.toShape(),
    icon = icon,
    title = {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
      )
    },
    subtitle = subtitle?.let { { Text(text = it) } },
    state = state,
    onCheckedChange = onCheckedChange,
  )
}
