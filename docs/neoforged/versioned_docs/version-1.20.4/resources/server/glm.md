# Global Loot Modifiers

Global Loot Modifiers are a data-driven method of handling modification of harvested drops without the need to overwrite dozens to hundreds of vanilla loot tables or to handle effects that would require interactions with another mod's loot tables without knowing what mods may be loaded. Global Loot Modifiers are also stacking, rather than last-load-wins, similar to tags.

## Registering a Global Loot Modifier

You will need 4 things:

1. Create a `global_loot_modifiers.json`.
    - This will tell NeoForge about your modifiers and works similar to [tags].
2. A serialized json representing your modifier.
    - This will contain all of the data about your modification and allows data packs to tweak your effect.
3. A class that extends `IGlobalLootModifier`.
    - The operational code that makes your modifier work. Most modders can extend `LootModifier` as it supplies base functionality.
4. Finally, a codec to encode and decode your operational class.
    - This is [registered] as any other `IForgeRegistryEntry`.

## The `global_loot_modifiers.json`

The `global_loot_modifiers.json` represents all loot modifiers to be loaded into the game. This file **MUST** be placed within `data/forge/loot_modifiers/global_loot_modifiers.json`.

:::danger
`global_loot_modifiers.json` will only be read in the `forge` namespace. The file will be neglected if it is under the mod's namespace.
:::

`entries` is an *ordered list* of the modifiers that will be loaded. The [ResourceLocation][resloc]s specified points to their associated entry within `data/<namespace>/loot_modifiers/<path>.json`. This is primarily relevant to data pack makers for resolving conflicts between modifiers from separate mods.

`replace`, when `true`, changes the behavior from appending loot modifiers to the global list to replacing the global list entries entirely. Modders will want to use `false` for compatibility with other mod implementations. Datapack makers may want to specify their overrides with `true`.

```js
{
  "replace": false, // Must be present
  "entries": [
    // Represents a loot modifier in 'data/examplemod/loot_modifiers/example_glm.json'
    "examplemod:example_glm",
    "examplemod:example_glm2"
    // ...
  ]
}
```

## The Serialized JSON

This file contains all of the potential variables related to your modifier, including the conditions that must be met prior to modifying any loot. Avoid hard-coded values wherever possible so that data pack makers can adjust balance if they wish to.

`type` represents the registry name of the [codec] used to read the associated JSON file. This must always be present.

`conditions` should represent the loot table conditions for this modifier to activate. Conditions should avoid being hardcoded to allow datapack creators as much flexibility to adjust the criteria. This must also be always present.

:::caution
Although `conditions` should represent what is needed for the modifier to activate, this is only the case if using the bundled NeoForge classes. If using `LootModifier` as a subclass, all conditions will be **ANDed** together and checked to see if the modifier should be applied.
:::

Any additional properties read by the serializer and defined by the modifier can also be specified.

```js
// Within data/examplemod/loot_modifiers/example_glm.json
{
  "type": "examplemod:example_loot_modifier",
  "conditions": [
    // Normal loot table conditions
    // ...
  ],
  "prop1": "val1",
  "prop2": 10,
  "prop3": "minecraft:dirt"
}
```

## `IGlobalLootModifier`

To supply the functionality a global loot modifier specifies, a `IGlobalLootModifier` implementation must be specified. These are instances generated each time a serializer decodes the information from JSON and supplies it into this object.

There are two methods that needs to be defined in order to create a new modifier: `#apply` and `#codec`. `#apply` takes in the current loot that will be generated along with the context information such as the currently level or additional defined parameters. It returns the list of drops to generate.

:::info
The returned list of drops from any one modifier is fed into other modifiers in the order they are registered. As such, modified loot can be modified by another loot modifier.
:::

`#codec` returns the registered [codec] used to encode and decode the modifier to/from JSON.

### The `LootModifier` Subclass

`LootModifier` is an abstract implementation of `IGlobalLootModifier` to provide the base functionality which most modders can easily extend and implement. This expands upon the existing interface by defining the `#apply` method to check the conditions to determine whether or not to modify the generated loot.

There are two things of note within the subclass implementation: the constructor which must take in an array of `LootItemCondition`s and the `#doApply` method.

The array of `LootItemCondition`s define the list of conditions that must be true before the loot can be modified. The supplied conditions are **ANDed** together, meaning that all conditions must be true.

The `#doApply` method works the same as the `#apply` method except that it only executes once all conditions return true.

```java
public class ExampleModifier extends LootModifier {

  public ExampleModifier(LootItemCondition[] conditionsIn, String prop1, int prop2, Item prop3) {
    super(conditionsIn);
    // Store the rest of the parameters
  }

  @NotNull
  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    // Modify the loot and return the new drops
  }

  @Override
  public Codec<? extends IGlobalLootModifier> codec() {
    // Return the codec used to encode and decode this modifier
  }
}
```

## The Loot Modifier Codec

The connector between the JSON and the `IGlobalLootModifier` instance is a [`Codec<T>`][codecdef], where `T` represents the type of the `IGlobalLootModifier` to use.

For ease of convenience, a loot conditions codec has been provided for an easy addition to a record-like codec via `LootModifier#codecStart`. This is utilized for [data generation][datagen] of the associated loot modifier.

```java
// For some DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRAR
public static final Supplier<Codec<ExampleModifier>> = REGISTRAR.register("example_codec", () ->
  RecordCodecBuilder.create(
    inst -> LootModifier.codecStart(inst).and(
      inst.group(
        Codec.STRING.fieldOf("prop1").forGetter(m -> m.prop1),
        Codec.INT.fieldOf("prop2").forGetter(m -> m.prop2),
        ForgeRegistries.ITEMS.getCodec().fieldOf("prop3").forGetter(m -> m.prop3)
      )
    ).apply(inst, ExampleModifier::new)
  )
);
```

[Examples][examples] can be found on the NeoForge Git repository, including silk touch and smelting effects.

[tags]: ./tags.md
[resloc]: ../../misc/resourcelocation.md
[codec]: #the-loot-modifier-codec
[registered]: ../../concepts/registries.md#methods-for-registering
[codecdef]: ../../datastorage/codecs.md
[datagen]: ../../datagen/glm.md
[examples]: https://github.com/neoforged/NeoForge/blob/1.20.x/tests/src/main/java/net/neoforged/neoforge/debug/loot/GlobalLootModifiersTest.java
