package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.data.damagesource.DamageSources;
import com.gregtechceu.gtceu.config.ConfigHolder;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;

/**
 * Interface defining the functionality of a machine which vents exhaust from a side.
 *
 * @implNote {@link com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine}
 */
public interface IExhaustVentMachine extends IMachineFeature {

    /**
     * @return the direction the vent faces
     */
    @NotNull Direction getVentingDirection();

    /**
     * @return if venting is needed
     */
    boolean needsVenting();

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
     * <strong>Server-Side Only.</strong>
     *
     * @return if the machine does not need venting
     */
    default boolean checkVenting() {
        if (needsVenting()) {
            if (self().getLevel() instanceof ServerLevel serverLevel) {
                tryDoVenting(serverLevel, self().getPos());
            }
        }
        return !needsVenting();
    }

    /**
     * @return if venting is being blocked by something
     */
    default boolean isVentingBlocked() {
        Level level = self().getLevel();
        Direction ventingSide = getVentingDirection();
        BlockPos ventingBlockPos = self().getPos().relative(ventingSide);
        BlockState state = level.getBlockState(ventingBlockPos);

        return state.canOcclude() || Shapes.blockOccudes(state.getCollisionShape(level, ventingBlockPos), Shapes.block(), ventingSide.getOpposite());
    }

    /**
     * Attempts to vent, if needed
     *
     * @param serverLevel the level containing the machine venting
     * @param pos the position of the machine
     */
    default void tryDoVenting(@NotNull ServerLevel serverLevel, @NotNull BlockPos pos) {
        if (needsVenting() && !isVentingBlocked()) {
            doVentingDamage(serverLevel, pos);

            Direction ventingDirection = getVentingDirection();
            double posX = pos.getX() + 0.5 + ventingDirection.getStepX() * 0.6;
            double posY = pos.getY() + 0.5 + ventingDirection.getStepY() * 0.6;
            double posZ = pos.getZ() + 0.5 + ventingDirection.getStepZ() * 0.6;
            createVentingParticles(serverLevel, posX, posY, posZ);

            if (ConfigHolder.INSTANCE.machines.machineSounds) {
                playVentingSound(serverLevel, posX, posY, posZ);
            }
            markVentingComplete();
        }
    }

    /**
     * Damages entities upon venting
     *
     * @param serverLevel the level containing the machine and entities
     * @param pos the position of the machine venting
     */
    default void doVentingDamage(@NotNull ServerLevel serverLevel, @NotNull BlockPos pos) {
        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos.relative(getVentingDirection())),
                entity -> !(entity instanceof Player player) || !player.isSpectator() && !player.isCreative())) {
            entity.hurt(DamageSources.getHeatDamage(), getVentingDamage());
            // TODO ADVANCEMENT
//            if (entity instanceof ServerPlayer) {
//                AdvancementTriggers.STEAM_VENT_DEATH.trigger((ServerPlayer) entity);
//            }
        }
    }

    /**
     * Create the particles for venting
     *
     * @param serverLevel the level containing the machine
     * @param posX the x position to send particles to
     * @param posY the y position to send particles to
     * @param posZ the z position to send particles to
     */
    default void createVentingParticles(@NotNull ServerLevel serverLevel, double posX, double posY, double posZ) {
        Direction ventingDirection = getVentingDirection();
        serverLevel.sendParticles(ParticleTypes.CLOUD, posX, posY, posZ,
                7 + serverLevel.random.nextInt(3),
                ventingDirection.getStepX() / 2.0,
                ventingDirection.getStepY() / 2.0,
                ventingDirection.getStepZ() / 2.0, 0.1);
    }

    /**
     * Play the venting sound
     *
     * @param serverLevel the level to play the sound in
     * @param posX the x position to play the sound at
     * @param posY the y position to play the sound at
     * @param posZ the z position to play the sound at
     */
    default void playVentingSound(@NotNull ServerLevel serverLevel, double posX, double posY, double posZ) {
        serverLevel.playSound(null, posX, posY, posZ, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
    }
}
