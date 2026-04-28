---
sidebar_position: 3
---
# Consumables

Consumables are [items][item] which can be used over a period of time, 'consuming' them in the process. Anything that can be eaten or drunk in Minecraft is a consumable of some kind.

## The `Consumable` Data Component

Any item that can be consumed has the [`DataComponents#CONSUMABLE` component][datacomponent]. The backing record `Consumable` defines how the item is consumed and what effects to apply after consumption.

A `Consumable` can be created either by directly calling the record constructor or via `Consumable#builder`, which sets the defaults for each field, followed by `build` once finished:

- `consumeSeconds` - A `float` representing the number of seconds needed to fully consume the item. `Item#finishUsingItem` is called after the alloted time passes. Defaults to 1.6 seconds, or 32 ticks.
- `animation` - Sets the [`ItemUseAnimation`][animation] to play while the item is being used. Defaults to `ItemUseAnimation#EAT`.
- `sound` - Sets the [`SoundEvent`][sound] to play while consuming the item. This must be a `Holder` instance. Defaults to `SoundEvents#GENERIC_EAT`.
    - If a vanilla instance is not a `Holder<SoundEvent>`, a `Holder` wrapped version can be obtained by calling `BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)`.
- `soundAfterConsume` - Sets the [`SoundEvent`][sound] to player once the item has finished being consumed. This delegates to the [`PlaySoundConsumeEffect`][consumeeffect].
- `hasConsumeParticles` - When `true`, spawns item [particles] every four ticks and once the item is fully consumed. Defauts to `true`.
- `onConsume` - Adds a [`ConsumeEffect`][consumeeffect] to apply once the item has fully been consumed via `Item#finishUsingItem`.

Vanilla provides some consumables within their `Consumables` class, such as `#defaultFood` for [food] items and `#defaultDrink` for [potions] and milk buckets.

The `Consumable` component can be added by calling `Item.Properties#component`:

```java
// Assume there is some DeferredRegister.Items ITEMS
public static final DeferredItem<Item> CONSUMABLE = ITEMS.registerSimpleItem(
    "consumable",
    new Item.Properties().component(
        DataComponents.CONSUMABLE,
        Consumable.builder()
            // Spend 2 seconds, or 40 ticks, to consume
            .consumeSeconds(2f)
            // Sets the animation to play while consuming
            .animation(ItemUseAnimation.BLOCK)
            // Play sound while consuming every tick
            .sound(SoundEvents.ARMOR_EQUIP_CHAIN)
            // Play sound once finished consuming
            .soundAfterConsume(SoundEvents.BREEZE_WIND_CHARGE_BURST)
            // Don't show particles while eating
            .hasConsumeParticles(false)
            .onConsume(
                // When finished consuming, applies the effects with a 30% chance
                new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F)
            )
            // Can have multiple
            .onConsume(
                // Teleports the entity randomly in a 50 block radius
                new TeleportRandomlyConsumeEffect(100f)
            )
            .build()
    )
);
```

### `ConsumeEffect`

When a consumable has finished being used, you may want to trigger some kind of logic to execute like adding a potion effect. These are handled by `ConsumeEffect`s, which are added to the `Consumable` by calling `Consumable.Builder#onConsume`.

A list of vanilla effects can be found in `ConsumeEffect`.

Every `ConsumeEffect` has two methods: `getType`, which specifies the registry object `ConsumeEffect.Type`; and `apply`, which is called on the item when it has been fully consumed. `apply` takes three arguments: the `Level` the consuming entity is in, the `ItemStack` the consumable was called on, and the `LivingEntity` consuming the object. When the effect is successfully applied, the method returns `true`, or `false` if it failed.

A `ConsumeEffect` can be created by implementing the interface and [registering] the `ConsumeEffect.Type` with the associated `MapCodec` and `StreamCodec` to `BuiltInRegistries#CONSUME_EFFECT_TYPE`:

```java
public record UsePortalConsumeEffect(ResourceKey<Level> level)
    implements ConsumeEffect, Portal {

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, entity.blockPosition());

            // Can successfully use portal
            return true;
        }

        // Cannot use portal
        return false;
    }

    @Override
    public ConsumeEffect.Type<? extends ConsumeEffect> getType() {
        // Set to registered object
        return USE_PORTAL.get();
    }

    @Override
    @Nullable
    public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        // Set teleport location
    }
}

// In some registrar class
// Assume there is some DeferredRegister<ConsumeEffect.Type<?>> CONSUME_EFFECT_TYPES
public static final Supplier<ConsumeEffect.Type<UsePortalConsumeEffect>> USE_PORTAL =
    CONSUME_EFFECT_TYPES.register("use_portal", () -> new ConsumeEffect.Type<>(
        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension")
            .xmap(UsePortalConsumeEffect::new, UsePortalConsumeEffect::level),
        ResourceKey.streamCodec(Registries.DIMENSION)
            .map(UsePortalConsumeEffect::new, UsePortalConsumeEffect::level)
    ));

// For some Item.Properties that is adding a CONSUMABLE component
Consumable.builder()
    .onConsume(
        new UsePortalConsumeEffect(Level.END)
    )
    .build();
```

### `ItemUseAnimation`

`ItemUseAnimation` is functionally an enum which doesn't define anything besides its id and name. Its uses are hardcoded into `ItemHandRenderer#renderArmWithItem` for first person and `PlayerRenderer#getArmPose` for third person. As such, simply creating a new `ItemUseAnimation` will only function similarly to `ItemUseAnimation#NONE`.

To apply some animation, you need to implement `IClientItemExtensions#applyForgeHandTransform` for first person and/or `IClientItemExtensions#getArmPose` for third person rendering.

#### Creating the `ItemUseAnimation`

First, let's create a new `ItemUseAnimation`. This is done using the [extensible enum][extensibleenum] system:

```json5
{
    "entries": [
        {
            "enum": "net/minecraft/world/item/ItemUseAnimation",
            "name": "EXAMPLEMOD_ITEM_USE_ANIMATION",
            "constructor": "(ILjava/lang/String;)V",
            "parameters": [
                // The id, should always be -1
                -1,
                // The name, should be a unique identifier
                "examplemod:item_use_animation"
            ]
        }
    ]
}
```

Then we can get the enum constant via `valueOf`:

```java
public static final ItemUseAnimation EXAMPLE_ANIMATION = ItemUseAnimation.valueOf("EXAMPLEMOD_ITEM_USE_ANIMATION");
```

From there, we can then start applying the transforms. To do this, we must create a new `IClientItemExtensions`, implement our desired methods, and register it via `RegisterClientExtensionsEvent` on the [**mod event bus**][modbus]:

```java
public class ConsumableClientItemExtensions implements IClientItemExtensions {
    // Implement methods here
}

// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
    event.registerItem(
        // The instance of the item extensions
        new ConsumableClientItemExtensions(),
        // A vararg of items that use this
        CONSUMABLE
    )
}
```

#### First Person

The first person transform, which all consumables have, is implemented via `IClientItemExtensions#applyForgeHandTransform`:

```java
public class ConsumableClientItemExtensions implements IClientItemExtensions {

    // ...

    @Override
    public boolean applyForgeHandTransform(
        PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
        float partialTick, float equipProcess, float swingProcess
    ) {
        // We first need to check if the item is being used and has our animation
        HumanoidArm usingArm = entity.getUsedItemHand() == InteractionHand.MAIN_HAND
            ? entity.getMainArm()
            : entity.getMainArm().getOpposite();
        if (
            entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0
            && usingArm == arm && itemInHand.getUseAnimation() == EXAMPLE_ANIMATION
        ) {
            // Apply transformations to pose stack (translate, scale, mulPose)
            // ...
            return true;
        }

        // Do nothing
        return false;
    }
}
```

#### Third Person

The third person transforms, which all but `EAT` and `DRINK` have special logic for, is implemented via `IClientItemExtensions#getArmPose`, where `HumanoidModel.ArmPose` can also be extended for a custom transform.

As an `ArmPose` requries a lambda as part of its constructor, an `EnumProxy` reference must be used:

```json5
{
    "entries": [
        {
            "name": "EXAMPLEMOD_ITEM_USE_ANIMATION",
            // ...
        },
        {
            "enum": "net/minecraft/client/model/HumanoidModel$ArmPose",
            "name": "EXAMPLEMOD_ARM_POSE",
            "constructor": "(ZLnet/neoforged/neoforge/client/IArmPoseTransformer;)V",
            "parameters": {
                // Point to class where the proxy is located
                // Should be separate as this is a client only class
                "class": "example/examplemod/client/MyClientEnumParams",
                // The field name of the enum proxy
                "field": "CUSTOM_ARM_POSE"
            }
        }
    ]
}
```

```java
// Create the enum parameters
public class MyClientEnumParams {
    public static final EnumProxy<HumanoidModel.ArmPose> CUSTOM_ARM_POSE = new EnumProxy<>(
        HumanoidModel.ArmPose.class,
        // Whether the pose uses both arms
        false,
        // The pose transformer
        (IArmPoseTransformer) MyClientEnumParams::applyCustomModelPose
    );

    private static void applyCustomModelPose(
        HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm
    ) {
        // Apply model transforms here
        // ...
    }
}

// In some client only class
public static final HumanoidModel.ArmPose EXAMPLE_POSE = HumanoidModel.ArmPose.valueOf("EXAMPLEMOD_ARM_POSE");
```

Then, the arm pose is set via `IClientItemExtensions#getArmPose`:

```java
public class ConsumableClientItemExtensions implements IClientItemExtensions {

    // ...

    @Override
    public HumanoidModel.ArmPose getArmPose(
        LivingEntity entity, InteractionHand hand, ItemStack stack
    ) {
        // We first need to check if the item is being used and has our animation
        if (
            entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0
            && entity.getUsedItemHand() == hand
            && itemInHand.getUseAnimation() == EXAMPLE_ANIMATION
        ) {
            // Return pose to apply
            return EXAMPLE_POSE;
        }

        // Otherwise return null
        return null;
    }
}
```

### Overriding Sounds on Entity

Sometimes, an entity may want to play a different sound while consuming an item. In those instances, the [`LivingEntity`][livingentity] instance can implement `Consumable.OverrideConsumeSound` and have `getConsumeSound` return the `SoundEvent` they want their entity to play.

```java
public class MyEntity extends LivingEntity implements Consumable.OverrideConsumeSound {
    
    // ...

    @Override
    public SoundEvent getConsumeSound(ItemStack stack) {
        // Return the sound to play
    }
}
```

## `ConsumableListener`

While consumables and effects that are applied after consumption are useful, sometimes the properties of an effect need to be externally available as other [data components][datacomponents]. For example, cats and wolves also eat [food] and query its nutrition, or item with potion contents query its color for rendering. In these instances, data components implement `ConsumableListener` to provide consumption logic.

A `ConsumableListener` only has one method: `#onConsume`, which takes in the current level, the entity consuming the item, the item being consumed, and the `Consumable` instance on the item. `onConsume` is called during `Item#finishUsingItem` when the item has been fully consumed.

Adding your own `ConsumableListener` is simply [registering a new data component][datacompreg] and implementing `ConsumableListener`.

```java
public record MyConsumableListener() implements ConsumableListener {

    @Override
    public void onConsume(
        Level level, LivingEntity entity, ItemStack stack, Consumable consumable
    ) {
        // Do things here
    }
}
```

### Food

Food is one type of `ConsumableListener` that is part of the hunger system. All of the functionality for food items is already handled within the `Item` class, so simply adding the `FoodProperties` to `DataComponents#FOOD` along with a consumable is all that's needed. There is a helper method called `food` which takes in the `FoodProperties` and the `Consumable` object, or `Consumables#DEFAULT_FOOD` if none is specified.

`FoodProperties` can be created either by directly calling the record constructor or via `new FoodProperties.Builder()`, followed by `build` once finished:

- `nutrition` - Sets how many hunger points are restored. Counts in half hunger points, so for example, Minecraft's steak restores 8 hunger points.
- `saturationModifier` - The saturation modifier used in calculating the [saturation value][hunger] restored when eating this food. The calculation is `min(2 * nutrition * saturationModifier, playerNutrition)`, meaning that using `0.5` will make the effective saturation value the same as the nutrition value.
- `alwaysEdible` - Whether this item can always be eaten, even if the hunger bar is full. `false` by default, `true` for golden apples and other items that provide bonuses beyond just filling the hunger bar.

```java
// Assume there is some DeferredRegister.Items ITEMS
public static final DeferredItem<Item> FOOD = ITEMS.registerSimpleItem(
    "food",
    new Item.Properties().food(
        new FoodProperties.Builder()
            // Heals 1.5 hearts
            .nutrition(3)
            // Carrot is 0.3
            // Raw Cod is 0.1
            // Cooked Chicken is 0.6
            // Cooked Beef is 0.8
            // Golden Aple is 1.2
            .saturationModifier(0.3f)
            // When set, the food can alway be eaten even with
            //  a full hunger bar.
            .alwaysEdible()
    )
);
```

For examples, or to look at the various values used by Minecraft, have a look at the `Foods` class.

To get the `FoodProperties` for an item, call `ItemStack.get(DataComponents.FOOD)`. This may return null, since not every item is edible. To determine whether an item is edible, null-check the result of the `getFoodProperties` call.

### Potion Contents

The contents of a [potion][potions] via `PotionContents` is another `ConsumableListener` whose effects are applied on consumption. They contain an optional potion to apply, an optional tint for the potion color, a list of custom [`MobEffectInstance`s][mobeffectinstance] to apply alongside the potion, and an optional translation key to use when getting the stack name. The modder needs to override `Item#getName` if not a subtype of `PotionItem`.

[animation]: #itemuseanimation
[consumeeffect]: #consumeeffect
[datacomponent]: datacomponents.md
[datacompreg]: datacomponents.md#creating-custom-data-components
[extensibleenum]: ../advanced/extensibleenums.md
[food]: #food
[hunger]: https://minecraft.wiki/w/Hunger#Mechanics
[item]: index.md
[livingentity]: ../entities/livingentity.md
[modbus]: ../concepts/events.md#event-buses
[mobeffectinstance]: mobeffects.md#mobeffectinstances
[particles]: ../resources/client/particles.md
[potions]: mobeffects.md#potions
[sound]: ../resources/client/sounds.md#creating-soundevents
[registering]: ../concepts/registries.md#methods-for-registering
