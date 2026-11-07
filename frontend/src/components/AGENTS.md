# components

## Overview

Vue 3 single-file components that run as islands on server-rendered Thymeleaf pages. `../main.js` mounts each one onto a `<div data-vue="Name">` placeholder and passes the placeholder's other `data-*` attributes as props.

## Interface

- `DeliverabilityChart.vue`: renders a line chart with the series "Bounce rate", "Reply rate" and "Inbox placement".
  - Prop `stats` (`String`, required): a JSON array of rows with `day`, `sent`, `bounces`, `replies` and `inboxPlacement`.

## Invariants

- Props arrive as strings because they come from `el.dataset`. Structured data must be a JSON string that the component parses itself, as `DeliverabilityChart` does with `JSON.parse(props.stats)`.
- `DeliverabilityChart` reports a rate of `0` when `sent` is `0`, so it never divides by zero.

## Side Effects

- `DeliverabilityChart` creates a `Chart` instance on its `<canvas>` in `onMounted`. It has no unmount cleanup.
- `DeliverabilityChart` imports `chart.js/auto` statically, so chart.js is part of the single `main.js` entry that loads on every page.

## Verification

- Run from `repos/web/frontend`: `npm test` (runs `vitest run`).
- No test files exist yet, so the command exits 1 with "No test files found".
