# Sistema de Gestión de Pedidos — Panadería

## Documento de Requerimientos y Diseño

---

## 1. Contexto del negocio

El sistema reemplaza un flujo completamente analógico en una panadería. El proceso actual consiste en:

1. Los clientes envían pedidos por WhatsApp (generalmente la noche anterior)
2. El repartidor verifica stock manualmente al llegar y prepara los pedidos
3. Se generan boletas en papel por cliente
4. Otro sector lee boleta por boleta y genera resúmenes de cobro semanales o mensuales

El objetivo es digitalizar y automatizar este flujo.

---

## 2. Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java + Spring Boot |
| ORM | Hibernate / JPA |
| Base de datos | PostgreSQL |
| Frontend | HTML + CSS + JavaScript vanilla |
| Comunicación | API REST (JSON) |

**Arquitectura:** capas separadas — Controller → Service → Repository → Domain. DTOs para comunicación entre frontend y backend. No exponer entidades de dominio directamente en la API.

---

## 3. Roles de usuario

| Rol | Responsabilidad | Dispositivo principal |
|-----|----------------|----------------------|
| Repartidor | Carga y gestiona pedidos del día, verifica stock, confirma entregas | Celular |
| Administración | ABM general, resúmenes de cobro, métricas, carga de pedidos | PC desktop |

Ambos roles pueden estar activos simultáneamente (sistema multiusuario web). La carga de pedidos es accesible desde ambos roles y dispositivos, adaptando la experiencia visual según el dispositivo.

---

## 4. Modelo de dominio

### 4.1 Enumeraciones

```
Unidad          → KG | LATA | LITRO
TipoDia         → HABIL | FIN_DE_SEMANA | FERIADO
TipoContacto    → WHATSAPP | TELEFONO | EMAIL
TipoFacturacion → AL_RECIBIR | SEMANAL
```

### 4.2 Entidades

#### Cliente
```
id                : Long (PK)
nombre            : String
direccion         : String
tipoFacturacion   : TipoFacturacion
ordenRuta         : Integer           // posición en la ruta de reparto diaria
contactos         : List<Contacto>
plantillas        : List<PlantillaPedido>  // opcional, puede ser vacía
tarifas           : List<TarifaCliente>    // opcional, puede ser vacía
```
> `ordenRuta` determina el orden en que aparecen los pedidos en la vista del repartidor. La ruta es mayormente fija pero puede reordenarse desde administración.

#### Contacto
```
id            : Long (PK)
valor         : String
tipoContacto  : TipoContacto
cliente       : Cliente (FK)
```
> Un cliente puede tener múltiples contactos de distintos tipos.

#### Repartidor
```
id        : Long (PK)
nombre    : String
contactos : List<Contacto>
```

#### Producto
```
id          : Long (PK)
descripcion : String
unidad      : Unidad
activo      : boolean
```
> La unidad de medida le pertenece al producto y no varía. Productos similares con distinta unidad se modelan como productos distintos (ej: "Criollo cocido" en KG y "Criollo crudo" en LATA son dos productos separados).

#### TarifaCliente
```
id       : Long (PK)
cliente  : Cliente (FK)
producto : Producto (FK)
precio   : BigDecimal
```
> Representa el precio acordado entre un cliente y un producto. Es opcional — no todos los clientes tienen tarifas definidas (clientes con facturación SEMANAL pueden no tenerla).

#### PlantillaPedido
```
id      : Long (PK)
cliente : Cliente (FK)
tipoDia : TipoDia
lineas  : List<LineaPlantilla>
```
> Representa el pedido recurrente de un cliente. Un cliente puede tener hasta dos plantillas: una para HABIL y otra para FIN_DE_SEMANA/FERIADO. Es completamente opcional — clientes sin plantilla cargan su pedido manualmente cada día.

#### LineaPlantilla
```
id        : Long (PK)
plantilla : PlantillaPedido (FK)
producto  : Producto (FK)
cantidad  : BigDecimal
```

#### PedidoDia
```
id               : Long (PK)
fecha            : LocalDate
cliente          : Cliente (FK)
repartidor       : Repartidor (FK)
plantillaOrigen  : PlantillaPedido (FK, opcional — null si es manual)
lineas           : List<LineaPedido>
```
> Métodos calculados (no persistidos):
> - `calcularTotal()` → suma subtotales de líneas
> - `tienePrecios()` → true si todas las líneas tienen precioUnitario

#### LineaPedido
```
id              : Long (PK)
pedido          : PedidoDia (FK)
producto        : Producto (FK)
cantidad        : BigDecimal
precioUnitario  : BigDecimal (opcional — null si cliente es SEMANAL sin tarifa)
```
> `precioUnitario` se copia desde `TarifaCliente` al momento de confirmar el pedido, no se recalcula. Esto preserva el historial aunque los precios cambien.
> - Método calculado: `subtotal()` → cantidad × precioUnitario (null si no tiene precio)

---

## 5. Reglas de negocio

1. **Generación automática de pedidos:** Al inicio del día, el sistema genera automáticamente un `PedidoDia` por cada cliente que tiene una `PlantillaPedido` cuyo `TipoDia` coincide con el día actual. Los clientes sin plantilla no generan pedido automático.

2. **Modificación de pedido del día:** El repartidor puede modificar las líneas de un `PedidoDia` generado automáticamente. Al modificar, el sistema pregunta si el cambio es permanente (actualiza la plantilla) o temporal (solo afecta el pedido del día).

3. **Pedidos manuales:** Clientes sin plantilla generan su `PedidoDia` manualmente, cargando cliente, repartidor y líneas. Accesible tanto desde celular como desde PC.

4. **Precios:** Al confirmar un `PedidoDia`, si el cliente tiene `TarifaCliente` para cada producto, el sistema copia el precio vigente en `LineaPedido.precioUnitario`. Si no existe tarifa, el campo queda null.

5. **Total:** El total de un pedido es calculado, no persistido. Se obtiene sumando los subtotales de las líneas que tienen precio.

6. **Stock:** El repartidor ingresa manualmente el stock disponible del día. El sistema avisa si algún pedido supera el stock disponible de un producto, pero no bloquea la operación.

7. **Tipo de día:** El sistema determina automáticamente el `TipoDia` de la fecha actual para seleccionar la plantilla correcta. FERIADO debe poder marcarse manualmente ya que el sistema no conoce el calendario de feriados.

8. **Boleta:** Cada `PedidoDia` puede generar una boleta con el detalle de líneas. Si el cliente es `AL_RECIBIR`, la boleta incluye precios y total. Si es `SEMANAL`, la boleta muestra solo cantidades sin precios.

9. **Resumen de cobro:** El sistema genera un resumen por cliente para un período (semana o mes) sumando todos sus `PedidoDia` del período. Solo disponible para el rol de Administración.

10. **Orden de ruta:** Los pedidos del día se muestran ordenados por `Cliente.ordenRuta`. El orden es mayormente fijo pero puede modificarse desde administración.

---

## 6. Diseño de frontend

### 6.1 Estrategia responsive

El sistema debe ser completamente responsive con dos experiencias diferenciadas:

| Dispositivo | Experiencia | Prioridad |
|-------------|-------------|-----------|
| Celular | Acciones rápidas, botones grandes, mínima escritura | Repartidor en ruta |
| Desktop | Densidad de información, tablas, filtros, análisis | Administración |

Breakpoint principal: `768px`. Por debajo → experiencia mobile. Por encima → experiencia desktop.

### 6.2 Mapa de pantallas

#### Pantallas compartidas (responsive — ambos roles)

**1. Login**
- Campo usuario y contraseña
- El sistema redirige según rol al autenticarse

**2. Home del día / Lista de pedidos pendientes**
- Pedidos ordenados por `ordenRuta`
- Cada tarjeta muestra: nombre del cliente, resumen de productos (ej: "Pan 5kg · Criollos 2 latas"), estado (pendiente / entregado)
- Mobile: tarjetas apiladas verticales, botón grande "Confirmar entrega"
- Desktop: tabla con más columnas visibles y acciones inline

**3. Detalle de pedido**
- Líneas completas con producto, cantidad y unidad
- Si el cliente es AL_RECIBIR: muestra precio unitario y subtotal por línea, total al pie
- Si el cliente es SEMANAL: muestra solo cantidades, sin precios
- Opción de modificar líneas antes de confirmar entrega
- Al modificar: modal que pregunta si el cambio es permanente o solo para hoy
- Botón para generar boleta (vista imprimible)

**4. Nuevo pedido manual**
- Selector de cliente (ordenado por ordenRuta)
- Selector de repartidor
- Agregar líneas: selector de producto + campo cantidad
- La unidad se muestra automáticamente según el producto seleccionado
- Mobile: formulario de una columna, pasos secuenciales
- Desktop: formulario completo en una sola vista

**5. Pedidos entregados del día**
- Lista de pedidos ya confirmados del día actual
- Opción de ver detalle o generar boleta

#### Pantallas exclusivas de Administración (desktop)

**6. Dashboard de métricas**
- Productos más vendidos: ranking con cantidades totales por período
- Días más fuertes de la semana: gráfico de barras por día
- Totales por cliente en el período: tabla ordenable
- Totales de mercadería por cliente y por producto: tabla cruzada
- Filtro de período: última semana / último mes / rango personalizado

**7. Resúmenes de cobro**
- Selector de cliente y período (semana / mes / rango)
- Lista de pedidos del período con fecha, detalle y subtotal
- Total del período al pie
- Opción de exportar o imprimir

**8. Historial de pedidos**
- Tabla con filtros: cliente, repartidor, fecha desde/hasta, estado
- Acceso al detalle de cualquier pedido histórico

**9. ABM — Clientes**
- Lista de clientes con ordenRuta, nombre, tipoFacturacion
- Formulario de alta/edición: nombre, dirección, tipoFacturacion, ordenRuta, contactos
- Gestión de plantillas de pedido por cliente (tipoDia + líneas)
- Gestión de tarifas por cliente (producto + precio)

**10. ABM — Productos**
- Lista con descripción, unidad, estado activo/inactivo
- Formulario de alta/edición
- Baja lógica (campo `activo`) — no se borran productos con historial

**11. ABM — Repartidores**
- Lista y formulario simple: nombre y contactos

**12. Configuración del día**
- Marcar el día actual como FERIADO (override manual del TipoDia)
- Registrar stock disponible por producto
- Ver alertas de stock insuficiente vs pedidos del día
- Botón para generar todos los pedidos automáticos del día

### 6.3 Navegación

**Mobile (repartidor):**
```
Bottom navigation bar:
[ Pedidos del día ]  [ Nuevo pedido ]  [ Entregados ]
```

**Desktop (administración):**
```
Sidebar izquierdo:
- Home / Pedidos del día
- Nuevo pedido
- Historial
- Resúmenes
- Dashboard
- Configuración del día
- ABM (Clientes / Productos / Repartidores)
```

### 6.4 Criterios de diseño

- Tipografía clara y legible en mobile (mínimo 16px en campos)
- Colores de estado consistentes: pendiente (amarillo), entregado (verde), alerta de stock (rojo)
- Sin modales complejos en mobile — usar páginas separadas para acciones importantes
- Confirmaciones de acciones destructivas siempre con doble paso
- La boleta debe ser imprimible — usar `@media print` para ocultar navegación y mostrar solo el contenido del pedido

---

## 7. Endpoints API REST

### Clientes
```
GET    /api/clientes
GET    /api/clientes/{id}
POST   /api/clientes
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}
PATCH  /api/clientes/{id}/orden-ruta
```

### Productos
```
GET    /api/productos
POST   /api/productos
PUT    /api/productos/{id}
PATCH  /api/productos/{id}/desactivar
```

### Plantillas
```
GET    /api/clientes/{id}/plantillas
POST   /api/clientes/{id}/plantillas
PUT    /api/plantillas/{id}
DELETE /api/plantillas/{id}
```

### Tarifas
```
GET    /api/clientes/{id}/tarifas
POST   /api/clientes/{id}/tarifas
PUT    /api/tarifas/{id}
```

### Pedidos del día
```
GET    /api/pedidos?fecha=YYYY-MM-DD
POST   /api/pedidos/generar?fecha=YYYY-MM-DD
POST   /api/pedidos
PUT    /api/pedidos/{id}
PATCH  /api/pedidos/{id}/confirmar
GET    /api/pedidos/{id}/boleta
```

### Stock
```
GET    /api/stock?fecha=YYYY-MM-DD
POST   /api/stock
GET    /api/stock/alertas?fecha=YYYY-MM-DD
```

### Configuración del día
```
GET    /api/configuracion-dia?fecha=YYYY-MM-DD
POST   /api/configuracion-dia/feriado?fecha=YYYY-MM-DD
```

### Resúmenes y métricas
```
GET    /api/resumen/{clienteId}?desde=YYYY-MM-DD&hasta=YYYY-MM-DD
GET    /api/metricas/productos-mas-vendidos?desde=YYYY-MM-DD&hasta=YYYY-MM-DD
GET    /api/metricas/dias-mas-fuertes?desde=YYYY-MM-DD&hasta=YYYY-MM-DD
GET    /api/metricas/totales-por-cliente?desde=YYYY-MM-DD&hasta=YYYY-MM-DD
GET    /api/metricas/mercaderia-por-cliente?desde=YYYY-MM-DD&hasta=YYYY-MM-DD
```

---

## 8. DTOs principales

### Request
- `ClienteRequest` — nombre, direccion, tipoFacturacion, ordenRuta
- `PedidoRequest` — clienteId, repartidorId, fecha, lineas[]
- `LineaRequest` — productoId, cantidad
- `ModificacionPedidoRequest` — lineas[], esPermanente (boolean)
- `StockRequest` — productoId, cantidadDisponible, fecha

### Response
- `ClienteResponse` — id, nombre, direccion, tipoFacturacion, ordenRuta, contactos[]
- `PedidoResumenResponse` — id, fecha, clienteNombre, repartidorNombre, totalLineas, total, estado
- `PedidoDetalleResponse` — id, fecha, cliente, repartidor, lineas[], total, tienePrecios, estado
- `LineaResponse` — productoDescripcion, unidad, cantidad, precioUnitario, subtotal
- `BolетаResponse` — datos del pedido formateados para impresión
- `ResumenCobroResponse` — clienteNombre, periodo, pedidos[], totalPeriodo
- `MetricaProductoResponse` — productoDescripcion, unidad, totalVendido, ranking
- `MetricaDiaResponse` — diaSemana, totalPedidos, totalMercaderia
- `MetricaClienteResponse` — clienteNombre, totalPeriodo, desglosePorProducto[]

---

## 9. Consideraciones técnicas

- Usar `BigDecimal` para cantidades y precios (nunca `double` o `float`)
- Usar `LocalDate` para fechas (nunca `Date` o `Timestamp` salvo necesidad)
- Validar requests con Bean Validation (`@NotNull`, `@Positive`, etc.)
- Manejo global de excepciones con `@ControllerAdvice`
- Separar claramente capas: Controller no accede a Repository directamente
- El frontend consume la API vía `fetch()` en JavaScript
- CORS configurado para desarrollo local
- Usar `@media print` en el frontend para la vista de boleta imprimible

---

## 10. Estructura de proyecto sugerida

```
panaderia-backend/
├── src/main/java/com/panaderia/
│   ├── controller/
│   ├── service/
│   │   └── impl/
│   ├── repository/
│   ├── domain/
│   │   ├── model/
│   │   └── enums/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── exception/
└── src/main/resources/
    └── application.properties

panaderia-frontend/
├── index.html
├── css/
│   ├── main.css
│   └── print.css
├── js/
│   ├── api/          // llamadas al backend por entidad
│   └── pages/        // lógica de cada pantalla
└── pages/
    ├── pedidos/
    ├── resumen/
    ├── dashboard/
    └── abm/
```
