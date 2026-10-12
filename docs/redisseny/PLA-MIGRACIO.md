# Pla de migració al redisseny «Caducity» (Material 3 Expressive)

Versió del pla: **2** · Data: 2026-10-11 · Estat: **aprovat** (respostes P1–P21 incorporades). **Regles de treball a la secció 9. Estat de les fases: taula següent.**

Base: `main` @ `c204b56`, versió `0.10.0-SNAPSHOT`, Room v2, minSdk 35, compileSdk 37, Compose BOM 2026.09.00, `material3 1.5.0-beta01`, Navigation 3, Koin, `multiplatform-settings`.
Entrades llegides: paquet de redisseny **v2** (inclosos `RESPOSTES-AL-PLA.md`, `PROMPT-ACTUALITZAR-PLA.md`, `PROMPT-CLAUDE-CODE.md` i `pantalles/11-calendari.md`).

**Cadència de lliurament:** no es publica a Play Store entre fases, però **cada fase acaba en un o més commits** que compilen i passen `./gradlew ktlintCheck test assembleDebug`, i es poden revertir sols. S'accepta UI híbrida entre fases. Es publicarà només quan tot estigui acabat. Es treballa **directament a `main`** (sense branques): l'app no es publicarà fins que el redisseny estigui acabat, per tant `main` pot anar en estat híbrid entre fases.

## Estat de les fases

S'actualitza en tancar cada fase (regla 6: tests verds → emulador → validació de l'usuari). Només es marca **Feta** després de la validació explícita.

| Fase | Estat | Commits | Data | Notes |
|---|---|---|---|---|
| 0 — Preparació i neteja | ✅ Feta | `5238d33` … `4443491` | 2026-10-11 | Deduït dels commits; no hi ha validació explícita registrada |
| 1 — Fonaments (tokens, tema, components) | ✅ Feta | `9878c05` … `260468e` | 2026-10-11 | Deduït dels commits |
| 2 — Model, dades i càlcul d'estat | ✅ Feta | `d2e4ed9` … `d1b365b` | 2026-10-11 | Room v3, backup v2, `PreferencesMigrator` pas 1 |
| 3 — Navegació (shell) | ✅ Feta | `aa72d49` … `ae8a69f` | 2026-10-11 | Validada per l'usuari. La prova en tauleta s'ha posposat al repàs global final (decidit 2026-10-11). Canvis: barra sempre visible per sobre del contingut; Ajustos és una icona que obre pantalla secundària; deep links nous (`OpenHome`/`OpenExpired`/`OpenAdd`) a la fase 12 |
| 4 — Afegeix | ✅ Feta | `353c8c7` … `5348114` | 2026-10-11 | Validada per l'usuari. Provada a l'emulador (clar i fosc). Parser/dates ràpides, camp de data, `AddViewModel`, pantalla, mode tanda amb `BatchSavedRow`/`BatchSavedCard` (entrada tocable, Edita, «Treu de la tanda» amb Desfés amb els mateixos ids i dates), mode edició (ruta i targeta; la targeta edita també la quantitat), `QuickCategorySheet`, neteja de l'antiga pantalla d'afegir. Comptador «✓ N desats» = entrades. La prova en tauleta queda per al repàs global final |
| 5 — Full d'Accions, Inici, Calendari | ✅ Feta | `abe51f1`, `0a4aeaa`, `06acadd`, `d41bb58` | 2026-10-11 | Validada per l'usuari. Provada a l'emulador (clar i fosc). Full d'Accions amb Desfés en un snackbar global al shell (`GlobalSnackbarEvents`); Inici nou (el Dashboard antic s'ha eliminat); tira de 7 dies i vista de mes amb estats sobre el cercle de 34 dp. «Cerca» només canvia a Rebost fins a la fase 6; el consell del widget queda amagat fins a la 12. Canvi de mes per gest (no pager): vegeu M1 a la secció 15. La prova en tauleta queda per al repàs global final |
| 6 — Rebost i Categoria | ✅ Feta | `e24a10e`, `aad8097`, `0a390ae`, `eb72c49`, `7f62109` | 2026-10-11 | Validada per l'usuari. Provada a l'emulador (clar; el fosc no s'ha provat). Rebost amb cercador enfocable des d'Inici; Categoria amb capçalera fixa, tira de 7 dies, xips i filtre per dia (P19 canviat a «filtra», segons `CategoriaDia.png`); píndoles Ahir/Avui/Demà. Eliminats `CategoryDetail*`, pàgines de producte i els use cases/DAOs de P7. `ItemDetailUiModel` (fins a la 7) i `DateItemsUiModel` (fins a la 10b) es mantenen `// TEMP`. Capçalera fixa a la resta de pantalles: M2. La prova en tauleta queda per al repàs global final |
| 7 — Pantalles d'estat (Caducats, Aviat, Frescos, Congelats) | ✅ Feta | `7d83245`, `4f8d0ad`, `cf03ab2`, `544cde8`, `e1bf916`, `9afdf9e` | 2026-10-11 | Validada per l'usuari. Tests i ktlint verds; provada a l'emulador (clar; el fosc no s'ha provat), sense crashos. `StatusListScreen(status)` amb les quatre variants, selecció múltiple i Desfés exacte per instantània (`ItemSnapshot`); l'Inici hi connecta bàner i caselles (a 0 no navega). S'han eliminat `filtered/**`, el full d'Accions antic i els mètodes de consum amb bloqueig. `ItemDetailUiModel` es manté `// TEMP(fase 10)`. Capçalera col·lapsable en fer scroll (pròpia, amb `nestedScroll`; `MotionLayout` a M3). La prova en tauleta queda per al repàs global final |
| 8 — Nova/Edita categoria i Ajustos | ✅ Feta | `9a62625` | 2026-10-11 | Validada per l'usuari. Tests i ktlint verds; provada a l'emulador (clar; el fosc, l'edició i la notificació real no s'han provat). Editor de categoria (nova/edita/esborra), Ajustos nous (Còpia, Privadesa, Quant a i Debug passen a rutes de la pila principal), una sola notificació diària amb un sol canal (canals antics esborrats) i pas 2 del `PreferencesMigrator` (també s'esborra `notifications_enabled_key`; l'interruptor passa a `daily_reminder_enabled`). Bullet de release notes pendent per al final del rediseny |
| 9 — Onboarding nou | ✅ Feta | `f59f7f3`, `8c9dc26`, `a0a68b4`, `b2eed14`, `c4c5b99`, `cc14ee0` | 2026-10-11 | Validada per l'usuari. Tests i ktlint verds; provada a l'emulador (clar i fosc, els 5 passos, permís de notificacions, selector d'hora, reacceptació només de l'avís), sense crashos. Categories suggerides sense marge propi; el marge hi arriba a la 12. Els noms de les categories suggerides es desen en l'idioma de l'onboarding (decidit deixar-ho així). El text legal continua sent una proposta pendent de revisió. La prova en tauleta queda per al repàs global final |
| 10 — Textos, accessibilitat, mode fosc, neteja | ✅ Feta | `3544f75` … (10b), `f7baa9f`, `2d97d06`, `78ed121`, `e0522eb` | 2026-10-12 | Validada per l'usuari (2026-10-12). 10b: eliminats `ui/components/calendar/**`, `kalendar` i models orfes; README/ROADMAP/CLAUDE.md al dia; lint sense errors (14 arreglats). 10a: 117 claus òrfenes fora, plurals ca/es amb `many`, diàleg de quantitat localitzat i redissenyat (`15-dialeg-quantitat.md`, extra no previst). Accessibilitat: tira/mes/camp de data ja tenien rols i `liveRegion`; afegits rols als `clickable` que faltaven i límit d'escala de font a les caselles de l'Inici. Mode fosc revisat a l'emulador (Inici, Rebost, Ajustos, Accions, diàleg). **Queda per al repàs global final:** TalkBack real, revisió de les traduccions ca/es i lletra gran a més pantalles. Nota M4 afegida |
| 11 — Marge d'«Aviat» per categoria (abans 8b) | ✅ Feta | `40f5e1d`, `1878e2b`, `9533dcb` | 2026-10-12 | Validada per l'usuari (2026-10-12). Columna dins `MIGRATION_2_3` i `3.json` regenerat (cal reinstal·lar les instal·lacions de desenvolupament amb la v3 antiga). `ThresholdSlider`, pantalla «Marge per categoria», Ajustos, editor, Inici («Més endavant») i Aviat; notificació amb el criteri nou; onboarding i fake data amb marges suggerits. Tests i ktlint verds; provada a l'emulador en clar i fosc, la tauleta queda per al repàs global. Punt a millorar M5: cap categoria es pot dir «Rebost» |
| 12 — Icona, splash, widget, dreceres | ✅ Feta (sense widget ni dreceres) | `3d59630`, `bf4037d`, `19bb67f` | 2026-10-12 | Validada per l'usuari (2026-10-12). Icona adaptativa, splash natiu d'Android 12+ (sense `core-splashscreen` perquè `minSdk` és 35: fons `surface` clar `#FBF6EC` i fosc `#1C1A15` a `Theme.Caducity`) i icona petita de notificació monocroma. Repàs a la tauleta (clar i fosc): barres superiors, botó «Add», capçalera d'estat i Rebost acotats a la columna de 600 dp (`94210b7`, `e9dde39`); «phone» → «device». Widget i dreceres queden per a un futur (decidit 2026-10-12) |
| 13 — Landing i política de privacitat (nova) | ⏳ Pendent (falta disseny) | — | 2026-10-12 | Fora de l'app, al repo `alorma/ConversesLanding`. Vegeu la fase 13 |

**Repàs global final (tancat el 2026-10-12, validat per l'usuari):** prova a tauleta de les fases 3–9 feta; mode fosc validat; edició de categoria i notificació diària provades. **Descartat de moment:** TalkBack real, revisió de les traduccions ca/es i lletra gran a més pantalles. **Recorregut amb instal·lació neta i actualització des de la v0.10 (acceptació de la 10b):** es farà amb la build de release, no abans.

---

## 0. Resum executiu i canvis respecte a la versió 1 del pla

Què cal saber:

1. **El mòdul d'IA existent s'elimina sencer** (Llamatik, `firebase-ai`, assistent, descàrrega de models, `INTERNET`, foreground service). Fase 0.
2. **No hi ha DataStore**: les preferències són `multiplatform-settings` (SharedPreferences). Es mantenen.
3. **El model ja té «consumit»** (`consumedDate`, fila conservada) i **«Esborra» ja és un `DELETE`**. Només cal afegir `discardedDate` (Llença) i `createdAt` (historial).
4. **«Aviat» actual ≡ llindar 3 dies amb `<=`** de l'especificació (avui: 4 dies fixos amb `<`). `soonThresholdDays = 3` per als existents no canvia res. L'estat avui es calcula al mapper de Room i no es refresca a mitjanit: es mou a una funció pura i reactiva.
5. **Quantitat = N ítems independents** (P2, opció B). Els `packSize > 1` antics queden com a «pack de N» amb consum parcial; no se'n creen de nous.
6. **Calendari ja dissenyat** (`11-calendari.md`): tira de 7 dies a Inici i Categoria + vista de mes. **Graella pròpia, sense `kalendar`**; `kalendar` i el calendari antic s'eliminen a la fase 10 (secció 5.13).
7. **Hi ha una peça fora d'ordre a la llista inicial**: Inici i Calendari obren el full d'Accions, així que **el full d'Accions passa de la fase 6 a la 5** (secció 11).
8. **Notificacions**: de «una per producte i categoria» (2 canals, 2 interruptors, hora 12:00) a **una de sola al dia**.
9. **Glance no admet fonts personalitzades**: el widget usa la tipografia del sistema en negreta (P13).
10. **El paquet v2 té una discrepància** (secció 4): cinc fitxes de l'especificació diuen estar actualitzades però no ho estan. Decidit (P21): s'alineen al primer commit de la fase 0.
11. **Mode fosc dissenyat**: hi ha referència visual (`captures-fosc/`, 6 pantalles) i especificació (`04-especificacio/12-mode-fosc.md`). Només canvien els colors; vegeu la nota a cada fase.

12. **Pantalles d'estat unificades** (canvi del 2026-10-11, `pantalles/13-pantalles-estat.md`): el bàner de caducats i les caselles Aviat/Frescos/Congelats de l'Inici obren la mateixa `StatusListScreen(status)`. Caducats deixa de ser una pantalla a part (és la variant `EXPIRED`). La fase 7 s'amplia i les caselles de l'Inici es connecten a la 7.

Canvis d'aquesta versió del pla respecte a la v1: respostes P1–P16 aplicades; P2/P6 reflectides arreu; migració amb buidat d'`identifier`; calendari dins d'abast (fases 5, 6, 10); fase 7 amb «Res caducat»; full d'Accions avançat a la fase 5; riscos i ordre revisats; P17–P21 resoltes; queda una hipòtesi menor (P22).

---

## 1. Decisions preses

| # | Decisió | On s'aplica |
|---|---|---|
| ✅ P1 | S'elimina tot l'IA i Llamatik. | Fase 0 |
| ✅ P2 | **Opció B**: el selector de quantitat crea **N ítems independents** amb la mateixa data. Consumit, Congela, Llença i canvi de data actuen sobre **1 unitat**. Ítems antics amb `packSize > 1`: «pack de N», conserven el consum parcial (`SplitAndConsume` només per a ells); no se'n creen de nous. | Fases 2, 4, 5, 6; 5.4 |
| ✅ P3 | Reacceptació **única**, només del pas d'avís, per als usuaris existents. | Fase 9; 6.3 |
| ✅ P4 | Descongela proposa `ara + remainingDays` editable; amb «Congelat» no s'accepta data passada. | Fases 2, 4, 5 |
| ✅ P5 | Consumit permès sempre des d'Accions; Caducats només ofereix Llença / Canvia la data. | Fases 5, 7 |
| ✅ P6 | Inici: **una fila per (producte, dia)** amb «N unitats»; el recompte de la secció compta **unitats**; ✓ consumeix **una** unitat (la més propera). | Fase 5 |
| ✅ P7 | Es perden «eliminar producte» i «buidar consumits»; productes buits invisibles però conservats per a l'historial. | Fases 2, 6 |
| ✅ P8 | Calendari dissenyat. **Graella de mes pròpia (no `kalendar`)**; justificació a 5.13. `kalendar` s'elimina a la fase 10. | Fases 5, 6, 10 |
| ✅ P9 | Es manté «La setmana comença»; afecta **només** la vista de mes (la tira comença avui). | Fases 5, 8 |
| ✅ P10 | La cerca de l'Inici obre Rebost amb el cercador enfocat. | Fases 5, 6 |
| ✅ P11 | Cerca per nom de producte (i categoria), sense accents ni majúscules, només actius. | Fase 6 |
| ✅ P12 | `add?itemId=` per a edició; «Mou» busca o crea el producte homònim a la categoria destí. | Fases 3, 4, 5 |
| ✅ P13 | Widget amb tipografia del sistema en negreta. | Fase 12 |
| ✅ P14 | `ImageVector` de Material Symbols Rounded generats per script a `:icons`. | Fase 1 |
| ✅ P15 | Es permet desar dates passades, amb «ja ha caducat» en `expired`. Es treu `FutureDateSelectableDates`. | Fase 4 |
| ✅ P16 | Textos nous a **ca**, **en** i **es** (en/es per revisar). | Fase 10 |
| Migració 2→3 | Buidar `identifier` també dels ítems que ja tenen producte, **i** la UI l'ignora sempre. «Unitat N» és derivat. | 6.1 |
| Fase 7 | Estat buit «**Res caducat**». | Fase 7 |
| Calendari | Dins d'abast: tira de 7 dies (Inici i Categoria) + vista de mes (`Calendar(categoryId?, date?)`). | Fases 5, 6 |
| ✅ P17 | Punts i recomptes del calendari **per unitat** (coherent amb Inici); un pack antic compta 1. | 5c |
| ✅ P18 | Tocar un dia de la tira de l'Inici sense secció `SOON` **obre el mes amb aquell dia seleccionat** (`Calendar(date=)`). | 5c |
| ✅ P19 | Tocar un dia de la tira de Categoria **filtra**: el xip de dia substitueix els xips d'estat i la llista mostra només les unitats que caduquen aquell dia, en una sola llista connectada (`CategoriaDia.png`). Canvi del 2026-10-11: abans ressaltava files, cosa que trencava les llistes connectades. | 6 |
| ✅ P20 | Mode edició (`add?itemId=`) edita **una unitat**. Si el nom nou no coincideix amb cap producte de la categoria, se'n **crea un de nou**; si coincideix, la unitat hi passa. Si el producte antic queda **sense unitats actives, s'amaga però es conserva** (com a P7). El selector de quantitat **s'amaga** en mode edició. | 4, 5a |
| ✅ P21 | S'alineen els fitxers desactualitzats de `04-especificacio/` amb les decisions, en un **commit propi de documentació** («docs: alinear especificació amb decisions») al **començament de la fase 0**, sense canviar cap decisió de disseny. | 0 |

---

## 2. Inventari del codi actual

Llegenda: **M** manté · **Mod** modifica · **S** substitueix · **E** elimina · **N** nou. Rutes relatives a `app/src/main/kotlin/com/alorma/caducity/` si no s'indica el contrari.

### 2.1 Tema (`ui/theme/`)

| Element | Fitxers | Acció | Notes |
|---|---|---|---|
| Colors M3 | `colors/Color.kt`, `ColorSchemes.kt`, `ColorContrastLevel.kt` | **S** | Esquemes clar/fosc des de `tokens.json`. Contrast level: **E** si no s'usa. |
| Color dinàmic | `DynamicColorScheme.kt`, `DynamicColorsSupport.kt` | **Mod** | Només superfícies i `primary*`; els estats mai. Default ja és `false`. |
| Colors d'estat | `ExpirationColors.kt` (Vibrant/Soft, `harmonize`) | **S** → `CaducityColors`/`StatusColors` | `consumed` desapareix. |
| Tone | `ThemeTone.kt`, `LocalThemeTone`, `ThemePreferences.themeTone`, `ThemePreferencesImpl` (`theme_tone`), `AppearanceSettingsScreen`, `SettingsContainer`, `DashboardSummaryCard`, `ExpirationDefaults` | **E** | 9 fitxers. Clau esborrada pel migrador de prefs. |
| Tipografia | `Typography.kt` (Google Sans Flex ×2, Roboto Flex) | **S** | Bricolage Grotesque + Figtree variables. |
| Formes | (sense `Shapes` propi) | **N** `CaducityShapes` | Radis 6/8/12/16/20/24/28/32. |
| `CaducityTheme` | `CaducityTheme.kt` | **Mod** | Afegir `colors`/`shapes`; treure `themeTone`/`expirationColors`; `CaducityDims` **E** si no s'usa. |
| `AppTheme` | `AppTheme.kt` | **Mod** | Ja usa `MaterialExpressiveTheme` + `MotionScheme.expressive()`. |
| Barres del sistema, previews | `SystemBarsAppearance*`, `preview/*` | **M** / **Mod** | |
| Helper de llistes | `ui/components/shape/ShapePosition.kt` + `ShapePositionTest` | **Mod** | Existeix (None/Single/Start/Middle/End); fixar-lo a 24/6 (llistes) i 24/8 (botons) i afegir `connectedShape(index, count)`. |

### 2.2 Components (`ui/components/`)

| Element | Acció | Notes |
|---|---|---|
| `StatusBadge.kt` (183 l., 5 usos) | **S** → `StatusPill` | |
| `expiration/ExpirationDefaults.kt`, `colors/ContainerColors.kt` | **S** / **E** | |
| `feedback/*` (dialog, snackbar, bottom sheet state) | **Mod** | Patró obligatori del `CLAUDE.md`; `AppFeedbackType.Status` → `CaducityColors`. |
| `scaffold/AppScaffold.kt`, `topbar/StyledTopBars.kt` | **Mod** | Allotjar barra flotant + FAB; `LargeFlexibleTopAppBar`. |
| `calendar/*` (10 fitxers, `kalendar`) | **E** (fase 10) | Es retira l'ús a les fases 5–6; el codi es manté fins a 10 només per no tenir forats intermedis. Vegeu 5.13. |
| `bottomsheet/*` (`ItemActionsBottomSheet` 545 l., `ItemActionsViewModel` 424, `ItemAction`, `ItemActionsState`) | **S** (el nou neix a la 5a; l'antic s'esborra a la 7) | Full d'Accions nou. |
| `bottomsheet/QuantitySelectorDialog.kt` | **M** (només packs antics) | P2. |
| `loading/*`, `responsive/*`, `adaptive/WindowSizeClass.kt` | **M** | Tauleta no s'adapta, però no es trenca (amplada màxima). |
| `config/navigation/BottomSheetScene.kt` | **M** | `SceneStrategy` de Nav3 per a `ModalBottomSheet`; es reutilitza per a Accions i Nova categoria ràpida. |
| `:icons` (≈40 `ImageVector` a mà) | **Mod** | Set de Material Symbols Rounded generat per script (outline + filled) — P14. |
| **N** | | `ConnectedRow`, `StatusPill`, `CategoryShapeIcon`, `ConnectedButtonGroup`, `CaducitySwitch`, `FilterChips`, `QuickDateChips`, `DateInputField`, `FloatingNavBar`, `WeekStrip`. |

### 2.3 Navegació

| Element | Fitxers | Acció | Notes |
|---|---|---|---|
| Back stack únic | `App.kt` (`NavDisplay`, inici = `OnboardingRoute` si `OnboardingFlag.isEnabled()` sinó `DashboardRoute`) | **S** | |
| Rutes velles | `DashboardRoute`, `FilteredItemsRoute.ByStatus`, `CategoryDetailRoute(categoryId, initialProductId)`, `CreateCategoryRoute`, `AiAssistantRoute`, `Settings`, `OnboardingRoute` | **S** | Fase 3. |
| Settings amb `NavDisplay` imbricat | `SettingsContainer.kt`, `SettingsRoute.kt` | **S** | Ajustos és una icona de la barra que obre una pantalla secundària (sense barra), no una pestanya; Privadesa/Còpia/Quant a, subpantalles del back stack principal. |
| Deep links | `DeepLinkAction` (`OpenProduct`, `OpenCategory`; Parcelable per `Intent`), `MainActivity` | **Mod** | **Només es llegeix a `onCreate`**: cal `onNewIntent` + `singleTop`. |
| Manifest | `AndroidManifest.xml` | **Mod** | `shortcuts.xml`, receiver del widget; treure `INTERNET`, `FOREGROUND_SERVICE*`, `SystemForegroundService` (fase 0). |

### 2.4 Pantalles actuals

| Pantalla | Fitxers | Acció |
|---|---|---|
| Splash | (cap) | **N** `SplashScreen` API |
| Dashboard | `ui/screen/dashboard/*` (11 fitxers), `ObtainDashboardUseCase`, `DashboardData` | **S** → Inici |
| Detall de categoria | `ui/screen/category/detail/*` (14), `…/product/*` (9) | **S** → Categoria |
| Afegir ítem | `CategoryDetailAddItem*` (387 + 335 l.) | **S** → Afegeix |
| Accions d'ítem | `ui/components/bottomsheet/*` | **S** → full Accions |
| Filtrat per estat | `ui/screen/filtered/*` (6), `GetItemsByStatusUseCase` | **S** → `StatusListScreen(status)` (Caducats, Aviat, Frescos, Congelats) |
| Crear categoria | `ui/screen/category/create/*` (7; crea amb ítems inicials) | **S** → Nova/Edita categoria |
| Settings | `SettingsRootScreen` (249 l.), `SettingsContainer`, `appearance/`, `notifications/` | **S** → Ajustos |
| Privacy, Backup, About | `settings/privacy/*`, `backup/*`, `about/*` | **Mod** |
| Debug | `settings/debug/*` (ja condicionat a `FLAG_DEBUGGABLE`) | **M** |
| IA | `ui/screen/ai/*`, `settings/ai/*` | **E** (fase 0) |
| Onboarding | `ui/screen/onboarding/*` (13 fitxers, 6 pàgines) | **S** |

### 2.5 Domini i dades

| Element | Estat actual | Acció |
|---|---|---|
| Room | `AppDatabase` v2; només `MIGRATION_1_2` (`ADD COLUMN packSize`); esquemes a `app/schemas/…/{1,2}.json`; sense `fallbackToDestructiveMigration` | **Mod** → v3 |
| `categories` | `id, name, description` NOT NULL | + `shape`, `icon`, `sortOrder` |
| `products` | `id, categoryId, name, createdAt` | **M** |
| `items` | `id, categoryId, identifier, productId?, expirationDate, pausedDate?, remainingDays?, consumedDate?, packSize?` | + `discardedDate`, `createdAt` |
| DAOs | `CategoryDao`, `ItemDao`, `ProductDao` | **Mod**: historial, recomptes, `moveItem`, `discard` |
| `ItemRoomMapper` | calcula `ItemStatus` amb llindar fix i `AppClock`; `toEntity(Item)` posa `consumedDate=null`/`remainingDays=null` (**bug latent** si es reutilitza) | **Mod**: treure el càlcul d'estat |
| `ItemStatus` | sealed: Expired, ExpiringSoon, Fresh, Frozen, Consumed (20 usos); `<` | **Mod**: + `Discarded`; funció pura amb `LocalDate` |
| Llindars | `AppExpirationThresholdsImpl`: 4 dies fixos; `consumeExpiredThreshold` 2 dies | **S**: llindar configurable i reactiu; `consumeExpiredThreshold` **E** |
| Backup | `BackupModels` (`CURRENT_VERSION = 1`), `RoomBackupDataSource`, export/import | **Mod**: v2 |
| Fake data | `FakePlayStoreDataStrategy` (5 categories), `FakeTestDataStrategy` | **Mod**: shape/icon/sortOrder; només debug |
| Hora | `AppClock`, `config/time/*`, `RelativeTimeFormatter`, `LocalizedDateFormatter` | **Mod**: «Fa 2 dies», «dc. 21 oct», «dimecres · d'aquí 10 dies», nom de mes |

#### 2.5.1 Use cases

| Use case | Acció |
|---|---|
| `AddItemToCategoryUseCase`, `CreateProductUseCase` | **S** → `AddItemBatchUseCase(name, categoryId, date, quantity, frozen)`: find-or-create del producte + **N ítems** en una transacció; retorna els ids per al Desfés |
| `ConsumeItemUseCase` | **Mod**: permet caducats (P5); treure `CannotConsumeExpiredInstance` |
| `SplitAndConsumeItemUseCase` | **M**, només per a packs antics (P2) |
| `SplitAndFreeze…`, `SplitAndDelete…` | **E**: Congela/Esborra/Llença actuen sobre l'ítem (un pack antic compta com una unitat) |
| `FreezeItemUseCase`/`UnfreezeItemUseCase` | **Mod**: P4 |
| `RescheduleItemUseCase` | **M** + variant «suma N dies a la data actual» |
| `DeleteItemUseCase` | **M** (és l'«Esborra») |
| `DeleteProductUseCase`, `ClearProductItemsUseCase`, `GetProductItemsUseCase`, `GetCategoryProductsUseCase`, `ProductDeletionStrategy` | **E** (P7) |
| `DeleteCategoryUseCase` | **M** + confirmació amb recompte |
| `CreateCategoryUseCase` | **Mod**: sense ítems inicials; shape/icon/sortOrder; nom únic |
| `ObtainCategoryDetailUseCase`, `ObtainDashboardUseCase`, `GetExpiringCategoriesUseCase`, `GetItemsByStatusUseCase` | **S** |
| **N** | `ObserveItemsUseCase` (BD + llindar + dia), `ObserveHomeUseCase`, `ObservePantryUseCase`, `ObserveCategoryUseCase`, **`ObserveItemsByStatusUseCase(status)`** (substitueix l'`ObserveExpiredUseCase` previst: serveix Caducats, Aviat, Frescos i Congelats), **`ObserveCalendarUseCase`**, `DiscardItemsUseCase`, `UpdateItemUseCase`, `MoveItemToCategoryUseCase`, `GetNameSuggestionsUseCase`, `UndoAddItemsUseCase`, `UpdateCategoryUseCase`, `SeedCategoriesUseCase` |

### 2.6 Preferències

Totes via `com.russhwolf.settings.Settings`.

| Clau actual | Propietari | Estat |
|---|---|---|
| `theme_mode` (LIGHT/DARK/SYSTEM) | `ThemePreferencesImpl` | **M** (= `themeMode`) |
| `use_dynamic_colors` (default false) | idem | **M** (= `dynamicColor`) |
| `theme_tone` | idem | **E** |
| `calendar_first_day_of_week` (DayOfWeek) | `CalendarPreferences` (a `ui/components/calendar`) | **Mod**: el paquet calendari s'elimina; es **mou** a `config/calendar/FirstDayOfWeekPreferences` (mateixa clau) a la fase 5c; opcions Dilluns/Dissabte/Diumenge (= `firstDayOfWeek`); afecta la vista de mes |
| `notifications_enabled_key` (default false, sincronitzada amb el permís) | helper + `SettingsNotificationConfigDataSource` | **Mod** (= `dailyReminderEnabled`) |
| `notifications_expired_enabled_key`, `notifications_expiring_soon_enabled_key` | helper | **E** |
| `notification_time_seconds` (default **12:00**) | `NotificationConfigDataSource` | **M** clau; default nou 09:00 |
| `fire-and-forget-user_onboarding` (FireAndForget, `autoDisable=false`; la clau real porta el prefix `fire-and-forget-`) | `OnboardingFlag` | **M** (= `onboardingCompleted`, invers) |
| `analytics_consent` (FireAndForget), `consent_analytics_storage`, `consent_ad_*`, `consent_given` | `feature/consent/*` | **M** (`analyticsEnabled` = `consent_analytics_storage`) |
| `ai_selected_model` i claus del worker de models | `AiModelPreferences` | **E** (fase 0) |
| `debug_remote_config_*` | debug | **M** |
| `ShowAppReviewFlag` | `feature/review` | **M** (revisar quan es dispara) |
| **Noves** | | `soon_threshold_days` (3), `disclaimer_accepted_version`, `disclaimer_accepted_at`, `WidgetTipFlag` (FireAndForget), `prefs_version` |

### 2.7 Notificacions i WorkManager

| Element | Fitxers | Acció |
|---|---|---|
| Worker diari | `ExpirationCheckWorker` (un notif. per producte + resum per categoria, per Soon i Expired) | **S** (fase 8): una notificació al dia (títol «N productes caduquen aviat», cos amb els 2–3 més urgents), només si hi ha SOON o «avui» |
| Planificador | `ExpirationWorkSchedulerImpl` (periòdic 24 h, `CANCEL_AND_REENQUEUE`), `NotificationDelayCalculator` | **M** (té test) |
| Helper | `AndroidExpirationNotificationHelper` (289 l.) | **Mod**: sense interruptors expired/soon ni grups; tap → `home` |
| Canals | `NotificationChannelManager`: `product_expiring_soon`, `product_expired`, textos en anglès hardcoded | **Mod**: un canal; textos a `strings.xml`; esborrar els antics |
| Permís | `registerContracts()` amb `rememberLauncherForActivityResult` | **Mod**: reutilitzat des de l'onboarding |

### 2.8 Icona, splash, widget, dreceres
No hi ha widget, dreceres ni splash propi. Icona: `res/drawable/ic_launcher*.xml` + `ic_launcher_foreground.xml`; debug amb fons vermell a `app/src/debug/res/`. Sense icona monocroma verificada (es comprova a la fase 12).

### 2.9 Textos i idiomes
- **en** (`values/`, 407 l.), **es** (368), **ca** (369), `locales_config.xml` (en, es, ca); `debug/res` té 3 `strings.xml` petits.
- L'especificació és en català: s'escriu **ca** i es tradueix a **en** i **es**. Calen `<plurals>`.
- Hardcoded en anglès: canals de notificació. No he vist selector d'idioma dins l'app.
- Doc legal: `privacy_policy.md`, `terms_and_conditions.md`.

### 2.10 Tests existents (`app/src/test`, només JVM, 2.569 l.)

| Test | Acció |
|---|---|
| `DashboardViewModelTest` (495) | **S** → `HomeViewModelTest` |
| `FilteredItemsByStatusViewModelTest` (400) | **S** → `StatusListViewModelTest` |
| `ProductPageViewModelTest` (512) + `FakeItemDao`, `FakeProductDao` | **E** (els fakes es reaprofiten si cal) |
| `DashboardActionsTest`, `EventTrackerTest` | **Mod** / **M** |
| `ShapePositionTest` (180) | **Mod** |
| `NotificationDelayCalculatorTest`, `ConsentPreferencesTest` | **M** |
| `AiJaroWinklerMatcherTest` (308), `LlamatikGroceryParserTest` (148) | **E** (fase 0) |

No hi ha `room-testing`, `androidTest` ni tests de captura; `robolectric = "4.17"` és al catàleg però sense ús.

### 2.11 Tracking i CI
`feature/tracking/Actions.kt` (20) i `Screens.kt` (18): alinear-les amb les rutes noves seguint el `CLAUDE.md` (`NavigationAction`, `origin`/`source`, mai dades d'usuari). El calendari afegeix `nav_calendar` (`origin = home | category`, `source = button`) i `calendar_day_select` (sense dades d'usuari). CI: `pr.yml`, `main.yml`, `release.yml`; release amb R8: regles per a Glance.

---

## 3. Mapatge pantalla actual → pantalla nova

### 3.1 Taula

| Actual | Nou | Fase |
|---|---|---|
| `DashboardScreen` + `DashboardSuccessContentList` + `DashboardSummaryCard` + calendari per categoria | `HomeScreen` (capçalera + `WeekStrip` + bàner + resum + seccions), `HomeEmptyState` | 5 |
| (calendari antic) | `CalendarScreen` (`Calendar(categoryId?, date?)`), `WeekStrip`, `CalendarMonthGrid` | 5 (i entrada des de Categoria a la 6) |
| `ItemActionsBottomSheet` + `QuantitySelectorDialog` + DatePicker | `ItemActionsSheet` | **5a** (l'antic s'elimina a la 7) |
| Secció de categories del dashboard | `PantryScreen` | 6 |
| `CategoryDetailScreen` (+ `Container`, `ProductTabContent`, `ProductActionsSplitButton`, calendari setmanal) | `CategoryScreen` (capçalera, `WeekStrip` filtrada, xips, seccions) | 6 |
| `FilteredItemsByStatusScreen` (+ `ProductItemsBottomSheet`) | `StatusListScreen(status)` (variants `EXPIRED`, `SOON`, `FRESH`, `FROZEN`) | 7 |
| Estats ≠ Expired de `FilteredItemsRoute.ByStatus` | filtre dins `CategoryScreen` / Rebost | 6 |
| `CreateCategoryScreen` | `CategoryEditorScreen` + `QuickCategorySheet` (sheet a la 4, pantalla a la 8) | 4 / 8 |
| `CategoryDetailAddItemScreen` | `AddScreen` (+ tanda, edició) | 4 |
| `SettingsRootScreen` + Appearance + Notifications | `SettingsScreen` | 8 |
| `OnboardingScreen` + 6 pàgines | `OnboardingScreen` + 5 passos | 9 |
| (cap) | `SplashScreen`, widget Glance, dreceres | 12 |
| `AiAssistantScreen`, `AiModelSettingsScreen` | — | 0 |

### 3.2 Funcionalitats actuals sense equivalent al disseny

| Funcionalitat actual | Situació |
|---|---|
| Assistent d'IA | Eliminat (P1) |
| Eliminar un **producte**, estratègies `ProductDeletionStrategy`, swipe a pestanyes | Eliminat (P7) |
| Netejar consumits (`clearConsumedItems`) | Eliminat (P7); no es crida mai més perquè esborraria l'historial d'autocompletat |
| Consumir **part** d'un pack | Només per a packs antics (P2) |
| Ítems inicials en crear categoria | Desapareix |
| Camp «Identificador» lliure | Desapareix; «Unitat N» és derivat |
| Dos interruptors de notificacions | Desapareix |
| Calendari setmanal/mensual per categoria | Substituït per tira de 7 dies + vista de mes (11-calendari.md) |

---

## 4. Estat del paquet de redisseny (discrepàncies detectades)

`PROMPT-ACTUALITZAR-PLA.md` diu que s'han actualitzat `01-navegacio.md` (ruta `calendar`), `02-migracio-pantalles.md`, `04-model-de-dades.md` (quantitat = N ítems), `05-textos.md` (textos del calendari) i `08-decisions-i-obertes.md`. **A la carpeta, aquests cinc fitxers segueixen sent la versió anterior**: no hi ha la ruta `calendar`, `08` encara diu «calendari pendent de disseny», `04` no menciona N ítems i `05` no té cap text de calendari. Sí que estan actualitzats `01-inici.md`, `03-categoria.md`, `11-calendari.md`, `README.md`, `PROMPT-*` i `RESPOSTES-AL-PLA.md`, i les captures `Main.png`, `Categoria.png`, `CalMes.png`.

**Tractament:** aquest pla és la font de veritat per a aquests punts (ruta `calendar`: fase 3; textos del calendari: 5.13 i fase 10; model/quantitat: 5.4). Decidit (P21): s'alineen amb les decisions al **primer commit de la fase 0** (`docs: alinear especificació amb decisions`), sense canviar cap decisió de disseny. Fins llavors, aquest pla preval.

---

## 5. Buits i conflictes entre especificació i codi

### 5.1 Final de vida de l'ítem
- Avui: Consumit = `consumedDate != null` (fila conservada); Esborra = `DELETE`.
- Disseny: Consumit / Llença / Esborra.
- Resolució: afegir `discardedDate` (nullable) i `ItemStatus.Discarded`; sense `endReason` (no cal reconstruir la taula). **Actiu** = `consumedDate IS NULL AND discardedDate IS NULL`; es crea `Item.isActive` per no repartir `!= Consumed` (20 usos). **`DISCARDED` es desa** (resposta a l'obert #2 de l'especificació).

### 5.2 «Aviat»
- Avui: `expirationDate.date() < (now + 4 dies).date()`; llindar constant al mapper. Disseny: `avui <= data <= avui + llindar`, llindar ∈ {1, 3, 7}.
- Equivalència: llindar 3 amb `<=` ≡ comportament actual ⇒ `soon_threshold_days = 3` per defecte i per als existents, sense canvi visible.
- Problemes: estat fixat en emetre (no es refresca a mitjanit ni en canviar el llindar). Solució: `ExpiryStatus.of(expiry: LocalDate, today: LocalDate, thresholdDays: Int, frozen: Boolean)` pura + `TodayProvider` (Flow que emet a mitjanit i a `ACTION_DATE_CHANGED`/`TIMEZONE_CHANGED`) combinat amb el Flow del llindar. Els mappers de Room retornen dades; l'estat el calculen els `Observe*`.
- **Zona horària:** `expirationDate` és un `Long` (Instant) i el `DatePicker` retorna mitjanit UTC; `Instant.date()` usa la zona del sistema ⇒ possible desfasament d'un dia a zones UTC−. Definim que **la data de caducitat és un `LocalDate`** al domini, escrita com a mitjanit local, normalitzada a l'entrada (teclat i DatePicker), amb tests. No canvia el tipus de columna.

### 5.3 Productes, ítems «sense producte» i «Unitat N»
- Avui: `productId` nullable; els ítems sense producte tenen el nom a `identifier`.
- Disseny: nom+categoria+data sempre; 1 unitat = fila simple; N unitats = desplegable.
- Resolució: (a) en afegir, **find-or-create** de `Product` per `(categoryId, nom normalitzat)`, ítems amb `identifier = ""`; (b) la migració 2→3 converteix els ítems sense producte en productes (6.1); (c) `productId` roman nullable a l'esquema però la UI el tracta com a obligatori (fallback: nom = `identifier` o «Producte»); (d) la UI **ignora sempre `identifier`**.
- «Unitat N» = posició dins el producte ordenada per `(expirationDate, createdAt, id)` (estable i derivada).

### 5.4 Quantitat (P2 = opció B)
- El selector de **Quantitat** crea **N ítems** amb la mateixa data, `packSize = null`, en una transacció. Límit màxim per alta: vegeu P22. En mode edició el selector s'amaga: s'edita **una** unitat (P20).
- Consumit, Congela, Llença, Canvia la data i Esborra actuen sobre **1 unitat**. Inici: ✓ consumeix la unitat més propera del (producte, dia).
- **Ítems antics amb `packSize > 1`** (sense migrar-los): es presenten com a «pack de N» (meta de fila i Accions), compten com **una** entrada als recomptes i conserven **Consumit parcial** (`SplitAndConsume` + `QuantitySelectorDialog`). Congela/Llença/Esborra/Canvia data actuen sobre el pack sencer. No se'n creen de nous; l'edició d'un pack el deixa tal com és (el selector no s'hi mostra).
- Desfés de l'alta en tanda: elimina els N ítems creats (i el producte si l'ha creat l'acció i queda buit).
- Backup: `packSize` es conserva; sense camps nous per a quantitat.

### 5.5 Historial per autocompletar
- Els ítems no tenen `createdAt` ⇒ no es pot ordenar per «últim ús». Solució: `items.createdAt INTEGER NULL` (null per a files antigues; s'ordena per `COALESCE(createdAt, expirationDate)`).
- Consulta: productes per nom que **comencen** o **contenen** el text (sense accents ni majúscules), amb freqüència (`COUNT` d'ítems, inclosos consumits i llençats) i `MAX(createdAt)`; retorna `(productId, name, categoryId)`; màx. 3.
- La data **no** s'omple en triar un suggeriment. Nom exacte ja existent a la categoria ⇒ noves unitats al producte.
- **Conflicte resolt:** `clearConsumedItems` s'elimina (P7), per no perdre historial.

### 5.6 Congelar (P4)
- Avui: `pausedDate` + `remainingDays`; `unfreeze` → `ara + remainingDays`; no es congela un caducat.
- Disseny: «Congelat» a Afegeix; Descongela «demana nova data».
- Resolució: Descongela proposa `ara + remainingDays` prellenat i **editable**. A Afegeix, «Congelat» desa `pausedDate = ara` i `remainingDays = dies fins a la data`; amb «Congelat» no s'accepta data passada (fora de «Congelat», sí — P15).

### 5.7 Consumit sobre caducats (P5)
Permès sempre des d'Accions. La variant `EXPIRED` de la pantalla d'estat només ofereix Llença / Canvia la data; `SOON`, `FRESH` i `FROZEN` ofereixen Consumit (✓) com a primer botó. `consumeExpiredThreshold` (2 dies) **E**.

### 5.8 Persistència, tanda i desfer
Mode tanda: estat al ViewModel (els ítems ja són a la BD). Desfés: Afegeix → elimina els ítems de l'últim desat; Consumit/Llença → neteja `consumedDate`/`discardedDate`; data ràpida → restaura el valor previ. Totes via `SnackbarState` (`CLAUDE.md`).

### 5.9 Navegació
- Nav3 no té pestanyes natives: `TopLevelBackStack` pròpia (3 pestanyes, estat de scroll/cerca de Rebost conservat).
- Rutes no recollides a l'especificació, afegides pel pla: **`calendar`** (`categoryId?`, `date?`), **`add?itemId=`** (edició), selector de categoria d'«Mou», selecció múltiple a les pantalles d'estat, **`StatusList(status)`** (`status/{soon|fresh|frozen}`; la ruta `expired` es manté com a àlies de `StatusList(EXPIRED)` per a dreceres, notificacions i deep links).
- Drecera/widget amb l'app oberta: `onNewIntent` + `launchMode="singleTop"`.

### 5.10 Preferències d'usuaris existents → 6.3.

### 5.11 Tipografia i icones
- Fonts: afegir Bricolage Grotesque i Figtree variables (`FontVariation.width(80f)` per a Bricolage; verificar a la fase 1 que el TTF exposa `wdth`).
- Glance sense fonts custom ⇒ widget amb tipografia del sistema en negreta (P13).
- Material Symbols Rounded amb eix `FILL`: `ImageVector` generats per script (P14).

### 5.12 Altres detalls
- `ShapePosition` ja existeix i es reaprofita.
- El `README.md` del repo descriu l'app com a «Kotlin Multiplatform… commonMain/desktopMain/webMain» (obsolet) → corregir a la fase 10.
- Els recomptes de la barra d'estats de Rebost es calculen a la mateixa consulta que els recomptes per categoria.

### 5.13 Calendari: tira de 7 dies i vista de mes (nou)

**Dades.** `ObserveCalendarUseCase(range: ClosedRange<LocalDate>, categoryId: String?)` → `Map<LocalDate, List<ItemWithStatus>>` d'ítems **actius** (ni consumits ni llençats) **sense congelats** (no tenen data efectiva). Agrupa per data local amb `TodayProvider` i `ExpiryStatus`. S'implementa observant el Flow d'ítems actius i filtrant/agrupant en memòria (el volum és de centenars de files; evita SQL dependent de la zona horària). La tira usa `avui..avui+6` (i els 7 següents en lliscar); el mes, `primer..últim dia visible de la graella`.

**`kalendar` o graella pròpia? → graella pròpia.** Justificació:
1. **La tira de 7 dies no és una setmana de calendari**: és una finestra mòbil que comença avui i es pagina de 7 en 7. `kalendar` `WeekCalendar` s'alinea al primer dia de la setmana; aconseguir-ho exigiria forçar l'API.
2. **La vista de mes és trivial** (cap rang infinit, cap esdeveniment multi-dia, cap cel·la de mes tancat): un `HorizontalPager` de `YearMonth` amb una graella de 7 columnes i càlcul de `LocalDate` amb `kotlinx-datetime`. És una funció pura testable (`CalendarMonthModel(yearMonth, firstDayOfWeek)`).
3. **El codi actual de `kalendar` és de 10 fitxers acoblats al disseny antic** (`AppCalendarConfig`, `DayContent`, headers, previews); reutilitzar-lo obliga a mantenir-lo i a adaptar-lo; reescriure és més petit que adaptar-lo.
4. **Elimina una dependència** multiplataforma (`compose-multiplatform`) que arrossega Compose Multiplatform sobre Android i és una font de fricció a cada pujada de Kotlin/Compose.
5. Control total de l'accessibilitat (`Role.Tab`/`Role.Button`, descripcions per cel·la) i del refresc a mitjanit.

Conseqüència: **fase 10 elimina** `ui/components/calendar/**`, `CalendarData` i la dependència `kalendar`; `CalendarPreferences` es mou abans (fase 5c) a `config/calendar/FirstDayOfWeekPreferences`.

**`WeekStrip`** (component reutilitzable; Inici i Categoria): 7 columnes iguals, gap 4 dp, sense targeta, marge −4 dp; per dia: inicial localitzada (12 sp, 600), número (Bricolage 18/800) en cercle de 34 dp, fila de punts de 6 dp (gap 3, **màx. 3**, color fort `soon`/`fresh`, mai `expired`); avui: fons `surface-card` + vora 2 dp `ink`; seleccionat: `primary`/`on-primary`; tornar a tocar el dia deselecciona; `selectableGroup` + `Role.Tab`; es refresca a mitjanit.
**Vista de mes** (`Calendar(categoryId?, date?)`): barra amb «Octubre 2026» (Bricolage 30/800) i fletxes; lliscar canvia de mes; amb `categoryId` el nom de la categoria al costat; targeta del mes (radi 24) que **respecta «La setmana comença»**; cel·la 52 dp sense fons, amb els estats (avui = vora 2 dp; seleccionat, també si és avui = fons `primary`) només sobre el cercle de 34 dp del número, i punts de 5 dp (màx. 3) a sota; **dies passats amb punts `expired`**; títol del dia «Avui»/«Demà» amb subtítol «diumenge 11 · N productes», o «Dimecres 21» + «d'aquí N dies · N productes»; llegenda Caducat · Aviat · Fresc; llista del dia seleccionat (per defecte avui) amb fila → full d'Accions; sense barra flotant ni FAB.

**Decisions sobre punts ambigus de l'especificació** (resoltes, P17–P21):
- **P17.** «Productes» al calendari (punts, «2 productes», `ObserveCalendarUseCase`) compta **unitats** (ítems); un pack antic compta 1.
- **P18.** Tocar un dia de la tira de l'Inici sense secció `SOON`: **obre la vista de mes amb aquell dia seleccionat** (`Calendar(date=)`).
- **P19.** Tocar un dia de la tira de Categoria: **filtra** per aquell dia (vegeu `11-calendari.md › Dia seleccionat a Categoria`); tornar a tocar el dia, el xip o la ✕ treu el filtre.
- **P20.** Mode edició (`add?itemId=`): edita **una unitat**; canviar-ne el nom la mou al producte homònim o, si no n'hi ha cap a la categoria, en **crea un de nou**; si el producte antic queda sense unitats actives, s'amaga però es conserva; el selector de quantitat s'amaga.
- **P21.** Alineació de l'especificació: vegeu secció 4.

---

### 5.14 Marge d'«Aviat» per categoria (nou, 2026-10-11)

Font: `pantalles/14-marge-per-categoria.md` (captures `Marge*.png`). Cada categoria pot tenir el seu marge (`soonThresholdDays: Int?`); si és `null` val el llindar global, que passa a dir-se «"Aviat" per defecte». `SOON` = `avui <= data <= avui + marge`.

- **On es calcula l'estat:** avui `ExpiryStatus.of(expiry, today, thresholdDays, frozen)` rep el llindar global i `ObserveItemsUseCase` el combina amb `ExpirationThresholds.soonThresholdDays`. Cal que `ActiveItem` porti el marge de la seva categoria (`categorySoonDays: Int?`) i que `PantryEntry` exposi el marge **efectiu**; l'`ExpiryStatusCalculator` de l'especificació és la mateixa funció pura amb un paràmetre més (es manté `ExpiryStatus.of` com a nucli; no es duplica el càlcul).
- **Llocs que filtren per `SOON`** (tots han de fer servir el marge efectiu; es revisen amb `git grep ExpiryStatus.SOON`): `ObserveHomeUseCase` (secció per dia, casella Aviat), `ObserveItemsByStatusUseCase` (pantalla Aviat), `ObserveWeekStripUseCase`/`ObserveCalendarUseCase` (punts i colors), `ObserveCategoryUseCase`/`ObservePantryUseCase` (recomptes), `NotificationContentBuilder` (fase 8), widget (fase 12, `WidgetStateMapper`).
- **Notificació:** el criteri canvia a «passa a `SOON` avui (`data − marge == avui`) o caduca avui/demà»; `NotificationContentBuilder` rep el marge efectiu per entrada.
- **Preferència global:** `soonThresholdDays` es manté (1/3/7) com a per defecte; l'escala de parades (1 dia … 3 mesos, `ThresholdStops`) només s'aplica al marge per categoria.

**Punts que xoquen amb el que ja hi ha (cal decidir):**
1. **Usuaris existents: decidit (2026-10-11) que no canvia res.** Totes les categories existents queden amb `soonThresholdDays = null` i segueixen el per defecte (3 dies), així que «llindar 3 = sense canvi visible» (5.2) continua valent. Els valors suggerits (p. ex. Rebost 1 mes = 30 dies, de la taula de l'especificació) només s'apliquen a les categories noves de l'onboarding i al botó «Torna als valors suggerits». No hi ha, doncs, cap omplert a la migració.
2. **Ajustos (fase 8, ja feta)** té «"Aviat" vol dir» i l'editor no té «Avisa'm abans»: es modifiquen a la 11 (no es reobre la 8).
3. **Backup:** `BackupCategory` guanya `soonThresholdDays` nullable. La versió 2 del backup encara no s'ha publicat, així que **s'amplia la v2 sense pujar `CURRENT_VERSION`**; els fitxers sense el camp → `null`.
4. **Inici (fase 5b, feta):** les seccions per dia passen a arribar només fins a `avui + 6` i la resta de `SOON` va a «Més endavant»; canvia `HomeData`/`ObserveHomeUseCase` i `HomeScreen`.
5. **Pantalla Aviat (fase 7, feta):** canvia la frase d'ajuda (ara no hi ha un únic N) i cada grup rep la pastilla «Avisa N abans».
6. El paquet del canvi es va fer sobre una còpia més antiga dels documents: només s'han fusionat els trossos del marge; `04-model-de-dades.md`, `05-textos.md`, `08-decisions-i-obertes.md` i `README.md` conserven la resta del que ja hi havia (unitats, calendari, mode fosc…). Les preferències segueixen sent `multiplatform-settings`, no DataStore.

## 6. Migracions

### 6.1 Room v2 → v3 (`MIGRATION_2_3`)

Només `ALTER TABLE ADD COLUMN` + `UPDATE`/`INSERT`; sense reconstruir taules (no es perden claus foranes). Les columnes noves porten `@ColumnInfo(defaultValue = …)` perquè l'esquema que valida Room coincideixi.

```sql
ALTER TABLE categories ADD COLUMN shape TEXT NOT NULL DEFAULT 'COOKIE';
ALTER TABLE categories ADD COLUMN icon TEXT NOT NULL DEFAULT 'eco';
ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0;
ALTER TABLE items ADD COLUMN discardedDate INTEGER;
ALTER TABLE items ADD COLUMN createdAt INTEGER;
```

Després, a `migrate()` amb Kotlin (testable):
1. `sortOrder`: `0..n-1` per `rowid`.
2. `shape`/`icon` per nom (normalitzat, sense accents), en/es/ca:

   | Paraules clau | Forma | Icona |
   |---|---|---|
   | dairy, lácteos, lactics, llet, leche | `COOKIE` | `water_full` |
   | produce, fruit, fruta, verdura, fruita | `CLOVER8` | `nutrition` |
   | meat, fish, carne, pescado, carn, peix | `CLOVER` | `set_meal` |
   | pantry, despensa, rebost | `SUNNY` | `grocery` |
   | bakery, panader, fleca, pan | `PUFFY` | `bakery_dining` |
   | drink, bebida, beguda | `BURST` | `local_drink` |
   | frozen, congelad | `BURST` | `icecream` |
   | sense coincidència | cicle `COOKIE, CLOVER8, CLOVER, SUNNY, PUFFY, BURST` per `sortOrder` | `eco` |

3. **Ítems sense producte → productes:** per cada `(categoryId, identifier)` amb `productId IS NULL AND identifier <> ''`, inserir un `Product` (`id` UUID en Kotlin, `name = identifier`, `createdAt = MIN(expirationDate)`), assignar `productId`.
4. **Buidat d'`identifier` (decidit):** `UPDATE items SET identifier = '' WHERE productId IS NOT NULL` (inclou els ítems que ja tenien producte, p. ex. «Cheddar Cheese #1», i els convertits al pas 3). Es perd l'etiqueta lliure antiga. La UI **a més** ignora sempre `identifier` (doble seguretat).
5. `createdAt` queda `NULL` per a files antigues.

`@Database(version = 3)`, `addMigrations(MIGRATION_1_2, MIGRATION_2_3)`, `3.json` exportat; **mai** `fallbackToDestructiveMigration`. Les migracions es mouen de `PlatformModule` a `data/datasource/room/Migrations.kt`.

**Backup v2:** `CURRENT_VERSION = 2`; `BackupCategory` + `shape`, `icon`, `sortOrder`; `BackupProductItem` + `discardedDate`, `createdAt`; `packSize` es manté. En importar v1: aplicar el mateix mapeig per nom (funció compartida `CategoryDefaults.forName`), convertir ítems sense producte i buidar `identifier`. Un backup v2 no s'importa en versions velles (ja ho valida `version > CURRENT`).

### 6.2 Seguretat de dades
Test de migració amb un esquema v2 poblat; `PRAGMA foreign_key_check` després de migrar; prova manual amb un dump de producció.

### 6.3 Preferències (`PreferencesMigrator`, versió `prefs_version`)
S'executa a `CaducityApplication.onCreate()` abans de `scheduleExpirationCheck`; idempotent; cada pas es marca amb `prefs_version`.

| Preferència | Instal·lació nova | Usuari existent (`OnboardingFlag` ja desactivat) |
|---|---|---|
| `onboardingCompleted` | false | true (no es reprodueix l'onboarding) |
| `soonThresholdDays` | 3 | **3** (equival al càlcul actual) |
| `dailyReminderEnabled` | segons permís a l'onboarding | `notifications_enabled_key && (expiring_soon \|\| expired)` |
| `dailyReminderTime` | 09:00 | conservar; si no hi havia clau, **escriure 12:00 explícit** (evita canviar-lo en silenci) |
| `analyticsEnabled` | false | conservar `consent_analytics_storage` |
| `disclaimerAcceptedVersion/At` | es desen a l'onboarding | **absents** → es mostra només el pas d'avís (P3) |
| `themeMode`, `dynamicColor` | SYSTEM / false | conservar |
| `firstDayOfWeek` | MONDAY | conservar; valors fora de Dilluns/Dissabte/Diumenge → Dilluns |
| `widgetTipDismissed` | false | false |
| `theme_tone`, `notifications_expired_enabled_key`, `notifications_expiring_soon_enabled_key`, `ai_*` | — | **eliminar claus** |

El migrador té **passos versionats** per respectar la regla d'eliminació: el **pas 1 (fase 2)** fixa `soonThresholdDays`, esborra `theme_tone` i `ai_*` i prepara les claus noves; el **pas 2 (fase 8)** deriva `dailyReminderEnabled`/`dailyReminderTime` de les claus antigues i esborra `notifications_expired_enabled_key` i `notifications_expiring_soon_enabled_key`. Els canals antics (`product_expiring_soon`, `product_expired`) s'esborren a la **fase 8** (no al migrador), quan ja no hi ha codi que els recreï.

---

### 6.4 Marge per categoria dins la migració v2 → v3 (decidit 2026-10-11)

**Decisió: dins la `MIGRATION_2_3` (6.1), sense v4.** La v3 no s'ha publicat mai (l'últim tag és `0.9.0`, a Room v2), així que cap usuari de producció la veurà. S'afegeix una línia a la migració i s'editen `CategoryRoomEntity`/`3.json`:

```sql
ALTER TABLE categories ADD COLUMN soonThresholdDays INTEGER;   -- NULL = per defecte
```
- `CategoryRoomEntity.soonThresholdDays: Int? = null` (sense `defaultValue`); `@Database(version = 3)` es manté; es **regenera `3.json`**.
- Cap `UPDATE`: totes les categories existents queden a `null` (5.14.1).
- **Cost, només per a les instal·lacions de desenvolupament:** una base de dades ja creada amb la v3 antiga (emuladors i mòbils amb builds de `main`) no es torna a migrar i Room la rebutja per hash diferent. Cal **desinstal·lar l'app o esborrar-ne les dades** una vegada. Per això la 11 ho ha d'anunciar en acabar.
- **Suggerits:** taula única `SuggestedThresholds` (Carn i peix 2, Fleca 2, Làctics 3, Fruita i verdura 3, Embotits 3, Plats preparats 2, Begudes 7, Rebost 30) usada per l'onboarding (`SeedCategoriesUseCase`) i per «Torna als valors suggerits».
- **Backup:** vegeu 5.14.3. Tests: `Migration2To3Test` ampliat (columna nova, categories existents a `null`; 1→3 inclòs) i `BackupTest` ampliat.

## 7. Textos nous no recollits a `05-textos.md` (calendari)

Proposta de claus (ca; es traduiran a en/es a la fase 10):

| Clau | Text |
|---|---|
| `home_calendar` | Calendari (contentDescription del botó) |
| `home_search` | Cerca (contentDescription) |
| `home_week_strip` | Pròxims 7 dies |
| `week_day_desc` | %1$s, %2$d productes (plurals: 1 producte) · `week_day_desc_none`: %1$s, res |
| `week_day_initials` | dl, dt, dc, dj, dv, ds, dg |
| `calendar_title_month` | Octubre 2026 (format localitzat) |
| `calendar_month_prev` / `_next` | Mes anterior / Mes següent |
| `calendar_back` | Torna |
| `calendar_day_title` | Dimecres 21 |
| `calendar_day_subtitle` | d'aquí 10 dies · 2 productes |
| `calendar_day_empty` | Res caduca aquest dia |
| `calendar_row_meta` | %1$s · unitat %2$d de %3$d |
| `calendar_cell_desc` | %1$s, %2$d productes (plurals) |
| `calendar_legend_expired` / `_soon` / `_fresh` | Caducat / Aviat / Fresc (reutilitzen `status_*`) |
| `category_calendar_cd` | Calendari de %s |
| `item_pack_of` | pack de %d |
| `item_units` | %d unitats (plurals: 1 unitat) |

---

## 8. Preguntes

Totes les P1–P21 estan resoltes (secció 1). Només queda la hipòtesi menor P22 (secció 14), que no bloqueja cap fase.

---

## 9. Fases

### Regles de treball (obligatòries, abans de començar qualsevol fase)

1. **Tasques concretes dins de cada fase.** Cada fase es descompon en tasques petites i verificables abans de tocar codi; es comparteix la llista amb l'usuari.
2. **Mai començar una fase sense permís explícit de l'usuari.** En acabar una fase s'atura i es demana permís per a la següent; el permís per a una fase no val per a les altres.
3. **Cada fase es prova a l'emulador**, tant per l'usuari com per Claude. Claude ho comprova de veritat (instal·lar, arrencar, navegar, captures de pantalla, `logcat` sense errors/crashos) i adjunta l'evidència al resum de fase. L'usuari valida a l'emulador abans de donar la fase per tancada.
4. **Migracions de BBDD (Room, backup, preferències): proves abans i després.** Es poblen dades estrictes i rellevants (casos límit inclosos), s'executa el codi i els tests **abans** de migrar i **després**, tants cops com calgui fins a tenir seguretat, comprovant integritat (`PRAGMA foreign_key_check`, recomptes, valors esperats). Mai `fallbackToDestructiveMigration`. Cal una còpia/dump prèvia de les dades de prova.
5. **Sense branques: tot a `main`.** Es fa commit en acabar cada tasca, i abans si el canvi és gran o perillós (esborrats massius, migracions). Cada commit compila i passa `./gradlew ktlintCheck test assembleDebug`; missatges en el format del repo, amb atribució.
6. **Ordre de tancament de fase:** tests verds → prova a l'emulador (Claude, amb evidència) → validació de l'usuari → commit(s) → **actualitzar la taula «Estat de les fases»** → aturada i petició de permís per a la següent fase.

Totes: `./gradlew ktlintCheck test assembleDebug` verd, app arrencable i commit(s) abans de passar a la següent.

**Regla d'eliminació:** un «Eliminar» només es fa a la fase on **mor l'últim consumidor** de l'element; si encara en té, primer es reconnecta. Cada fase ho comprova amb `git grep` abans d'esborrar. Els elements que cal conservar temporalment es marquen `// TEMP(fase N)` amb la fase on s'esborren. Tots els «Eliminar» d'aquest document s'han revisat contra aquesta regla.

### Regla de tauleta per a les pantalles noves (decidida: opció A, B posposada)

- Tauleta **no es dissenya** en aquest redisseny. Es **accepta una regressió temporal**: en tauleta, layout de mòbil centrat amb amplada màxima.
- **Fase 1:** es conserven `ui/adaptive/WindowSizeClass.kt` i `ResponsiveCenteredContainer`.
- **Totes les pantalles noves (fases 4–9):** el contingut va dins `ResponsiveCenteredContainer` (**600 dp** per a llistes i formularis; Rebost pot arribar a **~840 dp**). La barra flotant i el FAB s'alineen amb el **contingut centrat**, no amb les vores de la pantalla.
- **Rebost (fase 6):** la graella usa `calculateGridColumns()` (2 en mòbil, 3–4 en Medium/Expanded; s'ajusta el helper si cal).
- **Regressió coneguda (documentada):** `AdaptiveCalendar`, el panell lateral de calendari de `CategoryDetailScreen` i les variants expandides del Dashboard (`DashboardSummaryCard`, `DashboardSuccessContentList`) **s'eliminen amb les seves pantalles** (fases 5, 6, 10).
- **Comprovació:** cada fase d'UI es prova també, si és possible, en un emulador de tauleta només per verificar que no es trenca (contingut centrat, sense talls).

### Fase 0 — Preparació i neteja (nova)
**Objectiu:** terreny net abans de redissenyar.
- Esborrar `feature/ai/**`, `ui/screen/ai/**`, `settings/ai/**`, registres a `AppModule`/`WorkerModule`, entrades a `Dashboard*`/`SettingsRootScreen`/`App.kt`/`Actions.kt`; treure `llamatik` i `firebase-ai` de `libs.versions.toml`/`build.gradle.kts`; del manifest `INTERNET`, `FOREGROUND_SERVICE*`, `SystemForegroundService`; 2 tests d'IA; strings d'IA. Comprovar si `firebase-appcheck-*` només servia per a IA (si sí, fora, i el codi d'AppCheck a `CaducityApplication`).
- Infra de tests: `room-testing`, Robolectric activat (`isIncludeAndroidResources`, esquemes com a assets de test).
- Spike (no es commiteja): compilar `MaterialShapes.{Cookie9Sided,Clover8Leaf,Clover4Leaf,Sunny,Puffy,SoftBurst}`, `HorizontalFloatingToolbar` (+ `exitAlwaysScrollBehavior`), `ButtonGroup`, `LargeFlexibleTopAppBar`, `FontVariation.width` a `material3 1.5.0-beta01`.
- **Primer commit** (P21): `docs: alinear especificació amb decisions` — alinear `01-navegacio.md`, `02-migracio-pantalles.md`, `04-model-de-dades.md`, `05-textos.md` i `08-decisions-i-obertes.md` amb les decisions ja preses (sense canviar cap decisió de disseny). Abans de fer-lo, es comunica a l'usuari quins fitxers es toquen i què hi canvia.
- **Riscos:** acoblament de `firebase-ai`/AppCheck; mitigació: grep per símbol abans d'esborrar.
- **Acceptació:** l'app arrenca; sense cap entrada d'IA; APK debug més petit.
- **Tests:** els 2 d'IA s'eliminen; la resta verds.

### Fase 1 — Fonaments: tokens, tema, components base
**Objectiu:** tot el que es veu hereta la paleta nova; components base reutilitzables amb previews.
- **Crear:** `CaducityColors`/`StatusColors`/`ExpiryStatus.colors()`, `CaducityShapes`, `Fonts` + `res/font/bricolage_grotesque.ttf`, `figtree.ttf`, `CategoryShapes` (`CategoryShape` → `MaterialShapes`), components: `ConnectedRow`, `connectedShape`, `StatusPill`, `CategoryShapeIcon`, `ConnectedButtonGroup`, `CaducitySwitch`, `FilterChips`, `QuickDateChips`; pipeline d'icones (P14) amb set inicial (≈60 icones: nav, estat, accions, categoria, `calendar_month`, `chevron_*`).
- **Modificar:** `Color.kt`, `ColorSchemes.kt`, `Typography.kt` (13 estils), `CaducityTheme`, `AppTheme`, `ShapePosition`, `AppFeedbackType`, `StatusBadge` (adaptador temporal sobre `StatusPill`), `ExpirationDefaults`.
- **Eliminar:** Tone (9 fitxers), `ExpirationColors`, `CaducityDims` si orfe, fonts velles, fila «Tone».
- **Regla tauleta (decidida, opció A):** es **conserven** `ui/adaptive/WindowSizeClass.kt` (`rememberIsExpanded`, `rememberIsExpandedOrMedium`, `calculateGridColumns`) i `ResponsiveCenteredContainer`; no s'esborren ni es renomenen. Vegeu «Regla de tauleta per a les pantalles noves» a la secció 9.
- **Mode fosc (`12-mode-fosc.md`):** el `darkColorScheme` i els `StatusColors` foscos surten de la taula de substitució (valors `dark` de `tokens.json`). El snackbar usa `inverseSurface`/`inverseOnSurface` (fosc: fons `#F1EADC`, text `#29241C`). Els colors d'estat no canvien amb el color dinàmic, tampoc en fosc.
- **Riscos:** colors fixos a pantalles velles (`Color.Green/Cyan`); `wdth` de Bricolage; `material-kolor` pot quedar orfe.
- **Acceptació:** pantalles velles en crema/oliva sense vermell/llima/neó; galeria de previews (només debug) amb 4 estats, 6 formes, tipografia i components en clar/fosc, comparable amb `03-design-system/README.md` i amb `12-mode-fosc.md`.
- **Tests:** `connectedShape`, mapeig `CategoryShape`→`MaterialShapes`, `ShapePositionTest` actualitzat; test de contrast WCAG dels parells `*-container`/`on-*-container` llegint `tokens.json` (copiat a `src/test/resources`), **cobrint el tema clar i el fosc** (≥ 4,5:1; ràtios de referència a `12-mode-fosc.md`). Spike de screenshot testing (secció 10).

### Fase 2 — Model, dades i càlcul d'estat
**Objectiu:** domini i persistència preparats per a totes les pantalles (sense UI).
- **Room v3** (6.1), `Migrations.kt`, `3.json`, entitats, DAOs (resum de categories, suggeriments, `discard`, `moveItem`, recomptes), mappers.
- **Domini:** `ExpiryStatus` pura, `TodayProvider`, `ExpirationThresholds` reactiu, `ItemStatus.Discarded`, `Item.isActive`, `Category` amb `shape/icon/sortOrder`, models `Home*`/`Pantry*`/`ProductGroup`, `ObserveItemsUseCase`.
- **Use cases:** `AddItemBatchUseCase` (N ítems, P2), `UndoAddItemsUseCase`, `DiscardItemsUseCase`, `UpdateItemUseCase`, `MoveItemToCategoryUseCase`, `GetNameSuggestionsUseCase`, `UpdateCategoryUseCase`, `SeedCategoriesUseCase`, `Freeze`/`Unfreeze` (P4: s'hi **afegeix** el comportament nou). `ConsumeItemUseCase` hi guanya un mètode permissiu `consume(itemId)` (P5) per al codi nou; els mètodes antics amb bloqueig (`consumeItem`/`forceConsumeItem`) es mantenen `// TEMP(fase 7)` perquè el full vell encara els usa. `CreateCategoryUseCase` rep paràmetres opcionals `shape/icon/sortOrder` mantenint `items` `// TEMP(fase 8)`. **Cap eliminació en aquesta fase** (vegeu la regla d'eliminació); `ItemStatus.Discarded` obliga a completar els `when` exhaustius de les pantalles velles.
- **Prefs:** `PreferencesMigrator` **per passos versionats** (pas 1 a la fase 2: `soonThresholdDays = 3`, esborrar `theme_tone` i `ai_*`; pas 2 a la fase 8: claus de notificacions, vegeu 6.3), `SoonThresholdPreferences`, `DisclaimerPreferences`, `NotificationConfigDataSource` ampliat.
- **Backup v2** (6.1). **Fake data** actualitzat.
- **Riscos:** fase de més risc de pèrdua de dades ⇒ tests de migració obligatoris; canvi `<`→`<=` (test d'equivalència); zona horària (5.2); les pantalles velles continuen llegint `ItemStatus` ⇒ adaptadors temporals `// TEMP(fase N)` fins a les fases 5–7.
- **Acceptació:** l'app antiga funciona amb una BD migrada (dump v2 real instal·lat sobre la nova versió); fake data genera categories amb forma/icona.
- **Tests:** `ExpiryStatusTest` (avui, demà, ±llindar, ahir, congelat, canvi de dia, llindars 1/3/7, equivalència amb el càlcul antic per a 365 dates), `TodayProviderTest`, **`Migration2To3Test`** (categories en en/es/ca, sense coincidència, ítems amb/sense producte, congelats, consumits, **`identifier` buidat tant en ítems amb producte com convertits**, claus foranes), `CategoryDefaultsTest`, `PreferencesMigratorTest`, `NameSuggestionsTest`, `AddItemBatchUseCaseTest` (N ítems amb la mateixa data, find-or-create, undo elimina els N), `LegacyPackConsumeTest` (packSize>1 conserva consum parcial; mai se'n crea de nous), `DiscardUndoTest`, `BackupV1ImportTest`/`BackupV2RoundTripTest`.

### Fase 3 — Navegació (shell)
**Objectiu:** shell amb barra flotant + FAB, rutes finals i deep links; pantalles velles penjant temporalment de les rutes noves.
- **Crear:** `ui/navigation/Routes.kt` (`Home`, `Pantry`, `Settings`, `Category(id, filter?)`, `Expired` (àlies de `StatusList(EXPIRED)`; a la fase 7 es generalitza a **`StatusList(status)`**), **`Calendar(categoryId?, date?)`**, `Add(categoryId?, source?, itemId?)`, `CategoryEditor(id?)`, `ItemActions(itemId)`, `QuickCategory`, `Onboarding`, subpantalles d'Ajustos), `TopLevelBackStack`, `AppShell` amb `FloatingNavBar` (`HorizontalFloatingToolbar`, sempre visible, per sobre del contingut (slot FAB de l'Scaffold pare) i amb `LocalFloatingBarPadding` per a les llistes; decidit 2026-10-11: no s'amaga en scroll) i FAB 64 dp.
- **Modificar:** `App.kt` (entry provider nou; `BottomSheetSceneStrategy` per a Accions i Nova categoria ràpida), `MainActivity` (`singleTop`, `onNewIntent`), `DeepLinkAction` (`OpenHome`, `OpenExpired`, `OpenAdd(source, categoryId?)`, `OpenCategory`; compatible amb notificacions ja programades), `AppScaffold`, `Actions.kt`/`Screens.kt`, manifest.
- **Temporal:** Aviat → dashboard vell; Rebost → llista simple; Ajustos → settings vell; FAB → placeholder fins a la fase 4; `Calendar` → pantalla buida fins a la 5.
- **Riscos:** estat de pestanyes a Nav3; `SettingsContainer` es manté `// TEMP(fase 8)` dins la pantalla Ajustos; `ViewModelStore` per entrada de sheet.
- **Acceptació:** barra, FAB i pastilla activa farcida conformes a `Rebost.png`/`Main.png`; la barra és sempre visible i queda per sobre del contingut; secundàries sense barra; `adb shell am start` amb extras obre la destinació correcta amb l'app oberta i tancada.
- **Tests:** `TopLevelBackStackTest`, `DeepLinkParsingTest`, `DashboardActionsTest` actualitzat.

### Fase 4 — Afegeix (prioritat alta)
**Objectiu:** flux complet en 3 tocs.
- **Crear:** `ui/screen/add/{AddScreen, AddViewModel, AddState, AddNavigation, AddSideEffect}`, `NameField` + `SuggestionRow`, `CategoryChips` (multilínia, sense scroll), `ExpiryDateField` (`dd/mm/aa`, barres automàtiques, accepta `dd/mm/aaaa`, ajuda en viu amb `liveRegion`, **«ja ha caducat» en `expired`**), `ExpiryDateParser` (pur), `QuantityStepper` (límit P22, **crea N ítems** via `AddItemBatchUseCase`), `FrozenToggle`, mode tanda (**`BatchSavedRow` / `BatchSavedCard`** —`LazyRow` de targetes de 148 dp amb Edita i «Treu de la tanda», substitueix els xips «Nom · dd/mm»—, resum Categoria/Data amb «Canvia», `Fet`), Desfés de l'últim desat, mode edició (`itemId`, una unitat, P20), `QuickCategorySheet` (+ `CategoryForm`, `ShapePicker`, `IconPicker` reutilitzables a la fase 8).
- **Substitueix:** `CategoryDetailAddItem*` (s'eliminen aquí; el botó d'afegir de l'antic `CategoryDetailScreen` es reconnecta a `Add(categoryId)` `// TEMP(fase 6)`). **No elimina** `ProductActionsSplitButton` (el consumeix `ProductTabContent` fins a la 6) ni `FutureDateSelectableDates` (viu al paquet `category/create`, que s'elimina a la 8): Afegeix simplement no els usa (P15).
- **Fila «Desats en aquesta tanda»** (`06-afegir.md › Mode tanda`, 2026-10-11): `BatchSavedRow` (`LazyRow`, gap 8 dp, padding inicial 20 dp, **sense padding final** perquè es vegi un tros de la targeta següent) i `BatchSavedCard` (148 dp, `surface-card`, radi 20; `check_circle` `fresh` + nom fins a 2 línies, categoria, «N unitats · dd/mm», botons Edita i Treu de 44 dp). Ordre **del més recent al més antic** (`added` es mostra invertit) i, en desar, scroll animat a l'inici (`animateItem()` + `animateScrollToItem(0)`). Accessibilitat: la fila és una llista («Productes desats en aquesta tanda, del més recent al més antic»); «Edita {nom}» / «Treu {nom} de la tanda». Si s'esborren tots, la fila desapareix. **Comptador de la capçalera «✓ N desats» = nombre d'entrades (targetes de la fila), no d'unitats** (decidit 2026-10-11); les unitats es veuen a cada targeta.
- **Acció «Treu de la tanda» a `AddViewModel`** (`onRemoveEntry(entry)`): esborra les unitats de l'entrada **sense diàleg** i emet un snackbar «{nom} tret · Desfés». Reutilitza `UndoAddItemsUseCase`/`undoAdd` per esborrar (i el producte si l'alta el va crear i queda buit). **Desfés** el restaura amb **els mateixos ids i dates**: cal una instantània prèvia (`BatchSnapshot`: ids d'ítem, `productId`, nom del producte, categoria, caducitat, `createdAt`, congelat) i una operació nova `PantryDataSource.restoreBatch(snapshot)` (+ `RestoreBatchUseCase`) que reinsereix ítems i, si s'havia esborrat, el producte amb el mateix id; tot en una transacció. L'entrada torna a la seva posició.
- **Hàptics:** `Confirm` en desar, `Reject` en botó desactivat.
- **Riscos:** IME vs barra inferior (`imePadding`); mode edició dins el mateix ViewModel; col·lisions de nom; Desfés asíncron de N ítems; accessibilitat del camp de data; **restaurar amb els mateixos ids** (el producte pot haver-se esborrat en treure l'última unitat; els ids han de ser lliures en restaurar); scroll a l'inici de la fila sense sobreescriure un scroll de l'usuari mentre edita; tocar tota la targeta edita igual que el botó Edita (decidit 2026-10-11).
- **Acceptació:** `Afegir.png`, `AfegirNou.png`, **la nova `AfegirTanda.png`** (fila de targetes amb scroll horitzontal i un tros de la següent visible), `AfegirNovaCat.png`; nom+categoria+data obligatoris amb «Falten la categoria i la data per desar»; «Desa i un altre» manté categoria i data; Quantitat 3 desa 3 ítems (a Categoria es veuran com «3 unitats», fase 6); data passada es pot desar.; **preview en clar i en fosc; `AfegirFosc.png` forma part de l'acceptació**
- **Tests:** `AddViewModelTest` (validació, suggeriment omple nom+categoria **però no data**, exacte→noves unitats, tanda, Desfés, edició d'una unitat, congelat, **el comptador compta entrades (no unitats), treure un ítem de la tanda, desfer-ho (mateix id i dates), ordre del més recent al més antic, treure'ls tots → la fila desapareix**), `ExpiryDateParserTest` (`1/2/26`, `01022026`, `31/02`, any de 2 xifres, enganxat, esborrat), `QuickDatesTest`, `QuickCategoryTest` (nom únic), `QuantityBatchTest`.

### Fase 5 — Full d'Accions, Inici i Calendari
**Objectiu:** tres blocs, un commit cadascun, per ordre: 5a Accions → 5b Inici → 5c Calendari.

**5a. Full d'Accions** (avançat des de la fase 6 perquè Inici i Calendari l'obren)
- **Crear:** `ui/components/actions/ItemActionsSheet` + `ItemActionsSheetViewModel` (nom diferent de l'antic `ItemActionsViewModel` per no col·lidir a Koin mentre conviuen) (Consumit, Congela/Descongela, dates ràpides amb Desfés, DatePicker, Edita → `add?itemId=`, Mou a una altra categoria (selector), Esborra amb confirmació); actua sobre **1 unitat**; per a packs antics, Consumit demana quantitat amb `QuantitySelectorDialog`, i el text mostra «pack de N».
- **No elimina res.** El full antic (`bottomsheet/*`, `ItemActionsViewModel`) segueix sent usat per `CategoryDetail` (fins a la 6) i per `FilteredItemsByStatus` (fins a la 7); conviuen `// TEMP(fase 7)` i s'esborren a la 7. `QuantitySelectorDialog` es conserva (el nou full l'usa per a packs antics).
- **Acceptació:** `Accions.png`; les dates ràpides sumen a la **data actual de caducitat**, s'apliquen a l'instant i mostren snackbar Desfés.; **preview clar/fosc; `AccionsFosc.png`**
- **Tests:** `ItemActionsViewModelTest`, `MoveItemToCategoryTest`, `QuickRescheduleTest`, `FreezeUnfreezeTest` (P4).

**5b. Inici «Què caduca» + estat buit**
- **Crear:** `ui/screen/home/{HomeScreen, HomeViewModel, HomeUiModel, HomeNavigation}`; capçalera amb data, títol i **dos botons**: `calendar_month` («Calendari» → `Calendar`) i `search` («Cerca» → Rebost amb cercador enfocat, P10); *slot* buit per a la `WeekStrip` entre la capçalera i el bàner (la 5c el cobreix); `ExpiredBanner`, `SummaryRow`, `DaySection`, `ItemRow` amb botó `check`, `HomeEmptyState`, `WidgetTipCard` (s'activa a la fase 12).
- **Files:** una per **(producte, dia)** amb «N unitats» quan N > 1; el recompte de la secció i del subtítol («3 productes») **compta unitats**; ✓ consumeix **la unitat més propera** d'aquell (producte, dia) amb Desfés; pack antic: «pack de N» (compta 1).
- **Eliminar:** `ui/screen/dashboard/**`, `ObtainDashboardUseCase`, `DashboardData`. **Conserva** `CalendarData` (el consumeixen els components de calendari antics fins a la 10b) i `GetExpiringCategoriesUseCase` (l'usa el worker antic fins a la 8).
- **Riscos:** refresc a mitjanit (`TodayProvider`); recomptes coherents amb Rebost; agrupació per dia localitzada.
- **Acceptació:** `Main.png` **sense la tira de 7 dies** (que arriba a la 5c) i `InicibBuit.png`; variants amb `FakePlayStoreDataStrategy`: sense caducats, res aviat («Res caduca aviat. Bona feina.» + «Mira el rebost»), rebost buit.
- **Tests:** `HomeViewModelTest` (agrupació (producte, dia), recomptes per unitats, llindars 1/3/7, caducats exclosos de les seccions, bàner «el més antic, fa N dies», ✓ consumeix la unitat més propera i Desfés, buit), `HomeNavigationActionsTest`.

**5c. Calendari (tira + vista de mes)**
- **Crear:** `ui/components/weekstrip/` i `ui/screen/calendar/` (paquets nous; no toquen `ui/components/calendar/` antic, que es manté fins a la 10b): `WeekStrip` (finestra mòbil des d'avui, paginació de 7 en 7), `ui/screen/calendar/{CalendarScreen, CalendarViewModel, CalendarMonthGrid, CalendarMonthModel}`, `ObserveCalendarUseCase`, `config/calendar/FirstDayOfWeekPreferences` (mou `CalendarPreferences`, mateixa clau `calendar_first_day_of_week`; cal actualitzar els imports del codi antic que sobreviu fins a la 8 i la 11b: `SettingsContainer`, `CategoryDetail*`, components de calendari antics).
- **Wiring:** `WeekStrip` a Inici (tocar un dia → scroll a la seva secció; sense secció `SOON` → obre `Calendar(date=)` amb aquell dia seleccionat, P18); botó `calendar_month` de la capçalera; la vista de mes accepta `categoryId?` des del primer dia (l'entrada des de Categoria es connecta a la fase 6); fila del dia seleccionat → full d'Accions (5a).
- **Riscos:** doble gest de lliscament (pager de mesos vs scroll vertical i gest de tornada); refresc de la tira a mitjanit; punts coherents amb recomptes; zona horària (5.2).
- **Acceptació:** `Main.png` **complet** (amb la tira de 7 dies: punts, avui ressaltat) i `CalMes.png` (mes d'octubre 2026, llegenda, llista del dia 21, «Làctics · unitat 1 de 3»).; **preview clar/fosc; `MainFosc.png`**
- **Tests:** `WeekStripTest` (comença avui, 7 dies, màx. 3 punts, filtre per categoria, canvi de dia a mitjanit, sense `expired`), `CalendarMonthTest` (primer dia Dilluns/Dissabte/Diumenge, mesos de 28–31 dies i traspàs, dies passats amb caducats, selecció per defecte avui, filtre per categoria, navegació entre mesos), `ObserveCalendarUseCaseTest` (exclou consumits, llençats i congelats; agrupa per data local sense desfasament de zona; packs antics compten 1).

### Fase 6 — Rebost i Categoria
- **Crear:** `ui/screen/pantry/*` (graella 2 col., `CategoryCard` amb pastilla «N caducats» i barra d'estats decorativa, targeta «Nova categoria» discontínua, cercador pastilla amb resultats, P10/P11), `ui/screen/category/*` (capçalera forma 80 dp, **`WeekStrip` filtrada per categoria** (P19: tocar un dia filtra la llista per aquell dia), botó `calendar_month` a la barra superior que obre **`Calendar(categoryId)`**, xips de filtre amb recompte, seccions per estat, `ProductRow` simple/desplegable amb «Unitat N» derivat, FAB estès «Afegeix» preseleccionant categoria, menú Edita/Esborra categoria amb confirmació).
- **Eliminar** (mor aquí l'últim consumidor): `CategoryDetail*`, `category/detail/product/**` (inclou `ProductActionsSplitButton`), `DeleteProductUseCase`, `ClearProductItemsUseCase`, `GetProductItemsUseCase`, `GetCategoryProductsUseCase`, `ProductDeletionStrategy` i `ItemDao.deleteConsumed*/deleteAll*/deleteActive*` (P7); es retira la reconnexió `TEMP` d'Afegeix. **Conserva** el full d'Accions antic (encara el fa servir Caducats vell fins a la 7).
- **Riscos:** volum (≈2.800 línies velles); estat plegat/desplegat sobre canvis de dades; `animateItem()`; accessibilitat de les barres.
- **Acceptació:** `Rebost.png`, `Categoria.png` (amb tira de 7 dies, botó de calendari i filtre «Aviat 4»); un producte amb Quantitat 3 d'Afegeix es veu com «3 unitats» desplegable.; **preview clar/fosc; `RebostFosc.png` i `CategoriaFosc.png`**
- **Tests:** `PantryViewModelTest` (ordre per `sortOrder`, recomptes, cerca sense accents), `CategoryViewModelTest` (filtre per argument, seccions, 1 unitat = fila simple, N = desplegable, tira filtrada per categoria, filtre per dia amb «Unitat N de M»), `CategoryCalendarEntryTest`.

### Fase 7 — Pantalles d'estat (Caducats, Aviat, Frescos, Congelats)
**Objectiu:** una sola `StatusListScreen(status)` amb quatre variants (`pantalles/13-pantalles-estat.md`). Caducats passa a ser la variant `EXPIRED`. Dos commits: 7a pantalla + dades; 7b connexió des de l'Inici i neteja.

**7a. Pantalla i dades**
- **Crear:** `ui/screen/status/{StatusListScreen, StatusListViewModel(status), StatusListState, StatusListNavigation}`; `ObserveItemsByStatusUseCase(status: ExpiryStatus): Flow<List<CategoryGroup>>` (ítems actius agrupats per categoria, grups per nombre d'unitats desc., files segons la taula de l'spec; reutilitza `ExpiryStatus`, el llindar d'Ajustos i `TodayProvider`). Capçalera `*-container` de l'estat (radi inferior 32, `arrow_back`, overline «N productes» comptant **unitats**, títol, frase d'ajuda; «Selecciona»; «Llença'ls tots» amb diàleg només a `EXPIRED`), **col·lapsable en fer scroll** (canvi 2026-10-11, `13-pantalles-estat.md › En fer scroll`): es plega en una barra de 72 dp amb el mateix `*-container`, radi inferior 24 i ombra (elevació 2), amb `arrow_back`, títol petit (Bricolage 24/800) + recompte a sota, i els botons com a icones de 48 dp (`checklist` «Selecciona»; a Caducats, a l'esquerra, `delete_sweep` «Llença'ls tots» ple `expired`); la frase d'ajuda desapareix en plegar-se; la transició és contínua i lligada a l'scroll; sense scroll (llista curta) no es plega; en mode selecció la barra passa a ser la de selecció (`close` + «N seleccionats» + accions de l'estat). **API de la capçalera:** `StatusHeader(status, count, title, help, actions: List<HeaderAction>, collapseFraction)` on `HeaderAction(label, icon, primary, onClick)` (text + icona + si és principal); cada variant hi passa les seves accions, de manera que el component és comú i només canvia la llista. *Tooltip* en pulsació llarga i `contentDescription` = text del botó gran, grups per categoria, files connectades amb **dos botons rodons de 44 dp**, tocar la fila → full d'Accions, sense barra ni FAB, estats buits per variant, selecció múltiple amb barra d'accions per variant. Dates ràpides en full petit reutilitzant `QuickDateChips` (fase 1).
- **Variants:**

| Estat | Botó 1 | Botó 2 | Selecció múltiple |
|---|---|---|---|
| `EXPIRED` | `event_repeat` Canvia la data | `delete` Llença | Llença (N) · Canvia la data |
| `SOON` | `check` Consumit | `ac_unit` Congela | Consumit (N) · Congela (N) |
| `FRESH` | `check` Consumit | `ac_unit` Congela | Consumit (N) · Congela (N) |
| `FROZEN` | `check` Consumit | `water_drop` Descongela | Consumit (N) |

- Les files amb diverses unitats actuen sobre **una** unitat (la més propera a caducar); el full d'Accions en permet triar més. Consumit i Congela mostren snackbar amb Desfés (via el `GlobalSnackbarEvents` de la 5); Descongela obre el full de dates ràpides prellenat amb `ara + remainingDays` (5.6) i, en confirmar, l'ítem surt de Congelats.
- **Navegació:** `StatusList(status)` a Nav3 (`status/{soon|fresh|frozen}`); `Expired` i el deep link `OpenExpired` (drecera, notificacions) apunten a `StatusList(EXPIRED)`. Accions de tracking `nav_status_list` (`origin = home`, `source = banner | summary`), sense dades d'usuari.
- **Use cases:** tot el que cal ja és al pla: `ConsumeItemUseCase` (mod. P5), `FreezeItemUseCase`/`UnfreezeItemUseCase` (mod. P4), `RescheduleItemUseCase`, `DiscardItemsUseCase`, `UpdateItemUseCase`. **Únic afegit:** variants per a **diverses ids en una transacció** (accions massives) de Consumit, Congela i Descongela, amb Desfés massiu; `DiscardItemsUseCase` ja és per a llistes. Es poden posar com a sobrecàrregues dels use cases existents (sense use cases nous).
- **Eliminar** (mor aquí l'últim consumidor del full antic): `ui/screen/filtered/**`, `GetItemsByStatusUseCase`, `FilteredItemsRoute`, el full d'Accions antic (`bottomsheet/*` excepte `QuantitySelectorDialog`, `ItemActionsViewModel` antic), `SplitAndFreezeItemUseCase`, `SplitAndDeleteItemUseCase`, els mètodes antics bloquejants de `ConsumeItemUseCase` i `consumeExpiredThreshold`.
- **Capçalera col·lapsable — decisió d'implementació:** `material3 1.5.0-beta01` **sí** inclou `LargeFlexibleTopAppBar` (amb `subtitle`, `collapsedHeight`/`expandedHeight` i `TopAppBarScrollBehavior`, p. ex. `exitUntilCollapsedScrollBehavior`), però **no cobreix tot el disseny**: l'estat desplegat porta una frase d'ajuda de diverses línies i botons amb text sota el títol, i la forma (radi inferior 32→24) i l'ombra no són paràmetres de la barra. **Proposta:** capçalera pròpia amb `nestedScroll` (una `collapseFraction` 0..1 derivada de l'scroll de la `LazyColumn`) que interpola alçada, mida del títol, opacitat de la frase i text→icona dels botons, mantenint la barra d'estat/insets via `AppScaffold` (`topBar`). El `LargeFlexibleTopAppBar` quedaria com a alternativa si la 1.5 estable n'ampliés els *slots*. **Implementat** (commits `9afdf9e` i anteriors): capçalera pròpia amb `nestedScroll`, sense `MotionLayout`; vegeu M3 a la secció 15.
- **Riscos:** operacions massives (transacció única); Desfés massiu; coherència dels recomptes amb Inici i Rebost (unitats); canvi d'estat a mitjanit; capçalera col·lapsable (llista curta sense scroll, mode selecció, `nestedScroll` amb la barra inferior de selecció).
- **Acceptació:** `Caducats.png`, `EstatAviat.png`, `EstatFrescos.png`, `EstatCongelats.png`, **i les tres de scroll: `CaducatsScroll.png`, `EstatAviatScroll.png`, `EstatFrescosScroll.png`** (capçalera plegada); estats buits per variant (Caducats: «**Res caducat**» / «Tot el que tens és dins de data.»); **preview clar/fosc; `CaducatsFosc.png`** (les altres tres variants no tenen captura fosca: comprovar-les amb `12-mode-fosc.md`).
- **Tests:** `ObserveItemsByStatusUseCaseTest` (cada estat inclou només el que toca; ordre de grups i de files; canvi d'estat a mitjanit fresc → aviat → caducat; el llindar d'Ajustos canvia qui és Aviat), `StatusListViewModelTest` (consumir/congelar/descongelar + Desfés; la fila surt de la llista; selecció; llençar un/tots/seleccionats; estat buit), `DiscardItemsUseCaseTest`; UI: les quatre variants contra les captures; **capçalera:** en fer scroll es plega i els botons passen a icones; a dalt es desplega; sense scroll no es plega; «Llença'ls tots» des de la barra plegada obre el diàleg «Llençar N productes?».

**7b. Connexió de l'Inici**
- **Modificar** `HomeScreen`/`HomeViewModel`/`HomeNavigation` (fase 5b): el bàner «N productes caducats» → `StatusList(EXPIRED)`; les tres caselles del resum → `StatusList(SOON | FRESH | FROZEN)`, com a `Role.Button` amb `contentDescription` «Aviat, 9 productes. Obre la llista». **Es treu** el comportament antic (Aviat → scroll a la primera secció; Frescos/Congelats → Rebost). Amb recompte 0, la casella queda desactivada (sense ondulació ni navegació).
- **Tests:** `HomeNavigationActionsTest` ampliat (cada casella navega al seu estat; amb 0 no navega).

### Fase 8 — Nova/Edita categoria i Ajustos
- **Crear:** `ui/screen/category/editor/*` (previsualització 120 dp, forma 6, icona 12, descripció opcional, nom únic sense majúscules, mode edició + «Esborra la categoria»; reaprofita `CategoryForm`/`ShapePicker`/`IconPicker` de 4), `ui/screen/settings/SettingsScreen` + `SettingsViewModel`: Avisos (interruptor, `TimePicker`, llindar 1/3/7), Aparença (Tema, Color dinàmic, **La setmana comença** → afecta la vista de mes, P9), Dades (Còpia, Privadesa, Quant a); Debug només en debug.
- **Modificar:** `PrivacySettingsScreen` (+ text acceptat i data), `BackupScreen`, `AboutScreen` (crèdits OFL de Bricolage Grotesque, Figtree i Material Symbols).
- **Eliminar:** `category/create/**` (inclou `FutureDateSelectableDates` i l'argument `items` de `CreateCategoryUseCase`), `SettingsContainer`, `SettingsRootScreen`, `AppearanceSettingsScreen`, `NotificationsSettingsScreen`.
- **Notificacions** (aquí desapareixen els dos interruptors): reescriure `ExpirationCheckWorker` a **una sola notificació diària** (títol «N productes caduquen aviat», cos amb els 2–3 més urgents, només si hi ha `SOON` o «avui»; tap → `home`) amb un `NotificationContentBuilder` pur; simplificar `AndroidExpirationNotificationHelper` (sense interruptors expired/soon ni grups); `NotificationChannelManager` amb **un sol canal** i textos a `strings.xml`, i **esborrar els canals antics** (`deleteNotificationChannel`); eliminar `GetExpiringCategoriesUseCase` (mor aquí el seu últim consumidor); pas 2 del `PreferencesMigrator` (6.3). Reprogramar el worker en canviar hora/interruptor/llindar.
- **Acceptació:** `NovaCategoria.png`, `Ajustos.png`; canviar «La setmana comença» canvia el primer dia a la vista de mes.; **preview clar/fosc** (sense captura fosca: s'aplica la taula de substitució)
- **Tests:** `CategoryEditorViewModelTest` (nom únic, edita, esborra amb ítems), `SettingsViewModelTest` (llindar canvia l'estat a Inici i Calendari), `NotificationSchedulingTest`, `NotificationContentBuilderTest` (cap notificació si no hi ha `SOON`/«avui», recompte, 2–3 més urgents, «avui»/«demà»), `PreferencesMigratorStep2Test`.

### Fase 9 — Onboarding nou
- **Marge:** a la 9 les categories suggerides es creen **sense marge propi** (`null`) i el pas Avisos és el llindar únic; la fase 11 hi afegirà el marge per categoria.
- **Crear:** `WelcomeStep`, `CategoriesStep` (targetes casella, «Continua amb N»), `AlertsStep` (notificació d'exemple, llindar, hora, «Activa els avisos»/«Ara no»/«Avisos activats»), `PrivacyStep` (analítiques off), `DisclaimerStep` (casella desmarcada, botó desactivat, versionat), barra segmentada amb `progressSemantics`, animacions de `06-moviment.md` (respectant animació reduïda), `SeedCategoriesUseCase`.
- **Modificar:** `OnboardingViewModel` (sense *Skip*; en acabar: desa versió+data de l'avís, crea categories triades amb `shape/icon/sortOrder`, desa avisos/analítiques, `OnboardingFlag.disable()`); flux de reacceptació per a usuaris existents (només el pas 5, P3).
- **Eliminar:** les 6 pàgines velles, `OnboardingPagerIndicator`, `OnboardingButtons`; simplificar `ConsentPreferences` (DEFAULT / ANALYTICS_ONLY).
- **Riscos:** permís `POST_NOTIFICATIONS` (concedit, denegat, ja concedit); recreació; acceptació no escrita si el procés mor; **revisió legal del text** (és una proposta).
- **Acceptació:** `OB1`–`OB5` + `InicibBuit.png`; no es pot desar sense casella.; **preview clar/fosc** (sense captura fosca: taula de substitució; les formes usen els `*-container` foscos)
- **Tests:** `OnboardingViewModelTest` (sense categories permès, versió d'avís, no accepta sense casella, analytics off per defecte, categories amb forma/icona per defecte, permís concedit/denegat), `DisclaimerVersionTest`.

### Fase 10 — Textos, accessibilitat, mode fosc i neteja
Dos commits: 10a (textos, accessibilitat, mode fosc) i 10b (neteja).

**10a. Textos, accessibilitat, mode fosc**
- Textos ca/en/es (`05-textos.md` + secció 7), `plurals`, formats relatius, canals de notificació a `strings.xml`; eliminar claus òrfenes.
- Accessibilitat (`07-accessibilitat.md` + `Role.Tab` a la tira, `Role.Button` a cel·les del mes, descripcions «Dimarts 13, 2 productes»): àrees ≥ 48 dp, `contentDescription`, `selectableGroup`, `liveRegion` al camp de data, mida de font gran, Desfés accessible amb TalkBack.
- Mode fosc: comparar les 6 pantalles amb `captures-fosc/` (Inici, Rebost, Categoria, Accions, Afegeix, Caducats) i, per a Aviat, Frescos i Congelats (mateixa plantilla que Caducats, sense captura fosca), per a les que no en tenen (Ajustos, Nova categoria, Calendari/tira i mes, onboarding, widget), comprovar la taula de substitució de `12-mode-fosc.md` i que no queda cap color clar «encallat» (fons crema o text oliva fosc). Icones del sistema clares en fosc.

**10b. Neteja de codi mort**
- **Eliminar** `ui/components/calendar/**` (10 fitxers antics), `CalendarData`, `AppCalendarConfig*`, la dependència **`kalendar`** (5.13), `material-kolor` si orfe, `uiTiles` si la nova pantalla d'Ajustos no l'usa, icones no usades de `:icons`, `FakeItemDao`/`FakeProductDao` si orfes.
- Actualitzar `ROADMAP.md`, `README.md` (la descripció de «Kotlin Multiplatform» és obsoleta) i `CLAUDE.md` (Tone, FireAndForget, tracking, navegació, calendari).
- **Acceptació:** `./gradlew lint ktlintCheck test assembleRelease` verd; recorregut manual complet (instal·lació neta i actualització des de la v0.10) sense crashes; `git grep` sense `ThemeTone|kalendar|llamatik|ExpirationColors|firebase-ai`.

### Fase 11 — Marge d'«Aviat» per categoria (abans 8b)
Un o més commits. Font: `pantalles/14-marge-per-categoria.md`, 5.14 i 6.4. Va **després de l'onboarding (9) i dels textos/neteja (10)**: cal tocar també l'onboarding i afegir els textos en ca/en/es.
- **Dades:** columna nova dins la `MIGRATION_2_3` (6.4), `CategoryRoomEntity`/`Category.soonThresholdDays`, DAOs i mappers, backup (`BackupCategory` + camp nullable), `SuggestedThresholds`, `ThresholdStops` (parada ↔ dies; un valor fora de parades es conserva i el slider mostra la més propera).
- **Domini:** el càlcul d'estat usa el marge efectiu (5.14); `ActiveItem`/`PantryEntry` el porten; revisió de tots els filtres per `SOON`; `UpdateCategoryUseCase`/`CreateCategoryUseCase` accepten `soonThresholdDays: Int?`; `ObserveHomeUseCase` limita les seccions a `avui + 6` i afegeix «Més endavant»; `NotificationContentBuilder` amb el criteri nou.
- **UI:** `ThresholdSlider` (slider discret de 8 parades, `stateDescription`, hàptic); pantalla **«Marge per categoria»** (`Settings › Marge per categoria`, files desplegables d'una en una, «Torna als valors suggerits» amb diàleg); **Ajustos** (fila «"Aviat" per defecte» + fila nova amb resum d'una línia); **Nova/edita categoria** (secció «Avisa'm abans», previsualització de 96 dp; sense tocar-ho es desa `null`); **Inici** («Més endavant» plegable, ajuda); **Aviat** (frase nova + pastilla «Avisa N abans» per grup).
- **Onboarding (9, ja feta):** `SeedCategoriesUseCase` crea les categories suggerides amb el seu marge (`SuggestedThresholds`) i el pas Avisos canvia de text (selector = «per defecte»; «Cada categoria ja porta el seu marge. Aquest és per a les que creïs tu.»).
- **Textos (10a, ja feta):** les claus noves s'afegeixen aquí en ca/en/es amb `plurals`, i s'hi repassen l'accessibilitat i el mode fosc de les pantalles tocades.
- **Fora d'aquesta fase:** widget (la 12 usa el marge efectiu).
- **Riscos:** instal·lacions de desenvolupament ja a la v3 antiga (cal reinstal·lar, 6.4); filtres per `SOON` oblidats (`git grep ExpiryStatus.SOON`); `Slider` M3 expressive amb `steps` i accessibilitat.
- **Acceptació:** `MargeAjustos.png`, `MargeCategories.png`, `MargeNovaCategoria.png`, `MargeInici.png`, `MargeAviat.png`; **preview clar/fosc**; canviar el marge d'una categoria canvia a l'acte qui és «Aviat» a Inici, Aviat, tira i calendari.
- **Tests:** `ExpiryStatusCalculatorTest` (marge propi vs per defecte; límits: avui + marge inclòs, +1 exclòs; canviar el global no afecta categories amb marge propi), `ThresholdStopsTest`, `HomeSectionsTest` (fins a avui+6, «Més endavant», casella Aviat compta tots els `SOON`), `DailyNotificationTest` (només el primer dia a `SOON` + avui/demà; un producte amb marge d'1 mes no es repeteix), `Migration2To3Test` ampliat, `BackupTest` ampliat, `CategoryEditorViewModelTest` ampliat (`null` si no es toca, valor si es toca), `SettingsViewModelTest` ampliat.

### Fase 12 — Icona, splash, widget i dreceres ( **opcional**, l'última)
Icona adaptativa ja feta (`3d59630`). La resta és opcional: es pot publicar sense ella.
- Icona adaptativa (fons `#E8B95A`, Galeta `#4D5A1E`, fulla `#FBF6EC`), monocroma, debug amb franja «DEV» `expired`; `ic_notification` monocrom. El vector definitiu s'ha de dibuixar sobre la retícula de 108 dp (obert #8): si no hi és, es deixa el provisional marcat.
- Splash: `androidx.core:core-splashscreen`, fons `surface`; **en fosc, fons `#1C1A15`**; la icona A no canvia (mostassa + oliva).
- Widget (Glance, amb el marge efectiu de cada categoria, fase 11): `glance-appwidget` + `glance-material3`; `CaducityWidget`, receiver, `appwidget-provider`; dades via Koin; `updateAll()` després de cada escriptura i des del worker diari; botó **Afegeix** → `add?source=widget`, cos → `home`; estat buit; tipografia del sistema en negreta (P13).
- Dreceres estàtiques: Afegeix producte, Què caduca, Caducats (`StatusList(EXPIRED)`). Activar `WidgetTipCard` d'Inici.
- **Riscos:** R8 amb Glance; refresc a mitjanit; mides del widget.
- **Acceptació:** `Icona.png`, `Splash.png`, `Accessos.png`; la icona temàtica (Android 13+) es veu monocroma.
- **Tests:** `WidgetStateMapperTest` (N aviat, N caducats, 2 propers, buit), `ShortcutIntentTest`.

### Fase 13 — Landing de Caducity i política de privacitat (nova, 2026-10-12)
Fora d'aquest repo: una pàgina nova a `https://github.com/alorma/ConversesLanding`, que serà la landing de Caducity. **Falta el disseny**; no es pot començar fins que n'hi hagi.
- **Contingut:** landing de l'app + **pàgina de política de privacitat**. La URL de la política és la que es posarà a Google Play Console (fitxa de la botiga) en lloc de l'actual.
- **Text legal:** és el moment de la revisió de l'avís (hipòtesi del pas 5 de l'onboarding i `privacy_policy.md`/`terms_and_conditions.md`). La pàgina i l'app han de dir el mateix; cal revisar-lo per una persona competent abans de publicar. Si el text canvia, es puja la versió de l'avís (`disclaimer_accepted_version`) i els usuaris existents el tornen a acceptar (P3).
- **Pendent de decidir:** disseny de la landing; estructura del repo (carpeta/ruta nova, idiomes ca/en/es); si l'app enllaça la política des d'Ajustos › Privadesa; URL final per a Play.
- **Bloqueja la publicació:** sí, per a la política de privacitat de Play (no per a la resta de l'app).
---

## 10. Dependències i estratègia de tests

| Tema | Estat actual | Acció |
|---|---|---|
| Compose BOM | `2026.09.00` | Sense canvi |
| Material 3 | `1.5.0-beta01` fixat; ja s'usen `MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ButtonGroup` | Sense pujada; verificar a la fase 0 `MaterialShapes` (6 formes), `HorizontalFloatingToolbar`, `LargeFlexibleTopAppBar`; si falta alguna API, pujar a la beta/RC següent |
| Navigation 3 | `1.2.0` | Sense canvi (`SceneStrategy` ja en ús) |
| Glance | no hi és | **Nova** (`glance-appwidget`, `glance-material3`); versió estable més recent a la fase 12, compatible amb Kotlin 2.4.20/AGP 9.4.1 |
| Splash | no hi és | **Nova** (`core-splashscreen`) |
| Fonts | 3 a `res/font` | **Treure** Google Sans Flex ×2 i Roboto Flex; **afegir** Bricolage Grotesque i Figtree (variables, OFL), empaquetades |
| Icones | `:icons` a mà | Set generat de Material Symbols Rounded (P14) |
| Room | `2.8.5` | + `room-testing` (test) |
| Robolectric | només versió al catàleg | Activar per a migracions |
| Treure | `llamatik`, `firebase-ai`, (probable) `firebase-appcheck-*`, `kalendar` (10b), (probable) `material-kolor` | Fases 0 i 10b |
| Mantenir | `multiplatform-settings`, `fireandforget`, `koin*`, `work-runtime`, `firebase-analytics/crashlytics/config`, `play-review`, `compose-settings` (`uiTiles`) | `uiTiles` només si el nou Ajustos l'usa |

**Tests:** unitaris JVM com a base (estat, dates, calendari, ViewModels, use cases, prefs, parser de data, deep links); migracions amb `MigrationTestHelper` + Robolectric (1→2, 2→3, 1→3); backup (round-trip v2 i import v1). **Screenshot/UI:** el projecte no en té; es proposa un spike a la fase 1 de *Compose Preview Screenshot Testing* (o Roborazzi) només per al design system i 5–6 pantalles clau en clar/fosc; si no és compatible amb AGP 9.4.1/Kotlin 2.4.20, es substitueix per la galeria de previews de debug i revisió manual contra les captures. Cap fase es dona per tancada sense els tests de la seva secció.

---

## 11. Ordre i dependències entre fases

```
0 ─▶ 1 ─▶ 2 ─▶ 3 ─▶ 4 ─▶ 5a ─▶ 5b ─▶ 5c ─▶ 6 ─▶ 7 ─▶ 8 ─▶ 9 ─▶ 10 ─▶ 11 ─▶ 12 (opcional) ─▶ 13 (landing + privacitat, falta disseny)
                    │      ▲      │
                    │      └ Accions (5a) l'obren Inici (5b), Calendari (5c), Categoria (6), pantalles d'estat (7)
                    └ QuickCategorySheet i ShapePicker/IconPicker (4) reutilitzats a 8
```

La numeració és 0–12 (abans hi havia un salt a la 10: el 2026-10-11 s'han renumerat les fases 11–13 com a 10–12). Calendari a les fases 5 i 6, «Res caducat» a la 7, eliminació de `kalendar` a la 10. Canvis respecte a l'esquema inicial i raons:
1. **Afegida la fase 0** (neteja d'IA i infra de tests).
2. **El full d'Accions passa de la fase 6 a la 5a**: Inici i Calendari ja l'obren i no es pot navegar-hi sense. No altera la resta de fases.
3. **El calendari entra a la 5 (tira + mes) i a la 6 (entrada des de Categoria)**; `Calendar(categoryId?)` suporta categoria des de la 5c.
4. **`QuickCategorySheet` i els seus components** (`CategoryForm`, `ShapePicker`, `IconPicker`) es construeixen a la 4 i es reutilitzen a la 8.
5. **La fase 5 es divideix en tres commits** (5a, 5b, 5c); la 10 en dos (10a, 10b).
6. **Fase 11 (abans 8b, marge per categoria), afegida el 2026-10-11** i posposada després de la 10 (decidit el mateix dia): toca dades, domini i pantalles de les fases 5, 7 i 8 ja fetes, i també l'onboarding (9). El widget (12) la farà servir.
7. **Fase 12 (icona/splash/widget/dreceres) passa a l'última i és opcional** (decidit 2026-10-11).

---

## 12. Fora d'abast (explícit)

- IA, veu, reconeixement d'imatge, codi de barres, OCR de dates/tiquets, càmera.
- Llista de la compra i sincronització al núvol.
- Reordenar categories arrossegant (`sortOrder` es desa, sense UI).
- Tauleta/pantalles grans (només s'evita trencar-les; contingut centrat amb amplada màxima). **Fase futura «Tauleta»:** `ListDetailPaneScaffold` amb Rebost | Categoria i Inici | Calendari de mes; es dissenyarà abans d'implementar-la. Regressió temporal acceptada: es perden `AdaptiveCalendar`, el panell lateral de `CategoryDetailScreen` i les variants expandides del Dashboard.
- Cerca global avançada (més enllà d'obrir Rebost amb el cercador enfocat).
- Notificacions per producte, canals separats per estat, avisos de caducats.
- Revisió legal del text de l'avís (proposta; l'ha de revisar una persona competent abans de publicar). Es fa a la fase 13, amb la landing.
- Publicació a Play Store entre fases (decisió de l'usuari).

(El **calendari ja no és fora d'abast**: és dins d'abast, fases 5 i 6.)

---

## 13. Riscos transversals

| Risc | Mitigació |
|---|---|
| Pèrdua o corrupció de dades a la migració v2→v3 | Només `ADD COLUMN`; tests amb dades reals; `PRAGMA foreign_key_check`; prova amb dump de producció |
| Canvi silenciós d'estat d'ítems | Test d'equivalència entre càlcul antic i nou (llindar 3, `<=`) |
| Desfasament de data per zona horària (afecta també a tira i mes) | `LocalDate` al domini; tests amb zones UTC±12 |
| Eliminacions que trenquen la compilació | Regla d'eliminació (secció 9): només quan mor l'últim consumidor, comprovat amb `git grep`; `// TEMP(fase N)` |
| Marge per categoria canvia l'estat d'ítems existents (12) | Cap: les categories existents queden a `null` (5.14.1); test d'equivalència amb marge `null` ≡ comportament actual |
| Es perd l'etiqueta lliure `identifier` («Batch #1») en migrar | Acceptat (decisió); documentat a 6.1 |
| UI híbrida entre fases (no es publica) | Cada fase manté l'app funcional; adaptadors `// TEMP(fase N)` eliminats a la fase indicada |
| Fase 5 és gran (Accions + Inici + Calendari) | Tres commits (5a/5b/5c); 5a es pot entregar i provar sola |
| Fase 6 és gran (≈2.800 línies velles) | Commits per capa (dades → domini → UI) |
| Gestos de lliscament en conflicte (pager de mesos, tira, scroll, gest de tornada) | Prototip a 5c; àrees de gest delimitades; tests manuals |
| Refresc a mitjanit de tira/mes/Inici | `TodayProvider` únic; tests de canvi de dia |
| Definició de «productes» als punts del calendari (unitats vs productes) | Hipòtesi P17 documentada; canviar-la és una línia a `ObserveCalendarUseCase` |
| API experimental d'M3 expressiu pot canviar a la beta | Versió fixada; encapsulada a `ui/components` |
| Glance sense fonts custom | P13 |
| Paquet de redisseny amb 5 fitxes desactualitzades (secció 4) | Aquest pla és la font de veritat; P21 |
| Textos legals no revisats | Marcat; no tancar la fase 9 com a «llest per publicar» |
| Pèrdua d'historial per netejar consumits | Funcionalitat eliminada (P7) |

---

## 14. Hipòtesi menor pendent (no bloquejant)

| # | Pregunta | Per defecte |
|---|---|---|
| P22 | Límit màxim de **Quantitat** per alta a Afegeix (l'especificació no en defineix). | 20 |

Totes les altres preguntes (P1–P21) estan resoltes (secció 1).

---

## 15. Punts a millorar (després del redisseny)

| # | Punt | Detall |
|---|---|---|
| M1 (fet 2026-10-12) | Canvi de mes del calendari amb `HorizontalPager` | Avui (fase 5c) el mes canvia amb un gest de lliscar a la targeta i amb les fletxes, amb animació en deixar anar. L'especificació (`11-calendari.md`) demana un pager, on es veu el mes veí mentre s'arrossega. No s'ha fet perquè `CalendarViewModel` només carrega les dades del mes visible: el mes veí es veuria sense punts fins a acabar el gest. Per fer-ho cal que el ViewModel carregui també el mes anterior i el següent (rang ampliat a `ObserveCalendarUseCase`) i que `CalendarScreen` canviï `AnimatedContent` + `detectHorizontalDragGestures` per un `HorizontalPager` de mesos. |
| M2 (fet 2026-10-12) | Topbar comuna i capçalera fixa a totes les pantalles | Totes les pantalles han de portar una topbar similar, i les capçaleres (overline + títol, etc.) **no han de fer scroll dins el `LazyColumn`**: han de quedar fixes a dalt, amb només el contingut desplaçant-se. Avui Inici i Rebost (fases 5b i 6) posen la capçalera com a primer ítem del `LazyColumn` i se'n va amb el scroll. Cal un component de topbar compartit (a `ui/components/topbar`) i aplicar-lo a Inici, Rebost, Categoria, Caducats, Ajustos, etc. A decidir: topbar fixa en totes les pantalles de pestanya o col·lapsable. |
| M3 (fet 2026-10-12) | Capçalera col·lapsable amb `MotionLayout` | La capçalera de les pantalles d'estat (fase 7) es plega amb una capçalera pròpia i `nestedScroll` (`StatusHeader` + `StatusHeaderState`: `collapseFraction` i crossfade entre la capa desplegada i la plegada). A reavaluar passar-la a `MotionLayout` de Compose (`constraintlayout-compose`) per interpolar posició i mida dels elements de forma contínua (títol, recompte i botons text→icona) en lloc del crossfade. Afegiria la dependència; decisió de l'usuari 2026-10-11. |
| M4 (fet 2026-10-12: es queda el calendari propi) | Revisar si val la pena un calendari d'una llibreria externa | Es va eliminar `kalendar` (fase 10b, 2026-10-11) i el calendari és propi (`WeekStrip`, `CalendarMonthGrid`, `CalendarMonthModel`; justificació a 5.13). A l'usuari li sembla molt fort fer-ho a mà: al final, reavaluar si una llibreria externa (`kalendar` o una altra) ens estalviaria codi i manteniment, comparant-ho amb el cost real del codi propi (mides, accessibilitat, refresc a mitjanit, pager de mesos de M1). Si es decideix tornar-hi, el codi antic és a l'historial de git (commit de la 10b). |
| M5 (fet 2026-10-12) | Cap categoria suggerida o de dades de prova no es pot dir «Rebost» | L'app té una pantalla (pestanya) que es diu **Rebost**, així que una categoria també anomenada «Rebost» confon (a l'Inici, a Marges i al mateix Rebost apareix «Rebost» dues vegades amb sentits diferents). Avui l'onboarding proposa `SuggestedCategory.PANTRY` amb el text `ob_category_pantry` («Rebost»/«Pantry»/«Despensa»), i `FakePlayStoreDataStrategy` crea una categoria «Rebost». Cal canviar el nom (p. ex. «Secs i pasta», «Conserves» o «Armari») a l'onboarding en ca/en/es i al fake data, i comprovar que `SuggestedThresholds.forName` (que reconeix `rebost`/`despensa`/`pantry`) encara l'associa al marge d'1 mes amb el nom nou. Detectat a la fase 11 (2026-10-12). |
