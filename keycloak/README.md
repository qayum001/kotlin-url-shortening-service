# Keycloak realm bootstrap

[`mock-realm.json`](mock-realm.json) is the pre-created realm the app needs. It is
mounted into the Keycloak container at `/opt/keycloak/data/import` and imported on
startup via `start-dev --import-realm` (see [`../docker-compose.yml`](../docker-compose.yml)).

It defines:

- realm **`mock`** (self-registration enabled)
- public client **`mock-api`** — authorization-code + PKCE and direct-access grants,
  redirect URIs `http://localhost:5173/*`, web origins
- a ready-to-use dev user — **`testuser` / `testpassword`**

Keycloak admin console: http://localhost:8083 — **`admin` / `admin`**.

## Import behavior

The import strategy is **IGNORE_EXISTING**: on a **fresh** Keycloak the realm is
created automatically; if a `mock` realm already exists it is **left untouched**
(the log shows `Realm 'mock' already exists. Import skipped`). So this never
clobbers a realm you have changed at runtime.

## Force a re-import (after editing `mock-realm.json`)

Because existing realms are skipped, clear the Keycloak database first, then restart:

```bash
docker compose stop keycloak
docker exec mock psql -U postgres -c "DROP DATABASE keycloak WITH (FORCE);"
docker exec mock psql -U postgres -c "CREATE DATABASE keycloak;"
docker compose up -d keycloak
```

(Or `docker compose down -v && docker compose up -d` to reset everything.)

## Regenerate this file from a running Keycloak

```bash
TOKEN=$(curl -s -d client_id=admin-cli -d username=admin -d password=admin \
  -d grant_type=password \
  http://localhost:8083/realms/master/protocol/openid-connect/token | jq -r .access_token)
curl -s -H "Authorization: Bearer $TOKEN" \
  -X POST "http://localhost:8083/admin/realms/mock/partial-export?exportClients=true&exportGroupsAndRoles=true" \
  -o keycloak/mock-realm.json
```

The admin API does not export user credentials, so re-add the `testuser` block
(with a plaintext `password` credential) afterwards.
