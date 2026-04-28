---
sidebar_position: 2
---
# Data and Networking

An entity without data is quite useless, as such, storing data on an entity is essential. All entities store some default data, such as their type and their position. This article will explain how to add your own data, as well as how to synchronize that data.

The most simple way to add data is as a field in your `Entity` class. You can then interact with this data in any way you wish. However, this quickly becomes very annoying as soon as you have to synchronize that data. This is because most entity logic is run on the server only, and it is only occasionally (depending on the [`EntityType`][entitytype]'s `clientUpdateInterval` value) that an update is sent to the client; this is also the cause for easily noticeable entity "lags" when the server's tick speed is too slow.

As such, vanilla introduces a few systems to help with that, each of which serves a specific purpose. You also always have the option of [sending custom data][custom] when necessary.

## `SynchedEntityData`

`SynchedEntityData` is a system used for storing values at runtime and syncing them over the network. It is split into three classes:

- `EntityDataSerializer`s are basically wrappers around a [`StreamCodec`][streamcodec].
    - Minecraft uses a hard-coded map of serializers. NeoForge transforms this map into a registry, meaning that if you want to add new `EntityDataSerializer`s, they must be added by [registration].
    - Minecraft defines various default `EntityDataSerializer`s in the `EntityDataSerializers` class.
- `EntityDataAccessor`s are held by the entity and are used to get and set the data values.
- `SynchedEntityData` itself holds all `EntityDataAccessor`s for an entity, and automatically calls on the `EntityDataSerializer`s to sync values as needed.

To get started, create an `EntityDataAccessor` in your entity class:

```java
public class MyEntity extends Entity {
    // The generic type must match the one of the second parameter below.
    public static final EntityDataAccessor<Integer> MY_DATA =
        SynchedEntityData.defineId(
            // The class of the entity.
            MyEntity.class,
            // The entity data accessor type.
            EntityDataSerializers.INT
        );
}
```

:::danger
While the compiler will allow you to use a class other than the owning class as the first parameter in `SynchedEntityData#defineId()`, doing so can and will lead to hard-to-debug issues and, as such, is to be avoided at all costs. (This also includes adding fields via mixins or similar methods.)
:::

We must then define default values in the `defineSynchedData` method, like so:

```java
public class MyEntity extends Entity {
    public static final EntityDataAccessor<Integer> MY_DATA = SynchedEntityData.defineId(MyEntity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Our default value is zero.
        builder.define(MY_DATA, 0);
    }
}
```

Finally, we can get and set entity data like so (assuming we're in a method within `MyEntity`):

```java
int data = this.getEntityData().get(MY_DATA);
this.getEntityData().set(MY_DATA, 1);
```

## `readAdditionalSaveData` and `addAdditionalSaveData`

These two methods are used to read and write data to disk. They work by loading/saving your values from/to an [NBT tag][nbt], like so:

```java
// Assume that an `int data` exists in the class.
@Override
protected void readAdditionalSaveData(CompoundTag tag) {
    this.data = tag.getInt("my_data");
}

@Override
protected void addAdditionalSaveData(CompoundTag tag) {
    tag.putInt("my_data", this.data);
}
```

## Custom Spawn Data

In some cases, there is custom data needed for your entity on the client when it is spawned, but that same data doesn't change over time. When this is the case, you can implement the `IEntityWithComplexSpawn` interface on your entity and use its two methods `#writeSpawnData` and `#readSpawnData` to write/read data to/from the network buffer:

```java
@Override
public void writeSpawnData(RegistryFriendlyByteBuf buf) {
    buf.writeInt(1234);
}

@Override
public void readSpawnData(RegistryFriendlyByteBuf buf) {
    int i = buf.readInt();
}
```

Additionally, you can send your own packets upon spawning. To do so, override `IEntityExtension#sendPairingData` and send your packets there like any other packet:

```java
@Override
public void sendPairingData(ServerPlayer player, Consumer<CustomPacketPayload> packetConsumer) {
    // Call super for some base functionality.
    super.sendPairingData(player, packetConsumer);
    // Add your own packets.
    packetConsumer.accept(new MyPacket(...));
}
```

Please refer to the [Networking articles][networking] for more information on custom network packets.

## Data Attachments

Entities have been patched to extend `AttachmentHolder` and as such support data storage via [data attachments][attachment]. Its main use is to define custom data on entities that are not your own, i.e., entities added by Minecraft or other mods. Please see the linked article for more information.

## Custom Network Messages

For syncing, you can also always opt to use a custom packet to send additional information when needed. Please refer to the [Networking articles][networking] for more information.

[attachment]: ../datastorage/attachments.md
[custom]: #custom-network-messages
[entitytype]: index.md#entitytype
[nbt]: ../datastorage/nbt.md
[networking]: ../networking/index.md
[registration]: ../concepts/registries.md#methods-for-registering
[streamcodec]: ../networking/streamcodecs.md
