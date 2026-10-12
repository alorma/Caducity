# Caducity — Paquet d'imatges per a Google Play

Paquet per a **Claude Code**: ha de generar les imatges finals de la fitxa de Google Play (icona, feature graphic i captures) a partir de l'app real.

## Contingut

| Fitxer / carpeta | Què és |
|---|---|
| `PROMPT-CLAUDE-CODE.md` | Prompt per enganxar a Claude Code. Comença per aquí. |
| `01-especificacio.md` | Mides, requisits de Play, composició exacta de cada imatge, textos i dades de mostra. |
| `reference/` | Font HTML del canvas de disseny (`*.dc.html`), `canvas.json` (posicions i mides) i `tokens.json` (design system Caducity). Només com a referència de mides, colors i textos. |

## Nota sobre `reference/`

Els `.dc.html` són fitxers del format del canvas de disseny: **no s'obren directament al navegador** (necessiten el seu runtime). Llegeix-los com a codi font per treure'n posicions, colors i textos; no cal renderitzar-los.

Mapa dels fitxers:

- `Main.dc.html` → icona 512×512
- `Feature.dc.html`, `FeatureEs`, `FeatureEn` → feature graphic 1024×500 (ca, es, en)
- `Play1…Play7.dc.html` (català), `Play1…7Es` (castellà), `Play1…7En` (anglès) → captures 360×640 (s'exporten a ×3 → 1080×1920)
- `Cd*.dc.html` → pantalles de l'app (mockups del redisseny) que les captures incrusten

## Avís

Les captures del canvas són **mockups del redisseny**. Les imatges finals de Play han de sortir de l'app real (Google Play ho exigeix). No es publiquen fins que el redisseny estigui en producció.
