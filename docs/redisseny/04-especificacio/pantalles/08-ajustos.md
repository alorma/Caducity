# Ajustos

Captura: `Ajustos.png`. Font: `Ajustos.dc.html`. Substitueix `app/07_settings.png` i `app/08_settings_appearance.png`.

## Estructura (llistes connectades, overline per secció)

1. `arrow_back` + títol `screen-title` «Ajustos» (pot ser `LargeFlexibleTopAppBar` que es plega).
2. **Avisos**
   - Notificacions (`notifications`) · «Cada dia a les 9:00» · interruptor. Tocar el text obre el selector d'hora.
   - «"Aviat" per defecte» · «Per a les categories sense marge propi» · grup connectat **1 dia / 3 dies / 1 setmana** (seleccionat `primary` amb `check`).
   - **Marge per categoria** (`tune`) · resum d'una línia · `chevron_right` → pantalla «Marge per categoria». Vegeu `14-marge-per-categoria.md` i `MargeAjustos.png`.
3. **Aparença**
   - Tema: **Clar / Fosc / Sistema** (grup connectat).
   - Color dinàmic · «Desactivat: paleta Caducity» · interruptor (off per defecte).
   - La setmana comença · «Dilluns» · `chevron_right` (Dilluns / Dissabte / Diumenge).
4. **Dades**
   - Còpia de seguretat (`cloud_upload`) · «Exporta i restaura».
   - Privadesa (`shield`) → pantalla amb l'interruptor d'estadístiques anònimes i el text de l'avís legal acceptat (amb data).
   - Quant a Caducity (`info`) · «Versió i crèdits».

## Eliminat / mogut
- **Tone (Vibrant/Soft)**: eliminat.
- **Debug**: només visible en build `debug` (p. ex. una fila més a Dades, o un gest ocult).

## Interruptor (M3)
52×32. On: pista `primary`, polze blanc 24 amb `check`. Off: pista `surface-high`, vora 2 `outline`, polze 16 `outline`.
