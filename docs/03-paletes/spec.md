# Paletes de color

Captures: `PaletaAjustos.png` (selector), `PaletaGuia.png` (totes les paletes), `PaletaIniciRebost.png`, `PaletaIniciNevera.png`, `PaletaIniciHort.png`, `PaletaIniciFleca.png`, `PaletaIniciBessa.png`, `PaletaIniciCamp.png` (l'Inici amb cada paleta en clar) i `PaletaFoscRebost.png`, `PaletaFoscNevera.png`, `PaletaFoscHort.png`, `PaletaFoscFleca.png`, `PaletaFoscBessa.png`, `PaletaFoscCamp.png` (en fosc). Fonts: els `.dc.html` homònims.

## Idea
L'usuari tria una **paleta** de la app, com a alternativa al color dinàmic del sistema. Una paleta canvia l'ambient (fons, textos, color principal), **mai el significat** dels estats.

## Paletes
| Id | Nom | Caràcter |
|---|---|---|
| `DYNAMIC` | Dinàmic | Del fons de pantalla (Android 12+). **Amagat si API < 31.** |
| `REBOST` | Rebost | Italià: alfàbrega i mozzarella. **Per defecte** |
| `NEVERA` | Nevera | Tons freds, blau-violeta gèlid |
| `HORT` | Hort | Verds vius, més fresc |
| `FLECA` | Fleca | Pa torrat i caramel |
| `BESSA` | Bessa | Fruits del bosc |
| `CAMP` | Camp | Blat i gira-sol |

**Nota:** el Rebost actual (oliva) canvia a aquest Rebost italià; és un canvi visual de la paleta per defecte.

## Què canvia i què no
- **Canvia** (12 tokens): `surface`, `surface-card`, `surface-high`, `surface-shape`, `ink`, `ink-variant`, `outline`, `outline-variant`, `primary`, `on-primary`, `primary-container`, `on-primary-container`.
- **No canvia mai** (tokens d'estat, iguals a totes les paletes i a clar/fosc segons `12-mode-fosc.md`): `expired` (vermell) i el seu contenidor, `soon` (groc), `fresh` (verd), `frozen` (blau). També el vermell de «Esborra»/«Llença».
- Cap paleta fa servir el vermell, el groc ni el blau d'estat com a color principal; així un producte es llegeix igual a totes.
- Les formes de categoria usen `surface-shape` i la icona `primary` de la paleta.

## Tokens (clar)
`on-primary` és **#FFFFFF** a totes.
| Token | Rebost | Nevera | Hort | Fleca | Bessa | Camp |
|---|---|---|---|---|---|---|
| surface | #FFFAEE | #F4F6FB | #F6FAEC | #FFF4EA | #FFF5F8 | #FFFBE6 |
| surface-card | #F7EEDA | #E9EDF6 | #EAF3D6 | #F9E8D8 | #F9E7EE | #FAF2CC |
| surface-high | #F0E2CB | #DFE5F1 | #DFEBC4 | #F1DBC6 | #F1DAE4 | #F3E8B4 |
| surface-shape | #EFD8C0 | #D5DCEB | #D2E4AE | #EAD0B6 | #EACDDA | #EBDD98 |
| ink | #2B2418 | #1D2433 | #1B2814 | #3A2216 | #2E1823 | #2E2A10 |
| ink-variant | #6A5D45 | #55607A | #4C6339 | #7A5644 | #6F4A5B | #6B6330 |
| outline | #8F8266 | #7C869C | #7A9065 | #A07C68 | #9B7686 | #948B52 |
| outline-variant | #E0D2B2 | #CDD4E3 | #C8DDA8 | #E6CDBA | #E4CDD7 | #E5DA9F |
| primary | #2E6B3B | #3E5C94 | #2E7D1F | #8A4B12 | #8A2E5C | #6F5A00 |
| primary-container | #DCE9C6 | #D9E2F5 | #D3EE9A | #F6DAB5 | #F5D5E4 | #F7E58A |
| on-primary-container | #10300F | #14264A | #12360A | #3B1F02 | #3A0A22 | #3A2F00 |

## Tokens (fosc)
| Token | Rebost | Nevera | Hort | Fleca | Bessa | Camp |
|---|---|---|---|---|---|---|
| surface | #1B1A14 | #14161E | #13170F | #1E1610 | #1C1318 | #1A1807 |
| surface-card | #25231B | #1D202B | #1C2116 | #2A1F17 | #281B22 | #25220E |
| surface-high | #2F2C22 | #272B38 | #262C1E | #352920 | #33242C | #302C14 |
| surface-shape | #3A3628 | #323747 | #313925 | #42342A | #402E38 | #3D381B |
| ink | #F0E9D6 | #E6E9F4 | #E7F0DA | #F6E8DA | #F4E4EB | #F2EDC8 |
| ink-variant | #C2B89C | #B2B8CC | #B5C4A0 | #CDB6A2 | #CDB1BE | #C9C195 |
| outline | #968C72 | #8A91A8 | #8A9877 | #9E8470 | #9B8090 | #988F5F |
| outline-variant | #4A4533 | #3E4457 | #3C4630 | #54433A | #52404A | #4B4524 |
| primary | #8FCB97 | #AFC3F2 | #8EDB74 | #F0B77C | #F0A2C4 | #F2D95C |
| on-primary | #0C3014 | #11264F | #0A3004 | #4A2600 | #4A0A2A | #3A3000 |
| primary-container | #1F4A2A | #2A3D68 | #254A18 | #6A3A0E | #6B1F46 | #5A4A00 |
| on-primary-container | #D5EBCF | #D9E2F8 | #D3EE9A | #F6DAB5 | #F5D5E4 | #F7E58A |

Contrast verificat (mínim): text sobre fons ≥ 13,6:1 (clar) / ≥ 14,4:1 (fosc); `ink-variant` sobre targeta ≥ 5,4:1 / ≥ 8:1; `on-primary` sobre `primary` ≥ 5,2:1 / ≥ 7,5:1; contingut sobre contenidor ≥ 10,4:1 / ≥ 6,8:1. **Cal re-verificar-ho amb un test** en cas de canviar un valor.

## Selector (Ajustos › Aparença)
Substitueix l'interruptor «Color dinàmic» per una targeta **«Colors»** (fila connectada, radi 6, entre «Tema» i «La setmana comença»).
- Capçalera: títol «Colors» + subtítol amb el nom de la paleta triada i el seu caràcter (p. ex. «Nevera · tons freds, net i serè»; per a Dinàmic, «Del fons de pantalla»).
- Graella de **3 columnes**, cada opció: mostra de **56 dp** + nom (13 sp, 700 si seleccionada).
- Mostra d'una paleta: **dues meitats**, `primary` a dalt i `primary-container` a baix. Dinàmic: degradat còniic multicolor + icona `wallpaper` en blanc. Les mostres sempre es dibuixen amb els colors de **la seva** paleta (no amb els de la triada).
- **Seleccionada:** forma arrodonida (radi 18) en lloc del cercle (radi 28) + ✓ blanc. No seleccionades: cercle.
- `radiogroup`; cada mostra és un `radio` amb nom («Nevera») i estat.
- En tocar, s'aplica a l'instant amb un fos entre colors (`animateColorAsState`, ~300 ms, *effects*), sense confirmació.
- «Tema» (Clar/Fosc/Sistema) continua independent: cada paleta té variant clara i fosca.

## Implementació (Compose)
1. `enum class ColorPalette { DYNAMIC, REBOST, NEVERA, HORT, FLECA, BESSA, CAMP }`, `AppSettings.colorPalette` (DataStore). **Migració**: l'antic «color dinàmic» activat → `DYNAMIC`; desactivat o sense valor → `REBOST`. Elimina l'interruptor antic.
2. Un `CaducityColors` (immutable) per paleta i variant (clar/fosc) amb els 12 tokens, i `CaducityTheme(palette, darkTheme)` que els exposa pel `MaterialTheme.colorScheme` i pels *composition locals* del design system.
3. Els **tokens d'estat** viuen en un `StateColors` independent de la paleta; `DYNAMIC` també els conserva (només substitueix els 12 tokens pel `dynamicColorScheme`).
4. Les mostres del selector llegeixen directament de `CaducityColors` de cada paleta.
5. **Widget** (Glance) i **Tile de Wear**: segueixen la paleta triada (Dinàmic: el color dinàmic). L'**accent de la notificació** és el `primary` de la paleta.
6. Previews: cada pantalla clau (Inici, Rebost, Ajustos) en les 6 paletes × clar/fosc.

## Textos
| Clau | Text |
|---|---|
| `settings_colors` | Colors |
| `palette_dynamic` | Dinàmic |
| `palette_dynamic_help` | Del fons de pantalla |
| `palette_rebost` | Rebost |
| `palette_rebost_help` | alfàbrega i mozzarella |
| `palette_nevera` | Nevera |
| `palette_nevera_help` | tons freds, net i serè |
| `palette_hort` | Hort |
| `palette_hort_help` | verds vius, més fresc |
| `palette_fleca` | Fleca |
| `palette_fleca_help` | pa torrat i caramel |
| `palette_bessa` | Bessa |
| `palette_bessa_help` | fruits del bosc |
| `palette_camp` | Camp |
| `palette_camp_help` | blat i gira-sol |

## Obert
1. **Camp** té un groc proper al d'«Aviat»: es distingeix per l'ataronjat, la icona i el text, però cal vigilar-ho visualment.
2. **Hort**: el contenidor llima s'assembla al verd de «Frescos».
3. Accent de tomàquet al Rebost (decoratiu), descartat de moment pel vermell de «Caducat».
4. Si es vol una tria **personalitzada** (color propi) més endavant: fora d'abast.
