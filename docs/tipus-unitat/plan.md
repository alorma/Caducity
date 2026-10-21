# Tipus d'unitat + Es pot congelar: work plan

Spec: `docs/tipus-unitat/04-especificacio/pantalles/23-tipus-unitat.md`. Captures: `docs/tipus-unitat/02-disseny-nou/captures/`
(sources in `font-dc-html/`). Package from the 17 October design. The spec mentions `20-revisio-coherencia.md`, which is
not in the repo (it lives only in the design package). No custom unit types.

Two related changes:

- **A. Unit type**: 3 parents (Unitats, Mesurable, Grapats); subtypes only change the name and the icon.
  Unitats → Unitats, Tuppers, Paquets, Llaunes · Mesurable → Grams, Mil·lilitres · Grapats → none.
- **B. Es pot congelar**: switch on the category (`canFreeze`, default true) and override on the product
  (`canFreeze: Boolean?`, null = inherit). Effective value = product ?? category.

## Phases

1. **Unit type, model + dialog** (Unitats only active). Scope in the spec §1, §2, §3 (second line), §7.
2. **Es pot congelar** (independent of phase 1). Spec §2.1. Release notes: yes («Ara pots marcar categories i productes
   que no es poden congelar»).
3. Mesurable: `TipusGrams` modal, ✓/❄ on the row, frozen. Spec §3, §3.1, §4. *(not now)*
4. Grapats: `TipusAUll` modal, ✓ lowers one level. Spec §3, §3.1, §5. *(not now)*
5. Transforma with these types (whole ingredient consumed). Spec §6. *(not now)*

## What the code has today (21 October)

- Room v5. `ProductRoomEntity(id, categoryId, name, createdAt)`; `CategoryRoomEntity` has no freezing field.
  Items can have `productId = null` (standalone, e.g. after `moveItem`).
- Add/Edit: `AddScreen` + `AddViewModel`. **Edit works on one unit** and hides `QuantityFrozenRow`: only name, category
  and date change. `UpdateItemUseCase` resolves the product by name (`findOrCreateProduct`), so renaming moves the unit.
- Backup: `BackupData.CURRENT_VERSION = 4`, `BackupProduct` / `BackupCategory` must carry the new fields.
- Flags: no feature flag left in the app. There is a `RemoteConfig` base (Firebase + `DebugRemoteConfigsScreen`) that
  `transform_enabled` used.
- Plurals «%d unitat/unitats» also in `home_unit_count`, `notification_units`, `transform_result_units`.

## Open questions (to decide with Bernat before the task list)

See the chat of 21 October; answers go here as «Decisions taken with Bernat».

## Decisions taken with Bernat (21 October)

- **Order: phase 2 («Es pot congelar») first**, then phase 1.
- Proposals accepted as asked in the chat:
  - Edit changes the product, not only the unit: a type or «Es pot congelar» set in Edit applies to every unit of the product.
  - Add with an existing product shows that product's values; changing them changes the product.
  - Standalone items (`productId = null`) are always Unitats and take `canFreeze` from the category.
  - Phase 1 has no flag: `TipusTria` hides the parent tabs while only Unitats is active; columns for amount/level go in
    the same migration; the `RemoteConfig` flag arrives with Mesurable.
  - Add without a category: the «Es pot congelar» switch is not shown. Turning it off turns «Congelat» off.
  - Transforma: the freezer is hidden only for the results that cannot be frozen.
  - Backup goes to version 5 and still reads version 4.
- Product override: stored only when it differs from the category (`null` otherwise), so changing the category later
  still applies to products that never overrode it.

## Phase 2 tasks: Es pot congelar

- [x] a. Data: Room v6 (`categories.canFreeze` NOT NULL DEFAULT 1, `products.canFreeze` NULL), `MIGRATION_5_6`, (3ec7fad3)
  schema `6.json`, domain `Category.canFreeze` / `Product.canFreeze`, backup v5 reading v4.
- [x] b. Domain: effective value (product ?? category) in the read models (`ProductRowModel`, item actions state) and (448d0369)
  use cases to set it on category / product; the freeze use case refuses when it cannot be frozen.
- [x] c. Category editor: «Congelació» section with the switch (`NovaCategoria`). (e6834080)
- [x] d. Add / Edit: «Es pot congelar» row under Quantitat (also in Edit), inherited value + override, hides «Congelat». (0e830aeb)
- [x] e. Rows: no ❄ when it cannot be frozen (frozen rows keep ☀). (88eb187c)
- [x] f. Item actions modals: no «Congela», «Consumeix» takes the whole width. (8ef45b5a)
- [x] g. Transforma: no «Congela'n» for results that cannot be frozen. (8f470a77)

Strings ca/es/en go with the task that uses them.

## How phase 2 is run (parallel)

1. Claude alone, in sequence: **a**, then **b** (migration and effective value are the base of everything).
2. In parallel, one agent per `git worktree`: **c+g**, **d**, **e+f**. Haiku sub-agents only for ca/es/en strings,
   previews / screenshot tests and unit tests of cases already defined here; never the migration, backup, use cases or
   Compose logic.
3. Claude merges the worktrees (strings.xml conflicts expected), makes one commit per task, runs `ktlintCheck` and the
   tests, and validates on the device.

## Phase 2 validation (21 October, emulator-5554, debug build)

- Update from the 1.1.6 debug build (Room v5, seed imported) to this one: same counts (6 expired, 9 soon, 20 fresh), app runs, categories freezable.
- Version 4 backup (seed) imports on the new build.
- Category editor: «Congelació» switch; off → rows of the category lose ❄, the unit sheet only offers «Consumeix» full width.
- Add / Edit: switch inherits the category («Dry goods: no»); override on Pasta brings ❄ back only for Pasta; «Congelat» hidden in Add when it cannot be frozen.
- Transforma: result in a non-freezable category has no «Congela'n» chip; switching to Dairy shows it.
- Release notes 1.1.7 (en-US, ca, es-ES) added.
- Not tried: Pixel 8a (upgrade with real data), TalkBack.

## Phase 2 closed

Approved by Bernat on 21 October. Next: phase 1 (unit type) in a new session.

## Phase 1 tasks: Unit type, model + dialog (branch `feature/tipus-unitat`, one PR)

- [x] 1. Data: Room v7 (`products.unitType` NOT NULL DEFAULT 'UNITS', `items.amount`, `items.level`), `MIGRATION_6_7`, schema `7.json`, (e181383b)
  `Migration6To7Test`, backup v6 still reading v4 and v5.
- [x] 2. Domain: `UnitType` / `UnitParent`, `Product.unitType` through the read models, `setProductUnitType` + `SetProductUnitTypeUseCase`. (b38ae049)
- [x] 3. Row texts per type («3 paquets», «1 tupper»): `ProductRow`, ingredient picker, notifications; plurals ca/es/en. (84f0ba37)
- [x] 4. `TipusTria` sheet (parent tabs hidden while only Unitats is active). Built with the subtypes of Unitats, without the subtitle (it only makes sense with the tabs). (1872b613)
  New icons: `Tag`, `TakeoutDining`, `Inventory2`.
- [x] 5. Add / Edit: «Com ho comptes?» row, existing product shows its type, Edit changes the product. (87832ded)
  Validated on emulator-5554: Add, sheet, Edit (no Quantitat) and the Home row. The item actions sheet still says «unitats»
  (its strings are not per type; spec §3 only names the rows).
- [x] 6. Seed (`.claude/skills/plant-seed/seed-backup.json`): move it to backup v6 and give some products different unit types
  (Tuppers, Paquets, Llaunes). Update the skill text if the counts change. (b43097be)
- [x] 7. Debug fakes (`domain/usecase/fakedata/`: `ProductConfig.unitType`, `FakeShowcaseDataStrategy`, `FakeTestDataStrategy`,
  `PopulateFakeDataUseCase`): products with different unit types. (d70efd81)
  `FakeShowcaseDataStrategy` (Play screenshots, «only units») is left as it is.
- [x] 8. Final validation on the emulator: update from the v6 build with data, import v4/v5 backups, check the rows.

## Phase 1 validation (21 October, emulator-5554, debug build, branch `feature/tipus-unitat`)

- The build upgraded in place from Room v6 with data (Home loads, counts kept).
- Version 4 seed imports: 6 expired, 9 soon, 20 fresh. Version 6 seed imports: Ready meals shows «1 tupper», «3 tuppers»;
  Dry goods «3 paquets», «2 paquets».
- Add: «Com ho comptes? · Unitats» above Quantitat, the sheet lists Unitats, Tuppers, Paquets, Llaunes; picking Tuppers and
  saving 2 units gives «2 tuppers · Dairy» on Home. Edit: the row shows the product's type, no Quantitat, and changing it to
  Paquets changes the whole product.
- Unit tests, migration 6→7 test, backup round trip with `unitType`/`amount`/`level`, `ktlintCheck`: green.
- Not done: screenshot references of the existing screens (three of them differ on this machine with no change of ours),
  Pixel 8a upgrade with real data, TalkBack. Pending decision: the item actions sheet still says «unitats».

## Phase 3 tasks: Mesurable (Grams, Mil·lilitres) — same branch and PR, no flag

Decisions (Bernat, 21 October): no `RemoteConfig` flag (it goes to production when the feature is done); the rest of the proposal accepted:
changing the type in Edit leaves one batch with the nearest date and the default amount (500 g / 500 mL) and warns
«Canviar el tipus reinicia la quantitat»; thawing a partial batch keeps it as its own batch (no merging with the fresh one).

- [x] 9. Domain of the amount: `amount` through the read models; adding a Mesurable creates one batch; consuming subtracts (0 removes
  the product); freezing splits the batch (the frozen part is a new row with the chosen amount); Undo restores. Unit tests. (e076d89b)
- [x] 10. Row text: «500 g», «750 mL», «1,2 kg», «1,5 L» (≥ 1000); formatter + strings ca/es/en. Tests + `ProductRow` screenshot.
- [x] 11. `TipusTria` parent tabs (Grapats inactive), Quantitat row of Mesurable in Add (− 500 g +, steps 50 g / 100 mL, chips), (f48c72d6)
  warning in Edit when the parent changes. ViewModel tests, screenshots, emulator. (4b7cd34b)
  Texts of the Mesurable rule line («Una quantitat · el ✓ et demana quant») and examples are mine: the design only shows Unitats.
- [ ] 12. `TipusGrams` modal (header, «Quant n'has fet servir?», stepper, chips, progress bar, «Consumeix N g», «Congela»,
  «Més accions»); ✓ and ❄ on the row open it. Previews, screenshot vs `TipusGrams.png`, emulator.
- [ ] 13. Frozen and expired (§3.1): frozen modal with «Consumeix N» / «Descongela N» (thaw → Aviat today + 3 days), expired only
  «Llença». Tests + emulator.
- [ ] 14. Transforma: a Mesurable ingredient is consumed whole, no «N de M» selector. ViewModel tests.
- [ ] 15. Seed and debug fakes with grams / millilitres; final validation on the emulator; release notes.
