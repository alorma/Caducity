# 04 · Model de dades i preferències

> Aquests són **requisits del disseny**, no una implementació. Claude Code ha de contrastar-los amb el model actual (Room/DataStore, etc.) i proposar les migracions mínimes.

## Jerarquia (es manté)

```
Category
├── Product (opcional)
│   └── Item (unitat amb data de caducitat)
└── Item sense producte
```

Canvi d'UI, no de dades: si un producte té **una sola unitat**, la UI el mostra com una fila simple; si en té diverses, com a fila desplegable.

### Unitats i quantitat

- Cada **unitat** és un `Item`. El selector de **Quantitat** d'Afegeix crea **N ítems independents** amb la mateixa data; Consumit, Congela, Llença, canvi de data i Esborra actuen sobre **una** unitat.
- «Unitat N» és **derivat** (posició dins el producte, ordenada per data); el camp lliure `identifier` es buida i la UI l'ignora.
- Ítems antics amb `packSize > 1` (no se'n creen de nous): es mostren com «pack de N», compten com **una** unitat i conserven el consum parcial.
- **Mode edició** (`add?itemId=`): edita **una unitat**; el selector de quantitat s'amaga. Si es canvia el nom, la unitat passa al producte amb aquest nom dins la categoria, o se'n crea un de nou si no existeix; un producte que queda sense unitats actives s'amaga de la UI però es conserva (historial d'autocompletat).

## Category — camps nous

| Camp | Tipus | Notes |
|---|---|---|
| `shape` | enum (`COOKIE`, `FLOWER`, `CLOVER`, `SUNNY`, `PUFFY`, `BURST`) | Obligatori. Valor per defecte per a categories existents: assignar per ordre de creació o per nom conegut (vegeu taula a `03-design-system-compose.md`). |
| `icon` | string (nom de Material Symbol) | Obligatori. Per defecte segons nom; si no es reconeix, `eco`. |
| `sortOrder` | int | Per ordenar a Rebost i a la fila de xips d'Afegeix. |
| `description` | (ja existeix) | Passa a ser opcional i només editable a la pantalla completa. |

Llista tancada d'icones oferides: `water_full, nutrition, set_meal, grocery, bakery_dining, local_drink, local_cafe, liquor, icecream, egg, rice_bowl, cookie, soup_kitchen, kebab_dining, lunch_dining, breakfast_dining, eco`.

### Categories suggerides de l'onboarding

| Nom | Forma | Icona | Marcada per defecte |
|---|---|---|---|
| Làctics | Galeta | `water_full` | Sí |
| Fruita i verdura | Flor | `nutrition` | Sí |
| Carn i peix | Trèvol | `set_meal` | Sí |
| Rebost | Sol | `grocery` | Sí |
| Fleca | Coixí | `bakery_dining` | Sí |
| Begudes | Segell | `local_drink` | No |
| Embotits | Galeta | `kebab_dining` | No |
| Plats preparats | Flor | `soup_kitchen` | No |

## Item — estat i final de vida

Estat **derivat** (no desat) a partir de la data i del llindar:

```
FROZEN   si item.frozen == true
EXPIRED  si expiryDate < avui
SOON     si avui <= expiryDate <= avui + (category.soonThresholdDays ?: llindar global)
FRESH    altrament
```

Final de vida — cal distingir com surt un ítem del rebost:

| Acció UI | Resultat | Notes |
|---|---|---|
| **Consumit** | `CONSUMED` | Abans «Consume». |
| **Llença** (a Caducats) | `DISCARDED` | Nou. Permet comptar el malbaratament en el futur. |
| **Esborra** | Eliminació (error d'entrada) | No compta com a consumit ni llençat. |

**Decisió:** `CONSUMED` i `DISCARDED` es **desen** (la fila es conserva amb la seva data de final de vida); només «Esborra» elimina la fila. Els ítems consumits o llençats no compten com a actius a cap pantalla.

## Historial per autocompletar (Afegeix)

No és IA: és una consulta local.
- Suggeriments = productes existents (o noms d'ítems anteriors, inclosos els consumits) que **comencen** o **contenen** el text, ordenats per freqüència i data d'últim ús. Màxim 1–3.
- Tocar un suggeriment omple el **nom** i la **categoria** d'aquell producte. La data **no** s'omple.
- Si el nom coincideix exactament amb un producte existent de la categoria triada, el nou ítem s'afegeix a aquell producte (unitat nova).

## Mode tanda (Afegeix)

Estat de pantalla (no persistent): llista d'ítems desats en aquesta sessió, categoria i data actuals (es mantenen després de «Desa i un altre»), suport de **Desfés** de l'últim desat.

## Preferències

> Es mantenen amb `multiplatform-settings` (SharedPreferences); no es migra a DataStore.

| Clau | Tipus | Per defecte | On es canvia |
|---|---|---|---|
| `onboardingCompleted` | bool | false | Onboarding |
| `soonThresholdDays` | int (1, 3, 7) | 3 | Onboarding pas 2, Ajustos › Avisos. És el **per defecte**; cada categoria pot tenir el seu (`CategoryEntity.soonThresholdDays`, vegeu `pantalles/14-marge-per-categoria.md`). |
| `dailyReminderEnabled` | bool | true si es concedeix el permís | Onboarding, Ajustos |
| `dailyReminderTime` | LocalTime | 09:00 | Onboarding, Ajustos |
| `analyticsEnabled` | bool | **false** | Onboarding pas 3, Ajustos › Privadesa |
| `disclaimerAcceptedVersion` | int | — | Onboarding pas 4 |
| `disclaimerAcceptedAt` | timestamp | — | Onboarding pas 4 |
| `themeMode` | enum | SYSTEM | Ajustos |
| `dynamicColor` | bool | **false** | Ajustos |
| `firstDayOfWeek` | enum (Dilluns, Dissabte, Diumenge) | MONDAY | Ajustos (afecta la vista de mes; la tira de 7 dies sempre comença avui) |
| `widgetTipDismissed` | bool | false | Inici buit |
| ~~`tone`~~ | — | — | **Eliminar** (Vibrant/Soft) |

**Avís legal**: si `disclaimerAcceptedVersion` < versió actual del text, cal tornar a mostrar el pas d'acceptació (només aquest pas) en obrir l'app.

## Notificacions

- **Una sola notificació diària** a l'hora configurada. Hi entren només els productes que **passen a `SOON` avui** i els que **caduquen avui o demà** (així els marges llargs no es repeteixen cada dia). Títol: «N productes caduquen aviat». Text: els 2–3 més urgents («Llet sencera avui, Mozzarella demà…»).
- Permís `POST_NOTIFICATIONS` sol·licitat des del botó «Activa els avisos» de l'onboarding (no en obrir l'app).

## Marge per categoria
`CategoryEntity.soonThresholdDays: Int?` (null = per defecte). Valors suggerits de les categories de l'onboarding: Carn i peix 2, Fleca 2, Làctics 3, Fruita i verdura 3, Embotits 3, Plats preparats 2, Begudes 7, Rebost 30. Detall a `pantalles/14-marge-per-categoria.md`.
