# Track Flow

A logistics shipment tracking platform built as a full-stack study project: Java/Spring on the backend, Angular/NgRx on the frontend. Operations staff create and update shipments from a dark, data-dense console; customers track any shipment live, without an account.

## The Problem

A courier company needs a way to record shipments and their status as they move from pickup to delivery, and a way for customers to follow their package in real time — without exposing internal systems or requiring a customer login.

## Why This Project

Built to practice full-stack development end to end, with a deliberate architectural choice carried through both sides of the stack: a **Modular Monolith**, organized by business capability (`shipment`, `auth`, `user`) rather than by technical layer, with real-time updates propagated through Spring's internal domain events on the backend and a per-feature NgRx store on the frontend — no external message broker, because this project's scale doesn't need one.

## Architecture

```
Backend (Spring Boot)                  Frontend (Angular)
┌───────────────────────────┐          ┌───────────────────────────┐
│ Controllers + WebSocket    │  REST +  │ HTTP + WebSocket services │
│ (STOMP endpoints)          │◄───────► │ (HttpClient, RxJS/STOMP)  │
├───────────────────────────┤  STOMP   ├───────────────────────────┤
│ Services                   │          │ NgRx effects               │
│ (business logic, publishes │          │ (side effects, API calls) │
│  domain events)            │          ├───────────────────────────┤
├───────────────────────────┤          │ NgRx store                 │
│ Repositories (Spring Data  │          │ (actions, reducers,        │
│  JPA)                      │          │  selectors, entity)        │
├───────────────────────────┤          ├───────────────────────────┤
│ PostgreSQL                 │          │ Components                 │
│                             │          │ (standalone, Material)     │
└───────────────────────────┘          └───────────────────────────┘
```

When a shipment's status changes: the service persists the change and publishes a `ShipmentStatusChangedEvent`; a `@TransactionalEventListener(phase = AFTER_COMMIT)` picks it up — deliberately *after* the transaction commits, so a broadcast can never announce a change that ends up rolled back — and broadcasts it over STOMP to `/topic/shipments/{trackingCode}`. On the frontend, a shared `ShipmentSocketService` subscribes per tracking code and feeds events back into the NgRx store, so any open screen (the ops list, the detail dialog, the public tracking page) updates itself without polling or a manual refresh.

## Tech Stack

| Concern | Technology |
|---|---|
| Backend language / runtime | Java 21, Spring Boot 4.1 |
| Backend build tool | Maven |
| Database | PostgreSQL, Flyway |
| Real-time | Spring WebSocket (STOMP, no SockJS) |
| Auth | Self-issued JWT (Spring Security, HMAC/HS256) |
| Backend testing | Testcontainers (PostgreSQL), Spring Boot Test |
| Frontend framework | Angular 22 (standalone components) |
| State management | NgRx (store, effects, entity, store-devtools) |
| UI | Angular Material (custom dark theme) |
| Forms | Reactive Forms |
| Real-time client | @stomp/stompjs |

## Repository Structure

```
track-flow/
  backend/     Spring Boot API — see backend/README.md for full API reference,
               setup, and testing instructions
  frontend/    Angular application
```

## Running Locally

### 1. Backend

```bash
cd backend
cp .env.example .env   # fill in DB credentials and JWT_SECRET (openssl rand -base64 32)
docker compose up -d   # starts PostgreSQL
./mvnw spring-boot:run
```

Flyway applies all migrations on startup, including a seeded admin account:

```
email:    admin@trackflow.dev
password: ChangeMe123!
```

Change this immediately outside of local development. Full API reference, WebSocket testing instructions, and backend architecture notes live in [`backend/README.md`](backend/README.md).

### 2. Frontend

```bash
cd frontend
npm install
ng serve
```

Open `http://localhost:4200`. The Angular CLI dev server proxies `/api` and `/ws` to the backend on `localhost:8080` (see `frontend/proxy.conf.json`), so no CORS configuration is needed in development.

## Feature Tour

- **Operations console** (`/`, authenticated) — dark sidebar layout with live stat cards (total / in transit / delivered / cancelled, computed from real state, not fabricated metrics), a sortable and filterable shipment table with pagination, and a status pulse animation on rows when a WebSocket update arrives.
- **Create and update shipments** — modal forms (Angular Material `MatDialog`) instead of full page navigations for quick, in-context actions. Cancelled shipments can no longer be updated, reflected in the UI with a disabled, tooltipped lock icon.
- **Shipment detail** — clicking a tracking code opens a dialog with the current progress (a stepper across Created → In Transit → Delivered, or a cancelled state) and the full event history, without leaving the list.
- **Public tracking page** (`/track`) — accessible without login; enter a tracking code to see status and history, also updating live over WebSocket.
- **Authentication** — email/password login issuing a self-signed JWT; the token is persisted in `localStorage` and read synchronously into the NgRx store's initial state (avoiding a race condition between the route guard and session restoration on page reload); an `HttpInterceptorFn` attaches it to every `/api` request automatically.
- **User management** (`/admin/users`, admin only) — create users, change roles (`ADMIN` / `OPS`), and deactivate/reactivate accounts (soft delete, preserving audit history). Guarded both by route (`adminGuard`, decoding the JWT's `role` claim) and, for real, by the backend's `hasRole("ADMIN")` check.

## Testing

- Backend: `cd backend && ./mvnw verify` — runs the full integration test suite against ephemeral PostgreSQL containers via Testcontainers, including a real end-to-end WebSocket test (opens a STOMP session and asserts a broadcasted event is received).
- Frontend: no automated test suite yet — a natural next step for this project.

## License

Personal study project. No license restrictions — feel free to use it as a reference.