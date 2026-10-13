# Pla: paletes de color

Especificació: [`spec.md`](spec.md). Captures: `captures/PaletaAjustos` (selector), `PaletaGuia` (totes), `PaletaInici*` (Inici en clar) i `PaletaFosc*` (en fosc). Fonts de disseny: `font-dc-html/`.
Paquet **independent** de [diverses unitats](../01-diverses-unitats/plan.md) i de [Transforma](../02-transforma/plan.md).

## Punt de partida i diferències amb l'especificació

| Especificació | Codi actual | Decisió |
|---|---|---|
| `AppSettings.colorPalette` (DataStore) | No hi ha DataStore: `ThemePreferencesImpl` usa `multiplatform-settings` (`Settings`), claus `theme_mode` i `use_dynamic_colors` | Afegir `color_palette` a `ThemePreferencesImpl`; no introduir DataStore |
| Interruptor «Color dinàmic» | `StyledSettingsSwitchCard` a `SettingsScreen`, `SettingsState.dynamicColors`, `SettingsViewModel.onDynamicColorsChange`, textos `settings_dynamic_*` | Es substitueix per la targeta «Colors»; s'eliminen tots quatre |
| Migració: dinàmic activat → `DYNAMIC` | `use_dynamic_colors` per defecte `false` | Valor antic `true` → `DYNAMIC`; si no (`false` o sense valor) → `REBOST`. Un `DYNAMIC` desat es llegeix sempre com `DYNAMIC` |
| `CaducityColors` amb els 12 tokens | `CaducityColors` ja existeix però només té `surfaceShape` + els 4 estats; els altres tokens són constants a `Color.kt` i dos `ColorScheme` fixos a `ColorSchemes.kt` | Separar: `StateColors` (estats, ja és el que fa `CaducityColors` avui) i una paleta per variant que produeix el `ColorScheme` + `surfaceShape` |
| Rebost = oliva actual | Els valors actuals (`#FBF6EC`, primari `#4D5A1E`…) no són els del Rebost italià (`#FFFAEE`, `#2E6B3B`…) | El Rebost nou **substitueix** els valors de `Color.kt` (canvi visual de la paleta per defecte, ja previst a l'especificació) |
| Widget (Glance), Tile de Wear | No existeixen (són al [backlog](../backlog.md)) | No es fan aquí. Es deixa anotat: quan s'implementin, han de llegir la paleta (fase 5) |
| Accent de notificació | `AndroidExpirationNotificationHelper` | Revisar si posa color; si no, definir-lo (fase 5) |
| Test de contrast | `TokenContrastTest` llegeix `tokens.json` (només Rebost antic) | Reescriure sobre les paletes de Kotlin, no sobre el JSON (fase 4) |
| Previews «6 paletes × clar/fosc» | `@PreviewDynamicLightDark`, `PreviewTheme`, `screenshotTest/ComponentsScreenshotTests.kt` | Anotació/helper de previews per paleta; referències de captura via l'etiqueta `update-screenshots` |

Reutilitzem: `CaducityTheme` (accés a `colorScheme`/`colors`), `StyledSettingsGroup`, `ShapePosition`/`ConnectedRow`, `supportsDynamicColors()`, `dynamicColorScheme(isDark)` (ja només substitueix els tokens d'ambient i conserva estats i error, com demana l'especificació).

Les cartes d'estat (`ExpiredLight`…) i el vermell destructiu **no es toquen** en cap fase.

## Fase 1 — Model, setting i migració

1. `enum class ColorPalette { DYNAMIC, REBOST, NEVERA, HORT, FLECA, BESSA, CAMP }` (a `ui/theme/`), amb `fromName(String?)` tolerant (valor desconegut → `REBOST`).
2. `ThemePreferences`: `colorPalette: MutableState<ColorPalette>`, `loadColorPalette()`, `setColorPalette(...)`. Treure `useDynamicColors`/`setDynamicColorsEnabled` (i `ThemePreferencesNoOp`).
3. Migració dins `ThemePreferencesImpl`: si no existeix `color_palette`, llegir `use_dynamic_colors`: `true` → `DYNAMIC`, altrament `REBOST`; escriure `color_palette` i esborrar la clau antiga. El setting mai es reescriu en llegir: `DYNAMIC` continua sent `DYNAMIC`.
4. Tests: `ThemePreferencesMigrationTest` (sense valor → `REBOST`; `use_dynamic_colors=true` → `DYNAMIC`; `false` → `REBOST`; valor de paleta desconegut → `REBOST`; un `DYNAMIC` desat es llegeix com `DYNAMIC`; idempotent en una segona càrrega).

**Sortida:** la paleta triada es desa i es migra sense pèrdua; l'app encara es veu igual.

- [x] Tasques 1-4 (0d331f2)

## Fase 2 — Paletes i tema

1. Un fitxer per família de tokens: `ColorPaletteColors` (12 tokens, taules de l'especificació, clar i fosc) per a les 6 paletes, a `ui/theme/colors/palettes/` (un objecte per paleta). `Color.kt` deixa només els tokens d'estat i el scrim.
2. `fun ColorPalette.scheme(darkTheme): ColorScheme` + `surfaceShape` (reutilitzant el mapatge token → rol M3 actual de `ColorSchemes.kt`, sense canviar els rols d'estat, `tertiary`, `error`).
3. `StateColors` independent de la paleta (el que ara és `CaducityColors` sense `surfaceShape`). `LocalCaducityColors` continua exposant estats + `surfaceShape` de la paleta activa.
4. `AppThemeContent`: `colorPalette` + `darkTheme`; `DYNAMIC` → `dynamicColorScheme` (només 12 tokens, ja és així); si `!supportsDynamicColors()`, pinta Rebost sense tocar el setting. Fos de colors a ~300 ms (`animateColorAsState`, *effects*) en canviar de paleta, no en canviar de tema a l'inici.
5. Adaptar `InternalTheme`/`LocalSettingsTileColors` i les barres del sistema.
6. Tests: cada paleta × clar/fosc dóna un `ColorScheme` complet; els tokens d'estat són idèntics a totes (`StateColors` no depèn de la paleta); `DYNAMIC` no toca estats ni `error`.

**Sortida:** canviar `ColorPalette` des de les preferències (debug) repinta l'app amb la paleta, estats intactes.

- [x] Tasques 1-6 (495967e)

## Fase 3 — Targeta «Colors» a Ajustos

1. `ColorPaletteSwatch` + graella de 3 columnes (mostra 56 dp, dues meitats `primary`/`primary-container`; Dinàmic: degradat còniic + `wallpaper` blanc). Les mostres usen els colors **de la seva** paleta (no de la triada).
2. Seleccionada: forma de radi 18 + ✓ blanc; la resta, cercle (radi 28). Nom 13 sp, 700 si seleccionada. Animació de la forma.
3. Capçalera: títol «Colors» + subtítol «Nom · caràcter» (Dinàmic: «Del fons de pantalla»). Dinàmic amagat si `!supportsDynamicColors()`.
4. Semàntica: contenidor `radiogroup` (`selectableGroup`), cada mostra `Role.RadioButton` amb nom i estat.
5. `SettingsScreen`: la targeta ocupa el lloc de l'interruptor, entre «Tema» i «La setmana comença» (ajustar `ShapePosition`/`count` del grup: ara sempre 4 files). `SettingsState.colorPalette`, `SettingsViewModel.onColorPaletteChange`. Eliminar `onDynamicColorsChange` i el seu codi.
6. Textos de l'apartat «Textos» de l'especificació a `values`, `values-ca` i `values-es` (en/es per traducció de sentit); eliminar `settings_dynamic_*`.
7. Tests: UI del selector (tocar una paleta canvia el tema; es manté en reiniciar l'activitat), `SettingsViewModelTest` actualitzat, semàntica (un sol seleccionat, noms accessibles).

**Sortida:** es pot triar la paleta a Ajustos, s'aplica a l'instant amb fos i es recorda.

- [ ] Tasques 1-7

## Fase 4 — Contrast i previews

1. `PaletteContrastTest` (substitueix l'ús de `tokens.json` a `TokenContrastTest`): per a les 6 paletes en clar i fosc, comprova els mínims de l'especificació: text sobre fons ≥ 13,6 / 14,4; `ink-variant` sobre targeta ≥ 5,4 / 8; `on-primary` sobre `primary` ≥ 5,2 / 7,5; contingut sobre contenidor ≥ 10,4 / 6,8. Mantenir els parells d'estat existents (≥ 4,5).
2. Si `tokens.json` ja no té ús, retirar-lo dels recursos de test i del repo.
3. Previews d'Inici, Rebost i Ajustos en les 6 paletes × clar/fosc (helper a `ui/theme/preview/`); afegir-les als tests de captura i regenerar referències (etiqueta `update-screenshots` a la PR).
4. Revisió visual dels dos punts oberts de l'especificació: **Camp/Aviat** (groc proper) i **Hort/Frescos** (llima proper al verd). No es resolen aquí; es documenten amb captura i es consulten a Bernat.

**Sortida:** test de contrast verd i captures d'Inici i Ajustos en totes les paletes, clar i fosc, per revisar.

- [x] Tasques 1-4 (0d331f2)

## Fase 5 — Notificació (i widget/Wear quan existeixin)

1. Accent de la notificació = `primary` de la paleta triada (Dinàmic: el color dinàmic o, si no es pot resoldre fora de Compose, el `primary` del Rebost). Accés des del worker sense Compose: llegir `ThemePreferences` i el `ColorPaletteColors` de la paleta.
2. Test: l'accent canvia amb la paleta.
3. Anotar al [backlog](../backlog.md), a l'entrada del Widget, que ha de seguir la paleta (i el Tile de Wear si arriba a existir).

**Sortida:** les notificacions segueixen la paleta.

- [ ] Tasques 1-3

## Obert (de l'especificació, no es resolen sense Bernat)

1. **Camp** té un groc proper al d'«Aviat». Es vigila a la fase 4.
2. **Hort**: el contenidor llima s'assembla al verd de «Frescos». Es vigila a la fase 4.
3. Accent de tomàquet al Rebost: descartat pel vermell de «Caducat».
4. Color personalitzat (propi): fora d'abast.
5. *Decidit:* Dinàmic continua sent la primera opció del selector (a API ≥ 31) i es llegeix sempre com `DYNAMIC`; per defecte, `REBOST`. Només a la fase 2, per no fallar a API < 31 (p. ex. un setting restaurat d'un altre dispositiu), el tema pinta amb Rebost sense reescriure el setting.

## Nota de versions

Visible per a l'usuari (la paleta per defecte canvia i hi ha selector nou): mereix una línia a les notes de la versió quan toqui (vegeu `.claude/rules/release-notes.md`; es proposa en acabar, no s'edita sense acord).
