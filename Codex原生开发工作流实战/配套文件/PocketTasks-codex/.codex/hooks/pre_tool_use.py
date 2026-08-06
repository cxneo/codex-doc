#!/usr/bin/env python3
"""Block a few high-risk Android/project commands before Bash runs.

This is an intentionally small teaching example. Prefer Codex Rules for stable
command prefixes; keep this hook for checks that need to inspect the command.
"""

from __future__ import annotations

import json
import re
import sys


POLICIES = (
    (
        re.compile(r"\badb\s+shell\s+pm\s+clear\b", re.IGNORECASE),
        "Blocked automatic app-data deletion. Confirm the device and package, then run it manually.",
    ),
    (
        re.compile(r"(?:^|\s)(?:\./)?gradlew\s+[^\n]*(?:publish|upload)\w*", re.IGNORECASE),
        "Blocked an automatic Gradle publish/upload task. Use the protected release workflow.",
    ),
    (
        re.compile(
            r"\b(?:cat|head|tail|sed)\b[^\n]*(?:local\.properties|\.jks\b|\.keystore\b)",
            re.IGNORECASE,
        ),
        "Blocked reading a likely secret-bearing Android configuration file.",
    ),
)


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except (json.JSONDecodeError, OSError):
        print("Blocked Bash tool call because the hook input was not valid JSON.", file=sys.stderr)
        return 2

    if payload.get("tool_name") != "Bash":
        return 0

    tool_input = payload.get("tool_input") or {}
    if not isinstance(tool_input, dict):
        print("Blocked Bash tool call because tool_input was not an object.", file=sys.stderr)
        return 2

    command = tool_input.get("command", "")
    if not isinstance(command, str) or not command.strip():
        print("Blocked Bash tool call because tool_input.command was missing.", file=sys.stderr)
        return 2

    for pattern, reason in POLICIES:
        if pattern.search(command):
            print(
                json.dumps(
                    {
                        "hookSpecificOutput": {
                            "hookEventName": "PreToolUse",
                            "permissionDecision": "deny",
                            "permissionDecisionReason": reason,
                        }
                    }
                )
            )
            return 0

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
