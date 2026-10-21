package com.alorma.caducity.domain.usecase.fakedata

import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Small data set that covers the main cases of the app without bulk (about 40 units):
 * - 2 categories, 10 products, each product exercising a different mix.
 * - Every status: fresh, expiring soon, expired (recently and long ago), frozen.
 * - Units: single unit, several units on the same day (one row, «N unitats»), several days, mixed statuses in one product.
 * - Units that must not show up anywhere: consumed and used as an ingredient of a Transforma.
 * - A long name, to check ellipsis.
 * - Unit types: most products in units, others in tuppers, packs and cans (rows read «3 paquets», «1 tupper»).
 *
 * Dates are relative to today and to the expiration thresholds.
 */
class FakeTestDataStrategy(
  private val expirationThresholds: ExpirationThresholds,
) : FakeDataStrategy {
  private val freshDays: Long get() = expirationThresholds.soonExpiringThreshold.inWholeDays + 3

  override fun getCategoryConfigs(now: Instant): List<CategoryConfig> =
    listOf(
      createDairyCategory(now),
      createProduceCategory(now),
    )

  private fun createDairyCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Làctics",
      description = "Llet, formatge, iogurt i altres productes làctics",
      usualDurationDays = 7,
      products =
        listOf(
          // Mixed statuses: the row takes the state of the unit that expires first.
          ProductConfig(
            name = "Llet sencera",
            items =
              units(now, "Llet fresca", daysAhead = freshDays, count = 4) +
                units(now, "Llet aviat", daysAhead = 1, count = 2) +
                units(now, "Llet caducada", daysAhead = -1, count = 1) +
                units(now, "Llet congelada", daysAhead = 6, count = 2, frozen = true) +
                units(now, "Llet consumida", daysAhead = 2, count = 1, consumed = true) +
                units(now, "Llet transformada", daysAhead = 10, count = 1, transformed = true),
          ),
          // Several units long expired on the same day, plus one on another day.
          ProductConfig(
            name = "Formatge cheddar",
            unitType = UnitType.PACKS,
            items =
              units(now, "Formatge caducat", daysAhead = -6, count = 3) +
                units(now, "Formatge caducat un altre dia", daysAhead = -9, count = 1),
          ),
          // Long name, expires today.
          ProductConfig(
            name = "Iogurt natural amb fruites del bosc i cereals integrals",
            unitType = UnitType.TUPPERS,
            items = units(now, "Iogurt", daysAhead = 0, count = 1),
          ),
          ProductConfig(
            name = "Mantega",
            items = units(now, "Mantega", daysAhead = 8, count = 1, frozen = true),
          ),
          ProductConfig(
            name = "Nata",
            unitType = UnitType.CANS,
            items = units(now, "Nata", daysAhead = freshDays + 4, count = 1),
          ),
        ),
    )

  private fun createProduceCategory(now: Instant): CategoryConfig =
    CategoryConfig(
      name = "Fruita i verdura",
      description = "Fruita i verdura fresques",
      usualDurationDays = 7,
      products =
        listOf(
          // Fresh on several days, soon on a single day, and recently expired.
          ProductConfig(
            name = "Pomes",
            items =
              (0 until 5).flatMap { units(now, "Poma fresca", daysAhead = freshDays + it, count = 1) } +
                units(now, "Poma aviat", daysAhead = 2, count = 3) +
                units(now, "Poma caducada", daysAhead = -2, count = 2),
          ),
          ProductConfig(
            name = "Pastanagues",
            unitType = UnitType.PACKS,
            items =
              units(now, "Pastanaga aviat", daysAhead = 3, count = 1) +
                units(now, "Pastanaga fresca", daysAhead = freshDays, count = 1) +
                units(now, "Pastanaga consumida", daysAhead = -1, count = 2, consumed = true),
          ),
          ProductConfig(
            name = "Espinacs",
            unitType = UnitType.TUPPERS,
            items = units(now, "Espinacs", daysAhead = -1, count = 1),
          ),
          // Frozen on different days.
          ProductConfig(
            name = "Pèsols",
            unitType = UnitType.PACKS,
            items =
              (0L until 3L).flatMap {
                units(now, "Pèsols congelats", daysAhead = 5 + it, count = 1, frozen = true)
              },
          ),
          // Ingredients of a Transforma next to a fresh unit.
          ProductConfig(
            name = "Albergínia",
            items =
              units(now, "Albergínia transformada", daysAhead = 10, count = 2, transformed = true) +
                units(now, "Albergínia fresca", daysAhead = freshDays + 1, count = 1),
          ),
        ),
    )

  /** [count] units expiring on the same day, [daysAhead] days from [now] (negative = already expired). */
  private fun units(
    now: Instant,
    label: String,
    daysAhead: Long,
    count: Int,
    frozen: Boolean = false,
    consumed: Boolean = false,
    transformed: Boolean = false,
  ): List<ItemConfig> =
    List(count) { index ->
      ItemConfig(
        identifier = "$label #${index + 1}",
        expirationDate = now.plus(daysAhead.days),
        shouldFreeze = frozen,
        remainingDaysWhenFrozen = 5,
        shouldConsume = consumed,
        shouldTransform = transformed,
      )
    }
}
