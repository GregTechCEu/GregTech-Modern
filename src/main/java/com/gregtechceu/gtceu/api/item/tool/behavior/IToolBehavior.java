package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.common.data.item.GTToolActions;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Describes generic behaviour attachable to tools. Multiple behaviours can be attached to one tool.
 */
public interface IToolBehavior {

    default void init(IGTTool toolItem) {}

    /**
     * @param stack    The current ItemStack
     * @param target   the entity being hit
     * @param attacker the entity hitting the other
     */
    default void hitEntity(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {}

    /**
     * Called before a block is broken.
     * <p>
     * This is called on only the server side!
     *
     * @param stack  The current ItemStack
     * @param pos    Block's position in world
     * @param player The Player that is wielding the item
     */
    default void onBlockStartBreak(@NotNull ItemStack stack, @NotNull BlockPos pos, @NotNull Player player) {}

    /**
     * Called when a Block is destroyed using this Item.
     *
     * @param stack        The current ItemStack
     * @param world        The current world
     * @param state        The state of the destroyed block
     * @param pos          The position of the destroyed block
     * @param entityLiving the entity destroying the block
     */
    default void onBlockDestroyed(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state,
                                  @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {}

    /**
     * Called when an entity tries to play the 'swing' animation.
     *
     * @param entityLiving The entity swinging the item.
     * @param stack        The Item stack
     */
    default void onEntitySwing(@NotNull LivingEntity entityLiving, @NotNull ItemStack stack) {}

    /**
     *
     * @param stack    the tool
     * @param shield   the shield to disable
     * @param entity   the entity holding the shield
     * @param attacker the entity attacking the shield
     * @return if the tool can disable shields
     */
    default boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return false;
    }

    /**
     * Called when a Block is right-clicked with this Item, but before the block is activated
     *
     * @param stack   the stack used
     * @param context the context containing all information about the click.
     */
    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Queries if an item can perform the given action.
     * See {@link ToolActions} for a description of each stock action and {@link GTToolActions} for GTCEu's ones
     *
     * @param stack  The stack being used
     * @param action The action being queried
     * @return {@code true} if the stack can perform the action
     */
    default boolean canPerformAction(ItemStack stack, ToolAction action) {
        return false;
    }

    /**
     * Called when a Block is right-clicked with this Item
     *
     * @param context The UseOnContext used to determine actions.
     */
    @NotNull
    default InteractionResult onItemUse(UseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Called when the equipped item is right-clicked.
     *
     * @param world  the world in which the click happened
     * @param player the player clicking the item
     * @param hand   the hand holding the item
     */
    @NotNull
    default InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level world, @NotNull Player player,
                                                                @NotNull InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    /**
     * Called when a block is right-clicked with this Item
     *
     * @param context The UseOnContext used to determine the result.
     * @return True if the UI of the MetaMachine should open after using this tool.
     */
    default boolean shouldOpenUIAfterUse(UseOnContext context) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    default void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {}

    /**
     * Add the necessary NBT information to the tool
     * 
     * @param stack the tool
     * @param tag   the nbt tag to add to
     */
    default void addBehaviorNBT(@NotNull ItemStack stack, @NotNull CompoundTag tag) {}
}
