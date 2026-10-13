# Inici sense productes «Aviat»

Captures: `IniciSenseAviat.png` (sense caducats) i `IniciSenseAviatCaducats.png` (amb la targeta de caducats). Fonts: els `.dc.html` homònims.

## Problema
Quan no hi ha cap producte «Aviat», l'Inici mostrava només «No caduca res aviat. Bona feina.» i «Veure el rebost»: una pantalla buida, sense accions, encara que hi hagués productes frescos que caduquen aquesta mateixa setmana (la tira de la setmana en mostrava els punts) o caducats per revisar.

## Regles
L'Inici **mai no es queda buit** mentre hi hagi alguna cosa amb data:
1. **Sense missatge ni targeta**: no cal dir «Res caduca aviat»; la xifra «0 Aviat» ja ho diu. Just sota de les tres xifres comença directament la llista de **properes caducitats**.
2. **Properes caducitats, amb límit**: no es llisten tots els productes, sinó els **propers 3 dies amb productes** (els dies sense res es salten; «dimarts 13» i «dijous 15» poden ser dos dies consecutius de la llista) i, com a màxim, **8 files** en total (si un dia té moltes files, es talla i el darrer dia mostra «+N més»). Pot haver-hi 100 dies amb productes: se'n mostren només els primers. Compta tots els estats (frescos i congelats amb data) a partir d'avui. Mateixes seccions per dia, mateixes files i ✓ que «Aviat». Si no hi ha cap producte amb data, no es mostra cap secció.
3. **Un sol enllaç al final**: botó de text centrat «**Veure el rebost** ›» (`primary`, 48 dp, sense fons). Només en aquest estat. **Transforma** ja és al menú del botó «+ ⌄» (`16-transforma.md`); no es repeteix aquí.
4. La **targeta de caducats** (si n'hi ha) continua a dalt, igual que sempre.
5. El botó «+ ⌄» continua sent l'entrada principal per afegir.
6. **Rebost buit del tot** → continua l'estat «Inici buit» (`InicibBuit`): no s'ha de barrejar amb aquest.
7. **Pocs elements fixos**: a part de la targeta de caducats (si n'hi ha), no hi ha cap altre banner, targeta o missatge; la llista ha de quedar visible sense fer scroll.

## Textos (ca)
- Enllaç: «Veure el rebost»

## Tests
- Estat sense Aviat, amb frescos amb data: els primers 3 dies amb productes, màxim 8 files, + enllaç.
- Molts dies amb productes: només es mostren els primers 3 dies / 8 files.
- Sense Aviat i sense cap producte amb data: cap secció, només l'enllaç al rebost.
- Amb Aviat: no apareix l'enllaç (comportament actual).
- Amb caducats: la targeta de caducats i aquest estat conviuen.
