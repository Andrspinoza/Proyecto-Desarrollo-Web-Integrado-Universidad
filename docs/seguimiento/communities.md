# Seguimiento — communities (Mafer)

Solo Mafer edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #4 | Community, Membership y CommunityPost; `promote` agregado por André |
| Contrato de repositorio | ✅ | #4 | |
| Persistencia y test H2 | 🔨 | #16 | Entidades JPA, repositorios, mappers y adapters; 11 tests H2 |
| Capa de aplicación | ⏳ | | Commands, queries, handlers y DTOs copiando `iam` |
| Resources y endpoints | 🔨 | #4 | Resources y assemblers en `main`; faltan los controllers |

**Siguiente paso:** fusionar la persistencia; luego B6 (`Message` mínimo con `MessageSentEvent`) y la capa de aplicación de `communities`

**Última actualización:** 2026-10-19 · Mafer
