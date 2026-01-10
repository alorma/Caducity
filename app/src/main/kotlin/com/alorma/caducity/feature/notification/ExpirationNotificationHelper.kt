package com.alorma.caducity.feature.notification

import androidx.compose.runtime.MutableState
import com.alorma.caducity.config.navigation.ComposeNavigator
import com.alorma.caducity.domain.model.ProductWithInstances

interface ExpirationNotificationHelper : ComposeNavigator<Any> {
  fun areNotificationsEnabled(): MutableState<Boolean>
  fun setNotificationsEnabled(enabled: Boolean)
  fun hasNotificationPermission(): Boolean
  fun launch()
  fun changeState(enabled: Boolean)
  fun showExpirationNotification(expiringProducts: List<ProductWithInstances>)
}
