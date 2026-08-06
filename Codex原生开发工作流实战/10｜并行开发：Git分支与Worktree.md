# 10｜并行开发：Git、分支与 Worktree

上一讲给命令加上了安全边界，但还有一种常见事故不来自危险命令，而来自混乱现场：你正在修复 Room 迁移，Codex 又在同一个工作区实现筛选；两个任务都修改 ViewModel，生成文件和 Gradle 输出混在一起，最后谁也说不清哪项改动属于谁。

Git 负责记录历史，Worktree 负责隔离同时存在的工作现场。它们共同构成 Codex 开发最实用的“时间与空间安全”。

## 先理解三个不同概念

提交是一个可回看的历史节点；分支是指向一条演进线的名字；Worktree 是一个实际文件目录，让不同分支可以同时被检出。

```text
PocketTasks/                 main：你正在处理紧急缺陷
PocketTasks-filter/          codex/task-filter：Codex 实现筛选
PocketTasks-room-migration/  codex/room-v4：另一项数据库工作
```

三个目录共享同一个 Git 仓库对象，但源码、未提交修改和构建目录彼此隔离。Android 项目会产生大量 `build/` 输出，这种物理隔离尤其有价值。

## 什么时候值得使用 Worktree

不必为改一行文案创建 Worktree。下面几类任务更适合隔离：

- 预计修改跨多个文件或模块；
- 需要长时间运行构建或设备测试；
- 想同时比较两种架构方案；
- 当前工作区已有未提交改动；
- 任务可以独立评审和提交。

如果两项工作注定要频繁修改同一批核心文件，并行可能只会把冲突推迟到合并时。Worktree 能隔离现场，却不能消除逻辑耦合。

## 手工建立一个 Android 任务 Worktree

先确认主工作区状态：

```bash
git status --short
git branch --show-current
```

然后在父目录创建新工作树与分支：

```bash
git worktree add ../PocketTasks-filter -b codex/task-filter
cd ../PocketTasks-filter
./gradlew :app:testDebugUnitTest
```

最后一条命令非常重要。它证明新工作树在修改前具备可用基线。若失败，应先记录为基线问题，不要把责任留给未来的 diff。

Codex 桌面应用也可以管理 Worktree，并支持在本地工作区与 Worktree 之间交接任务。无论通过界面还是 Git 命令，背后的原则相同：一个任务、一条清楚的改动线、一个可复现基线。

## 给 Codex 一份干净的任务合同

在隔离目录启动任务时，告诉 Codex：

```text
你正在 codex/task-filter 分支的独立 Worktree 中。
只实现 specs/001-task-filter/ 中已批准的范围。
开始前运行目标单元测试并记录基线；
每完成一个 task 更新 tasks.md；
不要修改签名、版本号、发布配置或无关格式；
收尾时展示 diff、验证结果与尚未运行的设备测试。
```

Worktree 提供空间边界，Spec 提供业务边界，AGENTS.md 提供工程边界。三者叠加后，“隔离”才不仅是换了一个目录。

## 提交应当讲清一件事

AI 能快速改动很多文件，更要主动控制提交粒度。一个好提交应该：

- 对应一个可解释的任务或行为；
- 包含必要测试；
- 不夹带无关格式化或生成物；
- 提交信息说明意图，而不只是“update files”。

例如：

```text
feat(tasks): persist and restore task filter

- store TaskFilter in DataStore
- derive filtered UI state in TasksViewModel
- cover process recreation contract with tests
```

是否真的提交、推送或开 PR，仍由任务授权决定。Codex 不应把“实现功能”自动扩大为“发布到远端”。

## 合并前不是只看冲突

Git 能发现文本冲突，却看不见语义冲突。一个分支改变 `TaskEntity`，另一个分支改变筛选查询，即使自动合并成功，组合后的数据库行为也可能错误。

合并前至少做三件事：阅读相对于目标分支的完整 diff；重新运行组合后的测试和 Lint；检查 Spec 与宪法是否仍然满足。对于 Room Schema、依赖版本和 Manifest，优先安排人工复核。

## 不要把 Worktree 当备份系统

未提交改动仍然可能丢失，工作树也不替代远端备份和正式提交。完成或放弃工作树前，先确认状态、分支和需要保留的结果。清理工作树属于可能丢数据的动作，不应由模糊请求触发。

## 小结

Git 提供可追溯历史，Worktree 提供并行任务的物理隔离。它们不能替代任务拆分，却能让每个 Codex 任务拥有清楚现场、可靠基线和独立 diff。

到这里，安全和隔离仍依赖人在关键时刻记得执行。下一讲会引入 Hooks：在工具调用、会话开始或任务结束等事件发生时，自动运行团队约定的检查。

## 思考题

1. 你们当前最常发生的语义合并冲突是什么？
2. 哪类任务应该禁止与 Room Schema 修改并行？

## 延伸阅读

- [Codex Worktrees](https://developers.openai.com/codex/app/worktrees/)
- [Git worktree 文档](https://git-scm.com/docs/git-worktree)

