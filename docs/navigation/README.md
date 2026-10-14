# Navegació comuna amb Converses

Caducity i Converses han de navegar igual. Aquest pla porta Caducity al comportament comú; el mirall és `docs/navigation/README.md` de Converses. Ordre acordat: primer Caducity (fase K), després Converses (fase C), cadascuna en una sessió.

Com es treballa cada fase (tasques, commits, validació): `.claude/rules/phased-work.md`.

## Problemes d'ara

- `TopLevelBackStack.backStack` posa les altres pestanyes **sota** la pestanya actual: a Home la pila és `[Pantry, Home]` (el test `starts on home` ho fixa). En fer el gest de back predictiu a Home es veu Pantry a sota, fins i tot en arrencar; `pop()` retorna `false` i es crida `activity.finish()`, i es perd l'animació del sistema. Si Pantry té pantalles obertes, a sota de Home es veu la darrera (p. ex. Category).
- La barra flotant (`AppShell`) apareix i desapareix de cop: `showNavigation` canvia quan canvia la pila, sense animació, i durant el gest predictiu no hi és.
- Transicions per defecte de `NavDisplay`; canviar de pestanya reordena la pila i probablement s'anima com un back.
- La pila es guarda amb `retain`: no sobreviu a la mort del procés.

## Comportament comú (decidit)

1. **Una sola pila desable** (`rememberNavBackStack`). La primera pestanya (Home) és la base.
2. **Les pestanyes no acumulen historial:** anar a Pantry deixa `[Home, Pantry]`; anar a Home deixa `[Home]`. Les pantalles secundàries s'apilen a sobre (`[Home, Pantry, Category]`). Ajustos és secundària, no pestanya.
3. **En canviar de pestanya es perd l'estat** de la que deixes (scroll, cerca, pantalles obertes a sobre). Decidit: són top level i tenen prioritat.
4. **Back:** treu la darrera entrada. A la base, amb una sola entrada, el back és del sistema (tanca l'app amb la seva animació; no surt res a sota).
5. **Barra fixa** en un `Scaffold` pare, fora de `NavDisplay` (Material 3 i la recepta de UI comuna de Navigation 3). Només visible quan la pantalla de dalt és una pestanya (els bottom sheets no compten), amb animació d'entrada i sortida. El FAB va dins la barra.
6. **Transicions de Converses:** `nativeTransition` i `predictiveBack`; variant vertical per a les pantalles modals (Add, Transform, editors); fos en canviar de pestanya.
7. **Espai per a la barra constant** (`LocalFloatingBarPadding`) perquè les llistes no saltin.

## Fase K · Caducity

- [x] **K1.** (`62d493d0`) Reescriure l'interior de `TopLevelBackStack` sobre un `NavBackStack` desable amb els punts 1–4, mantenint l'API (`navigate`, `switchTab`, `pop`, `resetTo`, `openFromOutside`); `currentTab` = darrera ruta top level de la pila. `App.kt` crea la pila amb `rememberNavBackStack` (Onboarding o Home com a arrel) en lloc de `retain`.
  Validació: reescriure `TopLevelBackStackTest` (`starts on home` → `[Home]`, canvis de pestanya, back, onboarding, `openFromOutside`); `testDebugUnitTest`, `ktlintCheck`; emulador: back a Home sense Pantry a sota i amb l'animació del sistema. Risc: si `NavBackStack` no es pot fer servir en un test JVM, testejar la lògica sobre una llista normal.
- [x] **K2.** (`6f2f487c`) Barra a l'`AppShell` amb `AnimatedVisibility` (lliscar avall i fondre) en lloc de mostrar-la o amagar-la de cop.
  Validació: screenshot tests; emulador obrint Category i tornant.
- [x] **K3.** (`09bc2b6a`, ajustat a `661e637e`: entrades opaques i fade final) Portar les transicions de Converses (`ui/AppNavigation.kt`): `nativeTransition`, `predictiveBack`, metadata vertical per a les modals i fos per a les pestanyes.
  Validació: emulador; Bernat per a la sensació d'ús.

Execució: en sèrie, a la sessió principal, sense agents. Un commit per tasca.

Release notes: sí, una línia (el back des de Home ja no mostra Pantry a sota i tanca l'app com cal). Proposar-la en preparar la versió.
