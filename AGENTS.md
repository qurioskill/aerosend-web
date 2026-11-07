# aerosend-web Blueprint

## System Overview

`aerosend-web` is the customer-facing app. Spring Boot 3.3 on Java 21 renders Thymeleaf pages, and Vue 3 islands add interactive widgets to those pages. It has no database: all data comes from `aerosend-core` over REST through `CoreApiClient`.

A request flows like this:

1. A controller calls `CoreApiClient`, which calls core's `/api/v1`.
2. The controller returns a Thymeleaf view.
3. The view renders `<div data-vue="Name" data-*>` placeholders.
4. `frontend/src/main.js` mounts a Vue component on each placeholder.

The code currently implements three read-only pages: `/`, `/domains` and `/domains/{id}`. See [controller](./src/main/java/io/aerosend/web/controller/AGENTS.md).

## Subdirectories & System Boundaries

- frontend: [./frontend/AGENTS.md](./frontend/AGENTS.md) - Vite project for the Vue islands. It builds into `src/main/resources/static/dist`.
- src: [./src/AGENTS.md](./src/AGENTS.md) - Maven source tree for the Spring Boot app.
- src/main/resources - Not a contract directory, as it holds no source code:
  - `application.yml` sets `aerosend.core-api.base-url` (`http://localhost:8081`), `aerosend.core-api.connect-timeout` (2s), `aerosend.core-api.read-timeout` (5s), `server.port` (8080) and `spring.thymeleaf.cache: false`.
  - `application-local.yml` sets `aerosend.local.customer-id` (1).
  - `templates/` holds `layout.html`, `dashboard.html`, `domains/list.html` and `domains/detail.html`.

## Global Invariants

- All data access goes through `CoreApiClient`. Do not add a database or direct SQL to this repo.
- REST `/api/v1/*` is owned by core, so changing it is a cross-repo change (see `docs/architecture.md` in the harness repo).
- `data-vue` names in templates must match `registry` in `frontend/src/main.js`.
- Island props are strings, taken from `data-*` attributes. Structured data must be passed as JSON.
- The Vite output names `main.js` and `app.css` must match the `/dist/...` paths in `layout.html`.
- `src/main/resources/static/dist` is build output and is gitignored. Do not edit it by hand.
- Only the `local` profile sets the `customerId` request attribute that `/` and `/domains` require, and no `SecurityFilterChain` is defined. Outside that profile those routes return 400. See [config](./src/main/java/io/aerosend/web/config/AGENTS.md).

## Global Side Effects

- Blocking HTTP GETs to `aerosend-core`, bounded by the connect and read timeouts. The app makes no writes to core today.
- `npm run build` deletes and rewrites `src/main/resources/static/dist`.

## System Verification

- Java, run from `repos/web`: `mvn test`. `LocalCustomerConfigTest`, `DomainsControllerTest`, `DashboardControllerTest` and `DomainTableTest` are the test classes.
- Frontend, run from `repos/web/frontend`: `npm test` (runs `vitest run`). No test files exist yet, so the command exits 1 with "No test files found".
