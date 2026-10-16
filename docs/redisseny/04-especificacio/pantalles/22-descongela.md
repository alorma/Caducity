# 22 · Descongela

Faltava a l'app: un producte congelat no es podia descongelar. Complementa `20-revisio-coherencia.md` (apartats 3, 4 i 10), que té prioritat on es contradiguin.

Captures: `AccionsCongelat`, `QuantitatCongelat`, `EstatCongelats`.

## 1. Botó rodó a la fila (fila única de producte)

- Fila de producte **congelat**: **✓ «Consumeix»** + **☀ «Descongela»** (icona `sunny`, mateix color que el ❄: `#3F7F92` / el blau de l'estat Congelat). Ocupa **el mateix lloc que el ❄** de la fila normal: la fila té sempre dos botons rodons.
- Actua sobre **una unitat**: la que fa més temps que és congelada.
- Després: snackbar «1 croqueta descongelada · Desfés».
- On es veu: pantalla **Congelats**, secció «Congelat» de **Categoria**, i qualsevol llista que mostri la fila d'un congelat.
- TalkBack: `contentDescription` «Descongela una croqueta».
- Substitueix la regla anterior «Congelats: files només amb ✓».

## 2. Segona línia de la fila

«{N} unitats · Congelat el {data}» (p. ex. «8 unitats · Congelat el 4 oct»). Ha de mostrar **la data de congelació**, no només «Congelat»: és com se sap quin fa més temps que hi és.

## 3. Modal del producte congelat (tocar la fila)

### 1 unitat (`AccionsCongelat`)
- Capçalera: forma i icona de la categoria, nom, «1 unitat · congelat el 28 set», xip **«Congelat»** (colors de l'estat).
- Botons grans (M, 56 dp, en grup connectat): **«Consumeix»** (primari) + **«Descongela»** (container de Congelat, icona `sunny`).
- Nota informativa: «En descongelar, el producte passa a Aviat i caduca d'aquí 3 dies.»
- Llista: **Edita**, **Mou a una altra categoria**, **Llença**.
- **No** hi ha «Canvia la data» (un congelat no té data de caducitat) ni «Transforma aquest producte».

### Diverses unitats (`QuantitatCongelat`)
- Capçalera: «8 unitats · congelades el 4 oct».
- Selector − / «2 de 8» / +, amb el text «Primer les congelades fa més temps».
- Botons: **«Consumeix N»** + **«Descongela N»**.
- «Més accions» plegat (Edita, Mou, Llença; s'apliquen a tot el producte).

## 4. Què passa en descongelar (model)

- Les unitats descongelades deixen de ser congelades i reben **data de caducitat = avui + 3 dies** → entren a **Aviat**.
- Els 3 dies són un valor fix per ara (proposta; es pot fer configurable més endavant o per categoria).
- Si es descongelen totes les unitats, el producte deixa de sortir a Congelats.
- Desfés: torna les unitats a congelades amb la seva data de congelació original.

## 5. Textos (strings.xml)

| clau | ca | es | en |
|---|---|---|---|
| action_unfreeze | Descongela | Descongelar | Defrost |
| action_unfreeze_n | Descongela %d | Descongelar %d | Defrost %d |
| unfreeze_hint | En descongelar, el producte passa a Aviat i caduca d'aquí 3 dies. | Al descongelarlo, el producto pasa a Pronto y caduca en 3 días. | When defrosted, it moves to Soon and expires in 3 days. |
| unfreeze_order | Primer les congelades fa més temps | Primero las congeladas hace más tiempo | Oldest frozen first |
| snack_unfrozen | %1$d %2$s descongelat | %1$d %2$s descongelado | %1$d %2$s defrosted |
| frozen_on | Congelat el %s | Congelado el %s | Frozen on %s |

(Plurals i gènere de la snackbar: segueix la regla de textos de l'apartat 9 de `20-revisio-coherencia.md`.)
