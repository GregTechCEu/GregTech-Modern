# CONTEXT.md

GregTech Modern（GTM）1.20.1 Forge fork 向 MC 26.1.2 / NeoForge 26.x 移植工作的领域语言。只收术语，不收实现细节。

## Glossary

- **目标坐标**：MC 26.1.2（Mojang 新命名规则）+ NeoForge 26.1.2.x（取最新 stable，如 26.1.2.99 经证实存在；自称 .100 的需以 Maven 为准）。
- **移植 spec**：本轮 wayfinding 的 destination。一份 AFK agent 可直接执行的文档：版本坐标全钉死、API 差异清单、砍件清单与范围、模块任务 + 验收标准、同步操作流。止于规划，不含动手移植。
- **基线**：手头这份 fork，相对上游 `GregTechCEu/GregTech-Modern` 1.20.1 线**零改动**。移植起点，不是终点。
- **上游双线**：上游长期并行的两个版本线——`1.20.1`（默认分支，Forge）与 `1.21`（MC 1.21.1 + NeoForge 21.1.248）。上游没有 26.x 线。
- **rebase 跟踪**：与上游同步的策略。长期 rebase 上游、私货隔离、保持可合入上游的形状。在上游无 26.x 线的前提下，跟踪对象默认为 1.20.1 默认分支的修复（转译到 26.1），操作化细节由「同步操作流」ticket 锁定。
- **依赖政策（找全或砍）**：30+ 兼容/运行时依赖尽可能找到 26.x 对应版本；找不到的暂时砍掉，不等。
- **硬依赖**：缺了则移植在技术上无法成立的构建依赖（Registrate、MUI、MixinExtras、Configuration）。与兼容层不同：硬依赖若无官方版本，路线是**自己移植或找替代**，而不是砍，更不是无限期等。
- **Registrate 坐标**：目标依赖 `com.tterrag.registrate:Registrate:MC26.1-1.5.0`，从 `maven.gegy.dev/releases` 消费；26.2 专用的 `26.2-1.6.0` 不属于本目标。
- **MUI 移植**：完整移植 `ModularUI-Modern` 的 `1.21.1` 分支到本项目，保持独立模块边界，目标是让 GTM 继续使用完整 MUI API；不采用缩减为 GTM 实际引用最小集的策略。
- **JourneyMap pin**：JourneyMap 使用 CurseForge file ID `8768945` 的 NeoForge 26.1.2 构件；API 侧使用 26.1 snapshot 坐标并在构建中验证。
- **砍件**：按依赖政策被暂时砍掉的依赖（如 EMI、官方 Create、Embeddium、GameStages）。砍的是依赖坐标与其牵连代码，不是功能承诺；回补是 out of scope。
- **阻塞性依赖**：本轮决议为**无**——没有"缺了就停下等"的项，硬依赖走自移植路线。
- **翻译手册**：上游 1.20.1→1.21 分支的 Forge→NeoForge 迁移 diff，复用为 26.1 移植的参照实现。
