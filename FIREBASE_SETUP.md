# Firebase Setup for Caducity

This document explains how to set up Firebase services used by the Caducity app:
- **Crashlytics**: Crash reporting and analytics
- **Vertex AI**: AI-powered fake data generation feature
- **Remote Config**: Dynamic configuration values without app updates

## Prerequisites

1. A Firebase project (create one at https://console.firebase.google.com/)
2. For Vertex AI: Vertex AI API enabled in your Firebase project
3. For Crashlytics: Crashlytics enabled in your Firebase project
4. For Remote Config: Remote Config enabled in your Firebase project

## Step 1: Create/Configure Firebase Project

1. Go to https://console.firebase.google.com/
2. Create a new project or select an existing one
3. Add an Android app to your Firebase project:
   - Android package name: `com.alorma.caducity.dev` (for debug)
   - Android package name: `com.alorma.caducity` (for release)

## Step 2: Download Configuration File

1. In Firebase Console, go to Project Settings
2. Under "Your apps", find your Android app
3. Click "Download google-services.json"
4. Place the file in the `app/` directory:
   ```
   app/google-services.json
   ```

## Step 3: Enable Firebase Crashlytics

Firebase Crashlytics is already integrated in the app for crash reporting and analytics.

1. In Firebase Console, navigate to "Build" → "Crashlytics"
2. Click "Get Started" if not already enabled
3. The app will automatically start sending crash reports once built and run

### Testing Crashlytics Integration

To verify Crashlytics is working:

1. Build and install the app: `./gradlew installDebug`
2. Run the app on a device or emulator
3. Force a test crash (add a button that throws an exception for testing)
4. Restart the app (crashes are sent on next app launch)
5. Check Firebase Console → Crashlytics for the crash report

### Build Configuration

The app is configured to:
- **Debug builds**: Include Crashlytics but don't require mapping file upload
- **Release builds**: Include Crashlytics with ProGuard/R8 mapping files for stack trace deobfuscation
- **CI/CD**: GitHub Actions automatically uploads mapping files during release builds

### Mapping File Upload

ProGuard/R8 mapping files are automatically uploaded to Firebase Crashlytics during release builds in CI/CD environments (when `GITHUB_ACTIONS` environment variable is present). This allows Firebase to deobfuscate stack traces from minified release builds.

For local release builds without network access, the upload task may fail but the build will still succeed. The mapping files are generated locally and can be uploaded manually if needed.

## Step 4: Enable Vertex AI

1. In Firebase Console, navigate to "Build" → "Vertex AI in Firebase"
2. Click "Get Started" if not already enabled
3. Accept terms and enable the Vertex AI API
4. The gemini-1.5-flash model should be available by default

## Step 5: Create Vertex AI Prompt Templates

The app uses two prompt templates for AI-powered features:

### Template 1: Dashboard Products Generation (Template ID: `product-list-generation`)

This template generates complete products with variants and instances from the Dashboard screen.

1. In Firebase Console, navigate to "Build" → "Vertex AI in Firebase" → "Prompt Templates"
2. Click "Create Template"
3. Set Template ID: `product-list-generation`
4. Copy the system instructions and schema from `prompts/dashboard-products-template.md`
5. Define input parameter: `input` (string)
6. Test with sample inputs
7. Deploy template

### Template 2: Product Detail Variants Generation (Template ID: `product-detail-variants`)

This template generates variants and instances for an existing product from the Product Detail screen.

1. In Firebase Console, navigate to "Build" → "Vertex AI in Firebase" → "Prompt Templates"
2. Click "Create Template"
3. Set Template ID: `product-detail-variants`
4. Copy the system instructions and schema from `prompts/product-detail-variants-template.md`
5. Define input parameters:
   - `productName` (string) - Name of the existing product
   - `userPrompt` (string) - User's description of variants/instances
6. Test with sample inputs
7. Deploy template

**Important**: Template IDs must match exactly as specified above, as the code references them by these IDs.

## Step 6: Verify Setup

Build the project:
```bash
./gradlew assembleDebug
```

If the build succeeds, Firebase is properly configured.

## Using the Features

### Firebase Crashlytics

Crashlytics works automatically once the app is installed and running:

1. Crash reports are automatically collected when the app crashes
2. Reports are sent to Firebase on the next app launch
3. View crash reports in Firebase Console → Crashlytics
4. Stack traces are automatically deobfuscated for release builds

### AI-Powered Features

### AI-Powered Product Generation (Dashboard)

1. Build and install the app: `./gradlew installDebug`
2. Navigate to: Dashboard
3. Click the AI FAB button (floating action button)
4. Enter a natural language description of your groceries
   - Example: "3 bottles of 1L whole milk, expires in 5 days"
5. Click "Generate"
6. Review the generated products
7. Click "Confirm" to add them to your inventory

### AI-Powered Variant Generation (Product Detail)

1. Navigate to any product's detail screen
2. Click the Sparkles (✨) icon in the top bar
3. Enter a description of variants/instances to add
   - Example: "2 cartons of 500ml, expires in 7 days"
4. Click "Generate"
5. Review the generated variants and instances
6. Click "Add to Product" to confirm

### Debug Feature: Bulk Generation

1. Navigate to: Settings → Debug → Generate Fake Data
2. Configure parameters (products, variants, instances)
3. Click "Generate"
4. The app will use Gemini AI to create realistic grocery products

## Step 7: Configure Firebase Remote Config

Firebase Remote Config allows you to change app configuration and behavior without requiring users to update the app.

1. In Firebase Console, navigate to "Build" → "Remote Config"
2. Click "Get Started" if not already enabled
3. The app will automatically fetch remote config values on startup

### Creating Remote Config Parameters

You can create config parameters on demand in the Firebase Console:

1. In Firebase Console, go to "Build" → "Remote Config"
2. Click "Add parameter"
3. Enter parameter details:
   - **Parameter key**: Use constants from `RemoteConfigDefaults.Keys` (e.g., `example_feature_enabled`)
   - **Default value**: The value to use before remote config is fetched
   - **Description**: What this parameter controls
4. Set conditions (optional): Target specific users, app versions, or platforms
5. Click "Publish changes"

### Using Remote Config in Code

The app includes a complete Remote Config abstraction:

**Adding new config values:**

1. Add the key constant to `RemoteConfigDefaults.Keys`:
   ```kotlin
   object Keys {
     const val MY_NEW_FEATURE = "my_new_feature"
   }
   ```

2. Add the default value to `RemoteConfigDefaults.defaults`:
   ```kotlin
   val defaults: Map<String, Any> = mapOf(
     Keys.MY_NEW_FEATURE to true,
   )
   ```

3. Access the config value in your code:
   ```kotlin
   class MyViewModel(
     private val remoteConfig: RemoteConfigProvider
   ) : ViewModel() {
     fun checkFeature() {
       val isEnabled = remoteConfig.getBoolean(RemoteConfigDefaults.Keys.MY_NEW_FEATURE)
       if (isEnabled) {
         // Feature is enabled
       }
     }
   }
   ```

4. Create the parameter in Firebase Console with the same key
5. Publish changes
6. The app will fetch new values on next startup

**Manual fetch and activate:**

```kotlin
// In a ViewModel or use case
viewModelScope.launch {
  remoteConfig.fetchAndActivate()
    .onSuccess { activated ->
      // New values fetched and activated
    }
    .onFailure { exception ->
      // Handle error, defaults will be used
    }
}
```

### Remote Config Features

- **Automatic initialization**: Remote Config is initialized on app startup
- **Default values**: Defined in `RemoteConfigDefaults` for offline/fallback scenarios
- **Type-safe access**: `getString()`, `getBoolean()`, `getLong()`, `getDouble()`
- **Debug mode**: Faster fetch intervals (1 minute) in debug builds for testing
- **Production mode**: Standard fetch intervals (1 hour) for production builds

### Testing Remote Config

1. Build and install the app: `./gradlew installDebug`
2. Run the app - it will fetch remote config on startup
3. Check logs for "Remote Config: Fetch and activate successful"
4. Change parameter values in Firebase Console
5. Restart the app to see new values (or trigger manual fetch)


## Troubleshooting

### Build Error: "File google-services.json is missing"
- Ensure `google-services.json` is in the `app/` directory
- Check that the file is not in `.gitignore`

### Runtime Error: "Firebase not configured"
- Verify the package name in `google-services.json` matches the app ID
- Debug: `com.alorma.caducity.dev`
- Release: `com.alorma.caducity`

### Network/Quota Errors
- Check that Vertex AI API is enabled in Firebase Console
- Verify you haven't exceeded free tier quotas
- Check network connectivity

## Security Notes

- The `google-services.json` file contains API keys
- These keys are restricted to your app's package name in Firebase Console
- The file is already in `.gitignore` to prevent accidental commits
- Firebase Crashlytics automatically collects crash reports but does NOT collect personally identifiable information (PII) by default
- Review the [Firebase Crashlytics Privacy Policy](https://firebase.google.com/terms/crashlytics) for more information

## Cost

**Firebase Crashlytics:**
- Completely free with no quotas or limits
- Includes unlimited crash reports and analytics

**Firebase Vertex AI (Gemini):**
- Free tier: Generous quotas for development/testing
- Pay-as-you-go after free tier
- gemini-1.5-flash is optimized for low cost

Check current pricing: https://firebase.google.com/pricing

## Features Included

### Always Active (Required)
- **Crashlytics**: Crash reporting and analytics - always enabled when Firebase is configured
- **Remote Config**: Dynamic configuration values - automatically fetched on app startup

### Optional Features
- **Vertex AI**: AI-powered fake data generation - only used when explicitly requested by user

## Alternative: Disable Firebase

If you don't want to use Firebase at all, you can disable it by removing the plugins from `app/build.gradle.kts`:

```kotlin
// Comment out these lines:
// alias(libs.plugins.google.services)
// alias(libs.plugins.firebase.crashlytics)
```

**Note**: 
- The app will still build without Firebase
- Crashlytics will be disabled (no crash reports)
- Remote Config will be disabled (only default values will be used)
- The "Generate Fake Data" feature will show a Firebase configuration error when clicked
- You'll also need to remove or comment out the Firebase dependencies in `app/build.gradle.kts`
