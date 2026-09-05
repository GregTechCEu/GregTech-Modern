Type: task
Status: ready-for-agent
Blocked by: 10

## Question

迁移 GTM 的 recipe、ingredient、codec、datapack registry、loot、tags、models、data maps 和 datagen，使生成资源能被 MC 26.1.2 加载。

## Context

主 spec：[`spec.md`](../spec.md)。API 差异：[`05-api-gap-inventory.md`](05-api-gap-inventory.md)。当前数据/注册约定也见 [`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。

## Work

- 迁移 `GatherDataEvent`、client/server data providers、datapack built-in entries 和 data generation run。
- 将 NBT-sensitive item/fluid ingredients、recipe serializers 和 validation 迁移到 Data Component、Holder、codec 和 StreamCodec 语义。
- 将 ore vein、bedrock ore、bedrock fluid 等数据驱动注册迁移到目标 datapack registry，并移除旧同步包依赖。
- 迁移 loot entry/function/condition 的 MapCodec 注册、loot validation、tags 目录语义、data maps、model/item model 生成和资源命名。
- 保持 GTM recipe matching、材料数据、生成概率、loot 结果和自定义资源内容不变。

## Acceptance

- data generation 完成且生成资源在目标 dedicated server load 阶段无 codec、registry、tag、loot 或 data component 错误。
- 代表性材料、矿脉、机器 recipe、NBT/Data Component ingredient、loot table 和 custom registry round-trip 测试通过。
- 现有 recipe serializer/lookup/GameTest 回归测试在目标 toolchain 上通过。
- 生成资源不依赖被砍的第三方 integration。
