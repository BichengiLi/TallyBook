# 03 — BudgetDialog 文本框 + 滑块双向同步

Status: ready-for-agent

## Parent

[PRD: 数据流优化与 UX 性能提升](../PRD.md)

## What to build

当前 BudgetDialog 只有 Slider 调整预算。改为 OutlinedTextField + Slider 双向绑定：
- 月度总预算：文本框输入数字 + 滑块（500~5000），任一变更即时同步到另一个
- 日常预算：同上
- 其他支出：自动计算 = 总预算 - 日常预算
- 实时显示更新后的金额

## Acceptance criteria

- [ ] 月度总预算可文本框直接输入
- [ ] 日常预算可文本框直接输入
- [ ] 修改滑块时文本框同步更新
- [ ] 修改文本框时滑块同步更新
- [ ] 其他支出金额自动计算展示

## Blocked by

None — can start immediately
