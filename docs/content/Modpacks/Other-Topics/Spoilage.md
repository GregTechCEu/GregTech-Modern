---
title: Spoilage
---

**Spoilage** is a mechanic that allows items to *spoil*.<br>
Spoilable items spoil based on the amount of ticks that passed from their creation,
or, more specifically, from one of these events (due to Minecraft's limitations):

- The item was in an `IItemHandler` that is a capability of a `BlockEntity` that had `Level#getBlockEntity` called
- The item was crafted in a GregTech recipe
- The item was crafted in a crafting table
- The item was in a player's inventory for at least 1 tick
- The item was dropped
- `SpoilUtils.update(ItemStack, SpoilContext)` was called

If you want to make an item spoil, you need to attack the `ISpoilableItem` capability to it.
Please note that the spoilage timer still decrements even if the stack is in an unloaded chunk.

### SpoilContext
A `SpoilContext` is an object that represents the environment in which an item spoils.
It may represent:

- Nothing (all values are null, obtained by just calling the constructor without arguments)
- A block (contains `Level` and `BlockPos`)
- A block and an item handler (contains `Level`, `BlockPos`, `IItemHandler` and the number of the slot in that `IItemHandler`, that may be `-1`)
- An entity, usually a player (contains `Entity` and the number of the slot in that entity's inventory, that may be `-1`)

This info is used to spawn entities when the item spoils, or do something more complex.
The `SpoilableBehavior.builder()` can accept a function as a `result`, so you can do whatever you want there :)

### SpoilableBehavior
`SpoilableBehavior` is a helper class used to make items spoilable.

`SpoilableBehavior` builders can be obtained from a `RegisterSpoilablesEvent` and are used to create behaviors, currently they have the following methods:

- `.ticks(long)`
    used to specify ticks until spoiled
- `.ticks(Function<ItemStack, Long>)`
    used to specify a ticks until spoiled value that may depend on the stack itself, for example having more items in the stack may make it spoil slower
- `.result(ItemLike)`
    used to specify the resulting item
- `.result(ItemStack)`
    used to specify the resulting stack (may have NBT, but not count)
- `.result(Function<ItemStack, ItemStack>)`
    used to specify the resulting stack that may depend on the original stack
- `.result(EntityType<? extends Mob>)`
    used to specify the mob into which the item will spoil (it can still spoil into an item as well, but the item has to be specified first)
- `.result(Supplier<? extends EntityType<? extends Mob>>`
    same as `.result(EntityType<? extends Mob>)`, exists for convenience
- `.result(SpoilResultProvider)`
    used to specify a result function ((`ItemStack`, `SpoilContext`, `simulate`) -> `ItemStack`) for custom spoiling logic
- `.multiplyResult(int)`
    multiply all previously specified results (spoil into multiple items, spawn multiple mobs, etc.)
- `.tooltip(Component)`
    used to specify things to show in `Spoils into: ...` in the tooltip
- `.tooltip(Function<ItemStack, Component>)`
    same as `.tooltip(Component)`, but can depend on the stack

To attach a `SpoilableBehavior` to an item, you can use the `attachTo(ItemLike)` method.
It can be chained if you want to make multiple items spoil using the same behavior.

### SpoilUtils
`SpoilUtils` is a utility class with some static methods for updating items and blocks:

- `update`
    makes an item start spoiling with the specified `SpoilContext`
- `updateBlock`
    updates all items in any found `IItemHandler` capabilities of the specified block,
    the `SpoilContext` is generated automatically

!!! example
    ```java
    public class Example {
            
        // Make diamonds spoil into dirt and a dragon in 100 seconds, apples into jigsaws in 35 seconds
        @SubscribeEvent
        public static void attachSpoilables(RegisterSpoilablesEvent event) {
            event.getBuilder()
                    .ticks(20*100)
                    .result(Items.DIRT)
                    .result(EntityType.ENDER_DRAGON)
                    .build().attachTo(Items.DIAMOND);
            event.getBuilder()
                    .ticks(20*35)
                    .result(Items.JIGSAW)
                    .build().attachTo(Items.APPLE);
        }
        
        public void getAndSetValuesAndStuff(ItemStack stack) {
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            // If spoilable is null, it means the stack cannot spoil
            if (spoilable != null) {
                // Get amount of ticks until a completely fresh stack spoils
                long totalTicks = spoilable.getSpoilTicks(stack);
                // Get amount of ticks until this stack spoils (may be more than the previous value in some cases)
                long ticksRemaining = spoilable.getTicksUntilSpoiled(stack);
                // Get the stack this stack spoils into
                ItemStack spoilResult = spoilable.spoilResult(stack);
                // Get whether this stack should START spoiling
                boolean shouldStartSpoiling = spoilable.shouldSpoil(stack);
                // Get the amount of ticks until this stack spoils (may be more than spoilable.getSpoilTicks(stack))
                spoilable.setTicksUntilSpoiled(stack, 12345);
                // Freeze the spoiling progress of this stack
                spoilable.freezeSpoiling(stack);
                // Unfreeze the spoiling progress of this stack
                spoilable.unfreezeSpoiling(stack);
            }
        }
        
        public void makeStackStartSpoiling(ItemStack stack, Level level, BlockPos pos) {
            // If for some reason the stack still hasn't started spoiling, you can start the spoiling progress using this
            // That may happen if it is the result of a non-GT recipe and not a crafting result for example
            SpoilUtils.update(stack, new SpoilContext(level, pos));
        }
    
        public void disableFrozenAndNonFrozenEquality() {
            // If you want the player to have frozen stacks in their inventory, do this
            // A side effect of this is that filtering by ticks remaining until spoiled will no longer work
            SpoilUtils.FROZEN_EQUALITY = false;
        }
    }
    ```

!!! warning "Items may spoil in other mods' filters (even if they are phantom slots)."

### Frozen stacks

To freeze a stack's spoiling progress, you can use `spoilable.freezeSpoiling(stack)`, and `spoilable.unfreezeSpoiling(stack)`
to unfreeze. A frozen stack's freshness will never be changed unless `spoilable.setTicksUntilSpoiled(stack, value)` is called.
Currently, a stack is frozen only if it is in a phantom slot (in a GregTech filter).
!!! warning "`ItemHandlerHelper.canItemStacksStack` behaviour is completely different for spoilables"

    `ItemHandlerHelper.canItemStacksStack` returns `true` if:

    - Both items are not frozen and:
        - They are the same item
        - They have the same NBT
        - **Their ticks until spoiling are averaged before the equality check**
    - One of the items is frozen and:
        - They are the same item
        - They have the same non-spoilage-related NBT
        - **One of them MAY be not frozen, and they will still be equal if `ISpoilableItem.FROZEN_EQUALITY` is `true`**
        - **Their ticks until spoiling are NOT modified in any way in this method**

    !!! info "The following only applies if `ISpoilableItem.FROZEN_EQUALITY` is `true`:"
        That means that frozen and non-frozen spoilables may stack, this is done mostly to make filtering by remaining ticks possible.
        **Please prevent the player from having direct access to frozen stacks, as they could use them to bypass the spoiling system entirely.**

### Spoilables in recipes

If a GT recipe that does not have spoilable ingredients outputs a spoilable, it is outputted at full freshness.
If a GT recipe that has spoilable ingredients outputs a spoilable, it outputs it at the freshness level equal to the average
freshness of the ingredients. This can be overridden by setting `keepSpoilingProgress` (a parameter of the GTRecipe) to `false`.
<br><br>
Results of crafts in a crafting table are always outputted fully fresh.

!!! note

    Items will spoil in machine inputs, and there's no way to automatically remove items from inputs.
