# Recipes

Recipes are a way to transform some number of objects into other objects within a Minecraft world. Although the vanilla system deals purely with item transformations, the system as a whole can be expanded to use any object the programmer creates.

## Data-Driven Recipes

Most recipe implementations within vanilla are data driven via JSON. This means that a mod is not necessary to create a new recipe, only a [Data pack][datapack]. A full list on how to create and put these recipes within the mod's `resources` folder can be found on the [Minecraft Wiki][wiki].

A recipe can be obtained within the Recipe Book as a reward for completing an [advancement][advancement]. Recipe advancements always have `minecraft:recipes/root` as their parent, to not to appear on the advancement screen. The default criteria to gain the recipe advancement is a check if the user has unlocked the recipe from using it once or receiving it through a command like `/recipe`:

```js
// Within some recipe advancement json
"has_the_recipe": { // Criteria label
  // Succeeds if examplemod:example_recipe is used
  "trigger": "minecraft:recipe_unlocked",
  "conditions": {
    "recipe": "examplemod:example_recipe"
  }
}
//...
"requirements": [
  [
    "has_the_recipe"
    // ... Other criteria labels to be ORed against to unlock recipe
  ]
]
```

Data-driven recipes and their unlocking advancement can be [generated][datagen] via `RecipeProvider`.

## Recipe Manager

Recipes are loaded and stored via the `RecipeManager`. Any operations relating to getting available recipe(s) are handled by this manager. There are two important methods to know of:

 Method         | Description
 :---:          | :---
`getRecipeFor`  | Gets the first recipe that matches the current input.
`getRecipesFor` | Gets all recipes that match the current input.

Each method takes in a `RecipeType`, which denotes what method is being applied to use the recipe (crafting, smelting, etc.), a `Container` which holds the configuration of the inputs, and the current level which is passed to `Recipe#matches` along with the container.

:::tip
NeoForge provides the `RecipeWrapper` utility class which extends `Container` for wrapping around `IItemHandler`s and passing them to methods which requires a `Container` parameter.

```java
// Within some method with IItemHandlerModifiable handler
recipeManger.getRecipeFor(RecipeType.CRAFTING, new RecipeWrapper(handler), level);
```
:::

## Additional Features

NeoForge provides some additional behavior to the recipe schema and its implementations for greater control of the system.

### Recipe ItemStack Result

Except for `minecraft:stonecutting` recipes, all vanilla recipe serializers expand the `result` tag to take in a full `ItemStack` as a `JsonObject` instead of just the item name and amount in some cases.

```js
// In some recipe JSON
"result": {
  // The name of the registry item to give as a result
  "item": "examplemod:example_item",
  // The number of items to return
  "count": 4,
  // The tag data of the stack, can also be a string
  "nbt": {
      // Add tag data here
  }
}
```

:::note
The `nbt` tag can alternatively be a string containing a stringified NBT (or SNBT) for data which cannot be properly represented as a JSON object (such as `IntArrayTag`s).
:::

### Conditional Recipes

Recipes and their unlocking advancement can be [loaded conditionally and defaulted][conditional] depending on what information is present (mod loaded, item exists, etc.).

### Larger Crafting Grids

By default, vanilla declares a maximum width and height for a crafting grid to be a 3x3 square. This can be expanded by calling `ShapedRecipe#setCraftingSize` with the new width and height in `FMLCommonSetupEvent`.

:::caution
`ShapedRecipe#setCraftingSize` is **NOT** thread-safe. As such, it should be enqueued to the synchronous work queue via `FMLCommonSetupEvent#enqueueWork`.
:::

Larger crafting grids in recipes can be [data generated][datagen].

### Ingredient Types

A few additional [ingredient types][ingredients] are added to allow recipes to have inputs which check tag data or combine multiple ingredients into a single input checker.

[datapack]: https://minecraft.wiki/w/Data_pack
[wiki]: https://minecraft.wiki/w/Recipe
[advancement]: ../advancements.md
[datagen]: ../../../datagen/recipes.md
[conditional]: ../conditional.md#implementations
[ingredients]: ./ingredients.md#neoforge-types
