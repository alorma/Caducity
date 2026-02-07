# Firebase Remote Config Usage Guide

This guide explains how to use Firebase Remote Config in the Caducity app.

## Overview

Firebase Remote Config allows you to change app configuration and behavior without requiring users to update the app. The implementation follows the same pattern as the FireAndForget library, using abstract classes for each configuration parameter.

## Architecture

The Remote Config implementation follows the FireAndForget pattern:

```
config/remoteconfig/
├── RemoteConfigRunner.kt         # Abstract runner (like FireAndForgetRunner)
├── FirebaseRemoteConfigProvider.kt # Firebase implementation
├── RemoteConfig.kt               # Abstract base class (like FireAndForget)
├── RemoteConfigDefaults.kt       # Example configs and defaults
└── RememberRemoteConfig.kt       # Compose utilities
```

**Pattern Comparison:**

FireAndForget pattern:
```kotlin
class OnboardingFlag(runner: FireAndForgetRunner) : FireAndForget(
  fireAndForgetRunner = runner,
  name = "user_onboarding",
  defaultValue = true
)
// Usage: if (onboardingFlag.isEnabled()) { ... }
```

Remote Config pattern:
```kotlin
class ExampleFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "example_feature_enabled",
  defaultValue = false
)
// Usage: if (exampleFeatureConfig.isEnabled()) { ... }
```

## Setup

Remote Config is automatically initialized when the app starts. See `FIREBASE_SETUP.md` for Firebase project configuration.

## Usage Options

There are two ways to use Remote Config:

### Option 1: In Composables (Recommended)

Use `rememberRemoteConfig<T>()` directly in Composables:

```kotlin
@Composable
fun MyScreen() {
  val featureConfig = rememberRemoteConfig<ExampleFeatureConfig>()
  
  if (featureConfig.isEnabled()) {
    // Show new feature
    NewFeatureContent()
  } else {
    // Show old content
    OldContent()
  }
}
```

### Option 2: In ViewModels

Inject configs in ViewModels for business logic:

```kotlin
class MyViewModel(
  private val featureConfig: ExampleFeatureConfig
) : ViewModel() {
  
  fun checkFeature() {
    if (featureConfig.isEnabled()) {
      // Feature is enabled
      enableFeature()
    }
  }
}
```

## Adding a New Config Value

### 1. Create a Config Class

Create a class extending `RemoteConfig` in `RemoteConfigDefaults.kt`:

```kotlin
class MyFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "my_feature_enabled",
  defaultValue = false
)
```

### 2. Register in DI Module

Add it to `ConfigModule.kt`:

```kotlin
val configModule = module {
  // ...
  
  // Remote Configs
  singleOf(::MyFeatureConfig)
}
```

### 3. Add to Defaults Map

Add the default value to `RemoteConfigDefaults.defaults`:

```kotlin
object RemoteConfigDefaults {
  val defaults: Map<String, Any> = mapOf(
    "my_feature_enabled" to false,
    // ... other defaults
  )
}
```

### 4. Create Parameter in Firebase Console

1. Go to Firebase Console → Build → Remote Config
2. Click "Add parameter"
3. Enter the parameter key (e.g., `my_feature_enabled`)
4. Set the default value
5. Add conditions if needed
6. Click "Publish changes"

### 5. Use in Your Code

**In Composables:**

```kotlin
@Composable
fun MyScreen() {
  val myFeatureConfig = rememberRemoteConfig<MyFeatureConfig>()
  
  if (myFeatureConfig.isEnabled()) {
    // Show new feature
  }
}
```

**In ViewModels:**

```kotlin
class MyViewModel(
  private val myFeatureConfig: MyFeatureConfig
) : ViewModel() {
  fun checkFeature() {
    if (myFeatureConfig.isEnabled()) {
      // Feature is enabled
    }
  }
}
```

## Config Types

### Boolean Configs

```kotlin
class FeatureFlagConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "feature_flag",
  defaultValue = false
)

// In Composable
@Composable
fun FeatureScreen() {
  val featureFlag = rememberRemoteConfig<FeatureFlagConfig>()
  
  if (featureFlag.isEnabled()) {
    // Feature is enabled
  }
}

// In ViewModel
class MyViewModel(
  private val featureFlag: FeatureFlagConfig
) : ViewModel() {
  fun check() {
    if (featureFlag.isEnabled()) { ... }
  }
}
```

### String Configs

```kotlin
class WelcomeMessageConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "welcome_message",
  defaultValue = "Welcome!"
)

// In Composable
@Composable
fun WelcomeScreen() {
  val messageConfig = rememberRemoteConfig<WelcomeMessageConfig>()
  
  Text(text = messageConfig.asString())
}
```

### Long Configs

```kotlin
class MaxItemsConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "max_items",
  defaultValue = 100L
)

// In Composable
@Composable
fun ItemsList() {
  val maxItemsConfig = rememberRemoteConfig<MaxItemsConfig>()
  val maxItems = maxItemsConfig.asLong()
  
  LazyColumn {
    items(items.take(maxItems.toInt())) { item ->
      // ...
    }
  }
}
```

### Double Configs

```kotlin
class ThresholdConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "threshold_value",
  defaultValue = 0.75
)

// In Composable
@Composable
fun ThresholdIndicator() {
  val thresholdConfig = rememberRemoteConfig<ThresholdConfig>()
  val threshold = thresholdConfig.asDouble()
  
  // Use threshold value
}
```

## Manual Fetch and Activate

If you need to manually refresh config values:

```kotlin
class MyViewModel(
  private val remoteConfigRunner: RemoteConfigRunner
) : ViewModel() {
  
  fun refreshConfig() {
    viewModelScope.launch {
      remoteConfigRunner.fetchAndActivate()
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

### 1. Create One Class Per Config

```kotlin
// ✅ Good: Each config has its own class
class FeatureAConfig(runner: RemoteConfigRunner) : RemoteConfig(...)
class FeatureBConfig(runner: RemoteConfigRunner) : RemoteConfig(...)

// ❌ Bad: Sharing configs across features
// Don't do this - create specific config classes instead
```

### 2. Use Descriptive Names

```kotlin
// ✅ Good: Clear, descriptive names
class EnableNewDashboardConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "enable_new_dashboard",
  defaultValue = false
)

// ❌ Bad: Vague or generic names
class Config1(runner: RemoteConfigRunner) : RemoteConfig(...)
```

### 3. Always Provide Safe Defaults

```kotlin
// ✅ Good: Safe default that won't break functionality
class ExperimentalFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "experimental_feature",
  defaultValue = false  // Safe default
)

// ❌ Bad: Default that could cause issues
class RequiredFeatureConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "required_feature",
  defaultValue = true  // Risky if feature isn't ready
)
```

### 4. Register in DI Module

```kotlin
// ✅ Good: Register in ConfigModule
val configModule = module {
  singleOf(::MyFeatureConfig)
}

// ❌ Bad: Creating instances manually
// Don't do this - always use DI
```

### 5. Inject Individual Configs

```kotlin
// ✅ Good: Inject specific configs you need
class MyViewModel(
  private val featureAConfig: FeatureAConfig,
  private val featureBConfig: FeatureBConfig
) : ViewModel() {
  fun checkFeatures() {
    if (featureAConfig.isEnabled()) { /* ... */ }
    if (featureBConfig.isEnabled()) { /* ... */ }
  }
}

// ❌ Bad: Injecting the runner directly
class MyViewModel(
  private val remoteConfigRunner: RemoteConfigRunner
) : ViewModel() {
  // This works but doesn't benefit from the abstraction
}
```

## Common Use Cases

### Feature Flags

Enable/disable features remotely without app updates:

```kotlin
// 1. Create the config class
class EnableNewDashboardConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "enable_new_dashboard",
  defaultValue = false
)

// 2. Register in DI
singleOf(::EnableNewDashboardConfig)

// 3. Use in Composable
@Composable
fun DashboardScreen() {
  val enableNewDashboard = rememberRemoteConfig<EnableNewDashboardConfig>()
  
  if (enableNewDashboard.isEnabled()) {
    NewDashboardContent()
  } else {
    OldDashboardContent()
  }
}

// OR use in ViewModel
class DashboardViewModel(
  private val enableNewDashboard: EnableNewDashboardConfig
) : ViewModel() {
  fun getScreen() {
    if (enableNewDashboard.isEnabled()) {
      // Show new dashboard
    }
  }
}
```

### Dynamic Configuration

Adjust app behavior based on remote config:

```kotlin
// 1. Create the config class
class ExpirationThresholdConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "expiration_warning_days",
  defaultValue = 7L
)

// 2. Use in Composable
@Composable
fun ExpirationWarning(daysUntilExpiration: Long) {
  val thresholdConfig = rememberRemoteConfig<ExpirationThresholdConfig>()
  val threshold = thresholdConfig.asLong()
  
  if (daysUntilExpiration <= threshold) {
    WarningBadge()
  }
}
```

### A/B Testing

Show different experiences to different users:

```kotlin
// 1. Create the config class
class ExperimentVariantConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "experiment_variant",
  defaultValue = "control"
)

// 2. Use with Firebase Console conditions for user segments
@Composable
fun ExperimentScreen() {
  val variantConfig = rememberRemoteConfig<ExperimentVariantConfig>()
  
  when (variantConfig.asString()) {
    "variant_a" -> VariantAContent()
    "variant_b" -> VariantBContent()
    else -> ControlContent()
  }
}
```

### Emergency Messaging

Display important messages to users:

```kotlin
// 1. Create the config class
class EmergencyMessageConfig(runner: RemoteConfigRunner) : RemoteConfig(
  remoteConfigRunner = runner,
  key = "emergency_message",
  defaultValue = ""
)

// 2. Use in Composable
@Composable
fun MainScreen() {
  val emergencyConfig = rememberRemoteConfig<EmergencyMessageConfig>()
  val message = emergencyConfig.asString()
  
  Column {
    if (message.isNotEmpty()) {
      EmergencyBanner(message = message)
    }
    // Rest of screen content
  }
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
