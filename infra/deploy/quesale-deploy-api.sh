#!/usr/bin/env bash
set -euo pipefail

RELEASES=/opt/quesale-releases
ENV_FILE=/etc/quesale/api.env
BACKUPS=/var/backups/quesale
NETWORK=quesale-jwt-stage
API=quesale-jwt-stage-api
MYSQL=quesale-jwt-stage-mysql
PORT=8081
KEEP=5

read -r command sha checksum extra <<<"${SSH_ORIGINAL_COMMAND:-}"
if [[ "${command:-}" != deploy || ! "${sha:-}" =~ ^[0-9a-f]{40}$ || ! "${checksum:-}" =~ ^[0-9a-f]{64}$ || -n "${extra:-}" ]]; then
  echo "Uso: deploy <sha-del-commit> <sha256-del-jar>" >&2
  exit 2
fi

exec 9>/var/lock/quesale-deploy.lock
flock -n 9 || { echo "Hay otro despliegue en curso" >&2; exit 3; }

mkdir -p "$RELEASES" "$BACKUPS"
jar="$RELEASES/app-$sha.jar"
cat > "$jar.part"
echo "$checksum  $jar.part" | sha256sum -c - >/dev/null
mv "$jar.part" "$jar"

docker exec "$MYSQL" sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers --databases quesale' 2>/dev/null | gzip > "$BACKUPS/pre-$sha.sql.gz"
gzip -t "$BACKUPS/pre-$sha.sql.gz"

previous="$(readlink -f "$RELEASES/current" 2>/dev/null || true)"

start() {
  docker rm -f "$API" >/dev/null 2>&1 || true
  docker run -d --name "$API" --restart unless-stopped --network "$NETWORK" \
    -p "127.0.0.1:$PORT:8080" --env-file "$ENV_FILE" -v "$1:/app.jar:ro" \
    eclipse-temurin:21-jre java -jar /app.jar >/dev/null
}

healthy() {
  for _ in $(seq 1 60); do
    code="$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:$PORT/api/v1/universities" || true)"
    [ "$code" = 200 ] && return 0
    sleep 3
  done
  return 1
}

start "$jar"
if healthy; then
  ln -sfn "$jar" "$RELEASES/current.new"
  mv -T "$RELEASES/current.new" "$RELEASES/current"
  ls -1t "$RELEASES"/app-*.jar | tail -n +$((KEEP + 1)) | xargs -r rm -f
  echo "Desplegado $sha"
  exit 0
fi

docker logs --tail 40 "$API" >&2 || true
if [ -n "$previous" ] && [ -f "$previous" ]; then
  start "$previous"
  if healthy; then echo "Revertido a $(basename "$previous")" >&2; else echo "La version anterior tampoco responde" >&2; fi
fi
exit 1
