# Codex 原生开发工作流实战

> 一套可以直接发给 Android 团队阅读、演练和复盘的 Codex 工程化课程。

这不是一本“提示词大全”。课程用一个可运行的 Android 项目，把 Codex 放进需求、设计、编码、测试、审查、CI、维护和工具迁移的完整流程里。读完以后，你应该得到的不是几段聊天记录，而是一套可以留在仓库、交给同事复用的工作方式。

如果这是你第一次打开课程，先读[课程导读：怎么学、怎么练、怎么验收](课程导读｜怎么学怎么练怎么验收.md)。它会告诉你从哪里开始，以及哪些章节暂时可以跳过。

## 先用 5 分钟确认课程能跑

你需要 JDK 17、Android SDK Platform 35、Git、Python 3.11+ 和 Codex。Python 用于校验 TOML 与 Hook；Android Studio 是推荐的 Android 开发环境，但 Codex 当前没有官方 Android Studio / JetBrains IDE 扩展，本课程用 Codex CLI 或桌面应用配合 Android Studio。

```bash
cd 配套文件/PocketTasks-codex
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug
```

两条命令应以 `BUILD SUCCESSFUL` 结束。有设备或模拟器时再运行：

```bash
adb devices
./gradlew :app:connectedDebugAndroidTest
```

`adb devices` 必须显示状态为 `device` 的目标，才能声称设备测试已经执行。若只想检查课程文件、配置和故障实验，不运行 Android 构建：

```bash
ruby 配套文件/validate-course.rb
```

完整验证方式见[版本与兼容性](版本与兼容性.md)。

## 贯穿案例：PocketTasks

`PocketTasks` 是一个离线优先的待办应用。它足够小，可以在培训中看清全貌；也足够真实，会遇到 Compose 状态、Room 持久化、DataStore、协程、数据库迁移、界面测试和 CI 等典型问题。

```text
Compose 事件
  → TaskViewModel / TaskUiState
  → TaskRepository
  → Room + DataStore
  → Flow 回到界面
```

课程的 Android 基线是 Kotlin、Jetpack Compose、ViewModel、单向数据流、Repository、Room、DataStore、JVM 测试、Compose 仪器测试、Lint 与 Gradle Wrapper。只有业务逻辑确实需要复用或隔离时，才引入 Use Case。配套项目位于 [`配套文件/PocketTasks-codex`](配套文件/PocketTasks-codex/README.md)。

## 按你的目标选择路线

不必让所有同事用同一种速度读完 24 讲。

| 你是谁 / 想解决什么 | 建议路线 | 完成后的可见产物 |
|---|---|---|
| 第一次使用 Codex | 开篇、01～06、17～20 | 一次只读调查、一份 AGENTS.md、一条带测试证据的改动 |
| Android 技术负责人 | 01～10、16～22 | 项目宪法、安全边界、Spec/Plan/Tasks、CI 证据链 |
| 平台或效能负责人 | 08～16、21 | Rules、Hooks、MCP、Skill、Subagent、Exec/CI 方案 |
| 从 Claude Code 迁移 | 03、06、08～15、23 | 资产盘点表、差异决策和小范围试点 |
| 从 Cursor 迁移 | 03、05、06、08～10、13、24 | Rules/Commands/MCP 对照表和验证清单 |
| 负责内训 | [讲师手册](讲师手册.md)＋六个模块 | 六次 90 分钟课程、故障实验与评分记录 |

## 课程地图

### 模块一：建立共同语言

1. [范式演进：从 AI 助手到 AI 原生工作流](01｜范式演进：从AI助手到AI原生工作流.md)
2. [核心引擎：规范驱动开发](02｜核心引擎：规范驱动开发.md)
3. [系统地图：为什么选择 Codex](03｜系统地图：为什么选择Codex.md)

### 模块二：让 Codex 读懂项目

4. [环境搭建：安装、登录与第一次安全会话](04｜环境搭建：安装登录与第一次安全会话.md)
5. [核心交互：上下文注入与命令执行](05｜核心交互：上下文注入与命令执行.md)
6. [项目记忆：用 AGENTS.md 说清团队约定](06｜项目记忆：用AGENTS.md说清团队约定.md)
7. [项目宪法：把重要约束变成决策依据](07｜项目宪法：把重要约束变成决策依据.md)
8. [可复用任务：从临时提示词到团队入口](08｜可复用任务：从临时提示词到团队入口.md)

### 模块三：为自主执行加护栏和扩展

9. [安全边界：审批、沙箱与 Rules](09｜安全边界：审批沙箱与Rules.md)
10. [并行开发：Git、分支与 Worktree](10｜并行开发：Git分支与Worktree.md)
11. [事件自动化：用 Hooks 守住关键节点](11｜事件自动化：用Hooks守住关键节点.md)
12. [外部连接：MCP 与 Plugins](12｜外部连接：MCP与Plugins.md)
13. [专家能力：用 Skills 封装团队方法](13｜专家能力：用Skills封装团队方法.md)
14. [协作分工：用 Subagents 拆解复杂任务](14｜协作分工：用Subagents拆解复杂任务.md)
15. [无人值守：Codex Exec、SDK 与 CI](15｜无人值守：CodexExec、SDK与CI.md)

### 模块四：走完 Android 交付闭环

16. [顶层设计：搭建 Android 项目驾驶舱](16｜顶层设计：搭建Android项目驾驶舱.md)
17. [需求与设计：把想法写成可验证的 Spec](17｜需求与设计：把想法写成可验证的Spec.md)
18. [计划与任务：把 Spec 编译成实施路线](18｜计划与任务：把Spec编译成实施路线.md)
19. [编码与测试：让 Codex 按 TDD 推进](19｜编码与测试：让Codex按TDD推进.md)
20. [协作与审查：从代码能跑到改动可信](20｜协作与审查：从代码能跑到改动可信.md)
21. [构建与交付：把本地成功带进 CI](21｜构建与交付：把本地成功带进CI.md)
22. [维护与重构：在证据链上做外科手术](22｜维护与重构：在证据链上做外科手术.md)

### 模块五：迁移与落地

23. [从 Claude Code 迁移到 Codex](23｜从ClaudeCode迁移到Codex.md)
24. [从 Cursor 迁移到 Codex](24｜从Cursor迁移到Codex.md)

完成章节练习后，用[结课实践](结课实践.md)独立走完一次小功能闭环，再读[结束语](结束语｜把一次聪明变成团队能力.md)。

## 怎样把课程用于团队

建议每次培训只引入一个新机制，并要求学员留下一个可评审产物：调查记录、项目规则、Spec、失败测试、审查报告或 CI 日志。不要以“听完多少章节”作为完成标准。

团队分发前，负责人可以使用这些材料：

- [讲师手册](讲师手册.md)：六次 90 分钟课程、课前准备、演示脚本和常见故障；
- [术语表](术语表.md)：统一 Codex、Android 和工作流词汇；
- [版本与兼容性](版本与兼容性.md)：验证基线、实验能力和升级策略；
- [章节审校记录](章节审校记录.md)：相对 Claude Code 参考体系的逐讲复核结果；
- [结课实践](结课实践.md)：100 分验收量表；
- [CHANGELOG](CHANGELOG.md)：课程维护历史。

## 阅读约定

- “通过”必须能对应测试、命令、截图或人工步骤；“未运行”和“跳过”不能写成“通过”。
- 示例配置用于教学，不代表可以不经审查直接复制到生产仓库。
- Claude Code、Cursor 和 Codex 概念相似但配置格式与执行语义不同；迁移时按行为重建，不做机械改名。
- 产品界面、实验能力和配置格式会变化。遇到差异时，先看[版本与兼容性](版本与兼容性.md)，再回到官方文档。

## 官方入口与资料时效

课程按 2026 年 8 月的资料与本地验证编写。主要入口：

- [Codex 官方文档](https://developers.openai.com/codex/)
- [Claude Code 官方文档](https://code.claude.com/docs/zh-CN/overview)
- [Android 应用架构指南](https://developer.android.com/topic/architecture)
- [Jetpack Compose 文档](https://developer.android.com/develop/ui/compose/documentation)
- [Room 文档](https://developer.android.com/training/data-storage/room)
- [Android 命令行测试](https://developer.android.com/studio/test/command-line)
