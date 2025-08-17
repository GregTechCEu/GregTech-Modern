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

`temperature`, `level`, `energyDiscount`, and `tier` all must be integers
* `temperature`: Used by Electric Blast Furnace recipes.
* `level`: Used to determine Multi-Smelter Parallels, at 32*level.
* `energyDiscount`: Used to determine Multi-Smelter power usage. EU/t = (4 * Parallels) / (8 * Discount), before overclocks.
*Must be greater than 0.*
* `tier`: Used for Speed Bonus in the Pyrolyze Oven, and Energy Discount in the Cracking Unit. +50% Speed, 
-10% Energy per tier. *(Tiers above 10 will not cause the Cracker to consume negative energy.)*

## Standard Coils

For reference, the standard GregTech Modern coils use the following stats:
```js
Coil, Temperature, Level, Discount
CUPRONICKEL, 1800, 1, 1
KANTHAL, 2700, 2, 1
NICHROME, 3600, 2, 2
RTM_ALLOY, 4500, 4, 2
HSS_G, 5400, 4, 4
NAQUADAH, 7200, 8, 4
TRINIUM, 9001, 8, 8
TRITANIUM, 10800, 16, 8
```
This creates a pattern of each coil tier adding either 900 or 1800 temperature, and doubling either the Level or the 
Energy Discount. This is significant because
* For the Electric Blast Furnace, each coil tier unlocks new recipes, and adds either 
  * A 5% EU/t discount *(multiplicative)* to lower recipes *(from +900 temp)*
  * A Perfect Overclock to lower recipes *(from +1800 temp)*
* For the Multi-Smelter, there are effectively two stats: work-per-time, and work-per-EU.
  * For tiers that double the Level, the Multi-Smelter gains 2x the work-per-time, at the same work-per-EU
  * For tiers that double the Energy Discount, the Multi-Smelter gains 2x the work-per-EU, at the same work-per-time