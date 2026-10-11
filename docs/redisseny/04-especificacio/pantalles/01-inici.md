# Inici · «Què caduca»

Captures: `Main.png` (amb dades), `InicibBuit.png` (buit). Font: `Main.dc.html`, `InicibBuit.dc.html`.
Substitueix: `01-app-original/app/01_dashboard.png`.

## Estructura (de dalt a baix)

1. **Capçalera** (padding 28 dalt, 20 laterals)
   - Overline: data d'avui, format «Diumenge, 11 d'octubre» (`ink-variant`).
   - Títol `display`: «Què caduca».
   - A la dreta, dos botons rodons de 48 dp sobre `surface-high` (gap 8): `calendar_month` («Calendari» → vista de mes, vegeu `11-calendari.md`) i `search` («Cerca» → Rebost amb el cercador enfocat).
2. **Tira de 7 dies** (`WeekStrip`, vegeu `11-calendari.md`): comença avui, punts per producte i estat; tocar un dia fa scroll a la seva secció.
3. **Bàner de caducats** (només si n'hi ha ≥ 1)
   - Fons `expired-container`, radi 24, text `on-expired-container`.
   - Forma Segell 48 dp en `expired` amb icona `delete_history` blanca farcida.
   - «N productes caducats» (`body-large`), sota «El més antic, fa N dies» (`body-small`).
   - Acció «Revisa ›» → `expired`.
4. **Resum connectat** (3 caselles, gap 3, radis 20/8)
   - Aviat (`soon-container`), Frescos (`fresh-container`), Congelats (`frozen-container`). Número `number`, etiqueta `label` 13.
   - Aviat → fa scroll a la primera secció; Frescos/Congelats → Rebost (filtrat si es vol).
5. **Seccions per dia**: només productes `SOON` (dins el llindar). Agrupats per dia: «Avui», «Demà», després «Dimarts 13», «Dimecres 14»…
   - Capçalera de secció: títol `section-title` + subtítol «dilluns 12 · 3 productes» (`ink-variant`).
   - **Fila** (llista connectada, mín. 68 dp): forma de categoria 44 dp + nom (`title-row`) + «Categoria · quantitat» (`body-small`) + botó rodó 44 dp `check` («Marca com a consumit») sobre `surface`.
   - **Una fila per (producte, dia)**: si un producte té diverses unitats que caduquen el mateix dia, una sola fila amb «N unitats». El recompte de la secció compta unitats.
   - Tocar la fila → full Accions.
6. **Barra flotant + FAB** (vegeu `01-navegacio.md`). Deixar 140 dp de padding inferior al contingut.

## Estats

- **Sense caducats**: no es mostra el bàner.
- **Res aviat però hi ha productes**: en lloc de seccions, missatge «Res caduca aviat. Bona feina.» amb enllaç «Mira el rebost».
- **Rebost buit** (`InicibBuit.png`): títol + il·lustració de 3 formes (Galeta `fresh-container`, Sol `soon-container`, Flor `surface-shape`) + «El rebost és buit» + «Comença per la compra d'avui: afegeix el que acabes de guardar a la nevera.» + botó «Afegeix el primer producte». A sota, consell descartable del widget (`widgetTipDismissed`).

## Interaccions

- Botó `check` → marca **una** unitat (la més propera a caducar) com a Consumit + snackbar «Llet sencera consumida · Desfés».
- Pull-to-refresh no cal (dades locals).
