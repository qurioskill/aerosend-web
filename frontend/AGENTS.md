# frontend

## Overview

The Vite project for the Vue islands. It builds `src/main.js` into the Spring Boot app's static resources, and Thymeleaf pages load the result.

## Interface

- `package.json` scripts: `dev` (`vite`), `build` (`vite build`), `test` (`vitest run`).
- `vite.config.js`: default export from `defineConfig`, using the `@vitejs/plugin-vue` plugin.
- [src](./src/AGENTS.md): the island entry point and components.

## Invariants

- The output names are fixed by `rollupOptions.output`: `entryFileNames: 'main.js'` and `assetFileNames: 'app.[ext]'`. `src/main/resources/templates/layout.html` hard-codes `/dist/main.js` and `/dist/app.css`, so change both together.
- `build.outDir` is `../src/main/resources/static/dist`, which is gitignored.
- `pom.xml` has no frontend build step, so `npm run build` must run separately before the Spring app can serve the islands.
- The dev server proxies `/` to `http://localhost:8080`, so the Spring app must be running.

## Side Effects

- `npm run build` deletes and rewrites `src/main/resources/static/dist` (`emptyOutDir: true`).
- `npm run dev` starts a local Vite server that forwards requests to port 8080.

## Verification

- Run from `repos/web/frontend`: `npm test` (runs `vitest run`).
- No test files exist yet, so the command exits 1 with "No test files found".
