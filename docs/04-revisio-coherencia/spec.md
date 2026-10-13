# 20 · Revisió de coherència (13 d'octubre)

Aquest document recull les decisions de la revisió de tota l'app. **Té prioritat** sobre qualsevol altra especificació anterior que la contradigui (es diu quina a cada apartat). Captures: `02-disseny-nou/captures/`; fonts: `02-disseny-nou/font-dc-html/`.

---

## 0. Abast: només mòbil

Fora de l'abast **de moment** (no s'implementa; si ja hi ha codi, **no l'esborris sense preguntar**: proposa amagar-ho o deixar-ho darrere d'un flag):

- Tauleta, plegable (mode llibre) i Wear OS.
- Widget i dreceres de la pantalla d'inici. També el consell «Posa el widget…» de l'Inici buit.
- Temes de temporada (Halloween, Nadal) i l'opció d'Ajustos «Temes de temporada».
- Captures d'exemple de cada paleta: la informació de paletes viu només al design system. **El selector de colors d'Ajustos es queda.**
- Petició de valoració a Google Play (moment i normes).
- Mode «Afegeix en tanda» i el botó «Desa i un altre».
- Idees de futur (model PRO, sincronització, rebost compartit).

El **mode fosc** es manté.

---

## 1. Durada habitual per categoria (substitueix el «marge d'avís per categoria»)

Substitueix l'especificació del marge per categoria (paquet `PROMPT-CANVI-MARGE-CATEGORIA`).

- **«Aviat» és només global**: Ajustos › Avisos › «Aviat» vol dir (1 dia / 3 dies / 1 setmana). Subtítol: «Quants dies abans de caducar passa a groc i t'avisem». La pantalla Aviat diu «pròxims {N} dies» amb aquest valor.
- **Cada categoria té una «Durada habitual»**: quant triga a fer-se malbé aquell tipus d'aliment. Serveix **només** per suggerir la data quan afegeixes un producte.
  - Valors suggerits: Carn i peix 3 dies · Fleca 3 dies · Làctics 1 setmana · Fruita i verdura 1 setmana · Begudes 1 mes · Rebost 3 mesos. Una categoria pot no tenir-ne («Sense durada · la data la poses tu»).
  - Selector: el mateix control esglaonat d'abans (1–3 dies, 1–2 setmanes, 1–3 mesos).
  - Accés: Ajustos › secció «Productes» › «Durada habitual» (captura `MargeCategories`).
- **Afegeix**: en triar categoria, la data surt prefilada amb avui + durada, el xip ràpid corresponent queda marcat («+1 setmana») i a sota: «Suggerida: els Làctics duren 1 setmana». L'usuari la pot canviar. Sense durada → data buida.
- **Model / migració**: el camp actual de «dies d'avís» per categoria passa a ser «dies de durada» (nullable). Els valors antics **no** tenen el mateix significat: la migració posa els valors suggerits per a les categories per defecte i `null` per a les creades per l'usuari. Tots els productes passen a usar l'«Aviat» global. **Avisa'm abans d'escriure la migració.**
- Desapareix l'etiqueta de «marge propi» i l'artboard «Inici · Més endavant».

## 2. Quantitat = només unitats

No hi ha grams, bricks, bosses, safates… La quantitat és el **nombre d'elements**: «1 unitat», «N unitats». Revisa textos i dades de prova.

## 3. Una sola fila de producte (a totes les pantalles)

Inici, Aviat, Categoria, Caducats, calendari de mes i les llistes de fons de les modals fan servir **el mateix component**:

- 64 dp d'alt mínim, fons `surfaceContainer`, arrodoniment segons posició dins el grup (24/6 dp), 2 dp entre files.
- **Línia 1**: nom (16 sp, 600, una línia amb «…»).
- **Línia 2**: sempre comença per la **quantitat**; després, el que **no** digui ja la pantalla:
  - llista agrupada per dia (Inici, calendari) → `1 unitat · Làctics` (gris normal);
  - llista agrupada per categoria o estat (Aviat, Categoria, Caducats) → `2 unitats · D'aquí 2 dies` en **negreta del color de l'estat** (caducat vermell, aviat ocre, fresc verd, congelat blau).
  - Des de Categoria, el calendari omet la categoria.
- **Icona de categoria** a l'esquerra només si la llista barreja categories.
- **Accions rodones** (44 dp) a la dreta: vigent → ✓ i ❄; congelat → només ✓; caducat → només paperera («Llença»).
- **Tocar la fila obre la modal del producte**, a totes les pantalles.
- **Mantenir premuda la fila** entra en mode selecció (per a Transforma).
- Desapareixen: la pastilla de dies a la dreta, el desplegable «Unitat 1, Unitat 2…» de Categoria i la pulsació llarga a ✓.

## 4. Accions de producte

Secció «Accions de producte» del canvas.

- **Botons rodons**: ✓, ❄ i la paperera actuen sempre sobre **una unitat, la que caduca primer**. Després, *snackbar* «1 iogurt natural consumit · Desfés» (sense «Més…»).
- **Modal (bottom sheet) en tocar la fila**:
  - **1 unitat** (`Accions`): capçalera, «Consumit» / «Congela», «Canvia la data» (+1 dia, +3 dies, +1 setmana, calendari), Edita, Mou, Transforma aquest producte, **Llença**. Tot desplegat.
  - **Diverses unitats** (`QuantitatAccions`): capçalera «4 unitats · la primera, dt. 13 oct», selector − / «2 de 4» / + («Primer les que caduquen abans»), **«Consumeix N» / «Congela N»** i **«Més accions»** plegat (s'aplica a tot el producte). No hi ha cap diàleg de quantitat.
  - **Caducat, 1 unitat** (`AccionsCaducat`): avís vermell «Ha caducat. Per seguretat, llença'l.», un sol botó gran **«Llença»** i «Més accions» (plegat per defecte; a la captura es veu obert) amb:
    - **«Consumit igualment · sota la teva responsabilitat»**: cal **mantenir premut 1,5 s**; la fila s'omple d'esquerra a dreta; si deixes anar abans, es buida; vibració en completar-se. Amb TalkBack: acció personalitzada amb diàleg de confirmació.
    - «La data era incorrecta» (corregir un error d'entrada).
    - «Edita».
    - Mai Congela, Transforma ni Mou.
  - **Caducat, diverses unitats** (`QuantitatCaducat`): avís, selector (per defecte, el nombre d'unitats caducades), **«Llença N»** i «Més accions» plegat (mateix contingut).

## 5. «Llença»: una sola paraula

- «Esborra» desapareix de tota l'app: és **«Llença»**, amb icona de paperera (`delete`).
- **Caducats**: subtítol «Han caducat. Per seguretat, llença'ls.»; cada fila només té la paperera (sense «Canvia la data»); **«Llença'ls tots»** continua sent el botó principal, però demana confirmació («Llençar 7 productes a les escombraries?» → «Llença'ls» / «Cancel·la») i després *snackbar* «7 productes llençats · Desfés».

## 6. Navegació

- **Barra de navegació flotant** (Aviat · Rebost · Ajustos; la secció activa amb text): es manté. Semàntica de pestanyes, no s'amaga en fer scroll. Es pot construir amb `HorizontalFloatingToolbar` de M3 Expressive amb el FAB al costat.
- **Un sol «+» (FAB)** al costat: afegeix producte amb un toc. **Desapareix el botó dividit «+ ⌄»** i el seu menú.
- **Categoria**: el mateix FAB «+» (ja no «Afegeix» estès), amb `contentDescription` «Afegeix a {categoria}» i la categoria preseleccionada.

## 7. Transforma: entrades i nom

- **Entrades** (no hi ha botó general):
  1. **Selecció**: mantén premut un producte → mode selecció → «Transforma».
  2. **Modal del producte**: «Transforma aquest producte».
  3. **Fila al final de la llista d'Aviat (Inici)**: «Has cuinat? · Apunta-ho a Transforma» amb icona `soup_kitchen` i `chevron_right`. Obre directament `TransformScreen` buida (estat «mínim 1 ingredient»). **No es pot amagar.** No surt si no hi ha cap producte vigent.
- **Onboarding (pas 3) i modal de novetat**: la pista passa a «La tens al final de la llista d'Aviat, o mantén premut un producte.» (ja no «botó + ⌄»).
- **Nom**: ca «Transforma» (nom i botó); es nom «Transforma», botó «Transformar» (si els botons en castellà van en infinitiu; si van en imperatiu, «Transforma» a tot arreu); en «Transform».

## 8. Altres canvis de pantalla

- **Afegeix** i **Afegeix · producte nou**: un sol botó «Desa» a tota l'amplada (desactivat mentre faltin categoria o data).
- **Ajustos**: sense «Temes de temporada»; nova secció «Productes» › «Durada habitual».
- **Inici buit**: sense el consell del widget.

---

## Obert / futur (no ho facis)

- **Tipus de data per producte**: «Caduca» vs «Consum preferent», amb valor per defecte per categoria i estat «Data passada» (ocre, «Comprova'n l'aspecte i l'olor», Consumit / Llença) per als de consum preferent. Fase pròpia més endavant.
- Fila de Transforma compacta després d'un temps d'ús.
- Temes menors pendents: coherència de noms, el triple sentit de «Rebost» (app, pestanya, categoria), detalls de M3 Expressive, incoherències de les dades d'exemple.
