---
sidebar_position: 1
---
# The Interaction Pipeline

This page aims to make the fairly complex and confusing process of things being right-clicked by the player more understandable, as well as clarifying what result to use where and why.

## What Happens When I Right-Click?

When you right-click anywhere in the world, a number of things happen, depending on what you are currently looking at and what `ItemStack`s are in your hands. A number of methods returning one of two result types (see below) are called. Most of these methods cancel the pipeline if an explicit success or an explicit failure is returned. For the sake of readability, this "explicit success or explicit failure" will be called a "definitive result" from now on.

- `InputEvent.InteractionKeyMappingTriggered` is fired with the right mouse button and the main hand. If the event is canceled, the pipeline ends.
- Several circumstances are checked, for example that you are not in spectator mode or that all required feature flags for the `ItemStack` in your main hand are enabled. If at least one of these checks fails, the pipeline ends.
- Depending on what you are looking at, different things happen:
    - If you are looking at an entity that is within your reach and not outside the world border:
        - `PlayerInteractEvent.EntityInteractSpecific` is fired. If the event is canceled, the pipeline ends.
        - `Entity#interactAt` will be called **on the entity you are looking at**. If it returns a definitive result, the pipeline ends.
            - If you want to add behavior for your own entity, override this method. If you want to add behavior for a vanilla entity, use the event.
        - If the entity opens an interface (for example a villager trading GUI or a chest minecart GUI), the pipeline ends.
        - `PlayerInteractEvent.EntityInteract` is fired. If the event is canceled, the pipeline ends.
        - `Entity#interact` is called **on the entity you are looking at**. If it returns a definitive result, the pipeline ends.
            - If you want to add behavior for your own entity, override this method. If you want to add behavior for a vanilla entity, use the event.
            - For `Mob`s, the override of `Entity#interact` handles things like leashing and spawning babies when the `ItemStack` in your main hand is a spawn egg, and then defers mob-specific handling to `Mob#mobInteract`. The rules for results for `Entity#interact` apply here as well.
        - If the entity you are looking at is a `LivingEntity`, `Item#interactLivingEntity` is called on the `ItemStack` in your main hand. If it returns a definitive result, the pipeline ends.
    - If you are looking at a block that is within your reach and not outside the world border:
        - `PlayerInteractEvent.RightClickBlock` is fired. If the event is canceled, the pipeline ends. You may also specifically deny only block or item usage in this event.
        - `IItemExtension#onItemUseFirst` is called. If it returns a definitive result, the pipeline ends.
        - If the player is not sneaking and the event does not deny block usage, `UseItemOnBlockEvent` is fired. If the event is canceled, the cancellation result is used. Otherwise, `Block#useItemOn` is called. If it returns a definitive result, the pipeline ends.
        - If the `InteractionResult` is `TRY_WITH_EMPTY_HAND` and the executing hand is the main hand, then `Block#useWithoutItem` is called. If it returns a definitive result, the pipeline ends.
        - If the event does not deny item usage, `Item#useOn` is called. If it returns a definitive result, the pipeline ends.
- `Item#use` is called. If it returns a definitive result, the pipeline ends.
- The above process runs a second time, this time with the off hand instead of the main hand.

## `InteractionResult`

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

## `Item#useOn`

If you want the operation to be considered successful, but you do not want the arm to swing or an `ITEM_USED` stat point to be awarded, use `InteractionResult#CONSUME` and calling `#withoutItem`.

```java
// In Item#useOn
return InteractionResult.CONSUME.withoutItem();
```

## `Item#use`

This is the only instance where the transformed `ItemStack` is used from a `Success` variant (`SUCCESS`, `SUCCESS_SERVER`, `CONSUME`). The resulting `ItemStack` set by `Success#heldItemTransformedTo` replaces the `ItemStack` the usage was initiated with, if it has changed.

The default implementation of `Item#use` returns `InteractionResult#CONSUME` when the item is edible (has `DataComponents#CONSUMABLE`) and the player can eat the item (because they are hungry, or because the item is always edible) and `InteractionResult#FAIL` when the item is edible (has `DataComponents#CONSUMABLE`) but the player cannot eat the item. If the item is equippable (has `DataComponents#EQUIPPABLE`), then it returns `InteractionResult#SUCCESS` on swap with the held item replaced by the swaped item (via `heldItemTransformedTo`), or `InteractionResult#FAIL` if the enchantment on the armor has the `EnchantmentEffectComponents#PREVENT_ARMOR_CHANGE` component. Otherwise `InteractionResult#PASS` is returned.

Returning `InteractionResult#FAIL` here while considering the main hand will prevent offhand behavior from running. If you want offhand behavior to run (which you usually want), return `InteractionResult#PASS` instead.

[itemuseon]: #itemuseon
[side]: ../concepts/sides.md#the-logical-side
