# ADR-0002: Almacenamiento seguro con APIs nativas, sin dependencias

- **Fecha:** 2026-11-14
- **Estado:** Aceptado

## Contexto
La app necesita guardar tokens de sesión en Android e iOS con la llave protegida por hardware. `EncryptedSharedPreferences`, la opción habitual en Android, está deprecada. El volumen es pequeño: unos pocos valores de menos de 4 KB.

## Opciones consideradas
1. **Tink + DataStore:** robusto y con rotación de llaves, pero agrega una dependencia pesada solo en Android.
2. **`multiplatform-settings`:** API común lista, pero en Android no cifra por sí sola y habría que combinarla con otra cosa.
3. **APIs nativas detrás de una interfaz común:** Android Keystore + AES-GCM y el Keychain de iOS. Unas 150 líneas en total.
4. **No hacer nada (guardar en texto plano):** inaceptable para tokens, porque incumple MASVS-STORAGE-1.

## Decisión
Opción 3. Con pocos secretos, el código nativo es corto, se audita en una sola lectura y no arrastra dependencias que mantener ni CVEs que seguir.

## Consecuencias
- **Positivas:** cero dependencias, la llave nunca sale del hardware y el comportamiento es idéntico en ambas plataformas (verificado con el mismo contrato de tests).
- **Negativas y costos que se aceptan:**
  - El código criptográfico es nuestro y requiere revisión cuidadosa en cada cambio.
  - Sin rotación de llaves.
  - Probar el Keychain obliga a sumar un target `macosArm64` solo para tests.
- **Cuándo revisar esta decisión:**
  - Si hay que cifrar volúmenes grandes o archivos.
  - Si se exige rotación de llaves (pasar a Tink).
  - Si se requiere biometría por lectura.
