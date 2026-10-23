# Optional account and cloud sync (Firebase): work plan

Spec: `docs/compte-nuvol/compte-nuvol.md`. Captures: `docs/compte-nuvol/captures/` (sources in `font-dc-html/`).
Package from the design, 23 October. Large, independent plan: six phases, each one ends with the app stable and publishable.

## Principles that cannot be broken

- Without an account the app works exactly as today and nothing is sent anywhere (Firestore is not even initialised).
- Room is the source of truth; the UI never waits for the network.
- Only incremental reads and batched writes: it must fit the free quota, which is **shared by all users** of the project.

## Before starting (Bernat)

- **Reuse the existing Firebase project** (it already has Analytics, Crashlytics and Remote Config; `app/google-services.json`
  is git-ignored and CI writes it from the `GOOGLE_SERVICES_JSON_BASE64` secret).
- Create the Firestore database in an EU location (`eur3` or `europe-west1`). **The location cannot be changed later.**
  Use the `(default)` database: only one database per project gets the free quota.
- Enable Authentication › Google. Configure the Google Auth Platform consent screen (app name, support email, brand verification).
- Register the SHA-1 and SHA-256 of **three** keys: debug, upload key, and **Play App Signing** (Play Console › App integrity).
  Without the Play one, sign-in fails (`DEVELOPER_ERROR`) on installs from Play even though it works from Android Studio.
- Download `google-services.json` again (it now carries the web OAuth client used as `serverClientId`), update the local file
  and the CI secret.
- Web page to request account deletion (Play requirement) and update of the privacy policy and the Data safety form.

## Model review (what the code has today)

Room `AppDatabase` **v7**, `exportSchema = true`, schemas in `app/schemas/`, migration tests in `app/src/test/.../room/`
(`Migration2To3Test` … `Migration6To7Test`, Robolectric + `MigrationTestHelper`). Room 2.8.5 (`@Upsert` available).

| Room table | What it is | Spec's Firestore collection | Synced |
|---|---|---|---|
| `categories` | name, description, shape, icon, sortOrder, usualDurationDays, canFreeze | `categories` | yes |
| `products` | name + category + unitType + canFreeze override; feeds name suggestions (`getProductUsage`) | `nameHistory` | yes |
| `items` | each unit with a date (frozen, consumed, discarded, transformed, amount, level) | `products` | yes |
| `transformations` | id + createdAt | `transformations` | yes |

- **Primary keys**: every table has a `String` id, generated with `UUID.randomUUID()` / `Uuid.random()` (also in migrations,
  seeding and fake data). No autoincrement ids → **the id can be the Firestore document id; no `syncId` column needed.**
  A backup import keeps the ids from the file, so a document id must be validated (non-empty, no `/`).
- **Foreign keys**: `items.categoryId` and `products.categoryId` → `CASCADE`; `items.productId` → `SET_NULL`.
  `CategoryDao.updateCategory` already warns that `REPLACE` on a category deletes its items through the cascade.
- **History is never deleted**: consumed, discarded and transformed items stay as rows. The first upload is the whole history.
- **Hard deletes today**: `deleteCategory` (+ cascade), `deleteProduct`, `deleteProductIfUnused`, `deleteItem(s)`,
  `deleteTransformation`, and `clearAll*` used by the backup import.
- **Raw UPDATEs** that bypass any entity-level hook: `discardItem`, `restoreItem`, `moveItem`, `moveItemsToProduct`.
- **Undo** today reinserts or restores snapshots: `restoreDeleted`, `restoreSnapshots`, `undoTransform`, `restore`.
- Almost every write goes through `RoomPantryDataSource` (inside `withTransaction`); also `RoomCategoryDataSource`,
  `RoomProductDataSource`, `RoomItemDataSource`, `RoomBackupDataSource`. Time comes from `AppClock`.
- **Firebase today**: BoM 35.0.2, Crashlytics, Analytics, Remote Config (`config/remoteconfig/`). Missing: Auth, Firestore,
  Credential Manager / googleid. WorkManager + `koin-androidx-workmanager` already used (`ExpirationCheckWorker`).
- **No watch or tablet-specific module** (`settings.gradle.kts`: `:app`, `:icons`, `:baselineprofile`).
- Settings: `SettingsScreen` with `StyledSettingsGroup`s; the Data group has the backup. Onboarding privacy step:
  `ui/screen/onboarding/steps/PrivacyStep.kt`, strings `ob_privacy_*` («Les dades no surten del dispositiu»).
- Backup format `BackupData` v6 (`MIN_SUPPORTED_VERSION = 4`), used again for the automatic copy before «Fes servir només el compte».

## Firebase facts this plan relies on (to confirm when each phase starts)

- **Free quota (per project, shared by every user), daily reset at midnight Pacific**: 50,000 reads, 20,000 writes,
  20,000 deletes, 1 GiB stored, 10 GiB/month egress. When exhausted, requests fail until the reset (no charge, no
  degradation: errors). A Remote Config kill switch and graceful failure are therefore mandatory.
- **Listeners**: a listener re-attached after more than 30 minutes offline is billed as a new query; without the local
  persistence cache, every reconnect is billed as a new query. Deleted documents leaving a result set are not billed.
- **Server timestamps**: `FieldValue.serverTimestamp()` is set by the server at commit; offline it is null/estimated locally.
- **Batches**: a `WriteBatch` holds at most 500 operations.
- **Auth**: `FirebaseUser.delete()` and some sensitive actions throw `FirebaseAuthRecentLoginRequiredException`; fix with
  `reauthenticate()` using a fresh Google credential.
- **Sign in with Google**: Credential Manager (`androidx.credentials:credentials`,
  `credentials-play-services-auth`, `com.google.android.libraries.identity.googleid:googleid`).
  Button → `GetSignInWithGoogleOption(serverClientId)`; silent/bottom sheet → `GetGoogleIdOption`
  (`setFilterByAuthorizedAccounts`, `setAutoSelectEnabled`, nonce). On sign out: `clearCredentialState()`.
  Then `GoogleAuthProvider.getCredential(idToken, null)` → `signInWithCredential`. Button follows Google's branding guidelines.
- **Play**: apps with account creation must allow deleting the account and its data in the app and through a web link.
- **No Cloud Functions** (they need the paid Blaze plan): account data deletion is done from the client.

## Spec changes to decide with Bernat (before phase 1)

1. **No `syncId`**: the existing UUID `id` is the document id. (Spec: new `syncId` column.)
2. **Pull cursor on a server timestamp.** Each document gets `serverUpdatedAt = serverTimestamp()` and pulls query
   `serverUpdatedAt > lastPulledAt`. LWW keeps comparing the client `updatedAt`. Reason: with the spec's client-clock cursor,
   a device whose clock is behind writes documents the others never pull. Small overlap on the cursor (re-read the last
   few seconds; applying a document twice is harmless).
3. **One-shot pulls instead of permanent listeners**: on app foreground (throttled), after each upload, the «Ara» button,
   and the periodic worker. Cheaper and simpler; the cost is that a change on another device shows when the app comes to the
   foreground, not live. (Spec: snapshot listeners while in foreground.) Alternative: a listener only on `items`.
4. **Collection names** follow Room (`categories`, `products`, `items`, `transformations`) to avoid «products» meaning two
   things. (Spec: `products` / `nameHistory`.)
5. **Sync history** (consumed, discarded, transformed items)? Yes keeps statistics and the transform links intact; no saves
   quota. Proposal: yes.
6. **«Mòbil, tauleta i rellotge»**: there is no watch app. Proposal: «Tots els teus dispositius» / «Mòbil i tauleta».
7. **Backup import while signed in**: today it clears everything (hard delete) and inserts. Proposal: with a session, existing
   rows are soft-deleted (tombstones uploaded) and the imported ones marked dirty; without a session, as today.
8. **Default categories merge by normalised name** (case and accents ignored). Two devices in different languages will not
   match: accepted for v1?
9. **Remote tombstones are never purged** in v1 (tiny); the local purge after 30 days only removes rows already uploaded.
   Otherwise a device offline for more than 30 days could bring deleted products back.
10. **Firestore local cache**: memory only (`MemoryCacheSettings`), since Room already persists everything.
11. **App Check (Play Integrity)** to stop someone burning the shared quota with the public API key: in v1 or later?

## Phases and draft tasks

Each phase's list is proposed again and approved at the start of the phase (`.claude/rules/phased-work.md`): one phase per
session, one commit per validated task.

### Phase 1: local model ready to sync (no Firebase)

Not user-visible. The app must behave exactly as before.

- [ ] 1.1 Migration v7 → v8: `updatedAt INTEGER NOT NULL` (backfill: `createdAt` when present, else migration time),
  `deletedAt INTEGER` (null), `dirty INTEGER NOT NULL DEFAULT 1` on the four tables; `sync_state` table
  (`uid`, `collection`, `lastPulledAt`) keyed per account; indices on `deletedAt` / `dirty` where queries need them.
  Validation: `Migration7To8Test` from the v7 schema (rows kept, defaults right, FKs intact), schema 8.json exported.
- [ ] 1.2 Soft delete: every hard delete becomes `deletedAt = now, updatedAt = now, dirty = 1`.
  `deleteCategory` soft-deletes its products and items in the same transaction (the cascade no longer fires).
  Validation: DAO/data source unit tests per delete path, including the category with children.
- [ ] 1.3 Filter deleted rows in every read: `deletedAt IS NULL` in all queries; the joins in `observeActiveItemRows` and
  `getProductUsage` filter `p` and `c` too; `CategoryWithItemsRoomEntity` (`@Relation` cannot filter) is filtered in the
  mapper or with a view (check that Room accepts it). Validation: one unit test per query with a deleted row; the export
  excludes deleted rows.
- [ ] 1.4 Every write sets `updatedAt` (from `AppClock`) and `dirty = 1`, including the raw UPDATEs. Marking lives in one
  place (data sources), not scattered in ViewModels. Validation: unit test per write path (consume, freeze, unfreeze,
  reschedule, move, edit, transform, amount/level, category/product edits).
- [ ] 1.5 Undo paths: `restoreDeleted`, `restoreSnapshots`, `undoTransform`, `restore` clear `deletedAt` and bump
  `updatedAt` instead of reinserting. Validation: existing undo tests pass + new ones on tombstones.
- [ ] 1.6 Local purge of rows with `deletedAt` older than 30 days **and** `dirty = 0` (nothing is ever non-dirty yet, so it
  is inert until phase 4), run from the existing daily worker. Validation: unit test.
- [ ] 1.7 Walk-through on the emulator with a database upgraded from v7 (`/plant-seed`): add, consume, delete + undo,
  delete category, transform, backup export/import. Validation: emulator + full unit tests + `ktlintCheck`.

Risks: missing a query (deleted rows reappear), missing a write path (changes never upload), the cascade disappearing.

### Phase 2: Firebase and login (no sync)

- [ ] 2.1 Dependencies in the catalog (`firebase-auth`, `firebase-firestore`, credentials, googleid), Koin
  `accountModule`; `AccountRepository` exposing `Flow<AccountState>` (SignedOut / SignedIn(uid, name, email, photoUrl)).
  Firestore is not touched without a session. Validation: release build with R8 (CI), app unchanged when signed out.
- [ ] 2.2 Sign in with Credential Manager (`GetSignInWithGoogleOption`) → Firebase Auth; errors mapped (cancelled, no
  account, no network, `DEVELOPER_ERROR`). Validation: unit tests of the mapping with a fake; real sign-in on the emulator
  with a Google account.
- [ ] 2.3 Screen «Inicia sessió» (`CompteLogin`): route, ViewModel, official Google button, «Ara no», privacy text, tracking
  (`NavigationAction`s with no user data). Validation: previews + screenshot test, emulator.
- [ ] 2.4 Ajustos: Sunny card when signed out, row with avatar/name when signed in (status still static). Validation:
  screenshot tests of both states.
- [ ] 2.5 Sign out (temporary simple version, `clearCredentialState()` + `signOut()`); data stays. Validation: emulator.
- [ ] 2.6 Remote Config flag `account_enabled` (off by default) hiding the card; kill switch for later phases.
  Validation: debug runner toggles it.

### Phase 3: first sync

- [ ] 3.1 `FirestoreMapper` per entity with explicit `Map<String, Any?>` (no `toObject` reflection, R8-safe) +
  `updatedAt`, `serverUpdatedAt`, `deletedAt`, `updatedBy` (installation id). Validation: round-trip unit tests.
- [ ] 3.2 Remote-apply DAO methods that **do not** mark dirty and use `@Upsert`/update, never `REPLACE` (cascade/SET_NULL).
  Validation: unit test that applying a category keeps its items.
- [ ] 3.3 First-sign-in decision: count local (non-deleted) and remote; account empty → upload; phone empty → download;
  both → sheet. Validation: unit tests of the three cases.
- [ ] 3.4 Merge categories by normalised name before uploading: insert the remote id locally, move the children, hard-delete
  the old row with no tombstone. Validation: unit test (items and products point to the merged category).
- [ ] 3.5 Initial upload in batches of ≤ 500 and initial download (paged), then `sync_state` per `uid`. Validation:
  emulator with Firestore emulator or a test project; read/write counter in Logcat.
- [ ] 3.6 Sheet «El teu compte ja té dades» (`CompteFusio`) with counts; «Ajunta-ho tot» / «Fes servir només el compte»
  (automatic backup first, then local wipe **without tombstones or dirty**, then download). Validation: screenshot test,
  emulator for both options; the account data is untouched after «només el compte».

### Phase 4: continuous sync

- [ ] 4.1 Upload worker (unique `CoroutineWorker`, network constraint, debounce ~5 s, batches of ≤ 500, marks clean only
  rows whose `updatedAt` did not change meanwhile). Validation: unit tests with a fake remote; Logcat counter.
- [ ] 4.2 Pull (foreground throttled, after upload, «Ara», periodic ~6 h) with `serverUpdatedAt` cursor; LWW per document by
  `updatedAt`; tombstone wins over an older edit; a newer remote replaces a dirty local row. Validation: conflict unit tests.
- [ ] 4.3 `devices/{installationId}`: model + `lastSeen`, at most one write per day. Validation: unit test of the throttle.
- [ ] 4.4 Status for Ajustos/Compte: «Tot al dia», «Sincronitzant…», «Sense connexió · N canvis pendents»; option
  «Sincronitza amb dades mòbils» (default on; off = unmetered network). Validation: screenshot tests + emulator offline.
- [ ] 4.5 Quota protection: backoff on `RESOURCE_EXHAUSTED`, Remote Config `sync_enabled` kill switch, read/write counter
  per session in the debug screen. Validation: unit tests; estimate with Bernat's real row count.
- [ ] 4.6 End-to-end on two emulators (or emulator + phone with a test account): add, consume, delete + undo, edit
  category, offline then online. Validation: manual run with Bernat.

### Phase 5: account screen

- [ ] 5.1 Screen «Compte» (`ComptePerfil`): header, sync status + «Ara», mobile data switch, devices list, «Exporta les
  dades». Validation: screenshot tests, emulator.
- [ ] 5.2 Sign out sheet (`CompteSortir`): «Mantén-les» (local data stays, dirty and sync state cleared) / «Esborra-les del
  mòbil» (local wipe without tombstones). Validation: emulator; the account keeps its data in both cases.
- [ ] 5.3 Delete account: confirmation → reauthenticate if needed (fresh Google credential) → delete every document of
  `users/{uid}` in batches while still signed in → delete `users/{uid}` → `FirebaseUser.delete()` → no-account state keeping
  local data. Validation: emulator with a throwaway account; Firestore console empty afterwards.

### Phase 6: privacy and release

- [ ] 6.1 Firestore rules (`request.auth.uid == userId` on `users/{userId}/**`, deny everything else) in `firestore.rules`
  in the repo. Validation: rules unit tests with the emulator suite, or manual tests with the Rules Playground.
- [ ] 6.2 Onboarding privacy step and Ajustos › Privadesa texts (spec §4); bump `DisclaimerPreferences.CURRENT_VERSION` if the
  legal text changes. Validation: screenshot tests.
- [ ] 6.3 Strings ca/es/en (spec §7, go with each task that uses them; this task reviews them all). Validation: `ktlintCheck`,
  lint for missing translations.
- [ ] 6.4 Turn on `account_enabled`. Validation: release build on a real device installed from internal testing (Play App
  Signing SHA).
- Bernat: privacy policy, Data safety form, deletion web page.

## Release notes

Phases 1–3: no (not visible or not complete). Phases 4–5 when the flag is on: yes, for example «Ara pots iniciar sessió i
tenir el rebost a tots els dispositius».
