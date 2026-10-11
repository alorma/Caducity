package com.alorma.caducity.domain.model

/** A starter category offered by the onboarding (`04-model-de-dades.md`). The name comes from string resources. */
enum class SuggestedCategory(
  val shape: CategoryShape,
  val icon: CategoryIcon,
  val selectedByDefault: Boolean,
) {
  DAIRY(CategoryShape.COOKIE, CategoryIcon("water_full"), true),
  PRODUCE(CategoryShape.CLOVER8, CategoryIcon("nutrition"), true),
  MEAT_FISH(CategoryShape.CLOVER, CategoryIcon("set_meal"), true),
  PANTRY(CategoryShape.SUNNY, CategoryIcon("grocery"), true),
  BAKERY(CategoryShape.PUFFY, CategoryIcon("bakery_dining"), true),
  DRINKS(CategoryShape.BURST, CategoryIcon("local_drink"), false),
  COLD_CUTS(CategoryShape.COOKIE, CategoryIcon("kebab_dining"), false),
  READY_MEALS(CategoryShape.CLOVER8, CategoryIcon("soup_kitchen"), false),
}

/** A category to create from the onboarding, already named in the user's language. */
data class SeedCategory(
  val name: String,
  val shape: CategoryShape,
  val icon: CategoryIcon,
)
