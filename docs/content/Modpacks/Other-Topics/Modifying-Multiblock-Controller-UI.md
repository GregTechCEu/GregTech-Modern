---
title: Modifying Multiblock Controller UI
---
# Modifying Multiblock Controller UI

## Adding text component
To add text component to the UI, you need to use `.additionalDisplay` in multiblock code, e.g.:

```js title="ui_modified_multiblock.js"
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('ui_modified_multiblock', 'multiblock')
		.rotationState(RotationState.NON_Y_AXIS)
		.recipeType('electrolyzer')
		.recipeModifiers([GTRecipeModifiers.OC_NON_PERFECT_SUBTICK])
		.appearanceBlock(() => Block.getBlock("gtceu:solid_machine_casing"))
		.pattern(definition => FactoryBlockPattern.start()
			.aisle('###','   ','###')
			.aisle('###',' S ','###')
			.aisle('#C#','   ','###')
			.where('C', Predicates.controller(Predicates.blocks(definition.get())))
			.where('#', Predicates.blocks("gtceu:solid_machine_casing")
				.or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
				.or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
				.or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)))
			.where('S', Predicates.blocks("gtceu:steel_machine_casing"))
			.where(' ', Predicates.any())
			.build())
		.workableCasingModel("gtceu:block/casings/solid/machine_casing_solid_steel", "gtceu:block/multiblock/blast_furnace")
		.additionalDisplay((machine, components) => {	
			if (machine.isFormed()) { // (1)
				components.add(Component.literal("Muhehehe, i'm text!"))
                components.add(Component.literal("And I am his brother! ^^^")) // (2)
			}
		}) // (3)
});
```

1. Check if multiblock is formed
2. To add new line - Use `components.add(Component)`.
3. Using `.additionalDisplay()` to add text component.