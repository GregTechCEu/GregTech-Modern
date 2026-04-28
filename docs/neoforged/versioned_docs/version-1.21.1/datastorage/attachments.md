---
sidebar_position: 3
---
# Data Attachments

The data attachment system allows mods to attach and store additional data on block entities, chunks, and entities.

_To store additional level data, you can use [SavedData][saveddata]._

:::note
Data attachments for item stacks have been superceeded by vanilla's [data components][datacomponents].
:::

## Creating an attachment type

To use the system, you need to register an `AttachmentType`. The attachment type contains the following configuration:

- A default value supplier to create the instance when the data is first accessed.
- An optional serializer if the attachment should be persisted.
- (If a serializer was configured) The `copyOnDeath` flag to automatically copy entity data on death (see below).

:::tip
If you don't want your attachment to persist, do not provide a serializer.
:::

There are a few ways to provide an attachment serializer: directly implementing `IAttachmentSerializer`, implementing `INBTSerializable` and using the static `AttachmentType#serializable` method to create the builder, or providing a codec to the builder.

In any case, the attachment **must be registered** to the `NeoForgeRegistries.ATTACHMENT_TYPES` registry. Here is an example:

```java
// Create the DeferredRegister for attachment types
private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

// Serialization via INBTSerializable
private static final Supplier<AttachmentType<ItemStackHandler>> HANDLER = ATTACHMENT_TYPES.register(
    "handler", () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build()
);
// Serialization via codec
private static final Supplier<AttachmentType<Integer>> MANA = ATTACHMENT_TYPES.register(
    "mana", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
);
// No serialization
private static final Supplier<AttachmentType<SomeCache>> SOME_CACHE = ATTACHMENT_TYPES.register(
    "some_cache", () -> AttachmentType.builder(() -> new SomeCache()).build()
);

// In your mod constructor, don't forget to register the DeferredRegister to your mod bus:
ATTACHMENT_TYPES.register(modBus);
```

## Using the attachment type

Once the attachment type is registered, it can be used on any holder object. Calling `getData` if no data is present will attach a new default instance.

```java
// Get the ItemStackHandler if it already exists, else attach a new one:
ItemStackHandler stackHandler = chunk.getData(HANDLER);
// Get the current player mana if it is available, else attach 0:
int playerMana = player.getData(MANA);
// And so on...
```

If attaching a default instance is not desired, a `hasData` check can be added:

```java
// Check if the chunk has the HANDLER attachment before doing anything.
if (chunk.hasData(HANDLER)) {
    ItemStackHandler stackHandler = chunk.getData(HANDLER);
    // Do something with chunk.getData(HANDLER).
}
```

The data can also be updated with `setData`:

```java
// Increment mana by 10.
player.setData(MANA, player.getData(MANA) + 10);
```

:::important
Usually, block entities and chunks need to be marked as dirty when they are modified (with `setChanged` and `setUnsaved(true)`). This is done automatically for calls to `setData`:

```java
chunk.setData(MANA, chunk.getData(MANA) + 10); // will call setUnsaved automatically
```

but if you modify some data that you obtained from `getData` (including a newly created default instance) then you must mark block entities and chunks as dirty explicitly:

```java
var mana = chunk.getData(MUTABLE_MANA);
mana.set(10);
chunk.setUnsaved(true); // must be done manually because we did not use setData
```
:::

## Sharing data with the client

To sync block entity, chunk, or entity attachments to a client, you need to [send a packet to the client][network] yourself. For chunks, you can use `ChunkWatchEvent.Sent` to know when to send chunk data to a player.

## Copying data on player death

By default, entity data attachments are not copied on player death. To automatically copy an attachment on player death, set `copyOnDeath` in the attachment builder.

More complex handling can be implemented via `PlayerEvent.Clone` by reading the data from the original entity and assigning it to the new entity. In this event, the `#isWasDeath` method can be used to distinguish between respawning after death and returning from the End. This is important because the data will already exist when returning from the End, so care has to be taken to not duplicate values in this case.

For example:

```java
NeoForge.EVENT_BUS.register(PlayerEvent.Clone.class, event -> {
    if (event.isWasDeath() && event.getOriginal().hasData(MY_DATA)) {
        event.getEntity().getData(MY_DATA).fieldToCopy = event.getOriginal().getData(MY_DATA).fieldToCopy;
    }
});
```

[saveddata]: ./saveddata.md
[datacomponents]: ../items/datacomponents.md
[network]: ../networking/index.md
