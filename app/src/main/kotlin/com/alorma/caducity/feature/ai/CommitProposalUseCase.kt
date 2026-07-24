package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import kotlin.time.Instant
import kotlinx.coroutines.flow.first

/**
 * Persists an accepted [GroceryProposal] into the inventory, resolving the
 * target category and product from its [MatchResult].
 *
 * This orchestrates the domain use cases so the assistant ViewModel stays free
 * of create-category / create-product / add-item business logic.
 */
class CommitProposalUseCase(
  private val categoryDataSource: CategoryDataSource,
  private val addItemToCategoryUseCase: AddItemToCategoryUseCase,
  private val createCategoryUseCase: CreateCategoryUseCase,
  private val createProductUseCase: CreateProductUseCase,
) {
  suspend fun commit(
    proposal: GroceryProposal,
    matchResult: MatchResult,
    expirationDate: Instant,
  ): Result<Unit> =
    runCatching {
      val (categoryId, productId) =
        when (matchResult) {
          is MatchResult.Match -> matchResult.category.id to matchResult.product.id
          is MatchResult.CategoryMatch ->
            matchResult.category.id to createProductOrThrow(matchResult.category.id, proposal.productName)
          MatchResult.NoMatch -> {
            val categoryId = resolveCategoryId(proposal.category)
            categoryId to createProductOrThrow(categoryId, proposal.productName)
          }
        }

      repeat(proposal.quantity.coerceAtLeast(1)) {
        addItemToCategoryUseCase
          .addItem(
            categoryId = categoryId,
            identifier = proposal.productName,
            productId = productId,
            expirationDate = expirationDate,
          ).getOrThrow()
      }
    }

  /** Reuse an existing category with the same name (case-insensitive) or create one. */
  private suspend fun resolveCategoryId(categoryName: String): String {
    val existing =
      categoryDataSource
        .getCategories()
        .first()
        .firstOrNull { it.category.name.equals(categoryName, ignoreCase = true) }
    return existing?.category?.id
      ?: createCategoryUseCase
        .createCategory(name = categoryName, description = "", items = emptyList())
        .getOrThrow()
  }

  private suspend fun createProductOrThrow(
    categoryId: String,
    productName: String,
  ): String =
    createProductUseCase
      .create(categoryId = categoryId, name = productName)
      .getOrThrow()
      .id
}
