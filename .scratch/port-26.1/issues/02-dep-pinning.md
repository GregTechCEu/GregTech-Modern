Type: research
Status: resolved
Blocked by: none

## Question

`dependencies.gradle` + `gradle/forge.versions.toml` 里的每一项，其 26.1.2 / NeoForge 26.x 坐标是什么（含 Maven 仓库 URL）？确认不了的标"砍"并移交「砍件清单与代码牵连面」。

## Notes

- **第一步先查上游 1.21 分支的 MUI 与 Configuration 版本**（Q9 决议），再定 26.x 路线。
- 第一遍普查结论（2026-09-05，供复核而非复用）：有——Registrate 26.1-1.5.0、JEI 29.35.0.94、Jade 26.1.10、Curios 15.0.0+26.1.2、AE2 26.1.11-beta、KubeJS 8.0.4 + Rhino + Architectury 20.0.12、FTB 全家桶 26.1.2.x、CC:T v26.1.2-1.120.0、Xaero Minimap 26.4.2 / WorldMap 1.45.0、Cloth Config v26.1.154、MixinExtras 0.5.5（NeoForge 自带）；无——EMI、官方 Create、Embeddium（含 Oculus 待查）、GameStages（替代品 Chapters 2.0 待评估）、MUI、Configuration。
- NeoForge 取 26.1.2.x 最新 stable（自称 .100 的以 Maven 为准）。
- 用户确认：目标保持 MC 26.1.2 / NeoForge 26.1.2.x；曾考虑从 26.1-1.5.0 tag 源码构建 Registrate，后续研究确认该 tag 已有可消费二进制，最终决议以 `maven.gegy.dev` 二进制为准。
- 只做 fact-finding；发现记入 `## Findings`，不关闭 ticket。

## Findings（2026-09-05 复核，Modrinth API + CurseForge + maven.neoforged.net 实测）

**上游 1.21 分支结论（Q9 第一步）：** `gradle/libs.versions.toml` 在 1.21 分支只剩 MC/NF/toolchain，依赖已搬到 `gradle/forge.versions.toml`：Configuration = `3.1.1-neoforge`（`dev.toma.configuration:configuration-1.21.1`），MUI = `3.3.1-SNAPSHOT`（`brachy.modularui:modularui-mc1.21.1`）。附带：上游已删 Oculus/REI/AE2WTLib/Cloth，GameStages 注释掉（留 1.20.3 占位坐标），渲染改 Sodium+Iris+Embeddium 路线。

| 依赖 | 26.x 版本 | Maven URL | 状态 |
|---|---|---|---|
| NeoForge | 26.1.2.99（stable，mvnrepository 2026-08-26；官方索引 stable 至少到 .95，实施时重解） | https://maven.neoforged.net/releases/ | ok |
| Registrate | 26.1-1.5.0 | https://maven.tterrag.com/ | ok（沿用普查，实施时重验） |
| Configuration | **4.1.2+26.1.2**（新坐标 `dev.toma.configuration:configuration-neoforge`；旧 `configuration-26.1` 写法已废弃） | https://repo.repsy.io/mvn/toma/public/（备 https://api.repsy.io/mvn/toma/public） | ok**普查误判纠正：26.1.2 版存在** |
| MUI | 无 26.x（CurseForge 仅到 1.20.1；无 mc26 artifact） | — | **cut**→砍件清单 |
| MixinExtras | 0.5.5（NeoForge 自带） | 随 NF | ok（沿用） |
| JEI | 29.35.0.94（Modrinth 2026-09-03，beta） | https://maven.blamejared.com/（镜像 https://modmaven.dev） | ok |
| REI | 26.1.819+neoforge（2026-06-18，beta；普查未提，上游虽删但 artifact 存在） | https://api.modrinth.com/maven（+ https://maven.shedaniel.me/） | ok |
| EMI（官方） | 止于 1.21.1；仅非官方 port（emi-unofficial-port-unstable，Beta，`curse.maven:emi-unofficial-port-unstable-1544558`） | — | **cut**→砍件清单（要官方就砍） |
| Jade | 26.1.10+neoforge（2026-08-15，release） | https://api.modrinth.com/maven | ok |
| Curios | 15.0.0+26.1.2 | https://maven.theillusivec4.top/ | ok（沿用，实施时重验） |
| AE2 | 26.1.11-beta（2026-08-25；AE2WTLib 走 cursemaven 沿用） | https://api.modrinth.com/maven | ok |
| Create/Ponder/Flywheel（官方） | 止于 Create 6.0.10/1.21.1（2026-04-21），无 26.x | https://maven.createmod.net（查无 26.x） | **cut**→砍件清单（牵连 Create compat + Ponder 联动） |
| KubeJS | 26.1.2-8.0.4+neoforge（2026-07-23，beta；依赖 Architectury 必需） | https://maven.latvian.dev/releases（Architectury https://maven.shedaniel.me/） | ok；Rhino/Arch 20.0.12 沿用待重验 |
| FTB 全家桶 | 26.1.2.x | https://maven.ftb.dev/releases | ok（沿用，实施时重验） |
| CC:T | v26.1.2-1.120.0（2026-06-15，alpha） | https://maven.squiddev.cc | ok |
| Cloth Config | 26.1.154+neoforge（release） | https://maven.shedaniel.me/ | ok |
| Xaero | Minimap 26.4.2 / WorldMap 1.45.0 | https://chocolateminecraft.com/maven/ | ok（沿用，实施时重验） |
| Embeddium/Oculus | Modrinth 无 26.1.2 版；上游 1.21 已弃 Oculus 转 Sodium+Iris | — | **cut**→砍件清单（备选 Sodium+Iris 直引路线待评估） |
| ModernFix | 5.27.22+mc26.1.2（2026-08-31，release） | https://api.modrinth.com/maven | ok |
| GameStages | 止于 1.20.x；替代品 Chapters 2.0（`curse.maven:chapters-1538047:8560538`，26.1.2，2026-08-02，KubeJS/FTB 联动已适配） | https://cursemaven.com | **cut**（GameStages 本体）→砍件清单；Chapters 待评估 |
| 纯 dev 运行时（Spark/Observable/JAVD/Trenzalore/JourneyMap/KFF/Bookshelf/RLib/Argonauts/Heracles/WorldStripper） | cursemaven fileId 沿用，实施时逐个重解 | https://cursemaven.com | carry |

砍件移交：EMI（官方）、Create 官方（含 Ponder/Flywheel 联动）、Embeddium/Oculus、GameStages 本体。MUI 已移交「MUI 与 Configuration 路线裁决」并改为完整移植；Configuration 从缺失名单移除。

## Findings

### Remaining verification

核验日期：2026-09-05。下表只记已从上游仓库、Maven、Modrinth 或 CurseForge 元数据核验的事实；`curse.maven` 坐标最后一段是 CurseForge **file ID**，不是版本号。

| 依赖 | 26.1.2 坐标/文件（仓库） | Loader / 状态 |
|---|---|---|
| Registrate | `com.tterrag.registrate:Registrate:MC26.1-1.5.0` (`https://maven.gegy.dev/releases/`) | NeoForge library / verified |
| ModularUI | 无 artifact；最近源码为 `1.21.1` branch | no 26.1.2 artifact / unresolved port; project port required |
| Configuration | `dev.toma.configuration:configuration-neoforge:4.1.2+26.1.2` (`https://repo.repsy.io/mvn/toma/public/`；backup `https://api.repsy.io/mvn/toma/public/`) | NeoForge / verified |
| AE2WTLib | Modrinth `ncEqrp7o`, `ae2wtlib-26.1.1-beta.jar` | NeoForge, MC 26.1.2 / verified; no target CurseMaven file confirmed |
| Rhino | `dev.latvian.mods:rhino:2101.2.8-build.91` (`https://maven.latvian.dev/releases`)；CF file `8463898` | NeoForge-compatible / verified |
| Architectury | `dev.architectury:architectury-neoforge:20.0.12` (`https://maven.shedaniel.me/`；Modrinth file `qb6YlbgG`) | NeoForge / verified |
| FTB Library | `dev.ftb.mods:ftb-library-neoforge:26.1.2.7` (`https://maven.ftb.dev/releases/`) | NeoForge / verified |
| FTB Teams | `dev.ftb.mods:ftb-teams-neoforge:26.1.2.4` | NeoForge / verified |
| FTB Quests | `dev.ftb.mods:ftb-quests-neoforge:26.1.2.7` | NeoForge / verified |
| FTB Chunks | `dev.ftb.mods:ftb-chunks-neoforge:26.1.2.8` | NeoForge / verified |
| KotlinForForge | `thedarkcolour:kotlinforforge-neoforge:6.3.0` (`https://thedarkcolour.github.io/KotlinForForge/`)，Modrinth file `yi2Fz7Hg` | NeoForge, `kotlinforforge-6.3.0-all.jar` / verified |
| Spark | `curse.maven:spark-361579:8275631` | NeoForge `spark-1.10.173-neoforge.jar` / verified |
| Observable | no 26.1.2 file; latest checked NeoForge is 1.21.1 file `6697124` | not-found/cut |
| JAVD | `curse.maven:javd-370890:7869081` | NeoForge `javd-neo-26.1.1.0+mc26.1.1.jar`, CF lists 26.1.2 / verified |
| Trenzalore | `curse.maven:trenzalore-870210:7957903` | NeoForge `trenzalore-neo-26.1.2.1+mc26.1.2.jar` / verified |
| JourneyMap API | `info.journeymap:journeymap-api-neoforge:2.0.0-26.1-SNAPSHOT` (`https://jm.gserv.me/repository/maven-public/`) | NeoForge snapshot / verified for 26.1 line; exact 26.1.2 release unresolved |
| JourneyMap Forge | `curse.maven:journeymap-32274:8768945` | NeoForge `journeymap-neoforge-26.1.2-6.0.7.jar` / verified |
| ResourcefulLib | `com.teamresourceful.resourcefullib:resourcefullib-neoforge-26.1:4.0.1` (`https://maven.teamresourceful.com/repository/maven-public/`)；CF file `7927296` | NeoForge / verified |
| Argonauts | no 26.1.2 file in the Argonauts/Odyssey Allies project | not-found/cut |
| Heracles | no 26.1.2 file in the Heracles/Odyssey Quests project | not-found/cut |
| WorldStripper | no 26.1.2 CurseForge file | not-found/cut |
| GameStages | no 26.1.2 official file | not-found/cut |
| Bookshelf | `curse.maven:bookshelf-228525:8551659`, `Bookshelf-neoforge-MC26.1.2-26.1.2.15.jar` | NeoForge / verified |

Registrate 的 `26.1-1.5.0` tag 精确指向 commit `d5a1d33c87325b41482dda6981f4543e79269e3e`。事实是：`maven.gegy.dev/releases` 已发布该版本的 JAR、sources JAR 和 POM；同路径的 `maven.tterrag.com` 核验为 404。上游 README 的形状是加入 `maven.gegy.dev/releases`，消费 `com.tterrag.registrate:Registrate:MC26.1-1.5.0`。研究性建议（不是本 ticket 的决策）：按该 tag 源码构建后执行 `publishToMavenLocal`，项目用 `mavenLocal()` 消费同一坐标；这比 vendor source 更符合上游的 `maven-publish` 设置。Composite build 可行但未见上游文档支持。

ModularUI 的 Modrinth 项目只列到 1.20.1，26.1.2 artifact 未找到；官方现代源码是 `https://github.com/brachy84/ModularUI-Modern` 的 `1.21.1` branch（commit `c13e141b830c70922c3540ea245ecf5d29e1029d`），不是 v3.3.1 tag（该 tag 指向 1.20.1）。该 branch 的构建前提是 Java 21、Gradle 8.14、NeoForge 21.1.215、ModDevGradle 2.0.86 和 Parchment 2024.11.17；其 `gradle.properties` 的 Maven group 是 `brachy.modularui`。

来源：Registrate `https://github.com/tterrag1098/Registrate`；Configuration `https://github.com/Toma1O6/Configuration`；AE2WTLib `https://github.com/Mari023/AE2WirelessTerminalLibrary`；Rhino `https://github.com/kube-mods/rhino`；Architectury `https://github.com/architectury/architectury-api`；FTB `https://github.com/FTBTeam/FTB-Library`、`https://github.com/FTBTeam/FTB-Teams`、`https://github.com/FTBTeam/FTB-Quests`、`https://github.com/FTBTeam/FTB-Chunks`；KFF `https://github.com/thedarkcolour/KotlinForForge`；Spark `https://github.com/lucko/spark`；Observable `https://github.com/tasgon/observable`；JAVD `https://github.com/UnRealModding/JAVD`；ResourcefulLib `https://github.com/Team-Resourceful/ResourcefulLib`；Bookshelf `https://github.com/Darkhax-Minecraft/Bookshelf`。JourneyMap source is unavailable; use `https://github.com/TeamJM/journeymap` issue tracker. Trenzalore `https://www.curseforge.com/minecraft/mc-mods/trenzalore`、WorldStripper `https://www.curseforge.com/minecraft/mc-mods/world-stripper`、Argonauts `https://www.curseforge.com/minecraft/mc-mods/odyssey-allies`、Heracles `https://www.curseforge.com/minecraft/mc-mods/odyssey-quests` have only CurseForge project pages in the checked metadata; no public source URL was surfaced.

当前 `forge.versions.toml` 的旧值仍是**未经核验的 carry-over**，不是目标 pin：Spark `4738952`、Observable `5643037`、JAVD `4803995`、Trenzalore `4848244`、JourneyMap API `1.20-1.9-SNAPSHOT`、JourneyMap `5789363`、ResourcefulLib `5659871`、Argonauts `5263580`、Heracles `5406935`、AE2WTLib `5217955`、GameStages `15.0.2`、Bookshelf `20.2.13`。尤其 `5263580`、`5406935`、`5217955` 等是旧 Minecraft/loader 文件，不能据其推导 26.1.2 支持。

## Answer

依赖策略已经决议：目标保持 MC 26.1.2 + NeoForge 26.1.2.x；可核验的 26.1.2 版本进入 pin 表，不可核验的依赖暂时砍掉。Registrate 使用 `com.tterrag.registrate:Registrate:MC26.1-1.5.0`（`https://maven.gegy.dev/releases/`），不使用 26.2 专用的 `26.2-1.6.0`。Configuration 使用 `dev.toma.configuration:configuration-neoforge:4.1.2+26.1.2`。JourneyMap 保留，使用 CurseForge file `8768945`（`journeymap-neoforge-26.1.2-6.0.7.jar`）；对应 API 仍按 26.1 snapshot 处理并在构建阶段验证。

MUI 没有 26.1.2 构件，转入「MUI 与 Configuration 路线裁决」：完整移植 `ModularUI-Modern` 的 `1.21.1` 分支到本项目，保留独立模块边界；这不是本票的实现交付。
