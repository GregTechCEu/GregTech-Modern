---
title: Recipe Conditions
---

Recipe Conditions are recipe properties that can prevent a recipe from starting based on certain criteria, like for example Biome, Weather, Quest Completions, or self-made custom Conditions.

These conditions can be used in both java and kubejs recipes. However, custom conditons can only be done in java. If you want to see how to make these, check out the [Custom Recipe Condition](../Examples/Custom-Recipe-Condition.md) example page.              

!!! Note
    The condition is run after recipe matching and before recipe execution. If the recipe condition doesn't match, the machine will be suspended and won't be updated again until something in the inputs/outputs changes.

### Base Conditons 

- Biome: `.biome("namespace:biome_id")`
    - Locks a recipe behind being inside a certain biome, works with any biome a pack has loaded. For example, you could use `minecraft:plains`.
- Dimension: `.dimension("namespace:dimension_id")`
    - Locks a recipe being behind a certain dimension, the gas collector is a good example of this. For example, you could use `minecraft:the_end`
- Position_Y: `.posY(int min, int max)`
    -  Locks a recipe behind a certain y level in-world. For example, you could use `.posY(120, 130)` to have a recipe require a machine to be in between y 120 and y 130.
- Rain: `.rain(float level)`
    - Locks a recipe behind a certain level of rain. For example, you could use `.rain(1.0)` to make a recipe need full rain. 
- Adjacent_Fluids: `adjacentFluids("minecraft:water","minecraft:lava")`
    - You can pass through any amount of fluids into the array. Moreover, any fluid passed into the array will make the recipe require a full source block touching the machine. We also have `adjacentFluidTag("forge:water", "forge:lava")`.
- Adjacent_Blocks: `adjacentBlocks("minecraft:stone", "minecraft:iron_block")`
    - Much like the fluid condition, you can pass blocks into the array that lock the recipe behind needing the machine to touch these blocks. We also have `adjacentBlockTag("forge:stone", "forge:storage_blocks/iron")`.
- Thunder: `.thunder(float level)`
    - Locks a recipe behind a certain level of rain. For example, you could use `.thunder(1.0)` to make a recipe need a strong thunderstorm.
- Vent: This condition is auto added to any steam single block, it blocks recipes from running if the vent is obstructed.
- Cleanroom: `.cleanroom(CleanroomType.CLEANROOM)`
    - Locks a recipe to being inside a cleanroom. You can also use STERILE_CLEANROOM as well as your own custom cleanroom type.
- Fusion_Start_EU: `.fusionStartEU(long eu)`
    - Locks a recipe behind the amount of stored power in a fusion machine. To use this, the machine must use the FusionReactorMachine class. For example, you could use `.fusionStartEU(600000)`
- Station_Research: `.stationResearch(b => b.researchStack("namespace:item_id").EUt(long eu).CWUt(int minCWUPerTick, int TotalCWU))`
    - Locks a recipe behind having a certain research stack. For this condition to be properly seen, you will either need a base machine recipe type with the research ui component, or make your own. For example, you could do `.stationResearch(b => b.researchStack("gtceu:lv_motor").EUt(131000).CWUt(24, 12000))` which would lock a recipe behind needing a data orb with the lv motor research. It will also generate you a research station recipe.
- Scanner_Research: `.scannerResearch(b => b.researchStack("namespace:item_id").EUt(long eu))`
    - Much like station research, this condition locks a recipe behind needing a research stack. However, in this case it will default to a data stick. For example, you could do `.scannerResearch(b => b.researchStack("gtceu:lv_motor").EUt(8192))`, which would make the recipe need a data stick with the lv motor research, and generates a scanner recipe.
- Enviromental_Hazard: `.environmentalHazard(GTMedicalConditions.CARBON_MONOXIDE_POISONING)`
    - Locks a recipe into needing a certain environmental hazard to run. For now, carbon monoxide is the only one. An example of a machine using this condition is the air scrubber.
- Daytime: `.daytime(boolean notNight)`
    - Locks recipe behind whether it is day or night. For example, you could do `.daytime(true)`, to make the recipe need it to be daytime.

### Mod Dependent Conditions
- Ftb_Quests: `.ftbQuest(quest_id)`
    - Locks a recipe behind the owner of a machine completing a ftb quest. An example can't be easily given since every quest book is different.
- Gamestage: `.gameStage(gameStage_id)`
    - Locks a recipe behind a certain game stage.  
- Heracles_Quests: `.heraclesQuest(quest_id)`
    - Locks a recipe behind the owner of a machine completing a heracles quest. An example can't be easily given since every quest book is different.

