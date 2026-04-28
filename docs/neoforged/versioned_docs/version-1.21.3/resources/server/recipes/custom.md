# Custom Recipes

To add custom recipes, we need at least three things: a `Recipe`, a `RecipeType`, and a `RecipeSerializer`. Depending on what you are implementing, you may also need a custom `RecipeInput`, `RecipeDisplay`, `SlotDisplay`, `RecipeBookCategory`, and `RecipePropertySet` if reusing an existing subclass is not feasible.

For the sake of example, and to highlight many different features, we are going to implement a recipe-driven mechanic that requires you to right-click a `BlockState` in-world with a certain item, breaking the `BlockState` and dropping the result item.

## The Recipe Input

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

## The Recipe Class

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

    // Check whether the given input matches this recipe. The first parameter matches the generic.
    // We check our blockstate and our item stack, and only return true if both match.
    // If we needed to check the dimensions of our input, we would also do so here.
    @Override
    public boolean matches(RightClickBlockInput input, Level level) {
        return this.inputState == input.state() && this.inputItem.test(input.stack());
    }

    // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
    // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
    // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
    @Override
    public ItemStack assemble(RightClickBlockInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    // When true, will prevent the recipe from being synced within the recipe book or awarded on use/unlock.
    // This should only be true if the recipe shouldn't appear in a recipe book, such as map extending.
    // Although this recipe takes in an input state, it could still be used in a custom recipe book using
    //   the methods below.
    @Override
    public boolean isSpecial() {
        return true;
    }

    // This example outlines the most important methods. There is a number of other methods to override.
    // Some methods will be explained in the below sections as they cannot be easily compressed and understood here.
    // Check the class definition of Recipe to view them all.
}
```

## Recipe Book Categories

A `RecipeBookCategory` simply defines a group to display this recipe within in a recipe book. For example, an iron pickaxe crafting recipe would show up in the `RecipeBookCategories#CARFTING_EQUIPMENT` while a cooked cod recipe would show up in `#FURNANCE_FOOD` or `#SMOKER_FOOD`. Each recipe has one associated `RecipeBookCategory`. The vanilla categories can be found in `RecipeBookCategories`.

:::note
There are two cooked cod recipes, one for the furnance and one for the smoker. The furnace and smoker recipes have different book categories.
:::

If your recipe does not fit into one of the existing categories, typically because the recipe does not use one of the existing crafting stations (e.g., crafting table, furnace), then a new `RecipeBookCategory` can be created. Each `RecipeBookCategory` must be [registered][registry] to `BuiltInRegistries#RECIPE_BOOK_CATEGORY`:

```java
/// For some DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES
public static final Supplier<RecipeBookCategory> RIGHT_CLICK_BLOCK_CATEGORY = RECIPE_BOOK_CATEGORIES.register(
    "right_click_block", RecipeBookCategory::new
);
```

Then, to set the category, we must override `#recipeBookCategory` like so:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RIGHT_CLICK_BLOCK_CATEGORY.get();
    }
}
```

### Search Categories

All `RecipeBookCategory`s are technically `ExtendedRecipeBookCategory`s. There is another type of `ExtendedRecipeBookCategory` called `SearchRecipeBookCategory`, which is used to aggregate `RecipeBookCategory`s when viewing all recipes in a recipe book.

NeoForge allows users to specify their own `ExtendedRecipeBookCategory` as a search category via `RegisterRecipeBookSearchCategoriesEvent#register` on the mod event bus. `register` takes in the `ExtendedRecipeBookCategory` representing the search category and the `RecipeBookCategory`s that make up that search category. The `ExtendedRecipeBookCategory` search category does not need to be registered to some static vanilla registry.

```java
// In some location
public static final ExtendedRecipeBookCategory RIGHT_CLICK_BLOCK_SEARCH_CATEGORY = new ExtendedRecipeBookCategory() {};

@SubscribeEvent // on the mod event bus
public static void registerSearchCategories(RegisterRecipeBookSearchCategoriesEvent event) {
    event.register(
        // The search category
        RIGHT_CLICK_BLOCK_SEARCH_CATEGORY,
        // All recipe categories within the search category as varargs
        RIGHT_CLICK_BLOCK_CATEGORY.get()
    )
}
```

## Placement Info

A `PlacementInfo` is meant to define the crafting requirements used by the recipe consumer and whether/how it can be placed into its associated crafting station (e.g., crafting table, furnace). `PlacementInfo` are only meant for item ingredients, so if other types of ingredients are desired (e.g., fluid, block), the surrounding logic will need to be implemented from scratch. In these cases, the recipe can be labelled as not placeable, and say as such via `PlacementInfo#NOT_PLACEABLE`. However, if there is at least one item-like object in your recipe, you should create a `PlacementInfo`.

A `PlacementInfo` can be created via `create`, which takes in one or a list of ingredient, or `createFromOptionals`, which takes in a list of optional ingredients. If your recipe contains some representation of empty slots, then `createFromOptionals` should be used, providing an empty optional for an empty slot:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here
    private PlacementInfo info;

    @Override
    public PlacementInfo placementInfo() {
        // This delegate is in case the ingredient is not fully populated at this point in time
        // Tags and recipes are loaded at the same time, which is why this might be the case.
        if (this.info == null) {
            // Use optional ingredient as the block state may have an item representation
            List<Optional<Ingredient>> ingredients = new ArrayList<>();
            Item stateItem = this.inputState.getBlock().asItem();
            ingredients.add(stateItem != Items.AIR ? Optional.of(Ingredient.of(stateItem)): Optional.empty());
            ingredients.add(Optional.of(this.inputItem));

            // Create placement info
            this.info = PlacementInfo.createFromOptionals(ingredients);
        }

        return this.info;
    }
}
```

## Slot Displays

`SlotDisplay`s represent the information on what should render in what slot when viewed by a recipe consumer, like a recipe book. A `SlotDisplay` has two methods. First there's `resolve`, which takes in the `ContextMap` containing the available registries and fuel values (as shown in `SlotDisplayContext`); and the current `DisplayContentsFactory`, which accepts the contents to display for this slot; and returns the transformed list of contents into the output to be accepted. Then there's `type`, which holds the [`MapCodec`][codec] and [`StreamCodec`][streamcodec] used to encode/decode the display.

`SlotDisplay`s are typically implemented on the [`Ingredient` via `#display`, or `ICustomIngredient#display` for modded ingredients][ingredients]; however, in some cases, the input may not be an ingredient, meaning a `SlotDisplay` will need to use one available, or have a new one created.

These are the available slot displays provided by Vanilla and NeoForge:

- `SlotDisplay.Empty`: A slot that represents nothing.
- `SlotDisplay.ItemSlotDisplay`: A slot that respresents an item.
- `SlotDisplay.ItemStackSlotDisplay`: A slot that represents an item stack.
- `SlotDisplay.TagSlotDisplay`: A slot that represents an item tag.
- `SlotDisplay.WithRemainder`: A slot that represents some input that has some crafting remainder.
- `SlotDisplay.AnyFuel`: A slot that represents all fuel items.
- `SlotDisplay.Composite`: A slot that represents a combination of other slot displays.
- `SlotDisplay.SmithingTrimDemoSlotDisplay`: A slot that represents a random smithing drim being applied to some base with the given material.
- `FluidSlotDisplay`: A slot that represents a fluid.
- `FluidStackSlotDisplay`: A slot that represents a fluid stack.
- `FluidTagSlotDisplay`: A slot that represents a fluid tag.

We have three 'slots' in our recipe: the `BlockState` input, the `Ingredient` input, and the `ItemStack` result. The `Ingredient` input will already have an associated `SlotDisplay` and the `ItemStack` can be represented by `SlotDisplay.ItemStackSlotDisplay`. The `BlockState`, on the other hand, will need its own custom `SlotDisplay` and `DisplayContentsFactory`, as existing ones only take in item stacks, and for this example, block states are handled in a different fashion.

Starting with the `DisplayContentsFactory`, it is meant to be a transformer for some type to desired content display type. The available factories are:

- `DisplayContentsFactory.ForStacks`: A transformer that takes in `ItemStack`s.
- `DisplayContentsFactory.ForRemainders`: A transformer that takes in the input object and a list of remainder objects.
- `DisplayContentsFactory.ForFluidStacks`: A transformer that takes in a `FluidStack`.

With this, the `DisplayContentsFactory` can be implemented to transform the provided objects into the desired output. For example, `SlotDisplay.ItemStackContentsFactory`, takes the `ForStacks` transformer and has the stacks transformed into `ItemStack`s.

For our `BlockState`, we'll create a factory that takes in the state, along with a basic implementation that outputs the state itself.

```java
// A basic transformer for block states
public interface ForBlockStates<T> extends DisplayContentsFactory<T> {

    // Delegate methods
    default forState(Holder<Block> block) {
        return this.forState(block.value());
    }

    default forState(Block block) {
        return this.forState(block.defaultBlockState());
    }

    // The block state to take in and transform to the desired output
    T forState(BlockState state);
}

// An implementation for a block state output
public class BlockStateContentsFactory implements ForBlockStates<BlockState> {
    // Singleton instance
    public static final BlockStateContentsFactory INSTANCE = new BlockStateContentsFactory();

    private BlockStateContentsFactory() {}

    @Override
    public BlockState forState(BlockState state) {
        return state;
    }
}

// An implementation for an item stack output
public class BlockStateStackContentsFactory implements ForBlockStates<ItemStack> {
    // Singleton instance
    public static final BlockStateStackContentsFactory INSTANCE = new BlockStateStackContentsFactory();

    private BlockStateStackContentsFactory() {}

    @Override
    public ItemStack forState(BlockState state) {
        return new ItemStack(state.getBlock());
    }
}
```

Then, with that, we can create a new `SlotDisplay`. The `SlotDisplay.Type` must be [registered][registry]:

```java
// A simple slot display
public record BlockStateSlotDisplay(BlockState state) implements SlotDisplay {
    public static final MapCodec<BlockStateSlotDisplay> CODEC = BlockState.CODEC.fieldOf("state")
        .xmap(BlockStateSlotDisplay::new, BlockStateSlotDisplay::state);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockStateSlotDisplay> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), BlockStateSlotDisplay::state,
            BlockStateSlotDisplay::new
        );
    
    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return switch (factory) {
            // Check for our contents factory and transform if necessary
            case ForBlockStates<T> states -> Stream.of(states.forState(this.state));
            // If you want the contents to be handled differently depending on contents display
            //   then you can case on other displays like so
            case ForStacks<T> stacks -> Stream.of(stacks.forStack(state.getBlock().asItem()));
            // If no factories match, then do not return anything in the transformed stream
            default -> Stream.empty();
        }
    }

    @Override
    public SlotDisplay.Type<? extends SlotDisplay> type() {
        // Return the registered type from below
        return BLOCK_STATE_SLOT_DISPLAY.get();
    }
}

// In some registrar class
/// For some DeferredRegister<SlotDisplay.Type<?>> SLOT_DISPLAY_TYPES
public static final Supplier<SlotDisplay.Type<BlockStateSlotDisplay>> BLOCK_STATE_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register(
    "block_state",
    () -> new SlotDisplay.Type<>(BlockStateSlotDisplay.CODEC, BlockStateSlotDisplay.STREAM_CODEC)
);
```

## Recipe Display

A `RecipeDisplay` is the same as a `SlotDisplay`, except that it represents an entire recipe. The default interface only keeps track of the `result` of recipe and the `craftingStation` which represents the workbench where the recipe is applied. The `RecipeDisplay` also has a `type` that holds the [`MapCodec`][codec] and [`StreamCodec`][streamcodec] used to encode/decode the display. However, no available subtypes of `RecipeDisplay` contain all the information required to properly render our recipe on the client. As such, we will need to create our own `RecipeDisplay`.

All slots and ingredients should be represented as `SlotDisplay`s. Any restrictions, such as grid size, can be provided in any manner the user decides.

```java
// A simple recipe display
public record RightClickBlockRecipeDisplay(
    SlotDisplay inputState,
    SlotDisplay inputItem,
    SlotDisplay result, // Implements RecipeDisplay#result
    SlotDisplay craftingStation // Implements RecipeDisplay#craftingStation
) implements RecipeDisplay {
    public static final MapCodec<RightClickBlockRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
                    SlotDisplay.CODEC.fieldOf("inputState").forGetter(RightClickBlockRecipeDisplay::inputState),
                    SlotDisplay.CODEC.fieldOf("inputState").forGetter(RightClickBlockRecipeDisplay::inputItem),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(RightClickBlockRecipeDisplay::result),
                    SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(RightClickBlockRecipeDisplay::craftingStation)
                )
                .apply(instance, RightClickBlockRecipeDisplay::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RightClickBlockRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
        SlotDisplay.STREAM_CODEC,
        RightClickBlockRecipeDisplay::inputState,
        SlotDisplay.STREAM_CODEC,
        RightClickBlockRecipeDisplay::inputItem,
        SlotDisplay.STREAM_CODEC,
        RightClickBlockRecipeDisplay::result,
        SlotDisplay.STREAM_CODEC,
        RightClickBlockRecipeDisplay::craftingStation,
        RightClickBlockRecipeDisplay::new
    );

    @Override
    public RecipeDisplay.Type<? extends RecipeDisplay> type() {
        // Return the registered type from below
        return RIGHT_CLICK_BLOCK_RECIPE_DISPLAY.get();
    }
}

// In some registrar class
/// For some DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAY_TYPES
public static final Supplier<RecipeDisplay.Type<RightClickBlockRecipeDisplay>> RIGHT_CLICK_BLOCK_RECIPE_DISPLAY = RECIPE_DISPLAY_TYPES.register(
    "right_click_block",
    () -> new RecipeDisplay.Type<>(RightClickBlockRecipeDisplay.CODEC, RightClickBlockRecipeDisplay.STREAM_CODEC)
);
```

Then we can create the recipe display for the recipe by overriding `#display` like so:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here

    @Override
    public List<RecipeDisplay> display() {
        // You can have many different displays for the same recipe
        // But this example will only use one like the other recipes.
        return List.of(
            // Add our recipe display with the specified slots
            new RightClickBlockRecipeDisplay(
                new BlockStateSlotDisplay(this.inputState),
                this.inputItem.display(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.GRASS_BLOCK)
            )
        )
    }
}
```

## The Recipe Type

Next up, our recipe type. This is fairly straightforward because there's no data other than a name associated with a recipe type. They are one of two [registered][registry] parts of the recipe system, so like with all other registries, we create a `DeferredRegister` and register to it:

```java
public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, ExampleMod.MOD_ID);

public static final Supplier<RecipeType<RightClickBlockRecipe>> RIGHT_CLICK_BLOCK_TYPE =
        RECIPE_TYPES.register(
                "right_click_block",
                // We need the qualifying generic here due to generics being generics.
                registryName -> new RecipeType<RightClickBlockRecipe> {

                    @Override
                    public String toString() {
                        return registryName.toString();
                    }
                }
        );
```

After we have registered our recipe type, we must override `#getType` in our recipe, like so:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here

    @Override
    public RecipeType<? extends Recipe<RightClickBlockInput>> getType() {
        return RIGHT_CLICK_BLOCK_TYPE.get();
    }
}
```

## The Recipe Serializer

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
public static final DeferredRegister<RecipeType<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExampleMod.MOD_ID);

public static final Supplier<RecipeSerializer<RightClickBlockRecipe>> RIGHT_CLICK_BLOCK =
        RECIPE_SERIALIZERS.register("right_click_block", RightClickBlockRecipeSerializer::new);
```

And similarly, we must also override `#getSerializer` in our recipe, like so:

```java
public class RightClickBlockRecipe implements Recipe<RightClickBlockInput> {
    // other stuff here

    @Override
    public RecipeSerializer<? extends Recipe<RightClickBlockInput>> getSerializer() {
        return RIGHT_CLICK_BLOCK.get();
    }
}
```

## The Crafting Mechanic

Now that all parts of your recipe are complete, you can make yourself some recipe JSONs (see the [datagen] section for that) and then query the recipe manager for your recipes, like above. What you then do with the recipe is up to you. A common use case would be a machine that can process your recipes, storing the active recipe as a field.

In our case, however, we want to apply the recipe when an item is right-clicked on a block. We will do so using an [event handler][event]. Keep in mind that this is an example implementation, and you can alter this in any way you like (so long as you run it on the server). As we want the interaction state to match on both the client and server, we will also need to [sync any relevant input states across the network][networking].

We can set up a simple network implementation to sync the recipe inputs like so:

```java
// A basic packet class, must be registered.
public record ClientboundRightClickBlockRecipesPayload(
    Set<BlockState> inputStates, Set<Holder<Item>> inputItems
) implements CustomPacketPayload {

    // ...
}

// Packet stores data in an instance class.
// Present on both server and client to do initial matching.
public interface RightClickBlockRecipeInputs {

    Set<BlockState> inputStates();
    Set<Holder<Item>> inputItems();

    default boolean test(BlockState state, ItemStack stack) {
        return this.inputStates().contains(state) && this.inputItems().contains(stack.getItemHolder());
    }
}

// Server resource listener so it can be reloaded when recipes are.
public class ServerRightClickBlockRecipeInputs implements ResourceManagerReloadListener, RightClickBlockRecipeInputs {

    private final RecipeManager recipeManager;

    private Set<BlockState> inputStates;
    private Set<Holder<Item>> inputItems;

    public RightClickBlockRecipeInputs(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    // Set inputs here as #apply is fired synchronously based on listener registration order.
    // Recipes are always applied first.
    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) { // Should never be null
            // Populate inputs
            Set<BlockState> inputStates = new HashSet<>();
            Set<Holder<Item>> inputItems = new HashSet<>();

            this.recipeManager.recipeMap().byType(RIGHT_CLICK_BLOCK_TYPE.get())
                .forEach(holder -> {
                    var recipe = holder.value();
                    inputStates.add(recipe.getInputState());
                    inputItems.addAll(recipe.getInputItem().items());
                });
            
            this.inputStates = Set.unmodifiableSet(inputStates);
            this.inputItems = Set.unmodifiableSet(inputItems);
        }
    }

    public void syncToClient(Stream<ServerPlayer> players) {
        ClientboundRightClickBlockRecipesPayload payload =
            new ClientboundRightClickBlockRecipesPayload(this.inputStates, this.inputItems);
        players.forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    @Override
    public Set<BlockState> inputStates() {
        return this.inputStates;
    }

    @Override
    public Set<Holder<Item>> inputItems() {
        return this.inputItems;
    }
}

// Client implementation to hold the inputs.
public record ClientRightClickBlockRecipeInputs(
    Set<BlockState> inputStates, Set<Holder<Item>> inputItems
) implements RightClickBlockRecipeInputs {

    public ClientRightClickBlockRecipeInputs(Set<BlockState> inputStates, Set<Holder<Item>> inputItems) {
        this.inputStates = Set.unmodifiableSet(inputStates);
        this.inputItems = Set.unmodifiableSet(inputItems);
    }
}

// Handling the recipe instance depending on side.
public class ServerRightClickBlockRecipes {

    private static ServerRightClickBlockRecipeInputs inputs;

    public static RightClickBlockRecipeInputs inputs() {
        return ServerRightClickBlockRecipes.inputs;
    }

    @SubscribeEvent // on the game event bus
    public static void addListener(AddReloadListenerEvent event) {
        // Register server reload listener
        ServerRightClickBlockRecipes.inputs = new ServerRightClickBlockRecipeInputs(
            event.getServerResources().getRecipeManager()
        );
        event.addListener(ServerRightClickBlockRecipes.inputs);
    }

    @SubscribeEvent // on the game event bus
    public static void datapackSync(OnDatapackSyncEvent event) {
        // Send to client
        ServerRightClickBlockRecipes.inputs.syncToClient(event.getRelevantPlayers());
    }
}
public class ClientRightClickBlockRecipes {

    private static ClientRightClickBlockRecipeInputs inputs;

    public static RightClickBlockRecipeInputs inputs() {
        return ClientRightClickBlockRecipes.inputs;
    }

    // Handling the sent packet
    public static void handle(final ClientboundRightClickBlockRecipesPayload data, final IPayloadContext context) {
        // Do something with the data, on the main thread
        ClientRightClickBlockRecipes.inputs = new ClientRightClickBlockRecipeInputs(
            data.inputStates(), data.inputItems()
        );
    }
}

public class RightClickBlockRecipes {
    // Make proxy method to access properly
    public static RightClickBlockRecipeInputs inputs(Level level) {
        return level.isClientSide
            ? ClientRightClickBlockRecipes.inputs()
            : ServerRightClickBlockRecipes.inputs();
    }
}
```

Then, using the synced inputs, we can check the game for the used inputs:

```java
@SubscribeEvent // on the game event bus
public static void useItemOnBlock(UseItemOnBlockEvent event) {
    // Skip if we are not in the block-dictated phase of the event. See the event's javadocs for details.
    if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) return;
    // Get parameters to check input first
    Level level = event.getLevel();
    BlockPos pos = event.getPos();
    BlockState blockState = level.getBlockState(pos);
    ItemStack itemStack = event.getItemStack();

    // Check if the input can result in a recipe on both sides
    if (!RightClickBlockRecipes.inputs(level).test(blockState, itemStack)) return;

    // If so, make sure on server before checking recipe
    if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
        // Create an input and query the recipe.
        RightClickBlockInput input = new RightClickBlockInput(blockState, itemStack);
        Optional<RecipeHolder<? extends Recipe<CraftingInput>>> optional = serverLevel.recipeAccess().getRecipeFor(
            // The recipe type.
            RIGHT_CLICK_BLOCK_TYPE.get(),
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
            ItemEntity entity = new ItemEntity(level,
                    // Center of pos.
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    result);
            level.addFreshEntity(entity);
        }
    }

    // Cancel the event to stop the interaction pipeline regardless of side.
    // Already made sure that there could be a result.
    event.cancelWithResult(InteractionResult.SUCCESS_SERVER);
}
```

## Data Generation

To create a recipe builder for your own recipe serializer(s), you need to implement `RecipeBuilder` and its methods. A common implementation, partially copied from vanilla, would look like this:

```java
// This class is abstract because there is a lot of per-recipe-serializer logic.
// It serves the purpose of showing the common part of all (vanilla) recipe builders.
public abstract class SimpleRecipeBuilder implements RecipeBuilder {
    // Make the fields protected so our subclasses can use them.
    protected final ItemStack result;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    protected final String group;

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

    // Saves a recipe using the given RecipeOutput and key. This method is defined in the RecipeBuilder interface.
    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        // Build the advancement.
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);
        // Our factory parameters are the result, the block state, and the ingredient.
        RightClickBlockRecipe recipe = new RightClickBlockRecipe(this.inputState, this.inputItem, this.result);
        // Pass the id, the recipe, and the recipe advancement into the RecipeOutput.
        output.accept(key, recipe, advancement.build(key.location().withPrefix("recipes/")));
    }
}
```

And now, during [datagen][recipedatagen], you can call on your recipe builder like any other:

```java
@Override
protected void buildRecipes(RecipeOutput output) {
    new RightClickRecipeBuilder(
            // Our constructor parameters. This example adds the ever-popular dirt -> diamond conversion.
            new ItemStack(Items.DIAMOND),
            Blocks.DIRT.defaultBlockState(),
            Ingredient.of(Items.APPLE)
    )
            .unlockedBy("has_apple", this.has(Items.APPLE))
            .save(output);
    // other recipe builders here
}
```

:::note
It is also possible to have `SimpleRecipeBuilder` be merged into `RightClickBlockRecipeBuilder` (or your own recipe builder), especially if you only have one or two recipe builders. The abstraction here serves to show which parts of the builder are recipe-dependent and which are not.
:::

[codec]: ../../../datastorage/codecs.md
[datagen]: #data-generation
[event]: ../../../concepts/events.md
[ingredients]: ingredients.md
[networking]: ../../../networking/payload.md
[recipedatagen]: index.md#data-generation
[registry]: ../../../concepts/registries.md#methods-for-registering
[streamcodec]: ../../../networking/streamcodecs.md
