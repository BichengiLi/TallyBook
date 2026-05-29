# PRD: 数据流优化与 UX 性能提升

Status: ready-for-agent

## Problem Statement

调整月度预算时，用户感知到明显延迟（调整到5000需要数秒反应）。页面切换动画过于缓慢，影响使用流畅度。预算修改只能通过滑块操作，无法直接输入数字。

根因分析：
1. 预算保存触发两次独立协程并发写 DB，时序不确定导致额外重算
2. `calculateDailyBudget()` 对 5 个分类各执行一次串行 SQL 查询
3. 日预算重算后又链式触发 `calculateDailyBudget(tomorrow)`，形成计算链
4. 页面间使用默认 Navigation Compose 动画
5. BudgetDialog 只有滑块没有文本输入

## Solution

1. 合并两个预算更新方法为原子操作，单次 DB 写入
2. 分类查询从 5 次串行整合为批量 SQL
3. 预算 Dialog 同时提供滑条和文本框，双向同步
4. 禁用/缩短页面切换动画
5. 去掉链式计算中的不必要重算

预期效果：预算调整从 2-3 秒降低到 <200ms，页面切换瞬时完成。

## User Stories

1. As a user, I want to type a budget number directly into a text field, so that I can set precise values quickly without dragging a slider
2. As a user, I want the budget change to take effect immediately after I confirm, so that I don't wait seconds for the app to respond
3. As a user, I want page transitions to be instant, so that navigating between screens feels snappy
4. As a user, I want the monthly budget total and the daily/other split to be saved in one atomic operation, so that intermediate states don't cause flickering
5. As a user, I want the daily budget to recalculate immediately when the monthly total changes, without unnecessary re-computation chains
6. As a user, I want the slider and text field to stay in sync, so that changing one updates the other in real time

## Implementation Decisions

### 1. 合并更新方法

- Repository 新增 `updateMonthlyBudget(monthlyTotalBudget, otherBudget, dailyBudget)` 单方法
- 内部在一个 suspend 函数中依次：写入 MonthlyBudget → 重算 DailyBudget
- 删除 `updateMonthlyTotalBudget` 和 `updateMonthlyRatio` 两个独立方法
- ViewModel 对应合并为单个 `updateMonthlyBudget` 调用

### 2. 批量分类查询

- DAO 新增一条 SQL：对 5 个日常分类的 SUM 合并为一个查询返回
- `calculateDailyBudget` 内部的 for 循环替换为单次查询
- 查询次数从 O(n) 降到 O(1)

### 3. TextField + Slider 双向同步

- 月度总预算：OutlinedTextField + Slider，联动绑定
- 日常支出预算：同上
- 其他预算自动计算
- 任一控件变更立即反映到另一个
- 移除旧的编辑/确认切换模式

### 4. 页面动画

- Navigation composable 添加 `enterTransition = fadeIn()` / `exitTransition = fadeOut()` 或直接使用无动画的 `snapIn`/`snapOut`
- 动画时长缩短到 150ms 或完全禁用

### 5. 消除链式计算

- `updateBudgetSpent` 中不再调用 `calculateDailyBudget(tomorrow)`
- 明天的预算在明天首次打开 App 时懒计算（`initializeBudgetForDate` 已覆盖此场景）

## Out of Scope

- 数据库整体迁移到 KMP/SQLDelight
- 后端同步功能
- 完整 UI 重新设计

## Further Notes

- Room migration 版本号可能需再次升级（如果之前未达 version 3 的用户跳过了上次 migration）
- 所有改动向后兼容，不影响已有数据
