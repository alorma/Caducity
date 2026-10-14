# 21 · Ajustos (redisseny)

Substitueix la pantalla d'Ajustos anterior (captura `Ajustos`). Només mòbil. Captures: `Ajustos`, `AjustosColors`, `MargeCategories` (aquesta darrera ja estava definida a `20-revisio-coherencia.md` §1; aquí només hi canvia l'accés).

## Estructura: llista agrupada, tot a una pantalla

Capçalera: fletxa enrere + títol «Ajustos» (32 sp). Cinc grups amb etiqueta en majúscules (12 sp, 1,2 sp d'espaiat, color secundari). Dins de cada grup, targetes de 24 dp a les cantonades exteriors i 6 dp a les interiors, 2 dp de separació (patró de llista segmentada d'M3 Expressive). Fila mínima 60 dp, icona de 22 dp en color primari.

1. **Avisos**
   - «Notificacions» · subtítol «Cada dia a les 9:00» · interruptor amb ✓.
   - «“Aviat” vol dir» · subtítol «Quan passa a groc i t'avisem» · selector segmentat de 3 opcions: **1 dia · 3 dies · 1 setmana** (alçada 40 dp, la seleccionada porta ✓ i color primari). És el valor global d'«Aviat».
2. **Dates**
   - «Durada habitual» · «Per suggerir la data · Làctics 1 setmana…» · fletxa → pantalla `MargeCategories`.
   - «La setmana comença» · menú desplegable (Dilluns per defecte).
3. **Aparença**
   - «Tema» · selector segmentat **Clar · Fosc · Sistema**.
   - «Colors» · subtítol amb el nom de la paleta («Caducity · verd i crema») + mostra de color de 28 dp · **obre un full modal** (vegeu avall), no una pantalla.
4. **Les teves dades**
   - «Còpia de seguretat» («Exporta i restaura») → subpantalla.
   - «Privadesa» («Estadístiques d'ús: desactivades») → subpantalla.
5. Peu: «Quant a Caducity · versió i crèdits» (text discret, 13 sp, centrat).

Sense «Temes de temporada» (fora d'abast, vegeu `20-revisio-coherencia.md` §0).

## Colors com a full modal

`ModalBottomSheet` sobre la pantalla d'Ajustos atenuada. Contingut: títol «Colors», subtítol «S'apliquen a l'instant, en clar i en fosc.», quadrícula de 4 columnes amb: **Dinàmic** (colors del sistema, només Android 12+), **Caducity** (per defecte), Nevera, Hort, Fleca, Bessa, Camp. Cada mostra és un cercle de 56 dp mig fosc/mig clar; la triada té anell + ✓. Nota al peu: «Els colors dels estats (caducat, aviat, fresc, congelat) no canvien.» Tocar una mostra aplica el tema a l'instant (el full es queda obert; es tanca arrossegant o tocant fora).

## Canvis respecte a l'Ajustos anterior

- Una sola pantalla ordenada en 4 grups en lloc d'una llista llarga barrejada.
- Els selectors de poques opcions (Aviat, Tema) van **en línia** com a segmentats, no en diàlegs.
- Colors passa de pantalla pròpia a full modal.
- «Durada habitual» passa a viure al grup «Dates».
- Textos en català: imperatiu als botons, noms de nom a les files.
