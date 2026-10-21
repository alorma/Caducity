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

- [ ] a. Data: Room v6 (`categories.canFreeze` NOT NULL DEFAULT 1, `products.canFreeze` NULL), `MIGRATION_5_6`,
  schema `6.json`, domain `Category.canFreeze` / `Product.canFreeze`, backup v5 reading v4.
- [ ] b. Domain: effective value (product ?? category) in the read models (`ProductRowModel`, item actions state) and
  use cases to set it on category / product; the freeze use case refuses when it cannot be frozen.
- [ ] c. Category editor: «Congelació» section with the switch (`NovaCategoria`).
- [ ] d. Add / Edit: «Es pot congelar» row under Quantitat (also in Edit), inherited value + override, hides «Congelat».
- [ ] e. Rows: no ❄ when it cannot be frozen (frozen rows keep ☀).
- [ ] f. Item actions modals: no «Congela», «Consumeix» takes the whole width.
- [ ] g. Transforma: no «Congela'n» for results that cannot be frozen.

Strings ca/es/en go with the task that uses them.
