#!/usr/bin/env bash
set -euo pipefail

INFRA="$(cd "$(dirname "$0")/.." && pwd)"
TF="$INFRA/terraform"
ANS="$INFRA/ansible"

for archivo in "$TF/terraform.tfvars" "$ANS/group_vars/all/local.yml" "$INFRA/k8s/overlays/production/secrets.env"; do
  [ -f "$archivo" ] || { echo "Falta $archivo: ejecuta primero bash infra/scripts/preparar.sh"; exit 1; }
done

for comando in terraform ansible-playbook ssh; do
  command -v "$comando" >/dev/null || { echo "Falta $comando en este entorno (usa WSL)"; exit 1; }
done

terraform -chdir="$TF" init -input=false
terraform -chdir="$TF" apply
terraform -chdir="$TF" output -raw ansible_inventory > "$ANS/inventory.ini"

cd "$ANS"
ANSIBLE_CONFIG="$ANS/ansible.cfg" ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i inventory.ini site.yml

HOST="$(terraform -chdir="$TF" output -raw deploy_host)"
URL="$(terraform -chdir="$TF" output -raw app_url)"

echo
echo "Clúster listo. API: $URL/swagger-ui.html"
echo "Secretos del CD en GitHub (ejecútalos desde Windows, donde tienes gh):"
echo "  gh secret set DEPLOY_HOST --body $HOST"
echo "  gh secret set DEPLOY_USER --body deploy"
echo "  gh secret set DEPLOY_SSH_KEY < \\\\wsl\$\\${WSL_DISTRO_NAME:-Ubuntu}\\home\\$USER\\.ssh\\quesale_deploy"
