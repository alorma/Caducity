package com.alorma.caducity.ui.screen.onboarding

import androidx.annotation.StringRes
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.SuggestedCategory

@get:StringRes
val SuggestedCategory.nameRes: Int
  get() =
    when (this) {
      SuggestedCategory.DAIRY -> R.string.ob_category_dairy
      SuggestedCategory.PRODUCE -> R.string.ob_category_produce
      SuggestedCategory.MEAT_FISH -> R.string.ob_category_meat_fish
      SuggestedCategory.PANTRY -> R.string.ob_category_pantry
      SuggestedCategory.BAKERY -> R.string.ob_category_bakery
      SuggestedCategory.DRINKS -> R.string.ob_category_drinks
      SuggestedCategory.COLD_CUTS -> R.string.ob_category_cold_cuts
      SuggestedCategory.READY_MEALS -> R.string.ob_category_ready_meals
    }
