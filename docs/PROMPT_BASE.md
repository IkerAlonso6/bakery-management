# PROMPT BASE — Sistema de Gestión de Pedidos Panadería

> Este archivo debe ser leído por la IA al inicio de cada sesión antes de realizar cualquier acción.
> Contiene las instrucciones permanentes de comportamiento, arquitectura y convenciones del proyecto.

---

## 1. Descripción del proyecto

Estás trabajando en el desarrollo de un sistema web de gestión de pedidos para una panadería.
El sistema reemplaza un flujo analógico de pedidos, boletas y resúmenes de cobro.

Antes de hacer cualquier cosa, leé los siguientes documentos en este orden:
1. `docs/requerimientos.md` — contexto de negocio, modelo de dominio y reglas de negocio
2. `docs/tickets.md` — planificación de tareas por sprint
3. `docs/decisiones/` — decisiones técnicas ya tomadas (leer todos los archivos de esta carpeta)
4. `docs/progreso/` — resumen de lo que ya fue implementado (leer todos los archivos de esta carpeta)

No avances hasta haber leído todo lo anterior. El contexto acumulado en `docs/progreso/` es especialmente importante para mantener consistencia con lo ya construido.

---

## 2. Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21 + Spring Boot |
| ORM | Hibernate / JPA |
| Base de datos | PostgreSQL |
| Frontend | HTML + CSS + JavaScript vanilla |
| Comunicación | API REST (JSON) |
| Contenedores | Docker + Docker Compose |
| Servidor web | nginx |

---

## 3. Arquitectura obligatoria

### Backend
La arquitectura de capas es estricta y no debe violarse bajo ninguna circunstancia:

```
Controller → Service (interfaz) → ServiceImpl → Repository → Domain
```

- **Controller:** solo recibe requests, delega al Service, devuelve responses. No contiene lógica de negocio.
- **Service (interfaz):** define el contrato de la capa de negocio.
- **ServiceImpl:** implementa la lógica de negocio. Nunca accede directamente al Repository de otra entidad — usa el Service correspondiente.
- **Repository:** extiende `JpaRepository`. Solo contiene queries, sin lógica de negocio.
- **Domain:** entidades JPA puras. Pueden tener métodos calculados simples (`calcularTotal()`, `subtotal()`) pero sin dependencias externas.
- **DTOs:** toda comunicación entre frontend y backend usa DTOs. Nunca exponer entidades de dominio directamente en la API.

### Estructura de paquetes backend
```
com.panaderia/
├── controller/
├── service/
│   └── impl/
├── repository/
├── domain/
│   ├── model/
│   └── enums/
├── dto/
│   ├── request/
│   └── response/
└── exception/
```

### Frontend
```
panaderia-frontend/
├── index.html
├── css/
│   ├── main.css
│   └── print.css
├── js/
│   ├── api/          ← módulos de llamadas al backend por entidad
│   └── pages/        ← lógica de cada pantalla
└── pages/
    ├── pedidos/
    ├── admin/
    └── ...
```

---

## 4. Convenciones de código

### Nombrado — seguir estrictamente los nombres definidos en requerimientos.md
- Entidades: `Cliente`, `Producto`, `PedidoDia`, `LineaPedido`, `PlantillaPedido`, `LineaPlantilla`, `TarifaCliente`, `Repartidor`, `Contacto`, `StockDia`, `ConfiguracionDia`
- Enums: `Unidad`, `TipoDia`, `TipoContacto`, `TipoFacturacion`
- No inventar nombres alternativos. Si un nombre del documento de requerimientos parece incorrecto, preguntar antes de cambiarlo.

### Java
- Usar `BigDecimal` para todos los campos de precio y cantidad. Nunca `double` ni `float`.
- Usar `LocalDate` para fechas. Nunca `Date` ni `Timestamp` salvo necesidad explícita.
- Usar Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) para reducir boilerplate.
- Validar todos los DTOs de entrada con Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`, `@PositiveOrZero`).
- Anotar los controllers con `@RestController` y `@RequestMapping("/api/...")`.
- Usar `ResponseEntity<?>` como tipo de retorno en controllers.

### Manejo de errores
- Toda excepción de recurso no encontrado lanza `RecursoNoEncontradoException` → HTTP 404.
- Toda violación de regla de negocio lanza `ReglaNegocioException` → HTTP 400.
- El `GlobalExceptionHandler` centraliza el manejo. No usar try/catch en controllers ni services salvo casos específicos justificados.

### JavaScript (frontend)
- Toda llamada al backend pasa por `js/api/client.js` — nunca usar `fetch()` directamente en el código de una página.
- Separar la lógica de cada pantalla en su propio archivo dentro de `js/pages/`.
- No usar frameworks ni librerías externas salvo lo definido en requerimientos (Chart.js para métricas).
- Usar `async/await` para manejo de promesas. No usar `.then()` encadenado.

---

## 5. Seguridad

**El sistema no implementa autenticación ni autorización en esta etapa.**
No agregar Spring Security, JWT, ni ningún mecanismo de seguridad.
Los endpoints son públicos. Esto es una decisión consciente que se revisará en una etapa posterior.

---

## 6. Base de datos

- Motor: PostgreSQL
- Hibernate DDL: `spring.jpa.hibernate.ddl-auto=update` en desarrollo
- Nombrado de tablas: snake_case automático por convención de Hibernate
- No crear scripts SQL manuales — dejar que Hibernate gestione el esquema
- Usar `spring.jpa.show-sql=true` en desarrollo para visibilidad de queries

---

## 7. Comportamiento esperado de la IA

### Antes de codear
1. Leer todos los documentos indicados en la sección 1.
2. Identificar el ticket a implementar en `docs/tickets.md`.
3. Verificar en `docs/progreso/` qué fue implementado previamente para mantener consistencia.
4. **Explicar brevemente cómo vas a resolver el ticket** — qué clases vas a crear, qué decisiones vas a tomar — antes de escribir una sola línea de código. Esperar confirmación antes de proceder.

### Durante la implementación
- Implementar exactamente lo definido en el ticket. No agregar funcionalidad extra no solicitada.
- Si encontrás una ambigüedad o algo no definido en los documentos, preguntar antes de asumir.
- Seguir estrictamente la arquitectura y convenciones definidas en este documento.
- No cambiar código de tickets anteriores salvo que sea estrictamente necesario y se justifique explícitamente.

### Al terminar cada ticket
Generar automáticamente un archivo `docs/progreso/TICKET_ID.md` con el siguiente formato:

```markdown
# [TICKET_ID] — [Nombre del ticket]

## Estado
Completado

## Fecha
[fecha de implementación]

## Qué se implementó
- Lista de clases/archivos creados o modificados con su ruta completa

## Decisiones tomadas
- Decisiones que no estaban explícitas en el documento de requerimientos

## Dependencias generadas
- Qué otros tickets o componentes dependen de lo implementado aquí

## Pendientes / deuda técnica
- Cualquier cosa que quedó incompleta o que debería revisarse
```

---

## 8. Lo que la IA NO debe hacer

- ❌ Exponer entidades de dominio directamente como respuesta de la API
- ❌ Poner lógica de negocio en controllers o repositories
- ❌ Usar `double` o `float` para precios o cantidades
- ❌ Agregar Spring Security o cualquier mecanismo de autenticación
- ❌ Renombrar entidades, atributos o endpoints sin consultar primero
- ❌ Implementar funcionalidad no definida en el ticket actual
- ❌ Usar `.then()` encadenado en JavaScript
- ❌ Llamar a `fetch()` directamente desde el código de una página
- ❌ Acceder a un Repository desde otro Service directamente
- ❌ Modificar código de tickets anteriores sin justificación explícita

---

## 9. Control de versiones

- Repositorio: https://github.com/IkerAlonso6/bakery-management
- Rama activa: sprint-2
- Al terminar cada ticket: hacer commit con el mensaje `TICKET_ID - Nombre del ticket` y push a origin.