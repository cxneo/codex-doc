# 13｜专家能力：用 Skills 封装团队方法

`AGENTS.md` 适合长期约束，Spec 适合单次需求。像“怎样审查 Compose 状态和 Room 迁移”这样的成熟流程不应常驻每次上下文，也不应靠同事反复粘贴长提示词；它更适合成为 Skill。

Skill 是一个目录：必需 `SKILL.md`，可选 `references/`、`scripts/`、`assets/` 和 `agents/openai.yaml`。Codex 先看到名称与描述，真正选中后才完整读取说明与相关资源，这就是渐进式加载。

## 什么时候该创建 Skill

满足以下条件再做：

- 流程已经在真实任务中成功重复过；
- 有明确触发场景和不适用场景；
- 输入、输出和停止条件可以写清；
- 需要按需加载较长参考资料或确定性脚本；
- 团队愿意维护和评估它。

“以后也许有用”不是理由。一次性需求放在 Spec，简单命令放脚本，确定性格式检查交给 Lint/CI。

## Codex 从哪里发现 Skill

项目 Skill 放在从当前目录到仓库根沿途的 `.agents/skills/`；个人 Skill 放在 `~/.agents/skills/`；管理员和系统还有各自位置。同名 Skill 不会自动合并，因此团队应避免复制多个名字相同但内容不同的版本。

在 CLI 或 IDE 中使用 `/skills` 浏览，提示词里用 `$android-code-review` 显式调用。Codex 也能根据 `description` 隐式选择；描述写得含糊，就会误触发或漏触发。

## 拆解课程里的 Android Review Skill

入口是 [`SKILL.md`](./配套文件/PocketTasks-codex/.agents/skills/android-code-review/SKILL.md)：

```yaml
---
name: android-code-review
description: Review Android Kotlin changes for correctness, lifecycle,
  Compose state, coroutines, Room migrations, accessibility, architecture,
  and verification gaps. Use when reviewing an Android diff, working tree,
  branch, commit, or pull request before merge; do not use to implement fixes.
---
```

这段描述同时写了正向触发和边界：用于 Android diff 审查，不用于实现修复。正文继续规定：

1. 读取项目合同和完整 diff；
2. 沿 UI → 状态 → Repository → 数据源追踪；
3. 按改动类型加载参考；
4. 只读验证并准确记录结果；
5. 只报告可行动发现；
6. 产品、权限或环境不确定时停止。

## References 不是附件仓库

课程把长检查表拆成：

- [`compose-and-state.md`](./配套文件/PocketTasks-codex/.agents/skills/android-code-review/references/compose-and-state.md)
- [`room-and-data.md`](./配套文件/PocketTasks-codex/.agents/skills/android-code-review/references/room-and-data.md)

`SKILL.md` 明确说明何时读取哪一份。只改 README 时不加载 Room 指南；改 `TaskEntity`、Migration 或 DataStore 时才加载数据参考。这样既保留专业深度，又不污染无关任务上下文。

`scripts/` 只放真正需要确定性计算或文件处理的程序。能由现有 Gradle、Lint 或工具完成的事情，不要再写一层脆弱包装。

## `agents/openai.yaml` 管什么

课程的 [`agents/openai.yaml`](./配套文件/PocketTasks-codex/.agents/skills/android-code-review/agents/openai.yaml) 提供界面名称、短描述和默认提示：

```yaml
interface:
  display_name: "Android Code Review"
  short_description: "Review Android changes for architecture and platform risks"
  default_prompt: "Use $android-code-review to review the current Android diff and report evidence-backed findings."
```

还可以设置：

```yaml
policy:
  allow_implicit_invocation: false
```

此时 Codex 不会仅凭描述自动使用，显式 `$android-code-review` 仍可调用。涉及写外部系统、发布或高成本操作的 Skill，通常适合关闭隐式调用。

如果 Skill 必须使用某个 MCP，可在 `dependencies.tools` 声明依赖、传输方式和 URL。这只让工具依赖可被安装和连接，不会替代 Skill 中的步骤、权限与错误处理。

## 用内置创建器起步

流程已经明确时，可以在 Codex 里调用：

```text
$skill-creator

为 Android 项目创建一个只读的 Compose 性能审查 Skill。
它只在审查重组、稳定性或 Lazy 列表性能时触发；
不得修改代码；输出必须含文件证据、触发场景和可验证建议。
```

创建器会帮助确定触发、结构和是否需要脚本。生成物仍要由团队审查，尤其是自动执行脚本和工具依赖。

## Skill 的测试不是“调用一次感觉不错”

至少准备五类样例：

| 类型 | Android Review 示例 | 预期 |
|---|---|---|
| 明确正例 | “用 Android Review Skill 审查这个 diff” | 必须触发 |
| 自然正例 | “检查这次 Room 迁移是否会丢数据” | 可隐式触发 |
| 不完整输入 | “审查一下”但没有基线 | 先确定范围或安全停止 |
| 反例 | “实现任务筛选” | 不应触发只读 Review Skill |
| 边界 | 需要真机、生产凭据或产品决策 | 不猜，不扩大权限 |

还要评估输出：发现是否有真实位置、是否误报风格、严重程度是否膨胀、未运行设备测试是否如实记录。

## 一次真实调用

```text
使用 $android-code-review 审查当前工作区。
先读取 AGENTS.md、docs/constitution.md 和相关 Spec；不要修改文件。
如果触及 Compose 或数据层，只加载对应 reference。
报告可行动发现、实际运行的检查和残余验证缺口。
```

第 20 讲会用故障补丁检验这套 Skill，而不是只展示一份理想输出。

## Skill 与 Plugin 的边界

仓库内单个工作流优先直接提交 Skill。要跨项目安装、多 Skill 打包、连同 MCP 或展示资源一起分发时，再封装为 Plugin。先把方法本身做对，再考虑分发形态。

## 小结

好的 Skill 是可发现、可边界化、可测试的团队方法。名称和描述负责触发，正文负责流程与停止条件，References 提供按需专业知识，脚本提供确定性，`openai.yaml` 提供界面、调用政策和工具依赖。

下一讲会把 Skill 与自定义 Subagent 组合起来：让不同 Agent 承担独立调查，又不让它们共享一个嘈杂上下文。

## 延伸阅读

- [Codex Skills](https://developers.openai.com/codex/skills/)
- [Build skills](https://developers.openai.com/plugins/build/skills)
- [课程 Android Review Skill](./配套文件/PocketTasks-codex/.agents/skills/android-code-review/SKILL.md)
