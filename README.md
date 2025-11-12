# FINT Flyt Integration Service

Spring Boot service that maintains integration definitions for FINT Flyt tenants. It exposes an internal API for creating, updating, and querying integrations, persists state in PostgreSQL, validates references to configuration documents, and serves Kafka request/reply contracts that other Flyt services depend on.

## Highlights

- **RESTful integration registry** — Spring WebFlux controller for listing, fetching, creating, and patching integrations under `/internal/api/integrasjoner`.
- **PostgreSQL persistence** — JPA-backed repository with Flyway migrations ensuring a unique key per source application integration.
- **Kafka request/reply bridges** — Consumers and producers that expose integration lookups and configuration fetches over namespaced request topics.
- **Context-aware validation** — Custom Jakarta Bean Validation constraints that query configuration state before accepting active configuration changes.
- **Authorization-aware listings** — Optional `UserAuthorizationService` guard that filters data by the caller’s allowed source applications.

## Architecture Overview

| Component                                      | Responsibility                                                                                                 |
|-----------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `IntegrationController`                        | Handles internal HTTP requests, enforces authorization rules, and orchestrates validation.                    |
| `IntegrationService`                           | Coordinates repository access, DTO mapping, and business rules for CRUD operations.                           |
| `IntegrationRepository`                        | Spring Data JPA repository storing integrations in PostgreSQL with uniqueness constraints.                    |
| `IntegrationMappingService`                    | Maps between JPA entities and API DTOs to keep persistence separate from transport concerns.                  |
| `IntegrationValidatorFactory` & constraints    | Builds validators with contextual payload so active configuration IDs are verified before persist.            |
| `ConfigurationRequestProducerService`          | Issues Kafka request/reply calls to fetch configuration snapshots used during validation.                     |
| `IntegrationRequestConsumerConfiguration`      | Exposes Kafka listeners that answer integration lookup requests by ID or by (sourceApp, integrationId) tuple. |
| `ActiveConfigurationIdRequestConsumerConfiguration` | Serves active configuration IDs over Kafka topics so dependent services can resolve them dynamically.       |

## HTTP API

Base path: `/internal/api/integrasjoner`

| Method | Path                          | Description                                                                                         | Request body                              | Response                                                     |
|--------|-------------------------------|-----------------------------------------------------------------------------------------------------|-------------------------------------------|--------------------------------------------------------------|
| `GET`  | `/`                           | List integrations. Optional `sourceApplicationId` filters the result.                               | –                                         | `200 OK` with `IntegrationDto[]`.                            |
| `GET`  | `/?side&antall&sorteringFelt&sorteringRetning` | Paged listing with Spring Data pagination parameters and optional `sourceApplicationId` filter. | –                                         | `200 OK` with a `Page<IntegrationDto>` payload.              |
| `GET`  | `/{integrationId}`            | Fetch a single integration by ID. Authorization rules verified when permission consumer is enabled. | –                                         | `200 OK` with an `IntegrationDto`, `404` when not found.     |
| `POST` | `/`                           | Create a new integration. Rejects duplicates per source application integration ID.                 | `IntegrationPostDto` JSON (see below).    | `200 OK` with the created `IntegrationDto`. `409` on clash.  |
| `PATCH`| `/{integrationId}`            | Apply partial updates to destination, state, or active configuration.                               | `IntegrationPatchDto` JSON.               | `200 OK` with the updated `IntegrationDto`, `422` on invalid changes. |

Example `IntegrationPostDto` payload:

```json
{
  "sourceApplicationId": 15,
  "sourceApplicationIntegrationId": "case-updates",
  "destination": "https://org.example.no/integrations/case-updates"
}
```

Validation failures yield `422 Unprocessable Entity` with aggregated error messages. When user-permission checks are active, access to non-authorized source applications returns `403 Forbidden`.

## Kafka Integration

- `ConfigurationRequestProducerService` performs request/reply lookups on the `configuration` topic to validate referenced configuration IDs.
- `IntegrationRequestConsumerConfiguration` registers consumers that answer:
  - Integration by `integration-id`.
  - Integration by `(source-application-id, source-application-integration-id)`.
- `ActiveConfigurationIdRequestConsumerConfiguration` exposes a request endpoint that resolves an integration’s current active configuration ID.

All topics use the Flyt domain context defaults with per-tenant prefixes, five-minute retention, and a 15-second reply timeout for outbound requests.

## Scheduled Tasks

The service does not define scheduled jobs; configuration validation happens inline during PATCH operations.

## Configuration

Spring profiles layer common Flyt settings: `flyt-kafka`, `flyt-logging`, `flyt-postgres`, and `flyt-resource-server`.

Key properties:

| Property                                                                | Description                                                                                 |
|-------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `fint.application-id`                                                   | Default application ID used for Kafka client IDs and topic prefixes.                        |
| `novari.kafka.topic.orgId`                                              | Overridden per kustomize overlay to scope Kafka ACLs and topic prefixes.                    |
| `novari.flyt.resource-server.user-permissions-consumer.enabled`         | Toggles per-user source application filtering and authorization checks in the controller.  |
| `fint.database.url`, `fint.database.username`, `fint.database.password` | PostgreSQL connection parameters supplied through secrets.                                  |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`                  | Identity provider for JWT validation.                                                       |
| `management.endpoints.web.exposure.include`                             | Actuator endpoints exposed (health, info, prometheus).                                      |

Secrets referenced by the base manifest must provide database credentials and OAuth client configuration.

## Running Locally

Prerequisites:

- Java 21+
- Dockerized or local PostgreSQL instance
- Kafka broker (local or containerized)

Useful commands:

```shell
./gradlew clean build     # compile sources and run tests
./gradlew test            # unit test suite
./gradlew bootRun         # start the application with Flyt profiles
```

Use `SPRING_PROFILES_ACTIVE=local-staging` to apply the local overrides in `application-local-staging.yaml`. Provide a PostgreSQL instance (defaults to `jdbc:postgresql://localhost:5438/fint-flyt-integration-service`) and a Kafka broker on `localhost:9092`.

## Deployment

Kustomize structure:

- `kustomize/base/` holds the shared Application manifest, database/env wiring, and Actuator configuration.
- `kustomize/overlays/<org>/<env>/` includes tenant-specific patches (namespace, labels, Kafka topics, and URL paths).

Templates live under `kustomize/templates/`:

- `overlay.yaml.tpl` — single source of truth for overlay content.

Regenerate overlays after editing templates:

```shell
./script/render-overlay.sh
```

The script iterates over existing overlays, substitutes organization-specific values, and rewrites `kustomization.yaml` files in place.

## Security

- OAuth2 resource server with JWT validation against `https://idp.felleskomponent.no`.
- Internal API is gated by `novari.flyt.resource-server.security.api.internal` settings; optional user-permissions consumer restricts visibility to authorized source applications.

## Observability & Operations

- Readiness probe at `/actuator/health`.
- Prometheus metrics exposed at `/actuator/prometheus`.
- Structured logging leverages standard Spring Boot and Reactor logging context.

## Development Tips

- Validation rules rely on Kafka lookups; stub `ConfigurationRequestProducerService` in tests when asserting constraint behavior.
- Flyway migrations live under `src/main/resources/db/migration`; add new scripts for schema changes instead of modifying existing ones.
- `IntegrationService` handles entity mapping—prefer updating the mapper rather than touching controller DTO conversion.

## Contributing

1. Create a topic branch for your change.
2. Run `./gradlew test` before opening a pull request.
3. If you modify kustomize content, run `./script/render-overlay.sh` and commit the generated overlays.
4. Add or update unit tests to cover new functionality.

———

FINT Flyt Integration Service is maintained by the FINT Flyt team. Reach out on the internal Slack channel or file an issue in this repository for questions or enhancement ideas.
