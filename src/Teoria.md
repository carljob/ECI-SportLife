# 📘 Guía Completa del Proyecto SportLife

## 🔹 ¿Qué es un Endpoint?

Un **endpoint** es una URL de la API que permite interactuar con el sistema.

Ejemplo:

```
GET /products
```

Significa: obtener todos los productos.

### Tipos de endpoints (HTTP)

| Método | Uso                 |
| ------ | ------------------- |
| GET    | Obtener información |
| POST   | Crear datos         |
| PUT    | Actualizar          |
| DELETE | Eliminar            |

---

## 🔹 ¿Qué es un DTO?

DTO significa **Data Transfer Object**.

Es un objeto que se usa para enviar o recibir datos sin exponer la entidad real.

### ❌ Mal (exponer entidad):

```java
public Product {
    private Long id;
    private String name;
}
```

### ✅ Bien (DTO):

```java
public class ProductRequestDTO {
    private String name;
}
```

👉 Evita problemas de seguridad y desacopla el sistema.

---

## 🔹 Arquitectura del Proyecto (MVC)

### 1. Controller

* Recibe las peticiones HTTP
* Define endpoints

```java
@RestController
@RequestMapping("/products")
```

---

### 2. Service

* Contiene la lógica de negocio

```java
@Service
public class ProductService {
}
```

---

### 3. Repository

* Acceso a base de datos

```java
public interface ProductRepository extends JpaRepository<Product, Long>
```

---

## 🔹 Flujo del sistema

1. Usuario se registra
2. Consulta productos
3. Agrega al carrito
4. Realiza pago
5. Se genera orden

---

## 🔹 ¿Cómo identificar cada parte?

| Elemento    | Cómo reconocerlo                    |
| ----------- | ----------------------------------- |
| Endpoint    | Tiene `@GetMapping`, `@PostMapping` |
| DTO         | Está en carpeta `dto`               |
| Entidad     | Tiene `@Entity`                     |
| Servicio    | Tiene `@Service`                    |
| Repositorio | Extiende `JpaRepository`            |

---

## 🔹 Validaciones

Ejemplo:

```java
@NotNull
private String name;
```

Evita datos inválidos.

---

## 🔹 Códigos HTTP

| Código | Significado    |
| ------ | -------------- |
| 200    | OK             |
| 201    | Creado         |
| 400    | Error cliente  |
| 404    | No encontrado  |
| 500    | Error servidor |

---

## 🔹 Seguridad

Se recomienda:

* JWT (autenticación)
* TLS/SSL (HTTPS)
* CORS (control de acceso)

---

## 🔹 Pipeline (CI/CD)

Automatiza:

* Build
* Test
* Deploy

---

## 🔹 Conclusión

Este proyecto implementa un MVP de e-commerce usando buenas prácticas de ingeniería:

* Arquitectura en capas
* API REST
* Separación de responsabilidades
* Documentación clara

---
