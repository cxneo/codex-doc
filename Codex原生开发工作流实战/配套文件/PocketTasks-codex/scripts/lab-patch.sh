#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  printf 'Usage: %s <check|apply|reverse> <19-tdd|20-review|22-migration>\n' "$0" >&2
  exit 2
fi

action="$1"
lab="$2"
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
project_root="$(cd -- "$script_dir/.." && pwd)"

case "$lab" in
  19-tdd)
    patch_path="$project_root/docs/labs/19-tdd/red-baseline.patch"
    ;;
  20-review)
    patch_path="$project_root/docs/labs/20-review/active-filter-regression.patch"
    ;;
  22-migration)
    patch_path="$project_root/docs/labs/22-migration/missing-registration.patch"
    ;;
  *)
    printf 'Unknown lab: %s\n' "$lab" >&2
    exit 2
    ;;
esac

repo_root="$(git -C "$project_root" rev-parse --show-toplevel)"
project_prefix="$(git -C "$project_root" rev-parse --show-prefix)"
directory_args=()
if [[ -n "$project_prefix" ]]; then
  directory_args+=("--directory=$project_prefix")
fi

case "$action" in
  check)
    git -C "$repo_root" apply --check "${directory_args[@]}" "$patch_path"
    ;;
  apply)
    git -C "$repo_root" apply "${directory_args[@]}" "$patch_path"
    ;;
  reverse)
    git -C "$repo_root" apply --check -R "${directory_args[@]}" "$patch_path"
    git -C "$repo_root" apply -R "${directory_args[@]}" "$patch_path"
    ;;
  *)
    printf 'Unknown action: %s\n' "$action" >&2
    exit 2
    ;;
esac

printf '%s %s patch: %s\n' "$action" "$lab" "$patch_path"
