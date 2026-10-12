# Transforma (cuinar: ingredients → elaboracions)

Captures: `TransformaSeleccio.png`, `TransformaCaldo.png`, `TransformaPollastre.png`, `TransformaIngredient.png`, `EntradaMenu.png`, `EntradaAccions.png`. Fonts: els `.dc.html` homònims.

## Idea
Un o més ingredients del rebost es converteixen en **una o més elaboracions noves** (2 preparats de verdures + 1 pollastre → 4 racions de caldo; 1 pollastre → pits, ales, cuixes i carcassa). Els ingredients **desapareixen del rebost**; les elaboracions hi **entren com a productes nous**. Ha de ser ràpid: nom + quantitat + (opcional) congelar, i desar.

## Punts d'entrada
1. **Barra de selecció** (Caducats, Aviat, Frescos, Congelats, Categoria): en seleccionar productes, la barra inferior mostra **Consumit · Congela · Transforma** (grup de botons connectat, 68 dp, icona sobre el text; Transforma és el principal, `primary`). Es pot seleccionar d'unes seccions i d'altres, i diverses unitats d'un mateix producte. Captura `TransformaSeleccio`.
2. **Pulsació llarga al «+» de l'Inici**: menú amb **Transforma** i **Afegeix producte** (píndoles `primary-container`, el «+» passa a «×», scrim 45 %). El toc normal al «+» continua obrint «Afegeix producte». Captura `EntradaMenu`. *(Gest ocult: no és l'entrada principal.)*
3. **Full d'Accions d'un producte**: fila «Transforma amb aquest» (`primary-container`) entre «Mou a una altra categoria» i «Esborra». Obre Transforma amb aquest producte ja afegit com a ingredient (per defecte **1 unitat**; si en té més, el selector `− 1 de N +` permet canviar-ho). Captura `EntradaAccions`.

## Pantalla «Transforma» (ruta a pantalla completa, no diàleg)
Barra superior: `×` (tanca; si hi ha canvis, confirma el descart) · títol «Transforma» · botó **Desa** (`primary`; desactivat sense nom ni ingredients).

### Ingredients
Llista connectada (radis 24 exterior / 6 interior). Cada fila: forma de la categoria 44 dp, nom, meta «Caduca dl. 26 oct» i **×** per treure'l.
- Si l'ingredient té **diverses unitats** disponibles, la fila mostra un selector `− 1 de 2 +` (mínim 1, màxim les unitats que queden). Cada unitat és un ítem independent: es gasten les que marqui el selector, començant per les que caduquen primer.
- Última fila, tonal: **«Afegeix ingredient»** → pantalla d'ingredients (vegeu avall).
- Mínim 1 ingredient. Treure l'últim → es tanca amb confirmació.

### Resultat (una targeta per elaboració)
Cas simple (un resultat; `TransformaCaldo`): targeta `surface-card` amb
- camp **Nom** (obligatori; autoenfocat si ve buit; suggeriment amb historial del nom);
- **Racions / unitats**: selector `− 4 +` (1–99). Es crea **un ítem per unitat** (4 racions = 4 ítems del mateix producte, «Unitat N de 4»);
- xips: **categoria** (per defecte, la de l'historial d'aquest nom; si no n'hi ha, la del primer ingredient) i **data de caducitat** (per defecte, la de l'**ingredient que caduca primer**; text d'ajuda «Data de l'ingredient que caduca primer (Pollastre sencer)»). La data **mai s'allarga sola**; l'usuari pot canviar-la;
- fila **«Congela'n»** amb selector `− 2 +` (0..N) i resum «2 a la nevera · 2 al congelador». Els congelats entren com a `FROZEN` (congelats avui, **sense data de caducitat**), la resta frescos amb la data del xip.

Cas de diversos resultats (`TransformaPollastre`): cada resultat és una fila **plegada** (forma, nom, «2 unitats · Carn i peix · caduca dc. 14 oct» i xip «2 congelats» si n'hi ha) amb xevró; en tocar-la s'expandeix amb la targeta completa de dalt. Sota, **«Afegeix un altre resultat»** (tonal). El títol de secció compta «Resultat · 4 productes». Un resultat es pot treure (swipe o ×) mentre en quedi un.
- Pista: «Toca un resultat per canviar-ne el nom, les unitats, la categoria, la data o quantes en congeles.»

Nota al peu: «Els ingredients desapareixen del rebost en desar.»

### Afegeix ingredients (`TransformaIngredient`)
Pantalla a pantalla completa (no diàleg): cerca «Cerca al rebost», xips d'estat (Tots · Aviat · Fresc · **Congelat**) i de categoria (desplaçables), i llista de productes amb casella, meta (categoria · unitats/pes) i **pastilla d'estat** (Aviat/Fresc/Congelat). **Inclou ítems congelats.** Es poden marcar productes ja afegits (apareixen seleccionats). Botó inferior ample «Afegeix N ingredients». Els **caducats** no es llisten aquí (vegeu «Obert» 2).

## En desar
1. Es crea cada elaboració com a producte nou (+ ítems) a la categoria triada, amb `createdAt = ara`.
2. Els ítems ingredient passen a estat final **`TRANSFORMED`** (nou valor de `endReason`, al costat de `CONSUMED` i `DISCARDED`) amb `endDate` i `transformationId`.
3. **Desapareixen de tot arreu**: llistes, calendari, resum de l'Inici, widget, Wear OS, notificacions.
4. **Estadístiques**: compten com a **«transformats»** (ni consumits ni llençats). El resultat conté `origin = transformationId` perquè es pugui veure «d'on ve» (només dades; sense pantalla).
5. Les **restes** (pells, ossos…) no es registren.
6. Snackbar a l'origen: «5 ingredients transformats en 4 productes · **Desfés**». Desfés: elimina els resultats i restaura els ingredients (mateixos ítems, estat anterior).
7. Navegació: torna a la pantalla d'on s'havia entrat; si era una selecció, la selecció es tanca.

## Model de dades
- `ItemEntity.endReason`: afegir `TRANSFORMED` (Room: migració; els valors existents no canvien).
- `ItemEntity.transformationId: Long?` (ingredients i resultats comparteixen id) i `ItemEntity.originTransformationId` opcional als resultats (o un únic camp amb rol).
- `TransformationEntity(id, createdAt)`; sense més metadades.
- Un resultat congelat: `frozenDate = avui`, `expiryDate = null` (com qualsevol ítem congelat).
- Estats derivats: sense canvis; `TRANSFORMED` s'exclou de tots els filtres de «viu».

## Moviment
- Fila de resultat plegat → expandit: `AnimatedVisibility` amb expansió vertical + fos (`MotionScheme` *default spatial*).
- En desar: les files d'ingredients es tanquen en cascada (80 ms) i després la pantalla es tanca.

## Accessibilitat
- Cada selector té etiquetes «Una menys»/«Una més» i anuncia «1 de 2» com a regió `Polite`.
- Els botons de la barra de selecció porten el recompte («Transforma 2 productes»).
- Zones tàctils ≥ 48 dp; el xip «N congelats» té descripció «2 congelats».

## Textos
| Clau | Text |
|---|---|
| `transform_action` | Transforma |
| `transform_title` | Transforma |
| `transform_save` | Desa |
| `transform_section_ingredients` | Ingredients |
| `transform_add_ingredient` | Afegeix ingredient |
| `transform_ingredient_units` | %1$d de %2$d |
| `transform_section_result` | Resultat |
| `transform_section_results` | Resultat · %d productes |
| `transform_result_name` | Nom |
| `transform_servings` | Racions |
| `transform_date_hint` | Data de l'ingredient que caduca primer (%s). |
| `transform_freeze` | Congela'n |
| `transform_freeze_summary` | %1$d a la nevera · %2$d al congelador |
| `transform_add_result` | Afegeix un altre resultat |
| `transform_results_hint` | Toca un resultat per canviar-ne el nom, les unitats, la categoria, la data o quantes en congeles. |
| `transform_footer` | Els ingredients desapareixen del rebost en desar. |
| `transform_picker_title` | Afegeix ingredients |
| `transform_picker_search` | Cerca al rebost |
| `transform_picker_add` | Afegeix %d ingredients |
| `transform_done` | %1$d ingredients transformats en %2$d productes |
| `transform_menu_add` | Afegeix producte |
| `transform_from_actions` | Transforma amb aquest |
| `transform_discard_title` | Descartar els canvis? |

## Obert
1. **Entrada des del «+»**: pulsació llarga (proposat). Si es vol més descobrible, una pista única «Mantén premut + per cuinar».
2. Poder transformar ítems **caducats** (revisió a Caducats): proposat sí, amb data per defecte «avui».
3. Estadístiques de «transformats»: només dades per ara.

## Fora d'abast
Receptes, escalat de quantitats, pes/volum dels resultats, historial de transformacions visible, restes.
