# Data Maps

A registry data map contains data-driven, reloadable objects that can be attached to a registry object. This system allows more easily data-driving game behaviour, as they provide functionality such as syncing or conflict resolution, leading to a better and more configurable user experience.  

You can think of tags as registry object ➜ boolean maps, while data maps are more flexible registry object ➜ object maps.

A data map can be attached to both static, built-in, registries and dynamic data-driven datapack registries.  

Data maps support reloading through the use of the `/reload` command or any other means that reload server resources.

## Registration

A data map type should be statically created and then registered to the `RegisterDataMapTypesEvent` (which is fired on the [mod event bus][events]). The `DataMapType` can be created using a DataMapType.Builder, through `DataMapType#builder`.  

The builder provides a `synced` method which can be used to mark a data map as synced and have it sent to clients.  

A simple `DataMapType` has two generic arguments: `R` (the type of the registry the data map is for) and `T` (the values that are being attached). A data map of `SomeObject`s that are attached to `Item`s can, as such, be represented as `DataMapType<Item, SomeObject>`.  

Data maps are serialized and deserialized using [Codecs][codecs].

Let's take the following record representing the data map value as an example:

```java
public record DropHealing(
    float amount, float chance
) {
    public static final Codec<DropHealing> CODEC = RecordCodecBuilder.create(in -> in.group(
        Codec.FLOAT.fieldOf("amount").forGetter(DropHealing::amount),
        Codec.floatRange(0, 1).fieldOf("chance").forGetter(DropHealing::chance)
    ).apply(in, DropHealing::new));
}
```

:::warning
The value (`T`) should be an *immutable* object, as otherwise weird behaviour can be caused if the object is attached to all entries within a tag (since no copy is created).
:::

For the purposes of this example, we will use this data map to heal players when they drop an item. The `DataMapType` can be created as such:

```java
public static final DataMapType<Item, DropHealing> DROP_HEALING = DataMapType.builder(
    new ResourceLocation("mymod:drop_healing"), Registries.ITEM, DropHealing.CODEC
).build();
```

and then registered to the `RegisterDataMapTypesEvent` using `RegisterDataMapTypesEvent#register`.

## Syncing

A synced data map will have its values synced to clients. A data map can be marked as synced using `DataMapType$Builder#synced(Codec<T> networkCodec, boolean mandatory)`. The values of the data map will then be synced using the `networkCodec`. If the `mandatory` flag is set to `true`, clients that do not support the data map (including Vanilla clients) will not be able to connect to the server, nor vice-versa. A non-mandatory data map on the other hand is optional, so it will not prevent any clients from joining.

:::tip
A separate network codec allows for packet sizes to be smaller, as you can choose what data to send, and in what format. Otherwise the default codec can be used.
:::

## JSON Structure and location

Data maps are loaded from a JSON file located at `mapNamespace/data_maps/registryNamespace/registryPath/mapPath.json`, where:
- `mapNamespace` is the namespace of the ID of the data map
- `mapPath` is the path of the ID of the data map
- `registryNamespace` is the namespace of the ID of the registry; if the namespace is `minecraft`, this value will be omitted
- `registryPath` is the path of the ID of the registry

For more information, please [check out the dedicated page][structure].

## Usage

As data maps can be used on any registry, they can be queried through `Holder`s, and not through the actual registry objects. You can query a data map value using `Holder#getData(DataMapType)`. If that object doesn't have a value attached, the method will return `null`.

:::note
Only reference holders will return a value in that method. `Direct` holders will **not**.  Generally, you will only encounter reference holders (which are returned by methods such as `Registry#wrapAsHolder`, `Registry#getHolder` or the different `builtInRegistryHolder` methods).
:::

To continue the example above, we can implement our intended behaviour as follows:

```java
@SubscribeEvent // on the game event bus
public static void onItemDrop(final ItemTossEvent event) {
    final ItemStack stack = event.getEntity().getItem();
    // ItemStack has a getItemHolder method that will return a Holder<Item> which points to the item the stack is of
    //highlight-next-line
    final DropHealing value = stack.getItemHolder().getData(DROP_HEALING);
    // Since getData returns null if the item will not have a drop healing value attached, we guard against it being null
    if (value != null) {
        // And here we simply use the values
        if (event.getPlayer().level().getRandom().nextFloat() > value.chance()) {
            event.getPlayer().heal(value.amount());
        }
    }
}
```

## Advanced data maps

Advanced data maps are data maps which have additional functionality. Namely, the ability of merging values and selectively removing them, through a remover. Implementing some form of merging and removers is highly recommended for data maps whose values are collection-likes (like `Map`s or `List`s).

`AdvancedDataMapType` have one more generic besides `T` and `R`: `VR extends DataMapValueRemover<R, T>`. This additional generic allows you to datagen remove objects with increased type safety.

### Creation

You create an `AdvancedDataMapType` using `AdvancedDataMapType#builder`. Unlike the normal builder, the builder returned by that method will have two more methods (`merger` and `remover`), and it will return an `AdvancedDataMapType`. Registration methods remain the same.

### Mergers

An advanced data map can provide a `DataMapValueMerger` through `AdvancedDataMapType#merger`. This merger will be used to handle conflicts between data packs that attempt to attach a value to the same object. The merger will be given the two conflicting values, and their sources (as an `Either<TagKey<R>, ResourceKey<R>>` since values can be attached to all entries within a tag, not just individual entries), and is expected to return the value that will actually be attached. Generally, mergers should simply merge the values, and should not perform "hard" overwrites unless necessary (i.e. if merging isn't possible). If a pack wants to bypass the merger, it can do so by specifying the object-level `replace` field.  

Let's imagine a scenario where we have a data map that attaches integers to items:

```java
public class IntMerger implements DataMapValueMerger<Item, Integer> {
    @Override
    public Integer merge(Registry<Item> registry, Either<TagKey<Item>, ResourceKey<Item>> first, Integer firstValue, Either<TagKey<Item>, ResourceKey<Item>> second, Integer secondValue) {
        //highlight-next-line
        return firstValue + secondValue;
    }
}
```

The above merger will merge the values if two datapacks attach to the same object. So if the first pack attaches the value `12` to `minecraft:carrot`, and the second pack attaches the value `15` to `minecraft:carrot`, the final value will be `27`. However, if the second pack specifies the object-level `replace` field, the final value will be `15` as the merger won't be invoked.

NeoForge provides some default mergers for merging lists, sets and maps in `DataMapValueMerger`.  

The default merger (`DataMapValueMerger#defaultMerger`) has the typical behaviour you'd expect from normal data packs, where the newest value (which comes from the highest datapack) overwrites the previous value.

### Removers

An advanced data map can provide a `DataMapValueRemover` through `AdvancedDataMapType#remover`. The remover will allow selective removals of data map values, effectively decomposition. While by default a datapack can only remove the whole object attached to a registry entry, with a remover it can remove just speciffic values from the attached object (i.e. just the entry with a given key in the case of a map, or the entry with a specific property in the case of a list).  

The codec that is passed to the builder will decode remover instances. These removers will then be given the value currently attached and its source, and are expected to create a new object to replace the old value. Alternatively, an empty `Optional` will lead to the value being completely removed.  

An example of a remover that will remove a value with a specific key from a `Map`-based data map:

```java
public record MapRemover(String key) implements DataMapValueRemover<Item, Map<String, String>> {
    public static final Codec<MapRemover> CODEC = Codec.STRING.xmap(MapRemover::new, MapRemover::key);
    
    @Override
    public Optional<Map<String, String>> remove(Map<String, String> value, Registry<Item> registry, Either<TagKey<Item>, ResourceKey<Item>> source, Item object) {
        final Map<String, String> newMap = new HashMap<>(value);
        newMap.remove(key);
        return Optional.of(newMap);
    }
}
```

With the remover above in mind, we're attaching maps of string to string to items. Take the following data map JSON file:

```json5
{
    "values": {
        //highlight-start
        "minecraft:carrot": {
            "somekey1": "value1",
            "somekey2": "value2"
        }
        //highlight-end
    }
}
```

That file will attach the map `[somekey1=value1, somekey2=value2]` to the `minecraft:carrot` item. Now, another datapack can come on top of it and remove just the value with the `somekey1` key, as such:

```json5
{
    "remove": {
        // As the remover is decoded as a string, we can use a string as the value here. If it were decoded as an object, we would have needed to use an object.
        //highlight-next-line
        "minecraft:carrot": "somekey1"
    }
}
```

After the second datapack is read and applied, the new value attached to the `minecraft:carrot` item will be `[somekey2=value2]`.

## Datagen

Data maps can be [generated][datagen] through `DataMapProvider`. You should extend that class, and then override the `generate` method to create your entries, similar to tag generation.

Considering the drop healing example from the start, we could generate some values as follows:

```java
public class DropHealingGen extends DataMapProvider {

    public DropHealingGen(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        // In the examples below, we do not need to forcibly replace any value as that's the default behaviour since a merger isn't provided, so the third parameter can be false.

        // If you were to provide a merger for your data map, then the third parameter will cause the old value to be overwritten if set to true, without invoking the merger
        builder(DROP_HEALING)
            // Always give entities that drop any item in the minecraft:fox_food tag 12 hearts
            .add(ItemTags.FOX_FOOD, new DropHealing(12, 1f), false)
            // Have a 10% chance of healing entities that drop an acacia boat by one point
            .add(Items.ACACIA_BOAT.builtInRegistryHolder(), new DropHealing(1, 0.1f), false);
    }
}
```

:::tip
There are `add` overloads that accept raw `ResourceLocation`s if you want to attach values to objects added by optional dependencies. In that case you should also provide [a loading condition][conditional] through the var-args parameter to avoid crashes.
:::

[events]: ../concepts/events.md
[codecs]: ../datastorage/codecs.md
[structure]: ./structure.md
[datagen]: ../resources/index.md#data-generation
[conditional]: ../resources/server/conditional.md
