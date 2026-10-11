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
| IA | Eliminada del codi existent (Llamatik, `firebase-ai`, assistent). |
| Quantitat | El selector crea N ítems independents; les accions actuen sobre una unitat. Els `packSize > 1` antics es mostren com «pack de N». |
| Llença | `DISCARDED` es desa; només Esborra elimina la fila. |
| Calendari | Tira de 7 dies (Inici i Categoria) + vista de mes (`11-calendari.md`). Punts i recomptes per unitat. |
| Avís legal per a usuaris existents | Es mostra una sola vegada només el pas d'acceptació. |
| Cerca de l'Inici | Obre Rebost amb el cercador enfocat. |
| Dates passades | Es permet desar-les amb l'avís «ja ha caducat». |
| Setmana | «La setmana comença» afecta la vista de mes. |

## Temes oberts

1. ~~Calendari~~ — resolt: vegeu `pantalles/11-calendari.md`.
2. ~~«Llença» vs «Esborra»~~ — resolt: `DISCARDED` es desa.
3. **Mode fosc**: tokens definits, sense mockups. Implementar i revisar visualment.
4. **Tauleta / pantalles grans**: fora d'abast; no trencar-les (màxim d'amplada o `ListDetailPaneScaffold` més endavant).
5. **Text legal**: revisió per part d'algú de l'àmbit legal.
6. ~~Cerca global~~ — resolt: obre Rebost amb el cercador enfocat.
7. **Ordenar categories** (arrossegar a Rebost): fora d'abast.
8. **Icona vectorial definitiva**: cal dibuixar-la sobre la retícula d'icona adaptativa.
