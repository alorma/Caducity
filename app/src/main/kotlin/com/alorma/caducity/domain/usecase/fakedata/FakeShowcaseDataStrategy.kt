package com.alorma.caducity.domain.usecase.fakedata

import com.alorma.caducity.domain.model.UnitType
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * The pantry of the Play and social screenshots (spec §12): 61 products in 6 categories, most counted in units and
 * eleven measured ones (grams, millilitres), every category with its usual duration. Caducats 7, Aviat 8, Frescos 43, Congelats 3.
 *
 * Dates are relative to today, which plays the reference day (Sunday 11 October) with «Aviat» = 3 days:
 * the screenshots match when the app's «Aviat» setting is 3 days.
 */
class FakeShowcaseDataStrategy : FakeDataStrategy {
  override fun getCategoryConfigs(now: Instant): List<CategoryConfig> {
    fun product(
      name: String,
      daysAhead: Int,
      units: Int = 1,
      unitType: UnitType = UnitType.DEFAULT,
      amount: Int? = null,
    ) = ProductConfig(
      name,
      List(units) { ItemConfig(identifier = "", expirationDate = now.plus(daysAhead.days), amount = amount) },
      unitType,
    )

    fun frozen(
      name: String,
      frozenDaysAgo: Int,
      remainingDays: Int,
      units: Int = 1,
      unitType: UnitType = UnitType.DEFAULT,
      amount: Int? = null,
    ) = ProductConfig(
      name,
      List(units) {
        ItemConfig(
          identifier = "",
          expirationDate = now.plus(remainingDays.days),
          shouldFreeze = true,
          remainingDaysWhenFrozen = remainingDays,
          frozenDaysAgo = frozenDaysAgo,
          amount = amount,
        )
      },
      unitType,
    )

    return listOf(
      // 1 / 3 / 8 / 1
      CategoryConfig(
        name = "Làctics",
        description = "Llet, iogurts, formatges i ous",
        usualDurationDays = 7,
        products =
          listOf(
            product("Iogurt grec", daysAhead = -2, units = 3),
            product("Llet sencera", daysAhead = 0, unitType = UnitType.MILLILITRES, amount = 1000),
            product("Mozzarella", daysAhead = 1),
            product("Iogurt natural", daysAhead = 2, units = 4),
            product("Formatge fresc", daysAhead = 7),
            product("Llet semidesnatada", daysAhead = 9, units = 2),
            product("Formatge cheddar", daysAhead = 10, units = 3),
            product("Quefir", daysAhead = 11, unitType = UnitType.MILLILITRES, amount = 500),
            product("Nata per cuinar", daysAhead = 16),
            product("Ous", daysAhead = 18, units = 12),
            product("Mantega", daysAhead = 22, unitType = UnitType.GRAMS, amount = 250),
            product("Formatge manxego", daysAhead = 25),
            frozen("Parmesà ratllat", frozenDaysAgo = 13, remainingDays = 20, unitType = UnitType.GRAMS, amount = 150),
          ),
      ),
      // 2 / 2 / 14 / 0
      CategoryConfig(
        name = "Fruita i verdura",
        description = "Fruita i verdura fresques",
        usualDurationDays = 7,
        products =
          listOf(
            product("Pastanagues", daysAhead = -2),
            product("Tomàquets", daysAhead = -1),
            product("Espinacs", daysAhead = 0),
            product("Maduixes", daysAhead = 1),
            product("Plàtans", daysAhead = 5, units = 5),
            product("Enciam", daysAhead = 5),
            product("Raïm", daysAhead = 6),
            product("Pebrot vermell", daysAhead = 8),
            product("Carbassó", daysAhead = 10),
            product("Bròquil", daysAhead = 12),
            product("Kiwis", daysAhead = 12, units = 6),
            product("Pomes", daysAhead = 14, units = 6),
            product("Preparat de verdures", daysAhead = 15, units = 2),
            product("Taronges", daysAhead = 20, units = 8),
            product("Llimones", daysAhead = 25, units = 4),
            product("Cebes", daysAhead = 30, units = 3),
            product("Patates", daysAhead = 35, units = 10),
            product("Alls", daysAhead = 40),
          ),
      ),
      // 3 / 1 / 3 / 0
      CategoryConfig(
        name = "Carn i peix",
        description = "Carn, peix i marisc",
        usualDurationDays = 3,
        products =
          listOf(
            product("Gambes", daysAhead = -3, units = 2),
            product("Pit de pollastre", daysAhead = -1),
            product("Salmó", daysAhead = -1),
            product("Pernil dolç", daysAhead = 1, units = 3),
            product("Pollastre sencer", daysAhead = 5),
            product("Llom de porc", daysAhead = 6, units = 2),
            product("Hamburgueses", daysAhead = 8, units = 4),
          ),
      ),
      // 1 / 1 / 15 / 0
      CategoryConfig(
        name = "Secs i conserves",
        description = "Pasta, arròs, llegums i conserves",
        usualDurationDays = 90,
        products =
          listOf(
            product("Salsa de tomàquet", daysAhead = -5),
            product("Hummus", daysAhead = 3),
            product("Olives", daysAhead = 40),
            product("Cereals", daysAhead = 45, unitType = UnitType.GRAMS, amount = 500),
            product("Galetes", daysAhead = 60),
            product("Xocolata negra", daysAhead = 70),
            product("Cafè", daysAhead = 90, unitType = UnitType.GRAMS, amount = 250),
            product("Melmelada", daysAhead = 100),
            product("Tomàquet triturat", daysAhead = 120),
            product("Farina", daysAhead = 150, unitType = UnitType.GRAMS, amount = 1000),
            product("Llenties cuites", daysAhead = 180),
            product("Cigrons cuits", daysAhead = 200, units = 2),
            product("Macarrons", daysAhead = 250, unitType = UnitType.GRAMS, amount = 500),
            product("Arròs", daysAhead = 300, unitType = UnitType.GRAMS, amount = 2000),
            product("Oli d'oliva", daysAhead = 365, unitType = UnitType.MILLILITRES, amount = 1000),
            product("Tonyina en llauna", daysAhead = 400, units = 3),
            product("Mel", daysAhead = 500, unitType = UnitType.GRAMS, amount = 500),
          ),
      ),
      // 0 / 1 / 2 / 0
      CategoryConfig(
        name = "Fleca",
        description = "Pa i brioixeria",
        usualDurationDays = 3,
        products =
          listOf(
            product("Pa de motlle", daysAhead = 3),
            product("Pa de pagès", daysAhead = 4),
            product("Croissants", daysAhead = 5, units = 4),
          ),
      ),
      // 0 / 0 / 1 / 2
      CategoryConfig(
        name = "Plats preparats",
        description = "Sobres, plats cuinats i congelats",
        usualDurationDays = 3,
        products =
          listOf(
            product("Sofregit", daysAhead = 7, units = 2),
            frozen("Croquetes", frozenDaysAgo = 7, remainingDays = 3, units = 8),
            frozen("Brou", frozenDaysAgo = 7, remainingDays = 2, units = 2),
          ),
      ),
    )
  }
}
