# Prompt per a Claude Code: actualitzar el pla

1. Substitueix la carpeta `docs/redisseny/` del repositori per aquesta versió del paquet (versió 2). **Conserva** el teu `docs/redisseny/PLA-MIGRACIO.md`.
2. Enganxa aquest text a Claude Code:

---

He actualitzat el paquet de redisseny a `docs/redisseny/` (versió 2). Canvis respecte a la versió que vas fer servir per al pla:

- `RESPOSTES-AL-PLA.md`: les meves respostes a les preguntes P1–P16 i un parell d'afegits.
- **Calendari definit**: nova fitxa `04-especificacio/pantalles/11-calendari.md` (tira de 7 dies a l'Inici i a Categoria + vista de mes) i captures noves `02-disseny-nou/captures/Main.png`, `Categoria.png` i `CalMes.png` (fonts a `font-dc-html/`).
- Actualitzats: `pantalles/01-inici.md`, `pantalles/03-categoria.md`, `01-navegacio.md` (ruta `calendar`), `02-migracio-pantalles.md`, `04-model-de-dades.md` (quantitat = N ítems), `05-textos.md` (textos del calendari) i `08-decisions-i-obertes.md`.

Llegeix `RESPOSTES-AL-PLA.md`, `pantalles/11-calendari.md` i els fitxers actualitzats, i **actualitza `docs/redisseny/PLA-MIGRACIO.md`**. No escriguis codi encara.

1. Aplica totes les respostes. Marca cada pregunta com a ✅ amb la decisió i treu-la de les bloquejants.
2. Reflecteix P2 = opció B a tot el pla: model, use cases (`AddItemBatchUseCase` crea N ítems), Accions (sobre 1 unitat), compatibilitat amb ítems antics amb `packSize > 1`, backup i tests.
3. Reflecteix P6 a la fase 5: agrupació (producte, dia), recomptes per unitats, ✓ sobre la unitat més propera.
4. Afegeix a la migració 2→3 el buidat (o la ignorància a la UI) dels `identifier` dels ítems amb producte, amb el seu test.
5. **Calendari**:
   - Treu-lo de «Fora d'abast» i de les preguntes obertes.
   - Fase 5: component `WeekStrip` a l'Inici, botó `calendar_month` a la capçalera, nova ruta `Calendar(categoryId?, date?)` amb la vista de mes, i `ObserveCalendarUseCase`.
   - Fase 6: `WeekStrip` a Categoria (filtrat per categoria) i botó `calendar_month` a la barra superior, que obre `Calendar(categoryId)`.
   - Decideix i justifica si la graella del mes reaprofita `kalendar` o es fa a mà; si es fa a mà, programa l'eliminació de `kalendar` a la fase 11.
   - Afegeix els tests de la fitxa (`WeekStripTest`, `CalendarMonthTest`, `ObserveCalendarUseCaseTest`) i els textos a la fase 11.
6. Corregeix l'estat buit de la fase 7: «Res caducat».
7. Revisa si algun canvi altera l'ordre o les dependències de les fases (secció 11) i actualitza els riscos.

Quan acabis, ensenya'm un resum dels canvis al pla i espera la meva aprovació abans de començar la fase 0.
