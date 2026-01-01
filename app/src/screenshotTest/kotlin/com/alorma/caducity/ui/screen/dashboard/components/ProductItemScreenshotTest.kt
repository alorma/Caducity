package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

class ProductItemScreenshotTest {

  @Preview(name = "Product Item - With Multiple Statuses")
  @Composable
  fun ProductItemMultipleStatuses() {
    AppPreview {
      Surface {
        ProductItem(
          product = ProductUiModel.WithInstances(
            id = "1",
            name = "Organic Milk",
            description = "Fresh organic milk",
            today = "2024-01-15",
            instances = listOf(
              ProductInstanceUiModel(
                id = "i1",
                identifier = "Bottle #1",
                status = InstanceStatus.Expired,
                expirationDate = LocalDate(2024, 1, 14),
                expirationDateText = "2024-01-14"
              ),
              ProductInstanceUiModel(
                id = "i2",
                identifier = "Bottle #2",
                status = InstanceStatus.ExpiringSoon,
                expirationDate = LocalDate(2024, 1, 16),
                expirationDateText = "2024-01-16"
              ),
              ProductInstanceUiModel(
                id = "i3",
                identifier = "Bottle #3",
                status = InstanceStatus.Fresh,
                expirationDate = LocalDate(2024, 1, 22),
                expirationDateText = "2024-01-22"
              ),
            ).toImmutableList()
          ),
          onClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Product Item - Expired Only")
  @Composable
  fun ProductItemExpiredOnly() {
    AppPreview {
      Surface {
        ProductItem(
          product = ProductUiModel.WithInstances(
            id = "2",
            name = "Fresh Bread",
            description = "Whole wheat bread",
            today = "2024-01-15",
            instances = listOf(
              ProductInstanceUiModel(
                id = "i1",
                identifier = "Loaf #1",
                status = InstanceStatus.Expired,
                expirationDate = LocalDate(2024, 1, 13),
                expirationDateText = "2024-01-13"
              ),
              ProductInstanceUiModel(
                id = "i2",
                identifier = "Loaf #2",
                status = InstanceStatus.Expired,
                expirationDate = LocalDate(2024, 1, 14),
                expirationDateText = "2024-01-14"
              ),
            ).toImmutableList()
          ),
          onClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Product Item - Fresh Only")
  @Composable
  fun ProductItemFreshOnly() {
    AppPreview {
      Surface {
        ProductItem(
          product = ProductUiModel.WithInstances(
            id = "3",
            name = "Canned Beans",
            description = "Black beans",
            today = "2024-01-15",
            instances = listOf(
              ProductInstanceUiModel(
                id = "i1",
                identifier = "Can #1",
                status = InstanceStatus.Fresh,
                expirationDate = LocalDate(2024, 6, 15),
                expirationDateText = "2024-06-15"
              ),
              ProductInstanceUiModel(
                id = "i2",
                identifier = "Can #2",
                status = InstanceStatus.Fresh,
                expirationDate = LocalDate(2024, 7, 20),
                expirationDateText = "2024-07-20"
              ),
            ).toImmutableList()
          ),
          onClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Product Item - Empty State")
  @Composable
  fun ProductItemEmpty() {
    AppPreview {
      Surface {
        ProductItem(
          product = ProductUiModel.Empty(
            id = "4",
            name = "Greek Yogurt",
            description = "Plain yogurt"
          ),
          onClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }

  @Preview(name = "Product Item - Long Name")
  @Composable
  fun ProductItemLongName() {
    AppPreview {
      Surface {
        ProductItem(
          product = ProductUiModel.WithInstances(
            id = "5",
            name = "Extra Virgin Organic Cold Pressed Olive Oil Premium Quality",
            description = "High quality olive oil",
            today = "2024-01-15",
            instances = listOf(
              ProductInstanceUiModel(
                id = "i1",
                identifier = "Bottle #1",
                status = InstanceStatus.Fresh,
                expirationDate = LocalDate(2024, 12, 31),
                expirationDateText = "2024-12-31"
              ),
            ).toImmutableList()
          ),
          onClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }
  }
}
