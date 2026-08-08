---
title: "Customizing Veins"
---


# Creating and Modifying Ore Veins

You can create your own custom ore veins using KJS.  
It is also possible to modify or even delete existing ones.


## Creating New Veins

```js title="server_scripts/custom_ore_vein.js"
GTCEuServerEvents.oreVeins(event => {
    event.add("kubejs:custom_vein", vein => {
        // Basic vein generation properties
        vein.weight(200) // [*] (1)
        vein.clusterSize(40) // [*] (2)
        vein.density(0.25) // [*] (3)
        vein.discardChanceOnAirExposure(0) // (4)

        // Define where the vein can generate
        vein.layer("deepslate") // [*] (5)
        vein.dimensions("minecraft:overworld") // (6)
        vein.biomes("#minecraft:is_overworld") // (7)

        // Define a height range:
        // You must choose EXACTLY ONE of these options! [*]
        vein.heightRangeUniform(-60, 20) // (8)
        vein.heightRangeTriangle(-60, 20) // (9)
        vein.heightRange(/* ... */) // (10)

        // Define the vein's generator:
        vein.generator(/* ... */) // [*] (11)

        // Add one or more type of surface indicator to the vein:
        vein.addIndicator(/* ... */) // (12)
    })
})
```

1. An ore vein's weight determines the chance of it being chosen over another vein type, to be generated at a possible vein location.  
   The higher the weight, the more frequently an ore vein type will be generated.
2. Cluster size determines the diameter of an ore vein.
3. The density determines how frequently ores occur inside the vein.
4. Determines the chance of an ore block being skipped when it is exposed to air. Must be between `0` and `1`.  
   **Default:** `0`
5. See [Layers & Dimensions](./04-Layers-and-Dimensions.md)
6. Limits vein generation to the supplied dimensions. Note that these vein's layer must be applicable for them.  
   **Default:** All dimensions of the vein's layer.  
   <br>
   _Accepts any number of parameters._
7. Determines the biome (or biome tag) the vein can generate in.  
   **Default:** If no biome is explicitly set, the vein will generate in any biome.  
   <br>
   _Accepts either a single biome tag (prefixed with `#`), or any number of individual biomes._
8. Uniformly distributed across the height range
9. Biased towards the center of the height range
10. You can also use Minecraft's `HeightRangePlacement` directly, instead of the above shorthand versions:  
    ```js
    vein.heightRange(
        height: {
            type: "uniform",
            min_inclusive: {
                absolute: -60
            },
            max_inclusive: {
                absolute: 20
            }
        })
    ```
11. See [Generators](./02-Generators.md#vein-generators) for a list of available generators.
12. See [Generators](./02-Generators.md#indicator-generators) for a list of available generators.


??? example "Creating a new biome tag for your ore vein"
    In case you want to limit your ore vein to multiple biomes that don't have a common tag yet, you can either specify all biomes manually, or you can create a biome tag:

    ```js title="server_scripts/biome_tags.js"
    ServerEvents.tags('biome', event => {
        event.add('kubejs:my_biome_tag', 'minecraft:forest')
        event.add('kubejs:my_biome_tag', 'minecraft:river')
    })
    ```

    You can then use your biome tag by simply calling `vein.biomes('#kubejs:my_biome_tag')` in your vein definition.


## Removing an Existing Ore Vein

```js title="server_scripts/remove_ore_vein.js"
GTCEuServerEvents.oreVeins(event => {
     event.remove("gtceu:magnetite_vein_ow") 
})
```


??? example "Removing all ore veins"
    If you want to remove **all** predefined ore veins (for example if you want to completely change ore generation
    in your modpack), you can use the following code:

    ```js
    GTCEuServerEvents.oreVeins(event => {
        event.removeAll()
    })
    ```

    You can also filter the veins you want to remove:

    ```js
    event.removeAll((id, vein) => id.path != "magnetite_vein_ow")
    ```


## Modifying Existing Veins

```js title="server_scripts/modify_ore_vein.js"
GTCEuServerEvents.oreVeins(event => {
    event.modify("gtceu:cassiterite_vein", vein => {
        vein.density(1.0)
    })
})
```

The API for vein modifications is the same as for creating new veins.


!!! warning "Moving veins to other dimensions"
    When moving one of the default veins to another dimension, keep in mind that you also have to change their biome(s) accordingly.


??? example "Modifying ALL existing veins"
    You can also modify all existing ore veins at once:

    ```js title="server_scripts/modify_all_veins.js"
    GTCEuServerEvents.oreVeins(event => {
        event.modifyAll((id, vein) => {
            console.log("Modifying vein: " + id)
            vein.density(1.0)
        })
    })
    ```
