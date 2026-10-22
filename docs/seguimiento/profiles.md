# Seguimiento — profiles (Fernando)

Solo Fernando edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio base de Student y tests | ✅ En main | #5 | Student y value objects iniciales |
| Dominio de perfil y catálogos con tests | ✅ En main | #10 | Career, Visibility, Profile, integración en Student, Interest y Skill |
| Contrato de repositorio de Student | ✅ En main | #9 | StudentRepository |
| Entidad JPA de Student (B2) | ✅ En main | #5 | StudentJpaEntity con FK a universities |
| Persistencia de Student y test H2 | ✅ En main | #9 | StudentJpaRepository, mapper, adapter y seis pruebas H2 |
| Persistencia de catálogos y tests H2 | ✅ En main | #13 | Interest y Skill: contratos, entidades JPA, repositorios JPA, mappers, adapters y ocho pruebas H2 |
| Persistencia de perfil y tests H2 | ✅ En main | #14 | ProfileJpaEntity, relación con Student, mapper y adapter para el perfil completo; seis pruebas H2 adicionales |
| Contrato de fachada de profiles (B3) | ✅ En main | #19 | ProfilesContextFacade en interfaces/acl, con las tres firmas de la guía 6.3 |
| Implementación provisional de fachada | ✅ En main | PR #21 | Bean ProfilesContextFacadeImpl y cinco pruebas; consulta estudiantes y universidades. studentIdOfAccount devuelve vacío hasta que existan Account y su vínculo con Student |
| Afinidad y tests | ⏳ | | AffinityCalculator y AffinityScore pendientes |
| Capa de aplicación de catálogos | ✅ En main | PR #24 | Commands de creación, queries de listado, DTOs, assembler y handlers de Interest y Skill; catorce pruebas unitarias de aplicación |
| Creación y consulta de Student | ✅ En main | PR #33 | Commands, queries, DTOs y assembler; creación con fachada de iam y consulta por ID con visibilidad mediante connections. Diecisiete pruebas unitarias nuevas |
| Resources y endpoints de Student | ✅ En main | PR #33 | POST /api/v1/students y GET /api/v1/students/{id}; ocho pruebas HTTP con MockMvc. Sin Account ni JWT; GET usa X-Student-Id |
| Acciones de Profile y listado de Student | ⏳ | | Commands y query declarados; faltan handlers y endpoints de modificación del perfil y listado |
| Endpoints de catálogos | En PR de catálogos | PR #36 | Resources, assemblers y controllers POST/GET de intereses y habilidades; diez pruebas HTTP. Creación con X-Student-Id como administrador provisional, sin validación de rol hasta Account y JWT |

**Siguiente paso:** completar la verificación de POST/GET de ambos catálogos en Swagger y solicitar revisión del PR #36 con el seguimiento y la descripción actualizados. La suite completa, incluido ApplicationStartupTest, debe pasar. Después de su fusión y de incorporar main, generar el EER Diagram y el diccionario cuando estén todas las tablas. Las acciones de Profile, el listado y la afinidad quedan fuera de este PR. La resolución por cuenta requiere Account y su vínculo real con Student; no se equiparan sus IDs.

**Última actualización:** 2026-10-22 · Fernando
