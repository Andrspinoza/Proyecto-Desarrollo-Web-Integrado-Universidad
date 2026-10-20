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
| Implementación provisional de fachada | Listo para revisión | Por abrir | Bean ProfilesContextFacadeImpl y cinco pruebas; consulta estudiantes y universidades. studentIdOfAccount devuelve vacío hasta que existan Account y su vínculo con Student |
| Afinidad y tests | ⏳ | | AffinityCalculator y AffinityScore pendientes |
| Capa de aplicación | ⏳ | | |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** abrir el PR de la implementación provisional de la fachada y avisar a André y Jesús de su alcance. Después de su fusión y de incorporar main, continuar con commands, queries, DTOs, assemblers y handlers de profiles siguiendo iam (B5). La resolución por cuenta requiere Account y su vínculo real con Student; no se equiparan sus IDs. La afinidad sigue pendiente como bloque independiente.

**Última actualización:** 2026-10-19 · Fernando
