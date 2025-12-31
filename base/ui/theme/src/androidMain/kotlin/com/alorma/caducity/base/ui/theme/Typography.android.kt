package com.alorma.caducity.base.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

val TYPOGRAPHY = Typography()

actual val caducityTypography = Typography(
  displayLarge = TYPOGRAPHY.displayLarge.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  ),
  displayMedium = TYPOGRAPHY.displayMedium.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  ),
  displaySmall = TYPOGRAPHY.displaySmall.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  ),
  headlineLarge = TYPOGRAPHY.headlineLarge.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02, dlig"
  ),
  headlineMedium = TYPOGRAPHY.headlineMedium.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02, dlig"
  ),
  headlineSmall = TYPOGRAPHY.headlineSmall.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02, dlig"
  ),
  titleLarge = TYPOGRAPHY.titleLarge.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02, dlig"
  ),
  titleMedium = TYPOGRAPHY.titleMedium.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02, dlig"
  ),
  titleSmall = TYPOGRAPHY.titleSmall.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02, dlig"
  ),
  bodyLarge = TYPOGRAPHY.bodyLarge.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  ),
  bodyMedium = TYPOGRAPHY.bodyMedium.copy(
    fontFamily = AppFonts.googleFlex400,
    fontFeatureSettings = "ss02, dlig"
  ),
  bodySmall = TYPOGRAPHY.bodySmall.copy(
    fontFamily = AppFonts.googleFlex400,
    fontFeatureSettings = "ss02, dlig"
  ),
  labelLarge = TYPOGRAPHY.labelLarge.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  ),
  labelMedium = TYPOGRAPHY.labelMedium.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  ),
  labelSmall = TYPOGRAPHY.labelSmall.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02, dlig"
  )
)

@OptIn(ExperimentalTextApi::class)
object AppFonts {
  val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))

  val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))

  val roboto = FontFamily(Font(R.font.roboto_flex_logo))
}