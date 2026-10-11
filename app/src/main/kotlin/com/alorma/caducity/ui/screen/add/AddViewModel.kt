package com.alorma.caducity.ui.screen.add

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.UndoAddItemsUseCase
import com.alorma.caducity.feature.tracking.AddCloseAction
import com.alorma.caducity.feature.tracking.AddQuickCategoryAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class AddViewModel(
  initialCategoryId: String?,
  private val todayProvider: TodayProvider,
  private val addItemBatch: AddItemBatchUseCase,
  private val undoAddItems: UndoAddItemsUseCase,
  private val getNameSuggestions: GetNameSuggestionsUseCase,
  observeCategories: ObserveCategoriesUseCase,
  private val eventTracker: EventTracker,
) : BaseViewModel<AddNavigation, AddNavigationSideEffect, AddSideEffect>() {
  private val _state = MutableStateFlow(AddState(today = todayProvider.today(), categoryId = initialCategoryId))
  val state: StateFlow<AddState> = _state.asStateFlow()

  /** The category was chosen by the user (or preselected): a known name must not overwrite it. */
  private var categoryPinned = initialCategoryId != null
  private var suggestionJob: Job? = null

  init {
    viewModelScope.launch {
      observeCategories.observe().collect { categories ->
        _state.update { current ->
          current.copy(
            categories = categories,
            categoryId = current.categoryId?.takeIf { id -> categories.any { it.id == id } },
          )
        }
      }
    }
  }

  override fun navigate(navigation: AddNavigation) {
    when (navigation) {
      AddNavigation.Close -> {
        eventTracker.trackAction(AddCloseAction(batch = _state.value.isBatchMode))
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
    if (name.isBlank()) return

    suggestionJob =
      viewModelScope.launch {
        val typed = name.trim().normalizeForSearch()
        val suggestions = getNameSuggestions.suggest(name)
        val exact = suggestions.firstOrNull { it.name.normalizeForSearch() == typed }
        _state.update { current ->
          current.copy(
            suggestion = suggestions.firstOrNull { it !== exact },
            isNewProduct = suggestions.isEmpty(),
            categoryId =
              if (exact != null && !categoryPinned && current.categories.any { it.id == exact.categoryId }) {
                exact.categoryId
              } else {
                current.categoryId
              },
          )
        }
      }
  }

  /** Tapping the suggestion fills name and category, never the date. */
  fun onSuggestionClick() {
    val suggestion = _state.value.suggestion ?: return
    suggestionJob?.cancel()
    categoryPinned = false
    _state.update {
      it.copy(
        name = suggestion.name,
        suggestion = null,
        isNewProduct = false,
        categoryId = suggestion.categoryId.takeIf { id -> it.categories.any { c -> c.id == id } } ?: it.categoryId,
      )
    }
  }

  fun onClearName() = onNameChange("")

  fun onCategorySelect(categoryId: String) {
    categoryPinned = true
    _state.update { it.copy(categoryId = categoryId) }
  }

  /** The quick category sheet created [categoryId]: it is left selected. */
  fun onCategoryCreated(categoryId: String) = onCategorySelect(categoryId)

  fun onDateTextChange(text: String) = setDigits(ExpiryDateParser.normalize(text))

  fun onQuickDate(quickDate: QuickDate) = setDate(quickDate.from(_state.value.today))

  fun onPickDate() {
    val current = _state.value.date ?: _state.value.today
    emitSideEffect(AddSideEffect.ShowDatePicker(current.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()))
  }

  fun onDatePicked(date: LocalDate) = setDate(date)

  fun onQuantityChange(quantity: Int) {
    _state.update { it.copy(quantity = quantity.coerceIn(1, AddItemBatchUseCase.MAX_QUANTITY)) }
  }

  fun onFrozenChange(frozen: Boolean) {
    _state.update { it.copy(frozen = frozen) }
  }

  /** "Desa": saves and closes; in batch mode with the name empty it is "Fet". */
  fun onSave() = save(another = false)

  /** "Desa i un altre": saves, clears the name and keeps category and date. */
  fun onSaveAndAnother() = save(another = true)

  /** The snackbar "Desfés" of the entry [entry]. */
  fun onUndo(entry: AddedEntry) {
    viewModelScope.launch {
      undoAddItems.undo(entry.batch).onSuccess {
        _state.update { current -> current.copy(added = current.added - entry) }
      }
    }
  }

  private fun setDate(date: LocalDate) {
    val digits = "%02d%02d%04d".format(date.dayOfMonth, date.monthNumber, date.year)
    setDigits(digits)
  }

  private fun setDigits(digits: String) {
    _state.update { it.copy(dateDigits = digits, date = ExpiryDateParser.parse(digits)) }
  }

  private fun save(another: Boolean) {
    val current = _state.value
    if (current.isDone && !another) {
      emitSideEffect(AddSideEffect.Confirm)
      navigate(AddNavigation.Close)
      return
    }
    if (!current.canSave || (another && current.isDone)) {
      emitSideEffect(AddSideEffect.Reject)
      return
    }
    if (current.frozenWithPastDate) {
      emitSideEffect(AddSideEffect.ShowFrozenPastDate)
      emitSideEffect(AddSideEffect.Reject)
      return
    }
    val date = current.date ?: return
    val categoryId = current.categoryId ?: return
    val name = current.name.trim()

    _state.update { it.copy(saving = true) }
    viewModelScope.launch {
      addItemBatch
        .add(name = name, categoryId = categoryId, date = date, quantity = current.quantity, frozen = current.frozen)
        .onSuccess { batch ->
          val entry = AddedEntry(batch = batch, name = name, date = date, quantity = current.quantity)
          emitSideEffect(AddSideEffect.Confirm)
          if (another) {
            categoryPinned = false
            _state.update {
              it.copy(
                name = "",
                suggestion = null,
                isNewProduct = false,
                quantity = 1,
                frozen = false,
                added = it.added + entry,
                saving = false,
              )
            }
            emitSideEffect(AddSideEffect.ShowSaved(entry))
          } else {
            _state.update { it.copy(saving = false) }
            navigate(AddNavigation.Close)
          }
        }.onFailure {
          _state.update { it.copy(saving = false) }
          emitSideEffect(AddSideEffect.ShowSaveFailed)
        }
    }
  }
}
