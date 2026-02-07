# Firebase Remote Config Usage Guide

This guide explains how to use Firebase Remote Config in the Caducity app.

## Overview

Firebase Remote Config allows you to change app configuration and behavior without requiring users to update the app. Config values are fetched from Firebase and can be updated on-demand through the Firebase Console.

## Architecture

The Remote Config implementation follows clean architecture principles:

```
config/remoteconfig/
├── RemoteConfigProvider.kt        # Interface for config access
├── FirebaseRemoteConfigProvider.kt # Firebase implementation
└── RemoteConfigDefaults.kt        # Default values and keys
```

## Setup

Remote Config is automatically initialized when the app starts. See `FIREBASE_SETUP.md` for Firebase project configuration.

## Adding a New Config Value

### 1. Define the Key and Default Value

Add your config key and default value to `RemoteConfigDefaults.kt`:

```kotlin
object RemoteConfigDefaults {
  object Keys {
    const val MY_FEATURE_ENABLED = "my_feature_enabled"
    const val MAX_ITEMS_COUNT = "max_items_count"
    const val WELCOME_MESSAGE = "welcome_message"
  }
  
  val defaults: Map<String, Any> = mapOf(
    Keys.MY_FEATURE_ENABLED to false,
    Keys.MAX_ITEMS_COUNT to 100L,
    Keys.WELCOME_MESSAGE to "Welcome to Caducity!",
  )
}
```

### 2. Create the Parameter in Firebase Console

1. Go to Firebase Console → Build → Remote Config
2. Click "Add parameter"
3. Enter the parameter key (e.g., `my_feature_enabled`)
4. Set the default value
5. Add conditions if needed (targeting specific users/versions)
6. Click "Publish changes"

### 3. Access the Config Value

Inject `RemoteConfigProvider` into your ViewModel or use case:

```kotlin
class MyViewModel(
  private val remoteConfig: RemoteConfigProvider
) : ViewModel() {
  
  fun checkFeature() {
    val isEnabled = remoteConfig.getBoolean(RemoteConfigDefaults.Keys.MY_FEATURE_ENABLED)
    if (isEnabled) {
      // Feature is enabled
      enableFeature()
    }
  }
  
  fun getMaxItems(): Long {
    return remoteConfig.getLong(RemoteConfigDefaults.Keys.MAX_ITEMS_COUNT)
  }
  
  fun getWelcomeMessage(): String {
    return remoteConfig.getString(RemoteConfigDefaults.Keys.WELCOME_MESSAGE)
  }
}
```

## Available Methods

### Type-Safe Getters

```kotlin
// Get a boolean value
val enabled = remoteConfig.getBoolean("feature_enabled")

// Get a string value
val message = remoteConfig.getString("welcome_message")

// Get a long value
val count = remoteConfig.getLong("max_items")

// Get a double value
val threshold = remoteConfig.getDouble("threshold_value")
```

### Fetch and Activate

```kotlin
// Fetch and activate latest config values
viewModelScope.launch {
  remoteConfig.fetchAndActivate()
    .onSuccess { activated ->
      // activated = true if new values were activated
      // activated = false if no new values were available
      if (activated) {
        // Reload UI with new values
        loadData()
      }
    }
    .onFailure { exception ->
      // Handle error (defaults will continue to be used)
      Timber.e(exception, "Failed to fetch remote config")
    }
}

// Separate fetch and activate (advanced usage)
viewModelScope.launch {
  remoteConfig.fetch()
    .onSuccess {
      // Config fetched, now activate
      remoteConfig.activate()
    }
}
```

## Fetch Intervals

The fetch interval determines how often the app can fetch new config values:

- **Debug builds**: 1 minute (for quick testing)
- **Production builds**: 1 hour (to reduce network usage)

These intervals are configured in `FirebaseRemoteConfigProvider`.

## Testing Remote Config

### Using Debug Settings UI

1. Build and install the app: `./gradlew installDebug`
2. Navigate to: Settings → Debug
3. View current Remote Config values
4. Click "Refresh Remote Config" to manually fetch latest values
5. Check the snackbar message for fetch result

### Changing Values in Firebase Console

1. Go to Firebase Console → Remote Config
2. Update parameter values
3. Click "Publish changes"
4. In the app, click "Refresh Remote Config" (or restart the app)
5. New values will be displayed immediately

## Best Practices

### 1. Always Define Default Values

```kotlin
// ✅ Good: Define defaults in RemoteConfigDefaults
val defaults: Map<String, Any> = mapOf(
  Keys.FEATURE_ENABLED to false,  // Safe default
)

// ❌ Bad: Relying only on remote values without defaults
```

### 2. Use Constants for Keys

```kotlin
// ✅ Good: Use constants to avoid typos
val enabled = remoteConfig.getBoolean(RemoteConfigDefaults.Keys.FEATURE_ENABLED)

// ❌ Bad: Hard-coded strings
val enabled = remoteConfig.getBoolean("feature_enabled")
```

### 3. Handle Fetch Failures Gracefully

```kotlin
// ✅ Good: Handle both success and failure
remoteConfig.fetchAndActivate()
  .onSuccess { /* Update UI */ }
  .onFailure { /* Log error, use defaults */ }

// ❌ Bad: Assuming fetch always succeeds
remoteConfig.fetchAndActivate()
```

### 4. Use Appropriate Types

```kotlin
// ✅ Good: Use correct types
val count = remoteConfig.getLong("max_count")  // Numbers should be Long
val threshold = remoteConfig.getDouble("threshold")  // Decimals should be Double

// ❌ Bad: Using wrong types
val count = remoteConfig.getString("max_count").toInt()  // Fragile
```

### 5. Consider User Experience

```kotlin
// ✅ Good: Fetch in background, don't block UI
viewModelScope.launch {
  remoteConfig.fetchAndActivate()  // Background operation
}

// ✅ Good: Show loading state for user-initiated refresh
_uiState.value = _uiState.value.copy(isRefreshing = true)
remoteConfig.fetchAndActivate()
_uiState.value = _uiState.value.copy(isRefreshing = false)
```

## Common Use Cases

### Feature Flags

Enable/disable features remotely without app updates:

```kotlin
val enableNewDashboard = remoteConfig.getBoolean("enable_new_dashboard")
if (enableNewDashboard) {
  NewDashboardScreen()
} else {
  OldDashboardScreen()
}
```

### Dynamic Configuration

Adjust app behavior based on remote config:

```kotlin
val expirationThreshold = remoteConfig.getLong("expiration_warning_days")
val isExpiringSoon = daysUntilExpiration <= expirationThreshold
```

### A/B Testing

Show different experiences to different users:

```kotlin
// In Firebase Console, create conditions for user segments
val experimentVariant = remoteConfig.getString("experiment_variant")
when (experimentVariant) {
  "variant_a" -> ShowVariantA()
  "variant_b" -> ShowVariantB()
  else -> ShowDefault()
}
```

### Emergency Messaging

Display important messages to users:

```kotlin
val emergencyMessage = remoteConfig.getString("emergency_message")
if (emergencyMessage.isNotEmpty()) {
  ShowEmergencyBanner(message = emergencyMessage)
}
```

## Troubleshooting

### Config Values Not Updating

1. Check that you published changes in Firebase Console
2. Verify the parameter key matches exactly (case-sensitive)
3. Check fetch interval hasn't been throttled
4. Try manual refresh in Debug Settings

### Default Values Always Used

1. Verify Firebase is initialized correctly
2. Check network connectivity
3. Look for errors in Logcat (filter by "Remote Config")
4. Ensure google-services.json is properly configured

### Build Errors

If you get compilation errors:
1. Sync Gradle: File → Sync Project with Gradle Files
2. Clean build: `./gradlew clean`
3. Rebuild: `./gradlew assembleDebug`

## Resources

- [Firebase Remote Config Documentation](https://firebase.google.com/docs/remote-config)
- [Best Practices](https://firebase.google.com/docs/remote-config/best-practices)
- [Use Cases](https://firebase.google.com/docs/remote-config/use-cases)
