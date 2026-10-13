# Icona de l'app per paleta (clar i fosc)

Captura: `IconaPaletes.png`. Font: `IconaPaletes.dc.html`. Parteix de la proposta A de `09-icona-splash-widget.md` (Galeta + fulla); aquí només canvien els colors.

## Regla
- La icona **segueix la paleta triada** (`18-paletes-de-color.md`). **No hi ha cap opció a Ajustos** per triar-la a mà (de moment).
- **Dinàmic** → icona de **Rebost**.
- Els **temes de temporada** (si s'afegeixen) **no** canvien la icona.
- Cada paleta té variant **clara** (`drawable`) i **fosca** (`drawable-night`).

## Colors (adaptive icon: fons · Galeta · fulla)
| Paleta | Clar: fons | Galeta | Fulla | Fosc: fons | Galeta | Fulla |
|---|---|---|---|---|---|---|
| Rebost | `#E8B95A` | `#2E6B3B` | `#FFFAEE` | `#1F4A2A` | `#8FCB97` | `#1B1A14` |
| Nevera | `#D9E2F5` | `#3E5C94` | `#F4F6FB` | `#2A3D68` | `#AFC3F2` | `#14161E` |
| Hort | `#D3EE9A` | `#2E7D1F` | `#F6FAEC` | `#254A18` | `#8EDB74` | `#13170F` |
| Fleca | `#F6DAB5` | `#8A4B12` | `#FFF4EA` | `#6A3A0E` | `#F0B77C` | `#1E1610` |
| Bessa | `#F5D5E4` | `#8A2E5C` | `#FFF5F8` | `#6B1F46` | `#F0A2C4` | `#1C1318` |
| Camp | `#F7E58A` | `#6F5A00` | `#FFFBE6` | `#5A4A00` | `#F2D95C` | `#1A1807` |

Regla de derivació (per si s'afegeix una paleta): clar = fons `primary-container` (Rebost, per tradició, mostassa) · Galeta `primary` · fulla `surface`; fosc = fons `primary-container` fosc · Galeta `primary` fosc · fulla `surface` fosc. Els valors d'ambient surten de `tokens.json`.

## Implementació (Android)
- Un **`<activity-alias>`** per paleta (`.IconRebost`, `.IconNevera`, `.IconHort`, `.IconFleca`, `.IconBessa`, `.IconCamp`) a `AndroidManifest.xml`, cadascun amb el seu `android:icon` i `android:roundIcon`. Només un està habilitat; **Rebost per defecte**.
- Recursos per paleta: `ic_launcher_<paleta>.xml` (`<adaptive-icon>` amb `background` i `foreground`) i la **monocroma** (`<monochrome>`) compartida. Colors a `res/values/` (clar) i `res/values-night/` (fosc). Fosc a `drawable-night` si són vectors.
- En canviar de paleta (setting `ColorPalette`): `PackageManager.setComponentEnabledSetting(alias, STATE_ENABLED, DONT_KILL_APP)` per al nou i `STATE_DISABLED` per a la resta. Fer-ho **després** de desar el setting i, si cal, quan l'app passa a segon pla.
- Dinàmic = Rebost. Debug: la franja `DEV` s'aplica a cada alias.
- El **splash** continua usant la icona de la paleta triada (`windowSplashScreenAnimatedIcon`); si resulta complicat, deixa Rebost i avisa.

## Limitacions
- El llançador decideix quan refresca la icona (pot trigar uns segons) i alguns llançadors tanquen l'app en canviar d'alias (per això `DONT_KILL_APP`, que no sempre és respectat).
- La **variant fosca** (`-night`) només la respecten alguns llançadors; la resta mostren la clara. No és un error.
- Els accessos directes fixats a la pantalla d'inici poden mantenir la icona antiga.

## Obert
1. Fer la versió **vectorial definitiva** de la Galeta + fulla sobre la retícula de 108 dp (el símbol del mockup és provisional, igual que a `09`).
2. Si es vol una opció «Icona de l'app: segueix la paleta / sempre Rebost» a Ajustos (no decidit; **no** implementar-la ara).
