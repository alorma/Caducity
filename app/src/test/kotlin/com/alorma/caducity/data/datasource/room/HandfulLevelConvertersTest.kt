package com.alorma.caducity.data.datasource.room

import com.alorma.caducity.domain.model.HandfulLevel
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class HandfulLevelConvertersTest {
  private val converters = HandfulLevelConverters()

  @Test
  fun `every level reads back from its code`() {
    HandfulLevel.entries.forEach { level ->
      expectThat(converters.codeToLevel(converters.levelToCode(level))).isEqualTo(level)
    }
  }

  @Test
  fun `the stored codes are fixed and a missing level stays missing`() {
    expectThat(converters.levelToCode(HandfulLevel.LOTS)).isEqualTo("LOTS")
    expectThat(converters.levelToCode(HandfulLevel.HALF)).isEqualTo("HALF")
    expectThat(converters.levelToCode(HandfulLevel.LITTLE)).isEqualTo("LITTLE")
    expectThat(converters.levelToCode(null)).isNull()
    expectThat(converters.codeToLevel(null)).isNull()
  }

  @Test
  fun `an unknown code reads as a lot`() {
    expectThat(converters.codeToLevel("MUCH")).isEqualTo(HandfulLevel.LOTS)
  }
}
