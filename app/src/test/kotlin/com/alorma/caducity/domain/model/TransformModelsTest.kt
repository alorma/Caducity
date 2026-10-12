package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class TransformModelsTest {
  @Test
  fun `default expiration is the one of the ingredient that expires first`() {
    val dates = listOf(LocalDate(2026, 10, 20), LocalDate(2026, 10, 14), LocalDate(2026, 11, 1))

    expectThat(defaultResultExpiration(dates)).isEqualTo(LocalDate(2026, 10, 14))
  }

  @Test
  fun `default expiration keeps a past date instead of lengthening it`() {
    val dates = listOf(LocalDate(2026, 10, 1), LocalDate(2026, 10, 14))

    expectThat(defaultResultExpiration(dates)).isEqualTo(LocalDate(2026, 10, 1))
  }

  @Test
  fun `default expiration without ingredients is null`() {
    expectThat(defaultResultExpiration(emptyList())).isNull()
  }
}
