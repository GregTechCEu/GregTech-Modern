---
title: "Ore Processing Plant"
---


# Ore Processing Plant Multiblock (by trulyno)

## Recipe Type

=== "Java"
    ```java title="RecipeTypes.java"
        public final static GTRecipeType ORE_PROCESSING_RECIPES = register("ore_processing_plant", MULTIBLOCK)
            .setMaxIOSize(1, 8, 2, 1)
            .setEUIO(IO.IN)
            .setSound(GTSoundEntries.BATH);
    ```
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

## Multiblock

=== "Java"
    ```java title="MultiMachines.java"
    public static final MultiblockMachineDefinition ORE_PROCESSING_PLANT = REGISTRATE
            .multiblock("ore_processing_plant", WorkableElectricMultiblockMachine::new)
            .langValue("Ore Processing Plant")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(RecipeTypes.ORE_PROCESSING_PLANT)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK))
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition => MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                .slice(' AAA ', ' FFF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                .slice('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                .slice('AFFFA', 'F P F', 'F P F', 'F P F', ' FPF ', ' FMF ', ' B B ')
                .slice('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                .slice(' AAA ', ' FCF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                    .and(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .and(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                    .and(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
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
=== "JavaScript"
    ```js title="ore_processing_plant.js"
        GTCEuStartupEvents.registry('gtceu:machine', event => {
            event.create('ore_processing_plant', 'multiblock')
                .rotationState(RotationState.NON_Y_AXIS)
                .langValue("Ore Processing Plant")
                .recipeType('gtceu:ore_processing_plant')
                .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK))
                .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
                .pattern(definition => MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(' AAA ', ' FFF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                    .slice('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                    .slice('AFFFA', 'F P F', 'F P F', 'F P F', ' FPF ', ' FMF ', ' B B ')
                    .slice('AFFFA', 'FG GF', 'F   F', ' F F ', ' FFF ', '  F  ', '  B  ')
                    .slice(' AAA ', ' FCF ', ' FFF ', '  F  ', '     ', '     ', '     ')
                    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('F', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                        .and(Predicates.autoAbilities(definition.getRecipeTypes()))
                        .and(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .and(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
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
