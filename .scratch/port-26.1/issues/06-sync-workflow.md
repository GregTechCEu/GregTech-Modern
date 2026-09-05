Type: grilling
Status: resolved
Blocked by: none

## Question

上游无 26.x 线（1.20.1 默认 + 1.21 并行）的前提下，rebase 跟踪如何操作化？

## Notes

- HITL：必须与人类 live 对话决议，调用 grilling + domain-modeling 两个 skill。
- 待锁事项：git 基线布局（先 `git init` 锚定上游 1.20.1 某 tag 再开分支，锚点 tag 由本 ticket 定）、跟踪哪条线（默认 1.20.1 默认分支的修复如何转译到 26.1）、转译节拍（跟 release 跟 commit？）、私货隔离规则（当前零私货，规则先行）。
- 上游 AI_POLICY.md 的披露与人类把关义务写进操作流。
- 输出：决议 comment + 关闭 ticket，map 记一行；新术语同步 `CONTEXT.md`。

## Answer

同步方案按用户已确认的 rebase 跟踪策略固化如下：

- 将 `GregTechCEu/GregTech-Modern` 作为 `upstream` remote；以实现开始时上游 `1.20.1` 的明确 commit SHA 作为 26.1.2 移植基线，并把该 SHA 写入移植记录。
- 目标改动集中在独立的 26.1.2 / NeoForge 分支；上游 `1.20.1` 作为持续同步源，上游 `1.21` 仅作为 Forge→NeoForge 翻译手册，不直接作为目标基线。
- 同步按上游可审阅的 commit group 进行：先把上游 1.20.1 更新 rebase 到移植分支，再逐组将语义变化翻译到 26.1.2，随后执行完整构建、数据生成、GameTest 和干净服务端验证。不得把 1.20.1 commit 直接当作可编译 cherry-pick。
- MUI 作为本项目独立模块/依赖边界维护；上游 GTM 同步只处理 GTM 代码，避免把 MUI 移植改动混入上游同步提交。
- 当前基线零私货；以后新增功能必须进入独立提交组，注明是否需要向上游回移。上游贡献遵守仓库 `AI_POLICY.md`：披露 AI 工具和辅助范围，提交前由人类理解、审阅并编辑。

本票的结论是同步操作流，不要求本阶段创建 git remote 或提交；实际 git 初始化、分支和首个基线 SHA 属于实施计划的基础设施票。
