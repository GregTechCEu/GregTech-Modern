# Block Entities

`BlockEntities` are like simplified `Entities` that are bound to a Block. They are used to store dynamic data, execute tick based tasks, and dynamic rendering. Some examples from vanilla Minecraft would be handling of inventories on chests, smelting logic on furnaces, or area effects on beacons. More advanced examples exist in mods, such as quarries, sorting machines, pipes, and displays.

:::note
`BlockEntities` aren't a solution for everything and they can cause lag when used incorrectly. When possible, try to avoid them.
:::

## Registering

Block Entities are created and removed dynamically and as such are not registry objects on their own.

In order to create a `BlockEntity`, you need to extend the `BlockEntity` class. As such, another object is registered instead to easily create and refer to the *type* of the dynamic object. For a `BlockEntity`, these are known as `BlockEntityType`s.

A `BlockEntityType` can be [registered][registration] like any other registry object. To construct a `BlockEntityType`, its builder form can be used via `BlockEntityType$Builder#of`. This takes in two arguments: a `BlockEntityType.BlockEntitySupplier` which takes in a `BlockPos` and `BlockState` to create a new instance of the associated `BlockEntity`, and a varargs of `Block`s which this `BlockEntity` can be attached to. Building the `BlockEntityType` is done by calling `BlockEntityType$Builder#build`. This takes in a `Type` which represents the type-safe reference used to refer to this registry object in a `DataFixer`. Since `DataFixer`s are an optional system to use for mods, this can be passed as `null`.

```java
// For some DeferredRegister<BlockEntityType<?>> REGISTER
public static final Supplier<BlockEntityType<MyBE>> MY_BE = REGISTER.register("mybe", () -> BlockEntityType.Builder.of(MyBE::new, validBlocks).build(null));

// In MyBE, a BlockEntity subclass
public MyBE(BlockPos pos, BlockState state) {
  super(MY_BE.get(), pos, state);
}
```

## Creating a `BlockEntity`

To create a `BlockEntity` and attach it to a `Block`, the `EntityBlock` interface must be implemented on your `Block` subclass. The method `EntityBlock#newBlockEntity(BlockPos, BlockState)` must be implemented and return a new instance of your `BlockEntity`.

## Storing Data within your `BlockEntity`

In order to save data, override the following two methods:
```java
BlockEntity#saveAdditional(CompoundTag tag, HolderLookup.Provider registries)

BlockEntity#loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
```
These methods are called whenever the `LevelChunk` containing the `BlockEntity` gets loaded from/saved to a tag.
Use them to read and write to the fields in your block entity class.

:::note
Whenever your data changes, you need to call `BlockEntity#setChanged`; otherwise, the `LevelChunk` containing your `BlockEntity` might be skipped while the level is saved.
:::

:::danger
It is important that you call the `super` methods!

The tag names `id`, `x`, `y`, `z`, `NeoForgeData` and `neoforge:attachments` are reserved by the `super` methods.
:::

## Ticking `BlockEntities`

If you need a ticking `BlockEntity`, for example to keep track of the progress during a smelting process, another method must be implemented and overridden within `EntityBlock`: `EntityBlock#getTicker(Level, BlockState, BlockEntityType)`. This can implement different tickers depending on which logical side the user is on, or just implement one general ticker. In either case, a `BlockEntityTicker` must be returned. Since this is a functional interface, it can just take in a method representing the ticker instead:

```java
// Inside some Block subclass

// We use a second method here due to generic conversions
// If extending `BaseEntityBlock`, this method is also available there as a protected static method
private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
    BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
) {
    return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
}

@Nullable
@Override
public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    // You can return different tickers here, depending on whatever factors you want. A common use case would be
    // to return different tickers on the client or server, only tick one side to begin with,
    // or only return a ticker for some blockstates (e.g. when using a "my machine is working" blockstate property).
    return createTickerHelper(type, MY_BLOCK_ENTITY.get(), MyBlockEntity::tick);
}

// Inside MyBlockEntity
public static void tick(Level level, BlockPos pos, BlockState state, MyBlockEntity blockEntity) {
  // Do stuff
}
```

:::note
This method is called each tick; therefore, you should avoid having complicated calculations in here. If possible, you should make more complex calculations every X ticks. (The amount of ticks in a second may be lower then 20 (twenty) but won't be higher)
:::

## Synchronizing the Data to the Client

There are three ways of syncing data to the client: synchronizing on chunk load, on block updates, and with a custom network message.

### Synchronizing on LevelChunk Load

For this you need to override
```java
BlockEntity#getUpdateTag(HolderLookup.Provider registries)

IBlockEntityExtension#handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries)
```

The first method collects the data that should be sent to the client while the second one processes that data. If your `BlockEntity` doesn't contain much data, you might be able to use the methods out of the [Storing Data within your `BlockEntity`][storing-data] section.

:::caution
Synchronizing excessive/useless data for block entities can lead to network congestion. You should optimize your network usage by sending only the information the client needs when the client needs it. For instance, it is more often than not unnecessary to send the inventory of a block entity in the update tag, as this can be synchronized via its [`AbstractContainerMenu`][menu].
:::

### Synchronizing on Block Update

This method is a bit more complicated, but again you just need to override two or three methods. Here is a tiny example implementation of it:

```java
// In some subclass of BlockEntity
@Override
public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
  CompoundTag tag = new CompoundTag();
  //Write your data into the tag
  return tag;
}

@Override
public Packet<ClientGamePacketListener> getUpdatePacket() {
  // Will get tag from #getUpdateTag
  return ClientboundBlockEntityDataPacket.create(this);
}

// Can override IBlockEntityExtension#onDataPacket. By default, this will defer to  BlockEntity#loadWithComponents.
```
The static constructors `ClientboundBlockEntityDataPacket#create` takes:

- The `BlockEntity`.
- An optional function to get the `CompoundTag` from the `BlockEntity`. By default, this uses `BlockEntity#getUpdateTag`.

Now, to send the packet, an update notification must be given on the server.

```java
Level#sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags)
```

- The `pos` should be your `BlockEntity`'s position.
- For `oldState` and `newState`, you can pass the current `BlockState` at that position.
- `flags` is a bitmask that should contain `2`, which will sync the changes to the client. See `Block` for more info as well as the rest of the flags. The flag `2` is equivalent to `Block#UPDATE_CLIENTS`.

### Synchronizing Using a Custom Network Message

This way of synchronizing is probably the most complicated but is usually the most optimized, as you can make sure that only the data you need to be synchronized is actually synchronized. You should first check out the [`Networking`][networking] section and especially [`PayloadRegistrar`][payload] before attempting this. Once you've created your custom network message, you can send it to all users that have the `BlockEntity` loaded with `PacketDistrubtor#sendToPlayersTrackingChunk`.

:::caution
It is important that you do safety checks, the `BlockEntity` might already be destroyed/replaced when the message arrives at the player! You should also check if the chunk is loaded (`Level#hasChunkAt(BlockPos)`).
:::

[registration]: ../concepts/registries.md#methods-for-registering
[storing-data]: #storing-data-within-your-blockentity
[menu]: ../gui/menus.md
[networking]: ../networking/index.md
[payload]: ../networking/payload.md
