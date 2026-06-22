# BlockEndCall

Community-powered call blocker. Report a spam number once — it gets blocked for every user on the platform automatically.

## Architecture

```
┌─────────────────┐     HTTPS      ┌──────────────────────────────────┐
│  Android App    │ ─────────────► │  Spring Boot API  (port 8080)    │
│  (Java, API 29) │                │  Spring Security + JWT           │
└─────────────────┘                └────────────┬─────────────────────┘
       │                                        │
       │ CallScreeningService                   ├── PostgreSQL 16
       │ (intercepts calls, auto-blocks         └── Redis 7 (cache)
       │  confirmed spam numbers)
       ▼
  System dialer
```

## Tech Stack

| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Backend      | Java 17, Spring Boot 3.2, Spring Security 6     |
| Auth         | JWT (jjwt 0.12)                                 |
| Database     | PostgreSQL 16 + Flyway migrations               |
| Cache        | Redis 7 (number-check results cached 5 min)     |
| API Docs     | OpenAPI 3 / Swagger UI                          |
| Android      | Java, Android API 29+, Retrofit 2, Material 3   |
| CI           | GitHub Actions                                  |
| Infra        | Docker + Docker Compose                         |

## How It Works

1. User reports a spam number via the app (POST `/api/v1/numbers/report`)
2. The backend stores the report and increments the report counter
3. When a number reaches **5 reports** it is auto-confirmed as spam
4. Every user with the app set as their Call Screening app will have confirmed numbers **silently rejected** before the phone rings
5. Number check results are cached in Redis for 5 minutes to minimize latency during calls

## Quick Start (Docker)

```bash
docker compose up -d
# API:      http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
```

## Running Locally

**Prerequisites:** Java 17, Maven, PostgreSQL, Redis

```bash
cd backend
mvn spring-boot:run
```

## API Endpoints

| Method | Path                            | Auth     | Description                    |
|--------|---------------------------------|----------|--------------------------------|
| POST   | /api/v1/auth/register           | Public   | Create account                 |
| POST   | /api/v1/auth/login              | Public   | Get JWT token                  |
| GET    | /api/v1/numbers/check/{phone}   | Public   | Check if number is blocked     |
| GET    | /api/v1/numbers                 | Bearer   | List confirmed blocked numbers |
| POST   | /api/v1/numbers/report          | Bearer   | Report a spam number           |
| GET    | /api/v1/numbers/{id}            | Bearer   | Get number detail              |
| DELETE | /api/v1/numbers/{id}            | Admin    | Delete a number                |

## Android Setup

1. Open `android/` in Android Studio
2. Set `BASE_URL` in `app/build.gradle` to your backend address
3. Run on a device with API 29+
4. Grant the app the **Call Screening** role when prompted

## Spam Categories

`TELEMARKETING` · `SCAM` · `ROBOCALL` · `DEBT_COLLECTOR` · `PHISHING` · `UNKNOWN`
