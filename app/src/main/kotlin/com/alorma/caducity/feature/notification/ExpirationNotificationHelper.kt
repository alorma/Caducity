package com.alorma.caducity.feature.notification

import androidx.compose.runtime.MutableState
import com.alorma.caducity.config.navigation.ComposeNavigator
import kotlinx.datetime.LocalTime

interface ExpirationNotificationHelper : ComposeNavigator<Any> {
  fun areNotificationsEnabled(): MutableState<Boolean>
  fun setNotificationsEnabled(enabled: Boolean)
  fun hasNotificationPermission(): Boolean
  fun launch()
  fun changeState(enabled: Boolean)
  fun showExpiringSoonNotification(product: NotificationProduct)
  fun showExpiredNotification(product: NotificationProduct)
  fun areExpiredNotificationsEnabled(): MutableState<Boolean>
  fun setExpiredNotificationsEnabled(enabled: Boolean)
  fun areExpiringSoonNotificationsEnabled(): MutableState<Boolean>
  fun setExpiringSoonNotificationsEnabled(enabled: Boolean)
  fun getNotificationTime(): MutableState<LocalTime>
  fun setNotificationTime(time: LocalTime)
}

/**
 * Represents a single product (or standalone item group) to notify about.
 *
 * @param notificationId Stable unique ID derived from the product/category, used as Android notification ID.
 * @param title Product name, or category name for standalone items.
 * @param items Items to describe in the notification body.
 * @param categoryId Used to navigate to the category detail on tap.
 */
data class NotificationProduct(
  val notificationId: Int,
  val title: String,
  val items: List<NotificationItem>,
  val categoryId: String,
)

data class NotificationItem(
  val identifier: String?,
)
