# 第 20 讲：故障注入式审查

这个实验故意把“进行中”筛选的判断反转。请在干净的课程项目中执行：

```bash
./scripts/lab-patch.sh apply 20-review
./gradlew :app:testDebugUnitTest --tests '*TaskFilterTest' --rerun-tasks
```

预期只有 `active returns only unfinished visible tasks` 失败。不要直接读补丁猜答案；先用 `/review`
或 `$android-code-review` 对工作区改动做只读审查，再比较测试证据。

实验后先检查 diff，再运行 `./scripts/lab-patch.sh reverse 20-review`。
不要在目标文件已有个人改动时应用或恢复实验补丁。
