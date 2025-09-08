#!/usr/bin/env bash
# Compatibility shim: some environments referenced post-create-alpine.sh.
# Delegate to the unified wrapper so we don't duplicate logic.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec bash "${SCRIPT_DIR}/post-create-wrapper.sh"
