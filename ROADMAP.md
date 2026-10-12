# Caducity Roadmap

The current work is the **«Caducity» redesign** (Material 3 Expressive). Its phases, decisions and status live in [`docs/redisseny/PLA-MIGRACIO.md`](./docs/redisseny/PLA-MIGRACIO.md); that file is the source of truth.

The app is not published between phases. It is released once the redesign is finished.

## After the redesign

The improvement points of section 15 of the plan are done.

Out of scope for now: shopping list, cloud sync, barcode or camera input, tablet layouts (see section 12 of the plan).

## Design principles

- **Minimalist UI**: no cluttered screens, bottom sheets and dialogs for secondary actions.
- **Material 3 Expressive**: follow the design system in `docs/redisseny`.
- **Connected groups**: lists use the `ShapePosition` pattern.
- **Side effects**: dialogs, snackbars and bottom sheets always go through side effects, never UI state.
- **Clean Architecture**: keep domain, data and UI separated.
- **Type safety**: sealed interfaces for navigation and state.
