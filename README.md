# aerosend-web

Customer-facing app at **app.aerosend.io**: sign-up, domain ordering (batches of 10), mailbox creation, connecting sequencers, deliverability dashboards.

| | |
|---|---|
| Category | frontend |
| Change frequency | **medium** |
| Stack | Java 21, Spring Boot 3, **Thymeleaf** (server-rendered pages), **Vue 3** islands (charts/interactive widgets, built with Vite) |
| Owners | Product team |

## How it's put together

- Pages are Thymeleaf templates in `src/main/resources/templates`.
- Interactive widgets are Vue components in `frontend/src/components`, mounted onto `<div data-vue="ComponentName">` placeholders. `npm run build` emits to `src/main/resources/static/dist`.
- The app has **no database of its own**. All data goes through `CoreApiClient` → `aerosend-core` `/api/v1`.

## Run locally

```bash
(cd frontend && npm install && npm run dev)   # Vite on :5173, proxied
mvn spring-boot:run                            # :8080, expects core on :8081
```
