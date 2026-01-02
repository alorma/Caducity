package com.alorma.caducity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.alorma.caducity.R

val TYPOGRAPHY = Typography()

val caducityTypography = Typography(
  displayLarge = TYPOGRAPHY.displayLarge.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  ),
  displayMedium = TYPOGRAPHY.displayMedium.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  ),
  displaySmall = TYPOGRAPHY.displaySmall.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  ),
  headlineLarge = TYPOGRAPHY.headlineLarge.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02"
  ),
  headlineMedium = TYPOGRAPHY.headlineMedium.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02"
  ),
  headlineSmall = TYPOGRAPHY.headlineSmall.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02"
  ),
  titleLarge = TYPOGRAPHY.titleLarge.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02"
  ),
  titleMedium = TYPOGRAPHY.titleMedium.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02"
  ),
  titleSmall = TYPOGRAPHY.titleSmall.copy(
    fontFamily = AppFonts.roboto,
    fontFeatureSettings = "ss02"
  ),
  bodyLarge = TYPOGRAPHY.bodyLarge.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  ),
  bodyMedium = TYPOGRAPHY.bodyMedium.copy(
    fontFamily = AppFonts.googleFlex400,
    fontFeatureSettings = "ss02"
  ),
  bodySmall = TYPOGRAPHY.bodySmall.copy(
    fontFamily = AppFonts.googleFlex400,
    fontFeatureSettings = "ss02"
  ),
  labelLarge = TYPOGRAPHY.labelLarge.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  ),
  labelMedium = TYPOGRAPHY.labelMedium.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  ),
  labelSmall = TYPOGRAPHY.labelSmall.copy(
    fontFamily = AppFonts.googleFlex600,
    fontFeatureSettings = "ss02"
  )
)

@OptIn(ExperimentalTextApi::class)
object AppFonts {
  val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))

  val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))

  val roboto = FontFamily(Font(R.font.roboto_flex_logo))
}