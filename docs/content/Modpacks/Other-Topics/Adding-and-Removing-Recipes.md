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
        - `.notConsumable()`
    - Fluids:
        - `.inputFluids()`
        - `.chancedFluidInput()`
        - `.notConsumableFluid()`
    - Misc:
        - `.circuit()`
- Outputs:
    - Items:
        - `.itemOutput()`
        - `.itemOutputs()`
        - `.chancedOutput()`
    - Fluids:
        - `.outputFluids()`
        - `.chancedFluidOutput()`
- Energy:
    - `.inputEU()`:  
      Makes the recipe consume a lump sum of EU to start the recipe. Most often seen in fusion reactor recipes.
    - `.outputEU()`:  
      Makes the recipe produce a lump sum of EU upon recipe completion.
    - `.EUt()`:  
      Takes a numerical value representing an EU amount. Positive values will make the recipe consume energy per tick,
      negative ones will make it generate energy per tick.
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

!!! warning
When adding rock breaker recipes you will need to manually define the fluids the rock breaker will use.
(might change in the future)

```js title="rock_breaker.js"
ServerEvents.recipes(event => {
    event.recipes.gtceu.rock_breaker('rhino_jank')
        .notConsumable('minecraft:dirt')
        .itemOutputs('minecraft:dirt')
        .addDataString("fluidA", "minecraft:lava")
        .addDataString("fluidB", "minecraft:water")
        .duration(16)
        .EUt(30)
})
```
