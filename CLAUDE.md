# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FinHub is a **MSA-based personal integrated financial platform** (MSA 기반 개인 통합 금융 플랫폼) built with Java 17, Spring Boot 3.3.0, and Spring Cloud 2023.0.2. It uses a Maven multi-module structure with 8 microservices.

## Build & Test Commands

```bash
# Build all modules
./mvnw clean install

# Build a single service
./mvnw clean install -pl finhub-user

# Run all tests
./mvnw test

# Run tests for a single service
./mvnw test -pl finhub-banking

# Run a specific test class
./mvnw test -pl finhub-user -Dtest=FinhubUserApplicationTests

# Package without running tests
./mvnw package -DskipTests

# Run a service (after building)
./mvnw spring-boot:run -pl finhub-user
```

## Service Architecture

| Service | Port | Description |
|---|---|---|
| finhub-eureka | 8761 | Eureka service registry |
| finhub-gateway | 8080 | Spring Cloud Gateway (entry point) |
| finhub-user | 8081 | User management & JWT auth |
| finhub-banking | 8082 | Account & transaction management |
| finhub-investment | 8083 | Investment portfolio management |
| finhub-payment | 8084 | Payment processing |
| finhub-insurance | 8085 | Insurance product management |
| finhub-search | 8086 | Elasticsearch-based full-text search |
| finhub-notification | 8088 | Kafka consumer for notifications |

**Startup order**: eureka → gateway → all domain services

## Maven Multi-Module Structure

All modules inherit from the root `pom.xml` (`com.finhub:finhub:1.0.0-SNAPSHOT`), which provides:
- Spring Boot 3.3.0 parent
- Spring Cloud 2023.0.2 BOM
- Common deps: `lombok`, `spring-boot-starter-test`

**Do not** add `<parent>spring-boot-starter-parent</parent>` or `<dependencyManagement>` in module pom.xml files — these are managed by the root pom.

### Spring Boot 3 Starter Names (vs Spring Boot 4)
| Use this (SB3) | NOT this (SB4) |
|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| `org.flywaydb:flyway-core` | `spring-boot-starter-flyway` |
| `org.springframework.kafka:spring-kafka` | `spring-boot-starter-kafka` |
| `spring-cloud-starter-gateway-mvc` | `spring-cloud-starter-gateway-server-webmvc` |

**Eureka Server 주의사항**: `spring-cloud-starter-netflix-eureka-server`만으로는 기동 불가. `spring-boot-starter-web`을 반드시 함께 추가해야 `TransportClientFactories` 빈 생성 가능. `com.sun.jersey:jersey-bundle`은 1.x 아티팩트로 추가 금지.

## Infrastructure Dependencies

All services require these to be running locally:
- **PostgreSQL** on `localhost:5432` — separate database per service (`finhub_user`, `finhub_banking`, etc.), credentials: `postgres/postgres`
- **Kafka** on `localhost:9092` — used by banking, investment, payment (producers) and notification (consumer, group-id: `notification-group`)
- **Redis** on default port — used by finhub-user and finhub-notification
- **Elasticsearch** on `http://localhost:9200` — used by finhub-search, credentials: `elastic/elastic`

## Key Architectural Patterns

### API Routing
All external traffic enters through the gateway at port 8080. Routes follow `/api/v1/<service>/**`:
- `/api/v1/users/**` → lb://finhub-user
- `/api/v1/banking/**` → lb://finhub-banking
- etc.

### Authentication
- JWT tokens issued by finhub-user
- Secret key configured in `finhub-user/src/main/resources/application.yml`
- Access token: 30 min, Refresh token: 7 days
- All services integrate Spring Security; inter-service calls use JWT

### Database Migrations
Each service uses **Flyway** with migrations in `src/main/resources/db/migration/`. JPA DDL is set to `validate` — schema must be created via Flyway migrations.

### Event-Driven Communication
Kafka is used for async inter-service communication. Banking, investment, and payment services publish domain events; notification service consumes them using JSON deserialization with wildcard trusted packages.

### Package Structure Convention
Each service uses `com.finhub.<service>` as the base package. The standard layered structure to follow:
```
com.finhub.<service>/
├── controller/
├── service/
├── repository/
├── domain/ (or entity/)
├── dto/
└── config/
```

## Lombok

All services include Lombok. Use `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, etc. to reduce boilerplate.
