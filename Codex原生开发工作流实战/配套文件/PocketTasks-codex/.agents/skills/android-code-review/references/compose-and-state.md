# Compose、状态与生命周期评审参考

只在改动涉及 UI、ViewModel、导航、资源或仪器测试时使用。

## 状态所有权

- 确认屏幕状态有单一所有者；不要让 Composable 与 ViewModel 维护会分叉的业务状态。
- 确认 ViewModel 暴露不可变状态，UI 通过事件表达意图。
- 检查从 Repository Flow 到 UI State 的初始值、Loading、空值与错误语义。
- 检查派生列表是否从事实源与筛选条件计算，而非长期缓存第二份可变数据。
- 检查 `remember`、`rememberSaveable`、`SavedStateHandle`、DataStore 各自跨越的生命周期是否满足 Spec。

## Compose 副作用

- 检查业务调用是否直接发生在 Composable 函数体并会随重组重复执行。
- 检查 `LaunchedEffect`、`DisposableEffect` 和 `remember` 的 key 是否稳定且代表真正生命周期。
- 检查事件收集是否可能在重组、导航返回或多个 collector 下重复消费。
- 检查列表项是否使用稳定 key，项目对象身份是否会错误复用 UI 状态。

## 生命周期与协程

- 确认 Flow 使用生命周期感知方式收集。
- 确认长任务属于合适作用域，不因 Composable 离开而泄漏，也不因 ViewModel 销毁后继续更新 UI。
- 检查异常是否取消必要的长期 Flow，或被吞掉导致 UI 永久 Loading。
- 检查测试是否使用可控调度器，避免真实延时和偶发顺序。

## 交互、资源与无障碍

- 用户可见字符串进入资源，复数、格式化和本地化参数正确。
- 可点击元素具有角色、标签、状态或内容描述；选中与错误不能只靠颜色。
- 检查点击目标、焦点顺序、TalkBack 表述与禁用状态。
- 检查大字体、横屏、窄屏和系统 Insets 对主要交互的影响。

## 测试证据

- 单元测试覆盖状态转换、错误与并发边界。
- Compose 测试通过 Semantics 查找，不依赖脆弱节点位置。
- Activity 重建、导航返回、权限结果等平台行为使用合适的设备测试或人工步骤。
- 截图只证明某个视觉时刻，不能替代状态与交互断言。

