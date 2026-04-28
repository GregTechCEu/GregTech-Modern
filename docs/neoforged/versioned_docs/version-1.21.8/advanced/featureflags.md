# Feature Flags

Feature flags are a system that allows developers to gate a set of features behind some set of required flags, that being registered elements, gameplay mechanics, data pack entries or some other unique system to your mod.

A common use case would be gating experimental features/elements behind a experimental flag, allowing users to easily switch them on and play around with them before they are finalized.

:::tip
You are not forced to add your own flags. If you find a vanilla flag which would fit your use case, feel free to flag your blocks/items/entities/etc. with said flag.

For example in `1.21.3` if you were to add to the set of Pale Oak wood blocks, you'd only want those to show up if the `WINTER_DROP` flag is enabled.
:::

## Creating a Feature Flag

To create new Feature flags, a JSON file needs to be created and referenced in your `neoforge.mods.toml` file with the `featureFlags` entry inside of your `[[mods]]` block. The specified path must be relative to the `resources` directory:

```toml
# In neoforge.mods.toml:
[[mods]]
    # The file is relative to the output directory of the resources, or the root path inside the jar when compiled
    # The 'resources' directory represents the root output directory of the resources
    featureFlags="META-INF/feature_flags.json"
```

The definition of the entry consists of a list of Feature flag names, which will be loaded and registered during game initialization.

```json5
{
    "flags": [
        // Identifier of a Feature flag to be registered
        "examplemod:experimental"
    ]
}
```

## Retrieving the Feature Flag

The registered Feature flag can be retrieved via `FeatureFlagRegistry.getFlag(ResourceLocation)`. This can be done at any time during your mod's initialization and is recommended to be stored somewhere for future use, rather than looking up the registry each time you require your flag.

```java
// Look up the 'examplemod:experimental' Feature flag
public static final FeatureFlag EXPERIMENTAL = FeatureFlags.REGISTRY.getFlag(ResourceLocation.fromNamespaceAndPath("examplemod", "experimental"));
```

## Feature Elements

`FeatureElement`s are registry values which can be given a set of required flags. These values are only made available to players when the respective required flags match the flags enabled in the level.

When a feature element is disabled, it is fully hidden from the player's view, and all interactions will be skipped. Do note that these disabled elements will still exist in the registry and are merely functionally unusable.

The following is a complete list of all registries which directly implement the `FeatureElement` system:

- Item
- Block
- EntityType
- MenuType
- Potion
- MobEffect

### Flagging Elements

In order to flag a given `FeatureElement` as requiring your Feature flag, you simply pass it and any other desired flags into the respective registration method:

- `Item`: `Item.Properties#requiredFeatures`
- `Block`: `BlockBehaviour.Properties#requiredFeatures`
- `EntityType`: `EntityType.Builder#requiredFeatures`
- `MenuType`: `MenuType#new`
- `Potion`: `Potion#requiredFeatures`
- `MobEffect`: `MobEffect#requiredFeatures`

```java
// These elements will only become available once the 'EXPERIMENTAL' flag is enabled

// Item
DeferredRegister.Items ITEMS = DeferredRegister.createItems("examplemod");
DeferredItem<Item> EXPERIMENTAL_ITEM = ITEMS.registerSimpleItem("experimental", new Item.Properties()
    .requiredFeatures(EXPERIMENTAL) // mark as requiring the 'EXPERIMENTAL' flag
);

// Block
DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("examplemod");
// Do note that BlockBehaviour.Properties#ofFullCopy and BlockBehaviour.Properties#ofLegacyCopy will copy over the required features.
// This means that in 1.21.3, using BlockBehaviour.Properties.ofFullCopy(Blocks.PALE_OAK_WOOD) would have your block require the 'WINTER_DROP' flag.
DeferredBlock<Block> EXPERIMENTAL_BLOCK = BLOCKS.registerSimpleBlock("experimental", BlockBehaviour.Properties.of()
    .requiredFeatures(EXPERIMENTAL) // mark as requiring the 'EXPERIMENTAL' flag
);

// BlockItems are special in that the required features are inherited from their respective Blocks.
// The same is also true for spawn eggs and their respective EntityTypes.
DeferredItem<BlockItem> EXPERIMENTAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(EXPERIMENTAL_BLOCK);

// EntityType
DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "examplemod");
DeferredHolder<EntityType<?>, EntityType<ExperimentalEntity>> EXPERIMENTAL_ENTITY = ENTITY_TYPES.register("experimental", registryName -> EntityType.Builder.of(ExperimentalEntity::new, MobCategory.AMBIENT)
    .requiredFeatures(EXPERIMENTAL) // mark as requiring the 'EXPERIMENTAL' flag
    .build(ResourceKey.create(Registries.ENTITY_TYPE, registryName))
);

// MenuType
DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, "examplemod");
DeferredHolder<MenuType<?>, MenuType<ExperimentalMenu>> EXPERIMENTAL_MENU = MENU_TYPES.register("experimental", () -> new MenuType<>(
    // Using vanilla's MenuSupplier:
    // This is used when your menu is not encoding complex data during `player.openMenu`. Example:
    // (windowId, inventory) -> new ExperimentalMenu(windowId, inventory),

    // Using NeoForge's IContainerFactory:
    // This is used when you wish to read complex data encoded during `player.openMenu`.
    // Casting is important here, as `MenuType` specifically expects a `MenuSupplier`.
    (IContainerFactory<ExperimentalMenu>) (windowId, inventory, buffer) -> new ExperimentalMenu(windowId, inventory, buffer),
    
    FeatureFlagSet.of(EXPERIMENTAL) // mark as requiring the 'EXPERIMENTAL' flag
));

// MobEffect
DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "examplemod");
DeferredHolder<MobEffect, ExperimentalMobEffect> EXPERIMENTAL_MOB_EEFECT = MOB_EFFECTS.register("experimental", registryName -> new ExperimentalMobEffect(MobEffectCategory.NEUTRAL, CommonColors.WHITE)
    .requiredFeatures(EXPERIMENTAL) // mark as requiring the 'EXPERIMENTAL' flag
);

// Potion
DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, "examplemod");
DeferredHolder<Potion, ExperimentalPotion> EXPERIMENTAL_POTION = POTIONS.register("experimental", registryName -> new ExperimentalPotion(registryName.toString(), new MobEffectInstance(EXPERIMENTAL_MOB_EEFECT))
    .requiredFeatures(EXPERIMENTAL) // mark as requiring the 'EXPERIMENTAL' flag
);
```

### Validating Enabled Status

In order to validate if features should be enabled or not, you must first acquire the set of enabled features. This can be done in a variety of ways, but the common and recommended method is `LevelReader#enabledFeatures`.  

```java
level.enabledFeatures(); // from a 'LevelReader' instance
entity.level().enabledFeatures(); // from a 'Entity' instance

// Client Side
minecraft.getConnection().enabledFeatures();

// Server Side
server.getWorldData().enabledFeatures();
```

To validate if any `FeatureFlagSet` is enabled, you can pass the enabled features to `FeatureFlagSet#isSubsetOf`, and for validating if a specific `FeatureElement` is enabled, you can invoke `FeatureElement#isEnabled`.

:::note
`ItemStack` has a special `isItemEnabled(FeatureFlagSet)` method. This is so that empty stacks are treated as enabled even if the required features for the backing `Item` do not match the enabled features. It is recommended to prefer this method over `Item#isEnabled` where possible.
:::

```java
requiredFeatures.isSubsetOf(enabledFeatures);
featureElement.isEnabled(enabledFeatures);
itemStack.isItemEnabled(enabledFeatures);
```

## Feature Packs

_See also: [Resource Packs](../resources/index.md#assets), [Data Packs](../resources/index.md#data) and [Pack.mcmeta](../resources/index.md#packmcmeta)_

Feature packs are a type of pack that not only loads resources and/or data, but also has the ability to toggle on a given set of feature flags. These flags are defined in the `pack.mcmeta` JSON file at the root of this pack, which follows the below format:

:::note
This file differs from the one in your mod's `resources/` directory. This file defines a brand new feature pack and thus must be in its own folder.
:::

```json5
{
    "features": {
        "enabled": [
            // Identifier of a Feature flag to be enabled
            // Must be a valid registered flag
            "examplemod:experimental"
        ]
    },
    "pack": { /*...*/ }
}
```

There are a couple of ways for users to obtain a feature pack, namely installing them from an external source as a datapack, or downloading a mod that has a built-in feature pack. Both of these then need to be installed differently depending on the [physical side](../concepts/sides.md).

### Built-In

Built-in packs are bundled with your mod and are made available to the game using the `AddPackFindersEvent` event.

```java
@SubscribeEvent // on the mod event bus
public static void addFeaturePacks(final AddPackFindersEvent event) {
    event.addPackFinders(
            // Path relative to your mods 'resources' pointing towards this pack
            // Take note this also defines your packs id using the following format
            // mod/<namespace>:<path>`, e.g. `mod/examplemod:data/examplemod/datapacks/experimental`
            ResourceLocation.fromNamespaceAndPath("examplemod", "data/examplemod/datapacks/experimental"),
            
            // What kind of resources are contained within this pack
            // 'CLIENT_RESOURCES' for packs with client assets (resource packs)
            // 'SERVER_DATA' for packs with server data (data packs)
            PackType.SERVER_DATA,
            
            // Display name shown in the Experiments screen
            Component.literal("ExampleMod: Experiments"),
            
            // In order for this pack to load and enable feature flags, this MUST be 'FEATURE',
            // any other PackSource type is invalid here
            PackSource.FEATURE,
            
            // If this is true, the pack is always active and cannot be disabled, should always be false for feature packs
            false,
            
            // Priority to load resources from this pack in
            // 'TOP' this pack will be prioritized over other packs
            // 'BOTTOM' other packs will be prioritized over this pack 
            Pack.Position.TOP
    );
}
```

#### Enabling in Singleplayer

1. Create a new world.
2. Navigate to the Experiments screen.
3. Toggle on the desired packs.
4. Confirm changes by clicking `Done`.

#### Enabling in Multiplayer

1. Open your server's `server.properties` file.
2. Add the feature pack id to `initial-enabled-packs`, separating each pack by a `,`. The pack id is defined during registering your pack finder, as seen above.

### External

External packs are provided to your users in datapack form.

#### Installation in Singleplayer

1. Create a new world.
2. Navigate to the datapack selection screen.
3. Drag and drop the datapack zip file onto the game window.
4. Move the newly available datapack over to the `Selected` packs list.
5. Confirm changes by clicking `Done`.

The game will now warn you about any newly selected experimental features, potential bugs, issues and crashes. You can confirm these changes by clicking `Proceed` or `Details` to see an extensive list of all selected packs and which features they would enable.

:::note
External feature packs do not show up in the Experiments screen. The Experiments screen will only show built-in feature packs.

To disable external feature packs after enabling them, navigate back into the datapacks screen and move the external packs back into `Available` from `Selected`.
:::

#### Installation in Multiplayer

Enabling Feature Packs can only be done during initial world creation, and they cannot be disabled once enabled.

1. Create the directory `./world/datapacks`
2. Upload the datapack zip file into the newly created directory
3. Open your server's `server.properties` file
4. Add the datapack zip file name (excluding `.zip`) to `initial-enabled-packs` (separating each pack by a `,`)
   - Example: The zip `examplemod-experimental.zip` would be added like so `initial-enabled-packs=vanilla,examplemod-experimental`

### Data Generation

_See also: [Datagen](../resources/index.md#data-generation)_

Feature packs can be generated during regular mod datagen. This is best used in combination with built-in packs, but it is also possible to zip up the generated result and share it as an external pack. Just choose one, i.e. don't provide it as an external pack and also bundle it as a built-in pack.

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(final GatherDataEvent.Client event) {
    DataGenerator generator = event.getGenerator();
    
    // To generate a feature pack, you must first obtain a pack generator instance for the desired pack.
    // generator.getBuiltinDatapack(<shouldGenerate>, <namespace>, <path>);
    // This will generate the feature pack into the following path:
    // ./data/<namespace>/datapacks/<path>
    PackGenerator featurePack = generator.getBuiltinDatapack(true, "examplemod", "experimental");
        
    // Register a provider to generate the `pack.mcmeta` file.
    featurePack.addProvider(output -> PackMetadataGenerator.forFeaturePack(
            output,
            
            // Description displayed in the Experiments screen
            Component.literal("Enabled experimental features for ExampleMod"),
            
            // Set of Feature flags this pack should enable
            FeatureFlagSet.of(EXPERIMENTAL)
    ));
    
    // Register additional providers (recipes, loot tables) to `featurePack` to write any generated resources into this pack, rather than the root pack.
}
```
