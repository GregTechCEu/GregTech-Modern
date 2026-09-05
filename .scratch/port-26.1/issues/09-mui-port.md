Type: task
Status: ready-for-agent
Blocked by: 08

## Question

将 ModularUI-Modern `1.21.1` 分支完整移植到 MC 26.1.2 / NeoForge，并以仓库内独立 Gradle 模块供 GTM 使用。

## Context

主 spec：[`spec.md`](../spec.md)。路线决议：[`03-mui-config-decision.md`](03-mui-config-decision.md)。上游版本参照：[`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。

## Work

- 将 `1.21.1` 分支作为完整源码基线，迁移其构建工具链、公共 API、widget、drawable、screen、value sync、menu、recipe-viewer 和 loader 集成。
- 将 MUI 26.1.2 目标作为仓库内独立模块，保持 `brachy.modularui` 的公开包/API 边界；GTM 不复制 MUI 内部实现。
- 迁移 MUI 自身的 NeoForge/MC API、渲染、网络和 datagen 触点；移除对 1.21.1 专用 API 的残留引用。
- 让 GTM 的 cover、machine screen、recipe viewer、widget 和同步代码消费该模块，不为当前 GTM 用量制作缩减版替代 API。

## Acceptance

- MUI 模块可以独立编译并产出可被 GTM 消费的 artifact/project dependency。
- GTM 当前所有 `brachy.modularui` 引用完成解析，代表性 machine、cover、recipe-viewer panel 可构造。
- MUI API 的 screen/widget/value-sync/recipe-viewer smoke tests 通过；不能依赖被砍的 EMI、Create、Embeddium 或 Oculus。
- clean client 能打开至少一个 GTM machine/cover UI，不发生 classloading、渲染或网络同步崩溃。
