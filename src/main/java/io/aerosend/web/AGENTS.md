# io.aerosend.web

## Overview

The root package of the Spring Boot app. It holds the entry point, and component scanning starts here.

## Interface

- `WebApplication`: the `@SpringBootApplication` class, with `main(String[] args)`.
- [client](./client/AGENTS.md): `CoreApiClient`, the HTTP client for `aerosend-core`.
- [config](./config/AGENTS.md): `LocalCustomerConfig`, active only under the `local` profile.
- [controller](./controller/AGENTS.md): `DashboardController` and `DomainsController`.

## Invariants

- Beans must stay under `io.aerosend.web` to be picked up by the default component scan.
- The repo defines no `SecurityFilterChain`, although `spring-boot-starter-security` is on the classpath, so Spring Boot's default security configuration applies.

## Side Effects

- `main` starts the embedded web server on port 8080 (`server.port` in `application.yml`).

## Verification

- Run from `repos/web`: `mvn test`.
- `LocalCustomerConfigTest`, `DomainsControllerTest`, `DashboardControllerTest` and `DomainTableTest` are the test classes.
