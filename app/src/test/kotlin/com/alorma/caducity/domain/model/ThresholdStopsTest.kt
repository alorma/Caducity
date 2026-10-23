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
  fun `selected stop keeps no duration as none and snaps the rest`() {
    expectThat(ThresholdStops.selectedStop(null)).isNull()
    expectThat(ThresholdStops.selectedStop(7)).isEqualTo(7)
    expectThat(ThresholdStops.selectedStop(10)).isEqualTo(7)
    expectThat(ThresholdStops.selectedStop(45)).isEqualTo(30)
  }

  @Test
  fun `daysAt clamps out of range indexes`() {
    expectThat(ThresholdStops.daysAt(-3)).isEqualTo(1)
    expectThat(ThresholdStops.daysAt(99)).isEqualTo(90)
  }

  @Test
  fun `suggested durations follow the spec table`() {
    val expected =
      mapOf(
        SuggestedCategory.MEAT_FISH to 3,
        SuggestedCategory.BAKERY to 3,
        SuggestedCategory.READY_MEALS to 3,
        SuggestedCategory.DAIRY to 7,
        SuggestedCategory.PRODUCE to 7,
        SuggestedCategory.COLD_CUTS to 7,
        SuggestedCategory.DRINKS to 30,
        SuggestedCategory.PANTRY to 90,
      )
    expectThat(SuggestedCategory.entries.associateWith(SuggestedDurations::of)).isEqualTo(expected)
    SuggestedCategory.entries.forEach {
      expectThat(SuggestedDurations.of(it) in ThresholdStops.days).isEqualTo(true)
    }
  }

  @Test
  fun `suggested duration is found by category name in ca en es`() {
    expectThat(SuggestedDurations.forName("Làctics")).isEqualTo(7)
    expectThat(SuggestedDurations.forName("Fruita i verdura")).isEqualTo(7)
    expectThat(SuggestedDurations.forName("Carn i peix")).isEqualTo(3)
    expectThat(SuggestedDurations.forName("Despensa")).isEqualTo(90)
    expectThat(SuggestedDurations.forName("Pan y bollería")).isNull()
    expectThat(SuggestedDurations.forName("Fleca")).isEqualTo(3)
    expectThat(SuggestedDurations.forName("Bebidas")).isEqualTo(30)
    expectThat(SuggestedDurations.forName("Embotits")).isEqualTo(7)
    expectThat(SuggestedDurations.forName("Plats preparats")).isEqualTo(3)
    expectThat(SuggestedDurations.forName("Snacks")).isNull()
  }

  @Test
  fun `default category keeps its duration only while it keeps the suggested shape and icon`() {
    val dairy = SuggestedCategory.DAIRY
    expectThat(SuggestedDurations.forDefaultCategory("Làctics", dairy.shape, dairy.icon)).isEqualTo(7)
    expectThat(SuggestedDurations.forDefaultCategory("Làctics", dairy.shape, CategoryIcon("eco"))).isNull()
    expectThat(SuggestedDurations.forDefaultCategory("Làctics", CategoryShape.SUNNY, dairy.icon)).isNull()
    expectThat(SuggestedDurations.forDefaultCategory("Snacks", dairy.shape, dairy.icon)).isNull()
  }
}
