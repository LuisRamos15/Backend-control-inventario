# Documentación Técnica - Backend Control de Inventario

## Índice

1. [Resumen del Proyecto](#resumen-del-proyecto)
2. [Arquitectura por Capas](#arquitectura-por-capas)
3. [Flujo de Autenticación con JWT](#flujo-de-autenticación-con-jwt)
4. [Endpoints REST](#endpoints-rest)
   - [Autenticación](#autenticación)
   - [Productos](#productos)
   - [Movimientos](#movimientos)
   - [Usuarios](#usuarios)
   - [Dashboard](#dashboard)
   - [Reportes](#reportes)
5. [Manejo de Errores](#manejo-de-errores)
6. [Buenas Prácticas de Desarrollo](#buenas-prácticas-de-desarrollo)

## Resumen del Proyecto

Este backend es una API RESTful desarrollada en **Spring Boot 3.5.5** con **Java 21** y **MongoDB** como base de datos. Proporciona funcionalidades para el control y gestión de inventarios, incluyendo:

- **Autenticación y Autorización**: Usando JWT con roles (ADMIN, SUPERVISOR, OPERADOR).
- **Gestión de Productos**: CRUD completo con validaciones, búsqueda y paginación.
- **Movimientos de Inventario**: Registro de entradas, salidas y ajustes con historial.
- **Dashboard**: Métricas y reportes en tiempo real.
- **Reportes**: Generación de PDFs y Excel para inventario y movimientos.
- **Notificaciones en Tiempo Real**: WebSockets para alertas de productos.
- **Seguridad**: Configuración CORS, CSRF deshabilitado, filtros JWT.

La aplicación se ejecuta en el puerto 8080 y utiliza MongoDB Atlas para persistencia. Incluye documentación OpenAPI con Swagger UI.

## Arquitectura por Capas

El proyecto sigue una arquitectura por capas clara y modular:

### Config
- **CorsConfig.java**: Configuración de CORS para permitir solicitudes desde orígenes específicos.
- **WebSocketConfig.java**: Configuración de WebSockets para notificaciones en tiempo real.
- **OpenApiConfig.java**: Configuración de OpenAPI para documentación Swagger.

### Controlador
- **AuthControlador.java**: Maneja registro y login de usuarios.
- **ProductoControlador.java**: CRUD de productos, búsqueda y notificaciones WebSocket.
- **MovimientoControlador.java**: Registro y consulta de movimientos de inventario.
- **UsuarioControlador.java**: Gestión de usuarios (solo ADMIN).
- **DashboardControlador.java**: Endpoints para métricas y gráficos.
- **ReporteControlador.java**: Generación de reportes en PDF y Excel.

### DTO (Data Transfer Objects)
- **LoginReq.java**: Request para login (nombreUsuario, password).
- **LoginRes.java**: Response con token JWT.
- **UsuarioReq.java**: Request para registro/creación de usuarios.
- **Dashborad.java**: DTO para resumen del dashboard.
- **DiaMovimientos.java**: DTO para movimientos por día.
- **TopProducto.java**: DTO para productos más movidos.
- **MovimientoEvent.java**: DTO para eventos de movimientos.
- **MovimientoNotification.java**: DTO para notificaciones de movimientos.
- **MovimientoReq.java**: Request para movimientos.
- **UsuarioVista.java**: DTO para vista de usuarios.
- **AlertaEvent.java**: DTO para alertas de stock bajo.

### Entidad
- **Usuario.java**: Entidad de usuario con roles y hash de contraseña.
- **Producto.java**: Entidad de producto con validaciones (SKU único, stock, precios).
- **Movimiento.java**: Entidad de movimientos con timestamps y referencias a productos.
- **Rol.java**: Enum para roles (ADMIN, SUPERVISOR, OPERADOR).

### Repositorio
- **UsuarioRepositorio.java**: Repositorio para usuarios con consultas personalizadas.
- **ProductoRepositorio.java**: Repositorio para productos con índices y búsquedas.
- **MovimientoRepositorio.java**: Repositorio para movimientos con filtros por fecha y tipo.

### Seguridad
- **SecurityConfig.java**: Configuración de Spring Security con JWT y permisos por roles.
- **JwtUtil.java**: Utilidades para generar y validar tokens JWT.
- **JwtAuthFilter.java**: Filtro para validar JWT en cada request.
- **GlobalExceptionHandler.java**: Manejo global de excepciones con respuestas JSON estructuradas.

### Servicio
- **UsuarioServicio.java**: Lógica de negocio para usuarios (registro, validaciones).
- **ProductoServicio.java**: Lógica para productos (CRUD, búsquedas, validaciones).
- **MovimientoServicio.java**: Lógica para movimientos (registro con actualización de stock).
- **DashboardServicio.java**: Cálculos para métricas del dashboard.
- **UsuarioDetailsService.java**: Implementación de UserDetails para Spring Security.

## Flujo de Autenticación con JWT

### Registro
- **Endpoint**: `POST /api/auth/registro`
- **Público**: Sí
- **Descripción**: Crea un nuevo usuario. Si no se especifican roles, asigna OPERADOR por defecto.
- **Request Body**:
  ```json
  {
    "nombreUsuario": "usuario123",
    "password": "password123",
    "roles": ["OPERADOR"]
  }
  ```
- **Response**: Usuario creado (201 Created).

### Login
- **Endpoint**: `POST /api/auth/login`
- **Público**: Sí
- **Descripción**: Autentica al usuario y devuelve un token JWT válido por 120 minutos.
- **Request Body**:
  ```json
  {
    "nombreUsuario": "usuario123",
    "password": "password123"
  }
  ```
- **Response**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer"
  }
  ```

### Validación de JWT
- El filtro `JwtAuthFilter` valida el token en cada request protegido.
- Extrae el username y roles del token para autorización.
- Si el token es inválido o expirado, retorna 401 Unauthorized.

## Endpoints REST

### Autenticación
| Método | Endpoint | Descripción | Público | Roles |
|--------|----------|-------------|---------|-------|
| POST | `/api/auth/registro` | Registrar nuevo usuario | Sí | - |
| POST | `/api/auth/login` | Login y obtener JWT | Sí | - |

### Productos
| Método | Endpoint | Descripción | Público | Roles |
|--------|----------|-------------|---------|-------|
| GET | `/api/productos` | Listar todos los productos | No | Todos |
| GET | `/api/productos/{id}` | Obtener producto por ID | No | Todos |
| POST | `/api/productos` | Crear nuevo producto | No | Todos |
| PATCH | `/api/productos/{id}` | Actualizar producto parcialmente | No | Todos |
| DELETE | `/api/productos/{id}` | Eliminar producto | No | Todos |
| GET | `/api/productos/page` | Listar productos paginados | No | Todos |
| GET | `/api/productos/search` | Buscar productos (por q, categoria, sku) | No | Todos |

**Ejemplo - Crear Producto**:
```bash
POST /api/productos
Authorization: Bearer <token>
Content-Type: application/json

{
  "sku": "PROD001",
  "nombre": "Producto Ejemplo",
  "categoria": "Electrónica",
  "stock": 100,
  "minimo": 10,
  "stockMaximo": 200,
  "precioUnitario": 25.50,
  "descripcion": "Descripción opcional"
}
```

**Response (201 Created)**:
```json
{
  "id": "64f...",
  "sku": "PROD001",
  "nombre": "Producto Ejemplo",
  "categoria": "Electrónica",
  "stock": 100,
  "minimo": 10,
  "stockMaximo": 200,
  "precioUnitario": 25.50,
  "descripcion": "Descripción opcional"
}
```

### Movimientos
| Método | Endpoint | Descripción | Público | Roles |
|--------|----------|-------------|---------|-------|
| POST | `/api/movimientos` | Registrar movimiento | No | Todos |
| GET | `/api/movimientos/recientes` | Movimientos recientes | No | Todos |
| GET | `/api/movimientos` | Listar movimientos filtrados | No | Todos |

**Ejemplo - Registrar Movimiento**:
```bash
POST /api/movimientos
Authorization: Bearer <token>
Content-Type: application/json

{
  "productoId": "64f...",
  "cantidad": 50,
  "tipo": "ENTRADA"
}
```

**Response (201 Created)**:
```json
{
  "mensaje": "Movimiento registrado exitosamente",
  "producto": "Producto Ejemplo",
  "stockAntes": 100,
  "stockDespues": 150,
  "tipo": "ENTRADA",
  "cantidad": 50
}
```

### Usuarios
| Método | Endpoint | Descripción | Público | Roles |
|--------|----------|-------------|---------|-------|
| GET | `/api/usuarios` | Listar usuarios | No | ADMIN |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID | No | ADMIN |
| PATCH | `/api/usuarios/{id}` | Editar usuario | No | ADMIN |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario | No | ADMIN |

### Dashboard
| Método | Endpoint | Descripción | Público | Roles |
|--------|----------|-------------|---------|-------|
| GET | `/api/dashboard/resumen` | Resumen general | No | Todos |
| GET | `/api/dashboard/movimientos-por-dia` | Movimientos por día | No | Todos |
| GET | `/api/dashboard/top-productos` | Productos más movidos | No | Todos |

**Ejemplo - Resumen Dashboard**:
```json
{
  "totalProductos": 150,
  "productosBajoStock": 5,
  "totalMovimientosHoy": 25,
  "valorTotalInventario": 12500.00
}
```

### Reportes
| Método | Endpoint | Descripción | Público | Roles |
|--------|----------|-------------|---------|-------|
| GET | `/api/reportes/inventario/pdf` | Reporte PDF de inventario | No | Todos |
| GET | `/api/reportes/movimientos/pdf` | Reporte PDF de movimientos | No | Todos |
| GET | `/api/reportes/inventario/excel` | Reporte Excel de inventario | No | Todos |

## Manejo de Errores

El `GlobalExceptionHandler` maneja excepciones globalmente y retorna respuestas JSON estructuradas:

- **400 Bad Request**: Validaciones fallidas (e.g., campos requeridos, tipos inválidos).
- **401 Unauthorized**: Token JWT inválido o expirado.
- **403 Forbidden**: Acceso denegado por roles insuficientes.
- **404 Not Found**: Recurso no encontrado.
- **409 Conflict**: Duplicados (e.g., SKU o nombreUsuario existente).
- **500 Internal Server Error**: Errores genéricos del servidor.

**Ejemplo de Error de Validación**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "sku: El SKU es obligatorio; nombre: El nombre es obligatorio"
}
```

## Buenas Prácticas de Desarrollo

- **Validación**: Uso de Jakarta Validation en entidades y DTOs para asegurar integridad de datos.
- **Seguridad**: JWT para autenticación stateless, BCrypt para hashes de contraseña, roles para autorización.
- **Manejo de Excepciones**: Centralizado en `GlobalExceptionHandler` con respuestas consistentes.
- **Documentación**: OpenAPI/Swagger para documentación automática de endpoints.
- **WebSockets**: Para notificaciones en tiempo real sin polling.
- **Paginación y Búsqueda**: Implementada en productos y movimientos para performance.
- **Índices MongoDB**: Configurados en entidades para búsquedas eficientes.
- **CORS y CSRF**: Configurados apropiadamente para seguridad en APIs web.
- **Logging**: Configurado para debugging y monitoreo.
- **Pruebas**: Estructura preparada para tests unitarios e integración.