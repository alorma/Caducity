package com.alorma.caducity.ui.screen.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryData
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategorySection
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.toProductGroups
import com.alorma.caducity.domain.usecase.WeekStripData
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val today = LocalDate(2026, 10, 11)

private fun entry(
  name: String,
  status: ExpiryStatus,
  days: Int,
  units: Int = 1,
): List<PantryEntry> =
  List(units) { index ->
    PantryEntry(
      itemId = "$name-$index",
      productId = name,
      name = name,
      categoryId = "dairy",
      expiration = today.plus(DatePeriod(days = days)),
      status = status,
      frozenSince = null,
      remainingDays = days.takeIf { status == ExpiryStatus.FROZEN },
      createdAt = null,
    )
  }

/** Categoria amb dades d'exemple (sense tema, per a les captures de test). */
@Composable
internal fun CategoryPreviewContent() {
  val category = Category("dairy", "Làctics", "", CategoryShape.COOKIE, CategoryIcon("water_full"))
  val sections =
    listOf(
      CategorySection(ExpiryStatus.EXPIRED, entry("Iogurt grec", ExpiryStatus.EXPIRED, -2, 3).toProductGroups()),
      CategorySection(
        ExpiryStatus.SOON,
        (entry("Llet sencera", ExpiryStatus.SOON, 0) + entry("Mozzarella", ExpiryStatus.SOON, 1)).toProductGroups(),
      ),
      CategorySection(ExpiryStatus.FRESH, entry("Formatge cheddar", ExpiryStatus.FRESH, 10, 3).toProductGroups()),
      CategorySection(ExpiryStatus.FROZEN, entry("Mantega", ExpiryStatus.FROZEN, 60).toProductGroups()),
    )
  val data =
    CategoryData(
      today = today,
      category = category,
      productCount = sections.sumOf { it.productCount },
      nearest = today,
      statusCounts = sections.associate { it.status to it.productCount },
      sections = sections,
    )
  val state =
    CategoryState.Ready(
      data = data,
      weekStrip = WeekStripData(today, emptyMap()),
      filter = null,
      selectedDay = null,
      highlightProductId = null,
    )
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  Column(modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)) {
    CategoryTopBar(
      ready = state,
      scrollBehavior = scrollBehavior,
      onBack = {},
      onCalendar = {},
      onEdit = {},
      onDelete = {},
    )
    CategoryContent(state, {}, {}, {}, {}, {}, {}, {})
  }
}
