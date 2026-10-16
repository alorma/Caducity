# Descongela: work plan

Spec: `docs/redisseny/04-especificacio/pantalles/22-descongela.md`. Captures: `docs/redisseny/02-disseny-nou/captures/`.
Branch `claude/descongelar-gwa101`. Written from a cloud session with **no Android SDK**: nothing could be compiled or run
locally; everything below was only checked by reading code. GitHub CI (ktlint, unit tests, screenshots) is the first real build.

## Decisions taken with Bernat

- The «Descongela» that already existed in the modal of a single frozen unit (date picker) **stays**, and its snackbar got
  a «Desfés». Decided with Bernat: the picker proposes today + 3 days (same as the bulk action), so the info note
  «En descongelar, … 3 dies» (`unfreeze_hint`) is shown in that modal.
- Rows and the several-units modal use the spec rule: unfrozen units get today + 3 days (`UnfreezeItemUseCase.UNFREEZE_DAYS`).
- «Descongela» on a row acts on the unit frozen the longest (`frozenSince`, tie → original date). The whole app now
  sorts an all-frozen list that way (`sortedNearestFirst`).
- TalkBack label follows the existing pattern «Descongela {name}» (no «una»: the article's gender is unknown).

## Tasks

- [x] a. Domain: `PantryDataSource.unfreezeAll`, `UnfreezeItemUseCase.unfreeze(ids)`, undo with snapshots (tests in `PantryUseCasesTest`).
- [x] b. ☀ button on frozen rows (Congelats, Category, Pantry search) + second line «Congelat el {data}»; ViewModels + tests.
- [x] c. Modal of 1 frozen unit: «Congelat el …» header, ☀ icon, no «Transforma», undo on the snackbar.
- [x] d. Modal of several frozen units: selector («Primer les congelades fa més temps»), «Consumeix N» + «Descongela N», «Més accions» without «Canvia la data» / «Transforma».
- [x] e. Strings ca/es/en (`action_unfreeze*`, `unfreeze_*`, `snack_unfrozen`, `frozen_on`, `status_action_unfreeze`, `item_actions_frozen_one/many`).
- [x] Icon `sunny` added to `:icons` (generated with `icons/scripts/generate_symbols.py` data, file `Sunny.kt`).

## Pending on the PC (needs compiler / emulator)

- [ ] Run `./gradlew ktlintCheck testDebugUnitTest` and fix anything CI did not catch.
- [ ] Review the screenshot references (`update-screenshots` label): `itemActionsFrozenOneUnit`, `itemActionsFrozenSeveralUnits`, `statusFrozen` against the captures.
- [ ] Look at it on the emulator: the ☀ row button, the header date format («4 oct»), the Congelats list.
- [ ] Decide what to do with the info note in the single-unit modal (see above) and whether the picker should propose today + 3.
- [ ] Release notes: «Ara pots descongelar productes» (ask; not edited).
