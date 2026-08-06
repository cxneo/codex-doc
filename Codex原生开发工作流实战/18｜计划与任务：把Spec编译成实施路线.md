# 18｜计划与任务：把 Spec 编译成实施路线

上一讲已经确认 PocketTasks 的筛选行为。现在可以讨论技术了。但“新建枚举、改 ViewModel、改 UI、加测试”还称不上 Plan——它只是文件清单，没有解释状态所有权、数据边界与失败策略。

这一讲要把 Spec 编译成一条可以实施、可以审查、可以中途停下的路线。

## 先画出现状，再画目标

Codex 调查发现，任务数据来自 Room：DAO 返回 `Flow<List<TaskEntity>>`，Repository 映射为领域模型，`TasksViewModel` 组合数据并暴露 `StateFlow<TasksUiState>`，Compose 只渲染 UI State。

筛选偏好尚不存在，但项目已有 Preferences DataStore。目标数据流可以画成：

```text
                     ┌─ Room tasks Flow ───────────┐
TasksScreen event ─→ TasksViewModel                ├─ combine → TasksUiState → Compose
                     └─ DataStore filter Flow ─────┘
                              ↑
                       update filter
```

Room 仍然是任务事实源，DataStore 只保存用户偏好。ViewModel 组合两条流，UI 不维护第二份过滤后的可变列表。这与项目宪法一致。

## 做出关键技术决定

### 决定一：筛选模型放在哪里

`TaskFilter` 表示界面和偏好层都要理解的稳定概念，可以放在共享 model 包，而不是定义成 Composable 内部枚举。它只包含 `ALL`、`ACTIVE`、`COMPLETED` 和纯过滤语义。

### 决定二：用 DataStore，不用 SavedStateHandle

Spec 明确要求冷启动恢复，项目又已有 DataStore。`SavedStateHandle` 更适合保存导航与进程恢复相关状态，不承担长期偏好。复用现有 Preferences Repository，避免 UI 直接访问 DataStore。

### 决定三：是否创建 Use Case

过滤逻辑目前只服务一个 ViewModel，复杂度很低。按宪法“不为形式增加层”，先使用纯函数或模型方法，不创建只转发的 `FilterTasksUseCase`。如果未来首页小组件或 Wear 模块复用，再提取。

### 决定四：错误策略

DataStore 读取异常回退到 `ALL`；写入失败保留当前会话中的选择并走现有错误报告通道。任务读取失败继续使用已有错误 UI，不让偏好错误遮蔽核心数据。

## 明确文件影响面

Plan 不应承诺尚未确认的精确行号，但要给出可评审的组件边界：

```markdown
## 预计改动

- model/TaskFilter.kt：稳定筛选类型与纯过滤规则
- data/preferences/UserPreferencesRepository.kt：暴露 filter Flow 与更新方法
- ui/tasks/TasksUiState.kt：增加当前筛选和可见任务
- ui/tasks/TasksViewModel.kt：组合任务流与筛选流，处理筛选事件
- ui/tasks/TasksScreen.kt：渲染筛选控件与区分空状态
- 对应 test / androidTest：行为与界面证据

## 明确不改

- TaskEntity、TaskDao 与 Room Schema
- 排序规则
- 任务完成 / 删除的持久化语义
- Manifest、权限与发布配置
```

“明确不改”让评审者能迅速识别 diff 越界。

## 设计验证梯度

从最快、最确定的证据开始：

```text
1. TaskFilter 纯规则测试
2. UserPreferencesRepository 测试
3. TasksViewModel 组合流测试
4. Compose 控件与空状态测试
5. 全量单元测试 + Lint + Debug 构建
6. 设备上的重建与冷启动场景
```

每一步失败都能指向更小范围。设备测试放在最后，不代表它不重要，而是它成本更高、环境依赖更多。

## 风险不是一句“低风险”

Plan 列出具体风险与缓解：

| 风险 | 触发方式 | 缓解与证据 |
|---|---|---|
| Flow 初始值导致首次闪烁错误筛选 | DataStore 尚未发值 | 明确 Loading / 默认策略并测初始状态 |
| 完成任务后列表未即时更新 | 对过滤结果做了本地缓存 | 始终从组合 Flow 派生并测试 |
| 偏好解析因未知值崩溃 | 旧版本或损坏数据 | 未知值回退 ALL |
| 大字体下控件截断 | 三个选项固定宽度 | Compose 测试 + 人工大字体检查 |
| UI 测试在本地无设备 | 环境缺失 | 明确标为未运行，由 CI 设备 Job 执行 |

能说出“怎样触发”的风险，才可能变成测试。

## 把 Plan 拆成有依赖的 Tasks

```markdown
- [ ] T001 建立 TaskFilter 规则的失败测试
  - 依赖：无
  - 验证：目标单元测试应因类型尚不存在而失败

- [ ] T002 实现 TaskFilter，使 T001 通过
  - 依赖：T001
  - 验证：TaskFilterTest

- [ ] T003 为筛选偏好默认值、读写和未知值建立测试
  - 依赖：T002

- [ ] T004 实现 UserPreferencesRepository 的筛选持久化
  - 依赖：T003

- [ ] T005 为 ViewModel 的组合状态建立失败测试
  - 依赖：T002、T004

- [ ] T006 接入 ViewModel，并保持单一 uiState
  - 依赖：T005

- [ ] T007 增加 Compose 筛选控件、语义与两类空状态测试
  - 依赖：T006

- [ ] T008 实现 TasksScreen，并通过 UI 测试
  - 依赖：T007

- [ ] T009 执行回归验证并记录结果
  - 依赖：T001—T008
```

测试任务在实现任务之前，这会自然建立 TDD 节奏；每个任务又有停止点，Codex 不必一次修改所有层。

## 让 Codex 评审计划，而不是自我确认

计划完成后，可以启动只读 reviewer 或新会话，要求检查：是否满足每条验收；是否与宪法冲突；是否引入不必要层次；是否遗漏生命周期、错误和无障碍；任务依赖能否逐步验证。

让同一个会话直接说“我的计划很好”价值有限。独立视角的意义是尝试证伪。

## 小结

Plan 把用户行为映射为状态所有权、组件边界、风险与验证；Tasks 再把它拆成按依赖排序的小步动作。好的计划解释“为什么”，好的任务说明“做到哪一步可以证明”。

下一讲开始真正写 Kotlin。但我们不会把整份计划一次交给 Codex，而会沿着任务链，用失败测试建立红灯，再用最小实现逐个把灯变绿。

## 思考题

1. 如果项目尚未使用 DataStore，引入它与暂时不持久化各有什么代价？
2. 哪个 Task 最适合成为第一次可独立提交的检查点？

