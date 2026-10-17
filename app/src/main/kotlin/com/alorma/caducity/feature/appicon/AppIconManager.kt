package com.alorma.caducity.feature.appicon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import com.alorma.caducity.MainActivity
import com.alorma.caducity.R
import com.alorma.caducity.ui.theme.ColorPalette

/** Keeps the launcher icon in sync with the chosen color palette. */
interface AppIconManager {
  /** Enables the icon of [palette] and disables the others. Dynamic uses the Rebost icon. */
  fun apply(palette: ColorPalette)
}

/** One `activity-alias` per palette (see AndroidManifest.xml); exactly one is enabled at a time. */
class AndroidAppIconManager(
  private val context: Context,
) : AppIconManager {
  override fun apply(palette: ColorPalette) {
    val packageManager = context.packageManager
    val target = aliasFor(palette)
    // Enable the new alias before disabling the others so there is always a launcher entry.
    val aliases = listOf(target) + ALIASES.filter { it != target }
    aliases.forEach { alias ->
      val component = ComponentName(context.packageName, "$NAMESPACE.$alias")
      val wanted = alias == target
      if (packageManager.isAliasEnabled(component) != wanted) {
        val state =
          if (wanted) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
          } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
          }
        packageManager.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
      }
    }
  }

  private fun PackageManager.isAliasEnabled(component: ComponentName): Boolean =
    when (getComponentEnabledSetting(component)) {
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
      PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
      // Not overridden: the manifest decides (only Rebost is enabled there).
      else -> getActivityInfo(component, PackageManager.MATCH_DISABLED_COMPONENTS).enabled
    }

  companion object {
    private val NAMESPACE = MainActivity::class.java.name.substringBeforeLast('.')

    val ALIASES = ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }.map { aliasFor(it) }

    /** Manifest alias name (relative to the app namespace) for a palette. */
    fun aliasFor(palette: ColorPalette): String =
      when (palette) {
        ColorPalette.DYNAMIC, ColorPalette.REBOST -> "IconRebost"
        ColorPalette.NEVERA -> "IconNevera"
        ColorPalette.HORT -> "IconHort"
        ColorPalette.FLECA -> "IconFleca"
        ColorPalette.BESSA -> "IconBessa"
        ColorPalette.CAMP -> "IconCamp"
      }

    /** Adaptive icon of a palette, the same the launcher shows for [aliasFor]. */
    @DrawableRes
    fun iconFor(palette: ColorPalette): Int =
      when (palette) {
        ColorPalette.DYNAMIC, ColorPalette.REBOST -> R.drawable.ic_icon_rebost
        ColorPalette.NEVERA -> R.drawable.ic_icon_nevera
        ColorPalette.HORT -> R.drawable.ic_icon_hort
        ColorPalette.FLECA -> R.drawable.ic_icon_fleca
        ColorPalette.BESSA -> R.drawable.ic_icon_bessa
        ColorPalette.CAMP -> R.drawable.ic_icon_camp
      }
  }
}
