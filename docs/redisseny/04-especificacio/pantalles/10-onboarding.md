# Onboarding

Captures: `OB1Benvinguda.png` … `OB5Avis.png`, `InicibBuit.png`. Fonts: `OB*.dc.html`.
Substitueix `01-app-original/onboarding/01..10` (6 pàgines → benvinguda + 4 passos).

Recomanat: un `HorizontalPager` sense gest de lliscar lliure (avança amb botons) o una navegació per passos. **No hi ha Skip**: l'avís legal és obligatori.

## Comú als passos 1–4
- Overline «Pas N de 4» + **barra de progrés segmentada** (4 segments de 6 dp, gap 4, radi 3; fets `primary`, pendents `outline-variant`). El segment actual s'omple animat.
- Títol `Bricolage 36/800`, text `body` en `ink-variant`.
- A baix: «Enrere» (text, `primary`) a l'esquerra i botó principal (60 dp, pastilla, `primary`) a la dreta.

## 0 · Benvinguda (`OB1Benvinguda.png`)
- Formes decoratives animades: Flor `fresh-container` (dalt dreta, gira lent), Sol `soon-container` (esquerra, gira lent invers), Trèvol `expired-container` (respira).
- Icona A 96 dp · títol `Bricolage 48/800` «Sàpigues què caduca abans que passi» · «Apunta el que compres i Caducity t'avisa quan s'acosta la data. Menys menjar a les escombraries.»
- Botó a tota amplada **Comença →**.

## 1 · Categories (`OB2Categories.png`)
- «Què guardes a casa?» · «Tria les categories amb què vols començar. Les pots canviar quan vulguis des de Rebost.»
- Graella 2 columnes de **targetes casella** (104 dp, radi 24): forma 44 + icona + nom, `check_circle` / `radio_button_unchecked` a dalt a la dreta. Marcada: `primary-container` + vora 2 `primary`, forma `primary`. Vegeu categories i valors per defecte a `04-model-de-dades.md`.
- Botó dinàmic **«Continua amb N»**. Si N = 0: «Continua sense categories» (permès).

## 2 · Avisos (`OB3Avisos.png`)
- «Quan t'avisem?» · «Un sol avís al dia amb el que caduca aviat. Res més.»
- **Notificació d'exemple** (targeta `surface-card` radi 24 amb icona A petita, «Caducity · 9:00», «3 productes caduquen aviat», «Llet sencera avui, Mozzarella demà…»). Entra caient des de dalt.
- Grup «Avisa'm / Abans que caduqui»: **1 dia / 3 dies / 1 setmana** (per defecte 3) · fila «Hora / Cada dia» amb botó `9:00` (obre `TimePicker`).
- Botó a tota amplada **Activa els avisos** (`notifications_active`) → demana `POST_NOTIFICATIONS`; en tornar (concedit o no) avança. A sota: «Enrere» i «Ara no» (avança sense demanar permís; `dailyReminderEnabled=false`).
- Si el permís ja està concedit: el botó diu **Continua** i mostra «Avisos activats» amb `check_circle`.

## 3 · Privadesa (`OB4Privadesa.png`)
- «El teu rebost és teu» · «Els teus productes es guarden només en aquest mòbil. No cal compte ni registre.»
- Llista connectada: `smartphone` «Les dades no surten del mòbil» · `cloud_upload` «Pots fer-ne còpia i exportar-les des d'Ajustos».
- Targeta amb interruptor **Estadístiques d'ús anònimes** · «Quines pantalles fas servir, mai què guardes. Ens ajuda a millorar l'app.» — **off per defecte**.
- Nota: «Desactivat per defecte. Ho pots canviar a Ajustos › Privadesa.»

## 4 · Avís important (`OB5Avis.png`)
- «Abans de començar».
- Requadre `soon-container` / `on-soon-container`, radi 24, amb `warning` «Avís important» i el text complet (vegeu `05-textos.md`), més l'enllaç «Llegeix les condicions d'ús completes».
- **Casella** «He llegit l'avís i entenc que l'app no garanteix que un aliment sigui segur per menjar.» — **desmarcada per defecte**.
- Botó **Accepto i comença** (`check`): **desactivat** fins que es marca la casella; mentre està desactivat, a sota de la casella «Marca la casella per continuar.».
- En acceptar: desa `disclaimerAcceptedVersion` i `disclaimerAcceptedAt`, crea les categories triades, desa les preferències d'avisos/analítiques, `onboardingCompleted=true` i navega a `home` (estat buit).

## Després
`InicibBuit.png` — vegeu `01-inici.md`.
