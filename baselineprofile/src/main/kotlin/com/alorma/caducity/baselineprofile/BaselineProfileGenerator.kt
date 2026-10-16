package com.alorma.caducity.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Genera el Baseline Profile de l'app: arrencada, Home, calendari, llista per estat, afegir un producte,
 * Pantry amb cerca, detall d'un producte i Settings.
 *
 * Necessita l'app amb dades (Home amb productes) i la interfície en anglès: els elements es troben per text o
 * descripció. No desa res: els fluxos d'edició es tanquen sense guardar. Es llança amb
 * `./gradlew :app:generateReleaseBaselineProfile`.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
  @get:Rule
  val rule = BaselineProfileRule()

  @Test
  fun generate() =
    rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
      pressHome()
      startActivityAndWait()

      // Sense onboarding i amb dades, o el perfil sortiria fluix: millor fallar que commitejar-lo
      device.expectObject(
        By.desc("Pantry"),
        "L'app no és a Home (onboarding en marxa?). Deixa-la amb dades abans de generar: skill plant-seed",
      )
      device.expectObject(
        By.scrollable(true),
        "Home no té llista: l'app no té dades. Deixa-la amb dades abans de generar: skill plant-seed",
      )

      // Home: scroll amb les dades pintades
      device.scrollFirst()

      // Calendari
      device.findObject(By.desc("Calendar")).click()
      device.waitForIdle()
      device.pressBack()
      device.expectObject(By.text("What expires"), "Home no es mostra després del calendari")

      // Llista per estat (targeta Soon del resum; el seed no té congelats)
      device.expectObject(By.descStartsWith("Soon"), "Home no té la targeta d'aviat").click()
      val left = device.wait(Until.gone(By.text("What expires")), UI_TIMEOUT)
      check(left == true) { "La llista per estat no s'ha obert" }
      device.scrollFirst()
      device.pressBack()
      device.expectObject(By.text("What expires"), "Home no es mostra després de la llista per estat")

      // Afegir un producte: omple el nom, obre el selector de data i tanca sense desar
      device.findObject(By.desc("Add a product")).click()
      device.expectObject(By.clazz("android.widget.EditText"), "Afegir producte no es mostra").text = "Yogurt"
      device.waitForIdle()
      device.findObject(By.desc("Close"))?.click()
      device.waitForIdle()
      device.expectObject(By.text("What expires"), "Home no es mostra després d'afegir")

      // Pantry: llista, cerca i detall d'un producte
      device.findObject(By.desc("Pantry")).click()
      device.waitForIdle()
      device.scrollFirst()
      device.expectObject(By.clazz("android.widget.EditText"), "Cerca de Pantry no es mostra").text = "milk"
      device.waitForIdle()
      device.findObject(By.textContains("Whole milk"))?.click()
      device.waitForIdle()
      device.pressBack()
      device.waitForIdle()

      // Settings
      device.findObject(By.desc("Settings")).click()
      device.waitForIdle()
      device.scrollFirst()
    }

  private fun UiDevice.expectObject(
    selector: BySelector,
    message: String,
  ) = checkNotNull(wait(Until.findObject(selector), UI_TIMEOUT)) { message }

  private fun UiDevice.scrollFirst() {
    waitForIdle()
    // The list can recompose while it settles (after a navigation): look it up again for each fling
    listOf(Direction.DOWN, Direction.UP).forEach { direction ->
      findObject(By.scrollable(true))?.apply {
        setGestureMargin(displayWidth / 5)
        // The gesture is done even if the node goes stale while the fling reads it back
        try {
          fling(direction)
        } catch (_: StaleObjectException) {
        }
      }
    }
  }

  private companion object {
    const val PACKAGE_NAME = "com.alorma.caducity"
    const val UI_TIMEOUT = 5_000L
  }
}
