# Custom Recipes

Every recipe definition is made up of three components: the `Recipe` implementation which holds the data and handles the execution logic with the provided inputs, the `RecipeType` which represents the category or context the recipe will be used in, and the `RecipeSerializer` which handles decoding and network communication of the recipe data. How one chooses to use the recipe is up to the implementor.

## Recipe

The `Recipe` interface describes the recipe data and the execution logic. This includes matching the inputs and providing the associated result. As the recipe subsystem performs item transformations by default, the inputs are supplied through a `Container` subtype.

:::caution
The `Container`s passed into the recipe should be treated as if its contents were immutable. Any mutable operations should be performed on a copy of the input through `ItemStack#copy`.
:::

To be able to obtain a recipe instance from the manager, `#matches` must return true. This method checks against the provided container to see whether the associated inputs are valid. `Ingredient`s can be used for validation by calling `Ingredient#test`.

If the recipe has been chosen, it is then built using `#assemble` which may use data from the inputs to create the result.

:::tip
`#assemble` should always produce a unique `ItemStack`. If unsure whether `#assemble` does so, call `ItemStack#copy` on the result before returning.
:::

Most of the other methods are purely for integration with the recipe book.

```java
public record ExampleRecipe(Ingredient input, int data, ItemStack output) implements Recipe<Container> {
  // Implement methods here
}
```

:::note
While a record is used in the above example, it is not required to do so in your own implementation.
:::

## RecipeType

`RecipeType` is responsible for defining the category or context the recipe will be used within. For example, if a recipe was going to be smelted in a furnace, it would have a type of `RecipeType#SMELTING`. Being blasted in a blast furnace would have a type of `RecipeType#BLASTING`.

If none of the existing types match what context the recipe will be used within, then a new `RecipeType` must be [registered][forge].

The `RecipeType` instance must then be returned by `Recipe#getType` in the new recipe subtype.

```java
// For some Supplier<RecipeType<?>> EXAMPLE_TYPE
// In ExampleRecipe
@Override
public RecipeType<?> getType() {
  return EXAMPLE_TYPE.get();
}
```

## RecipeSerializer

A `RecipeSerializer` is responsible for decoding JSONs and communicating across the network for an associated `Recipe` subtype. Each recipe decoded by the serializer is saved as a unique instance within the `RecipeManager`. A `RecipeSerializer` must be [registered][forge].

Only three methods need to be implemented for a `RecipeSerializer`:

 Method     | Description
 :---:      | :---
fromJson    | Decodes a JSON into the `Recipe` subtype.
toNetwork   | Encodes a `Recipe` to the buffer to send to the client. The recipe identifier does not need to be encoded.
fromNetwork | Decodes a `Recipe` from the buffer sent from the server. The recipe identifier does not need to be decoded.

The `RecipeSerializer` instance must then be returned by `Recipe#getSerializer` in the new recipe subtype.

```java
// For some Supplier<RecipeSerializer<?>> EXAMPLE_SERIALIZER
// In ExampleRecipe
@Override
public RecipeSerializer<?> getSerializer() {
  return EXAMPLE_SERIALIZER.get();
}
```

:::tip
There are some useful methods to make reading and writing data for recipes easier. `Ingredient`s can use `#fromJson`, `#toNetwork`, and `#fromNetwork` while `ItemStack`s can use `CraftingHelper#getItemStack`, `FriendlyByteBuf#writeItem`, and `FriendlyByteBuf#readItem`.
:::

## Building the JSON

Custom Recipe JSONs are stored in the same place as other [recipes][json]. The specified `type` should represent the registry name of the **recipe serializer**. Any additional data is specified by the serializer during decoding.

```js
{
  // The custom serializer registry name
  "type": "examplemod:example_serializer",
  "input": {
    // Some ingredient input
  },
  "data": 0, // Some data wanted for the recipe
  "output": {
    // Some stack output
  }
}
```

## Non-Item Logic

If items are not used as part of the input or result of a recipe, then the normal methods provided in [`RecipeManager`][manager] will not be useful. Instead, an additional method for testing a recipe's validity and/or supplying the result should be added to the custom `Recipe` instance. From there, all the recipes for that specific `RecipeType` can be obtained via `RecipeManager#getAllRecipesFor` and then checked and/or supplied the result using the newly implemented methods.

```java
// In some Recipe subimplementation ExampleRecipe

// Checks the block at the position to see if it matches the stored data
boolean matches(Level level, BlockPos pos);

// Creates the block state to set the block at the specified position to
BlockState assemble(RegistryAccess access);

// In some manager class
public Optional<ExampleRecipe> getRecipeFor(Level level, BlockPos pos) {
  return level.getRecipeManager()
    .getAllRecipesFor(exampleRecipeType) // Gets all recipes
    .stream() // Looks through all recipes for types
    .filter(recipe -> recipe.matches(level, pos)) // Checks if the recipe inputs are valid
    .findFirst(); // Finds the first recipe whose inputs match
}
```

## Data Generation

All custom recipes, regardless of input or output data, can be created into a `FinishedRecipe` for [data generation][datagen] using the `RecipeProvider`.

[forge]: ../../../concepts/registries.md#methods-for-registering
[json]: https://minecraft.wiki/w/Recipe#JSON_format
[manager]: ./index.md#recipe-manager
[datagen]: ../../../datagen/recipes.md#custom-recipe-serializers
