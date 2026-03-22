---
title: Custom Recipe Modifiers
---

# Custom Recipe Modifiers / Data Logic

## Adding a Modifier
Custom recipe modifiers in KubeJS are done through a function. For this example, we will make multiblock that requires temperature for recipes, like the EBF does.
```js title="temperature_recipe_modifier.js"
const $GTRecipe = Java.loadClass("com.gregtechceu.gtceu.api.recipe.GTRecipe");
const $MetaMachine = Java.loadClass("com.gregtechceu.gtceu.api.machine.MetaMachine");

function TemperatureModifier(machine, recipe) {
    if (!(machine instanceof $MetaMachine)) return ModifierFunction.NULL // (1)
    if (!(recipe instanceof $GTRecipe)) return ModifierFunction.NULL
    
    if (!machine instanceof $CoilWorkableElectricMultiblockMachine) {
        return $RecipeModifier.nullWrongType($CoilWorkableElectricMultiblockMachine, machine);
    } else {

        let temp = machine.getCoilType().getCoilTemperature() // (3)

        let recipeTemp = recipe.data.getInt("RequiredTemp") // (4)
        if (recipeTemp > temp) {
            return ModifierFunction.NULL
        }
        return ModifierFunction.IDENTITY // (2)
    }
}
```

1. `ModifierFunction.NULL` Stops recipe.
2. `ModifierFunction.IDENTITY` Starts recipe.
3. Getting the coil temperature, multiblock **must** contain ``.heatingCoils()`` in any of its keys.
4. Checking if coil temperature is high enough.

## Using Modifier
```js title="example_temperature_multiblock.js"
const $CoilWorkableElectricMultiblockMachine = Java.loadClass("com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine");

GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
	event.create('example_smelting')
		.category('multiblock')
		.setMaxIOSize(1, 1, 0, 0)
		.setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, FillDirection.LEFT_TO_RIGHT)
		.setSound(GTSoundEntries.BATH);
});

GTCEuStartupEvents.registry('gtceu:machine', event => {

	GTRecipeTypes.get("example_smelting").addDataInfo((data) => (
		`Temperature: ${data.getInt("RequiredTemp")}K` // (4)
	)) // (3)

	event.create('example_smelter', 'multiblock')
		.rotationState(RotationState.NON_Y_AXIS)
		.machine((holder) => new $CoilWorkableElectricMultiblockMachine(holder)) // (1)
		.recipeType('alchemy')
		.recipeModifiers([(machine, recipe) => TemperatureModifier(machine, recipe)]) // (2)
		.appearanceBlock(() => Block.getBlock("gtceu:solid_machine_casing"))
		.pattern(definition => FactoryBlockPattern.start()
			.aisle('###','HHH','###')
			.aisle('###','H H','###')
			.aisle('#C#','HHH','###')
			.where('C', Predicates.controller(Predicates.blocks(definition.get())))
			.where('#', Predicates.blocks("gtceu:solid_machine_casing")
				.or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
				.or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
				.or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)))
			.where('H', Predicates.heatingCoils())
			.where(' ', Predicates.any())
			.build())
		.workableCasingModel("gtceu:block/casings/solid/machine_casing_solid_steel", "gtceu:block/multiblock/blast_furnace")
})
```

1. Making multiblock **coilMachine**, without it our modifier won't work.
2. Using our modifier.
3. Display our data in EMI.
4. Getting `RequiredTemp` data from our recipe.

## Using our Modifier in a Recipe
To use our modifier in recipe, you need to add data to it.
```js title="example_smelting.js"
ServerEvents.recipes(event => {
	event.recipes.gtceu.example_smelting('example:diamondirt')
        .itemInputs('minecraft:dirt')
        .itemOutputs('gtceu:raw_diamond')
        .addData("RequiredTemp", 1000) // (1)
        .duration(320)
        .EUt(GTValues.VA[GTValues.LV]);
})
```

1. Adding data to our recipe, in this situation - Temperature