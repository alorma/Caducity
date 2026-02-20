# GitHub Actions Workflows

This directory contains the GitHub Actions workflows for the Caducity project.

## Workflows

### Main (`main.yml`)
Runs on every push to the `main` branch. This workflow:
- Gets the current version from gradle
- Builds the debug APK
- Builds the release AAB
- Uploads artifacts

### PR (`pr.yml`)
Runs on every pull request to the `main` branch. This workflow:
- Gets the current version from gradle
- Builds the debug APK

### Release (`release.yml`)
Handles release builds and deployments. This workflow:
- Disables snapshot mode in version.properties
- Builds release AAB and APK artifacts
- Deploys to Google Play (beta track)
- Creates a GitHub release with tag
- Automatically bumps the minor version for the next development cycle
- Uses reusable composite actions from `.github/actions/`

**Trigger:** Manual (workflow_dispatch)

**Duration:** ~15-20 minutes

**Process:**
1. Get version for release
2. Build artifacts
3. Deploy to Google Play
4. Create GitHub release
5. Bump version (minor +1, patch reset to 0)

### Bump Version (`bump-version.yml`)
Manually bump the minor version without creating a release. This workflow:
- Increments the minor version by 1
- Resets the patch version to 0
- Sets snapshot to true
- Commits and pushes changes to main branch
- Uses reusable composite actions from `.github/actions/`

**Trigger:** Manual (workflow_dispatch)

**Duration:** ~1 minute

**Example:**
- Before: 0.0.5
- After: 0.1.0-snapshot

**When to use:** When you want to start a new minor version development cycle without creating a release.

#### How to Trigger Manually

1. Go to the **Actions** tab in the GitHub repository
2. Click **Run workflow**
3. Select the `main` branch
4. Click **Run workflow** button
