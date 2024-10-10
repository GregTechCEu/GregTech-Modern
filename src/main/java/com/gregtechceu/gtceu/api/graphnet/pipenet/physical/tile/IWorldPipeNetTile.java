package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Consumer;

public interface IWorldPipeNetTile {

    // universal (mostly for active nodes)

    @NotNull
    EnumMap<Direction, BlockEntity> getTargetsWithCapabilities(WorldPipeNetNode destination);

    @Nullable
    BlockEntity getTargetWithCapabilities(WorldPipeNetNode destination, Direction facing);

    PipeCapabilityWrapper getWrapperForNode(WorldPipeNetNode node);

    @NotNull
    ICoverable getCoverHolder();

    // fluid piping

    void spawnParticles(Direction direction, ParticleOptions particleType, int particleCount);

    void dealAreaDamage(int size, Consumer<LivingEntity> damageFunction);

    void playLossSound();

    void visuallyExplode();

    void setNeighborsToFire();
}
