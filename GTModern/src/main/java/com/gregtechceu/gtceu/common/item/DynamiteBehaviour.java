package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.entity.DynamiteEntity;

import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DynamiteBehaviour implements IInteractionItem {

    @Override
    public void onAttached(Item item) {
        DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {

            @Override
            protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                return Util.make(new DynamiteEntity(position.x(), position.y(), position.z(), level),
                        entity -> entity.setItem(stack));
            }
        });
    }

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
        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.7F, 1.0F);

        level.addFreshEntity(entity);

        return InteractionResultHolder.success(itemstack);
    }
}
