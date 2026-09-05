Type: task
Status: ready-for-agent
Blocked by: 10

## Question

将 GTM 的 SimpleChannel/FriendlyByteBuf 网络协议迁移为 NeoForge 26.1.2 CustomPacketPayload、Type、StreamCodec 和 payload handler，并恢复所有机器、GUI、cover、recipe 和同步消息。

## Context

主 spec：[`spec.md`](../spec.md)。网络差异：[`05-api-gap-inventory.md`](05-api-gap-inventory.md) 与 [`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。

## Work

- 为每个消息族定义 payload type、StreamCodec、方向、handler thread、可选/必需协商和错误处理。
- 将 payload registration 接入 `RegisterPayloadHandlersEvent` 及 26.1.2 client-side handler 生命周期。
- 迁移服务器到客户端、客户端到服务器和双向消息，覆盖 machine UI sync、cover sync、recipe sync、research、tooltips 和 integration messages。
- 处理目标 payload 大小限制、连接生命周期、无效数据和旧世界/旧客户端拒绝策略。
- 清理 SimpleChannel、NetworkDirection、旧 packet distributor 和旧 Forge network import。

## Acceptance

- 每个消息族至少有 codec round-trip test；代表性 GUI/machine sync 在 GameTest 或集成运行中恢复。
- clean client/server 连接和打开 GTM UI 不因 payload registration 或 handler thread 错误崩溃。
- 无旧 SimpleChannel/FriendlyByteBuf packet registration 残留作为 GTM 网络入口。
- 无效、截断和错误方向 payload 被拒绝而不让 server/client 崩溃。
