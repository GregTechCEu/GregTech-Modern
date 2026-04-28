# Tag Generation

[Tags] can be generated for a mod by subclassing `TagsProvider` and implementing `#addTags`. After implementation, the provider must be [added][datagen] to the `DataGenerator`.

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    event.getGenerator().addProvider(
        // Tell generator to run only when server data are generating
        event.includeServer(),
        // Extends net.neoforged.neoforge.common.data.BlockTagsProvider
        output -> new MyBlockTagsProvider(
            output,
            event.getLookupProvider(),
            MOD_ID,
            event.getExistingFileHelper()
        )
    );
}
```

## `TagsProvider`

The tags provider has two methods used for generating tags: creating a tag with objects and other tags via `#tag`, or using tags from other object types to generate the tag data via `#getOrCreateRawBuilder`.

:::note
Typically, a provider will not call `#getOrCreateRawBuilder` directly unless a registry contains a representation of objects from a different registry (blocks have item representations to obtain the blocks in the inventory).
:::

When `#tag` is called, a `TagAppender` is created which acts as a chainable consumer of elements to add to the tag:

Method           | Description
:---:            | :---
`add`            | Adds an object to a tag through its resource key. 
`addOptional`    | Adds an object to a tag through its name. If the object is not present, then the object will be skipped when loading.
`addTag`         | Adds a tag to a tag through its tag key. All elements within the inner tag are now a part of the outer tag.
`addOptionalTag` | Adds a tag to a tag through its name. If the tag is not present, then the tag will be skipped when loading.
`replace`        | When `true`, all previously loaded entries added to this tag from other datapacks will be discarded. If a datapack is loaded after this one, then it will still append the entries to the tag.
`remove`         | Removes an object or tag from a tag through its name or key.

```java
// In some TagProvider#addTags
this.tag(EXAMPLE_TAG)
    .add(EXAMPLE_OBJECT) // Adds an object to the tag
    .addOptional(new ResourceLocation("othermod", "other_object")) // Adds an object from another mod to the tag

this.tag(EXAMPLE_TAG_2)
    .addTag(EXAMPLE_TAG) // Adds a tag to the tag
    .remove(EXAMPLE_OBJECT) // Removes an object from this tag
```

:::important
If the mod's tags softly depends on another mod's tags (the other mod may or may not be present at runtime), the other mods' tags should be referenced using the optional methods.
:::

### Existing Providers

Minecraft contains a few tag providers for certain registries that can be subclassed instead. Additionally, some providers contain additional helper methods to more easily create tags.

Registry Object Type         | Tag Provider
:---:                        | :---
`Block`                      | `BlockTagsProvider`\*
`Item`                       | `ItemTagsProvider`
`EntityType`                 | `EntityTypeTagsProvider`
`Fluid`                      | `FluidTagsProvider`
`GameEvent`                  | `GameEventTagsProvider`
`Biome`                      | `BiomeTagsProvider`
`FlatLevelGeneratorPreset`   | `FlatLevelGeneratorPresetTagsProvider`
`WorldPreset`                | `WorldPresetTagsProvider`
`Structure`                  | `StructureTagsProvider`
`PoiType`                    | `PoiTypeTagsProvider`
`BannerPattern`              | `BannerPatternTagsProvider`
`CatVariant`                 | `CatVariantTagsProvider`
`PaintingVariant`            | `PaintingVariantTagsProvider`
`Instrument`                 | `InstrumentTagsProvider`
`DamageType`                 | `DamageTypeTagsProvider`
`Enchantment`                | `EnchantmentTagsProvider`\*

\* These providers are added by NeoForge.

#### `ItemTagsProvider#copy`

Blocks have item representations to obtain them in the inventory. As such, many of the block tags can also be an item tag. To easily generate item tags to have the same entries as block tags, the `#copy` method can be used which takes in the block tag to copy from and the item tag to copy to.

```java
//In ItemTagsProvider#addTags
this.copy(EXAMPLE_BLOCK_TAG, EXAMPLE_ITEM_TAG);
```

## Custom Tag Providers

A custom tag provider can be created via a `TagsProvider` subclass which takes in the registry key to generate tags for.

```java
public RecipeTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
    super(output, Registries.RECIPE_TYPE, registries, MOD_ID, fileHelper);
}
```

### Intrinsic Holder Tags Providers

One special type of `TagProvider`s are `IntrinsicHolderTagsProvider`s. When creating a tag using this provider via `#tag`, the object itself can be used to add itself to the tag via `#add`. To do so, a function is provided within the constructor to turn an object into its `ResourceKey`.

```java
// Subtype of `IntrinsicHolderTagsProvider`
public AttributeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
    super(
        output,
        Registries.ATTRIBUTE,
        registries,
        attribute -> BuiltInRegistries.ATTRIBUTE.getResourceKey(attribute).orElseThrow(),
        MOD_ID,
        fileHelper
    );
}
```

[tags]: ../resources/server/tags.md
[datagen]: ../resources/index.md#data-generation
[custom]: ../concepts/registries.md#custom-registries
