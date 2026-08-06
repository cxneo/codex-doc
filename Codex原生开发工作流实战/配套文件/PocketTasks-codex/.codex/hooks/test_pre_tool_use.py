#!/usr/bin/env python3
"""Black-box tests for the teaching PreToolUse hook."""

from __future__ import annotations

import json
import subprocess
import unittest
from pathlib import Path


HOOK = Path(__file__).with_name("pre_tool_use.py")


def invoke(command: str, tool_name: str = "Bash") -> dict[str, object] | None:
    completed = subprocess.run(
        ["python3", str(HOOK)],
        input=json.dumps(
            {
                "tool_name": tool_name,
                "tool_input": {"command": command},
            }
        ),
        text=True,
        capture_output=True,
        check=True,
    )
    return json.loads(completed.stdout) if completed.stdout else None


class PreToolUseHookTest(unittest.TestCase):
    def test_allows_narrow_android_unit_test(self) -> None:
        self.assertIsNone(invoke("./gradlew :app:testDebugUnitTest"))

    def test_denies_device_data_clear(self) -> None:
        result = invoke("adb shell pm clear com.example.pockettasks")
        output = result["hookSpecificOutput"]  # type: ignore[index]
        self.assertEqual("deny", output["permissionDecision"])
        self.assertIn("data deletion", output["permissionDecisionReason"])

    def test_denies_gradle_publish_task(self) -> None:
        result = invoke("./gradlew :app:publishRelease")
        output = result["hookSpecificOutput"]  # type: ignore[index]
        self.assertEqual("deny", output["permissionDecision"])

    def test_ignores_other_tools(self) -> None:
        self.assertIsNone(invoke("adb shell pm clear com.example.pockettasks", "Read"))

    def test_malformed_json_fails_closed(self) -> None:
        completed = subprocess.run(
            ["python3", str(HOOK)],
            input="not-json",
            text=True,
            capture_output=True,
            check=False,
        )
        self.assertEqual(2, completed.returncode)
        self.assertIn("not valid JSON", completed.stderr)


if __name__ == "__main__":
    unittest.main()
