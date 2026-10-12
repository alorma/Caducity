# Caducity Roadmap

What is left is in [`docs/backlog.md`](./docs/backlog.md). The «Caducity» redesign (Material 3 Expressive) shipped in 1.0.0; its plan is in the git history (`docs/redisseny`).

## Design principles

- **Minimalist UI**: no cluttered screens, bottom sheets and dialogs for secondary actions.
- **Material 3 Expressive**: follow the existing theme and components in `ui/theme` and `ui/components`.
- **Connected groups**: lists use the `ShapePosition` pattern.
- **Side effects**: dialogs, snackbars and bottom sheets always go through side effects, never UI state.
- **Clean Architecture**: keep domain, data and UI separated.
- **Type safety**: sealed interfaces for navigation and state.
