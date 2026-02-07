# Tablet Mode Implementation Plan

## Context

The Caducity app currently renders with a single fixed layout across all device sizes (phones, tablets, foldables). Despite documentation suggesting adaptive UI exists, there is no window size detection or responsive layouts implemented. All screens use hardcoded constraints that don't adapt to larger screens.

This plan implements comprehensive tablet mode support with:
- **Responsive screen layouts** optimized for wider displays (no master-detail panes)
- **Adaptive calendar views** (week on phones, month on tablets)
- **Responsive item grids** (3 columns on phones, 5-7 on tablets)
- **Material 3 window size classes** (Compact <600dp, Medium 600-840dp, Expanded >840dp)
- **Maintained navigation structure** - single-pane navigation across all devices

**Key Approach:** Instead of implementing complex master-detail pane systems, we optimize each individual screen for tablet displays while keeping the familiar single-pane navigation structure. This provides a better tablet experience without fundamentally changing the app's navigation patterns.

## GitHub Issues

| Phase    | Status | Issue                                                 | Description                               |
|----------|--------|-------------------------------------------------------|-------------------------------------------|
| Phase 1  | ✅      | [#110](https://github.com/alorma/Caducity/issues/110) | Foundation - Window Size Detection        |
| Phase 2  | ✅      | [#111](https://github.com/alorma/Caducity/issues/111) | Calendar Mode Adaptation                  |
| Phase 3  | ✅      | [#112](https://github.com/alorma/Caducity/issues/112) | Responsive Item Grids                     |
| Phase 4  | ✅      | [#113](https://github.com/alorma/Caducity/issues/113) | Dashboard Responsive Layout (In Progress) |
| Phase 5  | ✅      | [#114](https://github.com/alorma/Caducity/issues/114) | Category Detail Side-by-Side Layout       |
| Phase 6  | ✅      | [#123](https://github.com/alorma/Caducity/issues/123) | Settings Screens Centered Layout          |
| Phase 7  | ✅      | [#124](https://github.com/alorma/Caducity/issues/124) | Create Category Centered Form             |
| Phase 8  | ✅      | [#125](https://github.com/alorma/Caducity/issues/125) | Filtered Items Centered List              |
| Phase 9  | 📋     | [#115](https://github.com/alorma/Caducity/issues/115) | String Resources (if needed)              |
| Phase 10 | 📋     | [#116](https://github.com/alorma/Caducity/issues/116) | Testing & Verification                    |

**Project Board:** https://github.com/users/alorma/projects/3/views/1

---

## Phase 1: Foundation - Window Size Detection

> **GitHub Issue:** [#110](https://github.com/alorma/Caducity/issues/110)

### 1.1 Add Window Size Class Dependency

**File:** `gradle/libs.versions.toml`

Add Material 3 window size class library to enable adaptive UI:

```toml
[versions]
# ... existing versions ...
material3-window = "1.3.1"  # Or latest stable version

[libraries]
# ... existing libraries ...
androidx-material3-window-size = { group = "androidx.compose.material3", name = "material3-window-size-class", version.ref = "material3-window" }
```

**File:** `app/build.gradle.kts`

Add dependency:
```kotlin
dependencies {
    // ... existing dependencies ...
    implementation(libs.androidx.material3.window.size)
}
```

### 1.2 Create Window Size Utilities

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/adaptive/WindowSizeClass.kt` (new file)

Create reusable window size detection utilities:

```kotlin
package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Utility to determine if the current window is considered "expanded" (tablet/desktop).
 * Uses Material 3 window size class definitions:
 * - Compact: <600dp (phones)
 * - Medium: 600-840dp (small tablets, unfolded foldables)
 * - Expanded: >840dp (large tablets, desktops)
 */
@Composable
fun WindowSizeClass.isExpanded(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Expanded
}

@Composable
fun WindowSizeClass.isExpandedOrMedium(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Expanded ||
           widthSizeClass == WindowWidthSizeClass.Medium
}

@Composable
fun WindowSizeClass.isCompact(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}

/**
 * Calculate responsive column count for item grids based on available width.
 *
 * Breakpoints:
 * - <600dp (Compact): 3 columns
 * - 600-840dp (Medium): 5 columns
 * - >840dp (Expanded): 7 columns
 */
@Composable
fun WindowSizeClass.calculateGridColumns(): Int {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 3
        WindowWidthSizeClass.Medium -> 5
        WindowWidthSizeClass.Expanded -> 7
        else -> 3
    }
}
```

### 1.3 Integrate Window Size Detection in MainActivity

**File:** `app/src/main/kotlin/com/alorma/caducity/MainActivity.kt`

Pass WindowSizeClass to App composable:

```kotlin
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)  // Add this
            App(windowSizeClass = windowSizeClass)  // Pass to App
        }
    }
}
```

### 1.4 Thread WindowSizeClass Through App

**File:** `app/src/main/kotlin/com/alorma/caducity/App.kt`

Update App composable signature and pass to screens:

```kotlin
@Composable
fun App(
    windowSizeClass: WindowSizeClass,  // Add parameter
    // ... existing parameters
) {
    // ... existing theme setup ...

    // Pass windowSizeClass to navigation content
    // Screens will receive it via CompositionLocal or direct parameter
}
```

**Create CompositionLocal for WindowSizeClass:**

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/adaptive/LocalWindowSizeClass.kt` (new file)

```kotlin
package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}
```

Update App.kt to provide the LocalWindowSizeClass:

```kotlin
@Composable
fun App(windowSizeClass: WindowSizeClass, ...) {
    AppTheme {
        CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
            // ... navigation content
        }
    }
}
```

---

## Phase 2: Calendar Mode Adaptation ✅ COMPLETED

> **GitHub Issue:** [#111](https://github.com/alorma/Caducity/issues/111)
> **Status:** ✅ Implemented and tested
> **Implementation:** Automatic calendar adaptation without user settings

### Implementation Summary

Phase 2 implements automatic calendar adaptation based on screen size, without user-configurable settings. The calendar automatically switches between week and month views based on the device width.

**Key Changes:**
- Created `AdaptiveCalendar` composable that automatically switches views
- Updated `WindowSizeClass` helpers to use new adaptive library with `isWidthAtLeastBreakpoint()`
- Integrated adaptive calendars in Dashboard and Category Detail screens
- No user settings required - purely automatic behavior

**Behavior:**
- **Compact (<600dp)**: Week calendar view
- **Medium/Expanded (≥600dp)**: Month calendar view
- Gated by `TabletModeRemoteConfig` feature flag

### 2.1 Add Calendar Mode Enum

**SKIPPED:** User-configurable calendar mode was removed in favor of automatic adaptation only.

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/components/calendar/CalendarMode.kt` (new file)

```kotlin
package com.alorma.caducity.ui.components.calendar

/**
 * Calendar display mode preference.
 */
enum class CalendarMode {
    /** Show week view only */
    WEEK,

    /** Show month view only */
    MONTH,

    /** Automatically select based on screen size (week on phones, month on tablets) */
    AUTO
}
```

### 2.2 Extend Calendar Preferences

**SKIPPED:** No calendar preferences needed for automatic-only behavior.

```kotlin
data class CalendarConfigState(
    val firstDayOfWeek: DayOfWeek,
    val calendarMode: CalendarMode = CalendarMode.AUTO,  // Add this field
)

class CalendarPreferences(settings: Settings) {
    // ... existing firstDayOfWeek logic ...

    // Add calendar mode preference
    var calendarMode: CalendarMode
        get() = CalendarMode.valueOf(
            settings.getStringOrNull(KEY_CALENDAR_MODE) ?: CalendarMode.AUTO.name
        )
        set(value) {
            settings.putString(KEY_CALENDAR_MODE, value.name)
        }

    fun asState(): StateFlow<CalendarConfigState> {
        return combine(
            firstDayOfWeekFlow(),
            calendarModeFlow(),
        ) { firstDay, mode ->
            CalendarConfigState(firstDay, mode)
        }.stateIn(/* ... */)
    }

    private fun calendarModeFlow(): Flow<CalendarMode> = callbackFlow {
        val listener = { _: String ->
            trySend(calendarMode)
        }
        settings.addStringListener(KEY_CALENDAR_MODE, listener)
        send(calendarMode)
        awaitClose { settings.removeListener(KEY_CALENDAR_MODE, listener) }
    }

    companion object {
        // ... existing constants ...
        private const val KEY_CALENDAR_MODE = "calendar_mode"
    }
}
```

### 2.3 Create AdaptiveCalendar Composable ✅

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/components/calendar/AdaptiveCalendar.kt`

**Implemented:** Simplified automatic-only version

```kotlin
package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Composable
import com.alorma.caducity.ui.adaptive.rememberIsExpandedOrMedium
import kotlinx.datetime.LocalDate

/**
 * Adaptive calendar that switches between week and month views based on screen size:
 * - Compact (<600dp): Week view
 * - Medium/Expanded (≥600dp): Month view
 */
@Composable
fun AdaptiveCalendar(
  appCalendarConfig: AppCalendarConfig,
  todayColor: Color,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val shouldShowMonth = rememberIsExpandedOrMedium()

  if (shouldShowMonth) {
    CaducityMonthCalendar(
      appCalendarConfig = appCalendarConfig,
      onDateClick = onDateClick,
      modifier = modifier,
    )
  } else {
    CaducityWeekCalendar(
      appCalendarConfig = appCalendarConfig,
      todayColor = todayColor,
      onDateClick = onDateClick,
      modifier = modifier,
    )
  }
}
```

### 2.4 Update Dashboard to Use Adaptive Calendar ✅

**Files Updated:**
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/components/DashboardSuccessContentList.kt`

**Implemented:** Replaced `CaducityWeekCalendar` with `AdaptiveCalendar` in all dashboard views. No preferences injection needed - adaptation is automatic.

### 2.5 Update Category Detail Screens ✅

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/CategoryDetailScreen.kt`

**Implemented:** Category detail screens always show **week calendar** (non-adaptive).
- **Rationale**: Detail screens have limited vertical space and always benefit from compact week view
- Dashboard uses adaptive calendars, but detail views stay consistent across all screen sizes

### 2.6 Add Calendar Mode Settings UI

**SKIPPED:** No settings UI needed - automatic adaptation only.

### 2.7 Update WindowSizeClass Helpers ✅

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/adaptive/WindowSizeClass.kt`

**Implemented:** Updated all window size helpers to use the new adaptive library:
- Migrated from deprecated `androidx.compose.material3.windowsizeclass` to `androidx.compose.material3.adaptive`
- Used `currentWindowAdaptiveInfo()` composable instead of passing WindowSizeClass
- Updated extension functions to use `isWidthAtLeastBreakpoint()` with proper constants:
  - `WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND` (600dp)
  - `WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND` (840dp)
- Created composable helpers: `rememberIsExpandedOrMedium()`, `rememberIsExpanded()`, `rememberIsCompact()`
- All helpers now respect `TabletModeRemoteConfig` feature flag

**Files Updated:**
- Removed WindowSizeClass parameter from `MainActivity.kt` and `App.kt`
- Removed deprecated imports and calculations

---

## Phase 3: Responsive Item Grids ✅ COMPLETED

> **GitHub Issue:** [#112](https://github.com/alorma/Caducity/issues/112)
> **Status:** ✅ Implemented and tested
> **Implementation:** Item grids now adapt column count based on screen size

### Implementation Summary

Phase 3 implements responsive item grids that adapt the number of columns based on screen size. The existing `rememberGridColumns()` helper from Phase 1 was utilized to make item grids in the product detail pages responsive.

**Key Changes:**
- Updated `StatusGroupCard` in `ProductTabContent.kt` to use `rememberGridColumns()`
- Replaced hardcoded `maxItemsInEachRow = 3` with adaptive column count
- Leverages existing window size detection infrastructure from Phase 1

**Behavior:**
- **Compact (<600dp)**: 3 columns (phones)
- **Medium (600-840dp)**: 5 columns (small tablets)
- **Expanded (>840dp)**: 7 columns (large tablets)
- Gated by `TabletModeRemoteConfig` feature flag

**Files Modified:**
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductTabContent.kt`
  - Added import for `rememberGridColumns()`
  - Updated `StatusGroupCard` composable to calculate column count dynamically

### 3.1 Create Responsive Column Calculator

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/adaptive/ResponsiveColumns.kt` (new file)

```kotlin
package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Calculate responsive column count for grids based on available width and minimum item width.
 *
 * @param availableWidth Total available width for the grid
 * @param minItemWidth Minimum desired width per item
 * @param spacing Horizontal spacing between items
 * @param windowSizeClass Window size class for fallback calculation
 * @return Number of columns to display
 */
@Composable
fun rememberResponsiveColumns(
    availableWidth: Dp,
    minItemWidth: Dp = 120.dp,
    spacing: Dp = 8.dp,
    windowSizeClass: WindowSizeClass = LocalWindowSizeClass.current,
): Int {
    return remember(availableWidth, minItemWidth, spacing, windowSizeClass) {
        if (availableWidth == Dp.Unspecified || availableWidth <= 0.dp) {
            // Fallback to window size class-based calculation
            windowSizeClass.calculateGridColumns()
        } else {
            // Calculate based on available width and min item size
            val totalSpacing = spacing.value
            val effectiveWidth = availableWidth.value
            val itemWithSpacing = minItemWidth.value + totalSpacing
            val columns = (effectiveWidth / itemWithSpacing).toInt().coerceAtLeast(1)
            columns.coerceIn(2, 8)  // Min 2, max 8 columns
        }
    }
}
```

### 3.2 Update ProductTabContent Item Grids

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductTabContent.kt`

Replace hardcoded `maxItemsInEachRow = 3` with responsive calculation:

```kotlin
@Composable
private fun StatusGroupCard(
    items: ImmutableList<ItemDetailUiModel>,
    onItemClick: (ItemDetailUiModel) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val columnCount = windowSizeClass.calculateGridColumns()  // Responsive columns

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = columnCount,  // Use calculated value
    ) {
        items.forEach { item ->
            SuggestionChip(
                onClick = { onItemClick(item) },
                label = { Text(text = item.text) },
            )
        }
    }
}
```

Alternative approach using `BoxWithConstraints` for more precise width-based calculation:

```kotlin
@Composable
private fun StatusGroupCard(
    items: ImmutableList<ItemDetailUiModel>,
    onItemClick: (ItemDetailUiModel) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columnCount = rememberResponsiveColumns(
            availableWidth = maxWidth,
            minItemWidth = 100.dp,  // Minimum chip width
            spacing = 8.dp,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = columnCount,
        ) {
            items.forEach { item ->
                SuggestionChip(
                    onClick = { onItemClick(item) },
                    label = { Text(text = item.text) },
                )
            }
        }
    }
}
```

---

## Phase 4: Dashboard Responsive Layout ✅ IN PROGRESS

> **GitHub Issue:** [#113](https://github.com/alorma/Caducity/issues/113)
> **Status:** 🚧 Partially implemented - dashboard layout complete, working on other screens
> **Approach:** No master-detail panes - keep existing navigation structure with responsive layouts

### Design Decision: Single-Pane Responsive Layouts

**Rationale:** Instead of implementing complex master-detail pane navigation, we maintain the existing navigation structure while optimizing individual screens for tablet displays.

**Key Principles:**
- Keep existing navigation flow (no split-pane views)
- Optimize layouts for wider screens
- Better use of horizontal space
- Maintain consistency with phone UX patterns

### 4.1 Dashboard Responsive Layout ✅ COMPLETED

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/components/DashboardSuccessContentList.kt`

**Implemented Changes:**
- **Compact mode** (phones <600dp): Vertical LazyColumn with week calendars
- **Expanded mode** (tablets ≥840dp): Horizontal LazyRow with full-height month calendars
- Summary cards: 2x2 grid on phones, single row of 4 cards on tablets
- Calendar spacing: 64dp between calendar columns on tablets
- Each calendar card: 340dp wide with `fillMaxHeight()`

**Implementation:**
```kotlin
@Composable
fun DashboardSuccessContentList(
  state: DashboardState.Success,
  onNavigateToCategory: (String) -> Unit,
  onNavigateToStatus: (ItemStatus) -> Unit,
  lazyListState: LazyListState,
) {
  val isExpanded = rememberIsExpanded()

  if (isExpanded) {
    DashboardExpandedLayout(/* horizontal LazyRow */)
  } else {
    DashboardCompactLayout(/* vertical LazyColumn */)
  }
}
```

**Summary Cards Adaptation:**
```kotlin
// DashboardSummaryCard.kt
@Composable
fun DashboardSummaryCard(
  summary: DashboardSummary,
  onStatusClick: (ItemStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isExpanded = rememberIsExpanded()

  if (isExpanded) {
    // Single row with all 4 cards
    DashboardSummaryCardExpanded(/* ... */)
  } else {
    // 2x2 grid
    DashboardSummaryCardCompact(/* ... */)
  }
}
```

---

## Phase 6: Settings Screens Centered Layout

> **GitHub Issue:** [#TBD](https://github.com/alorma/Caducity/issues/TBD)
> **Status:** 📋 Planned

**Files:**
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/CategoryDetailScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/product/ProductTabContent.kt`

**Planned Layout:**

**Tablet Mode (≥840dp):**
```
┌─────────────────────────────────────────────────────┐
│ Top Bar (Title + Actions)                          │
├─────────────────┬───────────────────────────────────┤
│                 │                                   │
│   Month         │   Tabs (Scrollable)               │
│   Calendar      │   ─────────────────────           │
│   (1/3 width)   │   Product Tab Content (2/3 width) │
│                 │   - Status Groups                 │
│                 │   - Item Chips (7 columns)        │
│                 │   - FAB actions                   │
│                 │                                   │
└─────────────────┴───────────────────────────────────┘
```

**Phone Mode (<840dp):**
- Keep existing vertical layout (calendar → tabs → content)

**Implementation Plan:**

1. **Create Responsive Layout Composables:**
   - `CategoryDetailCompactLayout` - Current vertical layout for phones
   - `CategoryDetailExpandedLayout` - New side-by-side layout for tablets

2. **Update CategoryDetailSuccessContent:**
   ```kotlin
   @Composable
   private fun CategoryDetailSuccessContent(...) {
     val isExpanded = rememberIsExpanded()

     if (isExpanded) {
       CategoryDetailExpandedLayout(
         state = state,
         // Calendar on left (1/3)
         // Tabs + content on right (2/3)
       )
     } else {
       CategoryDetailCompactLayout(
         state = state,
         // Existing vertical layout
       )
     }
   }
   ```

3. **Calendar Changes:**
   - Tablet: Use `CaducityMonthCalendar` (full month view)
   - Phone: Keep `CaducityWeekCalendar`

4. **Layout Structure for Expanded Mode:**
   ```kotlin
   Row(modifier = Modifier.fillMaxSize()) {
     // Left pane: Month calendar (1/3)
     Surface(
       modifier = Modifier
         .weight(0.33f)
         .fillMaxHeight(),
       color = surfaceContainerHigh,
       shadowElevation = 2.dp,
     ) {
       CaducityMonthCalendar(
         appCalendarConfig = state.appCalendarConfig,
         onDateClick = { },
       )
     }

     // Right pane: Tabs + content (2/3)
     Column(
       modifier = Modifier
         .weight(0.67f)
         .fillMaxHeight()
     ) {
       // Tabs row with add button
       // HorizontalPager with ProductTabContent
     }
   }
   ```

5. **Product Tab Content:**
   - Already responsive with `rememberGridColumns()` (shows 7 columns on tablets)
   - No changes needed for item grids

6. **Empty State:**
   - Also needs responsive layout (calendar on left, empty message on right)

**Benefits:**
- Better use of horizontal space on tablets
- Month calendar provides more context
- Tabs and content area have more room (2/3 of screen)
- Maintains single-pane navigation
- Item grids already adaptive (will show 7 columns)

---

## Phase 7: Create Category Centered Form ✅ COMPLETED

> **GitHub Issue:** [#124](https://github.com/alorma/Caducity/issues/124)
> **Status:** ✅ Completed

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/create/CreateCategoryScreen.kt`

**Implementation Summary:**

Successfully implemented centered form layout for the Create Category screen on tablet devices.

**Changes Made:**
1. Added `rememberIsExpanded()` for window size detection
2. Wrapped form Column with responsive Box containers for centering
3. Applied 600dp max width constraint on tablet mode (≥840dp)
4. Centered bottomBar buttons to align with form content
5. Maintained full-width topBar and bottomBar containers

**Visual Behavior:**
- **Phone mode (<840dp):** Full-width form and buttons (unchanged)
- **Tablet mode (≥840dp):** Form and buttons centered with 600dp max width
- TopBar remains full width on all devices
- BottomAppBar container full width, buttons centered to match form

**Implementation Pattern:**
```kotlin
val isExpanded = rememberIsExpanded()

AppScaffold(
  topBar = { /* Full width */ },
  bottomBar = {
    BottomAppBar {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
      ) {
        Row(
          modifier = if (isExpanded) {
            Modifier.widthIn(max = 600.dp).fillMaxWidth()
          } else {
            Modifier.fillMaxWidth()
          }
        ) {
          // Buttons centered on tablets
        }
      }
    }
  }
) { paddingValues ->
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Box(
      modifier = if (isExpanded) {
        Modifier.widthIn(max = 600.dp).fillMaxWidth()
      } else {
        Modifier.fillMaxWidth()
      }
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 24.dp)
          .verticalScroll(rememberScrollState()),
      ) {
        // Form fields
      }
    }
  }
}
```

**Benefits:**
- Form doesn't stretch across entire tablet width
- Better readability and user focus
- Consistent with planned settings screen approach
- No changes to form validation or business logic
- Improved visual hierarchy on tablets

---

## Phase 7 (Original - Settings Screens Content)

**Files:**
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/SettingsRootScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/appearance/AppearanceSettingsScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/notifications/NotificationsSettingsScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/backup/BackupScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/about/AboutScreen.kt`
- `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/debug/DebugSettingsScreen.kt`

**Current Structure:**
- `Column` with full width settings cards
- `StyledSettingsGroup` wraps groups of settings with 2dp spacing
- `StyledSettingsCard` for individual setting items

**Planned Layout:**

**Tablet Mode (≥840dp):**
- Content centered on screen
- Maximum width: 600dp (2/3 of typical tablet screen)
- Horizontal padding ensures content doesn't stretch too wide

**Phone Mode (<840dp):**
- Keep current full-width layout with 16dp horizontal padding
- Existing behavior unchanged

**Implementation Approach:**

1. **Create Responsive Content Container:**
   ```kotlin
   @Composable
   fun ResponsiveSettingsContainer(
     modifier: Modifier = Modifier,
     content: @Composable () -> Unit,
   ) {
     val isExpanded = rememberIsExpanded()

     Box(
       modifier = modifier.fillMaxSize(),
       contentAlignment = Alignment.TopCenter,
     ) {
       Box(
         modifier = if (isExpanded) {
           Modifier
             .widthIn(max = 600.dp)
             .fillMaxWidth()
         } else {
           Modifier.fillMaxWidth()
         }
       ) {
         content()
       }
     }
   }
   ```

2. **Update All Settings Screens:**
   - Wrap the scrollable `Column` content with `ResponsiveSettingsContainer`
   - Keep existing horizontal padding (16.dp) for both modes
   - Tablet: Content will be centered with max 600dp width
   - Phone: Content fills width as before

3. **Example for SettingsRootScreen:**
   ```kotlin
   AppScaffold(
     topBar = { /* ... */ }
   ) { paddingValues ->
     ResponsiveSettingsContainer {
       Column(
         modifier = Modifier
           .fillMaxSize()
           .verticalScroll(rememberScrollState())
           .padding(paddingValues)
           .padding(horizontal = 16.dp),
         verticalArrangement = Arrangement.spacedBy(24.dp),
       ) {
         // Existing settings groups
       }
     }
   }
   ```

4. **Apply to All Settings Screens:**
   - Settings Root
   - Appearance Settings
   - Notifications Settings
   - Backup Settings
   - Debug Settings
   - About Screen

**Benefits:**
- Better readability on tablets (content not stretched too wide)
- Centered layout looks more professional
- Maintains existing vertical structure and groupings
- Consistent horizontal padding on both phone and tablet
- Simple wrapper pattern that's easy to apply consistently

**Visual Appearance:**

Phone (<840dp):
```
┌──────────────────────────┐
│ [Settings Card       ]   │ Full width with padding
│ [Settings Card       ]   │
└──────────────────────────┘
```

Tablet (≥840dp):
```
┌────────────────────────────────────────┐
│        [Settings Card]                 │ Centered, max 600dp
│        [Settings Card]                 │
└────────────────────────────────────────┘
```

---

## Phase 8: Filtered Items Centered List

> **GitHub Issue:** [#125](https://github.com/alorma/Caducity/issues/125)
> **Status:** 📋 Planned

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/create/CreateCategoryScreen.kt`

**Current Structure:**
- Full-width form with TextFields
- BottomAppBar with Cancel/Create buttons
- 24dp horizontal padding

**Planned Layout:**

**Tablet Mode (≥840dp):**
- Form centered on screen
- Maximum width: 600dp
- Better visual hierarchy with more breathing room

**Phone Mode (<840dp):**
- Keep current full-width form
- Existing 24dp padding

**Implementation Approach:**

1. **Use Same Pattern as Settings:**
   ```kotlin
   AppScaffold(
     topBar = { /* ... */ },
     bottomBar = { /* ... */ }
   ) { paddingValues ->
     Box(
       modifier = Modifier.fillMaxSize(),
       contentAlignment = Alignment.TopCenter,
     ) {
       Box(
         modifier = if (isExpanded) {
           Modifier.widthIn(max = 600.dp).fillMaxWidth()
         } else {
           Modifier.fillMaxWidth()
         }
       ) {
         Column(
           modifier = Modifier
             .fillMaxSize()
             .padding(paddingValues)
             .padding(horizontal = 24.dp)
             .verticalScroll(rememberScrollState()),
           verticalArrangement = Arrangement.spacedBy(16.dp),
         ) {
           // Existing form fields
         }
       }
     }
   }
   ```

2. **Benefits:**
   - Form doesn't stretch across entire tablet width
   - Better readability and user focus
   - Consistent with settings screen approach
   - No changes to form validation or logic

**Alternative Consideration:**
- Could add side-by-side preview of category (calendar mockup?)
- **Decision**: Start simple with centered form, evaluate preview later

---

## Phase 9: String Resources

> **GitHub Issue:** [#115](https://github.com/alorma/Caducity/issues/115)
> **Status:** ⏸️ On Hold - No new strings needed yet

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/filtered/FilteredItemsByStatusScreen.kt`

**Current Structure:**
- LazyColumn with category groups
- Each category shows filtered items
- Full-width list with 16dp padding

**Planned Layout:**

**Tablet Mode (≥840dp):**
- Content centered with max width 800dp (wider than settings for list content)
- Category cards displayed with better spacing
- Item chips within cards can use more columns (already responsive)

**Phone Mode (<840dp):**
- Keep current full-width layout
- Existing behavior unchanged

**Implementation Approach:**

1. **Center Content Container:**
   ```kotlin
   AppScaffold(
     topBar = { /* ... */ }
   ) { paddingValues ->
     Box(
       modifier = Modifier.fillMaxSize(),
       contentAlignment = Alignment.TopCenter,
     ) {
       Box(
         modifier = if (isExpanded) {
           Modifier.widthIn(max = 800.dp).fillMaxWidth()
         } else {
           Modifier.fillMaxWidth()
         }
       ) {
         LazyColumn(
           modifier = Modifier.fillMaxSize(),
           contentPadding = PaddingValues(16.dp),
           verticalArrangement = Arrangement.spacedBy(16.dp),
         ) {
           // Existing category items
         }
       }
     }
   }
   ```

2. **Why 800dp Max Width:**
   - List content benefits from slightly wider max width than forms
   - Allows category cards to breathe
   - Still prevents content from stretching too wide
   - Item grids already responsive (will show more columns)

3. **Future Enhancement (Optional):**
   - Could add filter chips at top (All, Expired, Expiring, Fresh, Frozen)
   - Quick status switcher without navigation
   - **Decision**: Start with centered layout, evaluate filters later

**Benefits:**
- Better list readability on tablets
- Category cards don't stretch too wide
- Item grids already adapt (rememberGridColumns() used in cards)
- Consistent centered approach across app

---

## Phase 5: Category Detail Side-by-Side Layout

> **GitHub Issue:** [#114](https://github.com/alorma/Caducity/issues/114)
> **Status:** 📋 Planned

---

---

## Phase 10: Testing & Verification

> **GitHub Issue:** [#116](https://github.com/alorma/Caducity/issues/116)
> **Status:** 📋 Planned

### 10.1 Manual Testing Checklist

**Files:** `app/src/main/res/values*/strings.xml`

Add strings for calendar mode settings:

```xml
<!-- English (values/strings.xml) -->
<string name="settings_calendar_mode">Calendar view</string>
<string name="settings_calendar_mode_description">Choose how calendars are displayed</string>
<string name="calendar_mode_week">Week view</string>
<string name="calendar_mode_month">Month view</string>
<string name="calendar_mode_auto">Automatic (adapt to screen size)</string>

<!-- Spanish (values-es/strings.xml) -->
<string name="settings_calendar_mode">Vista del calendario</string>
<string name="settings_calendar_mode_description">Elige cómo se muestran los calendarios</string>
<string name="calendar_mode_week">Vista semanal</string>
<string name="calendar_mode_month">Vista mensual</string>
<string name="calendar_mode_auto">Automático (adaptar al tamaño de pantalla)</string>

<!-- Catalan (values-ca/strings.xml) -->
<string name="settings_calendar_mode">Vista del calendari</string>
<string name="settings_calendar_mode_description">Tria com es mostren els calendaris</string>
<string name="calendar_mode_week">Vista setmanal</string>
<string name="calendar_mode_month">Vista mensual</string>
<string name="calendar_mode_auto">Automàtic (adaptar a la mida de pantalla)</string>
```

---

## Implementation Notes

### Critical Files to Modify

**Core Infrastructure:**
- `gradle/libs.versions.toml` - Add window size class dependency
- `app/build.gradle.kts` - Add dependency
- `MainActivity.kt` - Calculate and pass WindowSizeClass
- `App.kt` - Provide WindowSizeClass via CompositionLocal

**Adaptive Utilities (new files):**
- `ui/adaptive/WindowSizeClass.kt` - Window size detection helpers
- `ui/adaptive/LocalWindowSizeClass.kt` - CompositionLocal provider
- `ui/adaptive/ResponsiveColumns.kt` - Grid column calculation

**Calendar System:**
- `ui/components/calendar/CalendarMode.kt` (new) - Mode enum
- `ui/components/calendar/CalendarPreferences.kt` - Add mode preference
- `ui/components/calendar/AdaptiveCalendar.kt` (new) - Adaptive selector
- `ui/screen/dashboard/components/DashboardSuccessContentList.kt` - Use adaptive calendar
- `ui/screen/category/detail/CategoryDetailScreen.kt` - Use adaptive calendar
- `ui/screen/settings/appearance/AppearanceSettingsScreen.kt` - Add settings UI

**Item Grids:**
- `ui/screen/category/detail/product/ProductTabContent.kt` - Make responsive

**Dashboard Master-Detail:**
- `ui/screen/dashboard/DashboardLayoutMode.kt` (new) - Layout mode state
- `ui/screen/dashboard/DashboardViewModel.kt` - Add layout mode management
- `ui/screen/dashboard/components/DashboardMasterDetailLayout.kt` (new) - Master-detail UI
- `ui/screen/dashboard/DashboardScreen.kt` - Adaptive layout logic

**Category Detail:**
- `ui/screen/category/detail/CategoryDetailScreen.kt` - Add embedded mode

**Localization:**
- `app/src/main/res/values/strings.xml` (en)
- `app/src/main/res/values-es/strings.xml` (es)
- `app/src/main/res/values-ca/strings.xml` (ca)

### Architecture Decisions

**Why CompositionLocal for WindowSizeClass?**
- Avoids parameter drilling through many composable layers
- WindowSizeClass is global app state that rarely changes
- Easy access from any composable in the hierarchy

**Why Auto Mode as Default?**
- Best user experience: adapts automatically to device
- Users can override if they prefer specific view
- Follows Material Design guidelines for responsive UI

**Master-Detail Pane Ratio (40/60)?**
- 40% for category list provides enough space for category names + mini calendars
- 60% for detail pane allows comfortable viewing of category content
- Ratio can be adjusted based on user testing feedback

**Why Keep Week Calendar in Collapsed Mode?**
- Space constraint: category list + week calendar fits better than month
- Consistency: collapsed mode mimics phone experience
- User explicitly selected category, focus on detail pane

### Performance Considerations

- Window size class changes trigger recomposition only of adaptive composables
- Master-detail layout reuses existing CategoryDetailScreen composable
- Horizontal LazyRow in dashboard uses same data as vertical LazyColumn
- No data duplication or extra network/database queries needed

### Future Enhancements

**Out of Scope (but possible later):**
- Landscape-specific layouts for phones
- Three-pane layout for ultra-wide screens (>1200dp)
- Resizable master-detail divider (drag to adjust pane ratio)
- Picture-in-picture calendar widget
- Keyboard shortcuts for tablet navigation
- Mouse hover states for desktop use

---

## Summary

This plan implements comprehensive tablet mode support through:

1. ✅ **Window size detection** using Material 3 adaptive window size classes
2. ✅ **Adaptive calendar** that switches between week (phones) and month (tablets)
3. ✅ **Responsive item grids** that adapt column count to screen width (3/5/7 columns)
4. 🚧 **Responsive screen layouts** optimized for wider displays (no panes)
5. ✅ **Firebase Remote Config** integration for gradual rollout via feature flag

**Design Philosophy:** Single-pane navigation with screen-by-screen optimizations rather than master-detail panes. This maintains consistency with phone UX while providing better layouts for tablets.

**Implementation Status:**
- ✅ Phases 1-3: Foundation, calendars, and grids complete
- 🚧 Phase 4: Dashboard layout complete
- 📋 Phase 5: Category Detail side-by-side layout (planned)
- 📋 Phase 6: Settings screens centered layout (planned)
- ✅ Phase 7: Create Category centered form (complete)
- 📋 Phase 8: Filtered Items centered list (planned)
- 📋 Phases 9-10: String resources and testing (planned)

The implementation leverages existing components and maintains the current clean architecture. No breaking changes to data layer or domain logic are required—this is purely a UI adaptation layer.

**Current Progress:** Dashboard fully responsive, Create Category form centered on tablets. Next steps:
1. **Phase 5**: Category Detail with side-by-side layout (calendar left 1/3, tabs/content right 2/3)
2. **Phase 6**: Settings screens with centered layout (max 600dp width)
3. **Phase 8**: Filtered Items list with centered layout (max 800dp width)
