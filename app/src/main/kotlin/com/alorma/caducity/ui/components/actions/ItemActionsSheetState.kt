package com.alorma.caducity.ui.components.actions

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import kotlinx.datetime.LocalDate

/** One active unit of the product, as the sheet needs it. */
data class ActionUnit(
  val id: String,
  val expiration: LocalDate,
  val status: ExpiryStatus,
)

sealed interface ItemActionsSheetState {
  data object Loading : ItemActionsSheetState

  /** Every active unit of the product, [units] ordered by the one that expires first. */
  data class Ready(
    val name: String,
    val category: Category,
    val units: List<ActionUnit>,
    /** Units chosen with the − / + selector (multiple units only). */
    val selected: Int,
    val moreOpen: Boolean,
    /** Days until the nearest unit's date; negative when expired. */
    val daysLeft: Int,
    /** Other categories, offered by «Mou». */
    val otherCategories: List<Category>,
    val pickingCategory: Boolean = false,
  ) : ItemActionsSheetState {
    private val nearest: ActionUnit get() = units.first()

    val unitCount: Int get() = units.size
    val multiple: Boolean get() = units.size > 1
    val expiration: LocalDate get() = nearest.expiration
    val status: ExpiryStatus get() = nearest.status

    /** The nearest unit is expired: the sheet only offers to throw it out (or the «Més accions» escape hatches). */
    val expired: Boolean get() = status == ExpiryStatus.EXPIRED

    /** A single frozen unit: «Descongela». */
    val frozen: Boolean get() = !multiple && status == ExpiryStatus.FROZEN

    val expiredCount: Int get() = units.count { it.status == ExpiryStatus.EXPIRED }
    val freezableCount: Int
      get() = units.count { it.status == ExpiryStatus.SOON || it.status == ExpiryStatus.FRESH }

    /** Units «Congela N» freezes: the chosen number, or fewer if not that many can be frozen. */
    val freezeCount: Int get() = minOf(selected, freezableCount)
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
    val itemIds: List<String>,
  ) : ItemActionsSheetNavigationSideEffect
}

sealed interface ItemActionsSheetSideEffect {
  data object Confirm : ItemActionsSheetSideEffect

  data class ShowDatePicker(
    val currentMillis: Long,
    val purpose: DatePickerPurpose,
  ) : ItemActionsSheetSideEffect

  /** TalkBack route to «Consumit igualment», which sighted users do by holding. */
  data object ShowEatAnywayConfirm : ItemActionsSheetSideEffect
}

enum class DatePickerPurpose {
  Reschedule,
  Unfreeze,
  FixExpiredDate,
}

/** Quick offsets for «Canvia la data». */
enum class QuickReschedule(
  val days: Int,
) {
  Day(1),
  ThreeDays(3),
  Week(7),
}
