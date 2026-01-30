package com.alorma.caducity.config.resources

import android.content.Context
import androidx.annotation.StringRes

class StringProvider(private val context: Context) {
  fun getString(@StringRes resId: Int): String {
    return context.getString(resId)
  }
}