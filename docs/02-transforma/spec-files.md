# Transforma · pantalla en files (reorganització de la UI)

Captures: `TransformaCarrBuit.png` (accés lliure), `TransformaCarrMolts.png` (editant un resultat, amb 9 ingredients, 6 resultats, un stepper d'ingredient obert i «Congela'n» obert), `TransformaCarrPlegat.png` (després de «Fet»). Fonts: els `.dc.html` homònims.

**Substitueix l'apartat «Pantalla Transforma» de `16-transforma.md`** (només el disseny; la **lògica no canvia**: entrades, accés lliure, selector d'ingredients, desar, model de dades, desfés, snackbar i textos de dades). Les captures antigues `TransformaCaldo`, `TransformaPollastre` i `TransformaBuit` queden obsoletes (el cas «1 pollastre → 4 resultats» és ara simplement una fila de 4 resultats).

## Per què
A la versió vertical, cada ingredient era una targeta alta amb el seu stepper sempre visible i cada resultat una targeta + un formulari a part. Amb 4 ingredients i 3 resultats la pantalla ocupava 3-4 alçades de mòbil, tot tenia el mateix pes i era «carregós». Cal que hi càpiguen molts ingredients i molts resultats (4-5 o més) sense que la pantalla creixi.

## Estructura (de dalt a baix)
Pantalla a pantalla completa; **tot el contingut fa scroll vertical**, amb la barra de **Desa** fixa a baix (el contingut té `padding-bottom` ≈ 104 dp perquè no quedi tapat).

1. **Barra superior**: només `×` (tanca; confirma el descart si hi ha canvis) i el títol «Transforma». **Ja no hi ha «Desa» a dalt.**
2. **Secció «Ingredients · N»**: capçalera amb etiqueta (12 sp, majúscules, +1,2 sp) i, a la dreta, el botó tonal petit **«+ Afegeix»** (32 dp, `primary-container`) → pantalla d'ingredients (`TransformaIngredient`). El botó és **fora de l'scroll**: sempre a l'abast. Sota, una **fila amb scroll horitzontal** (`LazyRow`, `contentPadding` 20 dp, `spacedBy(10.dp)`, claus per id) de targetes d'ingredient.
3. **Secció «Resultat · M»**: mateixa capçalera amb **«+ Afegeix»** (afegeix un resultat buit, el selecciona, fa scroll fins a ell i li dona el focus al nom). Sota, una **fila horitzontal** de targetes de resultat. **Amb un sol resultat la fila no es mostra**: només el formulari (la fila apareix quan n'hi ha 2 o més).
4. **El resultat seleccionat és el formulari**: no hi ha un formulari a part. La targeta seleccionada **s'expandeix al seu lloc, dins la mateixa fila**, i es converteix en el formulari; les targetes veïnes queden empeses als costats i només se'n veu **una vora fina (≈10 dp) als marges de la pantalla**. En tocar una altra targeta (o la seva vora) o en fer scroll fins a una i tocar-la, la que era oberta es **replega** i s'**expandeix** l'altra. Hi ha **com a màxim un** resultat expandit: el formulari té un botó **«Fet»** que el replega, i aleshores **tots els resultats queden plegats** com a targetes petites a la fila (captura `TransformaCarrPlegat`). Tocar una targeta plegada l'expandeix.
5. Nota al peu: «Els ingredients desapareixen del rebost en desar.» (única nota; la resta de textos d'ajuda s'eliminen, vegeu «Textos»).
6. **Barra inferior fixa**: botó ample `primary` de 56 dp, amb degradat d'esvaïment cap al fons de pantalla i `imePadding` (queda sobre el teclat): «Desa · M productes». Desactivat (`surface-container-high`, text apagat) mentre no es pugui desar.

## Targeta d'ingredient (148 × 118 dp, radi 20, `surface-card`)
- Dalt: forma de la categoria (28 dp) i **×** per treure'l (28 dp, àrea tàctil ≥ 48 dp).
- Nom (14 sp, 600, 1 línia amb el·lipsi) i meta «caduca dc. 14 oct» (11 sp).
- Baix: **xip de quantitat** «1 de 25 ⇅» (32 dp, `surface-container-high`) **només si l'ingredient té més d'una unitat**. En tocar-lo es converteix en un stepper `− 2 de 25 +` (botons de 32 dp) que es torna a plegar quan l'usuari toca fora o desplaça la fila. Mínim 1, màxim les unitats que queden.

## Targeta de resultat (148 × 92 dp, radi 20)
- Forma de la categoria (28 dp) + nom (14 sp, 1 línia), meta «12 u. · Plats prep.» i, si en congela, una pastilla `FROZEN` «8 congelats».
- **Plegada** (no seleccionada): targeta de 148 × 92 dp. Sempre és vàlida: un resultat incomplet no es pot plegar (vegeu «Vores i colors (estat)…»).
- **Expandida** (seleccionada): vegeu «Formulari del resultat»; un resultat nou entra expandit i la fila fa `animateScrollToItem` perquè quedi centrat.
- Nom buit: «Nou resultat».

## Formulari del resultat (la targeta expandida)
Contenidor de **ample = pantalla − 40 dp** (350 dp al mockup), radi 20, `surface-card`, **contorn de 2 dp `primary`**, dins la mateixa `LazyRow` (ocupa el lloc de la targeta seleccionada). Les altres targetes de la fila queden a banda i banda, de manera que, amb l'expandida centrada i un `contentPadding` de 20 dp i `spacedBy(10.dp)`, en sobresurten ≈10 dp a cada marge.
Fila 1: camp **Nom** (46 dp; el focus verd i el teclat només quan s'acaba d'afegir un resultat buit o a l'accés lliure; **no** s'enfoca per defecte en seleccionar un resultat existent). Fila 2: «Racions» + stepper (1–99). Fila 3: xips **categoria** (desplegable) i **data** (data de l'ingredient que caduca primer per defecte; mai s'allarga sola; text d'ajuda només com a descripció d'accessibilitat del xip). Fila 4: xip discontinu **«Congela'n»** (mostra «Congela'n · N» si N > 0) que, en tocar-lo, es converteix en una fila `FROZEN` amb «Congela'n · N al congelador» i un stepper (0..racions). Sense resum «a la nevera / al congelador».
**Botó «Fet»** (última fila del formulari, a la dreta; botó tonal `primary-container`, 40 dp, icona `check`): replega el formulari. No valida res (un resultat invàlid mostra el punt d'error a la targeta plegada i bloqueja «Desa»). Quan tots són plegats no hi ha cap formulari obert: la fila mostra només targetes de 148 × 92 dp.
Amb **un sol resultat** expandit no es mostra la fila (només el formulari); en tocar «Fet» queda la targeta única a la fila.
**Navegar entre resultats**: tocar la vora fina d'una veïna, o fer scroll horitzontal i tocar-ne una. Com que la vora és petita (< 48 dp), la navegació **ha de ser possible també amb scroll horitzontal** (i amb accions d'accessibilitat «Resultat anterior / següent»). La capçalera «Resultat · M» pot mostrar la posició («Resultat · 3 de 6») – **obert**.

## Estat buit / accés lliure (`TransformaCarrBuit`)
- **Ingredients opcionals**: la capçalera no mostra «+ Afegeix»; en el seu lloc hi ha **una sola fila discontínua de 64 dp «+ Afegeix ingredient»** (en lloc de la targeta gran amb icona i text).
- Sense ingredients: nom, categoria i data obligatoris (regles de `16-transforma.md`, «Accés lliure»).
- **Desa** desactivat fins que el resultat és vàlid.

## Validació i «Desa»
- El text és «Desa · M productes» (M = nombre de resultats, el mateix que usa el snackbar). Amb 0 ingredients el snackbar continua sent «M productes afegits · Desfés».
- Si l'usuari toca «Desa» desactivat no passa res. El «Desa» només s'activa quan tots els resultats són vàlids i cap n'està obert incomplet.

## Molts elements (`TransformaCarrMolts`, `TransformaCarrPlegat`)
- Les files **no creixen en vertical**: l'alçada de la pantalla és la mateixa amb 2 que amb 20 elements.
- Els recomptes de les capçaleres («Ingredients · 9», «Resultat · 6») diuen quants n'hi ha encara que no es vegin.
- A la fila d'ingredients, targetes estretes perquè es vegi parcialment la següent. A la de resultats, les veïnes de l'expandida es veuen com una vora fina als marges.
- Es poden afegir tants resultats i ingredients com calgui; sense límit dur (si calgués un límit de seguretat, 50 per fila, fixat com a constant).

## Accessibilitat
Cada fila és una col·lecció (`collectionInfo`); cada targeta és un botó amb estat seleccionat (resultats) i descripció completa («Formatge cheddar, caduca dimecres 14 d'octubre, 1 de 25»); els botons ×, +, − amb etiquetes («Treu Formatge cheddar», «Una unitat més»…); àrees tàctils ≥ 48 dp; orde de focus: ingredients → resultats → formulari → Desa.

## Esquema de línies (FASE FINAL · OPCIONAL)
Entre les dues files hi ha un petit esquema dibuixat que lliga visualment «aquests ingredients → aquest resultat»:
- Un **node** de 56 dp amb forma de galeta ondulada (8 lòbuls, la mateixa família de formes que les icones de les targetes; `#EFD8C0`, icona `soup_kitchen` de cullerot amb vapor, verd `primary`, ~28 dp) al centre, dins de la fila de la capçalera «Resultat» (centrat entre l'etiqueta i el botó «+ Afegeix»).
- De cada **ingredient visible** surt una línia fina (1,5 dp, traç arrodonit, color sorra) que baixa i conflueix al node.
- Amb un resultat **expandit**: del node surt **una sola línia vertical verda** (`primary`, 2,5 dp) fins a la vora superior de la targeta expandida. Com que sempre queda **centrada**, la línia és sempre vertical i no cal calcular posicions.
- Amb **tots plegats**: del node surt una línia fina color sorra cap a cada **targeta de resultat visible** (centre visible; màxim 2-3), igual que als ingredients.
- Només es dibuixen línies d'ingredients amb **el centre visible** (màxim 2-3); es recalculen mentre es fa scroll (a partir de `LazyListState.layoutInfo.visibleItemsInfo`, amb un `Canvas`/`drawBehind` entre les files) i apareixen/desapareixen amb una animació curta.
- Sense ingredients (accés lliure), la línia d'entrada és puntejada.
- Respecta «reduir animacions».

### Animació d'expandir/replegar (part de la fase del formulari)
En seleccionar un altre resultat (o en tocar «Fet»): l'obert es replega (de l'ample de pantalla a 148 dp, i de l'alçada del formulari a 92 dp) i el nou s'expandeix (148 → ample de pantalla); tot dins la mateixa fila, amb `animateItem`/`animateContentSize` o `animateDpAsState` i `animateScrollToItem` per centrar-lo (~300 ms, corba d'èmfasi de M3). El contingut del formulari fa fosa creuada. Respecta «reduir animacions» (canvi directe).

**IMPORTANT: l'esquema de línies i el node s'implementen al FINAL de la refactorització de la UI i són opcionals.** Si en veiem la implementació massa complicada (sincronitzar posicions de la fila d'ingredients, rendiment, mides de pantalla), **no es fa** i la resta del disseny funciona igual sense línies ni node (el botó «+ Afegeix» i l'etiqueta de «Resultat» queden com a la resta de capçaleres). **L'expansió de la targeta en formulari NO és opcional**: és part del disseny. No bloquegis la resta per les línies.

## Textos
Es mantenen els de `16-transforma.md` tret de:
- **Nota del peu (única)**: «**No és un receptari:** només apunta què has gastat i què n'ha sortit. Els ingredients desapareixen del rebost en desar.» (la part en negreta, destacada).
- Nou: «Ingredients · N», «Resultat · M», «+ Afegeix», «Afegeix ingredient» (fila buida), «Nou resultat», «Desa · M productes» i «Congela'n · N».
- Es treuen: «Toca un resultat per canviar-ne el nom…», «Data de l'ingredient que caduca primer (…)», «N a la nevera · M al congelador» i la targeta gran «Què has cuinat?» de l'estat buit.

## Obert (no ho resolguis tu: constant o `TODO` clar i avisa'm)
1. Si el xip de quantitat dels ingredients ha de plegar-se sol després de X segons o només en tocar fora.
2. Si cal `snap` a la fila (`rememberSnapFlingBehavior`) o scroll lliure.
3. Límit màxim d'ingredients/resultats per fila.
4. Si la vora fina de les veïnes ha de ser tocable o només decorativa (i mostrar la posició «3 de 6»).

## No és un receptari (regla de producte, ha de quedar molt clar)
Transforma **no** és un apartat de receptes: no hi ha receptes, ni suggeriments de què cuinar, ni cerca, ni passos. Només registra què s'ha gastat i què n'ha sortit (amb la seva data). Es diu explícitament a **tres llocs**:
1. **Onboarding** (pas 3 de 5, nou): títol «Has cuinat? Apunta-ho», diagrama ingredients → node → plat, i una targeta «**No és un receptari**» amb la icona `menu_book` ratllada en vermell. Captura `OBTransforma`.
2. **Modal de novetat** per a qui actualitza (vegeu avall). Captura `NovetatTransforma`.
3. **Nota del peu** de la pantalla Transforma (text a «Textos»).
No utilitzis mai a la UI, a la Play Store ni a les release notes els mots «recepta», «cuina amb…» o «idees de plats» per descriure Transforma; fes servir «apunta què has cuinat».

## Onboarding: nou pas «Transforma» (3 de 5)
- Nou pas entre «Avisos» i «Privadesa»; els passos passen a **5** (`Pas X de 5` i la barra de progrés de 5 segments a tots): Benvinguda (sense comptador), Categories 1, Avisos 2, **Transforma 3**, Privadesa 4, Avís important 5. Cal reordenar la navegació («Continua» d'Avisos → Transforma; «Enrere» de Privadesa → Transforma).
- Contingut: subtítol «Transforma treu del rebost els ingredients que has gastat i hi afegeix el que n'ha sortit, amb la seva data.»; diagrama; targeta «No és un receptari» («No hi ha receptes ni suggeriments de què cuinar. Transforma només apunta què has gastat i què n'ha sortit.»); mini botó dividit «+ ⌄» amb «Sempre és al botó + de baix, a la part ⌄.»; botons «Enrere» / «Continua →».
- És **free** i es pot veure sense cap permís. Només informa: no obre la pantalla Transforma.
- Fonts: `OBTransforma.dc.html`; la resta d'`OB*` només canvien el comptador.

## Modal de novetat (usuaris que ja tenien l'app i actualitzen)
Full inferior modal (`ModalBottomSheet`) sobre l'Inici, amb scrim. Capçalera: xip «NOVETAT», títol «Transforma», text «Has cuinat amb coses del rebost? Apunta-ho: els ingredients en surten i el plat que n'ha sortit hi entra, amb la seva data.», diagrama, targeta «No és un receptari», pista «La trobaràs sempre al botó +, a la part ⌄.», CTA primari «Prova Transforma» i secundari «Ara no». Captura `NovetatTransforma`.
Regles de visualització (proposta; `TODO` clar si cal canviar-les):
- Es mostra **una sola vegada**, a la primera obertura després d'actualitzar, i **només** si l'usuari ja havia completat l'onboarding (guarda un flag, p. ex. `transformIntroSeen` a DataStore).
- Instal·lacions noves: veuen el pas de l'onboarding i el flag es marca com a vist en acabar-lo; no veuen la modal.
- No es mostra durant l'onboarding, ni en arribar per un enllaç profund/widget/notificació (s'ajorna a la següent obertura normal), ni amb diàleg o selecció oberts.
- «Prova Transforma» obre Transforma en accés lliure (`TransformaCarrBuit`); «Ara no» i tocar fora la tanquen. En els dos casos es marca com a vista. L'entrada del botó dividit «+ ⌄» continua disponible sempre.
- Accessible: focus a la capçalera, el botó «Ara no» té la mateixa mida tàctil que el primari, anunci del títol; respecta el tema (clar/fosc).
- No són dibuixades les versions fosc/paletes de les dues pantalles noves: usa els tokens habituals.
- Obert: si es vol un subtítol a l'entrada del menú «Transforma» («Apunta què has cuinat»).


## Vores i colors (estat) i línies que s'esvaeixen
Aquest apartat **té prioritat** sobre els colors de línia/vora que s'hagin descrit abans en aquest document (línia tan única, vores sense color).
**Vora de la targeta d'ingredient (2 dp, sobre el farciment `#F7EEDA`), segons l'estat:**
- Caduca aviat (en marge d'«Aviat»): ambre `#D9A021`.
- Congelat: blau `#4F95AD`. A la targeta, el subtítol diu «congelat».
- Resta (amb marge): verd `#7E9F4A`.
Usa els tokens de color d'estat de l'app, no aquests hex literals. Verifica que cada vora tingui contrast ≥ 3:1 contra el fons `#FFFAEE` (la groga i la verda són les justes; si no arriben, enfosqueix-les per a la vora).
**Línies ingredient → node:** cada línia (2 dp) té el color de la vora de la seva targeta, així es veu quin ingredient entra al node.
**Línies node → resultat i vora de la targeta de resultat:** to tan `#8F8266` (2 dp) amb **opacitat decreixent segons la distància horitzontal al centre de la pantalla**: `alpha = max(0.3, 1 − |x − centre| / 260 dp)`. La vora de la targeta de resultat segueix la mateixa alpha que la seva línia, de manera que el que és més lluny es veu més apagat. La línia vertical verda cap al formulari expandit no s'esvaeix mai.
**No existeix l'estat «resultat invàlid».** Un resultat incomplet no es pot plegar: el botó **«Fet» queda desactivat** (tonal apagat, `#F0E2CB` / text `#8F8266`, `enabled = false` i `disabled` a l'accessibilitat) mentre falti el nom o la categoria, i no s'hi pot passar a un altre resultat mentre sigui així (tocar-ne un altre, o la vora fina d'un veí, **no fa res**; opcionalment mostra el camp que falta amb l'error del camp habitual). Així, totes les targetes plegades són sempre vàlides i les seves vores/línies mai no són vermelles. El «Desa» queda desactivat mentre hi hagi un resultat obert incomplet. Captura `TransformaCarrBuit` (Fet apagat).
Les targetes seleccionades (expandides) mantenen la vora verda `primary` de 2 dp.

**Node (actualitzat):** el node és una galeta ondulada de 56 dp amb la icona `soup_kitchen` (substitueix el cercle de 40 dp amb `skillet` descrit més amunt). Sobresurt uns 8 dp per sobre de la fila de la capçalera «Resultat» i tapa l'extrem de les línies. Mateix node al diagrama de l'onboarding i de la modal de novetat.


## Esborrar un resultat
Es pot esborrar un resultat de dues maneres (mateixa acció):
- **Targeta plegada**: icona `close` a dalt a la dreta, igual que la dels ingredients (àrea tàctil ≥ 48 dp encara que la icona en faci 18; descripció «Esborra el resultat <nom>»).
- **Formulari expandit**: botó d'icona `delete` (40 dp visibles, ≥ 48 dp d'àrea) a baix a l'esquerra, a la mateixa fila que «Fet» (que va a la dreta). Captures `TransformaCarrMolts` i `TransformaCarrBuit`.
Comportament (proposta; `TODO` si cal canviar-ho):
1. **Sense diàleg de confirmació**; en lloc d'això, *snackbar* «Resultat esborrat» amb acció **«Desfés»** (restaura el resultat i la seva posició/selecció).
2. Si n'hi ha més d'un: en esborrar el que està obert, passen a l'estat **plegat** (tots els que queden són vàlids, vegeu més amunt) i la fila fa scroll al veí següent (o l'anterior si era l'últim). Si només en queda un, s'obre el seu formulari directament (sense fila, com sempre amb un sol resultat).
3. Si és el **resultat únic**: no desapareix la secció; el formulari es **buida** (nom, caducitat, categoria, «Congela'n» als valors per defecte) i el «Fet» queda desactivat. Un cop buit, el «Desa» queda desactivat.
4. Les línies i el node es recalculen; l'esborrat s'anima (la targeta es contreu i les veïnes s'hi acosten, `animateItem`; amb «reduir animacions», canvi directe). Anunci d'accessibilitat: «Resultat esborrat. N resultats».
5. L'esborrat només treu el resultat de la tanda; no toca l'estoc ni els ingredients.
