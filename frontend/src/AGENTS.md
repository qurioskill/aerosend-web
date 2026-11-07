# frontend/src

## Overview

The browser entry point for the Vue islands. `main.js` finds server-rendered placeholders and mounts a separate Vue app on each one. There is no client-side router or shared store.

## Interface

- `main.js`: the Vite entry. It exports nothing and runs on load, and it imports `styles.css`.
- `styles.css`: page-level styles for the Thymeleaf templates. Vite merges it into `app.css` with the components' scoped styles.
  - `registry`: maps island names to components. It holds `DeliverabilityChart`.
- Island protocol: `<div data-vue="ComponentName" data-prop="value">`. `data-vue` selects the component, and every other `data-*` attribute becomes a prop.
- [components](./components/AGENTS.md): the island components.

## Invariants

- A new island must be imported and added to `registry`. An unregistered name logs `Unknown Vue island: <name>` with `console.warn` and that element is skipped.
- `data-vue` values in Thymeleaf templates must match `registry` keys exactly.
- `vue` is stripped from `el.dataset` before the rest is passed as props. Attribute names follow `dataset` camelCase conversion, so `data-foo-bar` becomes `fooBar`.
- `main.js` runs once. Placeholders added to the DOM later are not mounted.

## Side Effects

- Calls `createApp(...).mount(el)` for each registered `[data-vue]` element, replacing that element's contents.

## Verification

- Run from `repos/web/frontend`: `npm test` (runs `vitest run`).
- No test files exist yet, so the command exits 1 with "No test files found".
