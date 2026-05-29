# 01 — 合并 Repository 预算更新为原子操作

Status: ready-for-agent

## Parent

[PRD: 数据流优化与 UX 性能提升](../PRD.md)

## What to build

将 `updateMonthlyTotalBudget` 和 `updateMonthlyRatio` 两个独立的 Repository 方法合并为一个原子操作 `updateMonthlyBudget(monthlyTotalBudget, otherBudget, dailyBudget)`。

- 单次 `viewModelScope.launch` 内完成 MonthlyBudget 写入 + DailyBudget 重算
- ViewModel 暴露单个合并方法替代原来的两个
- 调用方（HomeScreen BudgetRatioDialog）改为传三个参数

## Acceptance criteria

- [ ] `TallyBookRepository` 只有一个 `updateMonthlyBudget` 方法
- [ ] `TallyBookViewModel` 只有一个对外的预算更新方法
- [ ] HomeScreen 中 onConfirm 只触发一次协程
- [ ] 预算更新后即时刷新 UI，无 flickering

## Blocked by

None — can start immediately
