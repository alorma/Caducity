package com.alorma.caducity.base.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material3 Expressive Typography Scale for Caducity
 *
 * This typography system follows Material3 Expressive design guidelines,
 * providing enhanced visual hierarchy and personality to the app.
 *
 * Typography Scale:
 * - Display: Large, impactful text for marketing or hero sections (57-45sp)
 * - Headline: Prominent text for page titles and section headers (36-24sp)
 * - Title: Emphasis text for card titles and important labels (22-14sp)
 * - Body: Main content text (16-12sp)
 * - Label: UI elements like buttons and captions (14-11sp)
 */
val caducityTypography = Typography(
  // Display styles - Reserved for very large, impactful text
  displayLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
  ),
  displayMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
  ),
  displaySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
  ),

  // Headline styles - For page titles and major section headers
  headlineLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
  ),
  headlineSmall = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
  ),

  // Title styles - For card titles, dialog titles, and subsection headers
  titleLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
  ),
  titleSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),

  // Body styles - For main content and reading text
  bodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  ),
  bodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  bodySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),

  // Label styles - For buttons, tabs, captions, and UI elements
  labelLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)
