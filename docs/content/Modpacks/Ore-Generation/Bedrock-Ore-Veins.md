---
title: Bedrock Ore Veins
---


# Bedrock Ore Veins

While not enabled by default, GTCEu Modern contains bedrock ore veins and bedrock ore miners.

To enable this feature, you need to enable the config option **Machines -> doBedrockOres** and restart your game.

!!! warning "No recipes by default"
    The various tiers of bedrock ore miners don't have any recipes by default. It is up to modpack developers to create
    crafting recipes for these machines.


## Adding Bedrock Veins

By default, the mod doesn't include any bedrock ore veins.

You can add them using the `bedrockOreVeins` server event:

```js
GTCEuServerEvents.bedrockOreVeins(event => {
    event.add('kubejs:overworld_bedrock_ore_vein_iron', vein => {
        vein.weight(100)
            .size(3) // (1)
            .yield(10, 20)
            .material(GTMaterials.Goethite, 5) // (2)
            .material(GTMaterials.Limonite, 2)
            .material(GTMaterials.Hematite, 2)
            .material(GTMaterials.Malachite, 1)
            .dimensions('minecraft:overworld')
    })
})
```

1. The diameter of the bedrock vein in chunks
2. The second parameter defines the chance of each material being mined on each cycle
