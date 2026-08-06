# 23｜从 Claude Code 迁移到 Codex：迁移意图，不照搬文件名

Claude Code 与 Codex 都能读取仓库、修改代码和执行命令，但两者的项目指令、会话恢复、IDE、Checkpoint、记忆和自动化表面并不完全相同。

可靠迁移分三步：

```text
先盘点 → 使用官方 Import 生成候选结果 → 逐项验证语义
```

不要先批量复制 `.claude` 文件，更不要删除原配置后再尝试迁移。Codex 官方 Import 不会修改或删除现有 Claude Code 设置，可以并行验证后再决定切换范围。

## 第一步：建立资产清单

在仓库根目录只读执行：

```bash
find . -name CLAUDE.md -o -name CLAUDE.local.md \
  -o -path '*/.claude/rules/*' \
  -o -path '*/.claude/commands/*' \
  -o -path '*/.claude/agents/*' \
  -o -path '*/.claude/skills/*' \
  -o -path '*/.claude/settings*.json'
```

再检查用户层配置：

```bash
find "$HOME/.claude" -maxdepth 4 -type f \
  \( -name '*.md' -o -name '*.json' \) -print
```

清单中为每项标注：

- 作用域：个人、项目或子目录；
- 类型：指令、命令、Skill、Hook、MCP、Subagent、记忆或会话；
- 是否包含凭据；
- 是否仍在使用；
- Codex 对应关系：直接、需改造或无严格等价物。

不要把 `~/.claude` 整个目录提交到仓库，也不要在迁移报告中粘贴 token。

## 第二步：使用 Codex 官方 Import

### CLI

必须从本地空闲的 Codex TUI 会话运行：

```bash
cd /path/to/android-project
codex
```

然后输入：

```text
/import
```

选择 **Claude Code**，再选择需要导入的设置、项目文件和最近聊天。

限制需要提前告诉团队：

- CLI 最多发现最近 30 天内的 50 个聊天；
- 任务正在运行时不能使用 `/import`；
- 远程会话中不能使用；
- 连接本地 app-server daemon 时不能使用；
- 导入只处理受支持资产，不代表行为已经验证。

### 桌面应用

打开 **Settings → Import**；如果没有独立 Import 页面，在 General 中寻找 **Import other agent setup**。桌面流程会提示哪些 Plugin 或连接仍需重新授权。

导入完成后，保留状态卡并逐项处理，不要看到“完成”就马上删除 Claude 配置。

## 第三步：逐项做语义映射

| Claude Code | Codex | 迁移结论 |
|---|---|---|
| `CLAUDE.md` | `AGENTS.md` | 可导入，但必须重新检查发现和覆盖顺序 |
| `CLAUDE.local.md` | 未提交的 `AGENTS.override.md` 或个人全局指令 | 需改造，避免把个人路径提交到仓库 |
| `.claude/rules/` | 嵌套 `AGENTS.md`、Skill、Rules 或 Hook | 按意图拆分，不能只改后缀 |
| `settings.json` | `config.toml` | 可导入候选，字段和权限语义必须复核 |
| Slash commands | Skills | 可转换，但要补齐输入、步骤、产物和停止条件 |
| Skills | `.agents/skills/` | 高度可迁移，仍需检查引用路径和调用策略 |
| Hooks | Codex Hooks | 事件和输入输出协议不同，必须重新测试 |
| MCP | Codex MCP | 传输可对应，认证、scope、审批需重新配置 |
| Subagents | `.codex/agents/*.toml` | 能力类似，角色字段、并发和权限继承不同 |
| Auto memory | Codex Memories | 都是记忆层，但存储与控制方式不同 |
| Plan mode | `/plan` | 接近，但交互和状态语义不完全相同 |
| 长任务持续目标 | `/goal` | Codex 专门的持续目标层 |
| Sessions | `/resume`、`/fork`、`/compact` | 需要重新建立会话管理习惯 |
| Checkpoint / rewind | Git、Worktree、桌面分块/文件回退 | **无一对一 CLI 等价能力** |
| Agent Teams | Codex Subagents | 不是同一套团队协议，需重新设计委派边界 |
| JetBrains 插件 | Android Studio + Codex CLI/桌面；VS Code 扩展 | **没有同等官方 Android Studio 插件** |
| Chrome 集成 | Browser、Chrome extension 或 Computer Use | 取决于入口、平台、地区和组织策略 |
| Scheduled tasks | Codex Scheduled tasks | 可迁移日程意图，需重新选择执行环境 |
| Remote control | Codex Remote connections / app-server 相关能力 | 不是命令级直接替换，先做安全设计 |

## 迁移 CLAUDE.md：先去重，再分层

假设旧文件同时包含：

```markdown
- 所有项目都使用中文汇报
- PocketTasks 使用 Compose + Room
- 数据库升级禁止 destructive migration
- 当前正在修复 AND-184
- 我的 Android SDK 在 /Users/alice/Library/Android/sdk
```

不要全部复制进根 `AGENTS.md`。正确拆分：

| 内容 | 新位置 |
|---|---|
| 个人表达偏好 | `~/.codex/AGENTS.md` 或 Personality |
| 项目技术栈与验证命令 | 仓库根 `AGENTS.md` |
| Room 严格规则 | 数据库目录的嵌套 AGENTS/override |
| 当前 issue 状态 | Spec、Tasks 或 issue 系统 |
| 个人 SDK 绝对路径 | 本地环境，不提交 |

迁移后用 `/debug-config` 检查 Codex 实际读取了哪些层，而不是只确认文件存在。

## Commands 迁移为 Skills

Claude command 如果只有一段短 Prompt，可以先保留为普通模板；如果它已经是一套团队流程，就迁移为 Skill。

旧命令：

```text
review-android：检查 Compose、协程和 Room。
```

Codex Skill 至少应明确：

```markdown
---
name: android-code-review
description: Review Android diffs for correctness, lifecycle, state and data risks.
---

1. Read AGENTS.md and the relevant Spec.
2. Restrict review to the requested diff.
3. Check Compose state, coroutine ownership, Room migrations and tests.
4. Report only actionable findings with tight locations.
5. State coverage and residual risks when no findings exist.
```

课程中的完整实例位于 [Android review Skill](./配套文件/PocketTasks-codex/.agents/skills/android-code-review/SKILL.md)。

## Hooks 必须重写协议，而不是复制脚本

Claude 和 Codex 都有 Hook，但事件字段、matcher、阻断方式和信任流程不同。

Codex `PreToolUse` 阻断示例：

```json
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Destructive command blocked by project hook."
  }
}
```

也可以退出码 `2` 并把原因写到 `stderr`。迁移后必须用安全命令、危险命令和边界命令分别测试；不能因为脚本能够启动就认为语义一致。

## Checkpoint 是迁移中的最大差异之一

Claude Code 提供面向会话的 checkpoint/rewind 工作流。Codex 当前不应被描述成具有完全相同的 CLI `/rewind`。

在 Codex 中组合使用：

- 小改动：`/diff`，桌面审查窗按分块或文件回退；
- 可提交阶段：Git commit；
- 互斥方案：`/fork` + 独立 Git 分支或桌面 Worktree；
- 后台任务：桌面 Worktree，必要时 Handoff 回 Local；
- 数据迁移：数据库备份、旧 schema 和 Migration 测试。

回退机制必须在任务开始前确定，不能等出问题后才问“Codex 的 checkpoint 在哪里”。

## Android Studio 用户的迁移路径

Claude JetBrains 插件用户不宜被告知“安装 Codex IDE 扩展即可平替”。推荐渐进路径：

1. 继续用 Android Studio 做 Sync、Preview、Profiler 和模拟器；
2. 在 Android Studio Terminal 或外部终端启动 Codex CLI；
3. 用 Codex 桌面应用管理长任务、Worktree 和审查；
4. 只有确实需要围绕 VS Code 选区协作时再安装 Codex IDE 扩展；
5. 必须视觉操作 IDE 时评估 Computer Use，并限制可操作应用。

## 用同一组任务做 A/B 验收

迁移不是“Codex 能回答问题”就结束。选择三项具有代表性的 Android 任务：

### 任务一：只读架构调查

- 找到 Room Database、DAO、Repository、ViewModel 和 Compose 入口；
- 不允许改文件；
- 比较遗漏率、证据引用和耗时。

### 任务二：小型功能

- 按同一 Spec 增加任务筛选；
- 比较 diff 半径、测试数量、人工纠偏次数和完成时间。

### 任务三：高风险迁移

- 调查 v1→v2 Room Migration；
- 比较是否保留数据、是否运行旧库测试、是否诚实报告未验证项。

建议记录：

| 指标 | Claude Code | Codex | 备注 |
|---|---:|---:|---|
| 首次有效计划耗时 | | | |
| 人工纠偏次数 | | | |
| 非必要修改文件数 | | | |
| 目标测试通过率 | | | |
| 审查有效发现数 | | | |
| 未声明风险数 | | | |

## 迁移完成清单

- [ ] 已保存 Claude 原配置，未做破坏性删除。
- [ ] 已运行官方 Import 并保留结果清单。
- [ ] `AGENTS.md` 层级通过 `/debug-config` 验证。
- [ ] Skills 的引用路径和触发策略已经测试。
- [ ] MCP 已重新认证并限制工具权限。
- [ ] Hooks 通过允许、阻断和边界样例。
- [ ] Subagents 权限和并发已复核。
- [ ] 已决定 checkpoint 的 Git/Worktree 替代策略。
- [ ] Android Studio 使用方式已向团队说明。
- [ ] 三类 A/B 任务已经跑完。
- [ ] Claude Code 保留一个观察期，没有立即删除。

## 完成标志

团队能够明确指出哪些资产已直接导入、哪些被重新设计、哪些能力没有一对一等价物；Android 构建和验证不依赖口头承诺。

## 延伸阅读

- [Codex Import](https://learn.chatgpt.com/docs/import)
- [Codex AGENTS.md](https://learn.chatgpt.com/docs/customization/agents-md)
- [Codex Memories](https://learn.chatgpt.com/docs/customization/memories)
- [Claude Code 概览](https://code.claude.com/docs/zh-CN/overview)
- [Claude Code JetBrains](https://code.claude.com/docs/zh-CN/jetbrains)
- [Claude Code 会话](https://code.claude.com/docs/zh-CN/sessions)
