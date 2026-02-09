# Firebase Authentication Integration Plan

## Context

The user wants to add cloud backup functionality to Caducity using Firebase Firestore. To enable this, we need to integrate Firebase Authentication with Google Sign-In as the authentication method. This is the foundation phase that will enable future sync capabilities.

**Key Requirements**:
- Google Sign-In only (no email/password)
- Progressive sign-in prompt after user's first success moment (create item or item action)
- Dashboard avatar showing sign-in status (generic icon → sign-in, profile picture → account menu)

**Good News**: Firebase is already fully integrated with Analytics, Crashlytics, Remote Config, and App Check. We're building on solid existing infrastructure.

## Implementation Plan

### Phase 1: Add Firebase Auth Dependencies

**File**: `app/build.gradle.kts`

Add Firebase Auth and Google Sign-In dependencies:
```kotlin
// In dependencies block, add alongside existing Firebase dependencies
implementation(libs.firebase.auth)
implementation(libs.play.services.auth) // For Google Sign-In
```

**File**: `gradle/libs.versions.toml`

Add version catalog entries:
```toml
[versions]
firebase-auth = "23.1.0"  # Or latest stable
play-services-auth = "21.2.0"

[libraries]
firebase-auth = { module = "com.google.firebase:firebase-auth", version.ref = "firebase-auth" }
play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "play-services-auth" }
```

### Phase 2: Create Auth Domain Layer

**New File**: `app/src/main/kotlin/com/alorma/caducity/feature/auth/domain/AuthState.kt`

```kotlin
sealed interface AuthState {
  data object SignedOut : AuthState
  data class SignedIn(
    val userId: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
  ) : AuthState
}
```

**New File**: `app/src/main/kotlin/com/alorma/caducity/feature/auth/domain/AuthManager.kt`

```kotlin
interface AuthManager {
  val authState: StateFlow<AuthState>

  suspend fun signInWithGoogle(activityContext: Context): Result<AuthState.SignedIn>
  suspend fun signOut(): Result<Unit>
  fun getCurrentUser(): AuthState
}
```

**New File**: `app/src/main/kotlin/com/alorma/caducity/feature/auth/data/FirebaseAuthManager.kt`

Implementation using Firebase Auth + Google Sign-In:
```kotlin
class FirebaseAuthManager(
  private val firebaseAuth: FirebaseAuth,
  private val context: Context,
) : AuthManager {

  private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
  override val authState: StateFlow<AuthState> = _authState.asStateFlow()

  init {
    // Listen to auth state changes
    firebaseAuth.addAuthStateListener { auth ->
      _authState.value = auth.currentUser?.toAuthState() ?: AuthState.SignedOut
    }
  }

  override suspend fun signInWithGoogle(activityContext: Context): Result<AuthState.SignedIn> {
    // Configure Google Sign-In with web client ID from google-services.json
    // Launch Google Sign-In intent
    // Exchange Google token for Firebase credential
    // Sign in to Firebase
  }

  override suspend fun signOut(): Result<Unit> {
    firebaseAuth.signOut()
    return Result.success(Unit)
  }

  override fun getCurrentUser(): AuthState {
    return firebaseAuth.currentUser?.toAuthState() ?: AuthState.SignedOut
  }

  private fun FirebaseUser.toAuthState(): AuthState.SignedIn {
    return AuthState.SignedIn(
      userId = uid,
      email = email,
      displayName = displayName,
      photoUrl = photoUrl?.toString(),
    )
  }
}
```

### Phase 3: Create Auth Module

**New File**: `app/src/main/kotlin/com/alorma/caducity/feature/auth/AuthModule.kt`

```kotlin
val authModule = module {
  single { FirebaseAuth.getInstance() }
  singleOf(::FirebaseAuthManager) bind AuthManager::class
  singleOf(::SignInPromptFlag)
}
```

**Register in AppModule**: Add `includes(authModule)` alongside other modules.

### Phase 4: Create SignInPromptFlag

**New File**: `app/src/main/kotlin/com/alorma/caducity/feature/auth/SignInPromptFlag.kt`

```kotlin
class SignInPromptFlag(runner: FireAndForgetRunner) : FireAndForget(
  fireAndForgetRunner = runner,
  name = "firebase_signin_prompt",
  autoDisable = true,  // Only show once
)
```

### Phase 5: Update Dashboard with Sign-In Prompt

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardViewModel.kt`

**Changes**:
1. Change `SideEffect` generic parameter from `Unit` to new `DashboardSideEffect` sealed interface
2. Inject `AuthManager` and `SignInPromptFlag`
3. Check flag in `init` block after user creates/interacts with items
4. Emit side effect to show sign-in prompt

```kotlin
// Add sealed interface
sealed interface DashboardSideEffect {
  data object ShowSignInPrompt : DashboardSideEffect
  data object ShowSignInSuccess : DashboardSideEffect
  data object ShowSignInError : DashboardSideEffect
}

// Update ViewModel signature
class DashboardViewModel(
  // ... existing parameters
  private val authManager: AuthManager,
  private val signInPromptFlag: SignInPromptFlag,
) : BaseViewModel<
  DashboardNavigation,
  DashboardNavigationSideEffect,
  DashboardSideEffect  // Changed from Unit
>() {

  init {
    loadDashboard()
    checkSignInPrompt()
  }

  private fun checkSignInPrompt() {
    viewModelScope.launch {
      // Wait for user to have at least one item
      state.filterIsInstance<DashboardState.Success>()
        .first { it.dashboard.totalItems > 0 }

      // Check if we should show prompt
      if (signInPromptFlag.isEnabled() && authManager.getCurrentUser() is AuthState.SignedOut) {
        delay(500) // Brief delay for better UX
        emitSideEffect(DashboardSideEffect.ShowSignInPrompt)
      }
    }
  }

  fun onSignInWithGoogle(activityContext: Context) {
    viewModelScope.launch {
      val result = authManager.signInWithGoogle(activityContext)
      if (result.isSuccess) {
        signInPromptFlag.disable()
        emitSideEffect(DashboardSideEffect.ShowSignInSuccess)
      } else {
        emitSideEffect(DashboardSideEffect.ShowSignInError)
      }
    }
  }

  fun onDismissSignInPrompt() {
    signInPromptFlag.disable()
  }
}
```

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardScreen.kt`

**Changes**:
1. Initialize `dialogState` and `snackbarState`
2. Create `SideEffectHandler` composable
3. Handle sign-in prompt dialog
4. Pass states to `AppScaffold`

```kotlin
@Composable
fun DashboardScreen(
  // ... existing parameters
) {
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()

  SideEffectHandler(
    viewModel = viewModel,
    dialogState = dialogState,
    snackbarState = snackbarState,
  )

  // ... existing state collection

  AppScaffold(
    // ... existing parameters
    dialogState = dialogState,
    snackbarState = snackbarState,
    // ... rest of scaffold
  )
}

@Composable
private fun SideEffectHandler(
  viewModel: DashboardViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
) {
  val context = LocalContext.current
  val activity = context.findActivity()

  LaunchedEffect(viewModel.sideEffects) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        DashboardSideEffect.ShowSignInPrompt -> launch {
          val result = dialogState.showAlertDialog(
            title = { Text(stringResource(R.string.dashboard_signin_prompt_title)) },
            text = { Text(stringResource(R.string.dashboard_signin_prompt_message)) },
            positiveButton = { Text(stringResource(R.string.dashboard_signin_prompt_signin)) },
            negativeButton = { Text(stringResource(R.string.dashboard_signin_prompt_later)) },
            type = AppFeedbackType.Info,
          )

          when (result) {
            DialogResult.Positive -> {
              activity?.let { viewModel.onSignInWithGoogle(it) }
            }
            DialogResult.Negative, DialogResult.Dismissed -> {
              viewModel.onDismissSignInPrompt()
            }
          }
        }

        DashboardSideEffect.ShowSignInSuccess -> launch {
          snackbarState.showSnackbar(
            message = R.string.dashboard_signin_success,
            type = AppFeedbackType.Success,
          )
        }

        DashboardSideEffect.ShowSignInError -> launch {
          snackbarState.showSnackbar(
            message = R.string.dashboard_signin_error,
            type = AppFeedbackType.Error,
          )
        }
      }
    }
  }
}
```

### Phase 6: Add Dashboard Avatar

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardScreen.kt`

Update top bar to show avatar:

```kotlin
AppScaffold(
  topBar = {
    StyledTopAppBar(
      title = { Text(text = stringResource(R.string.dashboard_screen_title)) },
      actions = {
        // Add avatar before settings icon
        UserAvatar(
          authState = viewModel.authState.collectAsState().value,
          onAvatarClick = { /* Navigate to account menu */ },
        )

        IconButton(onClick = onNavigateToSettings) {
          Icon(imageVector = AppIcons.Outlined.Settings, contentDescription = null)
        }
      },
    )
  },
  // ...
)
```

**New File**: `app/src/main/kotlin/com/alorma/caducity/ui/components/UserAvatar.kt`

```kotlin
@Composable
fun UserAvatar(
  authState: AuthState,
  onAvatarClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (authState) {
    is AuthState.SignedOut -> {
      IconButton(onClick = onAvatarClick, modifier = modifier) {
        Icon(
          imageVector = AppIcons.Outlined.Person,
          contentDescription = stringResource(R.string.dashboard_signin_button_description),
        )
      }
    }

    is AuthState.SignedIn -> {
      IconButton(onClick = onAvatarClick, modifier = modifier) {
        if (authState.photoUrl != null) {
          AsyncImage(
            model = authState.photoUrl,
            contentDescription = authState.displayName,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape),
          )
        } else {
          Icon(
            imageVector = AppIcons.Filled.Person,
            contentDescription = authState.displayName,
          )
        }
      }
    }
  }
}
```

### Phase 7: Add Settings Screen Integration

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/SettingsRootScreen.kt`

Add new "Account" section at the top:

```kotlin
LazyColumn {
  // Add Account section before Appearance
  item {
    StyledSettingsGroup(title = { Text(stringResource(R.string.settings_account_title)) }) {
      AccountSettingsCard(
        authState = authState,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
        position = ShapePosition.Single,
      )
    }
  }

  // Existing Appearance section...
}
```

**New File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/account/AccountSettingsScreen.kt`

Simple screen showing:
- Current sign-in status
- Email/name if signed in
- "Sign In with Google" button if signed out
- "Sign Out" button if signed in
- Cloud backup toggle (enabled only when signed in)

### Phase 8: String Resources

**File**: `app/src/main/res/values/strings.xml`

Add all required strings:

```xml
<!-- Sign-In Prompt -->
<string name="dashboard_signin_prompt_title">Back up your data?</string>
<string name="dashboard_signin_prompt_message">Sign in with Google to automatically back up your groceries and sync across devices.</string>
<string name="dashboard_signin_prompt_signin">Sign In</string>
<string name="dashboard_signin_prompt_later">Maybe Later</string>
<string name="dashboard_signin_success">Successfully signed in!</string>
<string name="dashboard_signin_error">Sign-in failed. Please try again.</string>

<!-- Settings -->
<string name="settings_account_title">Account</string>
<string name="settings_account_signin">Sign in with Google</string>
<string name="settings_account_signout">Sign Out</string>
<string name="settings_account_signed_in_as">Signed in as %s</string>

<!-- Avatar -->
<string name="dashboard_signin_button_description">Sign in to enable cloud backup</string>
```

### Phase 9: Analytics Tracking

**File**: `app/src/main/kotlin/com/alorma/caducity/feature/tracking/Actions.kt`

Add auth-related actions:

```kotlin
class SignInPromptShownAction : Action(
  name = "signin_prompt_shown",
)

class SignInSuccessAction : Action(
  name = "signin_success",
  parameters = mapOf("method" to "google"),
)

class SignInDismissedAction : Action(
  name = "signin_dismissed",
)

class SignOutAction : Action(
  name = "signout",
)
```

Track in `DashboardViewModel`:
```kotlin
private fun checkSignInPrompt() {
  // ...
  if (signInPromptFlag.isEnabled() && authManager.getCurrentUser() is AuthState.SignedOut) {
    eventTracker.trackAction(SignInPromptShownAction())
    emitSideEffect(DashboardSideEffect.ShowSignInPrompt)
  }
}

fun onSignInWithGoogle(activityContext: Context) {
  viewModelScope.launch {
    val result = authManager.signInWithGoogle(activityContext)
    if (result.isSuccess) {
      eventTracker.trackAction(SignInSuccessAction())
      // ...
    }
  }
}

fun onDismissSignInPrompt() {
  eventTracker.trackAction(SignInDismissedAction())
  signInPromptFlag.disable()
}
```

## Critical Files to Modify

### New Files (Create):
1. `app/src/main/kotlin/com/alorma/caducity/feature/auth/domain/AuthState.kt`
2. `app/src/main/kotlin/com/alorma/caducity/feature/auth/domain/AuthManager.kt`
3. `app/src/main/kotlin/com/alorma/caducity/feature/auth/data/FirebaseAuthManager.kt`
4. `app/src/main/kotlin/com/alorma/caducity/feature/auth/AuthModule.kt`
5. `app/src/main/kotlin/com/alorma/caducity/feature/auth/SignInPromptFlag.kt`
6. `app/src/main/kotlin/com/alorma/caducity/ui/components/UserAvatar.kt`
7. `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/account/AccountSettingsScreen.kt`

### Existing Files (Modify):
1. `gradle/libs.versions.toml` - Add Firebase Auth dependencies
2. `app/build.gradle.kts` - Add Firebase Auth dependencies
3. `app/src/main/kotlin/com/alorma/caducity/di/AppModule.kt` - Include authModule
4. `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardViewModel.kt` - Add side effects and sign-in logic
5. `app/src/main/kotlin/com/alorma/caducity/ui/screen/dashboard/DashboardScreen.kt` - Add dialog handler and avatar
6. `app/src/main/kotlin/com/alorma/caducity/ui/screen/settings/SettingsRootScreen.kt` - Add account section
7. `app/src/main/kotlin/com/alorma/caducity/feature/tracking/Actions.kt` - Add auth tracking actions
8. `app/src/main/res/values/strings.xml` - Add string resources

## Verification Steps

### Manual Testing:
1. **Fresh Install**: Install app, complete onboarding
2. **Create First Item**: Add a category and item
3. **Sign-In Prompt**: Verify dialog appears after item creation
4. **Sign In**: Click "Sign In", verify Google Sign-In flow works
5. **Avatar Check**: Verify avatar shows profile picture in dashboard top bar
6. **Sign Out**: Go to Settings → Account → Sign Out
7. **Prompt Doesn't Re-appear**: Verify prompt doesn't show again (FireAndForget)

### Build Verification:
```bash
./gradlew clean assembleDebug
./gradlew test
```

### Analytics Verification:
- Check Firebase Console for `signin_prompt_shown`, `signin_success`, `signin_dismissed` events

## Implementation Notes

### Google Sign-In Configuration:
- Web Client ID is automatically available from `google-services.json` (already configured)
- SHA-1 fingerprint must be registered in Firebase Console for sign-in to work
- Debug and release SHA-1s should both be added

### Progressive Enhancement:
This phase establishes authentication foundation. Future phases will add:
- Phase 2: Firestore sync infrastructure (upload/download)
- Phase 3: Conflict resolution
- Phase 4: Background sync with WorkManager
- Phase 5: Sync settings UI (enable/disable, manual trigger, last sync time)

### Testing Strategy:
- Unit tests for `FirebaseAuthManager` (mock FirebaseAuth)
- Unit tests for `DashboardViewModel` sign-in logic
- Integration tests for sign-in flow
- Manual testing on real device (emulator won't have Google Play Services)

## Dependencies on Existing Code

**Reuses**:
- `FireAndForget` pattern from `feature/fireandforget/`
- `AppDialogState` from `ui/components/feedback/dialog/`
- `AppScaffold` from `ui/components/`
- `BaseViewModel` pattern from `ui/base/`
- `EventTracker` from `feature/tracking/`
- Settings UI components from `ui/screen/settings/components/`
- Existing Firebase infrastructure (Analytics, Crashlytics)

**No Breaking Changes**: All changes are additive. Existing features continue working without modification.
