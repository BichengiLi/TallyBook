# 02 — DAO 批量分类查询

Status: ready-for-agent

## Parent

[PRD: 数据流优化与 UX 性能提升](../PRD.md)

## What to build

`calculateDailyBudget` 内部对 5 个日常分类（FOOD, TRANSPORT, SHOPPING, MEDICAL, EDUCATION）各执行一次单独 SQL 查询。改为单条批量查询，将 5 次 DB 往返降到 1 次。

- TransactionDao 新增一个方法，接受分类列表和日期范围，返回批量 SUM
- Repository 改为调用新方法，移除 for-loop

## Acceptance criteria

- [ ] `calculateDailyBudget` 不再使用 for 循环逐分类查询
- [ ] 新 DAO 方法单次返回所有分类的合计
- [ ] 功能行为不变——预算计算结果与之前一致

## Blocked by

None — can start immediately
