# 02 · Migració pantalla per pantalla

Columna «Original»: captures de `01-app-original/`. Columna «Nova»: captures de `02-disseny-nou/captures/` i fitxa a `pantalles/`.

| Original | Nova | Tipus de canvi |
|---|---|---|
| `app/00_splash.png` (icona debug vermella) | `Splash.png` + `Icona.png` | Substitució: icona A, fons crema, wordmark |
| `app/01_dashboard.png` | `Main.png` (Inici «Què caduca») | **Reemplaçada**. El calendari per categoria es substitueix per una tira de 7 dies; apareix llista per urgència |
| `app/01_dashboard.png` (secció categories) | `Rebost.png` | **Nova pantalla** (pestanya). Categories com a graella de targetes |
| `app/02_category_detail.png` | `Categoria.png` | **Reescrita**. Fora el calendari setmanal antic i les pestanyes; tira de 7 dies i llista agrupada per estat amb filtres |
| `app/03_item_actions.png` | `Accions.png` | Redisseny. Mateixes accions + dates ràpides + Mou |
| `app/04_reschedule_datepicker.png` | `Accions.png` (dates ràpides) + DatePicker M3 | El DatePicker es manté com a opció «Tria»; s'hi arriba menys |
| `app/05_filtered_expired.png` | `Caducats.png` | Redisseny. Accions per fila i accions massives |
| (calendari mensual / setmanal antic) | `CalMes.png` + tira de 7 dies a `Main.png` i `Categoria.png` | **Nou disseny** (vegeu `pantalles/11-calendari.md`) |
| (filtrat per altres estats) | `Categoria.png` amb filtre / `Rebost.png` | Els filtres per estat viuen dins Categoria; Aviat és l'Inici |
| `app/06_create_category.png` | `NovaCategoria.png` + `AfegirNovaCat.png` | Redisseny. S'afegeix **forma** i **icona**; versió ràpida dins Afegeix |
| (botó «Add item» + split) | `Afegir.png`, `AfegirTanda.png`, `AfegirNou.png` | **Reescrit**. Formulari únic, autocompletat, data per teclat, mode tanda |
| — | `Accessos.png` | **Nou**: widget i dreceres |
| `app/07_settings.png` | `Ajustos.png` | Redisseny i reordenació (Avisos primer) |
| `app/08_settings_appearance.png` | `Ajustos.png` (secció Aparença) | Es fusiona dins Ajustos. **Desapareix «Tone» (Vibrant/Soft)** |
| Settings › Debug | — | **Només en build debug** (no apareix a release) |
| `onboarding/01..09` | `OB1..OB5` | **Reescrit**: 6 → 5 pàgines (benvinguda + 4 passos) |
| `onboarding/10_dashboard_empty.png` | `InicibBuit.png` | Estat buit nou amb crida a l'acció |

## Detall dels canvis per pantalla

### Dashboard → Inici «Què caduca»
- Fora: 4 targetes grans d'estat, calendari setmanal per categoria, engranatge.
- Dins: títol amb data, botons Calendari i Cerca, tira de 7 dies, bàner de caducats (si n'hi ha), resum connectat Aviat · Frescos · Congelats, llista agrupada per dia (Avui, Demà, dia de la setmana) dels productes dins el llindar «Aviat», botó ràpid «Consumit» per fila.
- Ajustos passa a la barra flotant.

### Detall de categoria → Categoria
- Fora: calendari setmanal antic, pestanyes per producte, xip «#1, #2» separat de la targeta.
- Dins: capçalera amb forma gran, botó Calendari, tira de 7 dies, xips de filtre per estat amb recompte, seccions per estat. **Producte amb diverses unitats** = fila desplegable amb les unitats a dins. **Producte amb una sola unitat** = fila simple (s'amaga el nivell producte).

### Accions d'ítem
- Es mantenen: Consume → **Consumit**, Freeze → **Congela**, Reschedule → **Canvia la data**, Delete → **Esborra**.
- Nou: dates ràpides (+1 dia, +3 dies, +1 setmana, Tria), **Edita**, **Mou a una altra categoria**.

### Expired Items → Caducats
- Nou: capçalera `expired-container`, accions massives «Llença'ls tots» i «Selecciona», accions per fila (canviar data / llençar).
- «Llença» és diferent d'«Esborra»: marca el producte com a llençat (vegeu model de dades).

### Settings + Appearance → Ajustos
- Seccions: **Avisos** (interruptor + hora, llindar «Aviat»: 1 dia / 3 dies / 1 setmana), **Aparença** (Tema, Color dinàmic, Primer dia de la setmana), **Dades** (Còpia de seguretat, Privadesa, Quant a).
- Eliminat: Tone (Vibrant/Soft). Debug només en debug.

### Onboarding
- Eliminades: «Key Features», «Getting Started» (substituïda per selecció de categories), botó Skip.
- Avís legal: passa a l'últim pas amb casella obligatòria.
