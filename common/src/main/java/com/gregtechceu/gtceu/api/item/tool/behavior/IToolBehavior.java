package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Describes generic behaviour attachable to tools. Multiple behaviours can be attached to one tool.
 */
public interface IToolBehavior {

    default void init(IGTTool toolItem) {

    }

    /**
     * @param stack    The current ItemStack
     * @param target   the entity being hit
     * @param attacker the entity hitting the other
     */
    default void hitEntity(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
    }

    /**
     * Called before a block is broken.
     * <p>
     * This is called on only the server side!
     *
     * @param stack  The current ItemStack
     * @param pos    Block's position in world
     * @param player The Player that is wielding the item
     */
    default void onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos pos, @Nonnull Player player) {
    }

    /**
     * Called when a Block is destroyed using this Item.
     *
     * @param stack        The current ItemStack
     * @param world        The current world
     * @param state        The state of the destroyed block
     * @param pos          The position of the destroyed block
     * @param entityLiving the entity destroying the block
     */
    default void onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityLiving) {
    }

    /**
     * Called when an entity tries to play the 'swing' animation.
     *
     * @param entityLiving The entity swinging the item.
     * @param stack        The Item stack
     */
    default void onEntitySwing(@Nonnull LivingEntity entityLiving, @Nonnull ItemStack stack) {
    }

    /**
     *
     * @param stack  the tool
     * @param shield the shield to disable
     * @param entity the entity holding the shield
     * @param attacker the entity attacking the shield
     * @return if the tool can disable shields
     */
    default boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return false;
    }

    /**
     * Called when a Block is right-clicked with this Item, but before the block is activated
     *
     * @param stack the stack used
     * @param context the context containing all information about the click.
     */
    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Called when a Block is right-clicked with this Item
     *
     * @param context The UseOnContext used to determine actions.
     */
    @Nonnull
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
    @Nonnull
    default InteractionResultHolder<ItemStack> onItemRightClick(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Environment(EnvType.CLIENT)
    default void addInformation(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
    }

    /**
     * Add the necessary NBT information to the tool
     * @param stack the tool
     * @param tag   the nbt tag to add to
     */
    default void addBehaviorNBT(@Nonnull ItemStack stack, @Nonnull CompoundTag tag) {
    }
}