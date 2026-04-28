import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Enchantments

Enchantments are special effects that can be applied to tools and other items. As of 1.21, enchantments are stored on items as [Data Components], are defined in JSON, and are comprised of so-called enchantment effect components. During the game, the enchantments on a particular item are contained within the `DataComponentTypes.ENCHANTMENT` component, in an `ItemEnchantment` instance.

A new enchantment can be added by creating a JSON file in your namespace's `enchantment` datapack subfolder. For example, to create an enchantment called `examplemod:example_enchant`, one would create a file `data/examplemod/enchantment/example_enchantment.json`. 

## Enchantment JSON Format

```json5
{
    // The text component that will be used as the in-game name of the enchantment.
    // Can be a translation key or a literal string. 
    // Remember to translate this in your lang file if you use a translation key!
    "description": {
        "translate": "enchantment.examplemod.enchant_name"
    },
    
    // Which items this enchantment can be applied to.
    // Can be either an item id, such as "minecraft:trident",
    // or a list of item ids, such as ["examplemod:red_sword", "examplemod:blue_sword"]
    // or an item tag, such as "#examplemod:enchantable/enchant_name".
    // Note that this doesn't cause the enchantment to appear for these items in the enchanting table.
    "supported_items": "#examplemod:enchantable/enchant_name",

    // (Optional) Which items this enchantment appears for in the enchanting table.
    // Can be an item, list of items, or item tag.
    // If left unspecified, this is the same as `supported_items`.
    "primary_items": [
        "examplemod:item_a",
        "examplemod:item_b"
    ],

    // (Optional) Which enchantments are incompatible with this one.
    // Can be an enchantment id, such as "minecraft:sharpness",
    // or a list of enchantment ids, such as ["minecraft:sharpness", "minecraft:fire_aspect"],
    // or enchantment tag, such as "#examplemod:exclusive_to_enchant_name".
    // Incompatible enchantments will not be added to the same item by vanilla mechanics.
    "exclusive_set": "#examplemod:exclusive_to_enchant_name",
    
    // The likelihood that this enchantment will appear in the Enchanting Table. 
    // Bounded by [1, 1024].
    "weight": 6,
    
    // The maximum level this enchantment is allowed to reach.
    // Bounded by [1, 255].
    "max_level": 3,
    
    // The maximum cost of this enchantment, measured in "enchanting power". 
    // This corresponds to, but is not equivalent to, the threshold in levels the player needs to meet to bestow this enchantment.
    // See below for details.
    // The actual cost will be between this and the min_cost.
    "max_cost": {
        "base": 45,
        "per_level_above_first": 9
    },
    
    // Specifies the minimum cost of this enchantment; otherwise as above.
    "min_cost": {
        "base": 2,
        "per_level_above_first": 8
    },

    // The cost that this enchantment adds to repairing an item in an anvil in levels. The cost is multiplied by enchantment level.
    // If an item has a DataComponentTypes.STORED_ENCHANTMENTS component, the cost is halved. In vanilla, this only applies to enchanted books.
    // Bounded by [1, inf).
    "anvil_cost": 2,
    
    // (Optional) A list of slot groups this enchantment provides effects in. 
    // A slot group is defined as one of the possible values of the EquipmentSlotGroup enum.
    // In vanilla, these are: `any`, `hand`, `mainhand`, `offhand`, `armor`, `feet`, `legs`, `chest`, `head`, and  `body`.
    "slots": [
        "mainhand"
    ],

    // The effects that this enchantment provides as a map of enchantment effect components (read on).
    "effects": {
        "examplemod:custom_effect": [
            {
                "effect": {
                    "type": "minecraft:add",
                    "value": {
                        "type": "minecraft:linear",
                        "base": 1,
                        "per_level_above_first": 1
                    }
                }
            }
        ]
    }
}
```

### Enchantment Costs and Levels

The `max_cost` and `min_cost` fields specify boundaries for how much enchanting power is needed to create this enchantment. There is a somewhat convoluted procedure to actually make use of these values, however.

First, the table takes into account the return value of `IBlockExtension#getEnchantPowerBonus()` for the surrounding blocks. From this, it calls `EnchantmentHelper#getEnchantmentCost` to derive a 'base level' for each slot. This level is shown in-game as the green numbers besides the enchantments in the menu. For each enchantment, the base level is modified twice by a random value derived from the item's enchantability (its return value from `IItemExtension#getEnchantmentValue()`), like so:

`(Modified Level) = (Base Level) + random.nextInt(e / 4 + 1) + random.nextInt(e / 4 + 1)`, where `e` is the enchantability score.

This modified level is adjusted up or down by a random 15%, and then is finally used to choose an enchantment. This level must fall within your enchantment's cost bounds in order for it to be chosen.

In practical terms, this means that the cost values in your enchantment definition might be above 30, sometimes far above. For example, with an enchantability 10 item, the table could produce enchantments up to 1.15 * (30 + 2 * (10 / 4) + 1) = 40 cost. 

## Enchantment Effect Components

Enchantment effect components are specially-registered [Data Components] that determine how an enchantment functions. The type of the component defines its effect, while the data it contains is used to inform or modify that effect. For instance, the `minecraft:damage` component modifies the damage that a weapon deals by an amount determined by its data.

Vanilla defines various [built-in enchantment effect components], which are used to implement all vanilla enchantments.

### Custom Enchantment Effect Components

The logic of applying a custom enchantment effect component must be entirely implemented by its creator. First, you should define a class or record to hold the information you need to implement a given effect. For example, let's make an example record class `Increment`:

```java
// Define an example data-bearing record.
public record Increment(int value) {
    public static final Codec<Increment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("value").forGetter(Increment::value)
            ).apply(instance, Increment::new)
    );

    public int add(int x) {
        return value() + x;
    }
}
```

Enchantment effect component types must be [registered] to `BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE`, which takes a `DataComponentType<?>`. For example, you could register an enchantment effect component that can store an `Increment` object as follows:

```java
// In some registration class
public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, "examplemod");

public static final DeferredHolder<DataComponentType<?>, DataComponentType<Increment>>> INCREMENT =
    ENCHANTMENT_COMPONENT_TYPES.register("increment",
        () -> DataComponentType.<Increment>builder()
            .persistent(Increment.CODEC)
            .build());
```

Now, we can implement some game logic that makes use of this component to alter an integer value:

```java
// Somewhere in game logic where an `itemStack` is available.
// `INCREMENT` is the enchantment component type holder defined above.
// `value` is an integer.
AtomicInteger atomicValue = new AtomicInteger(value);

EnchantmentHelper.runIterationOnItem(stack, (enchantmentHolder, enchantLevel) -> {
    // Acquire the Increment instance from the enchantment holder (or null if this is a different enchantment)
    Increment increment = enchantmentHolder.value().effects().get(INCREMENT.get());

    // If this enchant has an Increment component, use it.
    if(increment != null){
        atomicValue.set(increment.add(atomicValue.get()));
    }
});

int modifiedValue = atomicValue.get();
// Use the now-modified value elsewhere in your game logic.
```

First, we invoke one of the overloads of `EnchantmentHelper#runIterationOnItem`. This function accepts an `EnchantmentHelper.EnchantmentVisitor`, which is a functional interface that accepts an enchantment and its level, and is invoked on all of the enchantments that the given itemstack has (essentially a `BiConsumer<Enchantment, Integer>`).

To actually perform the adjustment, use the provided `Increment#add` method. Since this is inside of a lambda expression, we need to use a type that can be updated atomically, such as `AtomicInteger`, to modify this value. This also permits multiple `INCREMENT` components to run on the same item and stack their effects, like what happens in vanilla.

### `ConditionalEffect`
Wrapping the type in `ConditionalEffect<?>` allows the enchantment effect component to optionally take effect based on a given [LootContext].

`ConditionalEffect` provides `ConditionalEffect#matches(LootContext context)`, which returns whether the effect should be allowed to run based on its internal `Optional<LootItemConditon>`, and handled serialization and deserialization of its `LootItemCondition`.

Vanilla adds an additional helper method to further streamline the process of checking these conditions: `Enchantment#applyEffects()`. This method takes a `List<ConditionalEffect<T>>`, evaluates the conditions, and runs a `Consumer<T>` on each `T` contained by a `ConditionalEffect` whose condition was met. Since many vanilla enchantment effect components are defined as a `List<ConditionalEffect<?>>`, these can be directly plugged into the helper method like so:

```java
// `enchant` is an Enchantment instance.
// `lootContext` is a LootContext instance.
Enchantment.applyEffects(
    enchant.getEffects(EnchantmentEffectComponents.KNOCKBACK), // Or whichever other List<ConditionalEffect<T>> you want
    lootContext,
    (effectData) -> // Use the effectData (in this example, an EnchantmentValueEffect) however you want.
);
```

Registering a custom `ConditionalEffect`-wrapped enchantment effect component type can be done as follows:

```java
public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConditionalEffect<Increment>>> CONDITIONAL_INCREMENT =
    ENCHANTMENT_COMPONENT_TYPES.register("conditional_increment",
        () -> DataComponentType.ConditionalEffect<Increment>builder()
            // The LootContextParamSet needed depends on what the enchantment is supposed to do.
            // This might be one of ENCHANTED_DAMAGE, ENCHANTED_ITEM, ENCHANTED_LOCATION, ENCHANTED_ENTITY, or HIT_BLOCK
            // since all of these bring the enchantment level into context (along with whatever other information is indicated).
            .persistent(ConditionalEffect.codec(Increment.CODEC, LootContextParamSets.ENCHANTED_DAMAGE))
            .build());
```
The parameters to `ConditionalEffect.codec` are the codec for the generic `ConditionalEffect<T>`, followed by some `LootContextParamSets` entry.

## Enchantment Data Generation

Enchantment JSON files can be created automatically using the [data generation] system by passing a `RegistrySetBuilder` into `DatapackBuiltInEntriesProvider`. The JSON will be placed in `<project root>/src/generated/data/<modid>/enchantment/<path>.json`.

For more information on how `RegistrySetBuilder` and `DatapackBuiltinEntriesProvider` work, please see the article on [Data Generation for Datapack Registries]. 

<Tabs>
<TabItem value="datagen" label="Datagen">

```java

// This RegistrySetBuilder should be passed into a DatapackBuiltinEntriesProvider in your GatherDataEvent handler.
RegistrySetBuilder BUILDER = new RegistrySetBuilder();
BUILDER.add(
    Registries.ENCHANTMENT,
    bootstrap -> bootstrap.register(
        // Define the ResourceKey for our enchantment.
        ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("examplemod", "example_enchantment")
        ),
        new Enchantment(
            // The text Component that specifies the enchantment's name.
            Component.literal("Example Enchantment"),  
            
            // Specify the enchantment definition of for our enchantment.
            new Enchantment.EnchantmentDefinition(
                // A HolderSet of Items that the enchantment will be compatible with.
                HolderSet.direct(...), 

                // An Optional<HolderSet> of items that the enchantment considers "primary".
                Optional.empty(), 

                // The weight of the enchantment.
                30, 

                // The maximum level this enchantment can be.
                3, 

                // The minimum cost of the enchantment. The first parameter is base cost, the second is cost per level.
                new Enchantment.Cost(3, 1), 

                // The maximum cost of the enchantment. As above.
                new Enchantment.Cost(4, 2), 

                // The anvil cost of the enchantment.
                2, 

                // A list of EquipmentSlotGroups that this enchantment has effects in.
                List.of(EquipmentSlotGroup.ANY) 
            ),
            // A HolderSet of incompatible other enchantments.
            HolderSet.empty(), 

            // A DataComponentMap of the enchantment effect components associated with this enchantment and their values.
            DataComponentMap.builder() 
                .set(MY_ENCHANTMENT_EFFECT_COMPONENT_TYPE, new ExampleData())
                .build()
        )
    )
);

```

</TabItem>

<TabItem value="json" label="JSON" default>

```json5
// For more detail on each entry, please check the section above on the enchantment JSON format.
{
    // The anvil cost of the enchantment.
    "anvil_cost": 2,

    // The text Component that specifies the enchantment's name.
    "description": "Example Enchantment",

    // A map of the effect components associated with this enchantment and their values.
    "effects": {
        // <effect components>
    },

    // The maximum cost of the enchantment.
    "max_cost": {
        "base": 4,
        "per_level_above_first": 2
    },

    // The maximum level this enchantment can be.
    "max_level": 3,

    // The minimum cost of the enchantment.
    "min_cost": {
        "base": 3,
        "per_level_above_first": 1
    },

    // A list of EquipmentSlotGroup aliases that this enchantment has effects in.
    "slots": [
        "any"
    ],

    // The set of items that this enchantment can be applied to using an anvil.
    "supported_items": /* <supported item list> */,

    // The weight of this enchantment.
    "weight": 30
}
```

</TabItem>
</Tabs>

[Data Components]: /docs/items/datacomponents
[Codec]: /docs/datastorage/codecs
[Enchantment definition Minecraft wiki page]: https://minecraft.wiki/w/Enchantment_definition
[registered]: /docs/concepts/registries
[Predicate]: https://minecraft.wiki/w/Predicate
[data generation]: /docs/resources/#data-generation
[Data Generation for Datapack Registries]: https://docs.neoforged.net/docs/concepts/registries/#data-generation-for-datapack-registries
[relevant minecraft wiki page]: https://minecraft.wiki/w/Enchantment_definition#Entity_effects
[built-in enchantment effect components]: builtin.md
[LootContext]: /docs/resources/server/loottables/#loot-context