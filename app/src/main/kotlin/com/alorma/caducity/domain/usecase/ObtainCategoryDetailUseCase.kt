package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.DetailProduct
import com.alorma.caducity.domain.model.CategoryDetail
import com.alorma.caducity.domain.model.ProductDatedItems
import com.alorma.caducity.domain.model.ProductItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ObtainCategoryDetailUseCase(
  private val appClock: AppClock,
  private val categoryDataSource: CategoryDataSource,
  private val expirationThresholds: ExpirationThresholds,
) {

  fun obtain(categoryId: String): Flow<Result<CategoryDetail>> {
    return categoryDataSource.getCategory(categoryId).map { result ->
      result.map { category ->
        // Filter out frozen items but keep all products (even empty ones)
        // Consumed items are already filtered at the data source level
        val activeProducts = category.products.map { product ->
          product.copy(
            items = product.items
              .filter { it.status != InstanceStatus.Frozen }
              .toImmutableList()
          )
        }

        val activeStandaloneItems = category.standaloneItems
          .filter { it.status != InstanceStatus.Frozen }

        // Map products to DetailProduct with their dated items (including empty products)
        val detailProducts: List<DetailProduct> = activeProducts.map { product ->
          val dates: List<LocalDate> = product.items
            .map { it.expirationDate.date() }
            .distinct()
            .sorted()

          // Group items by date and create ProductDatedItems
          val datedItemsList: List<ProductDatedItems> = dates.map { date ->
            val itemsForDate: List<ProductItem> = product.items
              .filter { it.expirationDate.date() == date }
              .map { item ->
                val name = listOfNotNull(
                  item.identifier.takeIf { it.isNotEmpty() }
                ).joinToString(" - ")
                ProductItem(
                  id = item.id,
                  name = name,
                )
              }

            ProductDatedItems(
              date = date,
              status = instanceStatus(date),
              items = itemsForDate,
            )
          }

          DetailProduct(
            id = product.product.id,
            name = product.product.name,
            datedItemsGroups = datedItemsList,
          )
        }

        // Map standalone items (non-product items)
        val standaloneItemsList: List<ProductItem> = activeStandaloneItems.map { item ->
          val name = listOfNotNull(
            item.identifier.takeIf { it.isNotEmpty() }
          ).joinToString(" - ")
          ProductItem(
            id = item.id,
            name = name,
          )
        }

        CategoryDetail(
          category = category.category,
          products = detailProducts,
          standaloneItems = standaloneItemsList,
        )
      }
    }
  }

  private fun instanceStatus(
    expirationDate: LocalDate,
  ): InstanceStatus {
    return InstanceStatus.calculateStatus(
      expirationDate = expirationDate.atStartOfDayIn(
        TimeZone.currentSystemDefault()
      ),
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
    )
  }

}
