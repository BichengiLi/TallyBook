# 04 — 去掉链式计算

Status: ready-for-agent

## Parent

[PRD: 数据流优化与 UX 性能提升](../PRD.md)

## What to build

`updateBudgetSpent` 中记录完当天支出后，调用 `calculateDailyBudget(tomorrow)` 触发明天的预算重算。这个链式调用是不必要的——明天的预算在明天首次打开 App 时 `initializeBudgetForDate` 已能覆盖。

- 从 `updateBudgetSpent` 移除 `calculateDailyBudget(tomorrow)` 调用
- 验证 `initializeBudgetForDate` 在日期切换时的行为正确

## Acceptance criteria

- [ ] `updateBudgetSpent` 不再调用 `calculateDailyBudget(tomorrow)`
- [ ] 第二天记账时预算正确计算
- [ ] 不影响当天剩余预算显示

## Blocked by

None — can start immediately
