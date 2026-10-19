# Seguimiento — profiles (Fernando)

Solo Fernando edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio base de Student y tests | ✅ En main | #5 | Student y value objects iniciales |
| Dominio de perfil y catálogos con tests | ✅ En main | #10 | Career, Visibility, Profile, integración en Student, Interest y Skill |
| Contrato de repositorio de Student | ✅ En main | #9 | StudentRepository |
| Entidad JPA de Student (B2) | ✅ En main | #5 | StudentJpaEntity con FK a universities |
| Persistencia de Student y test H2 | ✅ En main | #9 | StudentJpaRepository, mapper, adapter y seis pruebas H2 |
| Persistencia de perfil y catálogos con tests H2 | ⏳ | | Pendiente; el mapper actual de Student todavía no guarda el perfil |
| Afinidad y tests | ⏳ | | AffinityCalculator y AffinityScore pendientes |
| Capa de aplicación | ⏳ | | |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** implementar la persistencia de Profile, Interest y Skill con tests H2. El PR #10 está fusionado y main ya fue incorporado. B5 está en main: después de terminar la persistencia, implementar la capa de aplicación tomando iam como modelo.

**Última actualización:** 2026-10-19 · Fernando
