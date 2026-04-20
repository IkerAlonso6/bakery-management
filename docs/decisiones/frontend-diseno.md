# Decisiones de Diseño — Frontend

> Este documento debe ser leído por la IA antes de implementar cualquier ticket de frontend.
> Define la identidad visual, componentes reutilizables, navegación y convenciones de JavaScript.

---

## 1. Identidad visual

### Concepto
Cálido pero moderno. Evoca una panadería sin caer en lo rústico o anticuado. Funcional, agradable y con personalidad — no genérico.

### Paleta de colores
```css
:root {
  /* Base */
  --color-bg:          #FAFAFA;
  --color-surface:     #FFFFFF;
  --color-border:      #E5E7EB;
  --color-text:        #1F2937;
  --color-text-muted:  #6B7280;

  /* Acento principal */
  --color-primary:     #F97316;
  --color-primary-hover: #EA6C0A;
  --color-primary-light: #FEF3E8;

  /* Estados */
  --color-pendiente:   #F59E0B;
  --color-pendiente-bg:#FFFBEB;
  --color-entregado:   #10B981;
  --color-entregado-bg:#ECFDF5;
  --color-alerta:      #EF4444;
  --color-alerta-bg:   #FEF2F2;

  /* Sidebar (desktop) */
  --color-sidebar-bg:  #1F2937;
  --color-sidebar-text:#F9FAFB;
  --color-sidebar-active: #F97316;
}
```

### Tipografía
- **Fuente principal:** Plus Jakarta Sans (Google Fonts)
- **Tamaño base:** 16px (nunca menos en mobile)
- **Jerarquía:**
  - Títulos de página: 24px, weight 700
  - Subtítulos / encabezados de sección: 18px, weight 600
  - Texto de cuerpo: 16px, weight 400
  - Texto secundario / labels: 14px, weight 400
  - Badges / chips: 12px, weight 600

### Importar fuente en HTML
```html
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&display=swap" rel="stylesheet">
```

---

## 2. Componentes reutilizables

Todos los componentes se implementan como **funciones JavaScript que devuelven strings HTML**.
Se definen en `js/components/` y se importan donde se necesitan.
Nunca duplicar la implementación de un componente en distintas páginas.

### Estructura de carpetas de componentes
```
js/
├── components/
│   ├── tarjetaPedido.js
│   ├── lineaPedido.js
│   ├── badge.js
│   ├── modal.js
│   ├── tabla.js
│   └── toast.js
├── api/
│   └── client.js
├── pages/
│   └── ...
└── state.js
```

### Tarjeta de pedido (mobile)
```javascript
// js/components/tarjetaPedido.js
export function renderTarjetaPedido(pedido) {
  return `
    <div class="card-pedido" data-id="${pedido.id}">
      <div class="card-pedido__header">
        <span class="card-pedido__orden">#${pedido.ordenRuta}</span>
        <span class="card-pedido__cliente">${pedido.clienteNombre}</span>
        ${renderBadgeEstado(pedido.estado)}
      </div>
      <div class="card-pedido__productos">${pedido.resumenProductos}</div>
      <div class="card-pedido__actions">
        <button class="btn btn--secondary" onclick="verDetalle(${pedido.id})">Ver detalle</button>
        ${pedido.estado === 'PENDIENTE'
          ? `<button class="btn btn--primary" onclick="confirmarEntrega(${pedido.id})">Confirmar entrega</button>`
          : ''}
      </div>
    </div>
  `;
}
```

### Badge de estado
```javascript
// js/components/badge.js
export function renderBadgeEstado(estado) {
  const config = {
    PENDIENTE:  { label: 'Pendiente',  clase: 'badge--pendiente' },
    ENTREGADO:  { label: 'Entregado',  clase: 'badge--entregado' },
    CON_ALERTA: { label: 'Sin stock',  clase: 'badge--alerta' },
  };
  const { label, clase } = config[estado] ?? { label: estado, clase: '' };
  return `<span class="badge ${clase}">${label}</span>`;
}
```

### Toast de notificación
```javascript
// js/components/toast.js
export function showToast(mensaje, tipo = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast toast--${tipo}`;
  toast.textContent = mensaje;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}
```

### Modal de confirmación
```javascript
// js/components/modal.js
export function showModal({ titulo, mensaje, onConfirmar, onCancelar }) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal">
      <h3 class="modal__titulo">${titulo}</h3>
      <p class="modal__mensaje">${mensaje}</p>
      <div class="modal__actions">
        <button class="btn btn--secondary" id="modal-cancelar">Cancelar</button>
        <button class="btn btn--primary" id="modal-confirmar">Confirmar</button>
      </div>
    </div>
  `;
  document.body.appendChild(modal);
  document.getElementById('modal-confirmar').onclick = () => { modal.remove(); onConfirmar(); };
  document.getElementById('modal-cancelar').onclick = () => { modal.remove(); if (onCancelar) onCancelar(); };
}
```

---

## 3. Clases CSS base obligatorias

Definidas en `css/main.css`. La IA debe usar estas clases y no inventar nombres alternativos.

### Botones
```css
.btn                  /* base de todos los botones */
.btn--primary         /* naranja, acción principal */
.btn--secondary       /* gris claro, acción secundaria */
.btn--danger          /* rojo, acciones destructivas */
.btn--sm              /* tamaño pequeño */
.btn--lg              /* tamaño grande (mobile) */
```

### Badges de estado
```css
.badge                /* base */
.badge--pendiente     /* fondo amarillo ámbar */
.badge--entregado     /* fondo verde */
.badge--alerta        /* fondo rojo */
```

### Tarjetas
```css
.card                 /* contenedor base con sombra y bordes redondeados */
.card-pedido          /* tarjeta específica de pedido */
.card-pedido__header
.card-pedido__productos
.card-pedido__actions
```

### Layout desktop
```css
.layout               /* contenedor principal: sidebar + contenido */
.sidebar              /* sidebar fijo izquierdo */
.sidebar__nav
.sidebar__nav-item
.sidebar__nav-item--active
.main-content         /* área de contenido principal */
```

### Layout mobile
```css
.bottom-nav           /* barra de navegación inferior */
.bottom-nav__item
.bottom-nav__item--active
```

### Formularios
```css
.form-group           /* contenedor de label + input */
.form-label
.form-input
.form-select
.form-error           /* mensaje de error de validación */
```

### Tablas (desktop)
```css
.tabla                /* tabla base */
.tabla__header
.tabla__row
.tabla__row--zebra    /* filas alternadas */
.tabla__cell
.tabla__actions       /* celda de acciones */
```

### Toast
```css
.toast                /* notificación flotante */
.toast--success
.toast--error
.toast--warning
```

### Modal
```css
.modal-overlay        /* fondo oscuro */
.modal                /* contenedor del modal */
.modal__titulo
.modal__mensaje
.modal__actions
```

---

## 4. Manejo de estado

Archivo: `js/state.js`

```javascript
// js/state.js
export const AppState = {
  fechaActiva: new Date().toISOString().split('T')[0], // YYYY-MM-DD
  pedidoSeleccionado: null,
  clienteSeleccionado: null,

  setFechaActiva(fecha) { this.fechaActiva = fecha; },
  setPedidoSeleccionado(pedido) { this.pedidoSeleccionado = pedido; },
  setClienteSeleccionado(cliente) { this.clienteSeleccionado = cliente; },
};
```

Reglas:
- Todo dato compartido entre pantallas pasa por `AppState`
- No usar variables globales sueltas — todo centralizado en `AppState`
- No usar `localStorage` ni `sessionStorage`

---

## 5. Navegación

### Estrategia
Páginas HTML separadas con navegación tradicional. No usar SPA ni ruteo en JavaScript.

### Mobile — Bottom navigation bar
Visible en todas las páginas mobile. Tres ítems fijos:
- 🏠 Pedidos del día → `pages/pedidos/home.html`
- ➕ Nuevo pedido → `pages/pedidos/nuevo.html`
- ✅ Entregados → `pages/pedidos/entregados.html`

### Desktop — Sidebar fijo
Visible en todas las páginas desktop. Ítems:
- Pedidos del día
- Nuevo pedido
- Historial
- Resúmenes
- Dashboard
- Configuración del día
- ABM → Clientes / Productos / Repartidores

### Responsive
- Por debajo de 768px → ocultar sidebar, mostrar bottom nav
- Por encima de 768px → ocultar bottom nav, mostrar sidebar

```css
@media (max-width: 768px) {
  .sidebar { display: none; }
  .bottom-nav { display: flex; }
}

@media (min-width: 769px) {
  .bottom-nav { display: none; }
  .sidebar { display: flex; }
}
```

---

## 6. Comunicación con el backend

Archivo base: `js/api/client.js`

```javascript
// js/api/client.js
const BASE_URL = 'http://localhost:8080/api';

export async function fetchAPI(method, endpoint, body = null) {
  const options = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };
  if (body) options.body = JSON.stringify(body);

  const response = await fetch(`${BASE_URL}${endpoint}`, options);

  if (!response.ok) {
    const error = await response.json().catch(() => ({ mensaje: 'Error desconocido' }));
    throw new Error(error.mensaje ?? `Error ${response.status}`);
  }

  return response.status === 204 ? null : response.json();
}
```

Módulos por entidad en `js/api/`:
```
js/api/
├── client.js         ← base, nunca modificar
├── pedidos.js        ← fetchAPI('/pedidos', ...)
├── clientes.js
├── productos.js
├── repartidores.js
├── stock.js
├── resumen.js
└── metricas.js
```

Cada módulo expone funciones nombradas:
```javascript
// js/api/pedidos.js
import { fetchAPI } from './client.js';

export const getPedidosPorFecha = (fecha) => fetchAPI('GET', `/pedidos?fecha=${fecha}`);
export const getPedidoDetalle = (id) => fetchAPI('GET', `/pedidos/${id}`);
export const postPedido = (data) => fetchAPI('POST', '/pedidos', data);
export const confirmarEntrega = (id) => fetchAPI('PATCH', `/pedidos/${id}/confirmar`);
```

---

## 7. Convenciones JavaScript

- Usar `async/await`. Nunca `.then()` encadenado.
- Manejo de errores con `try/catch` en cada función de página — nunca ignorar errores silenciosamente.
- Mostrar siempre feedback al usuario con `showToast()` en caso de éxito o error.
- Separar lógica de renderizado y lógica de datos — una función renderiza, otra llama a la API.
- Usar `const` por defecto, `let` solo cuando la variable cambia. Nunca `var`.

---

## 8. Responsividad y UX mobile

- Tamaño mínimo de área táctil: 44x44px (estándar Apple/Google)
- Espaciado generoso entre elementos tocables en mobile
- Formularios en mobile: un campo por fila, labels arriba del input
- Evitar hover-only interactions — todo debe funcionar con tap
- Feedback inmediato en acciones: deshabilitar botón mientras carga, mostrar spinner si tarda más de 500ms

---

## 9. Impresión (boleta)

```css
/* css/print.css */
@media print {
  .sidebar,
  .bottom-nav,
  .btn,
  .toast { display: none !important; }

  body { background: white; font-size: 14px; }
  .boleta { max-width: 100%; padding: 0; }
}
```

---

## 10. Lo que la IA NO debe hacer en frontend

- ❌ Usar frameworks externos (React, Vue, Alpine, etc.)
- ❌ Reimplementar un componente que ya existe en `js/components/`
- ❌ Llamar a `fetch()` directamente — siempre usar `fetchAPI()` de `client.js`
- ❌ Usar `.then()` encadenado
- ❌ Usar `var`
- ❌ Usar `localStorage` o `sessionStorage`
- ❌ Inventar clases CSS nuevas si ya existe una definida en este documento
- ❌ Crear estilos inline con el atributo `style=""` salvo casos puntuales justificados
- ❌ Usar colores o fuentes distintos a los definidos en las variables CSS
