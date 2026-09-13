# 03 — ViewModel：自律奖励核心逻辑

Status: needs-triage

## 内容

- `TallyBookViewModel` 新增：
  - `isRewardClaimed: StateFlow<Boolean>` — 今日是否已领取（实时日期比对 `DailyBudget.rewardClaimed`）
  - `rewardAmount: StateFlow<Double>` — 当前配置金额（从 SharedPreferences 读取）
  - `claimReward()` — 记一笔 INCOME/OTHER/note="奖励"，插入交易 + 标记 rewardClaimed = true + 触发 Snackbar
  - `setRewardAmount(amount: Double)` — 更新配置金额
  - **撤回逻辑修改**：`undoLastTransaction()` 对自律奖励交易不恢复 rewardClaimed
  - 需要区分普通撤回和奖励撤回：可在 Transaction 上做标记判断，或记录最后一笔是否为奖励交易

## 关键细节

- 日期比对用 `Clock.System.todayIn(TimeZone.currentSystemDefault())` 实时取，不缓存
- 撤回不恢复：undo 只删交易，rewardClaimed 保持 true

## 相关

- PRD：`.scratch/self-discipline-reward/PRD.md`
- 依赖：#01（rewardClaimed 字段）、#02（SharedPreferences）
