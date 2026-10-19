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
| Persistencia de perfil y tests H2 | Listo para revisión | Por abrir | ProfileJpaEntity, relación con Student, mapper y adapter para el perfil completo; seis pruebas H2 adicionales |
| Afinidad y tests | ⏳ | | AffinityCalculator y AffinityScore pendientes |
| Capa de aplicación | ⏳ | | |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** abrir el PR de persistencia de Profile. Después de su fusión y de incorporar main, implementar la capa de aplicación de profiles tomando iam como modelo (B5 ya está en main). La afinidad sigue pendiente como bloque independiente.

**Última actualización:** 2026-10-19 · Fernando
