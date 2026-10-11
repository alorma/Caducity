package com.alorma.caducity.feature.deeplink

import android.os.Parcelable
import com.alorma.caducity.ui.navigation.AppRoute
import com.alorma.caducity.ui.navigation.Category
import com.alorma.caducity.ui.navigation.Home
import kotlinx.parcelize.Parcelize

sealed interface DeepLinkAction : Parcelable {
  /** Tap on the daily reminder. */
  @Parcelize
  data object OpenHome : DeepLinkAction

  @Parcelize
  data class OpenProduct(
    val categoryId: String,
    val productId: String? = null,
  ) : DeepLinkAction

  @Parcelize
  data class OpenCategory(
    val categoryId: String,
  ) : DeepLinkAction
}

/** Destination this action opens; `OpenProduct` keeps working for notifications scheduled before the redesign. */
fun DeepLinkAction.toRoute(): AppRoute =
  when (this) {
    DeepLinkAction.OpenHome -> Home
    is DeepLinkAction.OpenProduct -> Category(id = categoryId, initialProductId = productId)
    is DeepLinkAction.OpenCategory -> Category(id = categoryId)
  }
