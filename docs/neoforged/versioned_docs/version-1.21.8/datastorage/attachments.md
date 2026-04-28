---
sidebar_position: 4
---
# Data Attachments

The data attachment system allows mods to attach and store additional data on block entities, chunks, entities, and levels.

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

There are a few ways to provide an attachment serializer: directly implementing `IAttachmentSerializer`, implementing [`ValueIOSerializable`][valueio] and using the static `AttachmentType#serializable` method to create the builder, or providing a map codec to the builder.

In any case, the attachment **must be registered** to the `NeoForgeRegistries.ATTACHMENT_TYPES` registry. Here is an example:

```java
// Create the DeferredRegister for attachment types
private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

// Serialization via ValueIOSerializable
private static final Supplier<AttachmentType<ItemStackHandler>> HANDLER = ATTACHMENT_TYPES.register(
    "handler", () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build()
);
// Serialization via map codec
private static final Supplier<AttachmentType<Integer>> MANA = ATTACHMENT_TYPES.register(
    "mana", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("mana")).build()
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

To sync block entity, chunk, level, or entity attachments to a client, you can implement `sync` in the builder. Attachments are then sent to the client when the attachment is default-created through `AttachmentHolder#getData`, updated through `AttachmentHolder#setData`, or removed through `AttachmentHolder#removeData`. If the data should be sent at other times, then `AttachmentHolder#syncData` can be called with the `AttachmentType` to sync.

`AttachmentType.Builder#sync` has three overloads; however, they each create an `AttachmentSyncHandler<T>`, where `T` is the type of the data attachment. The handler has three methods: two to `read` and `write` to the network, and one to determine whether a given player can see the data broadcasted by the holder (`sendToPlayer`). The sync handler is ignored if the data attachment is removed.

```java
public class ExampleSyncHandler implements AttachmentSyncHandler<ExampleData> {

    @Override
    public void write(RegistryFriendlyByteBuf buf, ExampleData attachment, boolean initialSync) {
        // Write the attachment data to the buffer
        // If `initialSync` is true, you should write the entire attachment as the client does not have any prior data
        // If `initialSync` is false, you can choose to only write the data you would like to update
        
        // Example:
        if (initialSync) {
            // Write entire attachment
            ExampleData.STREAM_CODEC.encode(buf, attachment);
        } else {
            // Write update data
        }
    }

    @Override
    @Nullable
    public ExampleData read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable ExampleData previousValue) {
        // Read the data from the buffer and return the new data attachment
        // `previousValue` is `null` if there was no prior data on the client
        // The result should return `null` if the data attachment should be removed

        // Example:
        if (previousValue == null) {
            // Read entire attachment
            return ExampleData.STREAM_CODEC.decode(buf);
        } else {
            // Read update data and merge to previous value
            return previousValue;
        }
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        // Return whether the holder data is synced to the given player client
        // The players checked are different depending on the attachment holder:
        // - Block entities: All players tracking the chunk the block entity is within
        // - Chunk: All players tracking the chunk
        // - Entity: All players tracking the current entity, includes the current player if they are the attachment holder
        // - Level: All players in the current dimension / level

        // Example:
        // Only send the attachment if they are the attachment holder
        return holder == to;
    }
}
```

The two other overloads which delegate to `AttachmentSyncHandler` take in a [`StreamCodec`][streamcodec] for `read` and `write`, and an optional predicate for `sendToPlayer`.

```java
// Assume ExampleData has some stream codec STREAM_CODEC

// Sync handler
public static final Supplier<AttachmentType<ExampleData>> WITH_SYNC_HANDLER = ATTACHMENT_TYPES.register(
    "with_sync_handler", () -> AttachmentType.builder(() -> new ExampleData())
        .sync(new ExampleSyncHandler())
        .build()
);


// Stream codec
public static final Supplier<AttachmentType<ExampleData>> WITH_STREAM_CODEC = ATTACHMENT_TYPES.register(
    "with_stream_codec", () -> AttachmentType.builder(() -> new ExampleData())
        .sync(ExampleData.STREAM_CODEC)
        .build()
);

// Stream codec with predicate
public static final Supplier<AttachmentType<ExampleData>> WITH_PREDICATE = ATTACHMENT_TYPES.register(
    "with_predicate", () -> AttachmentType.builder(() -> new ExampleData())
        .sync((holder, to) -> holder == to, ExampleData.STREAM_CODEC)
        .build()
);
```

:::note
Using the `StreamCodec` overloads means that the entire data attachment will be synced each time, ignoring any data that was previously on the client.
:::

## Copying data on player death

By default, [entity] data attachments are not copied on player death. To automatically copy an attachment on player death, set `copyOnDeath` in the attachment builder.

More complex handling can be implemented via `PlayerEvent.Clone` by reading the data from the original entity and assigning it to the new entity. In this event, the `#isWasDeath` method can be used to distinguish between respawning after death and returning from the End. This is important because the data will already exist when returning from the End, so care has to be taken to not duplicate values in this case.

For example:

```java
@SubscribeEvent // on the game event bus
public static void onClone(PlayerEvent.Clone event) {
    if (event.isWasDeath() && event.getOriginal().hasData(MY_DATA)) {
        event.getEntity().getData(MY_DATA).fieldToCopy = event.getOriginal().getData(MY_DATA).fieldToCopy;
    }
}
```

[datacomponents]: ../items/datacomponents.md
[entity]: ../entities/index.md
[saveddata]: saveddata.md
[streamcodec]: ../networking/streamcodecs.md
[valueio]: valueio.md#valueioserializable
