# Despliegue de ¿Qué sale?

> **Producción actual (19-nov-2026).** Corre en Docker sobre la VPS 1, no en K3s. El flujo `preparar.sh` + `desplegar.sh` de más abajo describe el diseño a futuro con Terraform y K3s y **no debe ejecutarse contra las VPS actuales**: Terraform reinstalaría los servidores. Este bloque describe lo que realmente corre.

```text
Navegador → Cloudflare (HTTPS) → túnel cloudflared (VPS 1) → Nginx 127.0.0.1:8091 → API 127.0.0.1:8081 → MySQL (red docker quesale-jwt-stage)
```

| Pieza | Dónde | Detalle |
|---|---|---|
| Frontend | `/opt/quesale-preview-20261119/html` | Lo publica el CD del repo del frontend por `rrsync` |
| Nginx | contenedor `quesale-jwt-stage-frontend` | `infra/nginx/quesale-web.conf`: frontend, `/api`, `/ws`, Swagger y las páginas `*-test.html`. El archivo está montado: tras editarlo hay que **reiniciar** el contenedor, no solo recargar |
| API | contenedor `quesale-jwt-stage-api` | Jar por SHA en `/opt/quesale-releases`, entorno en `/etc/quesale/api.env` (no está en git) |
| MySQL | contenedor `quesale-jwt-stage-mysql` | Volumen de Docker; sin alta disponibilidad |
| Despliegue | `infra/deploy/quesale-deploy-api.sh` | Se instala como `/usr/local/sbin/quesale-deploy-api` y es el único comando permitido a la llave del CD (usuario `quesale-deploy`) |

### Cómo despliega el CD del backend

`cd.yml` compila el jar y lo envía por SSH con `deploy <sha> <sha256>`. El script verifica la suma, respalda MySQL en `/var/backups/quesale/pre-<sha>.sql.gz`, reinicia la API con el jar nuevo y espera `GET /api/v1/universities` = 200 hasta 3 minutos. Si no responde, vuelve al jar anterior y termina con error. Después el workflow comprueba `PUBLIC_URL`.

Secretos del repo: `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`, `DEPLOY_KNOWN_HOSTS`; variable `PUBLIC_URL`. La imagen de GHCR se publica con las etiquetas `sha-<commit>`, `latest` y `X.Y.Z` (al empujar un tag `vX.Y.Z`), pero hoy no se usa para desplegar.

### Qué no cubre

- La rama de datos: un despliegue que cambie el esquema no se revierte con el jar; el respaldo previo permite restaurar a mano.
- Un solo servidor y una sola base. K3s en los tres nodos existe pero no aloja la aplicación.
- No hay entorno de staging separado todavía.

---

Clúster K3s de tres nodos en Contabo. Terraform administra las VPS y el DNS de Cloudflare, Ansible configura los servidores y GitHub Actions publica cada versión.

```text
preparar.sh  → genera llaves, contraseñas y archivos locales (no se suben a git)
desplegar.sh → Terraform (VPS + DNS) → inventario → Ansible (UFW, WireGuard, K3s, cert-manager, app, coturn)
CI           → pruebas y validación de infra en cada PR
CD           → imagen a GHCR y rollout en K3s al fusionar a main
```

## Carpetas

| Ruta | Qué contiene |
|---|---|
| `infra/terraform` | Proveedores `contabo/contabo` y `cloudflare/cloudflare`: 3 VPS, llaves y registros DNS |
| `infra/ansible` | `site.yml` con `base`, `wireguard`, `k3s`, `apps` y `coturn` |
| `infra/k8s` | API, MySQL, respaldo diario de MySQL y valores del entorno (Kustomize) |
| `infra/scripts` | `preparar.sh`, `desplegar.sh` y `contabo.sh` |
| `.github/workflows` | `ci.yml` y `cd.yml` |

## Lo que hace una persona (una sola vez)

1. **Comprar las 3 VPS** en el panel de Contabo: Cloud VPS, contrato de **1 mes**, región US Central, **Ubuntu 24.04** y, en la llave SSH, el contenido de `~/.ssh/id_ed25519.pub` de WSL. Así se crea la cuenta (Contabo la crea con la primera compra).
2. **Credenciales de API de Contabo**: panel del cliente → Account → Security & Access → API. Anota client id, client secret, el correo y define la contraseña de API.
3. **Dominio**: comprarlo (Namecheap, GoDaddy o el `.me` del Student Pack) y agregarlo a **Cloudflare** cambiando los nameservers en el registrador.
4. **Cloudflare**: copiar el **Zone ID** del dominio y crear un **API token** con permiso «Zone → DNS → Edit» sobre esa zona.
5. **Resend**: crear la cuenta, verificar el dominio y crear una **API key**.
6. **GitHub**: un token clásico con `read:packages` para que el clúster descargue la imagen.
7. **Primera imagen**: fusionar a `main` una vez para que el CD publique `ghcr.io/<usuario>/quesale-api`.

## Desplegar (desde WSL, en la raíz del repo)

```bash
bash infra/scripts/preparar.sh
```

Pregunta los datos de los pasos anteriores (las claves no se muestran), genera contraseñas aleatorias y crea:

- `infra/k8s/overlays/production/secrets.env`
- `infra/ansible/group_vars/all/local.yml`
- `infra/terraform/terraform.tfvars` (busca solo la imagen Ubuntu 24.04 y las 3 VPS de tu cuenta y pide confirmación)

Si un archivo ya existe, no lo toca.

```bash
bash infra/scripts/desplegar.sh
```

Corre Terraform (muestra el plan y pide `yes`), escribe el inventario, ejecuta Ansible y al final imprime la URL y los tres comandos `gh secret set` para el CD (`DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`).

Para ver la imagen y las VPS de tu cuenta sin desplegar: `bash infra/scripts/contabo.sh` con las variables `CONTABO_*` definidas.

## Verificación de correo

- Al crear un estudiante la API emite un token, guarda solo su hash y envía `APP_PUBLIC_URL/api/v1/email-verifications/confirm?token=...`. Vence a las 24 horas.
- En producción `EMAIL_VERIFICATION_REQUIRED=true`: sin verificar, 403 en conexiones, meetups, comunidades, mensajes, reportes, bloqueos y llamadas.
- Sin SMTP configurado (desarrollo) el enlace se escribe en el log.

## Llamadas

- La API solo intercambia las señales de WebRTC; el audio y el video van directos entre navegadores o por coturn en el nodo 1 (`turn.<dominio>`, puertos 3478 TCP/UDP y 49160-49200 UDP).
- `GET /api/v1/calls/ice-servers` entrega STUN y credenciales TURN temporales.
- Micrófono y cámara solo funcionan con HTTPS o en `localhost`.

## Comprobar

```bash
ssh deploy@<ip-nodo-1> "sudo k3s kubectl get nodes"
ssh deploy@<ip-nodo-1> "sudo k3s kubectl -n quesale get pods"
curl -I https://<dominio>/v3/api-docs
```

Prueba de caída: apagar un nodo desde el panel de Contabo y comprobar que `get nodes` sigue respondiendo.

Respaldos: cada día a las 03:00 (Lima) en `/var/backups/quesale` del nodo 1; se conservan 7 días.

## Límites conocidos

- La API corre con **una réplica**: el broker STOMP del chat vive en memoria.
- MySQL es una sola instancia fijada al nodo 1. Si ese nodo cae, la base no está disponible hasta recuperarlo; los respaldos están en el mismo nodo.
- Con tres registros A, un nodo caído deja de recibir tráfico solo cuando se quita su registro.
- Los tres nodos comparten región.
- Terraform guarda su estado en `infra/terraform/terraform.tfstate` local (ignorado por git): no lo borres.
- Nada de esto se ha ejecutado aún contra VPS reales; la primera corrida puede pedir ajustes.
