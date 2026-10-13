package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.screen.transform.TransformEmptyPreview
import com.alorma.caducity.ui.screen.transform.TransformFreeEntryFilledPreview
import com.alorma.caducity.ui.screen.transform.TransformFreeEntryPreview
import com.alorma.caducity.ui.screen.transform.TransformManyIngredientsPreview
import com.alorma.caducity.ui.screen.transform.TransformMultiplePreview
import com.alorma.caducity.ui.screen.transform.TransformSimplePreview
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerNoResultsPreview
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerNothingSelectedPreview
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerSelectionPreview
import com.android.tools.screenshot.PreviewTest

private const val Width = 411
private const val Height = 900
private const val Night = Configuration.UI_MODE_NIGHT_YES

class TransformScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun transformEmpty() {
    TransformEmptyPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun transformSimple() {
    TransformSimplePreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun transformMultiple() {
    TransformMultiplePreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun transformManyIngredients() {
    TransformManyIngredientsPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun transformFreeEntry() {
    TransformFreeEntryPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun transformFreeEntryFilled() {
    TransformFreeEntryFilledPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun pickerSelection() {
    IngredientPickerSelectionPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun pickerNothingSelected() {
    IngredientPickerNothingSelectedPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "en", widthDp = Width, heightDp = Height)
  @Preview(name = "Dark", locale = "en", widthDp = Width, heightDp = Height, uiMode = Night)
  @Composable
  fun pickerNoResults() {
    IngredientPickerNoResultsPreview()
  }
}
