package com.gregtechceu.gtceu.core.mixins.sable;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.integration.sable.SableAssemblyRotation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Sable assembles a contraption by copying each block's saved data straight into the destination
 * sub-level, but the suppressed onPlace leaves a pipe out of its network and never turns its connection
 * mask or covers to match the angle the contraption was assembled at. From afterMove the pipe's saved
 * orientation is rotated to the assembly angle first, then its own tick is run once to join the network
 * from that corrected data; the join is guarded so an already-present node is not registered twice,
 * which would leak the net's chunk references.
 */
@Mixin(value = PipeBlock.class, remap = false)
public abstract class PipeBlockSubLevelMixin implements BlockSubLevelAssemblyListener {

    @Shadow
    public abstract void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

    @Shadow
    public abstract LevelPipeNet<?, ?> getWorldPipeNet(ServerLevel level);

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState state, BlockPos oldPos,
                          BlockPos newPos) {
        if (resultingLevel.getBlockEntity(newPos) instanceof PipeBlockEntity<?, ?> pipe) {
            SableAssemblyRotation.rotatePipe(pipe, SableAssemblyRotation.current(), resultingLevel.registryAccess());
        }
        if (getWorldPipeNet(resultingLevel).getNetFromPos(newPos) == null) {
            tick(state, resultingLevel, newPos, resultingLevel.getRandom());
        }
    }
}
