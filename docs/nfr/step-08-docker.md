# Step 8 — NFR-OPS-02: Docker + Docker Compose

**NFR IDs:** NFR-OPS-02
**Effort:** ~1.5 hrs | **Dependencies:** Step 3 (prod profile for env vars)
**Commit:** `feat(devops): add Dockerfile + docker-compose (NFR-OPS-02)`

---

## SRS Requirement

> Environment parity: development (Docker Compose), staging (K8s), production (K8s).

## Current State

No `Dockerfile`, `docker-compose.yml`, or `.dockerignore` files in the project.

## Implementation

### New File: `taskhive-backend/Dockerfile`

Multi-stage build for minimal image size:

```dockerfile
# ── Stage 1: Build ───────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ─────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S taskhive && adduser -S taskhive -G taskhive
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/uploads /app/logs && chown -R taskhive:taskhive /app

USER taskhive
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### New File: `taskhive-frontend/Dockerfile`

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

EXPOSE 3000
ENV HOSTNAME="0.0.0.0"
CMD ["node", "server.js"]
```

### New File: `docker-compose.yml` (project root)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: taskhive
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-ONLY", "pg_isready", "-U", "postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./taskhive-backend
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/taskhive
      DB_USERNAME: postgres
      DB_PASSWORD: password
      JWT_SECRET: DockerComposeDevSecretKey2026ForLocalDevelopmentOnly
      COOKIE_DOMAIN: localhost
      MAIL_HOST: smtp.gmail.com
      MAIL_USERNAME: ${MAIL_USERNAME:-test@gmail.com}
      MAIL_PASSWORD: ${MAIL_PASSWORD:-test}
      FRONTEND_URL: http://localhost:3000
      ADMIN_EMAIL: admin@taskhive.com
      ADMIN_PASSWORD: Admin@123
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - uploads:/app/uploads

  frontend:
    build: ./taskhive-frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8081
    depends_on:
      - backend

volumes:
  postgres_data:
  uploads:
```

### New File: `.dockerignore` (both backend + frontend)

**Backend `.dockerignore`:**
```
target/
.git
*.md
.idea/
*.iml
```

**Frontend `.dockerignore`:**
```
node_modules/
.next/
.git
*.md
```

## Verification

```bash
# Build and run everything
docker-compose up --build

# Test
curl http://localhost:8081/actuator/health
curl http://localhost:3000

# Clean up
docker-compose down -v
```
