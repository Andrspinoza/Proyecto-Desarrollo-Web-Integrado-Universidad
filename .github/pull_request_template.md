## Resumen

<!-- Qué agrega o corrige este PR, en 2-3 líneas. -->

**Contexto:** <!-- iam · profiles · connections · meetups · messaging · communities · moderation · subscriptions · shared · docs -->
**Capa(s):** <!-- domain · infrastructure · application · interfaces · docs -->

Closes #<!-- número del issue, si existe -->

## Commits incluidos

<!-- Uno por línea, en orden. Deben contar la construcción paso a paso. -->

1.

## Cómo probarlo

- [ ] `./mvnw -q compile`
- [ ] `./mvnw test` (dominio y persistencia con H2)
- [ ] Endpoints probados en Swagger (`/swagger-ui.html`): <!-- cuáles -->
- [ ] Colección Postman / Thunder actualizada en `docs/api/`

## Checklist

- [ ] Commits progresivos: cada uno es un cambio lógico que compila
- [ ] Sin línea `Co-Authored-By` de IA (D-08)
- [ ] Sin Lombok en `domain` ni en `application` (D-02)
- [ ] Ningún endpoint devuelve entidades de dominio ni JPA: solo resources
- [ ] No toqué el contexto de otro integrante, `pom.xml` ni `shared/` sin avisar
- [ ] Si cambió una regla o decisión, actualicé el `AGENTS.md` o la guía y su registro de cambios
- [ ] Si este PR entrega una pieza B (B0, B1, B2…), actualicé la tabla de estado del `AGENTS.md` (sección 2)
- [ ] Puedo explicar este código en la exposición
