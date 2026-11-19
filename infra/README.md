# Despliegue de ¿Qué sale?

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
