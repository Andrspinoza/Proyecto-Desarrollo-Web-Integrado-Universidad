#!/usr/bin/env bash
set -euo pipefail

INFRA="$(cd "$(dirname "$0")/.." && pwd)"
SECRETS_ENV="$INFRA/k8s/overlays/production/secrets.env"
LOCAL_VARS="$INFRA/ansible/group_vars/all/local.yml"
TFVARS="$INFRA/terraform/terraform.tfvars"
ADMIN_KEY="$HOME/.ssh/id_ed25519"
DEPLOY_KEY="$HOME/.ssh/quesale_deploy"

. "$INFRA/scripts/contabo.sh"

preguntar() {
  local nombre="$1" texto="$2" oculto="${3:-}"
  if [ -z "${!nombre:-}" ]; then
    if [ -n "$oculto" ]; then
      read -rsp "$texto: " "$nombre"; echo
    else
      read -rp "$texto: " "$nombre"
    fi
  fi
  [ -n "${!nombre}" ] || { echo "Falta $nombre"; exit 1; }
}

aleatorio() {
  openssl rand -hex 24
}

valor_existente() {
  [ -f "$SECRETS_ENV" ] && grep -E "^$1=" "$SECRETS_ENV" | head -1 | cut -d= -f2- || true
}

[ -f "$ADMIN_KEY" ] || ssh-keygen -t ed25519 -N "" -f "$ADMIN_KEY" -C quesale-admin
[ -f "$DEPLOY_KEY" ] || ssh-keygen -t ed25519 -N "" -f "$DEPLOY_KEY" -C quesale-deploy

preguntar DOMAIN "Dominio (ej. quesale.pe)"
preguntar LETSENCRYPT_EMAIL "Correo para Let's Encrypt"
preguntar MAIL_FROM "Remitente de los correos (dominio verificado en Resend)"
preguntar RESEND_API_KEY "API key de Resend" oculto
preguntar GHCR_USER "Usuario de GitHub"
preguntar GHCR_TOKEN "Token de GitHub con read:packages" oculto

TURN_SECRET="$(valor_existente CALLS_TURN_SECRET)"
TURN_SECRET="${TURN_SECRET:-$(aleatorio)}"

if [ -f "$SECRETS_ENV" ]; then
  echo "Ya existe $SECRETS_ENV; no se toca"
else
  DB_PASSWORD="$(aleatorio)"
  umask 077
  cat > "$SECRETS_ENV" <<EOF
DB_PASSWORD=$DB_PASSWORD
MYSQL_PASSWORD=$DB_PASSWORD
MYSQL_ROOT_PASSWORD=$(aleatorio)
SPRING_MAIL_PASSWORD=$RESEND_API_KEY
MAIL_FROM=$MAIL_FROM
APP_PUBLIC_URL=https://$DOMAIN
WS_ALLOWED_ORIGINS=https://$DOMAIN
CORS_ALLOWED_ORIGINS=https://$DOMAIN
CALLS_TURN_URLS=turn:turn.$DOMAIN:3478?transport=udp,turn:turn.$DOMAIN:3478?transport=tcp
CALLS_TURN_SECRET=$TURN_SECRET
EOF
  echo "Creado $SECRETS_ENV"
fi

if [ -f "$LOCAL_VARS" ]; then
  echo "Ya existe $LOCAL_VARS; no se toca"
else
  umask 077
  cat > "$LOCAL_VARS" <<EOF
domain: '$DOMAIN'
letsencrypt_email: '$LETSENCRYPT_EMAIL'
deploy_public_key: '$(cat "$DEPLOY_KEY.pub")'
turn_secret: '$TURN_SECRET'
ghcr_user: '$GHCR_USER'
ghcr_token: '$GHCR_TOKEN'
EOF
  echo "Creado $LOCAL_VARS"
fi

if [ -f "$TFVARS" ]; then
  echo "Ya existe $TFVARS; no se toca"
else
  preguntar CONTABO_CLIENT_ID "Contabo client id"
  preguntar CONTABO_CLIENT_SECRET "Contabo client secret" oculto
  preguntar CONTABO_USER "Contabo usuario de API (correo)"
  preguntar CONTABO_PASSWORD "Contabo contraseña de API" oculto
  preguntar CLOUDFLARE_API_TOKEN "Token de Cloudflare (Zone.DNS edit)" oculto
  preguntar CLOUDFLARE_ZONE_ID "Zone ID de Cloudflare"

  CONTABO_TOKEN="$(contabo_token)"
  IMAGE_ID="${IMAGE_ID:-$(contabo_ubuntu_image)}"
  echo "Imagen Ubuntu 24.04: $IMAGE_ID"
  echo "VPS de la cuenta (id, ip, plan, región, estado):"
  contabo_instances | tee /tmp/quesale-instancias.txt
  IDS="$(cut -f1 /tmp/quesale-instancias.txt | head -3 | sed 's/.*/"&"/' | paste -sd, -)"
  rm -f /tmp/quesale-instancias.txt
  echo "Se usarán estas VPS (en orden): [$IDS]. Terraform las reinstala con Ubuntu 24.04 y tu llave."
  read -rp "¿Continuar? (s/n): " ok
  [ "$ok" = "s" ] || { echo "Cancelado"; exit 1; }

  umask 077
  cat > "$TFVARS" <<EOF
contabo_client_id     = "$CONTABO_CLIENT_ID"
contabo_client_secret = "$CONTABO_CLIENT_SECRET"
contabo_user          = "$CONTABO_USER"
contabo_password      = "$CONTABO_PASSWORD"
ssh_public_key        = "$(cat "$ADMIN_KEY.pub")"
root_password         = "$(aleatorio)Aa1"
image_id              = "$IMAGE_ID"
existing_instance_ids = [$IDS]
domain                = "$DOMAIN"
cloudflare_api_token  = "$CLOUDFLARE_API_TOKEN"
cloudflare_zone_id    = "$CLOUDFLARE_ZONE_ID"
EOF
  echo "Creado $TFVARS"
fi

echo
echo "Listo. Siguiente paso: bash infra/scripts/desplegar.sh"
