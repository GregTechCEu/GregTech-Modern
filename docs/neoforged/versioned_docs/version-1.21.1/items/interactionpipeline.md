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
        - If the `ItemInteractionResult` is `PASS_TO_DEFAULT_BLOCK_INTERACTION` and the executing hand is the main hand, then `Block#useWithoutItem` is called. If it returns a definitive result, the pipeline ends.
        - If the event does not deny item usage, `Item#useOn` is called. If it returns a definitive result, the pipeline ends.
- `Item#use` is called. If it returns a definitive result, the pipeline ends.
- The above process runs a second time, this time with the off hand instead of the main hand.

## Result Types

There are three different types of results: `InteractionResult`s, `ItemInteractionResult`s, and `InteractionResultHolder<T>`s. `InteractionResult` is used most of the time, only `Item#use` uses `InteractionResultHolder<ItemStack>`, and only `BlockBehaviour#useItemOn` and `CauldronInteraction#interact` use `ItemInteractionResult`.

`InteractionResult` is an enum consisting of five values: `SUCCESS`, `CONSUME`, `CONSUME_PARTIAL`, `PASS` and `FAIL`. Additionally, the method `InteractionResult#sidedSuccess` is available, which returns `SUCCESS` on the server and `CONSUME` on the client.

`InteractionResultHolder<T>` is a wrapper around `InteractionResult` that adds additional context for `T`. `T` can be anything, but in 99.99 percent of cases, it is an `ItemStack`. `InteractionResultHolder<T>` provides wrapper methods for the enum values (`#success`, `#consume`, `#pass` and `#fail`), as well as `#sidedSuccess`, which calls `#success` on the server and `#consume` on the client.

`ItemInteractionResult` is a parallel to `InteractionResult` specifically for when an item is used on a block. It is an enum of six values: `SUCCESS`, `CONSUME`, `CONSUME_PARTIAL`, `PASS_TO_DEFAULT_BLOCK_INTERACTION`, `SKIP_DEFAULT_BLOCK_INTERACTION`,  and `FAIL`. Each `ItemInteractionResult` can be mapped to a `InteractionResult` via `#result`; `PASS_TO_DEFAULT_BLOCK_INTERACTION`, `SKIP_DEFAULT_BLOCK_INTERACTION` both represent `InteractionResult#PASS`. Similarly, `#sidedSucess` also exists for `ItemInteractionResult`.

Generally, the different values mean the following:

- `InteractionResult#sidedSuccess` (or `InteractionResultHolder#sidedSuccess` / `ItemInteractionResult#sidedSucess` where needed) should be used if the operation should be considered successful, and you want the arm to swing. The pipeline will end.
- `InteractionResult#SUCCESS` (or `InteractionResultHolder#success` / `ItemInteractionResult#SUCCESS` where needed) should be used if the operation should be considered successful, and you want the arm to swing, but only on one side. Only use this if you want to return a different value on the other logical side for whatever reason. The pipeline will end.
- `InteractionResult#CONSUME` (or `InteractionResultHolder#consume` / `ItemInteractionResult#CONSUME` where needed) should be used if the operation should be considered successful, but you do not want the arm to swing. The pipeline will end.
- `InteractionResult#CONSUME_PARTIAL` is mostly identical to `InteractionResult#CONSUME`, the only difference is in its usage in [`Item#useOn`][itemuseon].
    - `ItemInteractionResult#CONSUME_PARTIAL` is similar within its usage in `BlockBehaviour#useItemOn`.
- `InteractionResult.FAIL` (or `InteractionResultHolder#fail` / `ItemInteractionResult#FAIL` where needed) should be used if the item functionality should be considered failed and no further interaction should be performed. The pipeline will end. This can be used everywhere, but it should be used with care outside of `Item#useOn` and `Item#use`. In many cases, using `InteractionResult.PASS` makes more sense.
- `InteractionResult.PASS` (or `InteractionResultHolder#pass` where needed) should be used if the operation should be considered neither successful nor failed. The pipeline will continue. This is the default behavior (unless otherwise specified).
    - `ItemInteractionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION` allows `BlockBehaviour#useWithoutItem` to be called for the mainhand while `#SKIP_DEFAULT_BLOCK_INTERACTION` prevents the method from executing altogether. `#PASS_TO_DEFAULT_BLOCK_INTERACTION` is the default behavior (unless otherwise specified).

Some methods have special behavior or requirements, which are explained in the below chapters.

## `IItemExtension#onItemUseFirst`

`InteractionResult#sidedSuccess` and `InteractionResult.CONSUME` don't have an effect here. Only `InteractionResult.SUCCESS`, `InteractionResult.FAIL` or `InteractionResult.PASS` should be used here.

## `Item#useOn`

If you want the operation to be considered successful, but you do not want the arm to swing or an `ITEM_USED` stat point to be awarded, use `InteractionResult.CONSUME_PARTIAL`.

## `Item#use`

This is the only instance where the return type is `InteractionResultHolder<ItemStack>`. The resulting `ItemStack` in the `InteractionResultHolder<ItemStack>` replaces the `ItemStack` the usage was initiated with, if it has changed.

The default implementation of `Item#use` returns `InteractionResultHolder#consume` when the item is edible and the player can eat the item (because they are hungry, or because the item is always edible), `InteractionResultHolder#fail` when the item is edible but the player cannot eat the item, and `InteractionResultHolder#pass` if the item is not edible.

Returning `InteractionResultHolder#fail` here while considering the main hand will prevent offhand behavior from running. If you want offhand behavior to run (which you usually want), return `InteractionResultHolder#pass` instead.

[itemuseon]: #itemuseon
