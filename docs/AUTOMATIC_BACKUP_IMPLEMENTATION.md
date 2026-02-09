# Automatic Backup Feature - Implementation Plan

## Context

The Caducity Android app currently supports manual backup/restore through a settings screen using Android's file picker. Users can export their grocery data (categories, products, items) to JSON files and import them later. However, there's no automatic backup mechanism, which means users risk losing data if they forget to backup manually or if the app crashes/device is lost.

This plan implements an automatic backup system with two complementary mechanisms:

1. **Action-triggered backups**: Automatically create a backup after any data change (create/edit/delete category, product, or item). This backup overwrites a single file (`auto_backup.json`) to provide the most recent data snapshot without accumulating storage.

2. **Scheduled backups**: Time-based backups using Android's WorkManager to create timestamped backup files at user-configurable intervals (hourly/daily/weekly). These backups are retained (last 10 files) to provide historical recovery points.

The user wants both mechanisms because they serve different purposes: action backups ensure no recent work is lost, while scheduled backups provide time-based recovery options. All automatic backups are stored locally in the app's private storage directory, with cloud storage planned for future enhancement.

## Architecture Overview

The implementation follows the app's Clean Architecture pattern with clear layer separation:

**Domain Layer**: `AutoBackupPreferences` interface, `AutoBackupUseCase` for backup orchestration
**Data Layer**: `AutoBackupPreferencesImpl` for settings persistence, `AutoBackupFileManager` for file I/O, `AutoBackupTrigger` for database change detection
**Feature Layer**: `AutoBackupWorker` and `AutoBackupWorkScheduler` for WorkManager integration
**UI Layer**: `AutoBackupSettingsScreen` and `AutoBackupSettingsViewModel` for user configuration

The system leverages existing infrastructure: Room's `InvalidationTracker` for database changes, WorkManager for scheduling (already used for expiration notifications), multiplatform-settings for preferences, and the existing `ExportBackupUseCase` for data serialization.

## Implementation Steps

### 1. Create Domain Models and Preferences Interface

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/autobackup/AutoBackupPreferences.kt` (new)

Create preferences interface following the `ThemePreferences` pattern at `app/src/main/kotlin/com/alorma/caducity/ui/theme/ThemePreferences.kt`:

```kotlin
package com.alorma.caducity.domain.autobackup

import androidx.compose.runtime.MutableState
import kotlinx.datetime.Instant

interface AutoBackupPreferences {
  val isEnabled: MutableState<Boolean>
  val scheduleFrequency: MutableState<BackupFrequency>

  fun loadIsEnabled(): Boolean
  fun loadScheduleFrequency(): BackupFrequency
  fun loadLastBackupTimestamp(): Instant?

  fun setEnabled(enabled: Boolean)
  fun setScheduleFrequency(frequency: BackupFrequency)
  fun setLastBackupTimestamp(timestamp: Instant)
}

enum class BackupFrequency(val hours: Long) {
  HOURLY(1),
  DAILY(24),
  WEEKLY(168)  // 24 * 7
}
```

**Key Points**:
- Use `MutableState` for reactive UI updates (same as `ThemePreferences`)
- `BackupFrequency` enum stores hour values for WorkManager scheduling
- Store last backup timestamp for UI display

### 2. Implement Preferences with Persistent Storage

**File**: `app/src/main/kotlin/com/alorma/caducity/data/autobackup/AutoBackupPreferencesImpl.kt` (new)

Follow the implementation pattern from `app/src/main/kotlin/com/alorma/caducity/ui/theme/ThemePreferencesImpl.kt`:

```kotlin
package com.alorma.caducity.data.autobackup

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alorma.caducity.domain.autobackup.AutoBackupPreferences
import com.alorma.caducity.domain.autobackup.BackupFrequency
import com.russhwolf.settings.Settings
import kotlinx.datetime.Instant

class AutoBackupPreferencesImpl(
  private val settings: Settings,
) : AutoBackupPreferences {

  companion object {
    private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    private const val KEY_AUTO_BACKUP_FREQUENCY = "auto_backup_frequency"
    private const val KEY_AUTO_BACKUP_LAST_TIMESTAMP = "auto_backup_last_timestamp"
    private const val DEFAULT_ENABLED = false
    private val DEFAULT_FREQUENCY = BackupFrequency.DAILY
  }

  override val isEnabled: MutableState<Boolean> = mutableStateOf(loadIsEnabled())
  override val scheduleFrequency: MutableState<BackupFrequency> = mutableStateOf(loadScheduleFrequency())

  override fun loadIsEnabled(): Boolean {
    return settings.getBoolean(KEY_AUTO_BACKUP_ENABLED, DEFAULT_ENABLED)
  }

  override fun loadScheduleFrequency(): BackupFrequency {
    val name = settings.getString(KEY_AUTO_BACKUP_FREQUENCY, DEFAULT_FREQUENCY.name)
    return BackupFrequency.valueOf(name)
  }

  override fun loadLastBackupTimestamp(): Instant? {
    val epochMillis = settings.getLong(KEY_AUTO_BACKUP_LAST_TIMESTAMP, -1L)
    return if (epochMillis >= 0) Instant.fromEpochMilliseconds(epochMillis) else null
  }

  override fun setEnabled(enabled: Boolean) {
    isEnabled.value = enabled
    settings.putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled)
  }

  override fun setScheduleFrequency(frequency: BackupFrequency) {
    scheduleFrequency.value = frequency
    settings.putString(KEY_AUTO_BACKUP_FREQUENCY, frequency.name)
  }

  override fun setLastBackupTimestamp(timestamp: Instant) {
    settings.putLong(KEY_AUTO_BACKUP_LAST_TIMESTAMP, timestamp.toEpochMilliseconds())
  }
}
```

**Key Points**:
- Uses existing `Settings` instance from multiplatform-settings library (already available in Koin)
- Default: disabled, daily frequency
- Stores timestamp as epoch milliseconds for persistence

### 3. Create File Manager for Local Storage

**File**: `app/src/main/kotlin/com/alorma/caducity/data/autobackup/AutoBackupFileManager.kt` (new)

Reference `app/src/main/kotlin/com/alorma/caducity/feature/backup/AndroidBackupFileHandler.kt` for JSON serialization pattern:

```kotlin
package com.alorma.caducity.data.autobackup

import android.content.Context
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class AutoBackupFileManager(
  private val context: Context,
  private val appClock: AppClock,
  private val dateFilenameFormat: DateTimeFormat<LocalDateTime>,
) {

  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
  }

  companion object {
    private const val ACTION_BACKUP_FILENAME = "auto_backup.json"
    private const val SCHEDULED_BACKUP_DIR = "backups"
    private const val SCHEDULED_BACKUP_PREFIX = "caducity_scheduled_"
    private const val MAX_SCHEDULED_BACKUPS = 10
  }

  suspend fun writeActionBackup(data: BackupData): Result<Unit> = runCatching {
    val file = File(context.filesDir, ACTION_BACKUP_FILENAME)
    val jsonString = json.encodeToString(data)
    file.writeText(jsonString)
  }

  suspend fun writeScheduledBackup(data: BackupData): Result<File> = runCatching {
    val backupDir = File(context.filesDir, SCHEDULED_BACKUP_DIR)
    backupDir.mkdirs()

    val timestamp = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedDate = dateFilenameFormat.format(timestamp)
    val filename = "$SCHEDULED_BACKUP_PREFIX$formattedDate.json"
    val file = File(backupDir, filename)

    val jsonString = json.encodeToString(data)
    file.writeText(jsonString)
    file
  }

  suspend fun cleanupOldScheduledBackups(): Result<Int> = runCatching {
    val backupDir = File(context.filesDir, SCHEDULED_BACKUP_DIR)
    if (!backupDir.exists()) return@runCatching 0

    val backups = backupDir.listFiles { file ->
      file.name.startsWith(SCHEDULED_BACKUP_PREFIX) && file.extension == "json"
    }?.sortedByDescending { it.lastModified() } ?: emptyList()

    val toDelete = backups.drop(MAX_SCHEDULED_BACKUPS)
    toDelete.forEach { it.delete() }
    toDelete.size
  }

  fun getScheduledBackups(): List<File> {
    val backupDir = File(context.filesDir, SCHEDULED_BACKUP_DIR)
    return backupDir.listFiles { file ->
      file.name.startsWith(SCHEDULED_BACKUP_PREFIX) && file.extension == "json"
    }?.sortedByDescending { it.lastModified() } ?: emptyList()
  }

  fun getActionBackupFile(): File? {
    val file = File(context.filesDir, ACTION_BACKUP_FILENAME)
    return if (file.exists()) file else null
  }
}
```

**Key Points**:
- Action backup: `context.filesDir/auto_backup.json` (always overwritten)
- Scheduled backup: `context.filesDir/backups/caducity_scheduled_YYYYMMDD_HHmmss.json` (timestamped)
- Cleanup: Keep only last 10 scheduled backups based on file modification time
- Reuses `BackupData` model from existing backup system

### 4. Create Use Case for Backup Orchestration

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/autobackup/AutoBackupUseCase.kt` (new)

Reference `app/src/main/kotlin/com/alorma/caducity/domain/usecase/backup/ExportBackupUseCase.kt` for backup data export:

```kotlin
package com.alorma.caducity.domain.usecase.autobackup

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.autobackup.AutoBackupFileManager
import com.alorma.caducity.domain.autobackup.AutoBackupPreferences
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase

class AutoBackupUseCase(
  private val exportBackupUseCase: ExportBackupUseCase,
  private val autoBackupFileManager: AutoBackupFileManager,
  private val autoBackupPreferences: AutoBackupPreferences,
  private val appClock: AppClock,
) {

  suspend fun triggerActionBackup(): Result<Unit> {
    if (!autoBackupPreferences.loadIsEnabled()) {
      return Result.success(Unit)
    }

    val backupData = exportBackupUseCase.export().getOrElse {
      return Result.failure(it)
    }

    return autoBackupFileManager.writeActionBackup(backupData)
  }

  suspend fun triggerScheduledBackup(): Result<Unit> {
    if (!autoBackupPreferences.loadIsEnabled()) {
      return Result.success(Unit)
    }

    val backupData = exportBackupUseCase.export().getOrElse {
      return Result.failure(it)
    }

    autoBackupFileManager.writeScheduledBackup(backupData).getOrElse {
      return Result.failure(it)
    }

    autoBackupFileManager.cleanupOldScheduledBackups()
    autoBackupPreferences.setLastBackupTimestamp(appClock.now())

    return Result.success(Unit)
  }
}
```

**Key Points**:
- Reuses existing `ExportBackupUseCase` for data serialization (already exports all categories/products/items)
- Action backups: Check if enabled, export data, write to single file
- Scheduled backups: Export data, write timestamped file, cleanup old files, update timestamp
- Both types fail gracefully if auto backup is disabled

### 5. Implement Database Change Trigger

**File**: `app/src/main/kotlin/com/alorma/caducity/data/autobackup/AutoBackupTrigger.kt` (new)

This is the most critical component - it detects database changes and triggers action backups:

```kotlin
package com.alorma.caducity.data.autobackup

import android.util.Log
import androidx.room.InvalidationTracker
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.domain.usecase.autobackup.AutoBackupUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoBackupTrigger(
  private val database: AppDatabase,
  private val autoBackupUseCase: AutoBackupUseCase,
  private val coroutineScope: CoroutineScope,
) {

  private var debounceJob: Job? = null
  private val DEBOUNCE_DELAY_MS = 1000L

  companion object {
    private const val TAG = "AutoBackupTrigger"
  }

  private val observer = object : InvalidationTracker.Observer("categories", "products", "items") {
    override fun onInvalidated(tables: Set<String>) {
      Log.d(TAG, "Database tables invalidated: $tables")

      debounceJob?.cancel()
      debounceJob = coroutineScope.launch {
        delay(DEBOUNCE_DELAY_MS)

        Log.d(TAG, "Triggering action backup after debounce")
        autoBackupUseCase.triggerActionBackup().onFailure { error ->
          Log.e(TAG, "Action backup failed", error)
        }
      }
    }
  }

  fun start() {
    Log.d(TAG, "Starting auto backup trigger")
    database.invalidationTracker.addObserver(observer)
  }

  fun stop() {
    Log.d(TAG, "Stopping auto backup trigger")
    database.invalidationTracker.removeObserver(observer)
    debounceJob?.cancel()
  }
}
```

**Key Points**:
- Uses Room's `InvalidationTracker` to detect changes to categories, products, or items tables
- Debounces with 1 second delay to batch rapid changes (e.g., creating category with 10 items)
- Runs in application-scoped coroutine (survives screen navigation)
- Logs operations for debugging, fails silently on errors (user doesn't need notification)
- Reference: See how `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/AppDatabase.kt` is structured

### 6. Create WorkManager Worker for Scheduled Backups

**File**: `app/src/main/kotlin/com/alorma/caducity/feature/autobackup/worker/AutoBackupWorker.kt` (new)

Follow the pattern from `app/src/main/kotlin/com/alorma/caducity/feature/notification/worker/ExpirationCheckWorker.kt`:

```kotlin
package com.alorma.caducity.feature.autobackup.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alorma.caducity.domain.usecase.autobackup.AutoBackupUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AutoBackupWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val autoBackupUseCase: AutoBackupUseCase by inject()

  companion object {
    private const val TAG = "AutoBackupWorker"
    const val WORK_NAME = "auto_backup_work"
  }

  override suspend fun doWork(): Result {
    return try {
      Log.d(TAG, "Starting scheduled backup")

      autoBackupUseCase.triggerScheduledBackup().fold(
        onSuccess = {
          Log.d(TAG, "Scheduled backup completed successfully")
          Result.success()
        },
        onFailure = { error ->
          Log.e(TAG, "Scheduled backup failed", error)
          Result.retry()
        }
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error during scheduled backup", e)
      Result.retry()
    }
  }
}
```

**Key Points**:
- Extends `CoroutineWorker` for suspend function support
- Uses Koin for dependency injection (same as `ExpirationCheckWorker`)
- Returns `Result.retry()` on failure (WorkManager handles exponential backoff)
- Logs for debugging

### 7. Create WorkManager Scheduler Interface and Implementation

**File**: `app/src/main/kotlin/com/alorma/caducity/feature/autobackup/worker/AutoBackupWorkScheduler.kt` (new)

```kotlin
package com.alorma.caducity.feature.autobackup.worker

import com.alorma.caducity.domain.autobackup.BackupFrequency

interface AutoBackupWorkScheduler {
  fun scheduleAutoBackup(frequency: BackupFrequency)
  fun cancelAutoBackup()
  fun triggerImmediateBackup()
}
```

**File**: `app/src/main/kotlin/com/alorma/caducity/feature/autobackup/worker/AutoBackupWorkSchedulerImpl.kt` (new)

Follow `app/src/main/kotlin/com/alorma/caducity/feature/notification/worker/ExpirationWorkSchedulerImpl.kt`:

```kotlin
package com.alorma.caducity.feature.autobackup.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alorma.caducity.domain.autobackup.BackupFrequency
import java.util.concurrent.TimeUnit

class AutoBackupWorkSchedulerImpl(
  private val context: Context,
) : AutoBackupWorkScheduler {

  override fun scheduleAutoBackup(frequency: BackupFrequency) {
    val constraints = Constraints.Builder()
      .setRequiresBatteryNotLow(true)
      .setRequiresStorageNotLow(true)
      .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
      .build()

    val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
      repeatInterval = frequency.hours,
      repeatIntervalTimeUnit = TimeUnit.HOURS,
      flexTimeInterval = 15,
      flexTimeIntervalUnit = TimeUnit.MINUTES
    )
      .setConstraints(constraints)
      .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      AutoBackupWorker.WORK_NAME,
      ExistingPeriodicWorkPolicy.REPLACE,
      workRequest
    )
  }

  override fun cancelAutoBackup() {
    WorkManager.getInstance(context).cancelUniqueWork(AutoBackupWorker.WORK_NAME)
  }

  override fun triggerImmediateBackup() {
    val workRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>().build()
    WorkManager.getInstance(context).enqueue(workRequest)
  }
}
```

**Key Points**:
- Uses `ExistingPeriodicWorkPolicy.REPLACE` to update frequency dynamically
- Battery and storage constraints prevent backups when device is low
- 15-minute flex interval allows WorkManager to optimize battery usage
- `triggerImmediateBackup()` for "Backup Now" button in settings

### 8. Create Settings ViewModel

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/autobackup/AutoBackupSettingsViewModel.kt` (new)

Follow ViewModel pattern from `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/backup/BackupViewModel.kt`:

```kotlin
package com.alorma.caducity.ui.screen.settings.autobackup

import com.alorma.caducity.base.viewmodel.BaseViewModel
import com.alorma.caducity.domain.autobackup.AutoBackupPreferences
import com.alorma.caducity.domain.autobackup.BackupFrequency
import com.alorma.caducity.feature.autobackup.worker.AutoBackupWorkScheduler

class AutoBackupSettingsViewModel(
  private val autoBackupPreferences: AutoBackupPreferences,
  private val autoBackupWorkScheduler: AutoBackupWorkScheduler,
) : BaseViewModel<Unit, Unit, AutoBackupSettingsSideEffect>() {

  val isEnabled = autoBackupPreferences.isEnabled
  val scheduleFrequency = autoBackupPreferences.scheduleFrequency
  val lastBackupTimestamp = autoBackupPreferences.loadLastBackupTimestamp()

  fun onToggleEnabled(enabled: Boolean) {
    autoBackupPreferences.setEnabled(enabled)
    if (enabled) {
      autoBackupWorkScheduler.scheduleAutoBackup(scheduleFrequency.value)
    } else {
      autoBackupWorkScheduler.cancelAutoBackup()
    }
  }

  fun onChangeFrequency(frequency: BackupFrequency) {
    autoBackupPreferences.setScheduleFrequency(frequency)
    if (isEnabled.value) {
      autoBackupWorkScheduler.scheduleAutoBackup(frequency)
    }
  }

  fun onBackupNow() {
    autoBackupWorkScheduler.triggerImmediateBackup()
    emitSideEffect(AutoBackupSettingsSideEffect.BackupTriggered)
  }

  override fun navigate(navigation: Unit) {
    // No navigation from this screen
  }
}

sealed interface AutoBackupSettingsSideEffect {
  data object BackupTriggered : AutoBackupSettingsSideEffect
}
```

**Key Points**:
- Uses `Unit` for navigation types (no navigation from settings)
- Reactive state with `MutableState` automatically updates UI
- Reschedules WorkManager when frequency changes
- `onBackupNow()` triggers immediate scheduled backup (creates timestamped file)

### 9. Create Settings UI Screen

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/autobackup/AutoBackupSettingsScreen.kt` (new)

Reference `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/SettingsRootScreen.kt` for compose-settings library usage. The screen should include:
- Enable/disable switch using `SettingsSwitch`
- Frequency selector using `SettingsMenuLink` (with dialog for selecting frequency)
- Last backup timestamp display (read-only `SettingsMenuLink`)
- "Backup Now" button using `SettingsMenuLink`
- Snackbar feedback using `SideEffectHandler`

**Key Points**:
- Uses compose-settings library for consistent UI (same as other settings screens)
- Follow the pattern from existing settings screens for side effect handling

### 10. Update Settings Root Screen Navigation

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/SettingsRootScreen.kt`

Add navigation to auto backup settings screen in the "Privacy & Backup" group. Change the existing Backup card's `position` from `ShapePosition.End` to `ShapePosition.Middle`, and add a new card with `position = ShapePosition.End` for automatic backup settings.

**Note**: Also need to add `onNavigateToAutoBackup: () -> Unit` parameter to `SettingsRootScreen` composable and wire it through the navigation graph.

### 11. Create Koin Module for Dependency Injection

**File**: `app/src/main/kotlin/com/alorma/caducity/di/AutoBackupModule.kt` (new)

Follow pattern from `app/src/main/kotlin/com/alorma/caducity/di/AppModule.kt`:

```kotlin
package com.alorma.caducity.di

import com.alorma.caducity.data.autobackup.AutoBackupFileManager
import com.alorma.caducity.data.autobackup.AutoBackupPreferencesImpl
import com.alorma.caducity.data.autobackup.AutoBackupTrigger
import com.alorma.caducity.domain.autobackup.AutoBackupPreferences
import com.alorma.caducity.domain.usecase.autobackup.AutoBackupUseCase
import com.alorma.caducity.feature.autobackup.worker.AutoBackupWorkScheduler
import com.alorma.caducity.feature.autobackup.worker.AutoBackupWorkSchedulerImpl
import com.alorma.caducity.ui.screen.settings.autobackup.AutoBackupSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val autoBackupModule = module {
  singleOf(::AutoBackupPreferencesImpl) bind AutoBackupPreferences::class

  single {
    AutoBackupFileManager(
      context = androidContext(),
      appClock = get(),
      dateFilenameFormat = get(qualifier = ConfigQualifier.DateFormat.BackupName),
    )
  }

  singleOf(::AutoBackupUseCase)

  single {
    AutoBackupTrigger(
      database = get(),
      autoBackupUseCase = get(),
      coroutineScope = CoroutineScope(SupervisorJob()),
    )
  }

  single<AutoBackupWorkScheduler> {
    AutoBackupWorkSchedulerImpl(context = androidContext())
  }

  viewModelOf(::AutoBackupSettingsViewModel)
}
```

**Update**: `app/src/main/kotlin/com/alorma/caducity/di/AppModule.kt`

Add `includes(autoBackupModule)` to the main module.

**Key Points**:
- Uses application-scoped coroutine with `SupervisorJob` for `AutoBackupTrigger`
- Reuses existing `AppClock` and date formatter
- All components registered as singletons except ViewModel

### 12. Initialize Auto Backup System in MainActivity

**File**: `app/src/main/kotlin/com/alorma/caducity/MainActivity.kt`

Add initialization in `onCreate()` (after `consentManager` initialization):

```kotlin
class MainActivity : AppCompatActivity() {

  private val consentManager: ConsentManager by inject()
  private val autoBackupTrigger: AutoBackupTrigger by inject()
  private val autoBackupWorkScheduler: AutoBackupWorkScheduler by inject()
  private val autoBackupPreferences: AutoBackupPreferences by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    consentManager.applyDefaultConsent()

    // Initialize auto backup system
    autoBackupTrigger.start()

    // Schedule worker if enabled
    if (autoBackupPreferences.loadIsEnabled()) {
      autoBackupWorkScheduler.scheduleAutoBackup(
        autoBackupPreferences.loadScheduleFrequency()
      )
    }

    enableEdgeToEdge()
    // ... rest of onCreate
  }

  override fun onDestroy() {
    autoBackupTrigger.stop()
    super.onDestroy()
  }
}
```

**Key Points**:
- Start trigger on app launch (registers Room observer)
- Schedule WorkManager job if auto backup is enabled
- Stop trigger on destroy (cleanup)

### 13. Add String Resources

**File**: `app/src/main/res/values/strings.xml`

Add all required string resources:

```xml
<!-- Auto Backup Settings -->
<string name="settings_auto_backup_title">Automatic Backup</string>
<string name="settings_auto_backup_description">Configure automatic backup frequency</string>
<string name="auto_backup_enable_title">Enable Automatic Backups</string>
<string name="auto_backup_enable_subtitle">Automatically backup your data</string>
<string name="auto_backup_frequency_title">Backup Frequency</string>
<string name="auto_backup_frequency_subtitle">How often to create scheduled backups</string>
<string name="auto_backup_frequency_hourly">Hourly</string>
<string name="auto_backup_frequency_daily">Daily</string>
<string name="auto_backup_frequency_weekly">Weekly</string>
<string name="auto_backup_last_backup_title">Last Backup</string>
<string name="auto_backup_last_backup_never">Never</string>
<string name="auto_backup_backup_now_title">Backup Now</string>
<string name="auto_backup_backup_now_subtitle">Create a backup immediately</string>
<string name="auto_backup_storage_location_title">Storage Location</string>
<string name="auto_backup_storage_location_subtitle">Local device storage</string>
<string name="auto_backup_triggered">Backup started</string>
```

## Critical Files to Create/Modify

**New Files** (11 total):
1. `app/src/main/kotlin/com/alorma/caducity/domain/autobackup/AutoBackupPreferences.kt` - Preferences interface
2. `app/src/main/kotlin/com/alorma/caducity/data/autobackup/AutoBackupPreferencesImpl.kt` - Preferences implementation
3. `app/src/main/kotlin/com/alorma/caducity/data/autobackup/AutoBackupFileManager.kt` - File I/O manager
4. `app/src/main/kotlin/com/alorma/caducity/domain/usecase/autobackup/AutoBackupUseCase.kt` - Backup orchestration
5. `app/src/main/kotlin/com/alorma/caducity/data/autobackup/AutoBackupTrigger.kt` - Database change detection (CRITICAL)
6. `app/src/main/kotlin/com/alorma/caducity/feature/autobackup/worker/AutoBackupWorker.kt` - WorkManager worker
7. `app/src/main/kotlin/com/alorma/caducity/feature/autobackup/worker/AutoBackupWorkScheduler.kt` - Scheduler interface
8. `app/src/main/kotlin/com/alorma/caducity/feature/autobackup/worker/AutoBackupWorkSchedulerImpl.kt` - Scheduler implementation
9. `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/autobackup/AutoBackupSettingsViewModel.kt` - Settings ViewModel
10. `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/autobackup/AutoBackupSettingsScreen.kt` - Settings UI
11. `app/src/main/kotlin/com/alorma/caducity/di/AutoBackupModule.kt` - Koin DI module

**Modified Files** (4 total):
1. `app/src/main/kotlin/com/alorma/caducity/MainActivity.kt` - Initialize auto backup system
2. `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/SettingsRootScreen.kt` - Add navigation
3. `app/src/main/kotlin/com/alorma/caducity/di/AppModule.kt` - Include new module
4. `app/src/main/res/values/strings.xml` - Add string resources

## Testing & Verification

### Manual Testing Checklist

**Settings UI**:
- [ ] Navigate to Settings → Automatic Backup
- [ ] Toggle auto backup on/off - verify WorkManager scheduled/canceled
- [ ] Change frequency (hourly/daily/weekly) - verify reschedule
- [ ] Tap "Backup Now" - verify snackbar and timestamped file created
- [ ] Verify last backup timestamp updates after manual trigger

**Action Backups**:
- [ ] Enable auto backup
- [ ] Create category - verify `auto_backup.json` exists in `context.filesDir`
- [ ] Add item - verify file timestamp updates
- [ ] Delete item - verify file timestamp updates
- [ ] Create category with 5 items - verify only 1 backup created (debouncing works)
- [ ] Open backup file - verify JSON is valid and contains all data

**Scheduled Backups**:
- [ ] Enable auto backup with hourly frequency
- [ ] Use Android Studio's WorkManager Inspector to verify job scheduled
- [ ] Trigger immediate backup - verify timestamped file in `context.filesDir/backups/`
- [ ] Verify filename format: `caducity_scheduled_YYYYMMDD_HHmmss.json`
- [ ] Manually create 12 scheduled backups - verify oldest 2 deleted (keep 10)
- [ ] Change frequency to daily - verify job rescheduled with new interval

**Error Scenarios**:
- [ ] Disable auto backup mid-operation - verify backups stop
- [ ] Manually delete `auto_backup.json` - verify recreated on next change
- [ ] Kill app during backup - verify WorkManager retries scheduled backup
- [ ] Import manual backup - verify action backup triggered after import

### Verification Commands

**Check action backup exists**:
```bash
adb shell ls /data/data/com.alorma.caducity/files/auto_backup.json
```

**Check scheduled backups**:
```bash
adb shell ls -la /data/data/com.alorma.caducity/files/backups/
```

**Pull backup files for inspection**:
```bash
adb pull /data/data/com.alorma.caducity/files/auto_backup.json
adb pull /data/data/com.alorma.caducity/files/backups/ backups/
```

**View WorkManager jobs**:
Use Android Studio → App Inspection → WorkManager Inspector

**Check SharedPreferences**:
```bash
adb shell cat /data/data/com.alorma.caducity/shared_prefs/*.xml | grep auto_backup
```

## Edge Cases & Architectural Decisions

### Edge Case: Multiple Rapid Data Changes
**Problem**: Creating category with 20 items triggers 21 database changes
**Solution**: Debouncing with 1 second delay - only backup after silence
**Result**: Single backup after all operations complete

### Edge Case: User Changes Frequency While Backup Running
**Problem**: Worker executing while new schedule set
**Solution**: `ExistingPeriodicWorkPolicy.REPLACE` ensures new schedule after current execution
**Result**: Current backup completes, new schedule takes effect

### Edge Case: Storage Full
**Problem**: Cannot write backup file
**Solution**: Action backup fails silently (logged), scheduled backup retries via WorkManager
**Result**: No user notification, next change/schedule retries

### Architectural Decision: InvalidationTracker vs Use Case Hooks
**Choice**: Room's `InvalidationTracker` for database change detection
**Rationale**:
- Non-invasive (no changes to existing use cases)
- Reliable (catches ALL database changes)
- Centralized (single responsibility)
- Future-proof (new use cases automatically trigger)

**Alternative Rejected**: Decorator pattern on use cases (too invasive)

### Architectural Decision: Two Backup Files
**Choice**: Separate files for action (`auto_backup.json`) and scheduled backups (timestamped)
**Rationale**:
- Action: Always up-to-date, single file, quick recovery
- Scheduled: Historical snapshots, time-based recovery
- Storage is cheap, data loss is expensive

### Architectural Decision: Application-Scoped Coroutine
**Choice**: `CoroutineScope(SupervisorJob())` for `AutoBackupTrigger`
**Rationale**:
- Backups must survive screen navigation
- ViewModel scopes tied to UI lifecycle
- `SupervisorJob` prevents backup failures from crashing app

## Future Enhancements

**Cloud Storage Integration**: Add Google Drive backend for scheduled backups (requires OAuth, network constraints, Drive API integration)

**Backup Encryption**: Optional encryption using Android Keystore for privacy

**Selective Backup**: Allow user to backup only specific categories (for large databases)

**Restore from Auto Backup**: Add UI to browse and restore from automatic backups (currently only manual restore)

**Backup Notifications**: Optional notification after successful scheduled backup

**Backup History**: Show list of all automatic backups with sizes and timestamps in settings