Type: task
Status: ready-for-agent
Blocked by: 08, 10, 11

## Question

接入已核验的 26.1.2 third-party integration，并彻底移除没有目标构件的 optional integration，确保核心 mod 在依赖缺席时仍能运行。

## Context

主 spec：[`spec.md`](../spec.md)。依赖研究：[02-dep-pinning.md](02-dep-pinning.md)。砍件范围：[04-cut-list-scope.md](04-cut-list-scope.md)。

## Work

- pin 并接入 JEI、REI、Jade、Curios、AE2、AE2WTLib、KubeJS/Rhino/Architectury、FTB、CC:T、Xaero、JourneyMap、KotlinForForge、ResourcefulLib、Bookshelf、ModernFix、Spark、JAVD 和 Trenzalore。
- 对 JourneyMap 使用 file `8768945`，并验证 API snapshot 与 runtime 构件的兼容性。
- 移除官方 EMI、Create/Ponder/Flywheel、Embeddium/Oculus、GameStages、Observable、Argonauts、Heracles 和 WorldStripper 的 dependency、metadata、run config、mixin 和 GTM integration。
- 保持可选依赖检测隔离；optional integration 初始化失败不能阻断 core registry、server 或 data generation。
- 保留 JEI/REI 双 recipe-viewer 设计，但将 runtime 选择限制在可解析的 26.1.2 artifacts。

## Acceptance

- 目标依赖可解析到 26.1.2/NeoForge 坐标；旧 1.20.1 file ID 不会被当作目标版本使用。
- 每个保留 integration 在存在时完成初始化或至少通过 compile/runtime smoke；不存在时 core GTM 仍能启动。
- 被砍 integration 的源、metadata、mixin 和 dependency 不再参与编译或 runtime discovery。
- JourneyMap、AE2、JEI/REI、Jade、KJS、FTB 和 CC:T 的代表性 integration path 有验证记录。
