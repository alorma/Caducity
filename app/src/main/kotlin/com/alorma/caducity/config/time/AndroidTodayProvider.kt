package com.alorma.caducity.config.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.alorma.caducity.config.clock.AppClock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

class AndroidTodayProvider(
  private val context: Context,
  private val appClock: AppClock,
) : TodayProvider {
  override fun today(): LocalDate = appClock.nowDate()

  override fun observe(): Flow<LocalDate> =
    systemChanges()
      .onStart { emit(Unit) }
      .flatMapLatest { untilNextMidnight() }
      .distinctUntilChanged()

  /** Emits today and, at midnight, the next day (restarted when the system date or zone changes). */
  private fun untilNextMidnight(): Flow<LocalDate> =
    flow {
      while (true) {
        val timeZone = TimeZone.currentSystemDefault()
        val today = appClock.nowDate(timeZone)
        emit(today)
        val nextMidnight: Instant = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        delay((nextMidnight - appClock.now()).inWholeMilliseconds.coerceAtLeast(1).milliseconds)
      }
    }

  private fun systemChanges(): Flow<Unit> =
    callbackFlow {
      val receiver =
        object : BroadcastReceiver() {
          override fun onReceive(
            context: Context,
            intent: Intent,
          ) {
            trySendBlocking(Unit)
          }
        }
      val filter =
        IntentFilter().apply {
          addAction(Intent.ACTION_DATE_CHANGED)
          addAction(Intent.ACTION_TIMEZONE_CHANGED)
          addAction(Intent.ACTION_TIME_CHANGED)
        }
      context.registerReceiver(receiver, filter)
      awaitClose { context.unregisterReceiver(receiver) }
    }
}
