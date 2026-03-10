package com.alorma.caducity.feature.notification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.GetExpiringCategoriesUseCase
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.feature.notification.NotificationGroupSummary
import com.alorma.caducity.feature.notification.NotificationItem
import com.alorma.caducity.feature.notification.NotificationProduct
import timber.log.Timber

/**
 * WorkManager worker that checks for expiring categories and shows notifications.
 * Runs periodically in the background to notify users about categories with items expiring soon.
 *
 * Uses Koin for dependency injection - dependencies are injected via Koin's inject() delegate.
 */
class ExpirationCheckWorker(
  context: Context,
  params: WorkerParameters,
  private val getExpiringCategoriesUseCase: GetExpiringCategoriesUseCase,
  private val notificationHelper: ExpirationNotificationHelper,
) : CoroutineWorker(context, params) {
  companion object {
    private const val TAG = "ExpirationCheckWorker"
    const val WORK_NAME = "expiration_check_work"
  }

  override suspend fun doWork(): Result =
    try {
      Timber.tag(TAG).d("Starting expiration check...")

      val allCategories = getExpiringCategoriesUseCase.load()
      Timber.tag(TAG).d("Found ${allCategories.size} categories with expiring/expired items")

      allCategories.forEach { category ->
        val expiringSoonProducts = buildNotificationProducts(category, ItemStatus.ExpiringSoon)
        expiringSoonProducts.forEach { notificationHelper.showExpiringSoonNotification(it) }
        if (expiringSoonProducts.isNotEmpty()) {
          notificationHelper.showExpiringSoonGroupSummary(
            NotificationGroupSummary(
              notificationId = "${category.category.id}_expiring_soon_summary".hashCode(),
              groupKey = expiringSoonProducts.first().groupKey,
              categoryId = category.category.id,
              categoryName = category.category.name,
              count = expiringSoonProducts.sumOf { it.items.size },
            ),
          )
        }

        val expiredProducts = buildNotificationProducts(category, ItemStatus.Expired)
        expiredProducts.forEach { notificationHelper.showExpiredNotification(it) }
        if (expiredProducts.isNotEmpty()) {
          notificationHelper.showExpiredGroupSummary(
            NotificationGroupSummary(
              notificationId = "${category.category.id}_expired_summary".hashCode(),
              groupKey = expiredProducts.first().groupKey,
              categoryId = category.category.id,
              categoryName = category.category.name,
              count = expiredProducts.sumOf { it.items.size },
            ),
          )
        }
      }

      Result.success()
    } catch (e: Exception) {
      Timber.tag(TAG).e(e)
      Result.retry()
    }

  private fun buildNotificationProducts(
    category: CategoryWithItems,
    status: ItemStatus,
  ): List<NotificationProduct> {
    val groupKey = buildGroupKey(category.category.id, status)
    val results = mutableListOf<NotificationProduct>()

    // One notification per product
    category.products.forEach { categoryProduct ->
      val matchingItems = categoryProduct.items.filter { it.status == status }
      if (matchingItems.isNotEmpty()) {
        results.add(
          NotificationProduct(
            notificationId = (category.category.id + categoryProduct.product.id + status).hashCode(),
            title = categoryProduct.product.name,
            items = matchingItems.map { NotificationItem(it.identifier.takeIf { id -> id.isNotBlank() }) },
            categoryId = category.category.id,
            productId = categoryProduct.product.id,
            groupKey = groupKey,
          ),
        )
      }
    }

    // Standalone items grouped under the category name
    val standaloneItems = category.standaloneItems.filter { it.status == status }
    if (standaloneItems.isNotEmpty()) {
      results.add(
        NotificationProduct(
          notificationId = (category.category.id + "standalone" + status).hashCode(),
          title = category.category.name,
          items = standaloneItems.map { NotificationItem(it.identifier.takeIf { id -> id.isNotBlank() }) },
          categoryId = category.category.id,
          productId = null,
          groupKey = groupKey,
        ),
      )
    }

    return results
  }

  private fun buildGroupKey(
    categoryId: String,
    status: ItemStatus,
  ): String {
    val statusSuffix =
      when (status) {
        ItemStatus.Expired -> "expired"
        ItemStatus.ExpiringSoon -> "expiring_soon"
        else -> "other"
      }
    return "${categoryId}_$statusSuffix"
  }
}
