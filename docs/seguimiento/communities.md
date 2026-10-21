# Seguimiento — communities (Mafer)

Solo Mafer edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #4 | Community, Membership y CommunityPost; `promote` agregado por André |
| Contrato de repositorio | ✅ | #4 | |
| Persistencia y test H2 | ✅ | #16 | Entidades JPA, repositorios, mappers y adapters; 11 tests H2 |
| Capa de aplicación | 🔨 | #26 | Commands, queries, DTOs, un handler de commands y cuatro de queries; 32 tests |
| Resources y endpoints | 🔨 | #4 | Resources y assemblers en `main`; falta el controller |

**Siguiente paso:** fusionar la capa de aplicación; luego `CommunitiesController` (crear, listar, obtener, unirse, salir, miembros y publicaciones) con su prueba HTTP y la colección Postman

**Última actualización:** 2026-10-21 · Mafer
