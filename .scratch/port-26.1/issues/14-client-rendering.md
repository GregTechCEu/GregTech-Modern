Type: task
Status: ready-for-agent
Blocked by: 09, 10, 11, 13

## Question

迁移 GTM 的 client UI、实体/方块实体/物品渲染、粒子、动态高亮、shader 触点和 mixin，使 vanilla 26.1.2 client 可启动并显示核心 GTM 内容。

## Context

主 spec：[`spec.md`](../spec.md)。渲染/mixin 差异：[`05-api-gap-inventory.md`](05-api-gap-inventory.md)。MUI 依赖：[完整 MUI 移植票](09-mui-port.md)。

## Work

- 迁移 entity render state、block entity render state、GUI render state、SubmitNodeCollector、item special model、particle、overlay、tooltip 和 dynamic highlight API。
- 将 GTM machine screen、cover screen、recipe viewer、multiblock preview 和 MUI-backed widgets 接到目标 client lifecycle。
- 迁移或删除失效的 Embeddium/Oculus/Create client hooks；目标基线以 vanilla client 为准，不把 Sodium/Iris 作为硬依赖。
- 审核所有 client mixin target、refmap、injection point、compatibility level 和 side-only class。
- 修复颜色、深度、资源解析、模型烘焙、shader JSON 和动态 buffer 在两阶段渲染中的行为。

## Acceptance

- clean client 启动并能打开代表性 machine/cover/recipe-viewer UI。
- 代表性 machine block entity、pipe/cable、item model、particle、tooltip、multiblock preview 和 highlight 无加载崩溃。
- mixin 应用日志无失效目标；被砍集成的 mixin 不再参与加载。
- 在 vanilla 26.1.2 client 下完成手动 smoke check；若启用 Sodium/Iris，只作为额外兼容验证，不得成为核心验收前置。
