# Backlog

What is left after the «Caducity» redesign (1.0.0). The plan and the design package that came with it were removed from the repo; they are in the git history (`git log -- docs/redisseny`).

The plans, specs and design files of the three packages that followed the redesign (multi-unit actions, Transforma, Home without «soon» items) were removed once Transforma shipped. They are in the git history, at commit `0af1f05` (`git show 0af1f05:docs/<folder>/spec.md`, with the folders `01-diverses-unitats`, `02-transforma` and `03-inici-sense-aviat`).

## Features

- **Fingerprint for actions, free for reading** (configurable): biometric prompt before actions that change data (consume, freeze, reschedule, edit, move, delete); browsing stays open. A setting turns it on or off.
- **Haptics**: haptic feedback on key interactions (for example consume, freeze, delete, confirmations).
- **Product cost**: optional price on a product, entered per unit or as a total split across the N units created. Lets the app show how much is spent on food and how much is thrown away (expired or discarded items).

## Transforma loose ends

Transforma is on for everyone (the `transform_enabled` flag is gone). What is left:

- **Expired ingredients**: the picker does not list them. Proposal: allow them, with «today» as the default date (`TODO(transforma-open-2)`).
- **Frozen result without a date**: which `remainingDays` to store when thawing; proposal: the same value as «Add frozen» (`TODO(transforma-open-4)`).
- **Stats of transformed items**: the data is stored (`transformedDate`, `transformationId`), there is no screen.
- **Row details**: whether the quantity chip folds by itself after a few seconds, `snap` on the rows, a limit per row (50?), and whether the neighbour card's edge is tappable and shows «3 of 6».
- **Screenshot tests** for the onboarding step and the novelty modal: `Reveal` starts transparent, so a capture needs a no-animation mode first. The modal holds back for deep links, widgets and notifications, and while another screen is on top; a dialog or selection open over Home is only covered by «Home with nothing on top».
- **Release notes** for the version that ships Transforma (`.claude/rules/release-notes.md`): the update test and the user-visible bullets are still to decide.

## Quality

- **TalkBack on a real device**: roles and `liveRegion` are in place but it was never tried with TalkBack.
- **Review the ca/es translations**: not reviewed by a native reader.
- **Large font on more screens**: the scale is capped in some places only.
- **Clean install and upgrade walk-through with the release build**: clean install, and update from 0.9 (Room v2 → v3 → v4 and `PreferencesMigrator`) with no crashes, and from the last release (v3 → v4, the Transforma columns and table). Take a backup before.
- **Baseline profile**: the app has none (no `baselineprofile` module, no `profileinstaller`). A Perfetto trace of a cold start on the debug build (14 October) showed ~0.6 s in `bindApplication` and ~1.9 s of Compose `measure`/`recompose` over the first two frames, with 60+ skipped frames. A baseline profile (and `profileinstaller`) is the standard fix; measure the release build before and after.
- **Real Play screenshots**: the current ones are design mockups. Google Play expects screenshots of the real app.
- **Legal text**: the privacy policy and the notice (`privacy_policy.md`, `terms_and_conditions.md`, the onboarding notice, the pages on converses.app) have not been reviewed by a lawyer. If the text changes, bump `DisclaimerPreferences.CURRENT_VERSION` so everyone accepts it again.

## Cleanup

- **`// TEMP` leftovers**: adapters for old screens that may be dead now (`StatusBadge`, `ExpirationDefaults`, `ItemRoomMapper`, `UnfreezeItemUseCase`, `ExpirationThresholds`). Check with `git grep` and remove the ones with no consumer.
- **Quantity limit on Add**: 20 per entry is a hypothesis (P22), never confirmed.

## Fora d'abast de moment

Aquests punts estan fora del paquet de revisió de coherència: el codi s'ha tret i es tornaran a implementar quan calgui (el codi anterior és a l'historial de git). El pla i les decisions són a [`04-revisio-coherencia/plan.md`](04-revisio-coherencia/plan.md).

- **Valoració a Google Play**: demanar una ressenya amb l'API In-App Review (`com.google.android.play:review`), en un bon moment (per exemple després d'unes quantes unitats consumides), mai a la primera obertura ni després d'un error. Google limita cada quant es mostra, així que pot no sortir cada vegada. Cal decidir el moment i les normes abans de tornar-la.
- **Widget (Glance)**: mostra els comptadors d'«Aviat» i de caducats i els dos primers productes, amb el marge global d'«Aviat». `glance-appwidget` + `glance-material3`; `updateAll()` després de cada escriptura i des del worker diari; botó **Afegeix** (obre `add?source=widget`) i el cos obre l'Inici; estat buit. Riscos: R8 amb Glance, refresc a mitjanit, mides del widget. Ha de seguir la paleta de colors escollida (`ThemePreferences.colorPalette`; Dinàmic: el color del sistema). Tests: `WidgetStateMapperTest`.
- **Consell del widget**: la targeta «Posa el widget…» de l'Inici buit (`WidgetTipCard`, `WidgetTipFlag`). Depèn del widget: es torna a activar quan aquest surti.
- **Dreceres de la pantalla d'inici**: Afegeix producte, Què caduca i Caducats (`StatusList(EXPIRED)`). Test: `ShortcutIntentTest`.
- **Tauleta, plegable (mode llibre) i Wear OS**: avui el disseny és el de mòbil centrat a 600 dp (Pantry fins a uns 840 dp). Un mode tauleta real necessita disseny previ: `ListDetailPaneScaffold` amb Pantry | Categoria i Inici | calendari de mes. Wear OS només si arriba a existir un Tile.
- **Mode tanda («Desa i un altre»)**: afegir diversos productes seguits sense tancar la pantalla d'Afegeix. Ara només hi ha un «Desa» que tanca.

## Out of scope for now

AI, voice, image recognition, barcode, OCR of dates or receipts, camera; shopping list and cloud sync; drag to reorder categories (`sortOrder` is stored, no UI); advanced global search; per-product notifications, separate channels per status, notifications for expired items.
