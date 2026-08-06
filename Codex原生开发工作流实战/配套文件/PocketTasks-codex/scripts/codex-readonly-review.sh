#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "$script_dir/.." && pwd)"
output_path="${1:-$project_root/build/reports/codex/review.json}"
mkdir -p "$(dirname "$output_path")"

cd "$project_root"

codex exec \
  --ephemeral \
  --ignore-user-config \
  --sandbox read-only \
  --output-schema "$project_root/.github/codex/review-schema.json" \
  --output-last-message "$output_path" \
  - <<'PROMPT'
Read AGENTS.md, docs/constitution.md, and the current working-tree diff.
Perform a read-only Android review. Do not edit files.
Report only evidence-backed correctness, data-safety, lifecycle,
accessibility, or verification findings. Record checks actually run and
residual risks. Return exactly the JSON shape required by the output schema.
PROMPT

printf 'Codex review written to %s\n' "$output_path"
