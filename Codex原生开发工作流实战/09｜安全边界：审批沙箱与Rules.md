# 09｜安全边界：审批、沙箱、Permissions 与 Rules

Codex 能执行 Shell、操作设备和连接外部工具。安全不能靠提示词里的“请小心”，而要把人的决定、系统能力和团队政策分开。

## 四个概念不要混用

| 机制 | 回答的问题 |
|---|---|
| Approval / reviewer | 越界动作由人审，还是交给自动审查 |
| Sandbox / permission profile | 本地命令真正能读写哪些文件、访问哪些网络 |
| Rules / execpolicy | 某类命令应允许、询问还是禁止 |
| `requirements.toml` | 组织管理员强制哪些边界，用户不能覆盖 |

改变 reviewer 不会扩大沙箱。例如桌面应用的 **Approve for me / Auto-review** 仍受同一工作区边界；只是越界请求由自动审查处理。**Full access** 则改变实际触达范围，是另一回事。

## 旧式沙箱配置与新版 Permission Profiles

课程项目为了兼容广泛客户端，使用：

```toml
approval_policy = "on-request"
sandbox_mode = "workspace-write"

[sandbox_workspace_write]
network_access = false
```

常见沙箱预设：

- `read-only`：调查、评审；
- `workspace-write`：写工作区和允许的临时位置；
- `danger-full-access`：移除本地沙箱限制，只用于明确隔离环境。

新版 Codex 还提供 beta Permission Profiles，把文件系统规则和网络规则组合成命名策略；内置 `:read-only`、`:workspace`、`:danger-full-access`。它适合组织定义“Android 项目只写这些根目录、只访问这些依赖域名”的最小权限。

两套配置不能混用：如果任何活动配置或 CLI 参数出现 `sandbox_mode`，Codex 使用旧式沙箱，而不是 `default_permissions`。团队迁移到 Profiles 前必须统一 Codex 版本、移除旧字段并验证有效权限。

用 `/permissions` 查看当前可选模式；用 `/debug-config` 查实际来自哪一层。

## 配置优先级和项目信任

用户配置在 `~/.codex/config.toml`，项目可以从仓库根到当前目录逐层提供 `.codex/config.toml`，离当前目录更近的项目层优先。CLI 覆盖和组织要求还会参与最终解析。

项目只有被信任后，项目 `.codex/` 配置、Hooks 和 Rules 才会加载。不信任项目时，用户与系统层仍然存在。信任只是允许项目配置参与，不等于自动批准每条命令。

## Android 审批要看目标，不只看命令名

```text
./gradlew :app:testDebugUnitTest         写 build，可能下载依赖
./gradlew :app:connectedDebugAndroidTest 操作已连接设备
adb shell pm clear <package>             清除指定设备上的应用数据
./gradlew publishRelease                 可能发布制品
git push                                 改变远端状态
```

连接个人真机、生产 Firebase 项目或签名环境时，相同命令会有更高风险。审批时读取命令、cwd、文件目标、网络目标、设备序列号、凭据和可逆性。

## 网络不是简单的开与关

Android 首次构建经常需要 Gradle Plugin Portal、Google Maven、Maven Central 或组织镜像。默认关闭网络可以先暴露依赖是否已缓存；需要联网时，只开放批准域名和方法。

不要把开放网络当成解决所有 Gradle 问题的第一步。先确认：

- 项目声明了哪些仓库；
- 组织是否要求内部代理；
- 第三方镜像是否经过供应链审查；
- 构建脚本和依赖生命周期代码是否可信；
- 下载后能否回到更窄权限。

Permission Profiles 的网络代理与 allowlist 能提供更细粒度控制，但配置仍在 beta；按当前官方文档验证平台支持，不照抄旧截图。

## Rules：把重复命令判断写成政策

课程 [`default.rules`](./配套文件/PocketTasks-codex/.codex/rules/default.rules) 包含：

```python
prefix_rule(
    pattern = ["git", "push"],
    decision = "prompt",
    justification = "Pushing changes remote state and requires task-owner approval.",
    match = ["git push", "git push origin codex/task-filter"],
)

prefix_rule(
    pattern = ["adb", "shell", "pm", "clear"],
    decision = "forbidden",
    justification = "Confirm device and package, then run manually.",
    match = ["adb shell pm clear com.example.pockettasks"],
)
```

`match` 与 `not_match` 是内联样例；多条命中时采用最严格决定。不要允许全部 `./gradlew`：自定义 Task 可以下载、签名、上传或执行任意插件逻辑。

`codex execpolicy` 当前是 preview，但可以在保存前测试：

```bash
codex execpolicy check --pretty \
  --rules .codex/rules/default.rules \
  -- git push origin codex/task-filter
```

Rules 适合固定前缀。需要检查分支、包名、设备或文件内容时，使用第 11 讲的 Hook，并给 Hook 写自动测试。

## 组织政策：`requirements.toml`

Business / Enterprise 管理员可以约束审批策略、沙箱或 Permission Profiles、网络、MCP、Hooks、插件来源等敏感设置。若用户配置冲突，Codex 回退到兼容值并提示，用户不能在项目中绕过。

管理员可以只允许 `:read-only` 与 `:workspace`，禁止 full access；也可以要求只加载受管 Hooks，或按 MCP 名称和身份做 allowlist。项目 `AGENTS.md` 不能替代这些强制控制。

## 一套 Android 风险分级

| 等级 | 示例 | 默认处理 |
|---|---|---|
| 低 | 读源码、`rg`、查看 diff、目标 JVM 测试 | 沙箱内自动 |
| 中 | 写源码、下载依赖、启动模拟器、仪器测试 | 明确环境与目标 |
| 高 | 清数据、读取签名、发布、生产后台、push/merge | 人工审批或禁止 |

这个表不是永恒规则。企业设备、个人真机和隔离 CI 的风险不同，团队要写明适用环境。

## 数据边界同样重要

默认拒绝读取：`local.properties`、签名文件、服务账号 JSON、生产数据库、真实用户日志。凭据通过专门秘密系统提供，日志先脱敏；MCP、Plugin、Browser 和 Computer Use 都可能把数据带出仓库，需要单独审批。

## 动手验证

```bash
cd 配套文件/PocketTasks-codex
codex execpolicy check --pretty \
  --rules .codex/rules/default.rules \
  -- adb shell pm clear com.example.pockettasks
python3 .codex/hooks/test_pre_tool_use.py
```

前者验证固定政策，后者验证上下文门禁。若本机 CLI 版本不支持 preview `execpolicy`，记录为版本限制，不要把“命令不存在”写成规则通过。

## 小结

审批决定谁放行，沙箱或 Permission Profile 限制真实能力，Rules 处理可预测命令，Hooks 处理上下文，管理员要求提供不可绕过的组织边界。它们叠加后，Codex 才能在 Android 工程里既有用又可控。

下一讲会处理另一类边界：多个任务同时工作时，用 Worktree 隔离源码、构建输出和未提交状态。

## 延伸阅读

- [Codex sandbox and approvals](https://developers.openai.com/codex/security/)
- [Codex Permissions](https://developers.openai.com/codex/permissions/)
- [Codex Rules](https://developers.openai.com/codex/rules/)
- [Managed configuration](https://developers.openai.com/codex/enterprise/managed-configuration/)
