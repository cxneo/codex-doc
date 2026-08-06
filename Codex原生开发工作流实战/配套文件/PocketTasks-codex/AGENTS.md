# PocketTasks 工作指南

## 项目定位

PocketTasks 是离线优先的 Android 待办应用。使用 Kotlin、Jetpack Compose 和 Room；Room 是任务数据的本地事实源。

## 开始工作

1. 先阅读本文件；跨层功能、数据模型、权限或后台任务还要阅读 `docs/constitution.md`。
2. 调查现有实现并引用文件证据，不要根据目录名猜架构。
3. 中高风险改动先读取对应 `specs/<id>/spec.md`、`plan.md` 和 `tasks.md`。
4. 未决问题会改变用户行为或数据安全时，停止并请求决定。

## 架构约束

- Composable 只渲染不可变 UI State 并上报用户事件，不直接访问 DAO 或 Repository。
- ViewModel 使用生命周期合适的协程作用域，向 UI 暴露不可变状态。
- 数据通过 Repository 暴露；Room 是离线任务数据的事实源。
- 只有业务逻辑需要复用或显著降低 ViewModel 复杂度时才新增 Use Case。
- 不在 UI、ViewModel 和数据层重复持有同一份可变任务列表。
- 用户可见文案使用资源文件；交互需要可识别的无障碍语义。

## 数据安全

- Room Schema 改动必须包含 Migration、导出 Schema 和升级路径测试。
- 禁止 `fallbackToDestructiveMigration`，不得以清库代替迁移。
- 不读取、打印或提交 `local.properties`、Keystore、服务账号和真实用户数据。
- 不修改签名、版本号、发布任务或生产服务，除非任务明确授权并由人确认。

## 验证命令

按从窄到宽的顺序运行；模块名不一致时先查证，不要猜：

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

设备测试需要已连接设备或模拟器。没有运行条件时，明确报告“未运行”及原因，不能写成通过。

## 修改纪律

- 只改任务范围内的文件，不顺手升级依赖、整理无关代码或批量格式化。
- 新行为先建立失败测试；确认失败原因正确后再实现最小改动。
- 每完成一项任务更新 `tasks.md`，查看 diff 范围。
- 不自动提交、推送、创建 PR 或发布，除非用户明确要求。
- 收尾用四部分报告：改动、理由、实际验证、未验证与残余风险。

## 可复用能力

- Android 改动评审：显式使用 `$android-code-review`。
- 只读跨层调查：委派给 `android-explorer`。
- 独立审查：委派给 `android-reviewer`，不得让评审角色修改文件。

