# 19｜编码与测试：让 Codex 按 TDD 推进

Spec 已经定义行为，Plan 已经确定数据流，Tasks 也排好了依赖。现在终于可以写 Kotlin。但这一讲的重点不是让 Codex 一口气“生成整个功能”，而是控制反馈半径：一次只证明一个行为。

## TDD 对 Agent 的额外价值

红—绿—重构对人类开发者并不陌生。对 Codex 来说，它还有一个重要作用：失败测试提供机器可读的目标，最小实现限制改动范围，回归测试阻止它在后续步骤中破坏前面的行为。

```text
Red：写出表达需求的失败测试，确认失败原因正确
  ↓
Green：只写足够通过的实现
  ↓
Refactor：在测试保护下改善结构
  ↓
记录证据，进入下一项 Task
```

如果测试一开始就通过，可能测试没有覆盖新行为；如果失败原因是编译环境或旧缺陷，也不能把它当作预期红灯。

## T001：先定义筛选行为

给 Codex 的任务应当足够窄：

```text
执行 tasks.md 的 T001，只创建 TaskFilter 的行为测试，不实现产品代码。
覆盖 ALL、ACTIVE、COMPLETED，并证明输入顺序和原列表不被改变。
运行最窄测试，确认失败与 TaskFilter 尚未实现直接相关，然后停下汇报。
```

测试可能表达为：

```kotlin
class TaskFilterTest {
    private val active = Task(id = "1", title = "写周报", isCompleted = false)
    private val completed = Task(id = "2", title = "提交报销", isCompleted = true)
    private val tasks = listOf(active, completed)

    @Test
    fun active_keepsOnlyIncompleteTasks() {
        assertEquals(listOf(active), TaskFilter.ACTIVE.applyTo(tasks))
    }

    @Test
    fun completed_keepsOnlyCompletedTasks() {
        assertEquals(listOf(completed), TaskFilter.COMPLETED.applyTo(tasks))
    }

    @Test
    fun all_preservesOrderAndItems() {
        assertEquals(tasks, TaskFilter.ALL.applyTo(tasks))
    }
}
```

运行目标测试：

```bash
./gradlew :app:testDebugUnitTest --tests '*TaskFilterTest'
```

Codex 要报告失败摘要，而不是粘贴几百行 Gradle 日志。我们确认失败确实因为 `TaskFilter` 缺失后，才进入 T002。

## T002：最小实现，不顺手设计未来

```kotlin
enum class TaskFilter {
    ALL,
    ACTIVE,
    COMPLETED;

    fun applyTo(tasks: List<Task>): List<Task> = when (this) {
        ALL -> tasks
        ACTIVE -> tasks.filterNot(Task::isCompleted)
        COMPLETED -> tasks.filter(Task::isCompleted)
    }
}
```

这里没有加入标签、日期、搜索表达式或抽象策略接口，因为 Spec 没有要求。最小实现不是追求代码最短，而是不提前支付未知需求的复杂度。

目标测试转绿后，再运行相关模块测试，确认新类型没有破坏现有行为。

## T003—T004：测试持久化边界

偏好存储测试要覆盖三个合同：无值时默认 `ALL`；写入后 Flow 发出选择；遇到未知持久化值时安全回退。

此处不要让测试绑定 DataStore 文件的内部细节。通过 Repository 公开接口验证行为，这样未来序列化方式变化时，测试仍然表达产品合同。

给 Codex 的提示可以强调：

```text
先读取项目中其他偏好的测试方式并复用测试基建。
不要引入新的测试框架；不要用 Thread.sleep 等待 Flow；
如果现有测试工具无法稳定控制协程，先说明缺口再提出最小依赖方案。
```

这能防止它为了一个测试引入整套新的库或产生偶发失败。

## T005—T006：测试状态组合，而不是实现细节

ViewModel 测试关心可观察状态：给定任务流和筛选流，`uiState.visibleTasks` 是否正确；选择筛选是否调用偏好 Repository；完成进行中任务后，当前列表是否随数据事实源更新。

不要断言某个私有方法被调用，也不要复制 `combine` 实现。一个有价值的测试可以在内部重构后继续成立。

此时要特别检查时间控制：使用项目已有的协程测试调度器，显式推进任务；不要以真实延时“等它差不多完成”。

## T007—T008：Compose 测试关注语义

Compose 测试通过 Semantics 与 UI 交互。示意代码如下，具体 API 以项目依赖版本为准：

```kotlin
composeRule.onNodeWithText("已完成")
    .performClick()
    .assertIsSelected()

composeRule.onNodeWithText("当前筛选下没有任务")
    .assertIsDisplayed()
```

不要依赖控件在节点树中的第几个位置，也不要只做截图像素比较。文本、角色、选中状态和内容描述更接近用户与无障碍服务理解的界面。

设备测试命令是：

```bash
./gradlew :app:connectedDebugAndroidTest
```

它需要设备或模拟器。如果当前环境没有，Codex 应保留测试并报告“未运行”，由 CI 或开发者随后执行。

## 每个循环都检查 diff 半径

Agent 容易在实现过程中顺手重命名、格式化或升级依赖。每完成一个任务，检查：

```bash
git diff --stat
git diff --check
git diff -- app/src/main app/src/test app/src/androidTest
```

如果 T002 却改了 Gradle、Manifest 和十个 UI 文件，应立即停下来解释。越早发现范围膨胀，回退成本越低。

## T009：扩大验证，不伪造证据

所有小步完成后按梯度执行：

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

最后一项只能在设备环境存在时运行。结果摘要应该像实验记录：命令、结果、关键数量或失败点、未运行原因。不要用“所有测试通过”覆盖实际只跑过一个目标测试的事实。

## 何时允许偏离 TDD

探索未知 API、搭建 UI 原型或复现平台 Bug 时，可以先做一次可丢弃的 Spike。但要明确它不是生产实现；方向确认后，回到可验证合同再实现。否则“先试试看”的代码很容易悄悄进入主线。

## 小结

TDD 为 Codex 提供了一条窄而清楚的执行轨道：测试表达行为，最小实现控制范围，回归验证积累证据。关键不在机械追求红绿颜色，而在确认每次失败和通过分别证明了什么。

代码现在能跑，但作者视角往往看不见自己的盲区。下一讲我们会切换到独立审查，使用 `/review` 与 Android Review Skill 尝试证伪这次改动。

## 思考题

1. 哪个 ViewModel 测试最容易错误地绑定内部实现？
2. 如果 UI 测试因没有模拟器未运行，怎样让交付状态仍然诚实而可继续？

