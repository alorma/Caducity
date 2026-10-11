# Marge d'«Aviat» per categoria

Captures: `MargeAjustos.png`, `MargeCategories.png`, `MargeNovaCategoria.png`, `MargeInici.png`, `MargeAviat.png`. Fonts: `Marge*.dc.html`.

## Per què
Un llindar únic de 3 dies serveix per a làctics o peix, però no per a secs (pasta, arròs, llegums): 3 dies abans és massa tard per recordar-se'n. Cada categoria té ara el seu **marge**: quants dies abans de caducar un producte passa a `SOON` («Aviat») i entra als avisos.

## Regla
```
marge(item) = item.category.soonThresholdDays ?: prefs.soonThresholdDays   // global = «per defecte»
SOON  si avui <= expiryDate <= avui + marge(item)
```
La resta d'estats no canvia (`FROZEN`, `EXPIRED`, `FRESH`). Tot el que compta o filtra per `SOON` (Inici, resum, pantalla Aviat, tira de 7 dies, calendari, widget, notificació) fa servir `marge(item)`.

## Valors possibles (parades)
Sempre es desa en **dies**. La UI només ofereix aquestes 8 parades:

| Parada | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|
| Etiqueta | 1 dia | 2 dies | 3 dies | 1 setmana | 2 setmanes | 1 mes | 2 mesos | 3 mesos |
| Dies | 1 | 2 | 3 | 7 | 14 | 30 | 60 | 90 |

Si arriba un valor que no és una parada (p. ex. d'una còpia de seguretat), el slider se situa a la parada més propera però es conserva el valor.

## Valors suggerits (categories de l'onboarding)

| Categoria | Marge |
|---|---|
| Carn i peix | 2 dies |
| Fleca | 2 dies |
| Làctics | 3 dies |
| Fruita i verdura | 3 dies |
| Embotits | 3 dies |
| Plats preparats | 2 dies |
| Begudes | 1 setmana |
| Rebost | 1 mes |

Les categories creades per l'usuari comencen **sense marge propi** (`null`) → fan servir el per defecte, fins que l'usuari en triï un a Nova categoria.

## Component: `ThresholdSlider` (slider de parades)
M3 Expressive *slider* discret (`Slider` amb `steps = 6`, valor 0..7 mapejat a la taula).
- **Valor** a dalt: etiqueta en Bricolage 28/800 («1 setmana») i a la dreta «7 dies abans de caducar» (`ink-variant` 13).
- **Pista** gruixuda de 16 dp: activa `primary`, inactiva `primary-container`, separació de 6 dp a cada costat del mànec. **Mànec** vertical 4×44 dp `primary`.
- **Punts de parada** de 4 dp sobre la pista (`primary-container` sobre l'activa, `primary` sobre la inactiva). El de la posició actual queda amagat pel mànec.
- Sota la pista: el **número** de cada parada (1 2 3 · 1 2 · 1 2 3; la seleccionada en 800 `ink`) i una **clau** sota cada grup amb «dies», «setmanes», «mesos».
- Es pot lliscar o tocar un punt. Retorn hàptic suau a cada parada.
- Accessibilitat: `stateDescription` = etiqueta («1 setmana»); les accions d'augmentar/disminuir salten de parada.

## Pantalles

### Ajustos (`MargeAjustos.png`) — canvia `08-ajustos.md`
- La fila «"Aviat" vol dir» passa a **«"Aviat" per defecte»** · «Per a les categories sense marge propi». Mateix grup connectat 1 dia / 3 dies / 1 setmana (pref. global `soonThresholdDays`).
- Fila nova a sota (última de la llista connectada): `tune` · **Marge per categoria** · resum d'una línia amb el·lipsi («Làctics 3 dies · Rebost 1 mes · Begudes 1 setmana…») · `chevron_right` → pantalla nova.

### Marge per categoria (`MargeCategories.png`) — pantalla nova
- `arrow_back` · títol `screen-title` «Marge per categoria» · «Quants dies abans de caducar un producte passa a «Aviat» i t'avisem.»
- Llista connectada de **totes les categories**, ordenades pel marge (curt → llarg) i després per nom. Fila: forma 40 + nom + «Avisa N abans» + pastilla amb el valor (`surface`).
  - Sense marge propi: subtítol «Per defecte (Ajustos)» i pastilla transparent amb el valor global en `ink-variant`.
- **Tocar una fila la desplega** (només una oberta alhora): fons `surface-high`, forma en `primary-container`, pastilla `primary`; a dins el `ThresholdSlider` i una línia `lightbulb` «Suggerit per a … : X» (només si la categoria és de les suggerides i el valor és diferent del suggerit, o sempre si es prefereix). El canvi es desa a l'acte.
- Al final: botó de text `restart_alt` **«Torna als valors suggerits»** → diàleg de confirmació; restaura la taula de suggerits i posa `null` a les categories pròpies.

### Nova / edita categoria (`MargeNovaCategoria.png`) — canvia `07-nova-categoria.md`
- Secció nova **«Avisa'm abans»** entre Icona i Descripció: targeta `surface-card` radi 20 amb el `ThresholdSlider` i l'ajuda «Llisca o toca un punt. Les begudes passaran a «Aviat» una setmana abans.» (amb el nom en viu).
- Valor inicial: el per defecte (global). Si l'usuari no el toca, es desa `null` (segueix el per defecte); si el toca, es desa el valor.
- Per fer-hi lloc, la previsualització passa a 96 dp (icona 42) i el gap entre seccions a 16.

### Inici (`MargeInici.png`) — canvia `01-inici.md`
- Les seccions per dia arriben **fins a avui + 6** (la mateixa finestra que la tira de 7 dies).
- Els `SOON` més enllà s'agrupen en una secció **«Més endavant»** (`section-title`) + «N productes», amb **una targeta plegada** per defecte: forma `event_upcoming` + noms («Cigrons cuits i Macarrons», o «Cigrons cuits, Macarrons i 3 més») + interval «Del 23 d'oct. al 4 de nov.» + botó rodó `expand_more`. Desplegada: llista connectada ordenada per data, files com les de l'Inici amb la data a la meta («Rebost · 1 paquet · dv. 23 oct»). L'estat plegat/desplegat es recorda durant la sessió.
- Ajuda a sota: `info` + «Ja són «Aviat» pel marge de la seva categoria (Rebost, 1 mes).» (si hi ha diverses categories: «…pel marge de la seva categoria.»).
- La casella **Aviat** del resum compta tots els `SOON` (també els de «Més endavant»).

### Aviat (`MargeAviat.png`) — canvia `13-pantalles-estat.md`
- Frase d'ajuda: **«Segons el marge de cada categoria. Fes-los servir primer.»** (substitueix «Caduquen en els pròxims N dies»).
- Capçalera de cada grup: a la dreta, pastilla `soon-container` / `on-soon` (28 dp, radi 14) amb `notifications` + **«Avisa N abans»**.

### Onboarding — canvia `10-onboarding.md`
- Pas Categories: les categories suggerides es creen amb el seu marge (taula de dalt). **No es mostra** en aquella pantalla.
- Pas Avisos: el selector de llindar passa a ser el **per defecte**; ajuda «Cada categoria ja porta el seu marge. Aquest és per a les que creïs tu.»

## Notificacions — canvia `04-model-de-dades.md`
Continua sent **una sola notificació diària**. Per no repetir durant setmanes els productes amb marges llargs, només hi entren:
1. els productes que **passen a `SOON` avui** (primer dia dins el marge), i
2. els que **caduquen avui o demà**.
Títol «N productes caduquen aviat»; text amb els 2–3 més urgents.

## Dades i migració
- `CategoryEntity.soonThresholdDays: Int?` (nullable, per defecte `null`). Migració de Room: afegir la columna i omplir les categories suggerides existents (per nom/clau de plantilla) amb el valor de la taula. Si la migració v2→v3 del pla encara no s'ha publicat, fer-ho dins la mateixa; si no, v3→v4.
- `ExpiryStatusCalculator(item, category, globalThreshold, today)` substitueix el càlcul que només rebia el llindar global.
- Còpia de seguretat: exportar/importar el camp nou (els fitxers antics sense camp → `null`).

## Tests
- `ExpiryStatusCalculatorTest`: marge propi vs per defecte; límits (avui + marge inclòs, +1 exclòs); canvi del global no afecta categories amb marge propi.
- `ThresholdStopsTest`: mapeig parada ↔ dies; valor fora de parades → parada més propera sense perdre el valor.
- `HomeSectionsTest`: seccions fins a avui+6 i «Més endavant» amb la resta de `SOON`; recompte de la casella Aviat.
- `DailyNotificationTest`: només entra el primer dia a `SOON` + avui/demà; no es repeteix un producte amb marge d'1 mes.
- `MigrationTest`: columna nova, suggerides omplertes, pròpies a `null`.

## Obert
- **Valor exacte personalitzat** (p. ex. 10 dies): no ara. Idea: tocar el valor gran obre un camp «Altre» en dies; el mànec es posa a la parada més propera i es mostra «10 dies (personalitzat)».
