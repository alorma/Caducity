package com.alorma.caducity.ui.screen.status

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val previewToday = LocalDate(2026, 10, 11)

private val dairy = Category("dairy", "Làctics", "", CategoryShape.COOKIE, CategoryIcon("water_full"))
private val meat = Category("meat", "Carn i peix", "", CategoryShape.CLOVER, CategoryIcon("set_meal"))

private fun row(
  category: Category,
  name: String,
  days: Int,
  status: ExpiryStatus,
  units: Int = 1,
): StatusRow {
  val expiration = previewToday.plus(days, DateTimeUnit.DAY)
  val entries =
    List(units) { index ->
      PantryEntry(
        itemId = "$name-$index",
        productId = name,
        name = name,
        categoryId = category.id,
        expiration = expiration,
        status = status,
        frozenSince = if (status == ExpiryStatus.FROZEN) Instant.parse("2026-10-01T10:00:00Z") else null,
        remainingDays = days,
        createdAt = null,
      )
    }
  return StatusRow(
    key = "$name|$expiration",
    name = name,
    category = category,
    expiration = expiration,
    frozenSince = null,
    entries = entries,
  )
}

private fun sampleReady(status: ExpiryStatus): StatusListState.Ready {
  val days =
    when (status) {
      ExpiryStatus.EXPIRED -> listOf(-3, -1)
      ExpiryStatus.SOON -> listOf(1, 3)
      ExpiryStatus.FRESH -> listOf(6, 20)
      ExpiryStatus.FROZEN -> listOf(60, 90)
    }
  val sections =
    listOf(
      StatusSection(
        category = dairy,
        rows = listOf(row(dairy, "Llet", days[0], status, units = 2), row(dairy, "Iogurt", days[1], status)),
      ),
      StatusSection(category = meat, rows = listOf(row(meat, "Pollastre", days[1], status))),
    )
  return StatusListState.Ready(status, totalCount = sections.sumOf { it.rows.size }, sections = sections, soonDays = 3)
}

/** Llista d'estat amb dades d'exemple (per a les captures de test). */
@Composable
fun StatusListPreview(status: ExpiryStatus) {
  PreviewTheme {
    StatusListLayout(
      status = status,
      ready = sampleReady(status),
      today = previewToday,
      dialogState = rememberAppDialogState(),
      onBack = {},
      onRowClick = {},
      onConsume = {},
      onFreeze = {},
      onDiscard = {},
      onAdd = {},
      onDiscardAll = {},
    )
  }
}

@PreviewLightDark
@Composable
private fun StatusExpiredPreview() {
  StatusListPreview(ExpiryStatus.EXPIRED)
}

@PreviewLightDark
@Composable
private fun StatusSoonPreview() {
  StatusListPreview(ExpiryStatus.SOON)
}
