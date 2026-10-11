# Caducity Roadmap

The current work is the **«Caducity» redesign** (Material 3 Expressive). Its phases, decisions and status live in [`docs/redisseny/PLA-MIGRACIO.md`](./docs/redisseny/PLA-MIGRACIO.md); that file is the source of truth.

The app is not published between phases. It is released once the redesign is finished.

## Ideas after the redesign

Collected in section 15 of the plan («Punts a millorar»):

- Month change in the calendar with a `HorizontalPager`.
- Shared top bar with a fixed header on every screen.
- Collapsible header with `MotionLayout`.
- Reconsider an external calendar library instead of the own one.

Out of scope for now: shopping list, cloud sync, barcode or camera input, tablet layouts (see section 12 of the plan).

## Design principles

- **Minimalist UI**: no cluttered screens, bottom sheets and dialogs for secondary actions.
- **Material 3 Expressive**: follow the design system in `docs/redisseny`.
- **Connected groups**: lists use the `ShapePosition` pattern.
- **Side effects**: dialogs, snackbars and bottom sheets always go through side effects, never UI state.
- **Clean Architecture**: keep domain, data and UI separated.
- **Type safety**: sealed interfaces for navigation and state.
