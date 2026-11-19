# Seguimiento — infra y despliegue (André)

Solo André edita este archivo, en el último commit de cada PR.

| Bloque | Estado | PR | Notas |
|---|---|---|---|
| Configuración por variables de entorno | 🔨 | #40 | `DB_HOST`, orígenes del WebSocket y CORS para el front |
| Imagen Docker | 🔨 | #40 | `Dockerfile` en dos etapas con usuario sin privilegios |
| CI y CD | 🔨 | #40 | Pruebas y validación de infra en cada PR; imagen a GHCR y rollout en K3s al fusionar |
| Terraform | 🔨 | #40 | 3 VPS de Contabo y DNS en Cloudflare |
| Ansible | 🔨 | #40 | UFW, WireGuard, K3s con etcd embebido, cert-manager, aplicación y coturn |
| Kustomize | 🔨 | #40 | API, MySQL, respaldo diario y valores del entorno |
| Scripts | 🔨 | #40 | `preparar.sh` y `desplegar.sh` |

**Siguiente paso:** comprar las 3 VPS, crear las cuentas (Cloudflare, Resend) y correr `infra/scripts/preparar.sh` y `infra/scripts/desplegar.sh` desde WSL. Pasos en `infra/README.md`.

**Última actualización:** 2026-11-19 · André
