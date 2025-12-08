package com.alorma.caducity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Provides platform-specific color schemes.
 * Each platform can override the base light and dark schemes with their own implementations.
 */
@Composable
expect fun platformLightColorScheme(): ColorScheme

@Composable
expect fun platformDarkColorScheme(): ColorScheme
