# PocketTasks Codex 驾驶舱

这是课程配套的“项目工程化层”，不包含完整 Android 业务源码。请在读完第 16 讲后，把需要的文件复制到一个真实的 Kotlin / Compose / Room 项目，再按项目实际模块、包名、命令与政策修改。

建议先采用：

1. `AGENTS.md` 与 `docs/constitution.md`；
2. `specs/000-template/`；
3. 验证三条 Gradle 命令；
4. 审查并启用 `.codex` 下的安全配置；
5. 用真实 diff 验证 `android-code-review` Skill。

重要说明：

- `.codex/hooks/pre_tool_use.py` 是会自动运行的代码，启用前必须自行审查；
- `.codex/config.toml` 采用保守示例，网络默认关闭；
- `:app:` 是示例模块名，必须按你的工程调整；
- CI 文件以 `.example.yml` 结尾，不会自动成为 GitHub Workflow；
- 课程没有放置 API Key、签名材料或任何真实凭据。

对应课程入口：[`../../README.md`](../../README.md)。

