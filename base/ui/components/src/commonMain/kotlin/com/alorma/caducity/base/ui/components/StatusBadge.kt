package com.alorma.caducity.base.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.CaducityTheme

@Composable
fun StatusBadge(
  text: String,
  containerColor: Color,
  onContainerColor: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .clip(MaterialTheme.shapes.small)
      .background(containerColor.copy(CaducityTheme.dims.dim3))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(containerColor)
    )
    Spacer(modifier = Modifier.width(4.dp))

    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = onContainerColor,
    )
  }
}
