Type: task
Status: ready-for-agent
Blocked by: none

## Question

建立 MC 26.1.2 + NeoForge 26.1.2.x 的可重复构建基线，并为后续实施票提供同步源和 Gradle 验收入口。

## Context

主 spec：[`spec.md`](../spec.md)。依赖事实：[`02-dep-pinning.md`](02-dep-pinning.md)。同步决议：[`06-sync-workflow.md`](06-sync-workflow.md)。

## Work

- 初始化或接入 upstream remote，并记录上游 `1.20.1` 基线 commit SHA；建立独立的 26.1.2 port 分支。
- 将 Gradle、Java、ModDevGradle、NeoForge、Mojang mappings、版本目录和 repository 配置改为目标坐标。
- 更新 mod metadata、loader、Minecraft version、run configuration、game-test namespace、data-generation run 和 mixin/toolchain 配置。
- 接入 Registrate `MC26.1-1.5.0`、Configuration `4.1.2+26.1.2` 和已确认的 Maven repositories；禁止旧 1.20.1 Forge 坐标通过隐式 carry-over 解析。
- 让空改动或最小 bootstrap 在目标 toolchain 上完成依赖解析与基础编译，作为后续票的共同起点。

## Acceptance

- 目标 Java/Gradle/NeoForge 版本可被 Gradle 解析，且 build 不再以 1.20.1 Forge 为运行时目标。
- clean server、clean client、data generation 和 GameTest server 的运行配置均能生成。
- upstream baseline SHA、目标 NeoForge 版本和依赖解析结果有可审阅记录。
- 未完成 GTM API 迁移时允许源代码编译失败，但失败必须来自已知旧 API，而不是工具链或依赖坐标错误。
