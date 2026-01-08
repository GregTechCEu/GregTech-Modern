---
title: "Adding & Removing Recipes"
---


# Adding and Removing Recipes

## Removing Recipes

Removing GTCEu Modern recipes with KubeJS works the same as any other recipe, meaning they can be removed by:
ID, Mod, Input, Output, Type or a Mixture.

```js title="gtceu_removal.js"
ServerEvents.recipes(event => {
    event.remove({ id: 'gtceu:smelting/sticky_resin_from_slime' }) // (1)
    event.remove({ mod: 'gtceu' }) // (2)
    event.remove({ type: 'gtceu:arc_furnace' }) // (3)
    event.remove({ input: '#forge:ingots/iron' }) // (4)
    event.remove({ output: 'minecraft:cobblestone' }) // (5)
    event.remove({ type: 'gtceu:assembler', input: '#forge:plates/steel' }) // (6)
})
```

1. Targets the slime to sticky resin furnace recipe only for removal.
2. Targets all recipes under the gtceu mod id for removal.
3. Targets all recipes in the arc furnace for removal.
4. Targets all recipes that have an input of `#forge:ingots/iron` for removal.
5. Targets all recipes that have an output of `minecraft:cobblestone` for removal.
6. Targets all recipes in the gtceu assembler that have an input of `#forge:plates/steel` for removal.


## Modifiying Recipes

With KubeJS it is possible to modify the Inputs or Outputs of existing GTCEu Modern recipes, which uses the same method
of targeting the recipes.

```js title="gtceu_modify.js"
ServerEvents.recipes(event => {
    event.replaceInput({ mod: 'gtceu' }, 'minecraft:sand', '#forge:sand') // (1)
    event.replaceOutput({ type: 'gtceu:arc_furnace' }, 'gtceu:wrought_iron_ingot', 'minecraft:dirt') // (2)
})
```

1. Targets all gtceu recipes that have and input of `minecraft:sand` and replaces it with `#forge:sand`.
2. Targets all gtceu arc furnace recipes that have an output of `gtceu:wrought_iron_ingot` and replaces it
   with `minecraft:dirt`.


## Adding Recipes

Syntax: `event.recipes.gtceu.RECIPE_TYPE(string: recipe id)`

```js title="gtceu_add.js"
ServerEvents.recipes(event => {
    event.recipes.gtceu.assembler('test')
        .itemInputs(
            '64x minecraft:dirt',
            '32x minecraft:diamond'
        )
        .inputFluids(
            Fluid.of('minecraft:lava', 1500)
        )
        .itemOutputs(
            'minecraft:stick'
        )
        .duration(100)
        .EUt(30)
})
```

### Event calls for adding inputs and outputs

- Basic calls:
    - `.input()`:  
      The most basic input definition available. Takes two parameters: one RecipeCapability that defines what input type
      this call is supposed to be (usually an item, a fluid or energy), and an Object that defines the input itself.
      Available RecipeCapabilities can be found in the GTCEu Modern GitHub or the mod's .JAR file, but the class containing
      all of GTCEu Modern's native RecipeCapabilities, `GTRecipeCapabilites`, must be manually loaded in your scripts.
      This method is unwieldy to use in Javascript; it is more user-friendly to use the ones below that clearly tell you
      what input type they call.
    - `.output()`:  
      As above, but defines an output instead. Takes the exact same parameters. This method is likewise unwieldy to use;
      it is more user-friendly to use the ones below that clearly tell you what output type they call.
- Inputs:
    - Items:
        - `.itemInput()`
        - `.itemInputs()`
        - `.chancedInput()`
        - `.itemInputsRanged()`
        - `.notConsumable()`
    - Fluids:
        - `.inputFluids()`
        - `.chancedFluidInput()`
        - `.inputFluidsRanged()`
        - `.notConsumableFluid()`
    - Misc:
        - `.circuit()`
- Outputs:
    - Items:
        - `.itemOutput()`
        - `.itemOutputs()`
        - `.chancedOutput()`
        - `.itemOutputsRanged()`
    - Fluids:
        - `.outputFluids()`
        - `.chancedFluidOutput()`
        - `.outputFluidsRanged()`
- Energy:
    - `.inputEU()`:  
      Makes the recipe consume a lump sum of EU to start the recipe. Most often seen in fusion reactor recipes.
    - `.outputEU()`:  
      Makes the recipe produce a lump sum of EU upon recipe completion.
    - `.EUt()`:  
      Takes a numerical value representing an EU amount. Positive values will make the recipe consume energy per tick,
      negative ones will make it generate energy per tick.
- Chanced Ingredients:
    - Ingredients that are not consumed/produced on every run of a recipe. Can be expressed as either a fraction, or as an
      integer chance out of 10,000.
    - Assigning an Input ingredient with a Chance of `0` causes that ingredient to be flagged as `Non-Consumed` in EMI. 
      This can also be done more easily using `.notConsumable()`.
    - Recipes with chanced ingredients can also have a Chance Logic designated for each of their input/output sets, using 
      one or more of the functions `.chancedItemInputLogic()`, `.chancedFluidInputLogic()`, `.chancedTickInputLogic()`,
      `.chancedItemOutputLogic()`, `.chancedFluidOutputLogic()`, `.chancedTickOutputLogic()`
    - Valid options for chanced logic are:
        - `or` - (default) Any item/fluid which succeeds on its chance roll is produced/consumed.
        - `and` - If _all_ items/fluids succeed on their chance roll, all are produced/consumed together. Otherwise, none are. 
        - `xor` - Normalizes ingredient chances, and guarantees that exactly one of the chanced items/fluids will be 
        produced/consumed on every run. XOR's behavior was changed in 7.0.0.
        - `first` - Makes a chance roll for each item/fluid, in order of registration. Only the first item which succeeds 
        on its roll is returned. Prior to 7.0.0, this was the behavior of `xor` logic. 
            - Because of its unpredictable behavior, FIRST has been Deprecated as of 7.3.0, and is scheduled for removal
            in 8.0.0.
- Ranged Ingredients:
    - Item or Fluid ingredients that will be consumed or produced in a random amount within a `min, max` range (inclusive).
- Circuits
    - Many GT recipes use a `Programmed Circuit` item with a Configuration value of `1-32` as a `Non-Consumed` input,
to distinguish them from other recipes in the same machine with similar ingredients. `.circuit()` adds one to a recipe.
- More granular functionality:
    - `.perTick()`:  
      Using this will enable you to control whether a recipe input/output is consumed/produced per tick the recipe is
      running or all at once at recipe start/end. Set to true with `.perTick(true)` to make the recipe builder consider
      any following input/output calls as per-tick. Remember to set the value to false with `.perTick(false)` after the
      calls you intend to be per-tick, to prevent behaviour you don't want!


### The Research System

GTCEu has Research System which allows for adding extra requirements to recipes such as:  
Scanner Research, Station Research and Computation.

```js title="scanner_research.js"
ServerEvents.recipes(event => {
    event.recipes.gtceu.assembly_line('scanner_test')
        .itemInputs('64x minecraft:coal')
        .itemOutputs('minecraft:diamond')
        .duration(10000)
        .EUt(GTValues.VA[GTValues.IV])
        ["scannerResearch(java.util.function.UnaryOperator)"](b => b.researchStack(Item.of('minecraft:coal_block')).EUt(GTValues.VA[GTValues.IV]).duration(420)) // (1)
})
```

1. Note that due to the way JS integration works, you have to force `scannerResearch` to be interpreted in a specific way:
   Scanner Research accepts an `ItemStack` input in the `.researchStack()` object, and you can also define the `EUt` and
   `Duration` outside of the `.researchStack()` object.

```js title="station_research"
ServerEvents.recipes(event => {
    event.recipes.gtceu.assembly_line('station_test')
        .itemInputs('64x minecraft:coal')
        .itemOutputs('minecraft:diamond')
        .duration(10000)
        .EUt(GTValues.VA[GTValues.IV])
        .stationResearch(b => b.researchStack(Item.of('minecraft:coal_block')).EUt(GTValues.VA[GTValues.IV]).CWUt(10)) // (1)
})
```

1. Just like `Scanner Research` `Station Research` accepts an `ItemStack` input in the `.researchStack()` object,
   however you can only define `EUt` and `CWUt` outside of the `.researchStack()` object. `CWUt` is used to define the
   duration of the `Station Research` recipe.

### Rock breaker fluids

Rock breaker recipes use AdjacentFluidConditions.

To add a condition, you can use the `adjacentFluids(Fluid...)` methods, see [our other condition builder methods](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema.java#L894).

```js title="rock_breaker.js"
ServerEvents.recipes(event => {
    event.recipes.gtceu.rock_breaker('rhino_jank')
        .notConsumable('minecraft:dirt')
        .itemOutputs('minecraft:dirt')
        .adjacentFluids('minecraft:water')
        .adjacentFluids('minecraft:lava')
        .duration(16)
        .EUt(30)
})
```

### More custom ingredients
For more custom ingredients, see the [Ingredients list in the sidebar](Ingredients/index.md)