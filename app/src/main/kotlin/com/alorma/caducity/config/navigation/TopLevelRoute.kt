package com.alorma.caducity.config.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.filled.Dashboard
import com.alorma.caducity.base.ui.icons.filled.List
import com.alorma.caducity.base.ui.icons.outlined.Dashboard
import com.alorma.caducity.base.ui.icons.outlined.List
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey {

  @Serializable
  data object Dashboard : TopLevelRoute

  @Serializable
  data class Products(
    val filterType: String,
    val date: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val statuses: List<String>? = null,
  ) : TopLevelRoute {
    companion object {

      operator fun invoke(): Products {
        return Products(filterType = "all")
      }

      fun byDate(date: LocalDate): Products {
        return Products(
          filterType = "date",
          date = date.toString(),
        )
      }

      fun byStatus(statuses: Set<InstanceStatus>): Products {
        return Products(
          filterType = "status",
          statuses = statuses.map { it.toSerializable() },
        )
      }

      private fun InstanceStatus.toSerializable(): String = when (this) {
        is InstanceStatus.Expired -> "expired"
        is InstanceStatus.ExpiringSoon -> "expiringSoon"
        is InstanceStatus.Fresh -> "fresh"
        is InstanceStatus.Frozen -> "frozen"
        is InstanceStatus.Consumed -> "consumed"
      }
    }

    fun toFilter(): ProductsListFilter {
      return when (filterType) {
        "date" -> {
          requireNotNull(date) { "date is required for ByDate filter" }
          ProductsListFilter.ByDate(LocalDate.parse(date))
        }

        "status" -> {
          requireNotNull(statuses) { "statuses is required for ByStatus filter" }
          ProductsListFilter.ByStatus(
            statuses.map { it.toInstanceStatus() }.toSet()
          )
        }

        "dateRange" -> {
          requireNotNull(startDate) { "startDate is required for ByDateRange filter" }
          requireNotNull(endDate) { "endDate is required for ByDateRange filter" }
          ProductsListFilter.ByDateRange(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate),
          )
        }

        "all" -> ProductsListFilter.All
        else -> error("Unknown filter type: $filterType")
      }
    }

    private fun String.toInstanceStatus(): InstanceStatus = when (this) {
      "expired" -> InstanceStatus.Expired
      "expiringSoon" -> InstanceStatus.ExpiringSoon
      "fresh" -> InstanceStatus.Fresh
      "frozen" -> InstanceStatus.Frozen
      "consumed" -> InstanceStatus.Consumed
      else -> error("Unknown status: $this")
    }
  }
}

@Composable
fun TopLevelRoute.selectedIconImageVector() = when (this) {
  TopLevelRoute.Dashboard -> AppIcons.Filled.Dashboard
  is TopLevelRoute.Products -> AppIcons.Filled.List
}

@Composable
fun TopLevelRoute.unSelectedIconImageVector() = when (this) {
  TopLevelRoute.Dashboard -> AppIcons.Outlined.Dashboard
  is TopLevelRoute.Products -> AppIcons.Outlined.List
}

@Composable
fun TopLevelRoute.textLabel() = when (this) {
  TopLevelRoute.Dashboard -> stringResource(R.string.dashboard_screen_title)
  is TopLevelRoute.Products -> stringResource(R.string.products_screen_title)
}

@Suppress("ModifierRequired")
@Composable
fun TopLevelRoute.Icon(
  selected: Boolean,
  modifier: Modifier = Modifier,
) {
  Icon(
    modifier = modifier,
    imageVector = if (selected) {
      selectedIconImageVector()
    } else {
      unSelectedIconImageVector()
    },
    contentDescription = textLabel(),
  )
}

@Suppress("ModifierRequired")
@Composable
fun TopLevelRoute.Label(
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier,
    text = textLabel(),
  )
}