# Damage Types & Damage Sources

A damage type denotes what kind of damage is being applied to an entity - physical damage, fire damage, drowning damage, magic damage, void damage, etc. The distinction into damage types is used for various immunities (e.g. blazes won't take fire damage), enchantments (e.g. blast protection will only protect against explosion damage), and many more use cases.

A damage type is a template for a damage source, so to speak. Or in other words, a damage source can be viewed as a damage type instance. Damage types exist as [`ResourceKey`s][rk] in code, but have all of their properties defined in data packs. Damage sources, on the other hand, are created as needed by the game, based off the values in the data pack files. They can hold additional context, for example the attacking entity.

## Creating Damage Types

To get started, you want to create your own `DamageType`. `DamageType`s are a [datapack registry][dr], and as such, new `DamageType`s are not registered in code, but are registered automatically when the corresponding files are added. However, we still need to provide some point for the code to get the damage sources from. We do so by specifying a [resource key][rk]:

```java
public static final ResourceKey<DamageType> EXAMPLE_DAMAGE =
        ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "example"));
```

Now that we can reference it from code, let's specify some properties in the data file. Our data file is located at `data/examplemod/damage_type/example.json` (swap out `examplemod` and `example` for the mod id and the name of the resource location) and contains the following:

```json5
{
    // The death message id of the damage type. The full death message translation key will be
    // "death.examplemod.example" (with swapped-out mod ids and names).
    "message_id": "example",
    // Whether this damage type's damage amount scales with difficulty or not. Valid vanilla values are:
    // - "never": The damage value remains the same on any difficulty. Common for player-caused damage types.
    // - "when_caused_by_living_non_player": The damage value is scaled if the entity is caused by a
    //   living entity of some sort, including indirectly (e.g. an arrow shot by a skeleton), that is not a player.
    // - "always": The damage value is always scaled. Commonly used by explosion-like damage.
    "scaling": "when_caused_by_living_non_player",
    // The amount of exhaustion caused by receiving this kind of damage.
    "exhaustion": 0.1,
    // The damage effects (currently only sound effects) that are applied when receiving this kind of damage. Optional.
    // Valid vanilla values are "hurt" (default), "thorns", "drowning", "burning", "poking", and "freezing".
    "effects": "hurt",
    // The death message type. Determines how the death message is built. Optional.
    // Valid vanilla values are "default" (default), "fall_variants", and "intentional_game_design".
    "death_message_type": "default"
}
```

:::tip
The `scaling`, `effects` and `death_message_type` fields are internally controlled by the enums `DamageScaling`, `DamageEffects` and `DeathMessageType`, respectively. These enums can be [extended][extenum] to add custom values if needed.
:::

The same format is also used for vanilla's damage types, and pack developers can change these values if needed.
 
## Creating and Using Damage Sources

`DamageSource`s are usually created on the fly when `Entity#hurt` is called. Be aware that since damage types are a [datapack registry][dr], you will need a `RegistryAccess` to query them, which can be obtained via `Level#registryAccess`. To create a `DamageSource`, call the `DamageSource` constructor with up to four parameters:

```java
DamageSource damageSource = new DamageSource(
        // The damage type holder to use. Query from the registry. This is the only required parameter.
        registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(EXAMPLE_DAMAGE),
        // The direct entity. For example, if a skeleton shot you, the skeleton would be the causing entity
        // (= the parameter above), and the arrow would be the direct entity (= this parameter). Similar to
        // the causing entity, this isn't always applicable and therefore nullable. Optional, defaults to null.
        null,
        // The entity causing the damage. This isn't always applicable (e.g. when falling out of the world)
        // and may therefore be null. Optional, defaults to null.
        null,
        // The damage source position. This is rarely used, one example would be intentional game design
        // (= nether beds exploding). Nullable and optional, defaulting to null.
        null
);
```

:::warning
`DamageSources#source`, which is a wrapper around `new DamageSource`, flips the second and third parameters (direct entity and causing entity). Make sure you are supplying the correct values to the correct parameters.
:::

If `DamageSource`s have no entity or position context whatsoever, it makes sense to cache them in a field. For `DamageSource`s that do have entity or position context, it is common to add helper methods, like so:

```java
public static DamageSource exampleDamage(Entity causer) {
    return new DamageSource(
            causer.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(EXAMPLE_DAMAGE),
            causer);
}
```

:::tip
Vanilla's `DamageSource` factories can be found in `DamageSources`, and vanilla's `DamageType` resource keys can be found in `DamageTypes`.
:::

The first and foremost use case for damage sources is `Entity#hurt`. This method is called whenever an entity is receiving damage. To hurt an entity with our own damage type, we simply call `Entity#hurt` ourselves:

```java
// The second parameter is the amount of damage, in half hearts.
entity.hurt(exampleDamage(player), 10);
```

Other damage type-specific behavior, such as invulnerability checks, is often run through damage type [tags]. These are both added by Minecraft and NeoForge and can be found under `DamageTypeTags` and `Tags.DamageTypes`, respectively.

## Datagen

_For more info, see [Data Generation for Datapack Registries][drdatagen]._

Damage type JSON files can be [datagenned][datagen]. Since damage types are a datapack registry, we add a `DatapackBuiltinEntriesProvider` to the `GatherDataEvent` and put our damage types in the `RegistrySetBuilder`:

```java
// In your datagen class
@SubscribeEvent // on the mod event bus
public static void onGatherData(GatherDataEvent event) {
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
    event.getGenerator().addProvider(
            event.includeServer(),
            output -> new DatapackBuiltinEntriesProvider(output, lookupProvider, new RegistrySetBuilder()
                    // Add a datapack builtin entry provider for damage types. If this lambda becomes longer,
                    // this should probably be extracted into a separate method for the sake of readability.
                    .add(Registries.DAMAGE_TYPE, bootstrap -> {
                        // Use new DamageType() to create an in-code representation of a damage type.
                        // The parameters map to the values of the JSON file, in the order seen above.
                        // All parameters except for the message id and the exhaustion value are optional.
                        bootstrap.register(EXAMPLE_DAMAGE, new DamageType(EXAMPLE_DAMAGE.location(),
                                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                                0.1f,
                                DamageEffects.HURT,
                                DeathMessageType.DEFAULT))
                    })
                    // Add datapack providers for other datapack entries, if applicable.
                    .add(...),
                    Set.of(ExampleMod.MOD_ID)
            )
    );
}
```

[datagen]: ../index.md#data-generation
[dr]: ../../concepts/registries.md#datapack-registries
[drdatagen]: ../../concepts/registries.md#data-generation-for-datapack-registries
[extenum]: ../../advanced/extensibleenums.md
[rk]: ../../misc/resourcelocation.md#resourcekeys
[tags]: tags.md
