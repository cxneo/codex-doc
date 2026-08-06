# Codex 原生开发工作流实战

> 一套可以发给 Android 团队共同学习、照着练习，并逐步落到真实项目中的 Codex 工程化教程。

很多 Codex 教程从“安装一条命令”开始，到“生成一段代码”结束。这样的内容能让人迅速获得新鲜感，却很难回答团队真正关心的问题：Codex 如何理解项目边界？谁来约束它？怎样让一次成功变成可重复的流程？代码写完以后，测试、评审、CI 和维护又该怎样接上？

这套课程不把 Codex 当成更快的代码补全，而把它放回软件工程的完整生命周期里。我们会从一个模糊需求出发，逐步建立项目上下文、规范、安全边界和可复用能力，最后让同一个 Android 项目走完需求、设计、实现、测试、审查、交付与维护。

第一次阅读请从[开篇词：当 AI 真正进入你的开发工作流](开篇词｜当AI真正进入你的开发工作流.md)开始。

## 贯穿案例：PocketTasks

`PocketTasks` 是一个离线优先的 Android 待办应用。它足够小，可以让第一次接触 Codex 的同学理解全貌；又足够真实，会遇到状态管理、数据持久化、并发、数据库迁移、界面测试和构建交付等典型问题。

课程中的技术基线是：

- Kotlin 与 Gradle Wrapper；
- Jetpack Compose 界面；
- ViewModel、单向数据流与不可变 UI State；
- Repository + Room，本地数据库是事实源；
- 只有业务逻辑确实需要复用或隔离时，才引入 Use Case；
- JVM 单元测试、Compose 仪器测试、Lint 与 CI。

这套结构参考 Android 官方架构建议，但不会把“推荐”误写成不可违背的教条。工程化的意义不是堆层次，而是让每个决定都有理由、能验证、可维护。

## 课程地图

### 第一部分：先建立正确的世界观

1. [范式演进：从 AI 助手到 AI 原生工作流](01｜范式演进：从AI助手到AI原生工作流.md)
2. [核心引擎：规范驱动开发](02｜核心引擎：规范驱动开发.md)
3. [系统地图：为什么选择 Codex](03｜系统地图：为什么选择Codex.md)

### 第二部分：让 Codex 真正读懂 Android 项目

4. [环境搭建：安装、登录与第一次安全会话](04｜环境搭建：安装登录与第一次安全会话.md)
5. [核心交互：上下文注入与命令执行](05｜核心交互：上下文注入与命令执行.md)
6. [项目记忆：用 AGENTS.md 说清团队约定](06｜项目记忆：用AGENTS.md说清团队约定.md)
7. [项目宪法：把重要约束变成决策依据](07｜项目宪法：把重要约束变成决策依据.md)
8. [可复用任务：从临时提示词到团队入口](08｜可复用任务：从临时提示词到团队入口.md)

### 第三部分：给自主执行加上护栏和扩展能力

9. [安全边界：审批、沙箱与 Rules](09｜安全边界：审批沙箱与Rules.md)
10. [并行开发：Git、分支与 Worktree](10｜并行开发：Git分支与Worktree.md)
11. [事件自动化：用 Hooks 守住关键节点](11｜事件自动化：用Hooks守住关键节点.md)
12. [外部连接：MCP 与 Plugins](12｜外部连接：MCP与Plugins.md)
13. [专家能力：用 Skills 封装团队方法](13｜专家能力：用Skills封装团队方法.md)
14. [协作分工：用 Subagents 拆解复杂任务](14｜协作分工：用Subagents拆解复杂任务.md)
15. [无人值守：Codex Exec、SDK 与 CI](15｜无人值守：CodexExec、SDK与CI.md)

### 第四部分：把能力装进同一套开发驾驶舱

16. [顶层设计：搭建 Android 项目驾驶舱](16｜顶层设计：搭建Android项目驾驶舱.md)
17. [需求与设计：把想法写成可验证的 Spec](17｜需求与设计：把想法写成可验证的Spec.md)
18. [计划与任务：把 Spec 编译成实施路线](18｜计划与任务：把Spec编译成实施路线.md)
19. [编码与测试：让 Codex 按 TDD 推进](19｜编码与测试：让Codex按TDD推进.md)
20. [协作与审查：从“代码能跑”到“改动可信”](20｜协作与审查：从代码能跑到改动可信.md)
21. [构建与交付：把本地成功带进 CI](21｜构建与交付：把本地成功带进CI.md)
22. [维护与重构：在证据链上做外科手术](22｜维护与重构：在证据链上做外科手术.md)

### 第五部分：从现有工具平稳迁移

23. [从 Claude Code 迁移到 Codex](23｜从ClaudeCode迁移到Codex.md)
24. [从 Cursor 迁移到 Codex](24｜从Cursor迁移到Codex.md)

最后还有一篇[结束语](结束语｜把一次聪明变成团队能力.md)，帮团队把课程变成自己的落地路线。

## 建议怎样学习

第一次阅读，不要急着复制所有配置。先顺序读到第 8 讲，在自己的 Android 项目中完成一次“理解项目—制定计划—修改—验证”的闭环。之后再根据真实痛点选择 Rules、Hooks、MCP、Skills 或 Subagents。

如果用于团队培训，可以每周安排两讲：前半小时讨论概念，后半小时在 `PocketTasks` 或团队项目里完成本讲实践。每次实践只提交一项新增能力，让评审者看得懂这项能力为什么存在。

课程的参考配置位于 [`配套文件/PocketTasks-codex`](配套文件/PocketTasks-codex)。它不是万能模板，而是一份“设计意图可见”的起点。复制之前，请先读第 16 讲。

## 资料时效与官方入口

本文按 2026 年 8 月的 Codex 与 Android 官方资料编写。产品界面与实验能力会变化；遇到命令或配置差异，请优先以官方文档为准：

- [Codex 官方文档](https://developers.openai.com/codex/)
- [Android 应用架构指南](https://developer.android.com/topic/architecture)
- [Jetpack Compose 文档](https://developer.android.com/develop/ui/compose/documentation)
- [Room 文档](https://developer.android.com/training/data-storage/room)
- [Android 命令行测试](https://developer.android.com/studio/test/command-line)
