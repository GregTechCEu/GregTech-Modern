Type: task
Status: ready-for-agent
Blocked by: 08, 16

## Question

固化上游同步、CI 和 26.1.2 发布元数据，使移植分支可长期 rebase 跟踪并产出不会误标为 1.20.1 Forge 的构建物。

## Context

主 spec：[`spec.md`](../spec.md)。同步决议：[`06-sync-workflow.md`](06-sync-workflow.md)。仓库 AI policy：[`AI_POLICY.md`](../../../AI_POLICY.md)。

## Work

- 将 upstream `1.20.1` 的同步流程、commit group 翻译规则、基线 SHA 记录和 MUI/GT 分离规则写入贡献/维护文档。
- 更新 CI matrix、Java setup、Gradle cache、build/test/data tasks、artifact naming、mod metadata 和 release checks。
- 确认 CI 不会因为旧版本目录、旧 loader 名称或旧 file ID 误构建出 1.20.1 artifact。
- 生成 26.1.2/NeoForge 发行候选 artifact，并记录依赖、客户端/服务端启动和测试结果；实际上传发布平台不属于本票必须动作。
- 确认后续向上游提案时遵守 AI disclosure 和 human review 要求。

## Acceptance

- CI 使用与本地相同的 build/data/GameTest 验收入口，并在失败时保留有用日志。
- 产物、metadata、filename 和 release notes 明确标注 MC 26.1.2 / NeoForge。
- 维护者可以按照文档从 upstream `1.20.1` 更新、翻译 commit group、运行验证并保留可审阅历史。
- 最终 PR 的 changed files、依赖坐标和验证结果可与本 spec 和计划票逐项对应。
