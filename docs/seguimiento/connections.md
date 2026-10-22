# Seguimiento — connections (Ubes)

Solo Ubes edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #18 | `Connection`, estados, eventos, excepciones y pruebas fusionados |
| Contratos | ✅ | #18 | Repositorio de dominio y fachada del contexto fusionados |
| Persistencia y test H2 | ✅ | #28 | Adapter JPA con FK a estudiantes y pruebas H2 |
| Capa de aplicación | ✅ | #28 | Handlers CQRS, DTOs y fachada `ConnectionsContextFacade` |
| Resources, assemblers, controllers, pruebas HTTP y Postman | ✅ | #28 | Crear, listar, aceptar, rechazar y cancelar; incluye solicitudes recibidas y enviadas |
| Colección `00-setup` | 🔨 | #37 | UTP y UPC, tres estudiantes UTP y uno UPC, intereses y habilidades; las 12 peticiones respondieron 201 en la API local. También se probaron en Swagger los POST de universidades, estudiantes, intereses y habilidades; todos respondieron 201. Sin Account |

**Siguiente paso:** esperar la revisión y fusión del PR #37. La colección se ejecutó de arriba abajo contra la API y sus tests guardan los IDs; los intereses y habilidades usan `X-Student-Id` de Camila. Account queda fuera de esta entrega.

**Última actualización:** 2026-10-22 · Ubes
