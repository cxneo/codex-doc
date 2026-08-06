# 任务列表筛选任务清单

状态：Teaching exercise / Reset before execution
关联：`spec.md`、`plan.md`

本文件展示实施顺序，不代表学习者已经运行测试。课程源码包含最终教学基线；先按
`docs/labs/19-tdd/README.md` 应用 RED 补丁，再逐项记录你自己的结果。

- [ ] T001 建立 `TaskFilter` 失败测试
  - 依赖：无
  - 完成条件：覆盖 ALL、ACTIVE、COMPLETED、顺序不变
  - 验证：`./gradlew :app:testDebugUnitTest --tests '*TaskFilterTest'`
  - 结果：未运行

- [ ] T002 实现 `TaskFilter` 最小规则
  - 依赖：T001
  - 完成条件：T001 转绿，无未来筛选抽象
  - 验证：同 T001，再运行相关模型测试
  - 结果：未运行

- [ ] T003 建立偏好默认、读写、未知值与写失败测试
  - 依赖：T002
  - 完成条件：四种合同均由 Repository 公开接口表达
  - 验证：目标 Repository 测试
  - 结果：未运行

- [ ] T004 实现筛选偏好持久化
  - 依赖：T003
  - 完成条件：复用已有 DataStore，不新增第二套存储
  - 验证：T003 转绿
  - 结果：未运行

- [ ] T005 建立 Repository / ViewModel 组合状态失败测试
  - 依赖：T002、T004
  - 完成条件：覆盖切换、完成任务后更新、初始值与写失败
  - 验证：目标 `TaskRepositoryTest` 与 ViewModel 测试
  - 结果：未运行

- [ ] T006 接入 ViewModel 单一 UI State
  - 依赖：T005
  - 完成条件：UI 不持有重复业务状态，T005 转绿
  - 验证：目标与全量 ViewModel 测试
  - 结果：未运行

- [ ] T007 建立 Compose 控件、语义与空状态测试
  - 依赖：T006
  - 完成条件：测试先因 UI 尚未实现而失败
  - 验证：目标设备测试
  - 结果：未运行

- [ ] T008 实现 PocketTasksScreen 筛选 UI
  - 依赖：T007
  - 完成条件：语义与两类空状态满足 Spec
  - 验证：T007 转绿
  - 结果：未运行

- [ ] T009 回归、独立审查与人工验证
  - 依赖：T001—T008
  - 完成条件：完整 diff 经过 `$android-code-review` 与人工评审
  - 验证：单元、Lint、构建、设备测试、TalkBack / 大字体 / 横屏
  - 结果：未运行

## 最终证据

- 目标测试：未运行
- 单元测试：未运行
- Lint：未运行
- Debug 构建：未运行
- 设备测试：未运行
- 人工验证：未运行

## 残余风险

- 这是教学任务记录，必须由学习者重新执行 RED/GREEN 与设备验证，不能把课程作者的基线结果当成自己的结果。
