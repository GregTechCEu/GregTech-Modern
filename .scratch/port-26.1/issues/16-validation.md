Type: task
Status: ready-for-agent
Blocked by: 09, 11, 12, 13, 14, 15

## Question

用现有 Gradle lifecycle 对完整移植做 build、数据生成、GameTest、dedicated server、client 和 optional dependency absence 验收，并补足行为回归测试。

## Context

主 spec：[`spec.md`](../spec.md)。测试决策基于当前 `src/test` 中的 recipe、machine、cover、AE2 和 GameTest prior art。

## Work

- 执行目标 toolchain 的 compile/package/build、data generation、GameTest server、clean server 和 clean client。
- 运行并修复现有 recipe serializer/lookup、overclock、machine part、cover、capability、AE2 pattern buffer 和 stress tests。
- 补充网络 codec、Data Component ingredient、datapack registry、loot/tag/data map、transfer transaction、MUI UI smoke 和 optional integration absence tests。
- 在没有 EMI/Create/Embeddium/Oculus/GameStages 等 cut mod 的 classpath 下运行 core validation。
- 记录每个失败是工具链、依赖坐标、机械 API、行为语义还是测试环境问题，并回指对应计划票。

## Acceptance

- build/package、data generation、GameTest server 和 clean server 成功。
- clean client 能启动并完成代表性 UI/render smoke；没有旧 Forge/removed integration linkage。
- 现有测试和新增核心行为测试通过，或有明确、已批准的 target-version 行为差异记录。
- 在缺失所有 cut integration 时，core GTM 仍可构建、启动和运行代表性 GameTest。
