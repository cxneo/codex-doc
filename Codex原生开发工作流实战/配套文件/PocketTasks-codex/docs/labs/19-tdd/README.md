# 第 19 讲 RED 基线

课程主分支保持全部测试通过。为了练习真正的 RED → GREEN，请在一次性练习分支中运行：

```bash
git switch -c codex/lab-19-tdd
./scripts/lab-patch.sh apply 19-tdd
./gradlew :app:testDebugUnitTest \
  --tests 'com.example.pockettasks.model.TaskFilterTest' \
  --rerun-tasks
```

`all hides archived tasks but keeps active and completed tasks` 等归档相关断言应失败。失败原因必须是归档任务仍然可见，而不是环境或编译错误。

只修改 `TaskFilter.kt` 恢复归档过滤，然后重新运行目标测试。完成后用 `git diff` 检查改动半径。

如果只想退出实验而不保留实现，先检查 diff，再运行
`./scripts/lab-patch.sh reverse 19-tdd`。不要用宽范围恢复命令覆盖个人改动。
