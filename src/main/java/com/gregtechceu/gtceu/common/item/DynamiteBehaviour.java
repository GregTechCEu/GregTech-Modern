package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.entity.DynamiteEntity;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class DynamiteBehaviour implements IInteractionItem {

    @Override
    public void onAttached(Item item) {
        DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior() {

            @Override
            public ItemStack execute(BlockSource source, ItemStack stack) {
                Vec3 position = source.center();
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                DynamiteEntity dynamite = Util.make(new DynamiteEntity(position.x(), position.y(), position.z(), source.level()), entity -> entity.setItem(stack));
                dynamite.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 1.1F, 6.0F);
                source.level().addFreshEntity(dynamite);
                stack.shrink(1);
                return stack;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player, InteractionHand usedHand) {
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