import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Recipes

Recipes are a way to transform a set of objects into other objects within a Minecraft world. Although Minecraft uses this system purely for item transformations, the system is built in a way that allows any kind of objects - blocks, entities, etc. - to be transformed. Almost all recipes use recipe data files; a "recipe" is assumed to be a data-driven recipe in this article unless explicitly stated otherwise.

Recipe data files are located at `data/<namespace>/recipe/<path>.json`. For example, the recipe `minecraft:diamond_block` is located at `data/minecraft/recipe/diamond_block.json`.

## Terminology

- A **recipe JSON**, or **recipe file**, is a JSON file that is loaded and stored by the `RecipeManager`. It contains info such as the recipe type, the inputs and outputs, as well as additional information (e.g. processing time).
- A **`Recipe`** holds in-code representations of all JSON fields, alongside the matching logic ("Does this input match the recipe?") and some other properties.
- A **`RecipeInput`** is a type that provides inputs to a recipe. Comes in several subclasses, e.g. `CraftingInput` or `SingleRecipeInput` (for furnaces and similar).
- A **recipe ingredient**, or just **ingredient**, is a single input for a recipe (whereas the `RecipeInput` generally represents a collection of inputs to check against a recipe's ingredients). Ingredients are a very powerful system and as such outlined [in their own article][ingredients].
- A **`PlacementInfo`** is a definition of items the recipe contains and what indexes they should populate. If the recipe cannot be captured to some degree based on the items provided (e.g., only changing the data components), then `PlacementInfo#NOT_PLACEABLE` is used.
- A **`SlotDisplay`** defines how a single slot should display within a recipe viewer, like the recipe book.
- A **`RecipeDisplay`** defines the `SlotDisplay`s of a recipe to be consumed by a recipe viewer, like the recipe book. While the interface only contains methods for the result of a recipe and the workstation the recipe was conducted within, a subtype can capture information like ingredients or grid size.
- The **`RecipeManager`** is a singleton field on the server that holds all loaded recipes.
- A **`RecipeSerializer`** is basically a wrapper around a [`MapCodec`][codec] and a [`StreamCodec`][streamcodec], both used for serialization.
- A **`RecipeType`** is the registered type equivalent of a `Recipe`. It is mainly used when looking up recipes by type. As a rule of thumb, different crafting containers should use different `RecipeType`s. For example, the `minecraft:crafting` recipe type covers the `minecraft:crafting_shaped` and `minecraft:crafting_shapeless` recipe serializers, as well as the special crafting serializers.
- A **`RecipeBookCategory`** is a group representing some recipes when viewed through a recipe book.
- A **recipe [advancement]** is an advancement responsible for unlocking a recipe in the recipe book. They are not required, and generally neglected by players in favor of recipe viewer mods, however the [recipe data provider][datagen] generates them for you, so it's recommended to just roll with it.
- A **`RecipePropertySet`** defines the available list of ingredients that can be accepted by the defined input slot in a menu.
- A **`RecipeBuilder`** is used during datagen to create JSON recipes.
- A **recipe factory** is a method reference used to create a `Recipe` from a `RecipeBuilder`. It can either be a reference to a constructor, or a static builder method, or a functional interface (often named `Factory`) created specifically for this purpose.

## JSON Specification

The contents of recipe files vary greatly depending on the selected type. Common to all recipe files are the `type` and [`neoforge:conditions`][conditions] properties:

```json5
{
    // The recipe type. This maps to an entry in the recipe serializer registry.
    "type": "minecraft:crafting_shaped",
    // A list of data load conditions. Optional, NeoForge-added. See the article linked above for more information.
    "neoforge:conditions": [ /*...*/ ]
}
```

A full list of types provided by Minecraft can be found in the [Built-In Recipe Types article][builtin]. Mods can also [define their own recipe types][customrecipes].

## Using Recipes

Recipes are loaded, stored and obtained via the `RecipeManager` class, which is in turn obtained via `ServerLevel#recipeAccess` or - if you don't have a `ServerLevel` available - `ServerLifecycleHooks.getCurrentServer()#getRecipeManager`. The server does not sync the recipes to the client by default, instead it only sends the `RecipePropertySet`s for restricting inputs on menu slots. Additionally, whenever a recipe is unlocked for the recipe book, its `RecipeDisplay`s and the corresponding `RecipeDisplayEntry`s are sent to the client (excluding all recipes where `Recipe#isSpecial` returns true) As such, recipe logic should always run on the server.

The easiest way to get a recipe is by its resource key:

```java
RecipeManager recipes = serverLevel.recipeAccess();
// RecipeHolder<?> is a record of the resource key and the recipe itself.
Optional<RecipeHolder<?>> optional = recipes.byKey(
    ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("diamond_block"))
);
optional.map(RecipeHolder::value).ifPresent(recipe -> {
    // Do whatever you want to do with the recipe here. Be aware that the recipe may be of any type.
});
```

A more practically applicable method is constructing a `RecipeInput` and trying to get a matching recipe. In this example, we will be creating a `CraftingInput` containing one diamond block using `CraftingInput#of`. This will create a shapeless input, a shaped input would instead use `CraftingInput#ofPositioned`, and other inputs would use other `RecipeInput`s (for example, furnace recipes will generally use `new SingleRecipeInput`).

```java
RecipeManager recipes = serverLevel.recipeAccess();
// Construct a RecipeInput, as required by the recipe. For example, construct a CraftingInput for a crafting recipe.
// The parameters are width, height and items, respectively.
CraftingInput input = CraftingInput.of(1, 1, List.of(new ItemStack(Items.DIAMOND_BLOCK)));
// The generic wildcard on the recipe holder should then extend CraftingRecipe.
// This allows for more type safety later on.
Optional<RecipeHolder<? extends CraftingRecipe>> optional = recipes.getRecipeFor(
        // The recipe type to get the recipe for. In our case, we use the crafting type.
        RecipeType.CRAFTING,
        // Our recipe input.
        input,
        // Our level context.
        serverLevel
);
// This returns the diamond block -> 9 diamonds recipe (unless a datapack changes that recipe).
optional.map(RecipeHolder::value).ifPresent(recipe -> {
    // Do whatever you want here. Note that the recipe is now a CraftingRecipe instead of a Recipe<?>.
});
```

Alternatively, you can also get yourself a potentially empty list of recipes that match your input, this is especially useful for cases where it can be reasonably assumed that multiple recipes match:

```java
RecipeManager recipes = serverLevel.recipeAccess();
CraftingInput input = CraftingInput.of(1, 1, List.of(new ItemStack(Items.DIAMOND_BLOCK)));
// These are not Optionals, and can be used directly. However, the list may be empty, indicating no matching recipes.
Stream<RecipeHolder<? extends Recipe<CraftingInput>>> list = recipes.recipeMap().getRecipesFor(
    // Same parameters as above.
    RecipeType.CRAFTING, input, serverLevel
);
```

Once we have our correct recipe inputs, we also want to get the recipe outputs. This is done by calling `Recipe#assemble`:

```java
RecipeManager recipes = serverLevel.recipeAccess();
CraftingInput input = CraftingInput.of(...);
Optional<RecipeHolder<? extends CraftingRecipe>> optional = recipes.getRecipeFor(...);
// Use ItemStack.EMPTY as a fallback.
ItemStack result = optional
        .map(RecipeHolder::value)
        .map(recipe -> recipe.assemble(input, serverLevel.registryAccess()))
        .orElse(ItemStack.EMPTY);
```

If necessary, it is also possible to iterate over all recipes of a type. This is done like so:

```java
RecipeManager recipes = serverLevel.recipeAccess();
// Like before, pass the desired recipe type.
Collection<RecipeHolder<?>> list = recipes.recipeMap().byType(RecipeType.CRAFTING);
```

## Recipe Priorities

Sometimes, recipes can overlap with others, usually because one pattern uses a specific item while another same pattern uses a tag that has the item within. In these instances, vanilla uses the first recipe it finds, which is determined by whatever recipe is read and loaded first. This can be an issue, as if the specific item recipe is loaded after the tag-based recipe, then the specific item recipe can never be obtained.

To combat this issue, NeoForge introduces recipe priorities to order which recipes should be displayed first. The entries are represented as a map of recipe registry keys to integer priority values. The priority values are sorted based on the highest value, recipes not specified defaulting to `0`. This means that recipes with a priority greater than `0` are ordered first, while recipes less than `0` are ordered last. The priority map is located in `data/<namespace>/recipe_priorities.json`, where all the recipe priorities are merged together, unless `replace` is true, which will clear out all previously loaded entries.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    // When true, clears out all previously loaded entries.
    "replace": false,
    // The map of recipe entries to their priority values.
    // If a recipe does not have a priority, it defaults to 0.
    "entries": {
        // Points to 'data/examplemod/recipe/higher_priority.json'
        // This recipe will be checked before any defaults.
        "examplemod:higher_priority": 1,
        // Points to 'data/examplemod/recipe/lower_priority.json'
        // This recipe will be checked after any defaults.
        "examplemod:lower_priority": -1,
        // Points to 'data/examplemod/recipe/even_lower_priority.json'
        // This recipe will be checked after any defaults and the
        // 'lower_priority' recipe.
        "examplemod:even_lower_priority": -2
    }
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Generates the recipe priorities
public class ExamplePrioritiesProvider extends RecipePrioritiesProvider {

    public ExamplePrioritiesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        // Replace 'examplemod' with your mod id.
        super(output, registries, "examplemod");
    }

    @Override
    protected void start() {
        // Registers a recipe entry to a priority value.

        this.add(
            // Points to 'data/examplemod/recipe/higher_priority.json'
            ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("examplemod", "higher_priority")),
            // This recipe will be checked before any defaults.
            1
        );

        this.add(
            // Points to 'data/examplemod/recipe/lower_priority.json'
            ResourceLocation.fromNamespaceAndPath("examplemod", "lower_priority"),
            // This recipe will be checked after any defaults.
            -1
        );

        this.add(
            // Points to 'data/examplemod/recipe/even_lower_priority.json'
            // The namespace is inferred from the mod id passed to the provider.
            "even_lower_priority",
            // This recipe will be checked after any defaults and the 'lower_priority' recipe.
            -2
        );
    }
}
```

</TabItem>
</Tabs>

## Other Recipe Mechanisms

Some mechanisms in vanilla are generally considered recipes, but are implemented differently in code. This is generally either due to legacy reasons, or because the "recipes" are constructed from other data (e.g. [tags]).

:::warning
Recipe viewer mods will generally not pick up these recipes. Support for these mods must be added manually, please see the corresponding mod's documentation for more information.
:::

### Anvil Recipes

Anvils have two input slots and one output slot. The only vanilla use cases are tool repairing, combining and renaming, and since each of these use cases needs special handling, no recipe files are provided. However, the system can be built upon using `AnvilUpdateEvent`. This [event] allows getting the input (left input slot) and material (right input slot) and allows setting an output item stack, as well as the experience cost and the number of materials to consume. The process can also be prevented as a whole by [canceling][cancel] the event.

```java
// This example allows repairing a stone pickaxe with a full stack of dirt, consuming half the stack, for 3 levels.
@SubscribeEvent // on the game event bus
public static void onAnvilUpdate(AnvilUpdateEvent event) {
    ItemStack left = event.getLeft();
    ItemStack right = event.getRight();
    if (left.is(Items.STONE_PICKAXE) && right.is(Items.DIRT) && right.getCount() >= 64) {
        event.setOutput(new ItemStack(Items.STONE_PICKAXE));
        event.setMaterialCost(32);
        event.setXpCost(3);
    }
}
```

### Brewing

See [the Brewing chapter in the Mob Effects & Potions article][brewing].

### Extending the Crafting Grid Size

The `ShapedRecipePattern` class, responsible for holding the in-memory representation of shaped crafting recipes, has a hardcoded limit of 3x3 slots, hindering mods that want to add larger crafting tables while reusing the vanilla shaped crafting recipe type. To solve this problem, NeoForge patches in a static method called `ShapedRecipePattern#setCraftingSize(int width, int height)` that allows increasing the limit. It should be called during `FMLCommonSetupEvent`. The biggest value wins here, so for example if one mod added a 4x6 crafting table and another added a 6x5 crafting table, the resulting values would be 6x6.

:::danger
`ShapedRecipePattern#setCraftingSize` is not thread-safe. It must be wrapped in an `event#enqueueWork` call.
:::

### Client-Side Recipes

By default, vanilla does not send any recipes to the [logical client][logicalside]. Instead, the `RecipePropertySet` / `SelectableRecipe.SingleInputSet` is synced to handle proper client-side behavior during user interactions. Additionally, when a recipe is unlocked in the recipe book, its `RecipeDisplay` is synced. However, these two cases are limited in scope, especially when more data is needed from the recipe itself. In these instances, NeoForge provides a way to send the full recipes for a given `RecipeType` to the client.

There are two events that must be listened to on the [game event bus][events]: `OnDatapackSyncEvent` and `RecipesReceivedEvent`. First, specify the `RecipeType`s to sync to the client by calling `OnDatapackSyncEvent#sendRecipes`. Then, the recipes can be accessed from the provided `RecipeMap` via `RecipesReceivedEvent#getRecipeMap`. Additionally, any recipes stored on the client should be cleared once the player logs out of the world via `ClientPlayerNetworkEvent.LoggingOut`.

```java
// Assume we have some custom RecipeType<ExampleRecipe> EXAMPLE_RECIPE_TYPE

@SubscribeEvent // on the game event bus
public static void datapackSync(OnDatapackSyncEvent event) {
    // Specify what recipe types to sync to the client
    event.sendRecipes(EXAMPLE_RECIPE_TYPE);
}

// In some class only on the physical client

private static final List<RecipeHolder<ExampleRecipe>> EXAMPLE_RECIPES = new ArrayList<>();

@SubscribeEvent // on the game event bus only on the physical client
public static void recipesReceived(RecipesReceivedEvent event) {
    // First remove the previous recipes
    EXAMPLE_RECIPES.clear();

    // Then store the recipes you want
    EXAMPLE_RECIPES.addAll(event.getRecipeMap().byType(EXAMPLE_RECIPE_TYPE));
}

@SubscribeEvent // on the game event bus only on the physical client
public static void clientLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
    // Clear the stored recipes on world log out
    EXAMPLE_RECIPES.clear();
}
```

:::warning
If you are planning on syncing recipes for your recipe type, `OnDatapackSyncEvent` should be called on both physical sides. All worlds, including singleplayer, have a delineation between the server and client, meaning that referencing a datapack registry entry from the server on the client will likely crash the game.
:::

## Data Generation

Like most other JSON files, recipes can be datagenned. For recipes, we want to extend the `RecipeProvider` class and override `#buildRecipes`, and extend the `RecipeProvider.Runner` class to pass to the data generator:

```java
public class MyRecipeProvider extends RecipeProvider {

    // Construct the provider to run
    protected MyRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }
 
    @Override
    protected void buildRecipes() {
        // Add your recipes here.
    }

    // The runner to add to the data generator
    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new MyRecipeProvider(provider, output);
        }
    }
}
```

Of note is the `RecipeOutput` parameter. Minecraft uses this object to automatically generate a recipe advancement for you. On top of that, NeoForge injects [conditions] support into `RecipeOutput`, which can be called on via `#withConditions`.

Recipes themselves are commonly added through subclasses of `RecipeBuilder`. Listing all vanilla recipe builders is beyond the scope of this article (they are explained in the [Built-In Recipe Types article][builtin]), however creating your own builder is explained [in the custom recipes page][customdatagen].

Like all other data providers, recipe providers must be registered to the `GatherDataEvent`s like so:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    // Call event.createDatapackRegistryObjects(...) first if adding datapack objects

    event.createProvider(MyRecipeProvider.Runner::new);
}
```

The recipe provider also adds helpers for common scenarios, such as `twoByTwoPacker` (for 2x2 block recipes), `threeByThreePacker` (for 3x3 block recipes) or `nineBlockStorageRecipes` (for 3x3 block recipes and 1 block to 9 items recipes).

[advancement]: ../advancements.md
[brewing]: ../../../items/mobeffects.md#brewing
[builtin]: builtin.md
[cancel]: ../../../concepts/events.md#cancellable-events
[codec]: ../../../datastorage/codecs.md
[conditions]: ../conditions.md
[customdatagen]: custom.md#data-generation
[customrecipes]: custom.md
[datagen]: #data-generation
[event]: ../../../concepts/events.md
[ingredients]: ingredients.md
[logicalside]: ../../../concepts/sides.md#the-logical-side
[streamcodec]: ../../../networking/streamcodecs.md
[tags]: ../tags.md
