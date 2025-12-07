# Caducity Navigation Structure

This document describes the navigation implementation in the Caducity app.

## Overview

The app uses **Navigation 3 UI for Compose Multiplatform** with type-safe navigation using Kotlin Serialization, combined with **Material 3 Adaptive Navigation** for responsive layouts that adapt to different screen sizes.

## Navigation Architecture

### Screen Definitions

The app has three main screens defined in `Screen.kt`:

1. **Dashboard** - The main landing screen showing product list
2. **CreateProduct** - Form screen for adding new products
3. **Settings** - App settings and preferences

### Navigation Structure

```
App (Root)
├── Adaptive Navigation Suite (responds to screen size)
│   ├── Compact (Phone): Bottom Navigation Bar
│   ├── Medium (Tablet): Navigation Rail
│   └── Expanded (Desktop): Navigation Drawer
│   
│   Navigation Items:
│   ├── Dashboard (Home Icon)
│   └── Settings (Settings Icon)
│
└── NavGraph
    ├── Dashboard Screen (Start Destination)
    │   └── Can navigate to CreateProduct via FAB
    ├── CreateProduct Screen
    │   └── Can navigate back to Dashboard
    └── Settings Screen
```

## Adaptive Layout Support

The app implements **Material 3 Adaptive Navigation** which automatically adjusts the navigation UI based on the device screen size:

### Navigation Types by Screen Size

- **Compact (< 600dp)** - Phones
  - Uses `NavigationBar` at the bottom
  - Traditional mobile navigation pattern
  
- **Medium (600-840dp)** - Small tablets, foldables
  - Uses `NavigationRail` on the left side
  - Vertical navigation with icons and labels
  - More efficient use of screen space
  
- **Expanded (> 840dp)** - Large tablets, desktops
  - Uses `NavigationDrawer` on the left side
  - Full navigation menu with icons and text
  - Optimal for large screen experiences

The adaptive behavior is powered by `currentWindowAdaptiveInfo()` which provides window size class information that drives the navigation type selection.

## File Structure

```
composeApp/src/commonMain/kotlin/com/alorma/caducity/
├── App.kt                                    # Main app entry point with adaptive navigation
├── navigation/
│   ├── Screen.kt                             # Sealed interface for navigation routes
│   └── NavGraph.kt                           # Navigation graph configuration
└── ui/screens/
    ├── dashboard/
    │   └── DashboardScreen.kt               # Product list with FAB to add products
    ├── create/
    │   └── CreateProductScreen.kt           # Form to create new products
    └── settings/
        └── SettingsScreen.kt                # App settings and preferences
```

## Features

### Dashboard Screen
- Displays list of products with expiration dates (currently sample data)
- Floating Action Button (FAB) to navigate to CreateProduct screen
- Accessible via adaptive navigation (bottom bar, rail, or drawer depending on screen size)

### CreateProduct Screen
- Form with fields for:
  - Product Name
  - Expiration Date
- Cancel and Save buttons
- Navigates back to Dashboard after save/cancel

### Settings Screen
- Toggle for notifications
- Toggle for dark mode
- App version information
- Accessible via adaptive navigation (bottom bar, rail, or drawer depending on screen size)

## Dependencies

The following dependencies have been added:

```kotlin
// In gradle/libs.versions.toml
navigation3 = "1.0.0-alpha06"
adaptive-navigation3 = "1.3.0-alpha02"

androidx-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
compose-material3-adaptive-navigation3 = { module = "org.jetbrains.compose.material3.adaptive:adaptive-navigation3", version.ref = "adaptive-navigation3" }

// Plugins
jetbrains-kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

## Usage

### Adaptive Navigation Setup

The adaptive navigation is initialized in `App.kt`:

```kotlin
val navController = rememberNavController()

// Determine navigation type based on window size
val adaptiveInfo = currentWindowAdaptiveInfo()
val customNavSuiteType = when (adaptiveInfo.windowSizeClass.windowWidthSizeClass) {
    WindowWidthSizeClass.COMPACT -> NavigationSuiteType.NavigationBar
    WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
    WindowWidthSizeClass.EXPANDED -> NavigationSuiteType.NavigationDrawer
    else -> NavigationSuiteType.NavigationBar
}

NavigationSuiteScaffold(
    layoutType = customNavSuiteType,
    navigationSuiteItems = { /* navigation items */ }
) {
    NavGraph(navController = navController)
}
```

### Navigation Actions

Navigation between screens is handled through callbacks and the NavController:

- **To CreateProduct**: `navController.navigate(Screen.CreateProduct)`
- **Back navigation**: `navController.popBackStack()`
- **To Settings**: `navController.navigate(Screen.Settings)`

All navigation actions preserve state using `saveState = true` and `restoreState = true` to maintain user context when switching between tabs.

## Benefits of Adaptive Navigation

1. **Responsive Design**: Automatically adapts to different screen sizes and form factors
2. **Platform-Appropriate UI**: Uses the best navigation pattern for each device type
3. **Consistent Experience**: Maintains the same navigation structure across all layouts
4. **Future-Proof**: Ready for foldables, tablets, and desktop platforms
5. **Material Design 3**: Follows the latest Material Design guidelines

## Future Enhancements

- Implement actual product data persistence
- Add product detail screen
- Implement edit product functionality
- Add deep linking support
- Add animations between screens
- Explore list-detail adaptive layouts for product viewing
