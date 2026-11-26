---
title: "Layers & Dimensions"
---


# Layers and Dimensions


## Creating a New World Gen Layer

To create ore veins in another dimension (or just at the location of certain blocks), you need to create a new worldgen layer.  
You may also need to add a custom stone type for your ores.

```js title="startup_scripts/world_gen_layers.js"
GTCEuStartupEvents.registry('gtceu:world_gen_layer', event => {
    event.create('my_custom_layer')
        .targets('#minecraft:stone_ore_replaceables', 'minecraft:endstone') // [*] (1)
        .dimensions('minecraft:overworld', 'minecraft:the_end') // [*]
})
```

1. Accepts tags, blocks and block states.  
   Also accepts a `RuleTest` or `RuleTestSupplier` in case you need a bit more flexibility.


Once the layer is created, you can refer to it by its name when creating or modifying an ore vein:

```js title="server_scripts/ores.js"
GTCEuServerEvents.oreVeins(event => {
    event.add("kubejs:custom_vein", vein => {
        vein.layer("my_custom_layer")
        // ...
    })
})
```
