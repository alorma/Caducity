# Pla: redisseny d'Ajustos (14 d'octubre)

Especificació: [`spec.md`](spec.md). Captures: `captures/` (`Ajustos`, `AjustosColors`, `MargeCategories`); fonts: `font-dc-html/`.
Flux: `.claude/rules/phased-work.md`. Paquet independent de la revisió de coherència; la «Durada habitual» i l'«Aviat» global ja hi són (fases 1 i 6 de `04-revisio-coherencia`), així que aquí no hi ha cap migració.

## Decisions (Bernat, 14 d'octubre)

- «Idioma de l'aplicació» es queda, a Aparença, després de Colors (el disseny no la té).
- Tocar la fila «Notificacions» continua obrint el selector d'hora; l'interruptor activa o desactiva.
- Capçalera: `LargeFlexibleTopAppBar` d'M3E (títol gran, es plega en fer scroll).
- Depuració (només en builds de depuració): última fila de «Les teves dades».
- «La setmana comença»: menú desplegable natiu, mateixes opcions (dilluns, dissabte, diumenge).
- «Quant a»: peu discret; obre la mateixa pantalla.

## Tasques

1. Documentació: aquest paquet a `docs/05-ajustos/` i aquest pla.
2. Estructura i grups: capçalera, grups Avisos · Dates · Aparença · Les teves dades, peu «Quant a», llista segmentada i icones.
3. Selectors: segmentats d'«Aviat» i «Tema» (✓, 40 dp) i desplegable de «La setmana comença».
4. Colors en full modal (`AppBottomSheetState`): fila amb nom i mostra de 28 dp; full amb graella de 4 columnes, mostres de 56 dp, ✓ i nota; s'aplica a l'instant i el full es queda obert.
5. Textos i subtítols (ca, es, en): Privadesa amb l'estat de les estadístiques, Durada habitual «Per suggerir la data · …», «Aviat» «Quan passa a groc i t'avisem», etiquetes de grup, fora textos sense ús.
6. Captures de test i tancament; proposta de release notes.

## Estat

| Tasca | Estat |
| --- | --- |
| 1 — Documentació | Feta: `2c8f2b64` |
| 2 — Estructura i grups | Feta: `be813024` (icones noves `event_available`, `view_week`, `contrast`; el generador d'icones ara també porta les quatre que s'havien afegit a mà) |
| 3 — Selectors | Feta: `2ae0e8a3`, `31ef3928` (`ToggleButton` connectats d'M3E amb ✓; desplegable natiu per a «La setmana comença») |
| 4 — Colors en full modal | Feta: `465357c1`, `e04ca72b` (✓ del color del text del tema, Bernat) |
| 5 — Textos i subtítols | Feta: `ae8e2e59` (Privadesa llegeix el consentiment en tornar a Ajustos; «verd i crema» sense «(per defecte)», com a la captura nova) |
| 6 — Captures i tancament | Feta: `80db8fb7` (full de Colors en les 6 paletes, clar i fosc; referències refrescades) |

Fora del pla, demanat per Bernat: `075a659c` «Quant a» enllaça a la web (per idioma) en lloc del repositori de GitHub.

Desviacions: els botons segmentats queden sagnats sota el títol (els posa el `SettingsTileScaffold` de compose-settings), no a tota l'amplada com a la captura. Títols de fila foscos com a la captura (`ef0c3952`, també a les subpantalles d'Ajustos). Validat a l'emulador en clar (fosc només per captures de test).
