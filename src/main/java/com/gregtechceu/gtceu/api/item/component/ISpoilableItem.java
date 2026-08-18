package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.item.SpoilableItemStack;
import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Optional;

/**
 * This is a capability! {@link Item} subclasses should not implement this directly!
 * <br>
 * Spoilable items will, as the name implies, spoil (who could've thought).
 * Due to Minecraft's limitations, items will only start spoiling only if:
 * <ul>
 * <li>It is in an {@link IItemHandler}, which is a capability of a {@link BlockEntity} that had
 * {@link Level#getBlockEntity(BlockPos)} called (should cover most cases)</li>
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
 * The only exception is if one of the stacks is frozen, in which case it works as normal, except that
 * frozen stacks will be equal to non-frozen stacks if all else is equal. This is done to make filtering work correctly.
 * </p>
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
 * <br>
 * If you want to implement this interface yourself, please note that {@link SpoilableItemStack}
 * calls a mixin method in its {@link ISpoilableItem#updateFreshness} implementation.
 * </p>
 */
public interface ISpoilableItem {

    /**
     * Checks if this stack is supposed to already be spoiled, and spoils it into the
     * {@link ISpoilableItem#spoilResult}
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
     * @return the stack to replace the provided stack with when it spoils
     */
    ItemStack spoilResult(SpoilContext spoilContext, boolean simulate);

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
     * Called when {@link ItemHandlerHelper#canItemStacksStack(ItemStack, ItemStack)} is called.
     * If this returns an empty optional, {@link ItemHandlerHelper#canItemStacksStack(ItemStack, ItemStack)} will return
     * its
     * normal value, otherwise it will return the same value as this method.
     * <br>
     * This exists mostly for custom spoilable merging logic.
     *
     * @return whether these two stacks should be considered equal
     */
    default Optional<Boolean> isEqualTo(ItemStack other) {
        return Optional.empty();
    };
}
