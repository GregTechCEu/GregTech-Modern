---
icon: "material/link-box-variant"
title: "External Resources"
---


# :material-link-box-variant: External Resources

This page contains links to external documentation that may be useful for several topics regarding development, as well
as other resources you might find helpful.


## General Modding Docs

!!! link "Official NeoForge Docs"
    - [NeoForge-Wiki](https://docs.neoforged.net/)

    The local snapshot under `docs/neoforged` should be the first stop for 26.1/26.1.2 porting work.

!!! link "Other Modding Docs"
    - [Forge Community-Wiki](https://forge.gemwire.uk/wiki/Main_Page)
    - [MCJty's Modding Wiki](https://www.mcjty.eu/docs/intro)


## LDLib

GTCEu's 26.1.2 port is moving old LDLib-based UI and rendering code onto LDLib2. The current branch uses a tracked
patched LDLib2 artifact, `libs/ldlib2/ldlib2-neoforge-26.1.2-gtceu.1.jar`, plus bundled
`com.lowdragmc.lowdraglib` compatibility shims so existing UI code can keep working while the direct LDLib2 migration
continues.

For the current repository state, known gaps, and verification status, start with
[LDLib2 Migration State](Codebase-Guide/LDLib2-Migration-State.md).

!!! link "LDLib Docs"
    [:material-github: LDLib-Architectury :material-arrow-right: Wiki](https://github.com/Low-Drag-MC/LDLib-Architectury/wiki)


## Mixins

!!! link "Overview on using Mixins"
    [Fabric-Wiki :material-arrow-right: Mixins](https://fabricmc.net/wiki/tutorial:mixin_introduction)
    
    This is a great resource for getting started on how to use mixins, as well as a good quick reference if you're
    looking for how to do something specific.
        
    Note that this is not exclusive to Fabric, but applies for all platforms instead.

!!! link "Official Docs"
    [:material-github: Mixin :material-arrow-right: Wiki](https://github.com/SpongePowered/Mixin/wiki)

    A more detailed technical documentation on mixins.


## DFU (DataFixerUpper)

!!! link "Unofficial Documentation"
    [:material-github: Documented DataFixerUpper](https://github.com/kvverti/Documented-DataFixerUpper)

    Unofficial documentation for Mojang's DataFixerUpper library.


## Registrate

!!! link "Using Registrate"
    [:material-github: Registrate](https://github.com/tterrag1098/Registrate)
