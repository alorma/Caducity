# Caducity — captures dels fluxos principals

Build `debug` (`com.alorma.caducity.dev`) sobre l'emulador Pixel 9, amb dades de demostració.

| # | Captura | Què representa |
|---|---------|----------------|
| 00 | ![](00_splash.png) | **Splash screen.** Pantalla d'arrencada en fred, amb la icona de l'app (full amb una espurna, en negre sobre un cercle vermell). |
| 01 | ![](01_dashboard.png) | **Dashboard.** Pantalla principal: targetes resum per estat (Expired, Expiring, Fresh, Frozen) i, sota, un calendari setmanal per categoria (Dairy, Produce, Meat & Seafood, Pantry…). El botó **Add** permet crear una categoria i la roda de configuració porta a Settings. |
| 02 | ![](02_category_detail.png) | **Detall de categoria (Dairy).** Calendari de la setmana, pestanyes per producte (Cheddar Cheese, Greek Yogurt, Whole Milk…) i els ítems agrupats per data de caducitat, amb el seu estat. Botó **Add item** a baix. |
| 03 | ![](03_item_actions.png) | **Accions d'un ítem.** Bottom sheet que s'obre en tocar un ítem: Consume, Freeze, Reschedule i Delete. |
| 04 | ![](04_reschedule_datepicker.png) | **Reschedule.** Selector de data per canviar la caducitat de l'ítem (Cancel / OK). Es va cancel·lar, no s'ha canviat cap dada. |
| 05 | ![](05_filtered_expired.png) | **Ítems filtrats per estat.** Es mostra en tocar una targeta del dashboard (aquí, *Expired Items*): llista agrupada per categoria amb els productes i el nombre d'ítems. |
| 06 | ![](06_create_category.png) | **Crear categoria.** Formulari amb nom i descripció, i botons Cancel / Create. |
| 07 | ![](07_settings.png) | **Settings.** Seccions Appearance, Notifications, Privacy, Backup, Debug i About. |
| 08 | ![](08_settings_appearance.png) | **Settings › Appearance.** Tema (Light / Dark / System), Dynamic Colors, to (Vibrant / Soft) i primer dia de la setmana. |

## Flux resumit

`Splash → Dashboard → (targeta d'estat → Ítems filtrats) | (categoria → Detall → ítem → Accions → Reschedule) | Add → Crear categoria | Settings → Appearance`
