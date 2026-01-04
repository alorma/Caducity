package com.alorma.caducity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.alorma.caducity.R

val TYPOGRAPHY = Typography()

fun caducityTypography(useAppFonts: Boolean) = Typography(
  displayLarge = TYPOGRAPHY.displayLarge.copy(
    fontFamily = AppFonts.googleFlex600.takeIf { useAppFonts },
  ),
  displayMedium = TYPOGRAPHY.displayMedium.copy(
    fontFamily = AppFonts.googleFlex600.takeIf { useAppFonts },
  ),
  displaySmall = TYPOGRAPHY.displaySmall.copy(
    fontFamily = AppFonts.googleFlex600.takeIf { useAppFonts },
  ),
  headlineLarge = TYPOGRAPHY.headlineLarge.copy(
    fontFamily = AppFonts.roboto.takeIf { useAppFonts },
  ),
  headlineMedium = TYPOGRAPHY.headlineMedium.copy(
    fontFamily = AppFonts.roboto.takeIf { useAppFonts },
  ),
  headlineSmall = TYPOGRAPHY.headlineSmall.copy(
    fontFamily = AppFonts.roboto.takeIf { useAppFonts },
  ),
  titleLarge = TYPOGRAPHY.titleLarge.copy(
    fontFamily = AppFonts.roboto.takeIf { useAppFonts },
  ),
  titleMedium = TYPOGRAPHY.titleMedium.copy(
    fontFamily = AppFonts.roboto.takeIf { useAppFonts },
  ),
  titleSmall = TYPOGRAPHY.titleSmall.copy(
    fontFamily = AppFonts.roboto.takeIf { useAppFonts },
  ),
  bodyLarge = TYPOGRAPHY.bodyLarge.copy(
    fontFamily = AppFonts.googleFlex400.takeIf { useAppFonts },
  ),
  bodyMedium = TYPOGRAPHY.bodyMedium.copy(
    fontFamily = AppFonts.googleFlex400.takeIf { useAppFonts },
  ),
  bodySmall = TYPOGRAPHY.bodySmall.copy(
    fontFamily = AppFonts.googleFlex400.takeIf { useAppFonts },
  ),
  labelLarge = TYPOGRAPHY.labelLarge.copy(
    fontFamily = AppFonts.googleFlex600.takeIf { useAppFonts },
  ),
  labelMedium = TYPOGRAPHY.labelMedium.copy(
    fontFamily = AppFonts.googleFlex600.takeIf { useAppFonts },
  ),
  labelSmall = TYPOGRAPHY.labelSmall.copy(
    fontFamily = AppFonts.googleFlex600.takeIf { useAppFonts },
  )
)

object AppFonts {
  val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))
  val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))
  val roboto = FontFamily(Font(R.font.roboto_flex))
}