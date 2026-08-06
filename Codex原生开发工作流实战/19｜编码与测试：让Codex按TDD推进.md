# 19｜编码与测试：让 Codex 按 TDD 小步推进

这一讲不再用伪代码。配套 PocketTasks 是一个可编译 Android 项目，包含 Compose、ViewModel、Repository、DataStore、Room、JVM 测试、Compose 测试和 Migration 测试。

真实入口：

- [筛选模型](./配套文件/PocketTasks-codex/app/src/main/java/com/example/pockettasks/model/TaskFilter.kt)
- [筛选单元测试](./配套文件/PocketTasks-codex/app/src/test/java/com/example/pockettasks/model/TaskFilterTest.kt)
- [Repository](./配套文件/PocketTasks-codex/app/src/main/java/com/example/pockettasks/data/TaskRepository.kt)
- [Repository 测试](./配套文件/PocketTasks-codex/app/src/test/java/com/example/pockettasks/data/TaskRepositoryTest.kt)
- [Compose 页面](./配套文件/PocketTasks-codex/app/src/main/java/com/example/pockettasks/ui/PocketTasksScreen.kt)
- [Compose 测试](./配套文件/PocketTasks-codex/app/src/androidTest/java/com/example/pockettasks/ui/PocketTasksScreenTest.kt)
- [数据库迁移测试](./配套文件/PocketTasks-codex/app/src/androidTest/java/com/example/pockettasks/data/DatabaseMigrationTest.kt)

## 为什么 Agent 更需要 TDD

人可以在脑中保持“我只改筛选，不重构全项目”的意图；Agent 的搜索范围和生成速度更大，也更容易顺手扩大改动。

一个先失败的测试同时提供三样东西：

1. 对行为的可执行定义；
2. 对本次改动边界的约束；
3. 修改完成后的客观停止条件。

TDD 不是要求每次都机械写测试，而是让每一步都有可观察的状态变化：

```text
RED：测试因缺少目标行为而失败
GREEN：最小实现使目标测试通过
REFACTOR：不改变行为地整理实现
VERIFY：扩大测试范围并审查 diff
```

## 准备环境

从课程根目录进入项目：

```bash
cd 配套文件/PocketTasks-codex
```

项目要求 JDK 17 或更高版本，并使用仓库中的 Gradle Wrapper：

```bash
./gradlew --version
./gradlew :app:testDebugUnitTest
```

预期末尾：

```text
BUILD SUCCESSFUL
```

如果失败，先修复环境。不要在基线已经红灯时开始功能 TDD，否则你无法知道新失败是否来自本次修改。

课程主分支始终保持绿色。要复现 RED，在练习分支应用配套的教学补丁：

```bash
git switch -c codex/lab-19-tdd
./scripts/lab-patch.sh apply 19-tdd
```

补丁只移除“隐藏归档任务”的实现，保留对应测试，因此会产生一个原因明确的失败。练习结束后可以丢弃该练习分支，不影响课程基线。

## 循环一：先定义纯 Kotlin 筛选行为

目标规则：

- `ALL` 显示未归档的全部任务；
- `ACTIVE` 只显示未完成任务；
- `COMPLETED` 只显示已完成任务；
- 归档任务在三个筛选中都不可见。

先让 Codex 只改测试：

```text
读取 specs/001-task-filter/spec.md 和当前 TaskFilter 实现。
只修改 TaskFilterTest，为 ALL、ACTIVE、COMPLETED 和 archived 行为增加测试。
不要修改生产代码。运行目标测试并报告失败断言。
```

运行：

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.example.pockettasks.model.TaskFilterTest' \
  --rerun-tasks
```

应用 RED 补丁后，归档用例应失败。`--rerun-tasks` 防止已有编译缓存制造假绿，核心信息类似：

```text
TaskFilterTest > all hides archived tasks but keeps active and completed tasks FAILED
java.lang.AssertionError: expected:<[1, 2]> but was:<[1, 2, 3]>
```

这才是有效 RED：

- 测试成功编译；
- 失败原因正好是缺少“过滤归档任务”；
- 不是依赖下载、JDK 或语法错误。

## 循环二：只写最小实现

让 Codex 修复目标行为：

```text
只修改 TaskFilter.kt，使刚才的归档与状态筛选测试通过。
不要修改 Entity、Repository、Compose 或 Gradle 配置。
完成后运行目标测试并展示 diff。
```

核心实现是一个纯函数：

```kotlin
fun List<Task>.visibleFor(filter: TaskFilter): List<Task> =
    asSequence()
        .filterNot(Task::isArchived)
        .filter { task ->
            when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }
        }
        .toList()
```

再次运行目标测试，预期：

```text
BUILD SUCCESSFUL
```

然后检查改动半径：

```bash
git diff --stat
git diff -- app/src/main app/src/test
```

如果 Codex 同时重写了 UI 或引入新框架，就算测试通过也没有完成“最小实现”。

## 循环三：测试 Flow 与持久化边界

纯函数正确，不代表系统状态能够正确组合。Repository 需要把 Room 的任务流和 DataStore 的筛选流组合起来。

真实实现使用：

```kotlin
combine(taskDao.observeAll(), filterPreferences.selectedFilter) { tasks, filter ->
    FilteredTasks(filter, tasks.map(TaskEntity::asExternalModel).visibleFor(filter))
}
```

Repository 测试使用内存 Fake，不依赖模拟器：

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.example.pockettasks.data.TaskRepositoryTest'
```

这里应验证行为而不是内部调用次数：

- 切换到 `COMPLETED` 后，流只发出已完成任务；
- 空白标题不会插入数据库；
- 测试不会因为真实磁盘或 Android Context 变慢。

不要在 JVM 测试中伪造 DataStore 的每一层内部实现。通过 `FilterPreferences` 接口建立可控边界，真实 DataStore 留给集成验证。

## 循环四：Compose 测试只关心用户可见行为

Compose 测试位于 `androidTest`，验证：

- “进行中”筛选具有选中语义；
- 目标任务文字对用户可见；
- Checkbox 暴露可理解的无障碍说明。

只编译设备测试 APK：

```bash
./gradlew :app:assembleDebugAndroidTest
```

连接模拟器后执行：

```bash
adb devices
./gradlew :app:connectedDebugAndroidTest
```

预期 `adb devices` 至少出现一个状态为 `device` 的目标。若为 `offline` 或列表为空，应报告“设备测试未执行”，不能用“AndroidTest 已编译”冒充“设备测试已通过”。

## 循环五：Room Migration 必须使用旧版本证据

当前数据库从 v1 升到 v2，增加：

```sql
ALTER TABLE tasks
ADD COLUMN archived INTEGER NOT NULL DEFAULT 0
```

配套仓库保留：

- [v1 schema](./配套文件/PocketTasks-codex/app/schemas/com.example.pockettasks.data.PocketTasksDatabase/1.json)
- [v2 schema](./配套文件/PocketTasks-codex/app/schemas/com.example.pockettasks.data.PocketTasksDatabase/2.json)
- [`MIGRATION_1_2`](./配套文件/PocketTasks-codex/app/src/main/java/com/example/pockettasks/data/PocketTasksDatabase.kt)
- [`DatabaseMigrationTest`](./配套文件/PocketTasks-codex/app/src/androidTest/java/com/example/pockettasks/data/DatabaseMigrationTest.kt)

测试先用 `MigrationTestHelper` 创建 v1 数据库并插入旧任务，再让 Room 执行 Migration，最后断言：

- 旧任务标题仍然存在；
- `archived` 默认值为 `0`；
- Room 能按当前 schema 打开数据库。

只验证“新安装能够创建 v2 数据库”不算迁移测试，因为它完全绕过了真实升级路径。

## 每一轮怎样约束 Codex

一个可靠任务合同包含：

```text
目标行为：这一轮只解决什么
允许修改：精确到文件或目录
禁止修改：不能顺手动什么
目标测试：最小反馈命令
完成证据：失败前后输出和 diff
停止条件：发现架构冲突或额外风险时先停
```

例如：

```text
完成 T003：Repository 在筛选为 COMPLETED 时只发出已完成任务。
允许修改 TaskRepositoryTest.kt 和必要的 TaskRepository.kt。
不要修改数据库 schema、Compose 页面和 Gradle 依赖。
先提交失败测试证据，再最小实现；目标测试通过后停止并展示 diff。
```

## 从快到慢扩大验证

```bash
# 1. 单个测试类
./gradlew :app:testDebugUnitTest \
  --tests 'com.example.pockettasks.model.TaskFilterTest'

# 2. 全部 JVM 测试
./gradlew :app:testDebugUnitTest

# 3. 静态检查和可安装包
./gradlew :app:lintDebug :app:assembleDebug

# 4. 有设备时
./gradlew :app:connectedDebugAndroidTest
```

每一层回答的问题不同：

| 层级 | 能证明 | 不能证明 |
|---|---|---|
| 纯 Kotlin 测试 | 筛选规则 | Android 生命周期和真实数据库 |
| Repository 测试 | Flow 组合与业务边界 | UI 语义 |
| Lint/assemble | 静态质量与可构建 | 设备交互正确 |
| AndroidTest | Compose/Room 在设备环境工作 | 所有真机和发布场景 |

## 一份诚实的完成报告

```markdown
## 实现
- 增加 ALL / ACTIVE / COMPLETED 筛选。
- 筛选选择通过 DataStore 保存。
- Room v1→v2 增加 archived 列并保留旧数据。

## 已运行
- `./gradlew :app:testDebugUnitTest`：通过。
- `./gradlew :app:lintDebug :app:assembleDebug`：通过。
- `./gradlew :app:assembleDebugAndroidTest`：通过。

## 未运行
- `connectedDebugAndroidTest`：当前没有已连接设备。

## 残余风险
- 尚未覆盖进程被系统杀死后的真机恢复行为。
```

“没有设备”不是失败；把未运行的验证写成通过才是失败。

## 本讲练习

1. 在练习分支用 `./scripts/lab-patch.sh apply 19-tdd` 应用 RED 补丁并复现失败。
2. 只修改 `TaskFilter.kt` 使目标测试变绿。
3. 使用 `/diff` 检查改动半径。
4. 运行 JVM 测试、Lint 和 assemble。
5. 有模拟器时运行 Compose 和 Migration 测试。
6. 按上面的格式写完成报告。

## 完成标志

你能提供 RED、GREEN、diff 和扩大验证四类证据；也能明确区分“测试 APK 编译成功”和“设备测试执行成功”。
