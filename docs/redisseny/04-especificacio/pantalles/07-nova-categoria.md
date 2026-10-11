# Nova categoria (pantalla completa)

Captura: `NovaCategoria.png`. Font: `NovaCategoria.dc.html`. Substitueix `app/06_create_category.png`. També serveix per **editar** una categoria (títol «Edita la categoria», i al final botó outlined en `expired` «Esborra la categoria»).

## Estructura
1. Barra: `close` + «Nova categoria» + botó **Desa** (pastilla `primary`, 44 dp). Desactivat sense nom.
2. **Previsualització** centrada: forma 120 dp + icona 52 + nom (`Bricolage 28/800`). S'actualitza en viu.
3. **Nom** (camp 56 dp).
4. **Forma**: 6 opcions (Galeta, Flor, Trèvol, Sol, Coixí, Segell), graella 6 columnes, 52 dp.
5. **Icona**: 12 opcions, graella 6×2, 48 dp.
6. **Descripció · opcional**: àrea de text 80 dp, placeholder «Per exemple: refrescos, sucs i begudes fermentades».

Validació: nom únic (sense distingir majúscules); si es repeteix, missatge d'error sota el camp i Desa desactivat.
