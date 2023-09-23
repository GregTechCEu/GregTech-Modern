package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalDataAccessHatch;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class OpticalPipeNet extends PipeNet<OpticalPipeData> {

    private final Map<BlockPos, OpticalInventory> NET_DATA = new Object2ObjectOpenHashMap<>();

    public OpticalPipeNet(LevelPipeNet<OpticalPipeData, ? extends PipeNet<OpticalPipeData>> world) {
        super(world);
    }

    @Nullable
    public OpticalInventory getNetData(BlockPos pipePos, Direction facing) {
        OpticalInventory data = NET_DATA.get(pipePos);
        if (data == null) {
            data = OpticalNetWalker.createNetData(this, pipePos, facing);
            if (data == null) {
                // walker failed, don't cache, so it tries again on next insertion
                return null;
            }

            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<OpticalPipeData>> transferredNodes, PipeNet<OpticalPipeData> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((OpticalPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(OpticalPipeData nodeData, CompoundTag tagCompound) {

    }

    @Override
    protected OpticalPipeData readNodeData(CompoundTag tagCompound) {
        return new OpticalPipeData();
    }

    public static class OpticalInventory {

        @Getter
        private final BlockPos pipePos;
        @Getter
        private final Direction faceToHandler;
        @Getter
        private final int distance;
        @Getter
        private final OpticalPipeData properties;

        public OpticalInventory(BlockPos pipePos, Direction faceToHandler, int distance, OpticalPipeData properties) {
            this.pipePos = pipePos;
            this.faceToHandler = faceToHandler;
            this.distance = distance;
            this.properties = properties;
        }

        public BlockPos getHandlerPos() {
            return pipePos.relative(faceToHandler);
        }

        @Nullable
        public IOpticalDataAccessHatch getDataHatch(@Nonnull Level world) {
            BlockEntity tile = world.getBlockEntity(getHandlerPos());
            if (tile != null) {
                IDataAccessHatch hatch = GTCapabilityHelper.getDataAccess(world, getHandlerPos(), faceToHandler.getOpposite());
                return hatch instanceof IOpticalDataAccessHatch opticalHatch ? opticalHatch : null;
            }
            return null;
        }

        @Nullable
        public IOpticalComputationProvider getComputationHatch(@Nonnull Level world) {
            BlockEntity tile = world.getBlockEntity(getHandlerPos());
            if (tile != null) {
                return GTCapabilityHelper.getComputationProvider(world, getHandlerPos(), faceToHandler.getOpposite());
            }
            return null;
        }
    }
}
