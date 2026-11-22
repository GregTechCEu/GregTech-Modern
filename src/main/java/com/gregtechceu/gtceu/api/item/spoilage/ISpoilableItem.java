package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * This is a capability! {@link Item} subclasses should not implement this directly!
 * <br>
 * Spoilable items will, as the name implies, spoil (who could've thought).
 * Due to Minecraft's limitations, items will only start spoiling if:
 * <ul>
 * <li>It is in an {@link IItemHandlerModifiable}, which is exposed as a capability of a {@link BlockEntity} or
 * {@link Entity}
 * <li>It is an output of a {@link GTRecipe}</li>
 * <li>It enters a player's inventory and gets ticked at least once</li>
 * <li>It is dropped (exists as an entity)</li>
 * <li>Any other mod calls {@link SpoilUtils#update} on the item</li>
 * </ul>
 * If you are a developer of a mod that adds any other way to obtain items, that doesn't involve
 * any of the conditions above being true at any tick, consider adding compatibility with this feature :)
 * <br>
 * Items that don't start spoiling will simply not have spoilable NBT, and as a result, won't spoil,
 * until any of the above conditions become true.
 * <p>
 * Spoilable stacks will be frozen if they enter a phantom slot,
 * to prevent stacks spoiling in filters. If you are a developer of a mod that adds filters, consider calling
 * {@link ISpoilableItem#freezeSpoiling()} on stacks entering these filters for compatibility :)
 * </p>
 * <p>
 * Note that if an item is spoilable, it does not mean that it spoils in all cases, as you can override
 * {@link ISpoilableItem#shouldSpoil()}
 * in your own implementation of the {@link ISpoilableItem} interface.
 * </p>
 * <p>
 * To make an item spoilable, you can simply use {@link SpoilableBehavior#attachTo(ItemLike)}.
 * </p>
 */
public interface ISpoilableItem {

    /**
     * Get the item stack this capability is attached to.
     */
    ItemStack getStack();

    /**
     * Checks if this stack is supposed to already be spoiled, and spoils it into the
     * {@link ISpoilableItem#getSpoilResult}
     * 
     * @param createTag whether to start spoiling this stack if it didn't start spoiling yet (adds NBT)
     */
    void updateFreshness(SpoilContext spoilContext, boolean createTag);

    /**
     * Should return the amount of ticks that this item can stay fresh.
     * The result of this method shouldn't be based on the freshness of the provided stack
     */
    long getSpoilTicks();

    /**
     * @return the amount of ticks left until the provided {@link ItemStack} spoils.
     *         The ticks still reduce even when the item is unloaded, and only pause if the
     *         overworld time pauses, as all tick calculations are done with overworld tick time
     * @see ISpoilableItem#setTicksUntilSpoiled(long)
     */
    long getTicksUntilSpoiled();

    /**
     * Sets the amount of ticks left until the provided {@link ItemStack} spoils.
     * This modifies the provided stack's NBT data.
     * The provided value may be more than {@link ISpoilableItem#getSpoilTicks()}
     * 
     * @see ISpoilableItem#getTicksUntilSpoiled()
     */
    void setTicksUntilSpoiled(long value);

    /**
     * Freezes the stack's spoiling progress until it is unfrozen by
     * {@link ISpoilableItem#unfreezeSpoiling()}.
     * Frozen stacks will NOT spoil, even if {@link ISpoilableItem#getTicksUntilSpoiled()} is {@code <= 0}.
     * This method modifies the provided stack's NBT data.
     * Calls to {@link ItemHandlerHelper#canItemStacksStack(ItemStack, ItemStack)} with a frozen stack as one of the
     * arguments
     * will check equality of both stacks' {@link ISpoilableItem#getTicksUntilSpoiled()} values, as well as all
     * non-spoilage
     * related tags and the equality of the item itself.
     * 
     * @see ISpoilableItem#unfreezeSpoiling()
     */
    void freezeSpoiling();

    /**
     * Unfreezes the stack's spoiling progress. If the stack's
     * {@link ISpoilableItem#getTicksUntilSpoiled()} is {@code <= 0}, it will spoil
     * immediately after this method call.
     * This method modifies the provided stack's NBT data.
     *
     * @see ISpoilableItem#freezeSpoiling()
     */
    void unfreezeSpoiling();

    /**
     * @return whether this stack's spoiling is frozen
     */
    boolean isFrozen();

    /**
     * This function may have side effects (i.e. spawning an entity) when called with
     * {@code simulate = false}.
     *
     * @param spoilContext The spoil context, may contain the level, position, and entity.|
     * @param ticksOverdue The ticks elapsed since this item should have spoiled. An item may not spoil immediately in
     *                     some cases, e.g. if in an unloaded chunk.
     * @param simulate     True if side effects should not be performed.
     * 
     * @return the stack to replace the provided stack with when it spoils
     */
    ItemStack getSpoilResult(SpoilContext spoilContext, long ticksOverdue, boolean simulate);

    /**
     * Note: returning {@code false} in this method won't stop the item from spoiling if the spoiling NBT has already
     * been initialized
     */
    boolean shouldSpoil();

    /**
     * @return the tick on which this item started spoiling, might not actually be the creation tick in some cases
     */
    long getCreationTick();

    /**
     * Sets the tick on which this item started spoiling, modifying its spoiling progress accordingly
     * 
     * @param tick the value to set to
     */
    void setCreationTick(long tick);

    /**
     * This method averages the spoil progress of the two stacks (or, more
     * accurately, their {@link ISpoilableItem#getCreationTick()}).
     *
     * @implNote This implementation may lead to spoil progress averaging in situations other
     *           than stack merging, though I don't think this will lead to any big user-facing bugs.
     */
    default void prepareForComparisonWith(ISpoilableItem that) {
        if (this.isFrozen() || that.isFrozen()) {
            return;
        }
        long thisCreated = this.getCreationTick();
        long thatCreated = that.getCreationTick();

        if (thisCreated != thatCreated) {
            int thisCount = this.getStack().getCount(), thatCount = that.getStack().getCount();
            long average;
            if (thisCount > 0 && thatCount > 0) {
                average = (thisCreated * thisCount + thatCreated * thatCount) / (thisCount + thatCount);
            } else if (thisCount > 0) {
                average = thisCreated;
            } else {
                average = thatCreated;
            }
            this.setCreationTick(average);
            that.setCreationTick(average);
        }
    }
}
