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

- `QuickCategorySheet` (create from Add): stays as it is, or is replaced by the new screen? The product plan's «+ Nova» opens
  the full «Nova categoria» screen.
- Does the Aspecte sheet apply on drag-dismiss only, or also with «Fet»? (Spec: both apply; no cancel.)
- Name of the pantry in the «Com es veu al Rebost» text: use the name the app already uses in each language.
- Does the description limit already exist in code? If yes, keep it.

## Tasks

- [ ] a. Main screen: preview with ✏️, Name, «Opcions» list (Durada, Es pot congelar, Descripció) as rows; «Desa» in the top bar.
  Validation: previews + screenshot test; emulator, create and edit, no scroll on a 6" screen.
- [ ] b. Sheet Aspecte (shape ×6 without Flower, icon ×12, instant preview). Validation: emulator; shape/icon persist after save.
- [ ] c. Sheet Durada with the current values + sentence; fix the «Sense durada» bug. Unit test for the value → text mapping.
  Validation: «1 setmana» selected shows the right sentence; saved value matches the old slider's.
- [ ] d. Sheet Descripció (80 chars, counter, «Esborra» / «Fet», `imePadding`). Validation: emulator with keyboard open.
- [ ] e. Remove duration from Ajustos (row, screen, «Torna a les durades suggerides») keeping the stored values;
  «Suggerida … · Canvia» in Add opens the Durada sheet and recalculates the date; ⋮ › «Edita la categoria».
  Validation: stored durations survive; Add recalculates after changing; tracking actions added (`feature/tracking/Actions.kt`).
- [ ] f. Strings ca/es/en (go with each task that uses them) and placeholder style. Validation: `ktlintCheck`, unit tests.

Release notes: probably one bullet at the end («Pantalla de categoria més senzilla; la durada habitual ara es canvia des de
cada categoria»). Ask at the end of the plan (`.claude/rules/release-notes.md`).
