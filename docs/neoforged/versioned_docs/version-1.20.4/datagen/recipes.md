# Recipe Generation

Recipes can be generated for a mod by subclassing `RecipeProvider` and implementing `#buildRecipes`. A recipe is supplied for data generation once a `FinishedRecipe` view is accepted by the consumer. `FinishedRecipe`s can either be created and supplied manually or, for convenience, created using a `RecipeBuilder`.

After implementation, the provider must be [added][datagen] to the `DataGenerator`.

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    event.getGenerator().addProvider(
        // Tell generator to run only when server data are generating
        event.includeServer(),
        MyRecipeProvider::new
    );
}
```

## `RecipeBuilder`

`RecipeBuilder` is a convenience implementation for creating `FinishedRecipe`s to generate. It provides basic definitions for unlocking, grouping, saving, and getting the result of a recipe. This is done through `#unlockedBy`, `#group`, `#save`, and `#getResult` respectively.

:::important
[`ItemStack` outputs][stack] in recipes are not supported within vanilla recipe builders. A `FinishedRecipe` must be built in a different manner for existing vanilla recipe serializers to generate this data.
:::

:::warning
The item results being generated must have a valid `RecipeCategory` specified; otherwise, a `NullPointerException` will be thrown.
:::

All recipe builders except for [`SpecialRecipeBuilder`] require an advancement criteria to be specified. All recipes generate a criteria unlocking the recipe if the player has used the recipe previously. However, an additional criteria must be specified that allows the player to obtain the recipe without any prior knowledge. If any of the criteria specified is true, then the played will obtain the recipe for the recipe book.

:::tip
Recipe criteria commonly use `InventoryChangeTrigger` to unlock their recipe when certain items are present in the user's inventory.
:::

### ShapedRecipeBuilder

`ShapedRecipeBuilder` is used to generate shaped recipes. The builder can be initialized via `#shaped`. The recipe group, input symbol pattern, symbol definition of ingredients, and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(writer)
ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
  .pattern("a a") // Create recipe pattern
  .define('a', item) // Define what the symbol represents
  .unlockedBy("criteria", criteria) // How the recipe is unlocked
  .save(writer); // Add data to builder
```

#### Additional Validation Checks

Shaped recipes have some additional validation checks performed before building:

- A pattern must be defined and take in more than one item.
- All pattern rows must be the same width.
- A symbol cannot be defined more than once.
- The space character (`' '`) is reserved for representing no item in a slot and, as such, cannot be defined.
- A pattern must use all symbols defined by the user.

### ShapelessRecipeBuilder

`ShapelessRecipeBuilder` is used to generate shapeless recipes. The builder can be initialized via `#shapeless`. The recipe group, input ingredients, and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(writer)
ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
  .requires(item) // Add item to the recipe
  .unlockedBy("criteria", criteria) // How the recipe is unlocked
  .save(writer); // Add data to builder
```

### SimpleCookingRecipeBuilder

`SimpleCookingRecipeBuilder` is used to generate smelting, blasting, smoking, and campfire cooking recipes. Additionally, custom cooking recipes using the `SimpleCookingSerializer` can also be data generated using this builder. The builder can be initialized via `#smelting`, `#blasting`, `#smoking`, `#campfireCooking`, or `#cooking` respectively. The recipe group and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(writer)
SimpleCookingRecipeBuilder builder = SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, result, experience, cookingTime)
  .unlockedBy("criteria", criteria) // How the recipe is unlocked 
  .save(writer); // Add data to builder
```

### SingleItemRecipeBuilder

`SingleItemRecipeBuilder` is used to generate stonecutting recipes. Additionally, custom single item recipes using a serializer like SingleItemRecipe.Serializer can also be data generated using this builder. The builder can be initialized via `#stonecutting` or through the constructor respectively. The recipe group and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(writer)
SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(input, RecipeCategory.MISC, result)
  .unlockedBy("criteria", criteria) // How the recipe is unlocked
  .save(writer); // Add data to builder
```

## Non-`RecipeBuilder` Builders

Some recipe builders do not implement `RecipeBuilder` due to lacking features used by all previously mentioned recipes.

### SmithingTransformRecipeBuilder

`SmithingTransformRecipeBuilder` is used to generate smithing recipes which transform an item. Additionally, custom recipes using a serializer like SmithingTransformRecipe.Serializer can also be data generated using this builder. The builder can be initialized via `#smithing` or through the constructor respectively. The recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(writer)
SmithingTransformRecipeBuilder builder = SmithingTransformRecipeBuilder.smithing(template, base, addition, RecipeCategory.MISC, result)
  .unlocks("criteria", criteria) // How the recipe is unlocked
  .save(writer, name); // Add data to builder
```

### SmithingTrimRecipeBuilder

`SmithingTrimRecipeBuilder` is used to generate smithing recipes for armor trims. Additionally, custom upgrade recipes using a serializer like SmithingTrimRecipe.Serializer can also be data generated using this builder. The builder can be initialized via `#smithingTrim` or through the constructor respectively. The recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(writer)
SmithingTrimRecipe builder = SmithingTrimRecipe.smithingTrim(template, base, addition, RecipeCategory.MISC)
  .unlocks("criteria", criteria) // How the recipe is unlocked
  .save(writer, name); // Add data to builder
```

### SpecialRecipeBuilder

`SpecialRecipeBuilder` is used to generate empty JSONs for dynamic recipes that cannot easily be constrained to the recipe JSON format (dying armor, firework, etc.). The builder can be initialized via `#special`.

```java
// In RecipeProvider#buildRecipes(writer)
SpecialRecipeBuilder.special(dynamicRecipeSerializer)
  .save(writer, name); // Add data to builder
```

## Conditional Recipes

[Conditional recipes][conditional] can also be data generated via ConditionalRecipe.Builder. The builder can be obtained using `#builder`.

Conditions for each recipe can be specified by first calling `#addCondition` and then calling `#addRecipe` after all conditions have been specified. This process can be repeated as many times as the programmer would like.

After all recipes have been specified, advancements can be added for each recipe at the end using `#generateAdvancement`. Alternatively, the conditional advancement can be set using `#setAdvancement`.

```java
// In RecipeProvider#buildRecipes(writer)
ConditionalRecipe.builder()
  // Add the conditions for the recipe
  .addCondition(...)
  // Add recipe to return when conditions are true
  .addRecipe(...)

  // Add the next conditions for the next recipe
  .addCondition(...)
  // Add next recipe to return when the next conditions are true
  .addRecipe(...)

  // Create conditional advancement which uses the conditions
  // and unlocking advancement in the recipes above
  .generateAdvancement()
  .build(writer, name);
```

### IConditionBuilder

To simplify adding conditions to conditional recipes without having to construct the instances of each condition instance manually, the extended `RecipeProvider` can implement `IConditionBuilder`. The interface adds methods to easily construct condition instances.

```java
// In ConditionalRecipe$Builder#addCondition
(
  // If either 'examplemod:example_item'
  // OR 'examplemod:example_item2' exists
  // AND
  // NOT FALSE

  // Methods are defined by IConditionBuilder
  and( 
    or(
      itemExists("examplemod", "example_item"),
      itemExists("examplemod", "example_item2")
    ),
    not(
      FALSE()
    )
  )
)
```

## Custom Recipe Serializers

Custom recipe serializers can be data generated by creating a builder that can construct a `FinishedRecipe`. The finished recipe encodes the recipe data and its unlocking advancement, when present, to JSON. Additionally, the name and serializer of the recipe is also specified to know where to write to and what can decode the object when loading. Once a `FinishedRecipe` is constructed, it simply needs to be passed to the `Consumer` supplied by `RecipeProvider#buildRecipes`.

:::tip
`FinishedRecipe`s are flexible enough that any object transformation can be data generated, not just items.
:::

[datagen]: ../resources/index.md#data-generation
[ingredients]: ../resources/server/recipes/ingredients.md#neoforge-types
[stack]: ../resources/server/recipes/index.md#recipe-itemstack-result
[conditional]: ../resources/server/conditional.md
[special]: #specialrecipebuilder
