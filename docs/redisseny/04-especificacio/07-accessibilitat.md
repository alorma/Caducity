# 07 · Accessibilitat

- **Contrast**: text normal ≥ 4.5:1, text ≥ 24 sp ≥ 3:1, en clar i fosc. Els parells `*-container` / `on-*-container` dels tokens compleixen; **no** posar text sobre els colors forts `soon` i `fresh`.
- **L'estat mai només amb color**: sempre amb text («Caducat», «Avui», «Fa 2 dies»). La barra d'estats de les targetes de Rebost és decorativa (`clearAndSetSemantics` o descripció textual al conjunt de la targeta).
- **Àrees tàctils ≥ 48 dp** (els botons rodons de 44 dp necessiten `minimumInteractiveComponentSize`).
- Botons només-icona amb `contentDescription` (Cerca, Torna, Més opcions, Marca com a consumit, Canvia la data, Llença, Tria una data, Obre el calendari, Esborra el nom, Una menys, Una més).
- Grups de selecció única (filtres, Tema, llindar, categoria a Afegeix, forma, icona) amb semàntica de `selectableGroup` / `Role.RadioButton`; targetes de l'onboarding amb `Role.Checkbox`; interruptors amb `Role.Switch`.
- Barra de progrés de l'onboarding amb `progressSemantics` («Pas 2 de 4»).
- Camp de data: anunciar el text d'ajuda («dimecres, d'aquí 10 dies») com a `supportingText` / `liveRegion`.
- Snackbars amb acció «Desfés» accessible amb TalkBack.
- Suport de mida de font gran: les files han de créixer en alçada, no tallar text (els noms poden anar a 2 línies).
