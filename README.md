# 记账本 (TallyBook)

一个 Android 记账应用，支持手动记账、自动预算规划和统计分析。

## 功能特性

### 1. 手动记账
- 支持添加支出和收入记录
- 支出分类：餐饮、交通、购物、娱乐、医疗、教育、其他
- 收入分类：工资、奖金、投资、礼物、其他
- 添加备注信息
- 支持撤回上一条记录

### 2. 自动预算规划
- 月度预算模型：默认总额 60 元/天，其中日常预算 40 元/天、其他预算 20 元/天
- **分类核算**：日常类（餐饮、交通、购物、医疗、教育）与其他类（娱乐、其他）分开计算
- **收入抵扣**：日常支出可被当日收入抵扣，净支出 = max(日常支出 - 收入, 0)
- **动态日预算**：每日预算 = (本月剩余日常预算 - 本月累计日常净支出) / 本月剩余天数，上限 60 元
- **其他类超支自动调整**：当娱乐/其他支出超出当月其他预算时，自动从日常预算中划拨补足
- 可在设置页手动调整日常预算和其他预算的比例

### 3. 统计分析
- 月度收支概览
- 支出分类统计
- 预算执行情况分析

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM
- **数据库**: Room
- **导航**: Navigation Compose
- **异步处理**: Kotlin Coroutines + Flow
- **日期处理**: kotlinx-datetime

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
│   ├── TallyBookDatabase.kt # Room数据库
│   ├── Converters.kt        # 类型转换器
│   └── TallyBookRepository.kt # 数据仓库（含核心预算算法）
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

每月预算由三个数值组成：

| 字段 | 默认值 | 说明 |
|------|--------|------|
| totalBudget | 60 元 | 月度总预算（每日上限） |
| dailyBudget | 40 元 | 日常类预算（餐饮、交通、购物、医疗、教育） |
| otherBudget | 20 元 | 其他类预算（娱乐、其他） |

### 每日预算计算公式

```
本月剩余日常预算 = max(月度日常预算 - 本月1号至昨日的日常净支出, 0)
日常净支出 = max(日常支出 - 收入, 0)
今日预算 = min(本月剩余日常预算 / 本月剩余天数, 60)
```

即：把本月的日常预算余额均匀分配到剩余天数，单日不超过 60 元。

### 其他类超支处理

当娱乐/其他累计支出超过 `otherBudget` 时，自动扩大 `otherBudget` 并缩窄 `dailyBudget`，保持 `totalBudget` 不变。

### 示例

| 场景 | 月度日常预算 | 累计日常净支出 | 剩余天数 | 今日预算 |
|------|-------------|---------------|---------|---------|
| 月初 | 40元 | 0元 | 30天 | 40/30 ≈ 1.33元 |
| 月中无支出 | 40元 | 0元 | 15天 | min(40/15, 60) ≈ 2.67元 |
| 月中超支 | 40元 | 100元(日常) | 15天 | 0元 |

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
