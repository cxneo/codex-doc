# 09｜安全边界：审批、沙箱与 Rules

前面几讲一直在增强 Codex 的理解力。现在要反过来问：一个理解项目、能执行命令的 Agent，最坏能造成什么后果？

它可能误删文件、改动仓库外内容、把密钥带入日志、向错误环境发布构建，或者把一个看似普通的 Gradle task 连到外部服务。安全不能依赖“提示词里说一句请小心”，而要由几层独立机制共同承担。

## 三道边界解决三个问题

审批回答“这一次是否由人放行”；沙箱回答“即使执行，最多能触达哪里”；Rules 回答“某类命令按团队政策应该允许、询问还是禁止”。

```text
Rules：这类动作符合政策吗？
   ↓
Approval：这次动作需要人确认吗？
   ↓
Sandbox：动作真正能访问什么？
```

三者不能互相替代。审批可能被疲劳点击，沙箱不理解业务语义，Rules 也无法列出所有未知风险。多层防护的价值，就是一层失误时还有下一层。

## 沙箱：先限制物理能力

常见沙箱模式包括：

- `read-only`：适合代码调查和审查，不允许工作区写入；
- `workspace-write`：允许在工作区内修改，适合大多数本地开发；
- `danger-full-access`：限制最少，只应在明确理解环境与任务时使用。

Android 构建会写入模块 `build/`、Gradle 缓存，并可能访问网络下载依赖。最小权限不一定等于完全只读，而是给当前任务刚好够用的范围。

团队常用起点可以放在受信任项目的 `.codex/config.toml`：

```toml
approval_policy = "on-request"
sandbox_mode = "workspace-write"

[sandbox_workspace_write]
network_access = false
```

首次需要下载依赖时再显式放行网络，比长期默认开放更容易看清行为。注意，项目级配置只有在项目被信任后才会生效。

## 审批：读懂请求，而不是训练手速

看到审批请求时，按下面顺序读：命令、工作目录、参数中的目标、网络或凭据、可逆性。

PocketTasks 中，以下动作风险明显不同：

```text
./gradlew :app:testDebugUnitTest      本地验证，常规写 build 目录
./gradlew :app:connectedDebugAndroidTest  会操作连接设备
adb shell pm clear com.example...     会清除设备上的应用数据
./gradlew publishRelease              可能向外部仓库发布
git push                              改变远端状态
```

“都是开发命令”不是足够细的分类。尤其在连接了个人真机、生产 Firebase 项目或签名环境时，目标比命令名字更重要。

## Rules：把重复判断写成政策

Codex 的 Rules 使用 `.rules` 文件按命令前缀匹配。它们目前属于实验能力，语法可能演进；下面展示的是思路与当前形式：

```python
prefix_rule(
    pattern = ["git", "push"],
    decision = "prompt",
    justification = "推送会改变远端状态，必须由任务负责人确认",
    match = ["git push", "git push origin codex/task-filter"],
)

prefix_rule(
    pattern = ["adb", "shell", "pm", "clear"],
    decision = "forbidden",
    justification = "禁止自动清除设备应用数据；请由开发者确认设备与包名后手工执行",
    match = ["adb shell pm clear com.example.pockettasks"],
)
```

`match` 与 `not_match` 是规则的内联测试，可以帮助发现前缀写错。多条规则命中时采用更严格的决定：`forbidden` 高于 `prompt`，`prompt` 高于 `allow`。

不要轻易写一个很宽的允许规则，例如允许全部 `./gradlew`。Gradle task 可以执行任意插件逻辑，测试、签名、发布的风险完全不同。对稳定、无外部副作用的具体 task 建立规则更合理。

可以用下面的命令检查规则怎样裁决：

```bash
codex execpolicy check --pretty \
  --rules .codex/rules/default.rules \
  -- git push origin codex/task-filter
```

## Android 项目的风险分级

建议团队把操作分成三档，而不是维护一张无限增长的命令黑名单。

低风险：读取文件、`rg` 搜索、目标单元测试、查看 diff。它们通常可以在沙箱内自动执行。

中风险：写源码、下载依赖、启动模拟器、执行全量仪器测试。允许执行，但需要清楚资源和环境影响。

高风险：清数据、修改签名与密钥、发布制品、操作生产后台、推送或合并远端。默认要求人工确认，有些应直接禁止 Agent 执行。

## 安全也包括提示词数据

即使命令没有破坏性，输入上下文仍可能包含敏感信息。不要让 Codex 读取 `local.properties`、签名文件、服务账号 JSON 或真实用户数据。凭据通过环境或专门的秘密管理系统提供，错误日志在分享前脱敏。

如果接入 MCP 或 Plugin，数据边界会进一步扩大。第 12 讲会专门处理“能连接外部系统”之后的权限问题。

## 小结

安全工程的目标不是让 Codex 什么都做不了，而是让能力与任务风险匹配：沙箱限制触达范围，审批保留人的决定权，Rules 把重复决定变成政策。

下一讲我们会处理另一种风险：多个任务同时改同一仓库时，怎样用 Git 分支和 Worktree 隔离现场，并随时回到可靠状态。

## 思考题

1. 你们 Android 环境里，哪条看似普通的命令可能触达生产或真实设备数据？
2. 哪些动作应该“每次询问”，哪些应该“永远禁止自动执行”？

## 延伸阅读

- [Codex 安全与审批](https://developers.openai.com/codex/security/)
- [Codex Rules](https://developers.openai.com/codex/rules/)

