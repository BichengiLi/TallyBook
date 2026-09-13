# TallyBook — 记账本

个人记账 Android 应用。核心领域：手动记账、月度预算规划、收支统计分析。

## Language

**交易 (Transaction)**：
一笔记账记录，包含金额、类型（支出/收入）、分类、备注、日期。
_Avoid_: 记录、账单、流水

**支出 (Expense)**：
钱的流出。分类包括 FOOD（餐饮）、TRANSPORT（交通）、SHOPPING（购物）、ENTERTAINMENT（娱乐）、MEDICAL（医疗）、EDUCATION（教育）、OTHER（其他）。
_Avoid_: 消费、花费

**收入 (Income)**：
钱的流入。分类包括 SALARY（工资）、BONUS（奖金）、INVESTMENT（投资）、GIFT（礼金）、OTHER（其他）。
_Avoid_: 入账、进账

**日常类支出**：
FOOD、TRANSPORT、SHOPPING、MEDICAL、EDUCATION 五类。参与日预算计算，净支出（支出 − 收入）影响每日可用额度。
_Avoid_: 常规支出、必需支出

**其他类支出**：
ENTERTAINMENT、OTHER 两类。不参与日预算，由月度总额中的 otherBudget 覆盖，超支时从日常预算划拨补足。
_Avoid_: 非必需支出、弹性支出

**日预算 (DailyBudget)**：
每日可用的日常类支出额度。公式：(剩余月度日常预算 − 累计日常净支出) / 剩余天数，上限 60。每日一行，记录当天预算额、已用金额和剩余额度。
_Avoid_: 每日额度、日限额

**月度预算 (MonthlyBudget)**：
用户设定的月度总额（默认 2000），拆分为 dailyBudget（日常类份额）和 otherBudget（其他类份额）。
_Avoid_: 月预算、月总额

**自律奖励 (Self-Discipline Reward)**：
用户每日可领取一次的固定金额收入（默认 5 元）。通过主界面 ✕ 按钮展开面板中的"忍"按钮触发，自动记录一笔 INCOME / OTHER / note="奖励"。每日限一次，午夜重置，撤回不恢复资格。金额可长按配置。
_Avoid_: 忍按钮奖励、每日打卡奖励
