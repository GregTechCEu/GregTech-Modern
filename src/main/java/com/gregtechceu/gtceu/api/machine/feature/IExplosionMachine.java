package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * A machine which may cause explosion. e.g. whether, water around
 */
public interface IExplosionMachine extends IMachineFeature {

    /**
     * should be called per tick.
     * 
     * @param explosionPower       explosion level
     * @param additionalFireChance fire chance
     */
    default void checkWeatherOrTerrainExplosion(float explosionPower, double additionalFireChance) {
        if (!shouldWeatherOrTerrainExplosion()) return;
        var machine = self();
        var level = machine.getLevel();
        var pos = machine.getPos();
        if (GTValues.RNG.nextInt(1000) == 0) {
            for (Direction side : GTUtil.DIRECTIONS) {
                var fluidState = level.getBlockState(pos.relative(side)).getFluidState();
                if (!fluidState.isEmpty()) {
                    doExplosion(explosionPower);
                    return;
                }
            }
        }
        if (GTValues.RNG.nextInt(1000) == 0) {
            if (level.isRainingAt(pos) || level.isRainingAt(pos.east()) || level.isRainingAt(pos.west()) ||
                    level.isRainingAt(pos.north()) || level.isRainingAt(pos.south())) {
                if (level.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                    doExplosion(explosionPower);
                } else if (GTValues.RNG.nextInt(10) == 0) {
                    doExplosion(explosionPower);
                } else setOnFire(additionalFireChance);
            }
        }
    }

    default void doExplosion(float explosionPower) {
        doExplosion(self().getPos(), explosionPower);
    }

    default void doExplosion(BlockPos pos, float explosionPower) {
        var machine = self();
        var level = machine.getLevel();
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                explosionPower, ConfigHolder.INSTANCE.machines.doesExplosionDamagesTerrain ?
                        Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    default void setOnFire(double additionalFireChance) {
        var machine = self();
        var level = machine.getLevel();
        var pos = machine.getPos();
        boolean isFirstFireSpawned = false;
        for (Direction side : GTUtil.DIRECTIONS) {
            if (level.isEmptyBlock(pos.relative(side))) {
                if (!isFirstFireSpawned) {
                    level.setBlock(pos.relative(side), Blocks.FIRE.defaultBlockState(), 11);
                    if (!level.isEmptyBlock(pos.relative(side))) {
                        isFirstFireSpawned = true;
                    }
                } else if (additionalFireChance >= GTValues.RNG.nextDouble() * 100) {
                    level.setBlock(pos.relative(side), Blocks.FIRE.defaultBlockState(), 11);
                }
            }
        }
    }

    default boolean shouldWeatherOrTerrainExplosion() {
        return true;
    }
}
