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

## Step 4: Create Vertex AI Prompt Templates

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

## Step 5: Verify Setup

Build the project:
```bash
./gradlew assembleDebug
```

If the build succeeds, Firebase is properly configured.

## Using the Features

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
