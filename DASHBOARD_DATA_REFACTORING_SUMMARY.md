# Dashboard Data Refactoring Summary

## Overview
Refactored dashboard domain model to remove item details, following the same clean architecture pattern used in `CategoryDetail`. Dashboard now only knows about summary counts (status → count) and categories with calendar data (dates + statuses), not individual items.

## Changes Made

### 1. Domain Model - DashboardData.kt (NEW)
**Location**: `app/src/main/kotlin/com/alorma/caducity/domain/model/DashboardData.kt`

Created new domain models for dashboard:
- `DashboardData`: Top-level model with summary and categories
- `DashboardSummary`: Map of status → count (no item details)
- `DashboardCategory`: Category with CalendarData only

```kotlin
data class DashboardData(
  val summary: DashboardSummary,
  val categories: List<DashboardCategory>,
)

data class DashboardSummary(
  val statusCounts: Map<ItemStatus, Int>,
)

data class DashboardCategory(
  val category: Category,
  val calendarData: CalendarData,
)
```

**Key principle**: Domain layer contains NO item details, only aggregated data.

### 2. Use Case - ObtainDashboardCategoriesUseCase.kt (MODIFIED)
**Location**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/ObtainDashboardCategoriesUseCase.kt`

**Before**:
- Returned `Flow<ImmutableList<CategoryWithItems>>`
- Simple passthrough to data source
- 15 lines

**After**:
- Returns `Flow<DashboardData>`
- Calculates summary counts from all active items
- Creates `DateStatus` objects for each category's calendar
- Groups items by date with status calculation
- 69 lines

**Key changes**:
- Now depends on `AppClock` and `ExpirationThresholds` for status calculation
- Filters active items (excludes Frozen and Consumed)
- Creates summary with status → count mapping
- Builds calendar data with only dates, statuses, and counts (no items)

**Logic flow**:
1. Get all categories with items from data source
2. Collect all active items from all categories for summary
3. Group items by status and count them → `statusCounts: Map<ItemStatus, Int>`
4. For each category:
   - Collect category's active items
   - Group by date
   - Create `DateStatus` (date + status + itemCount)
5. Return `DashboardData` with summary and categories

### 3. Mapper - DashboardMapper.kt (MODIFIED)
**Location**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardMapper.kt`

**Before**:
- Accepted `ImmutableList<CategoryWithItems>`
- Extracted items from categories to build calendar
- Calculated summary from items
- 70 lines

**After**:
- Accepts `DashboardData`
- Builds calendar from `CalendarData` (dates and statuses only)
- Maps summary from status counts
- 56 lines (20% reduction)

**Key changes**:
- Now depends on `AppClock` and `RelativeTimeFormatter` for date formatting
- Creates `DateItemsUiModel` from `DateStatus` with empty items list
- Uses `appCalendarConfigMapper.createWithDatedContent()` instead of `createFromItems()`
- Maps summary counts from domain model

**Calendar building**:
```kotlin
val calendarDatedContent = dashboardCategory.calendarData.dateStatuses.map { dateStatus ->
  DateItemsUiModel(
    text = relativeTimeFormatter.format(today, dateStatus.date),
    status = dateStatus.status,
    date = dateStatus.date,
    items = persistentListOf(), // Empty - dashboard doesn't need item details
  )
}
```

### 4. ViewModel - DashboardViewModel.kt (MODIFIED)
**Location**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardViewModel.kt`

**Before**:
- Received `ImmutableList<CategoryWithItems>` from use case

**After**:
- Receives `DashboardData` from use case
- Parameter name changed from `categories` to `dashboardData`

**Change**:
```kotlin
// Before
.map { categories ->
  dashboardMapper.mapToPerCategoryState(categories = categories, firstDayOfWeek)
}

// After
.map { dashboardData ->
  dashboardMapper.mapToPerCategoryState(dashboardData = dashboardData, firstDayOfWeek)
}
```

## Architecture Benefits

### Clear Separation of Concerns
- **Use Case**: Calculates aggregated data (summary counts, date statuses)
- **Domain Model**: Contains only what's needed (no item details)
- **Mapper**: Transforms domain models to UI models with formatting
- **ViewModel**: Coordinates data flow

### Reduced Coupling
- Dashboard layer no longer knows about individual items
- Calendar receives only what it needs (dates + statuses)
- Summary is pre-calculated in domain layer

### Consistency
- Matches the pattern used in `CategoryDetail` refactoring
- Same `CalendarData` and `DateStatus` models used across features
- Unified approach to calendar data across the app

## Impact Summary

| Component | Before | After | Change |
|-----------|--------|-------|--------|
| ObtainDashboardCategoriesUseCase | 15 lines | 69 lines | +360% (added business logic) |
| DashboardMapper | 70 lines | 56 lines | -20% (simpler mapping) |
| DashboardData models | N/A | 3 new models | New domain layer |

**Overall**: Use case now owns the business logic (status calculation, aggregation), while mapper focuses on UI formatting. Domain model is clean and minimal.

## Compilation Status

✅ **Kotlin compilation successful** - All code changes compile without errors
- Verified with: `./gradlew compileDebugKotlin`
- No syntax errors or type mismatches
- Dependencies properly injected through Koin

Note: Full `assembleDebug` fails due to unrelated JDK/Gradle configuration issue with GraalVM JDK, not code changes.

## Files Changed
1. `domain/model/DashboardData.kt` - Created
2. `domain/usecase/ObtainDashboardCategoriesUseCase.kt` - Modified
3. `ui/screen/dashboard/DashboardMapper.kt` - Modified
4. `ui/screen/dashboard/DashboardViewModel.kt` - Modified
