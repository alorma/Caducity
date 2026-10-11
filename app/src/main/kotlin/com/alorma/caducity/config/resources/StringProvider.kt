package com.alorma.caducity.config.resources

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class StringProvider(
  private val context: Context,
) {
  fun getString(
    @StringRes resId: Int,
  ): String = context.getString(resId)

  fun getString(
    @StringRes resId: Int,
    vararg formatArgs: Any,
  ): String = context.getString(resId, *formatArgs)

  fun getQuantityString(
    @PluralsRes resId: Int,
    quantity: Int,
    vararg formatArgs: Any,
  ): String = context.resources.getQuantityString(resId, quantity, *formatArgs)
}
