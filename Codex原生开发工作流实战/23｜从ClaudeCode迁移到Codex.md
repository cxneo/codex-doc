# 23｜从 Claude Code 迁移到 Codex

如果团队已经围绕 Claude Code 建立了 `CLAUDE.md`、commands、skills、hooks、MCP 和 subagents，迁移最不应该做的事情，就是把这些经验全部丢掉，然后从一份空白 Codex 配置重新开始。

真正要迁移的是三层资产：项目知识、工作流程和安全意图。文件名与配置语法只是这些资产在不同系统中的表达。

## 先做资产盘点，不要先批量复制

在一个 Android 仓库中，先让 Codex 或人工只读列出：

```text
CLAUDE.md / 子目录 CLAUDE.md
.claude/settings*.json
.claude/commands/
.claude/skills/
.claude/agents/
MCP 配置
Hooks
个人级指令、记忆与项目配置
```

对每项标记四件事：所有者、作用域、是否仍在使用、含不含密钥或机器路径。很多旧配置已经失效，迁移是清理的机会，不必把历史垃圾完整保真。

## Codex 提供直接导入入口

当前 Codex 可以在桌面应用的 Settings 中使用 Import，也可以在 CLI 会话中运行：

```text
/import
```

导入器面向 Claude Code，可以转换或带入指令文件、设置、Skills、Plugins、项目、记忆、聊天、MCP、Hooks、斜杠命令和 Subagents 等资产。具体可见选项以你安装的当前版本为准。

导入不是“按下按钮就完成迁移”。它负责搬运与初步转换，团队仍需审查生成文件、作用域、凭据、命令和行为差异。

## 核心概念映射

| Claude Code 资产 | Codex 目标 | 迁移重点 |
|---|---|---|
| `CLAUDE.md` | `AGENTS.md` | 重写入口与作用域，避免机械改名 |
| `.claude/settings*.json` | `.codex/config.toml` 等配置层 | 按 Codex schema 重建权限与功能 |
| `.claude/commands/*.md` | `.agents/skills/*/SKILL.md` | 从文本命令升级为有触发描述的 Skill |
| `.claude/skills/` | `.agents/skills/` | 检查 frontmatter、资源路径与脚本 |
| `.claude/agents/` | `.codex/agents/*.toml` | 重写角色、权限与 developer instructions |
| MCP 配置 | Codex MCP 配置 | 重新认证，按工具限制读写 |
| Hooks | `.codex/hooks.json` 或内联配置 | 事件名、输入输出协议和信任机制需验证 |
| 权限规则 | Approval + Sandbox + Rules + Hooks | 迁移安全意图，不照抄语法 |

这张表是路线，不是 `cp` 命令。

## 迁移 CLAUDE.md：先去重，再分层

Claude Code 项目中的 CLAUDE.md 可能同时包含项目说明、编码规则、任务流程和个人偏好。迁移时先分类：

- 仓库通用入口与命令 → 根 `AGENTS.md`；
- Android 数据库等局部规则 → 对应目录 `AGENTS.md`；
- 长期架构理由 → `docs/constitution.md`；
- 某类任务的操作流程 → Skill；
- 个人表达偏好 → 用户级 AGENTS，而不是提交到项目。

PocketTasks 的 Room 迁移规则就不应埋在根文件二十屏之后，而应放在数据库目录的局部 AGENTS，并由根文件链接。

Codex 的 `AGENTS.override.md` 与逐层发现机制有自己的语义。迁移后，要分别从项目根、app 模块和数据库目录启动只读会话，让 Codex复述生效指令，验证作用域。

## 迁移 Commands：不要保留一个空壳名字

Claude Code 的自定义斜杠命令往往是一份 Markdown 工作流。Codex 当前推荐使用 Skills 承载新的可复用流程。

例如 `/android-review` 迁移后，不只是把正文放进 `SKILL.md`：

1. 用明确的 `name` 和 description 定义触发场景；
2. 核心步骤留在正文，Compose 与 Room 细则移到 references；
3. 检查脚本是否依赖 Claude 专属变量、工具名或输出格式；
4. 给 Skill 准备纯 UI、数据库和文档 diff 三种验证样例；
5. 显式调用 `$android-code-review` 测试，再观察隐式触发。

如果某条 command 只是两句临时提示，不必强行升级。可以保留为项目文档模板。

## 迁移权限：保持风险意图，不保持按钮名称

Claude Code 与 Codex 的权限、沙箱和 Hook 模型并非一一同构。团队应先写出原策略背后的意图：

```text
允许自动运行目标单元测试
网络默认关闭，需要下载依赖时询问
禁止清除设备数据
推送、发版、签名操作逐次人工确认
评审角色保持只读
```

然后使用 Codex 的 sandbox、approval、Rules、Hooks 和 Agent 配置重新表达，并用代表性命令逐条测试。直接复制 JSON 键名，只会制造一种“文件存在但政策未生效”的假安全感。

## MCP 与 Hooks 要重新做信任审查

导入后不要立即开启所有 MCP 和 Hook。逐个检查：连接到哪个端点、使用什么身份、暴露哪些工具、是否包含写操作、Hook 脚本从哪里加载、是否会上传提示词或日志。

凭据不应进入迁移包。让每位开发者或 CI 使用正式认证流程重新授权，也借此撤销已经不需要的旧权限。

## 用同一组 Android 任务做 A/B 验证

选择三项不改生产数据的任务，在迁移前后分别运行：

1. 只读追踪“完成任务”的数据流；
2. 对一份 Compose + ViewModel diff 做审查；
3. 根据一个小 Spec 生成 Plan，不修改代码。

比较的不是回答措辞，而是：项目规则是否生效、文件证据是否准确、危险动作是否被约束、产物结构是否稳定、未运行测试是否诚实报告。

只有这些合同一致，迁移才算完成。

## 推荐迁移顺序

```text
备份与盘点
  → 使用 Import 生成候选配置
  → 人工审查 diff 和敏感信息
  → 先迁项目指令与只读 Skills
  → 再迁 MCP、Hooks 与写权限
  → 用基准任务验证
  → 小范围试用
  → 删除确认废弃的旧入口
```

旧配置不要在第一天删除。保留一个明确回退窗口，但避免两套互相冲突的规则长期并行。确定迁移完成后，再通过正常评审移除旧资产。

## 小结

Claude Code 到 Codex 的迁移不是从零开始。Codex 的 Import 可以承担搬运，但真正工作是重新确认项目知识、流程与安全意图在新系统中确实生效。先读、再转、逐项启用、用同一组 Android 任务验证。

下一讲处理 Cursor。它既是编辑器，又包含 Agent、Rules、Commands、Hooks 与 Background Agents，迁移时更需要把“编辑体验”和“工程能力”拆开看。

## 思考题

1. 现有 CLAUDE.md 中，哪些内容其实不属于全仓库指令？
2. 哪个 Hook 或 MCP 权限最值得在迁移时重新收紧？

## 延伸阅读

- [Codex 官方文档](https://developers.openai.com/codex/)
- [Claude Code 中文概览](https://code.claude.com/docs/zh-CN/overview)

