# Track Flow — Backend

A logistics shipment tracking API built as a hands-on study project to practice full-stack development with Java/Spring and (soon) Angular. Operations staff create and update shipments; customers track them live over WebSocket without needing an account.

## The Problem

A courier company needs to track shipments from pickup to delivery, giving operations staff a way to record status changes and giving customers a way to follow their package in real time — without exposing any internal system or requiring customers to log in.

## Architecture

This project follows a **Modular Monolith**: the codebase is organized by business capability (`shipment`, `auth`), not by technical layer. Each module owns its entities, repository, service, and controller; modules don't reach into each other's internals directly.

Within the `shipment` module, status changes are decoupled using **Spring's internal domain events** (`ApplicationEventPublisher`), not an external message broker — this project doesn't need distributed messaging, so a broker would be unjustified complexity. When a shipment's status changes:

1. `ShipmentService` persists the change and publishes a `ShipmentStatusChangedEvent`.
2. A `@TransactionalEventListener(phase = AFTER_COMMIT)` picks it up — deliberately *after* the database transaction commits, so a WebSocket broadcast can never announce a change that ends up rolled back.
3. The listener broadcasts the event over STOMP to `/topic/shipments/{trackingCode}`.

```
Controller → Service (business logic, publishes domain event)
                 │
                 ├──► Repository → PostgreSQL
                 │
                 └──► TransactionalEventListener (after commit) → WebSocket broadcast
```

## Tech Stack

| Concern | Technology |
|---|---|
| Language / Runtime | Java 21 |
| Framework | Spring Boot 4.1 |
| Build tool | Maven |
| Database | PostgreSQL |
| DB versioning | Flyway |
| Real-time | Spring WebSocket (STOMP, no SockJS) |
| Auth | Self-issued JWT (Spring Security, HMAC/HS256) |
| Testing | Testcontainers (PostgreSQL), Spring Boot Test |

## Modules

- **`shipment`** — shipments, tracking events, REST API, WebSocket broadcasting.
- **`auth`** — login, JWT issuance, admin-managed user accounts (create, list, promote/demote, activate/deactivate).

## Running Locally

1. Copy `.env.example` to `.env` and fill in the values (`DB_USER`, `DB_PASSWORD`, `DB_NAME`, `JWT_SECRET`). Generate a strong secret for `JWT_SECRET`:
   ```bash
   openssl rand -base64 32
   ```
2. Start PostgreSQL:
   ```bash
   docker compose up -d
   docker compose ps   # wait for "healthy"
   ```
3. Run the application from IntelliJ (`BackendApplication`), or via Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

The `.env` file is loaded automatically at startup via `spring-dotenv` — no manual environment variable setup needed.

On startup, Flyway applies all migrations, including seeding a default admin account (see below).

## Database Migrations

| Version | Description |
|---|---|
| `V1` | `shipments` and `tracking_events` tables |
| `V2` | `users` table |
| `V3` | Seeds the default admin account |

## Default Admin Account

Since only an admin can create other users, one is seeded via migration to bootstrap the system:

```
email:    admin@trackflow.dev
password: ChangeMe123!
```

Change this password immediately in any environment beyond local development. There's currently no self-service "change my password" endpoint — an admin would need to be created fresh with a new password, or a password-reset feature added.

## API Reference

Base URL: `http://localhost:8080`

### Auth

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Authenticates with email/password, returns a JWT |

### Users (admin only)

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/users` | ADMIN | Create a new user (email, password, role) |
| `GET` | `/api/users` | ADMIN | List all users |
| `PATCH` | `/api/users/{id}/role` | ADMIN | Change a user's role (`ADMIN` / `OPS`) |
| `PATCH` | `/api/users/{id}/deactivate` | ADMIN | Deactivate an account (soft delete — blocks login, preserves history) |
| `PATCH` | `/api/users/{id}/activate` | ADMIN | Reactivate an account |

### Shipments

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/shipments` | Authenticated | Create a shipment |
| `PUT` | `/api/shipments/{trackingCode}/status` | Authenticated | Update a shipment's status |
| `GET` | `/api/shipments/{trackingCode}` | Public | Look up a shipment (customer tracking) |
| `GET` | `/api/shipments/{trackingCode}/history` | Public | Full chronological event history |

## Testing the API

### 1. Log in

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@trackflow.dev", "password": "ChangeMe123!"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
```

### 2. Create a shipment

```bash
curl -i -X POST http://localhost:8080/api/shipments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"origin": "São Paulo", "destination": "Rio de Janeiro", "carrier": "Correios"}'
```

Returns `201 Created` with the shipment, including a generated `trackingCode` (e.g. `TF1A2B3C4D5E`).

### 3. Update its status

```bash
curl -i -X PUT http://localhost:8080/api/shipments/TF1A2B3C4D5E/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status": "IN_TRANSIT", "location": "São Paulo Hub", "description": "Departed origin facility"}'
```

### 4. Track it (no auth needed — this is the public customer-facing endpoint)

```bash
curl http://localhost:8080/api/shipments/TF1A2B3C4D5E
curl http://localhost:8080/api/shipments/TF1A2B3C4D5E/history
```

### 5. Manage users (admin only)

```bash
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"email": "ops1@trackflow.dev", "password": "password123", "role": "OPS"}'

curl http://localhost:8080/api/users -H "Authorization: Bearer $TOKEN"
```

## Testing the WebSocket

The WebSocket endpoint is `ws://localhost:8080/ws` (STOMP, no SockJS fallback). Clients subscribe to `/topic/shipments/{trackingCode}` to receive live updates whenever that shipment's status changes.

A quick way to test manually is with a small Node script using `@stomp/stompjs`:

```javascript
import { Client } from '@stomp/stompjs';
import WebSocket from 'ws';

const client = new Client({
  webSocketFactory: () => new WebSocket('ws://localhost:8080/ws'),
  onConnect: () => {
    client.subscribe('/topic/shipments/TF1A2B3C4D5E', (message) => {
      console.log('Received:', JSON.parse(message.body));
    });
  },
});

client.activate();
```

Run this, then trigger a status update via `curl` (step 3 above) in another terminal — the message should print immediately.

## Running Tests

- `./mvnw test` — fast unit/context tests only, no Docker required.
- `./mvnw verify` — runs the full suite, including integration tests (`*IT`) that spin up ephemeral PostgreSQL containers via Testcontainers. Requires Docker to be running.

## Known Gotchas (Worth Remembering)

- **Flyway needs a per-database module.** Since Flyway 10, `flyway-core` alone doesn't support any specific database — `flyway-database-postgresql` must be added explicitly.
- **Self-issued JWTs with a symmetric key need the algorithm declared twice.** Once on the key itself (`OctetSequenceKey.algorithm(JWSAlgorithm.HS256)`, wrapped in `ImmutableJWKSet`, not a bare `ImmutableSecret`), and once when building the token (`JwtEncoderParameters.from(jwsHeader, claims)` — the single-argument `from(claims)` overload silently assumes RS256). Missing either one fails with `JwtEncodingException: Failed to select a JWK signing key`, which surfaces to callers as an opaque `401 Unauthorized` with an empty body — always check the *server* log, not just the HTTP status, when authentication seems to fail for no obvious reason.
- **`@TransactionalEventListener(phase = AFTER_COMMIT)`, not `@EventListener`.** A plain listener would fire mid-transaction, before Spring knows whether the change will actually be committed — risking a WebSocket broadcast for a status change that later gets rolled back.

## License

Personal study project. No license restrictions — feel free to use it as a reference.
