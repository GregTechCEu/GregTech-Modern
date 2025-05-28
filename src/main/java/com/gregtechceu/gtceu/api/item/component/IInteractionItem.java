package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public interface IInteractionItem extends IItemComponent {

    default InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (item.isEdible()) {
            ItemStack itemStack = player.getItemInHand(usedHand);
            if (player.canEat(itemStack.getFoodProperties(player).canAlwaysEat())) {
                player.startUsingItem(usedHand);
                return InteractionResultHolder.consume(itemStack);
            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }
    }

    default ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        return stack.isEdible() ? livingEntity.eat(level, stack) : stack;
    }

    default UseAnim getUseAnimation(ItemStack stack) {
        return stack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
    }

    default boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    default InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                   InteractionHand usedHand) {
        return InteractionResult.PASS;
    }

    default boolean sneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    default boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }
}
