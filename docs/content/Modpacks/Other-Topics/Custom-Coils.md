---
title: Custom Coils
---


## Coil Creation

Certain multiblock machines such as the Electric Blast Furnace, Alloy Blast Smelter, Multi-Smelter, Pyrolyze Oven, and 
Cracker use Heating Coils as part of their structure. The following code is used to define a custom Heating Coil block:

```js
StartupEvents.registry('block', event => {
    event.create('infinity_coil_block', 'gtceu:coil')
        .temperature(100)
        .level(0)
        .energyDiscount(1) // (1)
        .tier(10)
        .coilMaterial(() => GTMaterials.get('infinity'))
        .texture('kubejs:block/example_block')
        .hardness(5)
        .requiresTool(true)
        .material('metal')
})
```

`temperature`, `level`, `energyDiscount`, and `tier` all must be integers.
* `temperature`: Used by Electric Blast Furnace recipes.
* `level`: Used to determine Multi-Smelter Parallels, at 32*level.
* `energyDiscount`: Used to determine Multi-Smelter power usage. EU/t = (4 * Parallels) / (8 * Discount), before overclocks.
* `tier`: Used for Speed Bonus in the Pyrolyze Oven, and Energy Discount in the Cracking Unit. +50% Speed, 
-10% Energy per tier. (Tiers above 10 will not cause the Cracker to consume negative energy.)