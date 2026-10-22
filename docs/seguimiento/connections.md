# Seguimiento — connections (Ubes)

Solo Ubes edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #18 | `Connection`, estados, eventos, excepciones y pruebas fusionados |
| Contratos | ✅ | #18 | Repositorio de dominio y fachada del contexto fusionados |
| Persistencia y test H2 | ✅ | #28 | Adapter JPA con FK a estudiantes y pruebas H2 |
| Capa de aplicación | ✅ | #28 | Handlers CQRS, DTOs y fachada `ConnectionsContextFacade` |
| Resources, assemblers, controllers, pruebas HTTP y Postman | ✅ | #28 | Crear, listar, aceptar, rechazar y cancelar; incluye solicitudes recibidas y enviadas |
| Colección `00-setup` | 🔨 | — | Crear UTP y UPC, tres estudiantes UTP y uno UPC, intereses y habilidades; sin Account |

**Siguiente paso:** ejecutar `docs/api/00-setup.postman_collection.json` de arriba abajo contra la API y comprobar que los tests guardan los IDs. Los intereses y habilidades se crean con `X-Student-Id` de Camila. Account queda fuera de esta entrega.

**Última actualización:** 2026-10-22 · Ubes
