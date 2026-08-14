---
title: "Example Turbine"
---

### Example Turbine

Below is an example of a multiblock using the LargeTurbineMachine class for making custom large turbines.

### Multiblock

=== "Java"
```java title="MultiMachines.java"
public static final MultiblockMachineDefinition HYPER_GAS_TURBINE = REGISTRATE
        .multiblock("hyper_gas_turbine", (info) -> new LargeTurbineMachine(info, GTValues.LuV)) // The value shows one rotor holder tier above the recommended minimum rotor holder. The tier of rotor holder provides a boost based on the efficiency stat.
        .langValue("Hyper Gas Turbine")
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType(GTRecipeTypes.GAS_TURBINE_FUELS)
        .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE, LargeTurbineMachine::recipeModifier)
        .appearanceBlock(GTBlocks.CASING_STAINLESS_TURBINE)
        .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
            .slice("BBBBBBB", "BBBCBBB", "BBBDBBB", "BBBCBBB", "BBBBBBB")
            .slice("BBBCBBB", "BBCACBB", "BBCECBB", "BBCACBB", "BBBCBBB")
            .slice("BBCCCBB", "BCAAACB", "BCAEACB", "BCAEACB", "BBCCCBB")
            .slice("BCCCCCB", "CAAEAAC", "CEEEEEC", "CAEEEAC", "BCCFCCB")
            .slice("BBCCCBB", "BCAAACB", "BCAEACB", "BCAEACB", "BBCCCBB")
            .slice("BBBCBBB", "BBCACBB", "BBCECBB", "BBCACBB", "BBBCBBB")
            .slice("BBBBBBB", "BBBCBBB", "BBBGBBB", "BBBCBBB", "BBBBBBB")
            .where("A", Predicates.blocks("minecraft:air"))
            .where("B", Predicates.any())
            .where("C", Predicates.blocks("gtceu:stainless_steel_turbine_casing")
                .and(Predicates.autoAbilities(definition.getRecipeTypes()))
                .and(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
            .where("D", Predicates.ability(PartAbility.MUFFLER).setExactLimit(1))
            .where("F", Predicates.ability(PartAbility.ROTOR_HOLDER).setExactLimit(1))
            .where("E", Predicates.blocks("gtceu:stainless_steel_frame"))
            .where("G", Predicates.controller(Predicates.blocks(definition.get())))
                .build())
        .workableCasingModel(GTCEu.id("block/casings/steam/bronze/bottom"),
                GTCEu.id("block/machines/compressor"))
        .register();
```
=== "JavaScript"
    ```js title="hyper_gas_turbine.js"
    // In order to use multiblock logic extending beyond the normal WorkableElectricMultiblockMachine, (This is the multiblock type used by default for kubejs) you need to load a class. LargeTurbineMachines such as the gas, steam, and plasma turbines use this class.
    const $LargeTurbineMachine = Java.loadClass("com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine")
    
    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('hyper_gas_turbine', 'multiblock')
            .machine((info) => new $LargeTurbineMachine(info, GTValues.LuV)) // The value shows one rotor holder tier above the recommended minimum rotor holder. The tier of rotor holder provides a boost based on the efficiency stat.
            .langValue("Hyper Gas Turbine")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes("gas_turbine")
            .recipeModifiers([GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE, (machine, recipe) => $LargeTurbineMachine.recipeModifier(machine, recipe)])
            .appearanceBlock(GTBlocks.CASING_STAINLESS_TURBINE)
            .pattern(definition => MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("BBBBBBB", "BBBCBBB", "BBBDBBB", "BBBCBBB", "BBBBBBB")
                    .slice("BBBCBBB", "BBCACBB", "BBCFCBB", "BBCACBB", "BBBCBBB")
                    .slice("BBCCCBB", "BCAAACB", "BCAFACB", "BCAFACB", "BBCCCBB")
                    .slice("BCCCCCB", "CAAFAAC", "CFFFFFC", "CAFFFAC", "BCCECCB")
                    .slice("BBCCCBB", "BCAAACB", "BCAFACB", "BCAFACB", "BBCCCBB")
                    .slice("BBBCBBB", "BBCACBB", "BBCFCBB", "BBCACBB", "BBBCBBB")
                    .slice("BBBBBBB", "BBBCBBB", "BBBGBBB", "BBBCBBB", "BBBBBBB")
                .where("A", Predicates.blocks("minecraft:air"))
                .where("B", Predicates.any())
                .where("C", Predicates.blocks("gtceu:stainless_steel_turbine_casing")
                    .and(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .and(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .where("D", Predicates.ability(PartAbility.MUFFLER).setExactLimit(1))
                .where("E", Predicates.ability(PartAbility.ROTOR_HOLDER).setExactLimit(1))
                .where("F", Predicates.blocks("gtceu:stainless_steel_frame"))
                .where("G", Predicates.controller(Predicates.blocks(definition.get())))
                .build())
            .workableCasingModel("gtceu:block/casings/mechanic/machine_casing_turbine_stainless_steel",
                "gtceu:block/multiblock/generator/large_gas_turbine")
    });
    ```





