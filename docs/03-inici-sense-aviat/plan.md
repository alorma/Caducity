# Pla: Inici sense productes «Aviat»

Especificació: [`spec.md`](spec.md). Captures: `captures/IniciSenseAviat`, `IniciSenseAviatCaducats`. Fonts de disseny: `font-dc-html/`.
**Canvi de funcionament (2026-10-13):** quan no hi ha cap producte «Aviat», l'Inici deixa d'ensenyar «No caduca res aviat. Bona feina.» i passa a mostrar les **properes caducitats** (primers 3 dies amb productes, màxim 8 files) i un botó de text «Veure el rebost». Paquet independent: no depèn de [diverses unitats](../01-diverses-unitats/plan.md) ni de [Transforma](../02-transforma/plan.md). Validació al dispositiu connectat en aquell moment.

## Punt de partida i diferències amb l'especificació

| Especificació | Codi actual | Decisió |
|---|---|---|
| `HomeUiState` amb variant «sense Aviat» | `HomeState.Ready(data: HomeData, …)`; l'estat el dedueix `HomeData.nothingExpiresSoon` (`PantryReadModels.kt:127`) | No crear variant nova de `HomeState`: afegir a `HomeData` les properes caducitats i derivar l'estat de `nothingExpiresSoon` |
| «Properes caducitats» | `ObserveHomeUseCase` només construeix `sections` (SOON dins `HOME_SECTION_DAYS = 7`) i `later` (SOON més enllà) | Nou camp `upcoming` calculat només quan `soonCount == 0` |
| Missatge i botó | `HomeItem.NothingSoon` + `NothingSoon(onSeePantry)` amb `home_nothing_soon` i `home_nothing_soon_link` | Treure el missatge; el botó passa a ser un element propi al final de la llista |
| `NavigateToPantryAction`, `origin = "dashboard"`, `source = "empty_soon"` | Ja existeix `NavigateToPantryFromHomeAction(source)` amb `origin = "home"` i `source = "nothing_soon"` | Decisió 1 |
| Textos ca/en/es | `strings.xml` (en per defecte) + `values-ca`, `values-es` | Canviar `home_nothing_soon_link`; esborrar `home_nothing_soon` |

Es reutilitza: `HomeDaySection`, `HomeRow`, `HomeItem.SectionHeader/Row`, `toRow(...)`, la ✓ de consumir (`onConsume`), `HomeNavigation.Pantry`.

## Decisions (resoltes per Bernat, 2026-10-13)

1. **Tracking:** es manté `NavigateToPantryFromHomeAction` amb `origin = "home"`; només canvia `source` de `nothing_soon` a `empty_soon`.
2. **Congelats:** no entren a la llista; només els frescos.
3. **Tira de la setmana:** no es toca el comportament actual.
4. **Finestra de dies:** sense límit de dates; les condicions són només els primers 3 dies amb productes i un màxim de 8 files.
5. **«Més endavant» (`later`):** no es mostra en aquest estat (buit per construcció, ja que `soonCount == 0`).
6. **«+N més»:** s'implementa. Si les 8 files tallen un dia, el darrer dia mostra una fila de text no clicable «+N més», amb N = les files d'aquell dia que no hi caben.

## Fase 1 — Domini: properes caducitats

1. Model nou `HomeUpcoming` (o reutilitzar `HomeDaySection`) a `PantryReadModels.kt`: seccions per dia + `hiddenCount` (files del darrer dia que no hi caben). Constants `HOME_UPCOMING_DAYS = 3` i `HOME_UPCOMING_ROWS = 8`.
2. `ObserveHomeUseCase`: quan `soonCount == 0`, agafar les entrades FRESH amb data `>= today` (decisió 2: sense congelats), agrupar per dia, saltar dies buits, prendre els 3 primers i tallar a 8 files i comptar les amagades; reutilitzar `rowsOf`.
3. `HomeData` exposa `upcoming`; `nothingExpiresSoon` es manté com a condició de l'estat.
4. Tests (`ObserveHomeUseCase` / `HomeViewModelTest`, patró dels existents), els quatre casos de l'especificació:
   - sense Aviat i amb frescos amb data: primers 3 dies, màxim 8 files;
   - molts dies amb productes: només els 3 primers / 8 files;
   - sense cap producte amb data: cap secció;
   - amb Aviat: `upcoming` buit (comportament actual);
   - més: amb caducats, la targeta i la llista conviuen; un dia amb moltes files es talla a 8 i `hiddenCount` és correcte.
   - congelats: no apareixen.

**Sortida:** tests verds, cap pantalla canvia.

- [ ] Tasques 1-4

## Fase 2 — UI de l'Inici

1. `HomeScreen.items()`: en l'estat sense Aviat, `Banner` (si hi ha caducats) → `Summary` → seccions de `upcoming` → `SeePantry`; esborrar `HomeItem.NothingSoon`.
2. `HomeItem.MoreRows(count)`: fila de text «+N més» sota l'última fila quan `hiddenCount > 0`.
3. Reutilitzar `SectionHeader` i `Row` perquè tinguin els mateixos components i accions (✓) que «Aviat».
4. `HomeItem.SeePantry`: botó de text centrat «Veure el rebost ›», `primary`, 48 dp, sense fons; només en aquest estat.
5. La llista ha de quedar visible sense fer scroll (a part de la targeta de caducats): comprovar amb 8 files al dispositiu i, si cal, ajustar espaiats.
6. Estat de rebost totalment buit (`HomeItem.Empty`) sense canvis.
7. Textos (ca, en, es): canviar `home_nothing_soon_link`, esborrar `home_nothing_soon`; nou `home_upcoming_more` («+%1$d més» / «+%1$d more» / «+%1$d más»).
8. Previews de Compose en clar i en fosc: sense caducats i amb caducats (substitueixen `HomeNothingSoonPreview`). Actualitzar les referències de screenshot (`home_*`) amb l'etiqueta `update-screenshots` al PR.
9. Tracking: `NavigateToPantryFromHomeAction` amb `source = "empty_soon"` (decisió 1) i actualitzar `HomeActionsTest`. Sense dades d'usuari.

**Sortida:** l'Inici sense Aviat es veu com a `captures/IniciSenseAviat*.png`.

- [ ] Tasques 1-9

## Validació al dispositiu

Al dispositiu connectat: partir de dades falses i deixar-ho sense «Aviat»; comprovar (a) frescos amb data → 3 dies / 8 files + botó; (b) amb caducats → targeta a dalt; (c) sense productes amb data → només el botó; (d) rebost buit → estat buit sense canvis; (e) ✓ d'una fila i que el botó obre el rebost. Captures de l'Inici en clar i en fosc per ensenyar a Bernat.

## Notes de llançament

Sí que mereix una línia: és visible per a l'usuari («L'Inici ja no queda buit quan res no caduca aviat: mostra les properes caducitats»). Es proposa en acabar la fase 2; no s'edita `release-notes` fins que Bernat ho aprovi.
