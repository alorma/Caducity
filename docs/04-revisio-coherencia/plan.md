# Pla: revisió de coherència (13 d'octubre; v2, 14 d'octubre)

Especificació: [`spec.md`](spec.md) (té prioritat sobre les anteriors quan es contradiuen). Captures: `captures/` (35 pantalles, només mòbil); fonts: `font-dc-html/`.
**v2 (14 d'octubre):** el paquet `caducity-revisio-coherencia-v2` substitueix `spec.md`, `captures/` i `font-dc-html/`. Noves: `NovaCategoria`, `AfegirNovaCat`, `EstatFrescos`, `EstatCongelats`; fora: `CaducatsScroll`, `TransformaSeleccio`; la resta redibuixades. A l'especificació s'hi afegeixen la selecció múltiple fora d'abast (§0), la durada a Nova categoria (§1), «Consumeix igualment» (§4), Caducats amb el botó fix a baix (§5), pestanya «Inici» i FAB de 56 dp (§6), entrades i icona de Transforma (§7), i els apartats nous §9 (noms i textos), §10 (pantalles d'estat), §11 (M3 Expressive) i §12 (dades de prova). Reconciliat amb aquest pla a les files «v2» de la taula, a D7, D9, D11-D14 i a les fases 9 i 10.
Flux: `.claude/rules/phased-work.md` (una fase per sessió, tasques aprovades, un commit per tasca validada). Validació al dispositiu que estigui actiu (adb) en aquell moment, sigui físic o emulador; el pla no en fixa cap; captures en clar i en fosc al final de cada fase.
Compilació i comprovacions (Bernat, 14 d'octubre): **no passar build, tests ni `ktlintCheck` per cada canvi petit.** Primer es fan tots els canvis de la tasca i es valida **una sola vegada al final** (`./gradlew ktlintCheck testDebugUnitTest assembleDebug`); després, un commit per tasca. Si hi ha agents delegats (Haiku), se'ls ho diu explícitament.
Agents i subagents (Bernat, 14 d'octubre): la feina mecànica (treure codi, renoms, docs) es delega a agents amb model **Haiku**; el disseny, les decisions i la revisió es queden a la sessió principal. Condicions que se'ls passen sempre al prompt:
- Llegeixen `CLAUDE.md` i la tasca del pla; fan **només** la seva tasca, res fora d'abast.
- Editen amb `Edit`/`Write`, mai scripts Python/sed/perl (`rm` només per esborrar fitxers).
- Validen **una sola vegada al final** (`./gradlew ktlintCheck testDebugUnitTest assembleDebug`, JDK 21); si falla un test intermitent no relacionat, el reexecuten i ho diuen.
- Commit únic per tasca, només si valida, amb `git add` de fitxers concrets (mirar `git status` abans i no incloure canvis d'altres tasques ni del pla), missatge curt en anglès i la línia `Co-Authored-By`. Mai push.
- No instal·len res al dispositiu ni toquen les seves dades; la validació al dispositiu la fa Bernat.
- Si la validació falla i no ho poden arreglar, no fan commit i expliquen l'error.
- Informen en poques línies: fitxers tocats, validació, hash. Sense decidir res obert: ho pregunten.
- Els agents que toquen build, `git` o els mateixos fitxers van **en sèrie**; en paral·lel només tasques sense fitxers ni Gradle compartits (p. ex. docs).
- La sessió principal comprova `git log` i `git status` després de cada agent.

## Punt de partida (codi actual) i diferències amb l'especificació

| Especificació | Codi actual | Fase |
|---|---|---|
| §0 Tauleta, plegable, Wear | Només `ResponsiveCenteredContainer` (centra a 600 dp); helpers morts a `ui/adaptive/WindowSizeClass.kt` | 0: el centrat es queda (no és un mode tauleta); fora els helpers |
| §0 Widget, dreceres, consell del widget | No hi ha widget ni dreceres. `WidgetTipCard` + `WidgetTipFlag` existeixen però no surten mai (`WIDGET_AVAILABLE = false`) | 0 (D3) |
| §0 Temes de temporada, captures de paleta | No existeixen. El selector de colors d'Ajustos ja són només mostres de color | Res a fer |
| §0 Valoració a Google Play | `feature/review/` + `play-review`; única crida a `CategoryScreen.kt:127` en esborrar una categoria | 0 (D2) |
| §0 Mode tanda, «Desa i un altre» | Implementat i visible: `AddBottomBar`, `BatchSection`, `AddItemBatchUseCase`, `RemoveBatchEntryUseCase`, `RestoreBatchUseCase`, `add_batch_*` | 0 (D1) |
| §1 Durada habitual per categoria | `categories.soonThresholdDays` (Room v4), usat per a l'estat (`ObserveItemsUseCase`), notificacions, Ajustos › «Marge per categoria», editor de categoria («Avisa'm abans»), secció «Més endavant» d'Inici, píndola «Avisa X abans» d'Aviat. No hi ha prefill de data a Afegeix | 1 (dades) i 6 (comportament) |
| §1 Identificar categories per defecte | No hi ha clau estable (UUID, nom localitzat). `MIGRATION_2_3` ja fa coincidència per nom (`CategoryDefaults.forName`) | 1: mateix criteri, nom (ca/es/en) + forma/icona |
| §2 Quantitat = unitats | Cap gram/brick/bossa. Sí que hi ha `packSize` heretat de 0.9 («Pack de N»), el diàleg de quantitat i `SplitAndConsumeItemUseCase` només per a aquests packs | 1 (D4) |
| §3 Una sola fila | Sis files diferents: `HomeItemRow`, `StatusRowItem`, `ProductGroupRow` + `UnitRow` (desplegable «Unitat N»), `DayUnitRow`, `CalendarRowItem`, `PickerRow`. `HomeRow.nearestItemId` és el primer per `createdAt`, no per caducitat | 2: `ProductRow` únic. `PickerRow` de Transforma es queda (selector amb casella) |
| §3 Accions rodones | Inici: només ✓. Aviat/Fresc: ✓ ❄. Congelat: ✓ + descongela. Caducat: canvia la data + llença. Categoria i calendari: cap | 2: vigent ✓ ❄, congelat ✓, caducat paperera. Descongela queda a la modal |
| §3 Pulsació llarga → selecció | Cap fila té pulsació llarga. Selecció només a les llistes d'estat, amb el botó «Selecciona» | No es fa (D7) |
| §4 Modal: diverses unitats | `ItemActionsSheet` sempre actua sobre un `itemId`; no hi ha `ConsumeUnits`/`FreezeUnits` ni ajudant «primer la que caduca abans» | 3 |
| §4 Caducat: «Consumit igualment» mantingut 1,5 s | No existeix; la modal d'un caducat és igual que la d'un vigent | 3 |
| §5 «Llença» | La modal fa «Esborra» = esborrat definitiu amb diàleg i sense Desfés. Les llistes d'estat ja fan «Llença» (`discardedDate`, amb Desfés). ca: ~9 «Esborra»; es: «Borra/Borrar» | 4 (D6) |
| §5 Caducats | Subtítol «Si encara són bons, canvia'n la data…»; fila amb «Canvia la data» + llença; «Llença'ls tots» ja demana confirmació i té Desfés | 4 |
| §6 Navegació | `FloatingNavBar` ja és `HorizontalFloatingToolbar`. `SplitAddButton` («+» + «⌃» amb menú de Transforma). Categoria: `ExtendedFloatingActionButton` «Afegeix» | 5 |
| §7 Transforma | Entrades: menú del botó dividit, modal («Transforma aquest producte»), selecció a les llistes d'estat, modal de novetat. Pista d'onboarding/novetat amb icona «⌃» inline (`SplitButtonHint`) | 5 (pistes) i 7 (targeta de l'Inici) |
| §8 Afegeix, Ajustos, Inici buit | Dos botons a Afegeix. Ajustos: Avisos › «Aviat» per defecte + «Marge per categoria». Inici buit + `WidgetTipCard` (mai visible) | 0 i 6 |
| Llista de l'Inici (`Main`, `IniciSenseAviat*`) | Seccions per dia de 7 dies (`HOME_SECTION_DAYS`) amb el marge de cada categoria + «Més endavant». Amb res aviat: `HomeItem.NothingSoon` «Res caduca aviat. Bona feina.» | 7 (D8) |
| v2 §0 Selecció múltiple fora | Les llistes d'estat tenen «Selecciona» (`StatusListScreen`, `StatusHeader`, `StatusListViewModel`) amb barra Consumeix / Congela / Transforma; és l'única entrada a Transforma amb diversos productes | 9 (D11) |
| v2 §1 Nova categoria amb durada | L'editor (`CategoryEditorScreen`) ja llegeix `usualDurationDays` però mostra «Avisa'm abans»; el full ràpid d'Afegeix (`QuickCategorySheet`, `AfegirNovaCat`) no porta durada, i així ha de quedar | 6 |
| v2 §4 «Consumeix igualment» | La fase 3 (en curs, `22a89b7`) usa «Consumit igualment» (`item_actions_eat_anyway`) | 3 |
| v2 §5 i §10 Pantalles d'estat | `StatusHeader` és una capçalera gran que es plega en fer scroll (72 dp plegada); a Caducats «Llença'ls tots» és un botó de la capçalera, no fix a baix | 9 |
| v2 §6 Pestanya «Inici», FAB 56 dp | La pestanya es diu «Aviat» (`nav_soon`, icona `HourglassBottom` ja correcta). `SplitAddButton` amb dos `FloatingActionButton` | 5 |
| v2 §7 Transforma | La modal ja obre `Transform(itemIds)` amb el producte. Pistes amb el text del botó «+ ⌃» (`ob_transform_hint`, `transform_intro_hint`). `soup_kitchen` ja és la icona (no hi ha `skillet`); l'onboarding la pinta en forma `CLOVER8` | 5 |
| v2 §9 Noms i textos | Tema «Rebost» (`palette_rebost`); camp «Racions» a Transforma (`transform_servings`); la categoria per defecte ja es diu «Secs i conserves» (`ob_category_pantry`) | 8 |
| v2 §11 Formes de categoria | `CategoryShape`: Làctics `COOKIE` (Cookie9Sided) ✓, Fruita i verdura `CLOVER8` (ha de ser `Flower`), Carn i peix `CLOVER` ✓, Secs i conserves `SUNNY` ✓, Fleca `PUFFY` (ha de ser `Cookie4Sided`), Plats preparats `CLOVER8` (ha de ser `Puffy`). Es desa pel nom de l'enum (`AvatarConverters`) | 10 (D13) |
| v2 §11 Capçaleres i FAB | Totes les pantalles fan servir `StyledTopAppBar` (`TopAppBar` petit); cap `LargeFlexibleTopAppBar`. Categoria: `ExtendedFloatingActionButton` | 5 i 10 |
| v2 §12 Dades de prova | `FakeTestDataStrategy` no coincideix amb les captures (dia de referència, 61 productes, 6 categories) | 8 |

## Decisions (Bernat, 13 d'octubre)

- **D1 · Mode tanda**: fora del tot (botó, `BatchSection`, casos d'ús, textos). Afegeix desa i es tanca.
- **D2 · Valoració**: fora del tot (codi, flag, dependència).
- **D3 · Consell del widget**: fora del tot (`WidgetTipCard`, `WidgetTipFlag`, `HomeItem.WidgetTip`, textos).
- Regla general: **el que és fora d'abast es treu**; quan calgui es tornarà a implementar (queda a l'historial de git).
- **D4 · Packs heretats**: la migració v5 converteix cada pack de N en N unitats. Desapareixen «Pack de N», el diàleg de quantitat i `SplitAndConsumeItemUseCase`.
- **D5 · Durades per defecte**: la «Rebost» de l'especificació és la nostra «Secs i conserves» (PANTRY): 3 mesos. Carn i peix 3 d · Fleca 3 d · Làctics 1 setm · Fruita i verdura 1 setm · Begudes 1 mes · Embotits 1 setm · Plats preparats 3 d. (Bernat avisarà disseny del canvi de nom.)
- **D6 · «Llença»**: totes les accions sobre productes són «Llença» = `discardedDate` amb Desfés (fora l'esborrat definitiu de producte). Per a categories el verb és «eliminar» («Eliminar categoria»); el text exacte, a la revisió final de textos.
- **D7 · Selecció**: **no hi ha pulsació llarga ni selecció nova.** Tocar la barra del producte (no els botons rodons) obre la modal. A Transforma s'hi entra per la modal, la targeta del final de l'Inici i el «Selecciona» que ja tenen les llistes d'estat. La pista de l'onboarding i de la novetat perd «o mantén premut un producte». **v2:** el «Selecciona» de les llistes d'estat també surt (D11, fase 9).
- **D8 · Llista de l'Inici**: entra al paquet (fase 7). Amb «0 Aviat» la targeta «Has cuinat?» **també surt** (si hi ha algun producte vigent), tot i que `IniciSenseAviat` no la mostra.
- **D10 · Calendari de mes** (Bernat, 14 d'octubre): la línia 2 porta el color d'estat (`2 unitats · D'aquí 2 dies`, mode `WithDays`), no el gris de l'Inici que diu l'especificació §3. És l'única desviació; la resta de la fila és igual.
- **D9 · Textos**: la revisió final de textos (noms, castellà infinitiu/imperatiu, «Transforma/Transformar») la fa disseny; nosaltres hi posem el millor que puguem i preparem la llista (fase 8). **v2:** disseny ja ha tornat les regles (§9: ca imperatiu, es infinitiu, en forma base; «Transforma»/«Transformar»; tema «Caducity»; «Secs i conserves»; «unitat/unitats», comptadors en productes). La fase 8 les aplica; la llista per a disseny queda només per al que §9 no resolgui.

### Decisions v2 (Bernat, 14 d'octubre)

- **D7 (revisada) → D11 · Selecció múltiple**: fora del tot (regla general: el que és fora d'abast es treu). Fora «Selecciona», el mode selecció i la barra de botons que sortia a baix (Consumeix / Congela / Transforma), amb el codi, els textos i el tracking. Les llistes d'estat queden netes: capçalera, llista i, només a Caducats, «Llença'ls tots». A Transforma s'hi entra per la modal (un producte) i per la fila del final de l'Inici (buida); la resta d'ingredients, amb «+ Afegeix». Fase 9.
- **D12 · Caducats**: «Llença'ls tots» passa de la capçalera a un botó vermell fix a baix (56 dp) amb el text d'avís a sobre (fase 9). La confirmació i el Desfés de la fase 4 es mantenen.
- **D13 · Formes de categoria**: els selectors de forma (editor i full ràpid) només ofereixen les sis de §11: `COOKIE` (Cookie9Sided), `FLOWER` (Flower, nova), `CLOVER` (Clover4Leaf), `SUNNY` (Sunny), `COOKIE4` (Cookie4Sided, nova), `PUFFY` (Puffy). `CLOVER8` i `BURST` queden a l'enum **només per mostrar** les categories que ja les tenen (retrocompatibilitat: BBDD, còpies de seguretat); no es migren dades. Categories noves de l'onboarding: Fruita i verdura `FLOWER`, Fleca `COOKIE4`, Plats preparats `PUFFY`; Begudes i Embotits, una de les sis. `SoftBurst` es queda per a l'avís de caducats (no és forma de categoria).
- **D14 · Capçaleres i components**: `LargeFlexibleTopAppBar` amb subtítol a Inici (data), Rebost i Categoria, plegable en fer scroll (com diu §11; a la resta de pantalles, formularis, fulls i subpantalles d'Ajustos, una capçalera gran només ocuparia espai). Condició de Bernat: **a tot arreu components estàndard d'M3/M3E**, res fet a mà: `TopAppBar` d'M3E a la resta (amb `subtitle` on calgui), també a les llistes d'estat en lloc del `StatusHeader` propi; FAB, `FloatingToolbar`, botons i grups connectats d'M3E. Revisar que `StyledTopAppBar` sigui només un embolcall de colors.

## Ordre

La migració va **al principi** (fase 1, només dades) perquè la modal (fase 3) ja no ha de tractar packs, i perquè així només hi ha una migració (v4 → v5). El comportament nou de la durada i de l'«Aviat» global va a la fase 6, i la llista de l'Inici a la 7, que en depèn (la finestra de l'Inici és l'«Aviat» global).

**v2:** les fases noves es numeren al final per no canviar les referències, però l'ordre d'execució és **3 → 4 → 9 → 5 → 6 → 7 → 10 → 8**: la 9 va just després de la 4 (toquen Caducats i les llistes d'estat) i abans de la 5 (sense selecció, Transforma només té les entrades noves); la 10 va després de les pantalles que retoca; la 8 (textos i dades de prova) tanca el paquet perquè les captures finals quadrin.

## Fase 0 — Treure el que és fora d'abast

1. Valoració: `feature/review/`, la crida de `CategoryScreen`, `reviewModule`, `ShowAppReviewFlag`, `play-review(-ktx)` (build i catàleg). Validació: build, tests, ktlint; eliminar una categoria al dispositiu.
2. Consell del widget: `WidgetTipCard`, `WidgetTipFlag` (+ Koin), `HomeItem.WidgetTip`, `onDismissWidgetTip`, `WIDGET_AVAILABLE`, textos, test i preview. Validació: `HomeViewModelTest`, Inici buit al dispositiu.
3. Mode tanda: «Desa i un altre», `BatchSection`, `AddedEntry`/`isBatchMode`/`isDone`, casos d'ús de tanda que quedin sense ús, `add_batch_*`; un sol «Desa» a tota l'amplada, desactivat mentre falti categoria o data (Afegeix i producte nou, captures `Afegir`, `AfegirNou`). Validació: tests d'`AddViewModel`, afegir i editar al dispositiu.
4. Helpers morts de `WindowSizeClass.kt` i el KDoc del «tablet mode flag». Validació: build.
5. Backlog: valoració, widget i dreceres, tauleta, mode tanda com a «fora d'abast de moment» (es tornaran a implementar), enllaç a aquest pla; treure els que aquest paquet resol. Validació: lectura.

**Sortida:** Afegeix amb un sol «Desa»; la resta no es veu.

## Fase 1 — Migració v5 (només dades; cal aprovació explícita abans d'escriure-la)

Abans de començar: ensenyar l'SQL i la taula de correspondències i esperar el «sí».

1. Room v4 → v5 (`.claude/rules/room.md`): `categories.usualDurationDays INTEGER` (nullable, `ADD COLUMN`, sense reconstruir taules; `soonThresholdDays` es queda però deixa de llegir-se a la fase 6). Les categories per defecte, reconegudes pel nom com a `MIGRATION_2_3`, reben la durada de D5; les de l'usuari, `null`.
2. Packs → unitats (D4): cada ítem viu amb `packSize = N > 1` es converteix en N ítems d'1 (mateixa data, estat i producte); `packSize` queda a 1.
3. Domini i mapper: `Category.usualDurationDays`; `SeedCategory` amb la durada per a categories noves de l'onboarding.
4. Backup format v4: `usualDurationDays`; importar v3 amb el mateix criteri (nom → durada, packs → unitats).
5. Tests: `Migration4To5Test` (categories per defecte en ca/es/en, de l'usuari, pack de 4 → 4 ítems), anada i tornada de backup, import v3.

**Sortida:** l'app s'actualitza des de v4 sense pèrdua de dades; els packs es veuen com a N unitats; res més canvia. Validació amb l'app `.dev` al dispositiu (amb còpia de seguretat abans).

## Fase 2 — Fila única de producte

1. Model de fila comú (nom, ids de les unitats ordenats per caducitat, quantitat, estat, dies, categoria opcional, mode de la línia 2) i un ajudant pur «primer la que caduca abans». Corregir `HomeRow.nearestItemId` (avui per `createdAt`). Tests d'unitat.
2. Composable `ProductRow` (`ui/components/product/`): 64 dp mínim, `surfaceContainer`, `connectedShape` 24/6, 2 dp; línia 1 nom 16 sp/600 amb «…»; línia 2 quantitat + context (gris, o negreta del color d'estat); icona de categoria si la llista barreja categories; accions rodones de 44 dp per estat; tocar la barra obre la modal; ripple retallat a la forma. Previews i captures de test (clar/fosc, quatre estats, una i diverses unitats).
3. Inici: `HomeItemRow` → `ProductRow` (`1 unitat · Làctics`, ✓ ❄).
4. Llistes d'estat (Aviat, Frescos, Congelats, Caducats): `StatusRowItem` → `ProductRow` (`2 unitats · D'aquí 2 dies` en color d'estat). Congelat: només ✓. Caducat: només paperera. Amb «Selecciona» actiu, la casella substitueix els botons com ara.
5. Categoria: `ProductGroupRow`/`UnitRow`/`DayUnitRow` → `ProductRow`; fora el desplegable «Unitat N» i la píndola.
6. Calendari de mes i resultats de cerca del Rebost → `ProductRow` (des de Categoria, sense categoria).
7. Botons rodons = una unitat, la que caduca primer; snackbar «1 iogurt natural consumit · Desfés». Tocar la barra obre la modal a totes les pantalles (la modal actual fins a la fase 3).
8. Esborrar els composables antics i `StatusPill` si queda sense ús.

**Sortida:** totes les llistes amb la mateixa fila; captures de test verdes.

## Fase 3 — Modal d'accions

1. Casos d'ús `ConsumeUnitsUseCase(itemIds, n)` i `FreezeUnitsUseCase(itemIds, n)` (primer les que caduquen abans; congelar salta caducats; retornen snapshots per al Desfés). Tests.
2. Ruta `ItemActions` amb els ids de les unitats de la fila (no un sol `itemId`); estat amb la variant: 1 unitat / diverses / caducat 1 / caducat diverses.
3. Variant 1 unitat (`Accions`): capçalera, Consumit/Congela, «Canvia la data» (+1 d, +3 d, +1 setm, calendari), Edita, Mou, Transforma aquest producte, Llença.
4. Variant diverses (`QuantitatAccions`): capçalera «4 unitats · la primera, dt. 13 oct», selector − «2 de 4» + («Primer les que caduquen abans»), «Consumeix N»/«Congela N», «Més accions» plegat (s'aplica a tot el producte).
5. Component «mantén premut» («Consumeix igualment · sota la teva responsabilitat», v2 §4; 1,5 s, s'omple d'esquerra a dreta, es buida si es deixa anar, vibració en completar; TalkBack: acció personalitzada + diàleg de confirmació). Test de UI del temps.
6. Variants caducat (`AccionsCaducat`, `QuantitatCaducat`): avís vermell, «Llença»/«Llença N» (selector per defecte = unitats caducades), «Més accions» plegat amb «Consumeix igualment», «La data era incorrecta», «Edita». Mai Congela, Transforma ni Mou.
7. Fora `QuantitySelectorDialog`, `SplitAndConsumeItemUseCase` i `quantity_*`. Captures de test de les quatre variants en clar i fosc.

**Sortida:** les quatre variants coincideixen amb les captures; cap diàleg de quantitat.

## Fase 4 — «Llença» i Caducats

1. D6: l'acció de producte descarta (`discardedDate`) amb Desfés; fora el diàleg «Esborrar X?» i `DeleteItemUseCase` si queda sense ús.
2. Textos de producte «Esborra/Borra/Delete» → «Llença» (ca, es, en) amb icona `delete`; categories → «Eliminar categoria».
3. Caducats: confirmació «Llençar 7 productes a les escombraries?» → «Llença'ls»/«Cancel·la»; snackbar «7 productes llençats · Desfés». Fora `QuickDatesSheet` si queda sense ús. (v2: la posició del botó i el text «Han caducat. Per seguretat, llença'ls.» van a la fase 9.)
4. Tests de ViewModel (llençar amb Desfés, llençar-los tots).

## Fase 5 — Navegació, FAB i pistes de Transforma

1. `SplitAddButton` → FAB «+» sol al costat de la barra flotant (56 dp, arrodoniment 16 dp, color *vibrant*, v2 §11); fora `MoreActionsMenu` i `action_more_actions`.
2. Barra: semàntica de pestanyes, no s'amaga en fer scroll (comprovar). La pestanya «Aviat» passa a **«Inici»** (`nav_soon` → `nav_home`, ca/es/en; icona `hourglass_bottom` igual); el títol de la pantalla continua sent «Què caduca». Amb lletra al 200 %, si l'etiqueta activa no hi cap, només la icona.
3. Categoria: FAB «+» de 56 dp (en lloc de l'`ExtendedFloatingActionButton` «Afegeix») amb `contentDescription` «Afegeix a {categoria}» i categoria preseleccionada.
4. Onboarding (pas 3) i novetat: la pista ja no parla del botó «+ ⌃» (fora `SplitButtonHint` i la maqueta del botó de `TransformStep`); text definitiu (v2 §7) «La tens al final de l'Inici i a cada producte.» (`ob_transform_hint`, `transform_intro_hint`, ca/es/en).
5. Captures de test de la barra (Inici, Rebost) en clar i fosc.

## Fase 6 — Durada habitual i «Aviat» global

1. Estat i avisos només amb l'«Aviat» global (`ObserveItemsUseCase`, `NotificationContentBuilder`, `ExpiryStatus`); fora `PantryEntry.soonDays`, la píndola «Avisa X abans» i els textos de «marge propi». Tests.
2. Ajustos: «"Aviat" vol dir» + «Quants dies abans de caducar passa a groc i t'avisem»; secció «Productes» › «Durada habitual» (la pantalla de marges passa a ser aquesta, captura `MargeCategories`, amb «Sense durada · la data la poses tu»). Editor de categoria (Nova categoria i edició, captura `NovaCategoria`): «Durada habitual · opcional» en lloc d'«Avisa'm abans», amb «Per suggerir la data quan afegeixes {un producte de la categoria}». El full ràpid d'Afegeix (`AfegirNovaCat`) es queda sense durada.
3. Llista d'estat Aviat: «pròxims {N} dies».
4. Afegeix: en triar categoria, data = avui + durada, xip marcat, «Suggerida: els Làctics duren 1 setmana»; sense durada, data buida; l'usuari la pot canviar. Tests d'`AddViewModel`.
5. Onboarding (pas d'avisos): revisar que parli només del global.

## Fase 7 — Llista de l'Inici

1. Finestra = «Aviat» global, seccions per dia («Avui», «Demà dilluns 12 · 3 productes», «Dimarts 13»…), fora la secció «Més endavant» (`HomeLater*`, `home_later_*`). Tests d'`ObserveHomeUseCase`.
2. Amb «0 Aviat» (`IniciSenseAviat`, `IniciSenseAviatCaducats`): properes caducitats, els 3 primers dies amb productes, màxim 8 files («+N més» si es talla), i «Veure el rebost ›» al final; fora `NothingSoon`. La targeta de caducats continua a dalt.
3. Targeta «Has cuinat? · Apunta-ho a Transforma» al final de la llista (`soup_kitchen`, `chevron_right`): obre `Transform()` buida (captura `Main`, Inici llarg 390×1544 dp); no es pot amagar; no surt sense cap producte vigent; amb «0 Aviat» també surt, després de «Veure el rebost» o abans (a decidir amb captura al dispositiu). Tracking amb `source`.
4. Captures de test de l'Inici (`Main`, `MainFosc`, sense aviat, buit).

## Fase 8 — Textos i dades de prova

1. Regles de §9 a `strings.xml` (ca, es, en): botons = verbs i missatges = participis; ca imperatiu, es infinitiu («Guardar», «Tirar», «Transformar»), en forma base; nom «Transforma» (es botó «Transformar», en «Transform»). Llista per a disseny només del que §9 no resolgui.
2. Noms (§9): tema per defecte «Rebost» → «Caducity», subtítol «verd i crema (per defecte)» (`palette_rebost`, i el nom de la clau si cal); «Rebost» només per a la pestanya i «el teu rebost» (comprovar que cap text l'usi com a categoria); «Racions» → «Unitats» a Transforma (`transform_servings`); «Buida el nom» a l'etiqueta del botó de buidar el nom; abreviatures de dies dl., dt., dc., dj., dv., ds., dg. (comprovar que quadren amb la data).
3. Quantitats i comptadors (§9): sempre «1 unitat»/«N unitats» (mai «u.»), femení quan concorda («2 unitats · 2 congelades»); els comptadors de seccions i capçaleres compten productes, no unitats. Tests on hi hagi lògica.
4. Repàs de `strings.xml`: «Esborra» residuals, textos sense ús (`lint` UnusedResources).
5. Dades de prova (§12, `FakeTestDataStrategy` i previews): diumenge 11 d'octubre com a dia de referència, «Aviat» = 3 dies, 61 productes en 6 categories exactament com la taula de §12 (caducats 7, aviat 8, frescos 43, congelats 3), només unitats, categories amb durades. Validació: les captures de l'app amb aquestes dades coincideixen amb `Main`, `Rebost`, `Caducats`, `EstatAviat`.
6. Release notes: proposta (no editar sense «sí»).

## Fase 9 — Pantalles d'estat i fora la selecció (v2 §0, §5, §10)

1. Fora la selecció múltiple (D11): «Selecciona», mode selecció, la barra de botons de baix (Consumeix / Congela / Transforma), l'estat i els mètodes del `StatusListViewModel`, `onOpenTransform` de `StatusListScreen` (i la ruta a `App.kt`), textos i accions de tracking que quedin sense ús. Pantalla neta: capçalera + llista (+ «Llença'ls tots» a Caducats). Tests del ViewModel.
2. Capçalera sempre plegada amb un `TopAppBar` estàndard d'M3E (D14) en lloc de `StatusHeader`: color d'estat, fletxa enrere, títol, recompte com a `subtitle`; fora la capçalera gran, el text d'ajuda, els botons de la capçalera i el `NestedScrollConnection`. Mida: la del component (la captura en diu 72 dp; no es força). Aviat: «8 productes · pròxims 3 dies» (amb la fase 6.3).
3. Caducats (D12): «Llença'ls tots» vermell a tota l'amplada, fix a baix (56 dp, icona), amb «Han caducat. Per seguretat, llença'ls.» just a sobre; la llista hi deixa espai. Files només amb la paperera.
4. Congelats: files només amb ✓ (comprovar que ja és així des de la fase 2).
5. Entrades: rajoles del resum de l'Inici (Aviat, Frescos, Congelats) i l'avís vermell (Caducats) — comprovar.
6. Captures de test de les quatre pantalles en clar i fosc (`EstatAviat`, `EstatFrescos`, `EstatCongelats`, `Caducats`).

**Sortida:** les quatre llistes d'estat coincideixen amb les captures; no queda cap mode selecció a l'app.

## Fase 10 — Material 3 Expressive (v2 §11)

1. Formes de categoria (D13): `CategoryShape.FLOWER` (`MaterialShapes.Flower`) i `COOKIE4` (`MaterialShapes.Cookie4Sided`); llista `selectableShapes` amb les sis de §11 per als selectors (editor i full ràpid); `CLOVER8` i `BURST` es continuen dibuixant però no es poden triar (si una categoria en té una, el selector no en marca cap fins que se'n tria una altra). `SuggestedCategory`: Fruita i verdura `FLOWER`, Fleca `COOKIE4`, Plats preparats `PUFFY`, Begudes i Embotits dins les sis. Còpia de seguretat: accepta els noms nous i els antics. Avís de caducats amb `SoftBurst`. Forma de Transforma a l'onboarding: una de les sis. Tests del conversor i del backup.
2. Botons principals mida M (56 dp): Desa, Llença, Llença'ls tots, Consumeix N / Congela N (grup connectat), Crea i fes-la servir, botons de l'onboarding (`ButtonDefaults` de mida M d'M3E).
3. Botons rodons ✓ ❄ 🗑 de `ProductRow` amb el canvi de forma en prémer d'M3E (`IconButton` amb `shapes`).
4. Capçaleres (D14): `LargeFlexibleTopAppBar` amb subtítol a Inici (data), Rebost i Categoria, es pleguen en fer scroll; a la resta, `TopAppBar` d'M3E. Repàs de components fets a mà que tinguin equivalent estàndard d'M3/M3E i substituir-los.
5. Prova amb lletra al 200 %: la fila talla amb «…»; la barra flotant amaga l'etiqueta si no hi cap (fase 5.2). Captures de test amb `fontScale = 2f`.
6. Captures de test (Inici, Rebost, Categoria, Nova categoria) en clar i fosc.

**Sortida:** mides, formes i capçaleres segons §11; res es trenca amb lletra al 200 %.

## Release notes

Visibles per a l'usuari: fases 0 (un sol «Desa», fora la tanda), 1 (packs com a unitats), 2-7, 9 (llistes d'estat, fora «Selecciona») i 10 (formes i capçaleres). Fase 8, com a molt la línia de petits canvis. El text es decideix en preparar la versió.

## Estat

| Fase | Estat |
| --- | --- |
| 0 — Fora d'abast | Validada (14 d'octubre): `ffabb36` valoració, `458df5c` widget, `0472842` tanda, `55e1a77` helpers, `0d8dd93` backlog, `ebf8ba7` + `0752b2f` renom a `AddProductUseCase` |
| 1 — Migració v5 | Validada (14 d'octubre, `66ef801`; migració v4 → v5 comprovada a l'emulador amb dades fake). Abast ampliat (Bernat, 14 d'octubre): `packSize` i `soonThresholdDays` s'esborren de la BBDD (taules reconstruïdes), `usualDurationDays` hereta el marge configurat; la fase 3.7 (packs) i la 6.1 (Aviat global, «Més endavant») s'han avançat. Backup v4: només s'importa la versió actual, sense codi d'upgrade. Pendent a les fases 6/8: textos i noms «marge» (`CategoryMargins*`, «Marge per categoria», «Avisa'm abans»), `CreateCategoryUseCase` amb un paràmetre sense ús |
| 2 — Fila única | Validada (14 d'octubre; `567a5c5` neteja de textos): `7176e40` model, `d4fe7d1` `ProductRow`, `8887423` Inici, `1c5f1f4` llistes d'estat, `6bd9ea1` Categoria, `e60111a` calendari i cerca, `e825d00` dies, `5692109` snackbars. Provat a l'emulador (no Frescos, «Selecciona» ni Desfés). Tasca 8: `StatusPill`/`StatusPillText`/`StatusBadge` es queden perquè la modal (`ItemActionsSheet`) i el selector de Transforma encara els fan servir; es revisaran a la fase 3. Desviacions: D10 (calendari amb color d'estat); Congelats sense data de congelació; una fila amb unitats d'estats diferents mostra l'estat de la que caduca primer |
| 3 — Modal d'accions | Validada (14 d'octubre): `22a89b7` modal sobre el producte (selector, Consumeix/Congela N, variants de caducat, «mantén premut»), `e448b47` la ruta porta els ids de la fila (`ItemActions(itemIds)`) i «Consumeix igualment» (v2), `f4bb789` selector centrat. Provat a l'emulador. Desviacions: sense `ConsumeUnitsUseCase`/`FreezeUnitsUseCase` (els casos d'ús en bloc ja existien); «Llença» de la modal ja descarta amb Desfés (D6 avançat, fora `DeleteItemUseCase` i el diàleg d'esborrar). Pendent: «Descongela» no és a la variant de diverses unitats; botons principals de 56 dp (v2 §11) a la fase 10; textos provisionals a la fase 8 |
| 4 — «Llença» i Caducats | Validada (14 d'octubre): `3bc8229e` confirmació «a les escombraries» + «Llença'ls», categories «Eliminar», test de «Llença'ls tots» amb Desfés. La resta (D6, snackbars, fora `DeleteItemUseCase`) ja venia de la fase 3 i la 2. `QuickDatesSheet` es queda fins a la fase 9 (D11) |
| 5 — Navegació, FAB i pistes | Validada (14 d'octubre): `9aa5afe5` FAB «+» de 56 dp (`AddFab`, també a Categoria amb «Afegeix a {categoria}»), pestanya «Inici», pistes de Transforma sense la maqueta del botó, captures de la barra. Provat a l'emulador (Inici, Rebost, Categoria, onboarding, novetat, lletra al 200 %). Extres: `3c366605` dades fake petites, `1b6cb1ad` estat de càrrega de l'Inici, `a7099af7` `flowOn(Default)` als casos d'ús. Desviacions: un sol commit per a les tasques 1-5 (fitxers compartits); sense menú del botó dividit, Transforma s'obre des de la modal, «Selecciona» (fins a la fase 9) i la fila de l'Inici (fase 7). Pendent: baseline profile (backlog); les captures de test de la barra no tenen referències generades |
| 6 — Durada habitual i «Aviat» global | Pendent |
| 7 — Llista de l'Inici | Pendent |
| 8 — Textos i dades de prova | Pendent |
| 9 — Pantalles d'estat i fora la selecció | Pendent (D11, D12 decidides) |
| 10 — Material 3 Expressive | Pendent (D13, D14 decidides) |

Només es passa a «Validada» quan Bernat l'aprova (`.claude/rules/phased-work.md`).
