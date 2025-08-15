package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

import org.jetbrains.annotations.NotNull;

/**
 * Interface defining the functionality of a machine which vents exhaust from a side.
 *
 * @implNote {@link com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine}
 */
public interface IExhaustVentMachine extends IMachineFeature {

    EnumProperty<RelativeDirection> VENT_DIRECTION_PROPERTY = GTMachineModelProperties.VENT_DIRECTION;

    /**
     * @return the direction the vent faces
     */
    @NotNull
    Direction getVentingDirection();

    /**
     * @return if venting is needed
     */
    boolean isNeedsVenting();

    void setNeedsVenting(boolean needsVenting);

    /**
     * Mark the machine as no longer needing venting
     */
    void markVentingComplete();

    /**
     * @return the damage to deal to entities in the vent area
     */
    float getVentingDamage();

    /**
     * Checks the venting state. Performs venting only if required.
     *
     * @return if the machine does not need venting
     */
    default boolean checkVenting() {
        if (isNeedsVenting()) {
            tryDoVenting(self().getLevel(), self().getPos());
        }
        return !isNeedsVenting();
    }

    /**
     * @return if venting is being blocked by something
     */
    default boolean isVentingBlocked() {
        Level level = self().getLevel();
        Direction ventingSide = getVentingDirection();
        BlockPos ventingBlockPos = self().getPos().relative(ventingSide);
        BlockState state = level.getBlockState(ventingBlockPos);

        return state.canOcclude() || Shapes.blockOccudes(state.getCollisionShape(level, ventingBlockPos),
                Shapes.block(), ventingSide.getOpposite());
    }

    /**
     * Attempts to vent, if needed
     *
     * @param level the level containing the machine venting
     * @param pos   the position of the machine
     */
    default void tryDoVenting(@NotNull Level level, @NotNull BlockPos pos) {
        if (!isNeedsVenting()) return;

        if (!isVentingBlocked()) {
            performVenting(level, pos);
            return;
        }

        BlockPos ventingPos = pos.relative(getVentingDirection());
        if (GTUtil.tryBreakSnow(level, ventingPos, level.getBlockState(ventingPos), false)) {
            performVenting(level, pos);
        }
    }

    private void performVenting(@NotNull Level level, @NotNull BlockPos pos) {
        doVentingDamage(level, pos);

        Direction ventingDirection = getVentingDirection();
        double posX = pos.getX() + 0.5 + ventingDirection.getStepX() * 0.6;
        double posY = pos.getY() + 0.5 + ventingDirection.getStepY() * 0.6;
        double posZ = pos.getZ() + 0.5 + ventingDirection.getStepZ() * 0.6;
        createVentingParticles(level, posX, posY, posZ);

        if (ConfigHolder.INSTANCE.machines.machineSounds) {
            playVentingSound(level, posX, posY, posZ);
        }

        markVentingComplete();
    }

    /**
     * Damages entities upon venting
     *
     * @param level the level containing the machine and entities
     * @param pos   the position of the machine venting
     */
    default void doVentingDamage(@NotNull Level level, @NotNull BlockPos pos) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos.relative(getVentingDirection())),
                entity -> !(entity instanceof Player player) || !player.isSpectator() && !player.isCreative())) {
            entity.hurt(GTDamageTypes.HEAT.source(level), getVentingDamage());
            // TODO ADVANCEMENT
            // if (entity instanceof ServerPlayer) {
            // AdvancementTriggers.STEAM_VENT_DEATH.trigger((ServerPlayer) entity);
            // }
        }
    }

    /**
     * Create the particles for venting
     *
     * @param level the level containing the machine
     * @param posX  the x position to send particles to
     * @param posY  the y position to send particles to
     * @param posZ  the z position to send particles to
     */
    default void createVentingParticles(@NotNull Level level, double posX, double posY, double posZ) {
        Direction ventingDirection = getVentingDirection();
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

    /**
     * Play the venting sound
     *
     * @param level the level to play the sound in
     * @param posX  the x position to play the sound at
     * @param posY  the y position to play the sound at
     * @param posZ  the z position to play the sound at
     */
    default void playVentingSound(@NotNull Level level, double posX, double posY, double posZ) {
        level.playSound(null, posX, posY, posZ, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
    }
}
