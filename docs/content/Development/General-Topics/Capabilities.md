---
title: Capabilities
---


# How to access Capabilities of blocks and items

Fabric doesn't have a capability system like Forge does, but you can use several utility methods instead:

```java
FluidTransferHelper.getFluidTransfer(...);
ItemTransferHelper.getItemTransfer(...);
GTCapabilityHelper.getRecipeLogic(...)
GTCapabilityHelper.getControllable(...)
GTCapabilityHelper.getCoverable(...)
GTCapabilityHelper.getToolable(...)
GTCapabilityHelper.getWorkable(...)
GTCapabilityHelper.getElectricItem(...)
GTCapabilityHelper.getEnergyContainer(...)
```
