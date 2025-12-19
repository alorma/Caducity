# Caducity - Quick Start Guide

## What is Caducity?

Caducity is a Kotlin Multiplatform app that helps you track grocery expiration dates and reduce food waste. Track your groceries, get expiration alerts, and never waste food again!

---

## Current Features

### ✅ What Works Now
- **Dashboard**: View all your products with expiration status (Fresh, Expiring Soon, Expired)
- **Product Detail**: See all instances of a product and their expiration dates
- **Settings**: Customize theme (Light/Dark/System) and dynamic colors
- **Adaptive UI**: Automatically adjusts layout for phones, tablets, and desktops
- **Multiplatform**: Runs on Android, Desktop (JVM), Web browsers, and WebAssembly

### ⏳ What's Coming Soon
- Create and edit products
- Add/remove product instances
- Notifications for expiring items
- Search and filter products
- Statistics and insights

---

## How to Run

### Android
```bash
./gradlew installDebug
```

### Desktop
```bash
./gradlew :composeApp:run
```

### Web Browser
```bash
./gradlew jsBrowserDevelopmentRun
# Open http://localhost:8080 in your browser
```

### WebAssembly
```bash
./gradlew wasmJsBrowserDevelopmentRun
```

---

## Project Structure

```
composeApp/src/
├── commonMain/         # Shared code across all platforms
│   ├── ui/            # Compose UI screens
│   ├── domain/        # Business logic
│   ├── data/          # Data layer
│   └── di/            # Dependency injection
├── androidMain/       # Android-specific code
├── desktopMain/       # Desktop-specific code
└── webMain/           # Web-specific code
```

---

## Next Steps for Development

See **DEVELOPMENT_PLAN.md** for the complete roadmap!

### Immediate Next Steps (Phase 1)
1. Complete create product functionality
2. Add edit product capability
3. Implement delete product feature
4. Add product search/filter

### Short-term Goals (Phase 2)
1. Add instance management (add/edit/remove instances)
2. Implement instance actions (consume, freeze)
3. Auto-categorize status

### Medium-term Goals (Phase 3-4)
1. Persistent storage (Room for Android, SQLDelight/Storage for others)
2. Notifications and reminders

---

## Technology Stack

- **Language**: Kotlin 2.2.21
- **UI**: Compose Multiplatform with Material 3
- **Navigation**: Jetpack Navigation 3
- **DI**: Koin 4.1.1
- **Database**: Room (planned for Android)
- **Date/Time**: kotlinx-datetime

---

## Contributing

This is the beginning of an exciting project! Check the **DEVELOPMENT_PLAN.md** to see what needs to be built.

---

## Architecture Overview

**Data Flow:**
1. UI (Composables) → ViewModel
2. ViewModel → Use Case
3. Use Case → Data Source
4. Data Source → Repository/Database

**Key Patterns:**
- MVI/MVVM for state management
- Clean Architecture with layers
- Multiplatform shared business logic
- Platform-specific implementations via `expect`/`actual`

---

## Useful Commands

### Build
```bash
./gradlew clean build              # Full build
./gradlew assembleDebug            # Android debug
./gradlew assembleRelease          # Android release
```

### Testing
```bash
./gradlew allTests                 # All tests
./gradlew testDebugUnitTest        # Android unit tests
./gradlew jsTest                   # JavaScript tests
```

### Distribution
```bash
./gradlew :composeApp:packageDistributionForCurrentOS  # Native package
./gradlew jsBrowserDistribution                        # Web distribution
```

---

## Design Decisions

### Why Multiplatform?
- Write UI once, run everywhere
- Share business logic across platforms
- Native performance on each platform

### Why Material 3 Expressive?
- Modern, beautiful UI
- Adaptive components
- Built-in dynamic color support

### Why Compose Navigation 3?
- Type-safe navigation
- Multiplatform support
- Deep linking ready

---

## Current Limitations

- **Data is mock/in-memory** (no persistence yet)
- **Create product** dialog exists but doesn't function
- **No notifications** implemented yet
- **No search/filter** on dashboard
- **Desktop & Web** use fake data source (Room is Android-only currently)

These limitations are being addressed in the development plan phases!

---

## Getting Help

- Check **CLAUDE.md** for detailed technical guidance
- Review **DEVELOPMENT_PLAN.md** for feature roadmap
- Explore the codebase - it's well-structured and documented
