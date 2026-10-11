package com.alorma.caducity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alorma.caducity.R

object AppFonts {
  /** Bricolage Grotesque amb `wdth` 80: ulls amples, caixa estreta. */
  val bricolage =
    FontFamily(
      bricolageFont(FontWeight.Bold),
      bricolageFont(FontWeight.ExtraBold),
    )

  val figtree =
    FontFamily(
      figtreeFont(FontWeight.Normal),
      figtreeFont(FontWeight.SemiBold),
      figtreeFont(FontWeight.Bold),
    )

  @OptIn(ExperimentalTextApi::class)
  private fun bricolageFont(weight: FontWeight) =
    Font(
      resId = R.font.bricolage_grotesque,
      weight = weight,
      variationSettings =
        FontVariation.Settings(
          FontVariation.weight(weight.weight),
          FontVariation.width(80f),
        ),
    )

  @OptIn(ExperimentalTextApi::class)
  private fun figtreeFont(weight: FontWeight) =
    Font(
      resId = R.font.figtree,
      weight = weight,
      variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )
}

private fun display(
  size: Int,
  line: Int,
  weight: FontWeight,
) = TextStyle(
  fontFamily = AppFonts.bricolage,
  fontWeight = weight,
  fontSize = size.sp,
  lineHeight = line.sp,
)

private fun text(
  size: Int,
  line: Int,
  weight: FontWeight,
) = TextStyle(
  fontFamily = AppFonts.figtree,
  fontWeight = weight,
  fontSize = size.sp,
  lineHeight = line.sp,
)

/** Estil `number` (xifres del resum): no té rol a `Typography`. */
val NumberTextStyle = display(30, 30, FontWeight.ExtraBold)

/** `overline`: l'estil porta l'espaiat; el text s'ha de passar en majúscules. */
val OverlineTextStyle = text(12, 16, FontWeight.Bold).copy(letterSpacing = 1.2.sp)

// Escala: docs/redisseny/04-especificacio/03-design-system-compose.md
fun caducityTypography(): Typography =
  Typography(
    displayLarge = display(44, 44, FontWeight.ExtraBold),
    displayMedium = display(44, 44, FontWeight.ExtraBold),
    displaySmall = display(44, 44, FontWeight.ExtraBold),
    headlineLarge = display(40, 42, FontWeight.ExtraBold),
    headlineMedium = display(36, 36, FontWeight.ExtraBold),
    headlineSmall = display(24, 28, FontWeight.Bold),
    titleLarge = display(22, 28, FontWeight.Bold),
    titleMedium = display(20, 22, FontWeight.Bold),
    titleSmall = text(16, 22, FontWeight.SemiBold),
    bodyLarge = text(17, 24, FontWeight.Bold),
    bodyMedium = text(15, 22, FontWeight.Normal),
    bodySmall = text(13, 18, FontWeight.Normal),
    labelLarge = text(14, 20, FontWeight.Bold),
    labelMedium = text(13, 18, FontWeight.SemiBold),
    labelSmall = OverlineTextStyle,
  )

fun TextStyle.light() = copy(fontFamily = AppFonts.figtree, fontWeight = FontWeight.SemiBold)
