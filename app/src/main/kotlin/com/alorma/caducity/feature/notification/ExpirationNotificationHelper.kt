package com.alorma.caducity.feature.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.alorma.caducity.domain.model.ProductWithInstances

/**
 * Interface for showing expiration notifications.
 * Platform-specific implementations handle the notification display.
 */
interface ExpirationNotificationHelper {

  /**
   * Returns a MutableState reflecting whether notifications are enabled.
   * On Android, this checks the system permission state.
   */
  fun areNotificationsEnabled(): MutableState<Boolean>

  /**
   * Attempts to enable or disable notifications.
   * @param enabled Whether to enable notifications
   */
  fun setNotificationsEnabled(enabled: Boolean)

  /**
   * Checks if the app has notification permission without triggering state updates.
   * @return true if notifications are permitted by the system
   */
  fun hasNotificationPermission(): Boolean

  @Composable
  fun registerContract()

  fun launch()

  /**
   * Shows a notification for expiring products.
   *
   * @param expiringProducts List of products that are expiring soon
   */
  fun showExpirationNotification(expiringProducts: List<ProductWithInstances>)

  companion object {

  }
}
