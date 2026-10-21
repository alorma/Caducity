# 23 · Tipus d'unitat (com es compta un producte)

Captures a `02-disseny-nou/captures/`: `TipusAfegir`, `TipusTria`, `TipusFiles`, `TipusGrams`, `TipusAUll`.
Aquesta especificació cobreix **tot** el disseny; es fa per fases (vegeu el prompt). Fora d'abast: tipus personalitzats (aparcat a 20-revisio-coherencia.md, «Obert / futur»).

## 1. Model

Cada producte té un **tipus d'unitat** de llista **prefixada** (no editable). Tres **tipus pare**, i els **subtipus** només canvien el nom (singular/plural) i la icona:

| Pare | Subtipus | Regla |
|---|---|---|
| **Unitats** (per defecte) | Unitats, Tuppers, Paquets, Llaunes | Es compta d'un en un. Tot funciona com ara. |
| **Mesurable** | Grams, Mil·lilitres | Un sol lot amb una quantitat (500 g). Passos: 50 g / 100 mL. |
| **Grapats** | — | Nivell: Molt › La meitat › Poc. |

(«Ampolles» no hi és: una ampolla és un envàs d'un líquid, no un tipus de compte.)

- Producte existent → **Unitats** (migració sense canvis visibles).
- Mesurable: la quantitat és un enter (g o mL) d'un únic lot amb una sola data de caducitat. Si arriba a 0, el producte desapareix.
- Grapats: enum de 3 nivells (Molt, La meitat, Poc). Quan es gasta Poc, el producte es consumeix.
- Un producte amb tipus Mesurable o Grapats **no** es pot fusionar ni té «unitats» (el selector «N de M» no apareix).
- **Edita** permet canviar el tipus; avisa «Canviar el tipus reinicia la quantitat».
- La **Durada habitual** per categoria no canvia.

## 2. Afegeix i Edita (`TipusAfegir`, `TipusTria`)

- Nova fila **«Com ho comptes?»** a sobre de «Quantitat», amb el tipus actual a la dreta i xevró. Obre la modal `TipusTria`.
- `TipusTria`: bottom sheet «Com ho comptes?» (subtítol «Primer el tipus, després com es diu.»). A dalt, tres pestanyes grans: **Unitats / Mesurable / Grapats**, amb una línia que explica la regla («D'una en una · el ✓ en gasta una»). A sota, «Com es diu»: els subtítols del pare triat (selecció única; fons `#DCE9C6` i ✓). Grapats no té subtipus (no es mostra la llista). Es tanca en triar el subtipus. Resum a Afegeix: «Com ho comptes? · Tuppers».
- La fila «Quantitat» s'adapta:
  - Unitats: − N + com ara.
  - Mesurable: − 500 g + (pas 50 g / 100 mL) i xips de drecera (250 g · 500 g · 1 kg; 250 mL · 500 mL · 1 L).
  - Grapats: etiqueta «Queda» + control segmentat Molt / Meitat / Poc.
- El botó «Congelat» de la fila segueix igual (congela el que s'afegeix).

## 2.1 Es pot congelar (categoria + producte)

No té sentit congelar farina o macarrons secs. Nova opció **«Es pot congelar»**:
- **Categoria** (Nova categoria / Edita): interruptor a la secció «Congelació» (captura `NovaCategoria`). Per defecte, activat.
- **Producte** (Afegeix / Edita): interruptor «Es pot congelar» a sota de Quantitat (captura `TipusAfegir`), amb el valor heretat de la categoria; si l'usuari el canvia, queda com a **sobreescriptura** del producte (`canFreeze: Boolean?`; `null` = hereta). Valor efectiu = producte ?? categoria.
- Si no es pot congelar: **no hi ha ❄** a la fila, ni «Congela» a les modals (el botó «Consumeix» ocupa tot l'ample), ni el botó «Congelat» a Afegeix, i a Transforma no es poden enviar resultats al congelador.
- Els productes **ja congelats** no canvien: continuen congelats i ☀ «Descongela» segueix funcionant.
- Migració: tot a «es pot congelar» (cap canvi). Les categories per defecte **no** es toquen; l'usuari decideix (p. ex. Secs i conserves → no). Captura `TipusFiles`: Espaguetis, Llenties i Tonyina sense ❄.

## 3. Fila de producte (`TipusFiles`)

Segona línia, sempre `{quantitat} · {quan}`:
- Unitats: «3 paquets», «1 tupper», «4 unitats» (singular/plural per tipus).
- Mesurable: «500 g», «750 mL» (si ≥ 1000: «1,2 kg», «1,5 L»).
- Grapats: «Queda molt» / «Queda la meitat» / «Queda poc».
Colors i negreta segons les regles de 20-revisio-coherencia.md (§3).

**Accions ràpides de la fila:**

| Tipus | ✓ | ❄ |
|---|---|---|
| Unitats | Consumeix **1** (com ara) · snackbar «1 iogurt consumit · Desfés» | Congela **1** (com ara) |
| Mesurable | Obre la modal `TipusGrams` (no es pot saber quant) | Obre la mateixa modal |
| Grapats | **Baixa un nivell** directament: Molt → La meitat → Poc → consumit. Snackbar «Cacauets: ara en queda poc · Desfés» (a l'últim: «Cacauets consumits · Desfés») | Congela **tot** el producte, amb el nivell actual |

Tocar la fila obre sempre la modal d'accions del tipus.

## 3.1 Congelat i caducat

- **Congelat**: ✓ i ☀ segueixen la regla del tipus (Unitats: una unitat; Mesurable: modal amb «Consumeix N» / «Descongela N»; Grapats: ✓ baixa un nivell, ☀ descongela tot). En descongelar → Aviat amb **avui + 3 dies**; Desfés restaura.
- **Caducat**: per a Mesurable i Grapats només hi ha **«Llença»** (tot el producte), sense selector.

## 4. Modal Mesurable (`TipusGrams`)

Mateix esquelet que `QuantitatAccions`:
- Capçalera: icona de categoria, nom, «Queden 500 g · caduca el 12 de gener».
- Títol de secció «Quant n'has fet servir?».
- Selector − / valor gran amb unitat (`100 g`) / + (pas 50 g / 100 mL). Per defecte, 100 g (100 mL).
- Xips de drecera: 50 g · 100 g · 250 g · **Tot** (mL: 100 · 250 · 500 · Tot). «Tot» posa la quantitat restant.
- Barra de progrés: verd el que queda, ratllat ocre el que se'n va; text «en quedaran 400 g».
- Botons: **«Consumeix 100 g»** (principal) i **«Congela»** (congela la quantitat triada: crea una fila congelada amb aquests grams). «Més accions» plegat (mateix contingut que a la modal d'una unitat, sense Transforma).
- Si la quantitat triada és igual al total, el producte desapareix (consumit) o passa sencer a congelat.
- Snackbar «100 g d'espaguetis consumits · Desfés».

## 5. Modal Grapats (`TipusAUll`)

- Capçalera: «Queda la meitat · caduca el 12 de gener».
- Títol «Quant en queda?» i control de 3 botons grans (Molt / La meitat / Poc, icones `battery_full` / `battery_4_bar` / `battery_1_bar`). Tocar un nivell **el canvia a l'instant** (sense confirmar). Nota: «Toca el nivell per canviar-lo. ✓ a la llista baixa un nivell.»
- Botons: **«Ja no en queda»** (consumit) i **«Congela»** (tot el producte). «Més accions» plegat.

## 6. Transforma (fase 1)

Un ingredient de tipus Mesurable o Grapats es consumeix **sencer**. Els resultats sempre són en unitats. Es podria afinar més endavant (demanar quant).

## 7. Textos (ca / es / en)

Com ho comptes? / ¿Cómo lo cuentas? / How do you count it? · Unitats / Entero / Whole · Mesurable / Por cantidad / By amount · Grapats / A ojo / By eye · Unitats, Tuppers, Paquets, Llaunes, Grams, Mil·lilitres, Grapats (Units, Containers, Packs, Cans, Grams, Millilitres, Handfuls) · Mesurable (Measured) · Com es diu (What to call it) · Es pot congelar (Can be frozen) · Congelació (Freezing) · Molt / La meitat / Poc (A lot / Half / Little) · Quant n'has fet servir? · Quant en queda? · Ja no en queda · Canviar el tipus reinicia la quantitat.
