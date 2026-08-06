# 任务列表筛选实施计划

状态：Approved  
关联 Spec：`spec.md`

## 现状证据

本计划对应课程项目。开始练习前，用当前提交重新确认：

- Room DAO 暴露排序后的任务 Flow。
- `TaskRepository` 把 Entity 映射为应用模型并组合筛选 Flow。
- `TaskViewModel` 暴露单一不可变 `TaskUiState`。
- `PocketTasksScreen` 只渲染状态并上报事件。
- `DataStoreFilterPreferences` 负责筛选持久化。

## 目标设计

```text
                      Room tasks Flow
                            ↓
filter event → TaskViewModel → TaskRepository.combine → TaskUiState → PocketTasksScreen
                    ↑
             DataStore filter Flow
```

Room 仍是任务事实源。DataStore 只保存 `TaskFilter`。ViewModel 组合两条 Flow，UI 不维护第二份筛选列表。

## 关键决定

### 使用项目已有 DataStore

- 选择：通过 `FilterPreferences` 暴露筛选 Flow 与更新方法。
- 理由：Spec 要求冷启动恢复，项目已有同类基础设施。
- 不选择：`rememberSaveable` 和仅 `SavedStateHandle` 无法表达完整长期偏好合同。

### 暂不新增 Use Case

- 选择：用 `TaskFilter` 的纯函数表达三种规则。
- 理由：逻辑只服务一个 ViewModel，新增转发层没有降低复杂度。
- 回看条件：出现第二个消费者或组合规则显著变复杂。

### 保持会话选择与持久化结果解耦

- 选择：用户点击后立即更新会话状态，再尝试持久化。
- 理由：满足写入失败时本次会话仍可使用的 Spec。

## 预计改动

- `model/TaskFilter.kt`：筛选类型和纯规则。
- `data/FilterPreferences.kt`：筛选 Flow、默认值、更新和未知值回退。
- `data/TaskRepository.kt`：组合 Room 与筛选 Flow。
- `ui/TaskUiState` / `TaskViewModel.kt`：当前筛选、可见任务和事件。
- `ui/PocketTasksScreen.kt`：筛选控件、选中语义、两类空状态。
- 对应 `test` 与 `androidTest`。

## 明确不改

- Task Entity、DAO、Room Database 与 Migration。
- 排序规则、任务完成与删除语义。
- Manifest、权限、依赖版本、签名和发布配置。

## 宪法检查

- 涉及：离线优先、状态单向流动、复杂度有理由、行为可验证。
- 偏离：无。

## 风险与缓解

| 风险 | 触发场景 | 缓解和证据 |
|---|---|---|
| 初始筛选闪烁 | DataStore 首值未到达 | 明确初始策略并测试首个 UI State |
| 完成后列表不刷新 | 缓存派生列表 | 始终 combine 事实源并做 ViewModel 测试 |
| 未知偏好崩溃 | 旧值或损坏数据 | 回退 ALL 的 Repository 测试 |
| 写失败回滚 UI | 持久化异常 | 会话选择独立，覆盖失败路径 |
| 大字体截断 | 三项控件空间不足 | 设备测试与人工大字体检查 |

## 验证梯度

1. 目标 JVM 测试。
2. `./gradlew :app:testDebugUnitTest`。
3. `./gradlew :app:lintDebug :app:assembleDebug`。
4. `./gradlew :app:connectedDebugAndroidTest`。
5. TalkBack、大字体、横屏与冷启动人工检查。

## 发布与回退

- 这是本地 UI / 偏好功能，不迁移任务数据。
- 回退代码时保留未知 DataStore key 不会影响旧版本。
- 监控崩溃、ANR 与任务列表打开失败；不记录任务正文。

## 未决问题

无。
