# Startup Guide

URL shortener: Spring Boot (Kotlin) backend + React/Vite web client, fronted by
Keycloak (auth) and PostgreSQL, all wired together with Docker Compose.

## Services & URLs

| Service        | URL                              | Notes                                   |
| -------------- | -------------------------------- | --------------------------------------- |
| Web client     | http://localhost:5173            | React SPA (nginx)                       |
| Backend API    | http://localhost:8080            | Spring Boot                             |
| API docs       | http://localhost:8080/scalar     | OpenAPI JSON at `/v3/api-docs`          |
| Keycloak       | http://localhost:8083            | Admin console — `admin` / `admin`       |
| PostgreSQL     | localhost:5432                   | db `mock`, user/pass `postgres`         |

Public redirect endpoint: `http://localhost:8080/{code}` (no auth).

Keycloak comes pre-provisioned: the `mock` realm, the `mock-api` client, and a dev
user **`testuser` / `testpassword`** are auto-imported on a fresh start — no manual
setup. Details in [keycloak/README.md](keycloak/README.md).

---

## First-time clone

The web client lives in a git submodule, so pull it in before building:

```bash
git submodule update --init --recursive
```

---

## Run everything (Docker Compose)

Build images and start the full stack (postgres, keycloak, backend, web):

```bash
docker compose up -d --build
```

Then open http://localhost:5173.

Common operations:

```bash
docker compose ps                 # container status
docker compose logs -f app        # follow backend logs (app | web | keycloak | postgres)
docker compose up -d --build app  # rebuild + restart just the backend after code changes
docker compose up -d --build web  # rebuild + restart just the web client
docker compose restart app        # restart without rebuilding
docker compose down               # stop and remove containers (keeps data volume)
docker compose down -v            # stop and WIPE the database + Keycloak realm data
```

> After changing backend source or the web client, the image must be rebuilt
> (`--build`); a plain `docker compose up -d` reuses the existing image.

---

## Local development (services on the host)

Run Postgres + Keycloak in Docker, but the app and/or client directly on your
machine for fast iteration:

```bash
docker compose up -d postgres keycloak
```

### Backend

```bash
./gradlew bootRun          # starts on :8080, applies Flyway migrations
./gradlew build            # compile + test
./gradlew compileKotlin    # compile only
```

### Web client

```bash
cd url-shortening-web-client
npm install
npm run dev                # Vite dev server on :5173 (proxies /url -> :8080, /realms -> :8083)
npm run build              # typecheck + production build
npm run api:gen            # regenerate API types from the live backend OpenAPI spec
```

---

## Reset the database

Flyway rebuilds the schema on the next backend start, so to wipe app data while
keeping Keycloak intact, drop the `mock` schema:

```bash
docker exec mock psql -U postgres -d mock -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

To reset **everything** (app data + Keycloak realm/users), remove the volume:

```bash
docker compose down -v && docker compose up -d
```
