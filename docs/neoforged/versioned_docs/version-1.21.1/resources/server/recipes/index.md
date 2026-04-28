# Recipes

Recipes are a way to transform a set of objects into other objects within a Minecraft world. Although Minecraft uses this system purely for item transformations, the system is built in a way that allows any kind of objects - blocks, entities, etc. - to be transformed. Almost all recipes use recipe data files; a "recipe" is assumed to be a data-driven recipe in this article unless explicitly stated otherwise.

Recipe data files are located at `data/<namespace>/recipe/<path>.json`. For example, the recipe `minecraft:diamond_block` is located at `data/minecraft/recipe/diamond_block.json`.

## Terminology

- A **recipe JSON**, or **recipe file**, is a JSON file that is loaded and stored by the `RecipeManager`. It contains info such as the recipe type, the inputs and outputs, as well as additional information (e.g. processing time).
- A **`Recipe`** holds in-code representations of all JSON fields, alongside the matching logic ("Does this input match the recipe?") and some other properties.
- A **`RecipeInput`** is a type that provides inputs to a recipe. Comes in several subclasses, e.g. `CraftingInput` or `SingleRecipeInput` (for furnaces and similar).
- A **recipe ingredient**, or just **ingredient**, is a single input for a recipe (whereas the `RecipeInput` generally represents a collection of inputs to check against a recipe's ingredients). Ingredients are a very powerful system and as such outlined [in their own article][ingredients].
- The **`RecipeManager`** is a singleton field on the server that holds all loaded recipes.
- A **`RecipeSerializer`** is basically a wrapper around a [`MapCodec`][codec] and a [`StreamCodec`][streamcodec], both used for serialization.
- A **`RecipeType`** is the registered type equivalent of a `Recipe`. It is mainly used when looking up recipes by type. As a rule of thumb, different crafting containers should use different `RecipeType`s. For example, the `minecraft:crafting` recipe type covers the `minecraft:crafting_shaped` and `minecraft:crafting_shapeless` recipe serializers, as well as the special crafting serializers.
- A **recipe [advancement]** is an advancement responsible for unlocking a recipe in the recipe book. They are not required, and generally neglected by players in favor of recipe viewer mods, however the [recipe data provider][datagen] generates them for you, so it's recommended to just roll with it.
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

Recipes are loaded, stored and obtained via the `RecipeManager` class, which is in turn obtained via `ServerLevel#getRecipeManager` or - if you don't have a `ServerLevel` available - `ServerLifecycleHooks.getCurrentServer()#getRecipeManager`. Be aware that while the client has a full copy of the `RecipeManager` for display purposes, recipe logic should always run on the server to avoid sync issues.

The easiest way to get a recipe is by ID:

```java
RecipeManager recipes = serverLevel.getRecipeManager();
// RecipeHolder<?> is a record of the recipe id and the recipe itself.
Optional<RecipeHolder<?>> optional = recipes.byId(ResourceLocation.withDefaultNamespace("diamond_block"));
optional.map(RecipeHolder::value).ifPresent(recipe -> {
        // Do whatever you want to do with the recipe here. Be aware that the recipe may be of any type.
});
```

A more practically applicable method is constructing a `RecipeInput` and trying to get a matching recipe. In this example, we will be creating a `CraftingInput` containing one diamond block using `CraftingInput#of`. This will create a shapeless input, a shaped input would instead use `CraftingInput#ofPositioned`, and other inputs would use other `RecipeInput`s (for example, furnace recipes will generally use `new SingleRecipeInput`).

```java
RecipeManager recipes = serverLevel.getRecipeManager();
// Construct a RecipeInput, as required by the recipe. For example, construct a CraftingInput for a crafting recipe.
// The parameters are width, height and items, respectively.
CraftingInput input = CraftingInput.of(1, 1, List.of(Items.DIAMOND_BLOCK));
// The generic wildcard on the recipe holder should then extend Recipe<CraftingInput>.
// This allows for more type safety later on.
Optional<RecipeHolder<? extends Recipe<CraftingInput>>> optional = recipes.getRecipeFor(
        // The recipe type to get the recipe for. In our case, we use the crafting type.
        RecipeTypes.CRAFTING,
        // Our recipe input.
        input,
        // Our level context.
        serverLevel
);
// This returns the diamond block -> 9 diamonds recipe (unless a datapack changes that recipe).
optional.map(RecipeHolder::value).ifPresent(recipe -> {
        // Do whatever you want here. Note that the recipe is now a Recipe<CraftingInput> instead of a Recipe<?>.
});
```

Alternatively, you can also get yourself a potentially empty list of recipes that match your input, this is especially useful for cases where it can be reasonably assumed that multiple recipes match:

```java
RecipeManager recipes = serverLevel.getRecipeManager();
CraftingInput input = CraftingInput.of(1, 1, List.of(Items.DIAMOND_BLOCK));
// These are not Optionals, and can be used directly. However, the list may be empty, indicating no matching recipes.
List<RecipeHolder<? extends Recipe<CraftingInput>>> list = recipes.getRecipesFor(
        // Same parameters as above.
        RecipeTypes.CRAFTING, input, serverLevel
);
```

Once we have our correct recipe inputs, we also want to get the recipe outputs. This is done by calling `Recipe#assemble`:

```java
RecipeManager recipes = serverLevel.getRecipeManager();
CraftingInput input = CraftingInput.of(...);
Optional<RecipeHolder<? extends Recipe<CraftingInput>>> optional = recipes.getRecipeFor(...);
// Use ItemStack.EMPTY as a fallback.
ItemStack result = optional
        .map(RecipeHolder::value)
        .map(e -> e.assemble(input, serverLevel.registryAccess()))
        .orElse(ItemStack.EMPTY);
```

If necessary, it is also possible to iterate over all recipes of a type. This is done like so:

```java
RecipeManager recipes = serverLevel.getRecipeManager();
// Like before, pass the desired recipe type.
List<RecipeHolder<?>> list = recipes.getAllRecipesFor(RecipeTypes.CRAFTING);
```

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
        event.setOutput(Items.STONE_PICKAXE);
        event.setMaterialCost(32);
        event.setCost(3);
    }
}
```

### Brewing

See [the Brewing chapter in the Mob Effects & Potions article][brewing].

## Custom Recipes

To add custom recipes, we need at least three things: a `Recipe`, a `RecipeType`, and a `RecipeSerializer`. Depending on what you are implementing, you may also need a custom `RecipeInput` if reusing an existing subclass is not feasible.

For the sake of example, and to highlight many different features, we are going to implement a recipe-driven mechanic that requires you to right-click a `BlockState` in-world with a certain item, breaking the `BlockState` and dropping the result item.

### The Recipe Input

Let's begin by defining what we want to put into the recipe. It's important to understand that the recipe input represents the actual inputs that the player is using right now. As such, we don't use tags or ingredients here, instead we use the actual item stacks and blockstates we have available.

```java
// Our inputs are a BlockState and an ItemStack.
public record RightClickBlockInput(BlockState state, ItemStack stack) implements RecipeInput {
    // Method to get an item from a specific slot. We have one stack and no concept of slots, so we just assume
    // that slot 0 holds our item, and throw on any other slot. (Taken from SingleRecipeInput#getItem.)
    @Override
    public ItemStack getItem(int slot) {
        if (slot != 0) throw new IllegalArgumentException("No item for index " + slot);
        return this.stack();
    }

    // The slot size our input requires. Again, we don't really have a concept of slots, so we just return 1
    // because we have one item stack involved. Inputs with multiple items should return the actual count here.
    @Override
    public int size() {
        return 1;
    }
}
```

Recipe inputs don't need to be registered or serialized in any way because they are created on demand. It is not always necessary to create your own, the vanilla ones (`CraftingInput`, `SingleRecipeInput` and `SmithingRecipeInput`) are fine for many use cases.

Additionally, NeoForge provides the `RecipeWrapper` input, which wraps the `#getItem` and `#size` calls with respect to an `IItemHandler` passed in the constructor. Basically, this means that any grid-based inventory, such as a chest, can be used as a recipe input by wrapping it in a `RecipeWrapper`.

### The Recipe Class

Now that we have our inputs, let's get to the recipe itself. This is what holds our recipe data, and also handles matching and returning the recipe result. As such, it is usually the longest class for your custom recipe.

```java
// The generic parameter for Recipe<T> is our RightClickBlockInput from above.
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // An in-code representation of our recipe data. This can be basically anything you want.
    // Common things to have here is a processing time integer of some kind, or an experience reward.
    // Note that we now use an ingredient instead of an item stack for the input.
    private final BlockState inputState;
    private final Ingredient inputItem;
    private final ItemStack result;

    // Add a constructor that sets all properties. 
    public RightClickBlockRecipe(BlockState inputState, Ingredient inputItem, ItemStack result) {
        this.inputState = inputState;
        this.inputItem = inputItem;
        this.result = result;
    }

    // A list of our ingredients. Does not need to be overridden if you have no ingredients
    // (the default implementation returns an empty list here). It makes sense to cache larger lists in a field.
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.inputItem);
        return list;
    }

    // Grid-based recipes should return whether their recipe can fit in the given dimensions.
    // We don't have a grid, so we just return if any item can be placed in there.
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    // Check whether the given input matches this recipe. The first parameter matches the generic.
    // We check our blockstate and our item stack, and only return true if both match.
    @Override
    public boolean matches(RightClickBlockInput input, Level level) {
        return this.inputState == input.state() && this.inputItem.test(input.stack());
    }

    // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
    // for the recipe book, and commonly used by JEI and other recipe viewers as well.
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
    // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
    // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
    @Override
    public ItemStack assemble(RightClickBlockInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    // This example outlines the most important methods. There is a number of other methods to override.
    // Check the class definition of Recipe to view them all.
}
```

### The Recipe Type

Next up, our recipe type. This is fairly straightforward because there's no data other than a name associated with a recipe type. They are one of two [registered][registry] parts of the recipe system, so like with all other registries, we create a `DeferredRegister` and register to it:

```java
public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, ExampleMod.MOD_ID);

public static final Supplier<RecipeType<RightClickBlockRecipe>> RIGHT_CLICK_BLOCK =
        RECIPE_TYPES.register(
                "right_click_block",
                // We need the qualifying generic here due to generics being generics.
                () -> RecipeType.<RightClickBlockRecipe>simple(ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "right_click_block"))
        );
```

After we have registered our recipe type, we must override `#getType` in our recipe, like so:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here

    @Override
    public RecipeType<?> getType() {
        return RIGHT_CLICK_BLOCK.get();
    }
}
```

### The Recipe Serializer

A recipe serializer provides two codecs, one map codec and one stream codec, for serialization from/to JSON and from/to network, respectively. This section will not go in depth about how the codecs work, please see [Map Codecs][codec] and [Stream Codecs][streamcodec] for more information.

Since recipe serializers can get fairly large, vanilla moves them to separate classes. It is recommended, but not required to follow the practice - smaller serializers are often defined in anonymous classes within fields of the recipe class. To follow good practice, we will create a separate class that holds our codecs:

```java
// The generic parameter is our recipe class.
// Note: This assumes that simple RightClickBlockRecipe#getInputState, #getInputItem and #getResult getters
// are available, which were omitted from the code above.
public class RightClickBlockRecipeSerializer implements RecipeSerializer<RightClickBlockRecipe> {
    public static final MapCodec<RightClickBlockRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BlockState.CODEC.fieldOf("state").forGetter(RightClickBlockRecipe::getInputState),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(RightClickBlockRecipe::getInputItem),
            ItemStack.CODEC.fieldOf("result").forGetter(RightClickBlockRecipe::getResult)
    ).apply(inst, RightClickBlockRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RightClickBlockRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), RightClickBlockRecipe::getInputState,
                    Ingredient.CONTENTS_STREAM_CODEC, RightClickBlockRecipe::getInputItem,
                    ItemStack.STREAM_CODEC, RightClickBlockRecipe::getResult,
                    RightClickBlockRecipe::new
            );

    // Return our map codec.
    @Override
    public MapCodec<RightClickBlockRecipe> codec() {
        return CODEC;
    }

    // Return our stream codec.
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RightClickBlockRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
```

Like with the type, we register our serializer:

```java
public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExampleMod.MOD_ID);

public static final Supplier<RecipeSerializer<RightClickBlockRecipe>> RIGHT_CLICK_BLOCK =
        RECIPE_SERIALIZERS.register("right_click_block", RightClickBlockRecipeSerializer::new);
```

And similarly, we must also override `#getSerializer` in our recipe, like so:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RIGHT_CLICK_BLOCK.get();
    }
}
```

### The Crafting Mechanic

Now that all parts of your recipe are complete, you can make yourself some recipe JSONs (see the [datagen] section for that) and then query the recipe manager for your recipes, like above. What you then do with the recipe is up to you. A common use case would be a machine that can process your recipes, storing the active recipe as a field.

In our case, however, we want to apply the recipe when an item is right-clicked on a block. We will do so using an [event handler][event]. Keep in mind that this is an example implementation, and you can alter this in any way you like (so long as you run it on the server).

```java
@SubscribeEvent // on the game event bus
public static void useItemOnBlock(UseItemOnBlockEvent event) {
    // Skip if we are not in the block-dictated phase of the event. See the event's javadocs for details.
    if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) return;
    // Get the parameters we need.
    UseOnContext context = event.getUseOnContext();
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockState blockState = level.getBlockState(pos);
    ItemStack itemStack = context.getItemInHand();
    RecipeManager recipes = level.getRecipeManager();
    // Create an input and query the recipe.
    RightClickBlockInput input = new RightClickBlockInput(blockState, itemStack);
    Optional<RecipeHolder<? extends Recipe<CraftingInput>>> optional = recipes.getRecipeFor(
            // The recipe type.
            RIGHT_CLICK_BLOCK,
            input,
            level
    );
    ItemStack result = optional
            .map(RecipeHolder::value)
            .map(e -> e.assemble(input, level.registryAccess()))
            .orElse(ItemStack.EMPTY);
    // If there is a result, break the block and drop the result in the world.
    if (!result.isEmpty()) {
        level.removeBlock(pos, false);
        // If the level is not a server level, don't spawn the entity.
        if (!level.isClientSide()) {
            ItemEntity entity = new ItemEntity(level,
                    // Center of pos.
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    result);
            level.addFreshEntity(entity);
        }
        // Cancel the event to stop the interaction pipeline.
        event.cancelWithResult(ItemInteractionResult.sidedSuccess(level.isClientSide));
    }
}
```

### Extending the Crafting Grid Size

The `ShapedRecipePattern` class, responsible for holding the in-memory representation of shaped crafting recipes, has a hardcoded limit of 3x3 slots, hindering mods that want to add larger crafting tables while reusing the vanilla shaped crafting recipe type. To solve this problem, NeoForge patches in a static method called `ShapedRecipePattern#setCraftingSize(int width, int height)` that allows increasing the limit. It should be called during `FMLCommonSetupEvent`. The biggest value wins here, so for example if one mod added a 4x6 crafting table and another added a 6x5 crafting table, the resulting values would be 6x6.

:::danger
`ShapedRecipePattern#setCraftingSize` is not thread-safe. It must be wrapped in an `event#enqueueWork` call.
:::

## Data Generation

Like most other JSON files, recipes can be datagenned. For recipes, we want to extend the `RecipeProvider` class and override `#buildRecipes`:

```java
public class MyRecipeProvider extends RecipeProvider {
    // Get the parameters from GatherDataEvent.
    public MyRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Add your recipes here.
    }
}
```

Of note is the `RecipeOutput` parameter of `#buildRecipes`. Minecraft uses this object to automatically generate a recipe advancement for you. On top of that, NeoForge injects [conditions] support into `RecipeOutput`, which can be called on via `#withConditions`.

Recipes themselves are commonly added through subclasses of `RecipeBuilder`. Listing all vanilla recipe builders is beyond the scope of this article (they are explained in the [Built-In Recipe Types article][builtin]), however creating your own builder is explained [below][customdatagen].

Like all other data providers, recipe providers must be registered to `GatherDataEvent` like so:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

    // other providers here
    generator.addProvider(
            event.includeServer(),
            new MyRecipeProvider(output, lookupProvider)
    );
}
```

The recipe provider also adds helpers for common scenarios, such as `twoByTwoPacker` (for 2x2 block recipes), `threeByThreePacker` (for 3x3 block recipes) or `nineBlockStorageRecipes` (for 3x3 block recipes and 1 block to 9 items recipes).

### Data Generation for Custom Recipes

To create a recipe builder for your own recipe serializer(s), you need to implement `RecipeBuilder` and its methods. A common implementation, partially copied from vanilla, would look like this:

```java
// This class is abstract because there is a lot of per-recipe-serializer logic.
// It serves the purpose of showing the common part of all (vanilla) recipe builders.
public abstract class SimpleRecipeBuilder implements RecipeBuilder {
    // Make the fields protected so our subclasses can use them.
    protected final ItemStack result;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    protected String group;

    // It is common for constructors to accept the result item stack.
    // Alternatively, static builder methods are also possible.
    public SimpleRecipeBuilder(ItemStack result) {
        this.result = result;
    }

    // This method adds a criterion for the recipe advancement.
    @Override
    public SimpleRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    // This method adds a recipe book group. If you do not want to use recipe book groups,
    // remove the this.group field and make this method no-op (i.e. return this).
    @Override
    public SimpleRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
    // for serializing the recipes.
    @Override
    public Item getResult() {
        return this.result.getItem();
    }
}
```

So we have a base for our recipe builder. Now, before we continue with the recipe serializer-dependent part, we should first consider what to make our recipe factory. In our case, it makes sense to use the constructor directly. In other situations, using a static helper or a small functional interface is the way to go. This is especially relevant if you use one builder for multiple recipe classes.

Utilizing `RightClickBlockRecipe::new` as our recipe factory, and reusing the `SimpleRecipeBuilder` class above, we can create the following recipe builder for `RightClickBlockRecipe`s:

```java
public class RightClickBlockRecipeBuilder extends SimpleRecipeBuilder {
    private final BlockState inputState;
    private final Ingredient inputItem;

    // Since we have exactly one of each input, we pass them to the constructor.
    // Builders for recipe serializers that have ingredient lists of some sort would usually
    // initialize an empty list and have #addIngredient or similar methods instead.
    public RightClickBlockRecipeBuilder(ItemStack result, BlockState inputState, Ingredient inputItem) {
        super(result);
        this.inputState = inputState;
        this.inputItem = inputItem;
    }

    // Saves a recipe using the given RecipeOutput and id. This method is defined in the RecipeBuilder interface.
    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        // Build the advancement.
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);
        // Our factory parameters are the result, the block state, and the ingredient.
        RightClickBlockRecipe recipe = new RightClickBlockRecipe(this.inputState, this.inputItem, this.result);
        // Pass the id, the recipe, and the recipe advancement into the RecipeOutput.
        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
```

And now, during datagen, you can call on your recipe builder like any other:

```java
@Override
protected void buildRecipes(RecipeOutput output) {
    new RightClickRecipeBuilder(
            // Our constructor parameters. This example adds the ever-popular dirt -> diamond conversion.
            new ItemStack(Items.DIAMOND),
            Blocks.DIRT.defaultBlockState(),
            Ingredient.of(Items.APPLE)
    )
            .unlockedBy("has_apple", has(Items.APPLE))
            .save(output);
    // other recipe builders here
}
```

:::note
It is also possible to have `SimpleRecipeBuilder` be merged into `RightClickBlockRecipeBuilder` (or your own recipe builder), especially if you only have one or two recipe builders. The abstraction here serves to show which parts of the builder are recipe-dependent and which are not.
:::

[advancement]: ../advancements.md
[brewing]: ../../../items/mobeffects.md
[builtin]: builtin.md
[cancel]: ../../../concepts/events.md#cancellable-events
[codec]: ../../../datastorage/codecs.md
[conditions]: ../conditions.md
[customdatagen]: #data-generation-for-custom-recipes
[customrecipes]: #custom-recipes
[datagen]: #data-generation
[event]: ../../../concepts/events.md
[ingredients]: ingredients.md
[manager]: #using-recipes
[registry]: ../../../concepts/registries.md
[streamcodec]: ../../../networking/streamcodecs.md
[tags]: ../tags.md
