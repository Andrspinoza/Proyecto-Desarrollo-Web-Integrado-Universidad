# Seguimiento — iam (André)

Solo André edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #3 | University y EmailDomain |
| Contrato de repositorio | ✅ | #3 | |
| Persistencia y test H2 | ✅ | #3 | |
| Capa de aplicación (B5) | ✅ | | Commands, queries, DTO, assembler y handlers de University; modelo para los demás módulos |
| Resources y endpoints | ✅ | #3 | El controller usa los handlers de la capa de aplicación; prueba HTTP incluida |
| Base de `shared` para la capa de aplicación | ✅ | #11 | Raíz de agregado pura y `DomainEventDispatcher` |

**Siguiente paso:** `Account` (cuenta con contraseña cifrada) con la misma estructura, y luego B7 (WebSocket)

**Última actualización:** 2026-10-19 · André
