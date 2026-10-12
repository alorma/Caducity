# Prompt per a Claude Code

Enganxa això a Claude Code, amb aquest paquet descomprimit dins del repositori de Caducity (per exemple a `docs/google-play/`).

---

Necessito generar les imatges de la fitxa de Google Play de Caducity amb **captures reals de l'app**.

Llegeix primer `docs/google-play/README.md` i `docs/google-play/01-especificacio.md`. La carpeta `reference/` és només referència de mides, colors i textos; no s'ha de renderitzar.

## Què cal fer

1. **Explora el codi** i digues-me, abans de tocar res, quin mecanisme farà servir per:
   - posar dades de mostra a la base de dades (Room) només en builds de debug o en un test instrumentat, mai en release;
   - fixar la data del dispositiu al diumenge 11 d'octubre de 2026 (si l'app llegeix el rellotge del sistema, proposa com injectar-lo sense tocar el codi de producció);
   - fer les captures de manera reproduïble (instrumented test amb `UiAutomator`/`Compose` + `adb screencap`, Roborazzi o similar; tria el que ja encaixi al projecte).
2. **Crea les dades de mostra** de la secció 6 de l'especificació, fidels als productes i categories indicats.
3. **Genera les captures en brut** de les 7 pantalles de la taula 4 (Inici, Afegeix, Accions, Rebost, Categoria Làctics, Calendari, Inici fosc) **per a cada un dels 3 idiomes** (`ca`, `es-ES`, `en-US`; canvia l'idioma de l'app amb `setApplicationLocales`/`adb` o el locale del test), en un emulador de pantalla alta (relació aproximada 9:19,5), barra d'estat neta (demo mode: 12:00, bateria plena, sense notificacions) i **sense** barra de navegació del sistema visible si es pot evitar. Desa-les a `play/raw/<locale>/`. **Primer comprova que l'app té textos en castellà i anglès** (`values-es`, `values-en`); si falten o són incomplets, no inventis traduccions de la UI: avisa'm i continua amb els idiomes que sí que hi són. Les dades de mostra (noms de productes i categories) també han de sortir traduïdes per idioma; proposa'm la traducció dels noms de la secció 6 abans de fer-la servir.
4. **Escriu un script de composició** (Node + Playwright o Python + Pillow; el que ja tingui el projecte) que generi a `play/out/`:
   - `play/out/icon-512.png` (una sola, igual per a tots els idiomes) i, per a cada locale (`ca/`, `es-ES/`, `en-US/`), `feature-1024x500.png` i `phone-01…07.png`, amb les mides, colors, tipografies i textos exactes de l'especificació (textos per idioma a la secció 4b);
   - les fonts (Bricolage Grotesque, Figtree, Material Symbols Rounded) han de venir de fitxers locals del repositori, no de la xarxa.
5. **Verifica** automàticament: mides exactes, sense canal alfa a la feature graphic i a les captures, icona ≤ 1 MB, i obre cada imatge per revisar-la visualment (text tallat, desbordaments, bisell i ombra).
6. Si el repositori ja fa servir fastlane o Gradle Play Publisher, deixa també les imatges a l'estructura que esperen (`metadata/android/ca-ES/images/…`); si no, no hi afegeixis res.

## Normes

- No canviïs res de la UI de l'app ni del codi de producció. Si una pantalla real difereix molt del mockup, ho apunto i en parlem, no ho «arreglis».
- La captura de mode fosc només es fa si el mode fosc ja és a l'app; si no, salta-la i avisa'm.
- La icona ha de ser idèntica a la icona adaptativa de l'app; si no ho és, avisa'm abans de continuar.
- Si no pots tenir un emulador o un dispositiu, no inventis captures: para i digues-me què et falta.
- Dubtes en català i en un sol missatge.

Quan acabis, dona'm: la llista de fitxers generats amb mides i pesos, i qualsevol diferència entre captura real i mockup.
