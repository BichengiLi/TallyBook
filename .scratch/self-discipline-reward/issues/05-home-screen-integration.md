# 05 — HomeScreen 集成：替换 FAB + 接线

Status: needs-triage

## 内容

- `HomeScreen` 中用 `ExpandableFAB` 替换现有 `floatingActionButton`
- `onRewardClick` → `viewModel.claimReward()`
- `onAddClick` → `onNavigateToAddTransaction`（原 FAB 行为）
- Snackbar 保持现有机制，`claimReward()` 复用同一个 `showUndoSnackbar` 流程
- 长按金额调整弹窗（Dialog + TextField + 滑块，参考现有预算调整弹窗模式）

## 相关

- PRD：`.scratch/self-discipline-reward/PRD.md`
- 依赖：#04（ExpandableFAB 组件）、#03（ViewModel 逻辑）
