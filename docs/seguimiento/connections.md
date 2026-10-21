# Seguimiento — connections (Ubes)

Solo Ubes edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #18 | `Connection`, estados, eventos, excepciones y pruebas fusionados |
| Contratos | ✅ | #18 | Repositorio de dominio y fachada del contexto fusionados |
| Persistencia y test H2 | 🔨 | #28 | Adapter JPA con FK a estudiantes y pruebas H2 |
| Capa de aplicación | 🔨 | #28 | Handlers CQRS, DTOs y fachada `ConnectionsContextFacade` |
| Resources, assemblers, controllers, pruebas HTTP y Postman | 🔨 | #28 | Crear, listar, aceptar, rechazar y cancelar; incluye solicitudes recibidas y enviadas |

**Siguiente paso:** esperar revisión y fusión del PR #28. Después, completar `00-setup` cuando estén disponibles los endpoints de cuentas y estudiantes de `iam` y `profiles`.

**Última actualización:** 2026-10-21 · Ubes
