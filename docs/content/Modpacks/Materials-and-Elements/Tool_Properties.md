---
title: Tool Creation
---

You can make tools out of materials you create by calling toolStats inside your material's code. 
```js
.toolStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types)
```

An example of this being actually used is included below
```js title="example_tool_material.js"
GTCEuStartupEvents.registry('gtceu:material', event => {
    event.create('aluminfrost')
        .ingot()
        .color(0xadd8e6)
        .secondaryColor(0xc0c0c0).iconSet(GTMaterialIconSet.DULL)
        .toolStats(new ToolProperty(12, 7, 3072, 6,
            [
                GTToolType.DRILL_LV,
                GTToolType.MINING_HAMMER
            ]
        ))
```
