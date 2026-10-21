package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.domain.model.UnitType
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class UnitTypeConvertersTest {
  private val converters = UnitTypeConverters()

  @Test
  fun `every unit type reads back from its code`() {
    UnitType.entries.forEach { type ->
      expectThat(converters.codeToUnitType(converters.unitTypeToCode(type))).isEqualTo(type)
    }
  }

  @Test
  fun `the stored codes are fixed`() {
    expectThat(converters.unitTypeToCode(UnitType.UNITS)).isEqualTo("UNITS")
    expectThat(converters.unitTypeToCode(UnitType.TUPPERS)).isEqualTo("TUPPERS")
  }

  @Test
  fun `an unknown or missing code is counted in units`() {
    expectThat(converters.codeToUnitType("BOTTLES")).isEqualTo(UnitType.UNITS)
    expectThat(converters.codeToUnitType(null)).isEqualTo(UnitType.UNITS)
  }
}
