package com.alorma.caducity.base.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Google Sans font family using variable fonts from Android resources.
 * The variable fonts support all weights dynamically.
 */
private val appFontFamily = FontFamily(
  Font(R.font.googlesans, FontWeight.Normal, FontStyle.Normal),
  Font(R.font.googlesans_italic, FontWeight.Normal, FontStyle.Italic),
  Font(R.font.googlesans, FontWeight.Medium, FontStyle.Normal),
  Font(R.font.googlesans_italic, FontWeight.Medium, FontStyle.Italic),
  Font(R.font.googlesans, FontWeight.SemiBold, FontStyle.Normal),
  Font(R.font.googlesans_italic, FontWeight.SemiBold, FontStyle.Italic),
  Font(R.font.googlesans, FontWeight.Bold, FontStyle.Normal),
  Font(R.font.googlesans_italic, FontWeight.Bold, FontStyle.Italic),
)

/**
 * Material3 Expressive Typography Scale for Caducity - Android Implementation
 */
actual val caducityTypography = Typography(
  // Display styles - Reserved for very large, impactful text
  displayLarge = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
  ),
  displaySmall = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
  ),

  // Headline styles - For page titles and major section headers
  headlineLarge = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
  ),

  // Title styles - For card titles, dialog titles, and subsection headers
  titleLarge = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),

  // Body styles - For main content and reading text
  bodyLarge = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),

  // Label styles - For buttons, tabs, captions, and UI elements
  labelLarge = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)
