---
title: "Example Steam Multiblock"
---

### Large Steam Compressor Multiblock 

Below is an example of a multiblock using the SteamParallelMultiblockMachine class.
Steam multiblocks such as the Steam Grinder and Steam Oven use this class.

### Multiblock

=== "JavaScript"
    ```js title="example_steam_multiblock_multiblock.js"
    
    // In order to use multiblock logic extending beyond the default multiblock type for KJS (WorkableElectricMultiblockMachine), you need to load a class.
    const $SteamMulti = Java.loadClass('com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine');

    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('large_steam_compressor', 'multiblock')
            .machine((holder) => new $SteamMulti(holder, 4))
            // The number in holder is the max amount of parallel it can use.
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType('compressor')
            .recipeModifier((machine, recipe) => $SteamMulti.recipeModifier(machine, recipe), true)
            .appearanceBlock(GTBlocks.BRONZE_HULL)
            .pattern(definition => FactoryBlockPattern.start()
                .aisle("BCCCB", "BBCBB", "BBCBB", "BBBBB", "BBBBB")
                .aisle("CDDDC", "BDBDB", "BDEDB", "BBDBB", "BBBBB")
                .aisle("CDDDC", "CBBBC", "CEFEC", "BDDDB", "BBGBB")
                .aisle("CDDDC", "BDBDB", "BDEDB", "BBDBB", "BBBBB")
                .aisle("BCCCB", "BBHBB", "BBCBB", "BBBBB", "BBBBB")
                .where('B', Predicates.any())
                .where('C', Predicates.blocks('gtceu:steam_machine_casing').setMinGlobalLimited(10)
                   .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                   .or(Predicates.abilities(PartAbility.STEAM).setMaxGlobalLimited(1))
                   .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1)))
                .where('D', Predicates.blocks("gtceu:industrial_steam_casing"))
                .where('E', Predicates.blocks("gtceu:bronze_brick_casing"))
                .where('F', Predicates.blocks("gtceu:bronze_firebox_casing"))
                .where('G', Predicates.blocks("gtceu:bronze_machine_casing"))
                .where('H', Predicates.controller(Predicates.blocks(definition.get())))
                .build())
            .workableCasingModel("gtceu:block/casings/steam/bronze/bottom",
                "gtceu:block/machines/compressor")
    })
    ```

=== "Java"
    ```java title="MultiMachines.java"
    public static final MultiblockMachineDefinition LARGE_STEAM_COMPRESSOR = REGISTRATE
            .multiblock("large_steam_compressor", (holder) -> new SteamParallelMultiblockMachine(holder, 4))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(COMPRESSOR_RECIPES)
            .recipeModifier((machine, recipe) -> SteamParallelMultiblockMachine.recipeModifier(machine, recipe), true)
            .appearanceBlock(GTBlocks.BRONZE_HULL)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BCCCB", "BBCBB", "BBCBB", "BBBBB", "BBBBB")
                    .aisle("CDDDC", "BDBDB", "BDEDB", "BBDBB", "BBBBB")
                    .aisle("CDDDC", "CBBBC", "CEFEC", "BDDDB", "BBGBB")
                    .aisle("CDDDC", "BDBDB", "BDEDB", "BBDBB", "BBBBB")
                    .aisle("BCCCB", "BBHBB", "BBCBB", "BBBBB", "BBBBB")
                    .where('B', Predicates.any())
                    .where('C', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(10)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setMaxGlobalLimited(1)))
                    .where('D', Predicates.blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get()))
                    .where('E', Predicates.blocks(GTBlocks.BRONZE_BRICKS_HULL.get()))
                    .where('F', Predicates.blocks(GTBlocks.FIREBOX_BRONZE.get()))
                    .where('G', Predicates.blocks(GTBlocks.BRONZE_HULL.get()))
                    .where('H', Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/steam/bronze/bottom"),
                    GTCEu.id("block/machines/compressor"))
            .register();
    ```

### Lang

```json title="en_us.json"
{
    "block.gtceu.large_steam_compressor": "Large Steam Compressor",
}
```





