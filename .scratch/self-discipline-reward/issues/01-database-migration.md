# 01 — Room 迁移 3→4：新增 rewardClaimed 字段

Status: needs-triage

## 内容

- `DailyBudget` entity 新增 `rewardClaimed: Boolean = false`
- Room 数据库版本升至 4，添加 3→4 migration
- 迁移 SQL：`ALTER TABLE daily_budgets ADD COLUMN rewardClaimed INTEGER NOT NULL DEFAULT 0`
- `TransactionDao` / `BudgetDao` / `TallyBookRepository` 中对应查询和读写逻辑，确保 rewardClaimed 正确存取

## 相关

- PRD：`.scratch/self-discipline-reward/PRD.md`
- ADR：`docs/adr/0001-dailybudget-reward-field.md`
- 依赖：无（第一个任务）
