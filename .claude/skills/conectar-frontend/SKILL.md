---
name: conectar-frontend
description: Guía para añadir funcionalidades al frontend React de BEST XI que llaman a la API de Spring Boot, respetando el diseño existente. Úsala al añadir botones, formularios, llamadas fetch o resultados en App.jsx y App.css.
---

# Conectar React con la API en BEST XI

El usuario es programador junior y quiere aprender. Explica cada concepto nuevo (useState, useEffect, async/await, fetch) la primera vez que aparezca.

## Reglas de diseño (no negociables)

- El diseño actual funciona y no se rehace: fondo oscuro, verde lima como color principal, logo BEST XI, responsive.
- Antes de añadir estilos, lee `App.css` e `index.css` y reutiliza las variables, clases y colores que ya existen.
- Los estilos nuevos van al final de `App.css`, con un comentario que indique a qué funcionalidad pertenecen.
- Comprueba que lo nuevo se ve bien en móvil.

## Pasos para una nueva llamada a la API

1. Confirmar que el endpoint funciona en el navegador antes de tocar React.
2. Explicar qué estado hace falta (datos, cargando, error) y dar el código exacto de los `useState`, indicando dónde va y explicándolo línea a línea. El usuario casi no sabe frontend: no pedirle que lo deduzca a partir de pistas.
3. Dar el código exacto de la función `async` con `fetch`, en trozos pequeños y explicado con palabras sencillas. Comprobar que:
   - comprueba `response.ok` antes de leer el JSON,
   - gestiona el estado de carga,
   - muestra un mensaje de error comprensible si el backend está apagado.
4. Pintar el resultado en el JSX. Tú puedes encargarte de la maquetación y el CSS si el usuario lo prefiere, explicando qué hace cada clase.

## Cómo probarlo

1. Backend arrancado en 8080 y frontend con `npm.cmd run dev -- --host 0.0.0.0`.
2. Abrir la web, hacer la acción y explicar qué debería verse.
3. Si algo falla, revisar juntos la consola del navegador (F12) y la pestaña Network. Un error de CORS significa que hay que revisar la configuración del backend, no el frontend.
