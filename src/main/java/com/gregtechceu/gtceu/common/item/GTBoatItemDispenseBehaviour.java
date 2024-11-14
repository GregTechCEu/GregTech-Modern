package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.common.entity.GTBoat;
import com.gregtechceu.gtceu.common.entity.GTChestBoat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class GTBoatItemDispenseBehaviour extends DefaultDispenseItemBehavior {

    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final GTBoat.BoatType type;
    private final boolean isChestBoat;

    public GTBoatItemDispenseBehaviour(GTBoat.BoatType type) {
        this(type, false);
    }

    public GTBoatItemDispenseBehaviour(GTBoat.BoatType type, boolean isChestBoat) {
        this.type = type;
        this.isChestBoat = isChestBoat;
    }

    public ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = (Direction) source.getBlockState().getValue(DispenserBlock.FACING);
        Level level = source.getLevel();
        double d0 = 0.5625 + (double) EntityType.BOAT.getWidth() / 2.0;
        double d1 = source.x() + (double) direction.getStepX() * d0;
        double d2 = source.y() + (double) ((float) direction.getStepY() * 1.125F);
        double d3 = source.z() + (double) direction.getStepZ() * d0;
        BlockPos blockpos = source.getPos().relative(direction);

        Boat boat;
        if (isChestBoat) {
            boat = new GTChestBoat(level, d0, d1, d2);
            ((GTChestBoat) boat).setBoatType(type);
        } else {
            boat = new GTBoat(level, d0, d1, d2);
            ((GTBoat) boat).setBoatType(type);
        }

        boat.setYRot(direction.toYRot());
        double d4;
        if (((Boat) boat).canBoatInFluid(level.getFluidState(blockpos))) {
            d4 = 1.0;
        } else {
            if (!level.getBlockState(blockpos).isAir() ||
                    !((Boat) boat).canBoatInFluid(level.getFluidState(blockpos.below()))) {
                return this.defaultDispenseItemBehavior.dispense(source, stack);
            }

            d4 = 0.0;
        }

        ((Boat) boat).setPos(d1, d2 + d4, d3);
        level.addFreshEntity((Entity) boat);
        stack.shrink(1);
        return stack;
    }

    protected void playSound(BlockSource source) {
        source.getLevel().levelEvent(1000, source.getPos(), 0);
    }
}
