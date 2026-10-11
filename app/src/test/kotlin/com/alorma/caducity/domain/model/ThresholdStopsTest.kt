package com.alorma.caducity.domain.model

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ThresholdStopsTest {
  @Test
  fun `has the eight stops in days`() {
    expectThat(ThresholdStops.days).containsExactly(1, 2, 3, 7, 14, 30, 60, 90)
  }

  @Test
  fun `index to days and back round-trips`() {
    ThresholdStops.days.forEachIndexed { index, days ->
      expectThat(ThresholdStops.daysAt(index)).isEqualTo(days)
      expectThat(ThresholdStops.nearestIndex(days)).isEqualTo(index)
    }
  }

  @Test
  fun `a value outside the stops goes to the nearest stop`() {
    expectThat(ThresholdStops.nearestIndex(10)).isEqualTo(3)
    expectThat(ThresholdStops.nearestIndex(11)).isEqualTo(4)
    expectThat(ThresholdStops.nearestIndex(0)).isEqualTo(0)
    expectThat(ThresholdStops.nearestIndex(365)).isEqualTo(7)
    expectThat(ThresholdStops.nearestIndex(45)).isEqualTo(5)
  }

  @Test
  fun `daysAt clamps out of range indexes`() {
    expectThat(ThresholdStops.daysAt(-3)).isEqualTo(1)
    expectThat(ThresholdStops.daysAt(99)).isEqualTo(90)
  }

  @Test
  fun `suggested margins follow the spec table`() {
    val expected =
      mapOf(
        SuggestedCategory.MEAT_FISH to 2,
        SuggestedCategory.BAKERY to 2,
        SuggestedCategory.DAIRY to 3,
        SuggestedCategory.PRODUCE to 3,
        SuggestedCategory.COLD_CUTS to 3,
        SuggestedCategory.READY_MEALS to 2,
        SuggestedCategory.DRINKS to 7,
        SuggestedCategory.PANTRY to 30,
      )
    expectThat(SuggestedCategory.entries.associateWith(SuggestedThresholds::of)).isEqualTo(expected)
    SuggestedCategory.entries.forEach {
      expectThat(SuggestedThresholds.of(it) in ThresholdStops.days).isEqualTo(true)
    }
  }

  @Test
  fun `suggested margin is found by category name in ca en es`() {
    expectThat(SuggestedThresholds.forName("Làctics")).isEqualTo(3)
    expectThat(SuggestedThresholds.forName("Fruita i verdura")).isEqualTo(3)
    expectThat(SuggestedThresholds.forName("Carn i peix")).isEqualTo(2)
    expectThat(SuggestedThresholds.forName("Despensa")).isEqualTo(30)
    expectThat(SuggestedThresholds.forName("Pan y bollería")).isNull()
    expectThat(SuggestedThresholds.forName("Fleca")).isEqualTo(2)
    expectThat(SuggestedThresholds.forName("Bebidas")).isEqualTo(7)
    expectThat(SuggestedThresholds.forName("Embotits")).isEqualTo(3)
    expectThat(SuggestedThresholds.forName("Plats preparats")).isEqualTo(2)
    expectThat(SuggestedThresholds.forName("Snacks")).isNull()
  }
}
