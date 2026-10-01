# TallyBook — 记账本

Android 记账应用，Kotlin + Jetpack Compose + Room。核心功能：手动记账、月度预算自动规划、统计分析。

## 架构

MVVM：Room (TransactionDao / BudgetDao / MonthlyBudgetDao) → TallyBookRepository → TallyBookViewModel → Compose UI

数据流已优化：预算更新为单次原子操作，分类查询使用批量 SQL，无链式计算。

## 核心业务逻辑

- **月度预算模型**：monthlyTotalBudget（默认2000，可调），拆分为 dailyBudget + otherBudget
- **日常类**（FOOD, TRANSPORT, SHOPPING, MEDICAL, EDUCATION）：日预算 = (剩余月度日常预算 - 累计日常净支出) / 剩余天数，上限60
- **其他类**（ENTERTAINMENT, OTHER）：超支时自动从日常预算划拨补足
- **收入**：抵扣日常支出（净支出 = max(日常支出 - 收入, 0)）
- 核心算法在 `TallyBookRepository.calculateDailyBudget()`

## 数据库

Room v4，含迁移 1→2（创建 monthly_budgets 表）、2→3（新增 monthlyTotalBudget 列）和 3→4（daily_budgets 新增 rewardClaimed 列）。

历史 v2 有两种结构：提交 `9284a82` 已新增 monthlyTotalBudget，但没有递增数据库版本。2→3 迁移必须先检测列是否存在，保留已有值。数据库版本不得为适配旧 APK 而降低；不得通过破坏性迁移清空账目。

APK 发布必须递增 `app/build.gradle.kts` 的 versionCode，独立于 Room 版本。数据库迁移及冷启动测试在 `app/src/test/`，运行 `gradlew.bat :app:testDebugUnitTest`（Robolectric，无需设备）。

## UI

- 固定白天模式
- 页面过渡：slide + fade 250ms
- 预算调整：文本框 + 滑块双向同步
- 本月记录筛选：下拉菜单，分支出/收入两大类 + 子分类
- 月度记录管理：可删除历史月份（含确认弹窗，当月不可删），清理三表数据

## Operations

- `deleteMonthData(month)` — 删除整月数据（transactions + daily_budgets + monthly_budgets），Repository 层事务性操作

## 工作流

- PRD/Issues 存储在 `.scratch/<feature-slug>/`（如 `.scratch/self-discipline-reward/`）
- Triage 标签：needs-triage / needs-info / ready-for-agent / ready-for-human / wontfix

## Agent skills

### Issue tracker

Issues live as local markdown files under `.scratch/<feature>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the five canonical triage labels with default names. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context repo — one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
