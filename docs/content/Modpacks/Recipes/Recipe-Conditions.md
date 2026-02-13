---
title: Recipe Conditions
---

Recipe Conditions are recipe properties that can prevent a recipe from starting based on certain criteria, like for example Biome, Weather, Quest Completions, or self-made custom Conditions.

These conditions can be used in both Java and KubeJS recipes. However, custom conditions can only be done in Java addons. If you want to see how to make these, check out the [Custom Recipe Condition](../Examples/Custom-Recipe-Condition.md) example page.              

!!! Note
    The condition is run after recipe matching and before recipe execution. If the recipe condition doesn't match, the machine will be suspended and won't be updated again until something in the inputs/outputs changes.

### Base Conditons 

- Biome: `.biome("namespace:biome_id")`
    - Locks a recipe behind being inside a certain biome, works with any biome a pack has loaded.
      For example, you could do `.biome("minecraft:plains")`.
- Dimension: `.dimension("namespace:dimension_id")`
    - Locks a recipe being behind a certain dimension, the gas collector is a good example of this.  
    - For example, you could do `.dimension("minecraft:the_end")`
- Y Position: `.posY(int min, int max)`
    - Locks a recipe behind a certain y level in-world.
    - For example, you could use `.posY(120, 130)` to have a recipe require a machine to be in between y 120 and y 130.
- Rain: `.rain(float level)`
    - Locks a recipe behind a certain level of rain.
    - For example, you could use `.rain(1.0)` to make a recipe need full rain. 
- Adjacent Fluids: `.adjacentFluids("namespace:fluid_id", ...)`
    - You can pass any amount of fluids into the array. Moreover, any fluid passed into the array will make the recipe require a full source block touching the machine.
    - For example, you could use `.adjacentFluids("minecraft:water", "minecraft:lava")` to make a recipe require BOTH a water source and a lava source next to the machine.
    - We also have `.adjacentFluidTag("forge:water", "forge:lava")`, which does the same, but allows fluid _tags_ to be used.
- Adjacent Blocks: `.adjacentBlocks("namespace:block_id", ...)`
    - Much like the fluid condition, you can pass blocks into the array that lock the recipe behind needing the machine to touch these blocks.
    - For example, you could use `.adjacentBlocks("minecraft:stone", "minecraft:iron_block")` to make a recipe require a Stone block and a Block of Iron.
    - We also have `.adjacentBlockTag("forge:stone", "forge:storage_blocks/iron")`, which does the same, but allows block _tags_ to be used.
- Thunder: `.thunder(float level)`
    - Locks a recipe behind a certain level of rain.
    - For example, you could use `.thunder(1.0)` to make a recipe need a strong thunderstorm.
- Vent: This condition is automatically added to any recipes ran in a single block steam machine. It blocks recipes from running if the machine's vent is obstructed.
- Cleanroom: `.cleanroom(CleanroomType.CLEANROOM)`
    - Locks a recipe to being inside a cleanroom. You can also use `STERILE_CLEANROOM` as well as your own custom cleanroom type(s).
- Fusion Start EU: `.fusionStartEU(long eu)`
    - Locks a recipe behind the amount of stored power in a fusion machine. To use this, the machine must use the FusionReactorMachine class.
    - For example, you could use `.fusionStartEU(600000)`
- Station Research: `.stationResearch(b => b.researchStack("namespace:item_id").EUt(long eu).CWUt(int minCWUPerTick, int TotalCWU))`
    - Locks a recipe behind having a certain research stack. For this condition to be properly seen, you will either need a base machine recipe type with the research ui component, or make your own.
    - For example, you could do `.stationResearch(b => b.researchStack("gtceu:lv_motor").EUt(131000).CWUt(24, 12000))` which would lock a recipe behind needing a data orb with the lv motor research. It will also generate you a research station recipe.
- Scanner Research: `.scannerResearch(b => b.researchStack("namespace:item_id").EUt(long eu))`
    - Much like station research, this condition locks a recipe behind needing a research stack. However, in this case it will default to a data stick.
    - For example, you could do `.scannerResearch(b => b.researchStack("gtceu:lv_motor").EUt(8192))`, which would make the recipe need a data stick with the lv motor research, and generates a scanner recipe.
- Environmental Hazard: `.environmentalHazard("medical_condition_name")`
    - Locks a recipe into needing a certain environmental hazard to run. For now, `"carbon_monoxide_poisoning"` is the only one that's added to the world (by default). An example of a machine using this condition is the air scrubber.
    - For example, you could do `.environmentalHazard("carcinogen")` (if you have something that creates radiation, as if you don't, the recipe would never run.)
- Daytime: `.daytime(boolean isNight)`
    - Locks recipe behind whether it is day or night.
    - For example, you could do `.daytime(true)` to make the recipe require nighttime to run.

### Mod Dependent Conditions
- FTB Quests: `.ftbQuest("quest_id")`
    - Locks a recipe behind the owner of a machine completing a quest with FTB Quests.
    - An example can't be easily given since every quest book is different.
- Game Stages: `.gameStage("gamestage_id")`
    - Locks a recipe behind a certain game stage.  
- Odyssey Quests (Heracles): `.heraclesQuest("quest_id")`
    - Locks a recipe behind the owner of a machine completing a quest with Heracles.
    - An example can't be easily given since every quest book is different.

