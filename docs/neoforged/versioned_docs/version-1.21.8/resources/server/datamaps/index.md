# Data Maps

A data map contains data-driven, reloadable objects that can be attached to a registered object. This system allows for more easily data-driving game behavior, as they provide functionality such as syncing or conflict resolution, leading to a better and more configurable user experience. You can think of [tags] as registry object ➜ boolean maps, while data maps are more flexible registry object ➜ object maps. Similar to [tags], data maps will add to their corresponding data map rather than overwriting.

Data maps can be attached to both static, built-in, registries and dynamic data-driven datapack registries. Data maps support reloading through the use of the `/reload` command or any other means that reload server resources.

NeoForge provides various [built-in data maps][builtin] for common use cases, replacing hardcoded vanilla fields. More info can be found in the linked article.

## File Location

Data maps are loaded from a JSON file located at `<mapNamespace>/data_maps/<registryNamespace>/<registryPath>/<mapPath>.json`, where:

- `<mapNamespace>` is the namespace of the ID of the data map,
- `<mapPath>` is the path of the ID of the data map,
- `<registryNamespace>` is the namespace of the ID of the registry (omitted if it is `minecraft`), and
- `<registryPath>` is the path of the ID of the registry.

Examples:

- For a data map named `mymod:drop_healing` for the `minecraft:item` registry (as in the example below), the path will be `mymod/data_maps/item/drop_healing.json`.
- For a data map named `somemod:somemap` for the `minecraft:block` registry, the path will be `somemod/data_maps/block/somemap.json`.
- For a data map named `example:stuff` for the `somemod:custom` registry, the path will be `example/data_maps/somemod/custom/stuff.json`.

## JSON Structure

A data map file itself may contain the following fields:

- `replace`: A boolean that will clear the data map before adding the values of this file. This should never be shipped by mods, and only be used by pack developers that want to overwrite this map for their own purposes.
- `neoforge:conditions`: A list of [loading conditions][conditions].
- `values`: A map of registry IDs or tag IDs to values that should be added to the data map by your mod. The structure of the values themselves is defined by the data map's codec (see below).
- `remove`: A list of registry IDs or tag IDs to be removed from the data map.

### Adding Values

For example, let's assume that we have a data map object with two float keys `amount` and `chance` for the registry `minecraft:item`. A corresponding data map file could look something like this:

```json5
{
    "values": {
        // Attach a value to the carrot item
        "minecraft:carrot": {
            "amount": 12,
            "chance": 1
        },
        // Attach a value to all items in the logs tag
        "#minecraft:logs": {
            "amount": 1,
            "chance": 0.1
        }
    }
}
```

Data maps may support [mergers][mergers], which will cause custom merging behavior in the case of a conflict, e.g. if two mods add a data map value for the same item. To avoid the merger from triggering, we can specify the `replace` field on the element level, like so:

```json5
{
    "values": {
        // Overwrite the value of the carrot item
        "minecraft:carrot": {
            // highlight-next-line
            "replace": true,
            // The new value will be under a value sub-object
            "value": {
                "amount": 12,
                "chance": 1
            }
        }
    }
}
```

### Removing Existing Values

Removing elements can be done by specifying a list of item IDs or tag IDs to remove:

```json5
{
    // We do not want the potato to have a value, even if another mod's data map added it
    "remove": [
        "minecraft:potato"
    ]
}
```

Removals run after additions, so we can include a tag and then exclude certain elements from it again:

```json5
{
    "values": {
        "#minecraft:logs": { /* ... */ }
    },
    // Exclude crimson stem again
    "remove": [
        "minecraft:crimson_stem"
    ]
}
```

Data maps may support custom [removers] with additional arguments. To supply these, the `remove` list can be transformed into a JSON object that contains the to-be-removed elements as map keys and the additional data as the associated value. For example, let's assume that our remover object is serialized to a string, then our remover map could look something like this:

```json5
{
    "remove": {
        // The remover will be deserialized from the value (`somekey1` in this case)
        // and applied to the value attached to the carrot item
        "minecraft:carrot": "somekey1"
    }
}
```

## Custom Data Maps

To begin, we define the format of our data map entries. **Data map entries must be immutable**, making records ideal for this. Reiterating our example from above with two float values `amount` and `chance`, our data map entries will look something like this:

```java
public record ExampleData(float amount, float chance) {}
```

Like many other things, data maps are serialized and deserialized using [codecs]. This means that we need to provide a codec for our data map entry that we will use in a bit:

```java
public record ExampleData(float amount, float chance) {
    public static final Codec<ExampleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(ExampleData::amount),
            Codec.floatRange(0, 1).fieldOf("chance").forGetter(ExampleData::chance)
    ).apply(instance, ExampleData::new));
}
```

Next, we create the data map itself:

```java
// In this example, we register the data map for the minecraft:item registry, hence we use Item as the generic.
// Adjust the types accordingly if you want to create a data map for a different registry.
public static final DataMapType<Item, ExampleData> EXAMPLE_DATA = DataMapType.builder(
        // The ID of the data map. Data map files for this data map will be located at
        // <yourmodid>:examplemod/data_maps/item/example_data.json.
        ResourceLocation.fromNamespaceAndPath("examplemod", "example_data"),
        // The registry to register the data map for.
        Registries.ITEM,
        // The codec of the data map entries.
        ExampleData.CODEC
).build();
```

Finally, register the data map during the [`RegisterDataMapTypesEvent`][events] on the [mod event bus][modbus]:

```java
@SubscribeEvent // on the mod event bus
public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
    event.register(EXAMPLE_DATA);
}
```

### Syncing

Synced data maps will have their values synced to clients. A data map can be marked as synced by calling `#synced` on the builder, like so:

```java
public static final DataMapType<Item, ExampleData> EXAMPLE_DATA = DataMapType.builder(...)
        .synced(
                // The codec used for syncing. May be identical to the normal codec, but may also be
                // a codec with less fields, omitting parts of the object that are not required on the client.
                ExampleData.CODEC,
                // Whether the data map is mandatory or not. Marking a data map as mandatory will disconnect clients
                // that are missing the data map on their side; this includes vanilla clients.
                false
        ).build();
```

### Usage

As data maps can be used on any registry, they must be queried through `Holder`s, not through actual registry objects. Moreover, it will only work for reference holders, not `Direct` holders. However, most places will return a reference holder, for example `Registry#wrapAsHolder`, `Registry#getHolder` or the different `builtInRegistryHolder` methods, so in most situations this shouldn't be a problem.

You can then query the data map value via `Holder#getData(DataMapType)`. If an object does not have a data map value attached, the method will return `null`. Reusing our `ExampleData` from before, let's use them to heal the player whenever he picks them up:

```java
@SubscribeEvent // on the game event bus
public static void itemPickup(ItemEntityPickupEvent.Post event) {
    ItemStack stack = event.getItemStack();
    // Get a Holder<Item> via ItemStack#getItemHolder.
    Holder<Item> holder = stack.getItemHolder();
    // Get the data from the holder.
    //highlight-next-line
    ExampleData data = holder.getData(EXAMPLE_DATA);
    if (data != null) {
        // The values are present, so let's do something with them!
        Player player = event.getPlayer();
        if (player.getLevel().getRandom().nextFloat() > data.chance()) {
            player.heal(data.amount());
        }
    }
}
```

This process of course also works for all data maps provided by NeoForge.

## Advanced Data Maps

Advanced data maps are data maps that use `AdvancedDataMapType` instead of the standard `DataMapType` (of which `AdvancedDataMapType` is a subclass). They have some extra functionality, namely the ability to specify custom mergers and custom removers. Implementing this is highly recommended for data maps whose values are collections or collection-likes, such as `List`s or `Map`s.

While `DataMapType` has two generics `R` (registry type) and `T` (data map value type), `AdvancedDataMapType` has one more: `VR extends DataMapValueRemover<R, T>`. This generic allows for datagenning removers with proper type safety.

`AdvancedDataMapType`s are created using `AdvancedDataMapType#builder()` instead of `DataMapType#builder()`, returning an `AdvancedDataMapType.Builder`. This builder has two extra methods `#remover` and `#merger` for specifying removers and mergers (see below), respectively. All other functionality, including syncing, remains the same.

### Mergers

A merger can be used to handle conflicts between multiple data packs that attempt to add a value for the same object. The default merger (`DataMapValueMerger#defaultMerger`) will overwrite existing values (from e.g. data packs with lower priority) with new values, so a custom merger is necessary if this isn't the desired behavior.

The merger will be given the two conflicting values, as well as the objects the values are being attached to (as an `Either<TagKey<R>, ResourceKey<R>>`, since values can be attached to all objects in a tag or a single object) and the object's owning registry, and should return the value that should actually be attached. Generally, mergers should simply merge and not perform overwrites if possible (i.e. only if merging the normal way doesn't work). If a data pack wants to bypass the merger, it should specify the `replace` field on the object (see [Adding Values][add]).

Let's imagine a scenario where we have a data map that adds integers to items. We could then simply resolve conflicts by adding both values, like so:

```java
public class IntMerger implements DataMapValueMerger<Item, Integer> {
    @Override
    public Integer merge(Registry<Item> registry,
            Either<TagKey<Item>, ResourceKey<Item>> first, Integer firstValue,
            Either<TagKey<Item>, ResourceKey<Item>> second, Integer secondValue) {
        return firstValue + secondValue;
    }
}
```

This way, if one pack specifies the value 12 for `minecraft:carrot` and another pack specifies the value 15 for `minecraft:carrot`, then the final value for `minecraft:carrot` will be 27. If either of these objects specify `"replace": true`, then that object's value will be used. If both specify `"replace": true`, then the higher datapack's value is used.

Finally, don't forget to actually specify the merger in the builder, like so:

```java
// The types of the data map must match the type of the merger.
AdvancedDataMapType<Item, Integer> ADVANCED_MAP = AdvancedDataMapType.builder(...)
        .merger(new IntMerger())
        .build();
```

:::tip
NeoForge provides default mergers for lists, sets and maps in `DataMapValueMerger`.
:::

### Removers

Similar to mergers for more complex data, removers can be used for proper handling of `remove` clauses for an element. The default remover (`DataMapValueRemover.Default.INSTANCE`) will simply remove any and all information related to the specified object, so we want to use a custom remover to remove only parts of the object's data.

The codec passed to the builder (read on) will be used to decode remover instances. The remover will then be passed the value currently attached to the object and its source, and should return an `Optional` of the value to replace the old value. Alternatively, an empty `Optional` will lead to the value being actually removed.

Consider the following example of a remover that will remove a value with a specific key from a `Map<String, String>`-based data map:

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

With this remover in mind, consider the following data file:

```json5
{
    "values": {
        "minecraft:carrot": {
            "somekey1": "value1",
            "somekey2": "value2"
        }
    }
}
```

Now, consider this second data file that is placed at a higher priority than the first one:

```json5
{
    "remove": {
        // As the remover is decoded as a string, we can use a string as the value here.
        // If it were decoded as an object, we would have needed to use an object.
        "minecraft:carrot": "somekey1"
    }
}
```

That way, after both files are applied, the final result will be (an in-memory representation of) this:

```json5
{
    "values": {
        "minecraft:carrot": {
            "somekey2": "value2"
        }
    }
}
```

As with mergers, don't forget to add them to the builder. Note that we simply use the codec here:

```java
// We assume AdvancedData contains a Map<String, String> property of some sort.
AdvancedDataMapType<Item, AdvancedData> ADVANCED_MAP = AdvancedDataMapType.builder(...)
        .remover(MapRemover.CODEC)
        .build();
```

## Data Generation

Data maps can be [datagenned][datagen] by extending `DataMapProvider` and overriding `#gather` to create your entries. Reusing the `ExampleData` from before (with float values `amount` and `chance`), our datagen file could look something like this:

```java
public class MyDataMapProvider extends DataMapProvider {
    public MyDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }
    
    @Override
    protected void gather() {
        // We create a builder for the EXAMPLE_DATA data map and add our entries using #add.
        this.builder(EXAMPLE_DATA)
                // We turn on replacing. Don't ever ship a mod like this! This is purely for educational purposes.
                .replace(true)
                // We add the value "amount": 10, "chance": 1 for all slabs. The boolean parameter controls
                // the "replace" field, which should always be false in a mod.
                .add(ItemTags.SLABS, new ExampleData(10, 1), false)
                // We add the value "amount": 5, "chance": 0.2 for apples.
                .add(Items.APPLE.builtInRegistryHolder(), new ExampleData(5, 0.2f), false) // Can also use Registry#wrapAsHolder to get the holder of a registry object
                // We remove wooden slabs again.
                .remove(ItemTags.WOODEN_SLABS)
                // We add a mod loaded condition for Botania, because why not.
                .conditions(new ModLoadedCondition("botania"));
    }
}
```

This would then result in the following JSON file:

```json5
{
    "replace": true,
    "values": {
        "#minecraft:slabs": {
            "amount": 10,
            "chance": 1.0
        },
        "minecraft:apple": {
            "amount": 5,
            "chance": 0.2
        }
    },
    "remove": [
        "#minecraft:wooden_slabs"
    ],
    "neoforge:conditions": [
        {
            "type": "neoforge:mod_loaded",
            "modid": "botania"
        }
    ]
}
```

Like all data providers, don't forget to add the provider to the event:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    // Call event.createDatapackRegistryObjects(...) first if adding datapack objects

    event.createProvider(MyDataMapProvider::new);
}
```

[builtin]: builtin.md
[codecs]: ../../../datastorage/codecs.md
[conditions]: ../conditions.md
[datagen]: ../../index.md#data-generation
[events]: ../../../concepts/events.md
[add]: #adding-values
[mergers]: #mergers
[modbus]: ../../../concepts/events.md#event-buses
[removers]: #removers
[tags]: ../tags.md
