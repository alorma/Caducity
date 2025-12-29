package com.alorma.caducity.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A styled settings group that wraps content with proper spacing.
 * Use this to group related settings items together.
 *
 * @param modifier Optional modifier for the group
 * @param content The settings items to display within the group
 */
@Composable
fun StyledSettingsGroup(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(2.dp),
    content = content
  )
}
