# 04 — ExpandableFAB 组件：可展开浮钮 + 侧伸面板

Status: needs-triage

## 内容

创建 `ExpandableFAB` Composable 组件，包含：

### 主按钮
- 圆底（CircleShape，AnimePink），内部显示 ✕（纯文字/图标）
- 点击展开/收回：逆时针旋转 90°（展开）、顺时针旋转 90°（收回），rotate 动画 250ms
- 展开时点击面板外区域 → 收回

### 侧伸面板
- 白色圆角卡片（RoundedCornerShape），从主按钮向左水平伸出
- 内含两个按钮水平排列：
  - **"忍"按钮**：圆底，纯文字"忍"（白字 AnimePink 底）
    - 正常态：可点击
    - 禁用态（已领取）：灰色、alpha 降低，点击时 haptic feedback（震动）
  - **"+" 按钮**：圆底，`+` 图标（白字 AnimePink 底），始终可用
- 展开动画：fadeIn + slideInHorizontally（从右往左），250ms
- 收回动画：fadeOut + slideOutHorizontally（从左往右），250ms

### 参数接口
```kotlin
@Composable
fun ExpandableFAB(
    isRewardClaimed: Boolean,
    onRewardClick: () -> Unit,
    onAddClick: () -> Unit,
    onRewardLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

## 相关

- PRD：`.scratch/self-discipline-reward/PRD.md`
- 依赖：#03（需要 isRewardClaimed 状态和 onRewardClick 回调签名）
