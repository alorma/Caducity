# ProductPageViewModel Implementation Plan

## Overview

Implement data loading in `ProductPageViewModel` to display items for individual product tabs, similar to how `CategoryDetailViewModel` currently works.

## Current Architecture Analysis

**How CategoryDetailViewModel loads data:**
1. Uses `ObtainCategoryDetailUseCase` which calls `CategoryDataSource.getCategory(categoryId)`
2. The data source returns a `Flow<Result<CategoryWithItems>>` that includes all products and items for a category
3. The use case transforms this into a `CategoryDetail` domain model with separated item groups (active, frozen, consumed)
4. The ViewModel combines this with calendar preferences and maps to UI models
5. `CategoryDetailMapper` converts domain models to UI models for the screen

**What we need for ProductPageViewModel:**
- Load items for a specific **product** (not the entire category)
- Support both:
  - **Product items**: Items belonging to a specific product (`productId != null`)
  - **Standalone items**: Items without a product (`productId == null`, the "Other" tab)

## Implementation Steps

### 1. Add Query Method to ItemDao
**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/dao/ItemDao.kt`

Add two new query methods:

```kotlin
@Query("SELECT * FROM items WHERE categoryId = :categoryId AND productId = :productId")
fun getProductItems(categoryId: String, productId: String): Flow<List<ItemRoomEntity>>

@Query("SELECT * FROM items WHERE categoryId = :categoryId AND productId IS NULL")
fun getStandaloneItems(categoryId: String): Flow<List<ItemRoomEntity>>
```

**Why**: We need to query items filtered by category + product, and separately query standalone items (productId IS NULL).

---

### 2. Add Method to ItemDataSource Interface
**File**: `app/src/main/kotlin/com/alorma/caducity/domain/ItemDataSource.kt`

Add new method:

```kotlin
fun getItemsByProduct(categoryId: String, productId: String?): Flow<List<Item>>
```

**Why**: Domain layer needs an interface method to retrieve items. The `productId` is nullable to handle both product items and standalone items.

---

### 3. Implement in RoomItemDataSource
**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/RoomItemDataSource.kt`

Add implementation:

```kotlin
override fun getItemsByProduct(categoryId: String, productId: String?): Flow<List<Item>> {
  return if (productId != null) {
    itemDao.getProductItems(categoryId, productId)
  } else {
    itemDao.getStandaloneItems(categoryId)
  }.map { entities ->
    entities.map { entity -> itemMapper.toModel(entity) }
  }
}
```

**Why**: Implementation that delegates to the appropriate DAO query based on whether productId is null, then maps Room entities to domain models.

---

### 4. Create GetProductItemsUseCase
**File**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/GetProductItemsUseCase.kt` (NEW FILE)

Create new use case:

```kotlin
class GetProductItemsUseCase(
  private val itemDataSource: ItemDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {
  fun obtain(categoryId: String, productId: String?): Flow<ProductItems> {
    return itemDataSource.getItemsByProduct(categoryId, productId).map { items ->
      // Separate items by status
      val activeItems = items.filter {
        it.status != ItemStatus.Frozen && it.status != ItemStatus.Consumed
      }
      val frozenItems = items.filter { it.status == ItemStatus.Frozen }
      val consumedItems = items.filter { it.status == ItemStatus.Consumed }

      // Group active items by date
      val dates: List<LocalDate> = activeItems
        .map { it.expirationDate.date() }
        .distinct()
        .sorted()

      val datedItemsGroups: List<ProductDatedItems> = dates.map { date ->
        val itemsForDate = activeItems
          .filter { it.expirationDate.date() == date }
          .map { item ->
            ProductItem(
              id = item.id,
              name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
            )
          }

        ProductDatedItems(
          date = date,
          status = calculateStatus(date),
          items = itemsForDate,
        )
      }

      val frozenProductItems = frozenItems.map { item ->
        ProductItem(
          id = item.id,
          name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
        )
      }

      val consumedProductItems = consumedItems.map { item ->
        ProductItem(
          id = item.id,
          name = item.identifier.takeIf { it.isNotEmpty() } ?: "",
        )
      }

      ProductItems(
        datedItemsGroups = datedItemsGroups,
        frozenItems = frozenProductItems,
        consumedItems = consumedProductItems,
      )
    }
  }

  private fun calculateStatus(expirationDate: LocalDate): ItemStatus {
    return ItemStatus.calculateStatus(
      expirationDate = expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
    )
  }
}

// New domain model
data class ProductItems(
  val datedItemsGroups: List<ProductDatedItems>,
  val frozenItems: List<ProductItem>,
  val consumedItems: List<ProductItem>,
)
```

**Why**: Encapsulates the business logic for loading and organizing product items. Reuses existing domain models (`ProductDatedItems`, `ProductItem`) that are already used in `CategoryDetail`.

---

### 5. Update ProductPageState
**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductPageState.kt`

Replace content with:

```kotlin
sealed interface ProductPageState {
  data object Loading : ProductPageState

  @Stable
  data class Success(
    val datedItemsGroups: ImmutableList<DateItemsUiModel>,
    val frozenItems: ImmutableList<ItemDetailUiModel>,
    val consumedItems: ImmutableList<ItemDetailUiModel>,
  ) : ProductPageState

  data class Error(val message: String) : ProductPageState
}
```

**Why**: State now holds the actual item data instead of a UUID. Structure matches what `ProductTabContent` expects.

---

### 6. Create ProductPageMapper
**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductPageMapper.kt` (NEW FILE)

Create mapper:

```kotlin
class ProductPageMapper(
  private val appClock: AppClock,
) {
  fun mapToUiModel(productItems: ProductItems): ProductPageState.Success {
    val datedItemsGroups = productItems.datedItemsGroups.map { datedItems ->
      val dateText = formatDate(datedItems.date)

      DateItemsUiModel(
        text = dateText,
        status = datedItems.status,
        date = datedItems.date,
        items = datedItems.items.map { item ->
          ItemDetailUiModel(
            id = item.id,
            expirationDate = datedItems.date,
            status = datedItems.status,
            text = item.name.ifEmpty { "Item" },
          )
        }.toImmutableList(),
      )
    }.toImmutableList()

    val frozenItems = productItems.frozenItems.map { item ->
      ItemDetailUiModel(
        id = item.id,
        expirationDate = appClock.now().date(),
        status = ItemStatus.Frozen,
        text = item.name.ifEmpty { "Item" },
      )
    }.toImmutableList()

    val consumedItems = productItems.consumedItems.map { item ->
      ItemDetailUiModel(
        id = item.id,
        expirationDate = appClock.now().date(),
        status = ItemStatus.Consumed,
        text = item.name.ifEmpty { "Item" },
      )
    }.toImmutableList()

    return ProductPageState.Success(
      datedItemsGroups = datedItemsGroups,
      frozenItems = frozenItems,
      consumedItems = consumedItems,
    )
  }

  private fun formatDate(date: LocalDate): String {
    // Format date for display (e.g., "Today", "Tomorrow", or actual date)
    val today = appClock.now().date()
    return when {
      date == today -> "Today"
      date == today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
      date == today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
      else -> date.toString() // Or use a proper formatter
    }
  }
}
```

**Why**: Separates mapping logic from ViewModel. Converts domain models to UI models.

---

### 7. Update ProductPageViewModel
**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductPageViewModel.kt`

Replace implementation:

```kotlin
class ProductPageViewModel(
  private val categoryId: String,
  private val productId: String?,
  getProductItemsUseCase: GetProductItemsUseCase,
  productPageMapper: ProductPageMapper,
) : ViewModel() {

  val state: StateFlow<ProductPageState> = getProductItemsUseCase
    .obtain(categoryId, productId)
    .map { productItems ->
      productPageMapper.mapToUiModel(productItems)
    }
    .catch { error ->
      emit(ProductPageState.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
      viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = ProductPageState.Loading
    )
}
```

**Why**: ViewModel now loads real data from the use case, maps it to UI state, and exposes it as a StateFlow. Follows the same pattern as `CategoryDetailViewModel`.

---

### 8. Update ProductTabContent
**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductTabContent.kt`

Update composable to use ViewModel state instead of productTab parameter for item data:

```kotlin
@Composable
fun ProductTabContent(
  productTab: CategoryDetailProductTabUiModel,
  onItemClick: (ItemDetailUiModel) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductPageViewModel = koinViewModel(
    key = "${productTab.categoryId}-${productTab.id}"
  ) {
    parametersOf(
      productTab.categoryId,
      if (productTab.id != "other") productTab.id else null
    )
  }
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  when (val currentState = state) {
    is ProductPageState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
    }

    is ProductPageState.Success -> {
      // Render using currentState.datedItemsGroups, frozenItems, consumedItems
      // Remove duplicate rendering from productTab parameter
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(/* ... */),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Use currentState data instead of productTab data
        currentState.datedItemsGroups.forEach { datedItems ->
          item {
            SectionHeader(
              status = datedItems.status,
              date = datedItems.text,
              title = ExpirationDefaults.getTitle(datedItems.status),
              count = datedItems.items.size,
            )
          }
          item {
            StatusGroupCard(
              items = datedItems.items,
              onItemClick = onItemClick,
            )
          }
        }

        // Frozen items
        if (currentState.frozenItems.isNotEmpty()) {
          item {
            SectionHeader(
              status = ItemStatus.Frozen,
              title = stringResource(R.string.category_detail_section_frozen),
              count = currentState.frozenItems.size,
            )
          }
          item {
            StatusGroupCard(
              items = currentState.frozenItems,
              onItemClick = onItemClick,
            )
          }
        }

        // Consumed items
        if (currentState.consumedItems.isNotEmpty()) {
          item {
            SectionHeader(
              status = ItemStatus.Consumed,
              title = stringResource(R.string.category_detail_section_consumed),
              count = currentState.consumedItems.size,
            )
          }
          item {
            StatusGroupCard(
              items = currentState.consumedItems,
              onItemClick = onItemClick,
            )
          }
        }
      }
    }

    is ProductPageState.Error -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = currentState.message,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}
```

**Why**: The composable now uses data from the ViewModel state instead of the passed-in `productTab` parameter.

---

### 9. Register Dependencies in Koin
**File**: `app/src/main/kotlin/com/alorma/caducity/di/AppModule.kt`

Add to appModule:

```kotlin
factoryOf(::GetProductItemsUseCase)
factoryOf(::ProductPageMapper)
viewModelOf(::ProductPageViewModel)
```

**Why**: Koin needs to know how to construct these new classes for dependency injection.

---

## Benefits of This Approach

1. **Single Source of Truth**: Each product tab loads its own data independently
2. **Real-time Updates**: Using Room's `Flow` means UI automatically updates when items change
3. **Scoped ViewModels**: Each tab has its own ViewModel instance (via unique key)
4. **Reuses Existing Models**: Leverages `ProductItem`, `ProductDatedItems` from the domain layer
5. **Clean Architecture**: Follows the established pattern (DataSource → UseCase → ViewModel → UI)
6. **No CategoryDetailViewModel Changes**: As requested, we don't touch the existing ViewModel

## Data Flow

```
ItemDao (Room)
  ↓ Flow<List<ItemRoomEntity>>
RoomItemDataSource
  ↓ Flow<List<Item>> (mapped via ItemRoomMapper)
GetProductItemsUseCase
  ↓ Flow<ProductItems> (grouped and organized)
ProductPageViewModel
  ↓ StateFlow<ProductPageState> (mapped via ProductPageMapper)
ProductTabContent
  ↓ Renders UI
```

## Implementation Order

1. Start with data layer (ItemDao, ItemDataSource, RoomItemDataSource)
2. Create use case and domain model
3. Update UI state and mapper
4. Update ViewModel
5. Update composable
6. Register in Koin
7. Test with existing data

## Notes

- Don't modify `CategoryDetailViewModel` - it will be simplified later
- Reuse existing domain models: `ProductItem`, `ProductDatedItems`, `ItemStatus`
- Reuse existing UI models: `DateItemsUiModel`, `ItemDetailUiModel`
- Follow the exact same pattern as `ObtainCategoryDetailUseCase` for consistency
