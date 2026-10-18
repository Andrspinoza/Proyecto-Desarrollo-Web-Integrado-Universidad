# Seguimiento — profiles (Fernando)

Solo Fernando edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | 🔨 | #5 | Base de Student y value objects; falta el perfil |
| Contrato de repositorio de Student | 🔨 Listo para revisión | Por abrir | StudentRepository implementado en la rama dev/fernando |
| Entidad JPA de Student (B2) | ✅ En main | #5 | StudentJpaEntity con FK a universities |
| Persistencia de Student y test H2 | 🔨 Listo para revisión | Por abrir | StudentJpaRepository, mapper, adapter y seis pruebas H2; pendiente de fusión |
| Capa de aplicación | ⏳ | | |
| Resources y endpoints | ⏳ | | |

**Siguiente paso:** abrir el PR del bloque de persistencia de Student. Después de fusionarlo y traer main, continuar con Profile, Interest y Skill. La capa de aplicación espera a B5 en main.

**Última actualización:** 2026-10-17 · Fernando
