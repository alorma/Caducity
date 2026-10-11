# 08 · Decisions i temes oberts

## Decisions preses (amb el propietari del producte)

| Tema | Decisió |
|---|---|
| Design system | Nou i propi («Caducity»), no el de l'app Converses. |
| Personalitat | Càlida i alimentària (crema, oliva, tons de rebost, formes orgàniques). |
| Pantalla principal | «Què caduca» (llista per urgència). Categories a la pestanya Rebost. |
| Jerarquia de dades | Es manté Category → Product → Item; la UI amaga el nivell producte quan hi ha una sola unitat. |
| Idioma dels mockups | Català. |
| Entrada de dades | Només manual. **Sense IA, veu, codi de barres ni OCR** en aquesta fase. |
| Camps obligatoris | Nom, categoria i data. |
| Accessos ràpids | Widget a la pantalla d'inici + dreceres de la icona + dins l'app. |
| Al súper | S'afegeix directament al rebost amb la data real de l'envàs (no hi ha llista de la compra). |
| Producte nou / categoria nova | Avís «Producte nou», categoria obligatòria i xip «Nova» que obre un full ràpid sense sortir del flux. |
| Icona | Proposta A: Galeta oliva + fulla crema sobre mostassa. Debug amb franja «DEV». |
| Onboarding | Benvinguda + 4 passos: categories suggerides, avisos (llindar + hora + permís), privadesa, avís legal. Animat. |
| Avís legal | Acceptació explícita: casella desmarcada per defecte i botó desactivat fins marcar-la. Desar versió i data. |
| Analítiques | Off per defecte. |
| Tone Vibrant/Soft | Eliminat. |
| Debug a Ajustos | Només en build debug. |

## Temes oberts

1. **Calendari.** El nou disseny ha tret el calendari setmanal per categoria. Es vol recuperar alguna vista de calendari més senzilla (p. ex. una tira de la setmana a l'Inici amb punts d'estat per dia i/o una vista de mes amb punts). **Pendent de disseny** — no implementar fins tenir-lo; deixar l'Inici preparat per afegir-hi una capçalera.
2. **«Llença» vs «Esborra»**: decidir si `DISCARDED` es desa (per estadístiques futures de malbaratament) o equival a esborrar en aquesta fase.
3. **Mode fosc**: tokens definits, sense mockups. Implementar i revisar visualment.
4. **Tauleta / pantalles grans**: fora d'abast; no trencar-les (màxim d'amplada o `ListDetailPaneScaffold` més endavant).
5. **Text legal**: revisió per part d'algú de l'àmbit legal.
6. **Cerca global** (botó de l'Inici): comportament concret per definir (pot obrir Rebost amb el cercador enfocat).
7. **Ordenar categories** (arrossegar a Rebost): fora d'abast.
8. **Icona vectorial definitiva**: cal dibuixar-la sobre la retícula d'icona adaptativa.
