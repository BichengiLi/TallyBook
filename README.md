# 记账本 (TallyBook)

一个Android记账应用，具有自动预算规划功能。

## 功能特性

### 1. 手动记账
- 支持添加支出和收入记录
- 多种分类选择（餐饮、交通、购物、娱乐、医疗、教育等）
- 添加备注信息
- 查看历史记录

### 2. 自动预算规划
- 每日默认预算：50元
- **超支规则**：如果当天支出超过预算，超出部分将从第二天的预算中扣除
- **节省规则**：如果当天支出低于预算，节省的金额不会累积到第二天，第二天仍维持50元预算
- 实时显示预算使用情况和剩余金额

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
│   ├── TransactionDao.kt    # 交易记录DAO
│   ├── BudgetDao.kt         # 预算DAO
│   ├── TallyBookDatabase.kt # Room数据库
│   ├── Converters.kt        # 类型转换器
│   └── TallyBookRepository.kt # 数据仓库
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
│       └── StatisticsScreen.kt
└── MainActivity.kt          # 主Activity
```

## 预算规则详解

### 计算公式

1. **每日基础预算**: 50元
2. **超支情况**:
   - 明日预算 = 50元 - 今日超支金额
   - 如果计算结果为负数，则明日预算为0元
3. **节省情况**:
   - 明日预算 = 50元（不累积节省金额）

### 示例

| 日期 | 预算 | 实际支出 | 状态 | 明日预算 |
|------|------|----------|------|----------|
| 第1天 | 50元 | 60元 | 超支10元 | 40元 |
| 第2天 | 40元 | 35元 | 节省5元 | 50元 |
| 第3天 | 50元 | 50元 | 刚好用完 | 50元 |
| 第4天 | 50元 | 45元 | 节省5元 | 50元 |

## 关于微信登录和同步

### 当前状态
目前版本**不支持**微信登录和自动同步功能。

### 原因说明
1. **微信开放平台限制**: 微信登录需要企业资质申请开放平台账号
2. **数据同步复杂**: 需要搭建后端服务器处理数据同步
3. **隐私安全**: 涉及用户财务数据，需要更完善的安全措施

### 未来可能的实现方案
1. 使用微信开放平台SDK实现登录（需要企业资质）
2. 使用云服务（如Firebase、阿里云等）实现数据同步
3. 本地数据导出/导入功能作为备选方案

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
