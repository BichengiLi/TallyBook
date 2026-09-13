# 自律奖励日限额用 DailyBudget 字段追踪

自律奖励每人每天只能领一次。判断"今天领过没"有三种方案：(A) 查今天是否有 INCOME/OTHER/note="奖励" 的交易；(B) 在 `DailyBudget` 表加 `rewardClaimed: Boolean`；(C) SharedPreferences 存 `last_claim_date`。

我们选 **B**。

**Considered Options**

- **A（查询交易）**：零新存储，但脆弱——用户手动记一笔相同分类备注的交易，按钮就永久失效。
- **C（SharedPreferences）**：最简单，不碰数据库。但清应用数据即丢失，无法跨设备恢复，且和业务逻辑分离——其他每日状态（DailyBudget、交易）都在 Room 里，唯独这个在外面，不一致。
- **B（DailyBudget.rewardClaimed）**：需要 Room 迁移 3→4，但语义正确——每日预算行本身承载当日所有每日状态，且是单一可信源。

**Consequences**

- 需要 Room migration 3→4，新增 `rewardClaimed` 列到 `daily_budgets` 表，默认 `false`。
- 以后其他"每日一次"的功能都可以走同样的字段模式（再加一列即可）。
