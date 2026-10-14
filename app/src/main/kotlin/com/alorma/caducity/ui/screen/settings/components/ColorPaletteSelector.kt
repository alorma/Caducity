package com.alorma.caducity.ui.screen.settings.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.Info
import com.alorma.caducity.base.ui.icons.symbols.Palette
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Wallpaper
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.colors.tokens

private const val COLUMNS = 4
private val SwatchSize = 56.dp
private val RowSwatchSize = 28.dp

@get:StringRes
private val ColorPalette.nameRes: Int
  get() =
    when (this) {
      ColorPalette.DYNAMIC -> R.string.palette_dynamic
      ColorPalette.REBOST -> R.string.palette_caducity
      ColorPalette.NEVERA -> R.string.palette_nevera
      ColorPalette.HORT -> R.string.palette_hort
      ColorPalette.FLECA -> R.string.palette_fleca
      ColorPalette.BESSA -> R.string.palette_bessa
      ColorPalette.CAMP -> R.string.palette_camp
    }

@get:StringRes
private val ColorPalette.helpRes: Int
  get() =
    when (this) {
      ColorPalette.DYNAMIC -> R.string.palette_dynamic_help
      ColorPalette.REBOST -> R.string.palette_caducity_help
      ColorPalette.NEVERA -> R.string.palette_nevera_help
      ColorPalette.HORT -> R.string.palette_hort_help
      ColorPalette.FLECA -> R.string.palette_fleca_help
      ColorPalette.BESSA -> R.string.palette_bessa_help
      ColorPalette.CAMP -> R.string.palette_camp_help
    }

/** Fila «Colors» d'Ajustos: nom de la paleta i una mostra petita; obre el full de colors. */
@Composable
fun ColorPaletteRow(
  selected: ColorPalette,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
  val name = stringResource(selected.nameRes)
  val help = stringResource(selected.helpRes)
  StyledSettingsCard(
    modifier = modifier,
    icon = { Icon(Symbols.Outlined.Palette, contentDescription = null) },
    title = stringResource(R.string.settings_colors),
    subtitle = if (selected == ColorPalette.DYNAMIC) help else "$name · $help",
    action = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Swatch(palette = selected, size = RowSwatchSize)
        Icon(Symbols.Outlined.ChevronRight, contentDescription = null)
      }
    },
    onClick = onClick,
    shapes = shapes,
  )
}

/**
 * Contingut del full «Colors»: graella de 4 columnes amb una mostra per paleta (radiogroup) i la nota que els colors
 * dels estats no canvien. Tocar una mostra aplica la paleta a l'instant; el full es queda obert.
 */
@Composable
fun ColorPaletteSelector(
  selected: ColorPalette,
  palettes: List<ColorPalette>,
  onSelected: (ColorPalette) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = stringResource(R.string.settings_colors),
        style = CaducityTheme.typography.headlineSmall,
        color = CaducityTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() },
      )
      Text(
        text = stringResource(R.string.settings_colors_sheet_subtitle),
        style = CaducityTheme.typography.bodyMedium,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
    }
    Column(
      modifier = Modifier.selectableGroup(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      palettes.chunked(COLUMNS).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth()) {
          row.forEach { palette ->
            ColorPaletteSwatch(
              palette = palette,
              selected = palette == selected,
              onClick = { onSelected(palette) },
              modifier = Modifier.weight(1f),
            )
          }
          repeat(COLUMNS - row.size) { Box(modifier = Modifier.weight(1f)) }
        }
      }
    }
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = CaducityTheme.colorScheme.surfaceContainer),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          Symbols.Outlined.Info,
          contentDescription = null,
          tint = CaducityTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = stringResource(R.string.settings_colors_note),
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ColorPaletteSwatch(
  palette: ColorPalette,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val name = stringResource(palette.nameRes)
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(24.dp))
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
        .semantics { contentDescription = name }
        .padding(horizontal = 4.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    // The chosen one gets a ring around it and a check.
    Box(
      modifier =
        Modifier
          .size(SwatchSize + 8.dp)
          .then(
            if (selected) Modifier.border(3.dp, CaducityTheme.colorScheme.primary, CircleShape) else Modifier,
          ).padding(4.dp),
      contentAlignment = Alignment.Center,
    ) {
      Swatch(palette = palette, size = SwatchSize)
      if (selected || palette == ColorPalette.DYNAMIC) {
        Icon(
          imageVector = if (selected) Symbols.Outlined.Check else Symbols.Outlined.Wallpaper,
          contentDescription = null,
          tint = Color.White,
        )
      }
    }
    Text(
      text = name,
      fontSize = 13.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
      textAlign = TextAlign.Center,
      maxLines = 1,
      color = CaducityTheme.colorScheme.onSurface,
    )
  }
}

/** A circle half dark (primary) and half light (primary container) in the palette's colors; Dynamic is a gradient. */
@Composable
private fun Swatch(
  palette: ColorPalette,
  size: androidx.compose.ui.unit.Dp,
) {
  Box(modifier = Modifier.size(size).clip(CircleShape)) {
    if (palette == ColorPalette.DYNAMIC) {
      Box(modifier = Modifier.fillMaxSize().background(DynamicSwatchBrush))
    } else {
      // Les mostres usen els colors de la seva paleta, en el tema actual (clar/fosc).
      val tokens = palette.tokens(CaducityTheme.isDark)
      Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(tokens.primary))
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(tokens.primaryContainer))
      }
    }
  }
}

private val DynamicSwatchBrush =
  Brush.sweepGradient(
    listOf(
      Color(0xFFEA4335),
      Color(0xFFFBBC05),
      Color(0xFF34A853),
      Color(0xFF4285F4),
      Color(0xFF9B59B6),
      Color(0xFFEA4335),
    ),
  )
