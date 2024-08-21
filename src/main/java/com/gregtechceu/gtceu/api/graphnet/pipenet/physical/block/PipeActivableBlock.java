package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeActivableBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.ActivablePipeModel;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public abstract class PipeActivableBlock extends PipeBlock {

    public PipeActivableBlock(IPipeStructure structure) {
        super(structure);
    }

    @Override
    protected @NotNull BlockStateContainer.Builder constructState(BlockStateContainer.@NotNull Builder builder) {
        return super.constructState(builder).add(ActivablePipeModel.ACTIVE_PROPERTY);
    }

    @Override
    public Class<? extends PipeActivableBlockEntity> getTileClass(@NotNull Level world, @NotNull BlockState state) {
        return PipeActivableBlockEntity.class;
    }

    @Override
    public @Nullable PipeActivableBlockEntity getBlockEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (GTUtility.arePosEqual(lastTilePos.get(), pos)) {
            PipeBlockEntity tile = lastTile.get().get();
            if (tile != null && !tile.isInvalid()) return (PipeActivableBlockEntity) tile;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof PipeActivableBlockEntity pipe) {
            lastTilePos.set(pos.toImmutable());
            lastTile.set(new WeakReference<>(pipe));
            return pipe;
        } else return null;
    }
}
