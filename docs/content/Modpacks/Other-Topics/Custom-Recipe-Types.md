---
title: Custom Recipe Type
---


## Creating a Recipe Type

!!! important "Recipe Types MUST be registered before the machines or multiblocks"

```js title="test_recipe_type.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('test_recipe_type')
        .category('test')
        .setEUIO('in')
        .setMaxIOSize(3, 3, 3, 3) // (1)
        .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SOLIDIFIER_OVERLAY) // (2)
        .setProgressBar(GTGuiTextures.PROGRESS_ARROW) // (3)
        .setSound(GTSoundEntries.COOLING)
})
```

1. Max Item Inputs, Max Item Outputs, Max Fluid Inputs, Max Fluid Outputs
2. Slot overlays are set per slot. `setItemSlotOverlay(io, index, overlay)` and `setFluidSlotOverlay(io, index, overlay)` set one slot; `setItemSlotsOverlay(io, startIndex, endIndex, overlay)` and `setFluidSlotsOverlay(io, startIndex, endIndex, overlay)` set an inclusive range, both ends included.
3. A list of available textures can be found in [GTGuiTextures.java](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/mui/GTGuiTextures.java). Progress bar textures already carry their size and fill direction.

## Recipe info lines

`addRecipeInfo` adds a line of text below recipes of this type in the recipe viewer. It is given the recipe being displayed and returns the component to show for it, built with `Text.literal` or `Text.translate`. Return `Text.empty()` to show nothing for that recipe. Call it several times for several lines.

```js title="test_recipe_type.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('test_recipe_type')
        .addRecipeInfo(recipe => Text.translate('test_recipe_type.info'))
        .addRecipeInfo(recipe => recipe.data.contains('RequiredTemp') ?
            Text.literal(`Temperature: ${recipe.data.getInt('RequiredTemp')}K`) : Text.empty())
})
```
   
