# 00 · Resum i principis

## Per què es redissenya

Problemes de l'app actual (vegeu `01-app-original/`):

- **Colors massa saturats** (vermell, groc llima, verd neó, cian) que no transmeten calma ni personalitat.
- **Calendari setmanal repetit per categoria** al dashboard: ocupa molt i informa poc.
- **Detall de categoria confús**: pestanyes per producte, targetes «Fresh» repetides i el nom de l'ítem separat de la seva targeta.
- **Poca personalitat**: icona i splash de debug, jerarquia visual plana, textos en anglès poc treballats.
- **Afegir és lent**: cal crear categoria abans de poder afegir res, i el flux no està pensat per fer-se de pressa (cuinant, endreçant la compra, al súper).
- Onboarding llarg (6 pàgines), acaba amb l'app buida i amb l'avís legal en una pàgina fàcil de saltar.

## Què és el nou disseny

**Material 3 Expressive de rebost**: fons crema, oliva com a color d'acció, quatre colors d'estat suaus, formes orgàniques (`MaterialShapes`) i una tipografia de títols ampla i càlida (Bricolage Grotesque) amb Figtree per al text.

## Principis

1. **El color és estat. La forma és categoria.**
   - Quatre estats amb color propi: **Caducat**, **Aviat**, **Fresc**, **Congelat**. Aquests colors no s'usen per a res més (excepte `expired` per a accions destructives).
   - Les categories **no tenen color**: es distingeixen per una **forma** (`MaterialShapes`: Galeta, Flor, Trèvol, Sol, Coixí, Segell) i una **icona** Material Symbols.
2. **Primer, el que és urgent.** La pantalla d'inici és «Què caduca»: caducats, avui, demà, resta de la setmana. Les categories passen a la pestanya **Rebost**.
3. **Afegir en 3 tocs.** Nom (amb autocompletat de l'historial propi), categoria (un toc), data (teclat numèric com a l'envàs, o botó ràpid). Mode tanda per afegir molts productes seguits. Accessible des del widget i la drecera de la icona.
4. **Una forma per grup.** Llistes i grups de botons connectats: 2 dp de separació, radi exterior 24, interior 6 (llistes) o 8 (botons).
5. **Colors suaus, contrast real.** Text sobre contenidors d'estat sempre amb el seu `on-*-container`. Els colors forts d'estat només per a barres, punts i formes.
6. **Català, segona persona, frases curtes, verbs en imperatiu.** Sense exclamacions ni emojis.

## Abast d'aquesta fase

Inclou: totes les pantalles de `02-disseny-nou/`, el design system, la icona, el splash, el widget, les dreceres de la icona i l'onboarding nou.

Fora d'abast: IA, veu, càmera (codi de barres, OCR de dates o tiquets), llista de la compra, sincronització al núvol, tauleta (es pot adaptar després), mode fosc: **dins d'abast**, amb mockups i regles a `12-mode-fosc.md`.
