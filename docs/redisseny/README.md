# Caducity — Paquet de redisseny (Material 3 Expressive)

Aquest paquet descriu el redisseny complet de **Caducity**, l'app Android de seguiment de caducitats (Jetpack Compose). Està pensat perquè **Claude Code**, amb accés al codi de l'app, entengui què ha de canviar i en generi un pla de migració.

> Llegeix-lo en aquest ordre. Els documents de `04-especificacio/` són la font de veritat; les captures són la referència visual.

## Contingut

| Carpeta | Què hi ha |
|---|---|
| `01-app-original/` | Captures de l'app actual (`app/`) i de l'onboarding actual (`onboarding/`), amb els README originals que expliquen cada pantalla. |
| `02-disseny-nou/captures/` | Captures PNG (@2x) de cada pantalla nova. |
| `02-disseny-nou/font-dc-html/` | Font HTML de cada pantalla del canvas de disseny. Útil per extreure mides, colors, radis i textos exactes. `canvas.json` és l'índex. |
| `03-design-system/` | Design system **Caducity**: `README.md` (principis, estats, formes, patrons) i `tokens.json` (colors clar/fosc, tipografia, espaiat, radis, ombra). |
| `04-especificacio/` | Especificació funcional i visual per implementar. |
| `PROMPT-CLAUDE-CODE.md` | Prompt suggerit per demanar a Claude Code el pla de migració. |

## Especificació (`04-especificacio/`)

1. `00-resum-i-principis.md` — què canvia i per què, principis de disseny.
2. `01-navegacio.md` — nova arquitectura d'informació, rutes, widget i dreceres.
3. `02-migracio-pantalles.md` — taula pantalla original → pantalla nova (què es manté, què canvia, què desapareix).
4. `03-design-system-compose.md` — com traduir els tokens a `MaterialTheme` (Color, Type, Shapes, MaterialShapes).
5. `pantalles/*.md` — una fitxa per pantalla: estructura, components, estats, interaccions, textos.
6. `04-model-de-dades.md` — canvis de dades i preferències que el disseny necessita.
7. `05-textos.md` — tots els textos nous en català, amb claus suggerides.
8. `06-moviment.md` — animacions i transicions.
9. `07-accessibilitat.md` — requisits d'accessibilitat.
10. `08-decisions-i-obertes.md` — decisions preses i preguntes pendents.

## Restriccions clau (no negociables)

- **Res d'IA, veu ni reconeixement d'imatge/codi de barres** en aquesta fase. Tota entrada és manual.
- Per desar un producte calen **nom, categoria i data**.
- L'**avís legal** s'ha d'acceptar explícitament (casella **desmarcada** per defecte; botó desactivat fins marcar-la).
- **Analítiques desactivades** per defecte.
- Icona de l'app: **proposta A** (Galeta oliva + fulla crema sobre fons mostassa).

## Canvas de disseny

El disseny viu en un canvas a claude.ai («Caducity · Redisseny») i el design system en un artifact «Caducity». Aquest paquet n'és una còpia estàtica a data **2026-10-11**.
