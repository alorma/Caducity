# Caducats

Captura: `Caducats.png`. Font: `Caducats.dc.html`. Substitueix `app/05_filtered_expired.png`.

> És una variant de la plantilla comuna de pantalles d'estat (`13-pantalles-estat.md`). Aviat, Frescos i Congelats fan servir la mateixa plantilla amb altres colors i accions.

## Estructura

1. **Capçalera** a tota amplada, `expired-container`, radi inferior 32, text `on-expired-container`:
   - `arrow_back` · overline «7 productes» · títol `display` «Caducats» · «Si encara són bons, canvia'n la data. Si no, llença'ls.»
   - Botons: **Llença'ls tots** (`delete_sweep`, fons `expired`, text blanc) → diàleg «Llençar 7 productes?» / **Llença** · **Selecciona** (`checklist`, outlined `on-expired-container`) → mode selecció múltiple (checkbox per fila + barra inferior «Llença (N)» / «Canvia la data»).
2. **Grups per categoria** (ordenats per nombre de caducats, desc.): forma 28 + nom (`card-title` 18) + recompte.
3. **Fila** (llista connectada, mín. 64): nom + «Fa 1 dia · ds. 10 oct» en `on-expired-container` 600 + dos botons rodons 44 dp sobre `surface`: `event_repeat` («Canvia la data» → mateix bloc de dates ràpides en full petit) i `delete` en `expired` («Llença» → `DISCARDED`, snackbar amb Desfés).

## Estat buit
Si no queda res: «Res caducat» + «Tot el que tens és dins de data.» i tornar a Inici.
