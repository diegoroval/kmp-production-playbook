# Estrategia de testing

| Capa | Dónde | Qué se prueba | Cuándo corre |
|---|---|---|---|
| Lógica común | `commonTest` | Reglas, casos borde, errores | En cada PR (JVM e iOS) |
| `actual` por plataforma | `androidHostTest`, `iosTest` | Solo si el `actual` tiene lógica propia | En cada PR |
| Integración con el sistema operativo | `androidDeviceTest` (emulador), `macosTest` | Keystore, Keychain | En cada PR |
| Red real, dispositivo físico | Dispositivo | Pinning, biometría | Manual, al cerrar cada tema |

**Reglas:**
- Se prueba el comportamiento, no la implementación.
- Primero se escribe un test que falle por la razón correcta.
- No se prueba código del sistema operativo ni de librerías, solo nuestra integración con ellos.
- `jvm()` existe solo para correr `commonTest` rápido en CI. No es un target de producto.
- `macosArm64()` existe solo para probar el Keychain: el simulador iOS no le da Keychain a un binario de test sin app host.
- Contratos compartidos: un test abstracto en `commonTest` (p. ej. `SecureStorageContract`) que cada plataforma hereda con su implementación real.
