# PocketTasks pull-request review

Review the pull-request merge diff only. Do not modify the checkout.

1. Read `AGENTS.md`, the applicable nested guidance, and the relevant file under `specs/`.
2. Prioritize correctness, data loss, lifecycle, coroutine, Compose state, Room migration, accessibility, and missing-test risks.
3. Every finding must include severity, a tight file/line location, user impact, and a reproducible failure path.
4. Do not report formatting preferences as defects.
5. If there are no actionable findings, state the files and behaviors reviewed and list any validation you could not perform.
