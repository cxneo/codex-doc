# 24｜从 Cursor 迁移到 Codex

Cursor 迁移比 Claude Code 多一层：Cursor 同时是代码编辑器和 Agent 工作环境。团队说“迁移到 Codex”时，可能指更换 Agent，也可能指更换编辑器、远程执行方式或整套规则系统。

第一步不是卸载 Cursor，而是把这些问题拆开。Android 团队完全可以继续用 Android Studio 做 Preview、Profiler 和设备调试，同时使用 Codex CLI 或桌面应用承担 Agent 工作；编辑器迁移与工程化迁移不必同一天发生。

## 当前没有 Cursor 一键导入

Codex 的 `/import` 当前面向 Claude Code，不是 Cursor。因此 Cursor 资产应先盘点，再手工转换与验证。不要把 `.cursor` 整个复制到 `.codex`，两边文件名相似的能力也可能使用不同 schema 和生命周期。

## 资产映射图

| Cursor 资产 | Codex 中的去向 | 注意事项 |
|---|---|---|
| `.cursor/rules/*.mdc` | 根或目录级 `AGENTS.md`、宪法、Skills | 按规则类型与作用域重新分流 |
| 旧 `.cursorrules` | `AGENTS.md` 等 | 先清理；它在 Cursor 中已是旧形式 |
| `.cursor/commands/*.md` | `.agents/skills/*/SKILL.md` 或任务模板 | 增加触发描述、输入输出与验证 |
| Cursor Agent Skills | `.agents/skills/` | 检查发现路径与元数据，不假设完全兼容 |
| `.cursor/mcp.json` | Codex MCP 配置 | 重建认证与工具审批 |
| `.cursor/hooks.json` | `.codex/hooks.json` | 不能直接复制 schema；重写并测试事件协议 |
| Background Agents | Codex Cloud 或 App Worktree | 对齐环境、分支、网络与交付方式 |
| Cursor CLI 自动化 | `codex exec` / SDK / Action | 权限默认值不同，重新设计沙箱 |

如果仓库已经有根 `AGENTS.md`，这是很好的共享起点。Cursor CLI 与 Codex 都能读取它，但仍要验证各自的发现范围和优先级。

## 迁移 Rules：按意图拆，不按文件拆

Cursor Project Rules 有 Always、按 glob 自动附加、Agent Requested 和 Manual 等类型。迁移时逐条问：这究竟是长期项目约束、目录局部约束、任务方法，还是按需参考？

例如：

```text
Always：所有 ViewModel 暴露不可变 UI State
  → 根 AGENTS.md

Glob：app/**/data/local/** 发生 Schema 变化时必须写 MigrationTest
  → database 目录 AGENTS.md，或 Android Review Skill 的 Room reference

Agent Requested：排查 Compose 重组性能时读取的指南
  → performance Skill 的 reference

Manual：生成发布说明
  → 显式调用的 Skill
```

Codex 的 `.codex/rules/*.rules` 主要用于命令执行政策，不是 Cursor Rules 的同名替代品。不要把编码规范塞进 exec policy。

## 迁移 Commands：从快捷入口变成能力契约

打开每个 `.cursor/commands/*.md`，删除只对 Cursor UI 或工具名有效的内容，补齐：触发场景、必要输入、读取文件、允许修改范围、停止条件、产物与验证。

高频而成熟的流程变成 Skill；只负责初始化 Spec、Plan 或 Tasks 形状的内容可以保留为模板；很少使用或只服务个人的命令不必进入团队仓库。

迁移后用 `/skills` 检查可发现性，再用显式 `$skill-name` 完成一次真实任务。文件存在不等于 description 能正确触发。

## MCP 和 Hooks：语义相似，协议不同

Cursor 与 Codex 都支持 MCP，但配置位置、认证和工具审批不应假定完全一致。逐个服务重新添加，先启用只读工具，确认身份与作用域，再开放写操作。

两边也都有 Hooks，但事件名、输入 JSON、输出协议、超时与信任机制不同。迁移 Hook 的正确方式是：先写清原 Hook 的业务意图，再按 Codex Hooks 文档重写；用样例事件测试；先观察不阻断；最后才启用控制。

## Background Agent 怎样映射

Cursor Background Agents 通常在远程 Ubuntu 环境和独立 Git 分支工作。Codex 侧可以根据任务选择 Cloud 执行，或在桌面应用中使用本地 Worktree。

不要只比较“都能后台跑”。要逐项对齐：

- 环境初始化脚本和 Android SDK；
- JDK、Gradle 与缓存；
- 网络白名单；
- Secrets 是否可见；
- 分支和提交归属；
- 是否有模拟器或设备能力；
- 产物怎样回到本地评审。

对于强依赖 Android Emulator、硬件或本地私有服务的任务，本地 Worktree 可能更直接；纯代码调查和 JVM 测试更容易进入 Cloud。

## 特别注意非交互权限差异

Cursor CLI 的非交互 Agent 与 Codex Exec 可能有不同默认写权限。迁移脚本时，不要假设旧命令在新系统里会“同样执行”。Codex Exec 默认只读，需要修改时显式使用 `--sandbox workspace-write`；网络、MCP 写操作与远端状态仍要单独限制。

迁移自动化时先跑只读任务，检查 JSON 输出和退出行为，再开放最小写权限。任何自动推送、创建 PR 或发布的步骤都需要独立授权。

## Android 团队的双轨过渡

推荐两周双轨，而不是工具大爆炸：

第一阶段，让 Cursor 与 Codex 共用根 AGENTS.md、项目宪法和 Spec 模板。新建的稳定工作流只进入 `.agents/skills`，避免继续扩大旧 commands。

第二阶段，选一条真实功能线让 Codex 完整执行调查—计划—实现—验证；另一条相似任务保留原流程。比较评审时长、返工、测试证据和权限事件，而不是单看生成速度。

确认核心合同后，逐步停用重复的 `.cursor` 资产。编辑器是否更换可以由个人和 Android 工具需求另行决定。

## 迁移验收清单

迁移完成时，至少能回答：

- Codex 从哪些 AGENTS.md 获得规则，作用域是否正确？
- 原 Rules 中的长期约束是否有唯一新位置？
- 高频 Commands 是否已成为可触发、可验证的 Skills？
- MCP 身份与写权限是否重新审查？
- Hooks 是否按 Codex 协议测试，而非直接复制？
- 后台 Android 环境能否真正运行所需 Gradle task？
- `codex exec` 的沙箱和网络是否明确？
- 旧配置的退役日期和回退方案是否清楚？

## 小结

Cursor 到 Codex 的迁移需要把编辑器、Agent、规则、外部工具与后台环境分别对齐。没有一键导入反而迫使团队识别资产真正意图：项目知识进入 AGENTS 与宪法，成熟流程进入 Skills，命令安全进入 Codex Rules 和 Hooks，执行环境按 Android 任务选择。

工具迁移到这里结束，但工程化没有“毕业版本”。接下来的结束语会把 24 讲收束成一条持续改进路线：怎样让驾驶舱随着项目一起生长，而不是半年后变成另一套过期文档。

## 思考题

1. 团队说“离不开 Cursor”时，真正不可替代的是编辑器体验、规则资产还是后台环境？
2. 哪一条 Cursor Rule 在迁移后应该被删除，而不是转换？

## 延伸阅读

- [Cursor Rules](https://docs.cursor.com/context/rules-for-ai)
- [Cursor Commands](https://docs.cursor.com/en/agent/chat/commands)
- [Cursor MCP](https://docs.cursor.com/context/model-context-protocol)
- [Cursor Background Agents](https://docs.cursor.com/background-agent)
- [Codex 官方文档](https://developers.openai.com/codex/)

