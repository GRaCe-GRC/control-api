# control-api

Inner API for controls (Controls database).

> **Status:** CRUD for control groups and controls in the `controls` schema of the `controls` database.

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)
- PostgreSQL with Spring Data JDBC, schema managed by Flyway (`src/main/resources/db/migration`)

## Getting started
Start the database from [GRaCe-GRC-sandbox](https://github.com/GRaCe-GRC/GRaCe-GRC-sandbox), then the app:

```bash
(cd ../GRaCe-GRC-sandbox/compose && docker compose up -d --wait)
./mvnw spring-boot:run
```

On startup Flyway applies any new migrations to the `controls` schema. Entities are Java records
mapped with Spring Data JDBC.

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |
| `GET http://localhost:8080/swagger-ui.html` | API documentation (OpenAPI at `/v3/api-docs`) |

## Control groups API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/control-groups` | `parentGroupId` (optional), `groupRef`, `title`, `createdBy` | `201`, empty body, `Location` header with the new group's URL |
| `GET /api/v1/control-groups` | | `200` with the list of groups; `?parentId=<id>` returns only that group's children |
| `GET /api/v1/control-groups/{id}` | | `200` with the group |
| `PUT /api/v1/control-groups/{id}` | `parentGroupId` (optional), `groupRef`, `title` | `200` with the group (`createdAt` and `createdBy` never change) |
| `DELETE /api/v1/control-groups/{id}` | | `204` |

Errors use RFC 9457 problem details: `400` for invalid input (with an `errors` field per
property), `404` for an unknown id or `parentGroupId`, `409` for a duplicate `groupRef` or for
deleting a group that still has child groups or controls.

```bash
curl -s -H 'Content-Type: application/json' \
  -d '{"groupRef":"AC","title":"Access Control","createdBy":"me"}' \
  http://localhost:8080/api/v1/control-groups
```

## Controls API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/controls` | `groupId` (optional), `parentControlId` (optional), `controlRef`, `title`, `statement` (optional), `createdBy` | `201`, empty body, `Location` header with the new control's URL |
| `GET /api/v1/controls` | | `200` with the list of controls; `?groupId=<id>` returns a group's controls, `?parentId=<id>` a control's enhancements (both can be combined) |
| `GET /api/v1/controls/{id}` | | `200` with the control |
| `PUT /api/v1/controls/{id}` | `groupId` (optional), `parentControlId` (optional), `controlRef`, `title`, `statement` (optional) | `200` with the control (`createdAt` and `createdBy` never change) |
| `DELETE /api/v1/controls/{id}` | | `204` |

An enhancement is a control whose `parentControlId` is another control (for example `AC-2(1)`
under `AC-2`). `PUT` replaces every field it accepts, so an omitted optional field is cleared.
Errors: `400` for invalid input, `404` for an unknown id, `groupId` or `parentControlId`, `409`
for a duplicate `controlRef` or for deleting a control that still has enhancements.

```bash
curl -s -H 'Content-Type: application/json' \
  -d '{"controlRef":"AC-2","title":"Account Management","createdBy":"me"}' \
  http://localhost:8080/api/v1/controls
```

## Database

| Environment variable | Default |
|---|---|
| `CONTROL_DB_HOST` | `localhost` |
| `CONTROL_DB_PORT` | `5432` |
| `CONTROL_DB_NAME` | `controls` |
| `CONTROL_DB_USERNAME` | `control_api` |
| `CONTROL_DB_PASSWORD` | `control_api` |

The defaults match the sandbox and are for local use only. The app uses only the `controls`
schema; `control_mappings` in the same database belongs to control-mapping-api
([ADR 0006](https://github.com/GRaCe-GRC/GRaCe-GRC-docs/blob/main/adr/0006-controls-database-schemas.md)).

| Table | Migration |
|---|---|
| `control_groups` | `V1__create_control_groups.sql` |
| `controls` | `V2__create_controls.sql` |

## Test and build
```bash
./mvnw verify
```

Branch verify runs the same command on every push to a branch and on every pull request, through the
shared [Maven CI workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/maven-ci.yml) (`.github/workflows/branch-verify.yml`).

## Versions and images
Versions follow semantic versioning and come from the commit messages. Every merge to `main`
runs `.github/workflows/release.yml`: the CI again, then the shared
[release workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/README.md#release):

| Commit | Release |
|---|---|
| `fix: GRC-<n>: ...` | Patch |
| `feat: GRC-<n>: ...` | Minor |
| `chore:`, `docs:`, ... | No release |

A release creates the `vX.Y.Z` tag, a GitHub Release with notes, and the image
`ghcr.io/grace-grc/control-api:<version>` (also tagged `latest`). The version is not stored in
`pom.xml`: it uses `${revision}`, which is `0.0.1-SNAPSHOT` for local builds and the release
version in CI.

```bash
docker pull ghcr.io/grace-grc/control-api:latest
```

## Architecture
This repository is part of the **inner API layer**: services reach the system of records only through it.
Other projects use it through internal service APIs and events, referring to its entities by ID.
See the [GRaCe-GRC documentation hub](https://github.com/GRaCe-GRC/GRaCe-GRC-docs).

## Contributing
See the organization's [contributing guide](https://github.com/GRaCe-GRC/.github/blob/main/CONTRIBUTING.md).
Commit messages use Conventional Commits with the ticket ID, for example
`feat: GRC-13: add <change>`.

## License
Licensed under the [Apache License 2.0](LICENSE).
