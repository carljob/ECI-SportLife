# 🏋️ ECI-SportLife — Documentación Completa del MVP

> Proyecto desarrollado por **DOSW Company** para la plataforma de comercio electrónico deportivo **SportLife**.

---

## 📋 Tabla de Contenido

1. [Descripción del Proyecto](#1-descripción-del-proyecto)
2. [Matriz de Trazabilidad de Funcionalidades](#2-matriz-de-trazabilidad-de-funcionalidades)
3. [Especificación Detallada de Funcionalidades REST](#3-especificación-detallada-de-funcionalidades-rest)
4. [Diagrama de Componentes General](#4-diagrama-de-componentes-general)
5. [Diagrama de Componentes Específico](#5-diagrama-de-componentes-específico)
6. [Diagrama de Clases y Patrones de Software](#6-diagrama-de-clases-y-patrones-de-software)
7. [Diagrama de Base de Datos](#7-diagrama-de-base-de-datos)
8. [Seguridad de la Aplicación](#8-seguridad-de-la-aplicación)
9. [Roles y Permisos](#9-roles-y-permisos)
10. [TLS/SSL en una API REST](#10-tlsssl-en-una-api-rest)
11. [CORS en una API REST](#11-cors-en-una-api-rest)
12. [Pantallas (Figma)](#12-pantallas-figma)
13. [Parte Práctica — Implementación](#13-parte-práctica--implementación)

---

## 1. Descripción del Proyecto

**SportLife** es una tienda virtual especializada en productos deportivos (ropa, accesorios, implementos de entrenamiento). El MVP cubre el flujo completo de compra: registro de usuario → autenticación → navegación del catálogo → carrito → pago.

La aplicación expone una **API REST** construida con **Spring Boot**, usando persistencia **relacional (PostgreSQL/MySQL)** y **no relacional (MongoDB)**, documentada con **Swagger** y desplegada en **Azure**.

---

## 2. Matriz de Trazabilidad de Funcionalidades

### ¿Qué es una Matriz de Trazabilidad?

Una matriz de trazabilidad es una tabla que relaciona cada funcionalidad del sistema con su prioridad, dependencias y estado. Permite entender qué se debe construir primero, qué bloquea a qué, y cuáles son derivadas de otras funcionalidades.

**Criterios de prioridad usados:**
- **ALTA**: Sin esta funcionalidad, el sistema no puede operar. Es un bloqueador.
- **MEDIA**: Necesaria para cumplir el flujo completo, pero depende de funcionalidades de prioridad alta.
- **BAJA**: Mejoras o funcionalidades auxiliares que enriquecen la experiencia.

**Tipos de relación:**
- **Bloqueadora**: Esta funcionalidad debe existir para que otras funcionen. Ejemplo: no puedes agregar al carrito si no estás autenticado.
- **Derivada**: Esta funcionalidad se construye sobre otra ya existente. Ejemplo: ver el resumen del carrito deriva de haber podido agregar productos al carrito.
- **Independiente**: No depende de ninguna otra para desarrollarse, aunque en el flujo de negocio sí tenga un orden.

---

| ID  | Funcionalidad                        | Prioridad | Depende de     | Tipo de Relación     | Bloquea a              |
|-----|--------------------------------------|-----------|----------------|----------------------|------------------------|
| F01 | Registro de usuario                  | ALTA      | Ninguna        | Independiente        | F02                    |
| F02 | Autenticación (Login)                | ALTA      | F01            | Bloqueadora          | F05, F06, F07, F08     |
| F03 | Listar productos (con filtros)       | ALTA      | Ninguna        | Independiente        | F04, F05               |
| F04 | Ver detalle de un producto           | ALTA      | F03            | Derivada de F03      | F05                    |
| F05 | Agregar producto al carrito          | ALTA      | F02, F04       | Bloqueadora          | F06, F07               |
| F06 | Ver resumen del carrito              | MEDIA     | F05            | Derivada de F05      | F07                    |
| F07 | Modificar cantidad / eliminar ítem   | MEDIA     | F05            | Derivada de F05      | —                      |
| F08 | Procesar pago (checkout)             | ALTA      | F06            | Bloqueadora          | F09, F10               |
| F09 | Pago aprobado (actualizar stock)     | ALTA      | F08            | Derivada de F08      | —                      |
| F10 | Pago rechazado (reintento)           | MEDIA     | F08            | Derivada de F08      | —                      |

---

### Explicación de las relaciones más importantes

- **F01 → F02**: El registro es bloqueador del login porque sin una cuenta creada, no hay credenciales para autenticar. Relación **uno a uno en la secuencia de negocio**.

- **F02 → F05, F06, F07, F08**: La autenticación es bloqueadora de todo el flujo de compra porque el carrito y el pago deben estar asociados a un usuario identificado. Sin token JWT, estas rutas retornan `401 Unauthorized`.

- **F03 → F04**: Listar productos es bloqueador de ver el detalle porque para navegar a un producto necesitas su `productId`, el cual se obtiene del listado.

- **F05 → F06**: Agregar al carrito es bloqueador de ver el resumen porque el carrito solo existe si tiene ítems. Sin F05, F06 retornaría un carrito vacío sin utilidad de negocio.

- **F08 → F09/F10**: El checkout es bloqueador de ambos flujos de resultado (aprobado/rechazado) porque sin iniciar el proceso de pago no hay orden que pueda cambiar de estado.

---

## 3. Especificación Detallada de Funcionalidades REST

> Para cada funcionalidad se explica: verbo HTTP, idempotencia, entradas/salidas, validaciones y códigos de respuesta.

---

### F01 — Registro de Usuario

#### a. Verbo HTTP: `POST`

Se usa `POST` porque se está **creando un recurso nuevo** (un usuario) en el servidor. Cada llamada a este endpoint con los mismos datos genera un conflicto (email duplicado), lo que confirma que es una operación de creación, no de consulta o reemplazo.

#### b. ¿Es idempotente? **NO**

**Razón técnica**: Un endpoint es idempotente cuando múltiples llamadas con los mismos datos producen el mismo resultado en el servidor (sin efectos adicionales). `POST /register` **no es idempotente** porque:
- La primera llamada crea el usuario → HTTP 201.
- La segunda llamada con el mismo email lanza un error → HTTP 409.
- El estado del servidor cambia en la primera llamada (se inserta un registro en la BD).

#### c. Datos de Entrada

| Campo      | Tipo     | Obligatorio | Descripción                          |
|------------|----------|-------------|--------------------------------------|
| `name`     | `String` | ✅ Sí       | Nombre completo del usuario          |
| `email`    | `String` | ✅ Sí       | Correo electrónico único             |
| `password` | `String` | ✅ Sí       | Contraseña (mínimo 8 caracteres)     |

#### Datos de Salida

| Campo       | Tipo       | Descripción                       |
|-------------|------------|-----------------------------------|
| `id`        | `UUID`     | Identificador único del usuario   |
| `name`      | `String`   | Nombre del usuario                |
| `email`     | `String`   | Email registrado                  |
| `createdAt` | `DateTime` | Fecha de creación de la cuenta    |

#### d. Ejemplo de Entrada y Salida

**Request:**
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "Carlos Pérez",
  "email": "carlos.perez@email.com",
  "password": "MiPassword123"
}
```

**Response (Happy Path — 201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Carlos Pérez",
  "email": "carlos.perez@email.com",
  "createdAt": "2024-10-15T10:30:00Z"
}
```

#### e. Validaciones

**De input (formato/estructura):**
- `name`: no puede ser nulo ni vacío; longitud entre 2 y 100 caracteres.
- `email`: formato válido de email (regex estándar RFC 5322); no puede ser nulo.
- `password`: mínimo 8 caracteres; debe tener al menos una mayúscula, un número.

**De negocio:**
- El email **no debe existir** previamente en la base de datos (unicidad).
- La contraseña debe almacenarse **hasheada** con BCrypt (nunca en texto plano).

#### f. Códigos HTTP

| Código | Cuándo ocurre                                       | Mensaje                                    |
|--------|-----------------------------------------------------|--------------------------------------------|
| 201    | Usuario creado exitosamente                         | `"Usuario registrado correctamente"`       |
| 400    | Campos faltantes o formato inválido                 | `"El campo email no tiene formato válido"` |
| 409    | El email ya existe en el sistema                    | `"El email ya está registrado"`            |
| 500    | Error interno del servidor                          | `"Error interno, intente más tarde"`       |

---

### F02 — Autenticación (Login)

#### a. Verbo HTTP: `POST`

Se usa `POST` porque se **envían credenciales en el cuerpo** de la solicitud (nunca en la URL por seguridad). Aunque no crea un recurso persistente en BD, genera un token JWT (un recurso temporal) → semánticamente es una creación de sesión.

#### b. ¿Es idempotente? **NO**

**Razón técnica**: Cada llamada al login genera un nuevo token JWT con una fecha de expiración diferente (`iat` y `exp` distintos). El estado del servidor puede variar si se registran sesiones activas. Por tanto, no es idempotente.

#### c. Datos de Entrada

| Campo      | Tipo     | Obligatorio | Descripción              |
|------------|----------|-------------|--------------------------|
| `email`    | `String` | ✅ Sí       | Correo electrónico       |
| `password` | `String` | ✅ Sí       | Contraseña del usuario   |

#### Datos de Salida

| Campo          | Tipo     | Descripción                              |
|----------------|----------|------------------------------------------|
| `accessToken`  | `String` | Token JWT para autenticar peticiones     |
| `tokenType`    | `String` | Tipo de token (siempre `"Bearer"`)       |
| `expiresIn`    | `int`    | Tiempo de vida del token en segundos     |

#### d. Ejemplo de Entrada y Salida

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "carlos.perez@email.com",
  "password": "MiPassword123"
}
```

**Response (Happy Path — 200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjYXJsb3MucGVyZXoiLCJpYXQiOjE3MDAwMDAwMDB9.signature",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### e. Validaciones

**De input:**
- `email` y `password`: no pueden ser nulos ni vacíos.
- `email`: formato válido de correo.

**De negocio:**
- El email debe existir en la base de datos.
- La contraseña ingresada debe coincidir con el hash BCrypt almacenado.
- Si hay X intentos fallidos seguidos, bloquear temporalmente (política de seguridad recomendada).

#### f. Códigos HTTP

| Código | Cuándo ocurre                          | Mensaje                                      |
|--------|----------------------------------------|----------------------------------------------|
| 200    | Login exitoso, token generado          | Token en el body                             |
| 400    | Campos vacíos o formato incorrecto     | `"Credenciales con formato inválido"`        |
| 401    | Email o contraseña incorrectos         | `"Credenciales inválidas"`                   |
| 500    | Error interno                          | `"Error interno, intente más tarde"`         |

---

### F03 — Listar Productos (con filtros)

#### a. Verbo HTTP: `GET`

Se usa `GET` porque es una **operación de consulta** que no modifica el estado del servidor. Los filtros se pasan como **query parameters** en la URL.

#### b. ¿Es idempotente? **SÍ**

**Razón técnica**: `GET` es idempotente por definición en HTTP. Llamar al mismo endpoint con los mismos parámetros N veces devuelve siempre el mismo resultado (el mismo listado de productos). No altera el estado del servidor.

#### c. Datos de Entrada (Query Parameters)

| Parámetro    | Tipo      | Obligatorio | Descripción                          |
|--------------|-----------|-------------|--------------------------------------|
| `category`   | `String`  | ❌ No       | Filtrar por categoría (ej: running)  |
| `name`       | `String`  | ❌ No       | Buscar por nombre del producto       |
| `page`       | `int`     | ❌ No       | Número de página (default: 0)        |
| `size`       | `int`     | ❌ No       | Cantidad por página (default: 10)    |

#### Datos de Salida

| Campo        | Tipo              | Descripción                         |
|--------------|-------------------|-------------------------------------|
| `content`    | `List<Product>`   | Lista de productos                  |
| `totalPages` | `int`             | Total de páginas disponibles        |
| `totalItems` | `int`             | Total de productos encontrados      |

Cada producto en la lista contiene:

| Campo       | Tipo       | Descripción                  |
|-------------|------------|------------------------------|
| `id`        | `UUID`     | Identificador único          |
| `name`      | `String`   | Nombre del producto          |
| `category`  | `String`   | Categoría                    |
| `price`     | `Double`   | Precio unitario              |
| `stock`     | `int`      | Unidades disponibles         |
| `status`    | `String`   | `ACTIVE` o `INACTIVE`        |
| `imageUrl`  | `String`   | URL de la imagen principal   |

#### d. Ejemplo de Entrada y Salida

**Request:**
```http
GET /api/v1/products?category=running&page=0&size=5
Authorization: Bearer <token>
```

**Response (Happy Path — 200 OK):**
```json
{
  "content": [
    {
      "id": "prod-001",
      "name": "Zapatillas Nike Air Max",
      "category": "running",
      "price": 180000,
      "stock": 25,
      "status": "ACTIVE",
      "imageUrl": "https://cdn.sportlife.com/zapatillas-nike.jpg"
    }
  ],
  "totalPages": 3,
  "totalItems": 15
}
```

#### e. Validaciones

**De input:**
- `page` debe ser >= 0.
- `size` debe ser entre 1 y 100.
- `category` si se envía, debe ser un valor del enum de categorías válidas.

**De negocio:**
- Solo se retornan productos con `status = ACTIVE`.
- La búsqueda por `name` es **insensible a mayúsculas** (ILIKE en SQL).

#### f. Códigos HTTP

| Código | Cuándo ocurre                         | Mensaje                            |
|--------|---------------------------------------|------------------------------------|
| 200    | Listado retornado correctamente       | Lista en el body                   |
| 400    | Parámetro con formato inválido        | `"El parámetro page debe ser >= 0"`|
| 204    | No hay productos que coincidan        | Body vacío                         |
| 500    | Error interno                         | `"Error interno"`                  |

---

### F04 — Ver Detalle de un Producto

#### a. Verbo HTTP: `GET`

Consulta de **un recurso específico** identificado por su ID en el path. No modifica nada en el servidor.

#### b. ¿Es idempotente? **SÍ**

**Razón técnica**: `GET /products/{id}` siempre devuelve el mismo producto (o 404 si no existe). Múltiples llamadas producen el mismo resultado sin alterar el estado del servidor.

#### c. Datos de Entrada

| Parámetro | Tipo     | Ubicación   | Obligatorio | Descripción           |
|-----------|----------|-------------|-------------|-----------------------|
| `id`      | `UUID`   | Path param  | ✅ Sí       | ID del producto       |

#### Datos de Salida

| Campo         | Tipo            | Descripción                          |
|---------------|-----------------|--------------------------------------|
| `id`          | `UUID`          | Identificador único                  |
| `name`        | `String`        | Nombre del producto                  |
| `description` | `String`        | Descripción completa                 |
| `category`    | `String`        | Categoría                            |
| `price`       | `Double`        | Precio unitario                      |
| `stock`       | `int`           | Unidades disponibles                 |
| `images`      | `List<String>`  | URLs de todas las imágenes           |
| `status`      | `String`        | `ACTIVE` o `INACTIVE`                |

#### d. Ejemplo

**Request:**
```http
GET /api/v1/products/prod-001
Authorization: Bearer <token>
```

**Response (Happy Path — 200 OK):**
```json
{
  "id": "prod-001",
  "name": "Zapatillas Nike Air Max",
  "description": "Zapatillas de alto rendimiento para running de larga distancia.",
  "category": "running",
  "price": 180000,
  "stock": 25,
  "images": [
    "https://cdn.sportlife.com/zapatillas-1.jpg",
    "https://cdn.sportlife.com/zapatillas-2.jpg"
  ],
  "status": "ACTIVE"
}
```

#### e. Validaciones

**De input:**
- El `id` debe ser un UUID válido (formato `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).

**De negocio:**
- El producto debe existir en la BD.
- Si el producto está `INACTIVE`, retornar `404` (no visible para el cliente).

#### f. Códigos HTTP

| Código | Cuándo ocurre                       | Mensaje                             |
|--------|-------------------------------------|-------------------------------------|
| 200    | Producto encontrado y activo        | Detalle del producto                |
| 400    | UUID con formato inválido           | `"ID de producto inválido"`         |
| 404    | Producto no existe o está inactivo  | `"Producto no encontrado"`          |
| 500    | Error interno                       | `"Error interno"`                   |

---

### F05 — Agregar Producto al Carrito

#### a. Verbo HTTP: `POST`

Se **crea o modifica un ítem** dentro del carrito. Aunque es una modificación, se usa `POST` porque se está añadiendo un ítem que podría no existir aún en el carrito.

> **Nota**: Podría usarse `PUT` si el carrito ya existe y se reemplaza todo el ítem, o `PATCH` si se actualiza parcialmente. En este caso `POST` es correcto para añadir un ítem nuevo o acumular cantidad.

#### b. ¿Es idempotente? **NO**

**Razón técnica**: Si el mismo producto ya está en el carrito y el usuario vuelve a hacer `POST` con la misma cantidad, el comportamiento puede ser **acumular** (sumar cantidades) o **reemplazar**. Si acumula, cada llamada cambia el estado → no es idempotente. Si reemplaza, sería idempotente, pero la semántica habitual de "agregar al carrito" es acumular.

#### c. Datos de Entrada

| Campo       | Tipo   | Obligatorio | Descripción                          |
|-------------|--------|-------------|--------------------------------------|
| `productId` | `UUID` | ✅ Sí       | ID del producto a agregar            |
| `quantity`  | `int`  | ✅ Sí       | Cantidad deseada (mínimo 1)          |

Tomado del token JWT:
- `userId` (extraído del token, no se envía en el body)

#### Datos de Salida

| Campo         | Tipo          | Descripción                          |
|---------------|---------------|--------------------------------------|
| `cartId`      | `UUID`        | ID del carrito                       |
| `items`       | `List<Item>`  | Lista actualizada de ítems           |
| `totalAmount` | `Double`      | Total actualizado del carrito        |

#### d. Ejemplo

**Request:**
```http
POST /api/v1/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": "prod-001",
  "quantity": 2
}
```

**Response (Happy Path — 200 OK):**
```json
{
  "cartId": "cart-xyz",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Zapatillas Nike Air Max",
      "quantity": 2,
      "unitPrice": 180000,
      "subtotal": 360000
    }
  ],
  "totalAmount": 360000
}
```

#### e. Validaciones

**De input:**
- `productId` no puede ser nulo; debe ser UUID válido.
- `quantity` debe ser un entero >= 1.

**De negocio:**
- El producto debe existir y estar `ACTIVE`.
- El stock disponible debe ser >= `quantity` solicitada. Si no hay stock suficiente → error de negocio.
- El usuario debe estar autenticado (token válido).

#### f. Códigos HTTP

| Código | Cuándo ocurre                                  | Mensaje                                        |
|--------|------------------------------------------------|------------------------------------------------|
| 200    | Producto agregado/actualizado en el carrito    | Carrito actualizado                            |
| 400    | Campos inválidos o faltantes                   | `"La cantidad debe ser mayor a 0"`             |
| 401    | Token ausente o inválido                       | `"No autorizado"`                              |
| 404    | Producto no encontrado                         | `"Producto no encontrado"`                     |
| 422    | Stock insuficiente                             | `"Stock insuficiente para la cantidad pedida"` |
| 500    | Error interno                                  | `"Error interno"`                              |

---

### F06 — Ver Resumen del Carrito

#### a. Verbo HTTP: `GET`

Consulta del estado actual del carrito del usuario autenticado.

#### b. ¿Es idempotente? **SÍ**

**Razón técnica**: `GET` es idempotente. Llamar este endpoint N veces devuelve siempre el mismo carrito (mientras no haya modificaciones entre llamadas). No altera el estado del servidor.

#### c. Datos de Entrada

Solo el token JWT en el header (el `userId` se extrae de él). No hay body ni query params obligatorios.

#### Datos de Salida

| Campo         | Tipo          | Descripción                          |
|---------------|---------------|--------------------------------------|
| `cartId`      | `UUID`        | ID del carrito                       |
| `items`       | `List<Item>`  | Productos en el carrito              |
| `subtotal`    | `Double`      | Suma de subtotales                   |
| `total`       | `Double`      | Total (puede incluir impuestos)      |

Cada `Item` contiene: `productId`, `productName`, `quantity`, `unitPrice`, `subtotal`.

#### d. Ejemplo

**Request:**
```http
GET /api/v1/cart
Authorization: Bearer <token>
```

**Response (Happy Path — 200 OK):**
```json
{
  "cartId": "cart-xyz",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Zapatillas Nike Air Max",
      "quantity": 2,
      "unitPrice": 180000,
      "subtotal": 360000
    },
    {
      "productId": "prod-002",
      "productName": "Camiseta Adidas Climalite",
      "quantity": 1,
      "unitPrice": 75000,
      "subtotal": 75000
    }
  ],
  "subtotal": 435000,
  "total": 435000
}
```

#### e. Validaciones

**De negocio:**
- El usuario debe estar autenticado.
- Si el carrito está vacío, retornar body con lista vacía (no es un error).

#### f. Códigos HTTP

| Código | Cuándo ocurre               | Mensaje                        |
|--------|-----------------------------|--------------------------------|
| 200    | Carrito retornado           | Resumen del carrito            |
| 401    | Token inválido o ausente    | `"No autorizado"`              |
| 500    | Error interno               | `"Error interno"`              |

---

### F07 — Modificar Cantidad / Eliminar Ítem del Carrito

#### Actualizar cantidad — Verbo HTTP: `PATCH`

`PATCH` se usa cuando se actualiza **parcialmente** un recurso existente. Aquí solo se cambia la `quantity` de un ítem del carrito, no todo el carrito.

#### ¿Es idempotente? **SÍ** (para PATCH en este caso)

**Razón técnica**: Si envías `PATCH` con `quantity: 3` para el mismo ítem dos veces, el resultado final es el mismo (la cantidad queda en 3). Sin efectos adicionales por llamadas repetidas → es idempotente.

#### Eliminar ítem — Verbo HTTP: `DELETE`

`DELETE` elimina un recurso específico del carrito.

#### ¿Es idempotente? **SÍ**

**Razón técnica**: Borrar el mismo ítem dos veces produce el mismo resultado: el ítem no está en el carrito. La segunda llamada devuelve `404` o `204` pero no genera efectos adicionales.

#### Datos de Entrada para PATCH

| Campo      | Tipo   | Ubicación  | Obligatorio | Descripción              |
|------------|--------|------------|-------------|--------------------------|
| `itemId`   | `UUID` | Path param | ✅ Sí       | ID del ítem en el carrito|
| `quantity` | `int`  | Body       | ✅ Sí       | Nueva cantidad           |

#### Datos de Entrada para DELETE

| Campo    | Tipo   | Ubicación  | Obligatorio | Descripción              |
|----------|--------|------------|-------------|--------------------------|
| `itemId` | `UUID` | Path param | ✅ Sí       | ID del ítem a eliminar   |

#### Ejemplo PATCH:

**Request:**
```http
PATCH /api/v1/cart/items/item-001
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}
```

**Response (200 OK):**
```json
{
  "itemId": "item-001",
  "quantity": 3,
  "subtotal": 540000,
  "cartTotal": 615000
}
```

#### Ejemplo DELETE:

```http
DELETE /api/v1/cart/items/item-001
Authorization: Bearer <token>
```

**Response (204 No Content):** Body vacío.

#### Validaciones

- `quantity` para PATCH debe ser >= 1; si se envía 0 se debería eliminar el ítem (o retornar error).
- Validar stock disponible al actualizar la cantidad.
- El ítem debe pertenecer al carrito del usuario autenticado (no puede modificar carrito ajeno).

#### Códigos HTTP

| Código | Cuándo ocurre                     | Mensaje                                  |
|--------|-----------------------------------|------------------------------------------|
| 200    | Cantidad actualizada              | Ítem actualizado                         |
| 204    | Ítem eliminado exitosamente       | Body vacío                               |
| 400    | Cantidad inválida                 | `"La cantidad debe ser mayor a 0"`       |
| 401    | No autenticado                    | `"No autorizado"`                        |
| 403    | El ítem no pertenece al usuario   | `"Acceso denegado"`                      |
| 404    | Ítem no encontrado en el carrito  | `"Ítem no encontrado"`                   |
| 422    | Stock insuficiente para la nueva cantidad | `"Stock insuficiente"`            |

---

### F08 — Procesar Pago (Checkout)

#### a. Verbo HTTP: `POST`

Se **crea una orden de compra** y se inicia el proceso de pago. Es una creación de un recurso nuevo (la orden).

#### b. ¿Es idempotente? **NO**

**Razón técnica**: Cada llamada al checkout crea una nueva orden con un ID único y procesa el pago. Dos llamadas idénticas podrían generar dos cobros y dos órdenes distintas → definitivamente no es idempotente. Para evitar cobros duplicados se recomienda implementar un **Idempotency Key** en el header.

#### c. Datos de Entrada

| Campo            | Tipo     | Obligatorio | Descripción                        |
|------------------|----------|-------------|------------------------------------|
| `paymentMethod`  | `String` | ✅ Sí       | Método de pago (CARD, PSE, etc.)   |
| `cardNumber`     | `String` | ❌ No*      | Número de tarjeta (si aplica)      |
| `cardExpiry`     | `String` | ❌ No*      | Vencimiento tarjeta (MM/YY)        |
| `cardCvv`        | `String` | ❌ No*      | CVV de la tarjeta                  |

> *Obligatorio si `paymentMethod = CARD`

#### Datos de Salida

| Campo             | Tipo       | Descripción                          |
|-------------------|------------|--------------------------------------|
| `orderId`         | `UUID`     | ID único de la orden                 |
| `transactionId`   | `String`   | ID único de la transacción de pago   |
| `status`          | `String`   | `PAID` o `REJECTED`                  |
| `total`           | `Double`   | Total cobrado                        |
| `createdAt`       | `DateTime` | Fecha de la orden                    |
| `message`         | `String`   | Mensaje de confirmación o error      |

#### d. Ejemplo

**Request:**
```http
POST /api/v1/checkout
Authorization: Bearer <token>
Content-Type: application/json

{
  "paymentMethod": "CARD",
  "cardNumber": "4111111111111111",
  "cardExpiry": "12/26",
  "cardCvv": "123"
}
```

**Response — Pago Aprobado (200 OK):**
```json
{
  "orderId": "order-abc123",
  "transactionId": "txn-987654321",
  "status": "PAID",
  "total": 435000,
  "createdAt": "2024-10-15T11:00:00Z",
  "message": "Pago aprobado. Tu pedido está siendo procesado."
}
```

**Response — Pago Rechazado (200 OK con status REJECTED):**
```json
{
  "orderId": "order-abc124",
  "transactionId": "txn-987654322",
  "status": "REJECTED",
  "total": 435000,
  "createdAt": "2024-10-15T11:01:00Z",
  "message": "Pago rechazado. Verifique los datos de su tarjeta e intente nuevamente."
}
```

#### e. Validaciones

**De input:**
- `paymentMethod` debe ser un valor del enum: `CARD`, `PSE`, `CASH`.
- Si `paymentMethod = CARD`, los campos de tarjeta son obligatorios.
- `cardNumber`: 16 dígitos, solo números.
- `cardExpiry`: formato MM/YY, no puede estar vencida.
- `cardCvv`: 3 o 4 dígitos.

**De negocio:**
- El carrito no puede estar vacío.
- El stock de cada producto aún debe ser suficiente al momento del pago (puede haber cambiado desde que se agregó al carrito).
- Si el pago es aprobado: actualizar stock y marcar orden como `PAID`.
- Si el pago es rechazado: no modificar stock, marcar orden como `REJECTED`.

#### f. Códigos HTTP

| Código | Cuándo ocurre                                  | Mensaje                                      |
|--------|------------------------------------------------|----------------------------------------------|
| 200    | Proceso de pago ejecutado (aprobado o rechazado) | Ver status en el body                      |
| 400    | Datos de pago inválidos o faltantes            | `"Datos de tarjeta inválidos"`               |
| 401    | No autenticado                                 | `"No autorizado"`                            |
| 422    | Carrito vacío o stock insuficiente             | `"No hay productos en el carrito"`           |
| 500    | Error interno del servidor de pagos            | `"Error al procesar el pago"`                |

---

## 4. Diagrama de Componentes General

El diagrama de componentes **general** muestra los grandes bloques del sistema y cómo se comunican entre sí a alto nivel. No entra en detalles de clases o métodos, sino en responsabilidades de cada capa.

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTE                              │
│          (Navegador Web / App Móvil)                        │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS / REST
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY / NGINX                      │
│          (Enrutamiento, SSL Termination, CORS)              │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               SPORTLIFE - SPRING BOOT APP                   │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Auth Module │  │Product Module│  │  Cart Module     │  │
│  │  (JWT/BCrypt)│  │  (Catálogo)  │  │  (Carrito)       │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Payment Module (Checkout)               │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────┬────────────────────────┬────────────────────────┘
           │                        │
           ▼                        ▼
┌─────────────────┐      ┌──────────────────────┐
│   PostgreSQL    │      │      MongoDB          │
│  (Relacional)   │      │  (No Relacional)      │
│ Users, Orders,  │      │  Productos, Catálogo, │
│ Payments        │      │  Carrito (sesiones)   │
└─────────────────┘      └──────────────────────┘
```

**¿Por qué esta arquitectura?**
- El **API Gateway** centraliza la entrada, aplica SSL y maneja CORS, evitando que cada microservicio/módulo deba configurarlo individualmente.
- Los **módulos** dentro de Spring Boot están separados por responsabilidad de negocio (patrón de **separación de responsabilidades**).
- Se usan **dos bases de datos** porque los datos de usuarios y órdenes tienen estructura fija y relaciones claras (relacional), mientras que los productos con múltiples imágenes y el carrito temporal son más flexibles (no relacional).

---

## 5. Diagrama de Componentes Específico

El diagrama específico muestra la arquitectura interna de la aplicación SpringBoot, siguiendo el patrón **MVC** (Model-View-Controller, donde la "View" es la respuesta JSON de la API).

```
┌────────────────────────────────────────────────────────────────┐
│                    SPORTLIFE - Spring Boot                     │
│                                                                │
│  ┌─────────────────── CAPA CONTROLLER ───────────────────────┐ │
│  │  AuthController    ProductController    CartController     │ │
│  │  POST /register    GET /products        POST /cart/items   │ │
│  │  POST /login       GET /products/{id}   GET /cart          │ │
│  │                                         PATCH /cart/items  │ │
│  │                    OrderController      DELETE /cart/items  │ │
│  │                    POST /checkout                          │ │
│  └────────────────────────┬───────────────────────────────────┘ │
│                           │ llama a                             │
│  ┌─────────────────── CAPA SERVICE ──────────────────────────┐  │
│  │  AuthService       ProductService      CartService         │  │
│  │  - register()      - findAll()         - addItem()         │  │
│  │  - login()         - findById()        - getCart()         │  │
│  │  - generateJWT()   - filterBy()        - updateItem()      │  │
│  │                                        - removeItem()      │  │
│  │                    PaymentService                          │  │
│  │                    - processPayment()                      │  │
│  │                    - updateStock()                         │  │
│  └────────────────────────┬───────────────────────────────────┘  │
│                           │ llama a                              │
│  ┌─────────────────── CAPA REPOSITORY ───────────────────────┐   │
│  │  UserRepository        ProductRepository                  │   │
│  │  (JPA - PostgreSQL)    (MongoRepository - MongoDB)        │   │
│  │                                                           │   │
│  │  OrderRepository       CartRepository                     │   │
│  │  (JPA - PostgreSQL)    (MongoRepository - MongoDB)        │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─────────────────── CAPA MODEL ────────────────────────────┐   │
│  │  User    Product    CartItem    Order    Payment           │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─────────────────── CONFIGURACIÓN ─────────────────────────┐   │
│  │  SecurityConfig (JWT Filter)   SwaggerConfig               │   │
│  │  DataSourceConfig (dual BD)    CorsConfig                  │   │
│  └───────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────┘
```

**¿Por qué separar en Controller → Service → Repository?**
- **Controller**: Solo recibe la petición HTTP, valida el formato del input y delega la lógica. No debe tener reglas de negocio.
- **Service**: Contiene toda la lógica de negocio (validaciones de stock, generación de tokens, etc.). Es testeable de forma independiente con mocks.
- **Repository**: Abstrae el acceso a datos. Si cambias de PostgreSQL a MySQL, solo cambias la configuración, no la lógica de negocio.

---

## 6. Diagrama de Clases y Patrones de Software

### Clases Principales

```
┌─────────────────────┐         ┌──────────────────────────┐
│       User          │         │         Product           │
├─────────────────────┤         ├──────────────────────────┤
│ - id: UUID          │         │ - id: UUID               │
│ - name: String      │         │ - name: String           │
│ - email: String     │         │ - description: String    │
│ - password: String  │         │ - category: String       │
│ - role: Role (enum) │         │ - price: Double          │
│ - createdAt: Date   │         │ - stock: Integer         │
├─────────────────────┤         │ - images: List<String>   │
│ + register()        │         │ - status: ProductStatus  │
│ + authenticate()    │         ├──────────────────────────┤
└────────┬────────────┘         │ + isAvailable(): boolean │
         │ 1                    └──────────┬───────────────┘
         │                                 │
         │ 1                               │ N
┌────────▼────────────┐         ┌──────────▼───────────────┐
│       Cart          │◄────────│       CartItem           │
├─────────────────────┤   N     ├──────────────────────────┤
│ - id: UUID          │         │ - id: UUID               │
│ - userId: UUID      │         │ - productId: UUID        │
│ - items: List<Item> │         │ - quantity: Integer      │
│ - createdAt: Date   │         │ - unitPrice: Double      │
├─────────────────────┤         │ - subtotal: Double       │
│ + getTotal():Double │         └──────────────────────────┘
│ + addItem()         │
│ + removeItem()      │
└────────┬────────────┘
         │ 1
         │ genera
         ▼ 1
┌─────────────────────┐         ┌──────────────────────────┐
│       Order         │────────►│       Payment            │
├─────────────────────┤   1     ├──────────────────────────┤
│ - id: UUID          │         │ - id: UUID               │
│ - userId: UUID      │         │ - orderId: UUID          │
│ - items: List<Item> │         │ - transactionId: String  │
│ - total: Double     │         │ - method: PaymentMethod  │
│ - status: OrderStat.│         │ - status: PaymentStatus  │
│ - createdAt: Date   │         │ - processedAt: DateTime  │
└─────────────────────┘         └──────────────────────────┘
```

### Enumeraciones

```java
// Estados de la orden
enum OrderStatus { PENDING, PAID, REJECTED, CANCELLED }

// Estados de pago
enum PaymentStatus { APPROVED, REJECTED }

// Métodos de pago
enum PaymentMethod { CARD, PSE, CASH }

// Estado del producto
enum ProductStatus { ACTIVE, INACTIVE }

// Roles de usuario
enum Role { CUSTOMER, ADMIN }
```

---

### Patrones de Software Implementados

#### 1. Patrón Repository

**¿Qué es?** Abstrae la capa de acceso a datos detrás de una interfaz. Los servicios no conocen si están hablando con PostgreSQL, MongoDB o una BD en memoria.

**¿Por qué es necesario aquí?** SportLife usa dos bases de datos (PostgreSQL y MongoDB). Sin el patrón Repository, cada servicio tendría código específico de JPA o MongoDB mezclado con la lógica de negocio, haciendo el código imposible de testear y difícil de mantener.

```java
// Interfaz del repositorio
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategoryAndStatus(String category, ProductStatus status);
    List<Product> findByNameContainingIgnoreCase(String name);
}
```

#### 2. Patrón DTO (Data Transfer Object)

**¿Qué es?** Objetos que transportan datos entre capas sin exponer la entidad del modelo directamente.

**¿Por qué es necesario aquí?** Si se expone la entidad `User` directamente en la respuesta, se enviaría el campo `password` (hasheado o no). El DTO garantiza que solo se exponen los datos necesarios y seguros. Además, los DTOs desacoplan el modelo de la BD del contrato de la API.

```java
// Request DTO
public class RegisterRequestDTO {
    @NotBlank private String name;
    @Email private String email;
    @Size(min = 8) private String password;
}

// Response DTO (nunca expone password)
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
```

#### 3. Patrón Singleton (vía Spring)

**¿Qué es?** Garantiza que una clase tenga una sola instancia en toda la aplicación.

**¿Por qué es necesario aquí?** Los `@Service` y `@Repository` de Spring son Singletons por defecto. El `JwtUtil` que genera y valida tokens debe ser un Singleton para compartir la clave secreta de firma sin recrearla en cada petición.

```java
@Component
public class JwtUtil {
    private final String SECRET_KEY = "sportlife_secret_key_2024";
    // Una sola instancia en toda la app
    public String generateToken(String email) { ... }
    public boolean validateToken(String token) { ... }
}
```

#### 4. Patrón Factory Method (para pagos)

**¿Qué es?** Define una interfaz para crear objetos, pero permite a las subclases decidir qué clase instanciar.

**¿Por qué es necesario aquí?** SportLife soporta múltiples métodos de pago (CARD, PSE, CASH). Sin este patrón, el código de checkout tendría un `if/else` gigante. Con Factory, cada método de pago tiene su propia implementación y es fácil agregar nuevos métodos sin modificar código existente.

```java
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

public class CardPaymentProcessor implements PaymentProcessor {
    public PaymentResult process(PaymentRequest request) { /* lógica tarjeta */ }
}

public class PSEPaymentProcessor implements PaymentProcessor {
    public PaymentResult process(PaymentRequest request) { /* lógica PSE */ }
}

public class PaymentProcessorFactory {
    public static PaymentProcessor getProcessor(PaymentMethod method) {
        return switch (method) {
            case CARD -> new CardPaymentProcessor();
            case PSE -> new PSEPaymentProcessor();
            case CASH -> new CashPaymentProcessor();
        };
    }
}
```

---

## 7. Diagrama de Base de Datos

### a. Modelo Relacional (PostgreSQL)

El modelo relacional es apropiado para datos con **estructura fija y relaciones claras** entre entidades: usuarios, órdenes y pagos. Se basa en tablas, filas, columnas y claves foráneas.

**Tipos de relaciones usadas:**

- **Uno a Uno (1:1)**: Una orden tiene exactamente un pago, y un pago pertenece a exactamente una orden. Se implementa con una FK en la tabla `payments` apuntando a `orders`.
- **Uno a Muchos (1:N)**: Un usuario puede tener muchas órdenes, pero cada orden pertenece a un solo usuario. Se implementa con `user_id` como FK en `orders`.
- **Muchos a Muchos (N:M)**: Una orden puede tener muchos productos, y un producto puede aparecer en muchas órdenes. Se resuelve con una tabla intermedia `order_items` que rompe la relación en dos relaciones 1:N.

```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ id         UUID PK  │
│ name       VARCHAR  │
│ email      VARCHAR UNIQUE │
│ password   VARCHAR  │
│ role       VARCHAR  │
│ created_at TIMESTAMP│
└─────────┬───────────┘
          │ 1
          │ (un usuario puede tener muchas órdenes)
          │ N
┌─────────▼───────────┐         ┌─────────────────────┐
│       orders        │ 1 ─── 1 │      payments        │
├─────────────────────┤         ├─────────────────────┤
│ id         UUID PK  │         │ id          UUID PK  │
│ user_id    UUID FK  │         │ order_id    UUID FK  │
│ total      DECIMAL  │         │ transaction_id VARCHAR│
│ status     VARCHAR  │         │ method      VARCHAR  │
│ created_at TIMESTAMP│         │ status      VARCHAR  │
└─────────┬───────────┘         │ processed_at TIMESTAMP│
          │ 1                   └─────────────────────┘
          │ (una orden tiene muchos ítems)
          │ N
┌─────────▼───────────┐
│     order_items     │
├─────────────────────┤
│ id          UUID PK │
│ order_id    UUID FK │ ──► orders.id
│ product_id  UUID    │ (referencia al ID en MongoDB)
│ quantity    INTEGER │
│ unit_price  DECIMAL │
│ subtotal    DECIMAL │
└─────────────────────┘
```

> **¿Por qué `product_id` en `order_items` no tiene FK hacia una tabla `products`?** Porque los productos viven en MongoDB, no en PostgreSQL. Se guarda el ID como referencia lógica, y el precio unitario se copia al momento de la compra para que no cambie si el precio del producto se actualiza después.

---

### b. Modelo No Relacional (MongoDB)

MongoDB almacena documentos JSON. Es ideal para datos con **estructura variable** (un producto puede tener 1 imagen o 10), datos de **alta lectura** (el catálogo se consulta frecuentemente) y datos de **sesión temporal** (el carrito).

**Colecciones:**

#### Colección: `products`

```json
{
  "_id": "prod-001",
  "name": "Zapatillas Nike Air Max",
  "description": "Zapatillas de alto rendimiento para running de larga distancia.",
  "category": "running",
  "price": 180000,
  "stock": 25,
  "images": [
    "https://cdn.sportlife.com/img1.jpg",
    "https://cdn.sportlife.com/img2.jpg"
  ],
  "status": "ACTIVE",
  "createdAt": "2024-10-01T00:00:00Z",
  "updatedAt": "2024-10-15T00:00:00Z"
}
```

**¿Por qué MongoDB para productos?** El campo `images` es un array de longitud variable. En SQL necesitarías una tabla separada `product_images` con JOIN. En MongoDB es simplemente un array en el mismo documento, más simple y más rápido para leer.

#### Colección: `carts`

```json
{
  "_id": "cart-xyz",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Zapatillas Nike Air Max",
      "quantity": 2,
      "unitPrice": 180000,
      "subtotal": 360000
    },
    {
      "productId": "prod-002",
      "productName": "Camiseta Adidas",
      "quantity": 1,
      "unitPrice": 75000,
      "subtotal": 75000
    }
  ],
  "total": 435000,
  "updatedAt": "2024-10-15T10:45:00Z"
}
```

**¿Por qué MongoDB para el carrito?** El carrito es un dato temporal y de sesión. Su estructura varía según cuántos productos tiene el usuario. Guardarlo como un documento embebido (con los ítems dentro del mismo documento) evita múltiples JOINs y permite leerlo y actualizarlo en una sola operación.

---

## 8. Seguridad de la Aplicación

### Tipo de Seguridad Recomendada: **JWT + Spring Security + BCrypt**

#### 1. Autenticación con JWT (JSON Web Token)

**¿Qué es?** Un estándar abierto (RFC 7519) para transmitir información de forma segura entre partes como un objeto JSON firmado digitalmente.

**¿Cómo funciona en SportLife?**
1. El usuario hace login → el servidor genera un JWT firmado con una clave secreta.
2. El cliente almacena el token (en memoria o cookie HttpOnly).
3. En cada petición protegida, el cliente envía el token en el header: `Authorization: Bearer <token>`.
4. El servidor valida la firma del token. Si es válido, extrae el `userId` y permite el acceso.

**Ventajas:**
- **Sin estado (stateless)**: El servidor no necesita guardar sesiones en BD. El token contiene toda la información necesaria.
- **Escalable**: Funciona con múltiples instancias del servidor (no hay sesión compartida que sincronizar).
- **Seguro**: Si se firma con HS256 o RS256, nadie puede modificar el payload sin invalidar la firma.

#### 2. Cifrado de Contraseñas con BCrypt

**¿Qué es?** Un algoritmo de hashing adaptativo diseñado específicamente para contraseñas.

**¿Por qué no MD5 o SHA-256?** MD5 y SHA-256 son rápidos, lo que los hace vulnerables a ataques de fuerza bruta. BCrypt es lento por diseño (tiene un "work factor" configurable) y añade un **salt** automático, lo que hace que dos usuarios con la misma contraseña tengan hashes diferentes.

```java
// Al registrar
String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));

// Al hacer login
boolean isValid = BCrypt.checkpw(rawPassword, hashedPassword);
```

#### 3. Spring Security

Configura los endpoints protegidos vs públicos:
- **Públicos**: `POST /auth/register`, `POST /auth/login`, `GET /products`
- **Protegidos (requieren JWT)**: Todo lo relacionado con carrito y checkout.
- **Solo ADMIN**: Gestión de productos (crear, editar, desactivar).

---

## 9. Roles y Permisos

### Roles Identificados

Se identifican **2 roles** en el sistema:

| Rol         | Descripción                                              |
|-------------|----------------------------------------------------------|
| `CUSTOMER`  | Usuario final que navega, compra y gestiona su carrito   |
| `ADMIN`     | Administrador que gestiona el catálogo de productos      |

### Matriz de Permisos

| Funcionalidad                  | CUSTOMER | ADMIN |
|--------------------------------|----------|-------|
| Registro                       | ✅       | ✅    |
| Login                          | ✅       | ✅    |
| Listar productos               | ✅       | ✅    |
| Ver detalle de producto        | ✅       | ✅    |
| Agregar producto al carrito    | ✅       | ❌    |
| Ver resumen del carrito        | ✅       | ❌    |
| Modificar carrito              | ✅       | ❌    |
| Procesar pago                  | ✅       | ❌    |
| Crear producto (admin)         | ❌       | ✅    |
| Editar producto (admin)        | ❌       | ✅    |
| Desactivar producto (admin)    | ❌       | ✅    |
| Ver todas las órdenes (admin)  | ❌       | ✅    |

**¿Por qué el ADMIN no puede comprar?** Por separación de responsabilidades y para evitar conflictos de interés. El administrador gestiona el inventario; el cliente hace las compras. Si se requiere que un administrador también pueda comprar, se puede implementar un rol `ADMIN` que herede los permisos de `CUSTOMER`.

**Implementación con Spring Security:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/products")
public ResponseEntity<ProductResponseDTO> createProduct(...) { ... }

@PreAuthorize("hasRole('CUSTOMER')")
@PostMapping("/cart/items")
public ResponseEntity<CartResponseDTO> addToCart(...) { ... }
```

---

## 10. TLS/SSL en una API REST

### ¿Qué es TLS/SSL?

**SSL (Secure Sockets Layer)** y su sucesor **TLS (Transport Layer Security)** son protocolos criptográficos que cifran la comunicación entre el cliente y el servidor. Cuando una URL empieza con `https://`, se está usando TLS.

### ¿Cómo se implementa en Spring Boot?

**Paso 1: Obtener un certificado SSL**
- Opción gratuita: **Let's Encrypt** (certificado válido 90 días, renovable automáticamente).
- Opción de pago: Certificados de autoridades como DigiCert o Comodo.
- En desarrollo/pruebas: Certificado autofirmado (self-signed).

**Paso 2: Configurar en `application.properties`**
```properties
# Habilitar HTTPS
server.ssl.enabled=true
server.port=8443

# Ruta al keystore que contiene el certificado
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=miPasswordDelKeystore
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=sportlife
```

**Paso 3: Redirigir HTTP a HTTPS (opcional pero recomendado)**
```java
@Bean
public ServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
        @Override
        protected void postProcessContext(Context context) {
            SecurityConstraint constraint = new SecurityConstraint();
            constraint.setUserConstraint("CONFIDENTIAL"); // Fuerza HTTPS
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

### Ventajas de TLS/SSL para SportLife

| Ventaja                  | Explicación                                                                                     |
|--------------------------|-------------------------------------------------------------------------------------------------|
| **Confidencialidad**     | Los datos viajan cifrados. Si alguien intercepta el tráfico (ataque MITM), solo ve bytes cifrados. |
| **Integridad**           | Garantiza que los datos no fueron modificados en tránsito (ningún proxy puede alterar el body).  |
| **Autenticidad**         | El certificado prueba que el servidor es realmente SportLife.com y no un sitio malicioso.        |
| **Protege tokens JWT**   | Sin TLS, el token de autenticación viajaría en texto plano y podría ser robado.                  |
| **Protege datos de pago**| Los números de tarjeta y CVV van cifrados, cumpliendo PCI-DSS (estándar de seguridad de pagos). |
| **Confianza del usuario**| El candado verde en el navegador genera confianza en la plataforma de e-commerce.               |

---

## 11. CORS en una API REST

### ¿Qué es CORS?

**CORS (Cross-Origin Resource Sharing)** es un mecanismo de seguridad del navegador que **restringe las peticiones HTTP** que se hacen desde un dominio diferente al del servidor.

### ¿Por qué existe CORS?

El navegador implementa la **Política del Mismo Origen** (Same-Origin Policy): por defecto, un script en `https://miapp.com` no puede hacer peticiones a `https://api.sportlife.com` porque son **orígenes distintos** (dominio diferente).

Sin CORS configurado, cuando el frontend de SportLife (en `https://sportlife.com`) intente llamar a la API (en `https://api.sportlife.com`), el navegador bloqueará la petición con el error:

```
Access to XMLHttpRequest at 'https://api.sportlife.com/products' 
from origin 'https://sportlife.com' has been blocked by CORS policy.
```

### ¿Por qué es importante configurarlo correctamente?

- **Muy permisivo** (`allowedOrigins = "*"`): Cualquier sitio web del mundo puede hacer peticiones a tu API desde el navegador del usuario. Esto puede facilitar ataques CSRF.
- **Muy restrictivo** (sin configurar): Tu propio frontend no puede consumir la API.
- **Correcto**: Solo los orígenes autorizados pueden acceder.

### Implementación en Spring Boot

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // Solo permitir el frontend oficial de SportLife
            .allowedOrigins(
                "https://sportlife.com",
                "https://www.sportlife.com",
                "http://localhost:3000"  // Solo en desarrollo
            )
            // Métodos HTTP permitidos
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            // Headers permitidos (incluye el de autorización JWT)
            .allowedHeaders("Authorization", "Content-Type", "Accept")
            // Permite enviar cookies/credenciales
            .allowCredentials(true)
            // El navegador cachea la respuesta del preflight por 1 hora
            .maxAge(3600);
    }
}
```

**¿Por qué se incluye `OPTIONS`?** Antes de una petición con métodos no simples (POST con JSON, PATCH, DELETE), el navegador envía una petición **preflight** de tipo `OPTIONS` para preguntar si tiene permiso. El servidor debe responder `200 OK` con los headers de CORS para que el navegador proceda con la petición real.

**Resumen de por qué CORS es crítico para SportLife:**
- SportLife tiene un frontend separado de su API → orígenes distintos → CORS es obligatorio.
- Sin CORS, la aplicación web no puede funcionar en el navegador.
- CORS mal configurado es una vulnerabilidad de seguridad.

---

## 12. Pantallas (Figma)

Las pantallas del flujo de compra de SportLife diseñadas en Figma cubren:

1. **Pantalla de Registro** — Formulario con nombre, email y contraseña.
2. **Pantalla de Login** — Email y contraseña con enlace a registro.
3. **Catálogo de Productos** — Grid de productos con filtros por categoría y buscador por nombre. Muestra imagen, nombre, precio y botón "Agregar al carrito".
4. **Detalle de Producto** — Imágenes, descripción completa, precio, stock disponible, selector de cantidad y botón "Agregar al carrito".
5. **Carrito de Compras** — Lista de productos seleccionados con subtotales, opción de cambiar cantidad o eliminar, y total general.
6. **Checkout / Pago** — Resumen final del pedido, selección de método de pago, formulario de datos de tarjeta.
7. **Confirmación de Compra** — Mensaje de éxito con número de orden y transacción.
8. **Pago Rechazado** — Mensaje de error con opción de reintentar.

> 🔗 **Link de Figma**: [Insertar enlace al prototipo de Figma aquí]

---

## 13. Parte Práctica — Implementación

### Paso 1: Scaffolding del Proyecto (Estructura MVC con Maven)

```
ECI-SportLife/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/dosw/sportlife/
        │       ├── SportLifeApplication.java
        │       ├── config/
        │       │   ├── SecurityConfig.java
        │       │   ├── CorsConfig.java
        │       │   ├── SwaggerConfig.java
        │       │   └── DataSourceConfig.java
        │       ├── controller/
        │       │   ├── AuthController.java
        │       │   ├── ProductController.java
        │       │   ├── CartController.java
        │       │   └── OrderController.java
        │       ├── service/
        │       │   ├── AuthService.java
        │       │   ├── ProductService.java
        │       │   ├── CartService.java
        │       │   └── PaymentService.java
        │       ├── repository/
        │       │   ├── UserRepository.java       (JPA)
        │       │   ├── OrderRepository.java      (JPA)
        │       │   ├── ProductRepository.java    (MongoDB)
        │       │   └── CartRepository.java       (MongoDB)
        │       ├── model/
        │       │   ├── User.java
        │       │   ├── Product.java
        │       │   ├── Cart.java
        │       │   ├── CartItem.java
        │       │   ├── Order.java
        │       │   └── Payment.java
        │       ├── dto/
        │       │   ├── request/
        │       │   │   ├── RegisterRequestDTO.java
        │       │   │   ├── LoginRequestDTO.java
        │       │   │   ├── AddToCartRequestDTO.java
        │       │   │   └── CheckoutRequestDTO.java
        │       │   └── response/
        │       │       ├── UserResponseDTO.java
        │       │       ├── ProductResponseDTO.java
        │       │       ├── CartResponseDTO.java
        │       │       └── OrderResponseDTO.java
        │       ├── exception/
        │       │   ├── GlobalExceptionHandler.java
        │       │   ├── ResourceNotFoundException.java
        │       │   ├── InsufficientStockException.java
        │       │   └── EmailAlreadyExistsException.java
        │       └── util/
        │           └── JwtUtil.java
        └── resources/
            ├── application.properties
            ├── application-dev.properties
            └── application-prod.properties
```

---

### Paso 2: `pom.xml` con Dependencias

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.dosw</groupId>
    <artifactId>sportlife</artifactId>
    <version>1.0.0</version>
    <name>ECI-SportLife</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web (REST API) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Security (JWT) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
        </dependency>

        <!-- JPA (PostgreSQL) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- MongoDB -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>

        <!-- Lombok (reducción de boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Validación (Bean Validation) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Swagger / OpenAPI 3 -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- Testing: JUnit 5 + Mockito -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- Mockito ya incluido en spring-boot-starter-test -->
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!-- SonarQube -->
            <plugin>
                <groupId>org.sonarsource.scanner.maven</groupId>
                <artifactId>sonar-maven-plugin</artifactId>
                <version>3.10.0.2594</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### Paso 3: Configuración de Persistencia y Documentación

**`application.properties`** (configuración base):
```properties
spring.application.name=sportlife

# ======= PostgreSQL =======
spring.datasource.url=jdbc:postgresql://localhost:5432/sportlife_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ======= MongoDB =======
spring.data.mongodb.uri=mongodb://localhost:27017/sportlife_mongo
spring.data.mongodb.database=sportlife_mongo

# ======= JWT =======
jwt.secret=sportlife_secret_key_super_segura_2024
jwt.expiration=3600000

# ======= Swagger =======
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

---

### Paso 4: Ejemplo de Implementación — Registro de Usuario

**`RegisterRequestDTO.java`:**
```java
@Data
public class RegisterRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
```

**`AuthController.java`:**
```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticación")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado"),
        @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

**`GlobalExceptionHandler.java`:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponseDTO(400, message));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailExists(
            EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponseDTO(409, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDTO> handleStockError(
            InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponseDTO(422, ex.getMessage()));
    }
}
```

---

### Paso 5: Flujo de Git por Funcionalidad

```bash
# Crear rama feature desde develop
git checkout develop
git checkout -b feature/F01-registro-usuario

# Desarrollar la funcionalidad...

# Commit y push
git add .
git commit -m "feat(auth): implementar registro de usuario con validaciones"
git push origin feature/F01-registro-usuario

# Merge a develop (via Pull Request en GitHub)
# Una vez aprobado el PR:
git checkout develop
git merge feature/F01-registro-usuario
git push origin develop

# Eliminar la rama feature
git branch -d feature/F01-registro-usuario
git push origin --delete feature/F01-registro-usuario
```

---

### Paso 6: GitHub Actions — Pipeline CI/CD

**`.github/workflows/ci-cd.yml`:**

```yaml
name: SportLife CI/CD Pipeline

on:
  push:
    branches:
      - develop    # Ejecutar en cada merge a develop
      - master     # Ejecutar en cada merge a master

jobs:
  build-and-test:
    name: Build & Test
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: sportlife_test
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: test123
        ports:
          - 5432:5432

      mongodb:
        image: mongo:6
        ports:
          - 27017:27017

    steps:
      - name: Checkout código
        uses: actions/checkout@v4

      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cachear dependencias Maven
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Compilar el proyecto
        run: mvn clean compile -B

      - name: Ejecutar pruebas unitarias
        run: mvn test -B
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/sportlife_test
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: test123
          SPRING_DATA_MONGODB_URI: mongodb://localhost:27017/sportlife_test

      - name: Análisis con SonarQube
        run: mvn sonar:sonar -B
          -Dsonar.projectKey=ECI-SportLife
          -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }}
          -Dsonar.login=${{ secrets.SONAR_TOKEN }}

      - name: Generar artefacto JAR
        run: mvn package -DskipTests -B

      - name: Subir artefacto
        uses: actions/upload-artifact@v4
        with:
          name: sportlife-jar
          path: target/*.jar

  deploy:
    name: Deploy a Azure
    runs-on: ubuntu-latest
    needs: build-and-test
    if: github.ref == 'refs/heads/master'

    steps:
      - name: Descargar artefacto
        uses: actions/download-artifact@v4
        with:
          name: sportlife-jar

      - name: Login a Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}

      - name: Deploy a Azure App Service
        uses: azure/webapps-deploy@v2
        with:
          app-name: 'sportlife-api'
          package: '*.jar'
```

---

### Paso 7: Despliegue en Azure

1. Crear un **Azure App Service** (Plan B1 o superior para Java 17).
2. Configurar las variables de entorno en Azure (cadenas de conexión a PostgreSQL y MongoDB).
3. Conectar el repositorio de GitHub con Azure App Service para deployment automático desde `master`.
4. Configurar Azure Database for PostgreSQL y Azure Cosmos DB for MongoDB (o MongoDB Atlas).
5. El pipeline de GitHub Actions ejecuta el deploy automáticamente en cada push a `master`.

---

### Paso 8: Pruebas Funcionales con Swagger

La documentación interactiva estará disponible en:

```
https://<tu-app>.azurewebsites.net/swagger-ui.html
```

**Flujo de prueba sugerido en el video:**
1. `POST /auth/register` → Crear usuario.
2. `POST /auth/login` → Obtener JWT → Copiarlo en el botón "Authorize" de Swagger.
3. `GET /products` → Listar productos.
4. `GET /products/{id}` → Ver detalle.
5. `POST /cart/items` → Agregar al carrito.
6. `GET /cart` → Ver resumen.
7. `POST /checkout` → Procesar pago.
8. Verificar respuesta aprobada y rechazada.

---

## ⏱️ Tiempo de Desarrollo

| Sección                          | Tiempo estimado |
|----------------------------------|-----------------|
| Matriz de trazabilidad           | 30 min          |
| Especificación de endpoints      | 2 h             |
| Diagramas (componentes, clases)  | 1.5 h           |
| Diagramas de BD                  | 1 h             |
| Preguntas teóricas (7-10)        | 1 h             |
| Pantallas Figma                  | 1.5 h           |
| Implementación práctica          | 6-8 h           |
| Pipeline CI/CD y Azure           | 2 h             |
| **Total estimado**               | **~16 h**       |

---

*Documentación generada para el proyecto **ECI-SportLife** — PRE PARCIAL CORTE #2 — DOSW Company.*