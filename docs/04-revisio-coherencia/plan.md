# Pla: revisió de coherència (13 d'octubre)

Especificació: [`spec.md`](spec.md) (té prioritat sobre les anteriors quan es contradiuen). Captures: `captures/` (35 pantalles, només mòbil); fonts: `font-dc-html/`.
Flux: `.claude/rules/phased-work.md` (una fase per sessió, tasques aprovades, un commit per tasca validada). Validació al dispositiu de treball (adb wifi), mai a l'emulador; captures en clar i en fosc al final de cada fase.
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

## Decisions (Bernat, 13 d'octubre)

- **D1 · Mode tanda**: fora del tot (botó, `BatchSection`, casos d'ús, textos). Afegeix desa i es tanca.
- **D2 · Valoració**: fora del tot (codi, flag, dependència).
- **D3 · Consell del widget**: fora del tot (`WidgetTipCard`, `WidgetTipFlag`, `HomeItem.WidgetTip`, textos).
- Regla general: **el que és fora d'abast es treu**; quan calgui es tornarà a implementar (queda a l'historial de git).
- **D4 · Packs heretats**: la migració v5 converteix cada pack de N en N unitats. Desapareixen «Pack de N», el diàleg de quantitat i `SplitAndConsumeItemUseCase`.
- **D5 · Durades per defecte**: la «Rebost» de l'especificació és la nostra «Secs i conserves» (PANTRY): 3 mesos. Carn i peix 3 d · Fleca 3 d · Làctics 1 setm · Fruita i verdura 1 setm · Begudes 1 mes · Embotits 1 setm · Plats preparats 3 d. (Bernat avisarà disseny del canvi de nom.)
- **D6 · «Llença»**: totes les accions sobre productes són «Llença» = `discardedDate` amb Desfés (fora l'esborrat definitiu de producte). Per a categories el verb és «eliminar» («Eliminar categoria»); el text exacte, a la revisió final de textos.
- **D7 · Selecció**: **no hi ha pulsació llarga ni selecció nova.** Tocar la barra del producte (no els botons rodons) obre la modal. A Transforma s'hi entra per la modal, la targeta del final de l'Inici i el «Selecciona» que ja tenen les llistes d'estat. La pista de l'onboarding i de la novetat perd «o mantén premut un producte».
- **D8 · Llista de l'Inici**: entra al paquet (fase 7). Amb «0 Aviat» la targeta «Has cuinat?» **també surt** (si hi ha algun producte vigent), tot i que `IniciSenseAviat` no la mostra.
- **D9 · Textos**: la revisió final de textos (noms, castellà infinitiu/imperatiu, «Transforma/Transformar») la fa disseny; nosaltres hi posem el millor que puguem i preparem la llista (fase 8).

## Ordre

La migració va **al principi** (fase 1, només dades) perquè la modal (fase 3) ja no ha de tractar packs, i perquè així només hi ha una migració (v4 → v5). El comportament nou de la durada i de l'«Aviat» global va a la fase 6, i la llista de l'Inici a la 7, que en depèn (la finestra de l'Inici és l'«Aviat» global).

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
5. Component «mantén premut» (1,5 s, s'omple d'esquerra a dreta, es buida si es deixa anar, vibració en completar; TalkBack: acció personalitzada + diàleg de confirmació). Test de UI del temps.
6. Variants caducat (`AccionsCaducat`, `QuantitatCaducat`): avís vermell, «Llença»/«Llença N» (selector per defecte = unitats caducades), «Més accions» plegat amb «Consumit igualment», «La data era incorrecta», «Edita». Mai Congela, Transforma ni Mou.
7. Fora `QuantitySelectorDialog`, `SplitAndConsumeItemUseCase` i `quantity_*`. Captures de test de les quatre variants en clar i fosc.

**Sortida:** les quatre variants coincideixen amb les captures; cap diàleg de quantitat.

## Fase 4 — «Llença» i Caducats

1. D6: l'acció de producte descarta (`discardedDate`) amb Desfés; fora el diàleg «Esborrar X?» i `DeleteItemUseCase` si queda sense ús.
2. Textos de producte «Esborra/Borra/Delete» → «Llença» (ca, es, en) amb icona `delete`; categories → «Eliminar categoria».
3. Caducats: subtítol «Han caducat. Per seguretat, llença'ls.»; confirmació «Llençar 7 productes a les escombraries?» → «Llença'ls»/«Cancel·la»; snackbar «7 productes llençats · Desfés». Fora `QuickDatesSheet` si queda sense ús.
4. Tests de ViewModel (llençar amb Desfés, llençar-los tots).

## Fase 5 — Navegació, FAB i pistes de Transforma

1. `SplitAddButton` → FAB «+» sol al costat de la barra flotant; fora `MoreActionsMenu` i `action_more_actions`.
2. Barra: semàntica de pestanyes, no s'amaga en fer scroll (comprovar).
3. Categoria: FAB «+» amb `contentDescription` «Afegeix a {categoria}» i categoria preseleccionada.
4. Onboarding (pas 3) i novetat: la pista ja no parla del botó «+ ⌃» (fora `SplitButtonHint`); text provisional «La tens al final de la llista d'Aviat.» fins a la revisió de disseny (D7, D9).
5. Captures de test de la barra (Inici, Rebost) en clar i fosc.

## Fase 6 — Durada habitual i «Aviat» global

1. Estat i avisos només amb l'«Aviat» global (`ObserveItemsUseCase`, `NotificationContentBuilder`, `ExpiryStatus`); fora `PantryEntry.soonDays`, la píndola «Avisa X abans» i els textos de «marge propi». Tests.
2. Ajustos: «"Aviat" vol dir» + «Quants dies abans de caducar passa a groc i t'avisem»; secció «Productes» › «Durada habitual» (la pantalla de marges passa a ser aquesta, captura `MargeCategories`, amb «Sense durada · la data la poses tu»). Editor de categoria: «Durada habitual» en lloc d'«Avisa'm abans».
3. Llista d'estat Aviat: «pròxims {N} dies».
4. Afegeix: en triar categoria, data = avui + durada, xip marcat, «Suggerida: els Làctics duren 1 setmana»; sense durada, data buida; l'usuari la pot canviar. Tests d'`AddViewModel`.
5. Onboarding (pas d'avisos): revisar que parli només del global.

## Fase 7 — Llista de l'Inici

1. Finestra = «Aviat» global, seccions per dia («Avui», «Demà dilluns 12 · 3 productes», «Dimarts 13»…), fora la secció «Més endavant» (`HomeLater*`, `home_later_*`). Tests d'`ObserveHomeUseCase`.
2. Amb «0 Aviat» (`IniciSenseAviat`, `IniciSenseAviatCaducats`): properes caducitats, els 3 primers dies amb productes, màxim 8 files («+N més» si es talla), i «Veure el rebost ›» al final; fora `NothingSoon`. La targeta de caducats continua a dalt.
3. Targeta «Has cuinat? · Apunta-ho a Transforma» al final de la llista (`soup_kitchen`, `chevron_right`): obre `Transform()` buida; no es pot amagar; no surt sense cap producte vigent; amb «0 Aviat» també surt, després de «Veure el rebost» o abans (a decidir amb captura al dispositiu). Tracking amb `source`.
4. Captures de test de l'Inici (`Main`, `MainFosc`, sense aviat, buit).

## Fase 8 — Textos i dades de prova

1. Llista de textos per a disseny (ca, es, en): els nous i els canviats d'aquest paquet, «Transforma/Transformar», infinitiu/imperatiu en castellà. Aplicar el que torni.
2. Repàs de `strings.xml`: «Esborra» residuals, «unitat/unitats», textos sense ús (`lint` UnusedResources).
3. Dades de prova (`FakeTestDataStrategy`): sense packs, només unitats, categories amb durades.
4. Release notes: proposta (no editar sense «sí»).

## Release notes

Visibles per a l'usuari: fases 0 (un sol «Desa», fora la tanda), 1 (packs com a unitats), 2-7. Fase 8, com a molt la línia de petits canvis. El text es decideix en preparar la versió.

## Estat

| Fase | Estat |
| --- | --- |
| 0 — Fora d'abast | Validada (14 d'octubre): `ffabb36` valoració, `458df5c` widget, `0472842` tanda, `55e1a77` helpers, `0d8dd93` backlog, `ebf8ba7` + `0752b2f` renom a `AddProductUseCase` |
| 1 — Migració v5 | Pendent (cal aprovació de la migració) |
| 2 — Fila única | Pendent |
| 3 — Modal d'accions | Pendent |
| 4 — «Llença» i Caducats | Pendent |
| 5 — Navegació, FAB i pistes | Pendent |
| 6 — Durada habitual i «Aviat» global | Pendent |
| 7 — Llista de l'Inici | Pendent |
| 8 — Textos i dades de prova | Pendent |

Només es passa a «Validada» quan Bernat l'aprova (`.claude/rules/phased-work.md`).
