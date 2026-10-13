# Backlog

What is left after the «Caducity» redesign (1.0.0). The plan and the design package that came with it were removed from the repo; they are in the git history (`git log -- docs/redisseny`).

## Features

- **Widget (Glance)**: shows the soon and expired counts and the next two items, with each category's own «soon» margin. `glance-appwidget` + `glance-material3`; `updateAll()` after every write and from the daily worker; **Add** button opens `add?source=widget`, the body opens Home; empty state. Risks: R8 with Glance, midnight refresh, widget sizes. Tests: `WidgetStateMapperTest`. It must follow the chosen color palette (`ThemePreferences.colorPalette`; Dynamic: the system color), see [`03-paletes`](03-paletes/plan.md). Same for a Wear OS Tile if one ever exists.
- **Static shortcuts**: Add product, What expires, Expired (`StatusList(EXPIRED)`). Test: `ShortcutIntentTest`.
- **`WidgetTipCard` on Home**: depends on the widget. Turn it on when the widget ships (`HomeViewModel`, `// TEMP(fase 12)`).
- **Tablet and large screens**: today the phone layout is centered with a max width (600 dp; Pantry up to ~840 dp). A real design is needed first: `ListDetailPaneScaffold` with Pantry | Category and Home | month calendar.
- **Fingerprint for actions, free for reading** (configurable): biometric prompt before actions that change data (consume, freeze, reschedule, edit, move, delete); browsing stays open. A setting turns it on or off.
- **Haptics**: haptic feedback on key interactions (for example consume, freeze, delete, confirmations).
- **Product cost**: optional price on a product, entered per unit or as a total split across the N units created. Lets the app show how much is spent on food and how much is thrown away (expired or discarded items).
- **In-app review**: ask for a Play Store review with the Play In-App Review API (`com.google.android.play:review`), at a good moment (for example after a few items consumed), never on first launch or after an error. Google limits how often the prompt shows, so it may not appear every time.

## Quality

- **TalkBack on a real device**: roles and `liveRegion` are in place but it was never tried with TalkBack.
- **Review the ca/es translations**: not reviewed by a native reader.
- **Large font on more screens**: the scale is capped in some places only.
- **Clean install and upgrade walk-through with the release build**: clean install, and update from 0.9 (Room v2 → v3 and `PreferencesMigrator`) with no crashes. Take a backup before.
- **Real Play screenshots**: the current ones are design mockups. Google Play expects screenshots of the real app.
- **Legal text**: the privacy policy and the notice (`privacy_policy.md`, `terms_and_conditions.md`, the onboarding notice, the pages on converses.app) have not been reviewed by a lawyer. If the text changes, bump `DisclaimerPreferences.CURRENT_VERSION` so everyone accepts it again.

## Cleanup

- **`// TEMP` leftovers**: adapters for old screens that may be dead now (`StatusBadge`, `ExpirationDefaults`, `ItemRoomMapper`, `UnfreezeItemUseCase`, `ExpirationThresholds`). Check with `git grep` and remove the ones with no consumer.
- **Quantity limit on Add**: 20 per entry is a hypothesis (P22), never confirmed.

## Out of scope for now

AI, voice, image recognition, barcode, OCR of dates or receipts, camera; shopping list and cloud sync; drag to reorder categories (`sortOrder` is stored, no UI); advanced global search; per-product notifications, separate channels per status, notifications for expired items.
