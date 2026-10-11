# 01 · Navegació i arquitectura d'informació

## Abans

```
Splash → Dashboard (targetes d'estat + calendari setmanal per categoria)
            ├── targeta d'estat → Ítems filtrats per estat
            ├── categoria → Detall (pestanyes per producte) → ítem → Bottom sheet accions → DatePicker
            ├── FAB Add → Crear categoria
            └── engranatge → Settings → Appearance / Notifications / Privacy / Backup / Debug / About
```

## Després

```
Splash
 └── (primer cop) Onboarding: Benvinguda → Categories → Avisos → Privadesa → Avís important
 └── Shell principal amb barra flotant [Aviat · Rebost · Ajustos] + FAB «Afegeix»
       ├── Aviat (Inici «Què caduca»)              ← destinació inicial
       │     ├── bàner caducats → Caducats
       │     ├── resum Aviat/Frescos/Congelats → Caducats / Rebost (filtrat)
       │     └── fila → full Accions del producte
       ├── Rebost (graella de categories)
       │     ├── targeta categoria → Categoria
       │     │     └── fila / unitat → full Accions del producte
       │     └── «Nova categoria» → Nova categoria (pantalla completa)
       └── Ajustos
 FAB «Afegeix» (Aviat, Rebost, Categoria) → Afegeix (pantalla completa)
       ├── xip «Nova» → full Nova categoria (ràpida)
       └── «Desa i un altre» → mateix formulari, mode tanda
```

## Barra de navegació

- `HorizontalFloatingToolbar` (M3 Expressive) amb fons `primary-container`, flotant a 24 dp del límit inferior i 20 dp dels laterals, `shadow-float`.
- 3 destinacions: **Aviat** (`hourglass_bottom`), **Rebost** (`kitchen`), **Ajustos** (`settings`). La destinació activa es mostra com a pastilla `primary` amb icona farcida + etiqueta; les altres, només icona (amb `contentDescription`).
- A la dreta, separat, **FAB** de 64 dp, radi 20, `primary`, icona `add` (veu: «Afegeix un producte»).
- Pantalles secundàries (Categoria, Caducats, Afegeix, Nova categoria) no mostren la barra; la pantalla Categoria mostra un **FAB estès** «Afegeix» que preselecciona la categoria.

## Rutes suggerides

| Ruta | Pantalla | Arguments |
|---|---|---|
| `onboarding` | Onboarding (5 passos dins un pager) | — |
| `home` | Inici «Què caduca» | — |
| `pantry` | Rebost | — |
| `category/{id}` | Categoria | `filter?` (estat) |
| `expired` | Caducats | — |
| `add` | Afegeix | `categoryId?`, `source?` (`widget`, `shortcut`, `app`) |
| `category/new` | Nova categoria | — |
| `settings` | Ajustos | — |
| (sheet) | Accions del producte | `itemId` |
| (sheet) | Nova categoria ràpida | — (retorna la categoria creada a Afegeix) |

## Entrades externes

- **Widget** (Glance): mostra «N caduquen aviat», pastilla «N caducats», les 2 properes caducitats i botó **Afegeix** → `add?source=widget`. Tocar el cos del widget obre `home`.
- **Dreceres de la icona** (static/dynamic shortcuts): «Afegeix producte» → `add`, «Què caduca» → `home`, «Caducats» → `expired`.
- **Notificació diària**: obre `home`.
