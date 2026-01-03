# Build Issue Implementation Skill

You are helping to implement a GitHub issue from start to finish, following a complete development workflow.

**IMPORTANT**: Use the TodoWrite tool to track your implementation process with these high-level tasks:
1. Fetch and analyze GitHub issue
2. Create implementation branch
3. Implement the feature/fix
4. Build and verify
5. Commit changes
6. Create pull request

## When to Use This Skill

This skill should be invoked when the user wants to:
- "Build issue #X"
- "Implement issue #X"
- "Work on issue #X"
- "Fix issue #X"
- "Complete issue #X"

Where X is a GitHub issue number.

## Workflow Overview

This skill follows a complete feature development workflow:
1. **Analyze** - Fetch and understand the issue
2. **Plan** - Create branch and plan implementation
3. **Implement** - Write code following project conventions
4. **Verify** - Build and test the implementation
5. **Deliver** - Commit and create PR

## Step 1: Fetch and Analyze Issue

Extract the issue number from the user's request and fetch it:

```bash
gh issue view ${ISSUE_NUMBER} --json title,body,labels,state,comments,assignees
```

**Analyze the issue**:
- If the issue has a "Refined" label, use the refinement details as implementation guide
- If NOT refined, you may need to ask clarifying questions or refine it first
- Check for acceptance criteria, technical context, and implementation guidance
- Identify affected components from the issue description

**Key Questions**:
- Is the issue clear enough to implement?
- Are there acceptance criteria defined?
- What files/components are affected?
- Are there any dependencies or prerequisites?

## Step 2: Create Implementation Branch

Create a descriptive feature branch:

```bash
git checkout -b feature/${DESCRIPTIVE_NAME}
```

**Branch naming conventions**:
- `feature/` - New features
- `fix/` - Bug fixes
- `refactor/` - Code refactoring
- `chore/` - Maintenance tasks

Examples:
- `feature/integrate-fire-and-forget`
- `fix/dashboard-crash-on-empty-data`
- `refactor/extract-product-mapper`

## Step 3: Implement the Feature

Follow the project's architecture and conventions from CLAUDE.md:

### Project Conventions to Follow

**Architecture**:
- Clean Architecture with separation: Data → Domain → UI
- Use Koin dependency injection with `singleOf`, `viewModelOf`, `factoryOf`
- ViewModels use `StateFlow<UiState>` for UI state
- Use Cases contain business logic
- Data sources handle persistence (Room database)

**Code Organization**:
- Place ViewModels in `ui/screen/<feature>/`
- Place Use Cases in `domain/usecase/`
- Place data sources in `data/datasource/`
- Place DI modules in `di/`

**Dependency Management**:
- Add dependencies to `gradle/libs.versions.toml` first
- Use version catalog references in build files
- Follow existing patterns for dependency declarations

**Key Patterns**:
- Register ViewModels with `viewModelOf(::ClassName)`
- Register singletons with `singleOf(::ClassName)` or `single { }`
- Use `bind` to map implementations to interfaces
- Always inject dependencies via constructor

**Date/Time**:
- Use `kotlinx-datetime` for all date/time operations
- Store timestamps as `Long` (Unix epoch milliseconds)
- Use `AppClock` abstraction for testable time operations

**Opt-In APIs** (already enabled project-wide):
- `kotlin.time.ExperimentalTime`
- `androidx.compose.material3.ExperimentalMaterial3Api`
- `androidx.compose.material3.ExperimentalMaterial3ExpressiveApi`

### Implementation Steps

1. **Read affected files** - Always read files before modifying them
2. **Follow refinement guidance** - If issue is refined, follow the implementation steps
3. **Make incremental changes** - One logical change at a time
4. **Use Edit tool** - Prefer editing existing files over creating new ones
5. **Maintain consistency** - Follow existing code style and patterns

**IMPORTANT**:
- NEVER create files unless absolutely necessary
- ALWAYS prefer editing existing files
- Read files before editing them
- Use the exact indentation from the file (tabs/spaces)

## Step 4: Build and Verify

Build the project to verify the implementation:

```bash
# Clean build
./gradlew clean assembleDebug

# Or just compile
./gradlew compileDebugKotlin

# Run tests (if applicable)
./gradlew testDebugUnitTest
```

**Verification checklist**:
- [ ] Build succeeds without errors
- [ ] No new compilation warnings introduced
- [ ] Existing tests still pass
- [ ] Implementation meets acceptance criteria

**If build fails**:
1. Read the error message carefully
2. Identify the root cause
3. Fix the issue
4. Rebuild
5. Repeat until successful

## Step 5: Commit Changes

Once the build succeeds, commit your changes:

### Commit Process

1. **Review changes**:
```bash
git status
git diff
```

2. **Stage files**:
```bash
git add <files>
```

3. **Create commit** with descriptive message:
```bash
git commit -m "$(cat <<'EOF'
[Short summary of changes]

[Detailed description of what was changed and why]

Changes:
- [Specific change 1]
- [Specific change 2]
- [Specific change 3]

Resolves #${ISSUE_NUMBER}

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

**Commit message guidelines**:
- First line: Clear, concise summary (50 chars or less)
- Blank line
- Detailed description explaining the "why"
- List specific changes made
- Reference the issue with "Resolves #X" or "Fixes #X"
- Include Claude Code attribution

**Examples**:
```
Integrate FireAndForget library with Koin DI

Add FireAndForget library to enable one-time operation flags...
```

```
Fix dashboard crash when product list is empty

Handle empty state in DashboardViewModel to prevent...
```

## Step 6: Create Pull Request

Push the branch and create a PR:

1. **Push branch**:
```bash
git push -u origin feature/${BRANCH_NAME}
```

2. **Create PR** using gh CLI:
```bash
gh pr create --title "${PR_TITLE}" --body "$(cat <<'EOF'
## Summary

[Brief description of what this PR does]

## Changes

- ✅ [Change 1]
- ✅ [Change 2]
- ✅ [Change 3]

## Technical Details

[Important technical decisions, patterns used, etc.]

## Test Plan

- [x] Build succeeds: `./gradlew assembleDebug`
- [ ] Manual testing performed
- [ ] Tests added/updated (if applicable)

## Related Issues

Resolves #${ISSUE_NUMBER}

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

**PR Description Guidelines**:
- Start with clear summary
- List all changes with checkmarks
- Include technical details if relevant
- Provide test plan showing what was verified
- Reference the issue with "Resolves #X"
- Include usage examples if it's a new feature/API

**PR Title Examples**:
- "Integrate FireAndForget library with Koin DI"
- "Fix dashboard crash on empty product list"
- "Add product search functionality"
- "Refactor ProductMapper to use sealed classes"

## Error Handling

### Common Issues and Solutions

**Build Failures**:
- Check error messages carefully
- Read compiler errors to understand the issue
- Fix issues one at a time
- Rebuild after each fix

**Dependency Issues**:
- Verify artifact coordinates are correct
- Check version compatibility
- Ensure repositories are configured (Maven Central, JitPack)
- Sync Gradle files

**DI Issues**:
- Ensure all dependencies are registered in Koin modules
- Check module is included in parent module
- Verify constructor parameters match registered types

**Compilation Errors**:
- Read the file before editing
- Match existing code patterns
- Use correct imports
- Follow Kotlin conventions

## Best Practices

1. **Read Before Write**: Always read files before modifying them
2. **Incremental Implementation**: Make changes step by step
3. **Verify Frequently**: Build often to catch issues early
4. **Follow Conventions**: Adhere to project patterns and style
5. **Document Changes**: Write clear commit messages and PR descriptions
6. **Test Thoroughly**: Build and verify before committing
7. **Clean History**: Make logical, atomic commits
8. **Communicate Clearly**: Write descriptive messages for future maintainers

## Full Example Workflow

```bash
# 1. Fetch issue
gh issue view 54 --json title,body,labels,state

# 2. Create branch
git checkout -b feature/integrate-fire-and-forget

# 3. Implement (read, edit, write files)
# [Implementation steps...]

# 4. Build and verify
./gradlew assembleDebug

# 5. Commit
git add .
git commit -m "Integrate FireAndForget library with Koin DI

Add FireAndForget library (v1.0.0) to enable one-time operation flags.

Changes:
- Add dependencies to version catalog
- Create FireAndForgetModule with DI setup
- Register module in Koin

Resolves #54

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 6. Push and create PR
git push -u origin feature/integrate-fire-and-forget
gh pr create --title "Integrate FireAndForget library" --body "..."
```

## Tips for Natural Invocation

This skill responds to natural language like:
- "build issue 54"
- "implement issue #32"
- "work on issue 45"
- "complete the task in issue 12"
- "fix issue #23"

The skill will:
1. Extract the issue number automatically
2. Follow the complete workflow from analysis to PR
3. Ask for confirmation at key decision points
4. Provide progress updates throughout

---

**Now begin**: Extract the issue number from the user's request and start the implementation workflow.

**ARGUMENTS**: ${ISSUE_NUMBER}