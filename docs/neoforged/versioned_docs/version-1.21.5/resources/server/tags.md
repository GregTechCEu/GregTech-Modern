# Tags

A tag is, simply put, a list of registered objects of the same type. They are loaded from data files and can be used for membership checks. For example, crafting sticks will accept any combination of wooden planks (items tagged with `minecraft:planks`). Tags are often distinguished from "regular" objects by prefixing them with a `#` (for example `#minecraft:planks`, but `minecraft:oak_planks`).

Any [registry] can have tag files - while blocks and items are the most common use cases, other registries such as fluids, entity types or damage types often utilize tags as well. You can also create your own tags if you need them.

Tags are located at `data/<tag_namespace>/tags/<registry_path>/<tag_path>.json` for Minecraft registries, and `data/<tag_namespace>/tags/<registry_namespace>/<registry_path>/<tag_path>.json` for non-Minecraft registries. For example, to modify the `minecraft:planks` item tag, you would place your tag file at `data/minecraft/tags/item/planks.json`.

:::info
Unlike most other NeoForge data files, NeoForge-added tags do generally not use the `neoforge` namespace. Instead, they use the `c` namespace (e.g. `c:ingots/gold`). This is because the tags are unified between NeoForge and the Fabric mod loader, at the request of many modders developing on multiple loaders.

There are a few exceptions to this rule for some tags that tie closely into NeoForge systems. This includes many [damage type][damagetype] tags, for example.
:::

Overriding tag files is generally additive instead of replacing. This means that if two datapacks specify tag files with the same id, the contents of both files will be merged (unless otherwise specified). This behavior sets tags apart from most other data files, which instead replace any and all existing values.

## Tag File Format

Tag files have the following syntax:

```json5
{
    // The values of the tag.
    "values": [
        // A value object. Must specify the id of the object to add, and whether it is required.
        // If the entry is required, but the object is not present, the tag will not load. The "required" field
        // is technically optional, but when removed, the entry is equivalent to the shorthand below.
        {
            "id": "examplemod:example_ingot",
            "required": false
        }
        // Shorthand for {"id": "minecraft:gold_ingot", "required": true}, i.e. a required entry.
        "minecraft:gold_ingot",
        // A tag object. Distinguished from regular entries by the leading #. In this case, all planks
        // will be considered entries of the tag. Like normal entries, this can also have the "id"/"required" format.
        // Warning: Circular tag dependencies will lead to a datapack not being loaded!
        "#minecraft:planks"
    ],
    // Whether to remove all pre-existing entries before adding your own (true) or just add your own (false).
    // This should generally be false, the option to set this to true is primarily aimed at pack developers.
    "replace": false,
    // A finer-grained way to remove entries from the tag again, if present. Optional, NeoForge-added.
    // Entry syntax is the same as in the "values" array.
    "remove": [
        "minecraft:iron_ingot"
    ]
}
```

## Finding and Naming Tags

When you try to find an existing tag, it is generally recommended to follow these steps:

- Have a look at Minecraft's tags and see if the tag you're looking for is there. Minecraft's tags can be found in `BlockTags`, `ItemTags`, `EntityTypeTags` etc.
- If not, have a look at NeoForge's tags and see if the tag you're looking for is there. NeoForge's tags can be found in `Tags.Blocks`, `Tags.Items`, `Tags.EntityTypes`, etc.
- Otherwise, assume the tag is not specified in Minecraft or NeoForge, and thus you need to create your own tag.

When creating your own tag, you should ask yourself the following questions:

- Does this modify my mod's behavior? If yes, the tag should be in your mod's namespace. (This is common e.g. for my-thing-can-spawn-on-this-block kind of tags.)
- Would other mods want to use this tag as well? If yes, the tag should be in the `c` namespace. (This is common e.g. for new metals or gems.)
- Otherwise, use your mod's namespace.

Naming the tag itself also has some conventions to follow:

- Use the plural form. E.g.: `minecraft:planks`, `c:ingots`.
- Use folders for multiple objects of the same type, and an overall tag for each folder. E.g.: `c:ingots/iron`, `c:ingots/gold`, and `c:ingots` containing both. (Note: This is a NeoForge convention, Minecraft does not follow this convention for most tags.)

## Using Tags

To reference tags in code, you must create a `TagKey<T>`, where `T` is the type of tag (`Block`, `Item`, `EntityType<?>`, etc.), using a [registry key][regkey] and a [resource location][resloc]:

```java
public static final TagKey<Block> MY_TAG = TagKey.create(
        // The registry key. The type of the registry must match the generic type of the tag.
        Registries.BLOCK,
        // The location of the tag. This example will put our tag at data/examplemod/tags/blocks/example_tag.json.
        ResourceLocation.fromNamespaceAndPath("examplemod", "example_tag")
);
```

:::warning
Since `TagKey` is a record, its constructor is public. However, the constructor should not be used directly, as doing so can lead to various issues, for example when looking up tag entries.
:::

We can then use our tag to perform various operations on it. Let's start with the most obvious one: check whether an object is in the tag. The following examples will assume block tags, but the functionality is the exact same for every type of tag (unless otherwise specified):

```java
// Check whether dirt is in our tag.
// Assume access to Level level
boolean isInTag = level.registryAccess().lookupOrThrow(BuiltInRegistries.BLOCK).getOrThrow(MY_TAG).stream().anyMatch(holder -> holder.is(Items.DIRT));
```

Since this is a very verbose statement, especially when used often, `BlockState` and `ItemStack` - the two most common users of the tag system - each define a `#is` helper method, used like so:

```java
// Check whether the blockState's block is in our tag.
boolean isInBlockTag = blockState.is(MY_TAG);
// Check whether the itemStack's item is in our tag. Assumes the existence of MY_ITEM_TAG as a TagKey<Item>.
boolean isInItemTag = itemStack.is(MY_ITEM_TAG);
```

If needed, we can also get ourselves a set of tag entries to stream, like so:

```java
// Assume access to Level level
Stream<Holder<Block>> blocksInTag = level.registryAccess().lookupOrThrow(BuiltInRegistries.BLOCK).getOrThrow(MY_TAG).stream();
```

### Tags for Static Registries During Bootstrap

Sometimes, you need to get access to a `HolderSet` during the registry process. In this case, for static registries **only**, you can obtain the necessary `HolderGetter` via `BuiltInRegistries#acquireBootstrapRegistrationLookup`:

```java
// Assume access to Level level
HolderSet<Block> blockTag = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(MY_TAG);
```

## Datagen

Like many other JSON files, tags can be [datagenned][datagen]. Each kind of tag has its own datagen base class - one class for block tags, one for item tags, etc. -, and as such, we need one class for each kind of tag as well. All of these classes extend from the `TagsProvider<T>` base class, with `T` again being the type of the tag (`Block`, `Item`, etc.) The following table shows a list of tag providers for different objects:

| Type                       | Tag Provider Class                     |
|----------------------------|----------------------------------------|
| `BannerPattern`            | `BannerPatternTagsProvider`            |
| `Biome`                    | `BiomeTagsProvider`                    |
| `Block`                    | `BlockTagsProvider`                    |
| `CatVariant`               | `CatVariantTagsProvider`               |
| `DamageType`               | `DamageTypeTagsProvider`               |
| `Enchantment`              | `EnchantmentTagsProvider`              |
| `EntityType`               | `EntityTypeTagsProvider`               |
| `FlatLevelGeneratorPreset` | `FlatLevelGeneratorPresetTagsProvider` |
| `Fluid`                    | `FluidTagsProvider`                    |
| `GameEvent`                | `GameEventTagsProvider`                |
| `Instrument`               | `InstrumentTagsProvider`               |
| `Item`                     | `ItemTagsProvider`                     |
| `PaintingVariant`          | `PaintingVariantTagsProvider`          |
| `PoiType`                  | `PoiTypeTagsProvider`                  |
| `Structure`                | `StructureTagsProvider`                |
| `WorldPreset`              | `WorldPresetTagsProvider`              |

Of note is the `IntrinsicHolderTagsProvider<T>` class, which is a subclass of `TagsProvider<T>` and a common superclass for `BlockTagsProvider`, `ItemTagsProvider`, `FluidTagsProvider`, `EntityTypeTagsProvider`, and `GameEventTagsProvider`. These classes (from now on called intrinsic providers for simplicity) have some additional functionality for generation that will be outlined in a moment.

For the sake of example, let's assume that we want to generate block tags. (All other classes work the same with their respective tag types.)

```java
public class MyBlockTagsProvider extends BlockTagsProvider {
    // Get parameters from one of the `GatherDataEvent`s.
    public MyBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ExampleMod.MOD_ID);
    }

    // Add your tag entries here.
    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // Create a tag builder for our tag. This could also be e.g. a vanilla or NeoForge tag.
        this.tag(MY_TAG)
            // Add entries. This is a vararg parameter.
            // Non-intrinsic providers must provide ResourceKeys here instead of the actual objects.
            .add(Blocks.DIRT, Blocks.COBBLESTONE)
            // Add optional entries that will be ignored if absent. This example uses Botania's Pure Daisy.
            // Unlike #add, this is not a vararg parameter.
            .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "pure_daisy"))
            // Add a tag entry.
            .addTag(BlockTags.PLANKS)
            // Add multiple tag entries. This is a vararg parameter.
            // Can cause unchecked warnings that can safely be suppressed.
            .addTags(BlockTags.LOGS, BlockTags.WOODEN_SLABS)
            // Add an optional tag entry that will be ignored if absent.
            .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "ingots/tin"))
            // Add multiple optional tag entries. This is a vararg parameter.
            // Can cause unchecked warnings that can safely be suppressed.
            .addOptionalTags(ResourceLocation.fromNamespaceAndPath("c", "nuggets/tin"), ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/tin"))
            // Set the replace property to true.
            .replace()
            // Set the replace property back to false.
            .replace(false)
            // Remove entries. This is a vararg parameter. Accepts either resource locations, resource keys,
            // tag keys, or (intrinsic providers only) direct values.
            // Can cause unchecked warnings that can safely be suppressed.
            .remove(ResourceLocation.fromNamespaceAndPath("minecraft", "crimson_slab"), ResourceLocation.fromNamespaceAndPath("minecraft", "warped_slab"));
    }
}
```

This example results in the following tag JSON:

```json5
{
    "values": [
        "minecraft:dirt",
        "minecraft:cobblestone",
        {
            "id": "botania:pure_daisy",
            "required": false
        },
        "#minecraft:planks",
        "#minecraft:logs",
        "#minecraft:wooden_slabs",
        {
            "id": "c:ingots/tin",
            "required": false
        },
        {
            "id": "c:nuggets/tin",
            "required": false
        },
        {
            "id": "c:storage_blocks/tin",
            "required": false
        }
    ],
    "remove": [
        "minecraft:crimson_slab",
        "minecraft:warped_slab"
    ]
}
```

Like all data providers, add each tag provider to the `GatherDataEvent`s:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    // Call event.createDatapackRegistryObjects(...) first if adding datapack objects

    event.createProvider(MyBlockTagsProvider::new);
}
```

`ItemTagsProvider` has an additional helper method called `#copy`. It is intended for the common use case of item tags mirroring block tags:

```java
// In an ItemTagsProvider's #addTags method, assuming types TagKey<Block> and TagKey<Item> for the two parameters.
this.copy(EXAMPLE_BLOCK_TAG, EXAMPLE_ITEM_TAG);
```

### Custom Tag Providers

To create a custom tag provider for a custom [registry], or for a vanilla or NeoForge registry that doesn't have a tag provider by default, you can also create custom tag providers like so (using recipe type tags as an example):

```java
public class MyRecipeTypeTagsProvider extends TagsProvider<RecipeType<?>> {
    // Get parameters from the `GatherDataEvent`s.
    public MyRecipeTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        // Second parameter is the registry key we are generating the tags for.
        super(output, Registries.RECIPE_TYPE, lookupProvider, ExampleMod.MOD_ID);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) { /*...*/ }
}
```

If desirable and applicable, you can also extend `IntrinsicHolderTagsProvider<T>` instead of `TagsProvider<T>`, allowing you to pass in objects directly rather than just their resource keys. This additionally requires a function parameter that returns a resource key for a given object. Using attribute tags as an example:

```java
public class MyAttributeTagsProvider extends IntrinsicHolderTagsProvider<Attribute> {
    // Get parameters from the `GatherDataEvent`s.
    public MyAttributeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output,
            Registries.ATTRIBUTE,
            lookupProvider,
            // A function that, given an Attribute, returns a ResourceKey<Attribute>.
            attribute -> BuiltInRegistries.ATTRIBUTE.getResourceKey(attribute).orElseThrow(),
            ExampleMod.MOD_ID
        );
    }

    // Attributes can now be used here directly, instead of just their resource keys.
    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) { /*...*/ }
}
```

:::info
`TagsProvider` also exposes the `#getOrCreateRawBuilder` method, returning a `TagBuilder`. A `TagBuilder` allows adding raw `ResourceLocation`s to a tag, which can be useful in some scenarios. The `TagsProvider.TagAppender<T>` class, which is returned by `TagsProvider#tag`, is simply a wrapper around `TagBuilder`.
:::

[damagetype]: damagetypes.md
[datagen]: ../index.md#data-generation
[registry]: ../../concepts/registries.md
[regkey]: ../../misc/resourcelocation.md#resourcekeys
[resloc]: ../../misc/resourcelocation.md
