package com.alorma.caducity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Desktop platform color schemes.
 * Uses the base light and dark schemes from Color.kt
 */
@Composable
actual fun platformLightColorScheme(): ColorScheme = lightScheme

@Composable
actual fun platformDarkColorScheme(): ColorScheme = darkScheme
