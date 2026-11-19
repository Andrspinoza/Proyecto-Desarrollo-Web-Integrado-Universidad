#!/usr/bin/env bash
set -euo pipefail

contabo_token() {
  curl -fsS https://auth.contabo.com/auth/realms/contabo/protocol/openid-connect/token \
    --data-urlencode "client_id=$CONTABO_CLIENT_ID" \
    --data-urlencode "client_secret=$CONTABO_CLIENT_SECRET" \
    --data-urlencode "username=$CONTABO_USER" \
    --data-urlencode "password=$CONTABO_PASSWORD" \
    --data-urlencode "grant_type=password" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])'
}

contabo_get() {
  curl -fsS "https://api.contabo.com$1" \
    -H "Authorization: Bearer $CONTABO_TOKEN" \
    -H "x-request-id: $(python3 -c 'import uuid; print(uuid.uuid4())')"
}

contabo_ubuntu_image() {
  contabo_get "/v1/compute/images?standardImage=true&size=100" | python3 -c '
import json, sys
images = json.load(sys.stdin)["data"]
match = [i for i in images if "ubuntu" in i["name"].lower() and "24.04" in i["name"]]
if not match:
    sys.exit("No hay imagen Ubuntu 24.04 entre las imágenes estándar")
print(match[0]["imageId"])
'
}

contabo_instances() {
  contabo_get "/v1/compute/instances?size=100" | python3 -c '
import json, sys
for i in sorted(json.load(sys.stdin)["data"], key=lambda x: x["instanceId"]):
    ip = (i.get("ipConfig") or {}).get("v4", {}).get("ip", "-")
    print(i["instanceId"], ip, i.get("productId", "-"), i.get("region", "-"), i.get("status", "-"), sep="\t")
'
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  : "${CONTABO_CLIENT_ID:?Define CONTABO_CLIENT_ID}"
  : "${CONTABO_CLIENT_SECRET:?Define CONTABO_CLIENT_SECRET}"
  : "${CONTABO_USER:?Define CONTABO_USER}"
  : "${CONTABO_PASSWORD:?Define CONTABO_PASSWORD}"
  CONTABO_TOKEN="$(contabo_token)"
  echo "Imagen Ubuntu 24.04: $(contabo_ubuntu_image)"
  echo "VPS de la cuenta (id, ip, plan, región, estado):"
  contabo_instances
fi
