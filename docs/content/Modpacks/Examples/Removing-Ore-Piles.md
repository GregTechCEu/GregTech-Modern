---
title: "Removing Surface Ore Indicators"
---


# Removing Surface Ore Indicators

## Removal Script

```js title="remove_piles.js"
GTCEuServerEvents.oreVeins(event => {
    event.modifyAll((veinId, vein) => {
        vein.surfaceIndicatorGenerator(indicator => indicator
            .block(Block.getBlock("minecraft:air")) // (1)
            .placement("above")
            .density(0.4) // Unsure if this matters
            .radius(5) // Unsure if this matters
        )
    })
})
```

1. Replacing where a ore pile would be with an air block, essentially removing it.