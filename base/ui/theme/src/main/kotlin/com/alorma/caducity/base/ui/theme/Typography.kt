package com.alorma.caducity.base.ui.theme

import androidx.compose.material3.Typography

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
 *
 * Platform-specific implementation provides the appropriate font family.
 */
expect val caducityTypography: Typography
