# Blocks

Blocks are essential to the Minecraft world. They make up all the terrain, structures, and machines. Chances are if you are interested in making a mod, then you will want to add some blocks. This page will guide you through the creation of blocks, and some of the things you can do with them.

## One Block to Rule Them All

Before we get started, it is important to understand that there is only ever one of each block in the game. A world consists of thousands of references to that one block in different locations. In other words, the same block is just displayed a lot of times.

Due to this, a block should only ever be instantiated once, and that is during [registration]. Once the block is registered, you can then use the registered reference as needed.

Unlike most other registries, blocks can use a specialized version of `DeferredRegister`, called `DeferredRegister.Blocks`. `DeferredRegister.Blocks` acts basically like a `DeferredRegister<Block>`, but with some minor differences:

- They are created via `DeferredRegister.createBlocks("yourmodid")` instead of the regular `DeferredRegister.create(...)` method.
- `#register` returns a `DeferredBlock<T extends Block>`, which extends `DeferredHolder<Block, T>`. `T` is the type of the class of the block we are registering.
- There are a few helper methods for registering block. See [below] for more details.

So now, let's register our blocks:

```java
//BLOCKS is a DeferredRegister.Blocks
public static final DeferredBlock<Block> MY_BLOCK = BLOCKS.register("my_block", registryName -> new Block(...));
```

After registering the block, all references to the new `my_block` should use this constant. For example, if you want to check if the block at a given position is `my_block`, the code for that would look something like this:

```java
level.getBlockState(position) // returns the blockstate placed in the given level (world) at the given position
    //highlight-next-line
    .is(MyBlockRegistrationClass.MY_BLOCK);
```

This approach also has the convenient effect that `block1 == block2` works and can be used instead of Java's `equals` method (using `equals` still works, of course, but is pointless since it compares by reference anyway).

:::danger
Do not call `new Block()` outside registration! As soon as you do that, things can and will break:

- Blocks must be created while registries are unfrozen. NeoForge unfreezes registries for you and freezes them later, so registration is your time window to create blocks.
- If you try to create and/or register a block when registries are frozen again, the game will crash and report a `null` block, which can be very confusing.
- If you still manage to have a dangling block instance, the game will not recognize it while syncing and saving, and replace it with air.
:::

## Creating Blocks

As discussed before, we start by creating our `DeferredRegister.Blocks`:

```java
public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("yourmodid");
```

### Basic Blocks

For simple blocks which need no special functionality (think cobblestone, wooden planks, etc.), the `Block` class can be used directly. To do so, during registration, instantiate `Block` with a `BlockBehaviour.Properties` parameter. This `BlockBehaviour.Properties` parameter can be created using `BlockBehaviour.Properties#of`, and it can be customized by calling its methods. The most important methods for this are:

- `setId` - Sets the resource key of the block.
    - This **must** be set on every block; otherwise, an exception will be thrown.
- `destroyTime` - Determines the time the block needs to be destroyed.
    - Stone has a destroy time of 1.5, dirt has 0.5, obsidian has 50, and bedrock has -1 (unbreakable).
- `explosionResistance` - Determines the explosion resistance of the block.
    - Stone has an explosion resistance of 6.0, dirt has 0.5, obsidian has 1,200, and bedrock has 3,600,000.
- `sound` - Sets the sound the block makes when it is punched, broken, or placed.
    - The default value is `SoundType.STONE`. See the [Sounds page][sounds] for more details.
- `lightLevel` - Sets the light emission of the block. Accepts a function with a `BlockState` parameter that returns a value between 0 and 15.
    - For example, glowstone uses `state -> 15`, and torches use `state -> 14`.
- `friction` - Sets the friction (slipperiness) of the block.
    - Default value is 0.6. Ice uses 0.98.

So for example, a simple implementation would look something like this:

```java
//BLOCKS is a DeferredRegister.Blocks
public static final DeferredBlock<Block> MY_BETTER_BLOCK = BLOCKS.register(
    "my_better_block", 
    registryName -> new Block(BlockBehaviour.Properties.of()
        //highlight-start
        .setId(ResourceKey.create(Registries.BLOCK, registryName))
        .destroyTime(2.0f)
        .explosionResistance(10.0f)
        .sound(SoundType.GRAVEL)
        .lightLevel(state -> 7)
        //highlight-end
    ));
```

For further documentation, see the source code of `BlockBehaviour.Properties`. For more examples, or to look at the values used by Minecraft, have a look at the `Blocks` class.

:::note
It is important to understand that a block in the world is not the same thing as in an inventory. What looks like a block in an inventory is actually a `BlockItem`, a special type of [item] that places a block when used. This also means that things like the creative tab or the max stack size are handled by the corresponding `BlockItem`.

A `BlockItem` must be registered separately from the block. This is because a block does not necessarily need an item, for example if it is not meant to be collected (as is the case with fire, for example).
:::

### More Functionality

Directly using `Block` only allows for very basic blocks. If you want to add functionality, like player interaction or a different hitbox, a custom class that extends `Block` is required. The `Block` class has many methods that can be overridden to do different things; see the classes `Block`, `BlockBehaviour` and `IBlockExtension` for more information. See also the [Using blocks][usingblocks] section below for some of the most common use cases for blocks.

If you want to make a block that has different variants (think a slab that has a bottom, top, and double variant), you should use [blockstates]. And finally, if you want a block that stores additional data (think a chest that stores its inventory), a [block entity][blockentities] should be used. The rule of thumb here is that if you have a finite and reasonably small amount of states (= a few hundred states at most), use blockstates, and if you have an infinite or near-infinite amount of states, use a block entity.

#### Block Types

Block types are [`MapCodec`s][codec] used to serialize and deserialize a block object. This `MapCodec` is set via `BlockBehaviour#codec` and [registered][registration] to the block type registry. Currently, its only use is when the block list report is being generated. A block type should be created once for every subclass of `Block`. For example, `FlowerBlock#CODEC` represents the block type for most flowers while its subclass `WitherRoseBlock` has a separate block type.

If the block subclass only takes in the `BlockBehaviour.Properties`, then `BlockBehaviour#simpleCodec` can be used to create the `MapCodec`.

```java
// For some block subclass
public class SimpleBlock extends Block {
    public SimpleBlock(BlockBehavior.Properties properties) {
        // ...
    }

    @Override
    public MapCodec<SimpleBlock> codec() {
        return SIMPLE_CODEC.get();
    }
}

// In some registration class
public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, "yourmodid");

public static final Supplier<MapCodec<SimpleBlock>> SIMPLE_CODEC = REGISTRAR.register(
    "simple",
    () -> BlockBehaviour.simpleCodec(SimpleBlock::new)
);
```

If the block subclass contains more parameters, then [`RecordCodecBuilder#mapCodec`][codec] should be used to create the `MapCodec`, passing in `BlockBehaviour#propertiesCodec` for the `BlockBehaviour.Properties` parameter.

```java
// For some block subclass
public class ComplexBlock extends Block {
    public ComplexBlock(int value, BlockBehavior.Properties properties) {
        // ...
    }

    @Override
    public MapCodec<ComplexBlock> codec() {
        return COMPLEX_CODEC.get();
    }

    public int getValue() {
        return this.value;
    }
}

// In some registration class
public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, "yourmodid");

public static final Supplier<MapCodec<ComplexBlock>> COMPLEX_CODEC = REGISTRAR.register(
    "simple",
    () -> RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.INT.fieldOf("value").forGetter(ComplexBlock::getValue),
            BlockBehaviour.propertiesCodec() // represents the BlockBehavior.Properties parameter
        ).apply(instance, ComplexBlock::new)
    )
);
```

:::note
Although block types are basically unused at the moment, it is expected to become more important in the future as Mojang continues moving towards a codec-centered structure.
:::

### `DeferredRegister.Blocks` helpers

We already discussed how to create a `DeferredRegister.Blocks` [above], as well as that it returns `DeferredBlock`s. Now, let's have a look at what other utilities the specialized `DeferredRegister` has to offer. Let's start with `#registerBlock`:

```java
public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("yourmodid");

public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.register(
    "example_block", registryName -> new Block(
        BlockBehaviour.Properties.of()
            // The ID must be set on the block
            .setId(ResourceKey.create(Registries.BLOCK, registryName))
    )
);

// Same as above, except that the block properties are constructed eagerly.
// setId is also called internally on the properties object.
public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerBlock(
    "example_block",
    Block::new, // The factory that the properties will be passed into.
    BlockBehaviour.Properties.of() // The properties to use.
);
```

If you want to use `Block::new`, you can leave out the factory entirely:

```java
public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock(
    "example_block",
    BlockBehaviour.Properties.of() // The properties to use.
);
```

This does the exact same as the previous example, but is slightly shorter. Of course, if you want to use a subclass of `Block` and not `Block` itself, you will have to use the previous method instead.

### Resources

If you register your block and place it in the world, you will find it to be missing things like a texture. This is because [textures], among others, are handled by Minecraft's resource system. When adding a new block in Minecraft, you should either write or [generate][datagen] the following files:

- A [blockstate file][bsfile]
- A [block model][model]
- A [translation][i18n]
- A [loot table][loottable]
- Some block [tags], e.g. for mining

For all of the above, also reference the files and data generators of similar vanilla blocks.

## Using Blocks

Blocks are very rarely directly used to do things. In fact, probably two of the most common operations in all of Minecraft - getting the block at a position, and setting a block at a position - use blockstates, not blocks. The general design approach is to have the block define behavior, but have the behavior actually run through blockstates. Due to this, `BlockState`s are often passed to methods of `Block` as a parameter. For more information on how blockstates are used, and on how to get one from a block, see [Using Blockstates][usingblockstates].

In several situations, multiple methods of `Block` are used at different times. The following subsections list the most common block-related pipelines. Unless specified otherwise, all methods are called on both logical sides and should return the same result on both sides.

### Placing a Block

Block placement logic is called from `BlockItem#useOn` (or some subclass's implementation thereof, such as in `PlaceOnWaterBlockItem`, which is used for lily pads). For more information on how the game gets there, see [Right-Clicking Items][rightclick]. In practice, this means that as soon as a `BlockItem` is right-clicked (for example a cobblestone item), this behavior is called.

- Several prerequisites are checked, for example that you are not in spectator mode, that all required feature flags for the block are enabled or that the target position is not outside the world border. If at least one of these checks fails, the pipeline ends.
- `BlockBehaviour#canBeReplaced` is called for the block currently at the position where the block is attempted to be placed. If it returns `false`, the pipeline ends. Prominent cases that return `true` here are tall grass or snow layers.
- `Block#getStateForPlacement` is called. This is where, depending on the context (which includes information like the position, the rotation and the side the block is placed on), different block states can be returned. This is useful for example for blocks that can be placed in different directions.
- `BlockBehaviour#canSurvive` is called with the blockstate obtained in the previous step. If it returns `false`, the pipeline ends.
- The blockstate is set into the level via a `Level#setBlock` call.
    - In that `Level#setBlock` call, `BlockBehaviour#onPlace` is called.
- `Block#setPlacedBy` is called.

### Breaking a Block

Breaking a block is a bit more complex, as it requires time. The process can be roughly divided into three stages: "initiating", "mining" and "actually breaking".

- When the left mouse button is clicked, the "initiating" stage is entered. 
- Now, the left mouse button needs to be held down, entering the "mining" stage. **This stage's methods are called every tick.**
- If the "continuing" stage is not interrupted (by releasing the left mouse button) and the block is broken, the "actually breaking" stage is entered.

Or for those who prefer pseudocode:

```java
leftClick();
initiatingStage();
while (leftClickIsBeingHeld()) {
    miningStage();
    if (blockIsBroken()) {
        actuallyBreakingStage();
        break;
    }
}
```

The following subsections further break down these stages into actual method calls. For information about how the game gets from left-clicking to this pipeline, see [Left-Clicking an Item][leftclick].

#### The "Initiating" Stage

- Several prerequisites are checked, for example that you are not in spectator mode, that all required feature flags for the `ItemStack` in your main hand are enabled or that the block in question is not outside the world border. If at least one of these checks fails, the pipeline ends.
- `PlayerInteractEvent.LeftClickBlock` is fired. If the event is canceled, the pipeline ends.
    - Note that when the event is canceled on the client, no packets are sent to the server and thus no logic runs on the server.
    - However, canceling this event on the server will still cause client code to run, which can lead to desyncs!
- `BlockBehaviour#attack` is called.

#### The "Mining" Stage

- `PlayerInteractEvent.LeftClickBlock` is fired. If the event is canceled, the pipeline moves to the "finishing" stage.
    - Note that when the event is canceled on the client, no packets are sent to the server and thus no logic runs on the server.
    - However, canceling this event on the server will still cause client code to run, which can lead to desyncs!
- `BlockBehaviour#getDestroyProgress` is called and added to the internal destroy progress counter.
    - `BlockBehaviour#getDestroyProgress` returns a float value between 0 and 1, representing how much the destroy progress counter should be increased every tick.
- The progress overlay (cracking texture) is updated accordingly.
- If the destroy progress is greater than 1.0 (i.e. completed, i.e. the block should be broken), the "mining" stage is exited and the "actually breaking" stage is entered.

#### The "Actually Breaking" Stage

- `Item#canDestroyBlock` is called. If it returns `false` (determining that the block should not be broken), the pipeline moves to the "finishing" stage.
- `Player#canUseGameMasterBlocks` is called if the block is an instance of `GameMasterBlock`. This determines whether the player has the ability to destroy creative-only blocks. If `false`, the pipeline moves to the "finishing" stage.
- Server-only: `Player#blockActionRestricted` is called. This determines whether the current player cannot break the block. If `true`, the pipeline moves to the "finishing" stage.
- Server-only: `BlockEvent.BreakEvent` is fired. If canceled, the pipeline moves to the "finishing" stage. The initial canceled state is determined by the above three methods.
- `Block#playerWillDestroy` is called.
- Server-only: `IBlockExtension#canHarvestBlock` is called. This determines whether the block can be harvested, i.e. broken with drops. This will be ignored if `Player#preventsBlockDrops` returns true.
    - Server-only: `PlayerEvent.HarvestCheck` is fired if `IBlockExtension#canHarvestBlock` is not overriden without its super call. If `HarvestCheck#canHarvest` returns `false`, then `Block#playerDestroy` will not be called, preventing any resources or experience from dropping.
- `IBlockExtension#onDestroyedByPlayer` is called. If it returns `false`, the pipeline moves to the "finishing" stage.
    - The blockstate is removed from the level via a `Level#setBlock` call with `Blocks.AIR.defaultBlockState()` or the current logged fluid as the blockstate parameter.
        - In that `Level#setBlock` call, `Block#onRemove` is called.
    - `Block#destroy` is called if `IBlockExtension#onDestroyedByPlayer` returns `true`.
- Server-only: If the previous call to `IBlockExtension#canHarvestBlock` and `IBlockExtension#onDestroyedByPlayer` return `true`, then `Block#playerDestroy` is called.
    - Server-only: `Block#dropResources` is called. This determines what drops from the block when mined, including experience.
        - Server-only: `BlockDropsEvent` is fired. If the event is canceled, then nothing is dropped when the block breaks. Otherwise, every `ItemEntity` in `BlockDropsEvent#getDrops` is added to the current level. Additionally, `Block#popExperience` is called if `getDroppedExperience` is greater than 0.
            - Server-only: `IBlockExtension#getExpDrop` is called, augmented by `EnchantmentHelper#processBlockExperience`. This is the initial value set for `BlockDropsEvent#getDroppedExperience` before potentally being modified.
- Server-only: `PlayerDestroyItemEvent` is fired if the item used to mine the block broke at any point in the above process.

#### Mining Speed

The mining speed is calculated from the block's hardness, the used [tool]'s speed, and several entity [attributes] according to the following rules:

```java
// This will return the tool's mining speed, or 1 if the held item is either empty, not a tool,
// or not applicable for the block being broken.
float destroySpeed = item.getDestroySpeed(blockState);
// If we have an applicable tool, add the minecraft:mining_efficiency attribute as an additive modifier.
if (destroySpeed > 1) {
    destroySpeed += player.getAttributeValue(Attributes.MINING_EFFICIENCY);
}
// Apply effects from haste or conduit power.
if (player.hasEffect(MobEffects.HASTE) || player.hasEffect(MobEffects.CONDUIT_POWER)) {
    int haste = player.hasEffect(MobEffects.HASTE)
        ? player.getEffect(MobEffects.HASTE).getAmplifier()
        : 0;
    int conduitPower = player.hasEffect(MobEffects.CONDUIT_POWER)
        ? player.getEffect(MobEffects.CONDUIT_POWER).getAmplifier()
        : 0;
    int amplifier = Math.max(haste, conduitPower);
    destroySpeed *= 1 + (amplifier + 1) * 0.2f;
}
// Apply slowness effect.
if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
    destroySpeed *= switch (player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
        case 0 -> 0.3F;
        case 1 -> 0.09F;
        case 2 -> 0.0027F;
        default -> 8.1E-4F;
    };
}
// Add the minecraft:block_break_speed attribute as a multiplicative modifier.
destroySpeed *= player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
// If the player is underwater, apply the underwater mining speed penalty multiplicatively.
if (player.isEyeInFluid(FluidTags.WATER)) {
    destroySpeed *= player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
}
// If the player is trying to break a block in mid-air, make the player mine 5 times slower.
if (!player.onGround()) {
    destroySpeed /= 5;
}
destroySpeed = /* The PlayerEvent.BreakSpeed event is fired here, allowing modders to further modify this value. */;
return destroySpeed;
```

The exact code for this can be found in `Player#getDestroySpeed` for reference.

### Ticking

Ticking is a mechanism that updates (ticks) parts of the game every 1 / 20 seconds, or 50 milliseconds ("one tick"). Blocks provide different ticking methods that are called in different ways.

#### Server Ticking and Tick Scheduling

`BlockBehaviour#tick` is called through scheduled ticks. Scheduled ticks can be created through `Level#scheduleTick(BlockPos, Block, int)`, where the `int` denotes a delay. This is used in various places by vanilla, for example, the tilting mechanism of big dripleaves heavily relies on this system. Other prominent users are various redstone components.

#### Client Ticking

`Block#animateTick` is called exclusively on the client, every frame. This is where client-only behavior, for example the torch particle spawning, happens.

#### Weather Ticking

Weather ticking is handled by `Block#handlePrecipitation` and runs independent of regular ticking. It is called only on the server, only when it is raining in some form, with a 1 in 16 chance. This is used for example by cauldrons that fill during rain or snowfall.

#### Random Ticking

The random tick system runs independent of regular ticking. Random ticks must be enabled through the `BlockBehaviour.Properties` of the block by calling the `BlockBehaviour.Properties#randomTicks()` method. This enables the block to be part of the random ticking mechanic.

Random ticks occur every tick for a set amount of blocks in a chunk. That set amount is defined through the `randomTickSpeed` gamerule. With its default value of 3, every tick, 3 random blocks from the chunk are chosen. If these blocks have random ticking enabled, then their respective `BlockBehaviour#randomTick` methods are called.

Random ticking is used by a wide range of mechanics in Minecraft, such as plant growth, ice and snow melting, or copper oxidizing.

[above]: #one-block-to-rule-them-all
[attributes]: ../entities/attributes.md
[below]: #deferredregisterblocks-helpers
[blockentities]: ../blockentities/index.md
[blockstates]: states.md
[bsfile]: ../resources/client/models/index.md#blockstate-files
[codec]: ../datastorage/codecs.md#records
[datagen]: ../resources/index.md#data-generation
[i18n]: ../resources/client/i18n.md
[item]: ../items/index.md
[leftclick]: ../items/interactions.md#left-clicking-an-item
[loottable]: ../resources/server/loottables/index.md
[model]: ../resources/client/models/index.md
[registration]: ../concepts/registries.md#methods-for-registering
[resources]: ../resources/index.md#assets
[rightclick]: ../items/interactions.md#right-clicking-an-item
[sounds]: ../resources/client/sounds.md
[tags]: ../resources/server/tags.md
[textures]: ../resources/client/textures.md
[tool]: ../items/tools.md
[usingblocks]: #using-blocks
[usingblockstates]: states.md#using-blockstates
