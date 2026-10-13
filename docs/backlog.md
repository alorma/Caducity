# Backlog

What is left after the «Caducity» redesign (1.0.0). The plan and the design package that came with it were removed from the repo; they are in the git history (`git log -- docs/redisseny`).

The plans, specs and design files of the three packages that followed the redesign (multi-unit actions, Transforma, Home without «soon» items) were removed once Transforma shipped. They are in the git history, at commit `0af1f05` (`git show 0af1f05:docs/<folder>/spec.md`, with the folders `01-diverses-unitats`, `02-transforma` and `03-inici-sense-aviat`).

## Features

- **Consume and freeze several units** (spec in `01-diverses-unitats`, not started): the ✓/❄ tap acts on one unit and a snackbar offers «Més…» and «Desfés»; long press opens the quantity dialog straight away. Needs `ConsumeUnitsUseCase`/`FreezeUnitsUseCase` (take the ids of the row's units and `n`, soonest first), the existing `QuantitySelectorDialog` generalised per variant, and `multi_*` strings. The undo restores the union of the snapshots.
- **Home with no «soon» items** (spec in `03-inici-sense-aviat`, not started): instead of «No caduca res aviat» show the next expiries (first 3 days with products, at most 8 rows, fresh only, «+N més» when a day is cut) and a text button «Veure el rebost». Today `HomeItem.NothingSoon` is still the old message.
- **Widget (Glance)**: shows the soon and expired counts and the next two items, with each category's own «soon» margin. `glance-appwidget` + `glance-material3`; `updateAll()` after every write and from the daily worker; **Add** button opens `add?source=widget`, the body opens Home; empty state. Risks: R8 with Glance, midnight refresh, widget sizes. Tests: `WidgetStateMapperTest`. It must follow the chosen color palette (`ThemePreferences.colorPalette`; Dynamic: the system color). Same for a Wear OS Tile if one ever exists.
- **Static shortcuts**: Add product, What expires, Expired (`StatusList(EXPIRED)`). Test: `ShortcutIntentTest`.
- **`WidgetTipCard` on Home**: depends on the widget. Turn it on when the widget ships (`HomeViewModel`, `// TEMP(fase 12)`).
- **Tablet and large screens**: today the phone layout is centered with a max width (600 dp; Pantry up to ~840 dp). A real design is needed first: `ListDetailPaneScaffold` with Pantry | Category and Home | month calendar.
- **Fingerprint for actions, free for reading** (configurable): biometric prompt before actions that change data (consume, freeze, reschedule, edit, move, delete); browsing stays open. A setting turns it on or off.
- **Haptics**: haptic feedback on key interactions (for example consume, freeze, delete, confirmations).
- **Product cost**: optional price on a product, entered per unit or as a total split across the N units created. Lets the app show how much is spent on food and how much is thrown away (expired or discarded items).
- **In-app review**: ask for a Play Store review with the Play In-App Review API (`com.google.android.play:review`), at a good moment (for example after a few items consumed), never on first launch or after an error. Google limits how often the prompt shows, so it may not appear every time.

## Transforma loose ends

Transforma is on for everyone (the `transform_enabled` flag is gone). What is left:

- **Expired ingredients**: the picker does not list them. Proposal: allow them, with «today» as the default date (`TODO(transforma-open-2)`).
- **Frozen result without a date**: which `remainingDays` to store when thawing; proposal: the same value as «Add frozen» (`TODO(transforma-open-4)`).
- **Stats of transformed items**: the data is stored (`transformedDate`, `transformationId`), there is no screen.
- **Row details**: whether the quantity chip folds by itself after a few seconds, `snap` on the rows, a limit per row (50?), and whether the neighbour card's edge is tappable and shows «3 of 6».
- **Subtitle on the «Transforma» entry of the «+ ⌃» menu** («Apunta què has cuinat»).
- **Screenshot tests** for the onboarding step and the novelty modal: `Reveal` starts transparent, so a capture needs a no-animation mode first. The modal holds back for deep links, widgets and notifications, and while another screen is on top; a dialog or selection open over Home is only covered by «Home with nothing on top».
- **Release notes** for the version that ships Transforma (`.claude/rules/release-notes.md`): the update test and the user-visible bullets are still to decide.

## Quality

- **TalkBack on a real device**: roles and `liveRegion` are in place but it was never tried with TalkBack.
- **Review the ca/es translations**: not reviewed by a native reader.
- **Large font on more screens**: the scale is capped in some places only.
- **Clean install and upgrade walk-through with the release build**: clean install, and update from 0.9 (Room v2 → v3 → v4 and `PreferencesMigrator`) with no crashes, and from the last release (v3 → v4, the Transforma columns and table). Take a backup before.
- **Real Play screenshots**: the current ones are design mockups. Google Play expects screenshots of the real app.
- **Legal text**: the privacy policy and the notice (`privacy_policy.md`, `terms_and_conditions.md`, the onboarding notice, the pages on converses.app) have not been reviewed by a lawyer. If the text changes, bump `DisclaimerPreferences.CURRENT_VERSION` so everyone accepts it again.

## Cleanup

- **`// TEMP` leftovers**: adapters for old screens that may be dead now (`StatusBadge`, `ExpirationDefaults`, `ItemRoomMapper`, `UnfreezeItemUseCase`, `ExpirationThresholds`). Check with `git grep` and remove the ones with no consumer.
- **Quantity limit on Add**: 20 per entry is a hypothesis (P22), never confirmed.

## Out of scope for now

AI, voice, image recognition, barcode, OCR of dates or receipts, camera; shopping list and cloud sync; drag to reorder categories (`sortOrder` is stored, no UI); advanced global search; per-product notifications, separate channels per status, notifications for expired items.
