# Seguimiento — communities (Mafer)

Solo Mafer edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #4 | Community, Membership y CommunityPost; `promote` agregado por André |
| Contrato de repositorio | ✅ | #4 | |
| Persistencia y test H2 | ✅ | #16 | Entidades JPA, repositorios, mappers y adapters; 11 tests H2 |
| Capa de aplicación | ✅ | #26 | Commands, queries, DTOs, un handler de commands y cuatro de queries; 32 tests |
| Resources y endpoints | 🔨 | #34 | `CommunitiesController` con 11 endpoints, prueba HTTP con MockMvc (18 tests) y colección Postman |

**Siguiente paso:** fusionar el controller de `communities`; luego `messaging` (persistencia de `Message`, aplicación y endpoints)

**Última actualización:** 2026-10-21 · Mafer
