package com.alorma.caducity.data.backup

import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.normalizeForSearch
import java.util.UUID

/**
 * Brings a v1 backup to the v2 shape, applying the same rules as `MIGRATION_2_3`: category shape/icon/sortOrder
 * from the name, standalone items with an `identifier` become (or join) a product, and `identifier` is cleared
 * for every item that has a product. A backup that is already v2 is returned as is.
 */
fun BackupData.upgradeToV2(newId: () -> String = { UUID.randomUUID().toString() }): BackupData {
  if (version >= 2) return this

  val upgraded =
    categories.mapIndexed { index, category ->
      val defaults = CategoryDefaults.forName(category.name, index)

      val productsByName =
        category.products
          .map { product -> product.copy(items = product.items.map { it.copy(identifier = "") }) }
          .toMutableList()
      val idsByName =
        productsByName
          .withIndex()
          .associate { (i, product) ->
            product.name.normalizeForSearch() to i
          }.toMutableMap()

      val (named, unnamed) = category.standaloneItems.partition { it.identifier.isNotEmpty() }
      named.groupBy { it.identifier.normalizeForSearch() }.forEach { (key, group) ->
        val cleared = group.map { it.copy(identifier = "") }
        val existing = idsByName[key]
        if (existing != null) {
          val product = productsByName[existing]
          productsByName[existing] = product.copy(items = product.items + cleared)
        } else {
          idsByName[key] = productsByName.size
          productsByName +=
            BackupProduct(
              id = newId(),
              categoryId = category.id,
              name = group.first().identifier.trim(),
              createdAt = group.minOf { it.expirationDate },
              items = cleared,
            )
        }
      }

      category.copy(
        shape = defaults.shape.name,
        icon = defaults.icon.id,
        sortOrder = index,
        products = productsByName,
        standaloneItems = unnamed,
      )
    }

  return copy(version = BackupData.CURRENT_VERSION, categories = upgraded)
}
