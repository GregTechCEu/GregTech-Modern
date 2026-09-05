Type: task
Status: ready-for-agent
Blocked by: 10

## Question

迁移 GTM 的 item/fluid/energy transfer 和 capability 边界到 NeoForge 26.1.2，同时保持机器、管道、线缆、cover 和外部自动化行为。

## Context

主 spec：[`spec.md`](../spec.md)。重点研究：[`05-api-gap-inventory.md`](05-api-gap-inventory.md)。现有 machine/cover/capability 行为测试是主要 prior art。

## Work

- 将 Forge capability exposure、LazyOptional/provider、ForgeCapabilities 和旧 handler 访问改为目标 NeoForge capability/transfer model。
- 评估并迁移 ResourceHandler、EnergyHandler、transaction、snapshot/rollback、side access 和 context 语义。
- 在 GTM 自有 transfer API 与 NeoForge API 之间建立集中适配边界，避免所有机器和 cover 直接散落 target API 迁移。
- 逐类迁移 item bus、fluid pipe、energy cable、machine ability、cover handler、external capability provider 和 compatibility wrappers。
- 保持 simulate/execute、insert/extract、capacity、side、transaction failure 和 invalidation 语义。

## Acceptance

- 现有 cover、machine part、pipe、energy 和 AE2 相关行为测试通过，且增加代表性 transaction commit/rollback 测试。
- item/fluid/energy 在空、满、部分容量、模拟和执行场景下结果与 1.20.1 领域行为一致。
- 外部 NeoForge capability 查询可发现 GTM 支持的能力；不存在旧 Forge capability class 的 runtime linkage。
- dedicated server 和 GameTest server 均不触发 client-only capability/provider 加载。
