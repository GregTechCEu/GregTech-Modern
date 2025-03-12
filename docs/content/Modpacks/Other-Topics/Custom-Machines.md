---
title: Custom Machines
---


# Custom Machines


## Creating Custom Steam Machine

```js title="example_steam_machine.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('example_steam_machine_recipes')
        .setEUIO('in')
        .setMaxIOSize(6, 1, 0, 0) // (1)
        .setSlotOverlay(false, false, GuiTextures.COMPRESSOR_OVERLAY)
        .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, FillDirection.LEFT_TO_RIGHT)
        .setSound(GTSoundEntries.MIXER)
})

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('example_steam_singleblock', 'steam') // (2)
        .hasHighPressure(true) // (3)
        .definition((hp, builder) => (
            builder
                .recipeType("example_steam_machine_recipes") // (4)
                .workableSteamHullRenderer(hp, 'gtceu:block/machines/mixer') // (5)
        ))
})
```

1. (item in, item out, fluid in, fluid out) You *cannot* use fluid slots on steam machines currently due to steam tank issues
2. Machine ID, Machine Type ('steam' for a steam singeblock)
3. Register a high pressure variant of the steam machine
4. The recipe type by id used for the machine
5. Use the steam hull renderer(pass through if high pressure, overlay texture resource path)


## Creating Custom Electric Machine

```js title="test_electric_machine.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
	event.create('atomic_reconstructor', 'simple') // (1)
		.tiers(GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV, GTValues.UEV, GTValues.UIV) // (2)
		.definition((tier, builder) =>
			builder
				.langValue(GTValues.VLVH[tier] + " Atomic Reconstructor") // (3)
				.recipeType('atomic_reconstruction') // (4)
				.workableTieredHullRenderer('gtceu:block/machines/reconstructor') //(5)
		)
})
```


1. Machine ID, Machine Type(Simple Singleblock)
2. Tiers to register for machine
3. override lang generator, if you do not add this it will autogen based on id
4. recipe type used
5. uses the tiers hull texture, and resource path to a custom machine overlay

## Creating Custom Generator

```js title="test_generator.js"
GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
    event.create('coal_burner_recipe_type')
        .setEUIO('out')
        .setMaxIOSize(1, 0, 0, 0)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
        .setSound(GTSoundEntries.ARC)
        .setMaxTooltips(6)
})

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('coal_burner', 'generator') // (1)
        .tiers(GTValues.ULV) // (2)
        .definition((tier, builder) => (
            builder
                .langValue("Shoddy Coal Burning Generator") // (3)
                .recipeType('coal_burner_recipe_type') // (4)
                .recipeModifier(MachineModifiers.SIMPLE_GENERATOR) // (5)
                .simpleGeneratorMachineRenderer('gtceu:block/generators/combustion') // (6)
        ))
})
```
1. Machine ID, Machine Type(Simple Singleblock)
2. Tiers to register for machine
3. override lang generator, if you do not add this it will autogen based on id
4. recipe type used
5. recipe modifier for generator behaviour (when using generator beyond lowest tier, the recipes are paralleled for higher energy output)
6. uses the tiers hull texture, and resource path to a custom machine overlay

## Creating Custom Multiblock

```js title="test_multiblock.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('test_generator', 'multiblock')
        .tooltips(Component.translatable('your.langfile.entry.here')) // (1)
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
        .workableCasingRenderer(
            "gtceu:block/casings/solid/machine_casing_inert_ptfe",
            "gtceu:block/multiblock/large_chemical_reactor",
            false
        )
})
```


1. You can add tooltips to your multiblock controllers that show up when you mouseover them. Each separate call of ```.tooltips()``` will add a separate line to the controller's tooltip. ```Component.translatable()``` reads entries from .json lang files placed in ```kubejs/assets/gtceu/lang``` or supplied via a standalone resource pack. The ```Component``` class is autoloaded by KubeJS at compile time; it doesn't need to be manually loaded.


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
            .where('M', GTMachines.MAINTENANCE_HATCH[1], Direction.SOUTH)
            .where('e', GTMachines.ENERGY_INPUT_HATCH[1], Direction.NORTH)
            .where('i', GTMachines.ITEM_IMPORT_BUS[1], Direction.SOUTH)
            .where('0', GTMachines.ITEM_EXPORT_BUS[1], Direction.SOUTH)
        .build())
        .workableCasingRenderer(
            "gtceu:block/casings/solid/machine_casing_inert_ptfe",
            "gtceu:block/multiblock/large_chemical_reactor",
            false
        )
})
```

