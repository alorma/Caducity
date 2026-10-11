# Canvi: fila de la tanda a Afegeix

## Com afegir-ho

Copia aquests fitxers dins `docs/redisseny/`, **substituint** els existents:

- `02-disseny-nou/captures/AfegirTanda.png`
- `02-disseny-nou/font-dc-html/AfegirTanda.dc.html`
- `04-especificacio/pantalles/06-afegir.md`

## Prompt per a Claude Code

---

He actualitzat el disseny del mode tanda d'Afegeix a `docs/redisseny/`: `04-especificacio/pantalles/06-afegir.md` (apartat «Mode tanda › Fila "Desats en aquesta tanda"»), la captura `02-disseny-nou/captures/AfegirTanda.png` i la font `02-disseny-nou/font-dc-html/AfegirTanda.dc.html`.

Resum del canvi: els xips «Nom · dd/mm» dels productes desats en aquesta tanda se substitueixen per una **fila horitzontal amb scroll** (`LazyRow`) de targetes de 148 dp, del més recent al més antic. Es veu un tros de la targeta següent. Cada targeta té nom (fins a 2 línies), categoria, unitats i data, i dos botons: **Edita** (obre l'ítem en mode edició) i **Treu de la tanda** (esborra l'ítem, sense confirmació, amb snackbar «Desfés»).

Llegeix l'apartat i **actualitza `PLA-MIGRACIO.md`**, sense escriure codi:
- Fase 4: substitueix els xips de tanda pel component `BatchSavedRow` / `BatchSavedCard`; afegeix l'acció «Treu de la tanda» al `AddViewModel` (esborrar l'ítem + Desfés que el restaura amb el mateix id i dates) i el scroll a l'inici en desar.
- Tests de la fase 4: `AddViewModelTest` cobreix treure un ítem de la tanda, desfer-ho, l'ordre del més recent al més antic i treure'ls tots (la fila desapareix).
- Criteri d'acceptació: la nova `AfegirTanda.png`.

Ensenya'm el resum del canvi al pla.
