# 06｜项目记忆：用 AGENTS.md 说清团队约定

上一讲结束时，Codex 已经能沿着代码追踪数据流。但每次新会话，你仍可能重复解释：“这是 Compose 项目”“不要直接让 ViewModel 访问 DAO”“用 Gradle Wrapper”“没有模拟器就不要声称 UI 测试通过”。

`AGENTS.md` 就是解决这种重复的项目记忆。

## 把它当成新同事的第一页

一份好的 `AGENTS.md` 不会复制整本架构文档，也不会罗列所有目录。它应该帮助刚进入仓库的协作者迅速回答：

- 这个项目解决什么问题；
- 从哪里开始阅读；
- 修改时必须遵守哪些约束；
- 怎样构建和验证；
- 哪些操作风险很高；
- 更详细的资料在哪里。

换句话说，它是路标，不是百科全书。

## Codex 怎样发现 AGENTS.md

Codex 会把指令按作用域组合起来。用户级可以在 `~/.codex/AGENTS.md` 放个人通用偏好；进入项目后，它会从项目根目录沿着当前工作目录向下查找 `AGENTS.md`。更靠近当前文件的指令优先级更高。

同一目录如果存在 `AGENTS.override.md`，它会作为该目录的覆盖文件。这个机制适合特殊子树，而不适合随手绕过根规则。

对一个多模块 Android 仓库，可以这样组织：

```text
PocketTasks/
├── AGENTS.md                  # 全仓库约定
├── app/
│   └── AGENTS.md             # Android 应用模块细则
└── core/database/
    └── AGENTS.md             # Room Schema 与迁移要求
```

当 Codex 在 `core/database` 下工作时，会同时获得根规则和数据库规则；发生冲突时，距离更近的规则胜出。

## 为 PocketTasks 写第一版

先从够用的内容开始：

```markdown
# PocketTasks 工作指南

## 项目定位
离线优先的 Android 待办应用。Kotlin + Compose，Room 是任务数据的本地事实源。

## 阅读入口
- UI：app/src/main/java/.../ui/tasks/
- 数据：app/src/main/java/.../data/
- 架构决定：docs/constitution.md

## 实现约束
- Composable 不直接访问 DAO 或 Repository。
- ViewModel 暴露不可变 uiState，UI 通过事件回传意图。
- 数据层通过 Repository 暴露数据；不要为了形式给简单转发新增 Use Case。
- 用户可见文案进入资源文件；新增交互要考虑无障碍语义。
- Room Schema 改动必须提供 Migration 和迁移测试，不允许破坏性迁移。

## 验证
- 单元测试：./gradlew :app:testDebugUnitTest
- Lint：./gradlew :app:lintDebug
- 构建：./gradlew :app:assembleDebug
- 设备测试：./gradlew :app:connectedDebugAndroidTest（需要设备或模拟器）

## 工作方式
- 修改前先调查现有模式并给出计划。
- 只改任务范围内的文件，不整理无关代码。
- 收尾时列出实际执行的验证和未执行项。
```

这里没有写 Kotlin 语法规则，因为格式化器和 Lint 更适合做这件事；也没有列每个类，因为目录会变化，搜索比静态清单可靠。

## 用分层规则处理 Android 特殊风险

数据库目录比普通 UI 目录风险更高，可以增加一个局部文件：

```markdown
# Room 数据库附加规则

- 修改 Entity、索引或表结构前，先读取导出的 Schema 与全部 Migration。
- 禁止使用 fallbackToDestructiveMigration。
- 每次版本升级提供 MigrationTestHelper 测试。
- 不修改已经发布版本对应的历史 Schema 文件。
- 完成时报告升级路径，例如 3 → 4，而不只报告全新安装成功。
```

这样，只有进入数据库子树的任务才加载细节，根文件仍然容易阅读。

## 常见失败方式

### 把愿望写成规则

“代码要优雅、测试要充分”无法执行。改成“ViewModel 不暴露 MutableStateFlow”“每条验收标准至少对应一种验证证据”。

### 把规则写成小说

文件越长，关键约束越容易被淹没。背景说明放链接，入口文件只保留行动所需的信息。

### 写了不存在的命令

复制模板后最常见的问题，是模块名与 Gradle task 不匹配。所有命令都要在当前仓库验证。

### 同一条规则四处复制

重复会产生冲突。根文件写共同约定，目录文件只写增量。如果某项原则需要深入解释，链接到 `docs/constitution.md`。

## 检查 Codex 实际读到了什么

你可以让 Codex 在不修改文件的情况下复述当前生效的项目指令，并标注来源。也可以使用 `/status` 了解当前会话与工作目录。第一次引入分层 AGENTS 时，分别从根目录、`app` 和数据库目录启动一次调查，验证作用域是否符合预期。

如果项目尚无 `AGENTS.md`，CLI 的 `/init` 可以生成起点，但生成后仍要由团队删改和验证。自动生成的说明不是项目真相，代码和团队共识才是。

## 小结

`AGENTS.md` 是 Codex 的项目入职页：根文件建立共同语言，目录文件补充局部风险，覆盖文件只处理真正的例外。内容要短、具体、可验证，并把详细背景链接出去。

可是，“做事入口”与“为什么坚持这些原则”不是同一类信息。下一讲，我们会单独建立 `constitution.md`，让架构取舍在需求变化时仍有稳定依据。

## 思考题

1. 你的根 AGENTS.md 中，哪三条规则应该对所有模块生效？
2. 哪个 Android 子目录风险最高，值得增加局部 AGENTS.md？

## 延伸阅读

- [Codex：使用 AGENTS.md 提供自定义指令](https://developers.openai.com/codex/guides/agents-md/)

