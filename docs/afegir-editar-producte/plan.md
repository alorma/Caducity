# Add / Edit product with options and sheets: work plan

Spec: `docs/afegir-editar-producte/afegir-editar-producte.md`. Captures: `docs/afegir-editar-producte/captures/`
(sources in `font-dc-html/`). Package from the design, 21 October.

**Starts after `feature/tipus-unitat` is finished and after `docs/editar-categoria/plan.md`.** By then these exist, so the
spec's «if it does not exist yet» fallbacks do not apply: «Es pot congelar», unit types (Unitats, Mesurable, Grapats), the new
category screen and the category Durada sheet.

## What changes

1. Main screen: «Desa» in the top bar; Name + status line (history / new product); «Opcions» list with Categoria ›, Caduca ›,
   Quantitat › and the «Ja és congelat» switch.
2. Guided flow: new name + ↵ → Categoria sheet → Caduca sheet. From history, everything is filled in.
3. Sheet **Categoria**: big chips, tap picks and closes; «+ Nova» → new category screen.
4. Sheet **Caduca**: big date + calendar, 6 shortcuts in a grid, the category's usual duration (or «Posa'n una»).
5. Sheet **Quantitat**: big − N + and subtype chips; link to the full «Com ho comptes?» sheet (`TipusTria`).
6. **Edit**: same screen, 🗑 in the bar, no «Ja és congelat»; if frozen, «Congelat el» instead of Caduca.
7. **Bug**: default categories in English and repeated icons (spec §6).
8. Strings ca/es/en (spec §7).

## What the code has today (to confirm when the plan starts)

- `ui/screen/add/`: `AddScreen`, `AddViewModel`, `CategoryChips`, `ExpiryDateField`, `QuantityFrozenRow`, `UnitTypeRow`,
  `UnitTypeSheet`, `CanFreezeCard`, `AddBottomBar`. Edit works on one unit and hides the quantity.
- Edit changes the product when the type or «Es pot congelar» change (decision of 21 October, tipus-unitat).

## Open questions (to decide with Bernat before the task list)

- **`UnitTypeRow` and `CanFreezeCard` vs the new design.** The spec folds «Com ho comptes» into the Quantitat sheet and puts
  «Es pot congelar» out of the main list. Does the new design replace both rows? Where does the product's «Es pot congelar»
  override live (it exists since tipus-unitat phase 2)?
- **Edit and the model.** The spec says «one row = one product with N units and one date; editing changes only that row». Today
  Edit works on one unit. Confirm that nothing changes in the data model.
- **Quantitat in Edit.** The spec keeps the row in Edit; today it is hidden. Is it editable (adds/removes units) or read-only?
- **Mesurable / Grapats in the Quantitat sheet.** Amount (grams) and level don't use the − N + control. What does the sheet show
  for those types? Not covered by the spec.
- **Default categories bug.** Translated names while the user has not renamed them: check how they are stored (name saved in
  the DB vs resource) before choosing the fix.
- **Which icons** for the default categories (spec: `nutrition`, `grocery`…).

## Tasks

- [ ] a. Bug: default categories translated and with their own icons. Validation: unit test + emulator in ca/es/en; a renamed
  category keeps its name. **Release notes: yes** («Les categories per defecte ja surten en el teu idioma»).
- [ ] b. Main screen: top-bar «Desa», Name, status line, «Opcions» list (68 dp rows), «Falten la categoria i la data».
  Validation: previews + screenshot tests; emulator.
- [ ] c. Sheet Categoria (tap picks and closes, «+ Nova» → category screen, picked on return). Validation: emulator.
- [ ] d. Sheet Caduca (big field, 3 × 2 shortcuts, suggested duration / «Posa'n una» → category Durada sheet). Unit test for
  the shortcut matching the current date. Validation: emulator.
- [ ] e. Sheet Quantitat (− N +, subtype chips, link to `TipusTria`). Validation: emulator with each unit type.
- [ ] f. Guided flow (↵ → Categoria → Caduca; history fills everything). Unit tests in `AddViewModelTest`. Validation: emulator.
- [ ] g. Edit mode: 🗑 with «Desfés» snackbar, «Desa» only with changes, no «Ja és congelat», «Congelat el» for frozen items.
  Validation: emulator with a frozen and a non-frozen unit.
- [ ] h. Strings ca/es/en and final review. Validation: `ktlintCheck`, unit tests, screenshot references updated.

All feedback through side effects and `AppBottomSheetState` (`CLAUDE.md`); navigation through `navigate()` with tracking actions.

Release notes: the bug yes; the rest, one bullet at the end («Afegir i editar productes és més ràpid i ordenat»). Ask at the
end of the plan (`.claude/rules/release-notes.md`).
