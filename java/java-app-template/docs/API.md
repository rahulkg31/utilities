# API Reference

Full interactive spec: Swagger UI at `/swagger-ui.html`. The raw OpenAPI document is served at `/v3/api-docs` (springdoc defaults).

This file covers conventions, request bodies and examples. Swagger UI is the source of truth for response schemas.

## Base URL

| Environment  | URL                                                          |
| ------------ | ------------------------------------------------------------ |
| Local        | http://localhost:8080                                        |
| Docker / K8s | Depends on your port mapping, Service or Ingress (see [K8S.md](https://claude.ai/chat/K8S.md)) |

Endpoints are not versioned; there is no `/api/v1` prefix.

## Auth

No authentication is configured in the application code. Put the service behind a gateway or network policy if it must be restricted.

## Conventions

- Requests and responses use `application/json`.
- Identifiers are UUIDs.
- Request bodies are validated. A body that fails validation, or a malformed UUID in the path, is rejected with `400 Bad Request`. Validation messages are listed per field below (for example `port must be <= 65535`).
- Successful create, get, search, available and assign calls return `200 OK` with a JSON body. Delete returns `204 No Content` with no body.
- Unknown JSON properties are ignored in search requests (`SearchRequest`).
- The exact error body, and the status codes for cases such as "registry not found" or "no port available", are defined by the service layer and exception handling. Check Swagger UI or `curl -i` against a running instance.

## Endpoints

### Port registries (`/ports`)

| Method | Path             | Request body    | Success response                | Purpose                         |
| ------ | ---------------- | --------------- | ------------------------------- | ------------------------------- |
| POST   | /ports           | `CreateRequest` | 200, `IdResponse`               | Create a port registry          |
| GET    | /ports/{id}      | none            | 200, `RegistryResponse`         | Get a registry by ID            |
| POST   | /ports/search    | `SearchRequest` | 200, list of `RegistryResponse` | Search registries (paged)       |
| POST   | /ports/available | `IpRequest`     | 200, `AvailabilityResponse`     | Fetch available ports for an IP |
| POST   | /ports/assign    | `IpRequest`     | 200, `AssignmentResponse`       | Assign a port for an IP         |
| DELETE | /ports/{id}      | none            | 204                             | Delete a registry               |

### Health

| Method | Path    | Success response        | Purpose                                                 |
| ------ | ------- | ----------------------- | ------------------------------------------------------- |
| GET    | /health | 200, `{"status": "UP"}` | Liveness check, also usable by `scripts/healthcheck.sh` |

Spring Boot Actuator endpoints are also available under `/actuator`; which ones are exposed depends on the Actuator configuration.

## Request Bodies

### CreateRequest

| Field        | Type    | Required | Validation                    | Description                    |
| ------------ | ------- | -------- | ----------------------------- | ------------------------------ |
| name         | string  | yes      | not blank                     | Registry name                  |
| category     | string  | yes      | not blank                     | Registry category              |
| privateIp    | string  | yes      | not blank, valid IPv4 address | Private IP the port belongs to |
| port         | integer | yes      | 1 to 65535                    | Port number                    |
| networkScope | string  | yes      | not blank                     | Network scope of the registry  |
| description  | string  | no       | none                          | Free-text description          |

```json
{
  "name": "payments-api",
  "category": "backend",
  "privateIp": "10.0.1.15",
  "port": 8443,
  "networkScope": "internal",
  "description": "Payments service HTTPS port"
}
```

### IpRequest

Used by `/ports/available` and `/ports/assign`.

| Field | Type   | Required | Validation                    | Description                              |
| ----- | ------ | -------- | ----------------------------- | ---------------------------------------- |
| ip    | string | yes      | not blank, valid IPv4 address | IP address to query or assign a port for |

```json
{ "ip": "10.0.1.15" }
```

### SearchRequest

| Field   | Type                  | Required | Default | Validation | Description                                     |
| ------- | --------------------- | -------- | ------- | ---------- | ----------------------------------------------- |
| filters | map of string to list | no       | none    | none       | Filter name mapped to a list of values to match |
| page    | integer               | no       | 0       | >= 0       | Zero-based page index                           |
| size    | integer               | no       | 10      | 1 to 100   | Page size                                       |

```json
{
  "filters": {
    "category": ["backend", "database"],
    "networkScope": ["internal"]
  },
  "page": 0,
  "size": 20
}
```

An empty body `{}` returns the first page of 10 results with no filters.

## Examples

Response bodies for `IdResponse`, `RegistryResponse`, `AvailabilityResponse` and `AssignmentResponse` are documented in Swagger UI.

### Health check

```bash
curl http://localhost:8080/health
{"status": "UP"}
```

### Create a registry

```bash
curl -X POST http://localhost:8080/ports \
  -H "Content-Type: application/json" \
  -d '{
    "name": "payments-api",
    "category": "backend",
    "privateIp": "10.0.1.15",
    "port": 8443,
    "networkScope": "internal",
    "description": "Payments service HTTPS port"
  }'
```

Returns an `IdResponse` with the ID of the new registry.

### Get a registry

```bash
curl http://localhost:8080/ports/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

### Search registries

```bash
curl -X POST http://localhost:8080/ports/search \
  -H "Content-Type: application/json" \
  -d '{"filters": {"category": ["backend"]}, "page": 0, "size": 20}'
```

### Fetch available ports

```bash
curl -X POST http://localhost:8080/ports/available \
  -H "Content-Type: application/json" \
  -d '{"ip": "10.0.1.15"}'
```

### Assign a port

```bash
curl -X POST http://localhost:8080/ports/assign \
  -H "Content-Type: application/json" \
  -d '{"ip": "10.0.1.15"}'
```

### Delete a registry

```bash
curl -X DELETE http://localhost:8080/ports/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

Returns `204 No Content`.

### Validation failure

```bash
curl -i -X POST http://localhost:8080/ports/assign \
  -H "Content-Type: application/json" \
  -d '{"ip": "999.1.1.1"}'
```

Returns `400 Bad Request` (message: `ip must be a valid IPv4 address`).
