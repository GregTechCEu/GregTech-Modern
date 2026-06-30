---
title: Sync Handlers
---


## What are they
A `SyncHandler` is a mechanism that handles the syncing between client and server for MUI2. For its high level usages, see [Sync Basics](Sync-Basics.md). `SyncHandler`s generally can sync anything between client and server, and `SyncValue`s specifically sync data (e.g. a value) between client and server.

## How they work
You can create a `SyncHandler` with the following syntax:
`new IntSyncValue(() -> this.number, (newValue) -> this.number = newValue)`
This means that to check if the value changed, it will call the first argument (the IntSupplier, `() -> this.number`), and cache the value that gets returned.   
Then, whenever that value changes, it will send the changed value to the other side.   
Whenever a `SyncHandler` receives a value from the other side, it will call the setter with the value, in this case setting `this.number` to the new value on the client, and cache it for further use/checking.  
Do note that this means that a setter isn't necessary. You can use only a getter, and then call `syncHandler.getValue()` to get the last received / cached value.

!!! Note
    By default, `SyncValue`s or `SyncHandler`s don't allow client-to-server syncing. For that you will need to chain `.allowC2S()`. This gets called on the `SyncHandler` itself, so if you're using a builder, after the `.build()`



Here's a list of the built-in `SyncHandler`s:


 - `BigDecimalSyncValue`  
 - `BigIntegerSyncValue`  
 - `BinaryEnumSyncValue`  
 - `BooleanSyncValue`  
 - `ByteArraySyncValue`  
 - `ByteSyncValue`  
 - `DoubleSyncValue`  
 - `EnumSyncValue`  
 - `FloatSyncValue`  
 - `FluidSlotSyncHandler`  
 - `GenericCollectionSyncHandler<T>`  
 - `GenericListSyncHandler<T>`  
 - `GenericMapSyncHandler<T>`  
 - `GenericSetSyncHandler<T>`  
 - `GenericSyncValue<T>`  
 - `IntSyncValue`  
 - `LongArraySyncValue`  
 - `LongSyncValue`  
 - `ShortSyncValue`  
 - `StringSyncValue`  

## Generic Sync Handlers

In the previous list, I specifically want to draw attention to the Generic- SyncHandlers. These can take in custom methods for writing- and reading data for syncing stuff that isn't in this list.
e.g.
```java
        GenericListSyncHandler<Integer> numberListSyncHandler = GenericListSyncHandler.<Integer>builder()
                .getter(() -> this.serverInts)
                .setter(v -> this.serverInts = v)
                .serializer(FriendlyByteBuf::writeInt)
                .deserializer(FriendlyByteBuf::readInt)
                .immutableCopy()
                .build();
```

To make this easier as to not have to re-invent the (de)serializers, you can use what's called an `IByteBufAdapter`. They hold serialize, deserialize and equals methods. So you could do
```java
        GenericSyncValue<GTRecipe> recipeSyncHandler = GenericSyncValue.<GTRecipe>builder()
                        .getter(() -> rlmachine.getRecipeLogic().getLastRecipe())
                        .adapter(GTByteBufAdapters.GTRECIPE)
                        .build();
```
Here's a list of existing ByteBufAdapter:


 - `ByteBufAdapters.ITEM_STACK`  
 - `ByteBufAdapters.FLUID_STACK`  
 - `ByteBufAdapters.NBT`  
 - `ByteBufAdapters.STRING`  
 - `ByteBufAdapters.BYTE_BUF`  
 - `ByteBufAdapters.FRIENDLY_BYTE_BUF`  
 - `ByteBufAdapters.INT`  
 - `ByteBufAdapters.LONG`  
 - `ByteBufAdapters.FLOAT`  
 - `ByteBufAdapters.DOUBLE`  
 - `ByteBufAdapters.BOOL`  
 - `ByteBufAdapters.BYTE`  
 - `ByteBufAdapters.SHORT`  
 - `ByteBufAdapters.CHAR`  
 - `ByteBufAdapters.BLOCKSTATE`  
 - `ByteBufAdapters.BLOCKPOS`  
 - `ByteBufAdapters.GLOBAL_POS`  
 - `ByteBufAdapters.RESOURCE_LOCATION`  
 - `ByteBufAdapters.UUID`  
 - `ByteBufAdapters.COMPONENT`  
 - `GTByteBufAdapters.MONITOR_GROUPS`  
 - `GTByteBufAdapters.PATTERN_ERRORS`  
 - `GTByteBufAdapters.GTRECIPE`  

It is also possible to make your own ByteBufAdapter from a [CODEC](https://docs.minecraftforge.net/en/latest/datastorage/codecs/) using `ByteBufAdapters.makeAdapter(Codec<T> codec)`