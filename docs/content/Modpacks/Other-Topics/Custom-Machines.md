---
title: Custom Machines
---


# Custom Machines


## Creating Custom Steam Machine

```js title="test_steam_machine.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_simple_steam_machine', 'steam', true) // (1)
})
```

1. Machine ID, Machine Type, Has High Pressure Varient


## Creating Custom Electric Machine

```js title="test_electric_machine.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_electric', 'simple', 0, GTValues.LV, GTValues.MV, GTValues.HV) // (1)
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType('test_recipe_type')
        .tankScalingFunction(tier => tier * 3200)
})
```


1. Machine ID, Machine Type, Pollution Produced, Voltage Tiers



## Creating Custom Kinetic Machine

```js title="test_kinetic_machine.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_kinetic', 'kinetic', GTValues.LV, GTValues.MV, GTValues.HV)
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType('test_kinetic_recipe_type')
        .tankScalingFunction(tier => tier * 3200)
})
```


## Creating Custom Generator

```js title="test_generator.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_generator', 'generator', GTValues.LV, GTValues.MV, GTValues.HV) // (1)
        .recipeType('test_generator_recipe_type')
        .tankScalingFunction(tier => tier * 3200)
})
```


## Creating Custom Multiblock

```js title="test_multiblock.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_generator', 'multiblock')
        .tooltips(Component.translatable('your.langfile.entry.here')) // (1)
        .rotationState(RotationState.NON_Y_AXIS)
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .recipeTypes(['test_recipe_type_1', 'test_recipe_type_2'])
        .recipeModifiers([GTRecipeModifiers.PARALLEL_HATCH, ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK)]) // (2)
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCC', 'GGG', 'CCC')
            .aisle('CCC', 'GDG', 'CSC')
            .aisle('CKC', 'GGG', 'CMC')
            .where('K', Predicates.controller(Predicates.blocks(definition.get())))
            .where('M', Predicates.abilities(PartAbility.MAINTENANCE))
            .where('S', Predicates.abilities(PartAbility.MUFFLER))
            .where('D', Predicates.blocks(GTBlocks.COIL_CUPRONICKEL.get()))
            .where('G', Predicates.blocks('minecraft:glass'))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                .or(Predicates.autoAbilities(definition.getRecipeTypes())))
        .build())
        .workableCasingModel(
            "gtceu:block/casings/solid/machine_casing_inert_ptfe",
            "gtceu:block/multiblock/large_chemical_reactor"
        )
})
```


1. You can add tooltips to your multiblock controllers that show up when you mouseover them. Each separate call of ```.tooltips()``` will add a separate line to the controller's tooltip. ```Component.translatable()``` reads entries from .json lang files placed in ```kubejs/assets/gtceu/lang``` or supplied via a standalone resource pack. The ```Component``` class is autoloaded by KubeJS at compile time; it doesn't need to be manually loaded.
2. If electric and/or multiblock machines can process your custom recipe type, ```.recipeModifiers()``` will allow you to fine-tune the behaviour of these machine when running recipes of your custom recipe type. ```GTRecipeModifiers.PARALLEL_HATCH``` will enable Multiblock Machines to parallelize recipes of your custom type via an optional Parallel Hatch, while ```ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK)```will define how your recipes overclock in electric machines and multiblocks.

### Shape Info

Shape Info is used to manually define how your multiblock appears in the JEI/REI/EMI multiblock preview tab.

```js title="shape_info_test.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_generator', 'multiblock')
        .rotationState(RotationState.NON_Y_AXIS)
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .recipeTypes(['test_recipe_type_1', 'test_recipe_type_2'])
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCC', 'GGG', 'CCC')
            .aisle('CCC', 'GDG', 'CSC')
            .aisle('CKC', 'GGG', 'CMC')
            .where('K', Predicates.controller(Predicates.blocks(definition.get())))
            .where('M', Predicates.abilities(PartAbility.MAINTENANCE))
            .where('S', Predicates.abilities(PartAbility.MUFFLER))
            .where('D', Predicates.blocks(GTBlocks.COIL_CUPRONICKEL.get()))
            .where('G', Predicates.blocks('minecraft:glass'))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                .or(Predicates.autoAbilities(definition.getRecipeTypes())))
        .build())
        .shapeInfo(controller => MultiblockShapeInfo.builder()
            .aisle('eCe', 'GGG', 'CCC')
            .aisle('CCC', 'GDG', 'CSC')
            .aisle('iKo', 'GGG', 'CMC')
            .where('K', controller, Direction.SOUTH)
            .where('C', GTBlocks.CASING_STEEL_SOLID.get())
            .where('G', Block.getBlock('minecraft:glass'))
            .where('D', GTBlocks.COIL_CUPRONICKEL.get())
            .where('S', GTMachines.MUFFLER_HATCH[1], Direction.UP)
            .where('M', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH)
            .where('e', GTMachines.ENERGY_INPUT_HATCH[1], Direction.NORTH)
            .where('i', GTMachines.ITEM_IMPORT_BUS[1], Direction.SOUTH)
            .where('0', GTMachines.ITEM_EXPORT_BUS[1], Direction.SOUTH)
        .build())
        .workableCasingModel(
            "gtceu:block/casings/solid/machine_casing_inert_ptfe",
            "gtceu:block/multiblock/large_chemical_reactor"
        )
})
```
