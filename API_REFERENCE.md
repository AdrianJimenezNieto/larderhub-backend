# LarderHub API Reference

> **Base URL:** `http://localhost:8080`
> **Autenticación:** Todos los endpoints (excepto `/api/auth/**` y `/api/health`) requieren el header:
> ```
> Authorization: Bearer <JWT_TOKEN>
> ```

---

## 📋 Índice

1. [Auth](#1-auth)
2. [Households](#2-households)
3. [Inventario (Despensa)](#3-inventario-despensa)
4. [Catálogo de Productos](#4-catálogo-de-productos)

---

## 1. Auth

### `POST /api/auth/register`
Registra un nuevo usuario.

**¿Requiere token?** ❌ No

**Body (JSON):**
```json
{
  "username": "adriandev",
  "email": "adrian@example.com",
  "password": "securepass123"
}
```

**Respuesta `201 Created`:**
```json
{
  "token": "eyJhbGciOi...",
  "username": "adriandev",
  "email": "adrian@example.com"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | Email o username ya registrado / campos inválidos |

---

### `POST /api/auth/login`
Inicia sesión y obtiene un JWT.

**¿Requiere token?** ❌ No

**Body (JSON):**
```json
{
  "email": "adrian@example.com",
  "password": "securepass123"
}
```

**Respuesta `200 OK`:**
```json
{
  "token": "eyJhbGciOi...",
  "username": "adriandev",
  "email": "adrian@example.com"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `401` | Credenciales incorrectas |

---

### `GET /api/auth/me`
Devuelve los datos del usuario autenticado extraídos del JWT (sin consulta a BD).

**¿Requiere token?** ✅ Sí

**Respuesta `200 OK`:**
```json
{
  "username": "adriandev",
  "role": "ROLE_USER"
}
```

---

## 2. Households

> Un **household** es un grupo colaborativo con despensa compartida.
> El usuario que crea el household es automáticamente **ADMIN**.
> Para invitar a otros se comparte el `joinCode` (8 caracteres en mayúsculas).

---

### `POST /api/v1/households`
Crea un nuevo household. El usuario autenticado pasa a ser ADMIN.

**¿Requiere token?** ✅ Sí

**Body (JSON):**
```json
{
  "name": "Casa de Adrián"
}
```

**Respuesta `201 Created`:**
```json
{
  "id": 1,
  "name": "Casa de Adrián",
  "joinCode": "A3F9BC21",
  "createdAt": "2026-02-22T11:00:00",
  "myRole": "ADMIN"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | Nombre vacío o demasiado largo (>50 chars) |

---

### `POST /api/v1/households/join`
Unirse a un household existente usando su código de invitación.

**¿Requiere token?** ✅ Sí

**Body (JSON):**
```json
{
  "joinCode": "A3F9BC21"
}
```

**Respuesta `200 OK`:**
```json
{
  "id": 1,
  "name": "Casa de Adrián",
  "joinCode": "A3F9BC21",
  "createdAt": "2026-02-22T11:00:00",
  "myRole": "MEMBER"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | Código inválido o ya eres miembro de ese household |

---

### `GET /api/v1/households/mine`
Lista todos los households a los que pertenece el usuario autenticado.

**¿Requiere token?** ✅ Sí

**Respuesta `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "Casa de Adrián",
    "joinCode": "A3F9BC21",
    "createdAt": "2026-02-22T11:00:00",
    "myRole": "ADMIN"
  },
  {
    "id": 2,
    "name": "Piso de estudiantes",
    "joinCode": "C8D2EF99",
    "createdAt": "2026-02-22T12:00:00",
    "myRole": "MEMBER"
  }
]
```

---

### `GET /api/v1/households/{id}/members`
Lista todos los miembros de un household. Solo accesible si el usuario ya pertenece al household.

**¿Requiere token?** ✅ Sí

**Respuesta `200 OK`:**
```json
[
  {
    "userId": 1,
    "username": "adriandev",
    "role": "ADMIN",
    "joinedAt": "2026-02-22T11:00:00"
  },
  {
    "userId": 2,
    "username": "maria99",
    "role": "MEMBER",
    "joinedAt": "2026-02-22T11:30:00"
  }
]
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `403` | No eres miembro de este household |

---

## 3. Inventario (Despensa)

> El inventario representa los productos que tiene el household del usuario en su despensa.
> **El `householdId` se resuelve automáticamente desde el JWT** — el frontend no necesita enviarlo.
> Cada operación actúa sobre el household del usuario autenticado.

> ⚠️ **Prerrequisito:** El usuario debe pertenecer a al menos un household. Si no, la API devuelve `500` con el mensaje `"User does not belong to any household"`.

---

### `POST /api/v1/inventory`
Añade un producto a la despensa del household.

**¿Requiere token?** ✅ Sí

**Body (JSON):**
```json
{
  "productId": 3,
  "quantity": 2.5,
  "expirationDate": "2026-03-15"
}
```
> `expirationDate` es opcional. Formato: `YYYY-MM-DD`.

**Respuesta `201 Created`:**
```json
{
  "id": 10,
  "productId": 3,
  "productName": "Queso Rallado",
  "productBarcode": "8410000000003",
  "productImageUrl": null,
  "standardUnit": "gramos",
  "quantity": 2.5,
  "expirationDate": "2026-03-15"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | `productId` nulo / `quantity` <= 0 |
| `400` | Producto no encontrado en el catálogo |

---

### `GET /api/v1/inventory`
Lista todos los items de la despensa del household del usuario.

**¿Requiere token?** ✅ Sí

**Respuesta `200 OK`:**
```json
[
  {
    "id": 10,
    "productId": 3,
    "productName": "Queso Rallado",
    "productBarcode": "8410000000003",
    "productImageUrl": null,
    "standardUnit": "gramos",
    "quantity": 2.5,
    "expirationDate": "2026-03-15"
  },
  {
    "id": 11,
    "productId": 1,
    "productName": "Leche Entera 1L",
    "productBarcode": "8410000000001",
    "productImageUrl": "https://img.com/leche.jpg",
    "standardUnit": "litros",
    "quantity": 3.0,
    "expirationDate": null
  }
]
```

---

### `PUT /api/v1/inventory/{itemId}`
Actualiza la cantidad y/o fecha de caducidad de un item.

**¿Requiere token?** ✅ Sí

**Body (JSON):** *(todos los campos son opcionales)*
```json
{
  "quantity": 1.0,
  "expirationDate": "2026-04-01"
}
```

**Respuesta `200 OK`:**
```json
{
  "id": 10,
  "productId": 3,
  "productName": "Queso Rallado",
  "productBarcode": "8410000000003",
  "productImageUrl": null,
  "standardUnit": "gramos",
  "quantity": 1.0,
  "expirationDate": "2026-04-01"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `403` | El item no pertenece a tu household |

---

### `DELETE /api/v1/inventory/{itemId}`
Elimina un item de la despensa.

**¿Requiere token?** ✅ Sí

**Respuesta `204 No Content`** *(sin body)*

**Errores:**
| Código | Motivo |
|--------|--------|
| `403` | El item no pertenece a tu household |

---

## 4. Catálogo de Productos

> El catálogo es **global** y compartido por todos los usuarios.
> Los products del catálogo son los que se pueden añadir al inventario con `productId`.
> Al arrancar la aplicación se carga automáticamente con **20 productos de muestra** desde `data.sql`.

---

### `POST /api/v1/products`
Registra un nuevo producto en el catálogo global.

**¿Requiere token?** ✅ Sí

**Body (JSON):**
```json
{
  "name": "Mantequilla",
  "category": "Lácteos",
  "barcode": "8410000000099",
  "imageUrl": "https://img.com/mantequilla.jpg",
  "standardUnit": "gramos"
}
```
> Solo `name` es obligatorio. El resto son opcionales.

**Respuesta `201 Created`:**
```json
{
  "id": 21,
  "name": "Mantequilla",
  "category": "Lácteos",
  "barcode": "8410000000099",
  "imageUrl": "https://img.com/mantequilla.jpg",
  "standardUnit": "gramos"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | Nombre vacío |
| `400` | Barcode duplicado |

---

### `GET /api/v1/products`
Lista todos los productos del catálogo.

**¿Requiere token?** ✅ Sí

**Respuesta `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "Leche Entera 1L",
    "category": "Lácteos",
    "barcode": "8410000000001",
    "imageUrl": "https://img.com/leche.jpg",
    "standardUnit": "litros"
  },
  {
    "id": 2,
    "name": "Yogur Natural",
    "category": "Lácteos",
    "barcode": "8410000000002",
    "imageUrl": null,
    "standardUnit": "unidades"
  }
]
```

---

### `GET /api/v1/products/{id}`
Obtiene un producto concreto por su ID.

**¿Requiere token?** ✅ Sí

**Respuesta `200 OK`:**
```json
{
  "id": 1,
  "name": "Leche Entera 1L",
  "category": "Lácteos",
  "barcode": "8410000000001",
  "imageUrl": "https://img.com/leche.jpg",
  "standardUnit": "litros"
}
```

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | Producto no encontrado |

---

### `DELETE /api/v1/products/{id}`
Elimina un producto del catálogo.

**¿Requiere token?** ✅ Sí

**Respuesta `204 No Content`** *(sin body)*

**Errores:**
| Código | Motivo |
|--------|--------|
| `400` | Producto no encontrado |

---

## 🔑 Códigos HTTP utilizados

| Código | Significado |
|--------|-------------|
| `200 OK` | Consulta/actualización correcta |
| `201 Created` | Recurso creado correctamente |
| `204 No Content` | Eliminación correcta (sin body) |
| `400 Bad Request` | Validación fallida o regla de negocio violada |
| `401 Unauthorized` | Token ausente o inválido |
| `403 Forbidden` | Token válido pero sin permisos sobre ese recurso |
| `500 Internal Server Error` | Error inesperado del servidor |

---

## 🗺️ Estado actual del proyecto

### Slices implementados

| Slice | Estado | Descripción |
|-------|--------|-------------|
| **Slice 1** — Auth & Onboarding | ✅ Completo | Registro, login, JWT, CORS, RBAC básico |
| **Slice 2** — Inventario CRUD | ✅ Completo | CRUD de despensa + catálogo de productos |
| **Slice 3** — Households | ✅ Completo | Crear, unirse, listar grupos y miembros |

### Slices pendientes (propuesta)

| Slice | Descripción |
|-------|-------------|
| **Slice 4** — Lista de la compra | Generar lista automática a partir del inventario bajo mínimos |
| **Slice 5** — Notificaciones de caducidad | Alertas cuando un producto está próximo a caducar |
| **Slice 6** — Recetas | Sugerir recetas en función de lo que hay en la despensa |

---

## 🛠️ Stack técnico

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 3.3 + Java 17 |
| Seguridad | Spring Security 6 + JWT (JJWT 0.12) |
| Persistencia | Spring Data JPA + Hibernate 6 |
| Base de datos | PostgreSQL |
| Mapeo | MapStruct |
| Build | Maven |
| Puerto | `8080` |
