
```const CoilWorkableElectricMultiblockMachine = Java.loadClass("com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine")```

```GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create("superheated_pyrolyzing_oven", "multiblock")
        .rotationState(RotationState.NON_Y_AXIS)
        .machine((holder) => new CoilWorkableElectricMultiblockMachine(holder))
        .recipeTypes('pyrolyse_oven')
        .recipeModifiers([GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_PERFECT,GTRecipeModifiers.BATCH_MODE, (machine, recipe) => GTRecipeModifiers.pyrolyseOvenOverclock(machine, recipe)])
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

})```
