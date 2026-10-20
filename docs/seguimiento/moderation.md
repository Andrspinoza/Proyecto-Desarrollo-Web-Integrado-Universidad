# Seguimiento — moderation (Ubes)

Solo Ubes edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Dominio y tests | ✅ | #7 | Report, Block y sus pruebas fusionados |
| Contrato de repositorio | ✅ | #7 | Repositorios de dominio y fachada fusionados |
| Persistencia y test H2 | 🔨 | Pendiente | Adapters y pruebas H2 incluidos en el PR separado de moderation |
| Capa de aplicación | 🔨 | Pendiente | Handlers CQRS y pruebas unitarias incluidos en el PR separado |
| Resources y endpoints | 🔨 | Pendiente | Controllers conectados a handlers, pruebas HTTP y colección Postman incluidos en el PR separado |

**Siguiente paso:** abrir el PR de moderation hacia `main`; connections se gestiona en su PR independiente.

**Última actualización:** 2026-10-19 · Ubes
