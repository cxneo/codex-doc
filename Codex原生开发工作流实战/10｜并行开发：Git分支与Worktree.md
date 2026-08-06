# 10｜并行开发：Git、分支与 Worktree

你正在修复 Room 迁移，Codex 同时实现任务筛选。若两个任务共用一个目录，源码、未提交修改和 `build/` 输出会搅在一起。Worktree 的价值不是“多开窗口”，而是给每项工作一个独立现场。

这一讲要分清两套操作：任何终端都能使用的 Git Worktree，以及 Codex 桌面应用提供的托管 Worktree。它们共享 Git 原理，但交接、清理和界面能力不同。

## 先建立正确心智模型

```text
一次提交：可回看的历史节点
一条分支：某条演进线的名字
一个 Worktree：实际存在的一份工作目录
一次 Codex 任务：围绕目标、上下文与验证展开的会话
```

可能形成这样的布局：

```text
PocketTasks/                 main：人工处理紧急缺陷
PocketTasks-filter/          codex/task-filter：实现筛选
PocketTasks-room/            codex/room-v2-audit：调查迁移
```

目录共享 Git 对象库，但未提交源码和 Android 构建输出彼此隔离。Worktree 只能隔离现场，不能消除两个方案同时修改 `TaskViewModel` 时的语义冲突。

## 路线一：用 Git 手工创建永久 Worktree

先确认当前现场：

```bash
git status --short
git branch --show-current
git worktree list
```

再从父目录创建任务分支：

```bash
git worktree add ../PocketTasks-filter -b codex/task-filter
cd ../PocketTasks-filter
./gradlew :app:testDebugUnitTest
```

最后一条是基线测试。若它在任何改动之前失败，要把原因标成基线问题，不能让未来的 diff 背锅。

这种 Worktree 由你自己管理，目录和分支会一直保留，适合需要长期维护或交给同事的任务。

## 路线二：使用 Codex 桌面应用的托管 Worktree

在桌面应用创建任务时，可以选择：

- `Local`：直接使用现有目录，适合只读调查或你愿意共享现场的短任务；
- `Worktree`：在 Codex 管理的独立目录中工作，适合会改文件的并行任务。

托管 Worktree 通常位于 `$CODEX_HOME/worktrees`，默认以 detached HEAD 开始。不要因为看到 detached HEAD 就强行修复；先在应用中选择 **Create branch**，或在准备保留结果时明确创建分支。

任务可以在 Local 与 Worktree 之间使用 **Handoff** 交接。交接依赖 Git 操作：已跟踪文件可以移动，但 `.gitignore` 排除的本地文件不会自动跟着走。

## Android 项目为什么常在新 Worktree 构建失败

最常见原因是 `local.properties` 被忽略，其中保存了 Android SDK 路径。课程项目根目录提供了 [`.worktreeinclude`](./配套文件/PocketTasks-codex/.worktreeinclude)：

```gitignore
local.properties
```

Codex 创建本地托管 Worktree 时，会把匹配的已忽略文件复制过去。只放运行项目必需的本地配置；不要加入 `.jks`、服务账号、生产 `.env` 或其他密钥。

如果项目还需要生成配置，在桌面应用的 **Local environments** 中定义初始化脚本或常用动作。环境配置解决“怎样准备现场”，`AGENTS.md` 解决“怎样在项目里工作”，两者不要混写。

## 给隔离任务一份合同

进入 Worktree 后，可以这样发出任务：

```text
你在独立 Worktree 中实现 specs/001-task-filter/ 已批准范围。
先报告当前提交、分支状态和目标测试基线。
只处理 tasks.md 中尚未完成的任务；不要修改 Room Schema、签名与发布配置。
每完成一个可验证小步，更新任务状态并报告实际测试。
收尾时给出完整 diff、验证结果、未运行项与建议保留的分支名。
```

Worktree 提供空间边界，Spec 提供业务边界，`AGENTS.md` 提供工程边界；缺一项，现场仍可能干净但结果失控。

## Codex 有没有 Claude Code 那种 Checkpoint 回退

不要把两者说成完全相同。Codex CLI 当前没有一个等价于 Claude Code checkpoint rewind 的通用命令。可靠回退仍以 Git 为准：小步 diff、小步测试、需要保留时提交。

Codex 桌面应用的 Changes 面板可以按文件或 hunk 查看、暂存和撤销修改；托管 Worktree 清理时也可能保留恢复快照。这些能力很方便，但不是版本历史的替代品。撤销前必须确认目标，尤其不能覆盖同一文件中的个人改动。

## 合并前做组合验证

文本无冲突不代表行为无冲突。合并任务分支后至少执行：

```bash
git diff main...HEAD
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug
```

如果修改 Room、Manifest、依赖或 Compose 行为，再追加迁移测试、仪器测试和人工设备检查。评审的是组合结果，而不是两个分支各自曾经绿过。

## 清理前的四个问题

删除 Worktree 可能丢失未提交内容。先回答：

1. `git status --short` 是否为空？
2. 有价值的改动是否已在明确分支或补丁中？
3. 任务结果是否已经评审、合并或明确放弃？
4. 目录是否真的是目标 Worktree，而不是主仓库？

Codex 桌面应用会清理一部分旧托管 Worktree，默认保留数量也可能随版本和设置变化。长期成果必须进入正式分支或 PR，不能依赖托管目录一直存在。

## 动手练习

在课程项目中完成一次不改代码的演练：

```bash
git worktree list
git status --short
```

如果使用桌面应用，再新建一个 Worktree 任务，让 Codex只运行 `./gradlew :app:testDebugUnitTest` 并说明 `local.properties` 来自哪里。确认后创建任务分支，但不要推送。

## 小结

Git 提供历史，Worktree 提供并行现场，Codex 桌面应用补上托管、交接和逐块审查。真正的安全网仍是：清楚基线、明确任务合同、可验证小步和正式 Git 历史。

下一讲把“每次记得检查”变成自动门禁：用 Hooks 在工具执行前读取结构化事件并阻断危险动作。

## 延伸阅读

- [Codex Worktrees](https://developers.openai.com/codex/app/worktrees/)
- [Codex Local environments](https://developers.openai.com/codex/app/local-environments/)
- [Git worktree](https://git-scm.com/docs/git-worktree)

