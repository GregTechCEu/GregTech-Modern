---
title: "Superheated Pyrolyzing Oven"
---

### Superheated Pyrolyzing Oven Multiblock (by Phoenixvine)


Below is an example of a multiblock using the CoilWorkableElectricMultiblockMachine class and the pyrolyseOvenOverclock machine logic.

### Multiblock
=== "JavaScript"
    ```js title="superheated_pyrolyzing_oven_multiblock.js"
    // In order to use multiblock logic extending beyond the normal WorkableElectricMultiblockMachine, (This is the multiblock type used by default for kubejs) you need to load a class. Coil multiblocks such as the Electric Blast Furnace, Pyrolyse Oven, and the Cracker use this class.
    const CoilWorkableElectricMultiblockMachine = Java.loadClass("com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine")
    
    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create("superheated_pyrolyzing_oven", "multiblock")
            .machine((holder) => new CoilWorkableElectricMultiblockMachine(holder))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes('pyrolyse_oven')
            .recipeModifiers([GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_PERFECT,GTRecipeModifiers.BATCH_MODE, (machine, recipe) =>
            GTRecipeModifiers.pyrolyseOvenOverclock(machine, recipe)])
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition => FactoryBlockPattern.start()
                .aisle("BBCCCBB", "BBCDCBB", "BBCCCBB", "BBCCCBB", "BBEEEBB", "BBEEEBB")
                .aisle("BCCCCCB", "BCAFACB", "BCAFACB", "BCGGGCB", "BEAAAEB", "BEHHHEB")
                .aisle("CCCCCCC", "CAAFAAC", "CAAFAAC", "CGGGGGC", "EAAAAAE", "EHHHHHE")
                .aisle("CCCCCCC", "DFFFFFD", "CFFFFFC", "CGGGGGC", "EAAAAAE", "EHHMHHE")
                .aisle("CCCCCCC", "CAAFAAC", "CAAFAAC", "CGGGGGC", "EAAAAAE", "EHHHHHE")
                .aisle("BCCCCCB", "BCAFACB", "BCAFACB", "BCGGGCB", "BEAAAEB", "BEHHHEB")
                .aisle("BBCCCBB", "BBCICBB", "BBCCCBB", "BBCCCBB", "BBEEEBB", "BBEEEBB")
                .where("A", Predicates.blocks("minecraft:air"))
                .where("B", Predicates.any())
                .where('C', Predicates.blocks('gtceu:solid_machine_casing').setMinGlobalLimited(10) 
                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                    .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                .where("D", Predicates.blocks("gtceu:steel_firebox_casing"))
                .where("E", Predicates.blocks("gtceu:laminated_glass"))
                .where("F", Predicates.blocks("gtceu:ptfe_pipe_casing"))
                .where("G", Predicates.heatingCoils())
                .where("H", Predicates.blocks("gtceu:high_temperature_smelting_casing"))
                .where("M", Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                .where('I', Predicates.controller(Predicates.blocks(definition.get())))
                .build())
            .workableCasingModel("gtceu:block/casings/solid/machine_casing_solid_steel",
                "gtceu:block/multiblock/pyrolyse_oven");
    
    })
    ```


=== "Java"
    ```java title="MultiMachines.java"
    public static final MultiblockMachineDefinition SUPERHEATED_PYROLYZING_OVEN = REGISTRATE
            .multiblock("superheated_pyrolyzing_oven", (holder) => new CoilWorkableElectricMultiblockMachine(holder))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.PYROLYSE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, 
                GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK),
                (machine, recipe) => GTRecipeModifiers.pyrolyseOvenOverclock(machine, recipe))
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition => FactoryBlockPattern.start()
                .aisle(" AAA ", " FFF ", " FFF ", "  F  ", "     ", "     ", "     ")
                .aisle("AFFFA", "FG GF", "F   F", " F F ", " FFF ", "  F  ", "  B  ")
                .aisle("AFFFA", "F P F", "F P F", "F P F", " FPF ", " FMF ", " B B ")
                .aisle("AFFFA", "FG GF", "F   F", " F F ", " FFF ", "  F  ", "  B  ")
                .aisle(" AAA ", " FCF ", " FFF ", "  F  ", "     ", "     ", "     ")
                .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('P', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                .where('G', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                .where('A', Predicates.blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                .where('B', Predicates.blocks(GTBlocks.BRONZE_HULL.get())
                .where(' ', Predicates.any())
                .build())
            .workableCasingModel(
                GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                GTCEu.id("block/multiblock/primitive_blast_furnace")
            )
            .register();
    ```

### Lang

```json title="en_us.json"
{
    "block.gtceu.superheated_pyrolyzing_oven": "Superheated Pyrolyzing Oven",
}
```





