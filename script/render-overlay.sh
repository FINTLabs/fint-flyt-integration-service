#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE_DIR="$ROOT/kustomize/templates"
BASE_TEMPLATE="$TEMPLATE_DIR/overlay.yaml.tpl"

while IFS= read -r file; do
  rel="${file#"$ROOT/kustomize/overlays/"}"
  dir="$(dirname "$rel")"

  namespace="${dir%%/*}"
  env_name="${dir#*/}"
  if [[ "$env_name" == "$namespace" ]]; then
    env_name=""
  fi

  org_id="${namespace//-/.}"
  app_instance="fint-flyt-integration-service_${namespace//-/_}"
  kafka_topic="${namespace}.flyt.*"

  path_prefix=""
  if [[ -n "$env_name" && "$env_name" != "api" ]]; then
    path_prefix="${env_name}/"
  fi

  base_path="/${path_prefix}${namespace}"
  ingress_base_path="${base_path}/api/intern/integrasjoner"
  startup_path="${base_path}/actuator/health"
  readiness_path="${base_path}/actuator/health/readiness"
  liveness_path="${base_path}/actuator/health/liveness"
  metrics_path="${base_path}/actuator/prometheus"

  role_map_json="$(cat <<EOF
  {
    "$org_id":["USER"],
    "vigo.no":["DEVELOPER","USER"],
    "novari.no":["DEVELOPER","USER"]
  }
EOF
)"

  case "$namespace" in
    afk-no|bfk-no|ofk-no)
      role_map_json="$(cat <<EOF
  {
    "$org_id":["USER"],
    "viken.no":["USER"],
    "frid-iks.no":["USER"],
    "vigo.no":["DEVELOPER","USER"],
    "novari.no":["DEVELOPER","USER"]
  }
EOF
)"
      ;;
  esac

  role_map_lines="$(printf '%s\n' "$role_map_json" | sed 's/^/          /')"
  ROLE_MAP=$'\n'"$role_map_lines"

  export NAMESPACE="$namespace"
  export ORG_ID="$org_id"
  export APP_INSTANCE="$app_instance"
  export KAFKA_TOPIC="$kafka_topic"
  export URL_BASE_PATH="$base_path"
  export INGRESS_BASE_PATH="$ingress_base_path"
  export STARTUP_PATH="$startup_path"
  export READINESS_PATH="$readiness_path"
  export LIVENESS_PATH="$liveness_path"
  export METRICS_PATH="$metrics_path"
  export ROLE_MAP
  export FINT_KAFKA_TOPIC_ORGID="$namespace"

  tmp="$(mktemp)"
  envsubst < "$BASE_TEMPLATE" > "$tmp"
  mv "$tmp" "$file"
done < <(find "$ROOT/kustomize/overlays" -name kustomization.yaml -print | sort)
