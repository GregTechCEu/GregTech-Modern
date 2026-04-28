---
sidebar_position: 4
---
# Entities

In addition to regular network messages, there are various other systems provided to handle synchronizing entity data.

## Spawn Data

Since 1.20.2 Mojang introduced the concept of Bundle packets, which are used to send entity spawn packets together. This allows for more data to be sent with the spawn packet, and for that data to be sent more efficiently.

You can add extra data to the spawn packet NeoForge sends by implementing the following interface.

### IEntityWithComplexSpawn

If your entity has data that is needed on the client, but does not change over time, then it can be added to the entity spawn packet using this interface. `#writeSpawnData` and `#readSpawnData` control how the data should be encoded to/decoded from the network buffer. Alternatively you can override the method `IEntityExtension#sendPairingData` which is called when the entity's initial data is sent to the client. This method is called on the server, and can be used to send additional payloads to the client within the same bundle as the spawn packet.

## Dynamic Data Parameters

This is the main vanilla system for synchronizing entity data from the server to the client. As such, a number of vanilla examples are available to refer to.

Firstly, you need a `EntityDataAccessor<T>` for the data you wish to keep synchronized. This should be stored as a `static final` field in your entity class, obtained by calling `SynchedEntityData#defineId` and passing the entity class and a serializer for that type of data. The available serializer implementations can be found as static constants within the `EntityDataSerializers` class.

:::caution
You should __only__ create data parameters for your own entities, _within that entity's class_. Adding parameters to entities you do not control can cause the IDs used to send that data over the network to become desynchronized, causing difficult to debug crashes.
:::

Then, override `Entity#defineSynchedData` and call `SynchedEntityData.Builder#define` for each of your data parameters, passing the parameter and an initial value to use. Remember to always call the `super` method first!

You can then get and set these values via your entity's `entityData` instance. Changes made will be synchronized to the client automatically.
