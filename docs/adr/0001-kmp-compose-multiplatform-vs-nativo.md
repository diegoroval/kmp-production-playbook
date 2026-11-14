# ADR-0001: Kotlin Multiplatform + Compose Multiplatform para los proyectos nuevos

- **Fecha:** retroactivo. Documenta una decisión tomada en proyectos reales de clientes (anonimizados).
- **Estado:** Aceptado

## Contexto
Un equipo pequeño entregaba apps para Android e iOS desarrollando cada plataforma por separado. Eso tenía tres costos:
- La misma lógica de negocio se implementaba dos veces.
- Los mismos bugs se corregían dos veces, a veces con comportamientos distintos entre plataformas.
- Los tiempos de entrega se alargaban para clientes que validaban varias versiones por semana.

**Restricciones:** el equipo ya dominaba Kotlin y Android, los clientes exigían una experiencia nativa en ambas plataformas, y el presupuesto no permitía crecer el equipo.

## Opciones consideradas
1. **Seguir con nativo separado (Kotlin + Swift).** Máximo control y experiencia nativa, pero se mantiene la duplicación.
2. **Framework multiplataforma con otro lenguaje (Flutter o React Native).** Ambos se evaluaron:
   - **Flutter:** obligaba al equipo a aprender un lenguaje (Dart) y un modelo de arquitectura nuevos mientras entregaba.
   - **React Native:** los equipos de los clientes, que mantendrían o extenderían las apps, no tenían necesariamente experiencia en JavaScript.
3. **KMP compartiendo solo dominio y datos (UI nativa).** Menor riesgo, pero la UI se sigue duplicando.
4. **KMP + Compose Multiplatform (dominio, datos y UI compartidos).** Máximo código compartido sin salir de Kotlin, con la opción de bajar a nativo donde haga falta.

## Decisión
Opción 4, **KMP + Compose Multiplatform**, para los proyectos nuevos. El criterio principal fue reducir la duplicación sin perder la opción de escribir en nativo pantallas o integraciones puntuales (`expect/actual`, interoperabilidad con SwiftUI/UIKit).

## Consecuencias
- **Positivas:**
  - Hasta **90% de código compartido** entre Android e iOS.
  - Entre **30% y 45% menos tiempo de implementación** frente a desarrollar nativo por separado.
  - Una sola fuente de verdad para las reglas de negocio.
- **Negativas y costos que se aceptan:**
  - Integración con Xcode y tiempos de build en iOS.
  - Ecosistema de librerías multiplataforma menos maduro que el nativo.
  - Curva de aprendizaje para quien viene solo de iOS.
  - Hay que definir con disciplina el límite entre lo común y lo propio de cada plataforma (permisos, cámara, notificaciones).
- **Cuándo revisar esta decisión:**
  - Si un proyecto necesita una UI muy específica de una plataforma en la mayoría de sus pantallas.
  - Si el equipo pasa a tener especialistas iOS que no trabajan en Kotlin.
