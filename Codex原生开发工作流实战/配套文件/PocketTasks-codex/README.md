# PocketTasks：Codex Android 工程化训练项目

这是课程使用的完整 Android 示例项目，不再只是配置外壳。项目包含 Kotlin、Jetpack Compose、Room、DataStore、JVM 测试、Compose 测试、Room Migration 测试以及 GitHub Actions 示例。

## 你会在这里练习什么

- 用 `AGENTS.md` 和项目宪法约束 Codex；
- 按 Spec → Plan → Tasks 推进任务筛选功能；
- 用 RED → GREEN → REFACTOR 完成 TDD；
- 审查 Compose 状态、协程和 Room 数据风险；
- 在 CI 中运行确定性验证，并安全接入 Codex Action；
- 使用 Rules、Hooks、Skills 和 Subagents 扩展工作流。

## 环境要求

- JDK 17 或更高版本；
- Android SDK Platform 35；
- 可选：Android Studio 和 API 35 模拟器；
- Git；
- Codex CLI，仅在练习 Codex 章节时需要。

项目使用 Gradle Wrapper。不要依赖全局 Gradle。

## 第一次运行

```bash
./gradlew --version
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug
```

三条命令预期以 `BUILD SUCCESSFUL` 结束。Debug APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

有设备或模拟器时：

```bash
adb devices
./gradlew :app:connectedDebugAndroidTest
```

`adb devices` 必须显示状态为 `device` 的目标。只编译了 AndroidTest APK 不等于设备测试已经执行。

## 项目结构

```text
PocketTasks-codex/
├── app/
│   ├── schemas/                         # Room v1、v2 schema
│   └── src/
│       ├── main/                        # Compose、ViewModel、Room、DataStore
│       ├── test/                        # JVM 单元测试
│       └── androidTest/                 # Compose 与 Migration 测试
├── specs/                               # Spec / Plan / Tasks
├── docs/constitution.md                 # 课程约定的项目宪法
├── AGENTS.md                            # Codex 项目指令
├── .agents/skills/android-code-review/  # Android 审查 Skill
├── .codex/                              # 配置、Rules、Hooks、Agents
└── .github/                             # Android CI 与 Codex Action 示例
```

## 数据库迁移实验

数据库当前版本为 v2。v1→v2 增加 `tasks.archived`：

```sql
ALTER TABLE tasks
ADD COLUMN archived INTEGER NOT NULL DEFAULT 0
```

迁移证据包括：

- `app/schemas/.../1.json`；
- `app/schemas/.../2.json`；
- `PocketTasksDatabase.MIGRATION_1_2`；
- `DatabaseMigrationTest`。

禁止使用 `fallbackToDestructiveMigration` 让测试“变绿”。

## CI 文件说明

- `.github/workflows/android-ci.yml` 是可运行的常规 Android CI；
- `codex-pr-review.example.yml` 和 `codex-autofix.example.yml` 不会自动运行；
- 启用 Codex 示例前，必须审查触发者、fork、API Key、仓库写权限和费用策略；
- 示例没有 API Key、签名材料或真实凭据。

## Codex 配置安全提示

`.codex/hooks/pre_tool_use.py` 是自动执行代码。第一次进入项目时先审查：

```bash
sed -n '1,240p' .codex/config.toml
sed -n '1,260p' .codex/hooks.json
sed -n '1,260p' .codex/hooks/pre_tool_use.py
```

课程配置使用保守沙箱，网络默认关闭。受信任项目才会加载项目级 Codex 配置；信任项目并不等于以后所有命令都安全。

## 教学版本

- Android Gradle Plugin：8.9.2；
- Kotlin：2.0.21；
- Gradle Wrapper：8.11.1；
- compileSdk / targetSdk：35；
- JDK baseline：17。

这些版本是经过本课程验证的可复现基线，不代表永远固定为最新版本。升级时单独提交版本变更并重新运行完整验证。

`settings.gradle.kts` 为中国大陆培训环境显式配置了阿里云 Google/Public 镜像，并保留官方仓库作为后备。组织对第三方制品镜像有供应链限制时，应删除镜像行并使用经过批准的内部代理或官方仓库。

课程入口：[`../../README.md`](../../README.md)。
