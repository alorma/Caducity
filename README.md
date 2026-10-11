# Caducity

**Track groceries. Reduce waste. Save money.**

Caducity is an Android app that tracks when your groceries expire, so you can use them in time and waste less food. Built with Jetpack Compose and Material 3 Expressive.

---

## Features

- **Home**: what expires soon, grouped by day, with a 7-day strip and a banner for what already expired.
- **Add**: name, category and expiry date in a few taps, with batch mode, quantity and name suggestions.
- **Pantry and categories**: browse by category, filter by status or by day, and search.
- **Status lists**: Expired, Soon, Fresh and Frozen, with bulk actions and undo.
- **Calendar**: month view of what expires each day.
- **Daily reminder**: one notification a day, at the time you choose.
- **Backup and restore**, light/dark theme, dynamic colour, Catalan, Spanish and English.

The redesign plan and its status are in [`docs/redisseny/PLA-MIGRACIO.md`](./docs/redisseny/PLA-MIGRACIO.md).

---

## Requirements

- JDK 21
- Android SDK
- Gradle 9.8.1 (wrapper included)

## Build

```bash
./gradlew assembleDebug                      # debug APK
./gradlew installDebug                       # install on a connected device
./gradlew ktlintCheck test assembleDebug     # what CI runs on every PR
```

See [`CLAUDE.md`](./CLAUDE.md) for architecture and development guidelines.

---

## Architecture

Clean Architecture in a single Android module (`app`), plus `icons` for the generated icon set:

```
app/src/main/kotlin/com/alorma/caducity/
├── ui/        # Compose screens, components, theme, navigation (Navigation 3)
├── domain/    # Models and use cases
├── data/      # Room database, data sources, backup
├── config/    # Time, preferences, calendar settings
├── feature/   # Tracking, consent, review, notifications
└── di/        # Koin modules
```

**Key technologies:** Kotlin, Jetpack Compose with Material 3 Expressive, Navigation 3, Room, Koin, WorkManager, kotlinx-datetime, multiplatform-settings, Firebase Analytics and Crashlytics.

---

## License

[Add your license here]
