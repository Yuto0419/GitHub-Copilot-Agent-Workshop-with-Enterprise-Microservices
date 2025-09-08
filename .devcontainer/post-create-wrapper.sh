#!/usr/bin/env bash
set -euo pipefail

echo "[postCreate] Starting robust lookup..."
ROOT="${WORKSPACE_FOLDER:-$(pwd)}"
echo "[postCreate] Initial ROOT=${ROOT}"

if [ ! -f "${ROOT}/.devcontainer/post-create.sh" ]; then
  if [ -d "${ROOT}/java-skishop-microservices/.devcontainer" ] \
     && [ -f "${ROOT}/java-skishop-microservices/.devcontainer/post-create.sh" ]; then
    ROOT="${ROOT}/java-skishop-microservices"
    echo "[postCreate] Adjusted ROOT to nested repo path: ${ROOT}"
  fi
fi

SCRIPT="${ROOT}/.devcontainer/post-create.sh"
if [ -f "${SCRIPT}" ]; then
  echo "[postCreate] Executing ${SCRIPT}";
  chmod +x "${ROOT}/.devcontainer"/*.sh 2>/dev/null || true
  bash "${SCRIPT}"
else
  echo "[postCreate] WARNING: Script not found at ${SCRIPT}. Skipping custom post-create.";
fi

echo "[postCreate] Wrapper finished."
