# DevolucionesApp

MVP de plataforma de devoluciones (prueba técnica Full Stack — Java 21 / Angular 17).

## Requisitos

- JDK 21
- Maven 3.9+
- Node 18.19+ / 20.11+ (o compatible)
- Angular CLI 17 (`npx @angular/cli@17`)
- Docker + Docker Compose
- PostgreSQL 16 (vía Docker)

## Cómo levantar (desarrollo local)

```bash
cp .env.example .env
docker compose up -d db
cd backend && ./mvnw spring-boot:run
cd frontend && npm start
```

- API: http://localhost:8080/actuator/health  
- UI: http://localhost:4200  

> En Windows PowerShell usa `.\mvnw.cmd` en lugar de `./mvnw`.

## Estructura

```
devoluciones-app/
├── backend/     Spring Boot 3.3 · Java 21 · Flyway · PostgreSQL
├── frontend/    Angular 17 (standalone)
├── docs/        CSV de ejemplo y colección .http
├── docker-compose.yml
└── .env.example
```

## Estado del proyecto

Esqueleto inicial del monorepo. Próximos commits: dominio (máquina de estados), API REST, carga CSV y frontend.

## Usuarios seed (próximamente)

| Usuario       | Rol         |
|---------------|-------------|
| analista1     | ANALISTA    |
| supervisor1   | SUPERVISOR  |

## Decisiones de diseño

Se documentarán a medida que se implemente cada parte (máquina de estados, transaccionalidad de la carga, JWT, RxJS vs signals).
