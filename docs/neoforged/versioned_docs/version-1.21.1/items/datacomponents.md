---
sidebar_position: 2
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Data Components

Data components are key-value pairs within a map used to store data on an `ItemStack`. Each piece of data, such as firework explosions or tools, are stored as actual objects on the stack, making the values visible and operable without having to dynamically transform a general encoded instance (e.g., `CompoundTag`, `JsonElement`).

## `DataComponentType`

Each data component has an associated `DataComponentType<T>`, where `T` is the component value type. The `DataComponentType` represents a key to reference the stored component value along with some codecs to handle reading and writing to the disk and network, if desired.

A list of existing components can be found within `DataComponents`.

### Creating Custom Data Components

The component value associated with the `DataComponentType` must implement `hashCode` and `equals` and should be considered **immutable** when stored.

:::note
Component values can very easily be implemented using a record. Record fields are immutable and implement `hashCode` and `equals`.
:::

```java
// A record example
public record ExampleRecord(int value1, boolean value2) {}

// A class example
public class ExampleClass {

    private final int value1;
    // Can be mutable, but care needs to be taken when using
    private boolean value2;

    public ExampleClass(int value1, boolean value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value1, this.value2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof ExampleClass ex
                && this.value1 == ex.value1
                && this.value2 == ex.value2;
        }
    }
}
```

A standard `DataComponentType` can be created via `DataComponentType#builder` and built using `DataComponentType.Builder#build`. The builder contains three settings: `persistent`, `networkSynchronized`, `cacheEncoding`.

`persistent` specifies the [`Codec`][codec] used to read and write the component value to disk. `networkSynchronized` specifies the `StreamCodec` used to read and write the component across the network. If `networkSynchronized` is not specified, then the `Codec` provided in `persistent` will be wrapped and used as the [`StreamCodec`][streamcodec].

:::warning
Either `persistent` or `networkSynchronized` must be provided in the builder; otherwise, a `NullPointerException` will be thrown. If no data should be sent across the network, then set `networkSynchronized` to `StreamCodec#unit`, providing the default component value.
:::

`cacheEncoding` caches the encoding result of the `Codec` such that any subsequent encodes uses the cached value if the component value hasn't changed. This should only be used if the component value is expected to rarely or never change.

`DataComponentType` are registry objects and must be [registered].

<Tabs defaultValue="latest">
<TabItem value="latest" label="Latest">

```java
// Using ExampleRecord(int, boolean)
// Only one Codec and/or StreamCodec should be used below
// Multiple are provided for an example

// Basic codec
public static final Codec<ExampleRecord> BASIC_CODEC = RecordCodecBuilder.create(instance ->
    instance.group(
        Codec.INT.fieldOf("value1").forGetter(ExampleRecord::value1),
        Codec.BOOL.fieldOf("value2").forGetter(ExampleRecord::value2)
    ).apply(instance, ExampleRecord::new)
);
public static final StreamCodec<ByteBuf, ExampleRecord> BASIC_STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.INT, ExampleRecord::value1,
    ByteBufCodecs.BOOL, ExampleRecord::value2,
    ExampleRecord::new
);

// Unit stream codec if nothing should be sent across the network
public static final StreamCodec<ByteBuf, ExampleRecord> UNIT_STREAM_CODEC = StreamCodec.unit(new ExampleRecord(0, false));


// In another class
// The specialized DeferredRegister.DataComponents simplifies data component registration and avoids some generic inference issues with the `DataComponentType.Builder` within a `Supplier`
public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "examplemod");

public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExampleRecord>> BASIC_EXAMPLE = REGISTRAR.registerComponentType(
    "basic",
    builder -> builder
        // The codec to read/write the data to disk
        .persistent(BASIC_CODEC)
        // The codec to read/write the data across the network
        .networkSynchronized(BASIC_STREAM_CODEC)
);

/// Component will not be saved to disk
public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExampleRecord>> TRANSIENT_EXAMPLE = REGISTRAR.registerComponentType(
    "transient",
    builder -> builder.networkSynchronized(BASIC_STREAM_CODEC)
);

// No data will be synced across the network
public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExampleRecord>> NO_NETWORK_EXAMPLE = REGISTRAR.registerComponentType(
   "no_network",
   builder -> builder
        .persistent(BASIC_CODEC)
        // Note we use a unit stream codec here
        .networkSynchronized(UNIT_STREAM_CODEC)
);
```

</TabItem>
<TabItem value="21.1.48" label="[21.0.0, 21.1.48]">

```java
// Using ExampleRecord(int, boolean)
// Only one Codec and/or StreamCodec should be used below
// Multiple are provided for an example

// Basic codec
public static final Codec<ExampleRecord> BASIC_CODEC = RecordCodecBuilder.create(instance ->
    instance.group(
        Codec.INT.fieldOf("value1").forGetter(ExampleRecord::value1),
        Codec.BOOL.fieldOf("value2").forGetter(ExampleRecord::value2)
    ).apply(instance, ExampleRecord::new)
);
public static final StreamCodec<ByteBuf, ExampleRecord> BASIC_STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.INT, ExampleRecord::value1,
    ByteBufCodecs.BOOL, ExampleRecord::value2,
    ExampleRecord::new
);

// Unit stream codec if nothing should be sent across the network
public static final StreamCodec<ByteBuf, ExampleRecord> UNIT_STREAM_CODEC = StreamCodec.unit(new ExampleRecord(0, false));


// In another class
// The specialized DeferredRegister.DataComponents simplifies data component registration and avoids some generic inference issues with the `DataComponentType.Builder` within a `Supplier`
public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents("examplemod");

public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExampleRecord>> BASIC_EXAMPLE = REGISTRAR.registerComponentType(
    "basic",
    builder -> builder
        // The codec to read/write the data to disk
        .persistent(BASIC_CODEC)
        // The codec to read/write the data across the network
        .networkSynchronized(BASIC_STREAM_CODEC)
);

/// Component will not be saved to disk
public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExampleRecord>> TRANSIENT_EXAMPLE = REGISTRAR.registerComponentType(
    "transient",
    builder -> builder.networkSynchronized(BASIC_STREAM_CODEC)
);

// No data will be synced across the network
public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExampleRecord>> NO_NETWORK_EXAMPLE = REGISTRAR.registerComponentType(
   "no_network",
   builder -> builder
        .persistent(BASIC_CODEC)
        // Note we use a unit stream codec here
        .networkSynchronized(UNIT_STREAM_CODEC)
);
```
</TabItem>
</Tabs>

## The Component Map

All data components are stored within a `DataComponentMap`, using the `DataComponentType` as the key and the object as the value. `DataComponentMap` functions similarly to a read-only `Map`. As such, there are methods to `#get` an entry given its `DataComponentType` or provide a default if not present (via `#getOrDefault`).

```java
// For some DataComponentMap map

// Will get dye color if component is present
// Otherwise null
@Nullable
DyeColor color = map.get(DataComponents.BASE_COLOR);
```

### `PatchedDataComponentMap`

As the default `DataComponentMap` only provides methods for read-based operations, write-based operations are supported using the subclass `PatchedDataComponentMap`. This includes `#set`ting the value of a component or `#remove`ing it altogether.

`PatchedDataComponentMap` stores changes using a prototype and patch map. The prototype is a `DataComponentMap` that contains the default components and their
values this map should have. The patch map is a map of `DataComponentType`s to `Optional` values that contain the changes made to the default components.

```java
// For some PatchedDataComponentMap map

// Sets the base color to white
map.set(DataComponents.BASE_COLOR, DyeColor.WHITE);

// Removes the base color by
// - Removing the patch if no default is provided
// - Setting an empty optional if there is a default
map.remove(DataComponents.BASE_COLOR);
```

:::danger
Both the prototype and patch map are part of the hash code for the `PatchedDataComponentMap`. As such, any component values within the map should be treated as **immutable**. Always call `#set` or one of its referring methods discussed below after modifying the value of a data component.
:::

## The Component Holder

All instances that can hold data components implement `DataComponentHolder`. `DataComponentHolder` is effectively a delegate to the read-only methods within `DataComponentMap`.

```java
// For some ItemStack stack

// Delegates to 'DataComponentMap#get'
@Nullable
DyeColor color = stack.get(DataComponents.BASE_COLOR);
```

### `MutableDataComponentHolder`

`MutableDataComponentHolder` is an interface provided by NeoForge to support write-based methods to the component map. All implementations within Vanilla and NeoForge store data components using a `PatchedDataComponentMap`, so the `#set` and `#remove` methods also have delegates with the same name.

In addition, `MutableDataComponentHolder` also provides an `#update` method which handles getting the component value or the provided default if none is set, operating on the value, and then setting it back to the map. The operator is either a `UnaryOperator`, which takes in the component value and returns the component value, or a `BiFunction`, which takes in the component value and another object and returns the component value.

```java
// For some ItemStack stack

FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);

// Modifying the component value
explosion = explosion.withFadeColors(new IntArrayList(new int[] {1, 2, 3}));

// Since we modified the component value, 'set' should be called afterward
stack.set(DataComponents.FIREWORK_EXPLOSION, explosion);

// Update the component value (calls 'set' internally)
stack.update(
    DataComponents.FIREWORK_EXPLOSION,
    // Default value if no component value is present
    FireworkExplosion.DEFAULT,
    // Return a new FireworkExplosion to set
    explosion -> explosion.withFadeColors(new IntArrayList(new int[] {4, 5, 6}))
);

stack.update(
    DataComponents.FIREWORK_EXPLOSION,
    // Default value if no component value is present
    FireworkExplosion.DEFAULT,
    // An object that is supplied to the function
    new IntArrayList(new int[] {7, 8, 9}),
    // Return a new FireworkExplosion to set
    FireworkExplosion::withFadeColors
);
```

## Adding Default Data Components to Items

Although data components are stored on an `ItemStack`, a map of default components can be set on an `Item` to be passed to the `ItemStack` as a prototype when constructed. A component can be added to the `Item` via `Item.Properties#component`.

```java
// For some DeferredRegister.Items REGISTRAR
public static final Item COMPONENT_EXAMPLE = REGISTRAR.register("component",
    // register is used over other overloads as the DataComponentType has not been registered yet
    () -> new Item(
        new Item.Properties()
        .component(BASIC_EXAMPLE.value(), new ExampleRecord(24, true))
    )
);
```

If the data component should be added to an existing item that belongs to Vanilla or another mod, then `ModifyDefaultComponentsEvent` should be listened for on the [**mod event bus**][modbus]. The event provides the `modify` and `modifyMatching` methods which allows the `DataComponentPatch.Builder` to be modified for the associated items. The builder can either `#set` components or `#remove` existing components.

```java
@SubscribeEvent // on the mod event bus
public static void modifyComponents(ModifyDefaultComponentsEvent event) {
    // Sets the component on melon seeds
    event.modify(Items.MELON_SEEDS, builder ->
        builder.set(BASIC_EXAMPLE.value(), new ExampleRecord(10, false))
    );

    // Removes the component for any items that have a crafting item
    event.modifyMatching(
        item -> item.hasCraftingRemainingItem(),
        builder -> builder.remove(DataComponents.BUCKET_ENTITY_DATA)
    );
}
```

## Using Custom Component Holders

To create a custom data component holder, the holder object simply needs to implement `MutableDataComponentHolder` and implement the missing methods. The holder object must contain a field representing the `PatchedDataComponentMap` to implement the associated methods.

```java
public class ExampleHolder implements MutableDataComponentHolder {

    private int data;
    private final PatchedDataComponentMap components;

    // Overloads can be provided to supply the map itself
    public ExampleHolder() {
        this.data = 0;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    @Override
    public DataComponentMap getComponents() {
        return this.components;
    }

    @Nullable
    @Override
    public <T> T set(DataComponentType<? super T> componentType, @Nullable T value) {
        return this.components.set(componentType, value);
    }

    @Nullable
    @Override
    public <T> T remove(DataComponentType<? extends T> componentType) {
        return this.components.remove(componentType);
    }

    @Override
    public void applyComponents(DataComponentPatch patch) {
        this.components.applyPatch(patch);
    }

    @Override
    public void applyComponents(DataComponentMap components) {
        this.components.setAll(p_330402_);
    }

    // Other methods
}
```

### `DataComponentPatch` and Codecs

To persist components to disk or send information across the network, the holder could send the entire `DataComponentMap`. However, this is generally a waste of information as any defaults will already be present wherever the data is sent to. So, instead, we use a `DataComponentPatch` to send the associated data. `DataComponentPatch`es only contain the patch information of the component map without any defaults. The patches are then applied to the prototype in the receiver's location.

A `DataComponentPatch` can be created from a `PatchedDataComponentMap` via `#patch`. Likewise, `PatchedDataComponentMap#fromPatch` can construct a `PatchedDataComponentMap` given the prototype `DataComponentMap` and a `DataComponentPatch`.

```java
public class ExampleHolder implements MutableDataComponentHolder {

    public static final Codec<ExampleHolder> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("data").forGetter(ExampleHolder::getData),
            DataCopmonentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(holder -> holder.components.asPatch())
        ).apply(instance, ExampleHolder::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExampleHolder> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ExampleHolder::getData,
        DataComponentPatch.STREAM_CODEC, holder -> holder.components.asPatch(),
        ExampleHolder::new
    );

    // ...

    public ExampleHolder(int data, DataComponentPatch patch) {
        this.data = data;
        this.components = PatchedDataComponentMap.fromPatch(
            // The prototype map to apply to
            DataComponentMap.EMPTY,
            // The associated patches
            patch
        );
    }

    // ...
}
```

[Syncing the holder data across the network][network] and reading/writing the data to disk must be done manually.

[registered]: ../concepts/registries.md
[codec]: ../datastorage/codecs.md
[modbus]: ../concepts/events.md#event-buses
[network]: ../networking/payload.md
[streamcodec]: ../networking/streamcodecs.md
