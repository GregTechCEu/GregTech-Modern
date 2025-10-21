---
title: "Ore Processing Plant"
---


# Ore Processing Plant Multiblock (by trulyno)

## Recipe Type

=== "JavaScript"
    ```js title="ore_processing_plant.js"
    GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
        event.create('ore_processing_plant')
            .category('ore_processing_plant')
            .setEUIO('in')
            .setMaxIOSize(1, 8, 2, 1)
            .setSound(GTSoundEntries.BATH);
    });
    ```

=== "Java"
    ```java title="RecipeTypes.java"
        public final static GTRecipeType ORE_PROCESSING_RECIPES = register("ore_processing_plant", MULTIBLOCK)
            .setMaxIOSize(1, 8, 2, 1)
            .setEUIO(IO.IN)
            .setSound(GTSoundEntries.BATH);
    ```


## Multiblock
=== "JavaScript"
    ```js title="ore_processing_plant.js"
    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('ore_processing_plant', 'multiblock')
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType('ore_processing_plant')
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK))
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition => FactoryBlockPattern.start()
                .aisle(' AAA ', ' FFF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                .aisle('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                .aisle('AFFFA', 'F P F', 'F P F', 'F P F', ' FPF ', ' FMF ', ' B B ')
                .aisle('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                .aisle(' AAA ', ' FCF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('P', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                .where('G', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                .where('A', Predicates.blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                .where('B', Predicates.blocks('gtceu:bronze_machine_casing'))
                .where(' ', Predicates.any())
                .build())
            .workableCasingModel(
                "gtceu:block/casings/solid/machine_casing_robust_tungstensteel",
                "gtceu:block/multiblock/primitive_blast_furnace"
            );
        })
    ```

=== "Java"
    ```java title="MultiMachines.java"
    public static final MultiblockMachineDefinition ORE_PROCESSING_PLANT = REGISTRATE
            .multiblock("ore_processing_plant", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(RecipeTypes.ORE_PROCESSING_PLANT)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK))
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition => FactoryBlockPattern.start()
                .aisle(' AAA ', ' FFF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                .aisle('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                .aisle('AFFFA', 'F P F', 'F P F', 'F P F', ' FPF ', ' FMF ', ' B B ')
                .aisle('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                .aisle(' AAA ', ' FCF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                .where('M', Predicates.abilities(PartAbility.MUFFLER))
                .where('P', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                .where('G', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                .where('A', Predicates.blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                .where('B', Predicates.blocks('gtceu:bronze_machine_casing'))
                .where(' ', Predicates.any())
                .build())
            .workableCasingModel(
                GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                GTCEu.id("block/multiblock/primitive_blast_furnace")
            )
            .register();
    ```


## Lang

```json title="en_us.json"
{
    "block.gtceu.ore_processing_plant": "Ore Processing Plant",
    "gtceu.ore_processing_plant": "Ore Processing"
}
```