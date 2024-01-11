package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
        tagCompound.put("pipePos", NbtUtils.writeBlockPos(nodeData.getPipePos()));
        tagCompound.putByte("faceToHandler", (byte) nodeData.faceToHandler.ordinal());
        tagCompound.putInt("distance", nodeData.distance);
        tagCompound.putByte("connections", nodeData.connections);
    }

    @Override
    protected LaserData readNodeData(CompoundTag tagCompound) {
        BlockPos pipePos = NbtUtils.readBlockPos(tagCompound.getCompound("pipePos"));
        Direction direction = Direction.values()[tagCompound.getByte("faceToHandler")];
        int distance = tagCompound.getInt("distance");
        return new LaserData(pipePos, direction, distance, LaserPipeProperties.INSTANCE, tagCompound.getByte("connections"));
    }

    @AllArgsConstructor
    public static class LaserData implements IAttachData {
        /**
         * The current position of the pipe
         */
        @Getter
        private final BlockPos pipePos;
        /**
         * The current face to handler
         */
        @Getter
        private final Direction faceToHandler;
        /**
         * The manhattan distance traveled during walking
         */
        @Getter
        private final int distance;
        /**
         * The laser pipe properties of the current pipe
         */
        @Getter
        private final LaserPipeProperties properties;
        @Getter
        byte connections;

        @Override
        public boolean canAttachTo(Direction side) {
            return (connections & (1 << side.ordinal())) != 0 && side.getAxis() == this.faceToHandler.getAxis();
        }

        @Override
        public boolean setAttached(Direction side, boolean attach) {
            var result = canAttachTo(side);
            if (result != attach) {
                if (attach) {
                    connections |= (1 << side.ordinal());
                } else {
                    connections &= ~(1 << side.ordinal());
                }
            }
            return result != attach;
        }

        /**
         * @return The position of where the handler would be
         */
        @Nonnull
        public BlockPos getHandlerPos() {
            return pipePos.relative(faceToHandler);
        }

        /**
         * Gets the handler if it exists
         * @param world the world to get the handler from
         * @return the handler
         */
        @Nullable
        public ILaserContainer getHandler(@Nonnull Level world) {
            return GTCapabilityHelper.getLaser(world, getHandlerPos(), faceToHandler.getOpposite());
        }
    }
}
