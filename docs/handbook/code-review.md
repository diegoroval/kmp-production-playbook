# Guía de code review

## Qué revisar, en orden de importancia
1. **Corrección:** ¿hace lo que dice? ¿Contempla errores, casos borde y concurrencia?
2. **Seguridad:** datos sensibles, permisos, almacenamiento y red.
3. **Diseño:** ¿está en la capa correcta (común o plataforma)? ¿Agrega una abstracción que todavía nadie necesita?
4. **Tests:** ¿fallarían si la lógica se rompe?
5. **Legibilidad:** nombres claros e intención evidente.
6. **Estilo:** lo resuelve detekt; no se discute en el review.

## Cómo comentar
- Etiqueta cada comentario como **bloqueante**, **sugerencia** o **pregunta**.
- Comenta el código, no a la persona. Explica el *por qué*, no solo el *qué*.
- Si tienes que explicar algo más de 3 veces, el código necesita un cambio, no un comentario.
- Si un PR tiene más de 400 líneas, pide dividirlo.

## Revisión con agentes de IA
Un agente puede hacer una primera pasada con este checklist, pero la aprobación final siempre es humana. Todo lo que genera la IA se verifica con tests y con la spec.
