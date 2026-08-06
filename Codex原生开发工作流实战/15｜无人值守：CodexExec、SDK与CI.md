# 15｜无人值守：`codex exec`、SDK 与 CI

交互会话里，人可以看审批、纠正误解、停止危险命令。脚本与 CI 没有这个缓冲，所以“把 `codex` 放进 Shell”不是自动化方案。可维护自动化必须固定输入、权限、输出合同、凭据边界和失败方式。

## `codex exec` 的基础合同

最简单的调用是：

```bash
codex exec "只读总结这个 Android 仓库的模块与验证命令"
```

默认情况下：

- 运行在只读沙箱；
- 进度写到 stderr；
- 最终 Agent 消息写到 stdout；
- 必须位于 Git 仓库中，除非你明确使用 `--skip-git-repo-check`；
- 复用 CLI 已保存的认证与正常配置层。

因此下面的重定向只保存最终消息，终端仍可看到进度：

```bash
codex exec "生成最近十个提交的发布说明" > release-notes.md
```

不希望保存会话 rollout 文件时使用 `--ephemeral`。临时执行更干净，但也不要指望之后恢复这次会话。

## 三种 stdin 用法不要混淆

提示词作为参数，stdin 是补充上下文：

```bash
./gradlew :app:testDebugUnitTest 2>&1 \
  | codex exec "只读分析失败日志，列出根因假设和下一条最小验证"
```

整个 stdin 就是提示词：

```bash
codex exec - < .github/codex/prompts/review.md
```

动态程序生成完整提示词：

```bash
generate-review-prompt.sh | codex exec - --json > review-events.jsonl
```

`-` 让意图更清楚，避免以后修改管道时误把日志当成指令。

## 机器读取需要两类输出

`--json` 会把运行期间的事件作为 JSON Lines 写到 stdout，包括线程、轮次、命令、文件变更和错误。它适合日志处理：

```bash
codex exec --json "只读盘点项目风险" | jq -c 'select(.type == "turn.completed")'
```

如果下游只需要稳定的最终对象，使用 `--output-schema`。课程项目提供 [review-schema.json](./配套文件/PocketTasks-codex/.github/codex/review-schema.json)，固定四个字段：摘要、发现、实际检查、残余风险。

```bash
codex exec \
  --sandbox read-only \
  --output-schema .github/codex/review-schema.json \
  --output-last-message build/reports/codex/review.json \
  "只读审查当前工作区，不要修改文件"
```

`--output-schema` 约束最终回答的形状，不保证其中每条判断都正确。仍要验证文件、行号和命令证据。

## 课程中的可执行只读审查脚本

[`scripts/codex-readonly-review.sh`](./配套文件/PocketTasks-codex/scripts/codex-readonly-review.sh) 把关键设置写死：

```bash
cd 配套文件/PocketTasks-codex
bash -n scripts/codex-readonly-review.sh
./scripts/codex-readonly-review.sh
```

它使用：

- `--ephemeral`：不保留本次 rollout；
- `--ignore-user-config`：排除个人 `~/.codex/config.toml` 对 CI 的漂移；
- `--sandbox read-only`：只审查，不写产品代码；
- `--output-schema`：产出稳定 JSON；
- `--output-last-message`：将最终对象保存到报告目录。

脚本从自身所在目录解析 Android 项目根，而不是盲目采用外层 Git 根，因此课程项目嵌在文档仓库中或单独分发时都能找到 Schema。

`--ignore-user-config` 只忽略用户配置，不等于忽略项目政策。`--ignore-rules` 会跳过用户和项目 execpolicy Rules，只能在另有等价隔离的受控环境使用，不能为了“让 CI 通过”随手添加。

## 写入自动化要显式升级权限

默认只读是重要安全属性。确实需要生成补丁时：

```bash
codex exec --sandbox workspace-write \
  "修复当前失败的目标单元测试；不要提交、推送或发布"
```

`danger-full-access` 只适合已经隔离的 Runner 或容器。旧的 `--full-auto` 是兼容参数，新脚本应使用明确的 `--sandbox`。

自动化不能弹出新审批时，需额外授权的动作会失败。把失败当成边界反馈，不要自动重试为更高权限。

## 配置、Rules 与必需 MCP

非交互任务最怕“开发者电脑能跑，Runner 不一致”。建议脚本明确：

- 是否读取用户配置；
- 项目 `.codex/config.toml` 是否受信任；
- 哪些 Rules 必须生效；
- 所需 MCP 是否设为 `required = true`。

启用且标记为必需的 MCP 初始化失败时，`codex exec` 会报错退出，而不是悄悄在缺少关键数据源的情况下继续。这比生成一份信息不全但外表完整的报告更安全。

## 会话恢复适合两阶段流水线

交互之外也能继续同一线程：

```bash
codex exec "只读审查并找出并发问题"
codex exec resume --last "只修复刚才已经证实的问题，并运行目标测试"
```

也可以使用明确的会话 ID。`--last` 以当前工作目录为边界查找最近会话；跨目录行为不要靠猜。第一阶段如果用了 `--ephemeral`，就没有可恢复的持久会话。

## CI 凭据必须与仓库代码隔离

GitHub Actions 优先使用 `openai/codex-action@v1`，而不是在执行仓库脚本的整个 Job 中暴露 API Key。课程的 [Codex PR 审查示例](./配套文件/PocketTasks-codex/.github/workflows/codex-pr-review.example.yml) 和 [自动修复示例](./配套文件/PocketTasks-codex/.github/workflows/codex-autofix.example.yml) 展示了两条原则：

1. Codex 生成补丁的 Job 只有只读仓库权限；
2. 写分支或开 PR 放在另一个不持有 API Key 的 Job。

不要把 `OPENAI_API_KEY` 或 `CODEX_API_KEY` 设成会运行不受信任仓库代码的 Job 级环境变量。非 GitHub 环境中如果必须用 API Key，只为单次 `codex exec` 进程注入，并确保同一进程环境不运行仓库生命周期脚本。

## 何时使用 SDK

当你需要多轮线程、应用内事件处理或把 Codex 嵌入自己的服务时，再使用 Codex SDK。简单 CI 任务优先用 `codex exec` 或官方 Action，因为边界更容易审查。

SDK 同样要显式选择沙箱。典型流程是先 `read_only` 计划，再在同一线程用 `workspace_write` 实施，最后恢复 `read_only` 复审。SDK 不会替你设计凭据、审批和验证策略。

## 自动化验收清单

- 输入是否来自固定提示词或受控上下文？
- 默认是否只读，写入是否显式？
- stdout 是最终文本、JSONL 事件还是 Schema 对象？
- 失败时退出并保留诊断，还是吞错继续？
- API Key 是否与仓库代码和写权限隔离？
- 产物是建议、补丁还是远端变更？每一步由谁授权？

## 小结

`codex exec` 把交互任务变成可脚本化接口，但可靠性来自外围合同：最小沙箱、确定输入、结构化输出、明确失败和凭据隔离。课程脚本可以直接执行，也可以作为团队审查 Job 的起点。

下一讲把前 15 讲的机制装配到一个 Android 项目驾驶舱，说明哪些文件进仓库、哪些设置留在本机、日常任务从哪里进入。

## 延伸阅读

- [Codex non-interactive mode](https://learn.chatgpt.com/docs/non-interactive-mode)
- [Codex SDK](https://developers.openai.com/codex/sdk/)
- [Codex GitHub Action](https://github.com/openai/codex-action)
- [课程只读审查脚本](./配套文件/PocketTasks-codex/scripts/codex-readonly-review.sh)
