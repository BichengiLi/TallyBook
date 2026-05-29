# 记账本 (TallyBook)

一个 Android 记账应用，支持手动记账、月度预算自动规划和统计分析。

## 功能特性

### 1. 手动记账
- 支持添加支出和收入记录
- 支出分类：餐饮、交通、购物、娱乐、医疗、教育、其他
- 收入分类：工资、奖金、投资、礼物、其他
- 添加备注信息
- 支持撤回上一条记录

### 2. 自动预算规划
- 月度预算模型：默认月度总预算 2000 元，其中日常预算 40 元/天、其他预算 20 元/天
- **月度总预算可调**：支持文本框直接输入或滑块拖拽（范围 500~5000 元）
- **分类核算**：日常类（餐饮、交通、购物、医疗、教育）与其他类（娱乐、其他）分开计算
- **收入抵扣**：日常支出可被当日收入抵扣，净支出 = max(日常支出 - 收入, 0)
- **动态日预算**：每日预算 = (本月剩余日常预算 - 本月累计日常净支出) / 本月剩余天数，上限 60 元
- **其他类超支自动调整**：当娱乐/其他支出超出当月其他预算时，自动从日常预算中划拨补足

### 3. 统计分析
- 月度收支概览（结余 = 月度总预算 - 本月净支出）
- 支出分类统计

### 4. 月度记录管理
- 查看历史每月预算执行情况
- 删除历史月份记录（含确认弹窗，当月不可删除）
- 删除操作清理该月全部数据（交易记录 + 日预算 + 月度预算）

### 5. 筛选功能
- 本月记录页支持下拉菜单筛选
- 按支出/收入大类筛选
- 按具体分类（餐饮、交通、工资、奖金等）精确筛选

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM
- **数据库**: Room
- **导航**: Navigation Compose（slide+fade 250ms 过渡动画）
- **异步处理**: Kotlin Coroutines + Flow
- **日期处理**: kotlinx-datetime
- **主题**: 固定白天模式

## 项目结构

```
app/src/main/java/com/example/tallybook/
├── data/                    # 数据层
│   ├── Transaction.kt       # 交易记录数据模型
│   ├── DailyBudget.kt       # 每日预算数据模型
│   ├── MonthlyBudget.kt     # 月度预算数据模型
│   ├── TransactionDao.kt    # 交易记录DAO
│   ├── BudgetDao.kt         # 每日预算DAO
│   ├── MonthlyBudgetDao.kt  # 月度预算DAO
│   ├── TallyBookDatabase.kt # Room数据库（v3，含迁移脚本）
│   ├── Converters.kt        # 类型转换器
│   └── TallyBookRepository.kt # 数据仓库（核心预算算法）
├── viewmodel/               # 视图模型
│   └── TallyBookViewModel.kt
├── ui/                      # UI层
│   ├── theme/               # 主题
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── navigation/          # 导航
│   │   └── TallyBookNavigation.kt
│   └── screens/             # 页面
│       ├── HomeScreen.kt
│       ├── AddTransactionScreen.kt
│       ├── HistoryScreen.kt
│       ├── MonthlyHistoryScreen.kt
│       └── StatisticsScreen.kt
└── MainActivity.kt          # 主Activity
```

## 预算规则详解

### 数据模型

| 字段 | 默认值 | 说明 |
|------|--------|------|
| monthlyTotalBudget | 2000 元 | 月度总预算（可调整，范围 500~5000）|
| dailyBudget | 40 元 | 日常类预算（餐饮、交通、购物、医疗、教育）|
| otherBudget | 20 元 | 其他类预算（娱乐、其他）|

### 每日预算计算

```
本月剩余日常预算 = max(月度日常预算 - 本月1号至昨日的日常净支出, 0)
日常净支出 = max(日常支出 - 收入, 0)
今日预算 = min(本月剩余日常预算 / 本月剩余天数, 60)
```

### 其他类超支处理

当娱乐/其他累计支出超过 `otherBudget` 时，自动扩大 `otherBudget` 并缩窄 `dailyBudget`，保持总额不变。

## 构建说明

1. 使用Android Studio打开项目
2. 等待Gradle同步完成
3. 连接Android设备或启动模拟器
4. 点击运行按钮

## 系统要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK 26 或更高版本
- Kotlin 1.9.20 或更高版本

## 许可证

本项目仅供学习和个人使用。
