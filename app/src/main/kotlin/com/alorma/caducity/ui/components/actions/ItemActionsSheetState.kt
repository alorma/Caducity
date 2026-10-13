package com.alorma.caducity.ui.components.actions

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import kotlinx.datetime.LocalDate

sealed interface ItemActionsSheetState {
  data object Loading : ItemActionsSheetState

  data class Ready(
    val name: String,
    val category: Category,
    /** Position of this unit among its product's units (1-based) and how many there are. */
    val unitIndex: Int,
    val unitCount: Int,
    /** Legacy pack size (`> 1`), shown as «pack de N». */
    val pack: Int?,
    val expiration: LocalDate,
    val status: ExpiryStatus,
    /** Days until [expiration]; negative when expired. */
    val daysLeft: Int,
    /** Other categories, offered by «Mou». */
    val otherCategories: List<Category>,
    val pickingCategory: Boolean = false,
  ) : ItemActionsSheetState {
    val frozen: Boolean get() = status == ExpiryStatus.FROZEN
  }
}

sealed interface ItemActionsSheetNavigation {
  data object Close : ItemActionsSheetNavigation

  data object Edit : ItemActionsSheetNavigation

  data object Transform : ItemActionsSheetNavigation
}

sealed interface ItemActionsSheetNavigationSideEffect {
  data object Close : ItemActionsSheetNavigationSideEffect

  data class Edit(
    val itemId: String,
  ) : ItemActionsSheetNavigationSideEffect

  data class Transform(
    val itemId: String,
  ) : ItemActionsSheetNavigationSideEffect
}

sealed interface ItemActionsSheetSideEffect {
  data object Confirm : ItemActionsSheetSideEffect

  data class ShowDatePicker(
    val currentMillis: Long,
    val unfreeze: Boolean,
  ) : ItemActionsSheetSideEffect

  data class ShowDeleteConfirm(
    val name: String,
  ) : ItemActionsSheetSideEffect

  data class ShowQuantitySelector(
    val max: Int,
    val name: String,
    val category: Category,
    val expiration: LocalDate,
    val daysLeft: Int,
  ) : ItemActionsSheetSideEffect
}

/** Quick offsets for «Canvia la data». */
enum class QuickReschedule(
  val days: Int,
) {
  Day(1),
  ThreeDays(3),
  Week(7),
}
