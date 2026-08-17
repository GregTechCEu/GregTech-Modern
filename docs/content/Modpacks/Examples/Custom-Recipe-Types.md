---
title: Custom Recipe Type
---


## Creating a Recipe Type

m=== "Java"
    ```java title="AddonRecipeTypes.java"
    public static final GTRecipeType TEST_RECIPE_TYPE = GTRecipeTypes.register(AddonMod.id("test_recipe_type"))
        .setEUIO(IO.IN)
        .setMaxIOSize(3, 3, 3, 3) // (1)
        .UI(ui -> ui.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SOLIDIFIER_OVERLAY))
        .setSound(GTSoundEntries.COOLING)

    ```
=== "JavaScript"
    ```js title="test_recipe_type.js"
    GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
        event.create('test_recipe_type')
            .category('test')
            .setEUIO('in')
            .setMaxIOSize(3, 3, 3, 3) // (1)
            .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SOLIDIFIER_OVERLAY)
            .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
            .setSound(GTSoundEntries.COOLING)
    })
    ```

1. Max Item Inputs, Max Item Outputs, Max Fluid Inputs, Max Fluid Outputs

See [Recipe Type UI](../../Development/MUI2/Recipe-Type-UI.md) for information about customising your recipe type's UI.