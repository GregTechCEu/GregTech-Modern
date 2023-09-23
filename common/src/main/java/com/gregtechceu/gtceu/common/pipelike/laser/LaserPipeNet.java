package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class LaserPipeNet extends PipeNet<LaserPipeNet.LaserData> {

    private final Map<BlockPos, LaserData> netData = new Object2ObjectOpenHashMap<>();

    public LaserPipeNet(LevelLaserPipeNet world) {
        super(world);
    }

    @Nullable
    public LaserData getNetData(BlockPos pipePos, Direction facing) {
        LaserData data = netData.get(pipePos);
        if (data == null) {
            data = LaserNetWalker.createNetData(this, pipePos, facing);
            if (data == null) {
                // walker failed, don't cache, so it tries again on next insertion
                return null;
            }

            netData.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        netData.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        netData.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<LaserData>> transferredNodes, PipeNet<LaserData> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        netData.clear();
        ((LaserPipeNet) parentNet).netData.clear();
    }

    @Override
    protected void writeNodeData(LaserData nodeData, CompoundTag tagCompound) {

    }

    @Override
    protected LaserData readNodeData(CompoundTag tagCompound) {
        return null;
    }

    public static class LaserData implements IAttachData {

        @Getter @Nonnull
        private final Direction faceToHandler;
        @Getter
        private final LaserPipeProperties properties;

        public LaserData(Direction faceToHandler, LaserPipeProperties properties) {
            this.faceToHandler = faceToHandler;
            this.properties = properties;
        }

        @Override
        public boolean canAttachTo(Direction side) {
            return faceToHandler.getAxis() == side.getAxis();
        }

        public boolean canAttachTo(Direction.Axis side) {
            return faceToHandler.getAxis() == side;
        }

        @Override
        public boolean setAttached(Direction side, boolean attach) {
            return canAttachTo(side) != attach;
        }
    }
}
