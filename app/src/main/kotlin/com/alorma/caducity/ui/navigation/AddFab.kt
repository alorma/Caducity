package com.alorma.caducity.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.theme.CaducityShapes

/** The «+» FAB shared by the navigation bar and the category screen: 56 dp, radius 16, vibrant. */
@Composable
fun AddFab(
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = MaterialTheme.colorScheme
  FloatingActionButton(
    onClick = onClick,
    modifier = modifier.size(56.dp),
    shape = CaducityShapes.fab,
    containerColor = colors.primary,
    contentColor = colors.onPrimary,
  ) {
    Icon(
      imageVector = Symbols.Outlined.Add,
      contentDescription = contentDescription,
      modifier = Modifier.size(24.dp),
    )
  }
}
