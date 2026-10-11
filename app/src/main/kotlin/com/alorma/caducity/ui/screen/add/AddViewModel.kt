package com.alorma.caducity.ui.screen.add

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.AddedBatch
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.UndoAddItemsUseCase
import com.alorma.caducity.domain.usecase.UpdateItemUseCase
import com.alorma.caducity.feature.tracking.AddCloseAction
import com.alorma.caducity.feature.tracking.AddQuickCategoryAction
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
  private val addItemBatch: AddItemBatchUseCase,
  private val undoAddItems: UndoAddItemsUseCase,
  private val getNameSuggestions: GetNameSuggestionsUseCase,
  private val getActiveItem: GetActiveItemUseCase,
  private val updateItem: UpdateItemUseCase,
  observeCategories: ObserveCategoriesUseCase,
  categoryCreated: CategoryCreatedEvents,
  private val eventTracker: EventTracker,
) : BaseViewModel<AddNavigation, AddNavigationSideEffect, AddSideEffect>() {
  private val _state = MutableStateFlow(AddState(today = todayProvider.today(), categoryId = args.categoryId))
  val state: StateFlow<AddState> = _state.asStateFlow()

  /** The category was chosen by the user (or preselected): a known name must not overwrite it. */
  private var categoryPinned = args.categoryId != null
  private var suggestionJob: Job? = null

  /** The form as it was before editing a unit picked from «En aquesta tanda». */
  private var draft: AddState? = null

  /** A category just created in the sheet that the categories flow has not delivered yet. */
  private var pendingCategoryId: String? = null

  init {
    args.itemId?.let { startEdit(it, entry = null) }
    viewModelScope.launch {
      observeCategories.observe().collect { categories ->
        _state.update { current ->
          val pending = pendingCategoryId?.takeIf { id -> categories.any { it.id == id } }
          if (pending != null) pendingCategoryId = null
          current.copy(
            categories = categories,
            categoryId = pending ?: current.categoryId?.takeIf { id -> categories.any { it.id == id } },
          )
        }
      }
    }
    viewModelScope.launch { categoryCreated.events.collect(::onCategoryCreated) }
  }

  override fun navigate(navigation: AddNavigation) {
    when (navigation) {
      AddNavigation.Close -> {
        if (_state.value.editing?.entry != null) {
          cancelEdit()
          return
        }
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
  private fun onCategoryCreated(categoryId: String) {
    categoryPinned = true
    if (_state.value.categories.any { it.id == categoryId }) {
      _state.update { it.copy(categoryId = categoryId) }
    } else {
      pendingCategoryId = categoryId
    }
  }

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

  /** Tapping a chip of the batch edits its first unit; the rest of the form is restored afterwards. */
  fun onEditEntry(entry: AddedEntry) {
    val itemId = entry.batch.itemIds.firstOrNull() ?: return
    startEdit(itemId, entry)
  }

  private fun startEdit(
    itemId: String,
    entry: AddedEntry?,
  ) {
    viewModelScope.launch {
      val item = getActiveItem.get(itemId)
      if (item == null) {
        emitSideEffect(AddSideEffect.ShowSaveFailed)
        if (entry == null) emitNavigationSideEffect(AddNavigationSideEffect.Close)
        return@launch
      }
      suggestionJob?.cancel()
      if (_state.value.editing == null && entry != null) draft = _state.value
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
          quantity = entry?.quantity ?: 1,
          editing = EditTarget(itemId, entry),
        )
      }
      setDate(date)
    }
  }

  private fun cancelEdit() {
    val restored = draft ?: return
    draft = null
    categoryPinned = false
    _state.update { restored.copy(added = it.added, categories = it.categories, editing = null) }
  }

  private fun saveEdit(edit: EditTarget) {
    val current = _state.value
    if (!current.canSave) {
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

    val quantity = current.quantity

    _state.update { it.copy(saving = true) }
    viewModelScope.launch {
      applyEdit(edit, name, categoryId, date, quantity)
        .onSuccess { updatedEntry ->
          emitSideEffect(AddSideEffect.Confirm)
          val entry = edit.entry
          if (entry == null || updatedEntry == null) {
            _state.update { it.copy(saving = false) }
            navigate(AddNavigation.Close)
          } else {
            val base = draft
            draft = null
            categoryPinned = false
            _state.update { cur ->
              (base ?: cur.copy(name = "")).copy(
                added = cur.added.map { if (it == entry) updatedEntry else it },
                categories = cur.categories,
                editing = null,
                saving = false,
              )
            }
          }
        }.onFailure { error ->
          _state.update { it.copy(saving = false) }
          val pastFrozen = error is PantryError.PastDateWhenFrozen
          emitSideEffect(if (pastFrozen) AddSideEffect.ShowFrozenPastDate else AddSideEffect.ShowSaveFailed)
        }
    }
  }

  /**
   * Edits one unit (route) or, for a batch chip, every unit of the entry plus its quantity: extra units are
   * added with the same data and surplus ones removed. Returns the updated entry for a chip edit, null otherwise.
   */
  private suspend fun applyEdit(
    edit: EditTarget,
    name: String,
    categoryId: String,
    date: LocalDate,
    quantity: Int,
  ): Result<AddedEntry?> {
    val entry = edit.entry
    if (entry == null) {
      return updateItem.update(edit.itemId, name, categoryId, date).map { null }
    }
    var ids = entry.batch.itemIds
    for (id in ids) {
      updateItem.update(id, name, categoryId, date).onFailure { return Result.failure(it) }
    }
    var productId = entry.batch.productId
    val difference = quantity - ids.size
    if (difference > 0) {
      val added =
        addItemBatch
          .add(name = name, categoryId = categoryId, date = date, quantity = difference, frozen = false)
          .getOrElse { return Result.failure(it) }
      productId = added.productId
      ids = ids + added.itemIds
    } else if (difference < 0) {
      val surplus = ids.takeLast(-difference)
      undoAddItems
        .undo(AddedBatch(productId = productId, itemIds = surplus, productCreated = false))
        .onFailure { return Result.failure(it) }
      ids = ids.dropLast(-difference)
    }
    return Result.success(
      entry.copy(
        batch = AddedBatch(productId = productId, itemIds = ids, productCreated = entry.batch.productCreated),
        name = name,
        date = date,
        quantity = ids.size,
      ),
    )
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
    current.editing?.let { edit ->
      if (another) emitSideEffect(AddSideEffect.Reject) else saveEdit(edit)
      return
    }
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
