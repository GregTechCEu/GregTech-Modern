Type: grilling
Status: resolved
Blocked by: 02

## Question

基于「26.x 依赖版本钉死」的查证结果，MUI 与 Configuration 二者各走升级 / 自移植 / 替换 / 等中的哪条路线？

## Notes

- HITL：必须与人类 live 对话决议，agent 不许替人类作答。调用 grilling + domain-modeling 两个 skill。
- 输入：02 的 `## Findings`（尤其上游 1.21 分支用的版本）。
- 输出：决议 comment + 关闭 ticket，并在 map 的 Decisions so far 记一行 gist（指回本 ticket）。
- 若决议引入新术语（如替代库名），同步更新根 `CONTEXT.md`。

## Answer

- **MUI**：完整移植 `ModularUI-Modern` 的 `1.21.1` 分支到本项目的 26.1.2 / NeoForge 目标。保留 MUI 的独立模块边界与完整 API，不缩减为 GTM 当前引用的最小子集。移植产物暂放在本仓库内，后续 GTM 通过项目内模块依赖消费。
- **Configuration**：不自行重写，使用已核验的 `dev.toma.configuration:configuration-neoforge:4.1.2+26.1.2`，仓库为 `https://repo.repsy.io/mvn/toma/public/`（必要时使用 `https://api.repsy.io/mvn/toma/public/`）。
- **Registrate**：不并入本票的自移植范围，使用已核验的 `com.tterrag.registrate:Registrate:MC26.1-1.5.0`；26.2 专用的 `26.2-1.6.0` 不使用。
- **验收边界**：MUI 移植必须先能独立编译，再能被 GTM 编译 classpath 消费；Configuration 依赖必须能解析并通过 GTM 的配置初始化。具体实现任务进入最终移植 spec，不在本决策票执行。
