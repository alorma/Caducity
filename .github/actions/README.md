# GitHub Actions for Version Management

This directory contains reusable composite actions for managing version updates in the Caducity project.

## Actions

### 1. disable-snapshot

**Purpose:** Sets `snapshot=false` in `version.properties` file.

**Usage:**
```yaml
- name: Disable snapshot before release
  uses: ./.github/actions/disable-snapshot
```

**What it does:**
- Updates the `snapshot` property in `version.properties` to `false`
- Displays the updated file contents

### 2. bump-minor-version

**Purpose:** Increments the minor version by 1 and resets the patch version to 0 in `version.properties`.

**Usage:**
```yaml
- name: Bump minor version
  id: bump
  uses: ./.github/actions/bump-minor-version
```

**Outputs:**
- `next_version`: The new version string (e.g., "0.1.0")

**What it does:**
- Reads the current version from `version.properties`
- Increments the `minor` value by 1
- Resets the `patch` value to 0
- Sets `snapshot=true`
- Displays the updated file contents

**Example:**
- Current version: `0.0.5`
- New version: `0.1.0-snapshot`

### 3. commit-version

**Purpose:** Commits and pushes version.properties changes to the main branch.

**Inputs:**
- `version` (required): The version being committed (used in commit message)

**Usage:**
```yaml
- name: Commit version changes
  uses: ./.github/actions/commit-version
  with:
    version: ${{ steps.bump.outputs.next_version }}
```

**What it does:**
- Configures git with github-actions bot credentials
- Stages `version.properties` file
- Creates a commit with message: "Bump version to X.Y.Z"
- Pushes to main branch

## Workflows Using These Actions

### Bump Minor Version Workflow

**File:** `.github/workflows/bump-version.yml`

**Trigger:** Manual (workflow_dispatch)

**Purpose:** Allows manual triggering of a minor version bump without creating a release.

**What it does:**
1. Checks out the repository
2. Bumps the minor version (and resets patch to 0)
3. Commits and pushes the changes to main
4. Displays a summary in the workflow run

**When to use:** When you want to increment the minor version without creating a release (e.g., starting a new development cycle).

### Generate Release Workflow

**File:** `.github/workflows/release.yml`

**Trigger:** Manual (workflow_dispatch)

**Purpose:** Creates a full release including build, deployment, and version bump.

**What it does:**
1. Disables snapshot mode
2. Gets the current version
3. Builds AAB and APK artifacts
4. Deploys to Google Play (beta track)
5. Creates a GitHub release with tag
6. Bumps the minor version for next development cycle
7. Commits the updated version

## Version Format

The version follows semantic versioning: `MAJOR.MINOR.PATCH[-snapshot]`

- **major**: Major version (breaking changes)
- **minor**: Minor version (new features)
- **patch**: Patch version (bug fixes)
- **snapshot**: Boolean indicating if this is a development version

### Version Code Calculation

The version code is calculated as: `MMMNNNPPPS` where:
- `MMM`: Major version (3 digits, zero-padded)
- `NNN`: Minor version (3 digits, zero-padded)
- `PPP`: Patch version (3 digits, zero-padded)
- `S`: Snapshot indicator (0 for snapshot, 1 for release)

**Example:**
- Version `0.1.0-snapshot` → Version code: `0001000000`
- Version `0.1.0` → Version code: `0001000001`
