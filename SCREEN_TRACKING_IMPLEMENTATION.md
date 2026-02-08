# Screen Tracking Implementation Summary

## Overview
Implemented screen tracking across all 12 screens and 4 bottom sheets (16 total) in the Caducity app using the `TrackScreen` composable and concrete `Screen` event classes.

All screen events include the "Screen" suffix in their tracking names for consistency (e.g., "DashboardScreen", "ItemActionsBottomSheetScreen").

## Files Modified

### 1. New File Created
- **`app/src/main/kotlin/com/alorma/caducity/feature/tracking/Screens.kt`**
  - Created concrete `Screen` subclasses for all 12 screens
  - Provides type-safe tracking events

### 2. Phase 1: Direct Tracking Addition (8 screens)
These screens already had proper screen/content separation or didn't have previews:

1. **DashboardScreen.kt**
   - Added `TrackScreen(screen = DashboardScreenEvent())` after ViewModel injection
   - Uses import alias to avoid naming conflict
   - Preserves preview functionality (uses `DashboardContent`)

2. **CategoryDetailScreen.kt**
   - Added `TrackScreen(screen = CategoryDetailScreenEvent())` after state collection
   - Uses import alias to avoid naming conflict
   - No previews affected

3. **CategoryDetailAddItemScreen.kt**
   - Added `TrackScreen(screen = AddItemScreen())` after ViewModel injection
   - Direct import (no naming conflict)
   - No previews affected

4. **CreateCategoryScreen.kt**
   - Added `TrackScreen(screen = CreateCategoryScreenEvent())` after ViewModel injection
   - Uses import alias to avoid naming conflict
   - Preserves preview functionality (uses `CreateCategoryPage`)

5. **FilteredItemsByStatusScreen.kt**
   - Added `TrackScreen(screen = FilteredItemsByStatusScreenEvent())` after ViewModel injection
   - Uses import alias to avoid naming conflict
   - No previews affected

6. **OnboardingScreen.kt**
   - Added `TrackScreen(screen = OnboardingScreenEvent())` after ViewModel injection
   - Uses import alias to avoid naming conflict
   - No previews affected

7. **BackupScreen.kt**
   - Added `TrackScreen(screen = BackupScreenEvent())` after ViewModel injection
   - Uses import alias to avoid naming conflict
   - Preserves preview functionality (uses `BackupScreenContent`)

8. **NotificationsSettingsScreen.kt**
   - Added `TrackScreen(screen = NotificationsSettingsScreenEvent())` at start of screen
   - Uses import alias to avoid naming conflict
   - Preserves preview functionality (uses `NotificationsSettingsContent`)

### 3. Phase 2: Refactored with Content Extraction (4 screens)
These screens needed content composables extracted to preserve preview functionality:

9. **SettingsRootScreen.kt**
   - Extracted `SettingsRootContent` (private composable)
   - Added `TrackScreen(screen = SettingsScreen())` in main screen
   - Updated preview to use `SettingsRootContent`
   - Direct import (no naming conflict)

10. **AppearanceSettingsScreen.kt**
    - Extracted `AppearanceSettingsContent` (private composable)
    - Added `TrackScreen(screen = AppearanceSettingsScreenEvent())` in main screen
    - Updated preview to use `AppearanceSettingsContent`
    - Uses import alias to avoid naming conflict

11. **DebugSettingsScreen.kt**
    - Extracted `DebugSettingsContent` (private composable)
    - Added `TrackScreen(screen = DebugSettingsScreenEvent())` in main screen
    - Updated preview to use `DebugSettingsContent`
    - Uses import alias to avoid naming conflict

12. **AboutScreen.kt**
    - Extracted `AboutScreenContent` (private composable)
    - Added `TrackScreen(screen = AboutScreenEvent())` in main screen
    - Updated preview to use `AboutScreenContent`
    - Uses import alias to avoid naming conflict

## Screen Event Classes

All screen event classes are defined in `feature/tracking/Screens.kt`:

### Main Feature Screens
- `DashboardScreen` → "DashboardScreen"
- `CategoryDetailScreen` → "CategoryDetailScreen"
- `AddItemScreen` → "AddItemScreen"
- `CreateCategoryScreen` → "CreateCategoryScreen"
- `FilteredItemsByStatusScreen` → "FilteredItemsByStatusScreen"
- `OnboardingScreen` → "OnboardingScreen"

### Settings Screens
- `SettingsScreen` → "SettingsScreen"
- `AppearanceSettingsScreen` → "AppearanceSettingsScreen"
- `BackupScreen` → "BackupScreen"
- `NotificationsSettingsScreen` → "NotificationsSettingsScreen"
- `DebugSettingsScreen` → "DebugSettingsScreen"
- `AboutScreen` → "AboutScreen"

### Bottom Sheets (tracked as screens)
- `ItemActionsBottomSheetScreen` → "ItemActionsBottomSheetScreen"
- `ProductDeleteBottomSheetScreen` → "ProductDeleteBottomSheetScreen"
- `ProductClearItemsBottomSheetScreen` → "ProductClearItemsBottomSheetScreen"
- `ProductItemsBottomSheetScreen` → "ProductItemsBottomSheetScreen"

## Import Pattern

Two patterns were used based on naming conflicts:

### Pattern 1: Import Alias (naming conflict)
```kotlin
import com.alorma.caducity.feature.tracking.DashboardScreen as DashboardScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen

TrackScreen(screen = DashboardScreenEvent())
```

### Pattern 2: Direct Import (no naming conflict)
```kotlin
import com.alorma.caducity.feature.tracking.AddItemScreen
import com.alorma.caducity.feature.tracking.TrackScreen

TrackScreen(screen = AddItemScreen())
```

## Refactoring Pattern

For screens requiring refactoring:

```kotlin
// Before
@Composable
fun SomeScreen(...) {
  AppScaffold(...) { /* content */ }
}

@Preview
fun Preview() { SomeScreen(...) }

// After
@Composable
fun SomeScreen(...) {
  TrackScreen(screen = SomeScreenEvent())
  SomeScreenContent(...)
}

@Composable
private fun SomeScreenContent(...) {
  AppScaffold(...) { /* content */ }
}

@Preview
fun Preview() { SomeScreenContent(...) } // Preview uses content
```

## Key Principles Followed

1. **Preview Preservation**: All previews continue to work by using content composables
2. **Lifecycle Tracking**: `TrackScreen` automatically tracks on `ON_START` lifecycle events
3. **Type Safety**: Each screen has its own concrete `Screen` subclass
4. **Naming Conflicts**: Used import aliases where composable names match screen event class names
5. **Clean Separation**: Main screen composables handle DI and tracking, content composables handle UI

## Bottom Sheet Tracking

Bottom sheets are also tracked as screens since they represent distinct user flows and modal interactions:

1. **ItemActionsBottomSheet** (`ui/components/bottomsheet/ItemActionsBottomSheet.kt`)
   - Shows actions for items (consume, freeze, delete)
   - Tracked in `ItemActionsBottomSheetContent` composable
   - Content composable extracted for tracking

2. **ProductDeleteBottomSheet** (`ui/screen/category/detail/product/ProductBottomSheets.kt`)
   - Handles product deletion with item relocation options
   - Tracked in `DeleteProductWithItemsBottomSheetContent` composable
   - Content composable extracted for tracking

3. **ProductClearItemsBottomSheet** (`ui/screen/category/detail/product/ProductBottomSheets.kt`)
   - Clears consumed or all items from a product
   - Tracked in `ClearItemsBottomSheetContent` composable
   - Content composable extracted for tracking

4. **ProductItemsBottomSheet** (`ui/screen/dashboard/filtered/ProductItemsBottomSheet.kt`)
   - Shows list of items for a specific product
   - Tracked in `ProductItemsBottomSheetContent` composable
   - Content composable extracted for tracking

## Verification

✅ Build successful: `./gradlew assembleDebug` completed without errors
✅ All 12 screens + 4 bottom sheets (16 total) now emit screen tracking events
✅ All previews preserved and functional
✅ No breaking changes to existing functionality
✅ All tracking event names include "Screen" suffix for consistency

## Testing Navigation Flow

To verify tracking, navigate through:
```
Dashboard → Category Detail → Add Item
Dashboard → Create Category
Dashboard → Filtered Items
Dashboard → Settings → (all settings screens)
First launch → Onboarding
```

Each navigation should emit a screen view event with the corresponding screen name.

## Naming Convention

All screen event names include the "Screen" suffix:
- **Class name**: `DashboardScreen` (Kotlin class)
- **Event name**: `"DashboardScreen"` (string sent to Firebase/Timber)

This provides:
- **Consistency**: All screen events follow the same pattern
- **Clarity**: Easy to distinguish screen events from action events in analytics
- **Searchability**: Filter analytics by "Screen" suffix to find all screen views

## Notes

- **LanguageSettingsScreen**: Not found in codebase (may be integrated into AppearanceSettings or removed)
- **No ViewModel Changes**: All tracking is purely presentational at the screen level
- **Firebase/Timber**: Events automatically routed to configured trackers via `EventTracker` system
- **Bottom Sheets as Screens**: Bottom sheets are tracked as screens because they represent distinct modal user flows
- **Event Names**: All event names include "Screen" suffix (e.g., "DashboardScreen", "ItemActionsBottomSheetScreen")
