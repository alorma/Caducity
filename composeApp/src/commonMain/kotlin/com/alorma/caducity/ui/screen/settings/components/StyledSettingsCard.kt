package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.alorma.compose.settings.ui.SettingsMenuLink

@Composable
fun StyledSettingsCard(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  position: CardPosition = CardPosition.Single,
  icon: (@Composable () -> Unit)? = null,
  action: (@Composable () -> Unit)? = null,
) {
  SettingsMenuLink(
    modifier = modifier.fillMaxWidth(),
    shape = position.toShape(),
    icon = icon,
    title = {
      Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
      )
    },
    subtitle = if (subtitle != null
    ) {
      { Text(text = subtitle) }
    } else {
      null
    },
    action = action,
    onClick = onClick,
  )
}