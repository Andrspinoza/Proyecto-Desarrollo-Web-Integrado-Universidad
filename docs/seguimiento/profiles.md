# Seguimiento — profiles (Fernando)

Solo Fernando edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio base de Student y tests | ✅ En main | #5 | Student y value objects iniciales |
| Dominio de perfil y catálogos con tests | Listo para revisión | Por abrir | Career, Visibility, Profile, integración en Student, Interest y Skill |
| Contrato de repositorio de Student | ✅ En main | #9 | StudentRepository |
| Entidad JPA de Student (B2) | ✅ En main | #5 | StudentJpaEntity con FK a universities |
| Persistencia de Student y test H2 | ✅ En main | #9 | StudentJpaRepository, mapper, adapter y seis pruebas H2 |
| Persistencia de perfil y catálogos con tests H2 | ⏳ | | Pendiente; el mapper actual de Student todavía no guarda el perfil |
| Afinidad y tests | ⏳ | | AffinityCalculator y AffinityScore pendientes |
| Capa de aplicación | ⏳ | | |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** abrir el PR del dominio de perfil y catálogos. Después de su fusión y de incorporar main, continuar con su persistencia y tests H2. La capa de aplicación espera a B5 en main.

**Última actualización:** 2026-10-18 · Fernando
