package com.alorma.caducity.data.datasource.room

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CategoryDefaultsTest {
  private fun of(
    name: String,
    index: Int = 0,
  ) = CategoryDefaults.forName(name, index).let { it.shape to it.icon }

  @Test
  fun `matches names in English, Spanish and Catalan`() {
    expectThat(of("Dairy")).isEqualTo("COOKIE" to "water_full")
    expectThat(of("Lácteos")).isEqualTo("COOKIE" to "water_full")
    expectThat(of("Làctics")).isEqualTo("COOKIE" to "water_full")
    expectThat(of("Fruita i verdura")).isEqualTo("FLOWER" to "nutrition")
    expectThat(of("Carne y pescado")).isEqualTo("CLOVER" to "set_meal")
    expectThat(of("Carn")).isEqualTo("CLOVER" to "set_meal")
    expectThat(of("Rebost")).isEqualTo("SUNNY" to "grocery")
    expectThat(of("Despensa")).isEqualTo("SUNNY" to "grocery")
    expectThat(of("Fleca")).isEqualTo("PUFFY" to "bakery_dining")
    expectThat(of("Pan")).isEqualTo("PUFFY" to "bakery_dining")
    expectThat(of("Begudes")).isEqualTo("BURST" to "local_drink")
    expectThat(of("Congelados")).isEqualTo("BURST" to "icecream")
  }

  @Test
  fun `pan only matches as a whole word`() {
    expectThat(of("Panko", index = 1)).isEqualTo("FLOWER" to "eco")
  }

  @Test
  fun `pantry wins over bakery prefix`() {
    expectThat(of("Pantry")).isEqualTo("SUNNY" to "grocery")
  }

  @Test
  fun `unmatched names cycle shapes by index with the default icon`() {
    val shapes = (0..7).map { CategoryDefaults.forName("Zzz", it).shape }
    expectThat(shapes).isEqualTo(
      listOf("COOKIE", "FLOWER", "CLOVER", "SUNNY", "PUFFY", "BURST", "COOKIE", "FLOWER"),
    )
    expectThat(CategoryDefaults.forName("Zzz", 3).icon).isEqualTo("eco")
  }
}
