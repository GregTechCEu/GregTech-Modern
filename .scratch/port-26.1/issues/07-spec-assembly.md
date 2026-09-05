Type: task
Status: resolved
Blocked by: 01, 02, 03, 04, 05, 06

## Question

汇总 01–06 全部决议，写出 AFK agent 可直接执行的移植 spec（`.scratch/port-26.1/spec.md`）：模块任务 + 版本坐标 + 验收标准 + 砍件范围 + 同步操作流，人类 review 后关闭本轮。

## Notes

- 先读 map 全文与 01–06 每个 ticket 的决议（含 `## Findings` / resolution comment），决议冲突时以更新者为准并在 spec 顶部显式标注。
- Spec 粒度：每个模块任务可独立交给一个 agent session（含输入文件范围、目标坐标、完成标准）；版本坐标表直接可用；砍件范围精确到文件/接口。
- 完成后决议 comment + 关闭 ticket，map 的 Decisions so far 记一行（指回 spec.md）。

## Answer

已根据 01–06 的研究和用户确认生成 `.scratch/port-26.1/spec.md`，并将代码实现拆成独立、可并行但带阻塞关系的实施计划票。spec 的验收 seam 是现有 Gradle build/data/GameTest/clean runtime 生命周期；本轮只发布计划，不执行移植。
