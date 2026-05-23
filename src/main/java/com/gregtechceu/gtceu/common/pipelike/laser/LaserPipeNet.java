package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class LaserPipeNet extends PipeNet<LaserPipeProperties> {

    private final Map<BlockPos, LaserRoutePath> netData = new Object2ObjectOpenHashMap<>();

    public LaserPipeNet(LevelPipeNet<LaserPipeProperties, LaserPipeNet> world) {
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
}
