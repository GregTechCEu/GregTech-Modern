Type: task
Status: resolved
Blocked by: 02

## Question

被砍依赖各自牵连哪些代码？给出 spec 级的删除 / 桩化范围。

## Notes

- 候选砍件（以 02/03 结论为准）：EMI（runtime 二选一，已有 JEI 可留）、Create + Ponder 联动、Embeddium / Oculus（client runtime）、GameStages（→ Chapters 2.0 或删）、Observable / Argonauts / Heracles / WorldStripper 等无 26.1.2 构件的 dev-only runtime。MUI 不属于砍件，进入完整移植范围。
- 工作：在本仓库 grep 每个砍件的引用点并计数，按模块汇总牵连面；spec 写清删哪些文件、桩哪些接口。只产出决策与范围，不动手删代码。
- 完成后决议 comment + 关闭 ticket，map 记一行。

## Answer

本票按当前依赖研究结果闭合。以下依赖不进入 26.1.2 主线的运行时或构建依赖：官方 EMI、Create/Ponder/Flywheel、Embeddium/Oculus、GameStages、Observable、Argonauts、Heracles、WorldStripper。Chapters 不作为 GameStages 的自动替代品纳入本 spec。

- 删除对应的 Gradle 依赖、版本目录、运行配置和 mod metadata 声明。
- 删除或隔离对应的 GTM integration 模块：Create display/compat、Embeddium compat、EMI recipe-viewer integration、GameStages hooks，以及只服务于被砍 dev runtime 的启动配置。
- 不为被砍依赖保留空壳 API 或假实现；GTM 核心行为必须在没有这些 mod 时继续编译和运行。
- JEI、REI、Jade、AE2、AE2WTLib、KubeJS、FTB、CC:T、Xaero、JourneyMap、Curios 等已核验或明确保留的集成不在砍件范围。
- MUI 不是砍件，按「MUI 与 Configuration 路线裁决」完整移植。

本答案是基于已确认依赖政策的 spec 范围决议；实现阶段仍须用 26.1.2 解析结果复核每个可选构件的真实 file ID。
