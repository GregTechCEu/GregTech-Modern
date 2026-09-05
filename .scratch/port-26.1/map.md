## Destination

一份 AFK agent 可直接执行的《GTM 1.20.1 Forge → MC 26.1.2 / NeoForge 26.x 移植 spec》及其实施 task graph：版本坐标全钉死、API 差异清单、砍件清单与牵连范围、模块任务 + 验收标准、与上游双线并行的同步操作流。spec 已发布到 [`spec.md`](spec.md)；动手移植由 08–17 计划票执行。

## Notes

- Domain：MC mod 移植（GregTech Modern，`src/main` 下 1725 个 java 文件，单体 Forge 工程，gtceu 8.0.0）。
- 目标坐标：MC 26.1.2 + NeoForge 26.1.2.x 最新 stable（见 CONTEXT.md「目标坐标」）。
- 基线：当前 fork 相对上游 [GregTechCEu/GregTech-Modern](https://github.com/GregTechCEu/GregTech-Modern) 1.20.1 线零改动；上游无 26.x 线（仅 1.20.1 + 1.21 双线并行），故基线策略 = 从手头 1.20.1 移植，以上游 1.21 分支为 Forge→NeoForge 参照实现。
- 依赖政策：找全或砍；硬依赖可自移植；无阻塞性依赖（见 CONTEXT.md）。
- Spec 读者 = AFK agent：决议必须写到"可执行"粒度（坐标、范围、验收标准），不要只写方向。
- 各 session 按 ticket 的 Type 调用对应 skill（research / grilling + domain-modeling / prototype）；经过 `CONTEXT.md` 术语时用其词汇，凝固新术语就地更新 `CONTEXT.md`。
- 本仓库无 `.git`：研究阶段发现记进 ticket 的 `## Findings`；实施阶段由 08 票初始化/接入 upstream，并记录移植基线。
- 上游有 AI_POLICY.md：凡涉及向 GregTechCEu 上游贡献的内容，执行者须遵守其披露与人类把关义务。

## Decisions so far

- [26.x 依赖版本钉死](issues/02-dep-pinning.md)：目标保持 MC 26.1.2 / NeoForge 26.1.2.x；Registrate 直接消费 `MC26.1-1.5.0`，JourneyMap 保留 file `8768945`，MUI 进入完整移植路线。
- [上游 1.21 Forge→NeoForge 翻译手册](issues/01-upstream-121-playbook.md)：上游 1.21 只作迁移参照；无上游 26.x 分支，实际基线仍是当前零改动的 1.20.1。
- [MC 26.1.2 API 差异盘点](issues/05-api-gap-inventory.md)：网络、传输/Capability、客户端渲染是行为级重写；其余区域按机械迁移或局部行为变更拆分。
- [MUI 与 Configuration 路线裁决](issues/03-mui-config-decision.md)：完整移植 MUI `1.21.1` 分支到本项目；Configuration 直接使用 `4.1.2+26.1.2`，Registrate 使用已发布的 `MC26.1-1.5.0`。
- [砍件清单与代码牵连面](issues/04-cut-list-scope.md)：移除官方 EMI、Create/Ponder/Flywheel、Embeddium/Oculus、GameStages 及无 26.1.2 构件的 dev runtime；不保留空壳 API，MUI 不砍。
- [上游同步操作流](issues/06-sync-workflow.md)：以明确的上游 `1.20.1` SHA 为源，26.1 分支独立 rebase，按 commit group 翻译并验证；1.21 只作参考。
- [移植 spec 总装](issues/07-spec-assembly.md)：spec 已生成，实施计划票已拆分到 08–17。

## Implementation plan

实施阶段使用同一目录下的计划票；`Blocked by` 是实现前置，不是线性步骤。最先可取的是 08「移植分支与工具链基线」；08 完成后，09「完整 MUI 移植」与 10「核心 NeoForge API」可并行。

- [08 移植分支与工具链基线](issues/08-toolchain-baseline.md)：无阻塞；建立目标坐标、Java/Gradle/MDG、仓库、metadata、run config 和同步基线。
- [09 完整 MUI 26.1.2 移植](issues/09-mui-port.md)：阻塞于 08；从 ModularUI-Modern 1.21.1 分支完整迁移并让 GTM 消费。
- [10 核心 NeoForge API 与注册系统](issues/10-core-neoforge-api.md)：阻塞于 08；迁移入口、总线、注册表、能力注册、包名与公共初始化。
- [11 数据、配方与 datagen](issues/11-data-and-datagen.md)：阻塞于 10；迁移 Data Component、datapack registry、loot、tags、recipe codec 和生成链路。
- [12 传输与 Capability 语义](issues/12-transfer-capabilities.md)：阻塞于 10；迁移物品/流体/能量/自有 capability 边界及机器、管道、cover 行为。
- [13 网络与同步协议](issues/13-networking.md)：阻塞于 10；将 SimpleChannel 迁移为 payload/StreamCodec，并恢复 GUI、机器、配方同步。
- [14 客户端渲染、MUI UI 与 mixin](issues/14-client-rendering.md)：阻塞于 09, 10, 11, 13；迁移两阶段渲染、屏幕、BER、动态高亮、mixin 和 MUI 集成。
- [15 保留集成与砍件清理](issues/15-integrations-and-cuts.md)：阻塞于 08, 10, 11；接入已核验依赖，删除砍件及其牵连模块，保留无可选依赖时的核心行为。
- [16 测试、数据生成与运行时验收](issues/16-validation.md)：阻塞于 09, 11, 12, 13, 14, 15；执行全量 Gradle 验收并补行为测试。
- [17 上游同步与发布准备](issues/17-sync-and-release.md)：阻塞于 08, 16；固化 rebase 流程、CI、版本元数据和 26.1.2 发布产物检查。

## Not yet specified

- 上游 1.20.1 之外新功能的长期维护策略。
- 被砍依赖的功能回补（如下游世界需要）。
- 26.1.2 之后的 MC/NeoForge 版本升级。

## Out of scope

- 实际动手移植执行（本次 destination 止于 spec）。
- 上游 1.21 分支自身的演进（上游的事，我们只读它）。
- 被砍依赖的功能回补实现。
- 1.20.1 Forge 线的继续维护。
