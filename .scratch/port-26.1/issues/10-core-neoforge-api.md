Type: task
Status: ready-for-agent
Blocked by: 08

## Question

完成 GTM 公共初始化、Forge→NeoForge 基础 API、事件总线、注册表和 capability registration 迁移，使其他功能票有稳定的目标 API 基础。

## Context

主 spec：[`spec.md`](../spec.md)。迁移参照：[`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。差异清单：[`05-api-gap-inventory.md`](05-api-gap-inventory.md)。

## Work

- 迁移 mod entrypoint、mod bus/game bus、生命周期、side checks、server lifecycle、client event 和 event subscriber。
- 迁移 NeoForge package/import、registry keys、DeferredRegister、custom registry creation、registry freeze/sync 和 holder lookup。
- 迁移 GTM 自有 capability registration 与公共 capability helper 的 NeoForge 边界；保留 GTM 的领域接口，不把第三方 API 泄漏进所有业务代码。
- 更新基础 Minecraft/NeoForge 类型、ResourceLocation、level/random、holder 和 loader 变化，处理 26.1 mappings 的机械改名。
- 让未迁移模块以最小方式可编译或清晰地暴露后续模块的 API 错误，不改变 GTM 领域行为。

## Acceptance

- clean server 能完成 GTM mod construction、registry registration 和 common initialization，失败不得来自旧 Forge bus/registry class。
- 自有注册表、物品、方块、流体、block entity、menu、recipe type 和 capability key 能完成注册并可被 lookup。
- dedicated server 不加载 client-only 类；现有测试初始化入口能在目标 game-test server 中发现。
- 迁移记录列出无法机械替换的 API，并将它们转入 11–14 对应票，而不是留下隐式 TODO。
