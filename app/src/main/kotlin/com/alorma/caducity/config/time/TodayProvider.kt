package com.alorma.caducity.config.time

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Today's date; emits again when the day changes (midnight) or the system date/time zone changes. */
interface TodayProvider {
  fun today(): LocalDate

  fun observe(): Flow<LocalDate>
}
