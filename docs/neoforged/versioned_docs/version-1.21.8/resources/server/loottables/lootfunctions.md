# Loot Functions

Loot functions can be used to modify the result of a [loot entry][entry], or the multiple results of a [loot pool][pool] or [loot table][table]. In both cases, a list of functions is defined, which is run in order. During datagen, loot functions can be applied to `LootPoolSingletonContainer.Builder<?>`s, `LootPool.Builder`s and `LootTable.Builder`s by calling `#apply`. This article will outline the available loot functions. To create your own loot functions, see [Custom Loot Functions][custom].

:::note
Loot functions cannot be applied to composite loot entries (subclasses of `CompositeEntryBase` and their associated builder classes). They must be added to each singleton entry manually.
:::

All vanilla loot functions except `minecraft:sequence` can specify [loot conditions][conditions] in a `conditions` block. If one of these conditions fails, the function will not be applied. On the code side, this is controlled by the `LootItemConditionalFunction`, which all loot functions except for `SequenceFunction` extend.

## `minecraft:set_item`

Sets a different item to use in the result item stack.

```json5
{
    "function": "minecraft:set_item",
    // The item to use.
    "item": "minecraft:dirt"
}
```

It is currently not possible to create this function during datagen.

## `minecraft:set_count`

Sets an item count to use in the result item stack. Uses a [number provider][numberprovider].

```json5
{
    "function": "minecraft:set_count",
    // The count to use.
    "count": {
        "type": "minecraft:uniform",
        "min": 1,
        "max": 3
    },
    // Whether to add to the existing value instead of setting it. Optional, defaults to false.
    "add": true
}
```

During datagen, call `SetItemCountFunction#setCount` with the desired number provider and optionally an `add` boolean to construct a builder for this function.

## `minecraft:explosion_decay`

Applies an explosion decay. The item has a chance of 1 / `explosion_radius` to "survive". This is run multiple times depending on the count. Requires the `minecraft:explosion_radius` loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:explosion_decay"
}
```

During datagen, call `ApplyExplosionDecay#explosionDecay` to construct a builder for this function.

## `minecraft:limit_count`

Clamps the count of the item stack between a given `IntRange`.

```json5
{
    "function": "minecraft:limit_count",
    // The limit to use. Can have a min, a max, or both.
    "limit": {
        "max": 32
    }
}
```

During datagen, call `LimitCount#limitCount` with the desired `IntRange` to construct a builder for this function.

## `minecraft:set_custom_data`

Sets custom NBT data on the item stack.

```json5
{
    "function": "minecraft:set_custom_data",
    "tag": {
        "exampleproperty": 0
    }
}
```

During datagen, call `SetCustomDataFunction#setCustomData` with the desired [`CompoundTag`][nbt] to construct a builder for this function.

:::warning
This function should generally be considered deprecated. Use `minecraft:set_components` instead.
:::

## `minecraft:copy_custom_data`

Copies custom NBT data from a block entity or entity source to the item stack. Use of this is discouraged for block entities, use `minecraft:copy_components` or `minecraft:set_contents` instead. For entities, this requires setting the [entity target][entitytarget]. Requires the loot parameter corresponding to the specified source (entity target or block entity), no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:copy_custom_data",
    // The source to use. Valid values are either an entity target, "block_entity" to use the loot context's
    // block entity parameter, or be "storage" for command storage. If this is "storage", it can instead be a
    // JSON object that additionally specify the command storage path to be used.
    "source": "this",
    // Example for using "storage".
    "source": {
        "type": "storage",
        "source": "examplepath"
    },
    // The copy operation(s).
    "ops": [
        {
            // The source and target paths. In this example, we copy from "src" in the source to "dest" in the target.
            "source": "src",
            "target": "dest",
            // A merging strategy. Valid values are "replace", "append", and "merge".
            "op": "merge"
        }
    ]
}
```

During datagen, call `CopyCustomDataFunction#copyData` with the associated `NbtProvider` to get the builder. Then call `Builder#copy` with the desired source and target values, as well as a merging strategy (optional, defaults to `replace`), to construct a builder for this function.

## `minecraft:set_components`

Sets [data component][datacomponent] values on the item stack. Most vanilla use cases have specialized functions that are explained below.

```json5
{
    "function": "minecraft:set_components",
    // Any component can be used. In this example, we set the dyed color of the item to red.
    "components": {
        "dyed_color": {
            "rgb": 16711680
        }
    }
}
```

During datagen, call `SetComponentsFunction#setComponent` with the desired data component and value to construct a builder for this function.

## `minecraft:copy_components`

Copies [data component][datacomponent] values from a block entity to the item stack. Requires the `minecraft:block_entity` loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:copy_components",
    // The system is designed to allow multiple sources, however for now, there are only block entities available.
    "source": "block_entity",
    // By default, all components are copied. The "exclude" list allows excluding certain components, and the
    // "include" list allows explicitly re-including components. Both fields are optional.
    "exclude": [],
    "include": []
}
```

During datagen, call `CopyComponentsFunction#copyComponents` with the desired data source (usually `CopyComponentsFunction.Source.BLOCK_ENTITY`) to construct a builder for this function.

## `minecraft:copy_state`

Copies block state properties into the item stack's `block_state` [data component][datacomponent], used when trying to place a block. The block state properties to copy must be explicitly specified. Requires the `minecraft:block_state` loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:copy_state",
    // The expected block. If this does not match the block that is actually broken, the function does not run.
    "block": "minecraft:oak_slab",
    // The block state properties to save.
    "properties": {
        "type": "top"
    }
}
```

During datagen, call `CopyBlockState#copyState` with the block to construct a builder for this condition. The desired block state property values can then be set on the builder using `#copy`.

## `minecraft:set_contents`

Sets contents of the item stack.

```json5
{
    "function": "minecraft:set_contents",
    // The contents component to use. Valid values are "container", "bundle_contents" and "charged_projectiles".
    "component": "container",
    // A list of loot entries to add to the contents.
    "entries": [
        {
            "type": "minecraft:empty",
            "weight": 3
        },
        {
            "type": "minecraft:item",
            "item": "minecraft:arrow"
        }
    ]
}
```

During datagen, call `SetContainerContents#setContents` with the desired contents component to construct a builder for this function. Then, call `#withEntry` on the builder to add entries.

## `minecraft:modify_contents`

Applies a function to the contents of the item stack.

```json5
{
    "function": "minecraft:modify_contents",
    // The contents component to use. Valid values are "container", "bundle_contents" and "charged_projectiles".
    "component": "container",
    // The function to use.
    "modifier": "apply_explosion_decay"
}
```

It is currently not possible to create this function during datagen.

## `minecraft:set_loot_table`

Sets a container loot table on the result item stack. Intended for chests and other loot containers that retain this property when placed down.

```json5
{
    "function": "minecraft:set_loot_table",
    // The id of the loot table to use.
    "name": "minecraft:entities/enderman",
    // The id of the block entity type of the target block entity.
    "type": "minecraft:chest",
    // The random seed for generating loot tables. Optional, defaults to 0.
    "seed": 42
}
```

During datagen, call `SetContainerLootTable#withLootTable` with the desired block entity type, loot table resource key and optionally a seed to construct a builder for this function.

## `minecraft:set_name`

Sets a name for the result item stack. The name can be a [`Component`][component] instead of a literal string. It can also be resolved from an [entity target][entitytarget]. Requires the corresponding entity loot parameter if applicable, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:set_name",
    "name": "Funny Item",
    // The entity target to use.
    "entity": "this",
    // Whether to set the custom name ("custom_name") or the item name ("item_name") itself.
    // Custom name are displayed in italics and can be changed in an anvil, while item names cannot.
    "target": "custom_name"
}
```

During datagen, call `SetNameFunction#setName` with the desired name component, the desired name target and optionally an entity target to construct a builder for this function.

## `minecraft:copy_name`

Copies an [entity target][entitytarget]'s or block entity's name into the result item stack. Requires the loot parameter corresponding to the specified source (entity target or block entity), no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:copy_name",
    // The entity target, or "block_entity" if a block entity's name should be copied.
    "source": "this"
}
```

During datagen, call `CopyNameFunction#copyName` with the desired entity source to construct a builder for this function.

## `minecraft:set_lore`

Sets lore (tooltip lines) for the result item stack. The lines can be [`Component`][component]s instead of literal strings. It can also be resolved from an [entity target][entitytarget]. Requires the corresponding entity loot parameter if applicable, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:set_lore",
    "lore": [
        "Funny Lore",
        "Funny Lore 2"
    ],
    // The merging mode used. Valid values are:
    // - "append": Appends the entries to any existing lore entries.
    // - "insert": Inserts the entries at a certain position. The position is denoted as an additional field
    //   named "offset". "offset" is optional and defaults to 0.
    // - "replace_all": Removes all previous entries and then appends the entries.
    // - "replace_section": Removes a section of entries and then adds the entries at that position.
    //   The section removed is denoted through the "offset" and optional "size" fields.
    //   If "size" is omitted, the amount of lines in "lore" is used.
    "mode": {
        "type": "insert",
        "offset": 0
    },
    // The entity target to use.
    "entity": "this"
}
```

During datagen, call `SetLoreFunction#setLore` to construct a builder for this function. Then, call `#addLine`, `#setMode` and `#setResolutionContext` as needed on the builder.

## `minecraft:toggle_tooltips`

Enables or disables certain component tooltips.

```json5
{
    "function": "minecraft:toggle_tooltips",
    "toggles": {
        // All values are optional. If omitted, these values will use pre-existing values on the stack.
        // The pre-existing values are generally true, unless they have already been modified by another function.
        "minecraft:attribute_modifiers": false,
        "minecraft:can_break": false,
        "minecraft:can_place_on": false,
        "minecraft:dyed_color": false,
        "minecraft:enchantments": false,
        "minecraft:jukebox_playable": false,
        "minecraft:stored_enchantments": false,
        "minecraft:trim": false,
        "minecraft:unbreakable": false
    }
}
```

It is currently not possible to create this function during datagen.

## `minecraft:enchant_with_levels`

Randomly enchants the item stack with a given amount of levels. Uses a [number provider][numberprovider].

```json5
    {
    "function": "minecraft:enchant_with_levels",
    // The amount of levels to use.
    "levels": {
        "type": "minecraft:uniform",
        "min": 10,
        "max": 30
    },
    // A list of possible enchantments. Optional, defaults to all applicable enchantments for the item.
    "options": [
        "minecraft:sharpness",
        "minecraft:fire_aspect"
    ]
}
```

During datagen, call `EnchantWithLevelsFunction#enchantWithLevels` with the desired number provider to construct a builder for this function. Then, if desired, set a list of enchantments on the builder using `#fromOptions`.

## `minecraft:enchant_randomly`

Enchants the item with one random enchantment.

```json5
{
    "function": "minecraft:enchant_randomly",
    // A list of possible enchantments. Optional, defaults to all enchantments.
    "options": [
        "minecraft:sharpness",
        "minecraft:fire_aspect"
    ],
    // Whether to only allow compatible enchantments, or any enchantments. Optional, defaults to true.
    "only_compatible": true
}
```

During datagen, call `EnchantRandomlyFunction#randomEnchantment` or `EnchantRandomlyFunction#randomApplicableEnchantment` to construct a builder for this function. Then, if desired, call `#withEnchantment` or `#withOneOf` on the builder.

## `minecraft:set_enchantments`

Sets enchantments on the result item stack.

```json5
{
    "function": "minecraft:set_enchantments",
    // A map of enchantments to number providers.
    "enchantments": {
        "minecraft:fire_aspect": 2,
        "minecraft:sharpness": {
        "type": "minecraft:uniform",
        "min": 3,
        "max": 5,
        }
    },
    // Whether to add enchantment levels to existing levels instead of overwriting them. Optional, defaults to false.
    "add": true
}
```

During datagen, call `new SetEnchantmentsFunction.Builder` with the `add` boolean value (optionally) to construct a builder for this function. Then, call `#withEnchantment` to add an enchantment to set.

## `minecraft:enchanted_count_increase`

Increases the item stack count based on the enchantment value. Uses a [number provider][numberprovider]. Requires the `minecraft:attacking_entity` loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:enchanted_count_increase",
    // The enchantment to use.
    "enchantment": "minecraft:fortune",
    // The increase count per level. The number provider is rolled once per function, not once per level.
    "count": {
        "type": "minecraft:uniform",
        "min": 1,
        "max": 3
    },
    // The stack size limit, which will not be exceeded no matter the enchantment level. Optional.
    "limit": 5
}
```

During datagen, call `EnchantedCountIncreaseFunction#lootingMultiplier` with the desired number provider to construct a builder for this function. Optionally, call `#setLimit` on the builder afterwards.

## `minecraft:apply_bonus`

Applies an increase to the item stack count based on the enchantment value and various formulas. Requires the `minecraft:tool` loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:apply_bonus",
    // The enchantment value to query.
    "enchantment": "minecraft:fortune",
    // The formula to use. Valid values are:
    // - "minecraft:binomial_with_bonus_count": Applies a bonus based on a binomial distribution with
    //   n = enchantment level + extra and p = probability.
    // - "minecraft:ore_drops": Applies a bonus based on a special formula for ore drops, including randomness.
    // - "minecraft:uniform_bonus_count": Adds a bonus based on the enchantment level scaled by a constant multiplier.
    "formula": "ore_drops",
    // The parameter values, depending on the formula.
    // If the formula is "minecraft:binomial_with_bonus_count", requires "extra" and "probability".
    // If the formula is "minecraft:ore_drops", requires no parameters.
    // If the formula is "minecraft:uniform_bonus_count", requires "bonusMultiplier".
    "parameters": {}
}
```

During datagen, call `ApplyBonusCount#addBonusBinomialDistributionCount`, `ApplyBonusCount#addOreBonusCount` or `ApplyBonusCount#addUniformBonusCount` with the enchantment and other required parameters (depending on the formula) to construct a builder for this function.

## `minecraft:furnace_smelt`

Attempts to smelt the item as if it were in a furnace, returning the unmodified item stack if it could not be smelted.

```json5
{
    "function": "minecraft:furnace_smelt"
}
```

During datagen, call `SmeltItemFunction#smelted` to construct a builder for this function.

## `minecraft:set_damage`

Sets a durability damage value on the result item stack. Uses a [number provider][numberprovider].

```json5
{
    "function": "minecraft:set_damage",
    // The damage to set.
    "damage": {
        "type": "minecraft:uniform",
        "min": 10,
        "max": 300
    },
    // Whether to add to the existing damage instead of setting it. Optional, defaults to false.
    "add": true
}
```

During datagen, call `SetItemDamageFunction#setDamage` with the desired number provider and optionally an `add` boolean to construct a builder for this function.

## `minecraft:set_attributes`

Adds a list of [attribute modifiers][attributemodifier] to the result item stack.

```json5
{
    "function": "minecraft:set_attributes",
    // A list of attribute modifiers.
    "modifiers": [
        {
            // The resource location id of the modifier. Should be prefixed by your mod id.
            "id": "examplemod:example_modifier",
            // The id of the attribute the modifier is for.
            "attribute": "minecraft:generic.attack_damage",
            // The attribute modifier operation.
            // Valid values are "add_value", "add_multiplied_base" and "add_multiplied_total". 
            "operation": "add_value",
            // The amount of the modifier. This can also be a number provider.
            "amount": 5,
            // The slot(s) the modifier applies for. Valid values are "any" (any inventory slot),
            // "mainhand", "offhand", "hand", (mainhand/offhand/both hands),
            // "feet", "legs", "chest", "head", "armor" (boots/leggings/chestplates/helmets/any armor slots)
            // and "body" (horse armor and similar slots).
            "slot": "armor"
        }
    ],
    // Whether to replace the existing values instead of adding to them. Optional, defaults to true.
    "replace": false
}
```

During datagen, call `SetAttributesFunction#setAttributes` to construct a builder for this function. Then, add modifiers using `#withModifier` on the builder. Use `SetAttributesFunction#modifier` to get a modifier.

## `minecraft:set_potion`

Sets a potion on the result item stack.

```json5
{
    "function": "minecraft:set_potion",
    // The id of the potion.
    "id": "minecraft:strength"
}
```

During datagen, call `SetPotionFunction#setPotion` with the desired potion to construct a builder for this function.

## `minecraft:set_stew_effect`

Sets a list of stew effects on the result item stack.

```json5
{
    "function": "minecraft:set_stew_effect",
    // The effects to set.
    "effects": [
        {
        // The effect id.
        "type": "minecraft:fire_resistance",
        // The effect duration, in ticks. This can also be a number provider.
        "duration": 100
        }
    ]
}
```

During datagen, call `SetStewEffectFunction#stewEffect` to construct a builder for this function. Then, call `#withModifier` on the builder.

## `minecraft:set_ominous_bottle_amplifier`

Sets an ominous bottle amplifier on the result item stack. Uses a [number provider][numberprovider].

```json5
{
    "function": "minecraft:set_ominous_bottle_amplifier",
    // The amplifier to use.
    "amplifier": {
        "type": "minecraft:uniform",
        "min": 1,
        "max": 3
    }
}
```

During datagen, call `SetOminousBottleAmplifierFunction#setAmplifier` with the desired number provider to construct a builder for this function.

## `minecraft:exploration_map`

Transforms the result item stack into an exploration map if and only if it is a map. Requires the `minecraft:origin` loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:exploration_map",
    // A structure tag, containing the structures an exploration map can lead to.
    // Optional, defaults to "minecraft:on_treasure_maps", which only contains buried treasures by default.
    "destination": "minecraft:eye_of_ender_located",
    // The map decoration type to use. See the MapDecorationTypes class for available values.
    // Optional, defaults to "minecraft:mansion".
    "decoration": "minecraft:target_x",
    // The zoom level to use. Optional, defaults to 2.
    "zoom": 4,
    // The search radius to use. Optional, defaults to 50.
    "search_radius": 25,
    // Whether existing chunks are skipped when searching for structures. Optional, defaults to true.
    "skip_existing_chunks": true
}
```

During datagen, call `ExplorationMapFunction#makeExplorationMap` to construct a builder for this function. Then, call the various setters on the builder if desired.

## `minecraft:fill_player_head`

Sets the player head owner on the result item stack based on the given [entity target][entitytarget]. Requires the corresponding loot parameter, no modification is performed if that parameter is absent.

```json5
{
    "function": "minecraft:fill_player_head",
    // The entity target to use. If this doesn't resolve to a player, the stack is not modified.
    "entity": "this_entity"
}
```

During datagen, call `FillPlayerHead#fillPlayerHead` with the desired entity target to construct a builder for this function.

## `minecraft:set_banner_pattern`

Sets banner patterns on the result item stack. This is for banners, not banner pattern items.

```json5
{
    "function": "minecraft:set_banner_patterns",
    // A list of banner pattern layers.
    "patterns": [
        {
            // The id of the banner pattern to use.
            "pattern": "minecraft:globe",
            // The dye color of the layer.
            "color": "light_blue"
        }
    ],
    // Whether to append to the existing layers instead of replacing them.
    "append": true
}
```

During datagen, call `SetBannerPatternFunction#setBannerPattern` with the `append` boolean to construct a builder for this function. Then, call `#addPattern` to add patterns to the function.

## `minecraft:set_instrument`

Sets the instrument tag on the result item stack.

```json5
{
    "function": "minecraft:set_instrument",
    // The instrument tag to use.
    "options": "minecraft:goat_horns"
}
```

During datagen, call `SetInstrumentFunction#setInstrumentOptions` with the desired instrument tag to construct a builder for this function.

## `minecraft:set_fireworks`

```json5
{
    "function": "minecraft:set_fireworks",
    // The explosions to use. Optional, uses the existing data component value if absent.
    "explosions": [
        {
            // The firework explosion shape to use. Valid vanilla values are "small_ball", "large_ball",
            // "star", "creeper" and "burst". Optional, defaults to "small_ball".
            "shape": "star",
            // The colors to use. Optional, defaults to an empty list.
            "colors": [
                16711680,
                65280
            ],
            // The fade colors to use. Optional, defaults to an empty list.
            "fade_colors": [
                65280,
                255
            ],
            // Whether the explosion has a trail. Optional, defaults to false.
            "has_trail": true,
            // Whether the explosion has a twinkle. Optional, defaults to false.
            "has_twinkle": true
        }
    ],
    // The flight duration of the fireworks. Optional, uses the existing data component value if absent.
    "flight_duration": 5
}
```

It is currently not possible to create this function during datagen.

## `minecraft:set_firework_explosion`

Sets a firework explosion on the result item stack.

```json5
{
    "function": "minecraft:set_firework_explosion",
    // The firework explosion shape to use. Valid vanilla values are "small_ball", "large_ball",
    // "star", "creeper" and "burst". Optional, defaults to "small_ball".
    "shape": "star",
    // The colors to use. Optional, defaults to an empty list.
    "colors": [
        16711680,
        65280
    ],
    // The fade colors to use. Optional, defaults to an empty list.
    "fade_colors": [
        65280,
        255
    ],
    // Whether the explosion has a trail. Optional, defaults to false.
    "trail": true,
    // Whether the explosion has a twinkle. Optional, defaults to false.
    "twinkle": true
}
```

It is currently not possible to create this function during datagen.

## `minecraft:set_book_cover`

Sets a written book's non-page-specific content.

```json5
{
    "function": "minecraft:set_book_cover",
    // The book title. Optional, if absent, the book title remains unchanged.
    "title": "Hello World!",
    // The book author. Optional, if absent, the book author remains unchanged.
    "author": "Steve",
    // The book generation, i.e. how often it has been copied. Clamped between 0 and 3.
    // Optional, if absent, the book generation remains unchanged.
    "generation": 2
}
```

During datagen, call `new SetBookCoverFunction` with the desired parameters to construct a builder for this function.

## `minecraft:set_written_book_pages`

Sets the pages of a written book.

```json5
{
    "function": "minecraft:set_written_book_pages",
    // The pages to set, as a list of strings.
    "pages": [
        "Hello World!",
        "Hello World on page 2!",
        "Never Gonna Give You Up!"
    ],
    // The merging mode used. Valid values are:
    // - "append": Appends the entries to any existing lore entries.
    // - "insert": Inserts the entries at a certain position. The position is denoted as an additional field
    //   named "offset". "offset" is optional and defaults to 0.
    // - "replace_all": Removes all previous entries and then appends the entries.
    // - "replace_section": Removes a section of entries and then adds the entries at that position.
    //   The section removed is denoted through the "offset" and optional "size" fields.
    //   If "size" is omitted, the amount of lines in "lore" is used.
    "mode": {
        "type": "insert",
        "offset": 0
    }
}
```

It is currently not possible to create this function during datagen.

## `minecraft:set_writable_book_pages`

Sets the pages of a writable book (book and quill).

```json5
{
    "function": "minecraft:set_writable_book_pages",
    // The pages to set, as a list of strings.
    "pages": [
        "Hello World!",
        "Hello World on page 2!",
        "Never Gonna Give You Up!"
    ],
    // The merging mode used. Valid values are:
    // - "append": Appends the entries to any existing lore entries.
    // - "insert": Inserts the entries at a certain position. The position is denoted as an additional field
    //   named "offset". "offset" is optional and defaults to 0.
    // - "replace_all": Removes all previous entries and then appends the entries.
    // - "replace_section": Removes a section of entries and then adds the entries at that position.
    //   The section removed is denoted through the "offset" and optional "size" fields.
    //   If "size" is omitted, the amount of lines in "lore" is used.
    "mode": {
        "type": "insert",
        "offset": 0
    }
}
```

It is currently not possible to create this function during datagen.

## `minecraft:set_custom_model_data`

Sets the custom model data of the resulting item stack to use during rendering.

```json5
{
    "function": "minecraft:set_custom_model_data",
    // The float used during item model selection for the specified index
    // for a client item with a `minecraft:custom_model_data` range property.
    "floats": [
        // Will select the model where the property is less than 0.5 when "index": 0
        0.5,
        // Will select the model where the property is less than 0.25 when "index": 1
        0.25
    ],
    // The boolean used during item model selection for the specified index
    // for a client item with a `minecraft:custom_model_data` condition property.
    "flags": [
        // Will select the model where the condition is true when "index": 0
        true,
        // Will select the model where the condition is false when "index": 1
        false
    ],
    // The string used during item model selection for the specified index
    // for a client item with a `minecraft:custom_model_data` select property.
    "strings": [
        // Will select the model with the "dummy" case when "index": 0
        "dummy",
        // Will select the model with the "example" case when "index": 1
        "example"
    ],
    // The tint color to use for the specified index for a client item
    // with a `minecraft:custom_model_data` tint source.
    // 0xFF000000 is ORed with this value for an opaque color.
    "colors": [
        // Blue when "index": 0
        255,
        // Green when "index": 1
        65280
    ]
}
```

During datagen, call `new SetCustomModelDataFunction()` with the list of conditions, optional number providers, booleans, strings, and number providers to construct the associated object.

## `minecraft:filtered`

This function accepts an `ItemPredicate` that is checked against the generated stack; if the check succeeds, the other function is run. An `ItemPredicate` can specify a list of valid item ids (`items`), a min/max range for the item count (`count`), a `DataComponentPredicate` (`components`) and a map of `ItemSubPredicate`s (`predicates`); all fields are optional.

```json5
{
    "function": "minecraft:filtered",
    // The custom model data value to use. This can also be a number provider.
    "item_filter": {
        "items": [
            "minecraft:diamond_shovel"
        ]
    },
    // The other loot function to run, as either a loot modifier file or an in-line list of functions.
    "modifier": "examplemod:example_modifier"
}
```

It is currently not possible to create this function during datagen.

:::warning
This function should generally be considered deprecated. Use the passed function with a `minecraft:match_tool` condition instead.
:::

## `minecraft:reference`

This function references an item modifier and applies it to the result item stack. See [Item Modifiers][itemmodifiers] for more information.

```json5
{
    "function": "minecraft:reference",
    // Refers to the item modifier file at data/examplemod/item_modifier/example_modifier.json.
    "name": "examplemod:example_modifier"
}
```

During datagen, call `FunctionReference#functionReference` with the id of the referenced predicate file to construct a builder for this function.

## `minecraft:sequence`

This function runs other loot functions one after another.

```json5
{
    "function": "minecraft:sequence",
    // A list of functions to run.
    "functions": [
        {
            "function": "minecraft:set_count",
            // ...
        },
        {
            "function": "minecraft:explosion_decay"
        }
    ],
}
```

During datagen, call `SequenceFunction#of` with the other functions to construct a builder for this condition.

## See Also

- [Item Modifiers][itemmodifiers] on the [Minecraft Wiki][mcwiki]

[attributemodifier]: ../../../entities/attributes.md#attribute-modifiers
[component]: ../../client/i18n.md#components
[conditions]: lootconditions
[custom]: custom.md#custom-loot-functions
[datacomponent]: ../../../items/datacomponents.md
[entitytarget]: index.md#entity-targets
[entry]: index.md#loot-entry
[itemmodifiers]: https://minecraft.wiki/w/Item_modifier#JSON_format
[mcwiki]: https://minecraft.wiki
[nbt]: ../../../datastorage/nbt.md
[numberprovider]: index.md#number-provider
[pool]: index.md#loot-pool
[table]: index.md#loot-table
