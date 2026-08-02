# Sistema Hostal - Guía para Desarrolladores y Contexto de IA

Este documento sirve como regla principal (System Prompt) para cualquier IA que interactúe con el proyecto y como guía rápida para desarrolladores. Contiene el estado actual, arquitectura y reglas del sistema.

## Comandos Rápidos

```bash
# Iniciar desarrollo local (backend + frontend sin Docker)
iniciar.bat

# Detener desarrollo local
detener.bat

# Solo backend (Local)
cd backend && mvn spring-boot:run

# Solo frontend (Local)
cd frontend && npm run dev

# Despliegue Completo con Docker (Producción / Local)
docker compose up -d --build
```

## Arquitectura del Proyecto

El sistema es un gestor de ingresos/egresos para un hostal con autenticación. 
La aplicación está completamente "dockerizada".

```
hostal-system/
├── backend/                    # Spring Boot 3.3.5 + Java 17
│   ├── Dockerfile              # Usa eclipse-temurin:17
│   └── src/main/java/com/hostal/sistema/
│       ├── config/             # Security, JWT, CORS, OpenAPI
│       ├── controller/         # Endpoints REST (6 controllers)
│       ├── dto/                # Request/Response objects (12 DTOs)
│       ├── exception/          # Manejo global de errores
│       ├── model/              # Entidades JPA (4: Sede, Usuario, Ingreso, Egreso)
│       ├── repository/         # Consultas JPQL (3 repositories)
│       ├── security/           # UserDetailsService
│       └── service/            # Lógica de negocio (6 services)
│
├── frontend/                   # Vite + React 18 + TailwindCSS
│   ├── Dockerfile              # Multi-stage: Node build -> Nginx
│   ├── nginx.conf              # Sirve el SPA y hace proxy del /api al backend
│   └── src/
│       ├── api/                # Axios instance (las peticiones van a /api/...)
│       ├── components/         # Layout, Sidebar, PeriodSelector, ErrorBoundary
│       ├── context/            # AuthContext (estado global de auth)
│       └── pages/              # 6 páginas: Login, Dashboard, Ingresos...
│
├── database/                   
│   └── schema.sql              # Script de inicialización de tablas Postgres
├── docker-compose.yml          # Orquesta Postgres + Backend + Frontend (Nginx)
├── iniciar.bat                 # Script batch para desarrollo sin Docker
└── AGENTS.md                   # Este archivo (Contexto para la IA)
```

## Infraestructura y Despliegue (Docker)

El proyecto está preparado para desplegarse en un **VPS (recomendado: Hetzner)** utilizando **Docker Compose**.
1. **Red Interna:** Los 3 contenedores (postgres, backend, frontend) se comunican por la red `hostal-network`.
2. **Proxy Nginx (Frontend):** 
   - Expone el **puerto 80** al exterior.
   - Sirve los estáticos de React para peticiones a `/`.
   - Intercepta peticiones a `/api/` y hace un *proxy_pass* a `http://hostal-backend:8080/api/`. Esto **elimina los problemas de CORS** en producción.
3. **Backend:** El `application.yml` obtiene las variables de entorno de `docker-compose.yml` (`DB_HOST=postgres`, etc.). Está aislado del exterior, el tráfico pasa por Nginx.
4. **Base de Datos:** Postgres 15. Expone el puerto 5432 solo para debug local; en producción está protegido.

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 17, Spring Boot 3.3.5, Spring Security, JWT, JPA/Hibernate |
| Frontend | Vite, React 18, TailwindCSS 3, ECharts, Axios |
| Base de datos | PostgreSQL 15 |
| Infraestructura| Docker, Docker Compose, Nginx |
| Auth | JWT con auto-refresh al 25% de expiración |

## Convenciones de Código

### Backend (Java)
- **Paquetes:** Un controller, service, repository y dto por dominio (Ingreso, Egreso, etc.).
- **DTOs:** NUNCA exponer entidades JPA directamente. Siempre usar Request/Response DTOs.
- **IDs:** `BIGSERIAL` en BD = `Long` en Java.
- **Fechas:** Siempre `Instant` (no `LocalDate` ni `Date`).
- **Validación:** Usar anotaciones `@NotBlank`, `@Positive` en DTOs, no en services.
- **La sede NUNCA se envía desde el frontend**, siempre se deriva del token JWT del usuario autenticado.

### Frontend (React)
- **Componentes:** Functional components + hooks.
- **Estado:** Context API para auth (no Redux/Zustand).
- **Estilos:** TailwindCSS. No usar CSS modules ni styled-components. Priorizar estéticas modernas (glassmorphism, animaciones).
- **HTTP:** Siempre usar la instancia `api` de `src/api/api.js`.
- **Rutas:** Definidas en `App.jsx`, navegación via `react-router-dom`.
- **Alertas:** Usar `toast.success()` y `toast.error()` de `react-hot-toast`.

## Endpoints Principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/login | Login → JWT |
| GET | /api/auth/me | Perfil del usuario autenticado |
| GET/POST | /api/ingresos | Listar/Crear ingresos |
| POST | /api/ingresos/{id}/anular | Anular ingreso |
| GET/POST | /api/egresos | Listar/Crear egresos |
| POST | /api/egresos/{id}/anular | Anular egreso |
| GET | /api/movimientos | Listado combinado paginado |
| GET | /api/dashboard | Resumen financiero |
| GET | /api/reportes/* | Reportes y exportación CSV |

*Swagger UI disponible en:* `http://localhost:8080/swagger-ui.html` (Desarrollo local)

## Credenciales de Prueba (Base de datos por defecto)

| Usuario | Contraseña | Sede |
|---------|-----------|------|
| centro | centro123 | Centro |
| norte | norte123 | Norte |

## Agregar un Módulo Nuevo (Flujo de Trabajo)

1. Backend: Crear model/Entidad.java
2. Backend: Crear repository/EntidadRepository.java
3. Backend: Crear dto/EntidadRequest.java, EntidadResponse.java
4. Backend: Crear service/EntidadService.java
5. Backend: Crear controller/EntidadController.java
6. Frontend: Crear pages/EntidadPage.jsx
7. Frontend: Agregar ruta en App.jsx y NavLink en Sidebar.jsx

## Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `401 Unauthorized` | JWT expirado o ausente | Verificar header `Authorization: Bearer <token>` |
| `CORS error` en local | Frontend en puerto distinto | Nginx lo soluciona en prod. En local, ver `.env` |
| `column does not exist` | Entity no coincide con BD | Verificar `ddl-auto: update` en dev (`validate` en prod) |
| `Hostal-backend not found` | Falla red de docker | Verificar que `depends_on` esté configurado en docker-compose |
