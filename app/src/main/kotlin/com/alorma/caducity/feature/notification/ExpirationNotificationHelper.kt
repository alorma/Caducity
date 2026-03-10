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

  fun showExpiringSoonGroupSummary(summary: NotificationGroupSummary)

  fun showExpiredGroupSummary(summary: NotificationGroupSummary)

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
 * @param groupKey Notification group key derived from the category and status type.
 */
data class NotificationProduct(
  val notificationId: Int,
  val title: String,
  val items: List<NotificationItem>,
  val categoryId: String,
  val productId: String?,
  val groupKey: String,
)

/**
 * Represents the summary notification for a group of per-category notifications.
 *
 * @param notificationId Stable unique ID for the summary notification.
 * @param groupKey The group key shared by all notifications in this group.
 * @param categoryId Used to navigate to the category on tap.
 * @param categoryName Name of the category shown as the notification title.
 * @param count Total number of items across all notifications in the group.
 */
data class NotificationGroupSummary(
  val notificationId: Int,
  val groupKey: String,
  val categoryId: String,
  val categoryName: String,
  val count: Int,
)

data class NotificationItem(
  val identifier: String?,
)
