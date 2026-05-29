# 05 — 页面切换动画优化

Status: ready-for-agent

## Parent

[PRD: 数据流优化与 UX 性能提升](../PRD.md)

## What to build

Navigation Compose 使用默认动画（fade-through ~300-400ms），用户感觉页面切换缓慢。修改 `TallyBookNavigation` 中每个 `composable()` 的 enterTransition / exitTransition，使用更快或完全禁用动画。

- 所有页面切换改为 popEnterTransition / enterTransition / popExitTransition / exitTransition
- 动画时长 ≤ 150ms 或使用 snapIn/snapOut（无动画）
- 返回动画同理

## Acceptance criteria

- [ ] 所有 composable 路由配置了自定义 transition
- [ ] 页面切换视觉上明显加快
- [ ] 返回操作同样流畅

## Blocked by

None — can start immediately
