---
title: "Recipe Execution"
---

## Recipe Execution
After the recipe is found, we run `RecipeLogic.setupRecipe(recipe)`.
This method runs `machine.beforeWorking(recipe)`. If that fails, it resets and goes back to recipe searching.
If it doesn't, it continues and actually consumes the contents of the recipe using `handleRecipeIO(recipe, IO.IN)`.

- This calls `RecipeHelper.handleRecipeIO(machine, recipe, io, this.chanceCaches)`,
- Which calls `handleRecipe(holder, recipe, io, io == IO.IN ? recipe.inputs : recipe.outputs, chanceCaches, false, false)`.
- Which calls `RecipeRunner runner = new RecipeRunner(recipe, io, isTick, holder, chanceCaches, simulated); var result = runner.handle(contents)`

This `runner.handle(contents)` call is where the actual recipe consumption happens.
First, it calls `runner.fillContentMatchList(contents)`, which populates two values:

- `this.searchRecipeContents`, which contains all the recipe contents including every ingredient that has a chance to be consumed
- `this.recipeContents`, which contains the recipe contents with chances actually evaluated. So if your chance ingredient didn't pass the chance roll, it will not be in here.

then, it calls `return this.handleContents()`, which will use these two lists to actually check and/or consume the recipe contents.


## Needed Concepts
Before we dive in, we need to understand some concepts.
A `RecipeHandler` is the lowest level abstraction of I/O. It's for example the circuit slot in an input bus, it's the energy buffer of an energy hatch, etc.
A `RecipeHandlerList` is an abstraction around multiple RecipeHandlers, for example a dual input bus holds 3 `RecipeHandler`s:

 - One for the circuit slot
 - One for the item slots
 - One for the fluid slots

These are packaged in one `RecipeHandlerList`, which is what our `handleContents` method interacts with. When you call `RHL.handleRecipe`, that calls `.handleRecipe` on all their internal `RecipeHandler`s. 

A `RecipeHandlerList` can also have two other properties of interest to us:

 - `boolean isDistinct`, says if the bus is set to distinct or not
 - `long color`, says which color the bus is colored in-game

This dictates what `RecipeHandlerGroup` a `RecipeHandlerList` is put in:

 - If the `RecipeHandlerList` has a `RecipeHandler` whose `RecipeCapability` has `.shouldBypassDistinct()` return true, it gets put in the `BYPASS_DISTINCT` group. These busses (for example Energy Hatches) are global and should interact with every combination, regardless of distinctness, color, etc.
 - If the `RecipeHandlerList` is set to distinct, it will get put in the `BUS_DISTINCT` group.
 - If the `RecipeHandlerList` is undyed, it gets put in the `UNDYED` group
 - Otherwise, the `RecipeHandlerList` gets put in a group specific to its color.

It's important to note that, during grouping, UNDYED groups get added to every other color group as well. This is because our recipe handling logic works the following way:
1. If it's `BYPASS_DISTINCT`, it should be included in every recipe check
2. If it's `BUS_DISTINCT`, it should only check with itself (and any `BYPASS_DISTINCT`)
3. If it's a color, it will only check with its own color + the `UNDYED` busses + the `BYPASS_DISTINCT` busses.
4. If it's `UNDYED`, it will check with other `UNDYED` busses.

So UNDYED acts as a wildcard for other dyed groups.

During recipe handling, the `RecipeHandlerList`s get split up in groups during `RecipeHelper.addToRecipeHandlerMap`. This is also where the `UNDYED` wildcard gets take care of, as that will get added to the other color groups.

## RecipeRunner.handleContents
This is a very big function, so we will go through it in steps:
```java title="RecipeRunner.java"
private ActionResult handleContents() {
    if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
    if (!capabilityProxies.containsKey(io)) {
        return ActionResult.FAIL_NO_CAPABILITIES;
    }

    List<RecipeHandlerList> handlers = capabilityProxies.getOrDefault(io, Collections.emptyList());
    // Only sort for non-tick outputs
    if (!isTick && io.support(IO.OUT)) {
        handlers.sort(RecipeHandlerList.COMPARATOR.reversed());
    }

    Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
    for (var handler : handlers) {
        addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
    }
```

This  takes care of fetching the `RecipeHandlerList`s of a machine, and dividing them up into groups
we take care of the grouping `RecipeHandlerList`s by their respective group, and dividing them up.

```java title="RecipeRunner.java"
    // Specifically check distinct handlers first
    for (RecipeHandlerList handler : handlerGroups.getOrDefault(BUS_DISTINCT, Collections.emptyList())) {
        // Handle the contents of this handler and also all the bypassed handlers
        var res = handler.handleRecipe(io, recipe, searchRecipeContents, true);
        if (!res.isEmpty()) {
            for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                    Collections.emptyList())) {
                res = bypassHandler.handleRecipe(io, recipe, res, true);
                if (res.isEmpty()) break;
            }
        }
        if (res.isEmpty()) {
            if (!simulated) {
                // Actually consume the contents of this handler and also all the bypassed handlers
                recipeContents = handler.handleRecipe(io, recipe, recipeContents, false);
                if (!recipeContents.isEmpty()) {
                    for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                            Collections.emptyList())) {
                        recipeContents = bypassHandler.handleRecipe(io, recipe, recipeContents, false);
                        if (recipeContents.isEmpty()) break;
                    }
                }
            }
            recipeContents.clear();
            return ActionResult.SUCCESS;
        }
    }
```
This is the first block of logic that actually checks and consumes.
Do note the last argument of `handleRecipe` is the `simulate` argument. `true` doesn't actually consume the items, `false` does consume the items.
So we start off by looping through each `BUS_DISTINCT` `RecipeHandlerList`, and making it check the recipe.
Then, if the list of remaining items is not empty yet, we check the `RecipeHandlerList`s that are in the `BYPASS_DISTINCT` group.
If it is empty, then the simulation is a success. If `simulated` is false, we can run the code again, this time actually consuming the recipe contents. 
If it's not empty, we continue to the next distinct bus.

```java title="RecipeRunner.java"
    // Check the other groups.
    for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> handlerListEntry : handlerGroups.entrySet()) {
        if (handlerListEntry.getKey().equals(BUS_DISTINCT)) continue;

        // List to keep track of the remaining items for this RecipeHandlerGroup
        Map<RecipeCapability<?>, List<Object>> copiedRecipeContents = searchRecipeContent;
        boolean found = false;

        for (RecipeHandlerList handler : handlerListEntry.getValue()) {
            copiedRecipeContents = handler.handleRecipe(io, recipe, copiedRecipeContents, true);
            if (copiedRecipeContents.isEmpty()) {
                found = true;
                break;
            }
        }
        // If we're already in the bypass_distinct group, don't check it twice.
        if (!handlerListEntry.getKey().equals(BYPASS_DISTINCT)) {
            for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                    Collections.emptyList())) {
                copiedRecipeContents = bypassHandler.handleRecipe(io, recipe, copiedRecipeContents, true);
                if (copiedRecipeContents.isEmpty()) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) continue;
        if (simulated) return ActionResult.SUCCESS;
        // Start actually removing items.
        // Keep track of the remaining items for this RecipeHandlerGroup
        // First go through the handlers of the group
        for (RecipeHandlerList handler : handlerListEntry.getValue()) {
            recipeContents = handler.handleRecipe(io, recipe, recipeContents, false);
            if (recipeContents.isEmpty()) {
                return ActionResult.SUCCESS;
            }
        }
        // Then go through the handlers that bypass the distinctness system and empty those
        // If we're already in the bypass_distinct group, don't check it twice.
        if (!handlerListEntry.getKey().equals(BYPASS_DISTINCT)) {
            for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                    Collections.emptyList())) {
                recipeContents = bypassHandler.handleRecipe(io, recipe, recipeContents, false);
                if (recipeContents.isEmpty()) {
                    return ActionResult.SUCCESS;
                }
            }
        }
    }

    for (var entry : recipeContents.entrySet()) {
        if (entry.getValue() != null && !entry.getValue().isEmpty()) {
            return ActionResult.fail(null, entry.getKey(), io);
        }
    }

    return ActionResult.FAIL_NO_REASON;
}
```

This is the same loop of logic as we had before, but a little more complicated. 
Instead of checking each `RecipeHandlerList` individually like we did for `BUS_DISTINCT`, we check all the busses in each group at once. 
There's also the caveat of `BYPASS_DISTINCT` having to be checked with every group, but not with itself, so there's additional logic in place for that.
The rest is relatively the same.

This is how the logic for RecipeHelper.handleRecipeIO works. The same can be said for tick ingredients, except the tick inputs are passed in, as well as the sorting step in the start. The rest remains the same.

## The rest of RecipeLogic
To recap, this is what happens during `RecipeLogic.setupRecipe(recipe)` after the recipe is found.
Then, if that returns a success, a bunch of RecipeLogic related variables are set, like:
```java title="RecipeLogic.java"
    var handledIO = handleRecipeIO(recipe, IO.IN);
    if (handledIO.isSuccess()) {
        recipeDirty = false;
        lastRecipe = recipe;
        setStatus(Status.WORKING);
        progress = 0;
        duration = recipe.duration;
        isActive = true;
    }
```
This is all that happens in this tick (see [RecipeLogic.serverTick](./Recipe-Logic.md)).
Then, on the next tick, we call `recipeLogic.handleRecipeWorking()`, which calls the following:
```java title="RecipeLogic.java"
public ActionResult handleTickRecipe(GTRecipe recipe) {
    if (!recipe.hasTick()) return ActionResult.SUCCESS;

    var result = RecipeHelper.matchTickRecipe(machine, recipe);
    if (!result.isSuccess()) return result;

    result = handleTickRecipeIO(recipe, IO.IN);
    if (!result.isSuccess()) return result;

    result = handleTickRecipeIO(recipe, IO.OUT);
    return result;
}
```
Where `handleTickRecipeIO` calls `handleRecipe` with the `recipe.tickInputs` / `recipe.tickOutputs` and `tick=true`.

Then, if the `recipeLogic.progress >= recipeLogic.duration`, it calls `onRecipeFinish()`
This calls `machine.afterWorking()` and `handleRecipeIO(lastRecipe, IO.OUT)`, which outputs the recipe's output into the output busses.
