# 13｜专家能力：用 Skills 封装团队方法

上一讲让 Codex 连接了外部世界，但“能访问更多信息”不等于“知道怎样专业地处理问题”。一个资深 Android 工程师评审改动时，会主动检查状态所有权、生命周期、协程、Room 迁移、无障碍和测试证据。这套思考方式，正适合封装为 Skill。

## Skill 不是一篇知识文章

Skill 是一个自包含目录，至少包含 `SKILL.md`。它的元数据告诉 Codex 何时应该使用，正文告诉 Codex 被触发后怎样执行，还可以按需携带 references、scripts 和 assets。

```text
android-code-review/
├── SKILL.md
├── agents/
│   └── openai.yaml
└── references/
    ├── compose-and-state.md
    └── room-and-data.md
```

这和把一段提示词存在笔记软件里有三点区别：它会被 Codex 发现；它能随仓库版本演进；它可以只在需要时逐层加载资料，避免所有任务都背着整本 Android 手册。

## Skill 怎样被发现与触发

项目 Skill 放在 `.agents/skills/<skill-name>/SKILL.md`。Codex 会从当前目录向项目根目录扫描 `.agents/skills`；个人 Skill 可以放在用户级 Skills 目录中。

Skill 有两种触发方式：

- 显式：在请求中写 `$android-code-review`，或通过 `/skills` 选择；
- 隐式：请求与 Skill 的 description 明确匹配时，Codex 自动使用。

因此，description 不是宣传语，而是路由规则。要同时写清“做什么”和“何时使用”：

```yaml
---
name: android-code-review
description: Review Android Kotlin changes for correctness, lifecycle, Compose state, coroutines, Room migrations, accessibility, and verification gaps. Use when reviewing a diff, branch, commit, or pull request in an Android project before merge.
---
```

如果 description 只写“Android expert”，Codex 不知道是用来写页面、排障还是评审。

## 为 PocketTasks 设计评审流程

这个 Skill 的输入可以是当前 diff、某个提交或目标分支；输出应是按严重程度排列的可操作发现。核心步骤保持简短：

1. 读取 AGENTS.md、项目宪法与任务 Spec；
2. 确认评审范围和基线；
3. 阅读完整 diff，并追踪受影响的数据流；
4. 根据文件类型按需读取 Compose 或 Room 参考；
5. 运行不会改动产品逻辑的目标验证；
6. 只报告有证据的问题，给出文件、位置、触发场景和影响；
7. 明确没有运行的设备或环境验证。

Skill 不应直接修复代码，因为“评审”和“实现修复”是两个不同授权。发现问题后，由用户决定是否进入修复任务。

## 用渐进式披露控制上下文

`SKILL.md` 只保留每次评审都需要的过程。Compose 细则放 `references/compose-and-state.md`，只在 diff 涉及 Composable、ViewModel 或导航时读取；Room 细则只在 Entity、DAO、Database 或 Migration 变化时读取。

这种结构比一份 500 行总清单更好：

```text
元数据：始终可见，用来判断是否触发
SKILL.md：触发后加载，保存核心流程
references：遇到具体改动时才加载
scripts：需要确定性执行时直接运行
```

上下文是一种有限预算。Skill 的目标是提供 Codex 不容易从仓库自行推断的团队方法，而不是重复它已经知道的 Kotlin 基础。

## 让发现具有工程价值

一条差的评审意见是：“这里可能有生命周期问题。”

一条可行动的意见应像这样：

```text
[P1] 不要在 Composable 每次重组时重新启动数据收集

TasksScreen.kt:84 的 launch 直接位于 Composable 函数体。
筛选状态变化会触发重组并创建新的 collector，导致同一错误事件重复消费。
把收集放入以稳定 key 启动的 LaunchedEffect，或将一次性事件建模调整到 ViewModel 边界。
```

它包含严重程度、精确位置、触发条件、用户影响和修正方向。Skill 应明确“没有可定位证据就不报告”，减少泛泛而谈。

## 创建与验证 Skill

课程配套工程已经提供 `android-code-review` 示例。实际创建 Skill 时，推荐使用官方 skill-creator 的初始化脚本生成合法结构，再编辑内容，最后运行其校验脚本检查名称与 frontmatter。

验证不能只看文件格式。至少准备三种前向用例：

- 纯 Compose diff：应读取状态参考，不加载 Room 参考；
- Room Schema diff：应检查 Migration 和升级路径；
- 只有文档改动：不应虚构 Android 运行时风险。

把 Skill 用在真实 diff 上，观察它是否漏掉关键步骤、加载过多资料或产生低价值噪音，再迭代说明。

## 哪些内容不该做成 Skill

一条可以由 Lint 精确判定的格式规则，应交给 Lint；一次性的产品背景，应留在 Spec；必须在命令前阻断的安全政策，应交给 Rules 或 Hook；需要独立并行角色的调查，可能更适合 Subagent。

Skill 擅长的是“可重复但需要判断”的程序性知识。

## 小结

Skills 把团队方法变成可发现、可版本化、按需加载的专家能力。好的 Skill 路由准确、正文精简、引用按需、输出可行动，并且经过真实任务验证。

下一讲会继续拆分复杂度：当一个任务需要并行调查 UI、数据和测试时，怎样用 Subagents 建立职责清楚的协作，而不是让多个 Agent 一起制造噪音。

## 思考题

1. 你们团队哪项能力最依赖某一位资深工程师，且过程可以重复？
2. 这项能力中，哪些适合核心流程，哪些应按需放入 references？

## 延伸阅读

- [Codex Skills](https://developers.openai.com/codex/skills/)

