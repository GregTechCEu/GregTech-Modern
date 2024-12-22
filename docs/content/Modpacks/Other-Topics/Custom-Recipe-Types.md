---
title: Custom Recipe Type
---


## Creating a Recipe Type

!!! important "Recipe Types MUST be registered before the machines or multiblocks"

```js title="test_recipe_type.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('test_recipe_type')
        .category('test')
        .recipeModifiers([GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK)]) // (1)
        .setEUIO('in')
        .setMaxIOSize(3, 3, 3, 3) // (2)
        .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT) // (3)
        .setSound(GTSoundEntries.COOLING)
})
```

1. If electric and/or multiblock machines can process your custom recipe type, ```.recipeModifiers()``` will allow you to fine-tune the behaviour of these machine when running recipes of your custom recipe type. ```GTRecipeModifiers.PARALLEL_HATCH``` will enable Multiblock Machines to parallelize recipes of your custom type via an optional Parallel Hatch, while ```GTRecipeModifiers.ELECTRIC_OVERCLOCK```will define how your recipes overclock in electric machines and multiblocks.
2. Max Item Inputs, Max Item Outputs, Max Fluid Inputs, Max Fluid Outputs
3. A list of available ```GuiTextures``` and ```FillDirection```s can be found in the GTCEu Modern Github, or in the .jar file.


```js title="test_kinetic_recipe_type.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('test_recipe_type')
        .category('test_kinetic')
        .setEUIO('in')
        .setMaxIOSize(3, 3, 3, 3) 
        .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
        .setSound(GTSoundEntries.COOLING)
        .setMaxTooltip(6) // (1)
})
```

1. Helps to adjust the JEI/REI/EMI layout when adding addition conditions to the recipe like RPM and SU.    
