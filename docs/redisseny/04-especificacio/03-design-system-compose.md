# 03 · Design system a Compose

Font de veritat: `03-design-system/tokens.json` i `03-design-system/README.md`. Aquí hi ha la traducció recomanada a Compose.

## Tema

- `MaterialExpressiveTheme` (o `MaterialTheme` amb `MotionScheme.expressive()` si la versió de M3 ho permet).
- Dos esquemes: clar (per defecte) i fosc, amb els valors de `tokens.json`.
- **Color dinàmic**: opcional, **desactivat per defecte**. Si s'activa, només substitueix superfícies i `primary*`; els **colors d'estat no canvien mai** (són semàntics).
- Elimina la preferència **Tone (Vibrant/Soft)** i qualsevol paleta associada.

## Colors

Mapatge a `ColorScheme`:

| Token | Rol M3 |
|---|---|
| `surface` | `background`, `surface` |
| `surface-card` | `surfaceContainer` (files, targetes) |
| `surface-high` | `surfaceContainerHigh` (camps, botons no seleccionats) |
| `ink` | `onBackground`, `onSurface` |
| `ink-variant` | `onSurfaceVariant` |
| `outline` / `outline-variant` | `outline` / `outlineVariant` |
| `primary` / `on-primary` | `primary` / `onPrimary` |
| `primary-container` / `on-primary-container` | `primaryContainer` / `onPrimaryContainer` |
| `expired` / `on-expired` / `expired-container` / `on-expired-container` | `error` / `onError` / `errorContainer` / `onErrorContainer` |
| `scrim` | `scrim` |

Colors que **no** caben a `ColorScheme` → crear un `CaducityColors` amb `staticCompositionLocalOf`:

```kotlin
@Immutable
data class StatusColors(val strong: Color, val container: Color, val onContainer: Color)

@Immutable
data class CaducityColors(
    val surfaceShape: Color,      // farcit de la forma de categoria
    val expired: StatusColors,
    val soon: StatusColors,
    val fresh: StatusColors,
    val frozen: StatusColors,
)
```

Valors clar / fosc: `tokens.json` (`expired`, `soon`, `fresh`, `frozen` i els seus `-container` / `on-*-container`; `surface-shape`).

Recomanat: un `enum class ExpiryStatus { EXPIRED, SOON, FRESH, FROZEN }` amb una extensió `@Composable fun ExpiryStatus.colors(): StatusColors`.

## Tipografia

Fonts (Google Fonts, descarregables com a fonts del projecte o via `GoogleFont` provider):
- **Bricolage Grotesque** (variable: `wght` 200–800, `wdth` 75–100, `opsz` 12–96) per a títols i xifres, amb `wdth` ≈ 80 (`FontVariation.width(80f)`).
- **Figtree** (variable `wght` 300–900) per a la resta.

| Estil | Família | Mida / interlineat | Pes | Ús | → `Typography` |
|---|---|---|---|---|---|
| `display` | Bricolage | 44/44 | 800 | Títols de pestanya (Què caduca, Rebost, Caducats) | `displaySmall` |
| `screen-title` | Bricolage | 40/42 | 800 | Ajustos, Categoria | `headlineLarge` |
| `onboarding-title` | Bricolage | 36/36 | 800 | Títols d'onboarding | `headlineMedium` |
| `number` | Bricolage | 30/30 | 800 | Recomptes del resum | (estil propi) |
| `sheet-title` | Bricolage | 24/28 | 700 | Títol de fulls i d'Afegeix | `headlineSmall` |
| `section-title` | Bricolage | 22/28 | 700 | Avui, Demà… | `titleLarge` |
| `card-title` | Bricolage | 20/22 | 700 | Nom de categoria a la targeta | `titleMedium` (propi) |
| `body-large` | Figtree | 17/24 | 700 | Text destacat de bàners i botons grans | `bodyLarge` |
| `title-row` | Figtree | 16/22 | 600 | Nom de producte a les files | `titleSmall` |
| `body` | Figtree | 15/22 | 400 | Text general | `bodyMedium` |
| `label` | Figtree | 14/20 | 700 | Botons, xips | `labelLarge` |
| `body-small` | Figtree | 13/18 | 400 | Metadades | `bodySmall` |
| `overline` | Figtree | 12/16 | 700, majúscules, +1.2 sp | Etiquetes de secció i de camp | `labelSmall` |

## Formes

| Token | Valor | Ús |
|---|---|---|
| `radius-inner` | 6 dp | Cantonades interiors de llistes connectades |
| `radius-joint` | 8 dp | Cantonades interiors de grups de botons connectats |
| `radius-chip` | 12 dp | Xips, pastilles d'estat, dates ràpides |
| `radius-field` | 16 dp | Camps de text, suggeriments, caselles |
| `radius-fab` | 20 dp | FAB |
| `radius-group` | 24 dp | Exterior de llistes i bàners |
| `radius-card` | 28 dp | Targetes de categoria |
| `radius-sheet` | 32 dp | Fulls inferiors, barra flotant, capçalera Caducats |
| `radius-full` | 50% | Botons, cercador, interruptors |

### Llistes connectades
Separació **2 dp**. Primer element: `RoundedCornerShape(24, 24, 6, 6)`; mig: `6`; últim: `(6, 6, 24, 24)`; únic: `24`. Fes-ne un helper `connectedShape(index, count)`.

### Grups de botons connectats
Separació **3 dp**. Extrems `radius-full`, interiors `radius-joint` (8). Usar `ButtonGroup` d'M3 Expressive si està disponible.

### Formes de categoria (`MaterialShapes`)

| Nom (UI) | `MaterialShapes` | Categoria per defecte |
|---|---|---|
| Galeta | `Cookie9Sided` | Làctics |
| Flor | `Flower` | Fruita i verdura |
| Trèvol | `Clover4Leaf` | Carn i peix |
| Sol | `Sunny` | Rebost |
| Coixí | `Puffy` (o `Cookie4Sided`) | Fleca |
| Segell | `SoftBurst` (o `Cookie12Sided`) | Begudes / noves |

Forma farcida amb `surface-shape`, icona a sobre en `primary`. **Seleccionada**: forma `primary`, icona `on-primary`.
Mides: 28 (capçalera de grup a Caducats), 34–36 (xips), 44 (files), 56 (targetes i fulls), 64–80 (capçaleres), 120 (Nova categoria).

## Ombres

Només `shadow-float` (0 6 20 rgba(41,36,28,.18)) a la barra flotant, el FAB i el menú de dreceres. Res més té elevació.

## Icones

Material Symbols **Rounded**, 24 dp (18–22 dins de xips i files). Contorn per defecte; **farcit** per a la destinació activa, `check_circle` seleccionat i icones de l'app.
