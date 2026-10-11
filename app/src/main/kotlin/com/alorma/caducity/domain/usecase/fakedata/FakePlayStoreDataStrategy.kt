package com.alorma.caducity.domain.usecase.fakedata

import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Strategy for generating consistent fake data for PlayStore screenshots
 *
 * Creates:
 * - 5 fixed categories (always the same)
 * - Predefined products per category
 * - Items with varied expiration dates relative to current time
 * - Different item distribution per category for interesting screenshots
 *
 * The data is consistent (same categories/products) but item states vary based on creation time,
 * allowing for reproducible yet time-appropriate screenshots.
 */
class FakePlayStoreDataStrategy : FakeDataStrategy {
  override fun getCategoryConfigs(now: Instant): List<CategoryConfig> =
    listOf(
      createDairyCategory(now),
      createProduceCategory(now),
      createMeatCategory(now),
      createPantryCategory(now),
      createFrozenCategory(now),
    )

  private fun createDairyCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Làctics",
      description = "Llet, formatge, iogurt i altres productes làctics",
      soonThresholdDays = 3,
      products =
        listOf(
          ProductConfig(
            name = "Llet sencera",
            items =
              listOf(
                ItemConfig("Llet sencera #1", now.plus(2.days)),
                ItemConfig("Llet sencera #2", now.plus(5.days)),
                ItemConfig("Llet sencera #3", now.plus(8.days)),
              ),
          ),
          ProductConfig(
            name = "Formatge cheddar",
            items =
              listOf(
                ItemConfig("Formatge cheddar #1", now.plus(10.days)),
                ItemConfig("Formatge cheddar #2", now.plus(15.days)),
                ItemConfig("Formatge cheddar #3", now.plus(20.days)),
              ),
          ),
          ProductConfig(
            name = "Iogurt grec",
            items =
              listOf(
                ItemConfig("Iogurt grec #1", now.plus(3.days)),
                ItemConfig("Iogurt grec #2", now.plus(7.days)),
                ItemConfig("Iogurt grec #3", now.minus(1.days)),
              ),
          ),
        ),
      standaloneItems =
        listOf(
          StandaloneItemConfig("Mantega", now.plus(10.days)),
          StandaloneItemConfig("Nata agra", now.plus(5.days)),
        ),
    )

  private fun createProduceCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Fruita i verdura",
      description = "Fruita i verdura fresques",
      soonThresholdDays = 3,
      products =
        listOf(
          ProductConfig(
            name = "Pomes",
            items =
              listOf(
                ItemConfig("Pomes #1", now.plus(5.days)),
                ItemConfig("Pomes #2", now.plus(10.days)),
                ItemConfig("Pomes #3", now.plus(15.days)),
              ),
          ),
          ProductConfig(
            name = "Pastanagues",
            items =
              listOf(
                ItemConfig("Pastanagues #1", now.plus(8.days)),
                ItemConfig("Pastanagues #2", now.plus(12.days)),
                ItemConfig("Pastanagues #3", now.minus(2.days)),
              ),
          ),
          ProductConfig(
            name = "Espinacs",
            items =
              listOf(
                ItemConfig("Espinacs #1", now.plus(2.days)),
                ItemConfig("Espinacs #2", now.plus(4.days)),
                ItemConfig("Espinacs #3", now.plus(6.days)),
              ),
          ),
          ProductConfig(
            name = "Tomàquets",
            items =
              listOf(
                ItemConfig("Tomàquets #1", now.plus(3.days)),
                ItemConfig("Tomàquets #2", now.minus(1.days)),
                ItemConfig("Tomàquets #3", now.plus(7.days)),
              ),
          ),
        ),
      standaloneItems =
        listOf(
          StandaloneItemConfig("Plàtans", now.plus(3.days)),
          StandaloneItemConfig("Enciam", now.plus(4.days)),
        ),
    )

  private fun createMeatCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Carn i peix",
      description = "Carns i peix, frescos i congelats",
      soonThresholdDays = 2,
      products =
        listOf(
          ProductConfig(
            name = "Pit de pollastre",
            items =
              listOf(
                ItemConfig("Pit de pollastre #1", now.plus(2.days)),
                ItemConfig("Pit de pollastre #2", now.plus(4.days)),
                ItemConfig("Pit de pollastre #3", now.minus(1.days)),
              ),
          ),
          ProductConfig(
            name = "Carn picada",
            items =
              listOf(
                ItemConfig("Carn picada #1", now.plus(3.days)),
                ItemConfig("Carn picada #2", now.plus(5.days)),
                ItemConfig("Carn picada #3", now.plus(7.days)),
              ),
          ),
          ProductConfig(
            name = "Filet de salmó",
            items =
              listOf(
                ItemConfig("Filet de salmó #1", now.plus(2.days)),
                ItemConfig("Filet de salmó #2", now.minus(2.days)),
                ItemConfig("Filet de salmó #3", now.plus(6.days)),
              ),
          ),
        ),
      standaloneItems =
        listOf(
          StandaloneItemConfig("Cansalada", now.plus(7.days)),
          StandaloneItemConfig("Gambes", now.minus(1.days)),
        ),
    )

  private fun createPantryCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Secs i conserves",
      description = "Pasta, conserves i condiments",
      soonThresholdDays = 30,
      products =
        listOf(
          ProductConfig(
            name = "Pasta",
            items =
              listOf(
                ItemConfig("Pasta #1", now.plus(60.days)),
                ItemConfig("Pasta #2", now.plus(90.days)),
                ItemConfig("Pasta #3", now.plus(120.days)),
              ),
          ),
          ProductConfig(
            name = "Arròs",
            items =
              listOf(
                ItemConfig("Arròs #1", now.plus(180.days)),
                ItemConfig("Arròs #2", now.plus(240.days)),
                ItemConfig("Arròs #3", now.plus(300.days)),
              ),
          ),
          ProductConfig(
            name = "Tomàquet triturat",
            items =
              listOf(
                ItemConfig("Tomàquet triturat #1", now.plus(30.days)),
                ItemConfig("Tomàquet triturat #2", now.plus(45.days)),
                ItemConfig("Tomàquet triturat #3", now.minus(5.days)),
              ),
          ),
          ProductConfig(
            name = "Oli d'oliva",
            items =
              listOf(
                ItemConfig("Oli d'oliva #1", now.plus(90.days)),
                ItemConfig("Oli d'oliva #2", now.plus(150.days)),
                ItemConfig("Oli d'oliva #3", now.plus(210.days)),
              ),
          ),
        ),
      standaloneItems =
        listOf(
          StandaloneItemConfig("Pa", now.plus(2.days)),
          StandaloneItemConfig("Galetes salades", now.plus(14.days)),
        ),
    )

  private fun createFrozenCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Congelats",
      description = "Plats cuinats, verdures i postres congelats",
      products =
        listOf(
          ProductConfig(
            name = "Pizza",
            items =
              listOf(
                ItemConfig("Pizza #1", now.plus(30.days), shouldFreeze = true, remainingDaysWhenFrozen = 30),
                ItemConfig("Pizza #2", now.plus(60.days)),
                ItemConfig("Pizza #3", now.plus(90.days)),
              ),
          ),
          ProductConfig(
            name = "Gelat",
            items =
              listOf(
                ItemConfig("Gelat #1", now.plus(45.days), shouldFreeze = true, remainingDaysWhenFrozen = 45),
                ItemConfig("Gelat #2", now.plus(75.days)),
                ItemConfig("Gelat #3", now.plus(105.days)),
              ),
          ),
          ProductConfig(
            name = "Pèsols congelats",
            items =
              listOf(
                ItemConfig(
                  "Pèsols congelats #1",
                  now.plus(120.days),
                  shouldFreeze = true,
                  remainingDaysWhenFrozen = 120,
                ),
                ItemConfig("Pèsols congelats #2", now.plus(180.days)),
                ItemConfig("Pèsols congelats #3", now.plus(240.days)),
              ),
          ),
        ),
      standaloneItems =
        listOf(
          StandaloneItemConfig("Gofres", now.plus(60.days)),
          StandaloneItemConfig("Patates fregides", now.plus(90.days)),
        ),
    )
}
