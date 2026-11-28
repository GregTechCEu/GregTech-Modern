---
title: Overclocking & Parallel Logic
---


# How to set up Overclocking and Parallel Logic

To improve its generality, RecipeLogic has been rewritten in GTCEu Modern to support inputs and outputs other than
EU, items and fluids.

The new `RecipeLogic` no longer handles overclocked and parallel logic, but instead delegates it to the
machine's `RecipeModifier`s, which are set on the machine definition with `.recipeModifiers(...)`:

```java
/**
 * Used to modify a recipe on the fly e.g. applying overclock,
 * change chance, etc
 *
 * @param machine the machine that is running the recipe
 * @param recipe  recipe from detected from GTRecipeType
 * @return a ModifierFunction that will be applied to the recipe.
 *         ModifierFunction.NULL -- this recipe is unavailable
 */
ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe);
```


## Electric Overclocking

In general, a simple electric overclocking can be done this way. For details, see `OverclockingLogic` and `GTRecipeModifiers`

```java
public static ModifierFunction overclock(MetaMachine machine, GTRecipe recipe) {
    if (!(machine instanceof IOverclockMachine overclockMachine)) return ModifierFunction.IDENTITY;
    if (RecipeHelper.getRecipeEUtTier(recipe) > overclockMachine.getMaxOverclockTier()) {
        return ModifierFunction.NULL;
    }

    return OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(
        machine,
        recipe,
        overclockMachine.getOverclockVoltage()
    );
}
```

The common overclocking modifiers are already available in `GTRecipeModifiers`, e.g. `GTRecipeModifiers.OC_NON_PERFECT`.


## Parallel Logic

Parallel is also not complicated to implement. Let's take the `generator` as an example

```java
public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
    if (!(machine instanceof SimpleGeneratorMachine generator)) {
        return RecipeModifier.nullWrongType(SimpleGeneratorMachine.class, machine);
    }
    long EUt = recipe.getOutputEUt().getTotalEU(); // get the recipe's EU/t
    if (EUt <= 0) return ModifierFunction.NULL;

    // calculate the max parallel limitation.
    int maxParallel = (int) (generator.getOverclockVoltage() / EUt);
    // check how many parallels the machine actually has enough ingredients for.
    int parallels = ParallelLogic.getParallelAmountFast(generator, recipe, maxParallel);

    // multiply the recipe's contents and EU/t by the amount of parallels.
    return ModifierFunction.builder()
            .inputModifier(ContentModifier.multiplier(parallels))
            .outputModifier(ContentModifier.multiplier(parallels))
            .eutMultiplier(parallels)
            .parallels(parallels)
            .build();
}
```
