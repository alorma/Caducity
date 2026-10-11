Tenim un redisseny complet de l'app a `docs/redisseny/`. Llegeix primer `README.md` i després tots els fitxers de `04-especificacio/` (inclosa la carpeta `pantalles/`). Fes servir les captures de `01-app-original/` (estat actual) i `02-disseny-nou/captures/` (estat objectiu) com a referència visual, i els `.dc.html` de `02-disseny-nou/font-dc-html/` per a valors exactes (mides, colors, radis, textos). El design system és a `03-design-system/` (`tokens.json` és la font de veritat dels valors).

**No escriguis codi encara.** Vull un **pla de migració** a `docs/redisseny/PLA-MIGRACIO.md` que:

1. **Inventariï el codi actual** rellevant: tema (colors, tipografia, formes, Tone Vibrant/Soft), navegació, pantalles (dashboard, detall de categoria, accions, filtrat, crear categoria, settings, appearance, onboarding, splash), model de dades (Room entities, DAOs, migracions), preferències (DataStore), notificacions/WorkManager, widget si n'hi ha, recursos de text i idiomes, i tests existents. Per a cada element, indica fitxers i si es **manté**, **es modifica**, **es substitueix** o **s'elimina**.
2. Faci el **mapatge** entre cada pantalla actual i la nova (`04-especificacio/02-migracio-pantalles.md`) amb els composables concrets afectats.
3. Detecti **buits i conflictes** entre l'especificació i el codi (per exemple: si el model ja té estat consumit, com s'esborra avui un ítem, com es calcula «Expiring», quins idiomes hi ha) i proposi com resoldre'ls. Llista les preguntes que necessitis que respongui.
4. Proposi **migracions de base de dades** (Room) mínimes per a: `shape`, `icon`, `sortOrder` a categories; final de vida dels ítems (`CONSUMED` / `DISCARDED`); i les **preferències** noves/eliminades (`04-model-de-dades.md`), incloent valors per defecte per a usuaris existents.
5. Ordeni la feina en **fases incrementals**, cadascuna compilable, provable i publicable per separat. Suggeriment d'ordre (ajusta'l si el codi ho aconsella):
   1. Fonaments: tokens → `CaducityTheme` (colors clar/fosc, `CaducityColors` d'estat, tipografia amb fonts variables, formes, helper de llistes connectades), eliminació de Tone. Components base (fila connectada, pastilla d'estat, forma de categoria, grup de botons connectat, interruptor, xips).
   2. Model i dades: migracions Room, preferències, càlcul d'estat amb llindar configurable, consulta d'historial per autocompletar.
   3. Navegació nova: shell amb barra flotant + FAB, rutes.
   4. Afegeix (flux complet: autocompletat, data per teclat, dates ràpides, mode tanda amb Desfés, categoria nova ràpida). **Prioritat alta.**
   5. Inici «Què caduca» + estat buit.
   6. Rebost + Categoria + full d'Accions.
   7. Caducats (inclòs Llença i selecció múltiple).
   8. Nova/Edita categoria, Ajustos.
   9. Onboarding nou (animat; avís legal amb acceptació versionada; creació de categories triades; permís de notificacions).
   10. Icona adaptativa (normal, monocroma, debug amb franja DEV), splash, widget Glance, dreceres.
   11. Textos i traduccions, accessibilitat, mode fosc, neteja de codi mort (dashboard antic, calendari per categoria, pantalles d'onboarding antigues).
6. Per a cada fase: objectiu, fitxers a crear/modificar/eliminar, riscos, criteris d'acceptació (referenciant les captures), i **tests** a afegir o actualitzar (unitaris del càlcul d'estat i de les dates, de ViewModels, de migracions Room, i de UI/screenshot si el projecte en té).
7. Indiqui les **dependències** necessàries (versió de Compose Material 3 amb Expressive: `MaterialShapes`, `ButtonGroup`, `HorizontalFloatingToolbar`, `MotionScheme`; Glance per al widget; fonts) i si cal pujar versions.
8. Marqui explícitament el que és **fora d'abast** (IA, veu, càmera, calendari pendent de disseny, tauleta).

Quan tinguis el pla, ensenya-me'l i espera la meva aprovació abans de començar la fase 1.
