package com.alorma.caducity.ui.screen.settings.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.Morph
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Wallpaper
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.colors.tokens
import com.alorma.compose.settings.ui.core.LocalSettingsTileColors

private const val COLUMNS = 3
private val SwatchSize = 56.dp

@get:StringRes
private val ColorPalette.nameRes: Int
  get() =
    when (this) {
      ColorPalette.DYNAMIC -> R.string.palette_dynamic
      ColorPalette.REBOST -> R.string.palette_rebost
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
      ColorPalette.REBOST -> R.string.palette_rebost_help
      ColorPalette.NEVERA -> R.string.palette_nevera_help
      ColorPalette.HORT -> R.string.palette_hort_help
      ColorPalette.FLECA -> R.string.palette_fleca_help
      ColorPalette.BESSA -> R.string.palette_bessa_help
      ColorPalette.CAMP -> R.string.palette_camp_help
    }

/** Targeta «Colors»: capçalera amb la paleta triada i graella de mostres (radiogroup). */
@Composable
fun ColorPaletteSelector(
  selected: ColorPalette,
  palettes: List<ColorPalette>,
  onSelected: (ColorPalette) -> Unit,
  modifier: Modifier = Modifier,
  shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
  val containerColor = LocalSettingsTileColors.current?.containerColor ?: CaducityTheme.colorScheme.surfaceContainer
  val name = stringResource(selected.nameRes)
  val help = stringResource(selected.helpRes)
  val subtitle = if (selected == ColorPalette.DYNAMIC) help else "$name · $help"

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = shapes.shape,
    color = containerColor,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column {
        Text(
          text = stringResource(R.string.settings_colors),
          style = CaducityTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = CaducityTheme.colorScheme.onSurface,
        )
        Text(
          text = subtitle,
          style = CaducityTheme.typography.bodyMedium,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
      Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        palettes.chunked(COLUMNS).forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
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
  val progress by animateFloatAsState(if (selected) 1f else 0f, label = "swatchMorph")
  val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Clover8Leaf) }
  val name = stringResource(palette.nameRes)

  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(24.dp))
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
        .semantics { contentDescription = name }
        .padding(horizontal = 8.dp, vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier =
        Modifier
          .size(SwatchSize)
          .clip(MorphShape(morph, progress)),
      contentAlignment = Alignment.Center,
    ) {
      if (palette == ColorPalette.DYNAMIC) {
        Box(modifier = Modifier.fillMaxSize().background(DynamicSwatchBrush))
        Icon(
          imageVector = if (selected) Symbols.Outlined.Check else Symbols.Outlined.Wallpaper,
          contentDescription = null,
          tint = Color.White,
        )
      } else {
        // Les mostres usen els colors de la seva paleta, en el tema actual (clar/fosc).
        val tokens = palette.tokens(CaducityTheme.isDark)
        Column(modifier = Modifier.fillMaxSize()) {
          Box(modifier = Modifier.weight(1f).fillMaxWidth().background(tokens.primary))
          Box(modifier = Modifier.weight(1f).fillMaxWidth().background(tokens.primaryContainer))
        }
        if (selected) {
          Icon(
            imageVector = Symbols.Outlined.Check,
            contentDescription = null,
            tint = Color.White,
          )
        }
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
