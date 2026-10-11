# 12 · Mode fosc

Captures: `02-disseny-nou/captures-fosc/` — `MainFosc.png` (Inici), `RebostFosc.png`, `CategoriaFosc.png`, `AccionsFosc.png`, `AfegirFosc.png`, `CaducatsFosc.png`.
Fonts: `02-disseny-nou/font-dc-html/*Fosc.dc.html`.

Són les mateixes pantalles que en mode clar, amb els valors `dark` de `03-design-system/tokens.json`. **No hi ha cap canvi de layout, forma ni tipografia**: només canvien els colors. Si una pantalla no té captura fosca, s'aplica la mateixa regla de substitució.

## Regla de substitució (clar → fosc)

| Token | Clar | Fosc | Ús principal |
|---|---|---|---|
| `surface` | `#FBF6EC` | `#1C1A15` | Fons, fulls inferiors, fons dels botons rodons de les files |
| `surface-card` | `#F4ECDD` | `#26231D` | Files, targetes, avui a la tira |
| `surface-high` | `#ECE2CF` | `#302C24` | Camps, cercador, botons rodons de capçalera |
| `surface-shape` | `#E6DBC5` | `#3A352B` | Forma de categoria |
| `ink` | `#29241C` | `#F1EADC` | Text principal, cercle d'avui |
| `ink-variant` | `#665C4C` | `#CFC4B1` | Metadades |
| `outline` | `#8A7F6C` | `#9C9180` | Vores, interruptor apagat, drag handle |
| `outline-variant` | `#DCCFB8` | `#4A443A` | Vores de xips, targeta «Nova categoria» |
| `primary` | `#4D5A1E` | `#BCCB82` | FAB, botons principals, icones de categoria, seleccionat |
| `on-primary` | `#FFFFFF` | `#263010` | Text i icones sobre `primary` |
| `primary-container` | `#DCE3B0` | `#3A4515` | Barra flotant, xip seleccionat d'Afegeix, «Desa» |
| `on-primary-container` | `#283010` | `#DCE3B0` | Icones de la barra flotant |
| `expired` | `#B23F2A` | `#FF8E78` | Punts, barres, «Llença'ls tots», «Esborra» |
| `on-expired` | `#FFFFFF` | `#5A1408` | Text sobre `expired` |
| `expired-container` / `on-…` | `#F7D8CC` / `#7A2414` | `#5E2117` / `#FFD9CE` | Bàner de caducats, capçalera de Caducats, pastilles |
| `soon` | `#D9A23A` | `#E8B95A` | Punts i barres «Aviat» |
| `soon-container` / `on-…` | `#F8E2AE` / `#5C3F00` | `#4F3A0B` / `#F8E2AE` | Pastilles i resum «Aviat» |
| `fresh` | `#7E9B4C` | `#A6C277` | Punts i barres «Fresc» |
| `fresh-container` / `on-…` | `#DCE8C4` / `#2F4515` | `#2F3F1A` / `#DCE8C4` | Pastilles i resum «Frescos» |
| `frozen` | `#4F8FA3` | `#8CC3D4` | Punts, icona `ac_unit` |
| `frozen-container` / `on-…` | `#D3E5EA` / `#163F4C` | `#173E4A` / `#D3E5EA` | «Congela», resum «Congelats» |
| `scrim` | `rgba(41,36,28,.45)` | `rgba(0,0,0,.6)` | Darrere de fulls i diàlegs |
| `shadow-float` | `rgba(41,36,28,.18)` | `rgba(0,0,0,.5)` | Barra flotant i FAB |
| Snackbar (invers) | fons `ink`, text `surface` | fons `#F1EADC`, text `#29241C` | `inverseSurface` / `inverseOnSurface` |

## Regles específiques

- **Els colors d'estat no canvien amb el color dinàmic**, tampoc en fosc.
- **Ombres**: en fosc gairebé no es veuen; la barra flotant i el FAB es distingeixen pel color (`primary-container` / `primary`), no per l'ombra. No afegir elevació tonal extra.
- **Fulls inferiors** (Accions, Nova categoria ràpida): fons `surface` sobre `scrim`; les files de dins són `surface-card`, així queden separades del fons del full.
- **Barres de sistema**: icones clares en fosc (`SystemBarsAppearance` ja existent).
- **Icona i splash**: la icona A no canvia (mostassa + oliva). El fons del splash en fosc és `#1C1A15`.
- **Imatges i il·lustracions** (formes de l'estat buit, onboarding): les formes fan servir els `*-container` foscos.

## Contrast verificat (WCAG)

| Parell | Ràtio |
|---|---|
| `ink` / `surface` | 14.5 |
| `ink-variant` / `surface-card` | 9.1 |
| `on-primary` / `primary` | 7.9 |
| `on-primary-container` / `primary-container` | 7.7 |
| `on-expired-container` / `expired-container` | 9.4 |
| `on-soon-container` / `soon-container` | 8.5 |
| `on-fresh-container` / `fresh-container` | 8.9 |
| `on-frozen-container` / `frozen-container` | 8.9 |
| `expired` (text «Esborra») / `surface-card` | 7.0 |
| `on-expired` / `expired` | 6.1 |
| `outline` / `surface` (vores) | 5.6 |

Tots superen 4.5:1. El test de contrast de la fase 1 (que llegeix `tokens.json`) ha de cobrir també el tema fosc.

## Criteri d'acceptació (fase 11)

Comparar cada pantalla en fosc amb la seva captura de `captures-fosc/`. Per a les que no en tenen (Ajustos, Nova categoria, Calendari, onboarding, widget), comprovar que segueixen la taula de substitució i que no queda cap color clar «encallat» (fons crema o text oliva fosc).
