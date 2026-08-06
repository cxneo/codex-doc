# 14｜协作分工：用 Subagents 拆解复杂任务

一个跨层 Android 问题可能同时需要追踪 Compose 状态、检查 Room 查询、分析协程调度并评估测试缺口。如果一个 Agent 依次做完所有调查，会话会变长，注意力也会在不同问题间来回切换。

Subagent 允许主 Agent 把边界清晰的子任务交给不同角色，再汇总结果。但并行不是免费午餐：切分错误会导致重复阅读、结论冲突和修改碰撞。

## 把主 Agent 当成技术负责人

主 Agent 应保留目标、边界与最终整合。Subagent 更像临时专家，负责一个可独立回答的问题。

PocketTasks 出现“快速连续点击完成后，列表偶尔回退”的缺陷，可以这样拆：

```text
主 Agent：维护问题定义、整合证据、提出最终方案
├── explorer：追踪 UI 事件与 ViewModel 状态时序（只读）
├── explorer：追踪 Repository、Room 写入和 Flow 失效（只读）
└── reviewer：检查现有测试为何没有捕获竞争条件（只读）
```

三个任务都能独立调查，输出文件路径与假设。主 Agent 收到结果后先解决矛盾，再决定是否进入实现。

## 什么是好的委派边界

一个合适的子任务满足四个条件：目标单一、输入明确、输出可验、无需频繁与其他任务同步。

例如：

```text
调查 TaskRepository.completeTask 的所有调用者与线程上下文。
只读，不修改文件。输出：调用链、相关测试、可能竞争点，以及每个结论的路径和行号。
```

而“把这个功能全部做好”不是子任务，它只是把主 Agent 的责任转手，边界和完成标准都没有变清楚。

## Codex 的内置与自定义 Agent

Codex 提供通用的 `default`、执行型 `worker` 和只读探索型 `explorer`。项目还可以在 `.codex/agents/*.toml` 定义角色，例如 Android 审查者：

```toml
name = "android-reviewer"
description = "Read-only reviewer for Android architecture and regression risks"
sandbox_mode = "read-only"

developer_instructions = """
Review only the assigned scope. Trace Android lifecycle, Compose state,
coroutines, Room persistence, and verification gaps. Do not modify files.
Return prioritized findings with precise evidence; say explicitly when none exist.
"""
```

`name`、`description` 和 `developer_instructions` 是角色的核心。description 要帮助主 Agent 判断何时委派；指令要约束范围与输出。只读角色再加 `sandbox_mode = "read-only"`，让权限和职责一致。

会话中可以用 `/agent` 查看或切换 Agent 线程。你也可以在请求中直接要求合适的委派；但是否并行应由任务结构决定，而不是为了显得“多 Agent”。

## 哪些工作不应并行

下面这些情况更适合串行：

- 子任务依赖上一步尚未确定的架构决定；
- 多个 Agent 必须修改同一文件；
- 问题很小，协调成本大于调查成本；
- 数据库迁移顺序、版本号等需要单一事实源；
- 权限、发布或破坏性操作需要统一人工授权。

即使需要并行修改，也应给每个 Agent 独立 Worktree 和互斥文件范围，最后由一个负责人整合并运行组合验证。

## 防止三个常见陷阱

### 结果无法合并

所有子任务使用相同输出结构：已确认事实、证据位置、假设、未决问题、建议下一步。主 Agent 就能横向比较。

### 重复探索

在委派前划分组件或问题维度，并告诉每个 Agent 哪些内容不在其范围内。不要让三个人都“全面调查”。

### 把判断外包给多数票

三个 Agent 得出同一结论也不等于正确。主 Agent 必须回到代码、测试和平台文档，特别是结论冲突时。多 Agent 增加的是搜索带宽，不是事实权威。

## 一次完整的缺陷调查

主 Agent 可以按下面流程组织：

1. 复述症状和可复现条件；
2. 先做一次浅层扫描，识别可独立调查的边界；
3. 给只读 Subagents 分配 UI、数据与测试任务；
4. 收集结果，标出一致与冲突；
5. 主 Agent 复核关键证据并形成根因假设；
6. 只有根因足够确定后，才另起实现步骤；
7. 最后运行能覆盖组合行为的测试。

注意第六步：调查授权不自动包含修复授权。对于代码评审或诊断任务，Subagent 不应顺手改代码。

## 小结

Subagents 适合扩大彼此独立的探索与执行带宽。主 Agent 负责目标和整合，子任务必须边界清楚、输出一致、权限最小。并行工作的价值来自良好拆分，而不是 Agent 数量。

下一讲，我们会把交互式协作带出终端：使用 `codex exec`、SDK 和 CI，在没有人持续对话的环境中运行结构化任务。

## 思考题

1. 你最近处理的一个 Android 缺陷，能拆成哪两个互不依赖的只读调查？
2. 哪个最终决定必须由主 Agent 或人统一做出？

## 延伸阅读

- [Codex Subagents](https://developers.openai.com/codex/subagents/)

