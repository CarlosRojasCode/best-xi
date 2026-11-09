---
name: nuevo-endpoint
description: Guía para crear o modificar un endpoint REST en el backend Spring Boot de BEST XI (controller, servicio, repositorio, CORS y prueba). Úsala cuando haya que exponer datos nuevos al frontend o cambiar una respuesta de la API.
---

# Crear o modificar un endpoint en BEST XI

El usuario es programador junior y quiere aprender. Guíale paso a paso y deja que escriba él la lógica.

## Antes de escribir código

1. Comprueba si ya existe un endpoint o servicio que haga algo parecido (busca en `com/bestxi/`). Reutiliza antes de crear.
2. Explica en 2-3 frases qué capas hay que tocar y por qué: Controller (recibe la petición), Service (lógica), Repository (consulta a PostgreSQL), y si hace falta un DTO (forma de la respuesta).
3. Ten en cuenta el prefijo: `/api/jugadores` lleva `/api` y `/jugadores/{id}/titularidad` no. Pregunta antes de cambiar rutas existentes, porque el frontend depende de ellas.

## Mientras se implementa

- Deja que el usuario escriba el método del Service y la consulta del Repository. Tú puedes preparar el esqueleto del Controller y los imports.
- Si el endpoint lo va a llamar React, revisa la configuración CORS para `http://localhost:5173` y `http://localhost:5174`.
- No modifiques la fórmula ni los pesos del modelo salvo que se pida explícitamente.
- Devuelve DTOs en vez de entidades JPA directamente cuando la entidad tenga relaciones, para evitar respuestas enormes o bucles infinitos.

## Cómo probarlo

1. Reiniciar el backend: `.\mvnw.cmd spring-boot:run`
2. Abrir la URL en el navegador o con `curl` (por ejemplo `http://localhost:8080/api/jugadores?nombre=Fekir`).
3. Indicar qué JSON debería verse y qué significa cada campo.
4. Si falla, leer juntos el error de la consola de Spring Boot antes de proponer arreglos.
