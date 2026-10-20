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
| Contrato de fachada de profiles (B3) | Listo para revisión | Por abrir | ProfilesContextFacade en interfaces/acl, con las tres firmas de la guía 6.3; implementación pendiente |
| Afinidad y tests | ⏳ | | AffinityCalculator y AffinityScore pendientes |
| Capa de aplicación | ⏳ | | |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** abrir el PR pequeño del contrato ProfilesContextFacade y avisar a André y Jesús. Después de su fusión y de incorporar main, retomar la capa de aplicación de profiles siguiendo iam (B5), incluida la implementación de la fachada. El contrato permite programar a los consumidores; su uso en ejecución requiere una implementación. La afinidad sigue pendiente como bloque independiente.

**Última actualización:** 2026-10-19 · Fernando
