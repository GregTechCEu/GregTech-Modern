package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ExplodableMachineTrait extends MachineTrait {

    public static final MachineTraitType<ExplodableMachineTrait> TYPE = new MachineTraitType<>(ExplodableMachineTrait.class);

    private @Nullable TickableSubscription explosionSub = null;

    private boolean shouldExplodeInWeatherAndWater;
    @Getter
    @Setter
    private float explosionPower, fireChance;
    @Setter
    private Supplier<Boolean> explosionPredicate;

    public ExplodableMachineTrait(MetaMachine machine, float explosionPower, float fireChance, Supplier<Boolean> explosionPredicate) {
        super(machine);
        shouldExplodeInWeatherAndWater = true;
        this.explosionPredicate = explosionPredicate;
        this.explosionPower = explosionPower;
        this.fireChance = fireChance;
    }

    public ExplodableMachineTrait(MetaMachine machine, float explosionPower, float fireChance) {
        this(machine, explosionPower, fireChance, () -> true);
    }

    @Override
    public MachineTraitType<ExplodableMachineTrait> getTraitType() {
        return TYPE;
    }

    public boolean shouldExplodeInWeatherAndWater() {
        return shouldExplodeInWeatherAndWater;
    }

    public void setShouldExplodeInWeatherAndWater(boolean value) {
        shouldExplodeInWeatherAndWater = value;
        updateSubscription();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (!isRemote()) updateSubscription();
    }

    @Override
    public void onMachineUnload() {
        super.onMachineUnload();
    }

    private void updateSubscription() {
        if (!isRemote() && shouldExplodeInWeatherAndWater && ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion) {
            explosionSub = subscribeServerTick(explosionSub, this::checkExplosion);
        } else {
            if (explosionSub != null) explosionSub.unsubscribe();
            explosionSub = null;
        }
    }

    private void checkExplosion() {
        if (!shouldExplodeInWeatherAndWater || !explosionPredicate.get()) return;
        var level = machine.getLevel();
        var pos = getBlockPos();
        if (GTValues.RNG.nextInt(1000) == 0) {
            for (Direction side : GTUtil.DIRECTIONS) {
                var fluidState = level.getBlockState(pos.relative(side)).getFluidState();
                if (!fluidState.isEmpty()) {
                    doExplosion();
                    return;
                }
            }
        }
        if (GTValues.RNG.nextInt(1000) == 0) {
            if (level.isRainingAt(pos) || level.isRainingAt(pos.east()) || level.isRainingAt(pos.west()) ||
                    level.isRainingAt(pos.north()) || level.isRainingAt(pos.south())) {
                if (level.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                    doExplosion();
                } else if (GTValues.RNG.nextInt(10) == 0) {
                    doExplosion();
                } else setOnFire();
            }
        }
    }

    public void doExplosion() {
        doExplosion(getLevel(), getBlockPos(), explosionPower);
    }

    public void doExplosion(float power) {
        doExplosion(getLevel(), getBlockPos(), power);
    }

    public static void doExplosion(Level level, BlockPos pos, float explosionPower) {
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                explosionPower, ConfigHolder.INSTANCE.machines.doesExplosionDamagesTerrain ?
                        Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    public void setOnFire() {
        setOnFire(getLevel(), getBlockPos(), fireChance);
    }

    private static void setOnFire(Level level, BlockPos pos, double additionalFireChance) {
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
}
