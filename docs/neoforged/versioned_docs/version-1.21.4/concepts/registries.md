---
sidebar_position: 1
---
# Registries

Registration is the process of taking the objects of a mod (such as [items][item], [blocks][block], entities, etc.) and making them known to the game. Registering things is important, as without registration the game will simply not know about these objects, which will cause unexplainable behaviors and crashes.

A registry is, simply put, a wrapper around a map that maps registry names (read on) to registered objects, often called registry entries. Registry names must be unique within the same registry, but the same registry name may be present in multiple registries. The most common example for this are blocks (in the `BLOCKS` registry) that have an item form with the same registry name (in the `ITEMS` registry).

Every registered object has a unique name, called its registry name. The name is represented as a [`ResourceLocation`][resloc]. For example, the registry name of the dirt block is `minecraft:dirt`, and the registry name of the zombie is `minecraft:zombie`. Modded objects will of course not use the `minecraft` namespace; their mod id will be used instead.

## Vanilla vs. Modded

To understand some of the design decisions that were made in NeoForge's registry system, we will first look at how Minecraft does this. We will use the block registry as an example, as most other registries work the same way.

Registries generally register [singletons][singleton]. This means that all registry entries exist exactly once. For example, all stone blocks you see throughout the game are actually the same stone block, displayed many times. If you need the stone block, you can get it by referencing the registered block instance.

Minecraft registers all blocks in the `Blocks` class. Through the `register` method, `Registry#register()` is called, with the block registry at `BuiltInRegistries.BLOCK` being the first parameter. After all blocks are registered, Minecraft performs various checks based on the list of blocks, for example the self check that verifies that all blocks have a model loaded.

The main reason all of this works is that `Blocks` is classloaded early enough by Minecraft. Mods are not automatically classloaded by Minecraft, and thus workarounds are needed.

## Methods for Registering

NeoForge offers two ways to register objects: the `DeferredRegister` class, and the `RegisterEvent`. Note that the former is a wrapper around the latter, and is recommended in order to prevent mistakes.

### `DeferredRegister`

We begin by creating our `DeferredRegister`:

```java
public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        // The registry we want to use.
        // Minecraft's registries can be found in BuiltInRegistries, NeoForge's registries can be found in NeoForgeRegistries.
        // Mods may also add their own registries, refer to the individual mod's documentation or source code for where to find them.
        BuiltInRegistries.BLOCK,
        // Our mod id.
        ExampleMod.MOD_ID
);
```

We can then add our registry entries as static final fields using one of the following methods (see [the article on Blocks][block] for what parameters to add in `new Block()`):

```java
public static final DeferredHolder<Block, Block> EXAMPLE_BLOCK_1 = BLOCKS.register(
        // Our registry name.
        "example_block",
        // A supplier of the object we want to register.
        () -> new Block(...)
);

public static final DeferredHolder<Block, SlabBlock> EXAMPLE_BLOCK_2 = BLOCKS.register(
        // Our registry name.
        "example_block",
        // A function creating the object we want to register
        // given its registry name as a ResourceLocation.
        registryName -> new SlabBlock(...)
);
```

The class `DeferredHolder<R, T extends R>` holds our object. The type parameter `R` is the type of the registry we are registering to (in our case `Block`). The type parameter `T` is the type of our supplier. Since we directly register a `Block` in the first example, we provide `Block` as the second parameter. If we were to register an object of a subclass of `Block`, for example `SlabBlock` (as shown in the second example), we would provide `SlabBlock` here instead.

`DeferredHolder<R, T extends R>` is a subclass of `Supplier<T>`. To get our registered object when we need it, we can call `DeferredHolder#get()`. The fact that `DeferredHolder` extends `Supplier` also allows us to use `Supplier` as the type of our field. That way, the above code block becomes the following:

```java
public static final Supplier<Block> EXAMPLE_BLOCK_1 = BLOCKS.register(
        // Our registry name.
        "example_block",
        // A supplier of the object we want to register.
        () -> new Block(...)
);

public static final Supplier<SlabBlock> EXAMPLE_BLOCK_2 = BLOCKS.register(
        // Our registry name.
        "example_block",
        // A function creating the object we want to register
        // given its registry name as a ResourceLocation.
        registryName -> new SlabBlock(...)
);
```

:::note
Be aware that a few places explicitly require a `Holder` or `DeferredHolder` and will not just accept any `Supplier`. If you need either of those two, it is best to change the type of your `Supplier` back to `Holder` or `DeferredHolder` as necessary.
:::

Finally, since the entire system is a wrapper around registry events, we need to tell the `DeferredRegister` to attach itself to the registry events as needed:

```java
//This is our mod constructor
public ExampleMod(IEventBus modBus) {
    //highlight-next-line
    ExampleBlocksClass.BLOCKS.register(modBus);
    //Other stuff here
}
```

:::info
There are specialized variants of `DeferredRegister`s for blocks, items, and data components that provide helper methods: [`DeferredRegister.Blocks`][defregblocks], [`DeferredRegister.Items`][defregitems], and [`DeferredRegister.DataComponents`][defregcomp], respectively.
:::

### `RegisterEvent`

`RegisterEvent` is the second way to register objects. This [event][event] is fired for each registry, after the mod constructors (since those are where `DeferredRegister`s register their internal event handlers) and before the loading of configs. `RegisterEvent` is fired on the mod event bus.

```java
@SubscribeEvent // on the mod event bus
public static void register(RegisterEvent event) {
    event.register(
            // This is the registry key of the registry.
            // Get these from BuiltInRegistries for vanilla registries,
            // or from NeoForgeRegistries.Keys for NeoForge registries.
            BuiltInRegistries.BLOCK,
            // Register your objects here.
            registry -> {
                registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "example_block_1"), new Block(...));
                registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "example_block_2"), new Block(...));
                registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "example_block_3"), new Block(...));
            }
    );
}
```

## Querying Registries

Sometimes, you will find yourself in situations where you want to get a registered object by a given id. Or, you want to get the id of a certain registered object. Since registries are basically maps of ids (`ResourceLocation`s) to distinct objects, i.e. a reversible map, both of these operations work:

```java
BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt")); // returns the dirt block
BuiltInRegistries.BLOCK.getKey(Blocks.DIRT); // returns the resource location "minecraft:dirt"

// Assume that ExampleBlocksClass.EXAMPLE_BLOCK.get() is a Supplier<Block> with the id "yourmodid:example_block"
BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath("yourmodid", "example_block")); // returns the example block
BuiltInRegistries.BLOCK.getKey(ExampleBlocksClass.EXAMPLE_BLOCK.get()); // returns the resource location "yourmodid:example_block"
```

If you just want to check for the presence of an object, this is also possible, though only with keys:

```java
BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt")); // true
BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath("create", "brass_ingot")); // true only if Create is installed
```

As the last example shows, this is possible with any mod id, and thus a perfect way to check if a certain item from another mod exists.

Finally, we can also iterate over all entries in a registry, either over the keys or over the entries (entries use the Java `Map.Entry` type):

```java
for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
    // ...
}
for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
    // ...
}
```

:::note
Query operations always use vanilla `Registry`s, not `DeferredRegister`s. This is because `DeferredRegister`s are merely registration utilities.
:::

:::danger
Query operations are only safe to use after registration has finished. **DO NOT QUERY REGISTRIES WHILE REGISTRATION IS STILL ONGOING!**
:::

## Custom Registries

Custom registries allow you to specify additional systems that addon mods for your mod may want to plug into. For example, if your mod were to add spells, you could make the spells a registry and thus allow other mods to add spells to your mod, without you having to do anything else. It also allows you to do some things, such as syncing the entries, automatically.

Let's start by creating the [registry key][resourcekey] and the registry itself:

```java
// We use spells as an example for the registry here, without any details about what a spell actually is (as it doesn't matter).
// Of course, all mentions of spells can and should be replaced with whatever your registry actually is.
public static final ResourceKey<Registry<Spell>> SPELL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("yourmodid", "spells"));
public static final Registry<YourRegistryContents> SPELL_REGISTRY = new RegistryBuilder<>(SPELL_REGISTRY_KEY)
        // If you want to enable integer id syncing, for networking.
        // These should only be used in networking contexts, for example in packets or purely networking-related NBT data.
        .sync(true)
        // The default key. Similar to minecraft:air for blocks. This is optional.
        .defaultKey(ResourceLocation.fromNamespaceAndPath("yourmodid", "empty"))
        // Effectively limits the max count. Generally discouraged, but may make sense in settings such as networking.
        .maxId(256)
        // Build the registry.
        .create();
```

Then, tell the game that the registry exists by registering them to the root registry in `NewRegistryEvent`:

```java
@SubscribeEvent // on the mod event bus
public static void registerRegistries(NewRegistryEvent event) {
    event.register(SPELL_REGISTRY);
}
```

You can now register new registry contents like with any other registry, through both `DeferredRegister` and `RegisterEvent`:

```java
public static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(SPELL_REGISTRY, "yourmodid");
public static final Supplier<Spell> EXAMPLE_SPELL = SPELLS.register("example_spell", () -> new Spell(...));

// Alternatively:
@SubscribeEvent // on the mod event bus
public static void register(RegisterEvent event) {
    event.register(SPELL_REGISTRY_KEY, registry -> {
        registry.register(ResourceLocation.fromNamespaceAndPath("yourmodid", "example_spell"), () -> new Spell(...));
    });
}
```

## Datapack Registries

A datapack registry (also known as a dynamic registry or, after its main use case, worldgen registry) is a special kind of registry that loads data from [datapack][datapack] JSONs (hence the name) at world load, instead of loading them when the game starts. Default datapack registries most notably include most worldgen registries, among a few others.

Datapack registries allow their contents to be specified in JSON files. This means that no code (other than [datagen][datagen] if you don't want to write the JSON files yourself) is necessary. Every datapack registry has a [`Codec`][codec] associated with it, which is used for serialization, and each registry's id determines its datapack path:

- Minecraft's datapack registries use the format `data/yourmodid/registrypath` (for example `data/yourmodid/worldgen/biome`, where `worldgen/biome` is the registry path).
- All other datapack registries (NeoForge or modded) use the format `data/yourmodid/registrynamespace/registrypath` (for example `data/yourmodid/neoforge/biome_modifier`, where `neoforge` is the registry namespace and `biome_modifier` is the registry path).

Datapack registries can be obtained from a `RegistryAccess`. This `RegistryAccess` can be retrieved by calling `ServerLevel#registryAccess()` if on the server, or `Minecraft.getInstance().getConnection()#registryAccess()` if on the client (the latter only works if you are actually connected to a world, as otherwise the connection will be null). The result of these calls can then be used like any other registry to get specific elements, or to iterate over the contents.

### Custom Datapack Registries

Custom datapack registries do not require a `Registry` to be constructed. Instead, they just need a registry key and at least one [`Codec`][codec] to (de-)serialize its contents. Reiterating on the spells example from before, registering our spell registry as a datapack registry looks something like this:

```java
public static final ResourceKey<Registry<Spell>> SPELL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("yourmodid", "spells"));

@SubscribeEvent // on the mod event bus
public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
    event.dataPackRegistry(
            // The registry key.
            SPELL_REGISTRY_KEY,
            // The codec of the registry contents.
            Spell.CODEC,
            // The network codec of the registry contents. Often identical to the normal codec.
            // May be a reduced variant of the normal codec that omits data that is not needed on the client.
            // May be null. If null, registry entries will not be synced to the client at all.
            // May be omitted, which is functionally identical to passing null (a method overload
            // with two parameters is called that passes null to the normal three parameter method).
            Spell.CODEC,
            // A consumer which configures the constructed registry via the RegistryBuilder.
            // May be omitted, which is functionally identical to passing builder -> {}.
            builder -> builder.maxId(256)
    );
}
```

### Data Generation for Datapack Registries

Since writing all the JSON files by hand is both tedious and error-prone, NeoForge provides a [data provider][datagenindex] to generate the JSON files for you. This works for both built-in and your own datapack registries.

First, we create a `RegistrySetBuilder` and add our entries to it (one `RegistrySetBuilder` can hold entries for multiple registries):

```java
new RegistrySetBuilder()
    .add(Registries.CONFIGURED_FEATURE, bootstrap -> {
        // Register configured features through the bootstrap context (see below)
    })
    .add(Registries.PLACED_FEATURE, bootstrap -> {
        // Register placed features through the bootstrap context (see below)
    });
```

The `bootstrap` lambda parameter is what we actually use to register our objects. It has the type `BootstrapContext`. To register an object, we call `#register` on it, like so:

```java
// The resource key of our object.
public static final ResourceKey<ConfiguredFeature<?, ?>> EXAMPLE_CONFIGURED_FEATURE = ResourceKey.create(
    Registries.CONFIGURED_FEATURE,
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_configured_feature")
);

new RegistrySetBuilder()
    .add(Registries.CONFIGURED_FEATURE, bootstrap -> {
        bootstrap.register(
            // The resource key of our configured feature.
            EXAMPLE_CONFIGURED_FEATURE,
            // The actual configured feature.
            new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(...))
        );
    })
    .add(Registries.PLACED_FEATURE, bootstrap -> {
        // ...
    });
```

The `BootstrapContext` can also be used to lookup entries from another registry if needed:

```java
public static final ResourceKey<ConfiguredFeature<?, ?>> EXAMPLE_CONFIGURED_FEATURE = ResourceKey.create(
    Registries.CONFIGURED_FEATURE,
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_configured_feature")
);
public static final ResourceKey<PlacedFeature> EXAMPLE_PLACED_FEATURE = ResourceKey.create(
    Registries.PLACED_FEATURE,
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_placed_feature")
);

new RegistrySetBuilder()
    .add(Registries.CONFIGURED_FEATURE, bootstrap -> {
        bootstrap.register(EXAMPLE_CONFIGURED_FEATURE, ...);
    })
    .add(Registries.PLACED_FEATURE, bootstrap -> {
        HolderGetter<ConfiguredFeature<?, ?>> otherRegistry = bootstrap.lookup(Registries.CONFIGURED_FEATURE);
        bootstrap.register(EXAMPLE_PLACED_FEATURE, new PlacedFeature(
            otherRegistry.getOrThrow(EXAMPLE_CONFIGURED_FEATURE), // Get the configured feature
            List.of() // No-op when placement happens - replace with whatever your placement parameters are
        ));
    });
```

Finally, we use our `RegistrySetBuilder` in an actual data provider, and register that data provider to the event:

```java
@SubscribeEvent // on the mod event bus
public static void onGatherData(GatherDataEvent.Client event) {
    // Adds the generated registry objects to the current lookup provider for use
    // in other datagen.
    event.createDatapackRegistryObjects(
        // Our registry set builder to generate the data from.
        new RegistrySetBuilder().add(...),
        // (Optional) A biconsumer that takes in any conditions to load the object
        // associated with the resource key
        conditions -> {
            conditions.accept(resourceKey, condition);
        },
        // (Optional) A set of mod ids we are generating the entries for
        // By default, supplies the mod id of the current mod container.
        Set.of("yourmodid")
    );

    // You can use the lookup provider with your generated entries by either calling one
    // of the `#create*` methods or grabbing the actual lookup via `#getLookupProvider`
    // ...
}
```

[block]: ../blocks/index.md
[blockentity]: ../blockentities/index.md
[codec]: ../datastorage/codecs.md
[datagen]: #data-generation-for-datapack-registries
[datagenindex]: ../resources/index.md#data-generation
[datapack]: ../resources/index.md#data
[defregblocks]: ../blocks/index.md#deferredregisterblocks-helpers
[defregcomp]: ../items/datacomponents.md#creating-custom-data-components
[defregitems]: ../items/index.md#deferredregisteritems
[event]: events.md
[item]: ../items/index.md
[resloc]: ../misc/resourcelocation.md
[resourcekey]: ../misc/resourcelocation.md#resourcekeys
[singleton]: https://en.wikipedia.org/wiki/Singleton_pattern
