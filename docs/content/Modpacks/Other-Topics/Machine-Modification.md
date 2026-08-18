---
title: Machine Modification
---

# Machine Modification
If you want to modify an existing machine's definition, you can do so using the
`GTCEuStartupEvents.machineModification` event. It is fired right before a machine gets registered, so that
its builder is still accessible through the `event.getBuilder()` method. For examples of methods that machine builders
have, see [Custom Machines](Custom-Machines.md).
For a full list of methods, see [source](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/registry/registrate/MachineBuilder.java).