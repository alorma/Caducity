# Especificació — imatges de Google Play

## 1. Entregables i requisits de Play

| Fitxer de sortida | Mida | Format | Notes |
|---|---|---|---|
| `icon-512.png` | 512×512 | PNG 32 bits, ≤ 1 MB | Quadrat ple, **sense** cantonades arrodonides ni ombra (Play aplica la màscara). |
| `feature-1024x500.png` | 1024×500 | PNG o JPEG 24 bits, **sense alfa** | Res d'important a les vores. |
| `phone-01-inici.png` … `phone-07-mode-fosc.png` | 1080×1920 | PNG o JPEG 24 bits, **sense alfa** | Relació 9:16. Play accepta de 2 a 8 captures de telèfon. |

**Tres idiomes**: català (`ca`), castellà (`es-ES`) i anglès (`en-US`). Es genera el mateix conjunt per a cada idioma: `icon-512.png` és únic (igual per a tots); `feature-1024x500` i `phone-01…07` es repeteixen per idioma, en una carpeta per locale (`ca/`, `es-ES/`, `en-US/`). Els textos de cada idioma són a la secció 4b. Les captures reals han de fer-se amb l'app **en l'idioma corresponent** (vegeu el prompt). Ordre de les captures: el de la taula 4 (les 3-4 primeres són les que es veuen sense desplaçar-se a la fitxa).

## 2. Design system

Fitxers: `reference/tokens.json` (valors exactes) i `reference/design-system-README.md` (principis).

- Tipografia: **Bricolage Grotesque** (titulars, pes 800, `font-stretch: 80%`) i **Figtree** (text). Icones: **Material Symbols Rounded**.
- Colors usats a les imatges:

| Ús | Valor |
|---|---|
| Mostassa (marca, fons icona) | `#E8B95A` |
| Oliva `primary` | `#4D5A1E` |
| Contenidor oliva `primary-container` | `#DCE3B0` |
| Crema `surface` | `#FBF6EC` |
| `surface-high` | `#ECE2CF` |
| Tinta `ink` | `#29241C` |
| `ink-variant` | `#665C4C` |
| `on-primary-container` | `#283010` |
| Fosc: fons / tinta / variant / bisell | `#1C1A15` / `#F1EADC` / `#CFC4B1` / `#4A443A` |

Els colors d'estat (caducat, aviat, fresc, congelat) **no** es fan servir com a fons de les imatges.

## 3. Icona (`Main.dc.html`)

- Fons: `#E8B95A`, 512×512, quadrat ple.
- Forma Galeta (`MaterialShapes.Cookie9Sided`, aproximada al mockup amb la corba polar `r = 50 − a + a·cos(n·(t + rot))`, n = 9, a = 3, rot = 0, en una caixa de 100×100) de **341 px**, color `#4D5A1E`, centrada.
- Fulla: glif `eco` de Material Symbols Rounded, `FILL 1`, `wght 500`, `opsz 48`, **183 px**, color `#FBF6EC`, centrada.
- Zona segura: tot el contingut dins del cercle central de 341 px (66 %).
- Comprova que la icona adaptativa de l'app (foreground/background) coincideix amb aquesta.

## 4. Captures de telèfon (`Play1…Play7.dc.html`)

Disseny en un llenç de **360×640** (es renderitza a ×3 → 1080×1920).

Composició comuna:

- Títol: Bricolage Grotesque 800, `font-stretch 80%`, **28 px**, interlineat 1.04, `letter-spacing −0.3px`, màxim 2 línies. Posició: `left/right 28 px`, `top 26 px`.
- Subtítol: Figtree 500, **13.5 px**, interlineat 1.3, a 6 px sota el títol, màxim 2 línies.
- Telèfon: caixa de **232×502** amb **bisell de 6 px**, radi exterior 34 px, `overflow hidden`, a `left 58 px`, `top 134 px` (la caixa total és 244×514 i toca el límit inferior). Dins hi va la pantalla de l'app a mida 390×844 escalada ×0,5949.
- Ombra del telèfon: `0 10px 28px rgba(41,36,28,0.28)` (fons clars), `rgba(0,0,0,0.35)` sobre oliva, `rgba(0,0,0,0.5)` sobre fosc.

Taula de captures:

| # | Fitxer | Pantalla de l'app | Fons | Color títol / subtítol | Bisell | Títol | Subtítol |
|---|---|---|---|---|---|---|---|
| 1 | `phone-01-inici` | Inici · Què caduca | `#E8B95A` | `#29241C` / `#29241C` | `#29241C` | Què caduca, d'un cop d'ull | Caducats, avui, demà i la resta de la setmana, ordenats per urgència. |
| 2 | `phone-02-afegeix` | Afegeix producte | `#4D5A1E` | `#FBF6EC` / `#DCE3B0` | `#29241C` | Afegeix un producte en 3 tocs | Nom, categoria i data. Escriu la data amb el teclat numèric, com a l'envàs. |
| 3 | `phone-03-accions` | Full d'accions d'un producte (sobre la llista de Làctics amb vel) | `#DCE3B0` | `#29241C` / `#283010` | `#29241C` | Consumeix, congela o canvia la data | Toca un producte i decideix si el consumeixes, el congeles o en canvies la data. |
| 4 | `phone-04-rebost` | Rebost | `#E8B95A` | `#29241C` / `#29241C` | `#29241C` | Tot el rebost, per categories | Cada categoria té la seva forma i una barra amb l'estat dels productes. |
| 5 | `phone-05-categoria` | Categoria · Làctics | `#ECE2CF` | `#29241C` / `#665C4C` | `#29241C` | Filtra per estat i per dia | Caducat, aviat, fresc o congelat, i una tira amb els propers 7 dies. |
| 6 | `phone-06-calendari` | Calendari de mes | `#4D5A1E` | `#FBF6EC` / `#DCE3B0` | `#29241C` | Calendari de caducitats | Veu què caduca cada dia del mes, amb un punt de color per estat. |
| 7 | `phone-07-mode-fosc` | Inici en mode fosc | `#1C1A15` | `#F1EADC` / `#CFC4B1` | `#4A443A` | També en mode fosc | Colors suaus i contrast real, de dia i de nit. |

La captura 7 només es fa si el mode fosc ja és a l'app que es publica; si no, es descarta.

Els títols i subtítols de la taula són els **catalans**. Per a castellà i anglès, vegeu la secció 4b. Al canvas hi ha un artboard per idioma (`Play1…7`, `Play1…7Es`, `Play1…7En`); la maquetació és idèntica, només canvia el text.

## 4b. Textos per idioma

**Captures** (títol / subtítol):

| # | Català | Castellano | English |
|---|---|---|---|
| 1 | Què caduca, d'un cop d'ull / Caducats, avui, demà i la resta de la setmana, ordenats per urgència. | Qué caduca, de un vistazo / Caducados, hoy, mañana y el resto de la semana, ordenados por urgencia. | What expires, at a glance / Expired, today, tomorrow and the rest of the week, sorted by urgency. |
| 2 | Afegeix un producte en 3 tocs / Nom, categoria i data. Escriu la data amb el teclat numèric, com a l'envàs. | Añade un producto en 3 toques / Nombre, categoría y fecha. Escribe la fecha con el teclado numérico, como en el envase. | Add a product in 3 taps / Name, category and date. Type the date on the number pad, like on the package. |
| 3 | Consumeix, congela o canvia la data / Toca un producte i decideix si el consumeixes, el congeles o en canvies la data. | Consume, congela o cambia la fecha / Toca un producto y decide si lo consumes, lo congelas o le cambias la fecha. | Eat, freeze or change the date / Tap a product and decide whether to eat it, freeze it or change its date. |
| 4 | Tot el rebost, per categories / Cada categoria té la seva forma i una barra amb l'estat dels productes. | Toda la despensa, por categorías / Cada categoría tiene su forma y una barra con el estado de los productos. | Your whole pantry, by category / Each category has its own shape and a bar showing the state of its products. |
| 5 | Filtra per estat i per dia / Caducat, aviat, fresc o congelat, i una tira amb els propers 7 dies. | Filtra por estado y por día / Caducado, pronto, fresco o congelado, y una tira con los próximos 7 días. | Filter by status and by day / Expired, soon, fresh or frozen, plus a strip with the next 7 days. |
| 6 | Calendari de caducitats / Veu què caduca cada dia del mes, amb un punt de color per estat. | Calendario de caducidades / Mira qué caduca cada día del mes, con un punto de color por estado. | Expiry calendar / See what expires each day of the month, with a colored dot per status. |
| 7 | També en mode fosc / Colors suaus i contrast real, de dia i de nit. | También en modo oscuro / Colores suaves y contraste real, de día y de noche. | Dark mode too / Soft colors and real contrast, day and night. |

**Feature graphic** (lema sota «Caducity»; el nom de l'app no es tradueix):

- ca: Que no et caduqui res a la nevera.
- es: Que no se te caduque nada en la nevera.
- en: Don't let anything expire in your fridge.

Revisa que els textos en castellà i anglès coincideixin amb la terminologia de l'app en aquell idioma (estats Caducat/Aviat/Fresc/Congelat, «Rebost», «Consumit»); si l'app fa servir un altre mot, adapta el text de la imatge a l'app.

## 5. Feature graphic (`Feature.dc.html`) — 1024×500

- Fons `#FBF6EC`.
- Galeta mostassa `#E8B95A` de 620 px a `left 480, top −60` (surt per dalt, dreta i baix). Galeta `#DCE3B0` de 220 px a `left −80, top 372`.
- Bloc de text a `left 64`, amplada 430, centrat en vertical, `gap 20`:
  1. Mosaic d'icona 88×88, radi 26, fons `#E8B95A`, galeta oliva 59 px, fulla crema 31 px.
  2. «Caducity»: Bricolage Grotesque 800, `font-stretch 80%`, **96 px**, interlineat 0.92, `letter-spacing −1.5px`.
  3. «Que no et caduqui res a la nevera.»: Figtree 600, 26 px, interlineat 1.25, color `#665C4C`, amplada màx. 400.
- Dues pantalles de l'app (Inici i Rebost) amb bisell de 5 px `#29241C`, radi 30: la primera a `left 548, top 44`; la segona a `left 786, top 92`. Cada pantalla de 390×844 escalada ×0,54 (caixa interior 211×456). Les dues surten per baix.

## 6. Dades de mostra per a les captures reals

Data del dispositiu: **diumenge 11 d'octubre de 2026** (perquè coincideixin dies de la setmana i etiquetes «Avui / Demà»).

Resums de l'Inici: 7 caducats (el més antic, fa 5 dies), 9 Aviat, 42 Frescos, 3 Congelats. Rebost: 61 productes en 5 categories.

Categories (forma · icona): Làctics (Galeta · `water_full`), Fruita i verdura (Flor · `nutrition`), Carn i peix (Trèvol · `set_meal`), Rebost (Sol · `grocery`), Fleca (Coixí · `bakery_dining`).

Productes visibles a l'Inici:

| Quan | Producte | Categoria · quantitat |
|---|---|---|
| Avui | Llet sencera | Làctics · 1 brick |
| Avui | Espinacs | Fruita i verdura · 1 bossa |
| Demà | Pernil dolç | Carn i peix · 150 g |
| Demà | Maduixes | Fruita i verdura · 1 safata |
| Demà | Mozzarella | Làctics · 1 unitat |
| Dimarts 13 | Iogurt natural | Làctics · 2 unitats |
| Dimecres 14 | Hummus | Rebost · 1 pot |
| Dimecres 14 | Pa de motlle | Fleca · 1 paquet |

Categoria Làctics (14 productes): Caducat 1 (Iogurt grec, fa 2 dies) · Aviat 4 (Llet sencera avui, Mozzarella demà, Iogurt natural 2 unitats dt. 13…) · Fresc 8 (Formatge cheddar 3 unitats, propera dc. 21 oct; Mantega; Kéfir…) · Congelat 1 (Parmesà ratllat).

Accions: Formatge cheddar, «Unitat 1 de 3 · caduca dc. 21 oct», pastilla «10 dies».

Afegeix: nom «Formatge ched» amb suggeriment «Formatge cheddar · Làctics», categoria Làctics seleccionada, data 21/10/26.

Calendari: octubre 2026, dia 11 seleccionat, punts d'estat per dia com al mockup `Cd Calendari`.

Les xifres exactes són orientatives: l'objectiu és que la captura real s'assembli al mockup, no que sigui idèntica.
