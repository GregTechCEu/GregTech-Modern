---
title: "Custom Parallel Hatch"
---


# Custom Parallel Hatch Multi-Part (By Sparked)

## Parallel Hatch

```js title="extra_parallel_hatch.js"
const $ParallelHatchPartMachine = Java.loadClass(
    "com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine"
); // (1)

GTCEuStartupEvents.registry("gtceu:machine", (event) => {
  event
      .create("parallel_hatch", "custom") // (2)
      .tiers(GTValues.UHV, GTValues.UIV, GTValues.UEV, GTValues.UXV) // (3)
      .machine((holder, tier, tankScaling) => {
        return new $ParallelHatchPartMachine(holder, tier); // (4)
      })
      .definition((tier, builder) => {
        let name = "Simple";
        switch (tier) {
          case GTValues.UHV:
            name = "Epic";
            break;
          case GTValues.UIV:
            name = "Legendary";
            break;
          case GTValues.UEV:
            name = "Spectral";
            break;
          case GTValues.UXV:
            name = "Universal";
            break;
        }

        const $RecipeLogic = Java.loadClass(
            "com.gregtechceu.gtceu.api.machine.trait.RecipeLogic"
        );
        builder
            .langValue(name + " Parallel Control Hatch")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.PARALLEL_HATCH) // (5)
            .modelProperty($RecipeLogic.STATUS_PROPERTY, $RecipeLogic.Status.IDLE)
            .model(
                GTMachineModels.createWorkableTieredHullMachineModel(
                    GTCEu.id("block/machines/parallel_hatch_mk4") // (6)
                )[
                    "andThen(com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder$ModelInitializer)"
                    ]((ctx, prov, model) => {
                  model.addReplaceableTextures("bottom", "top", "side");
                })
            )
      }); // (7)
});
```

1. Loading the parallel hatch's java class is required
2. Using the GT registry event to register a tiered custom machine
3. The tiers to register the machine for
    Here, GT will generate items and blocks named `gtceu:uhv_parallel_hatch`, `gtceu:uev_parallel_hatch`, `gtceu:uiv_parallel_hatch`, and `gtceu:uxv_parallel_hatch`
4. Use the loaded class for creating the machines in the world
5. Specifying the multipart to use parallel hatch ability
6. The texture to use for the multipart, this example just uses the t4 texture as a placeholder
  You can look at gtm's assets to see the animations and textures to edit
7. You can just refer to the code in GCYMMachines.PARALLEL_HATCH here