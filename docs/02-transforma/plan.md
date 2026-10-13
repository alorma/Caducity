# Pla: Transforma (ingredients → elaboracions)

Especificació: [`spec.md`](spec.md). Captures: `captures/Transforma{Seleccio,Caldo,Pollastre,Ingredient,Buit}`, `EntradaMenu`, `EntradaAccions`.
**Canvi de disseny (2026-10-13):** nou **accés lliure** (`TransformaBuit`, vegeu «Accés lliure» a l'especificació: sense ingredients, nom + categoria + data obligatoris) i captures actualitzades. Afecta les fases 2 i 3 ja validades (vegeu «Fase 2b» i «Fase 3b») i la 5. Validació sempre al Pixel 8a, mai a l'emulador.
**Ordre: segona de les dues**, després de [diverses unitats](../01-diverses-unitats/plan.md). Independent a nivell de codi, però comparteix el patró de «Desfés» amb snapshots.

## Punt de partida i diferències amb l'especificació

L'especificació parla del model de disseny, que no coincideix del tot amb el codi:

| Especificació | Codi actual | Decisió |
|---|---|---|
| `endReason = TRANSFORMED` | No hi ha `endReason`: `ItemRoomEntity` té `consumedDate` i `discardedDate` | Afegir `transformedDate: Long?` (mateix patró) |
| `transformationId: Long` | Els ids són `String` (UUID) | `transformationId: String?` |
| Congelat = `frozenDate`, sense `expiryDate` | Congelat = `pausedDate` + `remainingDays`, `expirationDate` no nul | Reutilitzar la representació del camí «Afegeix congelat» (`addBatch(frozenRemainingDays)`); veure «Obert» |
| `TransformationEntity(id, createdAt)` | No existeix | Nova taula `transformations` |
| Widget, Wear | No existeixen (són al backlog) | Només cal que els filtres de «viu» exclouen transformats; el futur widget els heretarà |

Mètodes ja existents que es reutilitzen: `PantryDataSource.addBatch` (cerca/crea producte + N ítems), `snapshots/restoreSnapshots`, `GetNameSuggestionsUseCase`, `ShapePosition`, `CategoryShapeIcon`, `GlobalSnackbarEvents`.

## Fase 1 — Model i migració (Room v3 → v4)

1. `ItemRoomEntity`: `transformedDate: Long?`, `transformationId: String?` (ingredients) i `originTransformationId: String?` (resultats).
2. Nova entitat `TransformationRoomEntity(id, createdAt)` a `AppDatabase` (versió 4, schema `4.json`).
3. `MIGRATION_3_4` a `Migrations.kt`, només amb `ADD COLUMN` (amb `DEFAULT` si és `NOT NULL`) + `CREATE TABLE`, sense reconstruir taules. Registrar-la amb `.addMigrations(...)` a `di/PlatformModule.kt`. No tocar els JSON d'esquema existents; només commitejar `4.json`. Regla: `.claude/rules/room.md`.
4. **Excloure `transformedDate IS NOT NULL` de tots els filtres de «viu»**: les tres consultes de `ItemDao` (`consumedDate IS NULL AND discardedDate IS NULL`), `observeActiveItems`, i qualsevol consulta equivalent a `ProductDao`/`RoomItemDataSource`. Comprovar amb `git grep consumedDate`.
5. La restauració (`UPDATE items SET consumedDate = NULL, discardedDate = NULL`) també ha de netejar `transformedDate`.
6. Backup: `BackupModels` (+ export/import) inclouen els camps nous i la taula; pujar la versió del format i acceptar la antiga.
7. Revisar worker diari i notificacions: han de llegir de les consultes de «viu».
8. Tests: `Migration3To4Test` (patró de `Migration2To3Test`), test de consultes (un transformat no apareix enlloc), test de backup d'anada i tornada.

**Sortida:** l'app s'actualitza des de v3 sense pèrdua de dades i cap pantalla canvia.

- [x] Tasques 1-3 i 8 (migració): `65ebd93`, ktlint `a6cd7b5`
- [x] Tasques 4, 5, 7 (filtres de «viu» i restauració): `7e792d2`
- [x] Tasca 6 (backup, format v3): `34dea3f`
- [x] Dades falses amb ingredients transformats: `010951c`; una sola estratègia de dades falses (sense PlayStore): `51ba87b`

Validada amb l'app `.dev` al Pixel 8a: v3 → v4 amb 61 ítems, 5 categories i 27 productes intactes. Nota: `ItemDataSource.markItemAsTransformed` és provisional (només dades falses); la fase 2 el substitueix per `transform`.

## Fase 2 — Cas d'ús transaccional

1. Models de domini: `TransformIngredient(itemIds)`, `TransformResult(name, servings, categoryId, expiration, frozenCount)`.
2. `PantryDataSource.transform(ingredients, results, at)` en una sola transacció: crea la `Transformation`, crea cada resultat (producte nou o existent + un ítem per ració, `createdAt = ara`, `originTransformationId`, `frozenCount` d'ells congelats), tanca els ingredients amb `transformedDate = at` i `transformationId`. Retorna el que cal per desfer.
3. `PantryDataSource.undoTransform(...)`: elimina els resultats i restaura els ingredients (mateixos ítems, estat anterior via `ItemSnapshot`); elimina la `Transformation`.
4. `TransformItemsUseCase` amb `transform` i `undo`.
5. Les unitats gastades d'un ingredient amb `1 de N`: les que caduquen primer (ordre per caducitat).
6. Data per defecte = la de l'ingredient que caduca primer, mai allargada sola (helper pur, testable).
7. Tests d'unitat: ingredients desapareixen de les consultes de «viu»; resultats hi són; racions = ítems; congelats surten congelats; desfés deixa l'estat idèntic; ingredient amb `1 de 2` només gasta una unitat.

**Sortida:** cas d'ús verd sense UI.

- [x] Tasques 1 i 6 (models i helper de data): `33631c1`
- [x] Tasques 2 i 3 (`transform`/`undoTransform`, `ItemSnapshot` amb `transformedMillis` i `transformationId`): `f281745`
- [x] Tasques 4, 5 i 7 (tests a cada commit; `TransformItemsUseCase`; el cas d'ús rep `IngredientSpend(candidateItemIds, count)` i tria les unitats que caduquen primer): `1e34a91`
- [x] Dades falses amb `transform`, sense `markItemAsTransformed`: `87e7f78`

Decisió oberta 4 aplicada: un resultat congelat guarda els dies fins a la seva data (`TODO(transforma-open-4)`); es rebutja amb data passada.

## Fase 2b — Accés lliure al cas d'ús (nova, pendent)

1. `TransformItemsUseCase.transform` accepta **0 ingredients** (ara falla amb `ItemNotFound`); falla només si no hi ha resultats.
2. `PantryDataSource.transform` sense ingredients: **no crea `Transformation`**; els resultats són productes nous normals (sense `originTransformationId`). El desfés elimina només els resultats.
3. Els resultats sense ingredients necessiten categoria i data explícites (rebutjar-los al cas d'ús si falten; els congelats no necessiten data).
4. Tests: sense ingredients crea resultats i cap `Transformation`; desfés els elimina; categoria/data absents es rebutgen.

**Sortida:** cas d'ús verd també sense ingredients.

- [x] Tasques 1, 2 i 4 (`transform` sense ingredients, `TransformedBatch.transformationId` nul·lable, 3 tests nous): `886cb14`
- Tasca 3: el tipus ja exigeix categoria i data a `TransformResult`; l'obligatorietat sense ingredients la fa complir la UI (fase 3b). Un resultat tot congelat sense data haurà de passar una data qualsevol (per al `remainingDays`); decidir-ho a 3b.

## Fase 3 — Pantalla Transforma (sense entrades)

1. Ruta a pantalla completa a `Routes.kt` / `AppShell.kt`, amb `TransformViewModel` (`BaseViewModel<TransformNavigation, TransformNavigationSideEffect, TransformSideEffect>`) i `TransformState`.
2. `TransformScreen`: barra superior (× amb confirmació de descart, **Desa** desactivat sense nom ni ingredients); llista d'ingredients connectada (`ShapePosition`, forma de categoria 44 dp, meta, ×, selector `− 1 de N +`); «Afegeix ingredient»; mínim 1 ingredient (treure l'últim confirma i tanca).
3. Targeta de resultat simple: nom (autofocus si buit, suggeriments amb `GetNameSuggestionsUseCase`), selector de racions 1–99, xips de categoria i data (ajuda amb l'ingredient que caduca primer), fila «Congela'n» 0..N amb resum.
4. Múltiples resultats: files plegades amb xevró i xip «N congelats», expansió animada, «Afegeix un altre resultat», treure resultat mentre en quedi un, títol «Resultat · N productes», pista i nota al peu.
5. Diàlegs (descart) pel patró de `dialogState`/side effects; res d'`AlertDialog` propi.
6. Textos de l'apartat «Textos» a `values`, `values-ca`, `values-es`.
7. Previews clar/fosc: simple, múltiple, buit.

**Sortida:** la pantalla es pot obrir amb dades de prova (preview o ruta de depuració) i desa de veritat.

- [x] Tasques 1 i 5 (ruta `Transform`, `TransformViewModel`, estat, navegació i side effects; textos a cada tasca): `871d44e`
- [x] Tasca 2 (barra superior, ingredients connectats, diàleg de descart; entrada «Open Transforma» a Depuració): `2638905`
- [x] Tasca 3 (targeta de resultat simple): `0849ce6`
- [x] Tasca 4 (diversos resultats, plegats i expandibles): `334aae6`
- [x] Tasca 7 i plurals del missatge en desar («1 producte», «1 ingredient»): `ce8a30e`

Validada amb l'app `.dev` al Pixel 8a: desa de veritat i «Desfés» restaura els ingredients. «Afegeix ingredient» no fa res fins a la fase 4. Decisió oberta 2 aplicada: un ingredient caducat dona data per defecte «avui» (`TODO(transforma-open-2)`).

## Fase 3b — Pantalla en accés lliure (nova, pendent)

Ruta `Transform` amb `itemIds` buit = accés lliure.
1. Secció «Ingredients · opcional» amb estat buit («Què has cuinat?», icona de cassola, «Afegeix ingredient») segons `TransformaBuit`; textos nous a `values`, `values-ca`, `values-es`.
2. Un resultat buit amb Nom autoenfocat; xips **Categoria** i **Caducitat** buits (traç discontinu) i text d'ajuda «Sense ingredients, la categoria i la data les tries tu.»; nota al peu «Desa s'activa quan hi ha nom, categoria i data.»
3. `canSave`: amb ingredients, com ara; sense, exigeix nom + categoria + data (la data no si tots els resultats són congelats). Afegir un ingredient torna al comportament normal (data per defecte = la de l'ingredient que caduca primer).
4. Treure l'últim ingredient **no tanca** la pantalla en accés lliure (en els altres modes, sí).
5. Snackbar sense ingredients: «M productes afegits · Desfés».
6. Previews clar/fosc de l'estat buit; test d'UI/ViewModel de `canSave`.

**Sortida:** la ruta de Depuració pot obrir Transforma buit i desar sense ingredients.

- [x] Tasques 3, 4, 5 i part de la 2 (`freeEntry` a l'estat, `canSave`, data nul·la sense ingredients, xips buits, text d'ajuda, snackbar «M productes afegits», tests del ViewModel): `92b8927`
- [x] Tasques 1 i 2 (estat buit «Què has cuinat?», «Ingredients · opcional», nota al peu) i entrada de Depuració «Open Transforma (free entry)»: `b36f2be`
- Pendent: previews clar/fosc de l'estat buit ja escrites (`TransformFreeEntry*Preview`); les captures van a la fase 6.

Validada al Pixel 8a: obre buit, Desa desactivat, activat en triar categoria i data, «1 producte afegit · Desfés».

## Fase 4 — Selector d'ingredients

1. `IngredientPickerScreen` a pantalla completa: cerca, xips d'estat (Tots · Aviat · Fresc · Congelat) i de categoria, llista amb casella, meta i pastilla d'estat; inclou congelats; els ja afegits surten marcats; botó «Afegeix N ingredients».
2. Els **caducats** no es llisten (Obert 2).
3. Resultat retornat a `TransformViewModel` (fusiona sense duplicar).
4. Tests: filtres per cerca/estat/categoria; selecció prèvia.

- [x] Tasca 1 (estat, filtres i `IngredientPickerViewModel`, amb tests): `1be0e88`
- [x] Tasques 2, 3 i 4 (pantalla, ruta `IngredientPicker`, retorn a Transforma via `IngredientPickerResults`, tracking `nav_ingredient_picker`): `4478fe9`

Validada a l'emulador (el Pixel no era a l'adb): preselecció, recompte del botó i fusió a Transforma. El botó queda desactivat amb 0 seleccionats. Els filtres només estan validats amb tests; previews clar/fosc i captures, a la fase 6.

## Fase 5 — Entrades i flux final

**Flag:** totes les entrades noves (barra de selecció, full d'Accions, pulsació llarga al «+») s'han de mostrar només si `TransformFeature` (`transform_enabled`, Remote Config, apagat per defecte) és actiu. Ja protegeix l'entrada de Depuració. L'activació per als usuaris és una decisió a part.

1. **Barra de selecció** (`StatusListScreen`/`StatusListViewModel`, i Categoria si té selecció): grup connectat **Consumit · Congela · Transforma** (`TransformaSeleccio`: icona sobre text, 68 dp, Transforma `primary`, Congela en to gelat), etiqueta amb recompte per a accessibilitat. Substitueix els botons de text actuals (decisió pendent: com queda a Caducats i a Congelats, que no tenen Congela). Seleccions de diverses seccions i de diverses unitats.
2. **Full d'Accions** (`ItemActionsSheet`): fila «Transforma amb aquest» entre «Mou…» i «Esborra»; obre Transforma amb 1 unitat.
3. **Pulsació llarga al «+»** (`FloatingNavBar`/Inici): menú Transforma / Afegeix producte, scrim 45 %; el toc normal continua obrint Afegeix. **Transforma obre l'accés lliure** (`Transform(itemIds = [])`, fases 2b i 3b; han d'estar fetes abans).
4. En desar: files d'ingredients es tanquen en cascada (80 ms), torna a l'origen (la selecció es tanca), snackbar «N ingredients transformats en M productes · Desfés» (sense ingredients: «M productes afegits · Desfés»).
5. Tracking a `feature/tracking/Actions.kt` (`nav_transform`, `source = selection|long_press_fab|item_actions`; sense dades d'usuari).

## Fase 6 — Qualitat

- Test d'UI: Desa desactivat sense nom; `1 de 2` limitat a les unitats que queden; flux sencer amb desfés.
- Tests de captura clar/fosc de les pantalles (etiqueta `update-screenshots`).
- TalkBack: selectors amb «Una menys/Una més» i regió `Polite`; zones ≥ 48 dp.
- ktlint + `./gradlew test`.
- Prova d'actualització des de la release anterior (v3 → v4) abans de publicar.

## Obert (no resoldre sense decidir; deixar com a constants/`TODO`)

1. Entrada des del «+»: pulsació llarga (proposat) o pista única «Mantén premut + per cuinar».
2. Transformar ítems **caducats**: proposat sí, amb data per defecte «avui» (mentrestant, no llistats al selector).
3. Estadístiques de «transformats»: només dades; no hi ha pantalla (verificar si n'hi ha cap a l'app).
4. Resultat congelat sense data: quin `remainingDays` guardar (el model actual el necessita al descongelar). Proposta: el mateix valor que el camí d'Afegeix congelat.

## Fora d'abast

Receptes, escalat de quantitats, pes/volum, historial visible de transformacions, restes, widget/Wear (encara no existeixen).

## Estat

| Fase | Estat |
| --- | --- |
| 1 — Model i migració | Validada |
| 2 — Cas d'ús transaccional | Validada |
| 3 — Pantalla Transforma | Validada |
| 4 — Selector d'ingredients | Validada |
| 2b — Accés lliure al cas d'ús | Validada |
| 3b — Pantalla en accés lliure | Pendent |
| 5 — Entrades i flux final | Pendent (després de 2b i 3b) |
| 6 — Qualitat | Pendent |

Només es passa a «Validada» quan Bernat l'aprova (`.claude/rules/phased-work.md`).
