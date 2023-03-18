package com.lowdragmc.gtceu.api.machine.feature.multiblock;

import com.lowdragmc.gtceu.api.GTValues;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;

public interface IMufflerMachine extends IMultiPart {

    void recoverItemsTable(ItemStack... recoveryItems);

    /**
     * @return true if front face is free and contains only air blocks in 1x1 area
     */
    default boolean isFrontFaceFree() {
        var frontPos = self().getPos().relative(self().getFrontFacing());
        return self().getLevel().getBlockState(frontPos).isAir();
    }

     default void emitPollutionParticles() {
        var pos = self().getPos();
        var facing = self().getFrontFacing();
        float xPos = facing.getStepX() * 0.76F + pos.getX() + 0.25F;
        float yPos = facing.getStepY() * 0.76F + pos.getY() + 0.25F;
        float zPos = facing.getStepZ() * 0.76F + pos.getZ() + 0.25F;

        float ySpd = facing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
        float xSpd;
        float zSpd;

        if (facing.getStepY() == -1) {
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            xSpd = (float) Math.sin(temp) * 0.1F;
            zSpd = (float) Math.cos(temp) * 0.1F;
        } else {
            xSpd = facing.getStepX() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
            zSpd = facing.getStepZ() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
        }
        self().getLevel().addParticle(ParticleTypes.LARGE_SMOKE,
                xPos + GTValues.RNG.nextFloat() * 0.5F,
                yPos + GTValues.RNG.nextFloat() * 0.5F,
                zPos + GTValues.RNG.nextFloat() * 0.5F,
                xSpd, ySpd, zSpd);

    }
}
