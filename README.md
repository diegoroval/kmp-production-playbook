# KMP Production Playbook

Guía práctica, con código, para llevar apps **Kotlin Multiplatform** (Android + iOS) a producción con **seguridad** y **observabilidad**.

Cada tema responde lo mismo, en este orden:
1. Qué problema resuelve y por qué importa
2. Cuándo **sí** hace sentido
3. Cuándo **no** es necesario
4. Qué alternativas existen
5. Implementación paso a paso (código común + Android + iOS)
6. Cómo probarlo
7. La decisión, registrada en un ADR

## Temas

| # | Tema | Estado |
|---|---|---|
| 1 | [Almacenamiento seguro (Keystore / Keychain)](docs/temas/01-almacenamiento-seguro.md) | ✅ |
| 2 | [Certificate pinning](docs/temas/02-certificate-pinning.md) | ✅ |
| 3 | Autenticación OAuth2 + PKCE | 🔜 |
| 4 | Integridad del dispositivo (Play Integrity / App Attest) | 🔜 |
| 5 | Crash reporting | 🔜 |
| 6 | Logs estructurados, tracing y rendimiento | 🔜 |

## Cómo se trabaja en este repo
- [Definition of Done](docs/handbook/definition-of-done.md)
- [Guía de code review](docs/handbook/code-review.md)
- [Estrategia de testing](docs/handbook/testing.md)
- [Ramas y commits](docs/handbook/ramas-y-commits.md)
- [Decisiones de arquitectura (ADRs)](docs/adr/)

## Tech radar

| Adoptar | Probar | Mantener | Evitar |
|---|---|---|---|
| Kotlin Multiplatform, Compose Multiplatform, ADRs, CI en cada PR | SKIE para la interoperabilidad con Swift, OpenTelemetry mobile | Interoperabilidad nativa vía `expect/actual` | Guardar secretos en el cliente, pinning sin un plan de rotación |

## Correr el proyecto
Requisitos: JDK 17, Android SDK y Xcode (para iOS).

```bash
./gradlew :shared:jvmTest                 # tests comunes y pinning con OkHttp (rápido)
./gradlew :shared:iosSimulatorArm64Test   # tests en el simulador iOS (el de pinning necesita red)
./gradlew :shared:macosArm64Test          # tests del Keychain
./gradlew :shared:connectedAndroidDeviceTest  # tests del Keystore (emulador abierto)
./gradlew detekt                          # análisis estático
```

## Autor
Diego Rodríguez Valdivia · Mobile Tech Lead & Arquitecto de Soluciones · [LinkedIn](https://www.linkedin.com/in/diego-rodriguez-58b82234)

Licencia [MIT](LICENSE).
