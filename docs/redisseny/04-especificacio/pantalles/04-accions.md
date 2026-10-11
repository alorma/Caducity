# Accions d'un producte (full inferior)

Captura: `Accions.png`. Font: `Accions.dc.html`. Substitueix `app/03_item_actions.png` i en part `app/04_reschedule_datepicker.png`.

`ModalBottomSheet`, radi superior 32, fons `surface`, `scrim` darrere, padding 12/20/28.

## Contingut

1. Drag handle 32×4 (`outline`).
2. **Capçalera**: forma 56 + icona de la categoria · nom `sheet-title` · «Unitat 1 de 3 · caduca dc. 21 oct» · pastilla d'estat («10 dies» sobre `fresh-container`).
3. **Grup connectat principal** (64 dp, gap 3):
   - **Consumit** (`check`), `primary`, més ample (flex 1.4), radis 32/8.
   - **Congela** (`ac_unit`), `frozen-container` / `on-frozen-container`, radis 8/32. Si l'ítem ja és congelat: **Descongela** (`water_drop`) i en desar es demana nova data.
4. **Canvia la data** (overline) + fila: «+1 dia», «+3 dies», «+1 setmana» (xips outlined 44 dp, radi 12) + botó icona `calendar_month` sobre `surface-high` → `DatePicker` M3 (el de `04_reschedule_datepicker.png`, amb els colors nous).
   - Les dates ràpides sumen a la **data actual de caducitat** i s'apliquen a l'instant (snackbar amb Desfés).
5. **Llista connectada** (56 dp): **Edita** (`edit`) → Afegeix en mode edició · **Mou a una altra categoria** (`drive_file_move`) → selector de categoria · **Esborra** (`delete`, text i icona `expired`) → diàleg de confirmació «Esborrar Formatge cheddar?» / **Esborra**.

## Haptic
`Confirm` a Consumit, Congela, dates ràpides i Esborra confirmat.
