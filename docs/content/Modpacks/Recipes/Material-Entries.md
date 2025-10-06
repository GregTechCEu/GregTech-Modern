---
title: "Material Entry"
---

# Material Entries

With the abundance of items in GregTech, there comes an abundance of recipes to craft those items into. However, sometimes you make too many of a certain item and want to recover your lost materials. Luckily you can do that with GT's built in recycling system.

Any recipe that crafts an item, whether it be through a crafting table or a GT machine, can be specified to generate an additional recipe for decomposing that output item in the Macerator, Arc Furnace, and Extractor.

## ItemMaterialInfo

Before 7.0, the way to specify the decomposition information (called the `ItemMaterialInfo`) of an item was to strictly append it like the following:

```java title="ItemMaterialInfo.java"
ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_KANTHAL.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Kanthal, M * 8), // double wire
                        new MaterialStack(GTMaterials.Aluminium, M * 2), // foil
                        new MaterialStack(GTMaterials.Copper, M)) // ingot
        ); // (1)

        VanillaRecipeHelper.addShapedRecipe(provider, true, // (2)
             "casing_bronze_bricks", GTBlocks.CASING_BRONZE_BRICKS.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft), 
             "PhP", "PBP", "PwP", 
             'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze), 
             'B', new ItemStack(Blocks.BRICKS));

```

1. `GTValues.M` denotes a single (1) mol amount of the material (usually 1 full dust's worth)
2. The boolean denotes whether to generate a decomposition recipe for this recipe 

In 7.0, a system was introduced to automatically detect the inputs of a recipe and use that information when generating a decomposition recipe for the resulting items.

You can tell recipes to generate recycling information using either `.addMaterialInfo()` for item inputs ONLY or `addMaterialInfo(true, true)` for item and fluid inputs. You can also remove existing ItemMaterialInfo from an output item using `.removePreviousMaterialInfo()`, which will tell GT to not generate recycling recipes for the item output in that recipe.

In KubeJS, adding decomposition info to a recipe would look as follows:

```js title="itemDecomp.js"

ServerEvents.recipes(event => {
    event.recipes.gtceu.assembler('mv_hatch')
    .itemInputs('17x gtceu:iron_plate')
    .itemOutputs('1x gtceu:mv_energy_output_hatch')
    .duration(20)
    .addMaterialInfo(true) // (1)
    .EUt(10)

    event.recipes.gtceu.assembler('bucket')
    .itemInputs('4x minecraft:gold_ingot')
    .itemOutputs('minecraft:bucket')
    .removePreviousMaterialInfo() // (2)
    .duration(20)
    .EUt(23)
})

```

1. Generates a recycling recipe turning an MV energy hatch into 17 iron dust.
!!! note inline end
    This will overwrite the original recycling recipe if it exists.

2. Buckets will no longer have recycling recipes

The ItemMaterialInfo system only takes into account the first item output a recipe has when appending material information to that item. However, it will automatically scale the decomposition rate based on the amount of the output stack.

```js title="Seven Dirt"
ServerEvents.recipes(event => {
    event.recipes.gtceu.assembler('mv_hatch')
    .itemInputs('21x gtceu:iron_plate')
    .itemOutputs('7x minecraft:dirt')
    .duration(20)
    .addMaterialInfo(true) // (1)
    .EUt(10)
})
```

1. Each dirt will turn into 3 iron dust when macerated

## Crafting Table Recipes with Decomposition information

```js title="Crafting Table"
ServerEvents.recipes(event => { 
    event.recipes.gtceu.shaped('4x kubejs:examplium', [
            " A ",
            "ABA",
            " A "
        ], {
            A: "gtceu:steel_ingot",
            B: "minecraft:nether_star"
        })
        .addMaterialInfo(true) // (1) 
})
```

1. Each examplium will turn into 1 steel dust and 1 small nether star dust when macerated

??? tip "Decomposition recipes with Java"
    You can still generate decomposition information for shapeless or shaped recipes with `VanillaRecipeHelper`, the argument was just renamed from `withUnificationData` to `setMaterialInfoData`
