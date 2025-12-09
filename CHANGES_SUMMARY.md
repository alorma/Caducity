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
- **Visual Changes**:
  - **Color**: When a day is today AND has a product instance expiring:
    - Uses `MaterialTheme.colorScheme.primary` (instead of section color)
    - This makes today stand out with the app's primary accent color
  - **Shape**: When a day is today AND has a product instance:
    - Uses `MaterialShapes.Cookie6Sided` (instead of Cookie4Sided)
    - This provides a distinct shape to differentiate today from other days
  - Regular days with items continue to use section colors and Cookie4Sided shape
  - Days without items remain unstyled

## Visual Behavior

### Before
- All days with product instances: Section color + Cookie4Sided shape
- Days without instances: No styling

### After
- **Today with product instance**: Primary color + Cookie6Sided shape ⭐
- **Other days with instances**: Section color + Cookie4Sided shape
- **Days without instances**: No styling

## Technical Details

### Color Logic (Priority Order)
```kotlin
when {
  isToday && hasItem -> MaterialTheme.colorScheme.primary  // Primary accent
  hasItem -> DashboardSectionColors.getSectionColors(sectionType).container  // Section color
  else -> Color.Unspecified  // No styling
}
```

### Shape Logic (Priority Order)
```kotlin
when {
  isToday && hasItem -> MaterialShapes.Cookie6Sided.toShape()  // Distinct shape
  hasItem -> MaterialShapes.Cookie4Sided.toShape()  // Regular shape
  else -> RectangleShape  // No shape
}
```

## Dependencies
- Uses existing `AppClock` abstraction for testable time operations
- Uses existing Material 3 Expressive API shapes (Cookie4Sided, Cookie6Sided)
- Uses existing theme system for primary color

## Notes
- The implementation only highlights today when there are product instances expiring on that date
- If today has no expiring products, it appears as a regular day (no special styling)
- The primary color ensures today is immediately noticeable while maintaining the section color for context on other days
- The different shape (Cookie6Sided vs Cookie4Sided) provides an additional visual cue beyond just color
