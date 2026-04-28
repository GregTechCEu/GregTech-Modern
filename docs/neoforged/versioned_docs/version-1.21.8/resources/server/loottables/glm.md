# Global Loot Modifiers

Global Loot Modifiers, or GLMs for short, are a data-driven way to modify drops without the need to overwrite dozens or hundreds of vanilla loot tables, or to handle effects that would require interactions with another mod's loot tables without knowing what mods are loaded.

GLMs work by first rolling the associated [loot table][loottable] and then applying the GLM to the result of rolling the table. GLMs are also stacking, rather than last-load-wins, to allow for multiple mods to modify the same loot table, this is similar to [tags].

To register a GLM, you will need four things:

- A `global_loot_modifiers.json` file, located at `data/neoforge/loot_modifiers/global_loot_modifiers.json` (**not in your mod's namespace**). This file tells NeoForge what modifiers to apply, and in what order.
- A JSON file representing your loot modifier. This file contains all the data for your modification, allowing data packs to tweak your effect. It is located at `data/<namespace>/loot_modifiers/<path>.json`.
- A class that implements `IGlobalLootModifier` or extends `LootModifier` (which in turn implements `IGlobalLootModifier`). This class contains the code that makes the modifier work.
- A map [codec] to encode and decode your loot modifier class. Usually, this is implemented as a `public static final` field in the loot modifier class.

## `global_loot_modifiers.json`

The `global_loot_modifiers.json` file tells NeoForge what modifiers to apply to loot tables. The file may contain two keys:

- `entries` is a list of modifiers that should be loaded. The [`ResourceLocation`][resloc]s specified points to their associated entry within `data/<namespace>/loot_modifiers/<path>.json`. This list is ordered, meaning that modifiers will apply in the specified order, which is sometimes relevant when mod compatibility issues occur.
- `replace` denotes whether the modifiers should replace old ones (`true`) or simply add to the existing list (`false`). This works similar to the `replace` key in [tags], however unlike tags, the key is required here. Generally, modders should always use `false` here; the ability to use `true` is directed at modpack or data pack developers.

Example usage:

```json5
{
    "replace": false, // must be present
    "entries": [
        // represents a loot modifier in data/examplemod/loot_modifiers/example_glm_1.json
        "examplemod:example_glm_1",
        "examplemod:example_glm_2"
        // ...
    ]
}
```

## The Loot Modifier JSON

This file contains all values related to your modifier, for example chances to apply, what items to add, etc. It is recommended to avoid hard-coded values wherever possible so that data pack makers can adjust balance if they wish to. A loot modifier must contain at least two fields and may contain more, depending on the circumstances:

- The `type` field contains the registry name of the loot modifier.
- The `conditions` field is a list of loot table conditions for this modifier to activate.
- Additional properties may be required or optional, depending on the used codec.

:::tip
A common use case for GLMs is to add extra loot to one specific loot table. To achieve this, the [`neoforge:loot_table_id` condition][loottableid] can be used.
:::

An example usage may look something like this:

```json5
{
    // This is the registry name of the loot modifier
    "type": "examplemod:my_loot_modifier",
    "conditions": [
        // Loot table conditions here
    ],
    // Extra properties specified by the codec
    "field1": "somestring",
    "field2": 10,
    "field3": "minecraft:dirt"
}
```

## `IGlobalLootModifier` and `LootModifier`

To actually apply the loot modifier to the loot table, a `IGlobalLootModifier` implementation must be specified. In most cases, you will want to use the `LootModifier` subclass, which handles things like conditions for you. To get started, we extend `LootModifier` in our loot modifier class:

```java
// We cannot use a record because records cannot extend other classes.
public class MyLootModifier extends LootModifier {
    // See below for how the codec works.
    public static final MapCodec<MyLootModifier> CODEC = ...;
    // Our extra properties.
    private final String field1;
    private final int field2;
    private final Item field3;
    
    // First constructor parameter is the list of conditions. The rest is our extra properties.
    public MyLootModifier(LootItemCondition[] conditions, String field1, int field2, Item field3) {
        super(conditions);
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }
    
    // Return our codec here.
    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
    
    // This is where the magic happens. Use your extra properties here if needed.
    // Parameters are the existing loot, and the loot context.
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Add your items to generatedLoot here.
        return generatedLoot;
    }
}
```

:::info
The returned list of drops from a modifier is fed into other modifiers in the order they are registered. As such, modified loot can and should be expected to be modified by another loot modifier.
:::

## The Loot Modifier Codec

To tell the game about the existence of our loot modifier, we must define and [register] a [codec] for it. Reiterating on our previous example with the three fields, this would look something like this:

```java
public static final MapCodec<MyLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> 
        // LootModifier#codecStart adds the conditions field.
        LootModifier.codecStart(inst).and(inst.group(
                Codec.STRING.fieldOf("field1").forGetter(e -> e.field1),
                Codec.INT.fieldOf("field2").forGetter(e -> e.field2),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("field3").forGetter(e -> e.field3)
        )).apply(inst, MyLootModifier::new)
);
```

Then, we [register] the codec to the registry:

```java
public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ExampleMod.MOD_ID);

public static final Supplier<MapCodec<MyLootModifier>> MY_LOOT_MODIFIER =
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("my_loot_modifier", () -> MyLootModifier.CODEC);
```

## Builtin Loot Modifiers

NeoForge provides a loot modifier out of the box for you to use:

### `neoforge:add_table`

This loot modifier rolls a second loot table and adds the results to the loot table the modifier is applied to.

```json5
{
    "type": "neoforge:add_table",
    "conditions": [], // the required loot conditions
    "table": "minecraft:chests/abandoned_mineshaft" // the second table to roll
}
```

## Datagen

GLMs can be [datagenned][datagen]. This is done by subclassing `GlobalLootModifierProvider`:

```java
public class MyGlobalLootModifierProvider extends GlobalLootModifierProvider {
    // Get the parameters from the `GatherDataEvent`s.
    public MyGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ExampleMod.MOD_ID);
    }
    
    @Override
    protected void start() {
        // Call #add to add a new GLM. This also adds a corresponding entry in global_loot_modifiers.json.
        this.add(
                // The name of the modifier. This will be the file name.
                "my_loot_modifier_instance",
                // The loot modifier to add. For the sake of example, we add a weather loot condition.
                new MyLootModifier(new LootItemCondition[] {
                        WeatherCheck.weather().setRaining(true).build()
                }, "somestring", 10, Items.DIRT),
                // A list of data load conditions. Note that these are unrelated to the loot conditions
                // specified on the modifier itself. For the sake of example, we add a mod loaded condition.
                // An overload of #add is available that accepts a vararg of conditions instead of a list.
                List.of(new ModLoadedCondition("create"))
        );
    }
}
```

And like all data providers, you must register the provider to the `GatherDataEvent`s:

```java
@SubscribeEvent // on the mod event bus
public static void onGatherData(GatherDataEvent.Client event) {
    // Call event.createDatapackRegistryObjects(...) first if adding datapack objects

    event.createProvider(MyGlobalLootModifierProvider::new);
}
```

[codec]: ../../../datastorage/codecs.md
[datagen]: ../../index.md#data-generation
[loottable]: index.md
[loottableid]: lootconditions#neoforgeloot_table_id
[register]: ../../../concepts/registries.md#methods-for-registering
[resloc]: ../../../misc/resourcelocation.md
[tags]: ../tags.md
