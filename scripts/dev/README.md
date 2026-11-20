# Desarrollo local

Levantar la API con H2 en memoria, sin MySQL (H2 está en el classpath de pruebas):

```bash
SPRING_DATASOURCE_URL='jdbc:h2:mem:dev;MODE=MySQL;DB_CLOSE_DELAY=-1' SPRING_DATASOURCE_USERNAME=sa SPRING_DATASOURCE_PASSWORD= \
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop SEED_UNIVERSITIES=true \
JWT_SECRET='una-clave-local-de-al-menos-32-caracteres' CORS_ALLOWED_ORIGINS=http://localhost:4200 WS_ALLOWED_ORIGINS=http://localhost:4200 \
APP_PUBLIC_URL=http://localhost:4200 sh ./mvnw spring-boot:run -Dspring-boot.run.useTestClasspath=true > backend-dev.log
```

Con `SEED_UNIVERSITIES=true` se crean las 7 universidades, 25 intereses y 10 habilidades.

Crear cuatro estudiantes de ejemplo con perfil (la clave de prueba está en el script, solo para local):

```bash
BACKEND_LOG=backend-dev.log node scripts/dev/sembrar-estudiantes.mjs
```

El script confirma los correos leyendo el enlace de verificación del log, porque sin SMTP el enlace se escribe ahí.
