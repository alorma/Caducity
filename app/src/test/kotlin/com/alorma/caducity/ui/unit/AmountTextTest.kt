package com.alorma.caducity.ui.unit

import com.alorma.caducity.domain.model.UnitType
import java.util.Locale
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AmountTextTest {
  private val ca = Locale.forLanguageTag("ca")
  private val en = Locale.ENGLISH

  @Test
  fun `below a thousand it is grams or millilitres`() {
    expectThat(formatAmount(UnitType.GRAMS, 500, ca)).isEqualTo("500 g")
    expectThat(formatAmount(UnitType.GRAMS, 50, ca)).isEqualTo("50 g")
    expectThat(formatAmount(UnitType.MILLILITRES, 750, ca)).isEqualTo("750 mL")
  }

  @Test
  fun `from a thousand up it is kilograms or litres`() {
    expectThat(formatAmount(UnitType.GRAMS, 1000, ca)).isEqualTo("1 kg")
    expectThat(formatAmount(UnitType.GRAMS, 1200, ca)).isEqualTo("1,2 kg")
    expectThat(formatAmount(UnitType.MILLILITRES, 1500, ca)).isEqualTo("1,5 L")
    expectThat(formatAmount(UnitType.GRAMS, 1250, ca)).isEqualTo("1,25 kg")
  }

  @Test
  fun `the decimal separator follows the locale`() {
    expectThat(formatAmount(UnitType.GRAMS, 1200, en)).isEqualTo("1.2 kg")
    expectThat(formatAmount(UnitType.GRAMS, 100000, en)).isEqualTo("100 kg")
  }
}
