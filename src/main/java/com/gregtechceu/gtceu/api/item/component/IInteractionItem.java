package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
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

    default InteractionResult use(ItemStack item, Level level,
                                  Player player, InteractionHand usedHand) {
        FoodProperties food = item.get(DataComponents.FOOD);
        if (food != null) {
            if (player.canEat(food.canAlwaysEat())) {
                player.startUsingItem(usedHand);
                return InteractionResult.CONSUME.heldItemTransformedTo(item);
            } else {
                return InteractionResult.FAIL;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    default ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        return stack.get(DataComponents.FOOD) != null ? stack.finishUsingItem(level, livingEntity) : stack;
    }

    default ItemUseAnimation getUseAnimation(ItemStack stack) {
        return stack.getUseAnimation();
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

    default boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return false;
    }
}
