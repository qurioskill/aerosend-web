# controller

## Overview

Spring MVC controllers that fetch data through `CoreApiClient` and return Thymeleaf view names. Every route here is a read-only `GET`.

## Interface

| Route | Method | View | Model attributes |
|---|---|---|---|
| `GET /` | `DashboardController.dashboard` | `dashboard` | `domains`, `table`, `figuresUnavailable` |
| `GET /domains` | `DomainsController.list` | `domains/list` | `domains` |
| `GET /domains/{id}` | `DomainsController.detail` | `domains/detail` | `domainId`, `statsJson` |

Views live in `src/main/resources/templates`. Each one is wrapped by the `layout :: page(title, content)` fragment in `layout.html`.

`DomainTable` is a plain record with no Spring or HTTP dependency, used only by `DashboardController`. `DomainTable.from(domains, summaries)` produces:

- `sorted()`: `SortedRow`s, highest 3-day bounce rate first, then by name. Rates are fractions from 0 to 1.
- `notEnoughData()`: `NoDataRow`s with a `reason`, ordered by name. They carry no rates.
- `dataThrough()`: the latest `lastDataDay` across the customer's domains, or null.

## Invariants

- `dashboard` and `list` need a `customerId` request attribute (`@RequestAttribute`). **Only the `local` profile sets it** (see [config](../config/AGENTS.md)). Under any other profile these two routes fail argument binding and return 400.
- `detail` takes only the path `id` and does not check that the domain belongs to the current customer. `CoreApiClient.stats` sends no customer ID either, so this route does no ownership check of its own.
- On `/`, `table` is null exactly when `figuresUnavailable` is true.
- `core.domains` is the authoritative list in `DomainTable`. A domain with no summary counts as having no data, and summaries for unknown domains are ignored.
- A domain with fewer than `MIN_SENDS_LAST_3_DAYS` (30) sends in the last 3 days is never sorted. This minimum is the owner's choice.
- `DomainTable` throws `IllegalStateException` on a missing body or a null ID, window or count, because each means the contract with core has drifted.
- The dashboard gives no verdict or advice. It does not use the words "worst" or "needs attention", and missing data must never show as a 0% rate.
- `detail` always requests a 14-day window (`core.stats(id, 14)`).
- `layout.html` links to `/mailboxes` and `/integrations`, but no controller maps those routes.
- `detail` passes `statsJson`, a JSON string, to `DeliverabilityChart` through `data-stats`. See [components](../../../../../../../frontend/src/components/AGENTS.md).

## Side Effects

- `dashboard` makes two blocking calls to core through `CoreApiClient`, and the other handlers make one.
- `dashboard` catches `RestClientException` and `IllegalStateException` around the summary call only. It logs one warning and renders the plain list with "Figures are unavailable right now".
- Everywhere else, errors from core propagate out of the handlers, including a failed domain list on `/`. No `@ExceptionHandler` or `@ControllerAdvice` exists.

## Verification

- Run from `repos/web`: `mvn test`.
- `DomainsControllerTest` covers the JSON passed to the chart. `LocalCustomerConfigTest` exercises `DashboardController` through the `customerId` attribute.
- `DomainTableTest` covers the grouping, sorting and contract checks. `DashboardControllerTest` covers the rendered page and each failure path.
