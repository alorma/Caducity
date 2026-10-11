# Afegeix (flux principal d'entrada)

Captures: `Afegir.png` (producte conegut), `AfegirTanda.png` (mode tanda), `AfegirNou.png` (producte nou), `AfegirNovaCat.png` (categoria nova ràpida). Fonts: `Afegir*.dc.html`.
Substitueix el botó «Add item» amb split i el formulari antic.

**Objectiu**: afegir en 3 tocs, amb una mà, cuinant, endreçant la compra o al súper. **Sense IA, veu ni càmera.** Camps obligatoris: **nom, categoria, data**.

## Estructura (pantalla completa)

1. Barra: `close` (48 dp) + «Afegeix» (`sheet-title`). En mode tanda, a la dreta pastilla «✓ N desats» (`fresh-container`).
2. **En aquesta tanda** (només mode tanda): xips 36 dp `surface-card` amb «Nom · dd/mm» de cada ítem desat; tocar-ne un → l'edita.
3. **Nom** (overline + camp 60 dp, `surface-high`, vora 2 dp `primary` amb focus, radi 16, text 19/600). Focus i teclat oberts en entrar. Botó `cancel` per buidar.
   - **Suggeriment de l'historial** (màx. 1 visible; fila 52 dp `surface-card` radi 16): `history` + nom amb la part escrita en negreta + categoria a la dreta. Tocar → omple nom i categoria.
   - **Producte nou** (cap coincidència): avís `primary-container` amb `add_circle` «**Producte nou.** Tria'n la categoria i la data.»
4. **Categoria** (overline + xips en **diverses línies, sense scroll**): xip 44 dp radi 22 amb forma 34 dp + icona + nom. Seleccionat: fons `primary-container`, vora 2 `primary`, forma `primary` amb icona blanca. Últim xip **«+ Nova»** amb vora discontínua (ressaltat en `primary-container` si és producte nou) → full Nova categoria ràpida.
5. **Caduca** (overline + caixa 72 dp `surface-high` radi 16):
   - Camp amb `inputmode=numeric`, format **dd / mm / aa**, tipografia `Bricolage 32/800`. Les barres s'insereixen soles. Accepta també dd/mm/aaaa.
   - A la dreta, text d'ajuda viu: «dimecres · d'aquí 10 dies» (o «ja ha caducat» en `expired`).
   - Botó `calendar_month` → `DatePicker`.
   - Fila de **dates ràpides** (scroll horitzontal): Demà, +3 dies, +1 setmana, +2 setmanes, +1 mes (des d'avui).
6. **Quantitat + Congelat** (grup connectat 64 dp): «Quantitat» amb stepper − N + (botons 44 dp connectats) · botó commutador «Congelat» (`ac_unit`).
7. **Botons inferiors** (grup connectat, 64 dp, fix a 24 dp de baix, per sobre del teclat):
   - **Desa i un altre** (`add`, `primary`, flex) → desa, buida el nom, **manté categoria i data**, mostra snackbar «Formatge cheddar desat · Desfés» i entra en mode tanda.
   - **Desa** (112 dp, `primary-container`) → desa i tanca. En mode tanda amb el nom buit es converteix en **Fet** (`primary`) i «Desa i un altre» queda desactivat.
   - Desactivats (fons `surface-high`, text `outline`) mentre faltin camps; a sobre, «Falten la categoria i la data per desar».

## Mode tanda (`AfegirTanda.png`)
- Categoria i data es mostren com a files resum connectades amb botó «Canvia» (en lloc dels xips i el camp de data), més la nota «Es mantenen del producte anterior. Si escrius un nom que ja coneixes, s'omplen amb el seu.»
- Si l'usuari tria un suggeriment, la categoria s'actualitza a la del suggeriment; la data es manté.

## Nova categoria ràpida (`AfegirNovaCat.png`)
`ModalBottomSheet` sobre Afegeix: títol «Nova categoria» · previsualització (forma 64 + icona) al costat del camp **Nom** · **Forma**: 6 opcions en graella (52 dp, radi 16; seleccionada `primary-container` + vora `primary`) · **Icona**: 12 opcions en graella 6×2 (48 dp rodones; seleccionada `primary`) · nota «La descripció i l'ordre es poden canviar després, a Rebost.» · botó **Crea i fes-la servir** → crea la categoria, tanca el full i la deixa seleccionada.

## Punts d'entrada
FAB de la barra flotant, FAB estès de Categoria (preselecciona categoria), widget, drecera de la icona, botó de l'Inici buit.

## Haptic
`Confirm` en desar; `Reject` si es toca un botó desactivat.
