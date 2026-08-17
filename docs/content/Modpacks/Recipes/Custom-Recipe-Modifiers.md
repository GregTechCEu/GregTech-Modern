---
title: Custom Recipe Modifiers
---

# Custom Recipe Modifiers / Data Logic

## Adding a Modifier

Custom recipe modifiers in are done through a function. For this example, we will make multiblock that requires temperature for recipes, like the EBF does.

!!! Warning
    It is recommended that your custom recipe modifiers are implemented in Java.

=== "Java"
    ```java title="AddonRecipeModifiers.java"
    public static ModifierFunction customTemperatureModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilWorkable)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }
        int temp = coilWorkable.getCoilType().getCoilTemperature()

        int recipeTemp = recipe.data.getInt("RequiredTemp")
        if (recipeTemp > temp) {
            return ModifierFunction.NULL // Cancels recipe
        }
        return ModifierFunction.IDENTITY // Runs recipe
    }
    ```
=== "JavaScript"
    ```js title="temperature_recipe_modifier.js"
    const $GTRecipe = Java.loadClass("com.gregtechceu.gtceu.api.recipe.GTRecipe");
    const $MetaMachine = Java.loadClass("com.gregtechceu.gtceu.api.machine.MetaMachine");
    
    function TemperatureModifier(machine, recipe) {
        if (!machine instanceof $CoilWorkableElectricMultiblockMachine) {
            return $RecipeModifier.nullWrongType($CoilWorkableElectricMultiblockMachine, machine);
        } else {
    
            let temp = machine.getCoilType().getCoilTemperature() // (1)
    
            let recipeTemp = recipe.data.getInt("RequiredTemp") // (2)
            if (recipeTemp > temp) {
                return ModifierFunction.NULL // Cancels recipe
            }
            return ModifierFunction.IDENTITY // Runs recipe
        }
    }
    ```

1. Getting the coil temperature, multiblock **must** contain ``.heatingCoils()`` in any of its keys.
2. Checking if coil temperature is high enough.

## Using Modifier

```js title="example_temperature_multiblock.js"
const $CoilWorkableElectricMultiblockMachine = Java.loadClass("com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine");

GTCEuStartupEvents.registry('gtceu:recipe_type', event => {
	event.create('example_smelting')
		.category('multiblock')
		.setMaxIOSize(1, 1, 0, 0)
		.setProgressBar(GTGuiTextures.PROGRESS_FUSION)
		.addRecipeInfo(recipe => Text.literal(`Temperature: ${recipe.data.getInt("RequiredTemp")}K`)) // (3) (4)
		.setSound(GTSoundEntries.BATH);
});

GTCEuStartupEvents.registry('gtceu:machine', event => {

	event.create('example_smelter', 'multiblock')
		.rotationState(RotationState.NON_Y_AXIS)
		.machine((holder) => new $CoilWorkableElectricMultiblockMachine(holder)) // (1)
		.recipeType('alchemy')
		.recipeModifiers([(machine, recipe) => TemperatureModifier(machine, recipe)]) // (2)
		.appearanceBlock(() => Block.getBlock("gtceu:solid_machine_casing"))
		.pattern(definition => MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
			.slice('###','HHH','###')
			.slice('###','H H','###')
			.slice('#C#','HHH','###')
			.where('C', Predicates.controller(Predicates.blocks(definition.get())))
			.where('#', Predicates.blocks("gtceu:solid_machine_casing")
				.and(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
				.and(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
				.and(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)))
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