# Loot Conditions

Loot conditions can be used to check whether a [loot entry][entry] or [loot pool][pool] should be used in the current context. In both cases, a list of conditions is defined; the entry or pool is only used if all conditions pass. During datagen, they are added to a `LootPoolEntryContainer.Builder<?>` or `LootPool.Builder` by calling `#when` with an instance of the desired condition. This article will outline the available loot conditions. To create your own loot conditions, see [Custom Loot Conditions][custom].

## `minecraft:inverted`

This condition accepts another condition and inverts its result. Requires whatever loot parameters the other condition requires.

```json5
{
    "condition": "minecraft:inverted",
    "term": {
        // Some other loot condition.
    }
}
```

During datagen, call `InvertedLootItemCondition#invert` with the condition to invert to construct a builder for this condition.

## `minecraft:all_of`

This condition accepts any number of other conditions and returns true if all sub conditions return true. If the list is empty, it returns false. Requires whatever loot parameters the other conditions require.

```json5
{
    "condition": "minecraft:all_of",
    "terms": [
        {
            // A loot condition.
        },
        {
            // Another loot condition.
        },
        {
            // Yet another loot condition.
        }
    ]
}
```

During datagen, call `AllOfCondition#allOf` with the desired condition(s) to construct a builder for this condition.

## `minecraft:any_of`

This condition accepts any number of other conditions and returns true if at least one sub condition returns true. If the list is empty, it returns false. Requires whatever loot parameters the other conditions require.

```json5
{
    "condition": "minecraft:any_of",
    "terms": [
        {
            // A loot condition.
        },
        {
            // Another loot condition.
        },
        {
            // Yet another loot condition.
        }
    ]
}
```

During datagen, call `AnyOfCondition#anyOf` with the desired condition(s) to construct a builder for this condition.

## `minecraft:random_chance`

This condition accepts a [number provider][numberprovider] representing a chance between 0 and 1, and randomly returns true or false depending on that chance. The number provider should generally not return values outside the `[0, 1]` interval.

```json5
{
    "condition": "minecraft:random_chance",
    // A constant 50% chance for the condition to apply. 
    "chance": 0.5
}
```

During datagen, call `RandomChance#randomChance` with the number provider or a (constant) float value to construct a builder for this condition.

## `minecraft:random_chance_with_enchanted_bonus`

This condition accepts an enchantment id, a [`LevelBasedValue`][numberprovider] and a constant fallback float value. If the specified enchantment is present, the `LevelBasedValue` is queried for a value. If the specified enchantment is absent, or no value could be retrieved from the `LevelBasedValue`, the constant fallback value is used. The condition then randomly returns true or false, with the previously determined value denoting the chance that true is returned. Requires the `minecraft:attacking_entity` parameter, falling back to level 0 if absent.

```json5
{
    "condition": "minecraft:random_chance_with_enchanted_bonus",
    // Add a 20% chance per looting level to succeed.
    "enchantment": "minecraft:looting",
    "enchanted_chance": {
        "type": "linear",
        "base": 0.2,
        "per_level_above_first": 0.2
    },
    // Always fail if the looting enchantment is not present.
    "unenchanted_chance": 0.0
}
```

During datagen, call `LootItemRandomChanceWithEnchantedBonusCondition#randomChanceAndLootingBoost` with the registry lookup (`HolderLookup.Provider`), the base value and the increase per level to construct a builder for this condition. Alternatively, call `new LootItemRandomChanceWithEnchantedBonusCondition` to further specify the values.

## `minecraft:value_check`

This condition accepts a [number provider][numberprovider] and an `IntRange`, returning true if the result of the number provided is within the range.

```json5
{
    "condition": "minecraft:value_check",
    // May be any number provider.
    "value": {
        "type": "minecraft:uniform",
        "min": 0.0,
        "max": 10.0
    },
    // A range with min/max values.
    "range": {
        "min": 2.0,
        "max": 5.0
    }
}
```

During datagen, call `ValueCheckCondition#hasValue` with the number provider and the range to construct a builder for this condition.

## `minecraft:time_check`

This condition checks if the world time is within an `IntRange`. Optionally, a `period` parameter can be provided to modulo the time with; this can be used to e.g. check the time of day if `period` is 24000 (one in-game day/night cycle has 24000 ticks).

```json5
{
    "condition": "minecraft:time_check",
    // Optional, can be omitted. If omitted, no modulo operation will take place.
    // We use 24000 here, which is the length of one in-game day/night cycle.
    "period": 24000,
    // A range with min/max values. This example checks if the time is between 0 and 12000.
    // Combined with the modulo operand of 24000 specified above, this example checks if it is currently daytime.
    "value": {
        "min": 0,
        "max": 12000
    }
}
```

During datagen, call `TimeCheck#time` with the desired range to construct a builder for this condition. The `period` value can then be set on the builder using `#setPeriod`.

## `minecraft:weather_check`

This condition checks the current weather for raining and thundering.

```json5
{
    "condition": "minecraft:weather_check",
    // Optional. If unspecified, the rain state will not be checked.
    "raining": true,
    // Optional. If unspecified, the thundering state will not be checked.
    // Specifying "raining": true and "thundering": true is functionally equivalent to just specifying
    // "thundering": true, since it is always raining when a thunderstorm occurs.
    "thundering": false
}
```

During datagen, call `WeatherCheck#weather` to construct a builder for this condition. The `raining` and `thundering` values can then be set on the builder using `#setRaining` and `#setThundering`, respectively.

## `minecraft:location_check`

This condition accepts a `LocationPredicate` and an optional offset value for each axis direction. `LocationPredicate`s allow checking conditions such as the position itself, the block or fluid state at that position, the dimension, biome or structure at that position, the light level, whether the sky is visible, etc. All possible values can be viewed in the `LocationPredicate` class definition. Requires the `minecraft:origin` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:location_check",
    "predicate": {
        // Succeed if our target is anywhere in the nether.
        "dimension": "the_nether"
    },
    // Optional position offset values. Only relevant if you are checking the position in some way.
    // Must either be provided all at once, or not at all.
    "offsetX": 10,
    "offsetY": 10,
    "offsetZ": 10
}
```

During datagen, call `LocationCheck#checkLocation` with the `LocationPredicate` and optionally a `BlockPos` to construct a builder for this condition.

## `minecraft:block_state_property`

This condition checks for the specified block state properties to have the specified value in the broken block state. Requires the `minecraft:block_state` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:block_state_property",
    // The expected block. If this does not match the block that is actually broken, the condition fails.
    "block": "minecraft:oak_slab",
    // The block state properties to match. Unspecified properties can have either value.
    // In this example, we want to only succeed if a top slab - waterlogged or not - is broken.
    // If this specifies properties not present on the block, a log warning will be printed.
    "properties": {
        "type": "top"
    }
}
```

During datagen, call `LootItemBlockStatePropertyCondition#hasBlockStateProperties` with the block to construct a builder for this condition. The desired block state property values can then be set on the builder using `#setProperties`.

## `minecraft:survives_explosion`

This condition randomly destroys the drops. The chance for drops to survive is 1 / `explosion_radius` loot parameter. This function is used by all block drops, with very few exceptions such as the beacon or the dragon egg. Requires the `minecraft:explosion_radius` loot parameter, always succeeding if that parameter is absent.

```json5
{
    "condition": "minecraft:survives_explosion"
}
```

During datagen, call `ExplosionCondition#survivesExplosion` to construct a builder for this condition.

## `minecraft:match_tool`

This condition accepts an `ItemPredicate` that is checked against the `tool` loot parameter. An `ItemPredicate` can specify a list of valid item ids (`items`), a min/max range for the item count (`count`), a `DataComponentPredicate` (`components`) and an `ItemSubPredicate` (`predicates`); all fields are optional. Requires the `minecraft:tool` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:match_tool",
    // Match a netherite pickaxe or axe.
    "predicate": {
        "items": [
            "minecraft:netherite_pickaxe",
            "minecraft:netherite_axe"
        ]
    }
}
```

During datagen, call `MatchTool#toolMatches` with an `ItemPredicate.Builder` to invert to construct a builder for this condition.

## `minecraft:enchantment_active`

This condition returns whether an enchantment is active or not. Requires the `minecraft:enchantment_active` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:enchantment_active",
    // Whether the enchantment should be active (true) or not (false).
    "active": true
}
```

During datagen, call `EnchantmentActiveCheck#enchantmentActiveCheck` or `#enchantmentInactiveCheck` to construct a builder for this condition.

## `minecraft:table_bonus`

This condition is similar to `minecraft:random_chance_with_enchanted_bonus`, but with fixed values instead of randomized values. Requires the `minecraft:tool` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:table_bonus",
    // Apply the bonus if the fortune enchantment is present.
    "enchantment": "minecraft:fortune",
    // The chances to use per level. This example has a 20% chance of succeeding if unenchanted,
    // 30% if enchanted at level 1, and 60% if enchanted at level 2 or above.
    "chances": [0.2, 0.3, 0.6]
}
```

During datagen, call `BonusLevelTableCondition#bonusLevelFlatChance` with the enchantment id and the chances to construct a builder for this condition.

## `minecraft:entity_properties`

This condition checks a given `EntityPredicate` against an [entity target][entitytarget]. The `EntityPredicate` can check the entity type, mob effects, nbt values, equipment, location etc.

```json5
{
    "condition": "minecraft:entity_properties",
    // The entity target to use. Valid values are "this", "attacker", "direct_attacker" or "attacking_player".
    // These correspond to the "this_entity", "attacking_entity", "direct_attacking_entity" and
    // "last_damage_player" loot parameters, respectively.
    "entity": "attacker",
    // Only succeed if the target is a pig. The predicate may also be empty, this can be used
    // to check whether the specified entity target is set at all.
    "predicate": {
        "type": "minecraft:pig"
    }
}
```

During datagen, call `LootItemEntityPropertyCondition#entityPresent` with the entity target, or `LootItemEntityPropertyCondition#hasProperties` with the entity target and the `EntityPredicate`, to construct a builder for this condition.

## `minecraft:damage_source_properties`

This condition checks a given `DamageSourcePredicate` against the damage source loot parameter. Requires the `minecraft:origin` and `minecraft:damage_source` loot parameters, always failing if those parameter are absent.

```json5
{
    "condition": "minecraft:damage_source_properties",
    "predicate": {
        // Check whether the source entity is a zombie.
        "source_entity": {
        "type": "zombie"
        }
    }
}
```

During datagen, call `DamageSourceCondition#hasDamageSource` with a `DamageSourcePredicate.Builder` to construct a builder for this condition.

## `minecraft:killed_by_player`

This condition determines whether the kill was a player kill. Used by some entity drops, for example blaze rods dropped by blazes. Requires the `minecraft:last_player_damage` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:killed_by_player"
}
```

During datagen, call `LootItemKilledByPlayerCondition#killedByPlayer` to construct a builder for this condition.

## `minecraft:entity_scores`

This condition checks the [entity target][entitytarget]'s scoreboard. Requires the loot parameter corresponding to the specified entity target, always failing if that parameter is absent.

```json5
{
    "condition": "minecraft:entity_scores"
    // The entity target to use. Valid values are "this", "attacker", "direct_attacker" or "attacking_player".
    // These correspond to the "this_entity", "attacking_entity", "direct_attacking_entity" and
    // "last_damage_player" loot parameters, respectively.
    "entity": "attacker",
    // A list of scoreboard values that must be in the given ranges.
    "scores": {
        "score1": {
            "min": 0,
            "max": 100
        },
        "score2": {
            "min": 10,
            "max": 20
        }
    }
}
```

During datagen, call `EntityHasScoreCondition#hasScores` with an entity target to construct a builder for this condition. Then, add required scores to the builder using `#withScore`.

## `minecraft:reference`

This condition references a predicate file and returns its result. See [Item Predicates][predicate] for more information.

```json5
{
    "condition": "minecraft:reference",
    // Refers to the predicate file at data/examplemod/predicate/example_predicate.json.
    "name": "examplemod:example_predicate"
}
```

During datagen, call `ConditionReference#conditionReference` with the id of the referenced predicate file to construct a builder for this condition.

## `neoforge:loot_table_id`

This condition only returns true if the surrounding loot table id matches. This is typically used within [global loot modifiers][glm].

```json5
{
    "condition": "neoforge:loot_table_id",
    // Will only apply when the loot table is for dirt
    "loot_table_id": "minecraft:blocks/dirt"
}
```

During datagen, call `LootTableIdCondition#builder` with the desired loot table id to construct a builder for this condition.

## `neoforge:can_item_perform_ability`

This condition only returns true if the item in the `tool` loot context parameter (`LootContextParams.TOOL`), usually the item used to break the block or kill the entity, can perform the specified [`ItemAbility`][itemability]. Requires the `minecraft:tool` loot parameter, always failing if that parameter is absent.

```json5
{
    "condition": "neoforge:can_item_perform_ability",
    // Will only apply if the tool can strip a log like an axe
    "ability": "axe_strip"
}
```

During datagen, call `CanItemPerformAbility#canItemPerformAbility` with the id of the desired item ability to construct a builder for this condition.

## See Also

- [Item Predicates][predicatejson] on the [Minecraft Wiki][mcwiki]

[custom]: custom.md#custom-loot-conditions
[entitytarget]: index.md#entity-targets
[entry]: index.md#loot-entry
[glm]: glm.md
[itemability]: ../../../items/tools.md#itemabilitys
[mcwiki]: https://minecraft.wiki
[numberprovider]: index.md#number-provider
[pool]: index.md#loot-pool
[predicate]: https://minecraft.wiki/w/Predicate
[predicatejson]: https://minecraft.wiki/w/Predicate#JSON_format
[registry]: ../../../concepts/registries.md
