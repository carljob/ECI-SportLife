# 🏋️ SportLife API — Documentación Completa Pre-Parcial Corte #2

> **Empresa:** DOSW Company  
> **Proyecto:** SportLife MVP — Tienda virtual de productos deportivos  
> **Stack:** Java 17 · Spring Boot 2.7 · H2 (SQL) · MongoDB (NoSQL) · Swagger/OpenAPI  
> **Tiempo estimado de desarrollo teórico:** ~4 horas

---

## Tabla de Contenido

1. [Matriz de Trazabilidad](#1-matriz-de-trazabilidad)
2. [Especificación de Funcionalidades (Endpoints)](#2-especificación-de-funcionalidades)
3. [Diagrama de Componentes General](#3-diagrama-de-componentes-general)
4. [Diagrama de Componentes Específico](#4-diagrama-de-componentes-específico)
5. [Diagrama de Clases y Patrones de Software](#5-diagrama-de-clases-y-patrones-de-software)
6. [Diagramas de Base de Datos](#6-diagramas-de-base-de-datos)
7. [Seguridad](#7-seguridad)
8. [Roles y Permisos](#8-roles-y-permisos)
9. [TLS/SSL en API REST](#9-tlsssl-en-api-rest)
10. [CORS en API REST](#10-cors-en-api-rest)
11. [Diseño de Pantallas (Flujo de Compra)](#11-diseño-de-pantallas)
12. [Parte Práctica — Scaffolding y Código](#12-parte-práctica)
13. [Pipeline CI/CD — GitHub Actions](#13-pipeline-cicd)

---

## 1. Matriz de Trazabilidad

La matriz organiza las funcionalidades por prioridad, dependencias y bloqueos. Una funcionalidad **bloquea** a otra cuando debe estar completamente implementada antes de que la dependiente pueda funcionar.

| ID | Funcionalidad | Prioridad | Depende de | Bloquea a | Tipo |
|----|--------------|-----------|-----------|-----------|------|
| F01 | Registro de usuario | 🔴 Alta | — | F02, F03, F04, F05 | Base |
| F02 | Login / Autenticación | 🔴 Alta | F01 | F03, F04, F05 | Base |
| F03 | Listar productos | 🔴 Alta | — | F05 | Catálogo |
| F04 | Buscar producto por nombre | 🟡 Media | F03 | — | Catálogo |
| F05 | Filtrar productos por categoría | 🟡 Media | F03 | — | Catálogo |
| F06 | Ver detalle de producto | 🔴 Alta | F03 | F07 | Catálogo |
| F07 | Agregar producto al carrito | 🔴 Alta | F02, F06 | F08, F09 | Carrito |
| F08 | Ver resumen del carrito | 🔴 Alta | F07 | F09 | Carrito |
| F09 | Actualizar cantidad en carrito | 🟡 Media | F07 | — | Carrito |
| F10 | Iniciar checkout (crear orden) | 🔴 Alta | F08 | F11 | Pago |
| F11 | Procesar pago | 🔴 Alta | F10 | — | Pago |

**Interpretación de la Matriz:**
- **F01 y F02** son la base de todo el sistema: sin registro y login, no hay contexto de usuario.
- **F03** (listar productos) es independiente del usuario y puede implementarse en paralelo a F01/F02.
- **F07** (agregar al carrito) bloquea todo el flujo de compra: sin carrito, no hay checkout.
- **F10 → F11** son secuenciales: la orden debe existir antes de procesar el pago.

---

## 2. Especificación de Funcionalidades

### F01 — Registro de Usuario

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `POST` |
| **URL** | `/api/v1/auth/register` |
| **¿Es idempotente?** | ❌ No |
| **Razón técnica** | `POST` crea un nuevo recurso cada vez que se invoca. Si se envía dos veces el mismo correo, el segundo intento genera error de negocio. Idempotencia requeriría que repetir la llamada produzca el mismo resultado sin efectos adicionales, lo cual no ocurre aquí (el email debe ser único). |

**Datos de entrada:**

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `fullName` | `String` | ✅ Sí | Nombre completo del usuario |
| `email` | `String (email)` | ✅ Sí | Correo electrónico válido |
| `password` | `String` | ✅ Sí | Contraseña (mínimo 6 chars recomendado) |

**Ejemplo de entrada:**
```json
{
  "fullName": "Carlos Rodríguez",
  "email": "carlos@sportlife.com",
  "password": "segura123"
}
```

**Ejemplo de salida (éxito):**
```json
{
  "userId": 1,
  "token": "Y2FybG9zQHNwb3J0bGlmZS5jb206MTcxMjc2MDAwMA=="
}
```

**Validaciones de input:**
- `fullName`: no puede ser vacío ni nulo.
- `email`: formato válido de email (anotación `@Email`), no puede estar vacío.
- `password`: no puede ser vacío ni nulo.

**Validaciones de negocio:**
- El email no debe existir previamente en la base de datos.

**Códigos HTTP:**

| Escenario | Código | Mensaje |
|-----------|--------|---------|
| Registro exitoso | `200 OK` | `{ "userId": 1, "token": "..." }` |
| Email ya registrado | `400 Bad Request` | `{ "code": "BUSINESS_ERROR", "message": "El email ya esta registrado" }` |
| Campos inválidos | `400 Bad Request` | `{ "code": "VALIDATION_ERROR", "message": "must not be blank" }` |

---

### F02 — Login / Autenticación

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `POST` |
| **URL** | `/api/v1/auth/login` |
| **¿Es idempotente?** | ❌ No |
| **Razón técnica** | Aunque en términos del estado de la base de datos el login no modifica datos, genera un token nuevo en cada invocación. Por convención REST y semántica, `POST` es el verbo correcto para operaciones de autenticación ya que "crea" una sesión/token. No es idempotente porque dos llamadas iguales producen tokens distintos (timestamp diferente). |

**Datos de entrada:**

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `email` | `String (email)` | ✅ Sí |
| `password` | `String` | ✅ Sí |

**Ejemplo de entrada:**
```json
{
  "email": "carlos@sportlife.com",
  "password": "segura123"
}
```

**Ejemplo de salida:**
```json
{
  "token": "Y2FybG9zQHNwb3J0bGlmZS5jb206MTcxMjc2MDEwMA=="
}
```

**Validaciones de negocio:**
- El email debe existir en la base de datos.
- La contraseña debe coincidir con la almacenada.
- Mensaje genérico "Credenciales invalidas" para no revelar qué campo falló (seguridad).

**Códigos HTTP:**

| Escenario | Código | Mensaje |
|-----------|--------|---------|
| Login exitoso | `200 OK` | `{ "token": "..." }` |
| Credenciales incorrectas | `400 Bad Request` | `{ "code": "BUSINESS_ERROR", "message": "Credenciales invalidas" }` |

---

### F03 — Listar Productos

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `GET` |
| **URL** | `/api/v1/products` |
| **¿Es idempotente?** | ✅ Sí |
| **Razón técnica** | `GET` es siempre idempotente por definición HTTP: múltiples llamadas idénticas devuelven el mismo resultado sin alterar el estado del servidor. No tiene cuerpo de solicitud y solo lee datos. |

**Datos de entrada:** Ninguno (sin parámetros).

**Ejemplo de salida:**
```json
[
  {
    "id": 1,
    "name": "Balon Pro",
    "category": "futbol",
    "description": "Balon profesional talla 5",
    "price": 120000,
    "stock": 25
  },
  {
    "id": 2,
    "name": "Camiseta Runner",
    "category": "ropa",
    "description": "Camiseta transpirable",
    "price": 65000,
    "stock": 40
  }
]
```

**Validaciones:** No aplica (consulta abierta).

**Códigos HTTP:**

| Escenario | Código | Resultado |
|-----------|--------|-----------|
| Consulta exitosa | `200 OK` | Array de productos (puede ser vacío `[]`) |

---

### F04 — Buscar Producto por Nombre

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `GET` |
| **URL** | `/api/v1/products/search?name={nombre}` |
| **¿Es idempotente?** | ✅ Sí |
| **Razón técnica** | Operación de solo lectura (`GET`). La misma búsqueda siempre retorna el mismo conjunto de resultados dado el mismo estado de la BD. |

**Datos de entrada:**

| Parámetro | Tipo | Obligatorio | Descripción |
|-----------|------|-------------|-------------|
| `name` | `String (query param)` | ✅ Sí | Nombre o fragmento a buscar |

**Ejemplo:** `GET /api/v1/products/search?name=balon`

**Ejemplo de salida:**
```json
[
  {
    "id": 1,
    "name": "Balon Pro",
    "category": "futbol",
    "price": 120000,
    "stock": 25
  }
]
```

**Validaciones de negocio:** La búsqueda es case-insensitive (ignora mayúsculas/minúsculas).

**Códigos HTTP:**

| Escenario | Código |
|-----------|--------|
| Búsqueda exitosa (con o sin resultados) | `200 OK` |
| Parámetro `name` ausente | `400 Bad Request` |

---

### F05 — Filtrar Productos por Categoría

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `GET` |
| **URL** | `/api/v1/products/category?category={cat}` |
| **¿Es idempotente?** | ✅ Sí |
| **Razón técnica** | Igual que F04: operación de lectura pura. |

**Datos de entrada:**

| Parámetro | Tipo | Obligatorio |
|-----------|------|-------------|
| `category` | `String (query param)` | ✅ Sí |

**Ejemplo:** `GET /api/v1/products/category?category=gym`

**Ejemplo de salida:**
```json
[
  {
    "id": 3,
    "name": "Mancuernas 10kg",
    "category": "gym",
    "description": "Par de mancuernas",
    "price": 210000,
    "stock": 10
  }
]
```

**Códigos HTTP:**

| Escenario | Código |
|-----------|--------|
| Filtro exitoso | `200 OK` |
| Sin parámetro `category` | `400 Bad Request` |

---

### F06 — Ver Detalle de Producto

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `GET` |
| **URL** | `/api/v1/products/{id}` |
| **¿Es idempotente?** | ✅ Sí |
| **Razón técnica** | `GET` por ID es el ejemplo clásico de idempotencia: siempre devuelve el mismo recurso sin alterar nada. |

**Datos de entrada:**

| Parámetro | Tipo | Obligatorio | Ubicación |
|-----------|------|-------------|-----------|
| `id` | `Long` | ✅ Sí | Path variable |

**Ejemplo:** `GET /api/v1/products/1`

**Ejemplo de salida:**
```json
{
  "id": 1,
  "name": "Balon Pro",
  "category": "futbol",
  "description": "Balon profesional talla 5",
  "price": 120000,
  "stock": 25
}
```

**Códigos HTTP:**

| Escenario | Código | Mensaje |
|-----------|--------|---------|
| Producto encontrado | `200 OK` | Objeto producto |
| Producto no existe | `404 Not Found` | `{ "code": "NOT_FOUND", "message": "Producto no encontrado" }` |

---

### F07 — Agregar Producto al Carrito

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `POST` |
| **URL** | `/api/v1/cart/{userId}/items` |
| **¿Es idempotente?** | ❌ No |
| **Razón técnica** | Cada llamada acumula la cantidad al ítem existente (si el producto ya estaba en el carrito, suma). Dos llamadas con el mismo payload producen un estado diferente (cantidad duplicada), lo que rompe la idempotencia. |

**Datos de entrada:**

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `productId` | `Long` | ✅ Sí |
| `quantity` | `Integer (min=1)` | ✅ Sí |

**Path variable:**

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `userId` | `Long` | ✅ Sí |

**Ejemplo de entrada:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Ejemplo de salida:**
```json
{
  "userId": 5,
  "items": [
    {
      "productId": 1,
      "productName": "Balon Pro",
      "quantity": 2,
      "unitPrice": 120000,
      "subtotal": 240000
    }
  ],
  "total": 240000
}
```

**Validaciones de input:**
- `productId`: no nulo.
- `quantity`: no nulo, mínimo 1.

**Validaciones de negocio:**
- El producto debe existir.
- `quantity` no puede superar el stock disponible del producto.
- Si el producto ya está en el carrito, se suma la cantidad nueva; el total acumulado tampoco puede superar el stock.

**Códigos HTTP:**

| Escenario | Código | Mensaje |
|-----------|--------|---------|
| Ítem agregado | `200 OK` | Resumen del carrito actualizado |
| Producto no existe | `404 Not Found` | `NOT_FOUND` |
| Stock insuficiente | `400 Bad Request` | `"Stock insuficiente"` |
| Cantidad <= 0 | `400 Bad Request` | `"La cantidad debe ser mayor a 0"` |

---

### F08 — Ver Resumen del Carrito

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `GET` |
| **URL** | `/api/v1/cart/{userId}` |
| **¿Es idempotente?** | ✅ Sí |
| **Razón técnica** | Consulta de solo lectura. No altera estado. Múltiples llamadas iguales devuelven el mismo carrito. |

**Path variable:**

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `userId` | `Long` | ✅ Sí |

**Ejemplo de salida:**
```json
{
  "userId": 5,
  "items": [
    {
      "productId": 1,
      "productName": "Balon Pro",
      "quantity": 2,
      "unitPrice": 120000,
      "subtotal": 240000
    },
    {
      "productId": 2,
      "productName": "Camiseta Runner",
      "quantity": 1,
      "unitPrice": 65000,
      "subtotal": 65000
    }
  ],
  "total": 305000
}
```

**Códigos HTTP:**

| Escenario | Código |
|-----------|--------|
| Carrito encontrado (o vacío) | `200 OK` |

---

### F09 — Actualizar Cantidad en Carrito

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `PATCH` |
| **URL** | `/api/v1/cart/{userId}/items` |
| **¿Es idempotente?** | ✅ Sí |
| **Razón técnica** | `PATCH` que establece una cantidad específica (no acumulativa) es idempotente: llamar dos veces con `quantity: 3` deja el ítem siempre en 3 unidades. Distinto a `POST` que acumula. |

**Datos de entrada:**

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `productId` | `Long` | ✅ Sí |
| `quantity` | `Integer (min=1)` | ✅ Sí |

**Validaciones de negocio:**
- La nueva cantidad no puede superar el stock disponible.
- El producto debe existir en el carrito.

**Códigos HTTP:**

| Escenario | Código |
|-----------|--------|
| Actualización exitosa | `200 OK` |
| Stock insuficiente | `400 Bad Request` |
| Producto no encontrado | `404 Not Found` |

---

### F10 — Iniciar Checkout (Crear Orden)

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `POST` |
| **URL** | `/api/v1/checkout` |
| **¿Es idempotente?** | ❌ No |
| **Razón técnica** | Cada llamada crea una nueva orden y descuenta stock. Dos llamadas iguales generarían dos órdenes distintas y un doble descuento de inventario. |

**Datos de entrada:**

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `userId` | `Long` | ✅ Sí |

**Ejemplo de entrada:**
```json
{
  "userId": 5
}
```

**Ejemplo de salida:**
```json
{
  "orderId": 12,
  "userId": 5,
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "productName": "Balon Pro",
      "quantity": 2,
      "unitPrice": 120000
    }
  ],
  "total": 240000,
  "createdAt": "2024-04-10T10:30:00"
}
```

**Validaciones de negocio:**
- El carrito no puede estar vacío.
- Para cada ítem, el stock actual debe ser suficiente (validación en tiempo real al momento del checkout).
- El descuento de stock es transaccional: si algún ítem falla, ninguno se descuenta.

**Códigos HTTP:**

| Escenario | Código | Mensaje |
|-----------|--------|---------|
| Orden creada | `200 OK` | Resumen de la orden con estado PENDING |
| Carrito vacío | `400 Bad Request` | `"El carrito esta vacio"` |
| Stock insuficiente en checkout | `400 Bad Request` | `"Stock insuficiente para {producto}"` |

---

### F11 — Procesar Pago

| Aspecto | Detalle |
|---------|---------|
| **Verbo HTTP** | `POST` |
| **URL** | `/api/v1/checkout/payment` |
| **¿Es idempotente?** | ❌ No |
| **Razón técnica** | Procesar el pago dos veces sobre la misma orden produciría dos registros de pago y cambios de estado duplicados. En un sistema real se usaría un `idempotency-key` en el header para hacer esta operación reintentatble de forma segura. |

**Datos de entrada:**

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `orderId` | `Long` | ✅ Sí | ID de la orden a pagar |
| `method` | `String` | ✅ Sí | Método de pago (ej: "CARD", "CASH") |
| `amount` | `BigDecimal` | ✅ Sí | Monto enviado (debe ser >= total de la orden) |

**Ejemplo de entrada:**
```json
{
  "orderId": 12,
  "method": "CARD",
  "amount": 240000
}
```

**Ejemplo de salida (aprobado):**
```json
{
  "paymentId": "abc123-mongo-id",
  "orderId": 12,
  "method": "CARD",
  "amount": 240000,
  "approved": true,
  "orderStatus": "PAID"
}
```

**Ejemplo de salida (rechazado):**
```json
{
  "paymentId": null,
  "orderId": 12,
  "approved": false,
  "orderStatus": "REJECTED",
  "message": "El monto es menor al total de la orden"
}
```

**Validaciones de negocio:**
- La orden debe existir y estar en estado `PENDING`.
- El `amount` debe ser mayor o igual al total de la orden.
- Si el pago es aprobado: orden pasa a `PAID`, stock ya fue descontado en checkout.
- Si es rechazado: orden pasa a `REJECTED`, el stock NO se afecta (ya se descontó en checkout, pero en un MVP real se haría rollback o compensación).

**Códigos HTTP:**

| Escenario | Código | Mensaje |
|-----------|--------|---------|
| Pago aprobado | `200 OK` | `approved: true`, `orderStatus: "PAID"` |
| Monto insuficiente | `400 Bad Request` | `"El monto es menor al total de la orden"` |
| Orden no existe | `404 Not Found` | `"Orden no encontrada"` |

---

## 3. Diagrama de Componentes General

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENTE (Browser/App)                │
└─────────────────────────┬───────────────────────────────┘
                          │ HTTPS / REST JSON
                          ▼
┌─────────────────────────────────────────────────────────┐
│              API Gateway / Reverse Proxy                 │
│                    (Nginx / Azure)                       │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│              SportLife Backend (Spring Boot)             │
│  ┌─────────────┐  ┌────────────┐  ┌──────────────────┐  │
│  │ Controllers │  │  Services  │  │   Repositories   │  │
│  │  (REST API) │→ │ (Business) │→ │  (Persistence)   │  │
│  └─────────────┘  └────────────┘  └────────┬─────────┘  │
│                                            │             │
└────────────────────────────────────────────┼─────────────┘
                                             │
                    ┌────────────────────────┼────────────┐
                    │                        │            │
                    ▼                        ▼            │
         ┌──────────────────┐   ┌─────────────────────┐  │
         │  H2 / PostgreSQL │   │      MongoDB         │  │
         │  (Relacional)    │   │  (No Relacional)     │  │
         │  Users, Products,│   │  Carts, Payments     │  │
         │  Orders          │   │                      │  │
         └──────────────────┘   └─────────────────────┘  │
```

**¿Por qué esta arquitectura?**

El sistema utiliza **persistencia dual**:
- **H2/PostgreSQL (Relacional):** Para entidades con relaciones fuertes y transacciones ACID (usuarios, productos, órdenes). La consistencia transaccional es crítica al descontar stock.
- **MongoDB (No Relacional):** Para el carrito de compras (estructura flexible, varía por usuario) y pagos (documentos autocontenidos sin necesidad de joins).

---

## 4. Diagrama de Componentes Específico

```
com.sportlife
│
├── controller/                    [Capa de presentación]
│   ├── AuthController             POST /auth/register, /auth/login
│   ├── ProductController          GET  /products, /products/{id}, /search, /category
│   ├── CartController             POST /cart/{userId}/items, GET /cart/{userId}, PATCH /cart/{userId}/items
│   └── CheckoutController         POST /checkout, POST /checkout/payment
│
├── core/                          [Capa de negocio — agnóstica a infraestructura]
│   ├── services/
│   │   ├── AuthService (interface)
│   │   ├── AuthServiceImpl
│   │   ├── ProductService (interface)
│   │   ├── ProductServiceImpl
│   │   ├── CartService (interface)
│   │   ├── CartServiceImpl
│   │   ├── CheckoutService (interface)
│   │   └── CheckoutServiceImpl
│   ├── models/                    [Domain models — POJOs puros]
│   │   ├── User
│   │   ├── Product
│   │   ├── Cart / CartItem
│   │   ├── Order / OrderItem / OrderStatus
│   │   └── Payment
│   ├── validators/
│   │   └── StockValidator         Valida disponibilidad de stock
│   └── utils/
│       └── TokenUtils             Genera tokens Base64 (simulación JWT)
│
├── persistence/                   [Capa de infraestructura — acceso a datos]
│   ├── entities/
│   │   ├── UserEntity             @Entity JPA → tabla "users"
│   │   ├── ProductEntity          @Entity JPA → tabla "products"
│   │   ├── OrderEntity            @Entity JPA → tabla "orders"
│   │   ├── OrderItemEmbeddable    @Embeddable JPA
│   │   ├── CartDocument           @Document MongoDB → colección "carts"
│   │   ├── CartItemDocument       Subdocumento embebido
│   │   └── PaymentDocument        @Document MongoDB → colección "payments"
│   ├── repositories/
│   │   ├── UserRepository         JpaRepository<UserEntity, Long>
│   │   ├── ProductRepository      JpaRepository<ProductEntity, Long>
│   │   ├── OrderRepository        JpaRepository<OrderEntity, Long>
│   │   ├── CartRepository         MongoRepository<CartDocument, String>
│   │   └── PaymentRepository      MongoRepository<PaymentDocument, String>
│   ├── mappers/                   [Traducción entity ↔ domain model]
│   │   ├── UserPersistenceMapper
│   │   ├── ProductPersistenceMapper
│   │   ├── OrderPersistenceMapper
│   │   ├── CartPersistenceMapper
│   │   └── PaymentPersistenceMapper
│   └── config/
│       ├── DataInitializer        Carga datos semilla al arrancar
│       └── OpenApiConfig          Configuración Swagger/OpenAPI
│
├── dtos/                          [Objetos de transferencia de datos]
│   ├── request/
│   │   ├── RegisterUserRequest
│   │   ├── LoginRequest
│   │   ├── AddToCartRequest
│   │   ├── UpdateCartItemRequest
│   │   ├── CheckoutRequest
│   │   └── ProcessPaymentRequest
│   └── response/
│       ├── AuthResponse
│       ├── ProductResponse
│       ├── CartResponse / CartItemResponse
│       ├── OrderResponse
│       ├── PaymentResponse
│       └── ApiErrorResponse
│
├── handlers/                      [Manejo global de errores]
│   ├── GlobalExceptionHandler     @ControllerAdvice
│   ├── BusinessException          Errores de regla de negocio
│   └── ResourceNotFoundException  Recurso no encontrado (404)
│
└── mappers/
    └── DtoMapper                  Traduce domain model ↔ DTO de respuesta
```

---

## 5. Diagrama de Clases y Patrones de Software

### Modelos de Dominio

```
┌──────────────┐         ┌───────────────┐
│     User     │         │    Product    │
├──────────────┤         ├───────────────┤
│ id: Long     │         │ id: Long      │
│ fullName: Str│         │ name: String  │
│ email: String│         │ category: Str │
│ password: Str│         │ description   │
└──────────────┘         │ price: BigDec │
                         │ stock: Integer│
                         └───────────────┘

┌───────────┐  1     * ┌───────────────┐
│   Cart    │─────────▶│   CartItem    │
├───────────┤          ├───────────────┤
│ id: String│          │ productId:Long│
│ userId:Lng│          │ productName   │
└───────────┘          │ quantity: Int │
                       │ unitPrice     │
                       └───────────────┘

┌───────────┐  1     * ┌───────────────┐
│   Order   │─────────▶│  OrderItem    │
├───────────┤          ├───────────────┤
│ id: Long  │          │ productId:Long│
│ userId:Lng│          │ productName   │
│ total:Bdc │          │ quantity: Int │
│ status    │          │ unitPrice     │
│ createdAt │          └───────────────┘
└───────────┘

   «enum»
┌─────────────┐
│ OrderStatus │
├─────────────┤
│ PENDING     │
│ PAID        │
│ REJECTED    │
└─────────────┘

┌─────────────┐
│   Payment   │
├─────────────┤
│ id: String  │
│ orderId:Long│
│ method: Str │
│ amount: Bdc │
│ approved:boo│
└─────────────┘
```

### Patrones de Software Implementados

#### 1. Repository Pattern
**Dónde:** `UserRepository`, `ProductRepository`, `CartRepository`, etc.  
**Por qué:** Desacopla la lógica de negocio del mecanismo de persistencia. Los servicios no saben si están usando JPA o MongoDB; solo hablan con la interfaz del repositorio. Permite cambiar la base de datos sin tocar la lógica de negocio.

#### 2. Service Layer Pattern (Facade sobre negocio)
**Dónde:** `AuthService`, `ProductService`, `CartService`, `CheckoutService`.  
**Por qué:** Centraliza la lógica de negocio en una capa independiente. Los controladores solo orquestan (reciben request, invocan servicio, retornan response), sin contener lógica. Facilita pruebas unitarias (se mockean los servicios, no los controladores).

#### 3. Builder Pattern
**Dónde:** Todos los modelos de dominio (`User.builder()`, `Cart.builder()`, `Order.builder()`, etc.) gracias a la anotación `@Builder` de Lombok.  
**Por qué:** Los objetos del dominio tienen múltiples campos opcionales. El Builder evita constructores telescópicos y hace el código más legible y menos propenso a errores de orden de parámetros.

#### 4. DTO Pattern (Data Transfer Object)
**Dónde:** Paquetes `dtos/request` y `dtos/response`.  
**Por qué:** Separa la representación externa (lo que el cliente envía/recibe) de los modelos de dominio internos. Permite versionar la API sin cambiar el dominio. Evita exponer campos internos (como `password`) en las respuestas.

#### 5. Mapper Pattern (Anti-Corruption Layer)
**Dónde:** `UserPersistenceMapper`, `CartPersistenceMapper`, `DtoMapper`, etc.  
**Por qué:** Convierte entre las tres representaciones del sistema: entidad JPA/Mongo ↔ modelo de dominio ↔ DTO. Evita que el cambio de estructura en una capa afecte a las otras.

#### 6. Strategy / Validator Pattern
**Dónde:** `StockValidator`.  
**Por qué:** Encapsula una regla de validación reutilizable (verificar stock) en un componente independiente. Se inyecta donde se necesita (`CartServiceImpl`) y puede extenderse sin modificar los servicios que lo usan.

#### 7. Global Exception Handler (Chain of Responsibility)
**Dónde:** `GlobalExceptionHandler` con `@ControllerAdvice`.  
**Por qué:** Centraliza el manejo de todas las excepciones en un solo lugar. Evita try-catch repetidos en cada controlador. Garantiza respuestas de error consistentes en formato JSON para toda la API.

---

## 6. Diagramas de Base de Datos

### 6a. Modelo Relacional (H2 / PostgreSQL)

```
┌──────────────────────────────────────┐
│              USERS                   │
├──────────────────────────────────────┤
│ id          BIGINT  PK AUTO_INCREMENT│
│ full_name   VARCHAR(255)  NOT NULL   │
│ email       VARCHAR(255)  UNIQUE NN  │
│ password    VARCHAR(255)  NOT NULL   │
└──────────────────────────────────────┘
                  │
                  │ 1:N (un usuario puede tener muchas órdenes)
                  ▼
┌──────────────────────────────────────┐
│              ORDERS                  │
├──────────────────────────────────────┤
│ id          BIGINT  PK AUTO_INCREMENT│
│ user_id     BIGINT  FK → USERS(id)   │
│ total       DECIMAL(15,2)  NOT NULL  │
│ status      VARCHAR(20)    NOT NULL  │  ← PENDING | PAID | REJECTED
│ created_at  TIMESTAMP      NOT NULL  │
└──────────────────────────────────────┘
                  │
                  │ 1:N
                  ▼
┌──────────────────────────────────────┐
│           ORDER_ITEMS                │
├──────────────────────────────────────┤
│ id           BIGINT  PK              │
│ order_id     BIGINT  FK → ORDERS(id) │
│ product_id   BIGINT  FK → PRODUCTS   │
│ product_name VARCHAR(255)            │
│ quantity     INTEGER   NOT NULL      │
│ unit_price   DECIMAL(15,2)           │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│             PRODUCTS                 │
├──────────────────────────────────────┤
│ id          BIGINT  PK AUTO_INCREMENT│
│ name        VARCHAR(255)  NOT NULL   │
│ category    VARCHAR(100)  NOT NULL   │
│ description TEXT                     │
│ price       DECIMAL(15,2) NOT NULL   │
│ stock       INTEGER        NOT NULL  │
│ status      VARCHAR(20) DEFAULT'ACTIVO'│
└──────────────────────────────────────┘
```

**¿Por qué relacional para estas entidades?**
- `USERS` y `ORDERS` tienen una relación clara 1:N que se beneficia de integridad referencial (FK).
- El descuento de stock en `PRODUCTS` durante checkout requiere transacciones ACID para garantizar consistencia.
- `ORDER_ITEMS` es una tabla de unión clásica.

### 6b. Modelo No Relacional (MongoDB)

**Colección: `carts`**
```json
{
  "_id": "ObjectId('64abc...')",
  "userId": 5,
  "items": [
    {
      "productId": 1,
      "productName": "Balon Pro",
      "quantity": 2,
      "unitPrice": 120000
    },
    {
      "productId": 3,
      "productName": "Mancuernas 10kg",
      "quantity": 1,
      "unitPrice": 210000
    }
  ],
  "updatedAt": "2024-04-10T10:15:00Z"
}
```

**Colección: `payments`**
```json
{
  "_id": "ObjectId('64def...')",
  "orderId": 12,
  "method": "CARD",
  "amount": 450000,
  "approved": true,
  "transactionId": "TXN-20240410-12345",
  "processedAt": "2024-04-10T10:35:00Z"
}
```

**¿Por qué NoSQL para estas entidades?**
- El **carrito** es por naturaleza un documento autocontenido por usuario: no necesita joins, cambia frecuentemente y su estructura puede variar.
- Los **pagos** son registros de auditoría inmutables que crecen indefinidamente y se consultan por `orderId`; un documento embebido es más eficiente que múltiples tablas SQL.
- MongoDB permite almacenar el historial de ítems embebido, eliminando el overhead de joins.

---

## 7. Seguridad

### Tipo de seguridad recomendada: JWT + HTTPS

**¿Qué es JWT (JSON Web Token)?**  
Un estándar abierto (RFC 7519) para transmitir información de forma segura entre partes como un objeto JSON firmado digitalmente. Consta de tres partes: Header (algoritmo), Payload (claims: userId, email, rol, expiración) y Signature (firmada con clave secreta).

**¿Por qué JWT para SportLife?**

| Ventaja | Explicación |
|---------|-------------|
| **Sin estado (Stateless)** | El servidor no necesita guardar sesiones en BD. El token viaja en cada request y el servidor solo lo verifica. Escala horizontalmente sin problemas. |
| **Autorización por roles** | El payload puede incluir el rol del usuario (`ADMIN`, `CUSTOMER`). El backend verifica el rol sin consultar la BD en cada petición. |
| **Expiración configurable** | Se puede definir que los tokens expiren en 24h, forzando al usuario a re-autenticarse. |
| **Estándar de industria** | Compatible con Spring Security, OAuth2, y cualquier cliente moderno. |

**Implementación actual (MVP):** El proyecto usa una simulación Base64 en `TokenUtils`. En producción se reemplazaría por `spring-boot-starter-security` + `jjwt` library con firma HS256.

**Otras medidas de seguridad a implementar:**
- **BCrypt** para hashear contraseñas (nunca almacenar en texto plano).
- **Rate Limiting** en endpoints de autenticación para prevenir ataques de fuerza bruta.
- **Input Sanitization** para prevenir SQL Injection y XSS.
- **HTTPS obligatorio** en producción.

---

## 8. Roles y Permisos

Se identifican **dos roles principales**:

| Rol | Descripción |
|-----|-------------|
| `CUSTOMER` | Usuario registrado que puede comprar |
| `ADMIN` | Administrador de la plataforma |

### Matriz de permisos por funcionalidad:

| Funcionalidad | CUSTOMER | ADMIN | Anónimo |
|--------------|----------|-------|---------|
| Registrarse | ✅ | ✅ | ✅ |
| Login | ✅ | ✅ | ✅ |
| Listar productos | ✅ | ✅ | ✅ (público) |
| Ver detalle producto | ✅ | ✅ | ✅ (público) |
| Buscar/Filtrar productos | ✅ | ✅ | ✅ (público) |
| Agregar al carrito | ✅ | ❌ | ❌ |
| Ver carrito | ✅ (propio) | ✅ | ❌ |
| Actualizar carrito | ✅ (propio) | ❌ | ❌ |
| Checkout / Crear orden | ✅ | ❌ | ❌ |
| Procesar pago | ✅ | ❌ | ❌ |
| Crear/Editar productos | ❌ | ✅ | ❌ |
| Ver todas las órdenes | ❌ | ✅ | ❌ |
| Gestionar usuarios | ❌ | ✅ | ❌ |

**Justificación:**
- El catálogo de productos es **público** para maximizar la conversión (usuarios pueden ver productos sin registrarse).
- El carrito y checkout requieren autenticación para asociar la compra al usuario correcto.
- Un `CUSTOMER` solo puede ver/modificar **su propio** carrito (verificación de `userId` en el token vs `userId` en la URL).

---

## 9. TLS/SSL en API REST

### ¿Qué es TLS/SSL?
**TLS (Transport Layer Security)** es el protocolo criptográfico que cifra la comunicación entre cliente y servidor. SSL es su predecesor (hoy en desuso). HTTPS = HTTP + TLS.

### ¿Cómo se implementa en Spring Boot?

**Paso 1: Obtener certificado**
```bash
# Desarrollo: certificado auto-firmado con keytool
keytool -genkeypair -alias sportlife -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore sportlife.p12 -validity 365

# Producción: certificado de una CA (Let's Encrypt, DigiCert, Azure, etc.)
```

**Paso 2: Configurar application.yaml**
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:sportlife.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: sportlife
```

**Paso 3: Forzar redirect HTTP → HTTPS**
```java
@Bean
public ServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
        @Override
        protected void postProcessContext(Context context) {
            SecurityConstraint constraint = new SecurityConstraint();
            constraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            constraint.addCollection(collection);
            context.addConstraint(constraint);
        }
    };
    tomcat.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
    return tomcat;
}
```

### Ventajas para SportLife:

| Ventaja | Descripción |
|---------|-------------|
| **Confidencialidad** | Los tokens JWT, contraseñas y datos de pago viajan cifrados. Un atacante que intercepte el tráfico no puede leer el contenido. |
| **Integridad** | TLS garantiza que los datos no fueron modificados en tránsito (ataques Man-in-the-Middle). |
| **Autenticación del servidor** | El certificado prueba que el cliente se conecta al servidor legítimo de SportLife, no a uno falso. |
| **Confianza del usuario** | El candado en el navegador genera confianza al ingresar datos de pago. |
| **Cumplimiento normativo** | PCI-DSS (estándar de seguridad para pagos) exige TLS 1.2+ para transmitir datos de tarjetas. |

---

## 10. CORS en API REST

### ¿Qué es CORS?
**Cross-Origin Resource Sharing** es un mecanismo de seguridad del navegador que bloquea peticiones HTTP de un origen (dominio) diferente al del servidor, a menos que el servidor lo permita explícitamente.

**Ejemplo del problema:** Un frontend en `https://sportlife.com` intenta llamar a la API en `https://api.sportlife.com`. El navegador bloquea esta petición por ser de diferente origen.

### ¿Por qué es importante en SportLife?

SportLife tiene frontend y backend en dominios distintos (o puertos distintos en desarrollo: frontend en `localhost:3000`, API en `localhost:8080`). Sin CORS configurado, **ninguna llamada desde el frontend funcionaría**.

### Implementación en Spring Boot:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // En producción: solo el dominio del frontend
            .allowedOrigins("https://sportlife.com", "https://www.sportlife.com")
            // En desarrollo
            // .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
            .allowCredentials(true)
            .maxAge(3600); // Cache del preflight por 1 hora
    }
}
```

### ¿Por qué NO usar `allowedOrigins("*")` en producción?
Permitir todos los orígenes con `allowCredentials(true)` es inválido por spec. Además, abriría la API a peticiones desde cualquier sitio web malicioso (ataques CSRF). En producción siempre se especifican los dominios exactos permitidos.

### Beneficios clave:

| Beneficio | Descripción |
|-----------|-------------|
| **Seguridad** | Solo dominios autorizados pueden consumir la API |
| **Flexibilidad** | Permite diferentes configuraciones por entorno (dev/prod) |
| **Control granular** | Se puede permitir ciertos métodos HTTP solo a ciertos orígenes |

---

## 11. Diseño de Pantallas

El flujo de compra en Figma cubre las siguientes pantallas:

### Pantalla 1: Login / Registro
- Formulario con email y contraseña
- Link para registrarse si no tiene cuenta
- Validación en tiempo real de campos

### Pantalla 2: Catálogo de Productos
- Grid de productos con imagen, nombre, precio
- Barra de búsqueda por nombre
- Filtros por categoría (running, gym, ciclismo, ropa)
- Botón "Agregar al carrito" en cada tarjeta
- Contador de ítems en el carrito (header)

### Pantalla 3: Detalle de Producto
- Imagen grande del producto
- Nombre, descripción, precio
- Stock disponible
- Selector de cantidad (input numérico)
- Botón "Agregar al carrito"

### Pantalla 4: Carrito de Compras
- Lista de productos con imagen, nombre, cantidad y subtotal
- Botones para aumentar/disminuir cantidad
- Subtotal por ítem y total general
- Botón "Proceder al pago"

### Pantalla 5: Checkout / Pago
- Resumen de la orden (productos, cantidades, total)
- Formulario de método de pago (tarjeta/efectivo)
- Botón "Confirmar pago"

### Pantalla 6: Confirmación
- Mensaje de éxito con ícono
- ID de transacción único
- Resumen de productos comprados
- Botón "Seguir comprando"

### Pantalla 7: Pago Rechazado
- Mensaje de error claro
- Botón "Reintentar pago"
- Botón "Volver al carrito"

> **Nota:** El prototipo interactivo en Figma conecta estas pantallas siguiendo el flujo: Login → Catálogo → Detalle → Carrito → Checkout → Confirmación/Error

---

## 12. Parte Práctica

### Estructura del Proyecto (MVC + Maven)

```
ECI-SportLife/
├── pom.xml                          ← Configuración Maven
├── src/
│   ├── main/
│   │   ├── java/com/sportlife/
│   │   │   ├── SportLifeApplication.java
│   │   │   ├── controller/          ← Capa Vista/Controller (MVC)
│   │   │   ├── core/
│   │   │   │   ├── models/          ← Modelo de dominio
│   │   │   │   ├── services/        ← Lógica de negocio
│   │   │   │   ├── validators/
│   │   │   │   └── utils/
│   │   │   ├── persistence/
│   │   │   │   ├── entities/        ← Entidades JPA y documentos Mongo
│   │   │   │   ├── repositories/    ← Acceso a datos
│   │   │   │   ├── mappers/         ← Conversión entity ↔ model
│   │   │   │   └── config/
│   │   │   ├── dtos/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   └── handlers/
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/com/sportlife/
│           ├── core/CheckoutServiceImplTest.java
│           ├── controller/AuthControllerTest.java
│           └── persistence/ProductRepositoryTest.java
```

### Dependencias (pom.xml)

```xml
<dependencies>
  <!-- Spring Boot Web -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <!-- Validación de input (@NotBlank, @Email, @Min) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <!-- JPA — Persistencia relacional -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>

  <!-- MongoDB — Persistencia no relacional -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
  </dependency>

  <!-- Swagger / OpenAPI -->
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
  </dependency>

  <!-- Lombok — Reduce boilerplate (@Builder, @Data, @RequiredArgsConstructor) -->
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
  </dependency>

  <!-- H2 — Base de datos en memoria para desarrollo/pruebas -->
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>

  <!-- Testing: JUnit 5 + Mockito -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

### Configuración de Persistencia Dual (application.yaml)

```yaml
spring:
  # --- Persistencia Relacional (H2 en memoria para desarrollo) ---
  datasource:
    url: jdbc:h2:mem:sportlife;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update        # Crea/actualiza tablas automáticamente
    show-sql: false
  h2:
    console:
      enabled: true           # http://localhost:8080/h2-console

  # --- Persistencia No Relacional (MongoDB) ---
  data:
    mongodb:
      uri: mongodb://localhost:27017/sportlife

# --- Documentación Swagger ---
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

server:
  port: 8080
```

**¿Por qué configuración dual?**  
Spring Boot permite configurar JPA y MongoDB simultáneamente. JPA gestiona las conexiones relacionales a través de `DataSource`, mientras que Spring Data MongoDB usa su propio `MongoClient`. Ambos coexisten en el mismo contexto de aplicación sin conflictos gracias a la autoconfiguración de Spring Boot.

### Ejemplos de Código Clave

#### GlobalExceptionHandler — Manejo centralizado de errores
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse.builder()
                .code("NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.builder()
                .code("BUSINESS_ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
```

#### CheckoutServiceImpl — Lógica transaccional de checkout
```java
@Override
@Transactional
public Order checkout(Long userId) {
    Cart cart = cartService.getCart(userId);
    if (cart.getItems().isEmpty()) {
        throw new BusinessException("El carrito esta vacio");
    }

    // Crear orden con los ítems del carrito
    Order order = Order.builder()
        .userId(userId)
        .items(cart.getItems().stream().map(item -> OrderItem.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .build()).collect(Collectors.toList()))
        .total(cart.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add))
        .status(OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .build();

    // Descontar stock de forma transaccional
    order.getItems().forEach(item -> {
        var product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        int remaining = product.getStock() - item.getQuantity();
        if (remaining < 0) {
            throw new BusinessException("Stock insuficiente para " + product.getName());
        }
        product.setStock(remaining);
        productRepository.save(product);
    });

    OrderEntity saved = orderRepository.save(OrderPersistenceMapper.toEntity(order));
    cartService.clearCart(userId); // Vaciar carrito post-checkout
    return OrderPersistenceMapper.toModel(saved);
}
```

### Pruebas Unitarias

El proyecto incluye tres clases de prueba:

**1. `CheckoutServiceImplTest`** — Pruebas de la lógica de negocio con Mockito:
- `shouldCheckoutAndDiscountStock()`: verifica que el stock se descuente correctamente al hacer checkout.
- `shouldProcessPaymentAndMarkOrderAsPaid()`: verifica que el pago actualiza el estado de la orden a PAID.

**2. `AuthControllerTest`** — Pruebas de integración del controlador con MockMvc:
- `shouldLoginSuccessfully()`: verifica que el endpoint de login retorna un token.
- `shouldRegisterSuccessfully()`: verifica que el registro retorna userId y token.

**3. `ProductRepositoryTest`** — Pruebas del repositorio JPA:
- Verifica las queries de búsqueda por nombre e categoría.

### Cómo ejecutar el proyecto

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/ECI-SportLife.git
cd ECI-SportLife

# 2. Compilar y ejecutar pruebas
mvn clean test

# 3. Levantar la aplicación
mvn spring-boot:run

# 4. Acceder a la documentación
# Swagger UI: http://localhost:8080/swagger-ui.html
# API Docs:   http://localhost:8080/api-docs
# H2 Console: http://localhost:8080/h2-console
```

---

## 13. Pipeline CI/CD — GitHub Actions

### Estrategia de Ramas

```
main (producción)
  └── develop (integración)
        ├── feature/F01-registro-usuario
        ├── feature/F02-login
        ├── feature/F03-listar-productos
        ├── feature/F07-agregar-carrito
        └── feature/F10-checkout
```

**Flujo:**
1. Desarrollar en rama `feature/xxx`
2. Pull Request a `develop` → se ejecuta pipeline de CI
3. Merge a `develop` → pipeline completo (build + test + analysis)
4. Pull Request de `develop` a `main` → pipeline de deploy

### Archivo `.github/workflows/ci.yml`

```yaml
name: SportLife CI/CD Pipeline

on:
  push:
    branches: [ develop, main ]
  pull_request:
    branches: [ develop, main ]

jobs:
  # ──────────────────────────────
  # 1. BUILD
  # ──────────────────────────────
  build:
    name: Build
    runs-on: ubuntu-latest
    steps:
      - name: Checkout código
        uses: actions/checkout@v3

      - name: Configurar Java 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Compilar con Maven
        run: mvn clean compile -B

  # ──────────────────────────────
  # 2. TEST
  # ──────────────────────────────
  test:
    name: Test
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v3

      - name: Configurar Java 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Ejecutar pruebas unitarias
        run: mvn test -B

      - name: Publicar reporte de pruebas
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-reports
          path: target/surefire-reports/

  # ──────────────────────────────
  # 3. ANÁLISIS (SonarQube)
  # ──────────────────────────────
  analysis:
    name: SonarQube Analysis
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Necesario para análisis de historial

      - name: Configurar Java 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Análisis con SonarQube
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn verify sonar:sonar \
            -Dsonar.projectKey=sportlife-api \
            -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }} \
            -B

  # ──────────────────────────────
  # 4. DEPLOY (solo en main/develop)
  # ──────────────────────────────
  deploy:
    name: Deploy to Azure
    runs-on: ubuntu-latest
    needs: analysis
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v3

      - name: Configurar Java 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Empaquetar aplicación
        run: mvn clean package -DskipTests -B

      - name: Login en Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}

      - name: Deploy a Azure App Service
        uses: azure/webapps-deploy@v2
        with:
          app-name: sportlife-api
          package: target/sportlife-api-0.0.1-SNAPSHOT.jar
```

### Variables de entorno necesarias (GitHub Secrets)

| Secret | Descripción |
|--------|-------------|
| `SONAR_TOKEN` | Token de autenticación de SonarQube |
| `SONAR_HOST_URL` | URL de la instancia de SonarQube |
| `AZURE_CREDENTIALS` | JSON de credenciales de Service Principal de Azure |

### Despliegue en Azure

**Configuración del App Service:**
```bash
# Crear Resource Group
az group create --name sportlife-rg --location eastus

# Crear App Service Plan
az appservice plan create \
  --name sportlife-plan \
  --resource-group sportlife-rg \
  --sku B1 \
  --is-linux

# Crear Web App
az webapp create \
  --resource-group sportlife-rg \
  --plan sportlife-plan \
  --name sportlife-api \
  --runtime "JAVA:17-java17"

# Configurar variables de entorno en Azure
az webapp config appsettings set \
  --resource-group sportlife-rg \
  --name sportlife-api \
  --settings \
    SPRING_DATA_MONGODB_URI="${{ secrets.MONGO_URI }}" \
    SPRING_PROFILES_ACTIVE="prod"
```

---

## Resumen de Endpoints

| Método | URL | Funcionalidad |
|--------|-----|--------------|
| `POST` | `/api/v1/auth/register` | Registro de usuario |
| `POST` | `/api/v1/auth/login` | Autenticación |
| `GET` | `/api/v1/products` | Listar productos |
| `GET` | `/api/v1/products/search?name=` | Buscar por nombre |
| `GET` | `/api/v1/products/category?category=` | Filtrar por categoría |
| `GET` | `/api/v1/products/{id}` | Detalle de producto |
| `POST` | `/api/v1/cart/{userId}/items` | Agregar al carrito |
| `GET` | `/api/v1/cart/{userId}` | Ver carrito |
| `PATCH` | `/api/v1/cart/{userId}/items` | Actualizar cantidad |
| `POST` | `/api/v1/checkout` | Iniciar checkout |
| `POST` | `/api/v1/checkout/payment` | Procesar pago |

---

*Documentación generada para el Pre-Parcial Corte #2 — DOSW Company / SportLife MVP*
