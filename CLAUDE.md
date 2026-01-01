# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Caducity is a Kotlin Multiplatform grocery expiration tracker application built with Compose Multiplatform. The app helps users track their groceries and avoid food waste by monitoring expiration dates.

**Current Focus**: Android (minSdk 35) - The project maintains multiplatform structure for potential future expansion to Desktop and Web, but development is currently Android-only.

## Technology Stack

- **Language**: Kotlin 2.3.0 (cutting-edge versions, may require regular updates)
- **UI Framework**: Jetpack Compose Multiplatform (v1.10.0-rc02) with Material 3 Expressive API
- **Target Platform**: Android (minSdk 35, compileSdk 36, targetSdk 36)
- **Architecture**: MVI/MVVM with Compose state management
- **Navigation**: Jetpack Navigation 3 (alpha06)
- **Dependency Injection**: Koin 4.1.1
- **Build System**: Gradle with Kotlin DSL
- **Database**: Room 2.8.4
- **Date/Time**: kotlinx-datetime 0.7.1
- **Background Work**: WorkManager 2.11.0 (for expiration notifications)

## Build Commands

### Development Build
```bash
# Clean build
./gradlew clean build

# Android builds
./gradlew assembleDebug              # Android debug build
./gradlew assembleRelease            # Android release build
```

### Testing
```bash
# Run all tests
./gradlew test

# Run all checks (lint + tests)
./gradlew check

# Android device tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest

# Unit tests only
./gradlew testDebugUnitTest

# Lint
./gradlew lint
```

### Installation
```bash
# Install Android app
./gradlew installDebug    # Debug variant
./gradlew installRelease  # Release variant
./gradlew uninstallAll    # Uninstall all variants
```

## Architecture

### Project Structure
```
composeApp/src/
├── commonMain/kotlin/com/alorma/caducity/
│   ├── data/              # Data layer
│   │   ├── datasource/    # Abstract data source interfaces
│   │   ├── entity/        # Data entities
│   │   ├── model/         # Data models
│   │   └── mapper/        # Entity-to-Model mappers
│   ├── domain/            # Business logic
│   │   ├── model/         # Domain models
│   │   └── usecase/       # Use cases
│   ├── ui/                # UI layer
│   │   ├── screen/        # Feature screens (dashboard, settings)
│   │   ├── theme/         # Theming (Material 3 with dynamic colors)
│   │   ├── icons/         # Custom icons
│   │   └── adaptive/      # Adaptive/responsive utilities
│   ├── notification/      # Notification abstractions
│   ├── di/                # Dependency injection modules
│   ├── time/              # Time/clock abstraction
│   ├── App.kt             # Main app entry point with navigation
│   └── TopLevelBackStack.kt/TopLevelRoute.kt  # Navigation setup
└── androidMain/kotlin/com/alorma/caducity/
    ├── MainActivity.kt
    ├── data/datasource/   # Android-specific implementations (Room)
    ├── notification/      # Android notification & WorkManager
    └── language/          # Android language manager

base/
├── main/                  # Core domain models (InstanceStatus, etc.)
└── ui/
    ├── components/        # Reusable UI components (StatusBadge, TopBars)
    ├── icons/             # Custom icon definitions
    └── theme/             # Theme system (colors, typography, language, preferences)
```

### Multiplatform Architecture

**Common Code (commonMain)**:
- Defines all shared UI, business logic, and data abstractions
- Uses `expect` declarations for platform-specific implementations
- All Compose UI is in commonMain for potential future platform support
- Contains platform abstractions (interfaces) for notifications, language management, etc.

**Platform-Specific Code**:
- **Android** (`androidMain`):
  - Room database implementation (ProductDataSource)
  - MainActivity and Android-specific UI setup
  - Notification system (WorkManager, NotificationChannelManager)
  - Language management (AndroidLanguageManager)
  - System integration (dynamic colors, system bars)

**Dependency Injection Pattern**:
- `appModule` (common): ViewModels, use cases, shared services
- `platformModule` (expect/actual): Platform-specific implementations (e.g., ProductDataSource, NotificationDebugHelper)
- `themeModule` (base): Theme preferences and language management
- Both modules are combined in `App.kt` via Koin

### Navigation System

Uses Jetpack Navigation 3 with a custom `TopLevelBackStack` implementation:
- Type-safe navigation with sealed `TopLevelRoute` classes
- Adaptive UI: NavigationBar (compact) vs NavigationRail (expanded)
- Window size class detection for responsive layouts
- ViewModel scoping via `rememberViewModelStoreNavEntryDecorator()`
- State preservation via `rememberSaveableStateHolderNavEntryDecorator()`

### Theming

Material 3 Expressive API with:
- Dynamic color support (platform-dependent via `expect/actual`)
- Dark mode toggle with system default option
- Theme preferences persisted via custom `ThemePreferences` class
- Uses compose-settings library for settings UI tiles

### Notification System

Android notification system for expiration alerts:
- **ExpirationWorkScheduler**: Schedules periodic background checks using WorkManager
- **NotificationChannelManager**: Creates and manages Android notification channels
- **ExpirationNotificationHelper**: Interface for platform-specific notification implementations
- **AndroidExpirationNotificationHelper**: Android implementation using NotificationCompat
- **NotificationDebugHelper**: Interface for testing notifications (follows preferred interface pattern)
- Background work runs daily to check for expiring products

### Base Module Organization

The `base/` module contains reusable components separated into focused sub-modules:

- **base/main**: Core domain models shared across features
  - `InstanceStatus`: Enum for product instance states (Fresh, ExpiringSoon, Expired)

- **base/ui/components**: Reusable UI components
  - `StatusBadge`: Visual status indicators for product instances
  - `TopBars`: Common app bar components
  - `ExpirationDefaults`: Shared expiration-related UI constants

- **base/ui/icons**: Custom icon definitions using Compose vector graphics
  - `AppIcons`: Centralized icon access
  - Individual icon files (Search, Add, Calendar, etc.)

- **base/ui/theme**: Comprehensive theme system
  - Color schemes (dynamic colors, contrast levels, expressive palettes)
  - Typography with custom fonts
  - Theme preferences and persistence
  - Language management system
  - System bars appearance

## Key Patterns and Conventions

### Data Flow
1. **ViewModel** collects data from **UseCase**
2. **UseCase** calls **DataSource** (via platform module)
3. **DataSource** returns domain **Model** (mapped from **Entity**)
4. **ViewModel** maps to **UiModel** for screens
5. **Screen** observes `StateFlow<UiState>` from ViewModel

### Opt-In Requirements
The following experimental APIs are enabled project-wide:
- `kotlin.time.ExperimentalTime`
- `androidx.compose.material3.ExperimentalMaterial3Api`
- `androidx.compose.material3.ExperimentalMaterial3ExpressiveApi`

### Date/Time Handling
- Use `kotlinx-datetime` for all date/time operations
- Store timestamps as `Long` (Unix epoch milliseconds)
- Convert to `LocalDate`/`LocalDateTime` for display
- Use `AppClock` abstraction (injectable) for testable time operations

### Dependency Injection
- Register ViewModels with `viewModelOf(::ClassName)`
- Register singletons with `singleOf(::ClassName)` or `single { }`
- Use `bind` to map implementations to interfaces in platform modules
- Always inject dependencies via constructor

## Current Implementation Status

**Completed**:
- Basic app scaffold with navigation
- Dashboard screen with ViewModel
- Settings screen with theme selection
- Adaptive UI for different screen sizes
- Platform-specific data source abstraction
- DI setup with Koin
- Room database integration

**In Progress (see DEVELOPMENT_PLAN.md)**:
- Product and ProductInstance entities
- Full CRUD operations for products
- Dashboard statistics and data display
- Expiration notifications system

## Development Notes

### Adding New Screens
1. Create screen composable in `ui/screen/<feature>/`
2. Add corresponding ViewModel in same package
3. Define route in `TopLevelRoute.kt`
4. Register in `App.kt` `entryProvider` block
5. Add navigation item to `CompactContent`/`ExpandedContent`

### Adding Platform-Specific Code

**PREFERRED: Interface + Implementation Pattern**
For platform-specific functionality, prefer using interfaces over expect/actual:

1. Define an interface in `commonMain` (e.g., `NotificationDebugHelper`)
2. Create platform-specific implementation in `androidMain` (e.g., `AndroidNotificationDebugHelper`)
3. Bind implementation to interface in `platformModule`:
   ```kotlin
   singleOf(::AndroidNotificationDebugHelper) bind NotificationDebugHelper::class
   ```

**Benefits**:
- More flexible and testable than expect/actual
- Easier to mock for testing
- No Beta warnings for expect/classes
- Follows dependency inversion principle
- Consistent with other platform abstractions (e.g., `ExpirationNotificationHelper`)

**Example**:
```kotlin
// commonMain/notification/NotificationDebugHelper.kt
interface NotificationDebugHelper {
  fun triggerImmediateCheck()
}

// androidMain/notification/AndroidNotificationDebugHelper.kt
class AndroidNotificationDebugHelper(
  private val workScheduler: ExpirationWorkScheduler
) : NotificationDebugHelper {
  override fun triggerImmediateCheck() {
    workScheduler.triggerImmediateCheck()
  }
}

// androidMain/di/PlatformModule.android.kt
singleOf(::AndroidNotificationDebugHelper) bind NotificationDebugHelper::class
```

**When to use expect/actual**:
- Only use expect/for platform values or simple functions where interface pattern doesn't fit
- Common pattern: expect val/fun in DI modules (`expect val platformModule`)
- Keep expect/minimal and prefer interface pattern for classes

### Version Catalog (libs.versions.toml)
All dependencies are centralized in `gradle/libs.versions.toml`:
- Access via `libs.plugins.*` or `libs.*` in build files
- Update versions in `[versions]` section only
- Use `alias()` to reference in build.gradle.kts

### Java Version
- Source/target: Java 11
- Ensure JDK 11+ is configured in IDE

## Localization and Language Support

The app uses Compose Multiplatform resources with Android 13+ per-app language preferences. Currently supported languages: English (en), Spanish (es), and Catalan (ca).

### Adding a New Language

To add a new language (e.g., French):

1. **Add Language Enum Entry**
   - Edit `base/ui/theme/src/commonMain/kotlin/com/alorma/caducity/base/ui/theme/language/Language.kt`
   - Add new enum entry:
   ```kotlin
   enum class Language(val code: String) {
     ENGLISH("en"),
     SPANISH("es"),
     CATALAN("ca"),
     FRENCH("fr"),  // Add new language
   }
   ```

2. **Create Resource Directory**
   - Create `composeApp/src/commonMain/composeResources/values-{code}/` directory
   - Example: `composeApp/src/commonMain/composeResources/values-fr/`

3. **Add String Resources**
   - Copy `strings.xml` from `values/` to your new `values-{code}/` directory
   - Translate all string values to the target language
   - Keep string keys unchanged

4. **Update LanguageSettingsScreen**
   - Edit `composeApp/src/commonMain/kotlin/com/alorma/caducity/ui/screen/settings/language/LanguageSettingsScreen.kt`
   - Add string resource:
   ```kotlin
   import caducity.composeapp.generated.resources.language_french
   val languageFrench = stringResource(Res.string.language_french)
   ```
   - Add language to when expression:
   ```kotlin
   val title = when (language) {
     Language.ENGLISH -> languageEnglish
     Language.SPANISH -> languageSpanish
     Language.CATALAN -> languageCatalan
     Language.FRENCH -> languageFrench
   }
   ```

5. **Add Language Name String**
   - Edit all `composeApp/src/commonMain/composeResources/values*/strings.xml` files
   - Add the language name in each language:
   ```xml
   <!-- values/strings.xml (English) -->
   <string name="language_french">Français</string>

   <!-- values-es/strings.xml (Spanish) -->
   <string name="language_french">Francés</string>

   <!-- values-ca/strings.xml (Catalan) -->
   <string name="language_french">Francès</string>

   <!-- values-fr/strings.xml (French) -->
   <string name="language_french">Français</string>
   ```

6. **Update Android Locales Config**
   - Edit `composeApp/src/androidMain/res/xml/locales_config.xml`
   - Add new locale:
   ```xml
   <locale android:name="fr"/>
   ```

7. **Generate Resource Accessors**
   - Run: `./gradlew :composeApp:generateResourceAccessorsForCommonMain`
   - This generates the `Res.string.language_french` accessor

8. **Test**
   - Build and install: `./gradlew installDebug`
   - Navigate to Settings → Language
   - Verify new language appears and switches correctly

### Language Architecture

- **LanguageManager**: Abstract base class handling persistence and platform-specific application
  - Located: `base/ui/theme/src/commonMain/kotlin/com/alorma/caducity/base/ui/theme/LanguageManager.kt`
  - Persists selection using `Settings` (key: "app_language")
  - Reads from platform API (`AppCompatDelegate.getApplicationLocales()` on Android)

- **AndroidLanguageManager**: Android implementation
  - Located: `composeApp/src/androidMain/kotlin/com/alorma/caducity/language/AndroidLanguageManager.kt`
  - Uses `LocaleManager.applicationLocales` on Android 13+ for seamless language changes without app restart
  - Falls back to `AppCompatDelegate.setApplicationLocales()` on older Android versions
  - Requires Context to be injected via Koin

- **System Language Detection**: On first launch, detects system language and defaults to English if unsupported

- **No App Restart**: Language changes apply immediately without restarting the app on Android 13+ (API 33+)

## Important References

- Detailed implementation plan: `DEVELOPMENT_PLAN.md`
- All build configurations use Kotlin DSL (`.gradle.kts`)
- Version catalog: `gradle/libs.versions.toml` (centralized dependency management)
