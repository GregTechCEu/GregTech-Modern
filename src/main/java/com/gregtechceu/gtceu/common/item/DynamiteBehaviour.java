package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.entity.DynamiteEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DynamiteBehaviour implements IInteractionItem {

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);

        if (!player.isCreative()) {
            itemstack.shrink(1);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(itemstack);
        }

        DynamiteEntity entity = new DynamiteEntity(player, level);
        entity.shoot(player.getXRot(), player.getYRot(), 0.0F, 0.7F, 1.0F);

        level.addFreshEntity(entity);

        return InteractionResultHolder.success(itemstack);
    }
}