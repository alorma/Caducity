package com.alorma.caducity.ui.components.bottomsheet

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme

private val previewData
  @Composable get() =
    QuantityDialogData(
      name = "Llet sencera",
      shape = CategoryShape.COOKIE,
      icon = CategoryIcons.vectorOf(CategoryIcon.DEFAULT),
      expires = "caduca demà",
      max = 4,
    )

@Composable
private fun QuantityPreview(quantity: Int) {
  PreviewTheme {
    val data = previewData
    QuantityDialogContent(
      data = data,
      state = QuantityState(quantity, data.max),
      onStateChange = {},
      onConfirm = {},
      onCancel = {},
      modifier = Modifier.padding(16.dp),
    )
  }
}

@PreviewDynamicLightDark
@Composable
private fun QuantityMinimumPreview() = QuantityPreview(quantity = 1)

@PreviewDynamicLightDark
@Composable
private fun QuantityIntermediatePreview() = QuantityPreview(quantity = 2)

@PreviewDynamicLightDark
@Composable
private fun QuantityMaximumPreview() = QuantityPreview(quantity = 4)
