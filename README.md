# Sistema de Gestión Administrativa - Hostal

Sistema web interno para gestión administrativa de un hostal con dos sedes. Registra ingresos, egresos y calcula ganancias por sede.

## Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| Frontend | Vite + React 18 + TailwindCSS |
| Backend | Spring Boot 3.3.5 (Java 17) |
| Base de datos | PostgreSQL (Supabase) |
| Autenticación | Spring Security + JWT (BCrypt) |
| Despliegue | Vercel (frontend) + Render (backend) |

## Requisitos

- Java 17+
- Node.js 18+
- Docker (opcional)

## Instalación Local

### 1. Base de datos

```bash
docker-compose up -d
```

Esto levanta PostgreSQL en `localhost:5432` con el esquema y datos semilla.

### 2. Backend

```bash
cd backend
cp ../.env.example .env
./mvnw spring-boot:run
```

El backend corre en `http://localhost:8080`. Swagger UI disponible en `http://localhost:8080/swagger-ui.html`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

El frontend corre en `http://localhost:5173`.

## Usuarios de Prueba

| Usuario | Contraseña | Sede |
|---------|-----------|------|
| `centro` | `centro123` | Sede Centro |
| `norte` | `norte123` | Sede Norte |

## API Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login (público) |
| GET | `/api/auth/me` | Info del usuario autenticado |
| GET | `/api/dashboard` | Resumen financiero |
| POST | `/api/ingresos` | Registrar ingreso |
| POST | `/api/ingresos/{id}/anular` | Anular ingreso |
| POST | `/api/egresos` | Registrar egreso |
| POST | `/api/egresos/{id}/anular` | Anular egreso |
| GET | `/api/movimientos` | Listar movimientos (paginado) |
| GET | `/api/reportes/rango` | Reporte por rango de fechas |
| GET | `/api/reportes/ingresos-por-habitacion` | Ingresos agrupados |
| GET | `/api/reportes/egresos-por-categoria` | Egresos agrupados |
| GET | `/api/reportes/exportar-csv` | Exportar CSV |
| GET | `/actuator/health` | Health check |

## Despliegue

### Backend (Render)

1. Crear Web Service en Render (Starter always-on)
2. Configurar variables de entorno:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
   - `SPRING_PROFILES_ACTIVE=prod`
   - `JWT_SECRET` (≥256 bits base64)
   - `CORS_ORIGINS=https://tu-app.vercel.app`
3. Build Command: `cd backend && mvn clean package -DskipTests`
4. Start Command: `java -jar backend/target/*.jar`

### Frontend (Vercel)

1. Importar repositorio en Vercel
2. Framework Preset: Vite
3. Build Command: `cd frontend && npm run build`
4. Output Directory: `frontend/dist`
5. Variable de entorno: `VITE_API_URL=https://tu-app.onrender.com`

## Respaldo Automático

Workflow de GitHub Actions ejecuta `pg_dump` diariamente a las 3:00 AM UTC. Configurar el secret `SUPABASE_DB_URL` en el repositorio.

## Estructura del Proyecto

```
hostal-system/
├── backend/           # Spring Boot API
│   ├── src/main/java/com/hostal/sistema/
│   │   ├── config/    # Security, JWT, CORS, OpenAPI
│   │   ├── controller/# REST controllers
│   │   ├── dto/       # Request/Response DTOs
│   │   ├── exception/ # Exception handling
│   │   ├── model/     # JPA entities
│   │   ├── repository/# Data repositories
│   │   ├── security/  # UserDetailsService
│   │   └── service/   # Business logic
│   └── Dockerfile
├── frontend/          # React SPA
│   └── src/
│       ├── api/       # Axios instance
│       ├── components/# Layout, Sidebar, etc.
│       ├── context/   # Auth context
│       └── pages/     # Page components
├── database/          # SQL schema + seed data
├── .github/workflows/ # Backup automation
├── docker-compose.yml
└── .env.example
```
