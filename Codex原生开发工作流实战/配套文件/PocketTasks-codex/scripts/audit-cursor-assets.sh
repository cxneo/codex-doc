#!/usr/bin/env bash
set -euo pipefail

project_root="${1:-$(git rev-parse --show-toplevel)}"

printf 'Cursor assets under %s\n' "$project_root"
rg --files --hidden "$project_root" \
  -g '.cursor/**' \
  -g '.cursorrules' \
  -g 'AGENTS.md' \
  -g '!**/.git/**' \
  | sort || true

printf '\nCandidate rule metadata and external integrations\n'
rg -n --hidden \
  -g '.cursor/**' \
  -g '.cursorrules' \
  -g '!**/.git/**' \
  'alwaysApply|globs:|description:|mcpServers|hooks|command' \
  "$project_root" || true
