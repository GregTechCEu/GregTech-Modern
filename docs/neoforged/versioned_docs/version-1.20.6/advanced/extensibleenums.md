# Extensible Enums

Extensible Enums are an enhancement of specific Vanilla enums to allow new entries to be added. This is done by modifying the compiled bytecode of the enum at runtime to add the elements.

## `IExtensibleEnum`

All enums that can have new entries implement the `IExtensibleEnum` interface. This interface acts as a marker to allow the `RuntimeEnumExtender` launch plugin service to know what enums should be transformed.

:::warning
You should **not** be implementing this interface on your own enums. Use maps or registries instead depending on your usecase.
:::

### Creating an Enum Entry

To create a new enum entry, the static `create` method should be called. This static method is added to all extensible enums and is transformed at runtime to allow new enums to be added.

:::note
The `create` method should be called within a static final field if you need to refer to the value or within the main mod constructor if you do not.
:::

The `create` method contains a name parameter followed by the parameters of a supporting constructor. The name parameter represents the name of the enum constant, which is returned by `#name`. The name of the enum constant should be prefixed with your mod id followed by an underscore (`_`) to avoid conflicts between mods that decide to add the same enum constant (e.g., adding a enum constant called `test` with mod id `examplemod` should be `EXAMPLEMOD_TEST`).

:::note
Since extensible enums are added at runtime, technically, any UTF-8 character can be used for the name parameter. However, it is recommended to only use valid Java identifiers.
:::

```java
// In your main mod class
public static final FireworkExplosion.Shape DUMMY =
        FireworkExplosion.Shape.create("EXAMPLEMOD_DUMMY", 294, "examplemod_dummy");
```

## Contributing to NeoForge

To add a new extensible enum to NeoForge, there are at least two required things to do.

First, have the enum implement `IExtensibleEnum` to mark that this enum should be transformed via the `RuntimeEnumExtender`. Second, add a `create` method for every constructor within the enum. All create methods should start with a `String` parameter representing the name of the enum constant. The `create` methods should throw an `IllegalStateException`.

:::note
The `create` method(s) will be transformed at runtime, so unless the enum was not detectable, the `create` method will create a new enum entry.
:::

```java
// This is an example, not an actual enum within Vanilla
public enum ExampleEnum implements net.neoforged.neoforge.common.IExtensibleEnum {
    // VALUE_1 represents the name parameter here
    VALUE_1(0, "value_1", false),
    VALUE_2(1, "value_2", true),
    VALUE_3(2, "value_3");

    ExampleEnum(int arg1, String arg2, boolean arg3) {
        // ...
    }

    ExampleEnum(int arg1, String arg2) {
        this(arg1, arg2, false);
    }

    // Matches the first constructor
    public static ExampleEnum create(String name, int arg1, String arg2, boolean arg3) {
        throw new IllegalStateException("Enum not extended");
    }

    // Matches the second constructor
    public static ExampleEnum create(String name, int arg1, String arg2) {
        throw new IllegalStateException("Enum not extended");
    }

    // ...
}
```

### The `init` method

Sometimes, an enum will do something with its constants or values after registration. However, as these instances statically call the associated entries, the values added via `create` may not be referenced. To get around this, extensible enums can override the `init` method to do any post-constructor setup required by the specific enum constant.

```java
// As an example
public enum ExampleEnumInit {
    VALUE_1(0, "value_1", false),
    VALUE_2(1, "value_2", true);

    private static final Map<String, boolean> ARG2_TO_ARG3 =
        Arrays.stream(ExampleEnumInit.values())
        .collect(
            Collectors.toMap(
                e -> e.arg2,
                e -> e.arg3
            )
        );

    ExampleEnumInit(int arg1, String arg2, boolean arg3) {
        // ...
    }
}

// The final extended enum may look something like this
public enum ExampleEnumInit implements net.neoforged.neoforge.common.IExtensibleEnum {
    VALUE_1(0, "value_1", false),
    VALUE_2(1, "value_2", true);

    private static final Map<String, boolean> ARG2_TO_ARG3 =
        Arrays.stream(ExampleEnumInit.values())
        .collect(
            Collectors.toMap(
                e -> e.arg2,
                e -> e.arg3
            )
        );

    ExampleEnumInit(int arg1, String arg2, boolean arg3) {
        // ...
    }

    // Matches the constructor
    public static ExampleEnumInit create(String name, int arg1, String arg2, boolean arg3) {
        throw new IllegalStateException("Enum not extended");
    }

    @Override
    public void init() {
        // Gets called for every new enum
        ARG2_TO_ARG3.put(this.arg2, this.arg3);
    }
}
```

### Codecs

[Enum codecs][codec] generally take in the `values` array and resolve the codec instantly. As the array is evaluated before any mod entries are added, no mod entries will be supported. There are two solutions to this problem, which to choose depends on whether the enum implements the `StringRepresentable` interface or not.

For normal, non-`StringRepresentable` enums, the codec can be wrapped via `Codec#lazyInitialized` or [`NeoForgeStreamCodecs#lazy`][streamcodec]. These prevent the codec from being resolved until first usage, which will always be after all mod entries are added.

```java
// For some enum with codec
public enum ExampleEnumCodec {
    // ...
    ;

    public static final Codec<ExampleEnumCodec> CODEC = Codec.of(/* ... */);
    public static final StreamCodec<ByteBuf, ExampleEnumCodec> CODEC = StreamCodec.of(/* ... */);

    // ...
}

// The final extended enum may look something like this
public enum ExampleEnumCodec implements net.neoforged.neoforge.common.IExtensibleEnum {
    // ...
    ;

    public static final Codec<ExampleEnumCodec> CODEC = Codec.lazyInitialized(
        () -> Codec.of(/* ... */)
    );
    public static final StreamCodec<ByteBuf, ExampleEnumCodec> CODEC = 
        net.neoforged.neoforge.common.util.NeoForgeStreamCodecs.lazy(
            () -> StreamCodec.of(/* ... */)
        );

    // ...
}
```

For `StringRepresentable` enums, `IExtensibleEnum` provides `createCodecForExtensibleEnum` and `createStreamCodecForExtensibleEnum` which do not cache the values at all, meaning they will always obtain up-to-date enums added at any point.

```java
// For some enum with codec
public enum ExampleEnumStringCodec implements StringRepresentable {
    // ...
    ;

    private static final IntFunction<ExampleEnumStringCodec> BY_ID = ByIdMap.continuous(
        ExampleEnumStringCodec::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
    );

    public static final Codec<ExampleEnumStringCodec> CODEC = StringRepresentable.fromEnum(ExampleEnumStringCodec::values);
    public static final StreamCodec<ByteBuf, ExampleEnumStringCodec> CODEC = ByteBufCodecs.idMapper(BY_ID, ExampleEnumStringCodec::getId);

    ExampleEnumStringCodec(int id, String serializedName) {
        // ...
    }

    // ...
}

// The final extended enum may look something like this
public enum ExampleEnumStringCodec implements StringRepresentable, net.neoforged.neoforge.common.IExtensibleEnum {
    // ...
    ;

    private static final java.util.Map<String, ExampleEnumStringCodec> BY_NAME = 
        java.util.Arrays.stream(ExampleEnumStringCodec.values())
        .collect(java.util.stream.Collectors.toMap(
            e -> e.serializedName, e -> e
        ));


    public static final Codec<ExampleEnumStringCodec> CODEC =
        net.neoforged.neoforge.common.IExtensibleEnum.createCodecForExtensibleEnum(
            ExampleEnumStringCodec::values, ExampleEnumStringCodec::byName
        );
    public static final StreamCodec<ByteBuf, ExampleEnumStringCodec> CODEC =
        net.neoforged.neoforge.common.IExtensibleEnum.createStreamCodecForExtensibleEnum(ExampleEnumStringCodec::values);

    ExampleEnumStringCodec(int id, String serializedName) {
        // ...
    }

    @Override
    public void init() {
        BY_NAME.put(this.serializedName, this);
    }

    public static ExampleEnumStringCodec byName(String name) {
        return BY_NAME.get(name);
    }

    // ...
}
```

[codec]: ../datastorage/codecs.md
[streamcodec]: ../networking/streamcodecs.md#vanilla-and-neoforge
