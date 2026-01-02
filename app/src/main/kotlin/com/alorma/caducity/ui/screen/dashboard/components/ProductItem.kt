package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ProductItem(
  product: ProductUiModel,
  onClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Expressive spring animation for press effect
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.97f else 1f,
    animationSpec = spring(
      dampingRatio = 0.6f,
      stiffness = 400f,
    ),
    label = "card_press_scale"
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .then(modifier),
    colors = CardDefaults.cardColors(
      containerColor = CaducityTheme.colorScheme.surfaceContainer,
    ),
    shape = MaterialTheme.shapes.largeIncreased,
    elevation = CardDefaults.cardElevation(
      defaultElevation = 2.dp,
      pressedElevation = 4.dp,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(
          interactionSource = interactionSource,
          indication = null,
        ) { onClick(product.id) }
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = product.name,
        style = MaterialTheme.typography.headlineMedium,
        color = CaducityTheme.colorScheme.onSurface,
      )

      when (product) {
        is ProductUiModel.WithInstances -> {
          CollapsedInstancesView(product.instances)
        }

        is ProductUiModel.Empty -> {
          Text(
            text = "No active instances",
            style = MaterialTheme.typography.bodySmall,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun CollapsedInstancesView(
  instances: ImmutableList<ProductInstanceUiModel>,
) {
  // Group instances by status and count them
  val statusCounts = remember {
    derivedStateOf {
      instances.groupBy { it.status }
        .mapValues { it.value.size }
    }
  }

  // Sort by status priority: Expired, ExpiringSoon, Fresh
  val orderedStatuses = remember {
    derivedStateOf {
      listOf(
        InstanceStatus.Expired,
        InstanceStatus.ExpiringSoon,
        InstanceStatus.Fresh
      ).filter { statusCounts.value.containsKey(it) }
    }
  }

  Row(
    modifier = Modifier.clip(MaterialTheme.shapes.small),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    orderedStatuses.value.forEach { status ->
      // Safe to use getValue() because orderedStatuses is filtered by statusCounts.containsKey()
      val count = statusCounts.value.getValue(status)
      val colors = ExpirationDefaults.getColors(status)

      Box(
        modifier = Modifier
          .sizeIn(24.dp)
          .background(colors.container)
          .padding(8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = count.toString(),
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
          ),
          color = colors.onContainer,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Preview
@Composable
private fun ProductItemPreview() {
  AppPreview {
    Surface {
      ProductItem(
        product = productWithInstancesPreview,
        onClick = {},
      )
    }
  }
}
