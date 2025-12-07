# Caducity Navigation Structure

This document describes the navigation implementation in the Caducity app.

## Overview

The app uses **Jetpack Compose Navigation 3 Multiplatform** with type-safe navigation using Kotlin Serialization.

## Navigation Architecture

### Screen Definitions

The app has three main screens defined in `Screen.kt`:

1. **Dashboard** - The main landing screen showing product list
2. **CreateProduct** - Form screen for adding new products
3. **Settings** - App settings and preferences

### Navigation Structure

```
App (Root)
├── Bottom Navigation Bar
│   ├── Dashboard (Home Icon)
│   └── Settings (Settings Icon)
└── NavGraph
    ├── Dashboard Screen (Start Destination)
    │   └── Can navigate to CreateProduct via FAB
    ├── CreateProduct Screen
    │   └── Can navigate back to Dashboard
    └── Settings Screen
```

## File Structure

```
composeApp/src/commonMain/kotlin/com/alorma/caducity/
├── App.kt                                    # Main app entry point with bottom nav
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
- Accessible via bottom navigation bar

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
- Accessible via bottom navigation bar

## Dependencies

The following dependencies have been added:

```kotlin
// In gradle/libs.versions.toml
navigation-compose = "2.8.0-alpha10"
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }

// Plugins
jetbrains-kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

## Usage

The navigation is initialized in `App.kt`:

```kotlin
val navController = rememberNavController()
NavGraph(navController = navController)
```

Navigation between screens is handled through callbacks and the NavController:

- **To CreateProduct**: `navController.navigate(Screen.CreateProduct)`
- **Back navigation**: `navController.popBackStack()`
- **To Settings**: `navController.navigate(Screen.Settings)`

## Future Enhancements

- Implement actual product data persistence
- Add product detail screen
- Implement edit product functionality
- Add deep linking support
- Add animations between screens
