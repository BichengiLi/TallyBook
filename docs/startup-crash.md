# 启动闪退排查

先获取异常堆栈，区分 APK 与数据库版本不匹配、迁移失败以及数据转换异常。系统升级本身不会递增 Room 的数据库版本，不能仅凭升级时间判断原因。

## 采集日志

连接手机，开启并授权 USB 调试后运行：

```powershell
adb devices -l
adb logcat -b crash -d -v threadtime
adb shell dumpsys activity exit-info com.example.tallybook
adb shell dumpsys package com.example.tallybook
```

查找 `Process: com.example.tallybook` 对应的完整异常。先保存现有日志，再尝试启动；不要清空日志或应用数据。

## 已确认的 APK 与数据库不匹配

2026-10-01 真机排查复现：

```text
java.lang.IllegalStateException:
A migration from 4 to 3 was required but not found.
```

手机数据库 `PRAGMA user_version` 为 4，含 `daily_budgets.rewardClaimed`；数据库完整性检查通过。已安装 APK 的 Room 版本为 3，当前源码为 4。旧 APK 在启动查询月度预算时触发数据库降级请求，缺少 4→3 迁移，异常从协程传播到主线程并终止应用。

恢复方式是安装支持 v4 的当前 APK。覆盖安装前备份数据库及 WAL（如存在），核对应用 ID、签名和 `versionCode`，覆盖安装后核对交易内容及冷启动。备份与真机日志保存在被 Git 忽略的 `app/build/diagnostics/`，不提交个人账目。

本次日志证明了版本不匹配，尚不能证明是哪一次系统更新或备份恢复造成的。项目过去一直使用 `versionCode = 1`，无法依靠 APK 版本号区分不同代码版本；今后每次发布必须递增。

不要添加 `fallbackToDestructiveMigrationOnDowngrade()`：它会通过丢弃数据库掩盖错误。也不要降低当前 Room 版本来迎合旧 APK。

## 历史 v2 数据库兼容

提交 `9284a82` 给 `MonthlyBudget` 增加了 `monthlyTotalBudget`，但仍使用 Room v2。之后 2→3 迁移无条件新增同名列，会在这种历史数据库上报：

```text
duplicate column name: monthlyTotalBudget
```

迁移应读取 `PRAGMA table_info(monthly_budgets)`，仅在缺少该列时新增，已存在时保留原值。此问题与上述真机的 4→3 异常不同。

`TallyBookDatabaseMigrationTest` 覆盖 v1、原始 v2、已有该列的 v2、v3、v4 重启及全新数据库；通过实际 Room 校验、DAO 查询和 Repository 启动预算初始化验证数据保留。
