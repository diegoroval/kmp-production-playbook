# Tema 2 · Certificate pinning

Código: [`PinningConfig`](../../shared/src/commonMain/kotlin/dev/playbook/pinning/PinningConfig.kt) ·
[Android/JVM](../../shared/src/okhttpMain/kotlin/dev/playbook/pinning/PinnedHttpClient.okhttp.kt) ·
[iOS](../../shared/src/appleMain/kotlin/dev/playbook/pinning/PinnedHttpClient.apple.kt) ·
[ADR-0003](../adr/0003-pinning-con-ktor.md)

## 1. Qué problema resuelve y por qué importa
TLS confía en cualquier certificado firmado por alguna de las ~150 autoridades (CA) que trae el sistema operativo. Si alguien consigue que el equipo confíe en su CA (un proxy corporativo, malware que instala un certificado, una CA comprometida), puede leer y modificar el tráfico de la app sin que TLS se queje.

El pinning agrega una condición: además de ser válido, el certificado del servidor tiene que tener **una llave pública que la app ya conoce**. Un atacante puede emitir un certificado válido, pero no con tu llave privada. En banca y fintech lo pide MASVS-NETWORK-2 de OWASP.

El riesgo es el inverso: si el servidor cambia de llave y la app no la tiene, **la app se queda sin red para todos los usuarios** hasta que publiques una versión nueva y la instalen. Por eso este tema es más sobre rotación que sobre código.

## 2. Cuándo sí hace sentido
- Apps de banca, pagos, salud o cualquier app que maneje credenciales o datos sensibles.
- Cuando controlas el servidor y sus certificados, o hay un acuerdo con quien los administra.
- Cuando el modelo de amenaza incluye equipos con CAs instaladas por terceros (MDM, proxies, malware).

## 3. Cuándo no es necesario
- Apps sin datos sensibles: TLS estándar ya protege bien el caso común.
- APIs de terceros o CDNs que rotan llaves sin avisarte. Fijarlas es una caída programada.
- Equipos sin un proceso para rotar certificados ni para publicar una versión urgente. Sin eso, el pinning causa más incidentes de los que evita.

## 4. Alternativas
| Opción | Cuándo elegirla | Por qué no aquí |
|---|---|---|
| Solo TLS estándar | La mayoría de apps | El tema existe para las que necesitan más |
| `network_security_config` (Android) + `NSPinnedDomains` (iOS) | Una app sin cliente HTTP compartido; quieres cero dependencias | Vive en la app, no en un módulo KMP, y no se puede probar en CI desde una librería. Android sí ofrece `expiration`, que apaga el pinning en una fecha |
| `TrustManager` o delegado de `URLSession` propio | Necesitas reglas que ningún pinner soporta | Es código criptográfico propio y fácil de equivocar |
| Fijar la CA intermedia en vez del servidor | El servidor rota llaves seguido y no puedes coordinar | Protege menos: cualquier certificado de esa CA pasa |
| **Pinners de Ktor (esta guía)** | App KMP que ya usa Ktor | — |

## 5. Implementación paso a paso
**Qué se fija:** el hash SHA-256 de la llave pública (SPKI), no el certificado. El certificado se renueva cada año. La llave puede mantenerse en la renovación, y entonces el pin sigue sirviendo.

**Cómo obtener el pin de un servidor:**
```bash
openssl s_client -servername api.example.com -connect api.example.com:443 </dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary | openssl enc -base64
```

**Común (`PinningConfig`):** un mapa `host → pines`. La validación rechaza cualquier host con menos de **2 pines distintos**: el de la llave actual y uno de respaldo. Ese segundo pin es lo que permite rotar sin dejar a la app sin red.

**Android y JVM:** el `CertificatePinner` de OkHttp, configurado en el motor OkHttp de Ktor. Primero corre la validación TLS normal y después el pinning.

**iOS:** el `CertificatePinner` de Ktor para Darwin, como `handleChallenge` de `NSURLSession`. Valida la cadena con `SecTrust` y después compara el SPKI. Si no coincide, cancela la conexión con `TlsPeerUnverifiedException`.

**La llave de respaldo:**
1. Genera un par de llaves nuevo y guárdalo fuera del servidor (un gestor de secretos o una bóveda). No se usa todavía.
2. Su pin va en la app desde el primer release, junto al de la llave actual.
3. El día que rotas, el servidor pasa a usar la llave de respaldo. Las apps ya publicadas la aceptan.
4. En el siguiente release, la llave de respaldo pasa a ser la actual y agregas un respaldo nuevo.

**Pendiente para producción:**
- Un interruptor remoto para desactivar el pinning ante una emergencia. Si lo agregas, esa configuración remota tiene que venir firmada, o el atacante lo apagará.
- Monitorear los errores de pinning (tema 5): un pico después de un cambio en el servidor es la señal de que algo se rotó mal.

## 6. Cómo probarlo
| Plataforma | Test | Qué demuestra |
|---|---|---|
| Común | [`PinningConfigTest`](../../shared/src/commonTest/kotlin/dev/playbook/pinning/PinningConfigTest.kt) | Rechaza un solo pin, pines repetidos y formatos que no son SHA-256 |
| Android/JVM | [`OkHttpPinningTest`](../../shared/src/jvmTest/kotlin/dev/playbook/pinning/OkHttpPinningTest.kt), servidor TLS local con una CA propia | Pin correcto conecta, pines incorrectos cortan la conexión, y **después de rotar a la llave de respaldo la app sigue funcionando** |
| iOS | [`DarwinPinningTest`](../../shared/src/appleTest/kotlin/dev/playbook/pinning/DarwinPinningTest.kt), contra `example.com` con pines falsos | El pinner de Darwin corta la conexión. Necesita red, pero no depende de la llave real del host |

Android no tiene test propio en emulador: usa exactamente el mismo código OkHttp que corre en `jvmTest`.

**Verificación manual:** en un equipo con la CA de un proxy (Proxyman, mitmproxy o Charles) instalada y en confianza, las llamadas sin pinning se ven en el proxy y las que tienen pinning fallan.

## 7. Decisión
[ADR-0003: pinning con los pinners de Ktor (OkHttp y Darwin)](../adr/0003-pinning-con-ktor.md)
