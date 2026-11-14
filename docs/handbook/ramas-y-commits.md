# Ramas y commits

- **`main`** está siempre desplegable y protegida: solo recibe cambios por PR con CI en verde.
- **Ramas:** `tema/<n>-<nombre>`, `fix/<descripcion>`, `docs/<descripcion>`.
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/), por ejemplo `feat(secure-storage): ...`, `fix: ...`, `docs(adr): ...`.
- **Merge:** squash, para que cada PR deje un solo commit legible en `main`.
- **Releases:** un tag `vX.Y` por cada tema cerrado. Estos tags alimentan la métrica DORA de frecuencia de entrega.
