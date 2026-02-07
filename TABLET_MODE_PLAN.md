# Tablet Mode Implementation Plan

## Context

The Caducity app currently renders with a single fixed layout across all device sizes (phones, tablets, foldables). Despite documentation suggesting adaptive UI exists, there is no window size detection or responsive layouts implemented. All screens use hardcoded constraints that don't adapt to larger screens.

This plan implements comprehensive tablet mode support with:
- **Master-detail layouts** for Dashboard and Category Detail screens
- **Adaptive calendar views** (week on phones, month on tablets)
- **Responsive item grids** (3 columns on phones, 5-8 on tablets)
- **Material 3 window size classes** (Compact <600dp, Medium 600-840dp, Expanded >840dp)

The architecture is already well-structured with clean separation of concerns, making this primarily a UI adaptation task rather than a fundamental restructure.

## GitHub Issues

| Phase   | Issue                                                 | Description                        |
|---------|-------------------------------------------------------|------------------------------------|
| Phase 1 | [#110](https://github.com/alorma/Caducity/issues/110) | Foundation - Window Size Detection |
| Phase 2 | [#111](https://github.com/alorma/Caducity/issues/111) | Calendar Mode Adaptation           |
| Phase 3 | [#112](https://github.com/alorma/Caducity/issues/112) | Responsive Item Grids              |
| Phase 4 | [#113](https://github.com/alorma/Caducity/issues/113) | Dashboard Master-Detail Layout     |
| Phase 5 | [#114](https://github.com/alorma/Caducity/issues/114) | Category Detail Embedded Mode      |
| Phase 6 | [#115](https://github.com/alorma/Caducity/issues/115) | String Resources                   |
| Phase 7 | [#116](https://github.com/alorma/Caducity/issues/116) | Testing & Verification             |

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

## Phase 4: Dashboard Master-Detail Layout

> **GitHub Issue:** [#113](https://github.com/alorma/Caducity/issues/113)

### 4.1 Create Dashboard Layout Mode State

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardLayoutMode.kt` (new file)

```kotlin
package com.alorma.caducity.ui.screen.dashboard

/**
 * Dashboard layout mode for tablet adaptive UI.
 */
sealed interface DashboardLayoutMode {
    /** Full-screen mode: horizontal category scroll + full month calendar */
    data object FullScreen : DashboardLayoutMode

    /** Master-detail mode: category list (left) + category detail (right) */
    data class MasterDetail(val selectedCategoryId: String) : DashboardLayoutMode
}
```

### 4.2 Update DashboardViewModel for Layout Mode

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardViewModel.kt`

Add layout mode state:

```kotlin
class DashboardViewModel(/* ... */) : ViewModel() {

    private val _layoutMode = MutableStateFlow<DashboardLayoutMode>(
        DashboardLayoutMode.FullScreen
    )
    val layoutMode: StateFlow<DashboardLayoutMode> = _layoutMode.asStateFlow()

    fun onCategorySelected(categoryId: String) {
        _layoutMode.value = DashboardLayoutMode.MasterDetail(categoryId)
    }

    fun onBackToFullScreen() {
        _layoutMode.value = DashboardLayoutMode.FullScreen
    }

    // ... existing code
}
```

### 4.3 Create Master-Detail Layout Composable

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/components/DashboardMasterDetailLayout.kt` (new file)

```kotlin
package com.alorma.caducity.ui.screen.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailScreen

/**
 * Master-detail layout for tablet dashboard.
 *
 * Layout: [Category List (40%)] | [Category Detail (60%)]
 */
@Composable
fun DashboardMasterDetailLayout(
    selectedCategoryId: String,
    categories: ImmutableList<CategoryCalendarState>,
    calendarMode: CalendarMode,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Master pane: Category list (40% width)
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            DashboardSuccessContentList(
                categories = categories,
                calendarMode = CalendarMode.WEEK,  // Always week in collapsed mode
                onNavigateToCategory = { categoryId ->
                    onCategorySelected(categoryId)
                },
                // ... other parameters
            )
        }

        // Detail pane: Category detail (60% width)
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(start = 16.dp)  // Separator margin
        ) {
            CategoryDetailScreen(
                categoryId = selectedCategoryId,
                // Embedded mode - no top bar, no back navigation
                isEmbeddedMode = true,
            )
        }
    }
}
```

### 4.4 Update Dashboard Screen for Adaptive Layout

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardScreen.kt`

Add layout mode handling:

```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val layoutMode by viewModel.layoutMode.collectAsState()
    val calendarState by calendarPreferences.asState().collectAsState()

    // ... existing state collection ...

    when (val state = uiState) {
        is DashboardState.Success -> {
            // Tablet mode: master-detail layout
            if (windowSizeClass.isExpandedOrMedium() &&
                layoutMode is DashboardLayoutMode.MasterDetail) {

                DashboardMasterDetailLayout(
                    selectedCategoryId = (layoutMode as DashboardLayoutMode.MasterDetail).selectedCategoryId,
                    categories = state.categories,
                    calendarMode = calendarState.calendarMode,
                    onCategorySelected = { categoryId ->
                        viewModel.onCategorySelected(categoryId)
                    },
                )
            }
            // Full-screen mode or phone
            else {
                DashboardSuccessContent(
                    state = state,
                    calendarMode = if (windowSizeClass.isExpandedOrMedium() &&
                                     layoutMode is DashboardLayoutMode.FullScreen) {
                        CalendarMode.MONTH  // Full month in tablet full-screen mode
                    } else {
                        calendarState.calendarMode  // Respect preference
                    },
                    onNavigateToCategory = { categoryId ->
                        if (windowSizeClass.isExpandedOrMedium()) {
                            viewModel.onCategorySelected(categoryId)
                        } else {
                            // Phone: navigate full-screen
                            navController.navigate(CategoryDetailRoute(categoryId))
                        }
                    },
                )
            }
        }
        // ... other states
    }
}
```

### 4.5 Update DashboardSuccessContent for Horizontal Scroll

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/components/DashboardSuccessContentList.kt`

Add horizontal scroll mode for tablet full-screen:

```kotlin
@Composable
internal fun DashboardSuccessContentList(
    categories: ImmutableList<CategoryCalendarState>,
    calendarMode: CalendarMode,
    isHorizontalLayout: Boolean = false,  // New parameter
    // ... existing parameters
) {
    if (isHorizontalLayout) {
        // Tablet full-screen mode: horizontal scroll
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(categories) { category ->
                CategoryCalendarCard(
                    category = category,
                    calendarMode = CalendarMode.MONTH,  // Force month in horizontal
                    modifier = Modifier.width(400.dp),  // Fixed card width
                )
            }
        }
    } else {
        // Existing vertical LazyColumn implementation
        LazyColumn(/* ... */) {
            // ... existing code
        }
    }
}
```

---

## Phase 5: Category Detail Master-Detail Layout

> **GitHub Issue:** [#114](https://github.com/alorma/Caducity/issues/114)

### 5.1 Update CategoryDetailScreen for Embedded Mode

**File:** `app/src/main/kotlin/com/alorma/caducity/ui/screen/category/detail/CategoryDetailScreen.kt`

Add embedded mode parameter to hide top bar and navigation:

```kotlin
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    isEmbeddedMode: Boolean = false,  // New parameter
    viewModel: CategoryDetailViewModel = koinViewModel { parametersOf(categoryId) },
) {
    // ... existing code ...

    AppScaffold(
        topBar = {
            if (!isEmbeddedMode) {  // Only show top bar in standalone mode
                TopAppBar(
                    title = { Text(state.category.name) },
                    navigationIcon = { /* back button */ }
                )
            }
        },
        // ... rest of scaffold
    ) {
        // ... content
    }
}
```

### 5.2 Handle Back Navigation in Embedded Mode

The CategoryDetailScreen should not handle back navigation when embedded in master-detail layout. Update back press handling:

```kotlin
if (!isEmbeddedMode) {
    BackHandler {
        navController.popBackStack()
    }
}
```

---

## Phase 6: String Resources

> **GitHub Issue:** [#115](https://github.com/alorma/Caducity/issues/115)

### 6.1 Add New String Resources

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

## Phase 7: Testing & Verification

> **GitHub Issue:** [#116](https://github.com/alorma/Caducity/issues/116)

### 7.1 Manual Testing Checklist

**Tablet Mode (>600dp width):**
- [ ] Dashboard shows horizontal category scroll with month calendars (full-screen mode)
- [ ] Clicking category enters master-detail mode (list left, detail right)
- [ ] Selecting different categories updates detail pane without navigation
- [ ] Item grids show 5-7 columns in category detail
- [ ] Calendar mode setting works (Week/Month/Auto)
- [ ] Rotation maintains state correctly

**Phone Mode (<600dp width):**
- [ ] Dashboard shows vertical category list with week calendars
- [ ] Clicking category navigates to full-screen detail
- [ ] Item grids show 3 columns
- [ ] Calendar mode respects user preference
- [ ] Back navigation works correctly

**Settings:**
- [ ] Calendar mode setting persists across app restarts
- [ ] Auto mode switches between week/month based on screen size
- [ ] Manual week/month modes work on all screen sizes

### 7.2 Edge Cases

- [ ] App handles screen size changes during runtime (foldables, split-screen)
- [ ] Master-detail layout handles very narrow detail pane (small split-screen)
- [ ] Horizontal category scroll handles single category gracefully
- [ ] Empty states render correctly in both layouts
- [ ] Loading states don't flash when switching modes

### 7.3 Automated Testing

Create screenshot tests for adaptive layouts:

```bash
# Generate golden images for both phone and tablet
./gradlew updateDebugScreenshotTest

# Validate screenshots after changes
./gradlew testDebugScreenshotTest
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

1. **Window size detection** using Material 3 window size classes
2. **Adaptive calendar** that switches between week (phones) and month (tablets)
3. **Responsive item grids** that adapt column count to screen width
4. **Master-detail layouts** for Dashboard and Category Detail screens
5. **User preferences** for manual calendar mode override

The implementation leverages existing components (CaducityMonthCalendar already exists but unused) and maintains the current clean architecture. No breaking changes to data layer or domain logic are required—this is purely a UI adaptation layer.

Expected effort: ~3-5 days for implementation + testing + polish.
