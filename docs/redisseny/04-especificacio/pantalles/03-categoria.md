# Categoria

Captura: `Categoria.png`. Font: `Categoria.dc.html`. Substitueix `01-app-original/app/02_category_detail.png`.

## Estructura

1. Barra superior sense títol: `arrow_back` (48 dp) i `more_vert` (menú: Edita categoria, Esborra categoria).
2. **Capçalera**: forma 80 dp + icona 36 · nom `screen-title` · «14 productes · propera, avui».
3. **Xips de filtre** (una fila amb scroll horitzontal, alt 36, radi 12): «Tots 14» (seleccionat: `primary` amb `check`), «Caducat 1», «Aviat 4», «Fresc 8», «Congelat 1» — cada un amb punt de 8 dp del color fort d'estat. Selecció única.
4. **Seccions per estat** en ordre Caducat → Aviat → Fresc → Congelat (amaga les buides):
   - Capçalera: punt 10 dp + nom (`card-title` 20) + recompte.
   - **Fila de producte** (llista connectada, mín. 64): nom (`title-row`), meta «1 brick · dg. 11 oct» (`body-small`), **pastilla d'estat** a la dreta amb el temps relatiu («Fa 2 dies», «Avui», «Demà», «2 dies», «10 dies», «Congelat») sobre el `*-container` corresponent.
   - **Producte amb diverses unitats**: la fila diu «3 unitats · propera dc. 21 oct», mostra la pastilla de la unitat més propera i un `expand_more/expand_less`. Desplegat, cada unitat és una fila de 52 dp, sagnada 32 dp, amb punt d'estat 6 dp, «Unitat N» i la data a la dreta, dins el mateix grup connectat.
   - Una unitat sola = fila simple (sense nivell producte visible).
   - Tocar una fila/unitat → full Accions.
5. **FAB estès** a baix a la dreta: `add` + «Afegeix», 64 dp, radi 20 → `add?categoryId=…`.

## Notes
- Fora: calendari setmanal i pestanyes per producte.
- El filtre seleccionat pot arribar per argument (`filter=EXPIRED` etc.).
