# Rebost

Captura: `Rebost.png`. Font: `Rebost.dc.html`. Nova pestanya (abans les categories eren al dashboard).

## Estructura

1. Overline «61 productes · 5 categories» + títol `display` «Rebost».
2. **Cercador**: camp pastilla 52 dp, `surface-high`, icona `search`, placeholder «Cerca un producte». Resultats: llista de productes (mateixa fila que Categoria) amb la categoria a la meta.
3. **Graella 2 columnes**, gap 10. **Targeta de categoria** (`surface-card`, radi 28, mín. 178 dp, padding 16):
   - Forma 56 dp + icona; a dalt a la dreta, si n'hi ha, pastilla «N caducat(s)» (`expired-container`).
   - Nom (`card-title`) i «14 productes · propera, avui» (`body-small`).
   - **Barra d'estats** 8 dp: segments proporcionals al nombre d'ítems per estat (`expired`, `soon`, `fresh`, `frozen`), gap 2, radi 4. Omet els estats amb 0. Decorativa (`aria-hidden`); la informació textual ja hi és.
   - Tocar → `category/{id}`.
4. **Targeta «Nova categoria»**: vora discontínua 2 dp `outline-variant` (#CDBFA6), icona `add`, text `primary`. → `category/new`.
5. Barra flotant (Rebost actiu) + FAB.

## Ordre
Per `sortOrder` (configurable més endavant amb arrossegar; fora d'abast ara). Per defecte, l'ordre de l'onboarding.
