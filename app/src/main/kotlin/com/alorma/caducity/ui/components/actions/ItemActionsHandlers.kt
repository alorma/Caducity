package com.alorma.caducity.ui.components.actions

/** What the sheet's buttons can ask for; the view model implements it, previews use [NoItemActionsHandlers]. */
interface ItemActionsHandlers {
  fun onSelect(count: Int)

  /** The g or mL a measured product spends (− / + selector and shortcuts). */
  fun onAmountSelect(amount: Int)

  fun onToggleMore()

  fun onConsume()

  fun onFreezeToggle()

  fun onQuickReschedule(option: QuickReschedule)

  fun onPickDate()

  fun onFixDate()

  fun onDiscardSelected()

  fun onDiscardAll()

  fun onEatAnyway()

  fun onEatAnywayRequested()

  fun onEdit()

  fun onTransform()

  fun onMoveClick()

  fun onMoveBack()

  fun onMove(categoryId: String)
}

object NoItemActionsHandlers : ItemActionsHandlers {
  override fun onSelect(count: Int) = Unit

  override fun onAmountSelect(amount: Int) = Unit

  override fun onToggleMore() = Unit

  override fun onConsume() = Unit

  override fun onFreezeToggle() = Unit

  override fun onQuickReschedule(option: QuickReschedule) = Unit

  override fun onPickDate() = Unit

  override fun onFixDate() = Unit

  override fun onDiscardSelected() = Unit

  override fun onDiscardAll() = Unit

  override fun onEatAnyway() = Unit

  override fun onEatAnywayRequested() = Unit

  override fun onEdit() = Unit

  override fun onTransform() = Unit

  override fun onMoveClick() = Unit

  override fun onMoveBack() = Unit

  override fun onMove(categoryId: String) = Unit
}
