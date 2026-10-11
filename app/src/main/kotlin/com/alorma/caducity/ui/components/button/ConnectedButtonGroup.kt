package com.alorma.caducity.ui.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme

/**
 * Opció d'un [ConnectedButtonGroup]. `container`/`content` forcen colors (p. ex. Congela en
 * `frozen-container`, Llença en `expired`); sense ells, segueix la selecció.
 */
data class ConnectedButtonOption(
  val label: String,
  val icon: ImageVector? = null,
  val container: Color? = null,
  val content: Color? = null,
)

/** Separació entre botons d'un grup connectat. */
val ConnectedButtonSpacing: Dp = 3.dp

private val Full = CornerSize(50)
private val Joint = CornerSize(8.dp)

/** Extrems totalment arrodonits, cantonades interiors a 8 dp. */
fun connectedButtonShape(
  index: Int,
  count: Int,
): CornerBasedShape =
  when {
    count <= 1 -> RoundedCornerShape(Full)
    index <= 0 ->
      RoundedCornerShape(
        topStart = Full,
        bottomStart = Full,
        topEnd = Joint,
        bottomEnd = Joint,
      )
    index >= count - 1 ->
      RoundedCornerShape(
        topStart = Joint,
        bottomStart = Joint,
        topEnd = Full,
        bottomEnd = Full,
      )
    else -> RoundedCornerShape(Joint)
  }

/**
 * Grup de botons connectats. Amb `selectedIndex` es comporta com a selecció única (primary / surface-high);
 * amb `selectedIndex = null` són accions independents.
 */
@Composable
fun ConnectedButtonGroup(
  options: List<ConnectedButtonOption>,
  selectedIndex: Int?,
  onClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = 48.dp,
) {
  Row(
    modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
    horizontalArrangement = Arrangement.spacedBy(ConnectedButtonSpacing),
  ) {
    options.forEachIndexed { index, option ->
      val selected = index == selectedIndex
      val container =
        option.container
          ?: if (selected) CaducityTheme.colorScheme.primary else CaducityTheme.colorScheme.surfaceContainerHigh
      val content =
        option.content
          ?: if (selected) CaducityTheme.colorScheme.onPrimary else CaducityTheme.colorScheme.onSurface
      val role = if (selectedIndex != null) Role.RadioButton else Role.Button
      Surface(
        selected = selected,
        onClick = { onClick(index) },
        modifier =
          Modifier
            .weight(1f)
            .fillMaxHeight()
            .heightIn(min = height)
            .semantics { this.role = role },
        shape = connectedButtonShape(index, options.size),
        color = container,
        contentColor = content,
      ) {
        ButtonContent(option)
      }
    }
  }
}

@Composable
private fun RowScope.ButtonContent(option: ConnectedButtonOption) {
  Row(
    modifier = Modifier.padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    option.icon?.let {
      Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(6.dp))
    }
    Text(
      text = option.label,
      style = CaducityTheme.typography.labelLarge,
      maxLines = 2,
      textAlign = TextAlign.Center,
    )
  }
}
