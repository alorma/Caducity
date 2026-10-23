package com.alorma.caducity.ui.screen.add

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.FindProductUseCase
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.SetCategoryDurationUseCase
import com.alorma.caducity.domain.usecase.SetProductCanFreezeUseCase
import com.alorma.caducity.domain.usecase.SetProductUnitTypeUseCase
import com.alorma.caducity.domain.usecase.UpdateItemUseCase
import com.alorma.caducity.feature.tracking.AddCloseAction
import com.alorma.caducity.feature.tracking.AddQuickCategoryAction
import com.alorma.caducity.feature.tracking.ChangeSuggestedDurationAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.screen.category.quick.CategoryCreatedEvents
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

class AddViewModel(
  args: AddArgs,
  private val todayProvider: TodayProvider,
  private val addProduct: AddProductUseCase,
  private val getNameSuggestions: GetNameSuggestionsUseCase,
  private val getActiveItem: GetActiveItemUseCase,
  private val updateItem: UpdateItemUseCase,
  private val findProduct: FindProductUseCase,
  private val setProductCanFreeze: SetProductCanFreezeUseCase,
  private val setProductUnitType: SetProductUnitTypeUseCase,
  private val setCategoryDuration: SetCategoryDurationUseCase,
  observeCategories: ObserveCategoriesUseCase,
  categoryCreated: CategoryCreatedEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<AddNavigation, AddNavigationSideEffect, AddSideEffect>() {
  private val _state = MutableStateFlow(AddState(today = todayProvider.today(), categoryId = args.categoryId))
  val state: StateFlow<AddState> = _state.asStateFlow()

  /** The category was chosen by the user (or preselected): a known name must not overwrite it. */
  private var categoryPinned = args.categoryId != null
  private var suggestionJob: Job? = null
  private var productJob: Job? = null

  /** The user typed, picked or is editing the date: choosing a category no longer suggests one. */
  private var dateTouched = args.itemId != null

  /** A category just created in the sheet that the categories flow has not delivered yet. */
  private var pendingCategoryId: String? = null

  init {
    args.itemId?.let { startEdit(it) }
    viewModelScope.launch {
      observeCategories.observe().collect { categories ->
        _state.update { current ->
          val pending = pendingCategoryId?.takeIf { id -> categories.any { it.id == id } }
          if (pending != null) pendingCategoryId = null
          current
            .copy(
              categories = categories,
              categoryId = pending ?: current.categoryId?.takeIf { id -> categories.any { it.id == id } },
            ).withSuggestedDate()
        }
        refreshProduct()
      }
    }
    viewModelScope.launch { categoryCreated.events.collect(::onCategoryCreated) }
  }

  override fun navigate(navigation: AddNavigation) {
    when (navigation) {
      AddNavigation.Close -> {
        eventTracker.trackAction(AddCloseAction())
        emitNavigationSideEffect(AddNavigationSideEffect.Close)
      }

      AddNavigation.QuickCategory -> {
        eventTracker.trackAction(AddQuickCategoryAction())
        emitNavigationSideEffect(AddNavigationSideEffect.OpenQuickCategory)
      }
    }
  }

  fun onNameChange(name: String) {
    _state.update { it.copy(name = name, suggestion = null, isNewProduct = false) }
    suggestionJob?.cancel()
    refreshProduct()
    if (name.isBlank()) return

    suggestionJob =
      viewModelScope.launch {
        val typed = name.trim().normalizeForSearch()
        val suggestions = getNameSuggestions.suggest(name)
        val exact = suggestions.firstOrNull { it.name.normalizeForSearch() == typed }
        _state.update { current ->
          current
            .copy(
              suggestion = suggestions.firstOrNull { it !== exact },
              isNewProduct = suggestions.isEmpty(),
              categoryId =
                if (exact != null && !categoryPinned && current.categories.any { it.id == exact.categoryId }) {
                  exact.categoryId
                } else {
                  current.categoryId
                },
            ).withSuggestedDate()
        }
        // The exact match may have selected its category.
        refreshProduct()
      }
  }

  /** Tapping the suggestion fills name and category, never the date. */
  fun onSuggestionClick() {
    val suggestion = _state.value.suggestion ?: return
    suggestionJob?.cancel()
    categoryPinned = false
    _state.update {
      it
        .copy(
          name = suggestion.name,
          suggestion = null,
          isNewProduct = false,
          categoryId = suggestion.categoryId.takeIf { id -> it.categories.any { c -> c.id == id } } ?: it.categoryId,
        ).withSuggestedDate()
    }
    refreshProduct()
  }

  fun onClearName() = onNameChange("")

  /** «Suggerida … · Canvia»: the usual duration is edited in the category's Durada sheet. */
  fun onChangeSuggestedDuration() {
    val category = _state.value.category ?: return
    eventTracker.trackAction(ChangeSuggestedDurationAction())
    emitSideEffect(AddSideEffect.ShowCategoryDurationSheet(category.name))
  }

  /** The category's flow delivers the new value, which recalculates the suggested date while it is untouched. */
  fun onCategoryDurationChange(days: Int?) {
    val categoryId = _state.value.categoryId ?: return
    viewModelScope.launch {
      setCategoryDuration.set(categoryId, days).onFailure { emitSideEffect(AddSideEffect.ShowSaveFailed) }
    }
  }

  fun onCategorySelect(categoryId: String) {
    categoryPinned = true
    _state.update { it.copy(categoryId = categoryId).withSuggestedDate() }
    refreshProduct()
  }

  /** The quick category sheet created [categoryId]: it is left selected. */
  private fun onCategoryCreated(categoryId: String) {
    categoryPinned = true
    if (_state.value.categories.any { it.id == categoryId }) {
      _state.update { it.copy(categoryId = categoryId).withSuggestedDate() }
      refreshProduct()
    } else {
      pendingCategoryId = categoryId
    }
  }

  fun onDateTextChange(text: String) {
    dateTouched = true
    setDigits(ExpiryDateParser.normalize(text))
  }

  fun onQuickDate(quickDate: QuickDate) {
    dateTouched = true
    setDate(quickDate.from(_state.value.today))
  }

  fun onPickDate() {
    val current = _state.value.date ?: _state.value.today
    emitSideEffect(AddSideEffect.ShowDatePicker(current.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()))
  }

  fun onDatePicked(date: LocalDate) {
    dateTouched = true
    setDate(date)
  }

  fun onQuantityChange(quantity: Int) {
    _state.update { it.copy(quantity = quantity.coerceIn(1, AddProductUseCase.MAX_QUANTITY)) }
  }

  fun onFrozenChange(frozen: Boolean) {
    _state.update { it.copy(frozen = frozen) }
  }

  /**
   * The user flipped «Es pot congelar»: it becomes an override of the product. Choice: the override is kept when the
   * category or the name change later in the same session, because the user decided it explicitly; only an untouched
   * switch re-derives its value from the category / existing product.
   */
  fun onCanFreezeChange(canFreeze: Boolean) {
    _state.update { it.copy(canFreezeOverride = canFreeze) }
  }

  fun onUnitTypeClick() {
    emitSideEffect(AddSideEffect.ShowUnitTypePicker(_state.value.unitType))
  }

  /** The type is a property of the product: it is saved with it, and applies to every unit. */
  fun onUnitTypeChange(unitType: UnitType) {
    _state.update {
      val startsMeasured = unitType.parent == UnitParent.MEASURED && it.unitType.parent != UnitParent.MEASURED
      it.copy(
        unitTypeOverride = unitType,
        amount = if (startsMeasured) unitType.defaultAmount ?: it.amount else it.amount,
      )
    }
  }

  fun onLevelChange(level: HandfulLevel) {
    _state.update { it.copy(level = level) }
  }

  fun onAmountChange(amount: Int) {
    _state.update { it.copy(amount = amount.coerceIn(1, UnitType.MAX_AMOUNT)) }
  }

  /** Looks up the override and the unit type of the existing product that matches the typed name in the selected category. */
  private fun refreshProduct() {
    productJob?.cancel()
    val name = _state.value.name.trim()
    val categoryId = _state.value.categoryId
    if (name.isEmpty() || categoryId == null) {
      _state.update { it.copy(productCanFreeze = null, productUnitType = null) }
      return
    }
    productJob =
      viewModelScope.launch {
        val found = findProduct.find(categoryId, name)
        _state.update {
          if (it.name.trim() == name && it.categoryId == categoryId) {
            it.copy(productCanFreeze = found?.canFreeze, productUnitType = found?.unitType)
          } else {
            it
          }
        }
      }
  }

  /** "Desa": saves and closes. While editing a unit it updates that unit instead. */
  fun onSave() {
    val current = _state.value
    if (!current.canSave) return
    if (current.frozenWithPastDate) {
      emitSideEffect(AddSideEffect.ShowFrozenPastDate)
      emitSideEffect(AddSideEffect.Reject)
      return
    }
    val date = current.date ?: return
    val categoryId = current.categoryId ?: return
    val name = current.name.trim()
    val editItemId = current.editItemId

    _state.update { it.copy(saving = true) }
    viewModelScope.launch {
      val product: Result<String> =
        if (editItemId != null) {
          updateItem.update(editItemId, name, categoryId, date)
        } else {
          addProduct
            .add(
              name = name,
              categoryId = categoryId,
              date = date,
              quantity = current.quantity,
              frozen = current.frozenEffective,
              unitType = current.unitType,
              amount = current.amount.takeIf { current.isMeasured },
              level = current.level.takeIf { current.isHandful },
            ).map { it.productId }
        }
      // The overrides apply to the whole product, not only the unit, and is stored only if the user changed it.
      val saved: Result<Unit> =
        product.fold(
          onSuccess = { productId ->
            val canFreezeOverride = current.canFreezeOverride
            val unitTypeOverride = current.unitTypeOverride
            val freeze =
              if (canFreezeOverride !=
                null
              ) {
                setProductCanFreeze.set(productId, canFreezeOverride)
              } else {
                Result.success(Unit)
              }
            if (unitTypeOverride != null &&
              freeze.isSuccess
            ) {
              setProductUnitType.set(productId, unitTypeOverride)
            } else {
              freeze
            }
          },
          onFailure = { Result.failure(it) },
        )
      saved
        .onSuccess {
          emitSideEffect(AddSideEffect.Confirm)
          _state.update { it.copy(saving = false) }
          navigate(AddNavigation.Close)
        }.onFailure { error ->
          _state.update { it.copy(saving = false) }
          val pastFrozen = error is PantryError.PastDateWhenFrozen
          emitSideEffect(if (pastFrozen) AddSideEffect.ShowFrozenPastDate else AddSideEffect.ShowSaveFailed)
        }
    }
  }

  private fun startEdit(itemId: String) {
    viewModelScope.launch {
      val item = getActiveItem.get(itemId)
      if (item == null) {
        emitSideEffect(AddSideEffect.ShowSaveFailed)
        emitNavigationSideEffect(AddNavigationSideEffect.Close)
        return@launch
      }
      suggestionJob?.cancel()
      categoryPinned = true
      val date =
        item.remainingDays
          ?.takeIf { item.frozen }
          ?.let { _state.value.today.plus(DatePeriod(days = it)) }
          ?: item.expiration
      _state.update {
        it.copy(
          name = item.name,
          suggestion = null,
          isNewProduct = false,
          categoryId = item.categoryId,
          frozen = item.frozen,
          editItemId = itemId,
        )
      }
      setDate(date)
      refreshProduct()
    }
  }

  private fun setDate(date: LocalDate) {
    val digits = "%02d%02d%04d".format(date.dayOfMonth, date.monthNumber, date.year)
    setDigits(digits)
  }

  private fun setDigits(digits: String) {
    _state.update {
      it.copy(dateDigits = digits, date = ExpiryDateParser.parse(digits), suggestedDurationDays = null)
    }
  }

  /** Untouched date = today + the category's usual duration; a category without duration leaves it empty. */
  private fun AddState.withSuggestedDate(): AddState {
    if (dateTouched) return this
    val days = categories.firstOrNull { it.id == categoryId }?.usualDurationDays
    val suggested = days?.let { today.plus(DatePeriod(days = it)) }
    val digits = suggested?.let { "%02d%02d%04d".format(it.dayOfMonth, it.monthNumber, it.year) }.orEmpty()
    return copy(dateDigits = digits, date = suggested, suggestedDurationDays = days)
  }
}
