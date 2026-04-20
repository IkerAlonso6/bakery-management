# Tickets de Desarrollo — Sistema de Panadería

## Metodología

- **Sprints de 1 semana**
- Cada sprint entrega funcionalidad completa y testeable
- El backend precede al frontend en cada módulo para tener la API disponible
- Las dependencias entre tickets están indicadas explícitamente

---

## BACKEND

---

### Sprint 1 — Fundación del proyecto

**BE-01 — Setup inicial del proyecto**
- Crear proyecto Spring Boot con dependencias: Web, JPA, PostgreSQL Driver, Validation, Lombok
- Configurar `application.properties`: conexión a PostgreSQL, Hibernate DDL auto, puerto
- Configurar CORS para desarrollo local
- Crear estructura de paquetes: `controller`, `service`, `service/impl`, `repository`, `domain/model`, `domain/enums`, `dto/request`, `dto/response`, `exception`
- Verificar que la aplicación levanta correctamente
- _Dependencias: ninguna_

**BE-02 — Enumeraciones del dominio**
- Crear enum `Unidad` → KG, LATA, LITRO
- Crear enum `TipoDia` → HABIL, FIN_DE_SEMANA, FERIADO
- Crear enum `TipoContacto` → WHATSAPP, TELEFONO, EMAIL
- Crear enum `TipoFacturacion` → AL_RECIBIR, SEMANAL
- _Dependencias: BE-01_

**BE-03 — Entidades del dominio**
- Crear entidad `Producto`: id, descripcion, unidad (Unidad), activo
- Crear entidad `Repartidor`: id, nombre
- Crear entidad `Contacto`: id, valor, tipoContacto, relación con Cliente o Repartidor
- Crear entidad `Cliente`: id, nombre, direccion, tipoFacturacion, ordenRuta, lista de contactos
- Crear entidad `TarifaCliente`: id, cliente, producto, precio (BigDecimal)
- Crear entidad `PlantillaPedido`: id, cliente, tipoDia, lista de LineaPlantilla
- Crear entidad `LineaPlantilla`: id, plantilla, producto, cantidad (BigDecimal)
- Crear entidad `PedidoDia`: id, fecha (LocalDate), cliente, repartidor, plantillaOrigen (nullable), lista de LineaPedido
- Crear entidad `LineaPedido`: id, pedido, producto, cantidad (BigDecimal), precioUnitario (BigDecimal nullable)
- Anotar todas las entidades con JPA (`@Entity`, `@Table`, `@ManyToOne`, `@OneToMany`, etc.)
- Verificar que Hibernate genera el esquema correctamente
- _Dependencias: BE-02_

**BE-04 — Manejo global de excepciones**
- Crear excepción `RecursoNoEncontradoException`
- Crear excepción `ReglaNegocioException`
- Crear `GlobalExceptionHandler` con `@ControllerAdvice`
- Mapear excepciones a respuestas HTTP con mensaje descriptivo (404, 400, 500)
- _Dependencias: BE-01_

---

### Sprint 2 — ABM de entidades base

**BE-05 — Módulo Productos**
- Crear `ProductoRepository` extendiendo `JpaRepository`
- Crear `ProductoService` e implementación: listar activos, buscar por id, crear, actualizar, desactivar
- Crear `ProductoController`: GET /api/productos, POST /api/productos, PUT /api/productos/{id}, PATCH /api/productos/{id}/desactivar
- Crear `ProductoRequest`: descripcion, unidad — con validaciones `@NotBlank`, `@NotNull`
- Crear `ProductoResponse`: id, descripcion, unidad, activo
- _Dependencias: BE-03, BE-04_

**BE-06 — Módulo Repartidores**
- Crear `RepartidorRepository`
- Crear `RepartidorService` e implementación: listar, buscar por id, crear, actualizar
- Crear `RepartidorController`: GET /api/repartidores, POST /api/repartidores, PUT /api/repartidores/{id}
- Crear `RepartidorRequest`: nombre — con validaciones
- Crear `RepartidorResponse`: id, nombre, contactos[]
- _Dependencias: BE-03, BE-04_

**BE-07 — Módulo Clientes**
- Crear `ClienteRepository`
- Crear `ClienteService` e implementación: listar ordenado por ordenRuta, buscar por id, crear, actualizar, eliminar
- Crear `ClienteController`: GET /api/clientes, GET /api/clientes/{id}, POST /api/clientes, PUT /api/clientes/{id}, DELETE /api/clientes/{id}, PATCH /api/clientes/{id}/orden-ruta
- Crear `ClienteRequest`: nombre, direccion, tipoFacturacion, ordenRuta — con validaciones
- Crear `ClienteResponse`: id, nombre, direccion, tipoFacturacion, ordenRuta, contactos[]
- _Dependencias: BE-03, BE-04_

**BE-08 — Módulo Tarifas**
- Crear `TarifaClienteRepository`
- Crear `TarifaClienteService` e implementación: listar por cliente, crear, actualizar
- Crear `TarifaClienteController`: GET /api/clientes/{id}/tarifas, POST /api/clientes/{id}/tarifas, PUT /api/tarifas/{id}
- Crear `TarifaRequest`: productoId, precio — con validaciones `@NotNull`, `@Positive`
- Crear `TarifaResponse`: id, productoDescripcion, precio
- _Dependencias: BE-05, BE-07_

---

### Sprint 3 — Plantillas y lógica de pedidos

**BE-09 — Módulo Plantillas**
- Crear `PlantillaRepository` y `LineaPlantillaRepository`
- Crear `PlantillaService` e implementación:
  - listar plantillas por cliente
  - buscar plantilla por cliente y tipoDia
  - crear plantilla con sus líneas
  - actualizar líneas de una plantilla
  - eliminar plantilla
- Crear `PlantillaController`: GET /api/clientes/{id}/plantillas, POST /api/clientes/{id}/plantillas, PUT /api/plantillas/{id}, DELETE /api/plantillas/{id}
- Crear `PlantillaRequest`: tipoDia, lineas[] (productoId + cantidad)
- Crear `PlantillaResponse`: id, tipoDia, lineas[]
- _Dependencias: BE-05, BE-07_

**BE-10 — Módulo Stock**
- Crear entidad `StockDia`: id, fecha, producto, cantidadDisponible
- Crear `StockRepository`
- Crear `StockService` e implementación: registrar stock del día, listar por fecha, calcular alertas (productos con demanda > stock)
- Crear `StockController`: GET /api/stock?fecha=, POST /api/stock, GET /api/stock/alertas?fecha=
- Crear `StockRequest`: productoId, cantidadDisponible, fecha
- Crear `StockResponse`: productoId, productoDescripcion, unidad, cantidadDisponible, demandaTotal, tieneAlerta
- _Dependencias: BE-05_

**BE-11 — Módulo Configuración del día**
- Crear entidad `ConfiguracionDia`: id, fecha, tipoDia (override manual)
- Crear `ConfiguracionDiaService`: obtener tipoDia efectivo para una fecha (primero busca override, luego calcula según día de semana), marcar como feriado, generar pedidos automáticos desde plantillas
- Crear `ConfiguracionDiaController`: GET /api/configuracion-dia?fecha=, POST /api/configuracion-dia/feriado?fecha=, POST /api/pedidos/generar?fecha=
- Lógica de generación automática: para cada cliente con plantilla cuyo tipoDia coincide con el día, crear un PedidoDia copiando las líneas de la plantilla y asignando precioUnitario desde TarifaCliente si existe
- _Dependencias: BE-09, BE-08_

---

### Sprint 4 — Pedidos del día y boletas

**BE-12 — Módulo Pedidos**
- Crear `PedidoDiaRepository` y `LineaPedidoRepository`
- Crear `PedidoDiaService` e implementación:
  - listar pedidos por fecha ordenados por ordenRuta del cliente
  - buscar pedido por id con detalle completo
  - crear pedido manual
  - modificar líneas de un pedido (con flag esPermanente: si true, actualizar también la plantilla)
  - confirmar entrega (cambiar estado a entregado)
- Crear `PedidoDiaController`: GET /api/pedidos?fecha=, POST /api/pedidos, PUT /api/pedidos/{id}, PATCH /api/pedidos/{id}/confirmar
- Crear `PedidoRequest`: clienteId, repartidorId, fecha, lineas[]
- Crear `ModificacionPedidoRequest`: lineas[], esPermanente
- Crear `PedidoResumenResponse`: id, fecha, clienteNombre, ordenRuta, repartidorNombre, resumenProductos, estado, total
- Crear `PedidoDetalleResponse`: id, fecha, cliente, repartidor, lineas[], total, tienePrecios, estado
- Crear `LineaResponse`: productoDescripcion, unidad, cantidad, precioUnitario, subtotal
- Implementar `calcularTotal()` y `tienePrecios()` en la capa de servicio o como métodos del dominio
- _Dependencias: BE-09, BE-08, BE-11_

**BE-13 — Boleta**
- Crear `BoletaService`: armar la respuesta de boleta según tipoFacturacion del cliente (con precios si AL_RECIBIR, sin precios si SEMANAL)
- Agregar endpoint GET /api/pedidos/{id}/boleta
- Crear `BoletaResponse`: fecha, clienteNombre, clienteDireccion, repartidorNombre, lineas[], total (null si SEMANAL), tipoFacturacion
- _Dependencias: BE-12_

---

### Sprint 5 — Resúmenes y métricas

**BE-14 — Resúmenes de cobro**
- Crear `ResumenService`: dado un clienteId y rango de fechas, retornar todos los PedidoDia del período con sus líneas y calcular el total del período
- Agregar endpoint GET /api/resumen/{clienteId}?desde=&hasta=
- Crear `ResumenCobroResponse`: clienteNombre, desde, hasta, pedidos[], totalPeriodo
- _Dependencias: BE-12_

**BE-15 — Métricas**
- Crear `MetricasService` con queries agregadas (usando JPQL o queries nativas):
  - `productosmasVendidos(desde, hasta)`: suma de cantidad por producto en el período, ordenado desc
  - `diasMasFuertes(desde, hasta)`: suma de pedidos agrupados por día de semana
  - `totalesPorCliente(desde, hasta)`: suma de totales por cliente en el período
  - `mercaderiaPorCliente(desde, hasta)`: cantidad total por cliente y por producto
- Crear `MetricasController` con los cuatro endpoints GET /api/metricas/...
- Crear DTOs de respuesta correspondientes
- _Dependencias: BE-12_

---

## FRONTEND

---

### Sprint 3 — Setup y pantallas compartidas (en paralelo con BE Sprint 3)

**FE-01 — Setup del proyecto frontend**
- Crear estructura de carpetas: pages/, css/, js/api/, js/pages/
- Crear `main.css` con variables de color, tipografía base, clases de utilidad responsive
- Crear `print.css` para la vista de boleta imprimible
- Definir breakpoint principal en 768px
- Crear módulo `js/api/client.js` con función base `fetchAPI(method, endpoint, body)` que centraliza las llamadas al backend con manejo de errores
- Crear sistema de navegación: sidebar para desktop, bottom nav bar para mobile
- _Dependencias: BE-01_

**FE-02 — Pantalla Login**
- Crear `pages/login.html`
- Formulario con campos usuario y contraseña
- Al autenticar, redirigir según rol: repartidor → home del día, administración → dashboard
- Manejo de error de credenciales incorrectas
- _Dependencias: FE-01_

**FE-03 — Home del día / Lista de pedidos pendientes**
- Crear `pages/pedidos/home.html`
- Llamar GET /api/pedidos?fecha=hoy al cargar
- Renderizar tarjetas ordenadas por ordenRuta
- Cada tarjeta: nombre del cliente, resumen de productos, estado (badge color: amarillo pendiente / verde entregado)
- Mobile: tarjetas apiladas, botón grande "Ver detalle"
- Desktop: tabla con columnas cliente, productos, repartidor, estado, acciones
- Botón "Generar pedidos del día" visible solo para administración
- _Dependencias: FE-01, BE-12_

**FE-04 — Detalle de pedido**
- Crear `pages/pedidos/detalle.html`
- Llamar GET /api/pedidos/{id} al cargar
- Mostrar líneas completas: producto, cantidad, unidad
- Si tienePrecios: mostrar precioUnitario y subtotal por línea, total al pie
- Si no tienePrecios: mostrar solo cantidades
- Botón "Modificar pedido" → abre formulario de edición con checkbox "¿Es permanente?"
- Botón "Confirmar entrega" → llama PATCH /api/pedidos/{id}/confirmar
- Botón "Ver boleta" → navega a vista de boleta
- _Dependencias: FE-01, BE-12, BE-13_

**FE-05 — Boleta**
- Crear `pages/pedidos/boleta.html`
- Llamar GET /api/pedidos/{id}/boleta al cargar
- Mostrar datos del pedido formateados para impresión
- Si AL_RECIBIR: tabla con precios y total
- Si SEMANAL: tabla solo con cantidades
- Botón "Imprimir" que dispara `window.print()`
- La navegación se oculta con `@media print`
- _Dependencias: FE-04, BE-13_

**FE-06 — Nuevo pedido manual**
- Crear `pages/pedidos/nuevo.html`
- Selector de cliente (cargado desde GET /api/clientes, ordenado por ordenRuta)
- Selector de repartidor (cargado desde GET /api/repartidores)
- Sección para agregar líneas: selector de producto + campo cantidad, la unidad se muestra automáticamente
- Botón "Agregar línea" y botón "Eliminar" por línea
- Botón "Guardar pedido" → POST /api/pedidos
- Mobile: pasos secuenciales (cliente → repartidor → líneas → confirmar)
- Desktop: formulario completo en una vista
- _Dependencias: FE-01, BE-05, BE-06, BE-12_

**FE-07 — Pedidos entregados del día**
- Crear `pages/pedidos/entregados.html`
- Llamar GET /api/pedidos?fecha=hoy y filtrar por estado entregado
- Misma estructura visual que home del día pero con badge verde
- Acceso a detalle y boleta por pedido
- _Dependencias: FE-03, BE-12_

---

### Sprint 4 — Pantallas de administración (en paralelo con BE Sprint 4)

**FE-08 — Configuración del día**
- Crear `pages/admin/configuracion.html`
- Mostrar fecha actual y tipoDia efectivo
- Botón "Marcar como feriado" → POST /api/configuracion-dia/feriado
- Sección de stock: tabla de productos con campo cantidad disponible, botón guardar → POST /api/stock
- Sección de alertas: lista de productos con stock insuficiente (GET /api/stock/alertas)
- _Dependencias: FE-01, BE-10, BE-11_

**FE-09 — ABM Productos**
- Crear `pages/admin/productos.html`
- Tabla de productos con columnas: descripción, unidad, estado activo/inactivo
- Botón "Nuevo producto" → abre formulario modal o sección inline
- Botón "Editar" por fila → carga datos en formulario
- Botón "Desactivar" por fila → PATCH /api/productos/{id}/desactivar con confirmación
- _Dependencias: FE-01, BE-05_

**FE-10 — ABM Repartidores**
- Crear `pages/admin/repartidores.html`
- Tabla de repartidores con nombre y contactos
- Formulario de alta y edición
- _Dependencias: FE-01, BE-06_

**FE-11 — ABM Clientes**
- Crear `pages/admin/clientes.html`
- Tabla de clientes ordenada por ordenRuta con columnas: orden, nombre, dirección, tipoFacturacion
- Formulario de alta/edición con todos los campos incluyendo ordenRuta
- Sección de contactos: agregar/eliminar contactos por cliente
- Sección de plantillas: ver, crear y editar plantillas (tipoDia + líneas de productos)
- Sección de tarifas: tabla producto/precio editable por cliente
- _Dependencias: FE-01, BE-07, BE-08, BE-09_

---

### Sprint 5 — Resúmenes y métricas (en paralelo con BE Sprint 5)

**FE-12 — Historial de pedidos**
- Crear `pages/admin/historial.html`
- Filtros: cliente (select), repartidor (select), fecha desde/hasta, estado
- Tabla de resultados paginada con acceso a detalle
- _Dependencias: FE-01, BE-12_

**FE-13 — Resúmenes de cobro**
- Crear `pages/admin/resumen.html`
- Selector de cliente y rango de fechas (con atajos: esta semana, este mes)
- Tabla de pedidos del período: fecha, productos, total
- Total del período destacado al pie
- Botón imprimir con `@media print`
- _Dependencias: FE-01, BE-14_

**FE-14 — Dashboard de métricas**
- Crear `pages/admin/dashboard.html`
- Filtro de período compartido: última semana / último mes / rango personalizado
- Sección productos más vendidos: ranking con barras proporcionales
- Sección días más fuertes: gráfico de barras por día de semana (usando Chart.js o canvas nativo)
- Sección totales por cliente: tabla ordenable por total
- Sección mercadería por cliente: tabla cruzada cliente/producto con cantidades
- _Dependencias: FE-01, BE-15_

---

## DESPLIEGUE

---

### Sprint 6 — Infraestructura y despliegue

**DE-01 — Containerización del backend**
- Crear `Dockerfile` para el backend Spring Boot
- Imagen base: `eclipse-temurin:21-jre`
- Build en dos etapas: compilar con Maven, copiar JAR a imagen final
- Exponer puerto 8080
- _Dependencias: BE-15 (backend completo)_

**DE-02 — Containerización de la base de datos**
- Crear configuración de PostgreSQL en Docker
- Definir volumen persistente para los datos
- Crear script SQL de inicialización si es necesario
- _Dependencias: DE-01_

**DE-03 — Docker Compose**
- Crear `docker-compose.yml` con tres servicios: backend, postgres, frontend (nginx)
- Definir red interna entre servicios
- Variables de entorno para credenciales de base de datos
- Configurar healthcheck para postgres antes de levantar el backend
- _Dependencias: DE-01, DE-02_

**DE-04 — Containerización del frontend**
- Crear configuración de nginx para servir los archivos estáticos del frontend
- Crear `nginx.conf`: servir archivos en `/`, proxy hacia el backend en `/api/`
- Crear `Dockerfile` para el frontend
- _Dependencias: FE-14 (frontend completo), DE-03_

**DE-05 — Variables de entorno y configuración por ambiente**
- Separar `application.properties` en perfiles: `dev` y `prod`
- En prod: deshabilitar DDL auto de Hibernate, usar credenciales desde variables de entorno
- Documentar todas las variables de entorno necesarias en un `README.md`
- _Dependencias: DE-03_

**DE-06 — README y documentación de despliegue**
- Crear `README.md` en la raíz del proyecto con:
  - Descripción del sistema
  - Requisitos: Docker, Docker Compose
  - Instrucciones para levantar el proyecto en desarrollo y producción
  - Listado de variables de entorno
  - Descripción de los endpoints principales
- _Dependencias: DE-05_

---

## Resumen de sprints

| Sprint | Semana | Contenido |
|--------|--------|-----------|
| Sprint 1 | 1 | Fundación backend: setup, entidades, enums, excepciones |
| Sprint 2 | 2 | ABM backend: productos, repartidores, clientes, tarifas |
| Sprint 3 | 3 | Backend: plantillas, stock, configuración del día + Frontend: setup, login, pedidos |
| Sprint 4 | 4 | Backend: pedidos completos, boletas + Frontend: pantallas de administración base |
| Sprint 5 | 5 | Backend: resúmenes y métricas + Frontend: historial, resúmenes, dashboard |
| Sprint 6 | 6 | Despliegue: Docker, Docker Compose, nginx, documentación |
