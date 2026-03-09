package com.alorma.caducity.feature.notification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.GetExpiringCategoriesUseCase
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
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
        buildNotificationProducts(category, ItemStatus.ExpiringSoon).forEach {
          notificationHelper.showExpiringSoonNotification(it)
        }
        buildNotificationProducts(category, ItemStatus.Expired).forEach {
          notificationHelper.showExpiredNotification(it)
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
        ),
      )
    }

    return results
  }
}
