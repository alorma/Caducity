---
paths:
  - "app/src/main/kotlin/com/alorma/caducity/data/datasource/room/**"
  - "app/schemas/**"
  - "app/src/test/kotlin/com/alorma/caducity/data/datasource/room/**"
  - "app/src/main/kotlin/com/alorma/caducity/di/PlatformModule.kt"
---

# Room: the database is published

Caducity is live on Google Play. Real users have a database at the current schema version (see `AppDatabase.kt`). **Every database change MUST ship with a migration and a migration test.** No exceptions.

## What counts as a schema change

Anything that changes the exported schema JSON:

- adding, removing or renaming an entity (`@Entity`), a column, an index, a foreign key or a primary key;
- changing a column's type, nullability or default value;
- adding an entity to `@Database(entities = [...])` in `AppDatabase.kt`.

Enums and converters stored by name (for example `AvatarConverters`): adding a value is safe; **renaming or removing one breaks existing rows** and needs a data migration even though the schema JSON does not change.

DAO-only changes (new queries) are not schema changes.

## Checklist for every schema change

1. Bump `version` in `AppDatabase.kt` (one step: 3 → 4, 4 → 5…).
2. Build (`./gradlew :app:assembleDebug`) so KSP exports `app/schemas/.../<new version>.json`, and commit it.
3. **Never edit or delete an exported schema JSON** (`1.json` and later). They are what users have on their devices.
4. Write the migration in `Migrations.kt` (`MIGRATION_N_N+1`) and register it with `.addMigrations(...)` in `di/PlatformModule.kt`. New `NOT NULL` columns need a `DEFAULT` so existing rows stay valid.
5. Add a migration test next to `Migration2To3Test` (Robolectric + `MigrationTestHelper`; the schemas are exposed to the debug assets in `app/build.gradle.kts`): create the DB at version N, insert representative rows, run the migration and assert the data survived.

## Never

- Use `fallbackToDestructiveMigration`: it would wipe users' data.
- Reset the database to a "clean version 1" or regenerate `1.json`.
- Ask users to clear app data or reinstall.
