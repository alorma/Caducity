package com.alorma.caducity.feature.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** POST_NOTIFICATIONS only exists from Android 13; before that, notifications are on unless blocked in settings. */
fun Context.hasNotificationPermission(): Boolean =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
      PackageManager.PERMISSION_GRANTED
  } else {
    NotificationManagerCompat.from(this).areNotificationsEnabled()
  }
