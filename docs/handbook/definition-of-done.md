# Definition of Done

Un cambio está terminado cuando:

1. **Funciona en ambas plataformas.** Compila para Android e iOS, y la lógica nueva vive en `commonMain` salvo que exista un motivo documentado.
2. **Está probado.** Hay tests en `commonTest` para la lógica, y los tests por plataforma se agregan solo cuando el `actual` tiene lógica propia.
3. **CI está en verde:** tests en JVM y en el simulador de iOS, build de Android y detekt.
4. **Está documentado.** El README del tema explica *por qué*, *cuándo sí*, *cuándo no* y *qué alternativas* existen.
5. **La decisión quedó registrada.** Si se eligió entre opciones con trade-offs reales, hay un ADR en `docs/adr/`.
6. **Es seguro.** No incluye secretos, llaves, tokens ni datos reales, y las dependencias nuevas están justificadas.
7. **Pasó por PR.** Nada entra directo a `main`.
