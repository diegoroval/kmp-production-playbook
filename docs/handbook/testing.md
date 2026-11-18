# Estrategia de testing

| Capa | Dónde | Qué se prueba | Cuándo corre |
|---|---|---|---|
| Lógica común | `commonTest` | Reglas, casos borde, errores | En cada PR (JVM e iOS) |
| `actual` por plataforma | `androidHostTest`, `iosTest` | Solo si el `actual` tiene lógica propia | En cada PR |
| Integración con el sistema operativo | `androidDeviceTest` (emulador), `macosTest` | Keystore, Keychain | En cada PR |
| TLS y pinning | `jvmTest` (servidor TLS local), `appleTest` (host real con pines falsos) | Pin correcto, pin incorrecto, rotación | En cada PR |
| Red real, dispositivo físico | Dispositivo con proxy MITM | Pinning de punta a punta, biometría | Manual, al cerrar cada tema |

**Reglas:**
- Se prueba el comportamiento, no la implementación.
- Primero se escribe un test que falle por la razón correcta.
- No se prueba código del sistema operativo ni de librerías, solo nuestra integración con ellos.
- `jvm()` existe para correr `commonTest` rápido en CI y para probar el pinning de OkHttp contra un servidor TLS local. No es un target de producto.
- `macosArm64()` existe solo para probar el Keychain: el simulador iOS no le da Keychain a un binario de test sin app host.
- Contratos compartidos: un test abstracto en `commonTest` (p. ej. `SecureStorageContract`) que cada plataforma hereda con su implementación real.
