package com.alorma.caducity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.alorma.caducity.R

val TYPOGRAPHY = Typography()

val caducityTypography = Typography(
  displayLarge = TYPOGRAPHY.displayLarge.copy(
    fontFamily = AppFonts.googleFlex600,
  ),
  displayMedium = TYPOGRAPHY.displayMedium.copy(
    fontFamily = AppFonts.googleFlex600,
  ),
  displaySmall = TYPOGRAPHY.displaySmall.copy(
    fontFamily = AppFonts.googleFlex600,
  ),
  headlineLarge = TYPOGRAPHY.headlineLarge.copy(
    fontFamily = AppFonts.roboto,
  ),
  headlineMedium = TYPOGRAPHY.headlineMedium.copy(
    fontFamily = AppFonts.roboto,
  ),
  headlineSmall = TYPOGRAPHY.headlineSmall.copy(
    fontFamily = AppFonts.roboto,
  ),
  titleLarge = TYPOGRAPHY.titleLarge.copy(
    fontFamily = AppFonts.roboto,
  ),
  titleMedium = TYPOGRAPHY.titleMedium.copy(
    fontFamily = AppFonts.roboto,
  ),
  titleSmall = TYPOGRAPHY.titleSmall.copy(
    fontFamily = AppFonts.roboto,
  ),
  bodyLarge = TYPOGRAPHY.bodyLarge.copy(
    fontFamily = AppFonts.googleFlex400,
  ),
  bodyMedium = TYPOGRAPHY.bodyMedium.copy(
    fontFamily = AppFonts.googleFlex400,
  ),
  bodySmall = TYPOGRAPHY.bodySmall.copy(
    fontFamily = AppFonts.googleFlex400,
  ),
  labelLarge = TYPOGRAPHY.labelLarge.copy(
    fontFamily = AppFonts.googleFlex600,
  ),
  labelMedium = TYPOGRAPHY.labelMedium.copy(
    fontFamily = AppFonts.googleFlex600,
  ),
  labelSmall = TYPOGRAPHY.labelSmall.copy(
    fontFamily = AppFonts.googleFlex600,
  )
)

object AppFonts {
  val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))
  val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))
  val roboto = FontFamily(Font(R.font.roboto_flex))
}