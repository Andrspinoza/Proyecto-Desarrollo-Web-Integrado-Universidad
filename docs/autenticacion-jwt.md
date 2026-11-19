# Autenticación JWT: preparación y límites

El backend exige `JWT_SECRET` con al menos 32 bytes cuando `AUTH_ENABLED` es verdadero (predeterminado). La clave debe generarse aleatoriamente y mantenerse fuera de Git y de los registros. El token dura una hora y usa HS256. El login requiere una verificación de correo confirmada, independientemente de `EMAIL_VERIFICATION_REQUIRED`.

`POST /api/v1/auth/register`: `universityId`, `email`, `password`, `firstName`, `lastName`; devuelve `studentId` y un mensaje. `POST /api/v1/auth/login`: `email`, `password`; devuelve `accessToken`, `tokenType`, `expiresIn` en segundos y `studentId`. Las API protegidas reciben `Authorization: Bearer <accessToken>`. El servidor reemplaza cualquier `X-Student-Id` suministrado por el cliente con el sujeto del token. Los endpoints administrativos provisionales están deshabilitados; no se ha implementado rol administrador.

En WebSocket la identidad se valida en el frame STOMP CONNECT con `Authorization: Bearer <accessToken>`, no con el parámetro `studentId`. No se permiten suscripciones directas a `/queue`; el cliente usa `/user/queue/`.

`POST /api/v1/auth/resend-verification` acepta `email` y `password` para reenviar la verificación sin sesión. Antes de desplegar hay que configurar y probar SMTP, remitente y `APP_PUBLIC_URL` HTTPS, actualizar los orígenes permitidos de HTTP y WebSocket, y probar transacciones y actualizaciones sobre una copia de MySQL. Las contraseñas existentes no se migran desde ninguna otra aplicación. La desactivación `AUTH_ENABLED=false` existe exclusivamente para ejecutar pruebas heredadas: jamás utilizarla en un entorno público.

Las pruebas heredadas se ejecutan con la autenticación provisional deshabilitada para conservar sus contratos; las pruebas nuevas verifican el filtro autenticado de forma directa. Todavía se requieren pruebas de extremo a extremo con WebSocket, SMTP real y dos cuentas, límites contra fuerza bruta, recuperación de contraseña y auditoría antes de admitir datos reales.
