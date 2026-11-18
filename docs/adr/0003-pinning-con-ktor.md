# ADR-0003: Certificate pinning con los pinners de Ktor (OkHttp y Darwin)

- **Fecha:** 2026-11-18
- **Estado:** Aceptado

## Contexto
La app necesita fijar la llave pública de su API en Android e iOS. A diferencia del tema 1, aquí ya hay una dependencia inevitable: un cliente HTTP. En proyectos KMP ese cliente suele ser Ktor, y Ktor trae un pinner para cada motor. El riesgo principal no es técnico sino operativo: si el servidor rota su llave y la app no la conoce, la app se queda sin red.

## Opciones consideradas
1. **Pinners de Ktor:** el `CertificatePinner` de OkHttp en Android y el de Ktor para Darwin en iOS, con una configuración común. Se puede probar en CI con un servidor TLS local.
2. **Configuración nativa declarativa:** `network_security_config` en Android y `NSPinnedDomains` en iOS. Cero dependencias y Android permite una fecha de caducidad, pero vive en la app y no se puede probar desde un módulo compartido.
3. **Validación propia:** un `TrustManager` en Android y un delegado de `URLSession` en iOS. Control total, pero es código criptográfico propio.
4. **No hacer pinning:** válido para la mayoría de apps, no para las que manejan datos financieros (MASVS-NETWORK-2).

## Decisión
Opción 1. Ktor ya estaría en el proyecto, los dos pinners siguen el mismo modelo (SHA-256 del SPKI) y el comportamiento se demuestra con tests automáticos, incluido el caso de rotación a la llave de respaldo.

`PinningConfig` exige **al menos 2 pines distintos por host**. Una app con un solo pin no puede rotar sin una caída.

## Consecuencias
- **Positivas:**
  - Una sola configuración para las dos plataformas.
  - La rotación está probada en CI, no solo documentada.
  - Ningún código criptográfico propio.
- **Negativas y costos que se aceptan:**
  - Dependencias nuevas: Ktor y OkHttp.
  - Ktor queda en 3.5.x: la 3.6 trae OkHttp 5.5, que exige `compileSdk` 37, y AGP 9.0 llega hasta 36.
  - Los pines van en el binario: cambiarlos sin el respaldo exige un release y que los usuarios actualicen.
  - Sin fecha de caducidad automática, a diferencia de `network_security_config`.
  - El test de iOS necesita red.
- **Cuándo revisar esta decisión:**
  - Si la API pasa a estar detrás de un CDN que rota llaves sin coordinación.
  - Si se agrega configuración remota firmada (permite cambiar pines sin release).
  - Al subir a `compileSdk` 37: actualizar Ktor.
