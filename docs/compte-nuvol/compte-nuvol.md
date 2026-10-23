# Compte i núvol (Firebase)

Captures: `captures/CompteAjustos`, `CompteLogin`, `CompteFusio`, `ComptePerfil`, `CompteSortir`.

## Objectiu

Un **compte opcional** per tenir el mateix rebost a diversos dispositius i no perdre'l en canviar de mòbil. **Sense compte, l'app ha de funcionar exactament igual que ara.** No hi ha mode de pagament: tot ha de cabre a la quota gratuïta de Firebase.

## 1. Arquitectura

- **Room continua sent la font de veritat.** Tota la UI llegeix de Room; l'app funciona sense xarxa.
- **Firestore és una còpia** que se sincronitza en segon pla. La UI mai no espera la xarxa.
- **Firebase Auth** amb **Google** via Credential Manager (`GetGoogleIdOption` → `GoogleAuthProvider.getCredential`). Correu/contrasenya: fora d'abast.
- Firestore a la regió **UE** (`eur3` o `europe-west`), amb regles per usuari: `allow read, write: if request.auth.uid == userId` sobre `users/{userId}/**`.
- Sense compte, **no** s'inicialitza res de Firestore (ni s'envia res).

### Estructura a Firestore

```
users/{uid}
  products/{syncId}       camps del producte + updatedAt + deletedAt + updatedBy
  categories/{syncId}     nom, forma, icona, durada habitual, es pot congelar, ordre, descripció…
  nameHistory/{syncId}    noms usats → categoria, tipus d'unitat
  transformations/{syncId}
  devices/{installationId}  model, lastSeen
```

Els **ajustos del dispositiu** (tema, colors, avisos, «Aviat vol dir», primer dia de la setmana) **no** se sincronitzen.

### Canvis al model local (Room)

- Cada entitat sincronitzable rep un **`syncId` (UUID)** estable, a més de la clau local actual. Migració: generar-ne un per fila existent.
- **`updatedAt`** (epoch ms, rellotge local) i **`deletedAt`** (nullable): esborrar = marcar `deletedAt` (esborrat suau). La UI filtra els esborrats. Purga local dels esborrats sincronitzats al cap de 30 dies.
- **`dirty`** (boolean): hi ha canvis locals pendents de pujar.
- Taula `sync_state`: `lastPulledAt` per col·lecció.

## 2. Sincronització

- **Pujada (outbox)**: cada escriptura local marca `dirty = true` i actualitza `updatedAt`. Un `CoroutineWorker` únic (WorkManager, amb restricció de xarxa; només Wi-Fi si l'usuari ho tria) puja les files `dirty` en *batches* de fins a 500 i les marca netes.
- **Baixada**: amb l'app en primer pla, *snapshot listeners* per col·lecció amb `whereGreaterThan("updatedAt", lastPulledAt)`; en segon pla, un `PeriodicWorkRequest` (p. ex. cada 6 h) fa la mateixa consulta. Es llegeixen **només els canvis**, mai la col·lecció sencera (quota).
- **Conflictes (v1)**: *last-write-wins per document* segons `updatedAt`. Un esborrat (`deletedAt`) guanya sobre una edició anterior. Si una fila local està `dirty` i arriba una versió remota més nova, guanya la remota i es descarta la local.
- **Accions**: consumir, congelar, llençar i transformar es desen com a canvis normals del producte. La sincronització mai no ha de «tornar» un producte consumit (l'esborrat guanya).
- **Desfés** (snackbar) funciona en local; si el canvi ja s'havia pujat, el desfés és una nova escriptura amb `updatedAt` més nou.

## 3. Pantalles

### 3.1 Ajustos (`CompteAjustos`)

- **Sense sessió**: targeta a dalt de tot (fons `primaryContainer`, forma Sunny amb `cloud_sync`): «Guarda el rebost al núvol» · «Inicia sessió i tingues-lo a tots els dispositius» › → Inicia sessió.
- **Amb sessió**: la targeta passa a ser una fila normal: avatar (foto de Google o inicial) · nom · estat («Tot al dia», «Sincronitzant…», «Sense connexió · 3 canvis pendents») › → Compte.

### 3.2 Inicia sessió (`CompteLogin`)

Pantalla completa: ✕; forma Sunny amb `cloud_sync`; títol «El teu rebost, a tots els dispositius»; tres avantatges (Mòbil, tauleta i rellotge · Còpia automàtica · Funciona sense internet); botó **«Continua amb Google»** (amb el botó oficial de Google, seguint-ne les guies de marca); «Ara no»; text petit de privadesa: «Els productes es guarden al núvol (Firebase). Pots tancar la sessió o esborrar el compte i les dades quan vulguis.»

### 3.3 Primer inici de sessió (`CompteFusio`)

- Compte **buit**: es puja el rebost local sense preguntar.
- Mòbil **buit**: es baixa el compte sense preguntar.
- **Tots dos amb dades**: full «El teu compte ja té dades» amb el recompte («23 en aquest mòbil ⇄ 41 al compte») i dues opcions:
  - **Ajunta-ho tot** (per defecte): es pugen els locals i es baixen els remots. Els repetits queden com a files separades (no s'ajunten).
  - **Fes servir només el compte**: abans es desa una **còpia de seguretat local** automàtica (amb el mateix format que l'exportació actual), i després es reemplaça el local pel remot.
- **Categories amb el mateix nom** (p. ex. les per defecte): sí que s'ajunten per nom per no duplicar-les; els productes apunten a la categoria resultant.

### 3.4 Compte (`ComptePerfil`)

- Capçalera: avatar, nom, correu · Google.
- **Sincronització**: «Tot al dia · Última sincronització: fa 2 min» amb botó «Ara»; interruptor «Sincronitza amb dades mòbils» (per defecte activat; si no, només Wi-Fi).
- **Dispositius**: llista de `devices` (model + «Aquest dispositiu» / «Fa 3 dies»).
- **Les teves dades**: Exporta les dades › (la mateixa exportació que ja existeix) · Tanca la sessió · **Esborra el compte i les dades** (vermell).

### 3.5 Tancar la sessió (`CompteSortir`)

Full: «Tancar la sessió?» · «Les dades continuen al núvol. En aquest mòbil…» · **Mantén-les** (per defecte; l'app continua sense compte) / **Esborra-les del mòbil**. Botons Cancel·la / Tanca la sessió.

### 3.6 Esborrar el compte

Diàleg de confirmació amb el text explícit «S'esborraran el compte i tots els productes del núvol i de tots els dispositius. No es pot desfer.» → reautenticació si cal → s'esborren tots els documents de `users/{uid}` (subcol·leccions per *batches*) → `FirebaseUser.delete()` → l'app torna a l'estat sense compte, **mantenint** les dades locals d'aquest mòbil.

## 4. Privadesa i Play

- **Onboarding › Privadesa** (pas 4): avui diu «Els teus productes es guarden només en aquest mòbil… Les dades no surten del mòbil». Ha de passar a: «Sense compte, els teus productes es guarden només en aquest mòbil. Si inicies sessió, se'n guarda una còpia al núvol.»
- Actualitzar la **política de privadesa** i el formulari **Seguretat de les dades** de Google Play (dades de compte i contingut de l'app, xifrades en trànsit, l'usuari les pot esborrar).
- Google Play demana que es pugui **esborrar el compte des de l'app i també des d'un enllaç web**: cal una pàgina senzilla (p. ex. a la web de l'app) que expliqui com demanar-ho. *(Feina per a mi, no per a Claude Code.)*
- L'**analítica** continua separada i desactivada per defecte; no s'hi barreja l'identificador del compte.

## 5. Quota gratuïta

- Només text; cap imatge.
- Lectures incrementals (`updatedAt > lastPulledAt`), mai col·leccions senceres després del primer cop.
- Pujades en *batches* i amb *debounce* (no escriure a cada toc del − / +).
- Afegir un indicador intern (log) de lectures/escriptures per sessió per vigilar el consum durant les proves.

## 6. Fora d'abast

Rebost compartit (llar), login amb correu, sincronització d'ajustos, ajuntar productes duplicats.

## 7. Textos (ca / es / en)

Guarda el rebost al núvol / Guarda tu despensa en la nube / Save your pantry to the cloud · Inicia sessió i tingues-lo a tots els dispositius / Inicia sesión y tenla en todos tus dispositivos / Sign in to have it on all your devices · El teu rebost, a tots els dispositius / Tu despensa, en todos tus dispositivos / Your pantry, on all your devices · Mòbil, tauleta i rellotge / Móvil, tablet y reloj / Phone, tablet and watch · Còpia automàtica / Copia automática / Automatic backup · Funciona sense internet / Funciona sin internet / Works offline · Continua amb Google / Continuar con Google / Continue with Google · Ara no / Ahora no / Not now · El teu compte ja té dades / Tu cuenta ya tiene datos / Your account already has data · Ajunta-ho tot / Juntarlo todo / Merge everything · Fes servir només el compte / Usar solo la cuenta / Use only the account · Tot al dia / Todo al día / Up to date · Sincronitza amb dades mòbils / Sincronizar con datos móviles / Sync over mobile data · Dispositius / Dispositivos / Devices · Tanca la sessió / Cerrar sesión / Sign out · Esborra el compte i les dades / Eliminar la cuenta y los datos / Delete account and data · Mantén-les / Mantenerlos / Keep them · Esborra-les del mòbil / Borrarlos del móvil / Remove from this phone.
