# Code Examples: Today's Date Highlighting

This document shows the key code changes with explanations.

## 1. Data Model Enhancement

**File**: `DashboardUiModel.kt`

### Before
```kotlin
@Stable
data class ProductUiModel(
  val id: String,
  val name: String,
  val description: String,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val instances: List<ProductInstanceUiModel>,
)
```

### After
```kotlin
@Stable
data class ProductUiModel(
  val id: String,
  val name: String,
  val description: String,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val today: LocalDate,  // ← NEW: Current date for comparison
  val instances: List<ProductInstanceUiModel>,
)
```

**Why**: We add `today` to the UI model so each product item knows what day it is without recalculating.

---

## 2. Mapper Updates

**File**: `DashboardMapper.kt`

### Before
```kotlin
class DashboardMapper {
  // ...
  private fun ProductWithInstances.toUiModel(): ProductUiModel {
    val (startDate, endDate) = getDateRange()
    return ProductUiModel(
      id = product.id,
      name = product.name,
      description = product.description,
      startDate = startDate,
      endDate = endDate,
      instances = instances.map { /* ... */ }
    )
  }
}
```

### After
```kotlin
class DashboardMapper(
  private val appClock: AppClock,  // ← NEW: Injected clock for testability
) {
  // ...
  private fun ProductWithInstances.toUiModel(): ProductUiModel {
    val (startDate, endDate) = getDateRange()
    val today = appClock.now()  // ← NEW: Get current instant
      .toLocalDateTime(TimeZone.currentSystemDefault())  // ← Convert to local
      .date  // ← Extract date portion
    
    return ProductUiModel(
      id = product.id,
      name = product.name,
      description = product.description,
      startDate = startDate,
      endDate = endDate,
      today = today,  // ← NEW: Pass today to UI model
      instances = instances.map { /* ... */ }
    )
  }
}
```

**Why**: 
- We inject `AppClock` instead of calling `Clock.System.now()` directly for testability
- We calculate today's date once in the mapper and pass it to all UI components
- Using `TimeZone.currentSystemDefault()` ensures correct local date

---

## 3. UI Component Updates

**File**: `DashboardScreen.kt` (inside `ProductItem` composable)

### Before
```kotlin
WeekCalendar(
  state = state,
  dayContent = { weekDay ->
    val hasItem = weekDay.date in product.instances.map { it.expirationDate }
    val backgroundColor = if (hasItem) {
      DashboardSectionColors.getSectionColors(sectionType).container
    } else {
      Color.Unspecified
    }
    Column(
      modifier = Modifier
        .widthIn(48.dp)
        .clip(
          if (hasItem) {
            MaterialShapes.Cookie4Sided.toShape()
          } else {
            RectangleShape
          }
        )
        .background(backgroundColor)
        .padding(8.dp),
      // ... content
    ) {
      // ... text content
    }
  }
)
```

### After
```kotlin
// ← NEW: Smart first visible week logic
val firstVisibleDate = if (product.instances.any { it.expirationDate <= product.today }) {
  // If there are items expiring on or before today, show today's week
  product.today
} else {
  // Otherwise, show the week with the nearest (earliest) upcoming item
  product.startDate
}

val state = rememberWeekCalendarState(
  startDate = product.startDate,
  endDate = product.endDate,
  firstVisibleWeekDate = firstVisibleDate,  // ← Smart initial week
)

WeekCalendar(
  state = state,
  dayContent = { weekDay ->
    val hasItem = weekDay.date in product.instances.map { it.expirationDate }
    val isToday = weekDay.date == product.today  // ← NEW: Check if day is today
    
    // ← NEW: Enhanced color logic with priority
    val backgroundColor = when {
      isToday && hasItem -> MaterialTheme.colorScheme.primary  // Primary color for today with items
      isToday && !hasItem -> MaterialTheme.colorScheme.primaryContainer  // Subtle color for today without items
      hasItem -> DashboardSectionColors.getSectionColors(sectionType).container  // Section color
      else -> Color.Unspecified  // No color
    }
    
    Column(
      modifier = Modifier
        .widthIn(48.dp)
        .clip(
          // ← NEW: Enhanced shape logic with priority
          when {
            isToday && hasItem -> MaterialShapes.Cookie6Sided.toShape()  // 6-sided for today with items
            isToday && !hasItem -> MaterialShapes.Cookie4Sided.toShape()  // 4-sided for today without items
            hasItem -> MaterialShapes.Cookie4Sided.toShape()  // 4-sided for other days
            else -> RectangleShape  // No shape
          }
        )
        .background(backgroundColor)
        .padding(8.dp),
      // ... content
    ) {
      // ... text content (unchanged)
    }
  }
)
```

**Why**:
- `firstVisibleDate` logic ensures the calendar opens at the most relevant week:
  - Shows today's week if any items expire on or before today (user needs to see expired/expiring items)
  - Shows the earliest future item's week if all items are in the future (user wants to see next expiration)
- `isToday` flag clearly indicates if the current day is today
- `when` expressions provide priority-based logic (today > has item > nothing)
- Primary color makes today stand out across all sections
- Cookie6Sided shape provides additional visual differentiation

---

## 4. Key Design Decisions

### Using `when` instead of nested `if`
```kotlin
// ❌ Harder to read with nested if
val backgroundColor = if (isToday && hasItem) {
  MaterialTheme.colorScheme.primary
} else if (hasItem) {
  DashboardSectionColors.getSectionColors(sectionType).container
} else {
  Color.Unspecified
}

// ✅ Clear priority with when
val backgroundColor = when {
  isToday && hasItem -> MaterialTheme.colorScheme.primary
  hasItem -> DashboardSectionColors.getSectionColors(sectionType).container
  else -> Color.Unspecified
}
```

### Priority Logic Explained
```kotlin
when {
  isToday && hasItem -> /* ... */  // HIGHEST: Today with item (most important)
  hasItem -> /* ... */             // MEDIUM: Other days with items
  else -> /* ... */                // LOWEST: Empty days (no styling)
}
```

The order matters! We check `isToday && hasItem` first because:
1. If today has an item, it should use primary color (not section color)
2. Without this check first, today would get section color like any other day

---

## 5. Dependency Injection Setup

**File**: `AppModule.kt` (already existed, no changes needed)

```kotlin
val appModule = module {
  // ...
  single<AppClock> { KotlinAppClock() }  // ← Clock is already registered
  singleOf(::DashboardMapper)  // ← Mapper already registered, will receive clock via DI
  // ...
}
```

**Why**: Koin automatically injects `AppClock` into `DashboardMapper` constructor.

---

## 6. Testing Considerations

### Example Test Cases (not implemented, but recommended)

```kotlin
// Pseudo-code for potential tests

class DashboardMapperTest {
  @Test
  fun `today's date is correctly passed to UI model`() {
    // Given
    val fakeClock = FakeAppClock(instant = /* specific date */)
    val mapper = DashboardMapper(fakeClock)
    
    // When
    val uiModel = mapper.mapProductToUiModel(testProduct)
    
    // Then
    assertEquals(expectedLocalDate, uiModel.today)
  }
}

class DashboardScreenTest {
  @Test
  fun `today with item shows primary color`() {
    // Given a product with expiry date matching today
    
    // When rendering the calendar
    
    // Then verify primary color is used
  }
  
  @Test
  fun `today without item shows no color`() {
    // Given a product with no expiry on today
    
    // When rendering the calendar
    
    // Then verify no color is applied
  }
}
```

---

## 7. Material 3 Shapes Reference

The implementation uses Material 3 Expressive API shapes:

```kotlin
// Available in MaterialShapes object
MaterialShapes.Cookie4Sided  // Diamond-like with 4 rounded vertices
MaterialShapes.Cookie6Sided  // Hexagon-like with 6 rounded vertices

// Convert to Compose Shape
MaterialShapes.Cookie4Sided.toShape()
```

These shapes are part of the Material 3 Expressive API, enabled via:
```kotlin
languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
```

---

---

## 8. Calendar Initial Week Logic

The calendar's first visible week is determined by a smart algorithm:

```kotlin
val firstVisibleDate = if (product.instances.any { it.expirationDate <= product.today }) {
  // If there are items expiring on or before today, show today's week
  product.today
} else {
  // Otherwise, show the week with the nearest (earliest) upcoming item
  product.startDate
}
```

### Decision Tree

```
Has items expiring on/before today?
│
├─ YES → Show today's week
│         (User needs to see expired/expiring items immediately)
│
└─ NO  → Show earliest item's week
          (All items are in the future, show the next one)
```

### Examples

**Case 1: Milk with expired items**
- Today: May 17
- Items: May 10, May 12 (both expired)
- Result: Shows week containing May 17 (today's week)
- Why: User needs to see that items are expired

**Case 2: Fresh cheese, all future**
- Today: May 17
- Items: May 25, May 30, June 5 (all future)
- Result: Shows week containing May 25 (earliest item)
- Why: No urgent items, show next expiration

**Case 3: Bread expiring today**
- Today: May 17
- Items: May 17, May 20
- Result: Shows week containing May 17 (today's week)
- Why: Item expires today, needs immediate attention

---

## Summary

The implementation touches three main areas:

1. **Data Layer**: Add `today` field to UI model
2. **Mapping Layer**: Calculate and inject today's date using `AppClock`
3. **UI Layer**: 
   - Apply conditional styling based on `isToday` and `hasItem` flags
   - Smart calendar initial week based on item expiration dates

The result: 
- Today's date is clearly highlighted with primary color and distinct shape
- Calendar opens at the most relevant week for the user's context
