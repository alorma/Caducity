# Respostes al `PLA-MIGRACIO.md`

Respostes del propietari del producte a les preguntes de la secció 5 del pla (2026-10-11).

| # | Resposta |
|---|---|
| P1 | **Resolta**: s'elimina tot el mòdul d'IA (Llamatik, `firebase-ai`, assistent, descàrrega de models, `INTERNET`, foreground service). Fase 0. |
| P2 | **Opció B.** El selector de quantitat d'Afegeix crea **N ítems independents** amb la mateixa data. Consumit, Congela, Llença i canvi de data actuen sobre **1 unitat**. Els ítems antics amb `packSize > 1` es mostren com «pack de N» i conserven el consum parcial (`SplitAndConsume` només per a ells); no se'n creen de nous amb `packSize`. |
| P3 | **Sí.** Els usuaris existents veuen **una sola vegada** només el pas d'acceptació de l'avís legal, per desar `disclaimerAcceptedVersion`/`At`. |
| P4 | Recomanació per defecte: Descongela proposa `ara + remainingDays` prellenat i editable; no s'accepta data passada amb «Congelat». |
| P5 | Recomanació per defecte: Consumit permès sempre des d'Accions; Caducats només ofereix Llença / Canvia la data. |
| P6 | **Una fila per (producte, dia)** a l'Inici, amb «N unitats». El recompte de la secció compta **unitats**. El botó ✓ consumeix **una** unitat (la més propera a caducar). |
| P7 | Recomanació per defecte: es perden «eliminar producte» i «buidar consumits»; els productes buits queden invisibles a la UI però es conserven per a l'historial. |
| P8 | **No esborrar `kalendar`.** El calendari ja està dissenyat (`04-especificacio/pantalles/11-calendari.md`). Decidir al pla si es reaprofita per a la vista de mes o si és més simple una graella pròpia; justificar-ho. |
| P9 | **Es manté** «La setmana comença»: ara afecta la vista de mes. |
| P10 | Recomanació per defecte: la cerca de l'Inici obre Rebost amb el cercador enfocat. |
| P11 | Recomanació per defecte: cerca per nom de producte (i categoria), sense accents ni majúscules, només actius. |
| P12 | Recomanació per defecte: `add?itemId=` per a edició; «Mou» busca o crea el producte homònim a la categoria destí. |
| P13 | Recomanació per defecte: el widget fa servir la tipografia del sistema en negreta. |
| P14 | Recomanació per defecte: `ImageVector` de Material Symbols Rounded generats per script dins `:icons`. |
| P15 | **Es permet** desar una data passada, amb l'ajuda «ja ha caducat» en `expired`. Eliminar `FutureDateSelectableDates`. |
| P16 | Recomanació per defecte: els textos nous es tradueixen a **en** i **es** (a més de **ca**), per revisar. |

## Afegits i correccions

- **Migració 2→3**: buidar també l'`identifier` dels ítems que **ja tenen producte** (p. ex. «Cheddar Cheese #1»), o fer que la UI l'ignori sempre. «Unitat N» és derivat.
- **Fase 7**: l'estat buit és «**Res caducat**» (no «Res caduca»).
- **Calendari**: deixa de ser «pendent» i «fora d'abast». Entra a les fases 5 (tira a l'Inici + vista de mes) i 6 (tira a Categoria + vista de mes filtrada per categoria). Especificació completa a `04-especificacio/pantalles/11-calendari.md`.
