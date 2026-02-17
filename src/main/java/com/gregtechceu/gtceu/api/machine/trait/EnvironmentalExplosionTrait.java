package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class EnvironmentalExplosionTrait extends MachineTrait {

    public static final MachineTraitType<EnvironmentalExplosionTrait> TYPE = new MachineTraitType<>(
            EnvironmentalExplosionTrait.class);

    private @Nullable TickableSubscription explosionSub = null;

    private boolean enableEnvironmentalExplosions;
    @Getter
    @Setter
    private float explosionPower, fireChance;
    @Setter
    private BooleanSupplier explosionPredicate;

    public EnvironmentalExplosionTrait(MetaMachine machine, float explosionPower, float fireChance,
                                       BooleanSupplier explosionPredicate) {
        super(machine);
        enableEnvironmentalExplosions = true;
        this.explosionPredicate = explosionPredicate;
        this.explosionPower = explosionPower;
        this.fireChance = fireChance;
    }

    public EnvironmentalExplosionTrait(MetaMachine machine, float explosionPower, float fireChance) {
        this(machine, explosionPower, fireChance, () -> true);
    }

    @Override
    public MachineTraitType<EnvironmentalExplosionTrait> getTraitType() {
        return TYPE;
    }

    public boolean enableEnvironmentalExplosions() {
        return enableEnvironmentalExplosions;
    }

    public void setEnableEnvironmentalExplosions(boolean value) {
        enableEnvironmentalExplosions = value;
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
        if (!isRemote() && enableEnvironmentalExplosions &&
                ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion) {
            explosionSub = subscribeServerTick(explosionSub, this::checkEnvironment);
        } else {
            if (explosionSub != null) explosionSub.unsubscribe();
            explosionSub = null;
        }
    }

    private void checkEnvironment() {
        if (!enableEnvironmentalExplosions || !explosionPredicate.getAsBoolean()) return;
        var level = machine.getLevel();
        var pos = getBlockPos();
        if (GTValues.RNG.nextInt(1000) == 0) {
            for (Direction side : GTUtil.DIRECTIONS) {
                var fluidState = level.getBlockState(pos.relative(side)).getFluidState();
                if (!fluidState.isEmpty()) {
                    GTUtil.doExplosion(level, pos, explosionPower);
                    return;
                }
            }
        }
        if (level.isRainingAt(pos) || level.isRainingAt(pos.east()) || level.isRainingAt(pos.west()) ||
                level.isRainingAt(pos.north()) || level.isRainingAt(pos.south())) {
            if (level.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                if (GTValues.RNG.nextInt(1000) == 0) GTUtil.doExplosion(level, pos, explosionPower);
            } else if (GTValues.RNG.nextInt(10) == 0) {
                if (GTValues.RNG.nextInt(1000) == 0) GTUtil.doExplosion(level, pos, explosionPower);
            } else if (GTValues.RNG.nextInt(1000) == 0) GTUtil.setOnFire(level, pos, fireChance);
        }
    }
}
