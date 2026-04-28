---
sidebar_position: 2
---
# Stream Codecs

Stream codecs are a serialization tool used to describe how an object should be stored and read from a stream, such as buffers. Stream codecs are primarily used by Vanilla's [networking system][networking] to sync data.

:::info
As stream codecs are roughly analogous to [codecs], this page has been formatted in the same way to show the similarities.
:::

## Using Stream Codecs

Stream codecs encode and decode objects into some stream using `StreamCodec#encode` and `StreamCodec#decode`, respectively. `encode` takes in the stream and the object to encode into the stream. `decode` takes in the stream and returns the decoded object. Typically, the stream is either a `ByteBuf`, `FriendlyByteBuf`, or `RegistryFriendlyByteBuf`.

```java
// Let exampleStreamCodec represent a StreamCodec<ExampleJavaObject>
// Let exampleObject be a ExampleJavaObject
// Let buffer be a RegistryFriendlyByteBuf

// Encode Java object into the buffer stream
exampleStreamCodec.encode(buffer, exampleObject);

// Read Java object from buffer stream
ExampleJavaObject obj = exampleStreamCodec.decode(buffer);
```

:::note
Unless you are manually handling the buffer object, you will generally never call `encode` and `decode`.
:::

## Existing Stream Codecs

### `ByteBufCodecs`

`ByteBufCodecs` contains static instances of codecs for certain primitives and objects.

| Stream Codec   | Java Type     |
|----------------|---------------|
| `BOOL`         | `Boolean`     |
| `BYTE`         | `Byte`        |
| `SHORT`        | `Short`       |
| `INT`          | `Integer`     |
| `LONG`         | `Long`        |
| `FLOAT`        | `Float`       |
| `DOUBLE`       | `Double`      |
| `BYTE_ARRAY`   | `byte[]`\*    |
| `LONG_ARRAY`   | `long[]`      |
| `STRING_UTF8`  | `String`\*\*  |
| `TAG`          | `Tag`         |
| `COMPOUND_TAG` | `CompoundTag` |
| `VECTOR3F`     | `Vector3fc`   |
| `QUATERNIONF`  | `Quaternionfc`|
| `GAME_PROFILE` | `GameProfile` |

\* `byte[]` can be limited to a certain number of values via `ByteBufCodecs#byteArray`.

\*\* `String` can be limited to a certain number of characters via `ByteBufCodecs#stringUtf8`.

Additionally, there are some static instances that encode and decode primitives and objects using a different method.

#### Unsigned Shorts

`UNSIGNED_SHORT` is an alternative of `SHORT` that is meant to be treated as an unsigned number. As numbers are signed in Java, unsigned shorts are sent and received as `Integer`s with the upper two bytes masked out.

#### Variable-Sized Number

`VAR_INT` and `VAR_LONG` are stream codecs where the value is encoded to be as small as possible. This is done by encoding seven bits at a time, using the upper bit as a marker of whether there is more data for this number. Numbers between 0 and 2^28-1 for integers or 0 and 2^56-1 for longs will be sent shorter or equal to the number of bytes in a integer or long, respectively. If the values of your numbers are normally in this range and generally at the lower end of it, then these variable stream codecs should be used.

:::note
`VAR_INT` and `VAR_LONG` are alternatives for `INT` and `LONG`, respectively.
:::

#### Trusted Tags

`TRUSTED_TAG` and `TRUSTED_COMPOUND_TAG` are variants of `TAG` and `COMPOUND_TAG`, respectively, that have an unlimited heap to decode the tag to, compared to the 2MiB limit of `TAG` and `COMPOUND_TAG`. Trusted tag stream codecs should ideally only be used in clientbound packets, such as what Vanilla does for [block entity data packet][blockentity] and [entity data serializers][entity].

If a different limit should be used, then a `NbtAccounter` can be supplied with the given size using `ByteBufCodecs#tagCodec` or `#compoundTagCodec`. An optional-wrapped `Tag` can also be obtained using `#optionalTagCodec`.

#### Lenient JSON

`ByteBufCodecs#lenientJson` handles an arbitrary `JsonElement`, allowing for multiple floating-point descriptors like `nan` or `infinite`, or multiple top-level objects. It takes in the maximum size the JSON can be.

### Vanilla and NeoForge

Minecraft and NeoForge define many stream codecs for objects that are frequently encoded and decoded. Some examples include `Identifier#STREAM_CODEC` for `Identifier`s or `NeoForgeStreamCodecs#CHUNK_POS` for `ChunkPos`s.

Most of the stream codecs can be found within the object class itself or within `StreamCodec`, `ByteBufCodecs`, or `NeoForgeStreamCodecs`. 

## Creating Stream Codecs

Stream codecs can be created for reading or writing any object to a stream. This documentation will focus on the stream as a buffer as that is its primary purpose.

Stream codecs have two generics: `B` representing the buffer and `V` representing the object value. `B` is generally one of three types: `ByteBuf`, `FriendlyByteBuf`, `RegistryFriendlyByteBuf`, each extending one another. `FriendlyByteBuf` adds Minecraft-specific read and write methods while `RegistryFriendlyByteBuf` provides access to the list of registries and its objects.

When constructing a stream codec, `B` should be the least-specific buffer type. For example, a `Identifier` is sent as a string. As strings are supported by a regular `ByteBuf`, its type should be `StreamCodec<ByteBuf, Identifier>`. `FriendlyByteBuf` contains methods for writing a `ChunkPos`, so its type should be `StreamCodec<FriendlyByteBuf, ChunkPos>`. An `Item` needs access to the registry, so its type should be `StreamCodec<RegistryFriendlyByteBuf, Item>`.

Most methods that take in a stream codec look for `? super B` for the buffer type, meaning that all three of the above examples can be used if the buffer type is a `RegistryFriendlyByteBuf`.

### Member Encoders

`StreamMemberEncoder` is an alternative to `StreamEncoder` where the encoding object comes first and the buffer second. This is typically used when the encoding object contains an instance method to write the object to the buffer. A `StreamMemberEncoder` can be used to create the `StreamCodec` by calling `StreamCodec#ofMember`.

```java
// Some object to create a stream codec for
public class ExampleObject {
    
    // The normal constructor
    public ExampleObject(String arg1, int arg2, boolean arg3) { /* ... */ }

    // The stream decoder reference
    public ExampleObject(ByteBuf buffer) { /* ... */ }

    // The stream encoder reference
    public void encode(ByteBuf buffer) { /* ... */ }
}

// What the stream codec would look like
public static StreamCodec<ByteBuf, ExampleObject> STREAM_CODEC =
    StreamCodec.ofMember(ExampleObject::encode, ExampleObject::new);
```

### Composites

Stream codecs can read and write objects via `StreamCodec#composite`. Each composite stream codec defines a list of stream codecs and getters which are read/written in the order they are provided. `composite` has overloads up to twelve parameters.

Every two parameters in a `composite` represents the stream codec used to read/write the field and a getter to get the field to encode from the object. The final parameter is a function to create a new instance of the object when decoding.

```java
// Objects to create a stream codec for
public record SimpleExample(String arg1, int arg2, boolean arg3) {}
public record RegistryExample(double arg1, Holder<Item> arg2) {}

// The stream codecs
public static final StreamCodec<ByteBuf, SimpleExample> SIMPLE_STREAM_CODEC =
    StreamCodec.composite(
        // Stream codec and getter pair
        ByteBufCodecs.STRING_UTF8, SimpleExample::arg1,
        ByteBufCodecs.VAR_INT, SimpleExample::arg2,
        ByteBufCodecs.BOOL, SimpleExample::arg3,
        SimpleExample::new
    );

// Since this has a holder, a RegistryFriendlyByteBuf is used
public static final StreamCodec<RegistryFriendlyByteBuf, RegistryExample> REGISTRY_STREAM_CODEC =
    StreamCodec.composite(
        // Note that ByteBuf stream codecs can be used here
        ByteBufCodecs.DOUBLE, RegistryExample::arg1,
        ByteBufCodecs.holderRegistry(Registries.ITEM), RegistryExample::arg2,
        RegistryExample::new
    );
```

### Transformers

Stream codecs can be transformed into equivalent, or partially equivalent, representations using mapping methods. Two mapping methods apply to the value while one mapping method applies to the buffer.

The `map` method transforms the value using two functions: one to transform the current type into the new type, and one to transform the new type back into the current type. This is analogous to [codec transformers][transformers].

```java
public static final StreamCodec<ByteBuf, Identifier> STREAM_CODEC = 
    ByteBufCodecs.STRING_UTF8.map(
        // String -> Identifier
        Identifier::new,
        // Identifier -> String
        Identifier::toString
    );
```

The `apply` method transforms the value using a `StreamCodec.CodecOperation`. A `StreamCodec.CodecOperation` takes in a stream codec of the current type and returns a stream codec of the new type. These typically wrap around `map` or take in helper methods.

```java
public static final StreamCodec<ByteBuf, List<Identifier>> STREAM_CODEC =
    Identifier.STREAM_CODEC.apply(ByteBufCodecs.list());
```

The `mapStream` method transforms the buffer using a function that takes in the new buffer type and returns the current buffer type. This method should rarely be used as most methods with stream codecs do not need to change the type of the buffer.

```java
public static final StreamCodec<RegistryFriendlyByteBuf, Integer> STREAM_CODEC =
    ByteBufCodecs.VAR_INT.mapStream(buffer -> (ByteBuf) buffer);
```

### Unit

A stream codec which supplies an in-code value and encodes to nothing can be represented using `StreamCodec#unit`. This is useful if no information should be synced across the network.

:::warning
Unit stream codecs expect that any encoded object must match the unit specified; otherwise an error will be thrown. Therefore, all objects must have some `equals` implementation that returns true for the unit object, or that the instance provided to the stream codec is always provided when encoding.
:::

```java
public static final StreamCodec<ByteBuf, Item> UNIT_STREAM_CODEC =
    StreamCodec.unit(Items.AIR);
```
### Lazy Initialized

Sometimes, a stream codec may rely on data that is not present when it is constructed. In these situations `NeoForgeStreamCodecs#lazy` can be used for a stream codec to construct itself on first read/write. The method takes in a supplied stream codec.

```java
public static final StreamCodec<ByteBuf, Item> LAZY_STREAM_CODEC = 
    NeoForgeStreamCodecs.lazy(
        () -> StreamCodec.unit(Items.AIR)
    );
```

### Collections

A stream codec for collections can be generated from a object stream codec via `collection`. `collection` takes in an `IntFunction` that constructs the empty collection, a stream codec of the object, and an optional maximum size.

```java
public static final StreamCodec<ByteBuf, Set<BlockPos>> COLLECTION_STREAM_CODEC =
    ByteBufCodecs.collection(
        HashSet::new, // Constructs a set with the specified capacity
        BlockPos.STREAM_CODEC,
        256 // The set can only have up to 256 elements
    );
```

Another overload of `collection` can be specified with `StreamCodec#apply`.

```java
public static final StreamCodec<ByteBuf, Set<BlockPos>> COLLECTION_STREAM_CODEC =
    BlockPos.STREAM_CODEC.apply(
        ByteBufCodecs.collection(HashSet::new)
    );
```

List-based collections also can be specified through `StreamCodec#apply` by calling `ByteBufCodecs#list` with an optional maximum size.

```java
public static final StreamCodec<ByteBuf, List<BlockPos>> LIST_STREAM_CODEC =
    BlockPos.STREAM_CODEC.apply(
        // The list can only have up to 256 elements
        ByteBufCodecs.list(256)
    );
```

### Map

A stream codec for a map of key and value objects can be generated using two stream codecs via `ByteBufCodecs#map`. The function also takes in an `IntFunction` that constructs the empty map and an optional maximum size.

```java
public static final StreamCodec<ByteBuf, Map<String, BlockPos>> MAP_STREAM_CODEC =
    ByteBufCodecs.map(
        HashMap::new, // Constructs a map with the specified capacity
        ByteBufCodecs.STRING_UTF8,
        BlockPos.STREAM_CODEC,
        256 // The map can only have up to 256 elements
    );
```

### Either

A stream codec for two different methods of reading/writing some object data can be generated from two stream codecs via `ByteBufCodecs#either`. This method first reads/writes a boolean indicating whether to read/write the first or second stream codec, respectively.

```java
public static final StreamCodec<ByteBuf, Either<Integer, String>> EITHER_STREAM_CODEC = 
    ByteBufCodecs.either(
        ByteBufCodecs.VAR_INT,
        ByteBufCodecs.STRING_UTF8
    );
```

### Id Mapper

In most cases, when sending information across the network where an object is present on both sides, an integer representing an id is sent. Ids representing an object reduce the amount of information that need to be synced across the network. Both enums and registries make use of this.

`ByteBufCodecs#idMapper` provides a convenient way to send ids for objects. It either takes in two functions which convert an object to int and vice versa, or an `IdMap`.

```java
// For some enum
public enum ExampleIdObject {
    ;

    // Gets Id -> Enum
    public static final IntFunction<ExampleIdObject> BY_ID = 
        ByIdMap.continuous(
            ExampleIdObject::getId,
            ExampleIdObject.values(),
            ByIdMap.OutOfBoundsStrategy.ZERO
    );
    
    ExampleIdObject(int id) { /* ... */ }
}

// The stream codec would look like
public static final StreamCodec<ByteBuf, ExampleIdObject> ID_STREAM_CODEC =
    ByteBufCodecs.idMapper(ExampleIdObject.BY_ID, ExampleIdObject::getId);
```

### Optional

A stream codec for sending an `Optional` wrapped value can be generated by supplying a stream codec to `ByteBufCodecs#optional`. This method first reads/writes a boolean indicating whether to read/write the object.

```java
public static final StreamCodec<RegistryFriendlyByteBuf, Optional<DataComponentType<?>>> OPTIONAL_STREAM_CODEC =
    DataComponentType.STREAM_CODEC.apply(ByteBufCodecs::optional);
```

### Registry Objects

Registry objects can be sent across the network using one of three methods: `registry`, `holderRegistry`, or `holder`. Each takes in a `ResourceKey` representing the registry the registry object is in.

:::warning
Custom registries must be syncable by calling `RegistryBuilder#sync` and setting the value to `true`. Otherwise, the encoder will throw an exception.
:::

`registry` and `holderRegistry` returns the registry object or a holder wrapped registry object, respectively. These methods send over an id representing the registry object.

```java
// Registry object
public static final StreamCodec<RegistryFriendlyByteBuf, Item> VALUE_STREAM_CODEC =
    ByteBufCodecs.registry(Registries.ITEM);

// Holder of registry object
public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> HOLDER_STREAM_CODEC =
    ByteBufCodecs.holderRegistry(Registries.ITEM);
```

`holder` returns a holder wrapped registry object. This method sends over an id representing the registry object, or the registry object itself if the provided `Holder` is a direct reference. To do so, `holder` also takes in the stream codec of the registry object.

```java
public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEvent>> STREAM_CODEC =
    ByteBufCodecs.holder(
        Registries.SOUND_EVENT, SoundEvent.DIRECT_STREAM_CODEC
    );
```

:::note
`holder` will only throw an exception for a non-synced custom registry if the holder is not direct.
:::

### Holder Sets

Tags or sets of holder wrapped registry objects can be sent using `holderSet`. This takes in a `ResourceKey` representing the registry the registry objects are in.

```java
public static final StreamCodec<RegistryFriendlyByteBuf, HolderSet<Item>> HOLDER_SET_STREAM_CODEC =
    ByteBufCodecs.holderSet(Registries.ITEM);
```

### Recursive

Sometimes, an object may reference an object of the same type as a field. For example, `MobEffectInstance` takes in an optional `MobEffectInstance` if there is a hidden effect. In this case, `StreamCodec#recursive` can be used to supply the stream codec as part of a function to create the stream codec.

```java
// Define our recursive object
public record RecursiveObject(Optional<RecursiveObject> inner) { /* ... */ }

public static final StreamCodec<ByteBuf, RecursiveObject> RECURSIVE_CODEC = StreamCodec.recursive(
    recursedStreamCodec -> StreamCodec.composite(
        recursedStreamCodec.apply(ByteBufCodecs::optional),
        RecursiveObject::inner,
        RecursiveObject::new
    )
);
```

### Dispatch

Stream codecs can have sub-stream codecs that can decode a particular object based on some specified type via `StreamCodec#dispatch`. This is typically used with registry objects that represent a type, like `ParticleType` for `ParticleOptions` or `StatType` for `Stat`s.

A dispatch stream codec first attempts to read/write the type object. From there, the current object is read/written using one of the functions provided in the method. The first `Function` takes in the current object and gets the type to write the value. The second `Function` takes in the type object and gets the `StreamCodec` for the current object to read the value.

```java
// Define our object(s)
public abstract class ExampleObject {

    // Define the method used to specify the object type for encoding
    public abstract StreamCodec<? super RegistryFriendlyByteBuf, ? extends ExampleObject> streamCodec();
}

// Assume there is a ResourceKey<StreamCodec<? super RegistryFriendlyByteBuf, ? extends ExampleObject>> DISPATCH
public static final StreamCodec<RegistryFriendlyByteBuf, ExampleObject> DISPATCH_STREAM_CODEC =
    ByteBufCodecs.registry(DISPATCH).dispatch(
        // Get the stream codec from the specific object
        ExampleObject::streamCodec,
        // Get the stream codec from the registry object
        Function.identity()
    );
```

[networking]: payload.md
[codecs]: ../datastorage/codecs.md
[blockentity]: ../blockentities/index.md#syncing-on-block-update
[entity]: ../entities/data.md
[transformers]: ../datastorage/codecs.md#transformers
