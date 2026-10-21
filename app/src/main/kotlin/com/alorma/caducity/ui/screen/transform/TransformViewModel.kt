package com.alorma.caducity.ui.screen.transform

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.R
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.ActiveItem
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.IngredientSpend
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.TransformResult
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.TransformItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToIngredientPickerAction
import com.alorma.caducity.feature.tracking.TransformCloseAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.snackbar.ScreenResults
import com.alorma.caducity.ui.components.feedback.snackbar.SnackbarRequest
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerResults
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class TransformViewModel(
  private val args: TransformArgs,
  todayProvider: TodayProvider,
  private val getActiveItem: GetActiveItemUseCase,
  private val observeItems: ObserveItemsUseCase,
  private val getNameSuggestions: GetNameSuggestionsUseCase,
  private val transformItems: TransformItemsUseCase,
  observeCategories: ObserveCategoriesUseCase,
  private val screenResults: ScreenResults,
  private val eventTracker: EventTracker,
  private val pickerResults: IngredientPickerResults,
) : BaseViewModel<TransformNavigation, TransformNavigationSideEffect, TransformSideEffect>() {
  // Left for the screen underneath only once this one is gone: while it is still on top, its own scaffold
  // would take the snackbar and it would leave with it.
  private var pendingResult: SnackbarRequest? = null

  internal fun flushResult() {
    pendingResult?.let(screenResults::post)
    pendingResult = null
  }

  override fun onCleared() {
    flushResult()
  }

  private val _state =
    MutableStateFlow(TransformState(today = todayProvider.today(), freeEntry = args.itemIds.isEmpty()))
  val state: StateFlow<TransformState> = _state.asStateFlow()

  private var nextResultId = 1
  private val suggestionJobs = mutableMapOf<Int, Job>()

  /** Ingredients as they were first shown: a different list or count counts as a change to discard. */
  private var initialIngredients: List<Pair<String, Int>>? = null

  init {
    viewModelScope.launch {
      observeCategories.observe().collect { categories ->
        _state.update { it.copy(categories = categories) }
      }
    }
    viewModelScope.launch { pickerResults.results.collect(::onIngredientsPicked) }
    viewModelScope.launch {
      val active = getActiveItem.all().associateBy { it.itemId }
      val picked = args.itemIds.mapNotNull { active[it] }
      val ingredients = picked.toIngredients(active.values.toList(), unitStatuses())
      initialIngredients = ingredients.signature()
      // The picker can answer before this load ends (free entry starts empty): what it brought is not overwritten.
      _state.update { if (it.ingredients.isEmpty()) it.copy(ingredients = ingredients) else it }
    }
  }

  override fun navigate(navigation: TransformNavigation) {
    when (navigation) {
      TransformNavigation.Close -> {
        eventTracker.trackAction(TransformCloseAction())
        emitNavigationSideEffect(TransformNavigationSideEffect.Close)
      }
      TransformNavigation.IngredientPicker -> {
        eventTracker.trackAction(NavigateToIngredientPickerAction())
        val selected = _state.value.ingredients.flatMap { it.units.map { unit -> unit.itemId } }
        emitNavigationSideEffect(TransformNavigationSideEffect.OpenIngredientPicker(selected))
      }
    }
  }

  /** The × of the top bar: asks first when there is something to lose. */
  fun onCloseRequest() {
    if (hasChanges()) emitSideEffect(TransformSideEffect.ShowDiscardConfirm) else navigate(TransformNavigation.Close)
  }

  fun onDiscardConfirmed() = navigate(TransformNavigation.Close)

  /** The picker's selection is the final list: the ones left out go, the ones already there keep their count. */
  private fun onIngredientsPicked(itemIds: List<String>) {
    viewModelScope.launch {
      val active = getActiveItem.all()
      val byId = active.associateBy { it.itemId }
      val picked = itemIds.mapNotNull { byId[it] }.toIngredients(active, unitStatuses())
      if (picked.isEmpty()) return@launch
      _state.update { current ->
        val pickedKeys = picked.map { it.key }.toSet()
        val known = current.ingredients.map { it.key }.toSet()
        current.copy(
          ingredients = current.ingredients.filter { it.key in pickedKeys } + picked.filter { it.key !in known },
        )
      }
    }
  }

  /** g or mL a measured ingredient spends: at least 1, at most all it has. */
  fun onIngredientAmountChange(
    key: String,
    amount: Int,
  ) {
    _state.update { current ->
      current.copy(
        ingredients =
          current.ingredients.map {
            if (it.key == key && it.measured) it.copy(amount = amount.coerceIn(1, it.totalAmount)) else it
          },
      )
    }
  }

  fun onIngredientCountChange(
    key: String,
    count: Int,
  ) {
    _state.update { current ->
      current.copy(
        ingredients =
          current.ingredients.map {
            if (it.key == key && !it.measured) {
              it.copy(count = count.coerceIn(1, it.available))
            } else {
              it
            }
          },
      )
    }
  }

  /** Removing the last ingredient leaves nothing to transform: it confirms and closes, except in free entry. */
  fun onRemoveIngredient(key: String) {
    if (!_state.value.freeEntry &&
      _state.value.ingredients
        .singleOrNull()
        ?.key == key
    ) {
      emitSideEffect(TransformSideEffect.ShowDiscardConfirm)
      return
    }
    _state.update { current -> current.copy(ingredients = current.ingredients.filterNot { it.key == key }) }
  }

  fun onAddResult() {
    if (!_state.value.canAddResult) return
    val id = nextResultId++
    _state.update { it.copy(results = it.results + ResultUi(id = id), expandedResultId = id, focusResultId = id) }
  }

  fun onRemoveResult(id: Int) {
    if (_state.value.results.size <= 1) return
    suggestionJobs.remove(id)?.cancel()
    val before = _state.value
    val index = before.results.indexOfFirst { it.id == id }
    if (index < 0) return
    removedResult = RemovedResult(before.results[index], index, before.expandedResultId)
    _state.update { current ->
      val results = current.results.filterNot { it.id == id }
      current.copy(
        results = results,
        // The one left is always open; otherwise the open one stays, or the first takes over if it was removed.
        expandedResultId =
          if (results.size == 1 || current.expandedResultId == id) results.first().id else current.expandedResultId,
      )
    }
    emitSideEffect(TransformSideEffect.ShowResultRemoved)
  }

  /** «Desfés» on the snackbar of a removed result: it comes back where it was, and the selection with it. */
  fun onUndoRemoveResult() {
    val removed = removedResult ?: return
    removedResult = null
    _state.update { current ->
      val results = current.results.toMutableList().apply { add(removed.index.coerceAtMost(size), removed.result) }
      current.copy(results = results, expandedResultId = removed.expandedResultId)
    }
  }

  private class RemovedResult(
    val result: ResultUi,
    val index: Int,
    val expandedResultId: Int,
  )

  private var removedResult: RemovedResult? = null

  /** Expands [id]; the one that was open folds, so at most one is expanded. Does nothing while the open one lacks something. */
  fun onSelectResult(id: Int) {
    _state.update { current ->
      val open = current.results.firstOrNull { it.id == current.expandedResultId }
      if (open != null && open.id != id && !current.isValid(open)) current else current.copy(expandedResultId = id)
    }
  }

  /** «Fet»: folds the open result, so all of them are small cards. Validates nothing. */
  fun onDone() {
    _state.update { it.copy(expandedResultId = NONE_EXPANDED, focusResultId = null) }
  }

  fun onNameChange(
    id: Int,
    name: String,
  ) {
    updateResult(id) {
      it.copy(name = name, suggestion = null, historyCategoryId = null, productCanFreeze = emptyMap())
    }
    suggestionJobs.remove(id)?.cancel()
    if (name.isBlank()) return
    suggestionJobs[id] =
      viewModelScope.launch {
        val typed = name.trim().normalizeForSearch()
        val all = getNameSuggestions.suggest(name, limit = Int.MAX_VALUE)
        val suggestions = all.take(GetNameSuggestionsUseCase.DEFAULT_LIMIT)
        val matches = all.filter { it.name.normalizeForSearch() == typed }
        val exact = suggestions.firstOrNull { it.name.normalizeForSearch() == typed } ?: matches.firstOrNull()
        updateResult(id) {
          it.copy(
            suggestion =
              suggestions.firstOrNull { s ->
                s !== exact
              },
            historyCategoryId = exact?.categoryId,
            productCanFreeze = matches.associate { m -> m.categoryId to m.canFreeze },
          )
        }
      }
  }

  /** Tapping the suggestion fills the name and takes the category it already has. */
  fun onSuggestionClick(id: Int) {
    val suggestion =
      _state.value.results
        .firstOrNull { it.id == id }
        ?.suggestion ?: return
    suggestionJobs.remove(id)?.cancel()
    updateResult(id) {
      it.copy(
        name = suggestion.name,
        suggestion = null,
        historyCategoryId = suggestion.categoryId,
        productCanFreeze = mapOf(suggestion.categoryId to suggestion.canFreeze),
      )
    }
  }

  fun onServingsChange(
    id: Int,
    servings: Int,
  ) {
    val clamped = servings.coerceIn(1, TransformItemsUseCase.MAX_SERVINGS)
    updateResult(id) { it.copy(servings = clamped, frozenCount = it.frozenCount.coerceAtMost(clamped)) }
  }

  fun onUnitTypeClick(id: Int) {
    val result = _state.value.results.firstOrNull { it.id == id } ?: return
    emitSideEffect(TransformSideEffect.ShowUnitTypePicker(id, result.unitType))
  }

  /** A measured result is a single batch: one serving, with an amount that starts at the default. */
  fun onUnitTypeChange(
    id: Int,
    unitType: UnitType,
  ) = updateResult(id) {
    val measured = unitType.parent == UnitParent.MEASURED
    val servings = if (measured) 1 else it.servings
    it.copy(
      unitTypeOverride = unitType,
      servings = servings,
      amount = if (measured && !it.measured) unitType.defaultAmount ?: it.amount else it.amount,
      frozenCount = it.frozenCount.coerceAtMost(servings),
    )
  }

  fun onAmountChange(
    id: Int,
    amount: Int,
  ) = updateResult(id) { it.copy(amount = amount.coerceIn(1, UnitType.MAX_AMOUNT)) }

  fun onFrozenCountChange(
    id: Int,
    count: Int,
  ) = updateResult(id) { it.copy(frozenCount = count.coerceIn(0, it.servings)) }

  fun onCategorySelect(
    id: Int,
    categoryId: String,
  ) = updateResult(id) { it.copy(categoryOverride = categoryId) }

  fun onPickDate(id: Int) {
    val current = _state.value
    val result = current.results.firstOrNull { it.id == id } ?: return
    emitSideEffect(
      TransformSideEffect.ShowDatePicker(
        id,
        current.expirationToSave(result).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
      ),
    )
  }

  fun onDatePicked(
    id: Int,
    date: LocalDate,
  ) = updateResult(id) { it.copy(expirationOverride = date) }

  fun onSave() {
    val current = _state.value
    if (!current.canSave) {
      // Saving is blocked only by a result that lacks something (e.g. from an accessibility action): open the first one.
      if (!current.saving && current.ingredients.isNotEmpty()) {
        current.results.firstOrNull { !current.isValid(it) }?.let { onSelectResult(it.id) }
      }
      return
    }
    val spends =
      current.ingredients.map {
        IngredientSpend(
          candidateItemIds = it.units.map { unit -> unit.itemId },
          count = it.count,
          preferredItemIds = it.preferredIds,
          amount = it.amount.takeIf { _ -> it.measured },
        )
      }
    val results =
      current.results.map {
        TransformResult(
          name = it.name.trim(),
          servings = it.servings,
          categoryId = current.categoryOf(it) ?: return,
          expiration = current.expirationToSave(it),
          frozenCount = current.frozenCountOf(it),
          unitType = it.unitTypeOverride,
          amount = it.amount.takeIf { _ -> it.measured },
        )
      }
    _state.update { it.copy(saving = true) }
    viewModelScope.launch {
      transformItems
        .transform(spends, results)
        .onSuccess { batch ->
          val (message, messageArgs) = doneMessage(current.ingredients.sumOf { it.count }, results.size)
          pendingResult =
            SnackbarRequest(
              message = message,
              messageArgs = messageArgs,
              actionLabel = R.string.add_undo,
              type = AppFeedbackType.Success,
              onAction = { transformItems.undo(batch) },
            )
          for (row in 1..current.ingredients.size.coerceAtMost(MaxCascadedRows)) {
            delay(CascadeStepMillis)
            _state.update { it.copy(closedRows = row) }
          }
          _state.update { it.copy(saving = false) }
          navigate(TransformNavigation.Close)
        }.onFailure { error ->
          _state.update { it.copy(saving = false) }
          val pastFrozen = error is PantryError.PastDateWhenFrozen
          emitSideEffect(
            if (pastFrozen) TransformSideEffect.ShowFrozenPastDate else TransformSideEffect.ShowSaveFailed,
          )
        }
    }
  }

  /** One string per singular/plural pair: a plural resource can only follow one of the two numbers. */
  private fun doneMessage(
    ingredients: Int,
    products: Int,
  ): Pair<Int, List<Any>> =
    when {
      ingredients == 1 && products == 1 -> R.string.transform_done_one_one to emptyList()
      ingredients == 1 -> R.string.transform_done_one_ingredient to listOf(products)
      products == 1 -> R.string.transform_done_one_product to listOf(ingredients)
      else -> R.string.transform_done to listOf(ingredients, products)
    }

  private fun hasChanges(): Boolean {
    val current = _state.value
    return current.results.any { it != ResultUi(id = it.id) } || current.ingredients.signature() != initialIngredients
  }

  private fun updateResult(
    id: Int,
    transform: (ResultUi) -> ResultUi,
  ) {
    _state.update { current ->
      current.copy(
        results =
          current.results.map {
            if (it.id != id) return@map it
            // A result that cannot be frozen (its category or product says so) keeps nothing in the freezer.
            transform(it).let { updated -> updated.copy(frozenCount = current.frozenCountOf(updated)) }
          },
      )
    }
  }

  /** Units of the same product become one ingredient: the picked ones are spent, the rest of the product is available. */
  private suspend fun unitStatuses(): Map<String, ExpiryStatus> =
    observeItems.observe().first().associate { it.itemId to it.status }

  private fun List<ActiveItem>.toIngredients(
    active: List<ActiveItem>,
    statuses: Map<String, ExpiryStatus>,
  ): List<IngredientUi> =
    groupBy { it.productId ?: it.itemId }.map { (key, picked) ->
      val units = (active.filter { (it.productId ?: it.itemId) == key }).sortedBy { it.expiration }
      // A measured product is spent whole: every batch, whatever was picked.
      val whole = units.firstOrNull()?.unitType?.parent == UnitParent.MEASURED
      IngredientUi(
        key = key,
        name = picked.first().name,
        categoryId = picked.first().categoryId,
        units = units,
        count = if (whole) units.size else picked.size,
        amount = units.sumOf { it.amount ?: 0 }.takeIf { whole },
        preferredIds = (if (whole) units else picked).map { it.itemId }.toSet(),
        unitStatus = statuses,
      )
    }

  private fun List<IngredientUi>.signature(): List<Pair<String, Int>> = map { it.key to it.count }

  private companion object {
    /** The ingredient rows fold away one by one (80 ms apart) before the screen closes; capped so a long list stays quick. */
    const val CascadeStepMillis = 80L
    const val MaxCascadedRows = 6
  }
}
