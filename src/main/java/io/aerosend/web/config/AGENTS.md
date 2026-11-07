# config

## Overview

Spring configuration for the web app. It currently holds only the local development stand-in for customer identity.

## Interface

- `LocalCustomerConfig` (`@Profile("local")`): a `WebMvcConfigurer` that registers an interceptor setting the `customerId` request attribute on every request. The value comes from `aerosend.local.customer-id` in `application-local.yml`.

## Invariants

- `LocalCustomerConfig` must stay behind the `local` profile. It gives every request the same customer, so running it elsewhere would show that customer's domains to every signed-in user.
- It is not customer authentication. The app still has no real source of customer identity, so `/` and `/domains` return 400 outside the `local` profile.

## Side Effects

- None beyond setting the request attribute.

## Verification

- Run from `repos/web`: `mvn test`. `LocalCustomerConfigTest` checks both the `local` profile and the 400 without it.
