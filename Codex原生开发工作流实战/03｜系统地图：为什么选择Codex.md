# 03｜系统地图：为什么选择 Codex

上一讲建立了 SDD 的编译链，但我们还没有回答一个现实问题：同样是 AI 编程，为什么这套课选择 Codex？

不是因为它有一个“更神奇的聊天框”，而是因为 Codex 提供了从交互式开发到后台执行、从个人约定到仓库规范的一组连续表面。你可以在不同场景里换驾驶位置，却不必换掉项目本身的规则。

## 先把 Codex 看成一个系统

Codex 常用的工作表面包括：

- CLI：离仓库和 Shell 最近，适合探索、修改、执行 Gradle 命令与脚本化；
- IDE 扩展：边看 Kotlin、Compose Preview 和诊断边协作，适合日常编辑；
- Codex 桌面应用：适合同时管理多个任务、在本地与 Worktree 之间切换；
- Cloud：让任务在托管环境中执行，适合耗时、可并行且环境已准备好的工作；
- `codex exec`、SDK 与 GitHub Action：把能力接入脚本、CI 和自动化。

它们不是五套互不相干的产品。真正可迁移的核心仍然在仓库里：`AGENTS.md`、`.codex/config.toml`、Rules、Hooks、Skills、Agent 配置、Spec 和测试命令。

## 一项 Android 任务怎样穿过这些表面

假设 PocketTasks 的任务列表在旋转屏幕后重复弹出错误 Snackbar。

你可以先在 IDE 中选中相关 ViewModel，请 Codex解释 `StateFlow` 与一次性事件的当前处理方式；再在 CLI 中让它搜索所有事件消费点并运行目标单元测试；如果需要比较两种重构方案，可以在桌面应用中把一个方案放入隔离 Worktree；最后由 CI 执行全量 `testDebugUnitTest`、Lint 和必要的设备测试。

工具表面在变化，但任务的约束没有变化：不能把一次性事件误当持久 UI State、不能破坏现有导航、必须给出可复现测试。

这就是“项目规则高于聊天窗口”的意义。

## 四个选择维度

不要问“哪个入口最强”，先问当前任务需要什么。

| 场景 | 更合适的入口 | 原因 |
|---|---|---|
| 正在阅读一个 Composable，想局部解释或修改 | IDE 扩展 | 文件与选区上下文自然 |
| 要跨目录追踪 Repository、DAO 与测试 | CLI | 搜索和命令执行直接 |
| 同时尝试两个较大方案 | 桌面应用 + Worktree | 改动隔离，便于比较 |
| 耗时任务，不必持续盯着 | Cloud | 可以后台执行 |
| 要给每个 PR 做结构化检查 | `codex exec` 或 Action | 非交互、结果可机器读取 |

同一任务也可以跨表面接力。关键是把长期信息写入仓库，而不是依赖某个窗口的对话历史。

## Codex 工程化的六层结构

为了后面不被名词淹没，我们先建立一张地图：

```text
任务层        Spec / Plan / Tasks
知识层        AGENTS.md / constitution.md
能力层        Skills / Agents / MCP / Plugins
控制层        Sandbox / Approval / Rules / Hooks
执行层        CLI / IDE / App / Cloud / Exec
证据层        Diff / Tests / Lint / Build / Review
```

任务层说清“这次要做什么”；知识层说明“这个项目一贯怎样做”；能力层封装“遇到某类问题应该怎样做”；控制层决定“哪些动作可以发生”；执行层负责真正行动；证据层回答“凭什么相信已经完成”。

当结果不理想时，这张地图也能帮助定位：需求误解通常查任务层，架构跑偏查知识层，危险命令查控制层，漏测则查证据层。

## Codex 不替代 Android 专业工具

Codex 能调用 Gradle、阅读测试结果和修改项目，但它不会让 Android Studio、模拟器、Profiler、Layout Inspector 或 Play Console 失去价值。

正确关系是：Codex 帮你组织调查、执行和解释；专业工具产生平台事实；工程师做最终判断。例如性能问题不能只凭代码形状下结论，应让 Codex提出假设，再用 Macrobenchmark、Profiler 或系统跟踪收集证据。

## 本讲实践：为一次任务选择驾驶位

从项目积压中选三个任务：一个单文件小改、一个跨层功能、一个耗时调查。分别写下：

1. 主要在哪个 Codex 表面开始；
2. 哪些项目规则必须从仓库注入；
3. 完成时必须交出什么证据；
4. 哪一步仍然需要 Android 专业工具或人工判断。

这个练习会迫使你把“我想用 AI”改写成“我想用哪一种执行方式解决哪一种工程问题”。

## 小结

Codex 的价值不只在模型能力，而在一套可以共享项目规则、切换执行表面并保留证据链的系统。选择入口时看任务，而不是寻找唯一正确的界面。

地图已经有了。下一讲，我们从最朴素的地方开始：准备 Android 环境、安装登录，并完成第一次安全会话。

## 延伸阅读

- [Codex 官方文档](https://developers.openai.com/codex/)
- [Codex CLI](https://developers.openai.com/codex/cli/)
- [Codex IDE 扩展](https://developers.openai.com/codex/ide/)
- [Codex Cloud](https://developers.openai.com/codex/cloud/)

## 思考题

1. 你每天处理的 Android 任务中，哪些最适合 IDE 内协作，哪些更适合 CLI 或隔离 Worktree？
2. 如果切换 Codex 表面后项目约束就丢失，说明哪些信息还没有进入仓库？
