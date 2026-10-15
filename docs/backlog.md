# Backlog

What is left to do in Caducity.

## Features

- **Fingerprint for actions, free for reading** (configurable): biometric prompt before actions that change data (consume, freeze, reschedule, edit, move, delete); browsing stays open. A setting turns it on or off.
- **Haptics**: already in the item actions sheet, threshold slider, Add and onboarding. Review what is still missing (for example delete and other confirmations).
- **Product cost**: optional price on a product, entered per unit or as a total split across the N units created. Lets the app show how much is spent on food and how much is thrown away (expired or discarded items).

## Transforma loose ends

Transforma is on for everyone (the `transform_enabled` flag is gone). What is left:

- **Release notes** for the version that ships Transforma (`.claude/rules/release-notes.md`): the update test and the user-visible bullets are still to decide.

## Quality

- **TalkBack on a real device**: roles and `liveRegion` are in place but it was never tried with TalkBack.
- **Review the ca/es translations**: not reviewed by a native reader.
- **Large font on more screens**: the scale is capped in some places only.
- **Clean install and upgrade walk-through with the release build**: clean install, and update from 0.9 (Room v2 → v3 → v4 and `PreferencesMigrator`) with no crashes, and from the last release (v3 → v4, the Transforma columns and table). Take a backup before.
- **Real Play screenshots**: the current ones are design mockups. Google Play expects screenshots of the real app.
- **Legal text**: the privacy policy and the notice (`privacy_policy.md`, `terms_and_conditions.md`, the onboarding notice, the pages on converses.app) have not been reviewed by a lawyer. If the text changes, bump `DisclaimerPreferences.CURRENT_VERSION` so everyone accepts it again.

## Cleanup

- **Quantity limit on Add**: 20 per entry is a hypothesis, never confirmed.
- **Flaky unit tests** (14 October): `TransformViewModelTest` fails one different case almost every full run (timeouts waiting for state, e.g. «adding an ingredient to a free entry brings back the default date»), and `PantryViewModelTest` search once returned the previous query's results; both pass when re-run. Look at how they wait for state (`withTimeout` + `first`) and at races between `onQueryChange` and the results.

## Fora d'abast de moment

El codi d'aquests punts s'ha tret; es tornaran a implementar quan calgui (el codi anterior és a l'historial de git).

- **Valoració a Google Play**: demanar una ressenya amb l'API In-App Review (`com.google.android.play:review`), en un bon moment (per exemple després d'unes quantes unitats consumides), mai a la primera obertura ni després d'un error. Google limita cada quant es mostra, així que pot no sortir cada vegada. Cal decidir el moment i les normes abans de tornar-la.
- **Widget (Glance)**: mostra els comptadors d'«Aviat» i de caducats i els dos primers productes, amb el marge global d'«Aviat». `glance-appwidget` + `glance-material3`; `updateAll()` després de cada escriptura i des del worker diari; botó **Afegeix** (obre `add?source=widget`) i el cos obre l'Inici; estat buit. Riscos: R8 amb Glance, refresc a mitjanit, mides del widget. Ha de seguir la paleta de colors escollida (`ThemePreferences.colorPalette`; Dinàmic: el color del sistema). Tests: `WidgetStateMapperTest`.
- **Consell del widget**: la targeta «Posa el widget…» de l'Inici buit (`WidgetTipCard`, `WidgetTipFlag`). Depèn del widget: es torna a activar quan aquest surti.
- **Dreceres de la pantalla d'inici**: Afegeix producte, Què caduca i Caducats (`StatusList(EXPIRED)`). Test: `ShortcutIntentTest`.
- **Tauleta, plegable (mode llibre) i Wear OS**: avui el disseny és el de mòbil centrat a 600 dp (Pantry fins a uns 840 dp). Un mode tauleta real necessita disseny previ: `ListDetailPaneScaffold` amb Pantry | Categoria i Inici | calendari de mes. Wear OS només si arriba a existir un Tile.
- **Mode tanda («Desa i un altre»)**: afegir diversos productes seguits sense tancar la pantalla d'Afegeix. Ara només hi ha un «Desa» que tanca.
- **Estadístiques dels ítems transformats**: les dades ja es guarden (`transformedDate`, `transformationId`) però cap pantalla les llegeix. Té més sentit juntament amb el cost del producte («has aprofitat X€ cuinant»).

## Out of scope for now

AI, voice, image recognition, barcode, OCR of dates or receipts, camera; shopping list and cloud sync; drag to reorder categories (`sortOrder` is stored, no UI); advanced global search; per-product notifications, separate channels per status, notifications for expired items.
