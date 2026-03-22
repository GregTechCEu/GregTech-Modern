---
title: Bedrock Fluid Veins
---


# Bedrock Fluid Veins

Bedrock Fluid Veins are invisable veins that exist under the bedrock, to find Fluid Veins you must have at least a HV tier Prospector. A Fluid Drilling Rig must be used to obtain the fluids out of the vein.

## Creating a Bedrock Fluid Vein

```js title="fluid_veins.js"
// In server events
GTCEuServerEvents.fluidVeins(event => {

    event.add('gtceu:custom_bedrock_fluid_vein', vein => {
        vein.dimensions('minecraft:overworld') // (1)
        vein.fluid(() => Fluid.of('gtceu:custom_fluid').fluid) 
        vein.weight(600)
        vein.minimumYield(120)
        vein.maximumYield(720)
        vein.depletionAmount(2)
        vein.depletionChance(1)
        vein.depletedYield(50)
    });
});

```

1. Dimension where fluid vein will spawn.