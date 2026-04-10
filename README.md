# SportLife API (MVP)

API REST construida con Spring Boot para el MVP de SportLife, siguiendo MVC + Clean Architecture.

## Stack

- Java 17
- Spring Boot (Web, Validation, JPA, MongoDB)
- H2 (relacional para usuarios/productos/ordenes)
- MongoDB (carrito y pagos)
- Swagger/OpenAPI
- JUnit 5 + Mockito

## Estructura

- `controller`: endpoints REST
- `dtos/request` y `dtos/response`: contratos de entrada/salida
- `mappers`: conversiones dominio <-> DTO
- `handlers`: manejo centralizado de errores
- `core/services|models|validators|utils`: logica de negocio
- `persistence/entities|repositories|mappers|config`: acceso a datos y configuracion
- `src/main/resources/application.yaml`: configuracion
- `src/test/java/com/sportlife/{controller,core,persistence}`: pruebas

## Endpoints principales

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/products`
- `GET /api/v1/products/search?name=...`
- `GET /api/v1/products/category?category=...`
- `GET /api/v1/products/{id}`
- `POST /api/v1/cart/{userId}/items`
- `GET /api/v1/cart/{userId}`
- `PATCH /api/v1/cart/{userId}/items`
- `POST /api/v1/checkout`
- `POST /api/v1/checkout/payment`

## Swagger

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/api-docs`

## Ejecutar

```powershell
mvn clean spring-boot:run
```

## Probar

```powershell
mvn test
```
