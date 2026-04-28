# Recipe Generation

Recipes can be generated for a mod by subclassing `RecipeProvider` and implementing `#buildRecipes`. A recipe is supplied for data generation once a `Recipe` object is provided to the `RecipeOutput`. `Recipe`s can either be created and supplied manually or, for convenience, created using a `RecipeBuilder`.

After implementation, the provider must be [added][datagen] to the `DataGenerator`.

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    event.getGenerator().addProvider(
        // Tell generator to run only when server data are generating
        event.includeServer(),
        output -> new MyRecipeProvider(output, event.getLookupProvider())
    );
}
```

## `RecipeBuilder`

`RecipeBuilder` is an interface meant to be implemented to create `Recipe`s for generation. It provides basic definitions for unlocking, grouping, saving, and getting the result of a recipe. This is done through `#unlockedBy`, `#group`, `#save`, and `#getResult` respectively.

:::note
Although [`ItemStack` outputs][stack] in recipes are not supported within vanilla recipe builders, the `#save` methods can be implemented to do so. The only purpose of `#getResult` is to determine the name of the recipe when none is provided.
:::

:::warning
For vanilla `RecipeBuilder`s, the item results being generated must have a valid `RecipeCategory` specified; otherwise, a `NullPointerException` will be thrown depending on its usage.
:::

All recipe builders except for [`SpecialRecipeBuilder`] require an advancement criteria to be specified. All recipes generate a criteria unlocking the recipe if the player has used the recipe previously. However, an additional criteria must be specified that allows the player to obtain the recipe without any prior knowledge. If any of the criteria specified is true, then the played will obtain the recipe for the recipe book.

:::tip
Recipe criteria commonly use `InventoryChangeTrigger` to unlock their recipe when certain items are present in the user's inventory.
:::

### ShapedRecipeBuilder

`ShapedRecipeBuilder` is used to generate shaped recipes. The builder can be initialized via `#shaped`. The recipe group, input symbol pattern, symbol definition of ingredients, and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(output)
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
    .pattern("a a") // Create recipe pattern
    .define('a', item) // Define what the symbol represents
    .unlockedBy("criteria", criteria) // How the recipe is unlocked
    .save(output); // Add data to builder
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
// In RecipeProvider#buildRecipes(output)
ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
    .requires(item) // Add item to the recipe
    .unlockedBy("criteria", criteria) // How the recipe is unlocked
    .save(output); // Add data to builder
```

### SimpleCookingRecipeBuilder

`SimpleCookingRecipeBuilder` is used to generate smelting, blasting, smoking, and campfire cooking recipes. Additionally, custom cooking recipes using the `SimpleCookingSerializer` can also be data generated using this builder. The builder can be initialized via `#smelting`, `#blasting`, `#smoking`, `#campfireCooking`, or `#generic` respectively. The recipe group and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(output)
SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, result, experience, cookingTime)
    .unlockedBy("criteria", criteria) // How the recipe is unlocked 
    .save(output); // Add data to builder
```

### SingleItemRecipeBuilder

`SingleItemRecipeBuilder` is used to generate stonecutting recipes. Additionally, custom single item recipes using a serializer like SingleItemRecipe.Serializer can also be data generated using this builder. The builder can be initialized via `#stonecutting` or through the constructor respectively. The recipe group and the recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(output)
SingleItemRecipeBuilder.stonecutting(input, RecipeCategory.MISC, result)
    .unlockedBy("criteria", criteria) // How the recipe is unlocked
    .save(output); // Add data to builder
```

## Non-`RecipeBuilder` Builders

Some recipe builders do not implement `RecipeBuilder` due to lacking features used by all previously mentioned recipes.

### SmithingTransformRecipeBuilder

`SmithingTransformRecipeBuilder` is used to generate smithing recipes which transform an item. Additionally, custom recipes using a serializer like SmithingTransformRecipe.Serializer can also be data generated using this builder. The builder can be initialized via `#smithing` or through the constructor respectively. The recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(output)
SmithingTransformRecipeBuilder.smithing(template, base, addition, RecipeCategory.MISC, result)
    .unlocks("criteria", criteria) // How the recipe is unlocked
    .save(output, name); // Add data to builder
```

### SmithingTrimRecipeBuilder

`SmithingTrimRecipeBuilder` is used to generate smithing recipes for armor trims. Additionally, custom upgrade recipes using a serializer like SmithingTrimRecipe.Serializer can also be data generated using this builder. The builder can be initialized via `#smithingTrim` or through the constructor respectively. The recipe unlock criteria can be specified before saving.

```java
// In RecipeProvider#buildRecipes(output)
SmithingTrimRecipe.smithingTrim(template, base, addition, RecipeCategory.MISC)
    .unlocks("criteria", criteria) // How the recipe is unlocked
    .save(output, name); // Add data to builder
```

### SpecialRecipeBuilder

`SpecialRecipeBuilder` is used to generate empty JSONs for dynamic recipes that cannot easily be constrained to the recipe JSON format (dying armor, firework, etc.). The builder can be initialized via `#special`.

```java
// In RecipeProvider#buildRecipes(output)
SpecialRecipeBuilder.special(category -> new MyDynamicRecipe(category))
    .save(output, name); // Add data to builder
```

## Conditional Recipes

[Conditional recipes][conditional] can also be data generated by either using the `#accept` overload that takes in `ICondition`s or by passing in the `RecipeOutput` after calling `#withConditions` to specify the `ICondition`s.

```java
// In RecipeProvider#buildRecipes(output)
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
    .pattern("a a") // Create recipe pattern
    .define('a', item) // Define what the symbol represents
    .unlockedBy("criteria", criteria) // How the recipe is unlocked
    .save(output.withConditions(
        // The conditions that loads this recipe
        new ItemExistsCondition("other_mod", "other_mod_item")
    )
); // Add data to builder

output.accept(
    name, // The name of the receipe
    recipeObj, // The recipe instance
    advancementHolder, // A holder containing the name of the advancement and the advancement instance
    // The conditions that loads this recipe
    new ItemExistsCondition("other_mod", "other_mod_item")
);
```

### IConditionBuilder

To simplify adding conditions to conditional recipes without having to construct the instances of each condition instance manually, the extended `RecipeProvider` can implement `IConditionBuilder`. The interface adds methods to easily construct condition instances.

```java
// In RecipeProvider#buildRecipes(output)
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
    .pattern("a a") // Create recipe pattern
    .define('a', item) // Define what the symbol represents
    .unlockedBy("criteria", criteria) // How the recipe is unlocked
    .save(output.withConditions(
        // The conditions that loads this recipe
        itemExists("other_mod", "other_mod_item")
    )
); // Add data to builder

output.accept(
    name, // The name of the receipe
    recipeObj, // The recipe instance
    advancementHolder, // A holder containing the name of the advancement and the advancement instance
    // The conditions that loads this recipe
    itemExists("other_mod", "other_mod_item")
);
```

## Custom Recipe Serializers

Custom recipe serializers can be data generated by creating a builder that can construct a `Recipe`. The builder encodes the recipe and its unlocking advancement, when present, to JSON. Additionally, the name and serializer of the recipe is also specified to know where to write to and what can decode the object when loading. Once a `Recipe` is constructed, it simply needs to be passed to the `RecipeOutput` supplied by `RecipeProvider#buildRecipes`.

[datagen]: ../resources/index.md#data-generation
[ingredients]: ../resources/server/recipes/ingredients.md#neoforge-types
[stack]: ../resources/server/recipes/index.md#recipe-itemstack-result
[conditional]: ../resources/server/conditional.md
[special]: #specialrecipebuilder
