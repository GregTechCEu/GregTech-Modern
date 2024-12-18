---
title: "Custom Parallel Hatch"
---


# Custom Parallel Hatch Multi-Part (By Sparked)

## Parallel Hatch

```js title="extra_parallel_hatch.js"

const $ParallelHatchPartMachine = Java.loadClass(
	'com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine'
) // (1)

GTCEuStartupEvents.registry('gtceu:machine', event => { // (2)
    event.create(
        "uhv_parallel_hatch", // (3)
        "custom",
        (holder, tier) => {
            return new $ParallelHatchPartMachine(holder, tier);
        },
        GTValues.UHV // (4)
    )
	.abilities(PartAbility.PARALLEL_HATCH) // (5)
	.workableTieredHullRenderer(GTCEu.id("block/machines/parallel_hatch_mk4")) // (6)
})
```

1. Loading the java class that is required to build the parallel hatch multi part
2. Using the GT registry event to register the multi part, which is part of machine registry
3. The ID for the new parallel hatch
4. The tier used for the parallel hatch
5. Specifying the multipart to use parallel hatch ability
6. The texture to use for the multipart, this example just uses the t4 texture as a placeholder
	You can look at gtm's assets to see the animations and textures to edit