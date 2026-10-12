# Pla: consumir i congelar diverses unitats

Especificació: [`spec.md`](spec.md). Captures: `captures/MultiToc`, `MultiConsumeix`, `MultiCongela`.
**Ordre: primera de les dues** (sense migració, reutilitza el diàleg existent). Independent de [Transforma](../02-transforma/plan.md).

## Punt de partida (codi actual)

- `ui/components/bottomsheet/QuantitySelectorDialog.kt`: `QuantityDialogData`, `QuantityState`, `showQuantitySelectorDialog`, `QuantityDialogContent`. Avui només serveix per consumir packs antics des de `ItemActionsSheet.kt:472`, amb textos `quantity_*` fixos i selector `primary-container`.
- `PantryDataSource.consume/freeze/snapshots/restoreSnapshots` ja són transaccionals; `RestoreItemsUseCase` ja fa el «Desfés» amb `ItemSnapshot`.
- `ConsumeItemUseCase.consume(List<String>)` i `FreezeItemUseCase.freeze(List<String>)` ja apliquen una acció a N ítems (freeze salta els caducats i retorna els ids congelats).
- Els botons ✓/❄ de la fila ja actuen sobre **una** unitat (`StatusListViewModel`, `HomeViewModel`, `ItemActionsSheetViewModel`).
- Snackbars: `GlobalSnackbarEvents` / `AppSnackbarHost`.

## Decisions fixades

- Els casos d'ús reben **els ids de les unitats de la fila** (no `productId`): una fila d'una secció d'estat només té les seves unitats, i «les que caduquen primer» s'ha de calcular dins d'aquest conjunt. Es desvia de la signatura del prompt original (`productId, n`) a propòsit.
- No cal canvi de dades ni migració.

## Fase 1 — Casos d'ús (domini)

1. `ConsumeUnitsUseCase(itemIds, n)` i `FreezeUnitsUseCase(itemIds, n)`: ordenen per caducitat, agafen les `n` primeres, apliquen l'acció a `PantryDataSource` i retornen els `ItemSnapshot` previs (per al «Desfés»). Congelar: `frozenDate = avui`, es salten els caducats.
2. Un «gest» = el toc (1 unitat) + els extres de «Més…». El «Desfés» restaura la unió dels snapshots: ho guarda el ViewModel (llista acumulada), no el cas d'ús.
3. Tests d'unitat: ordre per caducitat; `n` > disponibles es limita; desfés d'un gest compost (toc + extres) deixa l'estat original; congelar salta caducats.

**Sortida:** tests verds, cap canvi d'UI.

## Fase 2 — Diàleg generalitzat

1. Afegir una variant `Consume`/`Freeze` (enum o sealed) a `QuantityDialogData`; el component, el selector i `QuantityState` no es dupliquen.
2. Per variant: color del selector (`primary-container` / `frozen-container` #D3E5EA, on #163F4C), pregunta, nota, overline i text del botó segons la taula de l'especificació; estats del botó («Consumeix 1» / «Consumeix els M»; «Congela'n 1» / «Congela'n totes»).
3. El màxim i el «més» de la pregunta venen de les dades (`max`, `afterTap: Boolean`). La variant actual (consum de pack antic) segueix igual.
4. Textos `multi_*` a `values`, `values-ca`, `values-es`.
5. Previews en clar i fosc de les dues variants; ampliar `QuantityStateTest`.

**Sortida:** previews coincideixen amb `MultiConsumeix` i `MultiCongela`; el flux del pack antic no canvia.

## Fase 3 — Gest i snackbar

1. Snackbar amb dues accions: «Més…» i «Desfés». Mirar si `AppSnackbarHost`/`GlobalSnackbar` admet una segona acció; si no, ampliar-lo (durada 4 s, «Més…» substitueix el temporitzador).
2. Tocar ✓/❄ en una fila amb diverses unitats: 1 unitat + snackbar «1 iogurt consumit/congelat · Més… · Desfés». Una sola unitat: res no canvia.
3. «Més…»: obre el diàleg amb `max` = unitats que queden i pregunta «més»; confirmar aplica els extres i acumula el snapshot; Cancel·la manté el toc.
4. Pulsació llarga a ✓/❄: obre el diàleg directament (valor 1, `max` = totes, sense gastar res abans).
5. Connectar a Inici, Rebost, Categoria i pantalles d'estat (a les files congelades només ✓). Gest compartit en un modificador/helper comú, no per pantalla.
6. Accions `semantics` personalitzades «Consumeix diverses» / «Congela diverses» (no depèn només de la pulsació llarga).
7. Tracking: noves accions a `feature/tracking/Actions.kt` (sense dades d'usuari; `source = tap|long_press|more`).

**Sortida:** flux complet a mà a les quatre pantalles.

## Fase 4 — Tests i acabat

- UI: la pulsació llarga obre el diàleg sense gastar cap unitat; «Més…» + Cancel·la manté el toc inicial; un sol «Desfés» restaura tot.
- Tests de captura (clar/fosc) del diàleg; regenerar referències amb l'etiqueta `update-screenshots`.
- TalkBack: l'snackbar anuncia el text i exposa «Més…»/«Desfés».
- ktlint + `./gradlew test`.

## Fora d'abast

Triar quines unitats concretes (sempre les que caduquen primer); consumir/congelar des de la barra de selecció (ja actua sobre totes les unitats).

## Estat

| Fase | Estat |
| --- | --- |
| 1 — Casos d'ús | Pendent |
| 2 — Diàleg generalitzat | Pendent |
| 3 — Gest i snackbar | Pendent |
| 4 — Tests i acabat | Pendent |

Només es passa a «Validada» quan Bernat l'aprova (`.claude/rules/phased-work.md`).
