package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class LaserPipeNet extends PipeNet<LaserPipeProperties> {

    private final Map<BlockPos, LaserRoutePath> netData = new Object2ObjectOpenHashMap<>();

    public LaserPipeNet(LevelLaserPipeNet world) {
        super(world);
    }

    @Nullable
    public LaserRoutePath getNetData(BlockPos pipePos, Direction facing) {
        LaserRoutePath data = netData.get(pipePos);
        if (data == null) {
            data = LaserNetWalker.createNetData(this, pipePos, facing);
            if (data == LaserNetWalker.FAILED_MARKER) {
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
    protected void writeNodeData(LaserPipeProperties laserPipeProperties, CompoundTag compoundTag) {}

    @Override
    protected LaserPipeProperties readNodeData(CompoundTag tagCompound) {
        return LaserPipeProperties.INSTANCE;
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
        @NotNull
        public BlockPos getHandlerPos() {
            return pipePos.relative(faceToHandler);
        }

        /**
         * Gets the handler if it exists
         * 
         * @param world the world to get the handler from
         * @return the handler
         */
        @Nullable
        public ILaserContainer getHandler(@NotNull Level world) {
            return GTCapabilityHelper.getLaser(world, getHandlerPos(), faceToHandler.getOpposite());
        }
    }
}
