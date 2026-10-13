package com.alorma.caducity.feature.appicon

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.ui.theme.ColorPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class AppIconManagerTest {
  private val context: Context = ApplicationProvider.getApplicationContext()
  private val manager = AndroidAppIconManager(context)

  private fun component(alias: String) = ComponentName(context.packageName, "com.alorma.caducity.$alias")

  private fun enabledAliases(): List<String> =
    AndroidAppIconManager.ALIASES.filter {
      val pm = context.packageManager
      when (pm.getComponentEnabledSetting(component(it))) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
        else -> pm.getActivityInfo(component(it), PackageManager.MATCH_DISABLED_COMPONENTS).enabled
      }
    }

  @Test
  fun `every palette alias exists in the manifest`() {
    AndroidAppIconManager.ALIASES.forEach { alias ->
      assertNotNull(
        alias,
        context.packageManager.getActivityInfo(component(alias), PackageManager.MATCH_DISABLED_COMPONENTS),
      )
    }
  }

  @Test
  fun `only Rebost is enabled by default`() {
    assertEquals(listOf("IconRebost"), enabledAliases())
  }

  @Test
  fun `each palette enables only its own alias`() {
    ColorPalette.entries.forEach { palette ->
      manager.apply(palette)
      assertEquals(palette.name, listOf(AndroidAppIconManager.aliasFor(palette)), enabledAliases())
    }
  }

  @Test
  fun `dynamic uses the Rebost icon`() {
    manager.apply(ColorPalette.CAMP)
    manager.apply(ColorPalette.DYNAMIC)
    assertEquals(listOf("IconRebost"), enabledAliases())
  }

  @Test
  fun `applying the same palette twice changes nothing`() {
    manager.apply(ColorPalette.HORT)
    manager.apply(ColorPalette.HORT)
    assertEquals(listOf("IconHort"), enabledAliases())
  }

  @Test
  fun `the launcher intent resolves to exactly one alias`() {
    manager.apply(ColorPalette.BESSA)
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(context.packageName)
    val resolved = context.packageManager.queryIntentActivities(intent, 0).map { it.activityInfo.name }
    assertEquals(listOf("com.alorma.caducity.IconBessa"), resolved)
  }
}
