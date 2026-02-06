# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Caducity is an Android grocery expiration tracker application built with Jetpack Compose. The app helps users track their groceries and avoid food waste by monitoring expiration dates.

**Target Platform**: Android (minSdk 35, compileSdk 36, targetSdk 36)

## Technology Stack

- **Language**: Kotlin 2.3.0 (cutting-edge versions, may require regular updates)
- **UI Framework**: Jetpack Compose with Material 3 Expressive API
- **Architecture**: MVI/MVVM with Compose state management
- **Navigation**: Jetpack Navigation 3 (alpha06)
- **Dependency Injection**: Koin 4.1.1
- **Build System**: Gradle with Kotlin DSL
- **Database**: Room 2.8.4
- **Date/Time**: kotlinx-datetime 0.7.1
- **Background Work**: WorkManager 2.11.0 (for expiration notifications)

## Build Commands

### Development Build
```bash
# Clean build
./gradlew clean build

# Android builds
./gradlew assembleDebug              # Android debug build
./gradlew assembleRelease            # Android release build
```

### Testing
```bash
# Run all tests
./gradlew test

# Run all checks (lint + tests)
./gradlew check

# Android device tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest

# Unit tests only
./gradlew testDebugUnitTest

# Lint
./gradlew lint

# Screenshot tests (Compose Preview Screenshot Testing)
./gradlew updateDebugScreenshotTest     # Generate golden images (first run)
./gradlew testDebugScreenshotTest       # Validate against golden images
./gradlew validateDebugScreenshotTest   # Verify screenshots after changes
./gradlew cleanDebugScreenshotTest      # Clean screenshot test cache
```

### Installation
```bash
# Install Android app
./gradlew installDebug    # Debug variant
./gradlew installRelease  # Release variant
./gradlew uninstallAll    # Uninstall all variants
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

### Project Structure
```
app/src/main/kotlin/com/alorma/caducity/
├── data/                  # Data layer
│   ├── datasource/        # Data source interfaces and Room implementations
│   ├── entity/            # Room entities
│   ├── model/             # Data models
│   └── mapper/            # Entity-to-Model mappers
├── domain/                # Business logic
│   ├── model/             # Domain models
│   └── usecase/           # Use cases
├── ui/                    # UI layer
│   ├── screen/            # Feature screens (dashboard, settings)
│   ├── theme/             # Theming (Material 3 with dynamic colors)
│   ├── icons/             # Custom icons
│   └── adaptive/          # Adaptive/responsive utilities
├── notification/          # Notification system (WorkManager, NotificationChannelManager)
├── language/              # Language manager
├── di/                    # Dependency injection modules
├── time/                  # Time/clock abstraction
├── MainActivity.kt        # Main activity
├── App.kt                 # Main app entry point with navigation
└── TopLevelBackStack.kt/TopLevelRoute.kt  # Navigation setup

base/
├── main/                  # Core domain models (InstanceStatus, etc.)
└── ui/
    ├── components/        # Reusable UI components (StatusBadge, TopBars)
    ├── icons/             # Custom icon definitions
    └── theme/             # Theme system (colors, typography, language, preferences)
```

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

Uses Jetpack Navigation 3 with a custom `TopLevelBackStack` implementation:
- Type-safe navigation with sealed `TopLevelRoute` classes
- Adaptive UI: NavigationBar (compact) vs NavigationRail (expanded)
- Window size class detection for responsive layouts
- ViewModel scoping via `rememberViewModelStoreNavEntryDecorator()`
- State preservation via `rememberSaveableStateHolderNavEntryDecorator()`

### Theming

Material 3 Expressive API with:
- Dynamic color support (Android 12+)
- Dark mode toggle with system default option
- Theme preferences persisted via custom `ThemePreferences` class
- Uses compose-settings library for settings UI tiles

### Notification System

Android notification system for expiration alerts:
- **ExpirationWorkScheduler**: Schedules periodic background checks using WorkManager
- **NotificationChannelManager**: Creates and manages Android notification channels
- **ExpirationNotificationHelper**: Handles notification creation and display using NotificationCompat
- **NotificationDebugHelper**: Interface for testing notifications
- Background work runs daily to check for expiring items across categories

### FireAndForget System

One-time operation flags for features like onboarding, announcements, and first-run setups:
- **FireAndForgetRunner**: Interface for managing one-time flags
- **SettingsFireAndForgetRunner**: Implementation using multiplatform-settings for persistence
- **fireAndForgetModule**: Koin DI module providing the singleton runner
- State persisted via SharedPreferences on Android

**Use Cases**:
- First-time user onboarding tutorials
- "What's new" announcements for version updates
- Feature discovery tooltips
- Initial app setup flags
- Settings screen "Reset tutorials" functionality

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

3. Inject the flag in your ViewModel or use case:
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

4. Register the ViewModel with Koin:
   ```kotlin
   viewModelOf(::DashboardViewModel)
   ```

**Key Methods**:
- `isEnabled()`: Check if the flag is enabled (operation should run)
- `disable()`: Mark the operation as completed (won't run again)
- `enable()`: Re-enable the flag (for "Reset tutorials" features)

### Base Module Organization

The `base/` module contains reusable components separated into focused sub-modules:

- **base/main**: Core domain models shared across features
  - `InstanceStatus`: Enum for item states (Fresh, ExpiringSoon, Expired, Frozen)

- **base/ui/components**: Reusable UI components
  - `StatusBadge`: Visual status indicators for items
  - `TopBars`: Common app bar components
  - `ExpirationDefaults`: Shared expiration-related UI constants

- **base/ui/icons**: Custom icon definitions using Compose vector graphics
  - `AppIcons`: Centralized icon access
  - Individual icon files (Search, Add, Calendar, etc.)

- **base/ui/theme**: Comprehensive theme system
  - Color schemes (dynamic colors, contrast levels, expressive palettes)
  - Typography with custom fonts
  - Theme preferences and persistence
  - Language management system
  - System bars appearance

## Key Patterns and Conventions

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

**Database Schema:**
- `categories` table: Top-level categories (id, name, description)
- `products` table: Products within categories (id, categoryId, name, createdAt)
- `items` table: Individual trackable items (id, categoryId, productId?, expirationDate, status, etc.)

**Key Concepts:**
- **Categories** organize related items (e.g., "Dairy", "Produce", "Meat")
- **Products** are optional groupings within categories (e.g., "Whole Milk" vs "2% Milk")
- **Items** are the actual trackable units with expiration dates
- Items can exist with or without a product grouping (standalone items)

## Domain Model Architecture

The domain layer follows Clean Architecture principles with clear separation of concerns:

### Core Domain Models (`domain/model/`)

**Primary Models:**
- `Category` - Top-level grouping (id, name, description)
- `Product` - Mid-level grouping within a category (id, categoryId, name, createdAt)
- `Item` - Individual trackable unit (id, categoryId, productId?, identifier, expirationDate, status, pausedDate)
- `NewItem` - DTO for creating new items (identifier, productId?, expirationDate)

**Composite Models:**
- `CategoryWithItems` - Category with all its products and items
  - Contains: `category: Category`, `products: List<CategoryProduct>`, `standaloneItems: List<Item>`
  - Nested: `CategoryProduct` (product with its items)
- `ProductWithItems` - Product with all its items
- `CategoryDetail` - Detailed category view for UI
  - Contains: `category: Category`, `products: List<DetailProduct>`, `standaloneItems: List<ProductItem>`
  - Nested types for UI presentation
- `CategoryListItem` - Simplified category for list views
- `ItemGroup` - Grouped items by identifier and product

**Status and Utilities:**
- `InstanceStatus` (in `base/main`) - Enum: Fresh, ExpiringSoon, Expired, Frozen
- `ItemComparator` - Interface for sorting items
- `StatusItemComparator` - Sorts items by status and expiration date

### Data Sources (`domain/`)

**Interface Pattern:**
All data sources are defined as interfaces in the domain layer and implemented in the data layer:

- `CategoryDataSource` - CRUD for categories and items
  - `getCategories(filter)`, `getCategory(id)`, `createCategory()`, `addItem()`, `deleteItem()`
  - Item lifecycle: `markItemAsConsumed()`, `freezeItem()`, `unfreezeItem()`

- `ProductDataSource` - CRUD for products within categories
  - `getProductsByCategory()`, `getProduct()`, `createProduct()`, `deleteProduct()`
  - `getActiveItemCount()` - Check if product has active items before deletion

- `ItemDataSource` - Query operations for items
  - `getAllItems()` - Get all items across all categories

### Use Cases (`domain/usecase/`)

**Category Use Cases:**
- `CreateCategoryUseCase` - Create new category with initial items
- `ObtainDashboardCategoriesUseCase` - Get categories for dashboard
- `ObtainCategoryDetailUseCase` - Get detailed category view
- `GetExpiringCategoriesUseCase` - Get categories with expiring items
- `ObtainCategoriesUseCase` - Get all categories

**Product Use Cases:**
- `CreateProductUseCase` - Create new product within category
- `DeleteProductUseCase` - Delete product (validates no active items)
- `GetCategoryProductsUseCase` - Get all products in a category

**Item Use Cases:**
- `AddItemToCategoryUseCase` - Add item to category (with optional product)
- `ConsumeItemUseCase` - Mark item as consumed (with expiration validation)
- `FreezeItemUseCase` - Freeze/unfreeze item (pauses expiration tracking)
- `DeleteItemUseCase` - Delete item

**Configuration:**
- `ExpirationThresholds` - Configurable thresholds for status calculation
  - `soonExpiringThreshold: Duration` - When items are considered "expiring soon"

## Room Database Layer

The data layer uses Room for local persistence with a clean mapping to domain models:

### Room Entities (`data/datasource/room/`)

**Core Entities:**
- `CategoryRoomEntity` - Table: `categories`
  - Fields: id, name, description

- `ProductRoomEntity` - Table: `products`
  - Fields: id, categoryId (FK → categories), name, createdAt
  - Foreign Key: CASCADE delete when category is deleted
  - Index: categoryId

- `ItemRoomEntity` - Table: `items`
  - Fields: id, categoryId (FK → categories), productId? (FK → products), identifier, expirationDate, pausedDate?, remainingDays?, consumedDate?
  - Foreign Keys:
    - categoryId → CASCADE delete when category is deleted
    - productId → SET_NULL when product is deleted (preserves standalone items)
  - Indexes: categoryId, productId

**Relation Entity:**
- `CategoryWithItemsRoomEntity` - Join query result
  - Embedded: CategoryRoomEntity
  - Relations: List<ItemRoomEntity>, List<ProductRoomEntity>
  - Method: `filterConsumed()` - Filter out consumed items in memory

### DAOs (Data Access Objects)

**CategoryDao:**
- `getAllCategoriesWithItems()` - All categories with their items and products
- `getCategoriesWithItemsByDateRange(startDate, endDate)` - Filter by expiration date
- `getCategoryWithItems(categoryId)` - Single category with items/products
- `insertCategory()`, `deleteCategory()`, `getAllCategoriesSync()`, `clearAllCategories()`

**ProductDao:**
- `getProductsByCategory(categoryId)` - All products in a category (sorted by name)
- `getProduct(productId)` - Single product
- `insertProduct()`, `deleteProduct()`, `getActiveItemCount(productId)`
- `getAllProductsSync()`, `clearAllProducts()`

**ItemDao:**
- `getAllItems()` - All items across all categories
- `getCategoryItems(categoryId)` - Items for a specific category
- `getItem(itemId)`, `insertItem()`, `updateItem()`, `deleteItem()`
- `getAllItemsSync()`, `clearAllItems()`

**AppDatabase:**
- Version: 1
- Entities: [CategoryRoomEntity, ItemRoomEntity, ProductRoomEntity]
- Abstract methods: `categoryDao()`, `itemDao()`, `productDao()`

### Mappers

The mapper architecture follows a separation of concerns pattern with specialized mappers for each entity type. All mappers are located in `data/datasource/room/mapper/`:

**CategoryRoomMapper:**
- Location: `data/datasource/room/mapper/CategoryRoomMapper.kt`
- Responsibility: Category entity ↔ domain model conversions
- Methods:
  - `toModel(entity: CategoryRoomEntity)` → `Category`
  - `toEntity(model: Category)` → `CategoryRoomEntity`

**ProductRoomMapper:**
- Location: `data/datasource/room/mapper/ProductRoomMapper.kt`
- Responsibility: Product entity ↔ domain model conversions
- Methods:
  - `toModel(entity: ProductRoomEntity)` → `Product`
  - `toEntity(model: Product)` → `ProductRoomEntity`
- Handles: Timestamp conversions for createdAt field

**ItemRoomMapper:**
- Location: `data/datasource/room/mapper/ItemRoomMapper.kt`
- Responsibility: Item entity ↔ domain model conversions with status calculation
- Constructor: `(AppClock, ExpirationThresholds)`
- Methods:
  - `toModel(entity: ItemRoomEntity)` → `Item` with calculated `ItemStatus`
  - `toEntity(model: Item, categoryId: String)` → `ItemRoomEntity`
  - `toEntity(model: NewItem, id: String, categoryId: String)` → `ItemRoomEntity`
- Status logic: Consumed > Frozen > Calculated (based on expiration and thresholds)

**CategoryWithItemsRoomMapper:**
- Location: `data/datasource/room/mapper/CategoryWithItemsRoomMapper.kt`
- Responsibility: Composite entity ↔ domain model conversions
- Constructor: `(CategoryRoomMapper, ProductRoomMapper, ItemRoomMapper)`
- Methods:
  - `toModel(entity: CategoryWithItemsRoomEntity)` → `CategoryWithItems`
- Dependencies: Delegates to specialized mappers for nested entities
- Handles: Grouping items by product and standalone items

**RoomEntityMapper (Facade):**
- Location: `data/datasource/room/RoomEntityMapper.kt`
- Responsibility: Unified interface that delegates to specialized mappers
- Constructor: `(CategoryRoomMapper, ProductRoomMapper, ItemRoomMapper, CategoryWithItemsRoomMapper)`
- Provides backward-compatible API for all entity conversions
- All mapping methods delegate to the appropriate specialized mapper

### Data Source Implementations

**RoomCategoryDataSource** (implements CategoryDataSource):
- Uses: CategoryDao, ItemDao, RoomEntityMapper
- Handles: Category CRUD operations (create, read, delete)
- Queries: Get categories with items, filtered by date range

**RoomProductDataSource** (implements ProductDataSource):
- Uses: ProductDao
- Handles: Product CRUD within categories
- Validation: Checks active item count before product deletion

**RoomItemDataSource** (implements ItemDataSource):
- Uses: ItemDao, AppClock, RoomEntityMapper
- Handles: Item CRUD operations (add, get, delete)
- Item lifecycle: Consume, freeze, unfreeze items
- Status management: Consumed, frozen, active items

**RoomBackupDataSource:**
- Handles backup/restore operations for all entities
- Maintains backward compatibility with old terminology in backup format
- Uses all three DAOs (CategoryDao, ProductDao, ItemDao)

### Data Flow
1. **ViewModel** collects data from **UseCase**
2. **UseCase** calls **DataSource** interface (domain layer)
3. **DataSource Implementation** (Room) queries DAOs
4. **DAO** returns Room entities
5. **Mapper** converts Room entities to domain models (with status calculation)
6. **DataSource** returns domain models to UseCase
7. **ViewModel** maps to **UiModel** for screens
8. **Screen** observes `StateFlow<UiState>` from ViewModel

## UI Layer Architecture

The UI layer follows MVI/MVVM pattern with Jetpack Compose:

### Screen Structure (`ui/screen/`)

**Dashboard (`dashboard/`):**
- `DashboardScreen` - Main dashboard with category list/calendar
- `DashboardViewModel` - State management, uses `ObtainDashboardCategoriesUseCase`
- `DashboardState` - Sealed class: Loading, Success (PerCategory/Calendar), Error
- `DashboardUiModel` - UI models: CategoryCalendarState, DashboardSummary (counts)
- `DashboardMapper` - Maps domain models to UI models

**Category Detail (`category/detail/`):**
- `CategoryDetailScreen` - Shows category with products and items
- `CategoryDetailViewModel` - Manages category, product, and item operations
- `CategoryDetailState` - Sealed class: Loading, Success, Error
- `CategoryDetailSideEffect` - Side effects: ItemConsumed, ItemFrozen, ProductCreated, dialogs, bottom sheets
- `CategoryDetailMapper` - Maps to product tabs and item groups
- `CategoryDetailAddItemScreen` - Screen for adding items to category/product
- `CategoryDetailAddItemViewModel` - Handles item creation with product selection

**Category Create (`category/create/`):**
- `CreateCategoryScreen` - Form for creating new category
- `CreateCategoryViewModel` - Handles validation and creation
- `CreateCategoryState` - Form state with validation

**Settings (`settings/`):**
- Settings screens for theme, language, notifications, etc.

### UI Models

**Category Detail:**
- `CategoryDetailUiModel` - UI representation of category (id, name, description)
- `ItemDetailUiModel` - UI representation of item (id, expirationDate, status, text)
- `DateItemsUiModel` - Grouped items by date (text, status, date, items)
- `CategoryDetailProductTabUiModel` - Sealed class for product tabs:
  - `Empty` - No items in product
  - `WithItems` - Product with grouped items by date
- `ProductUiModel` - Simple product UI model (id, name)

**Dashboard:**
- `CategoryCalendarState` - Category with calendar config (id, name, calendarConfig)
- `DashboardSummary` - Summary counts (expired, expiringSoon, fresh, frozen)

### ViewModels Pattern

All ViewModels follow the same structure:
```kotlin
class CategoryDetailViewModel(
  // Use cases injected via constructor
  private val obtainCategoryDetailUseCase: ObtainCategoryDetailUseCase,
  private val consumeItemUseCase: ConsumeItemUseCase,
  private val freezeItemUseCase: FreezeItemUseCase,
  // ... other use cases
) : ViewModel() {

  // State as StateFlow
  val state: StateFlow<CategoryDetailState> = ...

  // Side effects channel
  private val sideEffectChannel = Channel<CategoryDetailSideEffect>()
  val sideEffects = sideEffectChannel.receiveAsFlow()

  // Public methods for user actions
  fun onItemClick(item: ItemDetailUiModel) { ... }
  fun onConsumeItem(item: ItemDetailUiModel) { ... }

  // Private method to emit side effects
  private fun emitSideEffect(effect: CategoryDetailSideEffect) {
    viewModelScope.launch {
      sideEffectChannel.send(effect)
    }
  }
}
```

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

### Navigation

**Routes (`TopLevelRoute.kt`):**
- `DashboardRoute` - Main dashboard
- `CreateCategoryRoute` - Create new category
- `CategoryDetailRoute` - Category detail with nested routes:
  - `CategoryDetailRoutes.Root(categoryId)` - Category overview
  - `CategoryDetailRoutes.AddItem(categoryId, productId?)` - Add item screen
- `SettingsRoute` - Settings screens

**Navigation Pattern:**
- Type-safe navigation with sealed classes
- ViewModel scoping via `rememberViewModelStoreNavEntryDecorator()`
- State preservation via `rememberSaveableStateHolderNavEntryDecorator()`

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

3. **Handle in SideEffectHandler** using `AppBottomSheetState`:
   ```kotlin
   is CategoryDetailSideEffect.ShowItemActionsBottomSheet -> {
     bottomSheetState.ItemActionsBottomSheet(
       coroutineScope = this,
       item = effect.item,
       onConsume = {
         viewModel.onConsumeItem(effect.item)
       },
       onFreeze = {
         viewModel.onFreezeItem(effect.item)
       },
       onDelete = {
         viewModel.onDeleteItem(effect.item)
       },
     )
   }
   ```

4. **Create extension function** for the bottom sheet content:
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

### Opt-In Requirements
The following experimental APIs are enabled project-wide:
- `kotlin.time.ExperimentalTime`
- `androidx.compose.material3.ExperimentalMaterial3Api`
- `androidx.compose.material3.ExperimentalMaterial3ExpressiveApi`

### UI Design Language

#### Connected Groups with ShapePosition
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

**Completed**:
- ✅ Basic app scaffold with navigation
- ✅ Dashboard screen with ViewModel
- ✅ Settings screen with theme selection
- ✅ Adaptive UI for different screen sizes
- ✅ DI setup with Koin
- ✅ Room database integration (categories, products, items)
- ✅ Full CRUD operations for categories, products, and items
- ✅ Dashboard statistics and data display
- ✅ Expiration notifications system
- ✅ Category detail screens with product and item management
- ✅ Item status tracking (Fresh, ExpiringSoon, Expired, Frozen)
- ✅ FireAndForget system for onboarding and tutorials
- ✅ Multi-language support (en, es, ca)

## Development Notes

### Adding New Screens
1. Create screen composable in `ui/screen/<feature>/`
2. Add corresponding ViewModel in same package
3. Define route in `TopLevelRoute.kt`
4. Register in `App.kt` `entryProvider` block
5. Add navigation item to `CompactContent`/`ExpandedContent`

### Using Interfaces for Abstraction

When you need abstraction or testability, use the interface pattern:

1. Define an interface (e.g., `NotificationDebugHelper`)
2. Create concrete implementation (e.g., `AndroidNotificationDebugHelper`)
3. Bind implementation to interface in DI module:
   ```kotlin
   singleOf(::AndroidNotificationDebugHelper) bind NotificationDebugHelper::class
   ```

**Benefits**:
- Flexible and testable
- Easy to mock for testing
- Follows dependency inversion principle

**Example**:
```kotlin
// notification/NotificationDebugHelper.kt
interface NotificationDebugHelper {
  fun triggerImmediateCheck()
}

// notification/AndroidNotificationDebugHelper.kt
class AndroidNotificationDebugHelper(
  private val workScheduler: ExpirationWorkScheduler
) : NotificationDebugHelper {
  override fun triggerImmediateCheck() {
    workScheduler.triggerImmediateCheck()
  }
}

// di/AppModule.kt
singleOf(::AndroidNotificationDebugHelper) bind NotificationDebugHelper::class
```

### Version Catalog (libs.versions.toml)
All dependencies are centralized in `gradle/libs.versions.toml`:
- Access via `libs.plugins.*` or `libs.*` in build files
- Update versions in `[versions]` section only
- Use `alias()` to reference in build.gradle.kts

### Java Version
- Source/target: Java 11
- Ensure JDK 11+ is configured in IDE

## Localization and Language Support

The app uses Android resources with Android 13+ per-app language preferences. Currently supported languages: English (en), Spanish (es), and Catalan (ca).

### Adding a New Language

To add a new language (e.g., French):

1. **Add Language Enum Entry**
   - Edit `base/ui/theme/language/Language.kt`
   - Add new enum entry:
   ```kotlin
   enum class Language(val code: String) {
     ENGLISH("en"),
     SPANISH("es"),
     CATALAN("ca"),
     FRENCH("fr"),  // Add new language
   }
   ```

2. **Create Resource Directory**
   - Create `app/src/main/res/values-{code}/` directory
   - Example: `app/src/main/res/values-fr/`

3. **Add String Resources**
   - Copy `strings.xml` from `res/values/` to your new `values-{code}/` directory
   - Translate all string values to the target language
   - Keep string keys unchanged

4. **Update LanguageSettingsScreen**
   - Edit `ui/screen/settings/language/LanguageSettingsScreen.kt`
   - Add string resource:
   ```kotlin
   val languageFrench = stringResource(R.string.language_french)
   ```
   - Add language to when expression:
   ```kotlin
   val title = when (language) {
     Language.ENGLISH -> languageEnglish
     Language.SPANISH -> languageSpanish
     Language.CATALAN -> languageCatalan
     Language.FRENCH -> languageFrench
   }
   ```

5. **Add Language Name String**
   - Edit all `app/src/main/res/values*/strings.xml` files
   - Add the language name in each language:
   ```xml
   <!-- values/strings.xml (English) -->
   <string name="language_french">Français</string>

   <!-- values-es/strings.xml (Spanish) -->
   <string name="language_french">Francés</string>

   <!-- values-ca/strings.xml (Catalan) -->
   <string name="language_french">Francès</string>

   <!-- values-fr/strings.xml (French) -->
   <string name="language_french">Français</string>
   ```

6. **Update Android Locales Config**
   - Edit `app/src/main/res/xml/locales_config.xml`
   - Add new locale:
   ```xml
   <locale android:name="fr"/>
   ```

7. **Test**
   - Build and install: `./gradlew installDebug`
   - Navigate to Settings → Language
   - Verify new language appears and switches correctly

### Language Architecture

- **LanguageManager**: Base class handling persistence and language application
  - Located: `base/ui/theme/language/LanguageManager.kt`
  - Persists selection using `Settings` (key: "app_language")
  - Reads from Android API (`AppCompatDelegate.getApplicationLocales()`)

- **AndroidLanguageManager**: Implementation
  - Located: `language/AndroidLanguageManager.kt`
  - Uses `LocaleManager.applicationLocales` on Android 13+ for seamless language changes without app restart
  - Falls back to `AppCompatDelegate.setApplicationLocales()` on older Android versions
  - Requires Context to be injected via Koin

- **System Language Detection**: On first launch, detects system language and defaults to English if unsupported

- **No App Restart**: Language changes apply immediately without restarting the app on Android 13+ (API 33+)

## Important References

- Detailed implementation plan: `DEVELOPMENT_PLAN.md`
- All build configurations use Kotlin DSL (`.gradle.kts`)
- Version catalog: `gradle/libs.versions.toml` (centralized dependency management)
