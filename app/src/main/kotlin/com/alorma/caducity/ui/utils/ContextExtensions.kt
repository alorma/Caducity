package com.alorma.caducity.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Extension function to find the Activity from a Context.
 * Useful for Compose when we need to get the Activity from LocalContext.
 */
fun Context.findActivity(): Activity? {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) return context
    context = context.baseContext
  }
  return null
}
