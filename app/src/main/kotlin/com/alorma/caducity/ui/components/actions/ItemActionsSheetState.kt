package com.alorma.caducity.ui.components.actions

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import kotlinx.datetime.LocalDate

/** One active unit of the product, as the sheet needs it. */
data class ActionUnit(
  val id: String,
  val expiration: LocalDate,
  val status: ExpiryStatus,
  /** Day it went into the freezer; null if it is not frozen. */
  val frozenSince: LocalDate? = null,
  /** Amount of a measured batch (g or mL); null for units. */
  val amount: Int? = null,
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
    /** The product can go in the freezer; when false the sheet has no «Congela». */
    val canFreeze: Boolean = true,
    /** How the product is counted. */
    val unitType: UnitType = UnitType.DEFAULT,
    /** g or mL the buttons act on, for a measured product (the − / + selector). */
    val amountSpend: Int = 0,
  ) : ItemActionsSheetState {
    private val nearest: ActionUnit get() = units.first()

    /** A measured product: one batch with an amount instead of units. */
    val measured: Boolean get() = unitType.parent == UnitParent.MEASURED

    /** Amount of the batch the buttons act on (the one that expires first). */
    val batchAmount: Int get() = nearest.amount ?: 0

    val unitCount: Int get() = units.size
    val multiple: Boolean get() = units.size > 1
    val expiration: LocalDate get() = nearest.expiration
    val status: ExpiryStatus get() = nearest.status

    /** The nearest unit is expired: the sheet only offers to throw it out (or the «Més accions» escape hatches). */
    val expired: Boolean get() = status == ExpiryStatus.EXPIRED

    /** A single frozen unit: «Descongela». */
    val frozen: Boolean get() = !multiple && status == ExpiryStatus.FROZEN

    /** Every unit is frozen: the sheet offers «Descongela» instead of «Congela» and «Canvia la data». */
    val frozenOnly: Boolean get() = units.all { it.status == ExpiryStatus.FROZEN }

    /** Day the unit frozen the longest went into the freezer. */
    val frozenSince: LocalDate? get() = nearest.frozenSince

    val expiredCount: Int get() = units.count { it.status == ExpiryStatus.EXPIRED }
    val freezableCount: Int
      get() = if (canFreeze) units.count { it.status == ExpiryStatus.SOON || it.status == ExpiryStatus.FRESH } else 0

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
