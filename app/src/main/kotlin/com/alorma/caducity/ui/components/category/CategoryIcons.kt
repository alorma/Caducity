package com.alorma.caducity.ui.components.category

import androidx.compose.ui.graphics.vector.ImageVector
import com.alorma.caducity.base.ui.icons.symbols.BakeryDining
import com.alorma.caducity.base.ui.icons.symbols.Cake
import com.alorma.caducity.base.ui.icons.symbols.Coffee
import com.alorma.caducity.base.ui.icons.symbols.Cookie
import com.alorma.caducity.base.ui.icons.symbols.Eco
import com.alorma.caducity.base.ui.icons.symbols.Egg
import com.alorma.caducity.base.ui.icons.symbols.EmojiFoodBeverage
import com.alorma.caducity.base.ui.icons.symbols.Fastfood
import com.alorma.caducity.base.ui.icons.symbols.Grocery
import com.alorma.caducity.base.ui.icons.symbols.Icecream
import com.alorma.caducity.base.ui.icons.symbols.KebabDining
import com.alorma.caducity.base.ui.icons.symbols.LocalDrink
import com.alorma.caducity.base.ui.icons.symbols.LocalPizza
import com.alorma.caducity.base.ui.icons.symbols.LunchDining
import com.alorma.caducity.base.ui.icons.symbols.Nutrition
import com.alorma.caducity.base.ui.icons.symbols.Pets
import com.alorma.caducity.base.ui.icons.symbols.RamenDining
import com.alorma.caducity.base.ui.icons.symbols.Restaurant
import com.alorma.caducity.base.ui.icons.symbols.SetMeal
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.WaterFull
import com.alorma.caducity.base.ui.icons.symbols.WineBar
import com.alorma.caducity.domain.model.CategoryIcon

/** Category avatar icons by Material Symbol id (see [CategoryIcon]). */
object CategoryIcons {
  private val all: Map<String, ImageVector> by lazy {
    mapOf(
      "eco" to Symbols.Outlined.Eco,
      "water_full" to Symbols.Outlined.WaterFull,
      "nutrition" to Symbols.Outlined.Nutrition,
      "set_meal" to Symbols.Outlined.SetMeal,
      "grocery" to Symbols.Outlined.Grocery,
      "bakery_dining" to Symbols.Outlined.BakeryDining,
      "local_drink" to Symbols.Outlined.LocalDrink,
      "icecream" to Symbols.Outlined.Icecream,
      "egg" to Symbols.Outlined.Egg,
      "lunch_dining" to Symbols.Outlined.LunchDining,
      "restaurant" to Symbols.Outlined.Restaurant,
      "cookie" to Symbols.Outlined.Cookie,
      "local_pizza" to Symbols.Outlined.LocalPizza,
      "ramen_dining" to Symbols.Outlined.RamenDining,
      "kebab_dining" to Symbols.Outlined.KebabDining,
      "emoji_food_beverage" to Symbols.Outlined.EmojiFoodBeverage,
      "wine_bar" to Symbols.Outlined.WineBar,
      "coffee" to Symbols.Outlined.Coffee,
      "cake" to Symbols.Outlined.Cake,
      "fastfood" to Symbols.Outlined.Fastfood,
      "pets" to Symbols.Outlined.Pets,
    )
  }

  fun vectorOf(icon: CategoryIcon): ImageVector = all[icon.id] ?: all.getValue(CategoryIcon.DEFAULT.id)
}

val CategoryIcon.imageVector: ImageVector get() = CategoryIcons.vectorOf(this)
