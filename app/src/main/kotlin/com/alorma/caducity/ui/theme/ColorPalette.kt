package com.alorma.caducity.ui.theme

enum class ColorPalette {
  DYNAMIC,
  REBOST,
  NEVERA,
  HORT,
  FLECA,
  BESSA,
  CAMP,
  ;

  companion object {
    fun fromName(name: String?): ColorPalette = entries.firstOrNull { it.name == name } ?: REBOST
  }
}
