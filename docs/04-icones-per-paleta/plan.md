# Pla: icona de l'app per paleta

Especificació: [`spec.md`](spec.md). Captura: `captures/IconaPaletes.png` (les 6 paletes, clar i fosc). Font de disseny: `font-dc-html/IconaPaletes.dc.html`.
Depèn de [paletes](../03-paletes/plan.md) (ja fet: `ColorPalette`, `ThemePreferences.colorPalette`).

## Punt de partida i diferències amb l'especificació

| Especificació | Codi actual | Decisió |
|---|---|---|
| Galeta + fulla en VectorDrawable a 108 dp (el mockup és provisional) | Ja existeix: `ic_launcher_foreground.xml` (galeta amb fulla, 108 dp) i `ic_launcher_monochrome.xml` | Es reutilitza el dibuix; només canvien els colors. El `main` i el `debug` en tenen cadascun una còpia |
| Rebost clar: fons `#E8B95A`, galeta `#2E6B3B`, fulla `#FFFAEE` | Galeta `#4D5A1E`, fulla `#FBF6EC` (oliva antic) | La icona del Rebost passa als colors nous (com la paleta) |
| Un `activity-alias` per paleta | Un sol `MainActivity` amb el filtre `MAIN`/`LAUNCHER` | El filtre `LAUNCHER` passa als alias (si no, hi hauria dues icones); els altres filtres i els *deep links* es queden a `MainActivity` |
| Franja `DEV` a cada alias | `debug/res/drawable/ic_launcher_foreground.xml` dibuixa la franja dins del mateix vector | Per paleta, `ic_icon_<paleta>_foreground` és un `layer-list` (símbol + franja `DEV` només a `debug`) |
| Colors clar a `values`, fosc a `values-night` | `ic_launcher_background` únic | Tres colors per paleta: fons, galeta, fulla |
| Dinàmic → Rebost | — | `AppIconManager` tradueix `DYNAMIC` a Rebost |
| Splash amb la icona de la paleta | Splash per defecte | Si complica, es deixa Rebost i s'avisa (spec) |

No hi ha cap opció a Ajustos per triar la icona (spec: no implementar-la).

## Tasques

1. - [x] **Docs**: copiar captures, font i especificació; aquest pla.
2. - [ ] **Recursos**: colors per paleta (clar/fosc), 6 `adaptive-icon` + símbol vectorial per paleta, monocroma compartida, variant `debug` amb la franja. Validació: `assembleDebug`/`assembleRelease` + render de les 6 icones en clar i fosc.
3. - [ ] **Manifest**: 6 `activity-alias` (`.IconRebost`…`.IconCamp`), només Rebost habilitat. Validació: test que cada alias existeix.
4. - [ ] **`AppIconManager`**: paleta → alias habilitat (només un), `DONT_KILL_APP`, es reaplica a l'arrencada si no coincideix; Dinàmic → Rebost. Validació: test unitari.
5. - [ ] **Connexió**: `SettingsViewModel.onColorPaletteChange` i inici de `MainActivity`. Validació: tests + prova al mòbil amb captures del llançador (2 paletes, clar i fosc).

## Obert (no es resolen sense Bernat)

1. Opció «Icona de l'app: segueix la paleta / sempre Rebost» a Ajustos: no decidit, no s'implementa.

## Nota de versions

Visible per a l'usuari: mereix una línia a les notes (proposar en acabar, sense editar sense acord).
