# TallyBook — 记账本

Android 记账应用，Kotlin + Jetpack Compose + Room。核心功能：手动记账、月度预算自动规划、统计分析。

## 架构

MVVM：Room (TransactionDao / BudgetDao / MonthlyBudgetDao) → TallyBookRepository → TallyBookViewModel → Compose UI

## 核心业务逻辑

预算模型：月度总预算 60元/天（日常40 + 其他20），每日动态重算。

- **日常类**（FOOD, TRANSPORT, SHOPPING, MEDICAL, EDUCATION）：日预算 = (剩余月度日常预算 - 累计日常净支出) / 剩余天数，上限60
- **其他类**（ENTERTAINMENT, OTHER）：超支时自动从日常预算划拨补足
- **收入**：抵扣日常支出（净支出 = max(日常支出 - 收入, 0)）
- 核心算法在 `TallyBookRepository.calculateDailyBudget()`

## Agent skills

### Issue tracker

Issues live as local markdown files under `.scratch/<feature>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the five canonical triage labels with default names. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context repo — one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
