package com.alorma.caducity.ui.components.actions

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.datetime.LocalDate

private val dairy = Category("dairy", "Dairy", "", CategoryShape.COOKIE, CategoryIcon("local_drink"), 0)

private fun unit(
  id: String,
  day: Int,
  status: ExpiryStatus,
) = ActionUnit(id, LocalDate(2026, 10, day), status)

private fun ready(
  units: List<ActionUnit>,
  selected: Int = 1,
  moreOpen: Boolean = false,
  daysLeft: Int,
) = ItemActionsSheetState.Ready(
  name = "Greek yogurt",
  category = dairy,
  units = units,
  selected = selected,
  moreOpen = moreOpen,
  daysLeft = daysLeft,
  otherCategories = emptyList(),
)

@Composable
private fun SheetPreview(state: ItemActionsSheetState.Ready) {
  PreviewTheme {
    Surface {
      ActionsContent(state = state, actions = NoItemActionsHandlers)
    }
  }
}

@PreviewDynamicLightDark
@Composable
internal fun ItemActionsOneUnitPreview() {
  SheetPreview(ready(units = listOf(unit("a", 13, ExpiryStatus.SOON)), daysLeft = 2))
}

@PreviewDynamicLightDark
@Composable
internal fun ItemActionsSeveralUnitsPreview() {
  SheetPreview(
    ready(
      units =
        listOf(
          unit("a", 13, ExpiryStatus.SOON),
          unit("b", 13, ExpiryStatus.SOON),
          unit("c", 18, ExpiryStatus.FRESH),
          unit("d", 22, ExpiryStatus.FRESH),
        ),
      selected = 2,
      daysLeft = 2,
    ),
  )
}

@PreviewDynamicLightDark
@Composable
internal fun ItemActionsExpiredOneUnitPreview() {
  SheetPreview(
    ready(units = listOf(unit("a", 9, ExpiryStatus.EXPIRED)), moreOpen = true, daysLeft = -2),
  )
}

@PreviewDynamicLightDark
@Composable
internal fun ItemActionsExpiredSeveralUnitsPreview() {
  SheetPreview(
    ready(
      units =
        listOf(
          unit("a", 9, ExpiryStatus.EXPIRED),
          unit("b", 10, ExpiryStatus.EXPIRED),
          unit("c", 14, ExpiryStatus.SOON),
        ),
      selected = 2,
      daysLeft = -2,
    ),
  )
}
