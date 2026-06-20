# Balancify — Expense API

A Spring Boot 4 REST API for tracking group expenses and settling debts. Supports equal, percentage, and exact-amount split strategies with JWT-secured endpoints.

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 4.1 (Java 21) |
| Database | PostgreSQL 16 + Hibernate |
| Migrations | Liquibase |
| Auth | Keycloak (OAuth2/OIDC JWT) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build | Maven |

## Architecture

```
application/          # Use cases, commands, queries
domain/               # Entities (Expense, Group, User), value objects, domain services
infrastructure/       # REST controllers, persistence adapters, security
```

## API Endpoints

### Expenses — `/api/v1/expenses`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Record a new expense |
| GET | `/` | Query expenses (filters: groupId, payerId, startDate, endDate, settled) |
| GET | `/{id}` | Get single expense |
| PATCH | `/{id}/settle` | Mark expense as settled |
| DELETE | `/{id}` | Delete expense |

### Groups — `/api/v1/groups`
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List groups for current user |
| GET | `/{groupId}` | Get group details |
| POST | `/` | Create a new group |
| GET | `/{groupId}/balances` | Calculate minimized settlement transfers |
| GET | `/{groupId}/expenses` | Get group's expenses (paginated) |
| GET | `/{groupId}/members` | List group members with names |
| POST | `/{groupId}/members` | Add a member |
| DELETE | `/{groupId}/members/{userId}` | Remove a member |
| PATCH | `/{groupId}` | Rename group |

### Settlements — `/api/v1/settlements`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Record a cash settlement between two users |
| GET | `/?userId=` | List settlements for a user |

### Dashboard — `/api/v1/dashboard`
| Method | Path | Description |
|--------|------|-------------|
| GET | `/balances` | Get overall balance summary for current user |
| GET | `/{groupId}/simplified-debts` | Get simplified debts for a group |

## Split Strategies

When recording an expense, choose one of three split strategies:

- **`EQUAL`** — Splits evenly among all participants
- **`PERCENTAGE`** — Each participant has a defined percentage (must sum to 100%)
- **`EXACT`** — Each participant has an exact dollar amount (must sum to total)

## Quick Start

### Prerequisites
- Java 21
- Docker (for local infrastructure)

### 1. Start infrastructure
```bash
cd config
docker compose up -d postgres keycloak keycloak-config-cli
```

### 2. Run the API
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Run tests
```bash
./mvnw test
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/expense_db` | PostgreSQL connection URL |
| `DATABASE_USERNAME` | `postgres` | Database username |
| `DATABASE_PASSWORD` | `postgres` | Database password |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8180/realms/expense` | Keycloak JWT issuer URI |

## Docker

```bash
# Build
docker build -t expense-api .

# Run
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/expense_db \
  -e KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/expense \
  expense-api
```
