# Category screen (create and edit): work plan

Spec: `docs/editar-categoria/editar-categoria.md`. Captures: `docs/editar-categoria/captures/` (sources in `font-dc-html/`).
Package from the design, 21 October. Independent plan.

**Starts after `feature/tipus-unitat` is finished** (phases 3–5 included). By then «Es pot congelar» exists, so the
«Es pot congelar» row **is in** this plan (the spec says to leave it out only if the feature does not exist).

Order: this plan goes **before** `docs/afegir-editar-producte/plan.md`, because the product plan reuses the Durada sheet
(«Posa'n una» / «Canvia»).

## What changes

1. Compact screen with no scroll: tappable preview (✏️) + Name + «Opcions» list (Durada habitual ›, Es pot congelar, Descripció ›).
2. Sheet **Aspecte**: shape (6, never Flower) + icon (12), changes shown at once.
3. Sheet **Durada**: buttons with the same values as today's slider (1–3 days, 1–2 weeks, 1–3 months, Sense) + explanatory sentence.
   Fixes the bug «Sense durada» text showing with «1 setmana» selected.
4. Sheet **Descripció**: multiline, 80 characters (keep the code's limit if it already has one), counter, «Esborra» / «Fet».
5. Placeholders in normal weight and `onSurfaceVariant`.
6. Duration leaves Ajustos (row and per-category screen); Add's «Suggerida: … · Canvia» opens the category's Durada sheet;
   Category ⋮ menu › «Edita la categoria».
7. Strings ca/es/en (spec §6).

## What the code has today (to confirm when the plan starts)

- Editor in `ui/screen/category/editor/` (`CategoryEditorScreen` + ViewModel, state, side effects); quick creation in
  `ui/screen/category/quick/` (`QuickCategorySheet`, used from Add).
- Today the editor has the «Congelació» section with the switch (phase 2 of tipus-unitat).
- Per-category duration is edited in Ajustos › Dates.
- All feedback (sheets, dialogs, snackbars) goes through side effects and `AppBottomSheetState` (see `CLAUDE.md`):
  the three sheets are extension functions, not `@Composable` sheets in the screen.

## Open questions (to decide with Bernat before the task list)

- Does the Aspecte sheet apply on drag-dismiss only, or also with «Fet»? (Spec: both apply; no cancel.)
- Name of the pantry in the «Com es veu al Rebost» text: use the name the app already uses in each language.
- Does the description limit already exist in code? If yes, keep it.

## Decisions taken with Bernat (21 October)

- `QuickCategorySheet` (create from Add) **stays as it is for now**; only the editor screen changes. The product plan's «+ Nova»
  keeps opening what exists.

## Tasks

- [x] a. Main screen: preview with ✏️, Name, «Opcions» list (Durada, Es pot congelar, Descripció) as rows; «Desa» in the top bar. (620a93fb)
  Validation: previews + screenshot test; emulator, create and edit, no scroll on a 6" screen.
- [x] b. Sheet Aspecte (shape ×6 without Flower, icon ×12, instant preview). Validation: emulator; shape/icon persist after save. (1aa66ab8)
- [x] c. Sheet Durada with the current values + sentence; fix the «Sense durada» bug. Unit test for the value → text mapping. (ffd2b52c, 9c8fdce7)
  Validation: «1 setmana» selected shows the right sentence; saved value matches the old slider's.
- [x] d. Sheet Descripció (80 chars, counter, «Esborra» / «Fet», `imePadding`). Validation: emulator with keyboard open. (0d835c89)
- [x] e. Remove duration from Ajustos (row, screen, «Torna a les durades suggerides») keeping the stored values; (2bc5d7fd)
  «Suggerida … · Canvia» in Add opens the Durada sheet and recalculates the date; ⋮ › «Edita la categoria».
  Validation: stored durations survive; Add recalculates after changing; tracking actions added (`feature/tracking/Actions.kt`).
- [x] f. Strings ca/es/en (go with each task that uses them) and placeholder style. Validation: `ktlintCheck`, unit tests. (f1073892)

Release notes: Bernat decided (23 October) that none are needed for this plan.
