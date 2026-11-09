---
name: cerrar-sesion
description: Cierre de una sesión de trabajo en BEST XI. Úsala cuando el usuario diga que ha terminado por hoy, que quiere cerrar, o que quiere guardar el progreso.
---

# Cerrar la sesión de trabajo

1. Resume en pocas líneas qué se ha hecho hoy y qué ha aprendido el usuario (conceptos nuevos que han aparecido).
2. Pregunta si lo hecho está probado y funcionando. Si algo quedó a medias, anótalo como pendiente.
3. Actualiza `CLAUDE.md`:
   - sección «Tarea actual»: estado real y siguiente paso concreto,
   - sección «Hoja de ruta»: marca lo completado,
   - si se ha tomado alguna decisión técnica o se ha descubierto un problema de datos, añádelo en su sección.
   Mantén el archivo breve: sustituye información antigua en vez de acumularla.
4. Muestra al usuario los cambios del CLAUDE.md antes de guardarlos.
5. Recuerda hacer commit si usa git, proponiendo un mensaje claro, por ejemplo: `git add .` y `git commit -m "Botón Analizar titularidad conectado"`.
