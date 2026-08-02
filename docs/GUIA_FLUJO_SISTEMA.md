# Guía del Flujo del Sistema Hostal

> **Propósito:** Este documento explica de manera integral cómo funciona el sistema, incluyendo la comunicación entre componentes, el flujo de código, y conceptos fundamentales. Está diseñado para ser consultado por desarrolladores o IA.

---

## Resumen General del Sistema

El sistema es un **gestor de ingresos/egresos para un hostal** con autenticación.

### Arquitectura

```
hostal-system/
├── frontend/     → React (Vite + TailwindCSS) - Puerto 5173 (dev) / 80 (prod)
├── backend/      → Spring Boot (Java 17) - Puerto 8080
├── database/     → PostgreSQL 15
└── docker-compose.yml  → Orquesta todo
```

### Analogía: Tu sistema es un hotel

| Componente | Equivalente en el hotel |
|------------|------------------------|
| **Frontend (React)** | Recepcionista |
| **Backend (Spring Boot)** | Gerente |
| **Base de datos (PostgreSQL)** | Caja fuerte / Archivo |
| **JWT** | Llave magnética / Tarjeta de acceso |
| **Nginx** | Puerta principal / Portero |
| **AuthContext** | Sistema de seguridad del hotel |

---

## Parte 1: Conceptos Fundamentales

### 1.1 Qué es CORS

**CORS** = **C**ross-**O**rigin **R**esource **S**haring (Compartir Recursos entre Orígenes)

Es una **regla de seguridad** del navegador que **bloquea** las peticiones de un sitio web a otro si no tienen el mismo "origen".

**¿Qué es un "origen"?**
Un origen = **protocolo + dominio + puerto**

| Ejemplo | Origen |
|---------|--------|
| `http://localhost:5173` | `http://localhost:5173` |
| `http://localhost:8080` | `http://localhost:8080` |
| `https://tudominio.com` | `https://tudominio.com` |

**Si uno de esos 3 cambia → es un origen diferente**

**Problema:**
- Frontend en `localhost:5173` pide a Backend en `localhost:8080`
- Navegador: "Son orígenes diferentes → ¡BLOQUEADO!"

**Solución en tu proyecto:**
- **Desarrollo:** `CorsConfig.java` permite `localhost:5173`
- **Producción:** Nginx sirve todo desde el mismo dominio

---

### 1.2 Qué es Nginx

**Nginx** es un **servidor web** que:
1. **Sirve archivos estáticos** (HTML, CSS, JS)
2. **Hace de proxy inverso** (redirige peticiones a otros serviores)

**Analogía:** Nginx es el PORTERO del hotel

| Lo que el cliente pide | ¿Qué hace Nginx? |
|------------------------|------------------|
| `http://tudominio.com/` | Sirve los archivos de React |
| `http://tudominio.com/api/ingresos` | Redirige al Backend |

**Archivo:** `frontend/nginx.conf`
```nginx
server {
    listen 80;

    # Si piden algo que EMPIEZA con /
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # Si piden algo que EMPIEZA con /api/
    location /api/ {
        proxy_pass http://hostal-backend:8080/api/;
    }
}
```

---

### 1.3 Diferencia entre Contraseña y JWT

| | **Contraseña del Login** | **JWT** |
|---|---|---|
| **¿Qué es?** | Contraseña que **el usuario recuerda** | **Token** que **el sistema genera** |
| **¿Quién la crea?** | El usuario | El backend |
| **¿Dónde vive?** | En la memoria del usuario | En localStorage del navegador |
| **¿Qué valida?** | "¿Eres quien dices ser?" | "¿Tienes permiso para estar aquí?" |
| **¿Cuándo expira?** | Nunca | Sí (generalmente 1-24 horas) |

**Analogía: Credenciales de un edificio**
- **Contraseña** = Credencial de empleado (para entrar al edificio)
- **JWT** = Credencial de acceso a pisos específicos (expira al final del día)

---

### 1.4 Qué es un DTO

**DTO** = **D**ata **T**ransfer **O**bject (Objeto de Transferencia de Datos)

Es una **caja** que transporta datos entre el frontend y el backend.

| DTO | Tipo | ¿Quién lo crea? | Ejemplo |
|-----|------|------------------|---------|
| **Request** | Frontend → Backend | Frontend | `{ "username": "centro", "password": "centro123" }` |
| **Response** | Backend → Frontend | Backend (Service) | `{ "id": 1, "tipoHabitacion": "DOBLE", ... }` |

**Analogía: Restaurante**
- **Request** = Tu pedido (escribes qué quieres)
- **Response** = El plato que te sirven (el cocinero lo prepara)

**Por qué NO exponer entidades JPA directamente:**

| Entidad JPA (Ingreso) | DTO (IngresoResponse) |
|----------------------|----------------------|
| Tiene referencias circulares | No tiene referencias |
| Expone `id_usuario` (número) | Expone `sede` (nombre legible) |
| No tiene `registradoPor` | Sí tiene `registradoPor` |

---

### 1.5 Constructor vs Builder (Lombok)

#### Constructor (`@AllArgsConstructor`)
```java
@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
}

// Uso:
new LoginResponse(token, username)
```

#### Builder (`@Builder`)
```java
@Getter
@Builder
@AllArgsConstructor
public class IngresoResponse {
    private Long id;
    private String tipoHabitacion;
    private BigDecimal monto;
}

// Uso:
IngresoResponse.builder()
    .id(1L)
    .tipoHabitacion("DOBLE")
    .monto(new BigDecimal("150"))
    .build()
```

| Característica | Constructor | Builder |
|----------------|-------------|---------|
| **Sintaxis** | `new Clase(campo1, campo2)` | `Clase.builder().campo1(x).build()` |
| **Orden** | IMPORTA | NO importa |
| **Campos opcionales** | NO | SÍ |
| **Legibilidad** | Baja | Alta |

**Patrón en tu proyecto:**
- **Request DTOs:** `@Getter @Setter` (el frontend los crea)
- **Response DTOs:** `@Getter @Builder @AllArgsConstructor` (el Service los crea)

---

### 1.6 AuthContext vs JWT

| | **JWT** | **AuthContext** |
|---|---|---|
| **¿Qué es?** | Un **token** (cadena de texto) | Un **administrador** de estado en React |
| **¿Dónde vive?** | En `localStorage` | En la memoria de React |
| **¿Quién lo crea?** | El **backend** | El **frontend** |
| **¿Para qué sirve?** | Probar que eres quien dices ser | Manejar el estado de autenticación |

**Analogía: Gimnasio**
- **JWT** = Tu **tarjeta de acceso** (objeto físico)
- **AuthContext** = El **sistema de seguridad** (verifica tu tarjeta, la guarda, te avisa cuando vence)

---

## Parte 2: Flujo Completo del Código

### PASO 1: El usuario abre el navegador

**¿Qué hace el usuario?**
Escribe `http://localhost:5173` en el navegador.

**¿Qué carga el navegador?**
1. `index.html` → La puerta principal
2. `main.jsx` → El recepcionista que monta React

**Archivo:** `frontend/src/main.jsx`
```jsx
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>        ← Maneja las rutas
      <AuthProvider>       ← Maneja la autenticación
        <App />            ← Define qué página mostrar
        <Toaster />        ← Notificaciones
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
```

**Analogía:**
- `index.html` = Puerta principal del edificio
- `main.jsx` = Recepcionista que te da la bienvenida
- `BrowserRouter` = Mapa del edificio
- `AuthProvider` = Sistema de seguridad
- `App` = Elevador

---

### PASO 2: React carga App.jsx (Rutas)

**Archivo:** `frontend/src/App.jsx`

```jsx
function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;  // Sin usuario → login
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={
        <ProtectedRoute>
          <Layout />
        </ProtectedRoute>
      }>
        <Route index element={<DashboardPage />} />
        <Route path="nuevo-ingreso" element={<NuevoIngresoPage />} />
        <Route path="movimientos" element={<MovimientosPage />} />
        <Route path="reportes" element={<ReportesPage />} />
      </Route>
    </Routes>
  );
}
```

**Analogía:**
- `ProtectedRoute` = Guardia de seguridad
- Rutas = Directorio del edificio (pisos)
- `Layout` = Plano del piso (mismo pasillo, diferentes habitaciones)

---

### PASO 3: Login (LoginPage)

**Archivo:** `frontend/src/pages/LoginPage.jsx`

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  try {
    await login(username, password);  // Llama al AuthContext
    toast.success('¡Bienvenido!');
    navigate('/');                    // Redirige al dashboard
  } catch (error) {
    toast.error('Credenciales incorrectas');
  }
};
```

**Analogía:** Llenar un formulario en el banco

---

### PASO 4: AuthContext envía credenciales

**Archivo:** `frontend/src/context/AuthContext.jsx`

```jsx
const login = async (username, password) => {
  const response = await api.post('/api/auth/login', { username, password });
  const { token, username: u, nombreCompleto, sede } = response.data;
  
  localStorage.setItem('token', token);           // Guarda JWT
  localStorage.setItem('user', JSON.stringify(userData));  // Guarda usuario
  setUser(userData);                              // Actualiza estado
};
```

**Analogía:** Cajero del banco te da un comprobante → lo guardas en tu billetera

---

### PASO 5: Backend recibe la petición

**Flujo:**
1. `SecurityConfig` → Permite `/api/auth/login` sin JWT
2. `JwtAuthenticationFilter` → Se salta esta ruta
3. `AuthController` → Recibe `LoginRequest`
4. `AuthService` → Valida credenciales, genera JWT

**Archivo:** `backend/.../service/AuthService.java`
```java
public LoginResponse login(LoginRequest request) {
    Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

    if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
        throw new RuntimeException("Credenciales incorrectas");
    }

    String token = jwtUtil.generateToken(usuario.getUsername());

    return LoginResponse.builder()
            .token(token)
            .username(usuario.getUsername())
            .nombreCompleto(usuario.getNombreCompleto())
            .sede(usuario.getSede().getNombre())
            .build();
}
```

**Analogía:** Guardia → Recepcionista → Gerente

---

### PASO 6: JWT se devuelve al frontend

**El backend responde con:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "centro",
  "nombreCompleto": "Juan Pérez",
  "sede": "Centro"
}
```

**El frontend lo guarda en localStorage y lo usa en cada petición.**

**Archivo:** `frontend/src/api/api.js`
```jsx
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

**Analogía:** Llave magnética que se usa automáticamente

---

### PASO 7: Cada petición usa el JWT

> **Nota:** Este paso está pendiente de desarrollo detallado.

**Concepto:**
- Cada vez que React hace `api.get()` o `api.post()`, el interceptor agrega automáticamente el header `Authorization: Bearer eyJ...`
- El backend verifica el JWT en `JwtAuthenticationFilter`
- Si es válido → responde con datos
- Si no es válido → retorna 401

**Analogía:** Credencial automática que se verifica en cada piso

---

### PASO 8: Crear un Ingreso (ejemplo completo)

> **Nota:** Este paso está pendiente de desarrollo detallado.

**Flujo completo:**
1. Frontend envía `POST /api/ingresos` con `IngresoRequest`
2. Controller recibe, valida con `@Valid`
3. Service crea entidad JPA, guarda en BD
4. Service crea `IngresoResponse` con Builder
5. Frontend recibe la respuesta

**Analogía:** Restaurante (Pedir → Cocinero → Mesero)

---

### PASO 9: Dashboard y Reportes

> **Nota:** Este paso está pendiente de desarrollo detallado.

**Concepto:**
- `DashboardService` consulta totales de ingresos y egresos
- Devuelve `DashboardResumen` con Builder
- Frontend muestra gráficos con ECharts

---

### PASO 10: Cerrar sesión

> **Nota:** Este paso está pendiente de desarrollo detallado.

**Archivo:** `frontend/src/context/AuthContext.jsx`
```jsx
const logout = useCallback(() => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  setUser(null);
}, []);
```

**Analogía:** Devolver la llave magnética y salir del hotel

---

## Parte 3: Anatomía de un DTO

### Request DTO (Frontend → Backend)

```java
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
```

**Validaciones comunes:**
| Anotación | ¿Qué valida? | Ejemplo que FALLA |
|-----------|---------------|-------------------|
| `@NotBlank` | No puede ser null, vacío o espacios | `""`, `"   "`, `null` |
| `@NotNull` | No puede ser null | `null` |
| `@Positive` | Debe ser mayor que 0 | `-5`, `0` |

### Response DTO (Backend → Frontend)

```java
@Getter
@Builder
@AllArgsConstructor
public class IngresoResponse {
    private Long id;
    private String tipoHabitacion;
    private BigDecimal monto;
    private String sede;
    private String registradoPor;
}
```

**Uso en el Service:**
```java
return IngresoResponse.builder()
        .id(guardado.getId())
        .tipoHabitacion(guardado.getTipoHabitacion())
        .monto(guardado.getMonto())
        .sede(usuario.getSede().getNombre())
        .registradoPor(usuario.getNombreCompleto())
        .build();
```

---

## Parte 4: Infraestructura

### Docker Compose

```yaml
services:
  postgres:        # Base de datos
    image: postgres:15-alpine
    ports: [5432]

  backend:         # Spring Boot
    build: ./backend
    depends_on: postgres

  frontend:        # React + Nginx
    build: ./frontend
    ports: ["80:80"]
    depends_on: backend
```

### Nginx (Proxy Inverso)

```nginx
# Archivos estáticos de React
location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;
}

# Proxy al Backend
location /api/ {
    proxy_pass http://hostal-backend:8080/api/;
}
```

### CORS

**Desarrollo:** `CorsConfig.java` permite `localhost:5173`
**Producción:** Nginx sirve todo desde el mismo dominio (no hay CORS)

---

## Parte 5: Preguntas Frecuentes

**¿Por qué no enviar la contraseña en cada petición?**
Porque si alguien intercepta la contraseña, podría usarla siempre. El JWT expira, por lo que es más seguro.

**¿Por qué JWT expira?**
Para limitar el tiempo de acceso. Si un JWT es robado, solo funcionará por un tiempo limitado.

**¿Qué pasa si dos usuarios tienen la misma URL?**
Cada uno tiene su propio JWT. No pueden acceder a los datos del otro.

**¿Cómo funciona el auto-refresh?**
Si el JWT está por vencer (queda menos del 25% de tiempo), el backend envía un JWT nuevo en el header de la respuesta.

---

## Parte 6: Próximos Pasos (para preguntar a la IA)

Para continuar aprendiendo, puedes preguntar a la IA sobre:

### Cómo agregar una nueva entidad
1. Crear `model/NuevaEntidad.java`
2. Crear `repository/NuevaEntidadRepository.java`
3. Crear `dto/NuevaEntidadRequest.java` y `NuevaEntidadResponse.java`
4. Crear `service/NuevaEntidadService.java`
5. Crear `controller/NuevaEntidadController.java`
6. Crear `pages/NuevaEntidadPage.jsx` en el frontend
7. Agregar ruta en `App.jsx`

### Cómo crear un nuevo reporte
1. Agregar consulta JPQL en el Repository
2. Agregar método en el Service
3. Agregar endpoint en el Controller
4. Agregar página en el Frontend

### Cómo modificar la seguridad
- Editar `SecurityConfig.java` para agregar/quitar rutas permitidas
- Editar `JwtUtil.java` para cambiar expiración
- Editar `JwtAuthenticationFilter.java` para cambiar lógica de validación

### Cómo deployar en un VPS
1. Instalar Docker y Docker Compose
2. Clonar el repositorio
3. Ejecutar `docker compose up -d --build`
4. Configurar dominio y SSL (Let's Encrypt)

---

## Resumen: Todo el flujo en una línea

```
Tú llegas al hotel (navegador) → Puerta principal (Nginx) → Recepcionista (React) → 
Guardia verifica (ProtectedRoute) → Llenas formulario (LoginPage) → 
Cajero verifica (Backend) → Te da llave magnética (JWT) → 
Guardas en billetera (localStorage) → Cada piso verifica tu llave (Filter) → 
Si expira → Vuelves a recepción (login)
```

---

*Documento creado como guía de referencia para el Sistema Hostal.*
