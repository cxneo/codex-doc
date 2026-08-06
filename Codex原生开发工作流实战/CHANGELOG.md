# 课程变更记录

本文件记录会影响学习路径、事实边界、配套工程或验证结果的改动。小型错别字可以合并记录，产品能力变化必须单独说明。

## 2026-08-06｜第三阶段：编辑、培训与验收体系

- 重构课程首页，增加 5 分钟开始、角色路线、团队分发入口和阅读约定；
- 新增课程导读、讲师手册、术语表、版本与兼容性、逐章审校记录和结课实践；
- 新增课程一键校验脚本，统一检查链接、配置语法、Shell、Hooks 和实验补丁；
- 修正第 02 讲示例类名，使其与实际 `TaskViewModel` 一致；
- 结束语接入结课实践与季度维护闭环。

## 2026-08-06｜第二阶段：Codex 原生能力审校

- 按 Codex 当前官方能力重写或细化交互、安全、Worktrees、Hooks、MCP/Plugins、Skills、Subagents、Exec/SDK/CI、驾驶舱与迁移章节；
- 增加 `.worktreeinclude`、Hook 黑盒测试、结构化审查 Schema 和只读审查脚本；
- 增加 Cursor 资产审计与可逆实验补丁工具；
- 实际验证 Rules、`codex exec`、三个故障实验及完整 Android 本地构建。

对应提交：`7dc7805 feat(course): complete phase 2 Codex-native workflows`。

## 2026-08-06｜第一阶段：Android 可运行基线

- 将案例统一为 Kotlin + Compose + Room + DataStore 的 PocketTasks；
- 建立可运行 Android 工程、Spec/Plan/Tasks、JVM 测试、Compose 测试、Room v1→v2 Migration 测试和 CI；
- 用真实命令验证单元测试、Lint、构建、AndroidTest 编译和 API 35 模拟器设备测试。

对应提交：`ef5c8a5 feat(course): complete phase 1 Android hands-on foundation`。
