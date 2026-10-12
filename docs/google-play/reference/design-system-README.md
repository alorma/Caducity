Caducity és una app Android per saber què tens a la nevera i al rebost i què caduca aviat, per no llençar menjar. El sistema visual és **Material 3 Expressive de rebost**: crema, oliva i tons de cuina, formes orgàniques i una tipografia de titulars ampla i càlida.

## Principis

- **El color és estat. La forma és categoria.** Els quatre colors d'estat (Caducat, Aviat, Fresc, Congelat) només signifiquen estat. Les categories no tenen color: es distingeixen per una forma orgànica (Galeta, Flor, Trèvol, Sol, Coixí) i una icona.
- **Primer, el que és urgent.** La pantalla d'inici és «Què caduca»: caducats, avui, demà i la resta de la setmana. Les categories viuen a Rebost.
- **Una forma per grup.** Les llistes i els grups de botons es connecten amb 2 px de separació i formen una sola peça: `radius-group` 24 fora, `radius-inner` 6 dins.
- **Colors suaus, contrast real.** Els contenidors d'estat són pastels; el text sobre ells sempre és el seu `on-*-container`. Els colors forts (`expired`, `soon`, `fresh`, `frozen`) només pinten barres, punts i formes, mai text petit.
- **Vermell per a Caducat i per a destruir.** `expired` també marca Esborra i Llença.

## Veu

Català, segona persona del singular, frases curtes. Botons en imperatiu: **Afegeix**, **Desa**, **Congela**, **Llença**, **Esborra**, **Revisa**. Dates relatives quan ajuden («Avui», «Demà», «Fa 2 dies», «D'aquí 10 dies») i absolutes curtes al costat («dc. 21 oct»). Res d'exclamacions ni emojis.

## Estats

| Estat | Fons | Text | Fort | Quan |
|---|---|---|---|---|
| Caducat | `expired-container` | `on-expired-container` | `expired` | La data ja ha passat |
| Aviat | `soon-container` | `on-soon-container` | `soon` | Dins del llindar d'avís (per defecte 3 dies) |
| Fresc | `fresh-container` | `on-fresh-container` | `fresh` | Més enllà del llindar |
| Congelat | `frozen-container` | `on-frozen-container` | `frozen` | Marcat com a congelat; no compta caducitat |

Els estats es diferencien també per lluminositat i sempre porten etiqueta de text, mai només color.

## Color

Superfícies: `surface` (fons), `surface-card` (files i targetes), `surface-high` (camps, botons no seleccionats), `surface-shape` (forma de categoria). Text: `ink`, `ink-variant`. Acció: `primary` oliva amb `on-primary`; `primary-container` per a la barra flotant. Tema clar i fosc; el color dinàmic del sistema és opcional i desactivat per defecte.

## Tipografia

Dues famílies de Google Fonts. **Bricolage Grotesque** per a títols i xifres, pes 700–800 amb `font-stretch: 80%` (amples d'ull, estrets de caixa). **Figtree** per a la resta. Escala: `display` 44 · `screen-title` 40 · `number` 30 · `sheet-title` 24 · `section-title` 22 · `card-title` 20 · `body-large` 17 · `title-row` 16 · `body` 15 · `label` 14 · `body-small` 13 · `overline` 12 en majúscules amb 1.2 px d'espaiat.

## Formes

- **Categoria:** forma orgànica de `MaterialShapes` farcida de `surface-shape` amb la icona en `primary`. Galeta (`Cookie9Sided`) Làctics, Flor (`Flower`) Fruita i verdura, Trèvol (`Clover4Leaf`) Carn i peix, Sol (`Sunny`) Rebost, Coixí (`Puffy`) Fleca. L'usuari tria forma i icona en crear una categoria.
- **Radis:** `radius-inner` 6, `radius-joint` 8, `radius-chip` 12, `radius-field` 16, `radius-fab` 20, `radius-group` 24, `radius-card` 28, `radius-sheet` 32, `radius-full`.
- **Ombres:** només els elements flotants (`shadow-float`).

## Iconografia

**Material Symbols Rounded**, 24 dp (20 dins de xips). Contorn per defecte; farcit per a la destinació activa. Navegació: `hourglass_bottom` Aviat, `kitchen` Rebost, `settings` Ajustos. Accions: `check` Consumit, `ac_unit` Congela, `event_repeat` Canvia la data, `delete` Esborra.

## Patrons de pantalla

- **Inici · Què caduca:** títol `display`, bàner de caducats (`expired-container`, botó Revisa), resum connectat Aviat · Frescos · Congelats, i seccions Avui / Demà / dies de la setmana amb files connectades i botó ràpid Consumit.
- **Rebost:** cercador, graella 2 columnes de targetes de categoria (forma, nom, recompte, barra d'estats proporcional) i targeta «Nova categoria».
- **Categoria:** capçalera amb la forma gran, xips de filtre per estat i llista agrupada per estat. Un producte amb diverses unitats es desplega; amb una sola unitat és una fila simple.
- **Accions d'un producte:** full inferior amb Consumit i Congela connectats, dates ràpides (+1 dia, +3 dies, +1 setmana, Tria) i Edita / Mou / Esborra.
- **Navegació:** barra flotant (`HorizontalFloatingToolbar`) amb Aviat · Rebost · Ajustos i FAB Afegeix al costat.

## Implementació

Jetpack Compose amb Material 3 Expressive: `HorizontalFloatingToolbar`, `FloatingActionButton` mida mitjana, `ButtonGroup`, `FilterChip`, `MaterialShapes`, `ModalBottomSheet`, `LargeFlexibleTopAppBar`, `DatePicker`. Els previews d'aquest sistema són HTML estàtic que fa servir els tokens; no hi ha paquet de components web.
