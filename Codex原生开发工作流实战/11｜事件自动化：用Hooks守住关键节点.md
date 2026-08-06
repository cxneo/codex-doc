# 11｜事件自动化：用 Hooks 守住关键节点

“记得不要清真机数据”不是工程机制。Hooks 让一段确定性程序在会话或工具调用的固定节点执行，适合检查敏感输入、命令风险、验证证据和审计信息。

这一讲不追求写一个万能 Hook，而是把 PocketTasks 的 `adb shell pm clear` 拦下来，并用自动测试证明它真的被拦截。

## 先决定在哪个事件介入

| 时机 | 事件 | Android 示例 |
|---|---|---|
| 会话或子代理启动 | `SessionStart`、`SubagentStart` | 注入本机 SDK 状态或任务说明 |
| 用户提交请求 | `UserPromptSubmit` | 检测误贴 Token 或用户数据 |
| 工具执行前 | `PreToolUse` | 阻断清数据、发布或读取签名材料 |
| 请求权限时 | `PermissionRequest` | 按组织规则检查越界原因 |
| 工具执行后 | `PostToolUse` | 记录 Gradle 结果，不伪造通过 |
| 上下文压缩前后 | `PreCompact`、`PostCompact` | 保留阶段目标与未完成验证 |
| 子代理或当前轮准备结束 | `SubagentStop`、`Stop` | 检查交付摘要是否有证据 |
| 主会话结束 | `SessionEnd` | 写入脱敏审计信息 |

同一事件可能从多个配置来源匹配，而且匹配的命令 Hook 会并发启动。因此一个 Hook 不能假设自己能阻止另一个 Hook 启动。

## 配置从哪里来，为什么必须先信任

Codex 会从用户、项目、插件和受管配置层发现 `hooks.json` 或 `config.toml` 中的 `[hooks]`。项目 Hook 只有在 `.codex/` 项目层被信任时才加载。

非受管 Hook 是会自动执行的本地代码。Codex 按定义哈希记录信任；脚本或配置变化后，需要重新审查。使用 `/hooks` 查看来源、状态、信任或禁用结果。不要因为“它在公司仓库里”就跳过代码审查。

课程项目使用 [`.codex/hooks.json`](./配套文件/PocketTasks-codex/.codex/hooks.json)：

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "/bin/sh -c '从当前目录逐级向上定位 .codex/hooks/pre_tool_use.py 并执行'",
            "timeout": 5
          }
        ]
      }
    ]
  }
}
```

正文为可读性缩写了定位命令，完整值以课程 `hooks.json` 为准。它从当前目录逐级向上寻找项目脚本，因此从项目根、`app/` 子目录启动，或项目嵌在课程仓库中都能工作。当前可执行处理器类型是 `command`；配置字段中出现其他处理器类型，不代表它们已经会运行。

## 读懂 PreToolUse 的真实输入

`PreToolUse` 通过标准输入收到 JSON。Bash 命令位于 `tool_input.command`：

```json
{
  "tool_name": "Bash",
  "tool_input": {
    "command": "adb shell pm clear com.example.pockettasks"
  }
}
```

课程脚本 [`.codex/hooks/pre_tool_use.py`](./配套文件/PocketTasks-codex/.codex/hooks/pre_tool_use.py) 的关键路径可以逐行理解为：

1. 从 stdin 解析 JSON；无法解析或缺少 Bash 命令字段时以退出码 `2` 阻断，不猜字段；
2. 只处理 `tool_name == "Bash"`；
3. 从字典型 `tool_input` 读取字符串 `command`；
4. 用小范围模式匹配清数据、发布和敏感配置读取；
5. 命中后在 stdout 返回 Codex 认识的拒绝对象。

拒绝输出是：

```json
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Blocked automatic app-data deletion..."
  }
}
```

另一种阻断方式是以退出码 `2` 结束并把原因写入 stderr。课程选择结构化 JSON，因为测试可以明确断言决定与原因。其他异常退出通常表示 Hook 自己失败，不应被当成一条经过设计的拒绝政策。

## 不启动 Codex也能测试 Hook

课程提供黑盒测试 [`.codex/hooks/test_pre_tool_use.py`](./配套文件/PocketTasks-codex/.codex/hooks/test_pre_tool_use.py)：

```bash
cd 配套文件/PocketTasks-codex
python3 .codex/hooks/test_pre_tool_use.py
```

它覆盖五条边界：

- 允许窄范围单元测试；
- 拒绝 `adb shell pm clear`；
- 拒绝 Gradle 发布任务；
- 同样文本来自非 Bash 工具时不误杀；
- 事件 JSON 损坏时 fail closed，而不是放行未知 Bash 命令。

预期输出包含 `Ran 5 tests` 和 `OK`。如果只是手工喂一条危险命令，没有反例，过宽正则仍可能让日常开发无法工作。

你也可以直接观察事件合同：

```bash
printf '%s' '{"tool_name":"Bash","tool_input":{"command":"adb shell pm clear com.example.pockettasks"}}' \
  | python3 .codex/hooks/pre_tool_use.py
```

## Rules 与 Hooks 怎样分工

固定命令前缀优先写入 [`.codex/rules/default.rules`](./配套文件/PocketTasks-codex/.codex/rules/default.rules)：它可读、可测试，专门表达执行政策。

Hook 更适合必须解析上下文的判断，例如：

- `adb` 目标是不是受保护真机；
- 发布命令是否处在受保护分支；
- 文件路径是否落入签名材料；
- Stop 时是否存在已声明通过却没有记录的测试。

不要在 Hook 中再造一套模糊的 Shell 黑名单，也不要把 Lint 全量构建挂到每个 Kotlin 文件写入后。门禁越慢、误报越多，团队越容易绕开它。

## 上线 Hook 的四步法

1. **观察**：只记录匹配，不影响执行，先看误报；
2. **测试**：为允许、拒绝、缺失字段和异常输入写样例；
3. **阻断**：只拦确定的高风险动作，原因必须告诉人如何安全继续；
4. **受管**：组织强制策略才进入 `requirements.toml` 的 managed hooks；普通项目 Hook 保留团队可审查性。

启用或变更后，在 Codex 中运行 `/hooks`，确认加载的是预期文件和哈希。`[features] hooks = false` 可以关闭 Hooks；组织也能只允许受管 Hooks。

## 两个易踩坑

### 相对路径在模块目录失效

`python3 .codex/hooks/a.py` 假设当前目录就是项目根，而 `git rev-parse --show-toplevel` 在课程这种嵌套项目里又可能得到外层文档仓库。Android 开发者常从 `app/` 启动 Codex，所以课程配置逐级向上寻找最近的 `.codex/hooks/pre_tool_use.py`。

### 日志本身泄密

Hook 能看到提示词、命令和输出。不要把完整内容上传到遥测系统；先做字段最小化、脱敏、保留期与访问控制。安全 Hook 也可能成为新的数据出口。

## 小结

Hooks 的价值不在脚本语言，而在稳定的事件、结构化输入输出和可测试政策。PocketTasks 的第一个 Hook 很小：只在 Bash 前检查几类高风险命令，但它有真实配置、真实拒绝协议和四条自动测试。

下一讲会连接仓库外的系统。外部工具的读取和写入权限，需要沿用本讲的同一原则：先小范围可见，再逐步开放能力。

## 延伸阅读

- [Codex Hooks](https://developers.openai.com/codex/hooks/)
- [课程 Hook 配置](./配套文件/PocketTasks-codex/.codex/hooks.json)
- [课程 Hook 测试](./配套文件/PocketTasks-codex/.codex/hooks/test_pre_tool_use.py)
