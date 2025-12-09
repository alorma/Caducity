# Week Calendar Today Highlighting - Implementation Summary

## Overview
This implementation adds visual highlighting for today's date in the week calendar view of product expiration dates.

## Changes Made

### 1. Updated Data Model (`DashboardUiModel.kt`)
- Added `today: LocalDate` field to `ProductUiModel` to pass current date to UI components
- This allows the UI layer to identify which day is today without recalculating

### 2. Updated Mapper (`DashboardMapper.kt`)
- Injected `AppClock` dependency to get current time
- Modified `toUiModel()` to calculate today's date using `appClock.now()` and convert to LocalDate
- Passes `today` to `ProductUiModel` constructor

### 3. Updated UI (`DashboardScreen.kt`)
- Modified `ProductItem` composable's week calendar day rendering logic
- Added `isToday` check by comparing `weekDay.date` with `product.today`
- **Smart Calendar Initial Week**:
  - Shows today's week if any items expire on or before today
  - Shows earliest item's week if all items are in the future
  - Ensures users see the most relevant information first
- **Visual Changes**:
  - **Color - Today with items**: `MaterialTheme.colorScheme.primary` (vibrant accent)
  - **Color - Today without items**: `MaterialTheme.colorScheme.primaryContainer` (subtle indicator)
  - **Color - Other days with items**: Section-specific colors
  - **Shape - Today with items**: `MaterialShapes.Cookie6Sided` (6-sided for emphasis)
  - **Shape - Today without items**: `MaterialShapes.Cookie4Sided` (4-sided, still distinct)
  - **Shape - Other days with items**: `MaterialShapes.Cookie4Sided` (standard)
  - Days without items remain unstyled

## Visual Behavior

### Before
- All days with product instances: Section color + Cookie4Sided shape
- Days without instances: No styling

### After
- **Today with product instance**: Primary color + Cookie6Sided shape ⭐
- **Today without product instance**: Primary container color + Cookie4Sided shape 🟦
- **Other days with instances**: Section color + Cookie4Sided shape
- **Days without instances**: No styling

## Technical Details

### Color Logic (Priority Order)
```kotlin
when {
  isToday && hasItem -> MaterialTheme.colorScheme.primary  // Primary accent
  isToday && !hasItem -> MaterialTheme.colorScheme.primaryContainer  // Subtle today indicator
  hasItem -> DashboardSectionColors.getSectionColors(sectionType).container  // Section color
  else -> Color.Unspecified  // No styling
}
```

### Shape Logic (Priority Order)
```kotlin
when {
  isToday && hasItem -> MaterialShapes.Cookie6Sided.toShape()  // Distinct shape for emphasis
  isToday && !hasItem -> MaterialShapes.Cookie4Sided.toShape()  // Regular shape for today
  hasItem -> MaterialShapes.Cookie4Sided.toShape()  // Regular shape
  else -> RectangleShape  // No shape
}
```

## Dependencies
- Uses existing `AppClock` abstraction for testable time operations
- Uses existing Material 3 Expressive API shapes (Cookie4Sided, Cookie6Sided)
- Uses existing theme system for primary color

## Notes
- **Today with items**: Highlighted with primary color + Cookie6Sided shape (most prominent)
- **Today without items**: Highlighted with primaryContainer color + Cookie4Sided shape (subtle indicator)
- **Other days**: Show section colors only when they have items, or no styling if empty
- The primary color ensures today is immediately noticeable while maintaining the section color for context on other days
- Using primaryContainer for empty today provides a subtle indicator without overwhelming the UI
- The different shape (Cookie6Sided vs Cookie4Sided) provides an additional visual cue beyond just color
