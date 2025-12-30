---
title: "Recipe Searching"
---

# Recipe Searching
!!! Note
    This is not an in-depth exploration of Recipe Searching, but rather a general overview.
    A lot of code will be referenced, redacted and simplified. 

For recipe searching happens in 3 phases. 

1. Creation of the recipe  
2. Recipe Lookup  
3. Recipe Matching  

This document will go through them step by step

## Recipe creation
To create a recipe, one must first create a `RecipeType`. A `RecipeType` contains metadata about the recipe, but more importantly for this, a `RecipeLookup`. This is where the recipes are stored.  

This lookup is effectively a Trie. It holds a Branch, and every branch has a `Map<AbstractMapIngredient, Either<GTRecipe, Branch>>`. 
When you add the recipe to the lookup, you effectively add the ingredients one by one until you arrive at the recipe.  

So a simplified assembler(8) trie could be 
```
   Map { "8 cobblestone" -> Left(FurnaceRecipe),
         "4 iron rods" -> Right(Map {
               "4 iron plates" -> Left(Iron Machine Hull)
               })
        } 
```
## Recipe Lookup
During the `RecipeLogic.serverTick` method (see [Recipe Logic](./Recipe-Logic.md)), a method called `findAndHandleRecipe` is called.
This checks if the `lastRecipe` is set, and if it can be matched, runs it again.
If it can't be matched, it calls `handleSearchingRecipes(searchRecipe())`

`searchRecipe()` is the actual recipe searching logic. In big lines, it finds what ingredients are currently available in the machine, groups those, and traverses the `RecipeLookup` to create an iterator of recipes that are available.  
`handleSearchingRecipes()` then goes through that iterator, and for every recipe it runs the recipe modifier, checks if this machine can run it, if the inputs are there, if it has enough output space etc.  

The actual code for searchRecipe is:
```java
    return machine.getRecipeType().searchRecipe(machine, r -> matchRecipe(r).isSuccess())
```
`RecipeType.searchRecipe(...)` effectively calls `getLookup().getRecipeIterator(holder, canHandle)`.  
This canHandle function will be further explored in the Recipe Matching stage.  

This function first generates a list of ingredients from the machine via `.fromHolder`, which calls `holder.getCapabilitiesFlat(IO.IN)`. It then takes the list of `IRecipeHandlers`, calls `.getContents` on each, and converts all those contents to Ingredients.
After that, it passes the list of ingredients to a new RecipeIterator, which is a wrapper around `recurseIngredientTreeFindRecipe(ingredients, recipeMap.getLookup().getLookup(), canHandle,
/***/)`. 

With this, we have arrived at the function that actually does the searching of the recipe.
Before we can dive into the actual function, we need to understand how Ingredients work.

## From Machine to Ingredient
A trait is a type of object that gets stored on the machine on creation, e.g. 
```java
    public MachineTrait(MetaMachine machine) {
        // ...
        machine.attachTraits(this);
    }
```
A machine can have many different traits. One of these is `IRecipeHandler`. This trait is used when collecting the inputs/outputs for `RecipeLogic`. 
`IRecipeHandler`s are abstractions around for example the item slots and circuit slot of an input bus, or the energy buffer of a singleblock.

An example of one of these would be an `new NotifiableItemStackHandler(machine, slots, IO)`. On creation, it attaches itself to the machine, so you don't have to link it to the `RecipeLogic` in any way. The WorkableMachine takes care of that.  

This NotifiableHandler has a few important methods:

 - a `List<Ingredient> getContents()` method, which is where the ingredient list is retrieved for recipe search
 - a `List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate)` method, which is where the I/O magic of the handler happens
    - `IO io`:  whether the recipe is trying to input or output into the world (e.g. IO.IN takes stuff from the handler, IO.OUT puts stuff in the handler)
    - `GTRecipe recipe`: the recipe that's being run
    - `List<Ingredient> left`: the remaining items to put into / take out of the handler
    - `boolean simulate`: whether this is a simulate run (for e.g. recipe checking) or to actually modify the contents of the handler
    - returns `List<Ingredient>` a list of remaining ingredients, aka what could not be inserted / extracted. Note that if no contents are left, `null` should be returned.

In this case it's an `NotifiableRecipeHandlerTrait<Ingredient>` (where `Ingredient` is a wrapper around `ItemStack` to help account for ranged inputs/outputs), but a recipe handler can take anything.

To actually store this `Ingredient`, we need something that can be handled properly by our RecipeLookup. To do this, we wrap the `Ingredient` in another object, called an `ItemStackMapIngredient`. 
This class extends AbstractMapIngredient, which most importantly has a correct `.hash()` and `.equals()` function.  
These are used by our GTRecipeLookup to find the correct ingredients inside our Trie. 

!!! Note 
    while two objects that should match should also have return true on .equals check and have the same hash code, `.equals` does NOT have to be a correct `.equals` implementation, for example for matching of partial NBT:  
    (the following code is psuedocode)  
    ```
    PartialNBTItemStackMapIngredient(Iron, {foo: bar, bar: baz}).equals(PartialNBTItemStackMapIngredient(Iron, {foo: bar}) = True"
    ```

!!! Note
    It's important to note that even if two functions match with `.equals`, if they don't also have the same hash they won't be matched in our recipe search.

## GTRecipeLookup.recurseIngredientTreeFindRecipe
```java
  /**
     * Recursively finds a recipe
     *
     * @param ingredients the ingredients part
     * @param branchMap   the current branch of the tree
     * @param canHandle   predicate to test found recipe.
     * @param index       the index of the wrapper to get
     * @param count       how deep we are in recursion, < ingredients.length
     * @param skip        bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @return a recipe
     */
    @Nullable
    public GTRecipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                    @NotNull Branch branchMap, @NotNull Predicate<GTRecipe> canHandle,
                                                    int index, int count, BitSet skip) {
        // exhausted all the ingredients, and didn't find anything
        if (count == ingredients.size()) return null;

        // Iterate over current level of nodes.
        for (AbstractMapIngredient obj : ingredients.get(index)) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            Either<GTRecipe, Branch> result = targetMap.get(obj);
            if (result != null) {
                // if there is a recipe (left mapping), return it immediately as found, if it can be handled
                // Otherwise, recurse and go to the next branch.
                GTRecipe r = result.map(potentialRecipe -> canHandle.test(potentialRecipe) ? potentialRecipe : null,
                        potentialBranch -> diveIngredientTreeFindRecipe(ingredients, potentialBranch, canHandle, index,
                                count, skip));
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }
```

When our GTRecipeLookup scans the machine for inputs (via the handlers' `.getContents()` methods), it then converts these to MapIngredients.
This is the `List<List<AbstractMapIngredient>>` input. The reason it's a double list is that one `Content` can be turned into multiple `Ingredient`s. e.g. one written book can become: a `ItemStackMapIngredient`, an `ItemTagMapIngredient`, a `PartialNBTItemStackMapIngredient`, a `StrictNBTItemStackMapIngredient`, and potentially others based on addons / registration.
So for every content from `.getContents()`, a `List<AbstractMapIngredient>` gets made. See this excerpt from the `.fromHolder` method: 
```java
var compressed = cap.compressIngredients(handler.getContents());
for (var ingredient : compressed) {
    list.add(MapIngredientTypeManager.getFrom(ingredient, cap));
}
```

This list is then iterated, in our for loop in the method above. For every `AbstractMapIngredient`, `branchMap.getNodes()` is called in `determineRootNodes`.  

Then, in this map, we call `map.get(obj)`. Since this is implemented as a `HashMap`, this is where our `AbstractMapIngredient`'s methods come in. According to the `.hash` and `.equals` methods, this matches the ingredients for that layer of the Trie.  
When this get call is made, it can return `null` (if that ingredient isn't in the map), or it can return an `Either<GTRecipe, Branch>`.   
In the case it is a recipe, we check if it works with our current machine (see canHandle later), and if so we return this recipe.
In the case it is a branch, we recurse down into the next layer of our recipe search.

## Recipe Matching
In this entire call stack, a canHandle predicate is passed down. This predicate, being `r -> matchRecipe(r).isSuccess()` as you might recall from earlier, is a way to check if the machine can handle the current recipe.
After a few layers of indirection, we arrive at `RecipeHelper.matchRecipe`
```java
    private static ActionResult matchRecipe(IRecipeCapabilityHolder holder, GTRecipe recipe, boolean tick) {
        if (!holder.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;

        var result = handleRecipe(holder, recipe, IO.IN, tick ? recipe.tickInputs : recipe.inputs,
                Collections.emptyMap(), tick, true);
        if (!result.isSuccess()) return result;

        result = handleRecipe(holder, recipe, IO.OUT, tick ? recipe.tickOutputs : recipe.outputs,
                Collections.emptyMap(), tick, true);
        return result;
    }
```
As you can see, this calls handleRecipe for both the normal and tick inputs, with `simulate = true`. Do note that at this stage, RecipeModifiers have not been applied yet.

With the info on how the `Iterator<GTRecipe>` is created by `searchRecipes()` above, we can now dive into `handleSearchingRecipes(Iterator<GTRecipe>)`.
This method loops through the iterator, and calls `checkMatchedRecipeAvailable` on it.

```java
    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
    var modified = machine.fullModifyRecipe(match);
    if (modified != null) {
        var recipeMatch = checkRecipe(modified);
        if (recipeMatch.isSuccess()) {
            setupRecipe(modified);
        }
        if (lastRecipe != null && getStatus() == Status.WORKING) {
            lastOriginRecipe = match;
            lastFailedMatches = null;
            return true;
        }
    }
    return false;
}
```

This is where Recipe Modifiers are applied. If any of the RecipeModifiers return null, this recipe is ignored and we will continue on the next recipe in the iterator.  
If it isn't null, we validate that the inputs are again available (via an indirection to `RecipeHelper.matchRecipe` as seen above).  
If it is, we call `.setupRecipe(...)`. This setupRecipe call will call `machine.beforeWorking()` and (try to) consume the input items.
If after this, the recipe is running, we return true and have found a recipe. Our recipe search is over.


