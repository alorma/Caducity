package com.alorma.caducity.feature.deeplink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface DeepLinkAction : Parcelable {
  @Parcelize
  data class OpenProduct(
    val categoryId: String,
    val productId: String? = null,
  ) : DeepLinkAction
}
