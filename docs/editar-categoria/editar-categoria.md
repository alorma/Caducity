# Pantalla de categoria (nova i edició)

Captures: `captures/NovaCategoria`, `NovaCategoriaAspecte`, `NovaCategoriaDurada`, `NovaCategoriaDescripcio`, `Ajustos`, `Afegir`.

## Per què

La pantalla actual ocupa gairebé dues pantalles i totes les seccions pesen igual: previsualització gran + nom repetit, 18 botons de forma i icona, un slider de durada dens i una descripció sempre visible. A més:
- **Bug**: amb «1 setmana» seleccionat el text diu «Sense durada · la data la poses tu».
- Els placeholders («Nom», «Per exemple: …») surten en negreta, com si fossin text escrit.

L'objectiu és que **tot càpiga en una pantalla sense scroll** i que les opcions secundàries s'obrin en fulls.

## 1. Pantalla principal (`NovaCategoria`)

La mateixa pantalla serveix per **crear** («Nova categoria») i **editar** («Edita categoria»).

- **Barra superior**: ✕ · títol · «Desa» (desactivat fins que hi ha nom).
- **Previsualització**: forma (128 dp) amb la icona a dins i un botó rodó ✏️ a baix a la dreta (contorn del color de fons). Text a sota: «Toca per canviar la forma i la icona». Tocar la forma o el ✏️ obre el full **Aspecte**. És el mateix patró que la foto de perfil d'un contacte.
- **Nom**: camp d'una línia. Placeholder en pes normal i `onSurfaceVariant`.
- **Opcions**: llista agrupada (mateix estil que Ajustos: 24 dp a dalt del primer i a baix de l'últim, 6 dp entre files, 2 dp de separació):
  1. **Durada habitual** · subtítol «Per suggerir la data en afegir» · valor a la dreta («1 setmana», «Sense») + › → full **Durada**.
  2. **Es pot congelar** · subtítol «Si no, no surt «Congela»» · interruptor a la fila (sense títol de secció «Congelació»). Si aquesta feature encara no existeix al codi, deixa la fila fora; ve en un altre pla.
  3. **Descripció** · sense text: subtítol «Opcional», valor «Afegeix ›»; amb text: subtítol = el text (1 línia, el·lipsi), valor «Edita ›» → full **Descripció**.

## 2. Full Aspecte (`NovaCategoriaAspecte`)

Bottom sheet modal.
- Capçalera: previsualització petita (72 dp) + títol «Aspecte» + «{nom} · com es veu al Rebost» (si no hi ha nom: «Com es veu al Rebost»).
- **Forma**: 6 opcions en una fila (Galeta, Pentàgon, Trèvol, Sol, Coixí, Segell). **No s'usa mai la forma Flower.** Seleccionada: fons `primaryContainer`, contorn 2 dp `primary`, forma `primary`.
- **Icona**: graella de 6 × 2. Seleccionada: cercle `primary` amb icona `onPrimary`.
- Els canvis es veuen a l'instant a la previsualització del full. Botó «Fet» tanca. Tancar arrossegant també aplica (no hi ha «cancel·la»).

## 3. Full Durada (`NovaCategoriaDurada`)

Substitueix el slider. **Mateixos valors que ara**, en botons:
- «Dies»: 1 dia · 2 dies · 3 dies
- «Setmanes i mesos»: 1 setmana · 2 setmanes · 1 mes · 2 mesos · 3 mesos · Sense
Selecció única; el triat té fons `primary`, text `onPrimary` i ✓. Sota, una caixa amb 💡 que explica el valor: «En afegir {un producte de la categoria}, et proposarem que caduca **d'aquí 1 setmana**. Sempre la pots canviar.» Amb «Sense»: «No et proposarem cap data; la poses tu.» Botó «Fet».

## 4. Full Descripció (`NovaCategoriaDescripcio`)

- Títol «Descripció» + «Una pista del que hi va. Opcional.»
- Camp multilínia, **màxim 80 caràcters** (si el codi ja té un límit, mantén aquell), amb comptador «25 / 80». El focus i el teclat s'obren sols; el full puja per sobre del teclat (`imePadding`).
- Sota el camp, l'exemple en petit: «Per exemple: refrescos, sucs i begudes fermentades».
- Botons: **«Esborra»** (només si ja hi havia text; buida i tanca) i **«Fet»**. Tancar arrossegant o tocant fora desa el que hi ha escrit.

## 5. Durada fora d'Ajustos (`Ajustos`, `Afegir`)

- A **Ajustos › Dates** desapareix la fila «Durada habitual» i la pantalla «Durada habitual per categoria» (amb el seu botó «Torna a les durades suggerides»). Les durades guardades es mantenen; només canvia on s’editen. La durada només es configura a la pantalla de categoria. «Aviat vol dir» (global) es queda a Ajustos.
- A **Afegeix**, la línia «Suggerida: els Làctics duren 1 setmana» passa a ser tocable, amb «Canvia» a la dreta: obre el full Durada d'aquella categoria; en tornar, la data suggerida es recalcula.
- Accés a l'edició des de la pantalla de Categoria: menú ⋮ › «Edita la categoria».

## 6. Textos (ca / es / en)

Toca per canviar la forma i la icona / Toca para cambiar la forma y el icono / Tap to change shape and icon · Opcions / Opciones / Options · Aspecte / Aspecto / Look · Com es veu al Rebost / Cómo se ve en la Despensa / How it looks in the Pantry · Dies / Días / Days · Setmanes i mesos / Semanas y meses / Weeks and months · Sense / Sin / None · Una pista del que hi va. Opcional. / Una pista de lo que va aquí. Opcional. / A hint of what goes here. Optional. · Esborra / Borrar / Delete · Fet / Hecho / Done · Canvia / Cambiar / Change · Edita la categoria / Editar categoría / Edit category.
(Adapta «Rebost» / «Despensa» / «Pantry» al nom que ja faci servir l'app.)
