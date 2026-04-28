import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Built-In Enchantment Effect Components

Vanilla Minecraft provides numerous different types of enchantment effect components for use in [enchantment] definitions. This article will explain each, including their usage and in-code definition.

## Value Effect Components

_See also [Value Effect Components] on the Minecraft Wiki_

Value effect components are used for enchantments that alter a numerical value somewhere in the game, and are implemented by the class `EnchantmentValueEffect`. If a value is altered by more than one value effect component (for example, by multiple enchantments), all of their effects will apply.

Value effect components can be set to use any of these operations on their given values:
- `minecraft:set`: Overwrites the given level-based value.
- `minecraft:add`: Adds the specified level-based value to the old one.
- `minecraft:multiply`: Multiplies the specified level-based factor by the old one.
- `minecraft:remove_binomial`: Polls a given (level-based) chance using a binomial distibution. If it works, subtracts 1 from the value. Note that many values are effectively flags, being fully on at 1 and fully off at 0.
- `minecraft:all_of`: Accepts a list of other value effects and applies them in the stated sequence.

The Sharpness enchantment uses `minecraft:damage`, a value effect component, as follows to achieve its effect:

<Tabs>
<TabItem value="sharpness.json" label="JSON">

```json5
"effects": {
    // The type of this effect component is "minecraft:damage".
    // This means that the effect will modify weapon damage.
    // See below for a list of more effect component types.
    "minecraft:damage": [
        {
            // A value effect that should be applied.
            // In this case, since there's only one, this value effect is just named "effect".
            "effect": {
                // The type of value effect to use. In this case, it is "minecraft:add", so the value (given below) will be added 
                // to the weapon damage value.
                "type": "minecraft:add",

                // The value block. In this case, the value is a LevelBasedValue that starts at 1 and increases by 0.5 every enchantment level.
                "value": {
                    "type": "minecraft:linear",
                    "base": 1.0,
                    "per_level_above_first": 0.5
                }
            }
        }
    ]
}
```

</TabItem>
<TabItem value="sharpness.datagen" label="Datagen">

```java
// Passed into 'effects' in an Enchantment during data generation
// See the Data Generation section of the Enchantments entry to learn more
DataComponentMap.builder().set(
    // Selects the "minecraft:damage" component.
    EnchantmentEffectComponents.DAMAGE,

    // Constructs a list of one conditional AddValue without any requirements.
    List.of(new ConditionalEffect<>(
        new AddValue(LevelBasedValue.perLevel(1.0F, 0.5F)),
        Optional.empty()))
).build()
```

</TabItem>
</Tabs>

The object within the `value` block is a [LevelBasedValue], which can be used to have a value effect component that changes the intensity of its effect by level.

The `EnchantmentValueEffect#process` method can be used to adjust values based on the provided numerical operations, like so:

```java
// `valueEffect` is an EnchantmentValueEffect instance.
// `enchantLevel` is an integer representing the level of the enchantment
float baseValue = 1.0;
float modifiedValue = valueEffect.process(enchantLevel, server.random, baseValue);
```

### Vanilla Enchantment Value Effect Component Types

#### Defined as `DataComponentType<EnchantmentValueEffect>`

- `minecraft:crossbow_charge_time`: Modifies the charge-up time of this crossbow in seconds. Used by Quick Charge.
- `minecraft:trident_spin_attack_strength`: Modifies the 'strength' of the spin attack of a trident (see `TridentItem#releaseUsing`). Used by Riptide.

#### Defined as `DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>`

Armor related:
- `minecraft:armor_effectiveness`: Determines effectiveness of armor against this weapon on a scale of 0 (no protection) to 1 (normal protection). Used by Breach.
- `minecraft:damage_protection`: Each "point" of damage reduction reduces damage taken while wielding this item by 4%, to a maximum reduction of 80%. Used by Blast Protection, Feather Falling, Fire Protection, Protection, and Projectile Protection.

Attack related:
- `minecraft:damage`: Modifies attack damage with this weapon. Used by Sharpness, Impaling, Bane of Arthropods, Power, and Smite. 
- `minecraft:smash_damage_per_fallen_block`: Adds damage per block fallen to a mace. Used by Density.
- `minecraft:knockback`: Modifies the amount of knockback caused while wielding this weapon, measured in game units. Used by Knockback and Punch.
- `minecraft:mob_experience`: Modifies the amount of experience for killing a mob. Unused.

Durability related:
- `minecraft:item_damage`: Modifies the durability damage taken by the item. Values below 1 act as a chance that the item takes damage. Used by Unbreaking.
- `minecraft:repair_with_xp`: Causes the item to repair itself using XP gain, and determines how effective this is. Used by Mending.

Projectile related:
- `minecraft:ammo_use`: Modifies the amount of ammo used when firing a bow or crossbow. The value is clamped to an integer, so values below 1 will result in 0 ammo use. Used by Infinity.
- `minecraft:projectile_piercing`: Modifies the number of entities pierced by a projectile from this weapon. Used by Piercing.
- `minecraft:projectile_count`: Modifies the number of projectiles spawned when shooting this bow. Used by Multishot.
- `minecraft:projectile_spread`: Modifies the maximum spread of projectiles in degrees from the direction they were fired. Used by Multishot.
- `minecraft:trident_return_acceleration`: Causes the trident to return to its owner, and modifies the acceleration applied to this trident while doing so. Used by Loyalty.

Other:
- `minecraft:block_experience`: Modifies the amount of XP from breaking a block. Used by Silk Touch.
- `minecraft:fishing_time_reduction`: Reduces the time it takes for the bobber to sink while fishing with this rod by the given number of seconds. Used by Lure.
- `minecraft:fishing_luck_bonus`: Modifies the amount of [luck] used in the fishing loot table. Used by Luck of the Sea.

#### Defined as `DataComponentType<List<TargetedConditionalEffect<EnchantmentValueEffect>>>`

- `minecraft:equipment_drops`: Modifies the chance of equipment dropping from an entity killed by this weapon. Used by Looting.

## Location Based Effect Components

_See also: [Location Based Effect Components] on the Minecraft Wiki_

Location based effect components are components that implement `EnchantmentLocationBasedEffect`. These components define actions to take that need to know where in the level the wielder of the enchantment is. They operate using two major methods: `EnchantmentEntityEffect#onChangedBlock`, which is called when the enchanted item is equipped and when the wielder changes their `BlockPos`, and `onDeactivate`, which is called when the enchanted item is removed.

Here is an example which uses the `minecraft:attributes` location based effect component type to change the wielder's entity scale:

<Tabs>
<TabItem value="attribute.json" label="JSON">

```json5
// The type is "minecraft:attributes" (described below).
// In a nutshell, this applies an attribute modifier.
"minecraft:attributes": [
    {
        // This "amount" block is a LevelBasedValue.
        "amount": {
            "type": "minecraft:linear",
            "base": 1,
            "per_level_above_first": 1
        },

        // Which attribute to modify. In this case, modifies "minecraft:scale"
        "attribute": "minecraft:generic.scale",
        // The unique identifier for this attribute modifier. Should not overlap with others, but doesn't need to be registered.
        "id": "examplemod:enchantment.size_change",
        // What operation to use on the attribute. Can be "add_value", "add_multiplied_base", or "add_multiplied_total".
        "operation": "add_value"
    }
],
```

</TabItem>
<TabItem value="attribute.datagen" label="Datagen">

```java
// Passed into the effects of an Enchantment during data generation
DataComponentMap.builder().set(
    // Specifies the "minecraft:attributes" component type.
    EnchantmentEffectComponents.ATTRIBUTES,

    // This component takes a list of these EnchantmentAttributeEffect objects.
    List.of(new EnchantmentAttributeEffect(
        ResourceLocation.fromNamespaceAndPath("examplemod", "enchantment.size_change"),
        Attributes.SCALE,
        LevelBasedValue.perLevel(1F, 1F),
        AttributeModifier.Operation.ADD_VALUE
    ))
).build()
```


</TabItem>
</Tabs>

Vanilla adds the following location based events:

- `minecraft:all_of`: Runs a list of entity effects in sequence.
- `minecraft:apply_mob_effect`: Applies a [mob effect] to the affected mob.
- `minecraft:attribute`: Applies an [attribute modifier] to the wielder of the enchantment.
- `minecraft:change_item_damage`: Damages this item's durability.
- `minecraft:damage_entity`: Does damage to the affected entity. This stacks with attack damage if in an attacking context.
- `minecraft:explode`: Summons an explosion. 
- `minecraft:ignite`: Sets the entity on fire.
- `minecraft:play_sound`: Plays a specified sound.
- `minecraft:replace_block`: Replaces a block at a given offset.
- `minecraft:replace_disk`: Replaces a disk of blocks.
- `minecraft:run_function`: Runs a specified [datapack function].
- `minecraft:set_block_properies`: Modifies the block state properties of the specified block.
- `minecraft:spawn_particles`: Spawns a particle.
- `minecraft:summon_entity`: Summons an entity.

### Vanilla Location Based Effect Component Types

#### Defined as `DataComponentType<List<ConditionalEffect<EnchantmentLocationBasedEffect>>>`

- `minecraft:location_changed`: Runs a location based effect when the wielder's Block Position changes and when this item is equipped. Used by Frost Walker and Soul Speed.

#### Defined as `DataComponentType<List<EnchantmentAttributeEffect>>`

- `minecraft:attributes`: Applies an attribute modifier to the wielder, and removes it when the enchanted item is no longer equipped.

## Entity Effect Components

_See also [Entity Effect Components] on the Minecraft Wiki._

Entity effect components are components that implement `EnchantmentEntityEffect`, an subtype of `EnchantmentLocationBasedEffect`. These override `EnchantmentLocationBasedEffect#onChangedBlock` to run `EnchantmentEntityEffect#apply` instead; this `apply` method is also directly invoked somewhere else in the codebase depending on the specific type of the component. This allows effects to occur without waiting for the wielder's block position to change.

All types of location based effect component are also valid types of entity effect component, except for `minecraft:attribute`, which is registered only as a location based effect component.

Here is an example of the JSON definition of one such component from the Fire Aspect enchantment:

<Tabs>
<TabItem value="fire.json" label="JSON">

```json5
// This component's type is "minecraft:post_attack" (see below).
"minecraft:post_attack": [
    {
        // Decides whether the "victim" of the attack, the "attacker", or the "damaging entity" (the projectile if there is one, attacker if not) recieves the effect.
        "affected": "victim",
        
        // Decides which enchantment entity effect to apply.
        "effect": {
            // The type of this effect is "minecraft:ignite".
            "type": "minecraft:ignite",
            // "minecraft:ignite" requires a LevelBasedValue as a duration for how long the entity will be ignited.
            "duration": {
                "type": "minecraft:linear",
                "base": 4.0,
                "per_level_above_first": 4.0
            }
        },

        // Decides who (the "victim", "attacker", or "damaging entity") must have the enchantment for it to take effect.
        "enchanted": "attacker",

        // An optional predicate which controls whether the effect applies.
        "requirements": {
            "condition": "minecraft:damage_source_properties",
            "predicate": {
                "is_direct": true
            }
        }
    }
]
```

</TabItem>
<TabItem value="fire.datagen" label="Datagen">

```java
// Passed into the effects of an Enchantment during data generation
DataComponentMap.builder().set(
    // Specifies the "minecraft:post_attack" component type.
    EnchantmentEffectComponents.POST_ATTACK,

    // Defines the data for this component. In this case, a list of one TargetedConditionalEffect.
    List.of(
        new TargetedConditionalEffect<>(

            // Determines the "enchanted" field.
            EnchantmentTarget.ATTACKER,

            // Determines the "affected" field.
            EnchantmentTarget.VICTIM,

            // The enchantment entity effect.
            new Ignite(LevelBasedValue.perLevel(4.0F, 4.0F)),

            // The "requirements" clause. 
            // In this case, the only optional part activated is the isDirect boolean flag.
            Optional.of(
                new DamageSourceCondition(
                    Optional.of(
                        new DamageSourcePredicate(
                            List.of(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.of(true)
                        )
                    )
                )
            )
        )
    )
).build()
```

</TabItem>
</Tabs>

Here, the entity effect component is `minecraft:post_attack`. Its effect is `minecraft:ignite`, which is implemented by the `Ignite` record. This record's implementation of `EnchantmentEntityEffect#apply` sets the target entity on fire.

### Vanilla Enchantment Entity Effect Component Types

#### Defined as `DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>>`

- `minecraft:hit_block`: Runs an entity effect when an entity (for example, a projectile) hits a block. Used by Channeling.
- `minecraft:tick`: Runs an entity effect each tick. Used by Soul Speed.
- `minecraft:projectile_spawned`: Runs an entity effect after a projectile entity has been spawned from a bow or crossbow. Used by Flame.

#### Defined as `DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>>`

- `minecraft:post_attack`: Runs an entity effect after an attack damages an entity. Used by Bane of Arthropods, Channeling, Fire Aspect, Thorns, and Wind Burst.

For more detail on each of these, please look at the [relevant minecraft wiki page].

## Other Vanilla Enchantment Component Types

#### Defined as `DataComponentType<List<ConditionalEffect<DamageImmunity>>>`

- `minecraft:damage_immunity`: Applies immunity to a specified damage type. Used by Frost Walker.

#### Defined as `DataComponentType<Unit>`

- `minecraft:prevent_equipment_drop`: Prevents this item from being dropped by a player when dying. Used by Curse of Vanishing.
- `minecraft:prevent_armor_change`: Prevents this item from being unequipped from an armor slot. Used by Curse of Binding.

#### Defined as `DataComponentType<List<CrossbowItem.ChargingSounds>>`

- `minecraft:crossbow_charge_sounds`: Determines the sound events that occur when charging a crossbow. Each entry represents one level of the enchantment.

#### Defined as `DataComponentType<List<Holder<SoundEvent>>>`

- `minecraft:trident_sound`: Determines the sound events that occur when using a trident. Each entry represents one level of the enchantment.

[enchantment]: index.md
[Value Effect Components]: https://minecraft.wiki/w/Enchantment_definition#Components_with_value_effects
[Entity Effect Components]: https://minecraft.wiki/w/Enchantment_definition#Components_with_entity_effects
[Location Based Effect Components]: https://minecraft.wiki/w/Enchantment_definition#location_changed
[text component]: ../../client/i18n.md
[LevelBasedValue]: ../loottables/index.md#number-provider
[Attribute Effect Component]: https://minecraft.wiki/w/Enchantment_definition#Attribute_effects
[datapack function]: https://minecraft.wiki/w/Function_(Java_Edition)
[luck]: https://minecraft.wiki/w/Luck
[mob effect]: ../../../items/mobeffects.md
[attribute modifier]: https://minecraft.wiki/w/Attribute#Modifiers
[relevant minecraft wiki page]: https://minecraft.wiki/w/Enchantment_definition#Components_with_entity_effects