# Pantalles d'estat: Caducats, Aviat, Frescos i Congelats

Captures: `Caducats.png`, `EstatAviat.png`, `EstatFrescos.png`, `EstatCongelats.png`. Fonts: `Caducats.dc.html`, `EstatAviat.dc.html`, `EstatFrescos.dc.html`, `EstatCongelats.dc.html`.

## Per què

A l'Inici, el bàner de caducats obria una pantalla pròpia (Caducats), però les caselles del resum feien coses diferents cada una: Aviat feia scroll a la primera secció i Frescos/Congelats anaven al Rebost. Era inconsistent. Ara **les quatre peces del resum obren la mateixa pantalla**, parametritzada per estat.

| Origen a l'Inici | Destí | Ruta |
|---|---|---|
| Bàner «N productes caducats» | Caducats | `StatusListRoute(EXPIRED)` |
| Casella **Aviat** | Aviat | `StatusListRoute(SOON)` |
| Casella **Frescos** | Frescos | `StatusListRoute(FRESH)` |
| Casella **Congelats** | Congelats | `StatusListRoute(FROZEN)` |

La ruta `expired` existent (drecera de la icona, notificacions) passa a ser `StatusListRoute(EXPIRED)`.

## Plantilla comuna (`StatusListScreen`)

Igual que la de `05-caducats.md`; només canvien els colors, els textos i les accions.

1. **Capçalera** a tota amplada, fons `*-container` de l'estat, radi inferior 32, text `on-*-container`:
   - `arrow_back` · overline «N productes» (compta **unitats**, com el resum de l'Inici) · títol `display` · frase d'ajuda.
   - Botons: **Selecciona** (`checklist`, outlined `on-*-container`) → mode selecció múltiple. Només Caducats té, a més, l'acció massiva destacada **Llença'ls tots**.
2. **Grups per categoria**, ordenats pel nombre d'unitats (desc.): forma 28 + nom (`card-title` 18) + recompte.
3. **Fila** (llista connectada, mín. 64): nom (+ « · N unitats» si diverses unitats comparteixen data) + línia de data en `on-*-container` 600 + **dos botons rodons de 44 dp** sobre `surface`. Tocar la fila (fora dels botons) → full **Accions**.
4. Sense barra flotant ni FAB (pantalla secundària).

## Variants

| Estat | Colors | Títol | Frase d'ajuda | Línia de data | Ordre de files | Botó 1 | Botó 2 | Selecció múltiple |
|---|---|---|---|---|---|---|---|---|
| `EXPIRED` | `expired-container` / `on-expired-container` | Caducats | «Si encara són bons, canvia'n la data. Si no, llença'ls.» | «Fa 1 dia · ds. 10 oct» | més antic primer | `event_repeat` Canvia la data | `delete` Llença (color `expired`) | Llença (N) · Canvia la data |
| `SOON` | `soon-container` / `on-soon` | Aviat | «Caduquen en els pròxims N dies. Fes-los servir primer.» (N = llindar d'Ajustos) | «Avui · dg. 11 oct», «Demà · …», «D'aquí 2 dies · …» | data asc. | `check` Consumit | `ac_unit` Congela (color `frozen`) | Consumit (N) · Congela (N) |
| `FRESH` | `fresh-container` / `on-fresh` | Frescos | «Encara tenen marge. Primer, els que caduquen abans.» | «D'aquí 22 dies · dl. 2 nov» | data asc. | `check` Consumit | `ac_unit` Congela | Consumit (N) · Congela (N) |
| `FROZEN` | `frozen-container` / `on-frozen` | Congelats | «No caduquen mentre són al congelador. Quan en treguis un, descongela'l i posa-li data.» | «Congelat el 2 oct» | congelat fa més temps primer | `check` Consumit | `water_drop` Descongela (color `frozen`) | Consumit (N) |

- **Consumit** marca una unitat com a `CONSUMED` + snackbar «Llet sencera consumida · Desfés» (mateix comportament que el `check` de l'Inici).
- **Congela** → `FROZEN` + snackbar amb Desfés. La fila desapareix d'aquesta llista.
- **Descongela** → obre el mateix bloc de dates ràpides que «Canvia la data» (full petit) per posar la nova data de caducitat; en confirmar, l'ítem surt de Congelats.
- Les files amb diverses unitats actuen sobre **una** unitat (la més propera a caducar); el full Accions permet triar-ne més.

## En fer scroll: capçalera plegada

Captures: `CaducatsScroll.png`, `EstatAviatScroll.png`, `EstatFrescosScroll.png`. Fonts: `*Scroll.dc.html`. (Les maquetes porten més dades que les de l'estat inicial perquè la llista pugui fer scroll; per això Caducats hi diu «11 productes».)

La capçalera gran es plega en una **barra superior** quan la llista fa scroll (M3 *large flexible top app bar* → *small*, amb `TopAppBarScrollBehavior` `exitUntilCollapsed` o equivalent; la transició és contínua, lligada a l'scroll).

**Barra plegada** (alçada 72 dp, padding 12/8/12/4):
- Fons: el mateix `*-container` de l'estat (no canvia a `surface`); **radi inferior 24** i ombra suau (elevació 2) perquè es vegi la llista passant per sota.
- `arrow_back` (48 dp) · bloc de títol: títol en Bricolage 24/800 (`font-stretch` 80 %) + recompte a sota en *overline* 12/700 majúscules (`on-*-container`).
- **Accions a la dreta**, botons d'icona de 48 dp:
  - Totes les variants: `checklist` «Selecciona».
  - Caducats, a més i **a l'esquerra** de Selecciona: `delete_sweep` «Llença'ls tots» com a **botó d'icona ple** (fons `expired`, icona blanca) — continua sent l'acció principal. Obre el mateix diàleg «Llençar N productes?».
- La **frase d'ajuda desapareix** en plegar-se (només té sentit en entrar).
- Botons amb contentDescription igual que el text dels botons grans; *tooltip* en pulsació llarga.

**Comportament**
- En tornar a dalt de tot, la capçalera es desplega de nou (títol gran + frase + botons amb text).
- Si la llista no omple la pantalla (p. ex. Congelats amb 3 productes), no hi ha scroll i la capçalera es queda desplegada.
- En **mode selecció**, la barra (plegada o no) es transforma en la barra de selecció: `close` + «N seleccionats» + accions de l'estat; la barra inferior d'accions massives es manté com a l'apartat «Variants».
- Mode fosc: mateixos tokens `*-container` / `on-*-container` de la versió fosca.

**Tests (UI)**: en fer scroll la capçalera es plega i els botons passen a icones; a dalt es desplega; sense scroll no es plega; Llença'ls tots des de la barra plegada obre el diàleg.

## Estats buits
- Caducats: «Res caducat» + «Tot el que tens és dins de data.»
- Aviat: «Res caduca aviat» + «Bona feina.»
- Frescos: «Res fresc» + «Afegeix la compra d'avui.» + botó «Afegeix».
- Congelats: «El congelador és buit».
Els tres primers tornen a l'Inici en buidar-se; a la pràctica la casella amb 0 de l'Inici no navega (vegeu sota).

## Canvis a l'Inici
- Les tres caselles del resum són clicables (`Role.Button`, contentDescription «Aviat, 9 productes. Obre la llista»). Amb recompte 0 queden desactivades (sense ondulació ni navegació).
- **S'elimina** el comportament antic «Aviat → scroll a la primera secció» i «Frescos/Congelats → Rebost».

## Dades
- `ObserveItemsByStatusUseCase(status: ExpiryStatus)` → `Flow<List<CategoryGroup>>` d'ítems actius, agrupats per categoria i ordenats segons la taula. Reutilitza `ExpiryStatus`, el llindar d'Ajustos i `TodayProvider`. Caducats el fa servir també (substitueix la consulta pròpia si n'hi ha).
- Una sola `StatusListViewModel(status)`; les accions (consumir, congelar, descongelar, canviar data, llençar) delegen als use cases existents.

## Tests
- `ObserveItemsByStatusUseCaseTest`: cada estat inclou només el que toca; ordre de grups i de files; canvi d'estat a mitjanit (fresc → aviat → caducat); el llindar d'Ajustos canvia qui és Aviat.
- `StatusListViewModelTest`: consumir/congelar/descongelar + Desfés; la fila surt de la llista; estat buit.
- UI: les quatre variants contra les captures; la casella amb 0 no navega.
