package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import gregtech.api.cover.CoverableView;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumParticleTypes;

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

    void spawnParticles(Direction direction, EnumParticleTypes particleType, int particleCount);

    void dealAreaDamage(int size, Consumer<EntityLivingBase> damageFunction);

    void playLossSound();

    void visuallyExplode();

    void setNeighborsToFire();
}
