package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote IInteractionItem
 */
public interface IInteractionItem extends IItemComponent {
    default InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (item.isEdible()) {
            ItemStack itemStack = player.getItemInHand(usedHand);
            if (player.canEat(item.getFoodProperties().canAlwaysEat())) {
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

    default InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return InteractionResult.PASS;
    }
}
