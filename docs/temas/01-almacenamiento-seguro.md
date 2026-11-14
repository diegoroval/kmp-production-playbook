# Tema 1 · Almacenamiento seguro (Keystore / Keychain)

Código: [`SecureStorage`](../../shared/src/commonMain/kotlin/dev/playbook/securestorage/SecureStorage.kt) ·
[Android](../../shared/src/androidMain/kotlin/dev/playbook/securestorage/KeystoreSecureStorage.kt) ·
[iOS](../../shared/src/appleMain/kotlin/dev/playbook/securestorage/KeychainSecureStorage.kt) ·
[ADR-0002](../adr/0002-almacenamiento-seguro-sin-dependencias.md)

## 1. Qué problema resuelve y por qué importa
Un access token o refresh token guardado en `SharedPreferences` o `UserDefaults` queda en texto plano. Basta un backup, un equipo con root o jailbreak, o una herramienta forense para leerlo, y con ese token se suplanta al usuario sin conocer su contraseña.

La solución es que **la llave de cifrado viva en el hardware seguro del equipo** (TEE/StrongBox en Android, Secure Enclave y Keychain en iOS) y nunca en el código ni en un archivo. En banca y fintech es un requisito: MASVS-STORAGE-1 de OWASP.

## 2. Cuándo sí hace sentido
- Tokens de sesión, refresh tokens, IDs de dispositivo vinculados a la cuenta.
- La llave con la que cifras una base de datos local (SQLCipher, Room o SQLDelight cifrados).
- Cualquier dato que, si se filtra, permite actuar en nombre del usuario.

## 3. Cuándo no es necesario
- Preferencias de UI, flags de onboarding, caché de contenido público: `DataStore` o `UserDefaults` bastan.
- Datos que el servidor ya expone sin autenticación.
- Volúmenes grandes: no metas una base de datos en el Keychain. Cifra la base y guarda aquí **solo su llave**.
- Secretos de la app en sí (API keys de terceros): el cliente no puede guardar secretos. Muévelos a un backend.

## 4. Alternativas
| Opción | Cuándo elegirla | Por qué no aquí |
|---|---|---|
| `EncryptedSharedPreferences` (Jetpack Security) | Proyectos existentes que ya la usan | Deprecada por Google desde 2025, sin reemplazo directo |
| Tink + DataStore | Necesitas rotación de llaves o varios formatos cifrados | Agrega ~1 MB y una dependencia para 3 operaciones |
| `multiplatform-settings` (`KeychainSettings`) | Quieres una API común lista y aceptas la dependencia | En Android no cifra por sí sola |
| `KVault` y similares | Prototipos | Mantenimiento irregular. Revisa la actividad antes de adoptarla |
| **APIs nativas (esta guía)** | Pocos secretos, cero dependencias, control total | — |

## 5. Implementación paso a paso
**Común:** una interfaz de 3 métodos (`get`, `put`, `remove`). Cada plataforma la implementa con la API nativa, sin `expect/actual`, porque Android necesita un `Context` en el constructor.

**Android (`KeystoreSecureStorage`):**
1. Genera una sola vez una llave **AES-256-GCM dentro de `AndroidKeyStore`**. La llave no se puede exportar.
2. Al guardar, cifra con un IV aleatorio (lo genera el `Cipher`) y almacena `IV + texto cifrado` en Base64 en `SharedPreferences`.
3. Usa el **nombre de la clave como AAD**. Si alguien intercambia dos valores dentro del XML, el descifrado falla en vez de devolver el dato equivocado.
4. Si el descifrado falla (backup restaurado en otro equipo, llave invalidada), el valor se borra y se devuelve `null`. La app pide login de nuevo. **No intentes recuperarlo.**

**iOS (`KeychainSecureStorage`):**
1. Cada clave es un ítem `kSecClassGenericPassword` identificado por `service` + `account`.
2. Usa `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`: es legible en segundo plano (para refrescar tokens) y **no viaja en backups ni a otro iPhone**.
3. Para sobrescribir, borra el ítem y lo vuelve a crear. Es más simple que `SecItemUpdate` y suficiente para secretos pequeños.
4. Los objetos CoreFoundation se liberan explícitamente (`CFRelease`). En Kotlin/Native no hay ARC para ellos.

**Pendiente para producción:**
- Excluye el archivo de preferencias del backup en Android (`android:dataExtractionRules`).
- Si el dato exige biometría para leerse, agrega `setUserAuthenticationRequired(true)` o `kSecAccessControlBiometryCurrentSet`. Eso cambia la UX y merece su propio ADR.

## 6. Cómo probarlo
Un solo contrato, [`SecureStorageContract`](../../shared/src/commonTest/kotlin/dev/playbook/securestorage/SecureStorageContract.kt), con 5 casos: ausente, ida y vuelta (con Unicode), sobrescritura, borrado y persistencia entre instancias. Cada plataforma lo hereda con su implementación real:

| Plataforma | Dónde corre | Por qué ahí |
|---|---|---|
| Android | `androidDeviceTest` en emulador (job `android-device` en CI) | Keystore no existe en la JVM ni en Robolectric |
| iOS | `macosTest` (job `apple` en CI) | Mismo Security framework. El simulador iOS no da Keychain a un test sin app host (`errSecNotAvailable`, -25291) |

**Verificación manual:** en un emulador con root, abre `/data/data/<app>/shared_prefs/secure_storage.xml`. Solo debe haber Base64 ilegible.

## 7. Decisión
[ADR-0002: almacenamiento seguro con APIs nativas, sin dependencias](../adr/0002-almacenamiento-seguro-sin-dependencias.md)
