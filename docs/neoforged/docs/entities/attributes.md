---
sidebar_position: 4
---
# Attributes

Attributes are special fields of [living entities][livingentity] that determine basic properties such as max health, speed or armor. All attributes are stored as double values and synced automatically. Vanilla offers a wide range of default attributes, and you can also add your own.

Due to legacy implementations, not all attributes work with all entities. For example, flying speed is ignored by ghasts, and jump strength only affects horses, not players.

## Built-In Attributes

### Minecraft

The following attributes are in the `minecraft` namespace, and their in-code values can be found in the `Attributes` class.

| Name                             | In Code                          | Range          | Default Value | Usage                                                                                                                                                                 |
|----------------------------------|----------------------------------|----------------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `armor`                          | `ARMOR`                          | `[0,30]`       | 0             | The armor value of the entity. A value of 1 means half a chestplate icon above the hotbar.                                                                            |
| `armor_toughness`                | `ARMOR_TOUGHNESS`                | `[0,20]`       | 0             | The armor toughness value of the entity. See [Armor Toughness][toughness] on the [Minecraft Wiki][wiki] for more information.                                         |
| `attack_damage`                  | `ATTACK_DAMAGE`                  | `[0,2048]`     | 2             | The base attack damage done by the entity, without any weapon or similar item.                                                                                        |
| `attack_knockback`               | `ATTACK_KNOCKBACK`               | `[0,5]`        | 0             | The extra knockback dealt by the entity. Knockback additionally has a base strength not represented by this attribute.                                                |
| `attack_speed`                   | `ATTACK_SPEED`                   | `[0,1024]`     | 4             | The attack cooldown of the entity. Higher numbers mean more cooldown, setting this to 0 effectively re-enables pre-1.9 combat.                                        |
| `block_break_speed`              | `BLOCK_BREAK_SPEED`              | `[0,1024]`     | 1             | How fast the entity can mine blocks, as a multiplicative modifier. See [Mining Speed][miningspeed] for more information.                                              |
| `block_interaction_range`        | `BLOCK_INTERACTION_RANGE`        | `[0,64]`       | 4.5           | The interaction range in which the entity can interact with blocks, in blocks.                                                                                        |
| `burning_time`                   | `BURNING_TIME`                   | `[0,1024]`     | 1             | A multiplier for how long the entity will burn when ignited.                                                                                                          |
| `camera_distance`                | `CAMERA_DISTANCE`                | `[0,32]`       | 4             | The distance of the camera from the entity when in third person, including spectating or riding another entity.                                                        |
| `explosion_knockback_resistance` | `EXPLOSION_KNOCKBACK_RESISTANCE` | `[0,1]`        | 0             | The explosion knockback resistance of the entity. This is a value in percent, i.e. 0 is no resistance, 0.5 is half resistance, and 1 is full resistance.              |
| `entity_interaction_range`       | `ENTITY_INTERACTION_RANGE`       | `[0,64]`       | 3             | The interaction range in which the entity can interact with other entities, in blocks.                                                                                |
| `fall_damage_multiplier`         | `FALL_DAMAGE_MULTIPLIER`         | `[0,100]`      | 1             | A multiplier for fall damage taken by the entity.                                                                                                                     |
| `flying_speed`                   | `FLYING_SPEED`                   | `[0,1024]`     | 0.4           | A multiplier for flying speed. This is not actually used by all flying entities, and ignored by e.g. ghasts.                                                          |
| `follow_range`                   | `FOLLOW_RANGE`                   | `[0,2048]`     | 32            | The distance in blocks that the entity will target/follow the player.                                                                                                 |
| `gravity`                        | `GRAVITY`                        | `[1,1]`        | 0.08          | The gravity the entity is influenced by, in blocks per tick squared.                                                                                                  |
| `jump_strength`                  | `JUMP_STRENGTH`                  | `[0,32]`       | 0.42          | The jump strength of the entity. Higher value means higher jumping.                                                                                                   |
| `knockback_resistance`           | `KNOCKBACK_RESISTANCE`           | `[0,1]`        | 0             | The knockback resistance of the entity. This is a value in percent, i.e. 0 is no resistance, 0.5 is half resistance, and 1 is full resistance.                        |
| `luck`                           | `LUCK`                           | `[-1024,1024]` | 0             | The luck value of the entity. This is used when rolling [loot tables][loottables] to give bonus rolls or otherwise modify the resulting items' quality.               |
| `max_absorption`                 | `MAX_ABSORPTION`                 | `[0,2048]`     | 0             | The max absorption (yellow hearts) of the entity. A value of 1 means half a heart.                                                                                    |
| `max_health`                     | `MAX_HEALTH`                     | `[1,1024]`     | 20            | The max health of the entity. A value of 1 means half a heart.                                                                                                        |
| `mining_efficiency`              | `MINING_EFFICIENCY`              | `[0,1024]`     | 0             | How fast the entity can mine blocks, as an additive modifier, only if the used tool is correct. See [Mining Speed][miningspeed] for more information.                 |
| `movement_efficiency`            | `MOVEMENT_EFFICIENCY`            | `[0,1]`        | 0             | A linearly-interpolated movement speed bonus applied to the entity when it is walking on blocks that have a slowdown, such as soul sand.                              |
| `movement_speed`                 | `MOVEMENT_SPEED`                 | `[0,1024]`     | 0.7           | The movement speed of the entity. Higher value means faster.                                                                                                          |
| `oxygen_bonus`                   | `OXYGEN_BONUS`                   | `[0,1024]`     | 0             | An oxygen bonus for the entity. The higher this is, the longer it takes for the entity to start drowning.                                                             |
| `safe_fall_distance`             | `SAFE_FALL_DISTANCE`             | `[-1024,1024]` | 3             | The fall distance for the entity that is safe, i.e. the distance in which no fall damage is taken.                                                                    |
| `scale`                          | `SCALE`                          | `[0.0625,16]`  | 1             | The scale at which the entity is rendered.                                                                                                                            |
| `sneaking_speed`                 | `SNEAKING_SPEED`                 | `[0,1]`        | 0.3           | A movement speed multiplier applied to the entity when it is sneaking.                                                                                                |
| `spawn_reinforcements`           | `SPAWN_REINFORCEMENTS_CHANCE`    | `[0,1]`        | 0             | The chance for zombies to spawn other zombies. This is only relevant on hard difficulty, as zombie reinforcements do not occur on normal difficulty or lower.         |
| `step_height`                    | `STEP_HEIGHT`                    | `[0,10]`       | 0.6           | The step height of the entity, in blocks. If this is 1, the player can walk up 1-block ledges like they were slabs.                                                   |
| `submerged_mining_speed`         | `SUBMERGED_MINING_SPEED`         | `[0,20]`       | 0.2           | How fast the entity can mine blocks, as a multiplicative modifier, only if the entity is underwater. See [Mining Speed][miningspeed] for more information.            |
| `sweeping_damage_ratio`          | `SWEEPING_DAMAGE_RATIO`          | `[0,1]`        | 0             | The amount of damage done by sweep attacks, in percent of the main attack. This is a value in percent, i.e. 0 is no damage, 0.5 is half damage, and 1 is full damage. |
| `tempt_range`                    | `TEMPT_RANGE`                    | `[0,2048]`     | 10            | The range at which the entity can be tempted using items. Mainly for passive animals, e.g. cows or pigs.                                                              |
| `water_movement_efficiency`      | `WATER_MOVEMENT_EFFICIENCY`      | `[0,1]`        | 0             | A movement speed multiplier that is applied when the entity is underwater.                                                                                            |
| `waypoint_transmit_range`        | `WAYPOINT_TRANSMIT_RANGE`        | `[0,60000000]` | 0             | The range at which an entity can transmit its location to some waypoint tracker. |
| `waypoint_receive_range`         | `WAYPOINT_RECEIVE_RANGE`         | `[0,60000000]` | 0             | The range at which an entity can receive another transmitter.                    |

:::warning
Some attribute caps are set relatively arbitrarily by Mojang. This is especially notable for armor, which is capped at 30. NeoForge doesn't touch those caps, however there are mods to change them.
:::

### NeoForge

The following attributes are in the `neoforge` namespace, and their in-code values can be found in the `NeoForgeMod` class.

| Name               | In Code            | Range      | Default Value | Usage                                                                                                                                                |
|--------------------|--------------------|------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `creative_flight`  | `CREATIVE_FLIGHT`  | `[0,1]`    | 0             | Determines whether creative flight for the entity is enabled (\> 0) or disabled (\<\= 0).                                                            |
| `nametag_distance` | `NAMETAG_DISTANCE` | `[0,32]`   | 32            | How far the nametag of the entity will be visible, in blocks.                                                                                        |
| `swim_speed`       | `SWIM_SPEED`       | `[0,1024]` | 1             | A movement speed multiplier that is applied when the entity is underwater. This is applied independently from `minecraft:water_movement_efficiency`. |

## Default Attributes

When creating a `LivingEntity`, it is required to register a set of default attributes for them. When an entity is [spawned][spawning] in, its default attributes are set on it. Default attributes are registered in the [`EntityAttributeCreationEvent`][event] like so:

```java
@SubscribeEvent // on the mod event bus
public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
    event.put(
        // Your entity type.
        MY_ENTITY.get(),
        // An AttributeSupplier. This is typically created by calling LivingEntity#createLivingAttributes,
        // setting your values on it, and calling #build. You can also create the AttributeSupplier from scratch
        // if you want, see the source of LivingEntity#createLivingAttributes for an example.
        LivingEntity.createLivingAttributes()
            // Add an attribute with its default value.
            .add(Attributes.MAX_HEALTH)
            // Add an attribute with a non-default value.
            .add(Attributes.MAX_HEALTH, 50)
            // Build the AttributeSupplier.
            .build()
    );
}
```

:::tip
Some classes have specialized versions of `LivingEntity#createLivingAttributes`. For example, the `Monster` class has a method named `Monster#createMonsterAttributes` that can be used instead.
:::

In some situations, for example when making [your own attributes][custom], it is needed to add attributes to an existing entity's `AttributeSupplier`. This is done through the `EntityAttributeModificationEvent` like so:

```java
@SubscribeEvent // on the mod event bus
public static void modifyDefaultAttributes(EntityAttributeModificationEvent event) {
    event.add(
        // The EntityType to add the attribute for.
        EntityType.VILLAGER,
        // The Holder<Attribute> to add to the EntityType. Can also be a custom attribute.
        Attributes.ARMOR,
        // The attribute value to add.
        // Can be omitted, if so, the attribute's default value will be used instead.
        10.0
    );
    // We can also check if a given EntityType already has a given attribute.
    // In this example, if villagers don't have the armor attribute already, we add it.
    if (!event.has(EntityType.VILLAGER, Attributes.ARMOR)) {
        event.add(...);
    }
}
```

Be aware that unlike some other registries, custom attributes existing do not block vanilla clients from connecting to a NeoForge server. If a vanilla client connects, it will only receive the attributes in the `minecraft` namespace.

## Querying Attributes

Attribute values are stored on entities in an `AttributeMap`, which is basically a `Map<Attribute, AttributeInstance>`. Attribute instances are basically what item stacks are to items, i.e. whereas an attribute is a registered singleton, attribute instances are concrete attribute objects bound to a concrete entity.

The `AttributeMap` of an entity can be retrieved by calling `LivingEntity#getAttributes`. You can then query the map like so:

```java
// Get the attribute map.
AttributeMap attributes = livingEntity.getAttributes();
// Get an attribute instance. This may be null if the entity does not have the attribute.
AttributeInstance instance = attributes.getInstance(Attributes.ARMOR);
// Get the value for an attribute. Will fallback to the default for the entity if needed.
double value = attributes.getValue(Attributes.ARMOR);
// Of course, we can also check if an attribute is present to begin with.
if (attributes.hasAttribute(Attributes.ARMOR)) { ... }

// Alternatively, LivingEntity also offers shortcuts:
AttributeInstance instance = livingEntity.getAttribute(Attributes.ARMOR);
double value = livingEntity.getAttributeValue(Attributes.ARMOR);
```

:::info
When handling attributes, you will almost exclusively use `Holder<Attribute>`s instead of `Attribute`s. This is also why with custom attributes (see below), we explicitly store the `Holder<Attribute>`.
:::

## Attribute Modifiers

In contrast to querying, changing the attribute values is not as easy. This is mainly because there may be multiple changes required to an attribute at the same time.

Consider this: You are a player, who has an attack damage attribute of 1. You wield a diamond sword, which does 6 extra attack damage, so you have 7 total attack damage. Then you drink a strength potion, adding a damage multiplier. You then also have some sort of trinket equipped that adds yet another multiplier.

To avoid miscalculations and to better communicate how the attribute values are modified, Minecraft introduces the attribute modifier system. In this system, every attribute has a **base value**, which is typically sourced from the default attributes we discussed earlier. We can then add any amount of **attribute modifiers** that can be individually removed again, without us having to worry about correctly applying operations.

To get started, let's create an attribute modifier:

```java
// The name of the modifier. This is later used to query the modifier from the attribute map
// and as such must be (semantically) unique.
Identifier id = Identifier.fromNamespaceAndPath("yourmodid", "my_modifier");
// The modifier itself.
AttributeModifier modifier = new AttributeModifier(
    // The name we defined earlier.
    id,
    // The amount by which we modify the attribute value.
    2.0,
    // The operation used to apply the modifier. Possible values are:
    // - AttributeModifier.Operation.ADD_VALUE: Adds the value to the total attribute value.
    // - AttributeModifier.Operation.ADD_MULTIPLIED_BASE: Multiplies the value with the attribute base value
    //   and adds it to the total attribute value.
    // - AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL: Multiplies the value with the total attribute value,
    //   i.e. the attribute base value with all previous modifications already performed,
    //   and adds it to the total attribute value.
    AttributeModifier.Operation.ADD_VALUE
);
```

Now, to apply the modifier, we have two options: add it as a transient modifier, or as a permanent modifier. Permanent modifiers are saved to disk, while transient modifiers are not. The use case for permanent modifiers is things like permanent stat bonuses (e.g. some sort of armor or health skill), while transient modifiers are mainly for [equipment], [mob effects][mobeffect] and other modifiers that depend on the player's current state.

```java
AttributeMap attributes = livingEntity.getAttributes();
// Add a transient modifier. If a modifier with the same id is already present, this will throw an exception.
attributes.getInstance(Attributes.ARMOR).addTransientModifier(modifier);
// Add a transient modifier. If a modifier with the same id is already present, it is removed first.
attributes.getInstance(Attributes.ARMOR).addOrUpdateTransientModifier(modifier);
// Add a permanent modifier. If a modifier with the same id is already present, this will throw an exception.
attributes.getInstance(Attributes.ARMOR).addPermanentModifier(modifier);
// Add a permanent modifier. If a modifier with the same id is already present, it is removed first.
attributes.getInstance(Attributes.ARMOR).addOrReplacePermanentModifier(modifier);
```

These modifiers can also be removed again:

```java
// Remove by modifier object.
attributes.getInstance(Attributes.ARMOR).removeModifier(modifier);
// Remove by modifier id.
attributes.getInstance(Attributes.ARMOR).removeModifier(id);
// Remove all modifiers for an attribute.
attributes.getInstance(Attributes.ARMOR).removeModifiers();
```

Finally, we can also query the attribute map for whether it has a modifier with a certain ID, as well as query base values and modifier values separately, like so:

```java
// Check for the modifier being present.
if (attributes.getInstance(Attributes.ARMOR).hasModifier(id)) { ... }
// Get the base armor attribute value.
double baseValue = attributes.getBaseValue(Attributes.ARMOR);
// Get the value of a certain modifier.
double modifierValue = attributes.getModifierValue(Attributes.ARMOR, id);
```

## Custom Attributes

If needed, you can also add your own attributes. Like many other systems, attributes are a [registry], and you can register your own objects to it. To get started, create a `DeferredRegister<Attribute>` like so:

```java
public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
    BuiltInRegistries.ATTRIBUTE, "yourmodid");
```

For the attributes themselves, there are three classes you can choose from:

- `RangedAttribute`: Used by most attributes, this class defines lower and upper bounds for the attribute, along with a default value.
- `PercentageAttribute`: Like `RangedAttribute`, but is displayed in percent instead of float values. NeoForge-added.
- `BooleanAttribute`: An attribute that only has semantic true (\> 0) and false (\<\= 0). This still uses doubles internally. NeoForge-added.

Using `RangedAttribute` as an example (the other two work similarly), registering an attribute would look like this:

```java
public static final Holder<Attribute> MY_ATTRIBUTE = ATTRIBUTES.register("my_attribute", () -> new RangedAttribute(
    // The translation key to use.
    "attributes.yourmodid.my_attribute",
    // The default value.
    0,
    // Min and max values.
    -10000,
    10000
));
```

And that's it! Just don't forget to register your `DeferredRegister` to the mod bus, and off you go.

:::info
We use `Holder<Attribute>` here instead of `Supplier<RangedAttribute>` like with many other registered objects, as it makes working with entities a lot easier (most entity methods expect `Holder<Attribute>`s).

If, for some reason, you need a `Supplier<RangedAttribute>` (or a supplier of any other subclass of `Attribute`), you should use `DeferredHolder<Attribute, RangedAttribute>` as the type.

The same rules also apply for any other `Attribute` subclass, i.e., we generally use `Holder<Attribute>` instead of `Supplier<PercentageAttribute>` or `Supplier<BooleanAttribute>`.
:::

[custom]: #custom-attributes
[equipment]: ../inventories/container.md#containers-on-entitys
[event]: ../concepts/events.md
[livingentity]: livingentity.md
[loottables]: ../resources/server/loottables/index.md
[miningspeed]: ../blocks/index.md#mining-speed
[mobeffect]: ../items/mobeffects.md
[registry]: ../concepts/registries.md
[spawning]: index.md#spawning-entities
[toughness]: https://minecraft.wiki/w/Armor#Armor_toughness
[wiki]: https://minecraft.wiki
