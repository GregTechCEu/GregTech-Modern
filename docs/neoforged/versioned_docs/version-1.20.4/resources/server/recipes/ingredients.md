# Ingredients

`Ingredient`s are predicate handlers for item-based inputs which check whether a certain `ItemStack` meets the condition to be a valid input in a recipe. All [vanilla recipes][recipes] that take inputs use an `Ingredient` or a list of `Ingredient`s, which is then merged into a single `Ingredient`.

## Custom Ingredients

Custom ingredients can be specified by setting `type` to the name of the [ingredient's serializer][serializer], with the exception of [compound ingredients][compound]. When no type is specified, `type` defaults to the vanilla ingredient `minecraft:item`. Custom ingredients can also easily be used in [data generation][datagen].

### NeoForge Types

NeoForge provides a few additional `Ingredient` types for programmers to implement. 

#### CompoundIngredient

Though they are functionally identical, Compound ingredients replaces the way one would implement a list of ingredients would in a recipe. They work as a set OR where the passed in stack must be within at least one of the supplied ingredients. This change was made to allow custom ingredients to work correctly within lists. As such, **no type** needs to be specified.

```js
// For some input
[
  // At least one of these ingredients must match to succeed
  {
    // Ingredient
  },
  {
    // Custom ingredient
    "type": "examplemod:example_ingredient"
  }
]
```

#### StrictNBTIngredient

`StrictNBTIngredient`s compare the item, damage, and the share tags (as defined by `IForgeItem#getShareTag`) on an `ItemStack` for exact equivalency. This can be used by specifying the `type` as `forge:nbt`.

```js
// For some input
{
  "type": "forge:nbt",
  "item": "examplemod:example_item",
  "nbt": {
    // Add nbt data (must match exactly what is on the stack)
  }
}
```

### PartialNBTIngredient

`PartialNBTIngredient`s are a looser version of [`StrictNBTIngredient`][nbt] as they compare against a single or set of items and only keys specified within the share tag (as defined by `IForgeItem#getShareTag`). This can be used by specifying the `type` as `forge:partial_nbt`.

```js
// For some input
{
  "type": "forge:partial_nbt",

  // Either 'item' or 'items' must be specified
  // If both are specified, only 'item' will be read
  "item": "examplemod:example_item",
  "items": [
    "examplemod:example_item",
    "examplemod:example_item2"
    // ...
  ],

  "nbt": {
    // Checks only for equivalency on 'key1' and 'key2'
    // All other keys in the stack will not be checked
    "key1": "data1",
    "key2": {
      // Data 2
    }
  }
}
```

### IntersectionIngredient

`IntersectionIngredient`s work as a set AND where the passed in stack must match all supplied ingredients. There must be at least two ingredients supplied to this. This can be used by specifying the `type` as `forge:intersection`.

```js
// For some input
{
  "type": "forge:intersection",

  // All of these ingredients must return true to succeed
  "children": [
    {
      // Ingredient 1
    },
    {
      // Ingredient 2
    }
    // ...
  ]
}
```

### DifferenceIngredient

`DifferenceIngredient`s work as a set subtraction (SUB) where the passed in stack must match the first ingredient but must not match the second ingredient. This can be used by specifying the `type` as `forge:difference`.

```js
// For some input
{
  "type": "forge:difference",
  "base": {
    // Ingredient the stack is in
  },
  "subtracted": {
    // Ingredient the stack is NOT in
  }
}
```

## Creating Custom Ingredients

Custom ingredients can be created by implementing `IIngredientSerializer` for the created `Ingredient` subclass.

:::tip
Custom ingredients should subclass `AbstractIngredient` as it provides some useful abstractions for ease of implementation.
:::

### Ingredient Subclass

There are three important methods to implement for each ingredient subclass:

 Method       | Description
 :---:        | :---
getSerializer | Returns the [serializer] used to read and write the ingredient.
test          | Returns true if the input is valid for this ingredient.
isSimple      | Returns false if the ingredient matches on the stack's tag. `AbstractIngredient` subclasses will need to define this behavior, while `Ingredient` subclasses return `true` by default.

All other defined methods are left as an exercise to the reader to use as required for the ingredient subclass.

### IIngredientSerializer

`IIngredientSerializer` subtypes must implement three methods:

 Method         | Description
 :---:          | :---
parse (JSON)    | Converts a `JsonObject` to an `Ingredient`.
parse (Network) | Reads the network buffer to decode an `Ingredient`.
write           | Writes an `Ingredient` to the network buffer.

Additionally, `Ingredient` subclasses should implement `Ingredient#toJson` for use with [data generation][datagen]. `AbstractIngredient` subclasses make `#toJson` an abstract method requiring the method to be implemented.

Afterwards, a static instance should be declared to hold the initialized serializer and then registered using `CraftingHelper#register` either during the `RegisterEvent` for `RecipeSerializer`s or during `FMLCommonSetupEvent`. The `Ingredient` subclass return the static instance of the serializer in `Ingredient#getSerializer`.

```java
// In some serializer class
public static final ExampleIngredientSerializer INSTANCE = new ExampleIngredientSerializer();

// In some event handler class
@SubscribeEvent // on the mod event bus
public static void registerSerializers(RegisterEvent event) {
  event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS,
    helper -> CraftingHelper.register(registryName, INSTANCE)
  );
}

// In some ingredient subclass
@Override
public IIngredientSerializer<? extends Ingredient> getSerializer() {
  return INSTANCE;
}
```

:::tip
If using `FMLCommonSetupEvent` to register an ingredient serializer, it must be enqueued to the synchronous work queue via `FMLCommonSetupEvent#enqueueWork` as `CraftingHelper#register` is not thread-safe.
:::

[recipes]: https://minecraft.wiki/w/Recipe#List_of_recipe_types
[nbt]: #strictnbtingredient
[serializer]: #iingredientserializer
[compound]: #compoundingredient
[datagen]: ../../../datagen/recipes.md
