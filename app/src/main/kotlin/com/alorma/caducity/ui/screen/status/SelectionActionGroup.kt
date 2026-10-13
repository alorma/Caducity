package com.alorma.caducity.ui.screen.status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.shape.connectedPosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.theme.CaducityTheme

/** One button of [SelectionActionGroup]; [description] carries the count for accessibility. */
data class SelectionAction(
  val icon: ImageVector,
  val label: String,
  val description: String,
  val container: Color,
  val content: Color,
  val onClick: () -> Unit,
)

/** Connected group of buttons (icon over text, 68 dp) for the bottom bar of a selection. */
@Composable
fun SelectionActionGroup(
  actions: List<SelectionAction>,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
    actions.forEachIndexed { index, action ->
      Surface(
        color = action.container,
        contentColor = action.content,
        shape = connectedPosition(index, actions.size).toHorizontalShape(),
        modifier =
          Modifier
            .weight(1f)
            .height(68.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .semantics { contentDescription = action.description },
      ) {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(
                68.dp,
              ).clickable(enabled = enabled, role = Role.Button, onClick = action.onClick),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(imageVector = action.icon, contentDescription = null, modifier = Modifier.size(24.dp))
          Text(
            text = action.label,
            style = CaducityTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}
