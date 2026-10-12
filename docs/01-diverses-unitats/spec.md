# Consumir i congelar diverses unitats

Captures: `MultiToc.png`, `MultiConsumeix.png`, `MultiCongela.png`. Fonts: els `.dc.html` homònims. Reutilitza el diàleg de `15-dialeg-quantitat.md`.

## Problema
Un producte pot tenir diversos ítems (p. ex. «Iogurt natural · 4 unitats»). Calia poder consumir o congelar-ne 2 o 3 de cop **sense trencar el flux d'un sol toc**.

## Regles
- **Toc** a ✓ (consumit) o ❄ (congela) a una fila amb diverses unitats: actua sobre **1 unitat** (la que caduca primer). La fila passa a «3 unitats».
- **Snackbar** (fosc, 56 dp): «1 iogurt consumit» · **Més…** · **Desfés** (4 s). «Desfés» restaura la unitat. «Més…» obre el diàleg per fer-ne més (vegeu avall) i substitueix el temporitzador.
- **Pulsació llarga** a ✓ o ❄: obre directament el diàleg amb el valor inicial **1**, sense gastar cap unitat abans.
- En ítems d'una sola unitat res no canvia (ni «Més…» ni diàleg).
- Mateixa lògica a Rebost, Categoria, Inici i les pantalles d'estat. A les files congelades només ✓.

## Diàleg
Mateix component que el diàleg de quantitat (selector connectat `− N de M +`, radis 24/6, 64 dp, botons Cancel·la i principal), amb dues variants:
| | Consumeix (`MultiConsumeix`) | Congela (`MultiCongela`) |
|---|---|---|
| Overline | «4 unitats · caduca dl. 20 oct» | «1 congelat · queden 3» (només des de «Més…») |
| Pregunta | «Quantes en consumeixes?» | «Quantes en congeles?» / «Quantes més en congeles?» |
| Selector | `primary-container` | `frozen-container` #D3E5EA / `on` #163F4C |
| Nota | «Les 2 que quedin segueixen al rebost.» | «S'afegeixen als que ja són al congelador.» |
| Botó | «Consumeix 2» (`primary`) | «Congela'n 1» (`frozen-container`) |

- **Des de «Més…»** el màxim M són les unitats que queden (després del toc) i la pregunta diu «més». Un sol «Desfés» posterior restaura **tot el gest** (la unitat del toc + les afegides) com una sola acció.
- **Des de pulsació llarga** M = totes les unitats, la pregunta no diu «més».
- Només s'actua en prémer el botó principal; Cancel·la no canvia res (a «Més…» el toc inicial es manté).
- Les unitats afectades són sempre les que **caduquen primer**; en congelar, `frozenDate = avui` (sense data de caducitat).

## Estats del botó principal
Valor 1: «Consumeix 1» / «Congela'n 1»; valor M: «Consumeix els M» / «Congela'n totes».

## Textos
| Clau | Text |
|---|---|
| `multi_consumed_one` | 1 %s consumit |
| `multi_frozen_one` | 1 %s congelat |
| `multi_more` | Més… |
| `multi_consume_question` | Quantes en consumeixes? |
| `multi_freeze_question` | Quantes en congeles? |
| `multi_freeze_more_question` | Quantes més en congeles? |
| `multi_consume_note` | Les %d que quedin segueixen al rebost. |
| `multi_freeze_note` | S'afegeixen als que ja són al congelador. |
| `multi_overline_frozen` | %1$d congelat · queden %2$d |
| `multi_consume_cta` | Consumeix %d |
| `multi_consume_all` | Consumeix els %d |
| `multi_freeze_cta` | Congela'n %d |
| `multi_freeze_all` | Congela'n totes |

## Accessibilitat
Pulsació llarga amb acció personalitzada «Consumeix diverses» / «Congela diverses» (accions `semantics`), perquè no depengui només del gest. El snackbar anuncia el text i exposa «Més…» i «Desfés».

## Dades
Sense canvis de model: cada unitat és un `Item`; consumir/congelar N és aplicar-ho a N ítems en una transacció, i «Desfés» és la seva inversa.
