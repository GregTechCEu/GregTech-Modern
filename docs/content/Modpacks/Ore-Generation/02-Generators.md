---
title: "Generators"
---


# Vein & Indicator Generators

## Vein Generators


### Layered Vein Generator

```js
vein.layeredVeinGenerator(generator => generator
    .buildLayerPattern(pattern => pattern
        .layer(l => l.weight(3).mat(GTMaterials.Silver).size(2, 4))
        .layer(l => l.weight(2).mat(GTMaterials.Gold).size(1, 1))
        .layer(l => l.weight(1).block(() => Block.getBlock('minecraft:oak_log')).size(1, 1))
        .layer(l => l.weight(1).state(() => Block.getBlock('minecraft:oak_planks').defaultBlockState()).size(1, 1))
    )
)
```


### Veined Vein Generator

```js
vein.veinedVeinGenerator(generator => generator
    .oreBlock(GTMaterials.Silver, 4) // (1)
    .rareBlock(GTMaterials.Gold, 1) // (2)
    .rareBlockChance(0.25)
    .veininessThreshold(0.1)
    .maxRichnessThreshold(0.3)
    .minRichness(0.3)
    .maxRichness(0.5)
    .edgeRoundoffBegin(10) // (3)
    .maxEdgeRoundoff(0.2) // (4)
)
```

1. **Param 1:** Either a material or block state  
   **Param 2:** Generation weight
2. **Param 1:** Either a material or block state  
   **Param 2:** Generation weight
3. Determines how much the veins become thinner towards their ends
4. Determines how much the veins become thinner towards their ends


!!! info "Noise Parameters"
    The vein's noise parameters can be summarized as follows:
    - `veininessThreshold` defines how "sharp" the edges of the vein are.  
      Higher values result in more "blurry" edges.
    - `maxRichnessThreshold` defines how many ores generate _inside_ the vein (must be `>= veininessThreshold`).  
      A higher distance between the values results in less "filled" veins.
    - `minRichness` and `maxRichness` allow you to limit the output of this calculation to a specific range.

    The output of this calculation determines the chance for each block in the vein to generate.


!!! info "Height Ranges"
    The height range of the generator is automatically inferred if you use `heightRangeUniform()` or `heightRangeTriangle()` in the vein definition, _before setting the generator_. Otherwise you need to set the height range manually:

    ```js
    generator.minYLevel(10)
    generator.maxYLevel(90)
    ```


### Dike Vein Generator

```js
vein.dikeVeinGenerator(generator => generator
    .withBlock(GTMaterials.Silver, 3, 20, 60) // (1)
    .withBlock(GTMaterials.Gold, 1, 20, 40)
)
```

1. **Param 1:** Either a material or block state  
   **Param 2:** Generation weight  
   **Param 3:** Min Y Position  
   **Param 4:** Max Y Position  

!!! info "Height Ranges"
    The height range of the generator is automatically inferred if you use `heightRangeUniform()` or `heightRangeTriangle()` in the vein definition, _before setting the generator_. Otherwise you need to set the height range manually:

    ```js
    generator.minYLevel(10)
    generator.maxYLevel(90)
    ```


### Standard Vein Generator

!!! failure "Not yet documented"

```js
vein.standardVeinGenerator(generator => /* ... */)
```


### Geode Vein Generator

!!! failure "Not yet documented"

```js
vein.geodeVeinGenerator(generator => /* ... */)
```


## Indicator Generators


### Surface Rock Indicators

```js
vein.surfaceIndicatorGenerator(indicator => indicator
    .surfaceRock(GTMaterials.Platinum) // [*] (1)
    .placement("above") // (2)
    .density(0.4)
    .radius(5)
)
```

1. Instead of a surface rock, you can also define any other block:  
    ```js
    // Using a block:
    indicator.block(Block.getBlock('minecraft:oak_log'))

    // Using a block state:
    indicator.state(Block.getBlock('minecraft:oak_log').defaultBlockState())
    ```
2. Valid options:  
   `surface` generates indicators on the world's surface  
   `above` generates indicators in the next free space above  
   `below` generates indicators in the next free space below  
   <br>
   **Default:** `surface`