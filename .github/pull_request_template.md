## Resumen

Qué agrega o corrige este PR, en 2-3 líneas.

**Contexto:**
**Capa(s):**

Closes #

## Commits incluidos

1.

## Cómo probarlo

- [ ] `./mvnw -q compile`
- [ ] `./mvnw test` (dominio y persistencia con H2)
- [ ] Endpoints probados en Swagger (`/swagger-ui.html`)
- [ ] Colección Postman / Thunder actualizada en `docs/api/`

## Checklist

- [ ] Commits progresivos: cada uno es un cambio lógico que compila
- [ ] Sin Lombok en `domain` ni en `application` (D-02)
- [ ] Ningún endpoint devuelve entidades de dominio ni JPA: solo resources
- [ ] Solo modifiqué archivos de mi parte; lo que no es mío lo avisé antes a su dueño
- [ ] Si cambió una regla o decisión, actualicé el `AGENTS.md` o la guía y su registro de cambios
- [ ] Si este PR entrega una pieza B (B0, B1, B2…), actualicé la tabla de estado del `AGENTS.md` (sección 2)
- [ ] Puedo explicar este código en la exposición
