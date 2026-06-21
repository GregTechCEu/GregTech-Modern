package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

import lombok.Getter;
import lombok.Setter;

public class ExhaustVentMachineTrait extends MachineTrait {

    public static final MachineTraitType<ExhaustVentMachineTrait> TYPE = new MachineTraitType<>(
            ExhaustVentMachineTrait.class, false);

    @Getter
    @Setter
    private Direction ventingDirection = Direction.UP;
    @Getter
    @Setter
    @SaveField
    private boolean needsVenting;
    @Getter
    @Setter
    private float ventingDamageAmount;

    public ExhaustVentMachineTrait() {
        super();

        this.needsVenting = false;
        this.ventingDamageAmount = 0;
    }

    @Override
    public MachineTraitType<ExhaustVentMachineTrait> getTraitType() {
        return TYPE;
    }

    @Override
    public void onMachineLoad() {
        this.ventingDirection = getMachine().getFrontFacing().getOpposite();
    }

    public boolean isVentingBlocked() {
        BlockPos ventingBlockPos = getBlockPos().relative(getVentingDirection());
        BlockState state = getLevel().getBlockState(ventingBlockPos);

        return state.canOcclude() || Shapes.blockOccudes(state.getCollisionShape(getLevel(), ventingBlockPos),
                Shapes.block(), getVentingDirection().getOpposite());
    }

    public boolean checkVenting() {
        if (!needsVenting) return true;

        if (!isVentingBlocked()) {
            performVenting();
            return true;
        }

        BlockPos ventingPos = getBlockPos().relative(getVentingDirection());
        if (GTUtil.tryBreakSnow(getLevel(), ventingPos, getLevel().getBlockState(ventingPos), false)) {
            performVenting();
            return true;
        }
        return false;
    }

    private void performVenting() {
        doVentingDamage();
        createVentingParticles();
        if (ConfigHolder.INSTANCE.machines.machineSounds) {
            playVentingSound();
        }
        needsVenting = false;
    }

    private void doVentingDamage() {
        for (LivingEntity entity : getLevel().getEntitiesOfClass(LivingEntity.class,
                new AABB(getBlockPos().relative(getVentingDirection())),
                entity -> !(entity instanceof Player player) || !player.isSpectator() && !player.isCreative())) {
            entity.hurt(GTDamageTypes.HEAT.source(getLevel()), ventingDamageAmount);
        }
    }

    private void createVentingParticles() {
        var level = getLevel();
        var pos = getBlockPos();
        Direction ventingDirection = getVentingDirection();
        double posX = pos.getX() + 0.5 + ventingDirection.getStepX() * 0.6;
        double posY = pos.getY() + 0.5 + ventingDirection.getStepY() * 0.6;
        double posZ = pos.getZ() + 0.5 + ventingDirection.getStepZ() * 0.6;
        var count = 7 + level.random.nextInt(3);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, posX, posY, posZ,
                    count,
                    ventingDirection.getStepX() / 2.0,
                    ventingDirection.getStepY() / 2.0,
                    ventingDirection.getStepZ() / 2.0, 0.1);
        } else {
            for (int i = 0; i < count; ++i) {
                double d1 = level.random.nextGaussian() * (double) ventingDirection.getStepX() / 2.0;
                double d3 = level.random.nextGaussian() * (double) ventingDirection.getStepY() / 2.0;
                double d5 = level.random.nextGaussian() * (double) ventingDirection.getStepZ() / 2.0;
                double d6 = level.random.nextGaussian() * 0.1;
                double d7 = level.random.nextGaussian() * 0.1;
                double d8 = level.random.nextGaussian() * 0.1;
                try {
                    level.addParticle(ParticleTypes.CLOUD, posX + d1, posY + d3, posZ + d5, d6, d7, d8);
                    continue;
                } catch (Throwable throwable) {
                    GTCEu.LOGGER.warn("Could not spawn particle effect {}", ParticleTypes.CLOUD);
                    return;
                }
            }
        }
    }

    private void playVentingSound() {
        var pos = getBlockPos();
        var level = getLevel();
        double posX = pos.getX() + 0.5 + ventingDirection.getStepX() * 0.6;
        double posY = pos.getY() + 0.5 + ventingDirection.getStepY() * 0.6;
        double posZ = pos.getZ() + 0.5 + ventingDirection.getStepZ() * 0.6;
        level.playSound(null, posX, posY, posZ, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
    }
}
