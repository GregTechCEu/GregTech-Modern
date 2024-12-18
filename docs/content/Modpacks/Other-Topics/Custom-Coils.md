---
title: Custom Coils
---


## Coil Creation

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

1. The Energy Discount must be at least 1.
