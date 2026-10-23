package com.alorma.caducity.ui.components.threshold

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.alorma.caducity.R

/** «1 dia», «3 dies», «1 setmana», «2 mesos»: a duration written the way the Durada sheet labels its options. */
@Composable
fun thresholdLabel(days: Int): String =
  when {
    days >= 30 && days % 30 == 0 ->
      pluralStringResource(R.plurals.threshold_months, days / 30, days / 30)
    days >= 7 && days % 7 == 0 ->
      pluralStringResource(R.plurals.threshold_weeks, days / 7, days / 7)
    else -> pluralStringResource(R.plurals.threshold_days, days, days)
  }
