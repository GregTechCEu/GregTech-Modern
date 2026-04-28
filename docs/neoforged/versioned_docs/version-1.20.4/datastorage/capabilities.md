# Capabilities

Capabilities allow exposing features in a dynamic and flexible way without having to resort to directly implementing many interfaces.

In general terms, each capability provides a feature in the form of an interface.

NeoForge adds capability support to blocks, entities, and item stacks.
This will be explained in more detail in the following sections.

## Why use capabilities

Capabilities are designed to separate **what** a block, entity or item stack can do from **how** it does it.
If you are wondering whether capabilities are the right tool for a job, ask yourself the following questions:
1. Do I only care about **what** a block, entity or item stack can do, but not about **how** it does it?
2. Is the **what**, the behavior, only available for some blocks, entities, or item stacks, but not all of them?
3. Is the **how**, the implementation of that behavior, dependent on the specific block, entity or item stack?

Here are a few examples of good capability usage:
- *"I want my fluid container to be compatible with fluid containers from other mods, but I don't know the specifics of each fluid container."* - Yes, use the `IFluidHandler` capability.
- *"I want to count how many items are in some entity, but I do not know how the entity might store them."* - Yes, use the `IItemHandler` capability.
- *"I want to fill some item stack with power, but I do not know how the item stack might store it."* - Yes, use the `IEnergyStorage` capability.
- *"I want to apply some color to whatever block a player is currently targeting, but I do not know how the block will be transformed."* - Yes. NeoForge does not provide a capability to color blocks, but you can implement one yourself.

Here is an example of discouraged capability usage:
- *"I want to check if an entity is within the range of my machine."* - No, use a helper method instead.

## NeoForge-provided capabilities

NeoForge provides capabilities for the following three interfaces: `IItemHandler`, `IFluidHandler` and `IEnergyStorage`.

`IItemHandler` exposes an interface for handling inventory slots. The capabilities of type `IItemHandler` are:
- `Capabilities.ItemHandler.BLOCK`: automation-accessible inventory of a block (for chests, machines, etc).
- `Capabilities.ItemHandler.ENTITY`: inventory contents of an entity (extra player slots, mob/creature inventories/bags).
- `Capabilities.ItemHandler.ENTITY_AUTOMATION`: automation-accessible inventory of an entity (boats, minecarts, etc).
- `Capabilities.ItemHandler.ITEM`: contents of an item stack (portable backpacks and such).

`IFluidHandler` exposes an interface for handling fluid inventories. The capabilities of type `IFluidHandler` are:
- `Capabilities.FluidHandler.BLOCK`: automation-accessible fluid inventory of a block.
- `Capabilities.FluidHandler.ENTITY`: fluid inventory of an entity.
- `Capabilities.FluidHandler.ITEM`: fluid inventory of an item stack.
This capability is of the special `IFluidHandlerItem` type due to the way buckets hold fluids.

`IEnergyStorage` exposes an interface for handling energy containers. It is based on the RedstoneFlux API by TeamCoFH. The capabilities of type `IEnergyStorage` are:
- `Capabilities.EnergyStorage.BLOCK`: energy contained inside a block.
- `Capabilities.EnergyStorage.ENTITY`: energy containing inside an entity.
- `Capabilities.EnergyStorage.ITEM`: energy contained inside an item stack.

## Creating a capability

NeoForge supports capabilities for blocks, entities, and item stacks.

Capabilities allow looking up implementations of some APIs with some dispatching logic. The following kinds of capabilities are implemented in NeoForge:
- `BlockCapability`: capabilities for blocks and block entities; behavior depends on the specific `Block`.
- `EntityCapability`: capabilities for entities: behavior dependends on the specific `EntityType`.
- `ItemCapability`: capabilities for item stacks: behavior depends on the specific `Item`.

:::tip
For compatibility with other mods,
we recommend using the capabilities provided by NeoForge in the `Capabilities` class if possible.
Otherwise, you can create your own as described in this section.
:::

Creating a capability is a single function call, and the resulting object should be stored in a `static final` field.
The following parameters must be provided:
- The name of the capability.
Creating a capability with the same name multiple times will always return the same object.
Capabilities with different names are **completely independent**, and can be used for different purposes.
- The behavior type that is being queried. This is the `T` type parameter.
- The type for additional context in the query. This is the `C` type parameter.

For example, here is how a capability for side-aware block `IItemHandler`s might be declared:

```java
public static final BlockCapability<IItemHandler, @Nullable Direction> ITEM_HANDLER_BLOCK =
    BlockCapability.create(
        // Provide a name to uniquely identify the capability.
        new ResourceLocation("mymod", "item_handler"),
        // Provide the queried type. Here, we want to look up `IItemHandler` instances.
        IItemHandler.class,
        // Provide the context type. We will allow the query to receive an extra `Direction side` parameter.
        Direction.class);
```

A `@Nullable Direction` is so common for blocks that there is a dedicated helper:
```java
public static final BlockCapability<IItemHandler, @Nullable Direction> ITEM_HANDLER_BLOCK =
    BlockCapability.createSided(
        // Provide a name to uniquely identify the capability.
        new ResourceLocation("mymod", "item_handler"),
        // Provide the queried type. Here, we want to look up `IItemHandler` instances.
        IItemHandler.class);
```

If no context is required, `Void` should be used.
There is also a dedicated helper for context-less capabilities:
```java
public static final BlockCapability<IItemHandler, Void> ITEM_HANDLER_NO_CONTEXT =
    BlockCapability.createVoid(
        // Provide a name to uniquely identify the capability.
        new ResourceLocation("mymod", "item_handler_no_context"),
        // Provide the queried type. Here, we want to look up `IItemHandler` instances.
        IItemHandler.class);
```

For entities and item stacks, similar methods exist in `EntityCapability` and `ItemCapability` respectively.

## Querying capabilities
Once we have our `BlockCapability`, `EntityCapability`, or `ItemCapability` object in a static field, we can query a capability.

For entities and item stacks, we can try to find implementations of a capability with `getCapability`.
If the result is `null`, there no implementation is available.

For example:
```java
var object = entity.getCapability(CAP, context);
if (object != null) {
    // Use object
}
```
```java
var object = stack.getCapability(CAP, context);
if (object != null) {
    // Use object
}
```

Block capabilities are used a bit differently because blocks without a block entity can have capabilities as well.
The query is now performed on a `level`, with the `pos`ition that we are looking for as an additional parameter:
```java
var object = level.getCapability(CAP, pos, context);
if (object != null) {
    // Use object
}
```

If the block entity and/or the block state is known, they can be passed to save on query time:
```java
var object = level.getCapability(CAP, pos, blockState, blockEntity, context);
if (object != null) {
    // Use object
}
```

To give a more concrete example, here is how one might query an `IItemHandler` capability for a block, from the `Direction.NORTH` side:
```java
IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.NORTH);
if (handler != null) {
    // Use the handler for some item-related operation.
}
```

## Block capability caching
When a capability is looked up, the system will perform the following steps under the hood:
1. Fetch block entity and block state if they were not supplied.
2. Fetch registered capability providers. (More on this below).
3. Iterate the providers and ask them if they can provide the capability.
4. One of the providers will return a capability instance, potentially allocating a new object.

The implementation is rather efficient, but for queries that are performed frequently,
for example every game tick, these steps can take a significant amount of server time.
The `BlockCapabilityCache` system provides a dramatic speedup for capabilities that are frequently queried at a given position.

:::tip
Generally, a `BlockCapabilityCache` will be created once and then stored in a field of the object performing frequent capability queries.
When and where exactly you store the cache is up to you.
:::

To create a cache, call `BlockCapabilityCache.create` with the capability to query, the level, the position, and the query context.

```java
// Declare the field:
private BlockCapabilityCache<IItemHandler, @Nullable Direction> capCache;

// Later, for example in `onLoad` for a block entity:
this.capCache = BlockCapabilityCache.create(
        Capabilities.ItemHandler.BLOCK, // capability to cache
        level, // level
        pos, // target position
        Direction.NORTH // context
);
```

Querying the cache is then done with `getCapability()`:
```java
IItemHandler handler = this.capCache.getCapability();
if (handler != null) {
    // Use the handler for some item-related operation.
}
```

-*The cache is automatically cleared by the garbage collector, there is no need to unregister it.**

It is also possible to receive notifications when the capability object changes!
This includes capabilities changing (`oldHandler != newHandler`), becoming unavailable (`null`) or becoming available again (not `null` anymore).

The cache then needs to be created with two additional parameters:
- A validity check, that is used to determine if the cache is still valid.
In the simplest usage as a block entity field, `() -> !this.isRemoved()` will do.
- An invalidation listener, that is called when the capability changes.
This is where you can react to capability changes, removals, or appearances.

```java
// With optional invalidation listener:
this.capCache = BlockCapabilityCache.create(
        Capabilities.ItemHandler.BLOCK, // capability to cache
        level, // level
        pos, // target position
        Direction.NORTH, // context
        () -> !this.isRemoved(), // validity check (because the cache might outlive the object it belongs to)
        () -> onCapInvalidate() // invalidation listener
);
```

## Block capability invalidation
:::info
Invalidation is exclusive to block capabilities. Entity and item stack capabilities cannot be cached and do not need to be invalidated.
:::

To make sure that caches can correctly update their stored capability, **modders must call `level.invalidateCapabilities(pos)` whenever a capability changes, appears, or disappears**.
```java
// whenever a capability changes, appears, or disappears:
level.invalidateCapabilities(pos);
```

NeoForge already handles common cases such as chunk load/unloads and block entity creation/removal,
but other cases need to be handled explicitly by modders.
For example, modders must invalidate capabilities in the following cases:
- If a previously returned capability is no longer valid.
- If a capability-providing block (without a block entity) is placed or changes state, by overriding `onPlace`.
- If a capability-providing block (without a block entity) is removed, by overriding `onRemove`.

For a plain block example, refer to the `ComposterBlock.java` file.

For more information, refer to the javadoc of [`IBlockCapabilityProvider`][block-cap-provider].

## Registering capabilities
A capability _provider_ is what ultimately supplies a capability.
A capability provider is function that can either return a capability instance, or `null` if it cannot provide the capability.
Providers are specific to:
- the given capability that they are providing for, and
- the block instance, block entity type, entity type, or item instance that they are providing for.

They need to be registered in the `RegisterCapabilitiesEvent`.

Block providers are registered with `registerBlock`. For example:
```java
@SubscribeEvent // on the mod event bus
public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlock(
        Capabilities.ItemHandler.BLOCK, // capability to register for
        (level, pos, state, be, side) -> <return the IItemHandler>,
        // blocks to register for
        MY_ITEM_HANDLER_BLOCK,
        MY_OTHER_ITEM_HANDLER_BLOCK);
}
```

In general, registration will be specific to some block entity types, so the `registerBlockEntity` helper method is provided as well:
```java
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK, // capability to register for
        MY_BLOCK_ENTITY_TYPE, // block entity type to register for
        (myBlockEntity, side) -> <return the IItemHandler for myBlockEntity and side>);
```

:::danger
If the capability previously returned by a block or block entity provider is no longer valid,
-*you must invalidate the caches** by calling `level.invalidateCapabilities(pos)`.
Refer to the [invalidation section][invalidation] above for more information.
:::

Entity registration is similar, using `registerEntity`:
```java
event.registerEntity(
    Capabilities.ItemHandler.ENTITY, // capability to register for
    MY_ENTITY_TYPE, // entity type to register for
    (myEntity, context) -> <return the IItemHandler for myEntity>);
```

Item registration is similar too. Note that the provider receives the stack:
```java
event.registerItem(
    Capabilities.ItemHandler.ITEM, // capability to register for
    (itemStack, context) -> <return the IItemHandler for the itemStack>,
    // items to register for
    MY_ITEM,
    MY_OTHER_ITEM);
```

## Registering capabilities for all objects

If for some reason you need to register a provider for all blocks, entities, or items,
you will need to iterate the corresponding registry and register the provider for each object.

For example, NeoForge uses this system to register a fluid handler capability for all `BucketItem`s (excluding subclasses):
```java
// For reference, you can find this code in the `CapabilityHooks` class.
for (Item item : BuiltInRegistries.ITEM) {
    if (item.getClass() == BucketItem.class) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), item);
    }
}
```

Providers are asked for a capability in the order that they are registered.
Should you want to run before a provider that NeoForge already registers for one of your objects,
register your `RegisterCapabilitiesEvent` handler with a higher priority.
For example:
```java
modBus.addListener(RegisterCapabilitiesEvent.class, event -> {
    event.registerItem(
        Capabilities.FluidHandler.ITEM,
        (stack, ctx) -> new MyCustomFluidBucketWrapper(stack),
        // blocks to register for
        MY_CUSTOM_BUCKET);
}, EventPriority.HIGH); // use HIGH priority to register before NeoForge!
```
See [`CapabilityHooks`][capability-hooks] for a list of the providers registered by NeoForge itself.

[block-cap-provider]: https://github.com/neoforged/NeoForge/blob/1.20.x/src/main/java/net/neoforged/neoforge/capabilities/IBlockCapabilityProvider.java
[capability-hooks]: https://github.com/neoforged/NeoForge/blob/1.20.x/src/main/java/net/neoforged/neoforge/capabilities/CapabilityHooks.java
[invalidation]: #block-capability-invalidation
