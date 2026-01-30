package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.ItemStatus
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
        // Separate frozen and active items but keep all products (even empty ones)
        // Consumed items are already filtered at the data source level
        val productsWithSeparatedItems = category.products.map { product ->
          val activeItems = product.items.filter { it.status != ItemStatus.Frozen }
          val frozenItems = product.items.filter { it.status == ItemStatus.Frozen }
          product to Pair(activeItems, frozenItems)
        }

        val activeStandaloneItems = category.standaloneItems
          .filter { it.status != ItemStatus.Frozen }

        val frozenStandaloneItems = category.standaloneItems
          .filter { it.status == ItemStatus.Frozen }

        // Map products to DetailProduct with their dated items (including empty products)
        val detailProducts: List<DetailProduct> = productsWithSeparatedItems.map { (product, itemPair) ->
          val (activeItems, frozenItems) = itemPair

          val dates: List<LocalDate> = activeItems
            .map { it.expirationDate.date() }
            .distinct()
            .sorted()

          // Group active items by date and create ProductDatedItems
          val datedItemsList: List<ProductDatedItems> = dates.map { date ->
            val itemsForDate: List<ProductItem> = activeItems
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

          // Map frozen items
          val frozenItemsList: List<ProductItem> = frozenItems.map { item ->
            val name = listOfNotNull(
              item.identifier.takeIf { it.isNotEmpty() }
            ).joinToString(" - ")
            ProductItem(
              id = item.id,
              name = name,
            )
          }

          DetailProduct(
            id = product.product.id,
            name = product.product.name,
            datedItemsGroups = datedItemsList,
            frozenItems = frozenItemsList,
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

        // Map standalone frozen items
        val standaloneFrozenItemsList: List<ProductItem> = frozenStandaloneItems.map { item ->
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
          standaloneFrozenItems = standaloneFrozenItemsList,
        )
      }
    }
  }

  private fun instanceStatus(
    expirationDate: LocalDate,
  ): ItemStatus {
    return ItemStatus.calculateStatus(
      expirationDate = expirationDate.atStartOfDayIn(
        TimeZone.currentSystemDefault()
      ),
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
    )
  }

}
