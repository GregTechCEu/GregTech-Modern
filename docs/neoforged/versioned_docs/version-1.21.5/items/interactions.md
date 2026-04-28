---
sidebar_position: 1
---
# Interactions

This page aims to make the fairly complex and confusing process of things being left-clicked, right-clicked or middle-clicked by the player more understandable, as well as clarifying what result to use where and why.

## `HitResult`s

For the purpose of determining what the player is currently looking at, Minecraft uses a `HitResult`. A `HitResult` is somewhat equivalent to a ray cast result in other game engines, and most notably contains a method `#getLocation`.

A hit result can be of one of three types, represented through the `HitResult.Type` enum: `BLOCK`, `ENTITY`, or `MISS`. A `HitResult` of type `BLOCK` can be cast to `BlockHitResult`, while a `HitResult` of type `ENTITY` can be cast to `EntityHitResult`; both types provide additional context about what [block] or [entity] was hit. If the type is `MISS`, this indicates that neither a block nor an entity was hit, and should not be cast to either subclass.

Every frame on the [physical client][physicalside], the `Minecraft` class updates and stores the currently looked-at `HitResult` in the `hitResult` field. This field can then be accessed through `Minecraft.getInstance().hitResult`.

## Left-Clicking an Item

- It is checked that all required [feature flags][featureflag] for the [`ItemStack`][itemstack] in your main hand are enabled. If this check fails, the pipeline ends.
- `InputEvent.InteractionKeyMappingTriggered` is fired with the left mouse button and the main hand. If the [event][event] is [canceled][cancel], the pipeline ends.
- Depending on what you are looking at (using the [`HitResult`][hitresult] in `Minecraft`), different things happen:
    - If you are looking at an [entity] that is within your reach:
        - `AttackEntityEvent` is fired. If the event is canceled, the pipeline ends.
        - `IItemExtension#onLeftClickEntity` is called. If it returns true, the pipeline ends.
        - `Entity#isAttackable` is called on the target. If it returns false, the pipeline ends.
        - `Entity#skipAttackInteraction` is called on the target. If it returns true, the pipeline ends.
        - If the target is in the `minecraft:redirectable_projectile` tag (by default this is fireballs and wind charges) and an instance of `Projectile`, the target is deflected and the pipeline ends.
        - Entity base damage (the value of the `minecraft:generic.attack_damage` [attribute]) and enchantment bonus damage are calculated as two separate floats. If both are 0, the pipeline ends.
            - Note that this excludes [attribute modifiers][attributemodifier] from the main hand item, these are added after the check.
        - `minecraft:generic.attack_damage` attribute modifiers from the main hand item are added to the base damage.
        - `CriticalHitEvent` is fired. If the event's `#isCriticalHit` method returns true, the base damage is multiplied with the value returned from the event's `#getDamageMultiplier` method, which defaults to 1.5 if [a number of conditions][critical] pass and 1.0 otherwise, but may be modified by the event.
        - Enchantment bonus damage is added to the base damage, resulting in the final damage value.
        - [`Entity#hurt`][hurt] is called. If it returns false, the pipeline ends.
        - If the target is an instance of `LivingEntity`, `LivingEntity#knockback` is called.
            - Within that method, `LivingKnockBackEvent` is fired.
        - If the attack cooldown is > 90%, the attack is not a critical hit, the player is on the ground and not moving faster than their `minecraft:generic.movement_speed` attribute value, a sweep attack is performed on nearby `LivingEntity`s.
            - Within that method, `LivingEntity#knockback` is called again, which in turn fires `LivingKnockBackEvent` a second time.
        - `Item#hurtEnemy` is called. This can be used for post-attack effects. For example, the mace launches the player back in the air here, if applicable.
        - `Item#postHurtEnemy` is called. Durability damage is applied here.
    - If you are looking at a [block] that is within your reach:
        - The [block breaking sub-pipeline][blockbreak] is initiated.
    - Otherwise:
        - `PlayerInteractEvent.LeftClickEmpty` is fired.

## Right-Clicking an Item

During the right-clicking pipeline, a number of methods returning one of two result types (see below) are called. Most of these methods cancel the pipeline if an explicit success or an explicit failure is returned. For the sake of readability, this "explicit success or explicit failure" will be called a "definitive result" from now on.

- `InputEvent.InteractionKeyMappingTriggered` is fired with the right mouse button and the main hand. If the [event][event] is [canceled][cancel], the pipeline ends.
- Several circumstances are checked, for example that you are not in spectator mode or that all required [feature flags][featureflag] for the [`ItemStack`][itemstack] in your main hand are enabled. If at least one of these checks fails, the pipeline ends.
- Depending on what you are looking at (using the [`HitResult`][hitresult] in `Minecraft`), different things happen:
    - If you are looking at an [entity] that is within your reach and not outside the world border:
        - `PlayerInteractEvent.EntityInteractSpecific` is fired. If the event is canceled, the pipeline ends.
        - `Entity#interactAt` will be called **on the entity you are looking at**. If it returns a definitive result, the pipeline ends.
            - If you want to add behavior for your own entity, override this method. If you want to add behavior for a vanilla entity, use the event.
        - If the entity opens an interface (for example a villager trading GUI or a chest minecart GUI), the pipeline ends.
        - `PlayerInteractEvent.EntityInteract` is fired. If the event is canceled, the pipeline ends.
        - `Entity#interact` is called **on the entity you are looking at**. If it returns a definitive result, the pipeline ends.
            - If you want to add behavior for your own entity, override this method. If you want to add behavior for a vanilla entity, use the event.
            - For [`Mob`s][livingentity], the override of `Entity#interact` handles things like leashing and spawning babies when the `ItemStack` in your main hand is a spawn egg, and then defers mob-specific handling to `Mob#mobInteract`. The rules for results for `Entity#interact` apply here as well.
        - If the entity you are looking at is a `LivingEntity`, `Item#interactLivingEntity` is called on the `ItemStack` in your main hand. If it returns a definitive result, the pipeline ends.
    - If you are looking at a [block] that is within your reach and not outside the world border:
        - `PlayerInteractEvent.RightClickBlock` is fired. If the event is canceled, the pipeline ends. You may also specifically deny only block or item usage in this event.
        - `IItemExtension#onItemUseFirst` is called. If it returns a definitive result, the pipeline ends.
        - If the player is not sneaking and the event does not deny block usage, `UseItemOnBlockEvent` is fired. If the event is canceled, the cancellation result is used. Otherwise, `Block#useItemOn` is called. If it returns a definitive result, the pipeline ends.
        - If the `InteractionResult` is `TRY_WITH_EMPTY_HAND` and the executing hand is the main hand, then `Block#useWithoutItem` is called. If it returns a definitive result, the pipeline ends.
        - If the event does not deny item usage, `Item#useOn` is called. If it returns a definitive result, the pipeline ends.
- `Item#use` is called. If it returns a definitive result, the pipeline ends.
- The above process runs a second time, this time with the off hand instead of the main hand.

### `InteractionResult`

`InteractionResult` is a sealed interface that respresents the result of some interaction between an item or an empty hand and some object (e.g. entities, blocks, etc.). The interface is broken into four records, where there are six potential default states.

First there is `InteractionResult.Success`, which indicates that the operation should be considered sucessful, ending the pipeline. A successful state has two parameters: the `SwingSource`, which indicates whether the entity should swing on the respective [logical side][side]; and the `InteractionResult.ItemContext`, which holds whether the interaction was caused by a held item, and what the held item transformed into after use. The swing source is determined by one of the default states: `InteractionResult#SUCCESS` for client swing, `InteractionResult#SUCCESS_SERVER` for server swing, and `InteractionResult#CONSUME` for no swing. The item context is set via `Success#heldItemTransformedTo` if the `ItemStack` changed, or `withoutItem` if there wasn't an interaction between the held item and the object. The default sets there was an item interaction but no transformation.

```java
// In some method that returns an interaction result

// Item in hand will turn into an apple
return InteractionResult.SUCCESS.heldItemTransformedTo(new ItemStack(Items.APPLE));
```

:::note
`SUCCESS` and `SUCCESS_SERVER` should generally never be used in the same method. If the client has enough information to determine when to swing, then `SUCCESS` should always be used. Otherwise, if it relies on server information not present on the client, `SUCCESS_SERVER` should be used.
:::

Then there is `InteractionResult.Fail`, implemented by `InteractionResult#FAIL`, which indicates that the operation should be considered failed, allowing no further interaction to occur. The pipeline will end. This can be used anywhere, but it should be used with care outside of `Item#useOn` and `Item#use`. In many cases, using `InteractionResult#PASS` makes more sense.

Finally, there is `InteractionResult.Pass` and `InteractionResult.TryWithEmptyHandInteraction`, implemented by `InteractionResult#PASS` and `InteractionResult#TRY_WITH_EMPTY_HAND` respectively. These records indicate when an operation should be considered neither successful or failed, and the pipeline should continue. `PASS` is the default behavior for all `InteractionResult` methods except `BlockBehaviour#useItemOn`, which returns `TRY_WITH_EMPTY_HAND`. More specifically, if `BlockBehaviour#useItemOn` returns anything but `TRY_WITH_EMPTY_HAND`, `BlockBehaviour#useWithoutItem` will not be called regardless of if the item is in the main hand.

Some methods have special behavior or requirements, which are explained in the below chapters.

#### `Item#useOn`

If you want the operation to be considered successful, but you do not want the arm to swing or an `ITEM_USED` stat point to be awarded, use `InteractionResult#CONSUME` and calling `#withoutItem`.

```java
// In Item#useOn
return InteractionResult.CONSUME.withoutItem();
```

#### `Item#use`

This is the only instance where the transformed `ItemStack` is used from a `Success` variant (`SUCCESS`, `SUCCESS_SERVER`, `CONSUME`). The resulting `ItemStack` set by `Success#heldItemTransformedTo` replaces the `ItemStack` the usage was initiated with, if it has changed.

The default implementation of `Item#use` returns `InteractionResult#CONSUME` when the item is edible (has `DataComponents#CONSUMABLE`) and the player can eat the item (because they are hungry, or because the item is always edible) and `InteractionResult#FAIL` when the item is edible (has `DataComponents#CONSUMABLE`) but the player cannot eat the item. If the item is equippable (has `DataComponents#EQUIPPABLE`), then it returns `InteractionResult#SUCCESS` on swap with the held item replaced by the swaped item (via `heldItemTransformedTo`), or `InteractionResult#FAIL` if the enchantment on the armor has the `EnchantmentEffectComponents#PREVENT_ARMOR_CHANGE` component. Otherwise `InteractionResult#PASS` is returned.

Returning `InteractionResult#FAIL` here while considering the main hand will prevent offhand behavior from running. If you want offhand behavior to run (which you usually want), return `InteractionResult#PASS` instead.

## Middle-Clicking

- If the [`HitResult`][hitresult] in `Minecraft.getInstance().hitResult` is null or of type `MISS`, the pipeline ends.
- `InputEvent.InteractionKeyMappingTriggered` is fired with the left mouse button and the main hand. If the [event][event] is [canceled][cancel], the pipeline ends.
- Depending on what you are looking at (using the `HitResult` in `Minecraft.getInstance().hitResult`), different things happen:
    - If you are looking at an [entity] that is within your reach:
        - If `Entity#isPickable` returns false, the pipeline ends.
        - If you are not in creative, the pipeline ends.
        - `IEntityExtension#getPickedResult` is called. The resulting `ItemStack` is added to the player's inventory.
            - By default, this method forwards to `Entity#getPickResult`, which can be overridden by modders.
    - If you are looking at a [block] that is within your reach:
        - `Block#getCloneItemStack` is called and becomes the "selected" `ItemStack`.
            - By default, this returns the `Item` representation of the `Block`.
        - If the Control key is held down, the player is in creative and the targeted block has a [`BlockEntity`][blockentity], the `BlockEntity`'s data is added to the "selected" `ItemStack`.
        - If the player is in creative, the "selected" `ItemStack` is added to the player's inventory. Otherwise, a hotbar slot that matches the "selected" item is set active, if such a hotbar slot exists.

[attribute]: ../entities/attributes.md
[attributemodifier]: ../entities/attributes.md#attribute-modifiers
[block]: ../blocks/index.md
[blockbreak]: ../blocks/index.md#breaking-a-block
[blockentity]: ../blockentities/index.md
[cancel]: ../concepts/events.md#cancellable-events
[critical]: https://minecraft.wiki/w/Damage#Critical_hit
[effect]: mobeffects.md
[entity]: ../entities/index.md
[event]: ../concepts/events.md
[featureflag]: ../advanced/featureflags.md
[hitresult]: #hitresults
[hurt]: ../entities/index.md#damaging-entities
[itemstack]: index.md#itemstacks
[itemuseon]: #itemuseon
[livingentity]: ../entities/livingentity.md
[physicalside]: ../concepts/sides.md#the-physical-side
[side]: ../concepts/sides.md#the-logical-side
