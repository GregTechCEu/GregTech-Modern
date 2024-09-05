package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.graphnet.MultiNodeHelper;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.worldnet.WorldPosNetNode;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class WorldPipeNetNode extends WorldPosNetNode {

    private static final PipeBlockEntity FALLBACK = new PipeBlockEntity(GTBlockEntities.PIPE.get(), BlockPos.ZERO,
            GTBlocks.MATERIAL_PIPE_BLOCKS.get(TagPrefix.pipeNormal, GTMaterials.Aluminium).getDefaultState());

    @Nullable
    MultiNodeHelper overlapHelper;

    private WeakReference<IWorldPipeNetTile> tileReference;

    public WorldPipeNetNode(WorldPipeNet net) {
        super(net);
    }

    public @NotNull IWorldPipeNetTile getBlockEntity() {
        IWorldPipeNetTile tile = getBlockEntity(true);
        if (tile == null) {
            // something went very wrong, return the fallback to prevent NPEs and remove us from the net.
            getNet().removeNode(this);
            tile = FALLBACK;
        }
        return tile;
    }

    @Nullable
    public IWorldPipeNetTile getBlockEntityNoLoading() {
        return getBlockEntity(false);
    }

    private IWorldPipeNetTile getBlockEntity(boolean allowLoading) {
        if (tileReference != null) {
            IWorldPipeNetTile tile = tileReference.get();
            if (tile != null) return tile;
        }
        Level level = getNet().getLevel();
        if (!allowLoading && !level.isLoaded(getEquivalencyData())) return null;
        BlockEntity tile = level.getBlockEntity(getEquivalencyData());
        if (tile instanceof IWorldPipeNetTile pipe) {
            this.tileReference = new WeakReference<>(pipe);
            return pipe;
        } else return null;
    }

    @Override
    public void onRemove() {
        if (this.overlapHelper != null) {
            this.overlapHelper.removeNode(this);
            this.overlapHelper = null;
        }
    }

    @Override
    public @NotNull WorldPipeNet getNet() {
        return (WorldPipeNet) super.getNet();
    }

    @Override
    public WorldPipeNetNode setPos(BlockPos pos) {
        super.setPos(pos);
        this.getNet().synchronizeNode(this);
        return this;
    }

    @Override
    public boolean traverse(long queryTick, boolean simulate) {
        if (overlapHelper != null) {
            return overlapHelper.traverse(this.getNet(), queryTick, simulate);
        } else return true;
    }
}
