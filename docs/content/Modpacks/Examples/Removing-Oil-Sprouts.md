---
title: "Configuring Oil Sprouts"
---

# Configuring Oil Sprouts

!!! warning "This feature is named raw_oil_**sprout**, not raw_oil_spout."

Oil Sprouts are generated via Minecraft's "Configured Feature" system, and can be customized via standard datapacks.
If you are using kubejs, placing files in the `kubejs/data` folder is equivalent to adding files to a datapack.

## Removing Oil Sprouts

To disable oil sprouts entirely, place the following file in `kubejs/data/gtceu/worldgen/configured_feature/raw_oil_sprout.json`,
or create a datapack containing the equivalent. This will replace the sprouts with a `no_op` - i.e. a feature that does nothing.

```json title="data/gtceu/worldgen/configured_feature/raw_oil_sprout.json"
{
"type": "minecraft:no_op",
"config": {}
}
```

## Adjusting Oil Sprout Placement Conditions

If you just want to adjust the rarity of oil sprouts, that is configured via the "Placed Feature" system. Copy the
[current version](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/generated/resources/data/gtceu/worldgen/placed_feature/raw_oil_sprout.json)
of the placed feature file to `kubejs/data/gtceu/worldgen/placed_feature/raw_oil_sprout.json`, and modify the settings
as desired. (The default file uses a `"minecraft:rarity_filter"` to give a 1/64 chance of each chunk containing a sprout.)