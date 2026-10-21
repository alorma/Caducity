# Afegeix i Edita producte

Captures: `captures/Afegir` (des de l'historial), `AfegirNou` (producte nou), `AfegirCategoria`, `AfegirData`, `AfegirQuantitat`, `EditaProducte`, `EditaProducteCongelat`.

## Per què

El formulari actual és una columna llarga d'elements de pesos diferents: xips de categoria, un camp de data gran amb dreceres que no hi caben, «Com ho comptes?» en una fila sencera, i Quantitat + Congelat amb radis que no lliguen. Un placeholder («dd/mm/aa») va amb tipografia de display i al final queda un forat buit. El fem com la pantalla de categoria: **el nom a dalt i les opcions en files que obren fulls**.

## 1. Pantalla principal (Afegeix)

- **Barra superior**: ✕ · «Afegeix» · **Desa** (a la barra, com a Categoria). Desactivat fins que hi ha nom, categoria i data.
- **Nom**: camp d'una línia, amb el focus en obrir. Placeholder «Què afegeixes?» en pes normal i `onSurfaceVariant`. Mentre s'escriu, els suggeriments de l'historial surten com ara.
- **Línia d'estat** sota el nom (petita, `onSurfaceVariant`, icona a l'esquerra):
  - triat de l'historial: 🕘 «Del teu historial: categoria i data ja posades»;
  - nom nou: ⊕ «Producte nou. Tria'n la categoria i la data.»
- **Opcions** (llista agrupada, mateix estil que Ajustos i Categoria; files de 68 dp):
  1. **Categoria**: a l'esquerra, la forma i la icona de la categoria triada (o `category` si no n'hi ha); valor «Làctics ›». Sense valor: subtítol «Obligatori» i valor «Tria ›» en `primary`.
  2. **Caduca**: subtítol «diumenge · d'aquí 7 dies»; valor «25 oct ›». Sense valor: «Obligatori» · «Tria ›».
  3. **Quantitat**: valor «1 unitat ›», «3 tuppers ›»…
  4. **Ja és congelat**: subtítol «Va directe a Congelats», amb interruptor. Només surt si la categoria es pot congelar (si aquesta feature encara no existeix, surt sempre).
- Si falta algun obligatori, sota la llista: «Falten la categoria i la data per desar».

### Flux guiat

- Nom **nou** + ↵ del teclat → s'obre el full **Categoria**; en triar-ne una → s'obre el full **Caduca** (si la categoria té durada habitual, la data ja ve proposada i només cal «Fet»).
- Nom de l'**historial** → categoria, data (per durada habitual) i tipus de quantitat es posen sols; no s'obre cap full.
- Sempre es pot tocar qualsevol fila per canviar-la.

## 2. Full Categoria (`AfegirCategoria`)

Bottom sheet «Categoria» · «On el guardes?». Xips grans (52 dp) amb forma + icona + nom, en flux; el triat amb `primaryContainer` i contorn `primary`. **Tocar un xip tria i tanca** (sense «Fet»). Últim xip: «+ Nova» (contorn discontinu) → obre la pantalla de **Nova categoria**; en desar-la, queda triada i es torna a Afegeix.

## 3. Full Caduca (`AfegirData`)

- Títol «Caduca» · «Escriu-la o tria una drecera.»
- Camp de data gran (76 dp) amb focus i teclat numèric; placeholder «dd / mm / aa» en **pes normal** i `onSurfaceVariant`; botó de calendari a la dreta.
- Dreceres en graella 3 × 2: Demà · +3 dies · +1 setmana · +2 setmanes · +1 mes · +3 mesos. La que coincideix amb la data actual es marca.
- Línia de durada habitual: «Suggerida: els Làctics duren 1 setmana» (i la drecera corresponent marcada) o, si la categoria no en té, «Sense durada habitual per a Begudes · **Posa'n una**» → full Durada de la categoria.
- Botó «Fet».

## 4. Full Quantitat (`AfegirQuantitat`)

- − N + gran (72 dp), amb el nom del subtipus al costat del número («3 tuppers»).
- «Com ho comptes»: xips Unitats · Tuppers · Paquets · Llaunes (selecció única).
- Enllaç «Grams, mil·lilitres o a grapats ›» → full «Com ho comptes?» complet.
- Botó «Fet».
- Si els **tipus d'unitat** encara no existeixen al codi: només el − N + amb «unitats», sense xips ni enllaç.

## 5. Edita (`EditaProducte`, `EditaProducteCongelat`)

Mateixa pantalla, amb els valors plens.
- Barra: ✕ · «Edita» · 🗑 (vermell; elimina amb snackbar «Desfés») · **Desa** (només actiu si hi ha canvis).
- Sense línia d'estat ni suggeriments d'historial.
- **Sense «Ja és congelat»**: congelar, consumir i llençar són accions. Nota a sota: «Per congelar-lo, consumir-lo o llençar-lo, fes servir les accions del producte.»
- **Producte congelat**: la fila Caduca passa a **«Congelat el»** (icona ❄, fons blau clar `#E3EEF1` / el contenidor de l'estat congelat del tema), subtítol «fa 3 setmanes», valor «28 set ›» → el mateix full de data amb el títol «Congelat el» i sense dreceres de futur. Nota: «Està congelat. Per descongelar-lo, fes servir ☀ a la llista o a les accions.»
- Canviar la categoria mou el producte.
- **Model**: una fila = un producte amb N unitats i una sola data. Editar només canvia aquella fila. Dos productes amb el mateix nom continuen sent productes diferents (canviar-ho és una altra feature).

## 6. Bug relacionat

A la captura actual, les categories per defecte surten **en anglès** («Dairy», «Fruit & veg», «Dry goods»…) amb la UI en català, i Fruit & veg i Dry goods comparteixen la mateixa icona de fulla. Les categories per defecte han de mostrar el nom traduït a l'idioma de l'app (mentre l'usuari no les hagi reanomenat) i fer servir les seves icones (`nutrition`, `grocery`…).

## 7. Textos (ca / es / en)

Què afegeixes? / ¿Qué añades? / What are you adding? · Del teu historial: categoria i data ja posades / De tu historial: categoría y fecha ya puestas / From your history: category and date already set · Producte nou. Tria'n la categoria i la data. / Producto nuevo. Elige su categoría y fecha. / New item. Pick its category and date. · Obligatori / Obligatorio / Required · Tria / Elegir / Choose · Ja és congelat / Ya está congelado / Already frozen · Va directe a Congelats / Va directo a Congelados / Goes straight to Frozen · On el guardes? / ¿Dónde lo guardas? / Where do you keep it? · Escriu-la o tria una drecera. / Escríbela o elige un atajo. / Type it or pick a shortcut. · Posa'n una / Pon una / Set one · Com ho comptes / Cómo lo cuentas / How you count it · Congelat el / Congelado el / Frozen on · Per congelar-lo, consumir-lo o llençar-lo, fes servir les accions del producte. / Para congelarlo, consumirlo o tirarlo, usa las acciones del producto. / To freeze, use up or throw it out, use the item's actions.
