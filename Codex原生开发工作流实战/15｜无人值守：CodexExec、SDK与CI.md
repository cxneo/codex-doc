# 15｜无人值守：Codex Exec、SDK 与 CI

前面所有场景都有一个人在会话旁边，可以补充问题、批准命令、纠正方向。CI 中没有这种条件：输入必须完整，权限必须预先收紧，输出必须让机器和人都能判断。

`codex exec` 把 Codex 变成非交互执行入口。它不是“把聊天命令换个名字”，而是一种更严格的工作方式。

## 无人值守的三个变化

第一，不能依赖临场追问。任务必须提前写清目标、范围、允许动作和失败方式。

第二，默认倾向更安全。`codex exec` 默认使用只读沙箱；确实需要改工作区时，显式使用 `--sandbox workspace-write`。

第三，输出要结构化。人类可以读一段散文，流水线需要稳定字段、退出状态和可保存产物。

## 从只读审查开始

最适合进入 CI 的第一项 Codex 能力不是自动改代码，而是只读评审：

```bash
codex exec \
  "使用 \$android-code-review 审查当前分支相对 main 的 Android 改动。不要修改文件；按严重程度输出发现，并列出验证缺口。"
```

因为默认只读，即使任务描述有歧义，也不会直接改变仓库。先观察它在真实 PR 上的准确性和噪音，再决定是否作为必过检查。

## JSONL 与结果 Schema

加上 `--json` 后，Codex 会输出 JSON Lines 事件流，适合保存完整执行轨迹：

```bash
codex exec --json "调查当前 diff 的 Android 回归风险，不修改文件" \
  > codex-events.jsonl
```

如果下游只需要最终结构，可以通过 `--output-schema` 指定 JSON Schema，并用 `-o` 保存最终消息。例如要求结果包含 `summary`、`findings`、`tests_run` 与 `tests_missing`。

结构化输出的好处不是“更像 API”，而是可以做确定性判断：存在 P0/P1 时阻断，只有建议项时继续，把未运行设备测试清楚展示给评审者。

## 自动修复为何需要更窄的合同

当任务需要写文件时：

```bash
codex exec --sandbox workspace-write \
  "只修复指定 Lint 问题；不要修改基线或禁用规则。运行目标 Lint，输出改动与结果。"
```

写权限只是物理允许，并没有授权推送、发布或访问生产。任务仍应限制问题 ID、文件范围、验证命令和停止条件。遇到需要业务判断、数据库迁移或公开 API 改变时，应失败退出并交给人，而不是猜一个答案。

## Android CI 的现实分层

不是所有验证都放在同一个 Job：

```text
快速 Job
  ├── testDebugUnitTest
  ├── lintDebug
  └── Codex 只读 diff 审查

构建 Job
  └── assembleDebug / bundleRelease（按分支与密钥策略）

设备 Job
  └── connectedDebugAndroidTest 或托管设备
```

Codex 可以解释各 Job 的失败和归类风险，但不能把未启动设备导致的跳过写成测试通过。流水线摘要必须区分 passed、failed、skipped 与 not configured。

## GitHub Action 与 SDK 的位置

在 GitHub Actions 中，可以使用官方 `openai/codex-action` 把任务接入 PR 流程。凭据通过 GitHub Secrets 提供，权限限定到所需范围，来自 Fork 的不受信任代码要特别谨慎。

如果你需要在内部工具中持续管理线程、处理事件或把 Codex 嵌入更复杂的应用，可以使用 Codex SDK。选择原则很简单：Shell 能清楚表达的单次任务用 `codex exec`；需要程序化会话与深度集成时再使用 SDK。

## 一份可以审计的 CI 合同

在上线前，让团队逐项回答：

- 输入是否来自不受信任的 PR 文本或文件？
- 沙箱和网络权限是什么？
- Codex 能否读取 Secrets，是否真的需要？
- 哪些外部工具可调用，写操作怎样禁止？
- 输出在哪里保存，是否可能含敏感内容？
- 超时、失败和限额怎样处理？
- AI 发现是否直接阻断，还是先以建议运行？

建议先“影子运行”一段时间：结果对评审者可见，但不阻断合并。统计真实发现、误报和耗时后，再决定门禁策略。

## 小结

非交互执行要求任务更明确、权限更小、输出更结构化。`codex exec` 适合一次性自动化，SDK 适合程序化集成，官方 Action 适合 GitHub 工作流。先从只读、非阻断检查开始，再根据证据扩大自动化。

至此，我们已经认识了 Codex 的主要零件。下一讲进入课程的转折点：把项目规则、安全机制、专家能力和 SDD 模板装进 PocketTasks 的同一套“开发驾驶舱”。

## 思考题

1. 你们 CI 中哪项检查适合先以只读、非阻断方式引入 Codex？
2. 哪些 Android 任务无论如何都不应无人值守地自动修复？

## 延伸阅读

- [Codex 非交互模式](https://developers.openai.com/codex/noninteractive/)
- [Codex SDK](https://developers.openai.com/codex/sdk/)
- [Codex GitHub Action](https://developers.openai.com/codex/github-action/)

