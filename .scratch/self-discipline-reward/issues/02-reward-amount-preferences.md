# 02 — SharedPreferences 存储自律奖励金额

Status: needs-triage

## 内容

- 创建 `RewardPreferences` 工具类/对象，封装 SharedPreferences 读写
- 接口：`getRewardAmount(): Double`（默认 5.0）、`setRewardAmount(amount: Double)`
- 使用 `Flow<Double>` 或 `StateFlow<Double>` 暴露当前金额，以便 UI 响应式更新
- 金额范围：最小值 1 元

## 相关

- PRD：`.scratch/self-discipline-reward/PRD.md`
- 依赖：无（可和 #01 并行）
