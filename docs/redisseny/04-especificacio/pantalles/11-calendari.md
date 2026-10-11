# Calendari: tira de 7 dies i vista de mes

Captures: `Main.png` i `Categoria.png` (tira de 7 dies), `CalMes.png` (vista de mes). Fonts: `Main.dc.html`, `Categoria.dc.html`, `CalMes.dc.html`.
Substitueix el calendari setmanal per categoria del dashboard antic (`01-app-original/app/01_dashboard.png`) i el de la capçalera del detall de categoria (`app/02_category_detail.png`), amb una versió molt més senzilla.

## A) Tira de 7 dies (`WeekStrip`)

Component reutilitzable a dues pantalles. **No canvia la resta de la pantalla.**

| Pantalla | Posició | Productes que compta |
|---|---|---|
| Inici | entre la capçalera (data + «Què caduca» + botons) i el bàner de caducats | tots els actius |
| Categoria | entre la capçalera (forma + nom) i els xips de filtre | només els de la categoria |

- **Finestra mòbil**: 7 dies seguits que **comencen avui**, no una setmana de calendari, i per tant no depèn de «La setmana comença». Es pot lliscar horitzontalment per veure els 7 dies següents.
- Graella de 7 columnes iguals, gap 4 dp, **sense targeta de fons** (marge negatiu de −4 dp per alinear el contingut amb la resta).
- Cada dia és un botó (radi 18, padding 6/0/8, alineació centrada, gap 4):
  - inicial del dia localitzada (`dl dt dc dj dv ds dg`), 12 sp, pes 600, `ink-variant`;
  - número del dia, Bricolage 18/800, en un cercle de 34 dp;
  - fila de punts de 6 dp (gap 3): **un punt per producte** que caduca aquell dia, **màxim 3**, del color fort del seu estat (`soon` o `fresh`). `expired` no hi surt mai, perquè la tira comença avui. Si no hi ha res, la fila queda buida però conserva l'alçada.
- **Avui**: fons `surface-card` i cercle amb vora 2 dp `ink`.
- **Seleccionat**: fons `primary`, text `on-primary` (els punts es mantenen).
- **En tocar un dia**:
  - Inici: fa scroll fins a la secció d'aquell dia. Si el dia no té productes `SOON` (p. ex. un dia amb només frescos), mostra una línia «Res caduca aviat aquest dia» o, si es prefereix, obre la vista de mes amb aquell dia seleccionat.
  - **Categoria: FILTRA** (no ressalta). Vegeu `CategoriaDia.png` i l'apartat «Dia seleccionat a Categoria» a sota.
  - Tornar a tocar el dia seleccionat el desselecciona.
- **Accessibilitat**: `selectableGroup` + `Role.Tab`; contentDescription «Dimarts 13, 2 productes» / «Divendres 16, res».

### Dia seleccionat a Categoria (`CategoriaDia.png`)

Substitueix el «ressaltat» (vores al voltant de files), que trencava les llistes connectades i marcava productes sencers quan només una unitat caducava aquell dia.

- El dia triat a la tira queda **seleccionat** (fons `primary`, text `on-primary`, inicial del dia en `primary-container`); avui conserva el seu cercle.
- La fila de **xips d'estat se substitueix** per un **xip de dia** actiu: `event` + «Dimecres 14» + `close` (fons `primary`, radi 12, 36 dp) i, al costat, «3 unitats caduquen aquest dia» (`ink-variant`). Tocar el xip, la ✕ o el mateix dia de la tira treu el filtre i torna la vista normal (xips d'estat + seccions).
- La llista mostra **només les unitats que caduquen aquell dia**, en **una sola llista connectada** (sense seccions per estat, sense desplegables). Cada fila: nom del producte + «Unitat 3 de 3 · dc. 14 oct» (o «1 unitat · …» si només n'hi ha una) + pastilla d'estat. Tocar → full Accions d'aquella unitat.
- A sota, ajuda: `touch_app` + «Toca el dia o la ✕ per tornar a veure-ho tot.»
- Si el dia no té res a la categoria: «Res de Làctics caduca aquest dia.»
- En seleccionar el dia, la pantalla fa scroll fins que la tira quedi visible (la tira no s'ha de perdre fora de pantalla mentre hi ha un filtre actiu).
- A l'**Inici** no canvia: tocar un dia fa scroll a la secció d'aquell dia (allà les seccions ja són per dia).

## B) Vista de mes (`CalendarRoute(categoryId: String? = null)`)

### Accés
- **Inici**: botó rodó de 48 dp amb `calendar_month` (fons `surface-high`), **a l'esquerra de la lupa**, a la capçalera.
- **Categoria**: botó d'icona `calendar_month` a la barra superior, a l'esquerra de `more_vert`, que obre el calendari **filtrat per aquella categoria**.

### Estructura
1. Barra: `arrow_back` + «Octubre 2026» (Bricolage 30/800) + `chevron_left` / `chevron_right` («Mes anterior» / «Mes següent»). Lliscar horitzontalment també canvia de mes. Amb `categoryId`, sota el mes es mostra el nom de la categoria (o «Octubre 2026 · Làctics»).
2. **Targeta del mes** (`surface-card`, radi 24, padding 10/8/8):
   - capçalera de dies (12 sp, 700, `ink-variant`), que **respecta «La setmana comença»** (Dilluns / Dissabte / Diumenge);
   - graella de 7 columnes; les cel·les buides abans del dia 1 són invisibles;
   - **cel·la** de 52 dp, **sense fons propi**: número de 15 sp dins un **cercle de 34 dp** + punts de 5 dp (gap 2, màxim 3) del color fort de l'estat, a sota del cercle. **Aquí sí surten els dies passats amb punts `expired`** (productes caducats encara actius);
   - dies passats sense productes: número en `outline`; dies amb productes, avui i seleccionat: pes 700;
   - **Estats del dia — tots sobre el mateix cercle de 34 dp, mai sobre la cel·la sencera:**

     | Estat | Cercle | Número |
     |---|---|---|
     | Normal | transparent | `ink` |
     | **Avui** (no seleccionat) | vora 2 dp `ink`, sense fons | `ink` |
     | **Seleccionat** (no avui) | fons `primary`, sense vora | `on-primary` |
     | **Avui i seleccionat** (estat per defecte en obrir) | fons `primary`, **sense vora** (la selecció mana; no es dibuixa el cercle d'avui a dins) | `on-primary` |

     Els punts sempre queden a sota del cercle, fora del fons, perquè es vegin també en el dia seleccionat.
   - **llegenda** centrada a sota: Caducat · Aviat · Fresc (punt de 8 dp + text 12 sp `ink-variant`). Els congelats no tenen data efectiva i no surten al calendari.
3. **Dia seleccionat** (per defecte, avui):
   - títol `section-title`: **«Avui»** / **«Demà»** / «Dimecres 21» + subtítol «diumenge 11 · 2 productes» o «d'aquí 10 dies · 2 productes» (`ink-variant`). Quan el dia és avui, el títol és «Avui» (no «Diumenge 11 · Avui»);
   - llista connectada (mín. 64 dp): forma de categoria 44 dp + nom + «Làctics · unitat 1 de 3» + pastilla d'estat («Fresc», «Avui», «Fa 2 dies»…). En tocar una fila s'obre el **full d'Accions**.
   - Sense productes: «Res caduca aquest dia».
4. Sense barra flotant ni FAB (pantalla secundària).

### Accessibilitat
Cada cel·la amb contentDescription «21 d'octubre, 2 productes» i estat seleccionat; `Role.Button`. La informació d'estat també és a la llista (text), no només als punts.

## Dades
- `ObserveCalendarUseCase(range: ClosedRange<LocalDate>, categoryId: String?)` → `Map<LocalDate, List<ItemWithStatus>>` d'ítems **actius** (ni consumits ni llençats), sense congelats. Reutilitza `ExpiryStatus` i `TodayProvider`. El fan servir tant la tira (avui..avui+6) com la vista de mes (primer..últim dia visible de la graella).
- La tira es refresca a mitjanit (canvia el primer dia).

## Tests
- `WeekStripTest`: comença avui, 7 dies, màxim 3 punts, filtre per categoria, canvi de dia a mitjanit, sense `expired`.
- `CalendarMonthTest`: primer dia de la setmana Dilluns/Dissabte/Diumenge, mesos de 28–31 dies i anys de traspàs, dies passats amb caducats, selecció per defecte avui, filtre per categoria, navegació entre mesos.
- `ObserveCalendarUseCaseTest`: exclou consumits, llençats i congelats; agrupa per data local (sense desfasament de zona horària).
