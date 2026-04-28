import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Biome Modifiers

Biome Modifiers are a data-driven system that allows for changing many aspects of a biome, including the ability to inject or remove PlacedFeatures, add or remove mob spawns, change the climate, and adjust foliage and water color. NeoForge provides several default biome modifiers that cover the majority of use cases for both players and modders.

### Recommended Section To Read:

- Players or pack developers:
    - [Applying Biome Modifiers](#applying-biome-modifiers)
    - [Built-in Neoforge Biome Modifiers](#built-in-biome-modifiers)


- Modders doing simple additions or removal biome modifications:
    - [Applying Biome Modifiers](#applying-biome-modifiers)
    - [Built-in Neoforge Biome Modifiers](#built-in-biome-modifiers)
    - [Datagenning Biome Modifiers](#datagenning-biome-modifiers)


- Modders who want to do custom or complex biome modifications:
    - [Applying Biome Modifiers](#applying-biome-modifiers)
    - [Creating Custom Biome Modifiers](#creating-custom-biome-modifiers)
    - [Datagenning Biome Modifiers](#datagenning-biome-modifiers)


## Applying Biome Modifiers

To have NeoForge load a biome modifier JSON file into the game, the file will need to be under `data/<modid>/neoforge/biome_modifier/<path>.json` folder in the mod's resources, or in a [Datapack][datapacks]. Then, once NeoForge loads the biome modifier, it will read its instructions and apply the described modifications to all target biomes when the world is loaded up. Pre-existing biome modifiers from mods can be overridden by datapacks having a new JSON file at the exact same location and name.

The JSON file can be created by hand following the examples in the '[Built-in NeoForge Biome Modifiers](#built-in-biome-modifiers)' section or be datagenned as shown in the '[Datagenning Biome Modifiers](#datagenning-biome-modifiers)' section.

## Built-in Biome Modifiers

These biome modifiers are registered by NeoForge for anyone to use.

### None

This biome modifier has no operation and will do no modification. Pack makers and players can use this in a datapack to disable mods' biome modifiers by overriding their biome modifier JSONs with the JSON below.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:none"
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> NO_OP_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "no_op_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Register the biome modifiers.
    bootstrap.register(NO_OP_EXAMPLE, NoneBiomeModifier.INSTANCE);
});
```

</TabItem>
</Tabs>

### Add Features

This biome modifier type adds `PlacedFeature`s (such as trees or ores) to biomes so that they can spawn during world generation. The modifier takes in the biome id or tag of the biomes the features are added to, a `PlacedFeature` id or tag to add to the selected biomes, and the [`GenerationStep.Decoration`](#Available-Values-for-Decoration-Steps) the features will be generated within.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:add_features",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "#namespace:your_biome_tag",
    // Can either be a placed feature id, such as "examplemod:add_features_example",
    // or a list of placed feature ids, such as ["examplemod:add_features_example", minecraft:ice_spike", ...],
    // or a placed feature tag, such as "#examplemod:placed_feature_tag".
    "features": "namespace:your_feature",
    // See the GenerationStep.Decoration enum in code for a list of valid enum names.
    // The decoration step section further down also has the list of values for reference.
    "step": "underground_ores"
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Assume we have some PlacedFeature named EXAMPLE_PLACED_FEATURE.
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> ADD_FEATURES_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "add_features_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<PlacedFeature> placedFeatures = bootstrap.lookup(Registries.PLACED_FEATURE);

    // Register the biome modifiers.
    bootstrap.register(ADD_FEATURES_EXAMPLE,
        new AddFeaturesBiomeModifier(
            // The biome(s) to generate within
            HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS)),
            // The feature(s) to generate within the biomes
            HolderSet.direct(placedFeatures.getOrThrow(EXAMPLE_PLACED_FEATURE)),
            // The generation step
            GenerationStep.Decoration.LOCAL_MODIFICATIONS
        )
    );
})
```

</TabItem>
</Tabs>


:::warning
Care should be taken when adding vanilla `PlacedFeature`s to biomes, as doing so may cause what is known as a feature cycle violation (two biomes having the same two features in their feature lists, but in different orders within the same `GenerationStep`), leading to a crash. For similar reasons, you should not use the same `PlacedFeature` in more than one biome modifier.

Vanilla `PlacedFeature`s can be referenced in biome JSONs or added via biome modifiers, but should not be used in both. If you still need to add them this way, making a copy of the vanilla `PlacedFeature` under your own namespace is the easiest solution to avoid these problems.
:::

### Remove Features

This biome modifier type removes features (such as trees or ores) from biomes so that they will no longer spawn during world generation. The modifier takes in the biome id or tag of the biomes the features are removed from, a `PlacedFeature` id or tag to remove from the selected biomes, and the [`GenerationStep.Decoration`](#Available-Values-for-Decoration-Steps)s that the features will be removed from.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:remove_features",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "#namespace:your_biome_tag",
    // Can either be a placed feature id, such as "examplemod:add_features_example",
    // or a list of placed feature ids, such as ["examplemod:add_features_example", "minecraft:ice_spike", ...],
    // or a placed feature tag, such as "#examplemod:placed_feature_tag".
    "features": "namespace:problematic_feature",
    // Optional field specifying a GenerationStep, or a list of GenerationSteps, to remove features from.
    // If omitted, defaults to all GenerationSteps.
    // See the GenerationStep.Decoration enum in code for a list of valid enum names.
    // The decoration step section further down also has the list of values for reference.
    "steps": ["underground_ores", "underground_decoration"]
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> REMOVE_FEATURES_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "remove_features_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<PlacedFeature> placedFeatures = bootstrap.lookup(Registries.PLACED_FEATURE);

    // Register the biome modifiers.
    bootstrap.register(REMOVE_FEATURES_EXAMPLE,
        new RemoveFeaturesBiomeModifier(
            // The biome(s) to remove from
            biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
            // The feature(s) to remove from the biomes
            HolderSet.direct(placedFeatures.getOrThrow(OrePlacements.ORE_DIAMOND)),
            // The generation steps to remove from
            Set.of(
                GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                GenerationStep.Decoration.UNDERGROUND_ORES
            )
        )
    );
});
```

</TabItem>
</Tabs>


### Add Spawns 

This biome modifier type adds entity spawns to biomes. The modifier takes in the biome id or tag of the biomes the entity spawns are added to, and the `SpawnerData` of the entities to add. Each `SpawnerData` contains the entity id, the spawn weight, and the minimum/maximum number of entities to spawn at a given time.

:::note
If you are a modder adding a new entity, make sure the entity has a spawn restriction registered to `RegisterSpawnPlacementsEvent`. Spawn restrictions are used to make entities spawn on surfaces or in water safely. If you do not register a spawn restriction, your entity could spawn in mid-air, fall and die.
:::

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:add_spawns",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "#namespace:biome_tag",
    // Can be either a single object or a list of objects.
    "spawners": [
        {
            "type": "namespace:entity_type", // The id of the entity type to spawn
            "weight": 100, // int, spawn weight
            "minCount": 1, // int, minimum group size
            "maxCount": 4 // int, maximum group size
        },
        {
            "type": "minecraft:ghast",
            "weight": 1,
            "minCount": 5,
            "maxCount": 10
        }
    ]
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Assume we have some EntityType<?> named EXAMPLE_ENTITY.
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> ADD_SPAWNS_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "add_spawns_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);

    // Register the biome modifiers.
    bootstrap.register(ADD_SPAWNS_EXAMPLE,
        new AddSpawnsBiomeModifier(
            // The biome(s) to spawn the mobs within
            HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS)),
            // The spawners of the entities to add
            List.of(
                new SpawnerData(EXAMPLE_ENTITY, 100, 1, 4),
                new SpawnerData(EntityType.GHAST, 1, 5, 10)
            )
        )
    );
});
```

</TabItem>
</Tabs>


### Remove Spawns

This biome modifier type removes entity spawns from biomes. The modifier takes in the biome id or tag of the biomes the entity spawns are removed from, and the `EntityType` id or tag of the entities to remove.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:remove_spawns",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "#namespace:biome_tag",
    // Can either be an entity type id, such as "minecraft:ghast",
    // or a list of entity type ids, such as ["minecraft:ghast", "minecraft:skeleton", ...],
    // or an entity type tag, such as "#minecraft:skeletons".
    "entity_types": "#namespace:entitytype_tag"
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> REMOVE_SPAWNS_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "remove_spawns_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<EntityType<?>> entities = bootstrap.lookup(Registries.ENTITY_TYPE);

    // Register the biome modifiers.
    bootstrap.register(REMOVE_SPAWNS_EXAMPLE,
        new RemoveSpawnsBiomeModifier(
            // The biome(s) to remove the spawns from
            biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
            // The entities to remove spawns for
            entities.getOrThrow(EntityTypeTags.SKELETONS)
        )
    );
});
```

</TabItem>
</Tabs>


### Add Spawn Costs

Allows for adding new spawn costs to biomes. Spawn costs are a newer way of making mobs spawn spread out in a biome to reduce clustering. It works by having the entities give off a `charge` that surrounds them and adds up with other entities' `charge`. When spawning a new entity, the spawning algorithm looks for a spot where the total `charge` field at the location multiplied by the spawning entity's `charge` value is less than the spawning entity's `energy_budget`. This is an advanced way of spawning mobs, so it is a good idea to reference the Soul Sand Valley biome (which is the most prominent user of this system) for existing values to borrow.

The modifier takes in the biome id or tag of the biomes the spawn costs are added to, the `EntityType` id or tag of the entity types to add spawn costs for, and the `MobSpawnSettings.MobSpawnCost` of the entity. The `MobSpawnCost` contains the energy budget, which indicates the maximum number of entities that can spawn in a location based on the charge provided for each entity spawned.

:::note
If you are a modder adding a new entity, make sure the entity has a spawn restriction registered to `RegisterSpawnPlacementsEvent`.
:::

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:add_spawn_costs",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "#namespace:biome_tag",
    // Can either be an entity type id, such as "minecraft:ghast",
    // or a list of entity type ids, such as ["minecraft:ghast", "minecraft:skeleton", ...],
    // or an entity type tag, such as "#minecraft:skeletons".
    "entity_types": "#minecraft:skeletons",
    "spawn_cost": {
        // The energy budget
        "energy_budget": 1.0,
        // The amount of charge each entity takes up from the budget
        "charge": 0.1
    }
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> ADD_SPAWN_COSTS_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "add_spawn_costs_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<EntityType<?>> entities = bootstrap.lookup(Registries.ENTITY_TYPE);

    // Register the biome modifiers.
    bootstrap.register(ADD_SPAWN_COSTS_EXAMPLE,
        new AddSpawnCostsBiomeModifier(
            // The biome(s) to add the spawn costs to
            biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
            // The entities to add the spawn costs for
            entities.getOrThrow(EntityTypeTags.SKELETONS),
            new MobSpawnSettings.MobSpawnCost(
                1.0, // The energy budget
                0.1  // The amount of charge each entity takes up from the budget
            )
        )
    );
});
```

</TabItem>
</Tabs>


### Remove Spawn Costs

Allows for removing a spawn cost from a biome. Spawn costs are a newer way of making mobs spawn spread out in a biome to reduce clustering. The modifier takes in the biome id or tag of the biomes the spawn costs are removed from, and the `EntityType` id or tag of the entities to remove the spawn cost for.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:remove_spawn_costs",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "#namespace:biome_tag",
    // Can either be an entity type id, such as "minecraft:ghast",
    // or a list of entity type ids, such as ["minecraft:ghast", "minecraft:skeleton", ...],
    // or an entity type tag, such as "#minecraft:skeletons".
    "entity_types": "#minecraft:skeletons"
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> REMOVE_SPAWN_COSTS_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "remove_spawn_costs_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<EntityType<?>> entities = bootstrap.lookup(Registries.ENTITY_TYPE);

    // Register the biome modifiers.
    bootstrap.register(REMOVE_SPAWN_COSTS_EXAMPLE,
        new RemoveSpawnCostsBiomeModifier(
            // The biome(s) to remove the spawn costs from
            biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
            // The entities to remove spawn costs for
            entities.getOrThrow(EntityTypeTags.SKELETONS)
        )
    );
});
```

</TabItem>
</Tabs>


### Add Legacy Carvers

This biome modifier type allows adding carver caves and ravines to biomes. These are what was used for cave generation before the Caves and Cliffs update. It CANNOT add noise caves to biomes, because noise caves are a part of certain noise-based chunk generator systems and not actually tied to biomes.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
    {
    "type": "neoforge:add_carvers",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "minecraft:plains",
    // Can either be a carver id, such as "examplemod:add_carvers_example",
    // or a list of carver ids, such as ["examplemod:add_carvers_example", "minecraft:canyon", ...],
    // or a carver tag, such as "#examplemod:configured_carver_tag".
    "carvers": "examplemod:add_carvers_example",
    // See GenerationStep.Carving in code for a list of valid enum names.
    // Only "air" and "liquid" are available.
    "step": "air"
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Assume we have some ConfiguredWorldCarver named EXAMPLE_CARVER.
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> ADD_CARVERS_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "add_carvers_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<ConfiguredWorldCarver<?>> carvers = bootstrap.lookup(Registries.CONFIGURED_CARVER);

    // Register the biome modifiers.
    bootstrap.register(ADD_CARVERS_EXAMPLE,
        new AddCarversBiomeModifier(
            // The biome(s) to generate within
            HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS)),
            // The carver(s) to generate within the biomes
            HolderSet.direct(carvers.getOrThrow(EXAMPLE_CARVER)),
            // The generation step
            GenerationStep.Carving.AIR
        )
    );
});
```

</TabItem>
</Tabs>

### Removing Legacy Carvers

This biome modifier type allows removing carver caves and ravines from biomes. These are what was used for cave generation before the Caves and Cliffs update. It CANNOT remove noise caves from biomes, because noise caves are baked into the dimension's noise settings system and not actually tied to biomes.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
{
    "type": "neoforge:remove_carvers",
    // Can either be a biome id, such as "minecraft:plains",
    // or a list of biome ids, such as ["minecraft:plains", "minecraft:badlands", ...],
    // or a biome tag, such as "#c:is_overworld".
    "biomes": "minecraft:plains",
    // Can either be a carver id, such as "examplemod:add_carvers_example",
    // or a list of carver ids, such as ["examplemod:add_carvers_example", "minecraft:canyon", ...],
    // or a carver tag, such as "#examplemod:configured_carver_tag".
    "carvers": "examplemod:add_carvers_example",
    // Can either be a single generation step, such as "air",
    // or a list of generation steps, such as ["air", "liquid"].
    // See GenerationStep.Carving for a list of valid enum names.
    // Only "air" and "liquid" are available.
    "steps": [
        "air",
        "liquid"
    ]
}
```

</TabItem>
<TabItem value="datagen" label="Datagen">

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> REMOVE_CARVERS_EXAMPLE = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "remove_carvers_example") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
    HolderGetter<ConfiguredWorldCarver<?>> carvers = bootstrap.lookup(Registries.CONFIGURED_CARVER);

    // Register the biome modifiers.
    bootstrap.register(REMOVE_CARVERS_EXAMPLE,
        new AddFeaturesBiomeModifier(
            // The biome(s) to remove from
            biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
            // The carver(s) to remove from the biomes
            HolderSet.direct(carvers.getOrThrow(Carvers.CAVE)),
            // The generation steps to remove from
            Set.of(
                GenerationStep.Carving.AIR,
                GenerationStep.Carving.LIQUID
            )
        )
    );
});
```

</TabItem>
</Tabs>

### Available Values for Decoration Steps

The `step` or `steps` fields in many of the aforementioned JSONs are referring to the `GenerationStep.Decoration` enum. This enum has the steps listed out in the following order, which is the same order that the game uses for generating during worldgen. Try to put features in the step that makes the most sense for them.

|           Step           | Description                                                                             |
|:------------------------:|:----------------------------------------------------------------------------------------|
|     `raw_generation`     | First to run. This is used for special terrain-like features such as Small End Islands. |
|         `lakes`          | Dedicated to spawning pond-like feature such as Lava Lakes.                             |
|  `local_modifications`   | For modifications to terrain such as Geodes, Icebergs, Boulders, or Dripstone.          |
| `underground_structures` | Used for small underground structure-like features such as Dungeons or Fossils.         |
|   `surface_structures`   | For small surface only structure-like features such as Desert Wells.                    |
|      `strongholds`       | Dedicated for Stronghold structures. No feature is added here in unmodified Minecraft.  |
|    `underground_ores`    | The step for all Ores and Veins to be added to. This includes Gold, Dirt, Granite, etc. |
| `underground_decoration` | Used typically for decorating caves. Dripstone Cluster and Sculk Vein are here.         |
|     `fluid_springs`      | The small Lavafalls and Waterfalls come from features in this stage.                    |
|   `vegetal_decoration`   | Nearly all plants (flowers, trees, vines, and more) are added to this stage.            |
| `top_layer_modification` | Last to run. Used for placing Snow and Ice on the surface of cold biomes.               |


## Creating Custom Biome Modifiers

### The `BiomeModifier` Implementation

Under the hood, Biome Modifiers are made up of three parts:

- The [datapack registered][datareg] `BiomeModifier` used to modify the biome builder.
- The [statically registered][staticreg] `MapCodec` that encodes and decodes the modifiers.
- The JSON that constructs the `BiomeModifier`, using the registered id of the `MapCodec` as the indexable type.

A `BiomeModifier` contains two methods: `#modify` and `#codec`. `modify` takes in a `Holder` of the current `Biome`, the current `BiomeModifier.Phase`, and the builder of the biome to modify. Every `BiomeModifier` is called once per `Phase` to organize when certain modifications to the biome should occur:

| Phase               | Description                                                              |
|:-------------------:|:-------------------------------------------------------------------------|
| `BEFORE_EVERYTHING` | A catch-all for everything that needs to run before the standard phases. |
| `ADD`               | Adding features, mob spawns, etc.                                        |
| `REMOVE`            | Removing features, mob spawns, etc.                                      |
| `MODIFY`            | Modifying single values (e.g., climate, colors).                         |
| `AFTER_EVERYTHING`  | A catch-all for everything that needs to run after the standard phases.  |

All `BiomeModifier`s contain a `type` key that references the id of the `MapCodec` used for the `BiomeModifier`. The `codec` takes in the `MapCodec` that encodes and decodes the modifiers. This `MapCodec` is [statically registered][staticreg], with its id used as the `type` of the `BiomeModifier`.

```java
public record ExampleBiomeModifier(HolderSet<Biome> biomes, int value) implements BiomeModifier {
    
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == /* Pick the phase that best matches what your want to modify */) {
            // Modify the 'builder', checking any information about the biome itself
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return EXAMPLE_BIOME_MODIFIER.get();
    }
}

// In some registration class
private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
    DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);

public static final Supplier<MapCodec<ExampleBiomeModifier>> EXAMPLE_BIOME_MODIFIER =
    BIOME_MODIFIERS.register("example_biome_modifier", () -> RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ExampleBiomeModifier::biomes),
            Codec.INT.fieldOf("value").forGetter(ExampleBiomeModifier::value)
        ).apply(instance, ExampleBiomeModifier::new)
    ));
```


## Datagenning Biome Modifiers

A `BiomeModifier` JSON can be created through [data generation][datagen] by passing a `RegistrySetBuilder` to `DatapackBuiltinEntriesProvider`. The JSON will be placed at `data/<modid>/neoforge/biome_modifier/<path>.json`.

For more information on how `RegistrySetBuilder` and `DatapackBuiltinEntriesProvider` work, please see the article on [Data Generation for Datapack Registries][datapackdatagen].

```java
// Define the ResourceKey for our BiomeModifier.
public static final ResourceKey<BiomeModifier> EXAMPLE_MODIFIER = ResourceKey.create(
    NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
    ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_modifier") // The registry name
);

// BUILDER is a RegistrySetBuilder passed to DatapackBuiltinEntriesProvider
// in a listener for GatherDataEvent.
BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
    // Lookup any necessary registries.
    // Static registries only need to be looked up if you need to grab the tag data.
    HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);

    // Register the biome modifiers.
    bootstrap.register(EXAMPLE_MODIFIER,
        new ExampleBiomeModifier(
            biomes.getOrThrow(Tags.Biomes.IS_OVERWORLD),
            20
        )
    );
});
```

This will then result in the following JSON being created:

```json5
// In data/examplemod/neoforge/biome_modifier/example_modifier.json
{
    // The registy key of the MapCodec for the modifier
    "type": "examplemod:example_biome_modifier",
    // All additional settings are applied to the root object
    "biomes": "#c:is_overworld",
    "value": 20
}
```

[datareg]: ../concepts/registries.md#datapack-registries
[staticreg]: ../concepts/registries.md#methods-for-registering
[datapacks]: ../resources/index.md#data
[datagen]: ../resources/index.md#data-generation
[datapackdatagen]: ../concepts/registries#data-generation-for-datapack-registries
