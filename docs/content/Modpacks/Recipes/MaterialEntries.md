---
title: "Material Entry"
---

# Material Entries

With the abundance of items in Gregtech, there comes the abundance of recipes to craft those items into. However, sometimes  you make too many of a certain item and you want to recover your lost materials. Luckily you can with the recycling system built into GT.

Any recipe that crafts an item, whether it be through a crafting table or a gt machine, can be specified to generate an additional for decomposing that output item in the `Macerator`, `Arc Furnace`, and `Extractor` machines.

## ItemMaterialInfo

Before 1.7, the way to specify the decomposition information (called the `ItemMaterialInfo`) of an item was to strictly append it like the following:

```java title="ItemMaterialInfo.java"

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gregtechceu.gtceu.api.GTValues.M;

...

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

1. GTValues.M denotes a singular mol amount of the material(usually 1 full dust worth)
2. The boolean here denotes whether to generate a decomposition for this recipe

In 1.7, a system was introduced to automatically detect the inputs of a recipe and use that information when generating a decomp recipe for the resulting items.

You can tell recipes to generate recycling information using either `.addMaterialInfo(true)` for item inputs ONLY or `addMaterialInfo(true, true)` for item inputs and fluid inputs. You can also remove existing ItemMaterialInfo from a output item using `.removePreviousMaterialInfo()`, which will tell gt to not generate recycling recipes for the item output in that recipe

In KubeJS adding decomp info to a recipe would look as follows:

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

1. Generates recycling recipes turning 1 mv energy hatch into 17 iron dust, NOTE: will overwrite the original recycling recipe if it exists.
2. Buckets will no longer have recycling recipes

The ItemMaterialInfo system only takes into account the first item output a recipe has when appending material information to that item. However it will automatically scale the decomposition rate based on the amount of the output stack.

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

## Crafting Table Recipes with Decomp

NOTE: strictly for addons and java sided recipes, not kubejs

With the VanillaRecipeHelper you can still generate decomp information with shapeless or shaped recipes, the argument was just renamed from `withUnificationData` to `setMaterialInfoData`