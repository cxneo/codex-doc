# 第 22 讲：Room 迁移注册故障

这个实验模拟“迁移类仍在，但应用构建数据库时忘记注册”的线上风险：

```bash
./scripts/lab-patch.sh apply 22-migration
./gradlew :app:assembleDebug
```

构建仍可能成功，这正是事故危险之处。随后启动一个保留 v1 数据库的升级场景，
应用会因为缺少 1→2 路径而无法打开数据库。课程中的
`DatabaseMigrationTest` 显式注册迁移，因此它证明的是迁移 SQL；评审还必须检查
生产 `Room.databaseBuilder` 是否注册同一条路径。

完成调查后，先检查 diff，再运行
`./scripts/lab-patch.sh reverse 22-migration` 恢复实验改动。
