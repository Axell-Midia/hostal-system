# Hostal System - Documentación del Estado Actual (26-Jul-2026)

## Objetivo
Sistema de administración de hostal con 2 sedes, backend Spring Boot, frontend React + Vite, PostgreSQL local.

## Stack
- **Backend:** Java 17 / Spring Boot 3.3.5 / Spring Security + JWT / BCrypt / Maven
- **Frontend:** Vite + React 18 + TailwindCSS 3
- **Base de datos:** PostgreSQL 18 (localhost:5432, usuario `hostal_user`, contraseña `sa`, BD `hostal_db`)
- **Puertos:** Backend 8080, Frontend 5173

---

## Arquitectura del Backend

### Paquetes (`backend/src/main/java/com/hostal/sistema/`)
```
config/          → SecurityConfig, JwtUtil, CorsConfig, SwaggerConfig, DataInitializer
controller/      → AuthController, IngresoController, EgresoController, MovimientoController, DashboardController, ReporteController
service/         → AuthService, IngresoService, EgresoService, MovimientoService, DashboardService, ReporteService
repository/      → UsuarioRepository, IngresoRepository, EgresoRepository
model/           → Sede, Usuario, Ingreso, Egreso (entidades JPA)
dto/             → LoginRequest, LoginResponse, MeResponse, IngresoRequest, EgresoRequest, AnulacionRequest, MovimientoResponse, MovimientosPageResponse, DashboardResumen, ReporteGrupoResponse, UsuarioResponse
exception/       → GlobalExceptionHandler, RecursoNoEncontradoException, OperacionNoPermitidaException
```

### Autenticación JWT
- Login: `POST /api/auth/login` → `{ "token": "..." }`
- Todos los endpoints requieren header `Authorization: Bearer <token>`
- JWT contiene `sub` (username) y `sedeId`
- Passwords hasheados con BCrypt: `centro`/`centro123`, `norte`/`norte123`

### Reglas de negocio
- La **sede NUNCA se envía desde el frontend**; siempre se deriva del usuario autenticado en el JWT
- Las categorías de egreso son: ELECTRICIDAD, AGUA, INTERNET, OTROS
- OTROS requiere campo `descripcion` obligatorio
- La fecha se genera automáticamente con `Instant.now()` en el backend

### Base de datos (schema.sql)
- Usar `BIGSERIAL` para IDs (Java `Long` = `BIGINT`)
- `CHECK (monto > 0)` en ingreso y egreso
- Seed data: 2 usuarios, 8 ingresos, 5 egresos
- Las migraciones se ejecutan automáticamente al iniciar (`spring.sql.init.mode: always`)

---

## Endpoints del Backend

### Auth
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login, retorna JWT |
| GET | `/api/auth/me` | Perfil del usuario autenticado |

### Ingresos
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/ingresos` | Listar ingresos paginados |
| GET | `/api/ingresos/{id}` | Obtener ingreso por ID |
| POST | `/api/ingresos` | Crear ingreso (BODY: tipoHabitacion, monto, observacion) |
| POST | `/api/ingresos/{id}/anular` | Anular ingreso (BODY: motivo) |

### Egresos
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/egresos` | Listar egresos paginados |
| GET | `/api/egresos/{id}` | Obtener egreso por ID |
| POST | `/api/egresos` | Crear egreso (BODY: categoria, monto, descripcion) |
| POST | `/api/egresos/{id}/anular` | Anular egreso (BODY: motivo) |

### Movimientos (unificado)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/movimientos` | Listado combinado de ingresos + egresos |

**Query params:** `tipo` (TODOS/INGRESO/EGRESO), `desde`, `hasta`, `search`, `page`, `size`

### Dashboard
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/dashboard` | Resumen: totalIngresos, totalEgresos, ganancia |

### Reportes
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/reportes/rango` | Resumen financiero por período |
| GET | `/api/reportes/ingresos-por-habitacion` | Ingresos agrupados por tipo de habitación |
| GET | `/api/reportes/egresos-por-categoria` | Egresos agrupados por categoría |
| GET | `/api/reportes/exportar-csv` | Exportar movimientos a CSV |

**Query params de reportes:** `desde`, `hasta` (ISO-8601), `tipo` (para CSV)

---

## Arquitectura del Frontend

### Archivos (`frontend/src/`)
```
main.jsx              → Entry point
App.jsx               → Router (Login, Dashboard, Ingresos, Egresos, Movimientos, Reportes)
api.js                → Axios instance con interceptores JWT
context/
  AuthContext.jsx     → Manejo de estado auth (login, logout, user, token)
pages/
  LoginPage.jsx       → Formulario de login
  DashboardPage.jsx   → Resumen financiero con totales
  IngresosPage.jsx    → Crear ingreso
  EgresosPage.jsx     → Crear egreso
  MovimientosPage.jsx → Listado paginado con filtros, búsqueda, paginación
  ReportesPage.jsx    → Reportes financieros + exportar CSV
components/
  Layout.jsx          → Sidebar + contenido principal
  Sidebar.jsx         → Navegación lateral
  ErrorBoundary.jsx   → Captura de errores React
  Toast.jsx           → Notificaciones emergentes
  PeriodSelector.jsx  → Selector de fechas para reportes
```

### Dependencias
- `react-router-dom` → Rutas
- `axios` → HTTP client
- `tailwindcss` → Estilos
- `lucide-react` → Iconos

### Comandos
```bash
cd frontend
npm install          # Instalar dependencias
npm run dev          # Iniciar dev server en http://localhost:5173
npm run build        # Build de producción
```

---

## Bug Corregido: `lower(bytea)`

**Problema:** JPQL con parámetros `(:desde IS NULL OR i.fecha >= :desde)` causaba error en PostgreSQL porque no podía determinar el tipo del parámetro `?` cuando Hibernate pasaba null.

**Solución:** Eliminar las validaciones `IS NULL` de los queries JPQL usando `BETWEEN`, ya que el controller siempre envía fechas con valores por defecto (nunca null). Ver `IngresoRepository.java` y `EgresoRepository.java`.

---

## Archivos de Arranque
- `iniciar.bat` → Arranca backend (mvn spring-boot:run) + frontend (npm run dev)
- `detener.bat` → Detiene ambos procesos

---

## Próximos Pasos (NO implementar aún)
1. **Mejorar diseño del frontend** → Mejorar estilos con TailwindCSS, mejorar UX
2. **Añadir gráficos con ECharts** → Instalar `echarts` + `echarts-for-react`, agregar gráficos de barras/pastel en Dashboard y Reportes
