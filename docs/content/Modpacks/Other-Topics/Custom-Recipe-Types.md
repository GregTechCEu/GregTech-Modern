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
        .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT) // (2)
        .setSound(GTSoundEntries.COOLING)
})
```

1. Max Item Inputs, Max Item Outputs, Max Fluid Inputs, Max Fluid Outputs
2. A list of available ```GuiTextures``` and ```FillDirection```s can be found in the GTCEu Modern Github, or in the .jar file.
   
