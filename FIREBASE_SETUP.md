# Firebase Setup for Fake Data Generation

This document explains how to set up Firebase Vertex AI for the AI-powered fake data generation feature.

## Prerequisites

1. A Firebase project (create one at https://console.firebase.google.com/)
2. Vertex AI API enabled in your Firebase project

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

## Step 3: Enable Vertex AI

1. In Firebase Console, navigate to "Build" → "Vertex AI in Firebase"
2. Click "Get Started" if not already enabled
3. Accept terms and enable the Vertex AI API
4. The gemini-1.5-flash model should be available by default

## Step 4: Verify Setup

Build the project:
```bash
./gradlew assembleDebug
```

If the build succeeds, Firebase is properly configured.

## Using the Feature

1. Build and install the app: `./gradlew installDebug`
2. Navigate to: Settings → Debug → Generate Fake Data
3. Configure parameters (products, variants, instances)
4. Click "Generate"
5. The app will use Gemini AI to create realistic grocery products

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
- Consider adding `app/google-services.json` to `.gitignore` for open-source projects
- Provide a sample template file (`google-services.json.template`) instead

## Cost

Firebase Vertex AI (Gemini) pricing:
- Free tier: Generous quotas for development/testing
- Pay-as-you-go after free tier
- gemini-1.5-flash is optimized for low cost

Check current pricing: https://firebase.google.com/pricing

## Alternative: Disable Feature

If you don't want to use Firebase, you can temporarily disable the fake data feature by removing the google-services plugin from `app/build.gradle.kts`:

```kotlin
// Comment out this line:
// alias(libs.plugins.google.services)
```

The app will build but the "Generate Fake Data" button will show a Firebase configuration error when clicked.
