# GitHub Actions Workflows

This directory contains the GitHub Actions workflows for the Caducity project.

## Workflows

### Main (`main.yml`)
Runs on every push to the `main` branch. This workflow:
- Gets the current version from gradle
- Builds the debug APK
- Validates screenshot tests
- Builds the release AAB
- Uploads artifacts

### PR (`pr.yml`)
Runs on every pull request to the `main` branch. This workflow:
- Gets the current version from gradle
- Builds the debug APK
- Validates screenshot tests (fails if screenshots don't match)

### Release (`release.yml`)
Handles release builds and deployments.

### Update Screenshots (`update-screenshots.yml`)
Automatically generates and updates screenshot test reference images. This workflow:
- Runs automatically when screenshot test files or preview/theme files are modified on `main`
- Can be manually triggered from the GitHub Actions UI
- Generates new screenshot reference images using `./gradlew updateDebugScreenshotTest`
- Commits and pushes changes back to the `main` branch if screenshots have changed
- Uploads screenshot references as artifacts for review

#### How to Trigger Manually

1. Go to the **Actions** tab in the GitHub repository
2. Select **Update Screenshots** from the workflow list
3. Click **Run workflow**
4. Select the `main` branch
5. Click **Run workflow** button

The workflow will:
- Generate new screenshot reference images
- Check if any screenshots have changed
- If changes are detected, commit and push them to `main` with message: "Update screenshot test reference images [skip ci]"
- Upload screenshots as artifacts regardless of changes

#### Notes

- The workflow only runs on the `main` branch for safety
- Uses `[skip ci]` in commit message to prevent triggering other workflows
- Screenshot artifacts are retained for 30 days
- Changes are only committed if screenshots actually changed
