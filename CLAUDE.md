# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Caducity is an Android grocery expiration tracker application built with Jetpack Compose. The app helps users track their groceries and avoid food waste by monitoring expiration dates.

## Requirements

- **JDK 21** - Required for building the project
- **Gradle 9.3.1** - Included via wrapper
- **Android SDK** - For Android builds

## Build Commands

### Development Build
```bash
# Clean build
./gradlew clean build

# Android builds
./gradlew assembleDebug              # Android debug build
./gradlew assembleRelease            # Android release build
```

### Version Information
```bash
# Display current version information
./gradlew version         # Shows versionName, versionCode, and components

# CI/CD tasks (output plain values)
./gradlew -q printVersionName  # Prints only the version name (e.g., "0.0.2")
./gradlew -q printVersionCode  # Prints only the version code (e.g., "20")
```

## Architecture

### Architecture

**Clean Architecture with Clear Separation**:
- **Data Layer**: Room database implementation, entities, and data sources
- **Domain Layer**: Business logic, use cases, and domain models
- **UI Layer**: Jetpack Compose screens, ViewModels, and navigation
- **Base Module**: Reusable components and theming shared across features

**Dependency Injection Pattern**:
- `appModule`: ViewModels, use cases, shared services
- `dataModule`: Data sources, repositories, Room database
- `themeModule` (base): Theme preferences and language management
- All modules are combined in `App.kt` via Koin

### Navigation System

Uses Jetpack Navigation 3:

### Theming

Material 3 Expressive API with:
- Theme preferences persisted via custom `ThemePreferences` class
- Uses compose-settings library for settings UI tiles

### FireAndForget System

One-time operation flags for features like onboarding, announcements, and first-run setups:
- **FireAndForgetRunner**: Interface for managing one-time flags
- **SettingsFireAndForgetRunner**: Implementation using multiplatform-settings for persistence
- **fireAndForgetModule**: Koin DI module providing the singleton runner
- State persisted via SharedPreferences on Android

**How to Use**:
1. Define a flag class by extending `FireAndForget`:
   ```kotlin
   class OnboardingFlag(runner: FireAndForgetRunner) : FireAndForget(
     fireAndForgetRunner = runner,
     name = "user_onboarding",
     defaultValue = true  // true means enabled by default
   )
   ```

2. Register the flag in a Koin module:
   ```kotlin
   val appModule = module {
     singleOf(::OnboardingFlag)
   }
   ```

3. Inject the flag in your destination:
   ```kotlin
   class DashboardViewModel(
     private val onboardingFlag: OnboardingFlag,
   ) : ViewModel() {
     fun checkOnboarding() {
       if (onboardingFlag.isEnabled()) {
         // Show onboarding UI
         onboardingFlag.disable()  // Mark as completed
       }
     }
   }
   ```

**Key Methods**:
- `isEnabled()`: Check if the flag is enabled (operation should run)
- `disable()`: Mark the operation as completed (won't run again)
- `enable()`: Re-enable the flag (for "Reset tutorials" features)

### Firebase Analytics Consent Mode

The app implements Google's consent mode for Firebase Analytics to respect user privacy preferences:

**Components**:
- **ConsentManager**: Manages consent preferences and applies them to Firebase Analytics
- **ConsentPreferences**: Data class with predefined consent options (DEFAULT, ANALYTICS_ONLY, ALL_GRANTED)
- **ConsentFlag**: FireAndForget flag ensuring consent screen is shown only once
- **ConsentOnboardingPage**: UI component in onboarding flow for collecting user consent

**Consent Flow**:
1. On app startup in `MainActivity.onCreate()`, default consent (all denied) is applied
2. During onboarding (page 5 of 6), user sees consent screen with analytics toggle
3. User's choice is persisted via SharedPreferences
4. ConsentManager applies settings to Firebase Analytics using `setConsent()` API
5. Consent flag is disabled after onboarding completion

**Key Features**:
- Default deny approach (privacy-first)
- One-time consent screen using FireAndForget
- Persistent storage of user preferences
- Integration with Firebase Analytics consent API

**Based on**: [Google's App Consent Guide](https://developers.google.com/tag-platform/security/guides/app-consent)

### Analytics & Action Tracking System

The app uses a dual-layer tracking system for user behavior analytics:
1. **Screen Tracking**: Automatically tracks when users navigate to different screens
2. **Action Tracking**: Tracks user interactions and navigation actions within screens

Both tracking types use Firebase Analytics (via `FirebaseTracker`) and Timber logging (via `TimberTracker`) simultaneously through a composite pattern.

#### Core Components (`feature/tracking/`)

**Base Classes:**

- **`Action.kt`**: Base class for all trackable actions
  ```kotlin
  abstract class Action(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
  )
  ```

- **`NavigationAction`**: Specialized base class for navigation actions
  - Automatically prefixes event name with `nav_`
  - Automatically adds `origin` parameter identifying source screen
  ```kotlin
  abstract class NavigationAction(
    actionName: String,
    origin: String,
    parameters: Map<String, String> = emptyMap(),
  ) : Action(
    name = "nav_$actionName",
    parameters = parameters + ("origin" to origin),
  )
  ```

- **`Screen.kt`**: Base class for screen tracking events
  ```kotlin
  abstract class Screen(
    val name: String,
  )
  ```

- **`EventTracker`**: Composite tracker that delegates to multiple implementations (Firebase + Timber)
  ```kotlin
  class EventTracker(
    private val trackers: List<Tracker>,
  ) : Tracker {
    override fun trackScreen(screen: Screen)
    override fun trackAction(action: Action)
  }
  ```

#### Implementation Pattern

**1. Define Navigation Actions**

Create action classes that extend `NavigationAction` in `feature/tracking/Actions.kt`:

```kotlin
class NavigateToCreateCategoryAction : NavigationAction(
  actionName = "create_category",
  origin = "dashboard",
  parameters = mapOf("source" to "fab")
)

class NavigateToCategoryAction(source: String) : NavigationAction(
  actionName = "category",
  origin = "dashboard",
  parameters = mapOf("source" to source)
)
```

**2. Define Navigation Sealed Interface**

Create a sealed interface representing user navigation intents in `ui/screen/<feature>/<Feature>Navigation.kt`:

```kotlin
sealed interface DashboardNavigation {
  data object CreateCategory : DashboardNavigation
  data class Category(val categoryId: String, val source: String) : DashboardNavigation
  data class FilteredItems(val status: ItemStatus) : DashboardNavigation
  data object Settings : DashboardNavigation
}

sealed interface DashboardNavigationSideEffect {
  data object NavigateToCreateCategory : DashboardNavigationSideEffect
  data class NavigateToCategory(val categoryId: String) : DashboardNavigationSideEffect
  data class NavigateToFilteredItems(val status: ItemStatus) : DashboardNavigationSideEffect
  data object NavigateToSettings : DashboardNavigationSideEffect
}
```

#### Key Principles

**1. Track User Intent, Not Implementation Details**
- ✅ Good: Track `nav_category` with `source="calendar_date"`
- ❌ Bad: Track `onClickCalendarItem` or `handleCalendarDateSelection`

**2. Never Track User-Entered Data**
- ✅ Good: Track categorical parameters like `status="expired"`, `source="fab"`
- ❌ Bad: Track category IDs, names, user input text

**3. Use "origin" for Screen Context**

All navigation actions include an `origin` parameter identifying the source screen:
- `origin: "dashboard"` - Navigation from dashboard
- `origin: "category_detail"` - Navigation from category detail
- `origin: "settings"` - Navigation from settings

**4. Use "source" for UI Element Context**

Use `source` to identify specific UI elements that triggered the action:
- `source: "fab"` - Floating action button
- `source: "topbar"` - Top app bar
- `source: "category_title"` - Category title/card click
- `source: "calendar_date"` - Calendar date click
- `source: "summary"` - Summary statistics card

**5. Separate Navigation Side Effects**

Keep navigation side effects separate from other side effects (dialogs, snackbars):
- `navigationSideEffectChannel` - For navigation only
- `sideEffectChannel` - For dialogs, snackbars, bottom sheets, etc.

#### Benefits of This Architecture

1. **Single Entry Point**: Only one `navigate()` method per ViewModel reduces API surface area
2. **Type Safety**: Sealed interfaces ensure all navigation cases are handled with compile-time checks
3. **Discoverable**: All navigation options visible in sealed interface with IDE auto-completion
4. **Testable**: Single method to mock in tests, action classes simple to verify
5. **Separation of Concerns**: Navigation logic in ViewModel, tracking happens automatically
6. **Extensible**: Add new navigation actions without new methods
7. **Consistent Event Naming**: `nav_` prefix and `origin` parameter standardized across all actions

#### Adding Action Tracking to New Screens

## Data Model Hierarchy

The app uses a three-level hierarchy to organize grocery items:

```
Category (top-level grouping)
├── Product (optional mid-level grouping within a category)
│   └── Item (individual trackable unit with expiration date)
└── Standalone Item (item without a product grouping)
```

**Examples:**
- **Category**: "Dairy"
  - **Product**: "Whole Milk"
    - **Item**: Bottle expiring on 2024-02-15
    - **Item**: Bottle expiring on 2024-02-20
  - **Product**: "2% Milk"
    - **Item**: Bottle expiring on 2024-02-18
  - **Standalone Item**: Yogurt expiring on 2024-02-10 (no product grouping)

## Domain Model Architecture

The domain layer follows Clean Architecture principles with clear separation of concerns:

### Core Domain Models (`domain/model/`)

**Primary Models:**
- `Category` - Top-level grouping (id, name, description)
- `Product` - Mid-level grouping within a category (id, categoryId, name, createdAt)
- `Item` - Individual trackable unit (id, categoryId, productId?, identifier, expirationDate, status, pausedDate)
- `NewItem` - DTO for creating new items (identifier, productId?, expirationDate)

**Status and Utilities:**
- `InstanceStatus` (in `base/main`) - Enum: Fresh, ExpiringSoon, Expired, Frozen
- `ItemComparator` - Interface for sorting items
- `StatusItemComparator` - Sorts items by status and expiration date

### ViewModels Pattern

All ViewModels follow a consistent structure with clear separation between state, side effects, and navigation:

#### Navigation Sealed Interfaces

Each screen defines two sealed interfaces for type-safe navigation:

**Navigation Intent** - Represents user's navigation intent with all required data:
```kotlin
sealed interface DashboardNavigation {
  data object CreateCategory : DashboardNavigation
  data class Category(val categoryId: String, val source: String) : DashboardNavigation
  data class FilteredItems(val status: ItemStatus) : DashboardNavigation
  data object Settings : DashboardNavigation
}
```

**Navigation Side Effect** - Represents the actual navigation action to be performed:
```kotlin
sealed interface DashboardNavigationSideEffect {
  data object NavigateToCreateCategory : DashboardNavigationSideEffect
  data class NavigateToCategory(val categoryId: String) : DashboardNavigationSideEffect
  data class NavigateToFilteredItems(val status: ItemStatus) : DashboardNavigationSideEffect
  data object NavigateToSettings : DashboardNavigationSideEffect
}
```

**Key Differences:**
- **Navigation Intent**: May include extra data needed for tracking (e.g., `source` parameter)
- **Navigation Side Effect**: Contains only data needed for actual navigation (e.g., `categoryId`)

#### ViewModel API Design Principles

**1. Single `navigate()` Method**
- All navigation goes through one method
- Takes sealed interface parameter for type safety
- Automatically tracks actions via EventTracker
- Emits appropriate navigation side effects

**2. Separate Navigation Side Effects**
- `navigationSideEffectChannel` - For navigation events only
- `sideEffectChannel` - For dialogs, snackbars, bottom sheets
- Clear separation of concerns
- **IMPORTANT**: NEVER mix navigation and non-navigation side effects in the same sealed interface
- **IMPORTANT**: Use `Unit` for `NavigationSideEffect` when ViewModel doesn't navigate
- **IMPORTANT**: Use `Unit` for `SideEffect` when ViewModel has no dialogs/snackbars/bottom sheets

**3. Method Naming Conventions**
- `navigate(navigation: <Screen>Navigation)` - Navigation actions
- `on<Action>()` - User interactions (onRefresh, onItemClick)
- `emit<Type>SideEffect()` - Private helpers for side effect emission

**4. Minimal Public API**
- Expose only what the UI needs to call
- Keep implementation details private
- Use sealed interfaces for exhaustive when expressions

### State Management Pattern

**State:**
- Sealed class hierarchy for screen states
- Example: `sealed interface CategoryDetailState { object Loading, data class Success, data class Error }`
- Exposed as `StateFlow<State>` from ViewModel
- Screen observes state and renders accordingly

**Side Effects:**
- Sealed interface for one-time events
- Examples: Snackbars, Dialogs, Bottom Sheets, Navigation
- Exposed as `Flow<SideEffect>` from ViewModel (Channel-based)
- Handled in `SideEffectHandler` composable

#### Adding Navigation to a New Screen

1. Create `<Screen>Navigation.kt` with both sealed interfaces
2. Add `navigate()` method to ViewModel
3. Inject `EventTracker` in ViewModel
4. Create action classes in `feature/tracking/Actions.kt`
5. Add `LaunchedEffect` in screen composable to observe navigation side effects
6. Pass navigation callbacks from parent composable
7. UI calls `viewModel.navigate()` with intent objects

### Components (`base/ui/components/`)

**Reusable Components:**
- `StatusBadge` - Visual indicator for item status (Fresh, ExpiringSoon, Expired, Frozen)
- `ItemCard` - Card component for displaying items
- `AppScaffold` - Standard scaffold with dialog/snackbar/bottom sheet support
- `AppCalendar` - Calendar component for date selection and visualization
- `TopBars` - Standard app bars (TopAppBar, SearchBar, etc.)

### User Feedback: Dialogs, Snackbars, and Bottom Sheets

**IMPORTANT**: All user feedback (dialogs, snackbars, bottom sheets) MUST be handled through side effects, never directly from UI state.

#### State Initialization

All screens must initialize the three feedback states at the top level:

```kotlin
@Composable
fun CategoryDetailScreen(
  categoryId: String,
  viewModel: CategoryDetailViewModel = koinViewModel { parametersOf(categoryId) }
) {
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  val bottomSheetState = rememberAppBottomSheetState()

  SideEffectHandler(
    viewModel = viewModel,
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
  )

  // Pass all three states to scaffold
  AppScaffold(
    dialogState = dialogState,
    snackbarState = snackbarState,
    bottomSheetState = bottomSheetState,
    // ... other params
  ) {
    // Screen content
  }
}
```

#### Dialog Pattern

When you need to show a dialog:

1. **Add a side effect** in the screen's `SideEffect` sealed interface:
   ```kotlin
   sealed interface CategoryDetailSideEffect {
     data object ShowAddProductDialog : CategoryDetailSideEffect
   }
   ```

2. **Emit the side effect** from ViewModel:
   ```kotlin
   fun onShowAddProductDialog() {
     emitSideEffect(CategoryDetailSideEffect.ShowAddProductDialog)
   }
   ```

3. **Handle in SideEffectHandler** using `AppDialogState`:
   ```kotlin
   CategoryDetailSideEffect.ShowAddProductDialog -> {
     var productName by mutableStateOf("")
     val result = dialogState.showAlertDialog(
       title = { Text("Add Product") },
       text = {
         OutlinedTextField(
           value = productName,
           onValueChange = { productName = it },
           // ... other params
         )
       },
       positiveButton = { Text("Add") },
       negativeButton = { Text("Cancel") },
       type = AppFeedbackType.Info,
     )
     if (result == DialogResult.Positive) {
       viewModel.onCreateProduct(productName)
     }
   }
   ```

**Available `AppFeedbackType` values**:
- `AppFeedbackType.Info` - Neutral blue/gray styling
- `AppFeedbackType.Success` - Green success styling
- `AppFeedbackType.Error` - Red error styling
- `AppFeedbackType.Status(status)` - Status-based styling (Fresh, Expired, etc.)

#### Snackbar Pattern

When you need to show a snackbar:

1. **Add a side effect** for the snackbar event:
   ```kotlin
   sealed interface CategoryDetailSideEffect {
     data object ProductCreated : CategoryDetailSideEffect
     data object CreateProductFailed : CategoryDetailSideEffect
   }
   ```

2. **Emit from ViewModel**:
   ```kotlin
   fun onCreateProduct(name: String) {
     viewModelScope.launch {
       val result = createProductUseCase.create(categoryId, name)
       if (result.isSuccess) {
         emitSideEffect(CategoryDetailSideEffect.ProductCreated)
       } else {
         emitSideEffect(CategoryDetailSideEffect.CreateProductFailed)
       }
     }
   }
   ```

3. **Handle in SideEffectHandler** using `AppSnackbarState`:
   ```kotlin
   CategoryDetailSideEffect.CreateProductFailed -> {
     snackbarState.showSnackbar(
       message = R.string.error_create_product_failed,
       type = AppFeedbackType.Error,
     )
   }
   ```

#### Bottom Sheet Pattern

When you need to show a bottom sheet:

1. **Add a side effect** with the data to display:
   ```kotlin
   sealed interface CategoryDetailSideEffect {
     data class ShowItemActionsBottomSheet(
       val item: ItemDetailUiModel,
     ) : CategoryDetailSideEffect
   }
   ```

2. **Emit from ViewModel**:
   ```kotlin
   fun onItemClick(item: ItemDetailUiModel) {
     emitSideEffect(CategoryDetailSideEffect.ShowItemActionsBottomSheet(item))
   }
   ```

3. **Create extension function** for the bottom sheet content:
   ```kotlin
   private fun AppBottomSheetState.ItemActionsBottomSheet(
     coroutineScope: CoroutineScope,
     item: ItemDetailUiModel,
     onConsume: () -> Unit,
     onFreeze: () -> Unit,
     onDelete: () -> Unit,
   ) {
     coroutineScope.launch {
       show {
         // Bottom sheet content composable
         Column(modifier = Modifier.fillMaxWidth()) {
           Text(text = item.text)
           ListItem(
             headlineContent = { Text("Action") },
             modifier = Modifier.clickable {
               onConsume()
               coroutineScope.launch { this@ItemActionsBottomSheet.hide() }
             }
           )
         }
       }
     }
   }
   ```

**Key Points**:
- Use extension function on `AppBottomSheetState` for bottom sheet content
- Call `show { }` with a composable lambda for content
- Call `hide()` to dismiss the bottom sheet after actions
- Pass `coroutineScope` from `SideEffectHandler` to manage lifecycle

#### Key Rules

- **NEVER** create separate `@Composable` dialog/bottom sheet functions in screens
- **NEVER** use `remember { mutableStateOf(false) }` for dialog/bottom sheet visibility
- **NEVER** use `rememberModalBottomSheetState()` directly in screen composables
- **ALWAYS** use `dialogState.showAlertDialog()` for dialogs
- **ALWAYS** use `snackbarState.showSnackbar()` for snackbars
- **ALWAYS** use `bottomSheetState.show { }` for bottom sheets (via extension function)
- **ALWAYS** emit side effects from ViewModel, handle in `SideEffectHandler`
- **ALWAYS** pass `dialogState`, `snackbarState`, and `bottomSheetState` to screen composables
- **ALWAYS** create bottom sheet content as extension functions on `AppBottomSheetState`

## UI Patterns

### Connected Groups with ShapePosition
All lists displaying multiple items should use the `ShapePosition` pattern to create visually connected groups, unless explicitly specified otherwise. This creates a cohesive visual flow that matches the app's design language.

**Pattern**: Use `ShapePosition` enum with appropriate shape helpers:
- `.toHorizontalShape()` - For horizontal lists (LazyRow) - rounds left/right edges
- `.toVerticalShape()` - For vertical lists (LazyColumn) - rounds top/bottom edges

**ShapePosition Values**:
- `ShapePosition.Single` - Item stands alone: Large rounded corners on all sides
- `ShapePosition.Start` - First item in group: Large corners on leading edge, small on trailing edge
- `ShapePosition.Middle` - Middle items in group: Small corners on all sides
- `ShapePosition.End` - Last item in group: Small corners on leading edge, large on trailing edge

**Horizontal List Implementation** (LazyRow):
```kotlin
LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
  items(items) { item ->
    val index = items.indexOf(item)
    val shapePosition = when {
      items.size == 1 -> ShapePosition.Single
      index == 0 -> ShapePosition.Start
      index == items.size - 1 -> ShapePosition.End
      else -> ShapePosition.Middle
    }

    Card(
      modifier = Modifier.clip(shapePosition.toHorizontalShape())
    ) {
      // Card content
    }
  }
}
```

**Vertical List Implementation** (LazyColumn):
```kotlin
LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
  items(items) { item ->
    val index = items.indexOf(item)
    val shapePosition = when {
      items.size == 1 -> ShapePosition.Single
      index == 0 -> ShapePosition.Start
      index == items.size - 1 -> ShapePosition.End
      else -> ShapePosition.Middle
    }

    Card(
      modifier = Modifier.clip(shapePosition.toVerticalShape())
    ) {
      // Card content
    }
  }
}
```

**Where to Use**:
- Horizontal scrolling lists (`LazyRow`) - Use `.toHorizontalShape()`
- Vertical grouped lists (`LazyColumn` sections) - Use `.toVerticalShape()`
- Settings menu groups (vertical)
- Item cards within category/product groups (horizontal)
- Any UI showing related items in sequence

**Reference Implementations**:
- Horizontal: `ItemCard` in `CategoriesListItem.kt` (uses `.toHorizontalShape()`)
- Vertical: `StyledSettingsCard` in settings components (uses `.toVerticalShape()`)

## Key Patterns and Conventions

### Date/Time Handling
- Use `kotlinx-datetime` for all date/time operations
- Store timestamps as `Long` (Unix epoch milliseconds)
- Convert to `LocalDate`/`LocalDateTime` for display
- Use `AppClock` abstraction (injectable) for testable time operations

### Dependency Injection
- Register ViewModels with `viewModelOf(::ClassName)`
- Register singletons with `singleOf(::ClassName)` or `single { }`
- Use `bind` to map implementations to interfaces
- Always inject dependencies via constructor

## Current Implementation Status

## Development Notes

### Version Catalog (libs.versions.toml)
All dependencies are centralized in `gradle/libs.versions.toml`:
- Access via `libs.plugins.*` or `libs.*` in build files
- Update versions in `[versions]` section only
- Use `alias()` to reference in build.gradle.kts
