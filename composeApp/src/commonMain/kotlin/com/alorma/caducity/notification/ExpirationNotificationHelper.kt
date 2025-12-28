package com.alorma.caducity.notification

import com.alorma.caducity.domain.model.ProductWithInstances

/**
 * Interface for showing expiration notifications.
 * Platform-specific implementations handle the actual notification display.
 */
interface ExpirationNotificationHelper {
  /**
   * Shows a notification for expiring products.
   *
   * @param expiringProducts List of products that are expiring soon
   */
  fun showExpirationNotification(expiringProducts: List<ProductWithInstances>)

  companion object {
    /**
     * Intent extra key for filtering dashboard to show only expiring products.
     */
    const val EXTRA_SHOW_EXPIRING_ONLY = "show_expiring_only"
  }
}
