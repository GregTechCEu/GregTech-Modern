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
 * sub-level. That copy leaves a pipe out of its network, never turns its connection mask or covers to
 * the angle the contraption was assembled at, and restores the saved fields without marking them for
 * sync so the client keeps drawing the old shape. From afterMove the saved orientation is rotated to
 * the assembly angle, the client is told to resend and redraw, and the pipe's own tick is run once to
 * join the network from the corrected data. The join is guarded so an already present node is not
 * registered twice, which would leak the net's chunk references.
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
            pipe.getSyncDataHolder().resyncAllFields();
            pipe.scheduleRenderUpdate();
        }
        if (getWorldPipeNet(resultingLevel).getNetFromPos(newPos) == null) {
            tick(state, resultingLevel, newPos, resultingLevel.getRandom());
        }
    }
}
