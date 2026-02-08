# BaseViewModel Quick Reference Guide

## Overview

`BaseViewModel` is a base class that encapsulates the common navigation and side effect pattern used across all ViewModels in the Caducity app. It reduces boilerplate and ensures consistency.

## Type Parameters

```kotlin
BaseViewModel<NavigationIntent, NavigationSideEffect, SideEffect>
```

- **NavigationIntent**: Sealed interface representing user navigation intents (e.g., `DashboardNavigation`)
- **NavigationSideEffect**: Sealed interface representing navigation side effects to be handled by UI (e.g., `DashboardNavigationSideEffect`)
- **SideEffect**: Sealed interface representing non-navigation side effects like dialogs, snackbars (use `NoSideEffect` if none)

## Basic Usage

### 1. ViewModels with Only Navigation

For ViewModels that don't show dialogs, snackbars, or bottom sheets:

```kotlin
class DashboardViewModel(
  private val eventTracker: EventTracker,
  // ... other dependencies
) : BaseViewModel<
  DashboardNavigation,           // Navigation intent type
  DashboardNavigationSideEffect, // Navigation side effect type
  NoSideEffect                   // No other side effects
>() {

  val state: StateFlow<DashboardState> = ...

  override fun navigate(navigation: DashboardNavigation) {
    when (navigation) {
      DashboardNavigation.Settings -> {
        eventTracker.trackAction(NavigateToSettingsAction())
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToSettings)
      }
      // ... other cases
    }
  }
}
```

**Screen observation:**
```kotlin
@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel = koinViewModel(),
) {
  // Observe navigation side effects
  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        DashboardNavigationSideEffect.NavigateToSettings -> navController.navigate("settings")
        // ... other cases
      }
    }
  }
}
```

### 2. ViewModels with Navigation AND Other Side Effects

For ViewModels that show dialogs, snackbars, or bottom sheets:

```kotlin
class CategoryDetailViewModel(
  private val eventTracker: EventTracker,
  // ... other dependencies
) : BaseViewModel<
  CategoryDetailNavigation,  // Navigation intent type
  CategoryDetailSideEffect,  // Combined side effect type (includes navigation)
  CategoryDetailSideEffect   // Same type for both (temporary pattern)
>() {

  val state: StateFlow<CategoryDetailState> = ...

  // Navigation side effects
  override fun navigate(navigation: CategoryDetailNavigation) {
    when (navigation) {
      is CategoryDetailNavigation.AddItem -> {
        eventTracker.trackAction(NavigateToAddItemFromCategoryAction(...))
        emitNavigationSideEffect(CategoryDetailSideEffect.NavigateToAddItem(...))
      }
      // ... other cases
    }
  }

  // Other side effects
  fun onShowAddProductDialog() {
    emitSideEffect(CategoryDetailSideEffect.ShowAddProductDialog)
  }

  fun onDeleteCategoryClick() {
    emitSideEffect(CategoryDetailSideEffect.ShowDeleteCategoryDialog)
  }
}
```

**Screen observation:**
```kotlin
@Composable
fun CategoryDetailScreen(
  viewModel: CategoryDetailViewModel = koinViewModel(),
) {
  // Observe both navigation and other side effects from the same flow
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        // Navigation side effects
        is CategoryDetailSideEffect.NavigateToAddItem -> { /* navigate */ }
        CategoryDetailSideEffect.NavigateBack -> { /* navigate back */ }

        // Dialog side effects
        CategoryDetailSideEffect.ShowAddProductDialog -> { /* show dialog */ }
        CategoryDetailSideEffect.ShowDeleteCategoryDialog -> { /* show dialog */ }

        // Success/error side effects
        CategoryDetailSideEffect.ProductCreated -> { /* show snackbar */ }
        CategoryDetailSideEffect.CreateProductFailed -> { /* show error */ }
      }
    }
  }
}
```

## Protected Methods

BaseViewModel provides protected methods for emitting side effects:

```kotlin
// Emit a navigation side effect
protected fun emitNavigationSideEffect(effect: NavigationSideEffect)

// Emit a non-navigation side effect
protected fun emitSideEffect(effect: SideEffect)
```

These methods are protected to enforce the pattern - only the ViewModel can emit side effects, and the UI observes them.

## Public Properties

```kotlin
// Flow of navigation side effects for UI to observe
val navigationSideEffects: Flow<NavigationSideEffect>

// Flow of non-navigation side effects for UI to observe
val sideEffects: Flow<SideEffect>
```

## Migration Pattern

When migrating an existing ViewModel:

1. **Extend BaseViewModel** instead of `ViewModel`
2. **Specify type parameters**: `<NavigationIntent, NavigationSideEffect, SideEffect>`
3. **Remove manual channel/flow declarations** (inherited from base)
4. **Change `fun navigate()` to `override fun navigate()`**
5. **Remove `emitNavigationSideEffect()` implementation** (inherited)
6. **Remove `emitSideEffect()` implementation** (inherited)
7. **Keep existing business logic unchanged**

## Example: Before and After

### Before (Manual Pattern)

```kotlin
class DashboardViewModel(
  private val eventTracker: EventTracker,
) : ViewModel() {

  private val navigationSideEffectChannel = Channel<DashboardNavigationSideEffect>()
  val navigationSideEffects = navigationSideEffectChannel.receiveAsFlow()

  fun navigate(navigation: DashboardNavigation) {
    when (navigation) {
      DashboardNavigation.Settings -> {
        eventTracker.trackAction(NavigateToSettingsAction())
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToSettings)
      }
    }
  }

  private fun emitNavigationSideEffect(effect: DashboardNavigationSideEffect) {
    viewModelScope.launch {
      navigationSideEffectChannel.send(effect)
    }
  }
}
```

### After (BaseViewModel)

```kotlin
class DashboardViewModel(
  private val eventTracker: EventTracker,
) : BaseViewModel<DashboardNavigation, DashboardNavigationSideEffect, NoSideEffect>() {

  override fun navigate(navigation: DashboardNavigation) {
    when (navigation) {
      DashboardNavigation.Settings -> {
        eventTracker.trackAction(NavigateToSettingsAction())
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToSettings)
      }
    }
  }
}
```

**Lines saved: 10+ lines of boilerplate per ViewModel!**

## Best Practices

1. **Always use BaseViewModel** for new ViewModels
2. **Use NoSideEffect** when you don't have dialogs/snackbars
3. **Override navigate()** - it's abstract and must be implemented
4. **Track before emitting** - always call `eventTracker.trackAction()` before `emitNavigationSideEffect()`
5. **Keep side effects sealed** - use sealed interfaces for type safety
6. **Separate concerns** - navigation side effects should trigger navigation, other side effects should trigger UI feedback

## Common Mistakes

❌ **Forgetting to override navigate()**
```kotlin
class MyViewModel : BaseViewModel<...>() {
  fun navigate(navigation: MyNavigation) { // Missing 'override'
    // ...
  }
}
```

✅ **Correct:**
```kotlin
class MyViewModel : BaseViewModel<...>() {
  override fun navigate(navigation: MyNavigation) {
    // ...
  }
}
```

❌ **Calling emitSideEffect directly from UI**
```kotlin
// In composable - WRONG
Button(onClick = { viewModel.emitSideEffect(...) }) // emitSideEffect is protected!
```

✅ **Correct:**
```kotlin
// In ViewModel
fun onButtonClick() {
  emitSideEffect(MySideEffect.Something)
}

// In composable
Button(onClick = { viewModel.onButtonClick() })
```

## File Locations

- **BaseViewModel**: `app/src/main/kotlin/com/alorma/caducity/ui/base/BaseViewModel.kt`
- **NoSideEffect**: Defined in `BaseViewModel.kt`
- **Example ViewModels**:
  - `DashboardViewModel.kt` - Simple navigation only
  - `CategoryDetailViewModel.kt` - Navigation + side effects

## Summary

BaseViewModel provides:
- ✅ Less boilerplate code
- ✅ Consistent pattern across all ViewModels
- ✅ Type-safe navigation and side effects
- ✅ Clear separation of concerns
- ✅ Protected methods enforce proper usage
- ✅ Easy to test and maintain
