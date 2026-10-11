# 06 · Moviment

Usar el `MotionScheme.expressive()` de M3 (springs) com a base. Totes les animacions **es desactiven** quan el sistema té l'animació reduïda (`Settings.Global.ANIMATOR_DURATION_SCALE == 0` / `LocalReducedMotion` si existeix) — mostrar l'estat final.

## Onboarding (referència als `.dc.html`, que tenen les animacions CSS)

| Element | Animació | Durada / corba |
|---|---|---|
| Benvinguda · icona A | Escala 0.4 → 1.08 → 1 + fade | 700 ms, spring amb rebot |
| Benvinguda · Galeta de la icona | Rotació contínua | 28 s/volta, lineal |
| Benvinguda · Flor (dalt dreta) | Rotació contínua | 28 s/volta |
| Benvinguda · Sol (esquerra) | Rotació contínua inversa | 20 s/volta |
| Benvinguda · Trèvol | «Respira» escala 1 ↔ 1.08 | 4 s, ease-in-out, infinit |
| Benvinguda · títol / text / botó | Puja 24 dp + fade, escalonat | 600 ms, retards 250 / 400 / 550 ms |
| Passos · segment actual de la barra | `scaleX` 0 → 1 des de l'esquerra | 600 ms, retard 100 ms |
| Passos · títol | Puja 20 dp + fade | 520 ms, retard 120 ms |
| Categories · targetes | Puja + fade escalonat | 520 ms, retard 300 + 70·i ms |
| Avisos · notificació d'exemple | Cau des de dalt (−140 dp) amb rebot | 900 ms, retard 350 ms |
| Avisos · controls | Puja + fade | retard 480 ms |
| Privadesa · blocs | Puja + fade escalonat | retards 240 / 360 ms |
| Avís · requadre i casella | Puja + fade | retards 240 / 480 ms |
| Canvi de pas | Lliscament horitzontal (shared axis X) | spring per defecte |

## App

- Formes de categoria seleccionades (xips, onboarding): **morph** de forma/color amb `animateColorAsState` + escala 0.92 → 1.
- Fila consumida/llençada: col·lapse d'alçada + fade (`animateItem()` a `LazyColumn`).
- FAB → pantalla Afegeix: container transform (o shared bounds) si és viable; si no, fade through.
- Fulls inferiors: comportament estàndard de `ModalBottomSheet`.
- Snackbar «Desfés»: estàndard.
- Barra flotant: sempre visible, dibuixada per sobre del contingut (slot FAB de l'Scaffold pare, com a Converses); les pantalles deixen `LocalFloatingBarPadding` al final de les llistes (decidit 2026-10-11).
