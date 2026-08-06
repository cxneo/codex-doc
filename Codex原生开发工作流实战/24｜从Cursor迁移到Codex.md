# 24｜从 Cursor 迁移到 Codex

Cursor 同时是编辑器、Agent 和规则运行环境。团队说“迁移到 Codex”时，可能只是更换 Agent，也可能意味着更换编辑器、CLI 自动化、后台环境和权限系统。把这些事情绑在同一天，失败后很难知道是哪一层有问题。

Android 团队通常更适合保留 Android Studio 做 Sync、Compose Preview、Profiler、Layout Inspector 和模拟器，同时使用 Codex CLI 或桌面应用处理 Agent 工作。工程资产迁移与编辑器选择可以分开。

## 先说结论：没有 Cursor 一键导入

Codex 的 `/import` 当前用于 Claude 资产，不是 Cursor。因此迁移路径是：盘点 → 分类 → 转换 → 双轨验证 → 退役。不要把 `.cursor/` 整个复制到 `.codex/`；即使两边都叫 Rules、MCP 或 Hooks，文件协议和生命周期也不同。

## 第 0 步：冻结新资产，生成清单

先约定一周内不新增 Cursor 专用规则或命令，避免迁移目标继续移动。课程提供盘点脚本：

```bash
cd 配套文件/PocketTasks-codex
./scripts/audit-cursor-assets.sh /path/to/your-android-project
```

脚本列出 `.cursor/**`、旧 `.cursorrules`、已有 `AGENTS.md`，并搜索规则元数据、MCP 和 Hook 线索。它不会读取仓库外的个人 Cursor 设置，所以还要人工补充：

- Cursor Settings 中的 User Rules 和 Memories；
- 团队实际使用的 MCP 身份与工具；
- CLI/CI 脚本；
- Background Agent 环境；
- 只有口头约定、尚未写入仓库的流程。

给每项资产记录负责人、使用频率、作用域、是否含凭据、最后验证日期和退役决定。

## 资产不是一对一改名

| Cursor 资产 | Codex 主要去向 | 转换判断 |
|---|---|---|
| `.cursor/rules/*.mdc` | `AGENTS.md`、局部 `AGENTS.md`、宪法或 Skill | 按意图和作用域分流 |
| 旧 `.cursorrules` | 同上，或删除 | 先去重，不保留兼容包袱 |
| `.cursor/commands/*.md` | Skill 或 Spec/Plan/Tasks 模板 | 补触发条件、输出与验证 |
| Cursor Agent Skills | `.agents/skills/` | 检查元数据和引用路径，不假设完全兼容 |
| `.cursor/mcp.json` | Codex MCP 配置 | 重新建认证、必需性和工具审批 |
| Cursor Hooks | `.codex/hooks.json` + 可测试脚本 | 按 Codex 事件 JSON 重写 |
| Background Agents | Codex Cloud 或 App Worktree | 对齐 SDK、网络、Secrets、设备和交付 |
| Cursor CLI 脚本 | `codex exec`、SDK 或 Action | 重设沙箱、输出合同与认证 |
| User Rules / Memories | 个人 Codex 设置或删除 | 团队事实必须进入仓库 |

Codex `.codex/rules/*.rules` 是命令执行政策，不是 Cursor Project Rules 的同名替代。编码规范不能因为都叫 Rule 就塞进 execpolicy。

## 第 1 步：逐条迁移 `.mdc`

Cursor Project Rules 可以 Always、按 glob 自动附加、由 Agent 判断或手工引用；嵌套 `.cursor/rules` 还能按目录缩小范围。迁移时逐条问：

```text
所有 ViewModel 暴露不可变 StateFlow
→ 根 AGENTS.md：稳定、全项目工程约束

data/local 下改 Room Schema 必须增加 MigrationTest
→ 该目录的 AGENTS.md，或 Android Review Skill 的 Room reference

排查 Compose 重组性能的方法
→ performance Skill：只在相关任务加载

生成版本发布说明
→ 显式 Skill 或自动化脚本，不常驻上下文

已经由 ktlint 确定执行的格式规则
→ 删除文字规则，交给工具和 CI
```

转换后不要同时保留两份真相。双轨期可保留原文件，但在清单中标记新位置、冻结日期和删除门槛。

## 第 2 步：把 Commands 变成能力合同

对每个 `.cursor/commands/*.md` 补齐：

1. 什么时候应该触发，什么时候不触发；
2. 必须读取哪些项目文件；
3. 输入缺失时是否停止；
4. 可以修改什么、禁止修改什么；
5. 产物是什么；
6. 运行哪些验证；
7. 遇到设备、凭据或产品决策时怎样交接。

成熟高频流程进入 `.agents/skills/<name>/SKILL.md`；只定义文档形状的进入 `specs/000-template`；低频个人快捷词可以直接删除。

迁移后在 Codex 使用 `/skills` 检查发现，再显式调用 `$skill-name` 完成一项真实 Android 任务。文件存在不等于 description 能正确触发。

## 第 3 步：重建外部连接和安全门禁

### MCP

Cursor 与 Codex 都支持 MCP，但不要复制 JSON 后假定身份、OAuth、工具名和批准策略不变。逐个服务：

1. 在 Codex 中重新添加；
2. 先只开放读取工具；
3. 核对账号、组织与项目范围；
4. 对写工具设置 prompt 或更严格批准；
5. 自动化依赖的服务标记为 required，失败时停止。

### Hooks

先写原 Hook 的业务意图，再按 Codex `PreToolUse`、`PostToolUse` 等事件重写。用样例 stdin JSON 做允许与拒绝测试，先观察误报，最后启用阻断。课程的 [`pre_tool_use.py`](./配套文件/PocketTasks-codex/.codex/hooks/pre_tool_use.py) 和 [自动测试](./配套文件/PocketTasks-codex/.codex/hooks/test_pre_tool_use.py) 是最小参照。

### CLI 自动化

Cursor CLI 和 Codex Exec 的非交互权限、输出格式与配置位置不同，并且两边都在演进。不要翻译命令行参数；重新从任务风险设计：Codex Exec 默认只读，写入显式 `--sandbox workspace-write`，结果用 `--json` 或 `--output-schema`，凭据只注入必要进程。

## 第 4 步：选择 Android 的执行环境

| 任务 | 优先环境 | 原因 |
|---|---|---|
| Compose Preview、Profiler、设备交互 | Android Studio + Codex Local | 依赖本地图形和设备工具 |
| 独立功能实现 | Codex App Worktree | 与当前工作区隔离 |
| 代码调查、JVM 测试、文档 | Local Worktree 或 Cloud | 环境较轻，易复现 |
| 仪器测试、历史 Room 升级 | 有 SDK 与模拟器的本地/自托管环境 | 托管云环境未必有设备 |
| 定时只读报告 | Scheduled task | 已稳定方法按周期运行 |
| PR 补丁建议 | Codex Action | API Key 与写权限可分 Job |

映射 Cursor Background Agent 时，逐项核对 JDK、Android SDK、Gradle 缓存、网络白名单、Secrets、分支归属、设备能力和产物回传。只说“都能后台跑”没有工程意义。

## 两周双轨，不比生成速度

### 第一周：共享合同

Cursor 与 Codex 共用新的根 `AGENTS.md`、项目宪法、Spec 模板和确定性验证命令。Cursor 专用资产冻结，Codex 只承接一个边界清楚的功能。

### 第二周：相似任务对照

选择复杂度相近的两项真实工作，一条用旧流程，一条用 Codex。记录：

| 指标 | 记录方式 |
|---|---|
| 首次可评审时间 | 从任务批准到首个可读 diff |
| 人工纠偏次数 | 需求、范围、架构、权限分别计数 |
| 评审有效缺陷 | 去除纯风格意见后的发现数 |
| 返工 | 合并前和合并后分别记录 |
| 验证可信度 | 通过、失败、跳过、未运行是否区分 |
| 权限事件 | 读敏感文件、网络、设备、远端写入 |
| 成本 | 模型用量、CI 时间、工程师审查时间 |

样本量很小时不要宣布“效率提升 40%”。先把异常和定性问题找出来，再累计多个 Sprint。

## 退役与回退

每类资产只有满足以下条件才删除旧版本：

- 新位置有明确负责人；
- 至少通过一次真实任务；
- 同事能从 README 找到入口；
- 权限和凭据重新审查；
- 有 Git 标签、分支或文档记录可回退；
- 删除日期已经通知团队。

如果 Codex 流程失败，回退的是执行入口，不要回滚已经变得更清楚的共享 Spec、测试和项目知识。

## 迁移验收清单

- 所有 Cursor 资产是否有“迁移、合并、删除、保留”结论？
- 长期约束在 `AGENTS.md` 中是否只有一份真相？
- 高频 Commands 是否成为可触发、可验证的 Skills？
- MCP 是否逐个重新认证并从只读开始？
- Hooks 是否按 Codex 事件协议测试，而非复制 schema？
- Android Studio、本地 Worktree、Cloud 和 CI 是否按任务选择？
- `codex exec` 是否明确沙箱、输出与凭据？
- 双轨指标、退役日期和回退点是否可查？

## 小结

从 Cursor 迁移到 Codex，不是把 `.cursor` 改名为 `.codex`。先拆开编辑器、项目知识、可复用方法、外部连接、安全政策和执行环境，再逐项转换与验证。Android Studio 可以继续承担它最擅长的设备与图形工具，Codex 则成为仓库级工作流的 Agent 入口。

## 延伸阅读

- [Cursor Rules](https://docs.cursor.com/context/rules)
- [Cursor CLI Permissions](https://docs.cursor.com/cli/reference/permissions)
- [Cursor MCP](https://docs.cursor.com/context/model-context-protocol)
- [Codex 官方文档](https://developers.openai.com/codex/)
- [课程迁移盘点脚本](./配套文件/PocketTasks-codex/scripts/audit-cursor-assets.sh)
