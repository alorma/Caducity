package com.alorma.caducity.domain.model

import com.alorma.caducity.data.datasource.room.AvatarConverters
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CategoryDefaultsTest {
  private fun of(
    name: String,
    index: Int = 0,
  ) = CategoryDefaults.forName(name, index).let { it.shape.name to it.icon.id }

  @Test
  fun `matches names in English, Spanish and Catalan`() {
    expectThat(of("Dairy")).isEqualTo("COOKIE" to "water_full")
    expectThat(of("Lácteos")).isEqualTo("COOKIE" to "water_full")
    expectThat(of("Làctics")).isEqualTo("COOKIE" to "water_full")
    expectThat(of("Fruita i verdura")).isEqualTo("COOKIE7" to "nutrition")
    expectThat(of("Carne y pescado")).isEqualTo("CLOVER" to "set_meal")
    expectThat(of("Carn")).isEqualTo("CLOVER" to "set_meal")
    expectThat(of("Rebost")).isEqualTo("SUNNY" to "grocery")
    expectThat(of("Despensa")).isEqualTo("SUNNY" to "grocery")
    expectThat(of("Fleca")).isEqualTo("COOKIE4" to "bakery_dining")
    expectThat(of("Pan")).isEqualTo("COOKIE4" to "bakery_dining")
    expectThat(of("Begudes")).isEqualTo("CLOVER" to "local_drink")
    expectThat(of("Congelados")).isEqualTo("SUNNY" to "icecream")
  }

  @Test
  fun `pan only matches as a whole word`() {
    expectThat(of("Panko", index = 1)).isEqualTo("COOKIE7" to "eco")
  }

  @Test
  fun `pantry wins over bakery prefix`() {
    expectThat(of("Pantry")).isEqualTo("SUNNY" to "grocery")
  }

  @Test
  fun `unmatched names cycle shapes by index with the default icon`() {
    val shapes = (0..7).map { CategoryDefaults.forName("Zzz", it).shape.name }
    expectThat(shapes).isEqualTo(
      listOf("COOKIE", "COOKIE7", "CLOVER", "SUNNY", "COOKIE4", "PUFFY", "COOKIE", "COOKIE7"),
    )
    expectThat(CategoryDefaults.forName("Zzz", 3).icon.id).isEqualTo("eco")
  }

  @Test
  fun `converters round-trip and fall back for unknown shapes`() {
    val converters = AvatarConverters()
    expectThat(converters.stringToShape(converters.shapeToString(CategoryShape.CLOVER))).isEqualTo(CategoryShape.CLOVER)
    expectThat(converters.stringToShape("REMOVED_SHAPE")).isEqualTo(CategoryShape.COOKIE)
    expectThat(converters.stringToIcon("future_icon")).isEqualTo(CategoryIcon("future_icon"))
  }
}
