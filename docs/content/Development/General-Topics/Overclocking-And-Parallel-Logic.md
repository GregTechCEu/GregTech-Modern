---
title: Overclocking & Parallel Logic
---


# How to set up Overclocking and Parallel Logic

To improve its generality, RecipeLogic has been rewritten in GTCEu Modern to support inputs and outputs other than
EU, items and fluids.

The new `RecipeLogic` no longer handles overclocked and parallel logic, but instead delegates it to the
machine via `IRecipeLogicMachine`:

```java
/**
 * Override it to modify recipe on the fly e.g. applying overclock,
 * change chance, etc
 * 
 * @param recipe recipe from detected from GTRecipeType
 * @return modified recipe.
 *         null -- this recipe is unavailable
 */
@Nullable
GTRecipe modifyRecipe(GTRecipe recipe);
```


## Electric Overclocking

In general, a simple electric overclocking can be done this way. For details, see `OverclockingLogic` and `RecipeHelper`

```java
public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
    if (RecipeHelper.getRecipeEUtTier(recipe) > getTier()) {
        return null;
    }
    
    return RecipeHelper.applyOverclock(
        getDefinition().getOverclockingLogic(), 
        recipe,
        getMaxVoltage()
    );
}
```


## Parallel Logic

Parallel is also not complicated to implement. Let's take the `generator` as an example

```java
public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
    var EUt = RecipeHelper.getOutputEUt(recipe); // get the recipe's EU/t
        
    if (EUt > 0) {
        // calculate the max parallel limitation.
        var maxParallel = (int) (Math.min(
            energyContainer.getOutputVoltage(),
            GTValues.V[overclockTier]
        ) / EUt);
        
        while (maxParallel > 0) {
            // copy and apply parallel, it will affect all recipes' contents
            // and the recipe duration.
            var copied = recipe.copy(ContentModifier.multiplier(maxParallel));

            // If the machine has enough ingredients, return copied recipe.
            if (copied.matchRecipe(this)) {
                copied.duration = copied.duration / maxParallel;
        
                return copied;
            }
            
            // Trying to halve the number of parallelism
            maxParallel /= 2;
        }
    }
    return null;
}
```
