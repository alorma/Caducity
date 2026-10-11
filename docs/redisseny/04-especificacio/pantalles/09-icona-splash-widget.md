# Icona, splash, widget i dreceres

Captures: `Icona.png`, `Splash.png`, `Accessos.png`. Fonts: `Icona.dc.html`, `Splash.dc.html`, `Accessos.dc.html`.

## Icona de l'app — proposta A (triada)

- **Icona adaptativa**: fons sòlid **mostassa `#E8B95A`** (recupera el groc de producció) · primer pla: forma **Galeta** (`Cookie9Sided`) en **oliva `#4D5A1E`** ocupant ~2/3 de la zona segura, amb una **fulla** (`eco`, farcida) en **crema `#FBF6EC`** al centre.
- **Monocroma (temàtica)**: Galeta + fulla en una sola tinta (la fulla com a forat), per a Material You themed icons.
- **Debug**: la mateixa icona amb una **franja inferior `expired` amb «DEV»** en blanc. Deixar de canviar el fons a vermell.
- Cal fer la versió vectorial definitiva (VectorDrawable) sobre la retícula de 108 dp; el símbol de Material Symbols del mockup és provisional.

## Splash (`SplashScreen` API)
Fons `surface` (#FBF6EC); icona A centrada (fons mostassa circular). Opcional: wordmark «Caducity» (Bricolage 800, `primary`) a 64 dp de baix com a branding image.

## Widget (Glance, 4×2 aprox.)
Fons `surface`, radi 28, padding 18.
- Overline «CADUCITY» + «N caduquen aviat» (Bricolage 28/800) · pastilla «N caducats» (`expired-container`) si n'hi ha.
- 2 files connectades amb els propers productes: nom + «Avui/Demà/data» en `on-soon-container`.
- Botó gran **Afegeix** (`primary`, 56 dp, pastilla) → `add?source=widget`.
- Tocar la resta → `home`.
- Estat buit: «Res caduca aviat» + botó Afegeix.

## Dreceres de la icona (long-press)
Ordre: **Afegeix producte** (`add`, destacat) · **Què caduca** (`hourglass_bottom`) · **Caducats** (`delete_history`).
