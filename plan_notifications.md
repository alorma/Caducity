# Notification System Implementation Plan

## Overview
Implement a notification system to alert users when products are approaching their expiration dates. The system will use WorkManager for daily background checks and notify users 3 days before expiration (configurable in future).

## User Requirements
- Notify 3 days in advance (will be configurable via datasource in future)
- Fixed daily check time (9 AM for now, configurable later)
- Use WorkManager for battery-efficient background tasks
- Tapping notification opens app to filtered view of expiring products

## Current State Analysis

### Existing Structure
- **Data Models**:
  - `ProductInstanceEntity` stores expiration date as `Long` (timestamp)
  - `ProductInstance` model uses `kotlin.time.Instant` for expiration
  - Products are accessed via `ProductDataSource` interface with Room implementation

- **Architecture**:
  - Koin DI in place (`appModule`, `platformModule`)
  - Android-specific code goes in `androidMain`
  - UseCase pattern established (e.g., `ObtainDashboardProductsUseCase`)

- **Dependencies Available**:
  - kotlinx-datetime for date/time operations
  - Room for database
  - Koin for DI

### Missing Infrastructure
- No WorkManager dependency
- No notification channels setup
- No notification permissions in manifest
- No configuration datasource for notification settings

## Implementation Plan

### 1. Add Dependencies
**File**: `gradle/libs.versions.toml`
- Add WorkManager version (androidx-work = "2.10.0")
- Add WorkManager runtime-ktx library

**File**: `composeApp/build.gradle.kts`
- Add WorkManager dependency to androidMain dependencies

### 2. Update AndroidManifest
**File**: `composeApp/src/androidMain/AndroidManifest.xml`
- Add `POST_NOTIFICATIONS` permission (required for Android 13+)
- Add `SCHEDULE_EXACT_ALARM` permission (optional, for future precise scheduling)

**IMPORTANT**: `POST_NOTIFICATIONS` is a runtime permission on Android 13+ (API 33+). The app must:
1. Declare it in the manifest (this step)
2. Request it at runtime from the user (future enhancement - see Step 12)
3. For now, notifications will only work if user grants permission manually in system settings

### 3. Create Notification Configuration DataSource
**New Files**:
- `composeApp/src/commonMain/kotlin/com/alorma/caducity/data/datasource/NotificationConfigDataSource.kt` (interface)
  - Methods: `getExpirationThresholdDays()`, `getNotificationTime()`, `isNotificationsEnabled()`

- `composeApp/src/androidMain/kotlin/com/alorma/caducity/data/datasource/FakeNotificationConfigDataSource.kt` (implementation)
  - Hardcode 3 days threshold for now
  - Hardcode 9 AM notification time

**DI Integration**:
- Register in `platformModule` (androidMain/di/PlatformModule.android.kt)

### 4. Create Notification Channel Manager
**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/notification/NotificationChannelManager.kt`
- Create notification channel for expiration alerts
- Channel ID: "product_expiration"
- Channel name: "Product Expiration Alerts"
- Importance: HIGH (to ensure visibility)
- Method: `createNotificationChannel(context: Context)`

### 5. Create Notification Helper
**New File**: `composeApp/src/commonMain/kotlin/com/alorma/caducity/notification/ExpirationNotificationHelper.kt` (interface)
- Method: `showExpirationNotification(expiringProducts: List<ProductWithInstances>)`
- Defines constant for intent extra: `EXTRA_SHOW_EXPIRING_ONLY`

**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/notification/AndroidExpirationNotificationHelper.kt` (implementation)
- Implements `ExpirationNotificationHelper` interface
- Build and display notifications
- Handle notification content (title, text, icon)
- Create PendingIntent to open app with filtered view

**DI Integration**:
- Register in `platformModule` with `androidContext()` dependency

### 6. Create UseCase for Expiring Products
**New File**: `composeApp/src/commonMain/kotlin/com/alorma/caducity/domain/usecase/GetExpiringProductsUseCase.kt`
- Query products from `ProductDataSource`
- Filter by expiration date threshold (from config datasource)
- Use `AppClock` for current time comparison
- Return `Flow<List<ProductWithInstances>>` or suspend function

**DI Integration**:
- Register in `appModule`

### 7. Create WorkManager Worker
**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/worker/ExpirationCheckWorker.kt`
- Extend `CoroutineWorker`
- Inject dependencies via WorkManager factory
- Call `GetExpiringProductsUseCase`
- If products are expiring, call `ExpirationNotificationHelper`
- Return `Result.success()` or `Result.retry()`

### 8. Create WorkManager Factory (for DI)
**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/worker/ExpirationWorkerFactory.kt`
- Implement `WorkerFactory`
- Create workers with Koin dependencies
- Inject `GetExpiringProductsUseCase`, `NotificationConfigDataSource`, etc.

### 9. Create Work Scheduler
**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/worker/ExpirationWorkScheduler.kt`
- Schedule daily periodic work (or one-time for testing)
- Use `PeriodicWorkRequestBuilder` with 1-day repeat interval
- Set constraints (require battery not low, etc.)
- Use `ExistingPeriodicWorkPolicy.KEEP` to avoid duplicate schedules
- Method: `scheduleExpirationCheck(context: Context)`

**DI Integration**:
- Register as singleton in `platformModule`

### 10. Create Application Class
**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/CaducityApplication.kt`
- Extend `Application`
- Initialize Koin
- Set custom `WorkerFactory`
- Create notification channels
- Schedule WorkManager task on first launch

**Update AndroidManifest**:
- Add `android:name=".CaducityApplication"` to `<application>` tag

### 11. Handle Notification Tap (Navigation)
**Update File**: `composeApp/src/commonMain/kotlin/com/alorma/caducity/TopLevelRoute.kt`
- Add parameter to Dashboard route to support filtering (e.g., `showExpiringOnly: Boolean`)

**Update File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/MainActivity.kt`
- Handle intent extras from notification
- Pass filter parameter to navigation

**Update File**: `composeApp/src/commonMain/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardScreen.kt`
- Add filtering logic to show only expiring products when flag is true

### 12. Request Notification Permission at Runtime (Future Enhancement)
**Note**: This step is NOT included in the current implementation but should be added before production.

**New File**: `composeApp/src/androidMain/kotlin/com/alorma/caducity/permission/NotificationPermissionHandler.kt`
- Check if `POST_NOTIFICATIONS` permission is granted
- Request permission using `ActivityResultContracts.RequestPermission`
- Handle permission result and update UI accordingly

**Integration**:
- Call permission request on app first launch or from settings screen
- Show rationale dialog explaining why notifications are needed
- NOT use Accompanist Permissions

### 13. Testing Considerations
- Add logging to Worker to verify it runs
- Create manual trigger for testing (e.g., debug button to schedule immediate work)
- Test notification display on Android 13+ with runtime permission
- Verify deep linking works when tapping notification
- **For now**: Manually grant notification permission in system settings (Settings > Apps > Caducity > Notifications)

## File Structure Summary

### New Files to Create
```
composeApp/src/
├── commonMain/kotlin/com/alorma/caducity/
│   ├── data/datasource/
│   │   └── NotificationConfigDataSource.kt (interface)
│   └── domain/usecase/
│       └── GetExpiringProductsUseCase.kt
├── androidMain/kotlin/com/alorma/caducity/
│   ├── CaducityApplication.kt
│   ├── data/datasource/
│   │   └── FakeNotificationConfigDataSource.kt
│   ├── notification/
│   │   ├── NotificationChannelManager.kt
│   │   └── ExpirationNotificationHelper.kt
│   └── worker/
│       ├── ExpirationCheckWorker.kt
│       ├── ExpirationWorkerFactory.kt
│       └── ExpirationWorkScheduler.kt
```

### Files to Modify
```
- gradle/libs.versions.toml (add WorkManager)
- composeApp/build.gradle.kts (add WorkManager dependency)
- composeApp/src/androidMain/AndroidManifest.xml (permissions, Application class)
- composeApp/src/commonMain/.../di/AppModule.kt (register UseCase)
- composeApp/src/androidMain/.../di/PlatformModule.android.kt (register datasource, scheduler)
- composeApp/src/androidMain/.../MainActivity.kt (handle intent extras)
- composeApp/src/commonMain/.../TopLevelRoute.kt (add filter parameter)
- composeApp/src/commonMain/.../ui/screen/dashboard/DashboardScreen.kt (add filtering)
```

## Implementation Notes

### Notification Threshold Logic
- Current: Hardcoded 3 days in `FakeNotificationConfigDataSource`
- Future: Replace with Room entity storing user preferences
- Calculate expiring products: `expirationDate <= (currentDate + thresholdDays)`

### WorkManager Scheduling
- Use `PeriodicWorkRequest` with 24-hour interval
- Set initial delay to schedule first check at 9 AM next day
- Use `setInitialDelay()` to calculate time until next 9 AM

### Permission Handling
- POST_NOTIFICATIONS requires runtime permission on Android 13+
- Request permission in app (future enhancement)
- For now, notifications may not show if user denies permission

### Deep Link to Filtered View
- Pass intent extra: `SHOW_EXPIRING_ONLY = true`
- Dashboard checks this flag and filters products accordingly
- Use existing `ObtainDashboardProductsUseCase` and filter in ViewModel or Screen

### Koin + WorkManager Integration
- WorkManager requires custom `WorkerFactory` for DI
- Factory receives Koin instance and creates workers with dependencies
- Set factory in Application before WorkManager initialization

## Future Enhancements (Not in this implementation)
- User-configurable threshold days (settings UI)
- User-configurable notification time (time picker)
- Runtime permission request flow for notifications
- Notification preferences screen (enable/disable)
- Multiple notification thresholds (7 days, 3 days, 1 day)
- Rich notifications with action buttons (e.g., "View", "Snooze")
