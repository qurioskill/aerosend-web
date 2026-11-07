# client

## Overview

The app's only data access layer. `CoreApiClient` is a thin HTTP client over `aerosend-core`'s `/api/v1`. This app has no database of its own.

## Interface

- `CoreApiClient` (`@Component`), constructed with `aerosend.core-api.base-url`, `aerosend.core-api.connect-timeout` and `aerosend.core-api.read-timeout`:
  - `List<DomainView> domains(long customerId)`: `GET /api/v1/domains?customerId={id}`
  - `List<DailyStats> stats(long domainId, int days)`: `GET /api/v1/domains/{id}/stats?days={d}`
  - `List<DomainSummary> domainSummaries(long customerId)`: `GET /api/v1/domains/summary?customerId={id}`
- `CoreApiClient.DomainView(Long id, String name, String burnStatus, boolean dnsVerified)`
- `CoreApiClient.DailyStats(LocalDate day, long sent, long bounces, long replies, Double warmupScore, Double inboxPlacement)`
- `CoreApiClient.WindowCounts(Long sent, Long bounces, Long replies)`
- `CoreApiClient.DomainSummary(Long domainId, WindowCounts last14Days, WindowCounts last3Days, Double latestPlacement, LocalDate latestPlacementDay, LocalDate lastDataDay)`

## Invariants

- `DomainView`, `DailyStats`, `WindowCounts` and `DomainSummary` mirror core's DTOs and must be kept in sync when core changes (class Javadoc). REST `/api/v1/*` is owned by core, so changing it is a cross-repo change (see `docs/architecture.md` in the harness repo).
- `warmupScore` and `inboxPlacement` are boxed `Double`s, so they can be null.
- `WindowCounts` and `DomainSummary` must keep boxed types. A field renamed in core then arrives as null and `DomainTable` rejects it. A primitive `long` would arrive as 0, which reads as a healthy domain.
- `burnStatus` is a plain `String`, not an enum. Its values are defined by core. `DomainView` keeps it to mirror core's DTO, but no page renders it.
- `aerosend.core-api.base-url`, `connect-timeout` and `read-timeout` have no `@Value` default, so the app fails to start if one is missing.

## Side Effects

- Every method makes a blocking HTTP GET to core.
- The timeouts apply to all calls. A timeout surfaces as `ResourceAccessException`.
- The client sets no retries, error handlers or auth headers.

## Verification

- Run from `repos/web`: `mvn test`.
- No test covers this class directly: the controller tests replace it with a `@MockBean`.
